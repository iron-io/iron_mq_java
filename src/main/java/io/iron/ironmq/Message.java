package io.iron.ironmq;

import java.io.Serializable;

/**
 * The Message class represents a message retrieved from an IronMQ queue.
 */
public class Message implements Serializable {
    private String id;
    private String body;
    private long timeout;
    private long delay;
    private long expires_in;

    public static final long DEFAULT_EXPIRES_IN = 604800;

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
     * Returns the Message's delay.
     */
    public long getDelay() {
        return delay;
    }

    /**
     * Sets the number of seconds after which the Message will be available
     *
     * @param delay The new delay
     */
    public void setDelay(long delay) {
        this.delay = delay;
    }

    /**
     * Returns the number of seconds in which this message will be removed from the queue
     */
    public long getExpires_in() {
        return expires_in;
    }

    /**
     * Sets the number of seconds after which the message is deleted from the queue.
     *
     * @param expires_in The new expiry value
     */
    public void setExpires_in(long expires_in) {
        this.expires_in = expires_in;
    }

    /**
    * Returns a string representation of the Message.
    */
    public String toString() { return body; }
}
