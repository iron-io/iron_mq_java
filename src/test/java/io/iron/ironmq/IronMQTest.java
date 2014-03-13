package io.iron.ironmq;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

public class IronMQTest {
    private String queueName = "testing-queue";
    private String token = "";
    private String projectId = "";

    @Test(expected = HTTPException.class)
    public void testErrorResponse() throws IOException {
        // intentionally invalid project/token combination
        Client client = new Client("4444444444444", "aaaaaa", Cloud.ironAWSUSEast);
        Queue queue = client.queue("test-queue");
        queue.push("test");
    }

    @Test
    public void testCreatingQueueAndMessage() throws IOException {
        setCredentials();
        Client client = new Client(projectId, token, Cloud.ironAWSUSEast);
        Queue queue = new Queue(client, queueName);

        String body = "Hello, IronMQ!";
        String id = queue.push(body, 10);

        QueueModel infoAboutQueue = queue.getInfoAboutQueue();
        Assert.assertEquals(1, infoAboutQueue.size);
        Assert.assertEquals(queueName, infoAboutQueue.name);

        Message msg = queue.get();
        Assert.assertEquals(body, msg.getBody());
        Assert.assertEquals(id, msg.getId());
        queue.deleteMessage(msg);
        infoAboutQueue = queue.getInfoAboutQueue();
        Assert.assertEquals(0, infoAboutQueue.size);
    }

    @Test
    public void testGettingQueuesList() throws IOException {
        setCredentials();
        Client client = new Client(projectId, token, Cloud.ironAWSUSEast);
        Queue queue = new Queue(client, queueName);
        final String body = "Hello, IronMQ!";
        queue.push(body, 10);
        Queues queues = new Queues(client);
        ArrayList<QueueModel> allQueues = queues.getAllQueues();
        Assert.assertTrue(allQueues.size() >= 1);
        queue.destroy();
    }

    @Test
    public void testGettingMessageById() throws IOException {
        setCredentials();
        Client client = new Client(projectId, token, Cloud.ironAWSUSEast);
        Queue queue = new Queue(client, queueName);
        String body = "testing get message by id";
        String id = queue.push(body, 10);
        Message msg = queue.getMessageById(id);

        Assert.assertEquals(body, msg.getBody());
        Assert.assertEquals(id, msg.getId());
    }

    @Test
    public void testPostMultipleMessagesAndDelete() throws IOException {
        setCredentials();
        Client client = new Client(projectId, token, Cloud.ironAWSUSEast);
        Queue queue = new Queue(client, queueName);
        QueueModel infoAboutQueue = queue.getInfoAboutQueue();
        int queueSize = infoAboutQueue.size;

        String[] messages = {"c", "d"};
        Ids ids = queue.pushMessages(messages);

        infoAboutQueue = queue.getInfoAboutQueue();
        Assert.assertEquals(messages.length, ids.getSize());
        Assert.assertEquals(queueSize + 2, infoAboutQueue.size);

        queue.deleteMessages(ids);

        infoAboutQueue = queue.getInfoAboutQueue();
        Assert.assertEquals(queueSize, infoAboutQueue.size);
    }

    @Test
    public void testPeekAndClearAllMessages() throws IOException {
        setCredentials();
        Client client = new Client(projectId, token, Cloud.ironAWSUSEast);
        String queueNameMulti = "test-queue-multi";
        Queue queue = new Queue(client, queueNameMulti);
        queue.push("first-test-msg");
        QueueModel infoAboutQueue = queue.getInfoAboutQueue();
        int queueSize = infoAboutQueue.size;

        String[] messages = {"c", "d"};
        Ids ids = queue.pushMessages(messages);

        Messages msg = queue.peek(2);
        Assert.assertEquals(msg.getSize(), ids.getSize());

        infoAboutQueue = queue.getInfoAboutQueue();
        Assert.assertEquals(messages.length, ids.getSize());
        Assert.assertEquals(queueSize + 2, infoAboutQueue.size);

        queue.clear();

        infoAboutQueue = queue.getInfoAboutQueue();
        Assert.assertFalse(queueSize == infoAboutQueue.size);
    }

    @Test
    public void testReleaseMessageAndDestroyQueue() throws IOException {
        setCredentials();
        Client client = new Client(projectId, token, Cloud.ironAWSUSEast);
        Queues queues = new Queues(client);
        ArrayList<QueueModel> allQueuesOrig = queues.getAllQueues();

        String queueNewName = "release-testing";
        Queue queue = new Queue(client, queueNewName);
        String msgBody = "release-test-message";
        String id = queue.push(msgBody);
        queue.releaseMessage(id, 0);
        Message message = queue.get();
        Assert.assertEquals(message.getBody(), msgBody);

        ArrayList<QueueModel> allQueues = queues.getAllQueues();
        Assert.assertFalse(allQueuesOrig.size() == allQueues.size());
        queue.destroy();

        allQueues = queues.getAllQueues();
        Assert.assertTrue(allQueuesOrig.size() == allQueues.size());
    }

    @Test
    public void testSubscribers() throws IOException {
        setCredentials();
        Client client = new Client(projectId, token, Cloud.ironAWSUSEast);
        String queueNameSubscriber = "testing-queue-push";
        Queue queue = new Queue(client, queueNameSubscriber);
        String subscriberUrl1 = "http://mysterious-brook-1807.herokuapp.com/ironmq_push_1";
        String subscriberUrl2 = "http://mysterious-brook-1807.herokuapp.com/ironmq_push_2";
        String subscriberUrl3 = "http://mysterious-brook-1807.herokuapp.com/ironmq_push_3";

        Subscriber subscriber = new Subscriber();
        subscriber.url = subscriberUrl1;
        ArrayList<Subscriber> subscriberArrayList = new ArrayList<Subscriber>();
        subscriberArrayList.add(subscriber);
        Subscriber subscriber2 = new Subscriber();
        subscriber2.url = subscriberUrl2;
        subscriberArrayList.add(subscriber2);
        Subscriber subscriber3 = new Subscriber();
        subscriber3.url = subscriberUrl3;
        subscriberArrayList.add(subscriber3);
        queue.addSubscribersToQueue(subscriberArrayList);

        String[] messages = {"test1", "test2"};
        Ids ids = queue.pushMessages(messages);

        QueueModel infoAboutQueue = queue.getInfoAboutQueue();
        Assert.assertEquals(infoAboutQueue.subscribers.size(), 3);

        ArrayList<Subscriber> subscribersToRemove = new ArrayList<Subscriber>();
        subscribersToRemove.add(subscriber3);
        queue.removeSubscribersFromQueue(subscribersToRemove);
        infoAboutQueue = queue.getInfoAboutQueue();
        Assert.assertEquals(infoAboutQueue.subscribers.size(), 2);

        SubscribersInfo subscribersInfo = queue.getPushStatusForMessage(ids.getId(0));
        Assert.assertEquals(subscribersInfo.getSubscribers().size(), 3);

//        queue.deletePushMessageForSubscriber(ids.getId(0), subscribersInfo.getSubscribers().get(0).id);
//        subscribersInfo = queue.getPushStatusForMessage(ids.getId(0));
//        Assert.assertEquals(subscribersInfo.getSubscribers().size(), 1);

        queue.destroy();
    }

    @Test
    public void testAlerts() throws IOException {
        setCredentials();
        Client client = new Client(projectId, token, Cloud.ironAWSUSEast);
        String queueNameSubscriber = "test_alert_queue";
        Queue queue = new Queue(client, queueNameSubscriber);
        queue.push("test-message-alert");

        Alert alert = new Alert();
        alert.direction = "asc";
        alert.queue = queueNameSubscriber;
        alert.snooze = 5;
        alert.trigger = 101;
        alert.type = "fixed";
        Alert alert2 = new Alert();
        alert2.direction = "desc";
        alert2.queue = queueNameSubscriber;
        alert2.snooze = 6;
        alert2.trigger = 102;
        alert2.type = "fixed";
        Alert alert3 = new Alert();
        alert3.direction = "desc";
        alert3.queue = queueNameSubscriber;
        alert3.snooze = 7;
        alert3.trigger = 103;
        alert3.type = "fixed";

        ArrayList<Alert> alertArrayList = new ArrayList<Alert>();
        alertArrayList.add(alert);
        alertArrayList.add(alert2);
        alertArrayList.add(alert3);

        queue.addAlertsToQueue(alertArrayList);
//         queue.updateAlertsToQueue(alertArrayList);

        QueueModel infoAboutQueue = queue.getInfoAboutQueue();
        Assert.assertEquals(infoAboutQueue.alerts.size(), 3);

        queue.deleteAlertFromQueueById(infoAboutQueue.alerts.get(0).id);
        infoAboutQueue = queue.getInfoAboutQueue();
        Assert.assertEquals(infoAboutQueue.alerts.size(), 2);

        queue.deleteAlertsFromQueue(infoAboutQueue.alerts);

        infoAboutQueue = queue.getInfoAboutQueue();
        Assert.assertNull(infoAboutQueue.alerts);
    }

    private void setCredentials() throws IOException {
        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream("config.properties");
        } catch(FileNotFoundException fnfe) {
            System.out.println("config.properties not found");
            input = new FileInputStream("../../config.properties"); //maven release hack
        }
        prop.load(input);
        token = prop.getProperty("token");
        projectId = prop.getProperty("project_id");
    }
}
