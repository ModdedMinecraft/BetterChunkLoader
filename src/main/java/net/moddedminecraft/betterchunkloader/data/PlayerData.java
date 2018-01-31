package net.moddedminecraft.betterchunkloader.data;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.util.List;
import java.util.UUID;

public class PlayerData extends PlayerDataUtil {

    public PlayerData(String name, UUID uuid, Long lastOnline, Integer onlineChunksAmount, Integer alwaysOnChunksAmount) {
        super(name, uuid, lastOnline, onlineChunksAmount, alwaysOnChunksAmount);
    }

    public static class PlayerDataSerializer implements TypeSerializer<PlayerData> {
        @SuppressWarnings("serial")
        static final public TypeToken<List<PlayerData>> token = new TypeToken<List<PlayerData>>() {};

        @Override
        public PlayerData deserialize(TypeToken<?> token, ConfigurationNode node) throws ObjectMappingException {
            return new PlayerData(
                    node.getNode("username").getString(),
                    node.getNode("uuid").getValue(TypeToken.of(UUID.class)),
                    node.getNode("lastonline").getLong(),
                    node.getNode("onlineamount").getInt(),
                    node.getNode("alwaysonamount").getInt());
        }

        @Override
        public void serialize(TypeToken<?> token, PlayerData playerData, ConfigurationNode node) throws ObjectMappingException {
            node.getNode("username").setValue(playerData.name);
            node.getNode("uuid").setValue(new TypeToken<UUID>() {}, playerData.uuid);
            node.getNode("lastonline").setValue(playerData.lastOnline);
            node.getNode("onlineamount").setValue(playerData.onlineChunksAmount);
            node.getNode("alwaysonamount").setValue(playerData.alwaysOnChunksAmount);
        }
    }
}
