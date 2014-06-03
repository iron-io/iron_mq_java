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
    private String queueName = "java-testing-queue";
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
        String queueNameNew = queueName + "-new";
        Client client = new Client(projectId, token, Cloud.ironAWSUSEast);
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
    public void testUpdatingQueue() throws IOException {

        setCredentials();

        // Create the main push queue
        String queueNameNew = queueName + "-update";
        Client client = new Client(projectId, token, Cloud.ironAWSUSEast);
        Queue queue = new Queue(client, queueNameNew);

        Subscriber subscriber = new Subscriber("http://test.com");
        ArrayList<Subscriber> subscriberArrayList = new ArrayList<Subscriber>();
        subscriberArrayList.add(subscriber);

        QueueModel infoAboutQueue = queue.updateQueue(subscriberArrayList, null,"multicast",60,10);

        // Validate retries
        infoAboutQueue = queue.getInfoAboutQueue();
        Assert.assertEquals(60, infoAboutQueue.getRetries());


        // Update the queue
        QueueModel newInfoAboutQueue = queue.updateQueue(subscriberArrayList, infoAboutQueue.getAlerts(),"multicast",100,10);

        // Validate the error queue has been set
        Assert.assertEquals(100, newInfoAboutQueue.getRetries());

        // Clean up
        queue.destroy();
    }


    @Test
    public void testCreatingQueueAndErrorQueue() throws IOException {

        setCredentials();

        // Create the main push queue
        String queueNameNew = queueName + "-new";
        Client client = new Client(projectId, token, Cloud.ironAWSUSEast);
        Queue queue = new Queue(client, queueNameNew);

        Subscriber subscriber = new Subscriber("http://test.com");
        ArrayList<Subscriber> subscriberArrayList = new ArrayList<Subscriber>();
        subscriberArrayList.add(subscriber);

        QueueModel infoAboutQueue = queue.updateQueue(subscriberArrayList, null,"multicast", "",60,10);

        // Validate no error queue is set
        infoAboutQueue = queue.getInfoAboutQueue();
        Assert.assertEquals(null, infoAboutQueue.getErrorQueue());

        // Create the error queue
        String queueNameError = queueName + "-error";
        Queue errorQueue = new Queue(client, queueNameError);
        String body = "Hello, IronMQ!";
        errorQueue.push(body, 10);

        // Set the Error Queue on the main queue
        QueueModel newInfoAboutQueue = queue.updateQueue(subscriberArrayList, infoAboutQueue.getAlerts(),"multicast", queueNameError,60,10);

        // Validate the error queue has been set
        Assert.assertEquals(queueNameError, newInfoAboutQueue.getErrorQueue());
        Assert.assertEquals(queueNameError, queue.getInfoAboutQueue().getErrorQueue());

        // Clean up
        queue.destroy();
        errorQueue.destroy();
    }

    @Test
    public void testGettingQueuesList() throws IOException {
        setCredentials();
        Client client = new Client(projectId, token, Cloud.ironAWSUSEast);
        Queue queue = new Queue(client, queueName);
        String body = "Hello, IronMQ!";
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
    public void testPeekAndClearAllMessages() throws IOException {
        setCredentials();
        Client client = new Client(projectId, token, Cloud.ironAWSUSEast);
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
    public void testReleaseMessageAndDestroyQueue() throws IOException {
        setCredentials();
        Client client = new Client(projectId, token, Cloud.ironAWSUSEast);
        Queues queues = new Queues(client);
        ArrayList<QueueModel> allQueuesOrig = queues.getAllQueues();

        String queueNewName = "java-release-testing";
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
    public void testAlerts() throws IOException {
        setCredentials();
        Client client = new Client(projectId, token, Cloud.ironAWSUSEast);
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
