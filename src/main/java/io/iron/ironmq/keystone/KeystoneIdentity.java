package io.iron.ironmq.keystone;

import io.iron.ironmq.HttpClient;
import io.iron.ironmq.TokenContainer;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;

public class KeystoneIdentity implements TokenContainer {
    String username;
    String password;
    String token;
    Token tokenInfo;

    public KeystoneIdentity(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public KeystoneIdentity(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() throws IOException {
        if (tokenInfo == null || tokenInfo.isExpired()) {
            //singleRequest()

            // TODO:
            // 1. replace body with gsonified object
            // 2. get token from header
            // 3. ...

            String body = "{\"auth\":{\"identity\":{\"methods\":[\"password\"],\"password\":{\"user\":{\"password\":\"eleven-some-neat\",\"name\":\"qaas\",\"domain\":{\"id\":\"default\"}}}, \"scope\":{\"id\":\"5feece3b2ade44dfa2df60411c63110d\"}}}}";
            String path = "/identity/v3/auth/tokens";
            String scheme = "http";
            String host = "108.244.164.20";
            int port = 80;
            String method = "POST";

            URL url = new URL(scheme, host, port, path);

            System.out.println(method + " " + url + " " + (method != "GET" ? body : ""));

            HttpClient client = HttpClient.create();
            HashMap<String, String> headers = new HashMap<String, String>() {{
                put("Content-Type", "application/json");
                put("Accept", "application/json");
            }};
            Reader response = client.singleRequest(method, url, body, headers);

            //Ids ids = gson.fromJson(response, Ids.class);
            response.close();

        }

        return token;
    }

    void setToken(String token) {
        this.token = token;
    }
}
