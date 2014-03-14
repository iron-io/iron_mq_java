package io.iron.ironmq;

public class Subscriber {
    private String url;

    public Subscriber(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
