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
}
