package net.moddedminecraft.betterchunkloader.config;

import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.io.IOException;
import java.nio.file.Path;

@ConfigSerializable
public class CoreConfig {

    public final BetterChunkLoader plugin;

    public static ConfigurationLoader<CommentedConfigurationNode> loader;
    public static CommentedConfigurationNode config;

    public Path fileLoc;

    public CoreConfig(BetterChunkLoader main) throws IOException {
        plugin = main;
        fileLoc = plugin.Configdir.resolve("core.conf");
        loader = HoconConfigurationLoader.builder().setPath(fileLoc).build();
        config = loader.load();
        configCheck();
    }

    public Boolean debug = false;

    public String title = "&8[&6Chunkloader&8]&r";

    public Integer maxSize = 5;
    public String activeItemType = "minecraft:potion";
    public String inactiveItemType = "minecraft:glass_bottle";
    public String removeItemType = "minecraft:redstone_torch";

    public Integer loadDelay = 10;
    public String wandType = "minecraft:blaze_rod";

    public Integer defaultOnline = 1;
    public Integer maxOnline = 100;
    public String onlineBlockType = "minecraft:iron_block";

    public Integer defaultAlwaysOn = 0;
    public Integer maxAlwaysOn = 50;
    public Integer alwaysOnExpiry = 24;
    public String alwaysOnBlockType = "minecraft:diamond_block";

    //Database
    public String storageEngine;
    public String databaseFile;
    public String h2Prefix;
    public String mysqlHost;
    public int mysqlPort;
    public String mysqlDatabase;
    public String mysqlUser;
    public String mysqlPass;
    public String mysqlPrefix;
    public String server;



    private void configCheck() throws IOException {
        if (!fileLoc.toFile().exists()) {
            fileLoc.toFile().createNewFile();
        }

        //server
        server = check(config.getNode("server"), "", "Unique name of the server. Used for chunkloader server identification").getString();

        debug = check(config.getNode("Debug"), debug).getBoolean();
        title = check(config.getNode("Title"), title).getString();

        //Menu
        maxSize = check(config.getNode("Menu", "MaxSize"), maxSize, "Max chunkloader size to display in the menu. [0-7]").getInt();
        activeItemType = check(config.getNode("Menu", "ActiveRadiusItemType"), activeItemType).getString();
        inactiveItemType = check(config.getNode("Menu", "InactiveRadiusItemType"), inactiveItemType).getString();
        removeItemType = check(config.getNode("Menu", "RemoveItemType"), removeItemType).getString();

        //ChunkLoader
        loadDelay = check(config.getNode("ChunkLoader", "LoadDelay"), loadDelay, "Delay before loading chunkloaders during startup. (Seconds).").getInt();
        wandType = check(config.getNode("ChunkLoader", "WandType"), wandType).getString();

        //ChunkLoader Online
        defaultOnline = check(config.getNode("ChunkLoader", "Online", "DefaultOnline"), defaultOnline).getInt();
        maxOnline = check(config.getNode("ChunkLoader", "Online", "MaxOnline"), maxOnline).getInt();
        onlineBlockType = check(config.getNode("ChunkLoader", "Online", "BlockType"), onlineBlockType).getString();

        //ChunkLoader AlwaysOn
        defaultAlwaysOn = check(config.getNode("ChunkLoader", "AlwaysOn", "DefaultAlwaysOn"), defaultAlwaysOn).getInt();
        maxAlwaysOn = check(config.getNode("ChunkLoader", "AlwaysOn", "MaxAlwaysOn"), maxAlwaysOn).getInt();
        alwaysOnExpiry = check(config.getNode("ChunkLoader", "AlwaysOn", "Expiry"), alwaysOnExpiry, "Max amount in hours the owner can be offline before considering this loader 'Expired'").getInt();
        alwaysOnBlockType = check(config.getNode("ChunkLoader", "AlwaysOn", "BlockType"), alwaysOnBlockType).getString();

        //Database
        storageEngine = check(config.getNode("Storage", "storage-engine"), "h2", "The stoage engine that should be used, Allowed values: h2 or mysql").getString();
        databaseFile = check(config.getNode("Storage", "h2", "database-file"), "Database.db", "Where the databaseFile will be stored. Can be a relative or absolute path. An absolute path is recommended when using this to synchronize over several servers").getString();
        h2Prefix = check(config.getNode("Storage", "h2", "prefix"), "bcl_", "Prefix for the plugin tables").getString();
        mysqlHost = check(config.getNode("Storage", "mysql", "host"), "localhost", "Host of the MySQL Server").getString();
        mysqlPort = check(config.getNode("Storage", "mysql", "port"), "3306", "Port of the MySQL server. Default: 3306").getInt();
        mysqlDatabase = check(config.getNode("Storage", "mysql", "database"), "betterchunkloader", "The database to store in").getString();
        mysqlUser = check(config.getNode("Storage", "mysql", "user"), "root", "The user for the database").getString();
        mysqlPass = check(config.getNode("Storage", "mysql", "password"), "pass", "Password for that user").getString();
        mysqlPrefix = check(config.getNode("Storage", "mysql", "table-prefix"), "bcl_", "Prefix for the plugin tables").getString();


        loader.save(config);
    }

    private CommentedConfigurationNode check(CommentedConfigurationNode node, Object defaultValue, String comment) {
        if (node.isVirtual()) {
            node.setValue(defaultValue).setComment(comment);
        }
        return node;
    }

    private CommentedConfigurationNode check(CommentedConfigurationNode node, Object defaultValue) {
        if (node.isVirtual()) {
            node.setValue(defaultValue);
        }
        return node;
    }
}
