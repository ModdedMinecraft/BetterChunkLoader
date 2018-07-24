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


    private void configCheck() throws IOException {
        if (!fileLoc.toFile().exists()) {
            fileLoc.toFile().createNewFile();
        }

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
