package io.iron.ironmq;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HttpClient {
    private boolean forMq;

    private HttpClient() { }

    public HttpClient(boolean forMq) {
        this.forMq = forMq;
    }

    public static HttpClient create() {
        return new HttpClient(false);
    }

    public static HttpClient createForMq() {
        return new HttpClient(true);
    }

    static private class Error implements Serializable {
        String msg;
    }

    public Reader singleRequest(String method, URL url, String body, HashMap<String, String> headers) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        if (method.equals("DELETE") || method.equals("PATCH")) {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("X-HTTP-Method-Override", method);
        } else {
            conn.setRequestMethod(method);
        }

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            conn.setRequestProperty(entry.getKey(), entry.getValue());
        }

        if (body != null) {
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
        }

        conn.connect();

        if (body != null) {
            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            out.write(body);
            out.close();
        }

        int status = conn.getResponseCode();
        if (status != 200) {
            if (forMq) {
                String msg;
                if (conn.getContentLength() > 0 && conn.getContentType().equals("application/json")) {
                    InputStreamReader reader = null;
                    try {
                        reader = new InputStreamReader(conn.getErrorStream());
                        Gson gson = new Gson();
                        Error error = gson.fromJson(reader, Error.class);
                        msg = error.msg;
                    } catch (JsonSyntaxException e) {
                        msg = "IronMQ's response contained invalid JSON";
                    } finally {
                        if (reader != null)
                            reader.close();
                    }
                } else {
                    msg = "Empty or non-JSON response";
                }
                throw new HTTPException(status, msg);
            } else {
                throw new HTTPException(status, conn.getResponseMessage());
            }
        }

        return new InputStreamReader(conn.getInputStream());
    }

}
