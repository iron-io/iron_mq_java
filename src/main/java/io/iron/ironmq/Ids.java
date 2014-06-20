package io.iron.ironmq;

class Ids {
    private String[] ids;

    String getId(int i) {
        return ids[i];
    }

    int getSize(){
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
