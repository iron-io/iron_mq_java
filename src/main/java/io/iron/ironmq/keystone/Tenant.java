package io.iron.ironmq.keystone;

public class Tenant {
    String description;
    String enabled;
    String id;
    String name;

    public Tenant() {
    }

    public Tenant(String description, String enabled, String id, String name) {
        this.description = description;
        this.enabled = enabled;
        this.id = id;
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
