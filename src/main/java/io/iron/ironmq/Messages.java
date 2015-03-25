package io.iron.ironmq;

import java.util.ArrayList;
import java.util.Arrays;

public class Messages {
    private ArrayList<Message> messages;

    public Messages(Message... msgs) {
        messages = new ArrayList<Message>(Arrays.asList(msgs));
    }

    public Messages(ArrayList<Message> msgs) {
        messages = new ArrayList<Message>(msgs);
    }

    public Message getMessage(int i) {
        return messages.get(i);
    }

    public Message[] getMessages() {
        return messages.toArray(new Message[messages.size()]);
    }

    public int getSize() {
        return messages.size();
    }

    public boolean add(Message m) {
        return messages.add(m);
    }

    public MessageOptions[] toMessageOptions() {
        int length = messages.size();
        MessageOptions[] result = new MessageOptions[length];
        for (int i = 0; i < length; i++)
            result[i] = new MessageOptions(messages.get(i).getId(), messages.get(i).getReservationId());
        return result;
    }
}
