package io.iron.ironmq;

import java.io.IOException;

/**
 * The HTTPException class represents an HTTP response status that is not 200 OK.
 */
public class HTTPException extends IOException {
    private int status;

    /**
    * Creates a new HTTPException.
    *
    * @param status The HTTP status code.
    * @param message The text of the HTTP status code.
    */
    public HTTPException(int status, String message) {
        super(message);
        this.status = status;
    }

    /**
    * Returns the HTTP response's status code.
    */
    public int getStatusCode() { return this.status; }
}
