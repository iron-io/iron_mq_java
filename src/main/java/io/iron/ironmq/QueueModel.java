package io.iron.ironmq;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class QueueModel {
    private String id;
    private String name;
    private Integer size;
    private Integer total_messages;
    private String project_id;
    private Integer retries;
    private String pushType;
    private Integer retries_delay;
    private String error_queue;
    private ArrayList<Subscriber> subscribers;
    private ArrayList<Alert> alerts            ;
    @SerializedName("message_timeout") private Integer messageTimeout;
    @SerializedName("message_expiration") private Integer messageExpiration;

    public QueueModel(String id, String name, int size, int total_messages, String project_id, int retries, String pushType, int retriesDelay, String errorQueue, ArrayList<Subscriber> subscribers, ArrayList<Alert> alerts) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.total_messages = total_messages;
        this.project_id = project_id;
        this.retries = retries;
        this.pushType = pushType;
        this.retries_delay = retriesDelay;
        this.error_queue = errorQueue;
        this.subscribers = subscribers;
        this.alerts = alerts;
    }

    public QueueModel(int messageExpiration, int retries, String pushType, int retries_delay, String error_queue, ArrayList<Subscriber> subscribers, ArrayList<Alert> alerts, int messageTimeout) {
        this.messageExpiration = messageExpiration;
        this.retries = retries;
        this.pushType = pushType;
        this.retries_delay = retries_delay;
        this.error_queue = error_queue;
        this.subscribers = subscribers;
        this.alerts = alerts;
        this.messageTimeout = messageTimeout;
    }

    public QueueModel(int messageTimeout, int messageExpiration) {
        this.messageTimeout = messageTimeout;
        this.messageExpiration = messageExpiration;
    }

    public QueueModel(ArrayList<Subscriber> subscribers, String pushType) {
        this.pushType = pushType;
        this.subscribers = subscribers;
    }

    public QueueModel(ArrayList<Alert> alerts) {
        this.alerts = alerts;
    }

    public QueueModel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    /**
     * Returns total count of messages ever placed to the queue.
     *
     * @deprecated Use getTotalMessages() instead.
     */
    @Deprecated
    public int getTotal_messages() {
        return total_messages;
    }

    /**
     * Returns total count of messages ever placed to the queue.
     */
    public int getTotalMessages() {
        return total_messages;
    }

    @Deprecated
    public void setTotal_messages(int total_messages) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns Id of a project, which owns the Queue.
     *
     * @deprecated Use getProjectId() instead.
     */
    @Deprecated
    public String getProject_id() {
        return project_id;
    }

    /**
     * Returns Id of a project, which owns the Queue.
     */
    public String getProjectId() {
        return project_id;
    }

    @Deprecated
    public void setProject_id(String project_id) {
        throw new UnsupportedOperationException();
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public String getPushType() {
        return pushType;
    }

    public void setPushType(String pushType) {
        this.pushType = pushType;
    }

    public int getRetriesDelay() {
        return retries_delay;
    }

    public void setRetriesDelay(int retriesDelay) {
        this.retries_delay = retriesDelay;
    }

    public String getErrorQueue() {
        return error_queue;
    }

    public void setErrorQueue(String errorQueue) {
        this.error_queue = errorQueue;
    }

    public ArrayList<Subscriber> getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(ArrayList<Subscriber> subscribers) {
        this.subscribers = subscribers;
    }

    public ArrayList<Alert> getAlerts() {
        return alerts;
    }

    public void setAlerts(ArrayList<Alert> alerts) {
        this.alerts = alerts;
    }

    public int getMessageTimeout() {
        return messageTimeout;
    }

    public void setMessageTimeout(int messageTimeout) {
        this.messageTimeout = messageTimeout;
    }

    public int getMessageExpiration() {
        return messageExpiration;
    }

    public void setMessageExpiration(int messageExpiration) {
        this.messageExpiration = messageExpiration;
    }
}
