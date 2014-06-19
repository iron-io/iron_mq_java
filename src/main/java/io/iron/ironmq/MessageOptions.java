package io.iron.ironmq;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * The Message class represents a message retrieved from an IronMQ queue.
 */
public class MessageOptions implements Serializable {
    protected String id;
    @SerializedName("reservation_id")
    protected String reservationId;

    public MessageOptions() {
    }

    public MessageOptions(String reservationId) {
        this.reservationId = reservationId;
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
}
