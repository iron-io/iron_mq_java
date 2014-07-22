package io.iron.ironmq.keystone;

public class Identity {
    String[] methods;
    Password password;
    Scope scope;

    public Identity() {
    }

    public Identity(String[] methods, Password password, Scope scope) {
        this.methods = methods;
        this.password = password;
        this.scope = scope;
    }

    public String[] getMethods() {
        return methods;
    }

    public void setMethods(String[] methods) {
        this.methods = methods;
    }

    public Password getPassword() {
        return password;
    }

    public void setPassword(Password password) {
        this.password = password;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }
}
