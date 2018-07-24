package net.moddedminecraft.betterchunkloader.config;

import net.moddedminecraft.betterchunkloader.BetterChunkLoader;

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
            coreConfig = new CoreConfig(plugin);
            return true;
        } catch (IOException ex) {
            plugin.getLogger().error("Failed to load core configuration.", ex);
            return false;
        }
    }

    public boolean loadMessages() {
        try {
            messagesConfig = new MessagesConfig(plugin);
            return true;
        } catch (IOException ex) {
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
