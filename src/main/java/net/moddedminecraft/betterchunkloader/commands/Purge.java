package net.moddedminecraft.betterchunkloader.commands;

import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import net.moddedminecraft.betterchunkloader.Utilities;
import net.moddedminecraft.betterchunkloader.data.ChunkLoader;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

import java.util.List;

/**
 * Created by Lee on 28/01/2018.
 */
public class Purge implements CommandExecutor {

    private final BetterChunkLoader plugin;

    public Purge(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource sender, CommandContext commandContext) {
        List<ChunkLoader> chunkLoaders = plugin.getDataStore().getChunkLoaderData();
        int count = 0;
        count = chunkLoaders.stream().filter((chunkLoader) -> (!chunkLoader.blockCheck())).map((chunkLoader) -> {
            plugin.getDataStore().removeChunkLoader(chunkLoader.getUniqueId());
            return chunkLoader;
        }).map((_item) -> 1).reduce(count, Integer::sum);
        if (count > 0) {
            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunksPurgeSuccess));
            return CommandResult.success();
        } else {
            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunksPurgeFailure));
            return CommandResult.empty();
        }
    }
}
