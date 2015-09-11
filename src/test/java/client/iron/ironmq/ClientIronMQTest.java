package client.iron.ironmq;


import io.iron.ironmq.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.lang.Thread;
import java.util.ArrayList;

public class ClientIronMQTest {
  private static class SubmitCallable implements Callable<QueueModel> {
      private String msg;
      private Queue queue;

      public SubmitCallable(Queue queue, String msg) {
        this.queue = queue;
        this.msg = msg;
      }
      public QueueModel call() throws IOException, InterruptedException {
        queue.push(this.msg, 10);
        return queue.getInfoAboutQueue();
      }
    }

    private String queueName = "java-testing-queue";
    private String token = "";
    private String projectId = "";
    private Client client;

    @Before
    public void setUp() throws Exception {
        client = new Client();
    }

    @Test
    public void testPostMultipleMessagesAndIdsShouldNotBeNull() throws IOException {
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

    @Test
    public void testPostConcurrently() throws IOException, InterruptedException, ExecutionException {
      ExecutorService ex = Executors.newFixedThreadPool(10);
      ArrayList<Future<QueueModel>> futures = new ArrayList<Future<QueueModel>>();
      Queue queue = new Queue(client, queueName);
      for (int i = 0; i < 10; i++) {
        Future<QueueModel> submitted = ex.submit(new SubmitCallable(queue, "Hello, IronMQ # " + i + "!"));
        futures.add(submitted);
      }
      Thread.sleep(1000);
      Boolean found = false;
      for (Future<QueueModel> fut : futures) {
        QueueModel qModel = fut.get();
        if (qModel.getSize() == 10) {
          found = true;
        }
      }
      Assert.assertEquals(found, true);
    }
}
