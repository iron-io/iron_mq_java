package io.iron.ironmq;

public class Cloud {
    final String scheme;
    private String host;
    final int port;

    public static final Cloud ironAWSUSEast = new Cloud("https", "mq-aws-us-east-1.iron.io", 443);
    public static final Cloud ironRackspaceDFW = new Cloud("https", "mq-rackspace-dfw.iron.io", 443);

    Cloud(String scheme, String host, int port) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
    }
    
    public String getScheme() {
		return scheme;
	}
    
    public void setHost(String host) {
		this.host = host;
	}
    
    public String getHost() {
		return host;
	}
    
    public int getPort() {
		return port;
	}
}
