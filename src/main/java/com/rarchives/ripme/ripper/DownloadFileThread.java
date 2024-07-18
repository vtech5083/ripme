package com.rarchives.ripme.ripper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import com.rarchives.ripme.utils.Utils;

/**
 * Thread for downloading files.
 * Includes retry logic, observer notifications, and other goodies.
 */
public class DownloadFileThread extends Thread {

    private static final Logger logger = Logger.getLogger(DownloadFileThread.class);

    private String referrer = "";
    private Map<String,String> cookies = new HashMap<String,String>();

    private URL url;
    private File saveAs;
    private String prettySaveAs;
    private AbstractRipper observer;
    private int retries;

    private final int TIMEOUT;

    public DownloadFileThread(URL url, File saveAs, AbstractRipper observer) {
        super();
        this.url = url;
        this.saveAs = saveAs;
        this.prettySaveAs = Utils.removeCWD(saveAs);
        this.observer = observer;
        this.retries = Utils.getConfigInteger("download.retries", 1);
        this.TIMEOUT = Utils.getConfigInteger("download.timeout", 60000);
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }
    public void setCookies(Map<String,String> cookies) {
        this.cookies = cookies;
    }

    public void run() {
        if (!shouldDownload()) {
            return;
        }

        int tries = 0;
        boolean redirected = false;
        URL urlToDownload = this.url;

        while (tries <= this.retries) {
            tries++;
            try {
                HttpURLConnection huc = setupConnection(urlToDownload);
                int statusCode = huc.getResponseCode();

                if (isRedirect(statusCode)) {
                    urlToDownload = handleRedirect(huc, redirected);
                    redirected = true;
                    tries--;
                    continue;
                }

                if (isClientError(statusCode)) {
                    handleClientError(statusCode);
                    return;
                }

                if (isServerError(statusCode)) {
                    handleServerError(statusCode);
                    continue;
                }

                if (isImgurNotFound(huc, urlToDownload)) {
                    handleImgurNotFound();
                    return;
                }

                downloadFile(huc);
                observer.downloadCompleted(url, saveAs);
                logger.info("[+] Saved " + url + " as " + this.prettySaveAs);
                return;

            } catch (IOException e) {
                handleDownloadException(e, tries);
            }
        }

        handleExceededRetries();
    }

    private boolean shouldDownload() {
        try {
            observer.stopCheck();
        } catch (IOException e) {
            observer.downloadErrored(url, "Download interrupted");
            return false;
        }

        if (saveAs.exists() && !Utils.getConfigBoolean("file.overwrite", false)) {
            logger.info("[!] Skipping " + url + " -- file already exists: " + prettySaveAs);
            observer.downloadExists(url, saveAs);
            return false;
        }

        if (saveAs.exists()) {
            logger.info("[!] Deleting existing file" + prettySaveAs);
            saveAs.delete();
        }

        return true;
    }

    private HttpURLConnection setupConnection(URL urlToDownload) throws IOException {
        HttpURLConnection huc = (HttpURLConnection) urlToDownload.openConnection();
        huc.setInstanceFollowRedirects(true);
        huc.setConnectTimeout(TIMEOUT);
        huc.setRequestProperty("accept", "*/*");
        if (!referrer.isEmpty()) {
            huc.setRequestProperty("Referer", referrer);
        }
        huc.setRequestProperty("User-agent", AbstractRipper.USER_AGENT);
        setCookies(huc);
        huc.connect();
        return huc;
    }

    private void setCookies(HttpURLConnection huc) {
        StringBuilder cookieBuilder = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            if (!first) {
                cookieBuilder.append("; ");
            }
            cookieBuilder.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }
        String cookie = cookieBuilder.toString();
        if (!cookie.isEmpty()) {
            huc.setRequestProperty("Cookie", cookie);
        }
    }

    private boolean isRedirect(int statusCode) {
        return statusCode / 100 == 3;
    }

    private URL handleRedirect(HttpURLConnection huc, boolean redirected) throws IOException {
        String location = huc.getHeaderField("Location");
        URL newUrl = new URL(location);
        throw new IOException("Redirect status code " + huc.getResponseCode() + " - redirect to " + location);
    }

    private boolean isClientError(int statusCode) {
        return statusCode / 100 == 4;
    }

    private void handleClientError(int statusCode) {
        logger.error("[!] Non-retriable status code " + statusCode + " while downloading from " + url);
        observer.downloadErrored(url, "Non-retriable status code " + statusCode + " while downloading " + url.toExternalForm());
    }

    private boolean isServerError(int statusCode) {
        return statusCode / 100 == 5;
    }

    private void handleServerError(int statusCode) throws IOException {
        observer.downloadErrored(url, "Retriable status code " + statusCode + " while downloading " + url.toExternalForm());
        throw new IOException("Retriable status code " + statusCode);
    }

    private boolean isImgurNotFound(HttpURLConnection huc, URL urlToDownload) throws IOException {
        return huc.getContentLength() == 503 && urlToDownload.getHost().endsWith("imgur.com");
    }

    private void handleImgurNotFound() {
        logger.error("[!] Imgur image is 404 (503 bytes long): " + url);
        observer.downloadErrored(url, "Imgur image is 404: " + url.toExternalForm());
    }

    private void downloadFile(HttpURLConnection huc) throws IOException {
        InputStream bis = null;
        OutputStream fos = null;
        try {
            bis = new BufferedInputStream(huc.getInputStream());
            fos = new FileOutputStream(saveAs);
            IOUtils.copy(bis, fos);
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    logger.error("Error closing input stream", e);
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    logger.error("Error closing output stream", e);
                }
            }
        }
    }

    private void handleDownloadException(IOException e, int tries) {
        logger.debug("IOException", e);
        logger.error("[!] Exception while downloading file: " + url + " - " + e.getMessage());
    }

    private void handleExceededRetries() {
        logger.error("[!] Exceeded maximum retries (" + this.retries + ") for URL " + url);
        observer.downloadErrored(url, "Failed to download " + url.toExternalForm());
    }
}

