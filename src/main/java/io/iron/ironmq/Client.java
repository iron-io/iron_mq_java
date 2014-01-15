package io.iron.ironmq;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;

/**
 * The Client class provides access to the IronMQ service.
 */
public class Client {
    static final private String apiVersion = "1";

    static final Random rand = new Random();

    static final private HashMap<String, Object> defaultOptions;

    static {
        defaultOptions = new HashMap<String, Object>();
        defaultOptions.put("scheme", Cloud.ironAWSUSEast.getScheme());
        defaultOptions.put("host", Cloud.ironAWSUSEast.getHost());
        defaultOptions.put("port", Cloud.ironAWSUSEast.getPort());
    }

    private String projectId;
    private String token;
    private Cloud cloud;

    private String[] options_list;
    private Map<String, Object> options;
    private String env;

    static {
        System.setProperty("https.protocols", "TLSv1");
    }

    public Client() {
        this(null, null, null);
    }

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
        this(projectId, token, null);
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
        Map<String, Object> user_options = new HashMap<String, Object>();
        user_options.put("project_id", projectId);
        user_options.put("token", token);

        loadConfiguration("iron", "mq", user_options, new String[]{"project_id", "token"});
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

    Reader delete(String endpoint) throws IOException {
        return request("DELETE", endpoint, null);
    }

    Reader get(String endpoint) throws IOException {
        return request("GET", endpoint, null);
    }

    Reader post(String endpoint, String body) throws IOException {
        return request("POST", endpoint, body);
    }

    private Reader request(String method, String endpoint, String body) throws IOException {
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

    static private class Error implements Serializable {
        String msg;
    }

    private Reader singleRequest(String method, URL url, String body) throws IOException {
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
            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            out.write(body);
            out.close();
        }

        int status = conn.getResponseCode();
        if (status != 200) {
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
        }

        return new InputStreamReader(conn.getInputStream());
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public Object getOption(String name) {
        return options.get(name);
    }

    public String getEnv() {
        return env;
    }

    private void loadConfiguration(String company, String product, Map<String, Object> user_options, String[] extra_options_list) {
        options_list = ArrayUtils.addAll(new String[]{"scheme", "host", "port", "user_agent"}, extra_options_list);

        options = new HashMap<String, Object>();

        env = (String)user_options.get("env");

        if (env == null) {
            env = System.getenv(company.toUpperCase() + "_" + product.toUpperCase() + "_ENV");
        }

        if (env == null) {
            env = System.getenv(company.toUpperCase() + "_ENV");
        }

        if (env == null) {
            env = (String)defaultOptions.get("env");
        }

        loadFromHash(user_options);
        loadFromConfig(company, product, (String)user_options.get("config"));

        loadFromConfig(company, product, System.getenv(company.toUpperCase() + "_" + product.toUpperCase() + "_CONFIG"));
        loadFromConfig(company, product, System.getenv(company.toUpperCase() + "_CONFIG"));

        loadFromEnv(company.toUpperCase() + "_" + product.toUpperCase());
        loadFromEnv(company.toUpperCase());

        List<String> suffixes = new ArrayList<String>();

        if (env != null) {
            suffixes.add("-" + env);
            suffixes.add("_" + env);
        }

        suffixes.add("");

        for (String suffix : suffixes) {
            for (String config_base : new String[]{company + "-" + product, company + "_" + product, company}) {
                loadFromConfig(company, product, System.getProperty("user.dir") + "/" + config_base + suffix + ".json");
                loadFromConfig(company, product, System.getProperty("user.dir") + "/." + config_base + suffix + ".json");
                loadFromConfig(company, product, System.getProperty("user.dir") + "/config/" + config_base + suffix + ".json");
                loadFromConfig(company, product, System.getProperty("user.dir") + "/config/." + config_base + suffix + ".json");
                loadFromConfig(company, product, System.getProperty("user.home") + "/" + config_base + suffix + ".json");
                loadFromConfig(company, product, System.getProperty("user.home") + "/." + config_base + suffix + ".json");
            }
        }

        loadFromConfig(company, product, (String)defaultOptions.get("config"));
        loadFromHash(defaultOptions);

        projectId = (String)getOption("project_id");
        token = (String)getOption("token");

        cloud = new Cloud((String)getOption("scheme"), (String)getOption("host"), ((Double)getOption("port")).intValue());
    }

    private void setOption(String name, Object value) {
        if (ArrayUtils.contains(options_list, name)) {
            if (options.get(name) == null && value != null) {
                options.put(name, value);
            }
        }
    }

    private Map<String, Object> getSubHash(Map<String, Object> hash, String[] subs) {
        Map<String, Object> result = hash;

        for (String sub : subs) {
            if (result.get(sub) == null) {
                return null;
            }

            result = (Map<String, Object>)result.get(sub);
        }

        return result;
    }

    private void loadFromHash(Map<String, Object> hash) {
        if (hash == null) {
            return;
        }

        for (String option : options_list) {
            setOption(option, hash.get(option));
        }
    }

    private void loadFromConfig(String company, String product, String config_file) {
        if (config_file == null) {
            return;
        }

        File config = new File(config_file);

        if (!config.exists()) {
            return;
        }

        String config_data;

        try {
            config_data = FileUtils.readFileToString(config);
        } catch (IOException e) {
            return;
        }

        Gson gson = new Gson();
        Map<String, Object> config_hash = (Map<String, Object>)gson.fromJson(config_data, Map.class);

        if (env != null) {
            loadFromHash(getSubHash(config_hash, new String[]{env, company + "_" + product}));
            loadFromHash(getSubHash(config_hash, new String[]{env, company, product}));
            loadFromHash(getSubHash(config_hash, new String[]{env, product}));
            loadFromHash(getSubHash(config_hash, new String[]{env, company}));

            loadFromHash(getSubHash(config_hash, new String[]{company + "_" + product, env}));
            loadFromHash(getSubHash(config_hash, new String[]{company, product, env}));
            loadFromHash(getSubHash(config_hash, new String[]{product, env}));
            loadFromHash(getSubHash(config_hash, new String[]{company, env}));

            loadFromHash(getSubHash(config_hash, new String[]{env}));
        }

        loadFromHash(getSubHash(config_hash, new String[]{company + "_" + product}));
        loadFromHash(getSubHash(config_hash, new String[]{company, product}));
        loadFromHash(getSubHash(config_hash, new String[]{product}));
        loadFromHash(getSubHash(config_hash, new String[]{company}));
        loadFromHash(getSubHash(config_hash, new String[]{}));
    }

    private void loadFromEnv(String prefix) {
        for (String option : options_list) {
            setOption(option, System.getenv(prefix + "_" + option.toUpperCase()));
        }
    }

}
