package io.iron.ironmq;

import java.net.MalformedURLException;
import java.net.URL;

public class Cloud {
    String scheme;
    String host;
    int port;
    String pathPrefix = "";

    public static final Cloud ironAWSUSEast = new Cloud("https", "mq-aws-us-east-1-1.iron.io", 443);

    public Cloud(String url) throws MalformedURLException {
        URL u = new URL(url);
        this.scheme = u.getProtocol();
        this.host = u.getHost();
        this.port = u.getPort() < 0 ? u.getDefaultPort() : u.getPort();
        String path = u.getPath();
        this.pathPrefix = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    public Cloud(String scheme, String host, int port) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
    }

    public String getScheme() {
        return scheme;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPathPrefix() {
        return pathPrefix;
    }

    public void setPathPrefix(String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }
}
