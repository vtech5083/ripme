package com.rarchives.ripme.ripper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import org.apache.log4j.Logger;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Utils;
import java.io.Closeable;

/**
 * Thread for downloading files.
 * Includes retry logic, observer notifications, and other goodies.
 */
public class DownloadVideoThread extends Thread {

    private static final Logger logger = Logger.getLogger(DownloadVideoThread.class);

    private URL url;
    private File saveAs;
    private String prettySaveAs;
    private AbstractRipper observer;
    private int retries;

    public DownloadVideoThread(URL url, File saveAs, AbstractRipper observer) {
        super();
        this.url = url;
        this.saveAs = saveAs;
        this.prettySaveAs = Utils.removeCWD(saveAs);
        this.observer = observer;
        this.retries = Utils.getConfigInteger("download.retries", 1);
    }

    /**
     * Attempts to download the file. Retries as needed.
     * Notifies observers upon completion/error/warn.
     */
    public void run() {
        if (!prepareDownload()) {
            return;
        }

        int bytesTotal = getBytesTotal();
        if (bytesTotal == -1) {
            return;
        }

        int tries = 0;
        while (tries <= this.retries) {
            tries++;
            if (downloadFile(bytesTotal, tries)) {
                observer.downloadCompleted(url, saveAs);
                logger.info("[+] Saved " + url + " as " + this.prettySaveAs);
                return;
            }
        }

        handleMaxRetriesExceeded();
    }

    private boolean prepareDownload() {
        try {
            observer.stopCheck();
        } catch (IOException e) {
            observer.downloadErrored(url, "Download interrupted");
            return false;
        }

        if (saveAs.exists() && !handleExistingFile()) {
            return false;
        }

        return true;
    }

    private boolean handleExistingFile() {
        if (Utils.getConfigBoolean("file.overwrite", false)) {
            logger.info("[!] Deleting existing file" + prettySaveAs);
            saveAs.delete();
            return true;
        } else {
            logger.info("[!] Skipping " + url + " -- file already exists: " + prettySaveAs);
            observer.downloadExists(url, saveAs);
            return false;
        }
    }

    private int getBytesTotal() {
        try {
            int bytesTotal = getTotalBytes(this.url);
            observer.setBytesTotal(bytesTotal);
            observer.sendUpdate(STATUS.TOTAL_BYTES, bytesTotal);
            logger.debug("Size of file at " + this.url + " = " + bytesTotal + "b");
            return bytesTotal;
        } catch (IOException e) {
            logger.error("Failed to get file size at " + this.url, e);
            observer.downloadErrored(this.url, "Failed to get file size of " + this.url);
            return -1;
        }
    }

    private boolean downloadFile(int bytesTotal, int tryNumber) {
        logger.info("    Downloading file: " + url + (tryNumber > 1 ? " Retry #" + (tryNumber - 1) : ""));
        observer.sendUpdate(STATUS.DOWNLOAD_STARTED, url.toExternalForm());

        InputStream bis = null;
        OutputStream fos = null;
        try {
            bis = getInputStream();
            fos = new FileOutputStream(saveAs);
            return writeToFile(bis, fos, bytesTotal);
        } catch (IOException e) {
            logger.error("[!] Exception while downloading file: " + url + " - " + e.getMessage(), e);
            return false;
        } finally {
            closeQuietly(bis);
            closeQuietly(fos);
        }
    }

    private void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                logger.error("Error closing stream", e);
            }
        }
    }

    private InputStream getInputStream() throws IOException {
        HttpURLConnection huc = createHttpConnection();
        huc.connect();
        return new BufferedInputStream(huc.getInputStream());
    }

    private HttpURLConnection createHttpConnection() throws IOException {
        HttpURLConnection huc = (HttpURLConnection) this.url.openConnection();
        huc.setInstanceFollowRedirects(true);
        huc.setConnectTimeout(0);
        huc.setRequestProperty("accept", "*/*");
        huc.setRequestProperty("Referer", this.url.toExternalForm());
        huc.setRequestProperty("User-agent", AbstractRipper.USER_AGENT);
        logger.debug("Request properties: " + huc.getRequestProperties().toString());
        return huc;
    }

    private boolean writeToFile(InputStream bis, OutputStream fos, int bytesTotal) throws IOException {
        byte[] data = new byte[1024 * 256];
        int bytesRead;
        int bytesDownloaded = 0;

        while ((bytesRead = bis.read(data)) != -1) {
            try {
                observer.stopCheck();
            } catch (IOException e) {
                observer.downloadErrored(url, "Download interrupted");
                return false;
            }

            fos.write(data, 0, bytesRead);
            bytesDownloaded += bytesRead;
            updateProgress(bytesDownloaded);
        }

        return true;
    }

    private void updateProgress(int bytesDownloaded) {
        observer.setBytesCompleted(bytesDownloaded);
        observer.sendUpdate(STATUS.COMPLETED_BYTES, bytesDownloaded);
    }

    private void handleMaxRetriesExceeded() {
        logger.error("[!] Exceeded maximum retries (" + this.retries + ") for URL " + url);
        observer.downloadErrored(url, "Failed to download " + url.toExternalForm());
    }

    private int getTotalBytes(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("HEAD");
        conn.setRequestProperty("accept", "*/*");
        conn.setRequestProperty("Referer", this.url.toExternalForm());
        conn.setRequestProperty("User-agent", AbstractRipper.USER_AGENT);
        return conn.getContentLength();
    }
}