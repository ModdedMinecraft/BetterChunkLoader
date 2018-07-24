package net.moddedminecraft.betterchunkloader.events;

import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import net.moddedminecraft.betterchunkloader.Utilities;
import net.moddedminecraft.betterchunkloader.data.ChunkLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;

import java.io.IOException;
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
        final List<ChunkLoader> chunks = new ArrayList<ChunkLoader>(plugin.dataManager.getChunkLoaders(event.getTargetWorld()));
        for (ChunkLoader chunk : chunks) {
            if (chunk.isLoadable()) {
                plugin.getChunkManager().loadChunkLoader(chunk);
            }
        }
    }

    @Listener
    public void onWorldUnLoad(UnloadWorldEvent event) {
        final List<ChunkLoader> chunks = new ArrayList<ChunkLoader>(plugin.dataManager.getChunkLoaders(event.getTargetWorld()));
        for (ChunkLoader chunk : chunks) {
            if (chunk.isLoadable()) {
                plugin.getChunkManager().unloadChunkLoader(chunk);
            }
        }
    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event) {
        BlockSnapshot block = event.getTransactions().get(0).getOriginal();

        if (block == null) {
            return;
        }

        if (!block.getState().getType().equals(ChunkLoader.ONLINE_TYPE) && !block.getState().getType().equals(ChunkLoader.ALWAYSON_TYPE)) {
            return;
        }

        final List<ChunkLoader> chunks = new ArrayList<ChunkLoader>(plugin.getChunkLoaderData());

        plugin.dataManager.getChunkLoaderAt(block.getLocation().get()).ifPresent((chunkLoader) -> {

            Player player = event.getCause().last(Player.class).get();
            plugin.dataManager.getPlayerDataFor(chunkLoader.getOwner()).ifPresent((playerData) -> {

                HashMap<String, String> args = new HashMap<>();
                args.put("player", player.getName());
                args.put("playerUUID", player.getUniqueId().toString());
                args.put("ownerName", playerData.getName());
                args.put("owner", playerData.getUnqiueId().toString());
                args.put("type", chunkLoader.isAlwaysOn() ? "Always On" : "Online Only");
                args.put("location", Utilities.getReadableLocation(chunkLoader.getWorld(), chunkLoader.getLocation()));
                args.put("chunks", String.valueOf(chunkLoader.getChunks()));


                plugin.getChunkManager().unloadChunkLoader(chunkLoader);
                plugin.chunkLoaderData.remove(chunkLoader.getUniqueId(), chunkLoader); //TODO Move?

                try {
                    plugin.saveData();
                } catch (IOException | ObjectMappingException e) {
                    player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().removeFailure, args));
                    e.printStackTrace();
                }

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
