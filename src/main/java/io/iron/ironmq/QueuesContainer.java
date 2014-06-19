package io.iron.ironmq;

import java.io.Serializable;
import java.util.ArrayList;

public class QueuesContainer implements Serializable {
    private ArrayList<QueueModel> queues;

    public QueuesContainer() {
        this.queues = new ArrayList<QueueModel>();
    }

    public QueuesContainer(ArrayList<QueueModel> queues) {
        this.queues = queues;
    }

    /**
     * Returns the Array of QueueModels contained
     */
    public ArrayList<QueueModel> getQueues() {
        return queues;
    }

    /**
     * Returns the i'th element of queues list
     */
    public QueueModel get(int index) {
        return queues.get(index);
    }

    /**
     * Returns the size of queues collection
     */
    public int size() {
        return queues.size();
    }
}
