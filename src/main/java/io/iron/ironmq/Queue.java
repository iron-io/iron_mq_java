package io.iron.ironmq;

import java.io.IOException;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class Queue {
    final private Client client;
    final private String name;

    Queue(Client client, String name) {
        this.client = client;
        this.name = name;
    }

    public Message get() throws IOException {
        JSONObject jsonObj = client.get("queues/" + name + "/messages");
        Message msg = new Message();
        msg.setId((String)jsonObj.get("id"));
        msg.setBody(jsonObj.getString("body"));
        if (jsonObj.has("timeout")) {
            msg.setTimeout(jsonObj.getLong("timeout"));
        }
        return msg;
    }

    public void deleteMessage(String id) throws IOException {
        client.delete("queues/" + name + "/messages/" + id);
    }

    public void deleteMessage(Message msg) throws IOException {
        deleteMessage(msg.getId());
    }

    public void push(String msg) throws IOException {
        push(msg, 0);
    }

    public void push(String msg, long timeout) throws IOException {
        Message message = new Message();
        message.setBody(msg);
        message.setTimeout(timeout);
        String jsonStr = JSONSerializer.toJSON(message).toString();
        client.post("queues/" + name + "/messages", jsonStr);
    }
}
