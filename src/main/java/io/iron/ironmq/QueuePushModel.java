package io.iron.ironmq;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class QueuePushModel {
    private ArrayList<Subscriber> subscribers;
    private Integer retries;
    private String type;
    @SerializedName("retries_delay") private Integer retriesDelay;
    @SerializedName("error_queue")   private String errorQueue;

    public QueuePushModel(ArrayList<Subscriber> subscribers, String type, Integer retries, Integer retriesDelay, String errorQueue) {
        this.subscribers = subscribers;
        this.retries = retries;
        this.type = type;
        this.retriesDelay = retriesDelay;
        this.errorQueue = errorQueue;
    }

    public QueuePushModel(ArrayList<Subscriber> subscribers) {
        this(subscribers, null, null, null, null);
    }

    public QueuePushModel() {
        this(null, null, null, null, null);
    }

    public ArrayList<Subscriber> getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(ArrayList<Subscriber> subscribers) {
        this.subscribers = subscribers;
    }

    public Integer getRetries() {
        return retries;
    }

    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getRetriesDelay() {
        return retriesDelay;
    }

    public void setRetriesDelay(Integer retriesDelay) {
        this.retriesDelay = retriesDelay;
    }

    public String getErrorQueue() {
        return errorQueue;
    }

    public void setErrorQueue(String errorQueue) {
        this.errorQueue = errorQueue;
    }

    public void addSubscriber(Subscriber subscriber) {
        synchronized (this) {
            if (subscribers == null) {
                subscribers = new ArrayList<Subscriber>();
            }
        }
        subscribers.add(subscriber);
    }

    public void addSubscribers(ArrayList<Subscriber> subscribers) {
        synchronized (this) {
            if (subscribers == null) {
                subscribers = new ArrayList<Subscriber>();
            }
        }
        subscribers.addAll(subscribers);
    }

    public void removeSubscribers() {
        if (subscribers != null) {
            subscribers.clear();
        }
    }
}
