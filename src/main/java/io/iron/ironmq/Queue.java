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

    static final private Gson gson = new Gson();

    public Queue(Client client, String name) {
        if (name == null)
            throw new NullPointerException("Queue name cannot be null");
        this.client = client;
        this.name = name;
    }

    /**
     * Retrieves a Message from the queue and reserves it. If there are no items on the queue, an
     * EmptyQueueException is thrown.
     *
     * @throws io.iron.ironmq.EmptyQueueException If the queue is empty.
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     * @deprecated Use Queue.reserve() instead
     */
    @Deprecated
    public Message get() throws IOException {
        return reserve();
    }

    /**
     * Retrieves a Message from the queue and reserves it. If there are no items on the queue, an
     * EmptyQueueException is thrown.
     *
     * @throws io.iron.ironmq.EmptyQueueException If the queue is empty.
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public Message reserve() throws IOException {
        Messages msgs = reserve(1);
        Message msg;
        try {
            msg = msgs.getMessage(0);
        } catch (IndexOutOfBoundsException e) {
            throw new EmptyQueueException();
        }

        return msg;
    }

    /**
     * Retrieves Messages from the queue and reserves it. If there are no items on the queue, an
     * EmptyQueueException is thrown.
     * @param numberOfMessages The number of messages to receive. Max. is 100.
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     * @deprecated Use Queue.reserve(int) instead
     */
    @Deprecated
    public Messages get(int numberOfMessages) throws IOException {
        return reserve(numberOfMessages);
    }

    /**
     * Retrieves Messages from the queue and reserves it. If there are no items on the queue, an
     * EmptyQueueException is thrown.
     * @param numberOfMessages The number of messages to receive. Max. is 100.
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public Messages reserve(int numberOfMessages) throws IOException {
        return reserve(numberOfMessages, -1);
    }

    /**
     * Retrieves Messages from the queue and reserves it. If there are no items on the queue, an
     * EmptyQueueException is thrown.
     * @param numberOfMessages The number of messages to receive. Max. is 100.
     * @param timeout timeout in seconds.
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     * @deprecated Use Queue.reserve(int, int) instead
     */
    @Deprecated
    public Messages get(int numberOfMessages, int timeout) throws IOException {
        return reserve(numberOfMessages, timeout);
    }

    /**
     * Retrieves Messages from the queue and reserves it. If there are no items on the queue, an
     * EmptyQueueException is thrown.
     * @param numberOfMessages The number of messages to receive. Max. is 100.
     * @param timeout timeout in seconds.
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public Messages reserve(int numberOfMessages, int timeout) throws IOException {
        return reserve(numberOfMessages, timeout, 0);
    }

    /**
     * Retrieves Messages from the queue and reserves it. If there are no items on the queue, an
     * EmptyQueueException is thrown.
     * @param numberOfMessages The number of messages to receive. Max. is 100.
     * @param timeout timeout in seconds.
     * @param wait Time to long poll for messages, in seconds. Max is 30 seconds. Default 0.
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public Messages reserve(int numberOfMessages, int timeout, int wait) throws IOException {
        if (numberOfMessages < 1 || numberOfMessages > 100) {
            throw new IllegalArgumentException("numberOfMessages has to be within 1..100");
        }

        MessagesReservationModel payload = new MessagesReservationModel(numberOfMessages, timeout, wait);
        String url = "queues/" + name + "/reservations";
        IronReader reader = client.post(url, gson.toJson(payload));
        Messages messages = gson.fromJson(reader.reader, Messages.class);
        reader.close();
        return messages;
    }

    /**
     * Peeking at a queue returns the next messages on the queue, but it does not reserve them.
     * If there are no items on the queue, an EmptyQueueException is thrown.
     *
     * @throws io.iron.ironmq.EmptyQueueException If the queue is empty.
     * @throws io.iron.ironmq.HTTPException       If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException         If there is an error accessing the IronMQ server.
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
     * @param numberOfMessages The maximum number of messages to peek. Default is 1. Maximum is 100. Note: You may not
     *                         receive all n messages on every request, the more sparse the queue, the less likely
     *                         you are to receive all n messages.
     * @throws io.iron.ironmq.EmptyQueueException If the queue is empty.
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public Messages peek(int numberOfMessages) throws IOException {
        if (numberOfMessages < 1 || numberOfMessages > 100) {
            throw new IllegalArgumentException("numberOfMessages has to be within 1..100");
        }
        IronReader reader = client.get("queues/" + name + "/messages?n=" + numberOfMessages);
        try {
            return gson.fromJson(reader.reader, Messages.class);
        } finally {
            reader.close();
        }
    }

    /**
     * Touching a reserved message extends its timeout to the duration specified when the message was created.
     *
     * @param message The message to delete.
     *
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public MessageOptions touchMessage(Message message) throws IOException {
        return touchMessage(message, null);
    }

    /**
     * Touching a reserved message extends its timeout to the specified duration.
     *
     * @param message The message to delete.
     * @param timeout After timeout (in seconds), item will be placed back onto queue.
     *
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public MessageOptions touchMessage(Message message, int timeout) throws IOException {
        return touchMessage(message, (long) timeout);
    }

    /**
     * Touching a reserved message extends its timeout to the duration specified when the message was created.
     *
     * @param message The message to delete.
     * @param timeout After timeout (in seconds), item will be placed back onto queue.
     *
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public MessageOptions touchMessage(Message message, Long timeout) throws IOException {
        MessageOptions messageOptions = touchMessage(message.getId(), message.getReservationId(), timeout);
        message.setReservationId(messageOptions.getReservationId());
        return messageOptions;
    }

    /**
     * Touching a reserved message extends its timeout to the duration specified when the message was created.
     *
     * @param id The ID of the message to delete.
     * @param reservationId This id is returned when you reserve a message and must be provided to delete a message that is reserved.
     *
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public MessageOptions touchMessage(String id, String reservationId) throws IOException {
        return touchMessage(id, reservationId, null);
    }

    /**
     * Touching a reserved message extends its timeout to the specified duration.
     *
     * @param id The ID of the message to delete.
     * @param reservationId This id is returned when you reserve a message and must be provided to delete a message that is reserved.
     * @param timeout After timeout (in seconds), item will be placed back onto queue.
     *
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public MessageOptions touchMessage(String id, String reservationId, int timeout) throws IOException {
        return touchMessage(id, reservationId, (long) timeout);
    }

    /**
     * Touching a reserved message extends its timeout to the duration specified when the message was created.
     *
     * @param id The ID of the message to delete.
     * @param reservationId This id is returned when you reserve a message and must be provided to delete a message that is reserved.
     * @param timeout After timeout (in seconds), item will be placed back onto queue.
     *
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public MessageOptions touchMessage(String id, String reservationId, Long timeout) throws IOException {
        String payload = gson.toJson(new MessageOptions(null, reservationId, timeout));
        IronReader reader = client.post("queues/" + name + "/messages/" + id + "/touch", payload);
        try {
            return gson.fromJson(reader.reader, MessageOptions.class);
        } finally {
            reader.close();
        }
    }

    /**
     * Deletes a Message from the queue.
     *
     * @param id The ID of the message to delete.
     *
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public void deleteMessage(String id) throws IOException {
        deleteMessage(id, null);
    }

    /**
     * Deletes a Message from the queue.
     *
     * @param id The ID of the message to delete.
     * @param reservationId Reservation Id of the message. Reserved message could not be deleted without reservation Id.
     *
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public void deleteMessage(String id, String reservationId) throws IOException {
        deleteMessage(id, reservationId, null);
    }

    /**
     * Deletes a Message from the queue.
     *
     * @param id The ID of the message to delete.
     * @param reservationId Reservation Id of the message. Reserved message could not be deleted without reservation Id.
     *
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public void deleteMessage(String id, String reservationId, String subscriberName) throws IOException {
        String payload = gson.toJson(new SubscribedMessageOptions(reservationId, subscriberName));
        IronReader reader = client.delete("queues/" + name + "/messages/" + id, payload);
        reader.close();
    }

    /**
     * Deletes multiple messages from the queue.
     *
     * @param ids The IDs of the messages to delete.
     *
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public void deleteMessages(Ids ids) throws IOException {
        deleteMessages(ids.toMessageOptions());
    }

    /**
     * Deletes multiple messages from the queue.
     *
     * @param messages The list of the messages to delete.
     *
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public void deleteMessages(Messages messages) throws IOException {
        deleteMessages(messages.toMessageOptions());
    }

    private void deleteMessages(MessageOptions[] messages) throws IOException {
        String payload = gson.toJson(new MessagesOptions(messages));
        IronReader reader = client.delete("queues/" + name + "/messages", payload);
        reader.close();
    }

    /**
     * Destroy the queue.
     *
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public void destroy() throws IOException {
        IronReader reader = client.delete("queues/" + name);
        reader.close();
    }

    /**
     * Deletes a Message from the queue.
     *
     * @param msg The message to delete.
     *
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public void deleteMessage(Message msg) throws IOException {
        deleteMessage(msg.getId(), msg.getReservationId());
    }

    /**
     * Pushes a message onto the queue.
     *
     * @param msg The body of the message to push.
     * @return The new message's ID
     *
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
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
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public Ids pushMessages(String[] msg) throws IOException {
        return pushMessages(msg, 0);
    }

    /**
     * Pushes a message onto the queue.
     *
     * @param msg The body of the message to push.
     * @param delay The message's delay in seconds.
     * @return The new message's ID
     *
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public String push(String msg, long delay) throws IOException {
        Message message = new Message();
        message.setBody(msg);
        message.setDelay(delay);

        Messages msgs = new Messages(message);

        IronReader reader = client.post("queues/" + name + "/messages", msgs);
        Ids ids = gson.fromJson(reader.reader, Ids.class);
        reader.close();
        return ids.getId(0);
    }

    /**
     * Pushes a messages onto the queue.
     *
     * @param msg The array of the messages to push.
     * @param delay The message's delay in seconds.
     * @return The IDs of new messages
     *
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public Ids pushMessages(String[] msg, long delay) throws IOException {
        ArrayList<Message> messages = new ArrayList<Message>();
        for (String messageName: msg){
            Message message = new Message();
            message.setBody(messageName);
            message.setDelay(delay);
            messages.add(message);
        }

        MessagesArrayList msgs = new MessagesArrayList(messages);

        IronReader reader = client.post("queues/" + name + "/messages", msgs);
        Ids ids = gson.fromJson(reader.reader, Ids.class);
        reader.close();
        return ids;
    }

    /**
     * Clears the queue off all messages
     * @throws java.io.IOException
     */
    public void clear() throws IOException {
        IronReader reader = client.delete("queues/" + name + "/messages", "{}");
        reader.close();
    }

    /**
     * @return the name of this queue
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves Info about queue. If there is no queue, an EmptyQueueException is thrown.
     * @throws io.iron.ironmq.EmptyQueueException If there is no queue.
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public QueueModel getInfoAboutQueue() throws IOException {
        IronReader reader = client.get("queues/" + name);
        QueueContainer queueContainer = gson.fromJson(reader.reader, QueueContainer.class);
        reader.close();
        return queueContainer.getQueue();
    }

    /**
     * Retrieves Message from the queue by message id. If there are no items on the queue, an
     * EmptyQueueException is thrown.
     * @param id The ID of the message to get.
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public Message getMessageById(String id) throws IOException {
        String url = "queues/" + name + "/messages/" + id;
        IronReader reader = client.get(url);
        MessageContainer container = gson.fromJson(reader.reader, MessageContainer.class);
        reader.close();
        return container.getMessage();
    }

    static class Delay {
        private int delay;
        public Delay(int delay) {
            this.delay = delay;
        }
    }

    /**
     * Release reserved message after specified time. If there is no message with such id on the queue, an
     * EmptyQueueException is thrown.
     *
     * @param message The message to release.
     *
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public void releaseMessage(Message message) throws IOException {
        releaseMessage(message.getId(), message.getReservationId(), null);
    }

    /**
     * Release reserved message after specified time. If there is no message with such id on the queue, an
     * EmptyQueueException is thrown.
     *
     * @param message The message to release.
     * @param delay The time after which the message will be released.
     *
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public void releaseMessage(Message message, int delay) throws IOException {
        releaseMessage(message.getId(), message.getReservationId(), new Long(delay));
    }

    /**
     * Release reserved message after specified time. If there is no message with such id on the queue, an
     * EmptyQueueException is thrown.
     *
     * @param id The ID of the message to release.
     * @param delay The time after which the message will be released.
     *
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public void releaseMessage(String id, String reservationId, Long delay) throws IOException {
        String url = "queues/" + name + "/messages/" + id + "/release";
        String payload = gson.toJson(new MessageOptions(reservationId, delay));
        IronReader reader = client.post(url, payload);
        reader.close();
    }

    /**
     * Add subscribers to Queue. If there is no queue, an EmptyQueueException is thrown.
     * @param subscribersList The array list of subscribers.
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public void addSubscribers(ArrayList<Subscriber> subscribersList) throws IOException {
        addSubscribers(new Subscribers(subscribersList));
    }

    /**
     * Add subscribers to Queue. If there is no queue, an EmptyQueueException is thrown.
     * @param subscribers The array of subscribers.
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public void addSubscribers(Subscriber[] subscribers) throws IOException {
        addSubscribers(new Subscribers(subscribers));
    }

    /**
     * Add subscribers to Queue. If there is no queue, an EmptyQueueException is thrown.
     * @param subscribers The array of subscribers.
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public void addSubscribers(Subscribers subscribers) throws IOException {
        String payload = gson.toJson(subscribers);
        IronReader reader = client.post("queues/" + name + "/subscribers", payload);
        reader.close();
    }

    /**
     * Update old or add new subscribers to Queue. If there is no queue, an EmptyQueueException is thrown.
     * @param subscribersList The array list of subscribers.
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public void updateSubscribers(ArrayList<Subscriber> subscribersList) throws IOException {
        addSubscribers(subscribersList);
    }

    /**
     * Update old or add new subscribers to Queue. If there is no queue, an EmptyQueueException is thrown.
     * @param subscribers The array of subscribers.
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public void updateSubscribers(Subscriber[] subscribers) throws IOException {
        addSubscribers(subscribers);
    }

    /**
     * Update old or add new subscribers to Queue. If there is no queue, an EmptyQueueException is thrown.
     * @param subscribers The array of subscribers.
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public void updateSubscribers(Subscribers subscribers) throws IOException {
        addSubscribers(subscribers);
    }

    /**
     * Sets list of subscribers to a queue. Older subscribers will be removed.
     * If there is no queue, an EmptyQueueException is thrown.
     * @param subscribersList The array list of subscribers.
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public void replaceSubscribers(ArrayList<Subscriber> subscribersList) throws IOException {
        replaceSubscribers(new Subscribers(subscribersList));
    }

    /**
     * Sets list of subscribers to a queue. Older subscribers will be removed.
     * If there is no queue, an EmptyQueueException is thrown.
     * @param subscribers The array of subscribers.
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public void replaceSubscribers(Subscriber[] subscribers) throws IOException {
        replaceSubscribers(new Subscribers(subscribers));
    }

    /**
     * Sets list of subscribers to a queue. Older subscribers will be removed.
     * If there is no queue, an EmptyQueueException is thrown.
     * @param subscribers The array of subscribers.
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public void replaceSubscribers(Subscribers subscribers) throws IOException {
        String payload = gson.toJson(subscribers);
        IronReader reader = client.put("queues/" + name + "/subscribers", payload);
        reader.close();
    }

    /**
     * Remove subscribers from Queue. If there is no queue, an EmptyQueueException is thrown.
     * @param subscribersList The array list of subscribers.
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public void removeSubscribers(ArrayList<Subscriber> subscribersList) throws IOException {
        removeSubscribers(new Subscribers(subscribersList));
    }

    /**
     * Remove subscribers from Queue. If there is no queue, an EmptyQueueException is thrown.
     * @param subscribers The array list of subscribers.
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public void removeSubscribers(Subscriber[] subscribers) throws IOException {
        removeSubscribers(new Subscribers(subscribers));
    }

    /**
     * Remove subscribers from Queue. If there is no queue, an EmptyQueueException is thrown.
     * @param subscribers The array list of subscribers.
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public void removeSubscribers(Subscribers subscribers) throws IOException {
        String url = "queues/" + name + "/subscribers";
        String jsonMessages = gson.toJson(subscribers);
        IronReader reader = client.delete(url, jsonMessages);
        reader.close();
    }

    /**
     * Get push info of message by message id. If there is no message, an EmptyQueueException is thrown.
     * @param messageId The Message ID.
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public SubscribersInfo getPushStatusForMessage(String messageId) throws IOException {
        String url = "queues/" + name + "/messages/" + messageId + "/subscribers";
        IronReader reader = client.get(url);
        SubscribersInfo subscribersInfo = gson.fromJson(reader.reader, SubscribersInfo.class);
        reader.close();
        return subscribersInfo;
    }

    /**
     * Delete push message for subscriber by subscriber ID and message ID. If there is no message or subscriber,
     * an EmptyQueueException is thrown.
     * @param subscriberName The name of Subscriber.
     * @param messageId The Message ID.
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public void deletePushMessageForSubscriber(String messageId, String reservationId, String subscriberName) throws  IOException {
        deleteMessage(messageId, reservationId, subscriberName);
    }

    /**
     * Creates a queue for specified queue client.
     * If queue exists, it will be updated.
     */
    public QueueModel create() throws IOException {
        String url = "queues/" + name;
        IronReader reader = client.put(url, "{}");
        QueueContainer container = gson.fromJson(reader.reader, QueueContainer.class);
        reader.close();
        return container.getQueue();
    }

    /**
     * Creates a queue for specified queue client.
     * If queue exists, it will be updated.
     * @param subscribersList The subscribers list.
     * @param pushType The push type - multicast or unicast.
     * @param errorQueue The name of the error queue to use (can be null)
     * @param retries The retries.
     * @param retriesDelay The retries delay.
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public QueueModel create(ArrayList<Subscriber> subscribersList, String pushType, String errorQueue, int retries, int retriesDelay) throws IOException {
        QueueModel model = new QueueModel();
        model.setPushInfo(new QueuePushModel(subscribersList, retries, retriesDelay, errorQueue));
        model.setType(pushType);
        return create(model);
    }

    /**
     * Creates a queue for specified queue client.
     * If queue exists, it will be updated.
     * @param subscribersList The subscribers list.
     * @param pushType The push type - multicast or unicast.
     * @param retries The retries.
     * @param retriesDelay The retries delay.
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public QueueModel create(ArrayList<Subscriber> subscribersList, String pushType, int retries, int retriesDelay) throws IOException {
        return create(subscribersList, pushType, "", retries, retriesDelay);
    }

    /**
     * Creates a queue for specified queue client.
     * If queue exists, it will be updated.
     * @param model QueueModel instance with desired parameters of queue
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public QueueModel create(QueueModel model) throws IOException {
        String url = "queues/" + name;
        QueueContainer payload = new QueueContainer(model);

        IronReader reader = client.put(url, gson.toJson(payload));
        QueueContainer container = gson.fromJson(reader.reader, QueueContainer.class);
        reader.close();
        return container.getQueue();
    }

    /**
     * Update queue. If there is no queue, an EmptyQueueException is thrown.
     * @param subscribersList The subscribers list.
     * @param pushType The push type - multicast or unicast.
     * @param errorQueue The name of the error queue to use (can be null)
     * @param retries The retries.
     * @param retriesDelay The retries delay.
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public QueueModel updateQueue(ArrayList<Subscriber> subscribersList, String pushType, String errorQueue, int retries, int retriesDelay) throws IOException {
        QueueModel model = new QueueModel();
        model.setPushInfo(new QueuePushModel(subscribersList, retries, retriesDelay, errorQueue));
        model.setType(pushType);
        return update(model);
    }

    /**
     * Update queue. If there is no queue, an EmptyQueueException is thrown.
     * @param subscribersList The subscribers list.
     * @param pushType The push type - multicast or unicast.
     * @param retries The retries.
     * @param retriesDelay The retries delay.
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public QueueModel updateQueue(ArrayList<Subscriber> subscribersList, String pushType, int retries, int retriesDelay) throws IOException {
        return updateQueue(subscribersList, pushType, "", retries,retriesDelay);
    }

    public QueueModel update(QueueModel model) throws IOException {
        String url = "queues/" + name;
        QueueContainer payload = new QueueContainer(model);

        IronReader reader = client.patch(url, gson.toJson(payload));
        QueueContainer container = gson.fromJson(reader.reader, QueueContainer.class);
        reader.close();
        return container.getQueue();
    }

    /**
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */

    /**
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */

    /**
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
        

    /**
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
   

    /**
     
     * @throws io.iron.ironmq.HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
}
