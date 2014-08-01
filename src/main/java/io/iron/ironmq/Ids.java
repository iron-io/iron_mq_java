package io.iron.ironmq;

public class Ids {
    private String[] ids;

    public String getId(int i) {
        return ids[i];
    }

    public int getSize(){
        return ids.length;
    }

    public String[] getIds() {
        return ids;
    }

    public MessageOptions[] toMessageOptions() {
        int length = ids.length;
        MessageOptions[] result = new MessageOptions[length];
        for (int i = 0; i < length; i++)
            result[i] = new MessageOptions(ids[i], (String)null);
        return result;
    }
}
