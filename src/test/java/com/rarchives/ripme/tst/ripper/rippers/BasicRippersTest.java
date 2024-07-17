package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import com.rarchives.ripme.ripper.AbstractRipper;
import com.rarchives.ripme.ripper.rippers.*;

public class BasicRippersTest extends RippersTest {

    private static class RipperTest {
        Class<? extends AbstractRipper> ripperClass;
        List<String> urls;

        RipperTest(Class<? extends AbstractRipper> ripperClass, String... urls) {
            this.ripperClass = ripperClass;
            this.urls = Arrays.asList(urls);
        }
    }

    private static final List<RipperTest> RIPPER_TESTS = Arrays.asList(
            new RipperTest(DeviantartRipper.class,
                    "http://airgee.deviantart.com/gallery/",
                    "http://faterkcx.deviantart.com/gallery/"),
            new RipperTest(EightmusesRipper.class,
                    "http://www.8muses.com/index/category/jab-hotassneighbor7",
                    "https://www.8muses.com/album/jab-comics/a-model-life"),
            new RipperTest(TwitterRipper.class,
                    "https://twitter.com/danngamber01/media"),
            // Add other ripper tests here...
            new RipperTest(XhamsterRipper.class,
                    "http://xhamster.com/photos/gallery/1462237/alyssa_gadson.html")
    );

    public void testRippers() {
        for (RipperTest ripperTest : RIPPER_TESTS) {
            try {
                System.out.println("Testing " + ripperTest.ripperClass.getSimpleName());
                for (String url : ripperTest.urls) {
                    AbstractRipper ripper = ripperTest.ripperClass.getConstructor(URL.class).newInstance(new URL(url));
                    testRipper(ripper);
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | IOException e) {
                throw new RuntimeException("Failed to test ripper: " + ripperTest.ripperClass.getSimpleName(), e);
            }
        }
    }
}