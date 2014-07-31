package io.iron.ironmq.keystone;

public class Access {
    protected Token token;
    protected User user;

    public Access() {
    }

    public Access(Token token, User user) {
        this.token = token;
        this.user = user;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
