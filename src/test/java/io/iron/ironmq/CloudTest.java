package io.iron.ironmq;

import org.junit.Assert;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Properties;

public class CloudTest {
    @Test
    public void testBasic() throws MalformedURLException {
        Cloud c = new Cloud("http://test.net:8080/some/path");
        Assert.assertEquals("test.net", c.getHost());
        Assert.assertEquals("http", c.getScheme());
        Assert.assertEquals(8080, c.getPort());
        Assert.assertEquals("/some/path", c.getPathPrefix());
    }

    @Test
    public void testHttp() throws MalformedURLException {
        Cloud c = new Cloud("http://test.net:3445/");
        Assert.assertEquals("test.net", c.getHost());
        Assert.assertEquals("http", c.getScheme());
        Assert.assertEquals(3445, c.getPort());
    }

    @Test
    public void testHttpDefaultPort() throws MalformedURLException {
        Cloud c = new Cloud("http://test.net/");
        Assert.assertEquals("http", c.getScheme());
        Assert.assertEquals(80, c.getPort());
    }

    @Test
    public void testHttps() throws MalformedURLException {
        Cloud c = new Cloud("https://test.net:3445/");
        Assert.assertEquals("test.net", c.getHost());
        Assert.assertEquals("https", c.getScheme());
        Assert.assertEquals(3445, c.getPort());
    }

    @Test
    public void testHttpsDefaultPort() throws MalformedURLException {
        Cloud c = new Cloud("https://test.net/");
        Assert.assertEquals("https", c.getScheme());
        Assert.assertEquals(443, c.getPort());
    }

    @Test
    public void testEmptyPath() throws MalformedURLException {
        Cloud c = new Cloud("http://test.net/");
        Assert.assertEquals("", c.getPathPrefix());
    }
}
