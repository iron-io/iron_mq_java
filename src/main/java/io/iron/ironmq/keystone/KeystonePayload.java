package io.iron.ironmq.keystone;

public class KeystonePayload {
    Auth auth;

    public KeystonePayload(Auth auth) {
        this.auth = auth;
    }

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }
}
