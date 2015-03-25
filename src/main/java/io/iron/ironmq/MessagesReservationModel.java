package io.iron.ironmq;

import java.util.ArrayList;

public class MessagesReservationModel {
    private Integer n;
    private Integer timeout;
    private Integer wait;

    public MessagesReservationModel(int n, int timeout) {
        this.setN(n);
        this.setTimeout(timeout);
    }

    public MessagesReservationModel(int n, int timeout, int wait) {
        this.setN(n);
        this.setTimeout(timeout);
        this.setWait(wait);
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n >= 1 ? n : null;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout >= 0 ? timeout : null;
    }

    public Integer getWait() {
        return wait;
    }

    public void setWait(Integer wait) {
        this.wait = wait > 0 && wait <= 30 ? wait : null;
    }
}
