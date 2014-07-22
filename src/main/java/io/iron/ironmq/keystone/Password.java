package io.iron.ironmq.keystone;

public class Password {
    User user;

    public Password() {
    }

    public Password(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
