package io.iron.ironmq;

import java.util.ArrayList;

public class Subscribers {
    private ArrayList<Subscriber> subscribers;

    public Subscribers(ArrayList<Subscriber> subscribers) {
        this.subscribers = subscribers;
    }

    public Subscriber getSubscriber(int i) {
        return subscribers.get(i);
    }

    public ArrayList<Subscriber> getSubscribers() {
        return subscribers;
    }
}
