package io.iron.ironmq;

public class Messages {
    private Message[] messages;

    public Messages(Message... msgs) {
        messages = msgs;
    }

    public Message getMessage(int i) {
        return messages[i];
    }

    public Message[] getMessages() {
        return messages;
    }

    public int getSize() {
        return messages.length;
    }

    public MessageOptions[] toMessageOptions() {
        int length = messages.length;
        MessageOptions[] result = new MessageOptions[length];
        for (int i = 0; i < length; i++)
            result[i] = new MessageOptions(messages[i].getId(), messages[i].getReservationId());
        return result;
    }
}
