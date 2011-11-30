package io.iron.ironmq;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.net.HttpURLConnection;
import java.net.URL;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 * The Client class provides access to the IronMQ service.
 */
public class Client {
    static final private String proto = "http";
    static final private String host = "mq-aws-us-east-1.iron.io";
    static final private String apiVersion = "1";

    private String projectId;
    private String token;

    /**
     * Constructs a new Client using the specified project ID and token.
     * The network is not accessed during construction and this call will
     * succeed even if the credentials are invalid.
     *
     * @param projectId A 24-character project ID.
     * @param token An OAuth token.
     */
    public Client(String projectId, String token) {
        this.projectId = projectId;
        this.token = token;
    }

    /**
     * Returns a Queue using the given name.
     * The network is not accessed during this call.
     *
     * @param name The name of the Queue to create.
     */
    public Queue queue(String name) {
        return new Queue(this, name);
    }

    JSONObject delete(String endpoint) throws IOException {
        return request("DELETE", endpoint, null);
    }

    JSONObject get(String endpoint) throws IOException {
        return request("GET", endpoint, null);
    }

    JSONObject post(String endpoint, String body) throws IOException {
        return request("POST", endpoint, body);
    }

    private JSONObject request(String method, String endpoint, String body) throws IOException {
        String path = "/" + apiVersion + "/projects/" + projectId + "/" + endpoint + "?oauth=" + token;
        URL url = new URL(proto, host, path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        if (body != null) {
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
        }
        conn.connect();

        if (body != null) {
            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(body);
            out.flush();
        }

        int status = conn.getResponseCode();
        if (status != 200) {
            JSONObject jsonObj = streamToJSON(conn.getErrorStream());
            String msg = jsonObj.getString("msg");
            throw new HTTPException(status, msg);
        }

        JSONObject jsonObj = streamToJSON(conn.getInputStream());
        return jsonObj;
    }

    static private JSONObject streamToJSON(InputStream stream) throws IOException {
        StringBuilder builder = new StringBuilder();
        byte[] buf = new byte[0x10000];

        int n = 0;
        do {
            n = stream.read(buf);
            if (n > 0) {
                builder.append(new String(buf));
            }
        } while (n >= 0);

        stream.close();

        String jsonStr = builder.toString();
        return (JSONObject)JSONSerializer.toJSON(jsonStr);
    }
}
