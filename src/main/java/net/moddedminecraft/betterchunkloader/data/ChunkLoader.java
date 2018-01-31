package net.moddedminecraft.betterchunkloader.data;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.reflect.TypeToken;
import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ChunkLoader extends ChunkLoaderUtil {

    public ChunkLoader(UUID uuid, UUID world, UUID owner, Vector3i location, Vector3i chunk, Integer radius, Long creation, Boolean isAlwaysOn) {
        super(uuid, world, owner, location, chunk, radius, creation, isAlwaysOn);
    }

    public static class ChunkLoaderSerializer extends VectorSerializer implements TypeSerializer<ChunkLoader> {
        @SuppressWarnings("serial")
        static final public TypeToken<List<ChunkLoader>> token = new TypeToken<List<ChunkLoader>>() {};

        BetterChunkLoader plugin;

        public ChunkLoaderSerializer(BetterChunkLoader plugin) {
            super(plugin);
            this.plugin = plugin;
        }

        @Override
        public ChunkLoader deserialize(TypeToken<?> token, ConfigurationNode node) throws ObjectMappingException {
            Optional<Vector3i> locationVector = plugin.serializer.deserialize(node.getNode("location").getString());
            Optional<Vector3i> chunkVector = plugin.serializer.deserialize(node.getNode("chunk").getString());

            if (locationVector.isPresent() && chunkVector.isPresent()) {
                return new ChunkLoader(
                        node.getNode("uuid").getValue(TypeToken.of(UUID.class)),
                        node.getNode("world").getValue(TypeToken.of(UUID.class)),
                        node.getNode("owner").getValue(TypeToken.of(UUID.class)),
                        locationVector.get(),
                        chunkVector.get(),
                        node.getNode("radius").getInt(),
                        node.getNode("creation").getLong(),
                        node.getNode("isalwayson").getBoolean());
            } else {
                return new ChunkLoader(
                        node.getNode("uuid").getValue(TypeToken.of(UUID.class)),
                        node.getNode("world").getValue(TypeToken.of(UUID.class)),
                        node.getNode("owner").getValue(TypeToken.of(UUID.class)),
                        new Vector3i(0,0,0),
                        new Vector3i(0,0,0),
                        node.getNode("radius").getInt(),
                        node.getNode("creation").getLong(),
                        node.getNode("isalwayson").getBoolean());
            }
        }

        @Override
        public void serialize(TypeToken<?> token, ChunkLoader chunkLoader, ConfigurationNode node) throws ObjectMappingException {
            node.getNode("uuid").setValue(new TypeToken<UUID>() {}, chunkLoader.uuid);
            node.getNode("world").setValue(new TypeToken<UUID>() {}, chunkLoader.world);
            node.getNode("owner").setValue(new TypeToken<UUID>() {}, chunkLoader.owner);
            node.getNode("location").setValue(new TypeToken<Vector3i>() {}, chunkLoader.location);
            node.getNode("chunk").setValue(new TypeToken<Vector3i>() {},chunkLoader.chunk);
            node.getNode("radius").setValue(chunkLoader.radius);
            node.getNode("creation").setValue(chunkLoader.creation);
            node.getNode("isalwayson").setValue(chunkLoader.isAlwaysOn);
        }
    }
}
