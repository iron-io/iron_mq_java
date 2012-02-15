package io.iron.ironmq;

import java.io.IOException;

/**
 * The EmptyQueueException class represents a response from IronMQ indicating
 * the queue is empty.
 */
public class EmptyQueueException extends IOException {
    /**
    * Creates a new EmptyQueueException.
    */
    public EmptyQueueException() {
        super("Queue is empty");
    }
}
