package io.iron.ironmq;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.net.URLEncoder;
import java.util.ArrayList;

public class Queues {
    final private Client client;

    public Queues(Client client) {
        this.client = client;
    }

    /**
     * Retrieves queues in alphabetical order.

     * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     * @deprecated Deprecated because of two reasons: It doesn't retrieve all queues and Queues.getQueues is more preferable.
     */
    @Deprecated
    public ArrayList<QueueModel> getAllQueues() throws IOException {
        return getQueues(this.client);
    }

    /**
     * Retrieves queues in alphabetical order.

     * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public static ArrayList<QueueModel> getQueues(Client client) throws IOException {
        return getQueues(client, null, null);
    }

    /**
     * Retrieves queues in alphabetical order.
     *
     * @param perPage number of elements in response, default for IronMQ is 30
     *
     * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public static ArrayList<QueueModel> getQueues(Client client, int perPage) throws IOException {
        return getQueues(client, null, perPage);
    }

    /**
     * Retrieves queues in alphabetical order.
     *
     * @param previousQueueName this is the last queue on the previous page, it will start from the next one.
     *                          If queue with specified name doesn’t exist result will contain first per_page queues
     *                          that lexicographically greater than previous
     *
     * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public static ArrayList<QueueModel> getQueues(Client client, String previousQueueName) throws IOException {
        return getQueues(client, previousQueueName, null);
    }

    /**
     * Retrieves queues in alphabetical order.
     *
     * @param previousQueueName this is the last queue on the previous page, it will start from the next one.
     *                          If queue with specified name doesn’t exist result will contain first per_page queues
     *                          that lexicographically greater than previous
     * @param perPage number of elements in response, default for IronMQ is 30
     *
     * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
     * @throws java.io.IOException If there is an error accessing the IronMQ server.
     */
    public static ArrayList<QueueModel> getQueues(Client client, String previousQueueName, Integer perPage) throws IOException {
        StringBuilder params = new StringBuilder();
        if (previousQueueName != null && !previousQueueName.isEmpty()) {
            params.append(String.format("previous=%s", URLEncoder.encode(previousQueueName, "UTF-8")));
        }
        if (perPage != null) {
            if (perPage < 1) {
                throw new IllegalArgumentException("perPage parameter should be greater than 0");
            }
            params.append(String.format("%sper_page=%d", params.length() > 0 ? "&" : "", perPage));
        }
        String url = "queues?" + params;

        Reader reader = client.get(url);
        Gson gson = new Gson();
        QueuesContainer queues = gson.fromJson(reader, QueuesContainer.class);
        reader.close();
        return queues.getQueues();
    }

}
