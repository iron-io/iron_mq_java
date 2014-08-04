package io.iron.ironmq.keystone;

import com.google.gson.annotations.SerializedName;

import javax.sound.midi.MidiFileFormat;
import java.util.Date;

public class Token {
    protected Date localIssuedAt;
    protected Tenant tenant;
    protected String id;
    @SerializedName("issued_at") protected Date issuedAt;
    @SerializedName("expires")   protected Date expiresAt;

    public Token() {
        localIssuedAt = new Date();
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Date issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }

    /**
     * Indicated if token will expire after a few seconds.
     */
    public boolean isExpired() {
        return isExpired(10);
    }

    /**
     * Indicated if token will expire after N seconds.
     * @param seconds Number of seconds
     */
    public boolean isExpired(int seconds) {
        long diff = localIssuedAt.getTime() - issuedAt.getTime();
        long localExpiresAtTime = expiresAt.getTime() + diff;
        return (new Date().getTime() - seconds * 1000) >= localExpiresAtTime;
    }
}
