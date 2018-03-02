package net.moddedminecraft.betterchunkloader.commands;

import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import net.moddedminecraft.betterchunkloader.Permissions;
import net.moddedminecraft.betterchunkloader.Utilities;
import net.moddedminecraft.betterchunkloader.data.PlayerData;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;

import java.util.HashMap;
import java.util.Optional;

public class Balance implements CommandExecutor {

    private final BetterChunkLoader plugin;

    public Balance(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource sender, CommandContext commandContext) throws CommandException {
        Optional<User> playerName = commandContext.<User>getOne("player");

        if (playerName.isPresent()) {
            if (sender.hasPermission(Permissions.COMMAND_BALANCE + ".others")) {
                if (chunksInfo(sender, playerName.get(),true)) {
                    return CommandResult.success();
                } else {
                    sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.noPlayerExists));
                    return CommandResult.empty();
                }
            } else {
                sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.balance.noPermission));
                return CommandResult.empty();
            }
        } else {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                chunksInfo(sender, player, false);
                return CommandResult.success();
            }
        }
        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.balance.failure));
        return CommandResult.empty();
    }

    private boolean chunksInfo(CommandSource sender, User player, boolean other) {
        Optional<PlayerData> playerData = plugin.dataManager.getPlayerDataFor(player.getUniqueId());
        if (playerData.isPresent()) {
            int defaultOnline = plugin.getConfig().getCore().chunkLoader.online.defaultOnline;
            int defaultAlwayson = plugin.getConfig().getCore().chunkLoader.alwaysOn.defaultAlwaysOn;
            int onlineUsed = plugin.getUsedChunks(playerData.get().getUnqiueId(), false);
            int alwaysonUsed = plugin.getUsedChunks(playerData.get().getUnqiueId(), true);

            HashMap<String, String> args = new HashMap<>();
            args.put("player", playerData.get().getName());
            args.put("online", String.valueOf(playerData.get().getOnlineChunks() + defaultOnline));
            args.put("onlineused", String.valueOf(onlineUsed));
            args.put("alwayson", String.valueOf(playerData.get().getAlwaysOnChunks() + defaultAlwayson));
            args.put("alwaysonused", String.valueOf(alwaysonUsed));

            String title = plugin.getConfig().getMessages().commands.balance.success.titleSelf;
            if (other) title = plugin.getConfig().getMessages().commands.balance.success.titleOther;

            plugin.getPaginationService().builder()
                    .contents(Utilities.parseMessageList(plugin.getConfig().getMessages().commands.balance.success.items, args))
                    .title(Utilities.parseMessage(title))
                    .padding(Utilities.parseMessage(plugin.getConfig().getMessages().commands.balance.success.padding))
                    .sendTo(sender);
            return true;
        }
        return false;
    }
}
