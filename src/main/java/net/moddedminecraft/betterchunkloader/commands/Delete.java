package net.moddedminecraft.betterchunkloader.commands;

import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import net.moddedminecraft.betterchunkloader.Permissions;
import net.moddedminecraft.betterchunkloader.Utilities;
import net.moddedminecraft.betterchunkloader.data.ChunkLoader;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Delete implements CommandExecutor {

    private final BetterChunkLoader plugin;

    public Delete(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource sender, CommandContext commandContext) {
        Optional<User> playerName = commandContext.<User>getOne("player");
        Optional<String> loaderType = commandContext.<String>getOne("type");
        Optional<String> flagOp = commandContext.<String>getOne("flag");

        HashMap<String, String> args = new HashMap<>();
        boolean onlyExpired = false;
        boolean onlyActive = false;

        if (!loaderType.isPresent()) {
            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunksDeleteUsage, args));
            return CommandResult.empty();
        }

        String flags = "";
        if (flagOp.isPresent()){
            if (flagOp.get().equalsIgnoreCase("expired")) {
                onlyExpired = true;
                flags = "expired ";
            }
            if (flagOp.get().equalsIgnoreCase("active")) {
                onlyActive = true;
                flags = "active ";
            }
        }

        args.put("type", flags + loaderType.get());

        if (playerName.isPresent()) {
            args.put("player", playerName.get().getName());
            if (!sender.hasPermission(Permissions.COMMAND_DELETE + ".others")) {
                sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunksDeleteOthersNoPermission, args));
                return CommandResult.empty();
            }
            Optional<UUID> playerUUID = Utilities.getPlayerUUID(playerName.get().getName());
            if (!playerUUID.isPresent()) {
                sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().noPlayerExists, args));
                return CommandResult.empty();
            }
            if (loaderType.get().equalsIgnoreCase("online") || loaderType.get().equalsIgnoreCase("alwayson")) {
                List<ChunkLoader> clList = plugin.getDataStore().getChunkLoadersByType(playerUUID.get(), loaderType.get().equalsIgnoreCase("alwayson"));

                int success = 0;
                for (ChunkLoader chunkLoader : clList) {
                    if (onlyExpired && chunkLoader.isAlwaysOn()) {
                        if (chunkLoader.isExpired()) {
                            plugin.getChunkManager().unloadChunkLoader(chunkLoader);
                            if (plugin.getDataStore().removeChunkLoader(chunkLoader.getUniqueId())) {
                                success++;
                            }
                        }
                    }
                    if (onlyActive) {
                        if (chunkLoader.isLoadable()) {
                            plugin.getChunkManager().unloadChunkLoader(chunkLoader);
                            if (plugin.getDataStore().removeChunkLoader(chunkLoader.getUniqueId())) {
                                success++;
                            }
                        }
                    }
                    if (!onlyActive && !onlyExpired) {
                        plugin.getChunkManager().unloadChunkLoader(chunkLoader);
                        if (plugin.getDataStore().removeChunkLoader(chunkLoader.getUniqueId())) {
                            success++;
                        }
                    }
                }
                if (success > 0) {
                    sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunksDeleteOthersSuccess, args));
                    return CommandResult.success();
                } else {
                    sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunksDeleteOthersFailure, args));
                    return CommandResult.success();
                }
            } else {
                sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunksDeleteInvalidType, args));
                return CommandResult.empty();
            }
        } else {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (loaderType.get().equalsIgnoreCase("online") || loaderType.get().equalsIgnoreCase("alwayson")) {
                    List<ChunkLoader> clList = plugin.getDataStore().getChunkLoadersByType(player.getUniqueId(), loaderType.get().equalsIgnoreCase("alwayson"));

                    int success = 0;
                    for (ChunkLoader chunkLoader : clList) {
                        plugin.getChunkManager().unloadChunkLoader(chunkLoader);
                        if (plugin.getDataStore().removeChunkLoader(chunkLoader.getUniqueId())) {
                            success++;
                        }
                    }
                    if (success > 0) {
                        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunksDeleteOwnSuccess, args));
                        return CommandResult.success();
                    } else {
                        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunksDeleteOwnFailure, args));
                        return CommandResult.empty();
                    }
                } else {
                    sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunksDeleteInvalidType, args));
                    return CommandResult.empty();
                }
            } else {
                sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunksDeleteConsoleError, args));
                return CommandResult.empty();
            }
        }
    }
}
