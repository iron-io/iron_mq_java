package io.iron.ironmq;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

import com.google.gson.Gson;

/**
 * The Queue class represents a specific IronMQ queue bound to a client.
 */
public class Queue {
    final private Client client;
    final private String name;

    public Queue(Client client, String name) {
        if (name == null)
            throw new NullPointerException("Queue name cannot be null");
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
     * Deletes multiple messages from the queue.
     *
     * @param ids The IDs of the messages to delete.
     *
     * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws IOException If there is an error accessing the IronMQ server.
     */
    public void deleteMessages(Ids ids) throws IOException {
        Gson gson = new Gson();
        String jsonMessages = gson.toJson(ids);

        Reader reader = client.delete("queues/" + name + "/messages", jsonMessages);
        reader.close();
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
     * Pushes a messages onto the queue.
     *
     * @param msg The array of the messages to push.
     * @return The IDs of new messages
     *
     * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws IOException If there is an error accessing the IronMQ server.
     */
    public Ids pushMessages(String[] msg) throws IOException {
        return pushMessages(msg, 0);
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
     * Pushes a messages onto the queue.
     *
     * @param msg The array of the messages to push.
     * @param timeout The message's timeout in seconds.
     * @return The IDs of new messages
     *
     * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws IOException If there is an error accessing the IronMQ server.
     */
    public Ids pushMessages(String msg[], long timeout) throws IOException {
        return pushMessages(msg, timeout, 0);
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
     * Pushes a messages onto the queue.
     *
     * @param msg The array of the messages to push.
     * @param timeout The message's timeout in seconds.
     * @param delay The message's delay in seconds.
     * @return The IDs of new messages
     *
     * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws IOException If there is an error accessing the IronMQ server.
     */
    public Ids pushMessages(String[] msg, long timeout, long delay) throws IOException {
        return pushMessages(msg, timeout, delay, 0);
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
     * Pushes a messages onto the queue.
     *
     * @param msg The array of the messages to push.
     * @param timeout The message's timeout in seconds.
     * @param delay The message's delay in seconds.
     * @param expiresIn The message's expiration offset in seconds.
     * @return The IDs of new messages
     *
     * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws IOException If there is an error accessing the IronMQ server.
     */
    public Ids pushMessages(String[] msg, long timeout, long delay, long expiresIn) throws IOException {
        ArrayList<Message> messages = new ArrayList<Message>();
        for (String messageName: msg){
            Message message = new Message();
            message.setBody(messageName);
            message.setTimeout(timeout);
            message.setDelay(delay);
            message.setExpiresIn(expiresIn);
            messages.add(message);
        }

        MessagesArrayList msgs = new MessagesArrayList(messages);
        Gson gson = new Gson();
        String jsonMessages = gson.toJson(msgs);

        Reader reader = client.post("queues/" + name + "/messages", jsonMessages);
        Ids ids = gson.fromJson(reader, Ids.class);
        reader.close();
        return ids;
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

    /**
     * Retrieves Info about queue. If there is no queue, an EmptyQueueException is thrown.
     * @throws EmptyQueueException If there is no queue.
     * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws IOException If there is an error accessing the IronMQ server.
     */
    public QueueModel getInfoAboutQueue() throws IOException {
        Reader reader = client.get("queues/" + name);
        Gson gson = new Gson();
        QueueModel message = gson.fromJson(reader, QueueModel.class);
        reader.close();
        return message;
    }

    /**
     * Retrieves Message from the queue by message id. If there are no items on the queue, an
     * EmptyQueueException is thrown.
     * @param id The ID of the message to get.
     * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws IOException If there is an error accessing the IronMQ server.
     */
    public Message getMessageById(String id) throws IOException {
        String url = "queues/" + name + "/messages/" + id;
        Reader reader = client.get(url);
        Gson gson = new Gson();
        Message message = gson.fromJson(reader, Message.class);
        reader.close();
        return message;
    }

    static class Delay {
        private int delay;
        public Delay(int delay) {
            this.delay = delay;
        }
    }

    /**
     * Release locked message after specified time. If there is no message with such id on the queue, an
     * EmptyQueueException is thrown.
     * @param id The ID of the message to release.
     * @param delay The time after which the message will be released.
     * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws IOException If there is an error accessing the IronMQ server.
     */
    public void releaseMessage(String id, int delay) throws IOException {
        String url = "queues/" + name + "/messages/" + id + "/release";
        Gson gson = new Gson();
        Delay delayClass = new Delay(delay);
        String jsonMessages = gson.toJson(delayClass);
        Reader reader = client.post(url, jsonMessages);
        reader.close();
    }

    /**
     * Add subscribers to Queue. If there is no queue, an EmptyQueueException is thrown.
     * @param subscribersList The array list of subscribers.
     * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws IOException If there is an error accessing the IronMQ server.
     */
    public void addSubscribersToQueue(ArrayList<Subscriber> subscribersList) throws IOException {
        String url = "queues/" + name + "/subscribers";
        Subscribers subscribers = new Subscribers(subscribersList);
        Gson gson = new Gson();
        String jsonMessages = gson.toJson(subscribers);
        Reader reader = client.post(url, jsonMessages);
        reader.close();
    }

    /**
     * Remove subscribers from Queue. If there is no queue, an EmptyQueueException is thrown.
     * @param subscribersList The array list of subscribers.
     * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws IOException If there is an error accessing the IronMQ server.
     */
    public void removeSubscribersFromQueue(ArrayList<Subscriber> subscribersList) throws IOException {
        String url = "queues/" + name + "/subscribers";
        Subscribers subscribers = new Subscribers(subscribersList);
        Gson gson = new Gson();
        String jsonMessages = gson.toJson(subscribers);
        Reader reader = client.delete(url, jsonMessages);
        reader.close();
    }


    /**
     * Get push info of message by message id. If there is no message, an EmptyQueueException is thrown.
     * @param messageId The Message ID.
     * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws IOException If there is an error accessing the IronMQ server.
     */
    public SubscribersInfo getPushStatusForMessage(String messageId) throws IOException {
        String url = "queues/" + name + "/messages/" + messageId + "/subscribers";
        Reader reader = client.get(url);
        Gson gson = new Gson();
        SubscribersInfo subscribersInfo = gson.fromJson(reader, SubscribersInfo.class);
        reader.close();
        return subscribersInfo;
    }

    /**
     * Delete push message for subscriber by subscriber ID and message ID. If there is no message or subscriber,
     * an EmptyQueueException is thrown.
     * @param subscriberId The Subscriber ID.
     * @param messageId The Message ID.
     * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws IOException If there is an error accessing the IronMQ server.
     */
    public void deletePushMessageForSubscriber(String messageId, String subscriberId) throws  IOException {
        client.delete("queues/" + name + "/messages/" + messageId + "/subscribers/" + subscriberId);
    }

    static class UpdateQueue {
        private String pushType;
        private int retries;
        private int retriesDelay;
        private String error_queue;
        private ArrayList<Subscriber> subscribers;
        private ArrayList<Alert> alerts;

        public UpdateQueue(ArrayList<Subscriber> subscribers, ArrayList<Alert> alerts, String pushType, String errorQueue, int retries, int retriesDelay) {
            this.subscribers = subscribers;
            this.alerts = alerts;
            this.pushType = pushType;
            this.error_queue = errorQueue;
            this.retries = retries;
            this.retriesDelay = retriesDelay;
        }
    }

    /**
     * Update queue. If there is no queue, an EmptyQueueException is thrown.
     * @param subscribersList The subscribers list.
     * @param alertsList The alerts list.
     * @param pushType The push type - multicast or unicast.
     * @param retries The retries.
     * @param retriesDelay The retries delay.
     * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws IOException If there is an error accessing the IronMQ server.
     */
    public QueueModel updateQueue(ArrayList<Subscriber> subscribersList, ArrayList<Alert> alertsList, String pushType, String errorQueue, int retries, int retriesDelay) throws IOException {
        String url = "queues/" + name;
        UpdateQueue updateQueue = new UpdateQueue(subscribersList, alertsList, pushType, errorQueue, retries, retriesDelay);
        Gson gson = new Gson();
        String jsonMessages = gson.toJson(updateQueue);
        Reader reader = client.post(url, jsonMessages);
        QueueModel message = gson.fromJson(reader, QueueModel.class);
        reader.close();
        return message;
    }

    public QueueModel updateQueue(ArrayList<Subscriber> subscribersList, ArrayList<Alert> alertsList, String pushType, int retries, int retriesDelay) throws IOException {
        return updateQueue(subscribersList,alertsList,pushType,"",retries,retriesDelay);
    }

    /**
     * Add alerts to a queue. If there is no queue, an EmptyQueueException is thrown.
     * @param alerts The array list of alerts.
     * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws IOException If there is an error accessing the IronMQ server.
     */
    public void addAlertsToQueue(ArrayList<Alert> alerts) throws IOException {
        String url = "queues/" + name + "/alerts";
        Alerts alert = new Alerts(alerts);
        Gson gson = new Gson();
        String jsonMessages = gson.toJson(alert);
        Reader reader = client.post(url, jsonMessages);
        reader.close();
    }

    /**
     * Replace current queue alerts with a given list of alerts. If there is no queue, an EmptyQueueException is thrown.
     * @param alerts The array list of alerts.
     * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws IOException If there is an error accessing the IronMQ server.
     */
    public void updateAlertsToQueue(ArrayList<Alert> alerts) throws IOException {
        String url = "queues/" + name + "/alerts";
        Alerts alert = new Alerts(alerts);
        Gson gson = new Gson();
        String jsonMessages = gson.toJson(alert);
        Reader reader = client.put(url, jsonMessages);
        reader.close();
    }

    /**
     * Delete alerts from a queue. If there is no queue, an EmptyQueueException is thrown.
     * @param alert_ids The array list of alert ids.
     * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws IOException If there is an error accessing the IronMQ server.
     */
    public void deleteAlertsFromQueue(ArrayList<Alert> alert_ids) throws IOException {
        String url = "queues/" + name + "/alerts";
        Alerts alert = new Alerts(alert_ids);
        Gson gson = new Gson();
        String jsonMessages = gson.toJson(alert);
        Reader reader = client.delete(url, jsonMessages);
        reader.close();
    }

    /**
     * Delete alert from a queue by alert id. If there is no queue, an EmptyQueueException is thrown.
     * @param alert_id The alert id.
     * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws IOException If there is an error accessing the IronMQ server.
     */
    public void deleteAlertFromQueueById(String alert_id) throws IOException {
        String url = "queues/" + name + "/alerts/" + alert_id;
        client.delete(url);
    }
}
