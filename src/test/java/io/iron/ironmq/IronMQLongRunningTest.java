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

public class IronMQLongRunningTest {
    private String queueName = "java-testing-queue";
    private Client client;
    private Queue queue;

    @Before
    public void setUp() throws Exception {
        client = new Client(null, null, null, 3, 1);
        queue = new Queue(client, "my_queue_" + ts());
    }

    /**
     * Test shows how to increase time of message reservation
     * Expected that:
     * - message will be available after 30 seconds (initial timeout is 30 seconds)
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testReserveMessageWithTimeout() throws IOException, InterruptedException {
        queue.push("Test message");
        Message reservedFirstTime = queue.reserve(1, 30).getMessage(0);

        Thread.sleep(32000);
        Messages reservedAgain = queue.reserve(1);
        Assert.assertEquals(1, reservedAgain.getSize());
        Assert.assertEquals(reservedAgain.getMessage(0).getId(), reservedFirstTime.getId());
    }

    /**
     * Test shows how to increase time of message reservation
     * Expected that:
     * - message will not be available after 35 seconds (initial timeout is 30 seconds)
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testTouchMessage() throws IOException, InterruptedException {
        queue.push("Test message");
        Message message = queue.reserve(1, 30).getMessage(0);

        Thread.sleep(25000);
        queue.touchMessage(message);
        Thread.sleep(10000);
        Assert.assertEquals(0, queue.reserve(1).getSize());
    }

    /**
     * Test shows how to increase time of message reservation by specified amount of seconds
     * Expected that:
     * - message will be available only after specified amount of seconds.
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testTouchMessageWithTimeout() throws IOException, InterruptedException {
        queue.push("Test message");
        Message message = queue.reserve(1, 30).getMessage(0);
        queue.touchMessage(message, 40);

        Thread.sleep(35000);
        Assert.assertEquals(0, queue.reserve(1).getSize());
        Thread.sleep(10000);
        Assert.assertEquals(1, queue.reserve(1).getSize());
    }

    /**
     * Test shows how to touch a message multiple times.
     * Expected that:
     * - after second touch call message will have new reservation id
     * - new reservation id will not equal to old one
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testTouchMessageTwice() throws IOException, InterruptedException {
        queue.push("Test message");
        Message message = queue.reserve(1, 5).getMessage(0);

        Thread.sleep(3500);
        MessageOptions options1 = queue.touchMessage(message);
        Thread.sleep(3500);
        MessageOptions options2 = queue.touchMessage(message);

        Assert.assertFalse(options1.getReservationId().equals(options2.getReservationId()));
        Assert.assertEquals(message.getReservationId(), options2.getReservationId());
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
