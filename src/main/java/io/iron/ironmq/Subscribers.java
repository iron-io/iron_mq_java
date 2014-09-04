package io.iron.ironmq;

import java.util.ArrayList;
import java.util.Arrays;

public class Subscribers {
    private ArrayList<Subscriber> subscribers;

    public Subscribers(ArrayList<Subscriber> subscribers) {
        this.subscribers = subscribers;
    }

    public Subscribers(Subscriber[] subscribers) {
        this.subscribers = new ArrayList<Subscriber>(Arrays.asList(subscribers));
    }

    public Subscriber getSubscriber(int i) {
        return subscribers.get(i);
    }

    public ArrayList<Subscriber> getSubscribers() {
        return subscribers;
    }

    public Subscribers withoutNonIdAttributes() {
        ArrayList<Subscriber> subscribers = new ArrayList<Subscriber>();
        for (int i = 0; i < subscribers.size(); i++) {
            subscribers.add(new Subscriber(this.subscribers.get(i).getName()));
        }
        return new Subscribers(subscribers);
    }
}
