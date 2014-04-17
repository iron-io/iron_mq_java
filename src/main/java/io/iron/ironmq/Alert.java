package io.iron.ironmq;

public class Alert {
    private String type;
    private String direction;
    private int trigger;
    private int snooze;
    private String queue;
    private String id;

    public Alert(String type, String direction, int trigger, int snooze, String queue) {

        this.type = type;
        this.direction = direction;
        this.trigger = trigger;
        this.snooze = snooze;
        this.queue = queue;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public int getTrigger() {
        return trigger;
    }

    public void setTrigger(int trigger) {
        this.trigger = trigger;
    }

    public int getSnooze() {
        return snooze;
    }

    public void setSnooze(int snooze) {
        this.snooze = snooze;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
