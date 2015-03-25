package io.iron.ironmq;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

/**
 * The Message class represents a message retrieved from an IronMQ queue.
 */
public class Message extends MessageOptions implements Serializable {
    private String body;
    // Long, not long, so that it's nullable. Gson doesn't serialize null,
    // so we can use the default on the server and not have to know about
    // it.
    @SerializedName("expires_in") private Long expiresIn;
    @SerializedName("reserved_count") private long reservedCount;

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
     * Returns the number of times the message has been reserved.
     */
    public long getReservedCount() { return reservedCount; }

    /**
    * Returns the number of seconds in which the Message will be removed from the
    * queue. If the server default of 7 days will be used, 0 is returned.
    */
    public long getExpiresIn() {
        if (this.expiresIn == null) {
            return 0;
        }
        return this.expiresIn.longValue();
    }

    /**
    * Sets the number of seconds in which the Message will be removed from the
    * queue.
    *
    * @param expiresIn The new expiration offset in seconds. A value less than
    * or equal to 0 will cause the server default of 7 days to be used.
    */
    public void setExpiresIn(long expiresIn) {
        if (expiresIn > 0) {
            this.expiresIn = Long.valueOf(expiresIn);
        } else {
            this.expiresIn = null;
        }
    }

    /**
    * Returns a string representation of the Message.
    */
    public String toString() { return body; }
}
