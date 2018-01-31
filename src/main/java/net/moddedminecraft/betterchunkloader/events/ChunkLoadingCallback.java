package net.moddedminecraft.betterchunkloader.events;

import com.google.common.collect.ImmutableList;
import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import org.spongepowered.api.world.ChunkTicketManager;
import org.spongepowered.api.world.World;

public class ChunkLoadingCallback implements ChunkTicketManager.Callback {

    private final BetterChunkLoader plugin;

    public ChunkLoadingCallback(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onLoaded(ImmutableList<ChunkTicketManager.LoadingTicket> tickets, World world) {

    }
}
