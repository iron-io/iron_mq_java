package io.iron.ironmq.keystone;

public class KeystoneGetTokenPayload {
    Auth auth;

    public KeystoneGetTokenPayload(Auth auth) {
        this.auth = auth;
    }

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }
}
