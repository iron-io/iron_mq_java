package io.iron.ironmq;

public class IronTokenContainer implements TokenContainer {
    String token;

    public IronTokenContainer(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
