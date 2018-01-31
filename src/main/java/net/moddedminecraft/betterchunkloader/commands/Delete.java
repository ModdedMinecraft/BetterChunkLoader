package net.moddedminecraft.betterchunkloader.commands;

import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import net.moddedminecraft.betterchunkloader.Permissions;
import net.moddedminecraft.betterchunkloader.Utilities;
import net.moddedminecraft.betterchunkloader.data.ChunkLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;

import java.io.IOException;
import java.util.*;

public class Delete implements CommandExecutor {

    private final BetterChunkLoader plugin;

    public Delete(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource sender, CommandContext commandContext) throws CommandException {
        Optional<Player> playerName = commandContext.<Player>getOne("player");
        Optional<String> loaderType = commandContext.<String>getOne("type");

        HashMap<String, String> args = new HashMap<>();

        if (!loaderType.isPresent()) {
            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.delete.usage, args));
            return CommandResult.empty();
        }

        args.put("type", loaderType.get());

        if (playerName.isPresent()) {
            args.put("player", playerName.get().getName());
            if (!sender.hasPermission(Permissions.COMMAND_DELETE + ".others")) {
                sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.delete.others.noPermission, args));
                return CommandResult.empty();
            }
            Optional<UUID> playerUUID = Utilities.getPlayerUUID(playerName.get().getName());
            if (!playerUUID.isPresent()) {
                sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.noPlayerExists, args));
                return CommandResult.empty();
            }
            if (loaderType.get().equalsIgnoreCase("online") || loaderType.get().equalsIgnoreCase("alwayson")) {
                List<ChunkLoader> clList = getChunkLoadersByType(playerUUID.get(), loaderType.get().equalsIgnoreCase("alwayson"));

                int success = 0;
                for (ChunkLoader chunkLoader : clList) {
                    plugin.getChunkManager().unloadChunkLoader(chunkLoader);
                    plugin.chunkLoaderData.remove(chunkLoader.getUniqueId());
                    success++;
                }
                if (success > 0) {
                    try {
                        plugin.saveData();
                        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.delete.others.success, args));
                        return CommandResult.success();
                    } catch (IOException | ObjectMappingException e) {
                        e.printStackTrace();
                        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.delete.others.failure, args));
                        return CommandResult.success();
                    }
                } else {
                    sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.delete.others.failure, args));
                    return CommandResult.success();
                }
            } else {
                sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.delete.invalidType, args));
                return CommandResult.empty();
            }
        } else {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (loaderType.get().equalsIgnoreCase("online") || loaderType.get().equalsIgnoreCase("alwayson")) {
                    List<ChunkLoader> clList = getChunkLoadersByType(player.getUniqueId(), loaderType.get().equalsIgnoreCase("alwayson"));

                    int success = 0;
                    for (ChunkLoader chunkLoader : clList) {
                        plugin.getChunkManager().unloadChunkLoader(chunkLoader);
                        plugin.chunkLoaderData.remove(chunkLoader.getUniqueId());
                        success++;
                    }
                    if (success > 0) {
                        try {
                            plugin.saveData();
                            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.delete.own.success, args));
                            return CommandResult.success();
                        } catch (IOException | ObjectMappingException e) {
                            e.printStackTrace();
                            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.delete.own.failure, args));
                            return CommandResult.empty();
                        }
                    } else {
                        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.delete.own.failure, args));
                        return CommandResult.empty();
                    }
                } else {
                    sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.delete.invalidType, args));
                    return CommandResult.empty();
                }
            } else {
                sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.delete.consoleError, args));
                return CommandResult.empty();
            }
        }
    }

    public List<ChunkLoader> getChunkLoadersByType(UUID owner, Boolean type) {
        List<ChunkLoader> chunkLoaders = new ArrayList<>();
        plugin.dataManager.getChunkLoadersByOwner(owner).stream().filter((cl) -> (cl.isAlwaysOn().equals(type))).forEachOrdered((cl) -> {
            chunkLoaders.add(cl);
        });
        return chunkLoaders;
    }
}
