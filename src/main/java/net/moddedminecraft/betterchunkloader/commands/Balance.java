package net.moddedminecraft.betterchunkloader.commands;

import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import net.moddedminecraft.betterchunkloader.Permissions;
import net.moddedminecraft.betterchunkloader.Utilities;
import net.moddedminecraft.betterchunkloader.data.PlayerData;
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
    public CommandResult execute(CommandSource sender, CommandContext commandContext) {
        Optional<User> playerName = commandContext.<User>getOne("player");

        if (playerName.isPresent()) {
            if (sender.hasPermission(Permissions.COMMAND_BALANCE + ".others")) {
                if (chunksInfo(sender, playerName.get(),true)) {
                    return CommandResult.success();
                } else {
                    sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().noPlayerExists));
                    return CommandResult.empty();
                }
            } else {
                sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().balanceNoPermission));
                return CommandResult.empty();
            }
        } else {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                chunksInfo(sender, player, false);
                return CommandResult.success();
            }
        }
        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().balanceFailure));
        return CommandResult.empty();
    }

    private boolean chunksInfo(CommandSource sender, User player, boolean other) {
        Optional<PlayerData> playerData = plugin.getDataStore().getPlayerDataFor(player.getUniqueId());
        if (playerData.isPresent()) {
            int defaultOnline = plugin.getConfig().getCore().defaultOnline;
            int defaultAlwayson = plugin.getConfig().getCore().defaultAlwaysOn;
            int onlineUsed = plugin.getDataStore().getUsedChunks(playerData.get().getUnqiueId(), false);
            int alwaysonUsed = plugin.getDataStore().getUsedChunks(playerData.get().getUnqiueId(), true);

            HashMap<String, String> args = new HashMap<>();
            args.put("player", playerData.get().getName());
            args.put("online", String.valueOf(playerData.get().getOnlineChunks() + defaultOnline));
            args.put("onlineused", String.valueOf(onlineUsed));
            args.put("alwayson", String.valueOf(playerData.get().getAlwaysOnChunks() + defaultAlwayson));
            args.put("alwaysonused", String.valueOf(alwaysonUsed));

            String title = plugin.getConfig().getMessages().balanceTitleSelf;
            if (other) title = plugin.getConfig().getMessages().balanceTitleOther;

            plugin.getPaginationService().builder()
                    .contents(Utilities.parseMessageList(plugin.getConfig().getMessages().balanceItems, args))
                    .title(Utilities.parseMessage(title, args))
                    .padding(Utilities.parseMessage(plugin.getConfig().getMessages().balancePadding))
                    .sendTo(sender);
            return true;
        }
        return false;
    }
}
