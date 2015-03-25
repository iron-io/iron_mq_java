package io.iron.ironmq;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * The Message class represents a message retrieved from an IronMQ queue.
 */
public class MessageOptions implements Serializable {
    protected String id;
    protected Long delay;
    private Long timeout;

    @SerializedName("reservation_id")
    protected String reservationId;

    public MessageOptions() {
    }

    public MessageOptions(String reservationId) {
        this.reservationId = reservationId;
    }

    public MessageOptions(String reservationId, Long delay) {
        this.delay = delay;
        this.reservationId = reservationId;
    }

    public MessageOptions(String id, String reservationId) {
        this.id = id;
        this.reservationId = reservationId;
    }

    public MessageOptions(String id, String reservationId, Long timeout) {
        this.id = id;
        this.reservationId = reservationId;
        this.timeout = timeout;
    }

    /**
     * Returns Id of the Message.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns reservation Id of the Message.
     */
    public String getReservationId() {
        return reservationId;
    }

    /**
     * Returns the number of seconds after which the Message will be available.
     */
    public long getDelay() { return delay; }

    /**
     * Returns the Message's timeout.
     */
    public long getTimeout() { return timeout; }

    /**
     * Sets Id to the Message.
     *
     * @param id The new Id.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Sets reservation Id to the Message.
     *
     * @param reservationId Reservation Id of the Message.
     */
    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    /**
     * Sets the number of seconds after which the Message will be available.
     *
     * @param delay The new delay.
     */
    public void setDelay(long delay) { this.delay = delay; }

    /**
     * Sets the Message's timeout.
     *
     * @param timeout The new timeout.
     */
    public void setTimeout(long timeout) { this.timeout = timeout; }
}
