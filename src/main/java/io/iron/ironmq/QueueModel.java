package io.iron.ironmq;

import java.util.ArrayList;

public class QueueModel {
    String id;
    String name;
    int size;
    int total_messages;
    String project_id;
    int retries;
    String pushType;
    int retriesDelay;
    ArrayList<Subscriber> subscribers;
    ArrayList<Alert> alerts;
}
