package io.iron.ironmq.keystone;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.iron.ironmq.HttpClient;
import io.iron.ironmq.TokenContainer;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;

public class KeystoneIdentity implements TokenContainer {
    String username;
    String password;
    Token tokenInfo;

    public KeystoneIdentity(String username, String password) {
        this.username = username;
        this.password = password;
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
            // 1. replace body with gsonified object [OK]
            // 2. add tenant and domain to config
            // 3. get token from header
            // 4. ...

            String path = "/v2.0/tokens";
            String scheme = "http";
            String host = "ec2-54-91-225-58.compute-1.amazonaws.com";
            int port = 80;
            String method = "POST";
            String tenant = "people";

            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.S")
                    .create();

            KeystoneGetTokenPayload payload = new KeystoneGetTokenPayload(
                new Auth(
                    tenant,
                    new PasswordCredentials(username, password)
                )
            );
            String body = gson.toJson(payload);

            URL url = new URL(scheme, host, port, path);

            System.out.println(method + " " + url + " " + (method != "GET" ? body : ""));

            HttpClient client = HttpClient.create();
            HashMap<String, String> headers = new HashMap<String, String>() {{
                put("Content-Type", "application/json");
                put("Accept", "application/json");
            }};
            Reader response = client.singleRequest(method, url, body, headers);

            KeystoneGetTokenResponse tokenResponse = gson.fromJson(response, KeystoneGetTokenResponse.class);
            response.close();

            System.out.println(tokenResponse.getAccess().getToken().getId());
            tokenInfo = tokenResponse.getAccess().getToken();
        }

        return tokenInfo.getId();
    }

    public static String readFully(Reader reader) throws IOException {
        char[] arr = new char[8*1024]; // 8K at a time
        StringBuffer buf = new StringBuffer();
        int numChars;

        while ((numChars = reader.read(arr, 0, arr.length)) > 0) {
            buf.append(arr, 0, numChars);
        }

        return buf.toString();
    }
}
