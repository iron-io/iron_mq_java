package io.iron.ironmq;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 * The Client class provides access to the IronMQ service.
 */
public class Client {
    static final private String apiVersion = "1";

    static final Random rand = new Random();

    private String projectId;
    private String token;
    private Cloud cloud;

    /**
     * Constructs a new Client using the specified project ID and token.
     * The network is not accessed during construction and this call will
     * succeed even if the credentials are invalid.
     * This constructor uses the AWS cloud with the US East region.
     *
     * @param projectId A 24-character project ID.
     * @param token An OAuth token.
     */
    public Client(String projectId, String token) {
        this(projectId, token, Cloud.ironAWSUSEast);
    }

    /**
     * Constructs a new Client using the specified project ID and token.
     * The network is not accessed during construction and this call will
     * succeed even if the credentials are invalid.
     *
     * @param projectId A 24-character project ID.
     * @param token An OAuth token.
     * @param cloud The cloud to use.
     */
    public Client(String projectId, String token, Cloud cloud) {
        this.projectId = projectId;
        this.token = token;
        this.cloud = cloud;
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
        String path = "/" + apiVersion + "/projects/" + projectId + "/" + endpoint;
        URL url = new URL(cloud.scheme, cloud.host, cloud.port, path);

        final int maxRetries = 5;
        int retries = 0;
        while (true) {
            try {
                return singleRequest(method, url, body);
            } catch (HTTPException e) {
                // ELB sometimes returns this when load is increasing.
                // We retry with exponential backoff.
                if (e.getStatusCode() != 503 || retries >= maxRetries) {
                    throw e;
                }
                retries++;
                // random delay between 0 and 4^tries*100 milliseconds
                int pow = (1 << (2*retries))*100;
                int delay = rand.nextInt(pow);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private JSONObject singleRequest(String method, URL url, String body) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Authorization", "OAuth " + token);
        conn.setRequestProperty("User-Agent", "IronMQ Java Client");

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
            String msg;
            try {
                JSONObject jsonObj = streamToJSON(conn.getErrorStream());
                msg = jsonObj.getString("msg");
            } catch (JSONException e) {
                msg = "IronMQ's response contained invalid JSON";
            }
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
