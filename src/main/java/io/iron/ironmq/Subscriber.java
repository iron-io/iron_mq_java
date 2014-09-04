package io.iron.ironmq;

import java.util.HashMap;

public class Subscriber {
    private String url;
    private String name;
    private HashMap<String, String> headers;

    public Subscriber(String name) {
        this.name = name;
    }

    public Subscriber(String url, String name) {
        this.url = url;
        this.name = name;
    }

    public Subscriber(String url, String name, HashMap<String, String> headers) {
        this.url = url;
        this.name = name;
        this.headers = headers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HashMap<String, String> getHeaders() {
        if (headers == null) {
            headers = new HashMap<String, String>();
        }
        return headers;
    }
}
