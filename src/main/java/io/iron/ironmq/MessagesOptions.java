package io.iron.ironmq;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class MessagesOptions implements Serializable {
    private MessageOptions[] ids;

    public MessagesOptions(MessageOptions[] messages) {
        this.ids = messages;
    }

    public MessageOptions[] getMessages() {
        return ids;
    }

    public MessageOptions getMessage(int index) {
        return ids[index];
    }
}
