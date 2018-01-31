package net.moddedminecraft.betterchunkloader.commands;

import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import net.moddedminecraft.betterchunkloader.Utilities;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

public class Reload implements CommandExecutor {

    private final BetterChunkLoader plugin;

    public Reload(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource sender, CommandContext commandContext) throws CommandException {
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
                    try {
                        plugin.unloadChunks();
                        plugin.loadData();
                        plugin.loadChunks();
                        success = true;
                    } catch (IOException | ObjectMappingException e) {
                        success = false;
                        e.printStackTrace();
                        break;
                    }
                    break;
                }
                default: {
                    sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.reload.usage));
                    return CommandResult.success();
                }
            }
        } else {
            currentType = "all";
            try {
                plugin.unloadChunks();
                plugin.loadData();
                plugin.loadChunks();
                plugin.getConfig().loadMessages();
                plugin.getConfig().loadCore();
                success = true;
            } catch (IOException | ObjectMappingException e) {
                success = false;
                e.printStackTrace();
            }
        }
        HashMap<String, String> args = new HashMap<>();
        args.put("type", currentType);
        if (success) {
            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.reload.success, args));
            return CommandResult.success();
        } else {
            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.reload.failure, args));
            return CommandResult.empty();
        }
    }
}
