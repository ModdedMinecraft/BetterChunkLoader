package net.moddedminecraft.betterchunkloader.data;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.reflect.TypeToken;
import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.*;
import java.util.Optional;

public class VectorSerializer {

    private static BetterChunkLoader plugin ;

    public VectorSerializer(BetterChunkLoader plugin) {
        VectorSerializer.plugin = plugin;
    }

    public Optional<String> serialize(Vector3i vector) {
        try {
            StringWriter sink = new StringWriter();
            GsonConfigurationLoader loader = GsonConfigurationLoader.builder().setSink(() -> new BufferedWriter(sink)).build();
            ConfigurationNode node = loader.createEmptyNode();
            node.setValue(TypeToken.of(Vector3i.class), vector);
            loader.save(node);
            return Optional.of(sink.toString());
        } catch (IOException | ObjectMappingException ex) {
            plugin.getLogger().error("Error serializing Vector3i", ex);
            return Optional.empty();
        }
    }

    public Optional<Vector3i> deserialize(String vectorStr) {
        try {
            StringReader source = new StringReader(vectorStr);
            GsonConfigurationLoader loader = GsonConfigurationLoader.builder().setSource(() -> new BufferedReader(source)).build();
            ConfigurationNode node = loader.load();
            return Optional.of(node.getValue(TypeToken.of(Vector3i.class)));
        } catch (IOException | ObjectMappingException ex) {
            plugin.getLogger().error("Error deserializing Vector3i", ex);
            return Optional.empty();
        }
    }
}
