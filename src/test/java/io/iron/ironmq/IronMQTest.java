package io.iron.ironmq;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

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
        client = setCredentials();
    }

    @Test(expected = HTTPException.class)
    public void testErrorResponse() throws IOException {
        // intentionally invalid project/token combination
        Client client = new Client("4444444444444", "aaaaaa", Cloud.ironAWSUSEast);
        Queue queue = client.queue("test-queue");
        queue.push("test");
    }

    @Test
    @Ignore
    public void testCreatingQueueAndMessage() throws IOException {
        String queueNameNew = queueName + "-new";
        Queue queue = new Queue(client, queueNameNew);

        String body = "Hello, IronMQ!";
        String id = queue.push(body, 10);

        QueueModel infoAboutQueue = queue.getInfoAboutQueue();
        Assert.assertEquals(1, infoAboutQueue.getSize());
        Assert.assertEquals(queueNameNew, infoAboutQueue.getName());

        Message msg = queue.get();
        Assert.assertEquals(body, msg.getBody());
        Assert.assertEquals(id, msg.getId());
        queue.deleteMessage(msg);
        infoAboutQueue = queue.getInfoAboutQueue();
        Assert.assertEquals(0, infoAboutQueue.getSize());
        queue.destroy();
    }

    @Test
    @Ignore
    public void testUpdatingQueue() throws IOException {
        // Create the main push queue
        String queueNameNew = queueName + "-update";
        Queue queue = new Queue(client, queueNameNew);

        Subscriber subscriber = new Subscriber("http://test.com");
        ArrayList<Subscriber> subscriberArrayList = new ArrayList<Subscriber>();
        subscriberArrayList.add(subscriber);

        QueueModel infoAboutQueue = queue.updateQueue(subscriberArrayList, null, "multicast", 60, 10);

        // Validate retries
        infoAboutQueue = queue.getInfoAboutQueue();
        Assert.assertEquals(60, infoAboutQueue.getRetries());
        Assert.assertEquals(10, infoAboutQueue.getRetriesDelay());


        // Update the queue
        QueueModel newInfoAboutQueue = queue.updateQueue(subscriberArrayList, infoAboutQueue.getAlerts(), "multicast", 100, 100);

        // Validate the error queue has been set
        Assert.assertEquals(100, newInfoAboutQueue.getRetries());
        Assert.assertEquals(100, newInfoAboutQueue.getRetriesDelay());

        // Clean up
        queue.destroy();
    }


    @Test
    @Ignore
    public void testCreatingQueueAndErrorQueue() throws IOException {
        // Create the main push queue
        String queueNameNew = queueName + "-new";
        Queue queue = new Queue(client, queueNameNew);

        Subscriber subscriber = new Subscriber("http://test.com");
        ArrayList<Subscriber> subscriberArrayList = new ArrayList<Subscriber>();
        subscriberArrayList.add(subscriber);

        QueueModel infoAboutQueue = queue.updateQueue(subscriberArrayList, null, "multicast", "", 60, 10);

        // Validate no error queue is set
        infoAboutQueue = queue.getInfoAboutQueue();
        Assert.assertEquals(null, infoAboutQueue.getErrorQueue());

        // Create the error queue
        String queueNameError = queueName + "-error";
        Queue errorQueue = new Queue(client, queueNameError);
        String body = "Hello, IronMQ!";
        errorQueue.push(body, 10);

        // Set the Error Queue on the main queue
        QueueModel newInfoAboutQueue = queue.updateQueue(subscriberArrayList, infoAboutQueue.getAlerts(), "multicast", queueNameError, 60, 10);

        // Validate the error queue has been set
        Assert.assertEquals(queueNameError, newInfoAboutQueue.getErrorQueue());
        Assert.assertEquals(queueNameError, queue.getInfoAboutQueue().getErrorQueue());

        // Clean up
        queue.destroy();
        errorQueue.destroy();
    }

    @Test
    @Ignore
    public void testGettingQueuesList() throws IOException {
        Queue queue = new Queue(client, queueName);
        String body = "Hello, IronMQ!";
        queue.push(body, 10);
        Queues queues = new Queues(client);
        ArrayList<QueueModel> allQueues = queues.getAllQueues();
        Assert.assertTrue(allQueues.size() >= 1);
        queue.destroy();
    }

    @Test
    @Ignore
    public void testGettingMessageById() throws IOException {
        Queue queue = new Queue(client, queueName);
        String body = "testing get message by id";
        String id = queue.push(body, 10);
        Message msg = queue.getMessageById(id);

        Assert.assertEquals(body, msg.getBody());
        Assert.assertEquals(id, msg.getId());
    }

    @Test
    @Ignore
    public void testPostMultipleMessagesAndDelete() throws IOException {
        Queue queue = new Queue(client, queueName);
        String body = "Hello, IronMQ!";
        queue.push(body, 10);
        QueueModel infoAboutQueue = queue.getInfoAboutQueue();
        int queueSize = infoAboutQueue.getSize();

        String[] messages = {"c", "d"};
        Ids ids = queue.pushMessages(messages);

        infoAboutQueue = queue.getInfoAboutQueue();
        Assert.assertEquals(messages.length, ids.getSize());
        Assert.assertEquals(queueSize + 2, infoAboutQueue.getSize());

        queue.deleteMessages(ids);

        infoAboutQueue = queue.getInfoAboutQueue();
        Assert.assertEquals(queueSize, infoAboutQueue.getSize());
    }

    @Test
    @Ignore
    public void testPeekAndClearAllMessages() throws IOException {
        String queueNameMulti = "java-test-queue-multi";
        Queue queue = new Queue(client, queueNameMulti);
        queue.push("first-test-msg");
        QueueModel infoAboutQueue = queue.getInfoAboutQueue();
        int queueSize = infoAboutQueue.getSize();

        String[] messages = {"c", "d"};
        Ids ids = queue.pushMessages(messages);

        Messages msg = queue.peek(2);
        Assert.assertEquals(msg.getSize(), ids.getSize());

        infoAboutQueue = queue.getInfoAboutQueue();
        Assert.assertEquals(messages.length, ids.getSize());
        Assert.assertEquals(queueSize + 2, infoAboutQueue.getSize());

        queue.clear();

        infoAboutQueue = queue.getInfoAboutQueue();
        Assert.assertFalse(queueSize == infoAboutQueue.getSize());
    }

    @Test
    @Ignore
    public void testReleaseMessage() throws IOException {
        String queueNewName = "java-release-testing";
        Queue queue = new Queue(client, queueNewName);
        String msgBody = "release-test-message";
        String id = queue.push(msgBody);
        queue.releaseMessage(id, 0);
        Message message = queue.get();
        Assert.assertEquals(message.getBody(), msgBody);
    }

    @Test
    @Ignore
    public void testSubscribers() throws IOException {
        String queueNameSubscriber = "java-testing-queue-push";
        Queue queue = new Queue(client, queueNameSubscriber);
        String subscriberUrl1 = "http://mysterious-brook-1807.herokuapp.com/ironmq_push_1";
        String subscriberUrl2 = "http://mysterious-brook-1807.herokuapp.com/ironmq_push_2";
        String subscriberUrl3 = "http://mysterious-brook-1807.herokuapp.com/ironmq_push_3";

        Subscriber subscriber = new Subscriber(subscriberUrl1);
        ArrayList<Subscriber> subscriberArrayList = new ArrayList<Subscriber>();
        subscriberArrayList.add(subscriber);
        Subscriber subscriber2 = new Subscriber(subscriberUrl2);
        subscriberArrayList.add(subscriber2);
        Subscriber subscriber3 = new Subscriber(subscriberUrl3);
        subscriberArrayList.add(subscriber3);
        queue.addSubscribersToQueue(subscriberArrayList);

        String[] messages = {"test1", "test2"};
        Ids ids = queue.pushMessages(messages);

        QueueModel infoAboutQueue = queue.getInfoAboutQueue();
        Assert.assertEquals(infoAboutQueue.getSubscribers().size(), 3);

        ArrayList<Subscriber> subscribersToRemove = new ArrayList<Subscriber>();
        subscribersToRemove.add(subscriber3);
        queue.removeSubscribersFromQueue(subscribersToRemove);
        infoAboutQueue = queue.getInfoAboutQueue();
        Assert.assertEquals(infoAboutQueue.getSubscribers().size(), 2);

        SubscribersInfo subscribersInfo = queue.getPushStatusForMessage(ids.getId(0));
        Assert.assertEquals(subscribersInfo.getSubscribers().size(), 3);

//        queue.deletePushMessageForSubscriber(ids.getId(0), subscribersInfo.getSubscribers().get(0).id);
//        subscribersInfo = queue.getPushStatusForMessage(ids.getId(0));
//        Assert.assertEquals(subscribersInfo.getSubscribers().size(), 1);

        queue.destroy();
    }

    @Test
    @Ignore
    public void testAlerts() throws IOException {
        String queueNameSubscriber = "java-test_alert_queue";
        Queue queue = new Queue(client, queueNameSubscriber);
        queue.push("test-message-alert");

        Alert alert = new Alert("fixed", "asc", 101, 5, queueNameSubscriber);
        Alert alert2 = new Alert("fixed", "desc", 102, 6, queueNameSubscriber);
        Alert alert3 = new Alert("fixed", "desc", 103, 7, queueNameSubscriber);

        ArrayList<Alert> alertArrayList = new ArrayList<Alert>();
        alertArrayList.add(alert);
        alertArrayList.add(alert2);
        alertArrayList.add(alert3);

        queue.addAlertsToQueue(alertArrayList);
//         queue.updateAlertsToQueue(alertArrayList);

        QueueModel infoAboutQueue = queue.getInfoAboutQueue();
        Assert.assertEquals(infoAboutQueue.getAlerts().size(), 3);

        queue.deleteAlertFromQueueById(infoAboutQueue.getAlerts().get(0).getId());
        infoAboutQueue = queue.getInfoAboutQueue();
        Assert.assertEquals(infoAboutQueue.getAlerts().size(), 2);

        queue.deleteAlertsFromQueue(infoAboutQueue.getAlerts());

        infoAboutQueue = queue.getInfoAboutQueue();
        Assert.assertNull(infoAboutQueue.getAlerts());
    }

    @Test
    public void testPostMessage() throws IOException {
        Queue queue = new Queue(client, "my_queue_" + ts());
        String messageId = queue.push("Test message");
        Assert.assertTrue(messageId.length() > 0);
    }

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

    @Test(expected = EmptyQueueException.class)
    public void testReserveMessageFromEmptyQueue() throws IOException {
        Queue queue = new Queue(client, "my_queue_" + ts());
        queue.push("");
        queue.clear();
        Message message = queue.reserve();
    }

    @Test
    public void testReserveMessagesFromEmptyQueue() throws IOException {
        Queue queue = new Queue(client, "my_queue_" + ts());
        queue.push("");
        queue.clear();
        Messages messages = queue.reserve(4);
        Assert.assertEquals(0, messages.getSize());
        Assert.assertEquals(0, messages.getMessages().length);
    }

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

    @Test
    public void testReserveMessages() throws IOException {
        Queue queue = new Queue(client, "my_queue_" + ts());
        String messageText = "Test message " + ts();
        Ids ids = queue.pushMessages(new String[]{messageText + "0", messageText + "1", messageText + "2"});
        Messages messages = queue.reserve(4);

        Assert.assertTrue(messages.getSize() > 0);
        Assert.assertTrue(messages.getSize() < 4);
        for (int i = 0; i < messages.getSize(); i++) {
            Assert.assertTrue(messages.getMessage(i).getReservationId().length() > 0);
            Assert.assertEquals(ids.getId(i), messages.getMessage(i).getId());
            Assert.assertEquals(messageText + i, messages.getMessage(i).getBody());
            Assert.assertEquals(1, messages.getMessage(i).getReservedCount());
        }
    }

    @Test
    public void testTouchMessage() throws IOException, InterruptedException {
        Queue queue = new Queue(client, "my_queue_" + ts());
        queue.push("Test message");
        Message message = queue.reserve(1, 5).getMessage(0);

        System.out.println(message.getReservationId());
        System.out.println(message.getId());
        System.out.println(message.getBody());

        Thread.sleep(3500);
        queue.touchMessage(message);
        Thread.sleep(3500);
        Assert.assertEquals(0, queue.reserve(1).getSize());
    }

    @Test
    public void testClearQueue() throws IOException {
        Queue queue = new Queue(client, "my_queue");
        queue.push("Some message");
        Assert.assertTrue(queue.getInfoAboutQueue().getSize() > 0);
        queue.clear();
        Assert.assertEquals(0, queue.getInfoAboutQueue().getSize());
    }

    private Client setCredentials() throws IOException {
        Properties prop = new Properties();
        InputStream input;
        try {
            input = new FileInputStream("config.properties");
        } catch (FileNotFoundException fnfe) {
            System.out.println("config.properties not found");
            input = new FileInputStream("../../config.properties"); //maven release hack
        }
        prop.load(input);
        String token = prop.getProperty("token");
        String projectId = prop.getProperty("project_id");
        //return new Client(projectId, token, new Cloud("http", "mq-v3-beta.iron.io", 80), 3);
        return new Client(projectId, token, new Cloud("http", "localhost", 8080), 3);
        //return new Client(projectId, token, Cloud.ironAWSUSEast, 1);
    }

    private long ts() {
        return new Date().getTime();
    }
}
