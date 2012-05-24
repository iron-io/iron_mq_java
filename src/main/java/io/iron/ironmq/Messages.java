package io.iron.ironmq;

class Messages {
    private Message[] messages;

    Messages(Message... msgs) {
        messages = msgs;
    }

    Message getMessage(int i) {
        return messages[i];
    }
}
