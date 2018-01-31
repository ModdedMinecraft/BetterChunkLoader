package net.moddedminecraft.betterchunkloader;

import net.moddedminecraft.betterchunkloader.data.ChunkLoader;
import net.moddedminecraft.betterchunkloader.data.PlayerData;
import net.moddedminecraft.betterchunkloader.data.VectorSerializer;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DataManager {

    private BetterChunkLoader plugin;

    private final VectorSerializer serializer;

    public DataManager(BetterChunkLoader plugin) {
        this.plugin = plugin;
        this.serializer = new VectorSerializer(plugin);
    }

    public List<ChunkLoader> getChunkLoadersByOwner(UUID ownerUUID) {
        List<ChunkLoader> clList = new ArrayList<>();
        final List<ChunkLoader> chunks = new ArrayList<ChunkLoader>(plugin.getChunkLoaderData());
        for (ChunkLoader chunk : chunks) {
            if (chunk.getOwner().equals(ownerUUID)) {
                clList.add(chunk);
            }
        }
        return clList;
    }

    public List<ChunkLoader> getChunkLoaders(World world) {
        final List<ChunkLoader> chunks = new ArrayList<ChunkLoader>(plugin.getChunkLoaderData());
        List<ChunkLoader> clList = new ArrayList<ChunkLoader>();
        for (ChunkLoader chunk : chunks) {
            if (chunk.getWorld().equals(world.getUniqueId())) {
                clList.add(chunk);
            }
        }
        return clList;
    }

    public Optional<ChunkLoader> getChunkLoaderAt(Location<World> location) {
        List<ChunkLoader> chunks = getChunkLoaders(location.getExtent());

        if (chunks == null || chunks.isEmpty()) {
            return Optional.empty();
        }
        for (ChunkLoader chunk : chunks) {
            if (chunk.getLocation().equals(location.getBlockPosition())) {
                return Optional.of(chunk);
            }
        }
        return Optional.empty();
    }

    public List<ChunkLoader> getChunkLoadersByType(Boolean isAlwaysOn) {
        final List<ChunkLoader> chunks = new ArrayList<ChunkLoader>(plugin.getChunkLoaderData());
        List<ChunkLoader> clList = new ArrayList<>();

        for (ChunkLoader chunk : chunks) {
            if (isAlwaysOn && chunk.isAlwaysOn() || !isAlwaysOn && !chunk.isAlwaysOn()) {
                clList.add(chunk);
            }
        }
        return clList;
    }

    public Optional<PlayerData> getPlayerDataFor(UUID uuid) {
        return Optional.of(plugin.playersData.get(uuid));
    }

    public ChunkLoader getChunkloaderDataFor(UUID uuid) {
        return plugin.chunkLoaderData.get(uuid);
    }

}
