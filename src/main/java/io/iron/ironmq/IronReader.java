package io.iron.ironmq;

import java.io.IOException;
import java.io.Reader;
import java.net.HttpURLConnection;

/**
 * Created by tom on 10/5/14.
 */
public class IronReader {
    Reader reader;
    HttpURLConnection connection;

    protected IronReader(Reader reader, HttpURLConnection connection) {
        this.reader = reader;
        this.connection = connection;
    }

    public void close(){
        try {
            try {
                reader.close();
            } catch (IOException e) {

            }
            connection.disconnect();
        } catch (Exception e) {

        }
    }
}
