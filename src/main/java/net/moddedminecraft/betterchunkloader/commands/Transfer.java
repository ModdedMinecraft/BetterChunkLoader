package net.moddedminecraft.betterchunkloader.commands;

import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import net.moddedminecraft.betterchunkloader.data.PlayerData;
import net.moddedminecraft.betterchunkloader.database.transfer.TransferDataStoreManager;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;

import java.util.List;

/**
 * Created by Lee on 28/01/2018.
 */
public class Transfer implements CommandExecutor {

    private final BetterChunkLoader plugin;

    public Transfer(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource sender, CommandContext commandContext) throws CommandException {
        if (!plugin.getConfig().getCore().transferMysqlHost.equals("host")) {
            TransferDataStoreManager transferDataStoreManager = new TransferDataStoreManager(plugin);
            if (transferDataStoreManager.load()) {
                List<PlayerData> playerData = transferDataStoreManager.getDataStore().getPlayerData();
                int count = 0;
                    for (PlayerData pData : playerData) {
                       if (plugin.getDataStore().getPlayerDataFor(pData.getUnqiueId()).isPresent()) {
                           pData.setName(plugin.getDataStore().getPlayerDataFor(pData.getUnqiueId()).get().getName());
                           plugin.getDataStore().updatePlayerData(pData);
                           count++;
                       } else {
                           plugin.getDataStore().addPlayerData(pData);
                           count++;
                       }
                    }
                    sender.sendMessage(Text.of("Transfered " + count + " accounts"));
                    return CommandResult.success();

            }
            throw new CommandException(Text.of("Could not load dataStore"));
        }
        throw new CommandException(Text.of("There is no host set to transfer from"));
    }
}
