package net.moddedminecraft.betterchunkloader.events;

import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import net.moddedminecraft.betterchunkloader.Utilities;
import net.moddedminecraft.betterchunkloader.data.ChunkLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class WorldListener {

    private final BetterChunkLoader plugin;

    public WorldListener(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    public void register() {
        Sponge.getEventManager().registerListeners(plugin, this);
    }

    @Listener
    public void onWorldLoad(LoadWorldEvent event) {
        final List<ChunkLoader> chunks = new ArrayList<ChunkLoader>(plugin.getDataStore().getChunkLoaders(event.getTargetWorld()));
        for (ChunkLoader chunk : chunks) {
            if (chunk.isLoadable()) {
                plugin.getChunkManager().loadChunkLoader(chunk);
            }
        }
    }

    /*@Listener
    public void onWorldUnLoad(UnloadWorldEvent event) {
        final List<ChunkLoader> chunks = new ArrayList<ChunkLoader>(plugin.getDataStore().getChunkLoaders(event.getTargetWorld()));
        for (ChunkLoader chunk : chunks) {
            if (chunk.isLoadable()) {
                plugin.getChunkManager().unloadChunkLoader(chunk);
            }
        }
    }*/

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event) {
        BlockSnapshot block = event.getTransactions().get(0).getOriginal();

        if (block == null) {
            return;
        }

        if (!block.getState().getType().equals(ChunkLoader.ONLINE_TYPE) && !block.getState().getType().equals(ChunkLoader.ALWAYSON_TYPE)) {
            return;
        }

        plugin.getDataStore().getChunkLoaderAt(block.getLocation().get()).ifPresent((chunkLoader) -> {

            Player player = event.getCause().last(Player.class).get();
            plugin.getDataStore().getPlayerDataFor(chunkLoader.getOwner()).ifPresent((playerData) -> {

                HashMap<String, String> args = new HashMap<>();
                args.put("player", player.getName());
                args.put("playerUUID", player.getUniqueId().toString());
                args.put("ownerName", playerData.getName());
                args.put("owner", playerData.getUnqiueId().toString());
                args.put("type", chunkLoader.isAlwaysOn() ? "Always On" : "Online Only");
                args.put("location", Utilities.getReadableLocation(chunkLoader.getWorld(), chunkLoader.getLocation()));
                args.put("chunks", String.valueOf(chunkLoader.getChunks()));

                plugin.getChunkManager().unloadChunkLoader(chunkLoader);
                plugin.getDataStore().removeChunkLoader(chunkLoader.getUniqueId());

                player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().removeSuccess, args));
                Optional<Player> owner = Sponge.getServer().getPlayer(chunkLoader.getOwner());
                if (owner.isPresent() && player != owner.get()) {
                    player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().ownerNotify, args));
                }
                plugin.getLogger().info(player.getName() + " broke " + owner.get().getName() + "'s chunk loader at " + Utilities.getReadableLocation(chunkLoader.getWorld(), chunkLoader.getLocation()));

            });
        });
    }
}
