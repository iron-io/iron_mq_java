package io.iron.ironmq;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class IronMQTest {
    @Test public void testClient() throws IOException {
        Client c = new Client();
        IQueue q = c.queue("test-queue");

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
        IQueue q = c.queue("test-queue");

        q.push("test");
    }
}
