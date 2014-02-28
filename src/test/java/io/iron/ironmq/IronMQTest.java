package io.iron.ironmq;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class IronMQTest {
    private String token = "";
    private String projectId = "";

    Properties prop = new Properties();
    InputStream input = null;

    @Test public void testClient() throws IOException {
        input = new FileInputStream("config.properties");
        prop.load(input);
        token = prop.getProperty("token");
        projectId = prop.getProperty("project_id");

        Client c = new Client(projectId, token, Cloud.ironAWSUSEast);

        Queue q = c.queue("test-queue");

        q.clear();

        Assert.assertEquals(0, q.getSize());

        final String body = "Hello, IronMQ!";

        String id = q.push(body);

        Assert.assertEquals(1, q.getSize());

        Message msg = q.get();
        Assert.assertEquals(body, msg.getBody());
        Assert.assertEquals(id, msg.getId());
        q.deleteMessage(msg);
    }

    @Test(expected=HTTPException.class) public void testErrorResponse() throws IOException {
        // intentionally invalid project/token combination
        Client c = new Client("4444444444444", "aaaaaa", Cloud.ironAWSUSEast);
        Queue q = c.queue("test-queue");

        q.push("test");
    }
}
