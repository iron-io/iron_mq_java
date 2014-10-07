package io.iron.ironmq;

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
        client = new Client();
    }

    /**
     * This test tries to connect to ironmq server using invalid project_id and token
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
     * This is the simplest way of posting the message
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
     * This test shows old way of reserving a message queue.get()
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
     * Queue,reserve() should raise exception if queue is empty
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
     * Reservation of several messages should not raise exception id queue is empty
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
     * This test shows simplest way of reserving the message
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
     * This test shows how to get message using it's id.
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
     * This test shows 2 ways how reserved message could be deleted
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

    @Test(expected = HTTPException.class)
    public void testReleaseMessageWithoutReservationId() throws IOException, InterruptedException {
        Queue queue = new Queue(client, "my_queue_" + ts());
        queue.push("Test message");
        Message message = queue.reserve(1, 5).getMessage(0);

        Thread.sleep(500);
        queue.releaseMessage(message.getId(), 0);
    }

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

    @Test
    public void testListQueues() throws IOException {
        createQueueWithMessage("my_queue_" + ts());
        ArrayList<QueueModel> allQueues = Queues.getQueues(client);

        Assert.assertTrue(allQueues.size() > 0);
        Assert.assertTrue(allQueues.get(0).getName().length() > 0);
        Assert.assertNull("Expect json with only names", allQueues.get(0).getProject_id());
        Assert.assertNull("Expect json with only names", allQueues.get(0).getId());
    }

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

    @Test
    public void testClearQueue() throws IOException {
        Queue queue = new Queue(client, "my_queue_" + ts());
        queue.push("Some message");
        Assert.assertTrue(queue.getInfoAboutQueue().getSize() > 0);
        queue.clear();
        Assert.assertEquals(0, queue.getInfoAboutQueue().getSize());
    }

    @Test(expected = HTTPException.class)
    public void testGetInfoBeforeQueueCreated() throws IOException {
        Queue queue = new Queue(client, "my_queue_" + ts());
        QueueModel info = queue.getInfoAboutQueue();
    }

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

    @Test
    public void testUpdateQueue() throws IOException {
        String name = "my_queue_" + ts();
        Queue queue = new Queue(client, name);

        QueueModel payload = new QueueModel();
        payload.setMessageTimeout(69);
        QueueModel info = queue.update(payload);

        Assert.assertEquals(69, info.getMessageTimeout());
    }

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

    @Test(expected = HTTPException.class)
    public void testDeleteQueue() throws IOException, InterruptedException {
        String queueName = "my_queue_" + ts();
        Queue queue = new Queue(client, queueName);
        queue.push("Some message");
        queue.destroy();
        Queue sameQueue = new Queue(client, queueName);

        sameQueue.getInfoAboutQueue();
    }

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

    @Test
    public void testLongPolling() throws IOException {
        Queue queue = client.queue("qaas-qa-bm903k");
        queue.push("test");
        queue.clear();
        Date start = new Date();
        System.out.println("Listening to queue:      " + start);
        queue.reserve(1, 30, 5);
        Date end = new Date();
        System.out.println("Stop to listen to queue: " + end);
        Assert.assertTrue(end.getTime() - start.getTime() > 4);
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
