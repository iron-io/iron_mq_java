package io.iron.ironmq.keystone;

public class Auth {
    Identity identity;

    public Auth() {
    }

    public Auth(Identity identity) {
        this.identity = identity;
    }

    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }
}
