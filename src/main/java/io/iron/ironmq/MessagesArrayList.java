package io.iron.ironmq;

import java.util.ArrayList;

public class MessagesArrayList {
    private ArrayList<Message> messages;

    public MessagesArrayList(ArrayList<Message> messageArrayList) {
        this.messages = messageArrayList;
    }

    public Message getMessage(int i) {
        return messages.get(i);
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public int getSize() {
        return messages.size();
    }
}
