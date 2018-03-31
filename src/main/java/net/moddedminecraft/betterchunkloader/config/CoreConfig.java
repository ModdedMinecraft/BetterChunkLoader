package net.moddedminecraft.betterchunkloader.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class CoreConfig {

    @Setting("Debug")
    public Boolean debug = false;

    @Setting("ChunkLoader")
    public ChunkLoader chunkLoader = new ChunkLoader();

    @Setting("Title")
    public String title = "&8[&6Chunkloader&8]&r";

    @ConfigSerializable
    public static class ChunkLoader {

        @Setting(value = "LoadDelay", comment = "Delay before loading chunkloaders during startup. (Seconds).")
        public Integer loadDelay = 10;

        @Setting("Online")
        public Online online = new Online();

        @Setting("AlwaysOn")
        public AlwaysOn alwaysOn = new AlwaysOn();

        @Setting("WandType")
        public String wandType = "minecraft:blaze_rod";

        @ConfigSerializable
        public static class Online {

            @Setting("DefaultOnline")
            public Integer defaultOnline = 1;

            @Setting("MaxOnline")
            public Integer maxOnline = 100;

            @Setting("BlockType")
            public String blockType = "minecraft:iron_block";
        }

        @ConfigSerializable
        public static class AlwaysOn {

            @Setting("DefaultAlwaysOn")
            public Integer defaultAlwaysOn = 0;

            @Setting("MaxAlwaysOn")
            public Integer maxAlwaysOn = 50;

            @Setting(value = "Expiry", comment = "Max amount in hours the owner can be offline before considering this loader 'Expired'")
            public Integer expiry = 24;

            @Setting("BlockType")
            public String blockType = "minecraft:diamond_block";

        }
    }
}
