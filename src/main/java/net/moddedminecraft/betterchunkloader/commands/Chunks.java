package net.moddedminecraft.betterchunkloader.commands;

import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import net.moddedminecraft.betterchunkloader.Utilities;
import net.moddedminecraft.betterchunkloader.data.PlayerData;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.User;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class Chunks implements CommandExecutor {

    private final BetterChunkLoader plugin;

    public Chunks(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource sender, CommandContext commandContext) throws CommandException {
        String chunksChangeOperatorElement = commandContext.<String>getOne("change").get();
        String loaderTypeElement = commandContext.<String>getOne("type").get();
        User playerName = commandContext.<User>getOne("player").get();
        Integer changeValue = commandContext.<Integer>getOne("value").get();

        Optional<UUID> playerUUID = Utilities.getPlayerUUID(playerName.getName());
        if (!playerUUID.isPresent()) {
            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().noPlayerExists));
            return CommandResult.empty();
        }
        Optional<PlayerData> playerData = plugin.dataManager.getPlayerDataFor(playerUUID.get());
        if (!playerData.isPresent()) {
            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().noPlayerExists));
            return CommandResult.empty();
        }

        HashMap<String, String> args = new HashMap<String, String>() {{
            put("target", playerData.get().getName());
            put("targetUUID", playerData.get().getUnqiueId().toString());
            put("online", String.valueOf(playerData.get().getOnlineChunks()));
            put("alwayson", String.valueOf(playerData.get().getAlwaysOnChunks()));
            put("maxOnline", String.valueOf(plugin.getConfig().getCore().maxOnline));
            put("maxAlwaysOn", String.valueOf(plugin.getConfig().getCore().maxAlwaysOn));
            put("chunks", String.valueOf(changeValue));
        }};

        switch (chunksChangeOperatorElement) {
            case "add": {
                switch (loaderTypeElement) {
                    case "alwayson": {
                        args.put("type", "Always On");
                        args.put("limit", String.valueOf(plugin.getConfig().getCore().maxAlwaysOn));
                        if (playerData.get().getAlwaysOnChunks() + changeValue < 0 || playerData.get().getAlwaysOnChunks() + changeValue > plugin.getConfig().getCore().maxAlwaysOn) {
                            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunksAddFailure, args));
                            return CommandResult.empty();
                        }

                        playerData.get().addAlwaysOnChunks(changeValue);

                        try {
                            plugin.saveData();
                        } catch (IOException | ObjectMappingException e) {
                            e.printStackTrace();
                        }

                        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunksAddSuccess, args));
                        return CommandResult.success();
                    }
                    case "online": {
                        args.put("type", "Online Only");
                        args.put("limit", String.valueOf(plugin.getConfig().getCore().maxOnline));
                        if (playerData.get().getOnlineChunks() + changeValue < 0 || playerData.get().getOnlineChunks() + changeValue > plugin.getConfig().getCore().maxOnline) {
                            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunksAddFailure, args));
                            return CommandResult.empty();
                        }
                        playerData.get().addOnlineChunks(changeValue);

                        try {
                            plugin.saveData();
                        } catch (IOException | ObjectMappingException e) {
                            e.printStackTrace();
                        }

                        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunksAddSuccess, args));
                        return CommandResult.success();
                    }
                    default: {
                        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunksUsage, args));
                        return CommandResult.empty();
                    }
                }
            }
            case "remove": {
                switch (loaderTypeElement) {
                    case "alwayson": {
                        args.put("type", "Always On");
                        args.put("limit", String.valueOf(plugin.getConfig().getCore().maxAlwaysOn));
                        if (playerData.get().getAlwaysOnChunks() - changeValue < 0 || playerData.get().getAlwaysOnChunks() - changeValue > plugin.getConfig().getCore().maxAlwaysOn) {
                            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunksRemoveFailure, args));
                            return CommandResult.empty();
                        }

                        playerData.get().removeAlwaysOnChunks(changeValue);

                        try {
                            plugin.saveData();
                        } catch (IOException | ObjectMappingException e) {
                            e.printStackTrace();
                        }

                        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunksRemoveSuccess, args));
                        return CommandResult.success();
                    }
                    case "online": {
                        args.put("type", "Online Only");
                        args.put("limit", String.valueOf(plugin.getConfig().getCore().maxOnline));
                        if (playerData.get().getOnlineChunks() - changeValue < 0 || playerData.get().getOnlineChunks() - changeValue > plugin.getConfig().getCore().maxOnline) {
                            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunksRemoveFailure, args));
                            return CommandResult.empty();
                        }
                        playerData.get().removeOnlineChunks(changeValue);

                        try {
                            plugin.saveData();
                        } catch (IOException | ObjectMappingException e) {
                            e.printStackTrace();
                        }

                        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunksRemoveSuccess, args));
                        return CommandResult.success();
                    }
                    default: {
                        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunksUsage, args));
                        return CommandResult.empty();
                    }
                }
            }
            case "set": {
                if (changeValue < 0) {
                    sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunksSetFailure, args));
                    return CommandResult.empty();
                }
                switch (loaderTypeElement) {
                    case "alwayson": {
                        args.put("type", "Always On");
                        args.put("limit", String.valueOf(plugin.getConfig().getCore().maxAlwaysOn));
                        playerData.get().setAlwaysOnChunks(changeValue);

                        try {
                            plugin.saveData();
                        } catch (IOException | ObjectMappingException e) {
                            e.printStackTrace();
                        }

                        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunksSetSuccess, args));
                        return CommandResult.success();
                    }
                    case "online": {
                        args.put("type", "Online Only");
                        args.put("limit", String.valueOf(plugin.getConfig().getCore().maxOnline));
                        playerData.get().setOnlineChunks(changeValue);

                        try {
                            plugin.saveData();
                        } catch (IOException | ObjectMappingException e) {
                            e.printStackTrace();
                        }

                        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunksSetSuccess, args));
                        return CommandResult.success();
                    }
                    default: {
                        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunksUsage, args));
                        return CommandResult.empty();
                    }
                }
            }
            default: {
                sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunksUsage, args));
                return CommandResult.empty();
            }
        }
    }
}
