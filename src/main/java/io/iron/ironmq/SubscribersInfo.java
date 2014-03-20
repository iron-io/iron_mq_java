package io.iron.ironmq;

import java.util.ArrayList;

public class SubscribersInfo {
    private ArrayList<MessagePushModel> subscribers;

    public SubscribersInfo(ArrayList<MessagePushModel> subscribers) {
        this.subscribers = subscribers;
    }

    public MessagePushModel getSubscriber(int i) {
        return subscribers.get(i);
    }

    public ArrayList<MessagePushModel> getSubscribers() {
        return subscribers;
    }
}
