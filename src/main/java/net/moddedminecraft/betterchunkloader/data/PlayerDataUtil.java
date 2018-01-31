package net.moddedminecraft.betterchunkloader.data;

import java.util.UUID;

public class PlayerDataUtil {

    protected String name;
    protected UUID uuid;
    protected Long lastOnline;
    protected Integer onlineChunksAmount, alwaysOnChunksAmount;

    public PlayerDataUtil(String name, UUID uuid, Long lastOnline, Integer onlineChunksAmount, Integer alwaysOnChunksAmount) {
        this.name = name;
        this.uuid = uuid;
        this.lastOnline = lastOnline;
        this.onlineChunksAmount = onlineChunksAmount;
        this.alwaysOnChunksAmount = alwaysOnChunksAmount;
    }

    public String getName() {
        return name;
    }

    public void setName(String username) {
        this.name = username;
    }

    public UUID getUnqiueId() {
        return uuid;
    }

    public Long getLastOnline() {
        return lastOnline;
    }

    public void setLastOnline(Long lastOnline) {
        this.lastOnline = lastOnline;
    }

    public int getOnlineChunks() {
        return onlineChunksAmount;
    }

    public void setOnlineChunks(Integer onlineChunksAmount) {
        this.onlineChunksAmount = onlineChunksAmount;
    }

    public void addOnlineChunks(Integer onlineChunksAmount) {
        this.onlineChunksAmount = this.onlineChunksAmount + onlineChunksAmount;
    }

    public void removeOnlineChunks(Integer onlineChunksAmount) {
        this.onlineChunksAmount = this.onlineChunksAmount - onlineChunksAmount;
    }

    public int getAlwaysOnChunks() {
        return alwaysOnChunksAmount;
    }

    public void setAlwaysOnChunks(Integer alwaysOnChunksAmount) {
        this.alwaysOnChunksAmount = alwaysOnChunksAmount;
    }

    public void addAlwaysOnChunks(Integer alwaysOnChunksAmount) {
        this.alwaysOnChunksAmount = this.alwaysOnChunksAmount + alwaysOnChunksAmount;
    }

    public void removeAlwaysOnChunks(Integer alwaysOnChunksAmount) {
        this.alwaysOnChunksAmount = this.alwaysOnChunksAmount - alwaysOnChunksAmount;
    }


}
