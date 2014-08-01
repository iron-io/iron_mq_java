package io.iron.ironmq;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.iron.ironmq.keystone.KeystoneIdentity;
import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang3.StringUtils;

/**
 * The Client class provides access to the IronMQ service.
 */
public class Client {
    static final private String defaultApiVersion = "3";
    private String apiVersion;

    static final Random rand = new Random();

    static final private HashMap<String, Object> defaultOptions;

    static {
        defaultOptions = new HashMap<String, Object>();
        defaultOptions.put("scheme", Cloud.ironAWSUSEast.getScheme());
        defaultOptions.put("host", Cloud.ironAWSUSEast.getHost());
        defaultOptions.put("port", Cloud.ironAWSUSEast.getPort());
    }

    private String projectId;
    private TokenContainer tokenContainer;
    private Cloud cloud;

    private String[] optionsList;
    private Map<String, Object> options;
    private String env;

    static {
        System.setProperty("https.protocols", "TLSv1");
    }

    /**
     * This constructor is equivalent to {@link #Client(String, String, Cloud, Integer) Client(null, null, null, null)}.
     */
    public Client() {
        this(null, (String) null, null, null);
    }

    /**
     * This constructor is equivalent to {@link #Client(String, String, Cloud, Integer) Client(projectId, token, null, null)}.
     *
     * @param projectId A 24-character project ID.
     * @param token     An OAuth token.
     */
    public Client(String projectId, String token) {
        this(projectId, token, null, null);
    }

    /**
     * This constructor is equivalent to {@link #Client(String, String, Cloud, Integer) Client(projectId, token, cloud, null)}.
     *
     * @param projectId A 24-character project ID.
     * @param token     An OAuth token.
     * @param cloud     The cloud to use.
     */
    public Client(String projectId, String token, Cloud cloud) {
        this(projectId, token, cloud, null);
    }

    /**
     * Constructs a new Client using the specified project ID and token.
     * A null projectId, token, or cloud will be filled in using the
     * filesystem and environment for configuration as described
     * <a href="http://dev.iron.io/worker/reference/configuration/">here</a>.
     * The network is not accessed during construction and this call
     * succeeds even if the credentials are invalid.
     *
     * @param projectId  A 24-character project ID.
     * @param token      An OAuth token.
     * @param cloud      The cloud to use.
     * @param apiVersion Version of ironmq api to use, default is 3.
     */
    public Client(String projectId, String token, Cloud cloud, Integer apiVersion) {
        Map<String, Object> userOptions = new HashMap<String, Object>();
        userOptions.put("project_id", projectId);
        userOptions.put("token", token);
        if (cloud != null) {
            userOptions.put("cloud", cloud);
        }
        this.apiVersion = (apiVersion == null || apiVersion < 1) ? defaultApiVersion : apiVersion.toString();

        loadConfiguration("iron", "mq", userOptions, new String[]{"project_id", "token", "cloud"});
    }

    public Client(String projectId, KeystoneIdentity identity, Cloud cloud, Integer apiVersion) {
        Map<String, Object> userOptions = new HashMap<String, Object>();
        userOptions.put("project_id", projectId);
        userOptions.put("keystone", identity.toHash());
        if (cloud != null) {
            userOptions.put("cloud", cloud);
        }
        this.apiVersion = (apiVersion == null || apiVersion < 1) ? defaultApiVersion : apiVersion.toString();

        loadConfiguration("iron", "mq", userOptions, new String[]{"project_id", "token", "cloud"});
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

    Reader delete(String endpoint, String body) throws IOException {
        return request("DELETE", endpoint, body);
    }

    Reader get(String endpoint) throws IOException {
        return request("GET", endpoint, null);
    }

    Reader post(String endpoint, String body) throws IOException {
        return request("POST", endpoint, body);
    }

    Reader put(String endpoint, String body) throws IOException {
        return request("PUT", endpoint, body);
    }

    Reader patch(String endpoint, String body) throws IOException {
        return request("PATCH", endpoint, body);
    }

    private Reader request(String method, String endpoint, String body) throws IOException {
        String path = "/" + apiVersion + "/projects/" + projectId + "/" + endpoint;
        URL url = new URL(cloud.scheme, cloud.host, cloud.port, path);

        System.out.println(method + " " + url + " " + (method != "GET" ? body : ""));

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
                int pow = (1 << (2 * retries)) * 100;
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
        if (method.equals("DELETE") || method.equals("PATCH")) {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("X-HTTP-Method-Override", method);
        } else {
            conn.setRequestMethod(method);
        }
        conn.setRequestProperty("Authorization", "OAuth " + tokenContainer.getToken());
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

    private void loadConfiguration(String company, String product, Map<String, Object> userOptions, String[] extraOptionsList) {
        optionsList = ArrayUtils.addAll(new String[]{"scheme", "host", "port", "user_agent", "keystone"}, extraOptionsList);

        options = new HashMap<String, Object>();

        env = (String) userOptions.get("env");

        if (env == null) {
            env = System.getenv(company.toUpperCase() + "_" + product.toUpperCase() + "_ENV");
        }

        if (env == null) {
            env = System.getenv(company.toUpperCase() + "_ENV");
        }

        if (env == null) {
            env = (String) defaultOptions.get("env");
        }

        loadFromHash(userOptions);
        loadFromConfig(company, product, (String) userOptions.get("config"));

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
            for (String configBase : new String[]{company + "-" + product, company + "_" + product, company}) {
                loadFromConfig(company, product, System.getProperty("user.dir") + "/" + configBase + suffix + ".json");
                loadFromConfig(company, product, System.getProperty("user.dir") + "/." + configBase + suffix + ".json");
                loadFromConfig(company, product, System.getProperty("user.dir") + "/config/" + configBase + suffix + ".json");
                loadFromConfig(company, product, System.getProperty("user.dir") + "/config/." + configBase + suffix + ".json");
                loadFromConfig(company, product, System.getProperty("user.home") + "/" + configBase + suffix + ".json");
                loadFromConfig(company, product, System.getProperty("user.home") + "/." + configBase + suffix + ".json");
            }
        }

        loadFromConfig(company, product, (String) defaultOptions.get("config"));
        loadFromHash(defaultOptions);

        projectId = (String) getOption("project_id");
        String token = (String) getOption("token");

        HashMap<String, Object> keystoneHash = (HashMap<String, Object>) getOption("keystone");
        if (keystoneHash.containsKey("server") && keystoneHash.containsKey("tenant") &&
            keystoneHash.containsKey("username") && keystoneHash.containsKey("password")) {
            System.out.println("via keystone");
            tokenContainer = KeystoneIdentity.fromHash(keystoneHash);
        } else {
            if (StringUtils.isNotBlank(token)) {
                System.out.println("via iron-token");
                tokenContainer = new IronTokenContainer(token);
            }
        }

        if (userOptions.containsKey("cloud")) {
            Object cloudOption = userOptions.get("cloud");
            if (cloudOption != null && cloudOption instanceof Cloud) {
                cloud = (Cloud) cloudOption;
            }
        } else {
            cloud = new Cloud((String) getOption("scheme"), (String) getOption("host"), ((Number) getOption("port")).intValue());
        }
    }

    private void setOption(String name, Object value) {
        if (ArrayUtils.contains(optionsList, name)) {
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

            result = (Map<String, Object>) result.get(sub);
        }

        return result;
    }

    private void loadFromHash(Map<String, Object> hash) {
        if (hash == null) {
            return;
        }

        for (String option : optionsList) {
            setOption(option, hash.get(option));
        }
    }

    private void loadFromConfig(String company, String product, String configFile) {
        if (configFile == null) {
            return;
        }

        File config = new File(configFile);

        if (!config.exists()) {
            return;
        }

        Reader configReader;
        try {
            configReader = new FileReader(config);
        } catch (FileNotFoundException e) {
            return;
        }
        configReader = new BufferedReader(configReader);
        Gson gson = new Gson();

        Map<String, Object> configHash;
        try {
            configHash = (Map<String, Object>) gson.fromJson(configReader, Map.class);
        } finally {
            try {
                configReader.close();
            } catch (IOException e) {
            }
        }

        if (env != null) {
            loadFromHash(getSubHash(configHash, new String[]{env, company + "_" + product}));
            loadFromHash(getSubHash(configHash, new String[]{env, company, product}));
            loadFromHash(getSubHash(configHash, new String[]{env, product}));
            loadFromHash(getSubHash(configHash, new String[]{env, company}));

            loadFromHash(getSubHash(configHash, new String[]{company + "_" + product, env}));
            loadFromHash(getSubHash(configHash, new String[]{company, product, env}));
            loadFromHash(getSubHash(configHash, new String[]{product, env}));
            loadFromHash(getSubHash(configHash, new String[]{company, env}));

            loadFromHash(getSubHash(configHash, new String[]{env}));
        }

        loadFromHash(getSubHash(configHash, new String[]{company + "_" + product}));
        loadFromHash(getSubHash(configHash, new String[]{company, product}));
        loadFromHash(getSubHash(configHash, new String[]{product}));
        loadFromHash(getSubHash(configHash, new String[]{company}));
        loadFromHash(getSubHash(configHash, new String[]{}));
    }

    private void loadFromEnv(String prefix) {
        for (String option : optionsList) {
            setOption(option, System.getenv(prefix + "_" + option.toUpperCase()));
        }
    }

}
