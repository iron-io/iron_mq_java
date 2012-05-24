package io.iron.ironmq;

import java.io.IOException;
import java.io.Reader;

import com.google.gson.Gson;

/**
 * The Queue class represents a specific IronMQ queue bound to a client.
 */
public class Queue {
    final private Client client;
    final private String name;

    Queue(Client client, String name) {
        this.client = client;
        this.name = name;
    }

    /**
    * Retrieves a Message from the queue. If there are no items on the queue, an
    * EmptyQueueException is thrown.
    *
    * @throws EmptyQueueException If the queue is empty.
    * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
    * @throws IOException If there is an error accessing the IronMQ server.
    */
    public Message get() throws IOException {
        Reader reader = client.get("queues/" + name + "/messages");
        Gson gson = new Gson();
        Messages msgs = gson.fromJson(reader, Messages.class);

        Message msg;
        try {
            msg = msgs.getMessage(0);
        } catch (IndexOutOfBoundsException e) {
            throw new EmptyQueueException();
        }

        return msg;
    }

    /**
    * Deletes a Message from the queue.
    *
    * @param id The ID of the message to delete.
    *
    * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
    * @throws IOException If there is an error accessing the IronMQ server.
    */
    public void deleteMessage(String id) throws IOException {
        client.delete("queues/" + name + "/messages/" + id);
    }

    /**
    * Deletes a Message from the queue.
    *
    * @param msg The message to delete.
    *
    * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
    * @throws IOException If there is an error accessing the IronMQ server.
    */
    public void deleteMessage(Message msg) throws IOException {
        deleteMessage(msg.getId());
    }

    /**
    * Pushes a message onto the queue.
    *
    * @param msg The body of the message to push.
    *
    * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
    * @throws IOException If there is an error accessing the IronMQ server.
    */
    public void push(String msg) throws IOException {
        push(msg, 0);
    }

    public void push(String msg, long timeout) throws IOException {
        push(msg, 0, 0);
    }

    public void push(String msg, long timeout, long delay) throws IOException {
        push(msg, 0, 0, 0);
    }

    /**
    * Pushes a message onto the queue.
    *
    * @param msg The body of the message to push.
    * @param timeout The timeout of the message to push.
    *
    * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
    * @throws IOException If there is an error accessing the IronMQ server.
    */
    public void push(String msg, long timeout, long delay, long expiresIn) throws IOException {
        Message message = new Message();
        message.setBody(msg);
        message.setTimeout(timeout);
        message.setDelay(delay);
        message.setExpiresIn(expiresIn);

        Messages msgs = new Messages(message);
        Gson gson = new Gson();
        String body = gson.toJson(msgs);

        client.post("queues/" + name + "/messages", body);
    }
}
