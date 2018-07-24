package net.moddedminecraft.betterchunkloader.commands;

import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import net.moddedminecraft.betterchunkloader.Utilities;
import net.moddedminecraft.betterchunkloader.data.ChunkLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

import java.io.IOException;
import java.util.ArrayList;
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
    public CommandResult execute(CommandSource sender, CommandContext commandContext) throws CommandException {
        List<ChunkLoader> chunkLoaders = new ArrayList<>(plugin.getChunkLoaderData());
        int count = 0;
        count = chunkLoaders.stream().filter((chunkLoader) -> (!chunkLoader.blockCheck())).map((chunkLoader) -> {
            plugin.chunkLoaderData.remove(chunkLoader.getUniqueId());
            return chunkLoader;
        }).map((_item) -> 1).reduce(count, Integer::sum);
        if (count > 0) {
            try {
                plugin.saveData();
                sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunksPurgeSuccess));
                return CommandResult.success();
            } catch (IOException | ObjectMappingException e) {
                e.printStackTrace();
                sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunksPurgeFailure));
                return CommandResult.empty();
            }
        } else {
            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunksPurgeFailure));
            return CommandResult.empty();
        }
    }
}
