package io.iron.ironmq;

import com.google.gson.Gson;

import org.junit.Assert;
import org.junit.Test;

public class MockTest {
    /**
     * This test makes sure that we don't get overflow errors when queue size
     * or total_messages exceeds max int.
     */
    @Test
    public void testDeserializeQueue() {
        final Gson gson = new Gson();
        final long big = 3000000000L; // exceeds max int
        final String json = String.format("{\"total_messages\": %d, \"size\": %d}", big, big);
        QueueModel queue = gson.fromJson(json, QueueModel.class);
        //Assert.assertEquals(queue.getSizeLong(), big);
        //Assert.assertEquals(queue.getTotalMessagesLong(), big);
    }
}
