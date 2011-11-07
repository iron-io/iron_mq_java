package io.iron.ironmq;

import java.io.Serializable;

public class Message implements Serializable {
    private String id;
    private String body;
    private long timeout;

    public Message() {}

    public String getBody() { return body; }

    public void setBody(String body) { this.body = body; }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public long getTimeout() { return timeout; }

    public void setTimeout(long timeout) { this.timeout = timeout; }

    public String toString() { return body; }
}
