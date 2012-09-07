package io.iron.ironmq;

public class Cloud {
    final String scheme;
    final String host;
    final int port;

    public static final Cloud ironAWSUSEast = new Cloud("https", "mq-aws-us-east-1.iron.io", 443);
    public static final Cloud ironRackspaceDFW = new Cloud("https", "mq-rackspace-dfw.iron.io", 443);

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
}
