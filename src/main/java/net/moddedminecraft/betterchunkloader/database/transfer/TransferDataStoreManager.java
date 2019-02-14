package net.moddedminecraft.betterchunkloader.database.transfer;

import net.moddedminecraft.betterchunkloader.BetterChunkLoader;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public final class TransferDataStoreManager {

    private final BetterChunkLoader plugin;

    private final Map<String, Class<? extends TransferIDataStore>> dataStores = new HashMap<>();
    private TransferIDataStore dataStore;

    public TransferDataStoreManager(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    public boolean load() {
        if (getDataStore() != null) {
            clearDataStores();
        }
        registerDataStore("MYSQL", TransferMYSQLDataStore.class);
        switch (plugin.getConfig().getCore().storageEngine.toUpperCase()) {
            case "MYSQL":
                setDataStoreInstance("MYSQL");
                plugin.getLogger().info("Loading transfer datastore: MySQL");
                return getDataStore().load();
            default:
                plugin.getLogger().error("Unable to determine selected datastore.");
                plugin.getLogger().info("Available datastores: " + getAvailableDataStores().toString());
                return false;
        }
    }

    /**
     * Register a new Data Store. This should be run at onLoad()<br>
     *
     * @param dataStoreId ID that identifies this data store <br>
     * @param dataStoreClass a class that implements IDataStore
     */
    public void registerDataStore(String dataStoreId, Class<? extends TransferIDataStore> dataStoreClass) {
        dataStores.put(dataStoreId, dataStoreClass);
    }

    /**
     * Unregisters the data store with the provided id
     *
     * @param dataStoreId
     */
    public void unregisterDataStore(String dataStoreId) {
        dataStores.remove(dataStoreId);
    }

    /**
     * Unregisters all data stores
     */
    public void clearDataStores() {
        dataStores.clear();
    }

    /**
     * List of registered data stores id
     *
     * @return
     */
    public List<String> getAvailableDataStores() {
        List<String> list = new ArrayList<>();
        list.addAll(dataStores.keySet());
        return Collections.unmodifiableList(list);
    }

    /**
     * Sets and instantiate the data store
     *
     * @param dataStoreId
     */
    private void setDataStoreInstance(String dataStoreId) {
        try {
            dataStore = dataStores.get(dataStoreId).getConstructor(BetterChunkLoader.class).newInstance(this.plugin);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | InvocationTargetException | SecurityException e) {
            throw new RuntimeException("Couldn't instantiate data store " + dataStoreId + " " + e);
        }
    }

    /**
     * Gets current data store. Returns null if there isn't an instantiated data
     * store
     *
     * @return
     */
    public TransferIDataStore getDataStore() {
        return dataStore;
    }

}
