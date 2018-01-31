package net.moddedminecraft.betterchunkloader.config;

import com.google.common.reflect.TypeToken;
import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.File;
import java.io.IOException;

public class Config {

    private final BetterChunkLoader plugin;

    private CoreConfig coreConfig;
    private MessagesConfig messagesConfig;

    public Config(BetterChunkLoader plugin) {
        this.plugin = plugin;
        if (!this.plugin.configDir.exists()) {
            this.plugin.configDir.mkdirs();
        }
    }

    public boolean loadCore() {
        try {
            File file = new File(plugin.configDir, "core.conf");
            if (!file.exists()) {
                file.createNewFile();
            }
            ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setFile(file).build();
            CommentedConfigurationNode config = loader.load(ConfigurationOptions.defaults().setObjectMapperFactory(plugin.factory).setShouldCopyDefaults(true));
            coreConfig = config.getValue(TypeToken.of(CoreConfig.class), new CoreConfig());
            loader.save(config);
            return true;
        } catch (IOException | ObjectMappingException ex) {
            plugin.getLogger().error("Failed to load core configuration.", ex);
            return false;
        }
    }

    public boolean loadMessages() {
        try {
            File file = new File(plugin.configDir, "messages.conf");
            if (!file.exists()) {
                file.createNewFile();
            }
            ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setFile(file).build();
            CommentedConfigurationNode config = loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));
            messagesConfig = config.getValue(TypeToken.of(MessagesConfig.class), new MessagesConfig());
            loader.save(config);
            return true;
        } catch (IOException | ObjectMappingException ex) {
            plugin.getLogger().error("Failed to load messages configuration.", ex);
            return false;
        }
    }

    public CoreConfig getCore() {
        return coreConfig;
    }

    public MessagesConfig getMessages() {
        return messagesConfig;
    }
}
