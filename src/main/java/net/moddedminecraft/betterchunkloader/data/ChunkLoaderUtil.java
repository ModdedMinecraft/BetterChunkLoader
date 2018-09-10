package net.moddedminecraft.betterchunkloader.data;

import com.flowpowered.math.vector.Vector3i;
import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import net.moddedminecraft.betterchunkloader.Permissions;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.UUID;

public class ChunkLoaderUtil {

    protected UUID uuid;
    protected UUID world;
    protected UUID owner;

    protected Vector3i location;
    protected Vector3i chunk;
    protected Integer radius;

    protected Long creation;
    protected Boolean isAlwaysOn;

    protected String server;

    public static final BlockType ONLINE_TYPE = Sponge.getRegistry().getType(BlockType.class, BetterChunkLoader.getInstance().getConfig().getCore().onlineBlockType).orElse(BlockTypes.IRON_BLOCK);
    public static final BlockType ALWAYSON_TYPE = Sponge.getRegistry().getType(BlockType.class, BetterChunkLoader.getInstance().getConfig().getCore().alwaysOnBlockType).orElse(BlockTypes.DIAMOND_BLOCK);

    public ChunkLoaderUtil(UUID uuid, UUID world, UUID owner, Vector3i location, Vector3i chunk, Integer radius, Long creation, Boolean isAlwaysOn, String server) {
        this.uuid = uuid;
        this.world = world;
        this.owner = owner;
        this.location = location;
        this.chunk = chunk;
        this.radius = radius;
        this.creation = creation;
        this.isAlwaysOn = isAlwaysOn;
        this.server = server;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public UUID getWorld() {
        return world;
    }

    public UUID getOwner() {
        return owner;
    }

    public Vector3i getLocation() {
        return location;
    }

    public Vector3i getChunk() {
        return chunk;
    }

    public void setChunk(Vector3i chunk) {
        this.chunk = chunk;
    }

    public Integer getRadius() {
        return radius;
    }

    public void setRadius(Integer radius) {
        this.radius = radius;
    }

    public void setCreation(Long creation) {
        this.creation = creation;
    }

    public Long getCreation() {
        return creation;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public Boolean isAlwaysOn() {
        return isAlwaysOn;
    }

    public Boolean isExpired() {
        Optional<PlayerData> playerData = BetterChunkLoader.getInstance().getDataStore().getPlayerDataFor(owner);
        if (playerData.isPresent()) {
            PlayerData pData = playerData.get();
            if (isAlwaysOn()) {
                return System.currentTimeMillis() - pData.getLastOnline() > BetterChunkLoader.getInstance().getConfig().getCore().alwaysOnExpiry * 3600000L;
            }
        }
        return true;
    }

    public boolean isLoadable() {
        Optional<Player> player = Sponge.getServer().getPlayer(owner);
        return (player.isPresent() && player.get().isOnline() || (this.isAlwaysOn && !this.isExpired())) && this.blockCheck();
    }

    public Integer getChunks() {
        return Double.valueOf(Math.pow((2 * radius) + 1, 2)).intValue();
    }

    public Boolean canEdit(Player player) {
        if (player.getUniqueId().equals(owner)) {
            return true;
        } else {
            return player.hasPermission(Permissions.EDIT);
        }
    }

    public Boolean canCreate(Player player) {
        return player.hasPermission(Permissions.CREATE) || player.hasPermission(Permissions.CREATE + "." + (isAlwaysOn() ? "alwayson" : "online"));
    }

    public Boolean blockCheck() {
        Optional<World> _world = Sponge.getServer().getWorld(this.world);
        if (_world.isPresent()) {
            if (location == null) {
                return false;
            }
            BlockState block = _world.get().getBlock(location);
            if (isAlwaysOn) {
                return block.getType().equals(ALWAYSON_TYPE);
            } else {
                return block.getType().equals(ONLINE_TYPE);
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return this.world + ":" + this.location.getX() + "," + this.location.getZ() + (this.isAlwaysOn ? "y" : "n") + " - " + this.getChunks() + " - " + this.location.toString();
    }

    public Boolean contains(Vector3i vector) {
        return location.getX() - radius <= vector.getX() && vector.getX() <= location.getX() + radius && location.getZ() - radius <= vector.getZ() && vector.getZ() <= location.getZ() + radius;
    }

    public Boolean contains(int chunkX, int chunkZ) {
        return location.getX() - radius <= chunkX && chunkX <= location.getX() + radius && location.getZ() - radius <= chunkZ && chunkZ <= location.getZ() + radius;
    }
}
