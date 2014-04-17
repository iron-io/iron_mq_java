package io.iron.ironmq;

public class MessagePushModel {
    private int retries_delay;
    private int retries_remaining;
    private int status_code;
    private String status;
    private String url;
    private String id;

    public MessagePushModel(int retries_delay, int retries_remaining, int status_code, String status, String url, String id) {
        this.retries_delay = retries_delay;
        this.retries_remaining = retries_remaining;
        this.status_code = status_code;
        this.status = status;
        this.url = url;
        this.id = id;
    }

    public int getRetries_delay() {
        return retries_delay;
    }

    public void setRetries_delay(int retries_delay) {
        this.retries_delay = retries_delay;
    }

    public int getRetries_remaining() {
        return retries_remaining;
    }

    public void setRetries_remaining(int retries_remaining) {
        this.retries_remaining = retries_remaining;
    }

    public int getStatus_code() {
        return status_code;
    }

    public void setStatus_code(int status_code) {
        this.status_code = status_code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
