package io.iron.ironmq;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * The Message class represents a message retrieved from an IronMQ queue.
 */
public class SubscribedMessageOptions extends MessageOptions {
    @SerializedName("subscriber_name")
    protected String subscriberName;

    public SubscribedMessageOptions() {
    }

    public SubscribedMessageOptions(String reservationId, String subscriberName) {
        super(reservationId);
        this.subscriberName = subscriberName;
    }

    public SubscribedMessageOptions(String id, String reservationId, String subscriberName) {
        super(id, reservationId);
        this.subscriberName = subscriberName;
    }

    public SubscribedMessageOptions(String id, String reservationId, Long timeout, String subscriberName) {
        super(id, reservationId, timeout);
        this.subscriberName = subscriberName;
    }

    /**
     * Returns the name of Message's Subscriber.
     */
    public String getSubscriberName() {
        return subscriberName;
    }

    /**
     * Sets the name of Message's Subscriber.
     */
    public void setSubscriberName(String subscriberName) {
        this.subscriberName = subscriberName;
    }
}
