package com.rarchives.ripme.tst.ripper.rippers;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
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
            System.out.println("Testing " + ripperTest.ripperClass.getSimpleName());
            for (String url : ripperTest.urls) {
                try {
                    AbstractRipper ripper = createRipper(ripperTest.ripperClass, url);
                    testRipper(ripper);
                } catch (Exception e) {
                    handleRipperTestException(e, ripperTest.ripperClass, url);
                }
            }
        }
    }   

    private AbstractRipper createRipper(Class<? extends AbstractRipper> ripperClass, String url) 
        throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, MalformedURLException {
        Constructor<? extends AbstractRipper> constructor = ripperClass.getConstructor(URL.class);
        return constructor.newInstance(new URL(url));
    }

    private void handleRipperTestException(Exception e, Class<?> ripperClass, String url) {
        String errorMessage = String.format("Failed to test ripper %s with URL %s", ripperClass.getSimpleName(), url);
        logger.error(errorMessage, e);
        fail(errorMessage + ": " + e.getMessage());
    }
}