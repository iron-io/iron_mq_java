package io.iron.ironmq;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;

import com.google.gson.Gson;

/**
 * The Queue class represents a specific IronMQ queue bound to a client.
 */
public class Queue {
    final private Client client;
    final private String name;

    public Queue(Client client, String name) {
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
        Messages msgs = get(1);
        Message msg;
        try {
            msg = msgs.getMessage(0);
        } catch (IndexOutOfBoundsException e) {
            throw new EmptyQueueException();
        }

        return msg;
    }

    /**
    * Retrieves Messages from the queue. If there are no items on the queue, an
    * EmptyQueueException is thrown.
    * @param numberOfMessages The number of messages to receive. Max. is 100.
    * @throws EmptyQueueException If the queue is empty.
    * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
    * @throws IOException If there is an error accessing the IronMQ server.
    */
    public Messages get(int numberOfMessages) throws IOException {
        return get(numberOfMessages, -1);
    }

    /**
    * Retrieves Messages from the queue. If there are no items on the queue, an
    * EmptyQueueException is thrown.
    * @param numberOfMessages The number of messages to receive. Max. is 100.
    * @param timeout timeout in seconds.
    * @throws EmptyQueueException If the queue is empty.
    * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
    * @throws IOException If there is an error accessing the IronMQ server.
    */
    public Messages get(int numberOfMessages, int timeout) throws IOException {
        if (numberOfMessages < 1 || numberOfMessages > 100) {
            throw new IllegalArgumentException("numberOfMessages has to be within 1..100");
        }

        String url = "queues/" + name + "/messages?n=" + numberOfMessages;
        if (timeout > -1) {
            url += "&timeout=" + timeout;
        }
        Reader reader = client.get(url);
        Gson gson = new Gson();
        Messages messages = gson.fromJson(reader, Messages.class);
        reader.close();
        return messages;
    }
    
    /**
    * Peeking at a queue returns the next messages on the queue, but it does not reserve them. 
    * If there are no items on the queue, an EmptyQueueException is thrown.
    *
    * @throws EmptyQueueException If the queue is empty.
    * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
    * @throws IOException If there is an error accessing the IronMQ server.
    */
    public Message peek() throws IOException {
        Messages msgs = peek(1);
        Message msg;
        try {
            msg = msgs.getMessage(0);
        } catch (IndexOutOfBoundsException e) {
            throw new EmptyQueueException();
        }

        return msg;
    }

    /**
    * Peeking at a queue returns the next messages on the queue, but it does not reserve them.
    * 
    * @param numberOfMessages The number of messages to receive. Max. is 100.
    * @throws EmptyQueueException If the queue is empty.
    * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
    * @throws IOException If there is an error accessing the IronMQ server.
    */
    public Messages peek(int numberOfMessages) throws IOException {
    	 if (numberOfMessages < 1 || numberOfMessages > 100) {
             throw new IllegalArgumentException("numberOfMessages has to be within 1..100");
         }
         Reader reader = client.get("queues/" + name + "/messages/peek?n="+numberOfMessages);
         try{
        	 return new Gson().fromJson(reader, Messages.class);
         }finally{
        	 reader.close();
         }
    }
    
    /**
    * Touching a reserved message extends its timeout to the duration specified when the message was created.
    *
    * @param id The ID of the message to delete.
    *
    * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
    * @throws IOException If there is an error accessing the IronMQ server.
    */
    public void touchMessage(String id) throws IOException {
        client.post("queues/" + name + "/messages/" + id + "/touch", "");
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
     * Destroy the queue.
     * 
     * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws IOException If there is an error accessing the IronMQ server.
     */
    public void destroy() throws IOException {
    	client.delete("queues/" + name);
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
    * @return The new message's ID
    *
    * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
    * @throws IOException If there is an error accessing the IronMQ server.
    */
    public String push(String msg) throws IOException {
        return push(msg, 0);
    }

    /**
    * Pushes a message onto the queue.
    *
    * @param msg The body of the message to push.
    * @param timeout The message's timeout in seconds.
    * @return The new message's ID
    *
    * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
    * @throws IOException If there is an error accessing the IronMQ server.
    */
    public String push(String msg, long timeout) throws IOException {
        return push(msg, timeout, 0);
    }

    /**
    * Pushes a message onto the queue.
    *
    * @param msg The body of the message to push.
    * @param timeout The message's timeout in seconds.
    * @param delay The message's delay in seconds.
    * @return The new message's ID
    *
    * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
    * @throws IOException If there is an error accessing the IronMQ server.
    */
    public String push(String msg, long timeout, long delay) throws IOException {
        return push(msg, timeout, delay, 0);
    }

    /**
    * Pushes a message onto the queue.
    *
    * @param msg The body of the message to push.
    * @param timeout The message's timeout in seconds.
    * @param delay The message's delay in seconds.
    * @param expiresIn The message's expiration offset in seconds.
    * @return The new message's ID
    *
    * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
    * @throws IOException If there is an error accessing the IronMQ server.
    */
    public String push(String msg, long timeout, long delay, long expiresIn) throws IOException {
        Message message = new Message();
        message.setBody(msg);
        message.setTimeout(timeout);
        message.setDelay(delay);
        message.setExpiresIn(expiresIn);

        Messages msgs = new Messages(message);
        Gson gson = new Gson();
        String body = gson.toJson(msgs);

        Reader reader = client.post("queues/" + name + "/messages", body);
        Ids ids = gson.fromJson(reader, Ids.class);
        reader.close();
        return ids.getId(0);
    }

    /**
     * Clears the queue off all messages
     * @throws IOException
     */
    public void clear() throws IOException {
        client.post("queues/"+name+"/clear", "").close();
    }

    /**
     * @return the name of this queue
     */
    public String getName() {
        return name;
    }

    static class Info implements Serializable {
        int count;
        int size;
    }

    public int getSize() throws IOException {
        Reader reader = client.get("queues/"+name);
        Gson gson = new Gson();
        Info info = gson.fromJson(reader, Info.class);
        reader.close();
        return info.size;
    }
}
