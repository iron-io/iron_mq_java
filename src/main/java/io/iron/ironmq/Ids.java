package io.iron.ironmq;

public class Ids {
    private String[] ids;

    protected Ids() {
    }

    protected Ids(Messages messages) {
        Message[] source = messages.getMessages();
        ids = new String[source.length];
        for (int i = source.length - 1; i >= 0; i--) {
            ids[i] = source[i].getId();
        }
    }

    public String getId(int i) {
        return ids[i];
    }

    public int getSize(){
        return ids.length;
    }

    public String[] getIds() {
        return ids;
    }
}
