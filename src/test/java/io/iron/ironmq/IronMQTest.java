import java.io.IOException;

import io.iron.ironmq.*;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class IronMQTest {
    private String projectId;
    private String token;

    @Before public void setup() {
        projectId = System.getenv("IRON_PROJECT_ID");
        token = System.getenv("IRON_TOKEN");
        Assume.assumeTrue(projectId != null && token != null);
    }

    @Test public void testClient() throws IOException {
        Client c = new Client(projectId, token, Cloud.ironAWSUSEast);
        Queue q = c.queue("test-queue");

        // clear out the queue
        try {
            while (true) {
                Message msg = q.get();
                q.deleteMessage(msg);
            }
        } catch (EmptyQueueException e) {
        }

        final String body = "Hello, IronMQ!";

        q.push(body);

        Message msg = q.get();
        Assert.assertEquals(body, msg.getBody());
        q.deleteMessage(msg);
    }

    @Test(expected=HTTPException.class) public void testErrorResponse() throws IOException {
        // intentionally invalid project/token combination
        Client c = new Client("4444444444444", "aaaaaa", Cloud.ironAWSUSEast);
        Queue q = c.queue("test-queue");

        q.push("test");
    }
}
