package io.iron.ironmq;

import java.net.MalformedURLException;
import java.net.URL;

public class Cloud {
    final String scheme;
    final String host;
    final int port;
    final String suffix;

    public static final Cloud ironAWSUSEast = new Cloud("https", "mq-aws-us-east-1.iron.io", 443);
    public static final Cloud ironAWSEUWest = new Cloud("https", "mq-aws-eu-west-1.iron.io", 443);
    public static final Cloud ironRackspaceORD = new Cloud("https", "mq-rackspace-ord.iron.io", 443);
    public static final Cloud ironRackspaceLON = new Cloud("https", "mq-rackspace-lon.iron.io", 443);

    public Cloud(String url) throws MalformedURLException {
        URL u = new URL(url);
        this.scheme = u.getProtocol();
        this.host = u.getHost();
        this.port = u.getPort() < 0 ? u.getDefaultPort() : u.getPort();
        String path = u.getPath();
        this.pathPrefix = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    public Cloud(String scheme, String host, int port) {
        this(scheme, host, port, null);
    }

    public Cloud(String scheme, String host, int port, String suffix) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;

        if (suffix != null && !suffix.isEmpty()) {
            if (!suffix.startsWith("/"))
                suffix = "/" + suffix;
            if (suffix.endsWith("/"))                        {
                suffix = suffix.substring(0, suffix.length() - 1);
            }
        }
        this.suffix = suffix;
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

    public String getSuffix() {
        return suffix == null ? "" : suffix;
    }
}
