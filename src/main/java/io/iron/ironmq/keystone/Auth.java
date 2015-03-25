package io.iron.ironmq.keystone;

public class Auth {
    String tenantName;
    PasswordCredentials passwordCredentials;

    public Auth() {
    }

    public Auth(String tenantName, PasswordCredentials passwordCredentials) {
        this.tenantName = tenantName;
        this.passwordCredentials = passwordCredentials;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public PasswordCredentials getPasswordCredentials() {
        return passwordCredentials;
    }

    public void setPasswordCredentials(PasswordCredentials passwordCredentials) {
        this.passwordCredentials = passwordCredentials;
    }
}
