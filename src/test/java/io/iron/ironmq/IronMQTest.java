package io.iron.ironmq;

import io.iron.ironmq.keystone.KeystoneIdentity;
import io.iron.ironmq.keystone.Tenant;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.sound.midi.VoiceStatus;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

public class IronMQTest {
    private String queueName = "java-testing-queue";
    private Client client;

    @Before
    public void setUp() throws Exception {
        client = new Client(null, null, null, 3, 1);
    }

    /**
     * This test tries to connect to an ironmq server using an invalid project_id and token
     * Expected result is HTTPException
     */
    @Test(expected = HTTPException.class)
    public void testErrorResponse() throws IOException {
        // intentionally invalid project/token combination
        Client client = new Client("4444444444444", "aaaaaa");
        Queue queue = client.queue("test-queue");
        queue.push("test");
    }

    /**
     * This is the simplest way of posting a message
     * Expected that server will respond with id of the message
     * @throws IOException
     */
    @Test
    public void testPostMessage() throws IOException {
        Queue queue = new Queue(client, "my_queue_" + ts());
        String messageId = queue.push("Test message");
        Assert.assertTrue(messageId.length() > 0);
    }

    /**
     * This test shows old way of reserving a message using queue.get()
     * Test placed here to check backward compatibility
     * @throws IOException
     */
    @Test
    public void testReserveMessageViaGet() throws IOException {
        Queue queue = new Queue(client, "my_queue_" + ts());
        String messageText = "Test message " + ts();
        String messageId = queue.push(messageText);
        Message message = queue.get();

        Assert.assertTrue(message.getReservationId().length() > 0);
        Assert.assertEquals(messageId, message.getId());
        Assert.assertEquals(messageText, message.getBody());
        Assert.assertEquals(1, message.getReservedCount());
    }

    /**
     * Queue.reserve() should raise exception if queue is empty
     * Expected result is EmptyQueueException
     * @throws IOException
     */
    @Test(expected = EmptyQueueException.class)
    public void testReserveMessageFromEmptyQueue() throws IOException {
        Queue queue = new Queue(client, "my_queue_" + ts());
        queue.push("");
        queue.clear();
        Message message = queue.reserve();
    }

    /**
     * Reservation of several messages should not raise exception if queue is empty
     * Expected result: empty collection of messages
     * @throws IOException
     */
    @Test
    public void testReserveMessagesFromEmptyQueue() throws IOException {
        Queue queue = new Queue(client, "my_queue_" + ts());
        queue.push("");
        queue.clear();
        Messages messages = queue.reserve(4);
        Assert.assertEquals(0, messages.getSize());
        Assert.assertEquals(0, messages.getMessages().length);
    }

    /**
     * This test shows the easiest way to reserve a message
     * Expected:
     * - Message has id and reservation id
     * - Message has information that it has been reserved one time
     * @throws IOException
     */
    @Test
    public void testReserveMessage() throws IOException {
        Queue queue = new Queue(client, "my_queue_" + ts());
        String messageText = "Test message " + ts();
        String messageId = queue.push(messageText);
        Message message = queue.reserve();

        Assert.assertTrue(message.getReservationId().length() > 0);
        Assert.assertEquals(messageId, message.getId());
        Assert.assertEquals(messageText, message.getBody());
        Assert.assertEquals(1, message.getReservedCount());
    }

    /**
     * This test shows how to reserve multiple messages from a queue
     * Expected:
     * - Each message in result collection has id and reservation id
     * - Each message in result collection has information that it has been reserved one time
     * @throws IOException
     */
    @Test
    public void testReserveMessages() throws IOException {
        Queue queue = new Queue(client, "my_queue_" + ts());
        String messageText = "Test message " + ts();
        Ids ids = queue.pushMessages(new String[]{messageText + "0", messageText + "1", messageText + "2"});
        Messages messages = queue.reserve(4);

        Assert.assertTrue(messages.getSize() > 0);
        Assert.assertTrue(messages.getSize() < 4); // count of messages could be less than 4 in two cases:
                                                   // - there are less than 4 messages in queue (not in this test)
                                                   // - messages were pushed with big intervals between and IronMQ
                                                   //   can't find all within acceptable interval of time.
                                                   // for more info look at
                                                   // http://dev.iron.io/mq-onpremise/reference/api/#reserve-messages

        for (int i = 0; i < messages.getSize(); i++) {
            Assert.assertTrue(messages.getMessage(i).getReservationId().length() > 0);
            Assert.assertEquals(ids.getId(i), messages.getMessage(i).getId());
            Assert.assertEquals(messageText + i, messages.getMessage(i).getBody());
            Assert.assertEquals(1, messages.getMessage(i).getReservedCount());
        }
    }

    /**
     * This test shows how to use long-polling.
     * Expected that response time will be greater than long-polling interval (`wait` parameter)
     * because queue is empty
     * You can add the message in separate thread 2 seconds after the `queue.reserve(...);` call
     * and queue reserve will finish immediately
     * @throws IOException
     */
    @Test
    public void testReserveMessageWithWait() throws IOException {
        Queue queue = new Queue(client, "my_queue_" + ts());
        queue.push("test");
        queue.clear();

        int intervalInSeconds = 3;

        long start = new Date().getTime();
        Messages messages = queue.reserve(4, 60, intervalInSeconds);
        long finish = new Date().getTime();

        Assert.assertEquals(0, messages.getSize());
        System.out.println(finish - start);
        Assert.assertTrue(finish - start > intervalInSeconds * 1000);
        Assert.assertTrue(finish - start < intervalInSeconds * 2 * 1000);
    }

    /**
     * This test shows how to peek a message from a queue
     * Expected that
     * - Messsage has id and body
     * - Count of reservations is equal to 0 and reservation id is empty because peek doesn't reserve the message
     * - Second call of peek will return the same message because peek doesn't reserve the message
     * @throws IOException
     */
    @Test
    public void testPeekMessage() throws IOException {
        Queue queue = new Queue(client, "my_queue_" + ts());
        String messageText = "Test message " + ts();
        String messageId = queue.push(messageText);
        Message message = queue.peek();

        Assert.assertNull(message.getReservationId());
        Assert.assertEquals(messageId, message.getId());
        Assert.assertEquals(messageText, message.getBody());
        Assert.assertEquals(0, message.getReservedCount());

        Message sameMessage = queue.peek();
        Assert.assertEquals(message.getId(), sameMessage.getId());
    }

    /**
     * This test shows how to get a message using its id.
     * Please note that:
     * - Message would not be reserved with this command
     * - You wouldn't be able to delete it if the message is reserved by another user (because you havent reservation id)
     * Expected that:
     * - Messsage has id and body
     * - Count of reservations is equal to 0 and reservation id is empty because peek doesn't reserve the message
     * @throws IOException
     */
    @Test
    public void testGetMessageById() throws IOException {
        Queue queue = new Queue(client, "my_queue_" + ts());
        String messageText = "Test message " + ts();
        String messageId = queue.push(messageText);
        Message message = queue.getMessageById(messageId);

        Assert.assertNull(message.getReservationId());
        Assert.assertEquals(messageId, message.getId());
        Assert.assertEquals(messageText, message.getBody());
        Assert.assertEquals(0, message.getReservedCount());
    }

    /**
     * This test shows 2 ways that a reserved message can be deleted
     * Please note that
     * - Size of queue will not change after reservation. It will decrease only when message has been deleted.
     * Expected that
     * - Size of queue will decrease
     * @throws IOException
     */
    @Test
    public void testDeleteReservedMessage() throws IOException {
        Queue queue = createQueueWithMessage("my_queue_" + ts());
        queue.clear();
        queue.push("Test message");
        Message message = queue.reserve();

        Assert.assertEquals(1, queue.getInfoAboutQueue().getSize());
        queue.deleteMessage(message);
        // or second way:
        // queue.deleteMessage(message.getId(), message.getReservationId());
        Assert.assertEquals(0, queue.getInfoAboutQueue().getSize());
    }

    /**
     * This test shows how you should not delete a reserved message
     * Expected:
     * - Exception, because way of deleting non-reserved message (by id) is not acceptable
     *   for deleting reserved messages
     * Note:
     *   Method queue.deleteMessage(Message) is more convenient because it deletes reserved messages with
     *   reservation id and non-reserved messages without it.
     * @throws IOException
     */
    @Test(expected = HTTPException.class)
    public void testDeleteReservedMessageWithoutReservationId() throws IOException {
        Queue queue = new Queue(client, "my_queue_" + ts());
        queue.clear();
        queue.push("Test message");
        Message message = queue.reserve();

        // this way of deleting is acceptable for non reserved messages,
        // for example for messages which was got by id
        queue.deleteMessage(message.getId());
        Assert.assertEquals(0, queue.getInfoAboutQueue().getSize());
    }

    /**
     * This test shows how to delete reserved messages
     * Please note that
     * - Size of queue will not change after reservation. It will decrease only when messages have been deleted.
     * Expected that
     * - Size of queue will decrease
     * @throws IOException
     */
    @Test
    public void testDeleteReservedMessages() throws IOException {
        Queue queue = createQueueWithMessage("my_queue_" + ts());
        queue.clear();
        queue.push("Test message 1");
        queue.push("Test message 2");
        Messages messages = queue.reserve(2);

        Assert.assertEquals(2, queue.getInfoAboutQueue().getSize());
        queue.deleteMessages(messages);
        Assert.assertEquals(0, queue.getInfoAboutQueue().getSize());
    }

    /**
     * This test shows how to delete a partial set of the reserved messages.
     * Use case:
     *   User gets a bunch of messages and processes these messages. Some messages could not be processed
     *   with current consumer script. Unprocessed messages should not be removed, rather released back to queue.
     *   User wants to delete processed with single request to increase performance.
     * @throws IOException
     */
    @Test
    public void testDeleteReservedMessagesPartially() throws IOException {
        Queue queue = createQueueWithMessage("my_queue_" + ts());
        queue.clear();
        queue.pushMessages(new String[]{"Test message 1", "Test message 2", "Test message 3", "Test message 4"});
        Messages messages = queue.reserve(4);

        Assert.assertEquals(4, queue.getInfoAboutQueue().getSize());

        Messages messagesToDelete = new Messages();
        messagesToDelete.add(messages.getMessage(1));
        messagesToDelete.add(messages.getMessage(3));
        queue.deleteMessages(messagesToDelete);

        Assert.assertEquals(2, queue.getInfoAboutQueue().getSize());
    }

    /**
     * This test just verifies that deleteMessages method should raise an exception for
     * reserved messages without reservation ids
     * Expected:
     * - HTTPException
     * @throws IOException
     */
    @Test(expected = HTTPException.class)
    public void testDeleteReservedMessagesWithoutReservationId() throws IOException {
        Queue queue = new Queue(client, "my_queue_" + ts());
        queue.clear();
        queue.push("Test message 1");
        queue.push("Test message 2");
        Messages messages = queue.reserve(2);

        messages.getMessage(0).setReservationId(null);
        queue.deleteMessages(messages);
    }

    /**
     * Test shows how to increase time of message reservation
     * Expected that:
     * - message will be available after 5 seconds (initial timeout is 5 seconds)
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testTouchMessage() throws IOException, InterruptedException {
        Queue queue = new Queue(client, "my_queue_" + ts());
        queue.push("Test message");
        Message message = queue.reserve(1, 5).getMessage(0);

        Thread.sleep(3500);
        queue.touchMessage(message);
        Thread.sleep(3500);
        Assert.assertEquals(0, queue.reserve(1).getSize());
    }

    /**
     * This test shows how to release a message back to queue if, for example, it could not be processed
     * Expected that:
     * - Message should return to the queue and be available for reserving
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testReleaseMessage() throws IOException, InterruptedException {
        Queue queue = new Queue(client, "my_queue_" + ts());
        queue.push("Test message");
        Message message = queue.reserve(1, 5).getMessage(0);

        Thread.sleep(500);
        queue.releaseMessage(message);

        Message sameMessage = queue.reserve();
        Assert.assertEquals(message.getId(), sameMessage.getId());
    }

    /**
     * This test shows that a reserved message could not be released without a reservation id
     * Note:
     * - queue.releaseMessage(id, delay) has been deprecated
     * - reserved message can't be touched without reservation id as well
     * Expected:
     * - HTTPException (403) Wrong reservation_id
     * @throws IOException
     * @throws InterruptedException
     */
    @Test(expected = HTTPException.class)
    public void testReleaseMessageWithoutReservationId() throws IOException, InterruptedException {
        Queue queue = new Queue(client, "my_queue_" + ts());
        queue.push("Test message");
        Message message = queue.reserve(1, 5).getMessage(0);

        Thread.sleep(500);
        queue.releaseMessage(message.getId(), 0);
    }

    /**
     * This test shows old way of listing queues. Don't use it.
     * @throws IOException
     */
    @Test
    public void testListQueuesOldWay() throws IOException {
        createQueueWithMessage("my_queue_" + ts());
        Queues queues = new Queues(client);
        ArrayList<QueueModel> allQueues = queues.getAllQueues();

        Assert.assertTrue(allQueues.size() > 0);
        Assert.assertTrue(allQueues.get(0).getName().length() > 0);
        Assert.assertNull("Expect json with only names", allQueues.get(0).getProject_id());
        Assert.assertNull("Expect json with only names", allQueues.get(0).getId());
    }

    /**
     * This test shows the easiest way to get a list of queues
     * Expected
     * - list of names of queues
     * @throws IOException
     */
    @Test
    public void testListQueues() throws IOException {
        createQueueWithMessage("my_queue_" + ts());
        ArrayList<QueueModel> allQueues = Queues.getQueues(client);

        Assert.assertTrue(allQueues.size() > 0);
        Assert.assertTrue(allQueues.get(0).getName().length() > 0);
        Assert.assertNull("Expect json with only names", allQueues.get(0).getProject_id());
        Assert.assertNull("Expect json with only names", allQueues.get(0).getId());
    }

    /**
     * This test checks listing of queues with default pagination
     * More information available here: http://dev.iron.io/mq-onpremise/reference/api/#list-queues
     *                        and here: http://dev.iron.io/mq-onpremise/reference/api/#changes
     * @throws IOException
     */
    @Test
    public void testListQueuesDefaultPagination() throws IOException {
        int defaultPageSize = 30;
        for (int i = 0; i < defaultPageSize + 3; i++) {
            createQueueWithMessage(repeatString("z", i + 1));
        }
        ArrayList<QueueModel> queues = Queues.getQueues(client);

        Assert.assertTrue(queues.size() > 0);
        Assert.assertTrue(queues.size() <= defaultPageSize);
        Assert.assertTrue(queues.get(queues.size() - 1).getName().compareTo(repeatString("z", defaultPageSize + 2)) < 0);
    }

    /**
     * This test shows how to list queues with pagination
     * More information available here: http://dev.iron.io/mq-onpremise/reference/api/#list-queues
     *                        and here: http://dev.iron.io/mq-onpremise/reference/api/#changes
     * @throws IOException
     */
    @Test
    public void testListQueuesPagination() throws IOException {
        int pageSize = 4;
        for (int i = 0; i < pageSize * 2; i++) {
            createQueueWithMessage(repeatString("a", i + 1));
        }
        ArrayList<QueueModel> queues = Queues.getQueues(client, "aa", pageSize, null);

        Assert.assertTrue(queues.size() == pageSize);
        for (int i = 0; i < queues.size(); i++) {
            Assert.assertEquals(repeatString("a", i + 3), queues.get(i).getName());
        }

        Assert.assertEquals(2, Queues.getQueues(client, "aaaaaaa", 2, null).size());
        Assert.assertEquals("aaaaaaaa", Queues.getQueues(client, "aaaaaaa").get(0).getName());
        Assert.assertEquals(3, Queues.getQueues(client, 3).size());
    }

    /**
     * This test shows how to list queues with filtering
     * More information available here: http://dev.iron.io/mq-onpremise/reference/api/#list-queues
     *                        and here: http://dev.iron.io/mq-onpremise/reference/api/#changes
     * @throws IOException
     */
    @Test
    public void testListQueuesFiltering() throws IOException {
        String[] queueNames = new String[]{"abba", "abbca", "abbcb", "abbcd", "abbdd"};
        for (int i = 0; i < queueNames.length; i++)
            createQueueWithMessage(queueNames[i]);

        ArrayList<QueueModel> queues = Queues.getQueues(client, null, null, "abbc");

        Assert.assertTrue(queues.size() == 3);
        for (int i = 0; i < queues.size(); i++) {
            Assert.assertEquals(queueNames[i + 1], queues.get(i).getName());
        }
    }

    /**
     * This test shows how to retrieve information about a queue
     * Expected that:
     * - Queue will have name, size and total messages count
     * @throws IOException
     */
    @Test
    public void testGetQueueInfo() throws IOException {
        String queueName = "my_queue_" + ts();
        Queue queue = new Queue(client, queueName);
        for (int i = 0; i < 3; i++)
            queue.push("Some message");
        QueueModel info = queue.getInfoAboutQueue();

        Assert.assertEquals(queueName, info.getName());
        Assert.assertFalse(info.getProjectId().isEmpty());
        Assert.assertEquals(3, info.getSize());
        Assert.assertEquals(3, info.getTotalMessages());
    }

    /**
     * This test shows how to clear all messages from a queue
     * Expected that:
     * - after cleaning queue will contain no messages
     * @throws IOException
     */
    @Test
    public void testClearQueue() throws IOException {
        Queue queue = new Queue(client, "my_queue_" + ts());
        queue.push("Some message");
        Assert.assertTrue(queue.getInfoAboutQueue().getSize() > 0);
        queue.clear();
        Assert.assertEquals(0, queue.getInfoAboutQueue().getSize());
    }

    /**
     * This test shows that a queue can't be cleared until it has been created
     * Expected
     * - HTTPException (404) Queue not found
     * @throws IOException
     */
    @Test(expected = HTTPException.class)
    public void testGetInfoBeforeQueueCreated() throws IOException {
        Queue queue = new Queue(client, "my_queue_" + ts());
        QueueModel info = queue.getInfoAboutQueue();
    }

    /**
     * This test shows the easist way to create a queue 
     * @throws IOException
     */
    @Test
    public void testCreateQueue() throws IOException {
        String name = "my_queue_" + ts();
        Queue queue = new Queue(client, name);

        QueueModel response = queue.create();
        Assert.assertEquals(name, response.getName());
        Assert.assertEquals(60, response.getMessageTimeout());

        QueueModel info = queue.getInfoAboutQueue();
        Assert.assertEquals(name, info.getName());
    }

    /**
     * This test shows how to create a queue and specify it's parameters
     * Using the QueueModel class is preferred way of specifying parameters of queue
     * Expected that:
     * - Created queue will have all specified parameters
     * @throws IOException
     */
    @Test
    public void testCreateQueueWithParams() throws IOException {
        String name = "my_queue_" + ts();
        Queue queue = new Queue(client, name);

        QueueModel payload = new QueueModel();
        payload.setMessageTimeout(69);
        payload.setMessageExpiration(404);
        QueueModel info = queue.update(payload);

        Assert.assertEquals(69, info.getMessageTimeout());
        Assert.assertEquals(404, info.getMessageExpiration());
    }

    /**
     * This test shows how to create a push queue
     * To create a queue we need to pass array-list of subscribers, specify queue type, retries count and retries delay
     * @throws IOException
     */
    @Test
    public void testCreateQueueOverload2() throws IOException {
        String name = "my_queue_" + ts();
        Queue queue = new Queue(client, name);

        ArrayList<Subscriber> subs = new ArrayList<Subscriber>(){{ add(new Subscriber("http://localhost:3000/", "test")); }};

        QueueModel response = queue.create(subs, null, "multicast", 5, 3);
        Assert.assertEquals(name, response.getName());
        Assert.assertEquals(60, response.getMessageTimeout());
        Assert.assertEquals("multicast", response.getType());
        Assert.assertEquals(5, response.getPushInfo().getRetries().intValue());
        Assert.assertEquals(3, response.getPushInfo().getRetriesDelay().intValue());
        Assert.assertEquals(1, response.getPushInfo().getSubscribers().size());

        QueueModel info = queue.getInfoAboutQueue();
        Assert.assertEquals(name, info.getName());
    }

    /**
     * This test shows how to create a push queue
     * To create a queue we need to pass array-list of subscribers, specify queue type, name of error-queue,
     * retries count and retries delay
     * @throws IOException
     */
    @Test
    public void testCreateQueueOverload3() throws IOException {
        String name = "my_queue_" + ts();
        Queue queue = new Queue(client, name);

        ArrayList<Subscriber> subs = new ArrayList<Subscriber>(){{ add(new Subscriber("http://localhost:3000/", "test")); }};

        QueueModel response = queue.create(subs, null, "multicast", "err_q", 5, 3);
        Assert.assertEquals(name, response.getName());
        Assert.assertEquals(60, response.getMessageTimeout());
        Assert.assertEquals("multicast", response.getType());
        Assert.assertEquals(5, response.getPushInfo().getRetries().intValue());
        Assert.assertEquals(3, response.getPushInfo().getRetriesDelay().intValue());
        Assert.assertEquals("err_q", response.getPushInfo().getErrorQueue());
        Assert.assertEquals(1, response.getPushInfo().getSubscribers().size());

        QueueModel info = queue.getInfoAboutQueue();
        Assert.assertEquals(name, info.getName());
    }

    /**
     * This test shows how to update a queue
     * Feel free to use the QueueModel class to specify other parameters of queue
     * @throws IOException
     */
    @Test
    public void testUpdateQueue() throws IOException {
        String name = "my_queue_" + ts();
        Queue queue = new Queue(client, name);

        QueueModel payload = new QueueModel();
        payload.setMessageTimeout(69);
        QueueModel info = queue.update(payload);

        Assert.assertEquals(69, info.getMessageTimeout());
    }

    /**
     * This test shows how to update subscribers of a queue
     * Expected:
     * - old subscribers should be replaced by new subscribers
     * @throws IOException
     */
    @Test
    public void testUpdateQueueSubscribers() throws IOException {
        String name = "my_queue_" + ts();
        Queue queue = new Queue(client, name);

        QueueModel payload = new QueueModel();
        payload.addSubscriber(new Subscriber("http://localhost:3000", "test01"));
        payload.addSubscriber(new Subscriber("http://localhost:3030", "test02"));
        payload.addSubscriber(new Subscriber("http://localhost:3333", "test03"));
        QueueModel info = queue.update(payload);

        Assert.assertEquals(3, info.getPushInfo().getSubscribers().size());

        ArrayList<Subscriber> subscribers = new ArrayList<Subscriber>();
        subscribers.add(new Subscriber("http://localhost:3000", "test04"));
        subscribers.add(new Subscriber("http://localhost:3030", "test05"));
        QueueModel info2 = queue.updateSubscribers(subscribers);

        Assert.assertEquals(2, info2.getPushInfo().getSubscribers().size());
    }

    /**
     * This test shows how to update parameters of a push queue
     * @throws IOException
     */
    @Test
    public void testUpdateQueuePushParameters() throws IOException {
        String name = "my_queue_" + ts();
        final String url = "http://localhost:3000";
        Queue queue = new Queue(client, name);

        ArrayList<Subscriber> subscribers = new ArrayList<Subscriber>() {{ add(new Subscriber(url, "test")); }};
        QueueModel payload = new QueueModel(new QueuePushModel(subscribers, 4, 7, "test_err"));
        QueueModel info = queue.update(payload);

        Assert.assertEquals("test_err", info.getPushInfo().getErrorQueue());
        Assert.assertEquals("multicast", info.getType());
        Assert.assertEquals(4, info.getPushInfo().getRetries().intValue());
        Assert.assertEquals(7, info.getPushInfo().getRetriesDelay().intValue());

        Assert.assertEquals(1, info.getPushInfo().getSubscribers().size());
        Assert.assertEquals(url, info.getPushInfo().getSubscribers().get(0).getUrl());
    }

    /**
     * This test shows how to update alerts of a queue
     * Expected that:
     * - new alert will be available after update
     * @throws IOException
     */
    @Test
    public void testUpdateQueueAlerts() throws IOException {
        Queue queue = new Queue(client, "my_queue_" + ts());

        ArrayList<Alert> alerts = new ArrayList<Alert>();
        alerts.add(new Alert(Alert.typeProgressive, Alert.directionAscending, 5, "some_q"));
        QueueModel info = queue.updateAlerts(alerts);

        Assert.assertEquals(5, info.getAlerts().get(0).getTrigger());
        Assert.assertEquals(Alert.directionAscending, info.getAlerts().get(0).getDirection());
        Assert.assertEquals(Alert.typeProgressive, info.getAlerts().get(0).getType());
        Assert.assertEquals("some_q", info.getAlerts().get(0).getQueue());
    }

    /**
     * This test shows how to delete a queue
     * Expected:
     * - HTTPException (404), because queue is no longer available after deleting, so, getInfoAboutQueue
     *   should raise an exception
     * @throws IOException
     * @throws InterruptedException
     */
    @Test(expected = HTTPException.class)
    public void testDeleteQueue() throws IOException, InterruptedException {
        String queueName = "my_queue_" + ts();
        Queue queue = new Queue(client, queueName);
        queue.push("Some message");
        queue.get(1,30);
        queue.destroy();
        Queue sameQueue = new Queue(client, queueName);

        sameQueue.getInfoAboutQueue();
    }

    /**
     * This test shows how to add subscribers to a queue.
     * @throws IOException
     */
    @Test
    public void testAddSubscribers() throws IOException {
        String name = "my_queue_" + ts();
        Queue queue = new Queue(client, name);

        QueueModel payload = new QueueModel();
        payload.addSubscriber(new Subscriber("http://localhost:3001", "test01"));
        queue.update(payload);
        queue.addSubscribers(new Subscriber[]{new Subscriber("http://localhost:3002", "test02")});

        QueueModel info = queue.getInfoAboutQueue();

        Assert.assertEquals(2, info.getSubscribers().size());
        Subscriber subscriber = info.getSubscribers().get(1);
        Assert.assertEquals("test02", subscriber.getName());
        Assert.assertEquals("http://localhost:3002", subscriber.getUrl());
    }

    /**
     * This test shows another way of replacing subscribers. First one is update() with setting new subscribers
     * via new QueueModel(new QueuePushModel(subscribersArrayList))
     * Expected:
     * - old subscribers to be deleted
     * - new subscribers to appear in the queue-info
     * @throws IOException
     */
    @Test
    public void testReplaceSubscribers() throws IOException {
        String name = "my_queue_" + ts();
        Queue queue = new Queue(client, name);

        QueueModel payload = new QueueModel();
        payload.addSubscriber(new Subscriber("http://localhost:3001", "test01"));
        queue.update(payload);
        queue.replaceSubscribers(new Subscriber[]{new Subscriber("http://localhost:3002", "test02")});

        QueueModel info = queue.getInfoAboutQueue();
        Assert.assertEquals(1, info.getSubscribers().size());
        Subscriber subscriber = info.getSubscribers().get(0);
        Assert.assertEquals("test02", subscriber.getName());
        Assert.assertEquals("http://localhost:3002", subscriber.getUrl());
    }

    @Test
    @Ignore // there is a bug in implementation of ironmq
    public void testRemoveSubscribers() throws IOException {
        String name = "my_queue_" + ts();
        Queue queue = new Queue(client, name);

        QueueModel payload = new QueueModel();
        Subscriber[] subscribers = new Subscriber[]{
                new Subscriber("http://localhost:3001", "test01"),
                new Subscriber("http://localhost:3002", "test02"),
        };
        payload.addSubscriber(subscribers[0]);
        payload.addSubscriber(subscribers[1]);
        queue.update(payload);
        queue.removeSubscribers(new Subscriber[]{subscribers[0]});

        QueueModel info = queue.getInfoAboutQueue();
        Assert.assertEquals(1, info.getSubscribers().size());
        Subscriber subscriber = info.getSubscribers().get(0);
        Assert.assertEquals(subscribers[1].getName(), subscriber.getName());
        Assert.assertEquals(subscribers[1].getUrl(), subscriber.getUrl());
    }

    private long ts() {
        return new Date().getTime();
    }

    private Queue createQueueWithMessage(String queueName) throws IOException {
        Queue queue = new Queue(client, queueName);
        queue.push("Test message");
        return queue;
    }

    private String repeatString(String s, int times) {
        return new String(new char[times]).replace("\0", s);
    }
}
