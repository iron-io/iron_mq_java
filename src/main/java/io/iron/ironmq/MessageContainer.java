package io.iron.ironmq;

public class MessageContainer {
    private Message message;

    public MessageContainer(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
