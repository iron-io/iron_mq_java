package io.iron.ironmq;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class QueueModel {
    private String id;
    private String name;
    private String type;
    private Long size;
    private String project_id;
    private Long total_messages;
    private QueuePushModel push;
    private ArrayList<Alert> alerts;
    @SerializedName("message_timeout") private Integer messageTimeout;
    @SerializedName("message_expiration") private Integer messageExpiration;

    public QueueModel(String id, String name, Integer size, Integer total_messages, String project_id, Integer retries, String pushType, Integer retriesDelay, String errorQueue, ArrayList<Subscriber> subscribers, ArrayList<Alert> alerts) {
        this.id = id;
        this.name = name;
        this.size = (long)size;
        this.total_messages = (long)total_messages;
        this.project_id = project_id;
        this.alerts = alerts;
    }

    public QueueModel(int messageExpiration, int retries, String pushType, int retries_delay, String error_queue, ArrayList<Subscriber> subscribers, ArrayList<Alert> alerts, int messageTimeout) {
        this.messageExpiration = messageExpiration;
        this.alerts = alerts;
        this.messageTimeout = messageTimeout;
    }

    public QueueModel(int messageTimeout, int messageExpiration) {
        this.messageTimeout = messageTimeout;
        this.messageExpiration = messageExpiration;
    }

    public QueueModel(ArrayList<Alert> alerts) {
        this.alerts = alerts;
    }

    public QueueModel(QueuePushModel push) {
        this.push = push;
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

    /**
     * Returns the size of the queue.
     *
     * @deprecated Use getSizeLong() instead.
     */
    @Deprecated
    public int getSize() {
        return (int)(long)size;
    }

    /**
     * Returns the size of the queue.
     */
    public long getSizeLong() {
        return size;
    }

    public void setSize(int size) {
        this.size = (long)size;
    }

    /**
     * Returns total count of messages ever placed to the queue.
     *
     * @deprecated Use getTotalMessages() instead.
     */
    @Deprecated
    public int getTotal_messages() {
        return (int)(long)total_messages;
    }

    /**
     * Returns total count of messages ever placed to the queue.
     *
     * @deprecated Use getTotalMessagesLong() instead.
     */
    @Deprecated
    public int getTotalMessages() {
        return (int)(long)total_messages;
    }

    /**
     * Returns total count of messages ever placed to the queue.
     */
    public long getTotalMessagesLong() {
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

    @Deprecated
    public int getRetries() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public void setRetries(int retries) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public String getPushType() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public void setPushType(String pushType) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public int getRetriesDelay() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public void setRetriesDelay(int retriesDelay) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public String getErrorQueue() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public void setErrorQueue(String errorQueue) {
        throw new UnsupportedOperationException();
    }

    public ArrayList<Subscriber> getSubscribers() {
        return getPushInfo().getSubscribers();
    }

    @Deprecated
    public void setSubscribers(ArrayList<Subscriber> subscribers) {
        throw new UnsupportedOperationException();
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public QueuePushModel getPushInfo() {
        return push;
    }

    public void setPushInfo(QueuePushModel push) {
        this.push = push;
    }

    public void addSubscriber(Subscriber subscriber) {
        synchronized (this) {
            if (push == null) {
                push = new QueuePushModel();
            }
        }
        push.addSubscriber(subscriber);
    }

    public void addSubscribers(ArrayList<Subscriber> subscribers) {
        synchronized (this) {
            if (push == null) {
                push = new QueuePushModel();
            }
        }
        push.addSubscribers(subscribers);
    }
}
