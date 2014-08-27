package io.iron.ironmq;

public class Cloud {
    String scheme;
    String host;
    int port;
    String pathPrefix = "";

    public static final Cloud ironAWSUSEast = new Cloud("https", "mq-aws-us-east-1.iron.io", 443);
    public static final Cloud ironAWSEUWest = new Cloud("https", "mq-aws-eu-west-1.iron.io", 443);
    public static final Cloud ironRackspaceORD = new Cloud("https", "mq-rackspace-ord.iron.io", 443);
    public static final Cloud ironRackspaceLON = new Cloud("https", "mq-rackspace-lon.iron.io", 443);

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

    public String getPathPrefix() { return pathPrefix;}

    public void setPathPrefix(String pathPrefix) { this.pathPrefix = pathPrefix; }
}
