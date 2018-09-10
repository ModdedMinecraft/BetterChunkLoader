package net.moddedminecraft.betterchunkloader.database;

import com.flowpowered.math.vector.Vector3i;
import net.moddedminecraft.betterchunkloader.data.ChunkLoader;
import net.moddedminecraft.betterchunkloader.data.PlayerData;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IDataStore {

    public abstract String getDatabaseName();

    public abstract boolean load();

    public List<ChunkLoader> getChunkLoaderData();

    public List<ChunkLoader> getChunkLoaders(World world);

    public boolean removeChunkLoader(UUID uuid);

    public boolean getChunkLoaderExist(UUID uuid);

    public List<ChunkLoader> getChunkLoadersByType(UUID owner, Boolean isAlwaysOn);

    public List<ChunkLoader> getChunkLoadersByType(Boolean isAlwaysOn);

    public List<ChunkLoader> getChunkLoadersByOwner(UUID ownerUUID);

    public List<ChunkLoader> getChunkLoadersAt(World world, Vector3i chunk);

    public Optional<ChunkLoader> getChunkLoaderAt(Location<World> location);

    public List<PlayerData> getPlayerData();

    public Optional<PlayerData> getPlayerDataFor(UUID uuid);

    public abstract boolean addPlayerData(PlayerData playerData);

    public abstract boolean addChunkLoaderData(ChunkLoader chunkloader);

    public boolean updatePlayerData(PlayerData playerData);

    public boolean updateChunkLoaderData(ChunkLoader chunkloader);

    public int getAvailableChunks(UUID uuid, Boolean alwayson);

    public int getUsedChunks(UUID uuid, Boolean alwayson);
}
