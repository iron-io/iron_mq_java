package io.iron.ironmq;

import java.io.Serializable;

/**
 * The Message class represents a message retrieved from an IronMQ queue.
 */
public class Message implements Serializable {
    private String id;
    private String body;
    private long timeout;

    public Message() {}

    /**
    * Returns the Message's body contents.
    */
    public String getBody() { return body; }

    /**
    * Sets the Message's body contents.
    *
    * @param body The new body contents.
    */
    public void setBody(String body) { this.body = body; }

    /**
    * Returns the Message's ID.
    */
    public String getId() { return id; }

    /**
    * Sets the Message's ID.
    *
    * @param id The new ID.
    */
    public void setId(String id) { this.id = id; }

    /**
    * Returns the Message's timeout.
    */
    public long getTimeout() { return timeout; }

    /**
    * Sets the Message's timeout.
    *
    * @param timeout The new timeout.
    */
    public void setTimeout(long timeout) { this.timeout = timeout; }

    /**
    * Returns a string representation of the Message.
    */
    public String toString() { return body; }
}
