package io.iron.ironmq;

import java.io.IOException;

public class HTTPException extends IOException {
    private int status;

    public HTTPException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatusCode() { return this.status; }
}
