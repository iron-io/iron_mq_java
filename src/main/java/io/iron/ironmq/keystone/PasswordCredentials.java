package io.iron.ironmq.keystone;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class PasswordCredentials {
    protected String username;
    protected String password;

    public PasswordCredentials() {
    }

    public PasswordCredentials(String username, String password) {
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
}
