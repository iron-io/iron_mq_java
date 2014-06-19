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
     * Retrieves all queues in alphabetical order.

     * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     * @deprecated Deprecated because of two reasons: It doesn't retrieve all queues and Queues.getQueues is more preferable.
     */
    @Deprecated
    public ArrayList<QueueModel> getAllQueues() throws IOException {
        return getQueues(this.client);
    }

    /**
     * Retrieves all queues in alphabetical order.

     * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public static ArrayList<QueueModel> getQueues(Client client) throws IOException {
        String url = "queues";
        Reader reader = client.get(url);
        Gson gson = new Gson();
        QueuesContainer queues = gson.fromJson(reader, QueuesContainer.class);
        reader.close();
        return queues.getQueues();
    }

}
