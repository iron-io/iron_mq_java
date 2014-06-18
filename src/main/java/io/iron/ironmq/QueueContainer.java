package io.iron.ironmq;

public class QueueContainer {
    private QueueModel queue;

    public QueueContainer() {
    }

    public QueueContainer(QueueModel queue) {
        this.queue = queue;
    }

    public QueueModel getQueue() {
        return queue;
    }

    public void setQueue(QueueModel queue) {
        this.queue = queue;
    }
}
