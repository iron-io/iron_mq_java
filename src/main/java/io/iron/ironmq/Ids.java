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
}
