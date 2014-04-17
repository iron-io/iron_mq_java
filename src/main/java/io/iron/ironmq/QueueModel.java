package io.iron.ironmq;

import java.util.ArrayList;

public class QueueModel {
    private String id;
    private String name;
    private int size;
    private int total_messages;
    private String project_id;
    private int retries;
    private String pushType;
    private int retriesDelay;
    private ArrayList<Subscriber> subscribers;
    private ArrayList<Alert> alerts;

    public QueueModel(String id, String name, int size, int total_messages, String project_id, int retries, String pushType, int retriesDelay, ArrayList<Subscriber> subscribers, ArrayList<Alert> alerts) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.total_messages = total_messages;
        this.project_id = project_id;
        this.retries = retries;
        this.pushType = pushType;
        this.retriesDelay = retriesDelay;
        this.subscribers = subscribers;
        this.alerts = alerts;
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

    public int getTotal_messages() {
        return total_messages;
    }

    public void setTotal_messages(int total_messages) {
        this.total_messages = total_messages;
    }

    public String getProject_id() {
        return project_id;
    }

    public void setProject_id(String project_id) {
        this.project_id = project_id;
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
        return retriesDelay;
    }

    public void setRetriesDelay(int retriesDelay) {
        this.retriesDelay = retriesDelay;
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
}
