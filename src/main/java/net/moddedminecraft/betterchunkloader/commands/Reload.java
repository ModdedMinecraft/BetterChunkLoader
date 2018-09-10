package net.moddedminecraft.betterchunkloader.commands;

import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import net.moddedminecraft.betterchunkloader.Utilities;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

import java.util.HashMap;
import java.util.Optional;

public class Reload implements CommandExecutor {

    private final BetterChunkLoader plugin;

    public Reload(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource sender, CommandContext commandContext) {
        Optional<String> typeElement = commandContext.<String>getOne("type");
        String currentType;
        Boolean success;
        if (typeElement.isPresent()) {
            switch (typeElement.get()) {
                case "core": {
                    currentType = "core";
                    success = plugin.getConfig().loadCore();
                    break;
                }
                case "messages": {
                    currentType = "messages";
                    success = plugin.getConfig().loadMessages();
                    break;
                }
                case "data": {
                    currentType = "data";
                    plugin.unloadChunks();
                    plugin.getDataStore().load();
                    plugin.loadChunks();
                    success = true;
                    break;
                }
                default: {
                    sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunksReloadUsage));
                    return CommandResult.success();
                }
            }
        } else {
            currentType = "all";
            plugin.unloadChunks();
            plugin.getDataStore().load();
            plugin.loadChunks();
            plugin.getConfig().loadMessages();
            plugin.getConfig().loadCore();
            success = true;
        }
        HashMap<String, String> args = new HashMap<>();
        args.put("type", currentType);
        if (success) {
            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunksReloadSuccess, args));
            return CommandResult.success();
        } else {
            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunksReloadFailure, args));
            return CommandResult.empty();
        }
    }
}
