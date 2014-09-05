package io.iron.ironmq.keystone;

public class KeystoneGetTokenResponse {
    Access access;

    public KeystoneGetTokenResponse() {
    }

    public KeystoneGetTokenResponse(Access access) {
        this.access = access;
    }

    public Access getAccess() {
        return access;
    }

    public void setAccess(Access access) {
        this.access = access;
    }
}
