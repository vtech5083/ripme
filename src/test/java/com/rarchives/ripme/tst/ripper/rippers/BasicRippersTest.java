package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
//import java.util.Arrays;
//import java.util.List;

import com.rarchives.ripme.ripper.AbstractRipper;
import com.rarchives.ripme.ripper.rippers.*;

public class BasicRippersTest extends RippersTest {

    private void ripperTest(Class<? extends AbstractRipper> ripperClass, String... urls) throws IOException {
        for (String url : urls) {
            AbstractRipper ripper = getRipper(ripperClass, url);
            testRipper(ripper);
        }
    }

    private AbstractRipper getRipper(Class<? extends AbstractRipper> ripperClass, String url) throws IOException {
        try {
            Constructor<? extends AbstractRipper> constructor = ripperClass.getConstructor(URL.class);
            return constructor.newInstance(new URL(url));
        } catch (Exception e) {
            throw new IOException("Failed to create ripper for URL: " + url, e);
        }
    }

    public void testDeviantartRipper() throws IOException {
        ripperTest(DeviantartRipper.class,
                "http://airgee.deviantart.com/gallery/",
                "http://faterkcx.deviantart.com/gallery/");
    }

    public void testEightmusesRipper() throws IOException {
        ripperTest(EightmusesRipper.class,
                "http://www.8muses.com/index/category/jab-hotassneighbor7",
                "https://www.8muses.com/album/jab-comics/a-model-life");
    }

    public void testTwitterRipper() throws IOException {
        ripperTest(TwitterRipper.class, "https://twitter.com/danngamber01/media");
    }

    public void testFivehundredpxRipper() throws IOException {
        ripperTest(FivehundredpxRipper.class, "https://marketplace.500px.com/alexander_hurman");
    }

    public void testFuraffinityRipper() throws IOException {
        ripperTest(FuraffinityRipper.class, "https://www.furaffinity.net/gallery/mustardgas/");
    }

    public void testGifyoRipper() throws IOException {
        ripperTest(GifyoRipper.class, "http://gifyo.com/PieSecrets/");
    }

    public void testGirlsOfDesireRipper() throws IOException {
        ripperTest(GirlsOfDesireRipper.class, "http://www.girlsofdesire.org/galleries/krillia/");
    }

    public void testHentaifoundryRipper() throws IOException {
        ripperTest(HentaifoundryRipper.class, "http://www.hentai-foundry.com/pictures/user/personalami");
    }

    public void testImagearnRipper() throws IOException {
        ripperTest(ImagearnRipper.class, "http://imagearn.com//gallery.php?id=578682");
    }

    public void testImagebamRipper() throws IOException {
        ripperTest(ImagebamRipper.class, "http://www.imagebam.com/gallery/488cc796sllyf7o5srds8kpaz1t4m78i");
    }

    public void testImagevenueRipper() throws IOException {
        ripperTest(ImagevenueRipper.class, "http://img120.imagevenue.com/galshow.php?gal=gallery_1373818527696_191lo");
    }

    public void testImgboxRipper() throws IOException {
        ripperTest(ImgboxRipper.class, "http://imgbox.com/g/sEMHfsqx4w");
    }

    public void testModelmayhemRipper() throws IOException {
        ripperTest(ModelmayhemRipper.class, "http://www.modelmayhem.com/portfolio/520206/viewall");
    }

    public void testMotherlessRipper() throws IOException {
        ripperTest(MotherlessRipper.class, "http://motherless.com/G4DAA18D");
    }

    public void testNfsfwRipper() throws IOException {
        ripperTest(NfsfwRipper.class, "http://nfsfw.com/gallery/v/Kitten/");
    }

    public void testPhotobucketRipper() throws IOException {
        ripperTest(PhotobucketRipper.class, "http://s844.photobucket.com/user/SpazzySpizzy/library/Album%20Covers?sort=3&page=1");
    }

    public void testPornhubRipper() throws IOException {
        ripperTest(PornhubRipper.class, "http://www.pornhub.com/album/428351");
    }

    public void testShesFreakyRipper() throws IOException {
        ripperTest(ShesFreakyRipper.class, "http://www.shesfreaky.com/gallery/nicee-snow-bunny-579NbPjUcYa.html");
    }

    public void testTapasticRipper() throws IOException {
        ripperTest(TapasticRipper.class, "http://tapastic.com/episode/2139");
    }

    public void testTeenplanetRipper() throws IOException {
        ripperTest(TeenplanetRipper.class, "http://teenplanet.org/galleries/the-perfect-side-of-me-6588.html");
    }

    public void testTwodgalleriesRipper() throws IOException {
        ripperTest(TwodgalleriesRipper.class, "http://www.2dgalleries.com/artist/regis-loisel-6477");
    }

    public void testVidbleRipper() throws IOException {
        ripperTest(VidbleRipper.class, "http://www.vidble.com/album/y1oyh3zd");
    }

    public void testVineRipper() throws IOException {
        ripperTest(VineRipper.class, "https://vine.co/u/954440445776334848");
    }

    public void testVkRipper() throws IOException {
        ripperTest(VkRipper.class,
                "http://vk.com/album45506334_0",
                "https://vk.com/album45506334_0",
                "https://vk.com/photos45506334");
    }

    public void testXhamsterRipper() throws IOException {
        ripperTest(XhamsterRipper.class, "http://xhamster.com/photos/gallery/1462237/alyssa_gadson.html");
    }
}