package io.iron.ironmq;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

public class Queues {
    final private Client client;

    public Queues(Client client) {
        this.client = client;
    }

    /**
     * Retrieves all queues in project. If there are no items on the queue, an
     * EmptyQueueException is thrown.

     * @throws EmptyQueueException If the queue is empty.
     * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public ArrayList<QueueModel> getAllQueues() throws IOException {
        String url = "queues";
        Reader reader = client.get(url);
        Gson gson = new Gson();
        ArrayList<QueueModel> queues = gson.fromJson(reader, new TypeToken<ArrayList<QueueModel>>(){}.getType());
        reader.close();
        return queues;
    }

}
