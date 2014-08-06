package client.iron.ironmq;


import io.iron.ironmq.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ClientIronMQTest {

    private String queueName = "java-testing-queue";
    private String token = "";
    private String projectId = "";

    @Test
    public void testPostMultipleMessagesAndIdsShouldNotBeNull() throws IOException {
        Client client = setCredentials();
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
        queue.destroy();
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

        String host = prop.getProperty("serverHost");
        String scheme = prop.getProperty("serverScheme");
        int port = Integer.parseInt(prop.getProperty("serverPort"));

        return new Client(projectId, token, new Cloud(scheme, host, port), 3);
    }
}
