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
}
