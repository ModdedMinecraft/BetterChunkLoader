package net.moddedminecraft.betterchunkloader.database.transfer;

import net.moddedminecraft.betterchunkloader.data.PlayerData;

import java.util.List;

public interface TransferIDataStore {

    public abstract String getDatabaseName();

    public abstract boolean load();

    public List<PlayerData> getPlayerData();

}
