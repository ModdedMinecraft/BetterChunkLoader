package net.moddedminecraft.betterchunkloader;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import net.moddedminecraft.betterchunkloader.commands.CommandManager;
import net.moddedminecraft.betterchunkloader.config.Config;
import net.moddedminecraft.betterchunkloader.data.ChunkLoader;
import net.moddedminecraft.betterchunkloader.data.ChunkLoader.ChunkLoaderSerializer;
import net.moddedminecraft.betterchunkloader.data.PlayerData;
import net.moddedminecraft.betterchunkloader.data.PlayerData.PlayerDataSerializer;
import net.moddedminecraft.betterchunkloader.data.VectorSerializer;
import net.moddedminecraft.betterchunkloader.database.DataStoreManager;
import net.moddedminecraft.betterchunkloader.database.IDataStore;
import net.moddedminecraft.betterchunkloader.events.PlayerListener;
import net.moddedminecraft.betterchunkloader.events.WorldListener;
import net.moddedminecraft.betterchunkloader.menu.MenuListener;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.pagination.PaginationService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "betterchunkloader",
        name = "BetterChunkLoader",
        description = "Fork of ShadowKittens Sponge port of KaiKikuchi's BetterChunkLoader plugin.",
        authors = {
                "leelawd93"
        },
        version = "2.0.2"
)
public class BetterChunkLoader {

    private static BetterChunkLoader plugin;

    private Config config;
    private DataStoreManager dataStoreManager;
    private ChunkManager chunkManager;

    @Inject
    private Logger logger;

    @Inject
    private Game game;

    @Inject
    public PluginContainer pluginContainer;

    @Inject
    @ConfigDir(sharedRoot = false)
    public File configDir;

    @Inject
    @ConfigDir(sharedRoot = false)
    public Path Configdir;

    public VectorSerializer serializer;


    @Listener
    public void Init(GameInitializationEvent event) throws IOException, ObjectMappingException {
        plugin = this;
        config = new Config(this);
        serializer = new VectorSerializer(this);

        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(ChunkLoader.class), new ChunkLoaderSerializer(plugin));
        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(PlayerData.class), new PlayerDataSerializer());
    }

    @Listener
    public void onServerAboutStart(GameAboutToStartServerEvent event) throws IOException, ObjectMappingException {
        if (config.loadCore() && config.loadMessages()) {
            dataStoreManager = new DataStoreManager(this);
            if (dataStoreManager.load()) {
                getLogger().info("Registering Listeners...");

                chunkManager = new ChunkManager(this);

                new PlayerListener(this).register();
                new WorldListener(this).register();
                new MenuListener(this).register();
                new CommandManager(this).register();

                getLogger().info("Load complete.");
            } else {
                getLogger().error("Unable to load a datastore please check your Console/Config!");
            }
        }
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        Sponge.getScheduler().createTaskBuilder().delay(config.getCore().loadDelay, TimeUnit.SECONDS).execute(() -> {
            int count = 0;
            count = getDataStore().getChunkLoaderData().stream()
                    .filter((chunkLoader) -> (chunkLoader.getServer().equalsIgnoreCase(plugin.getConfig().getCore().server)))
                    .filter((chunkLoader) -> (chunkLoader.isLoadable()))
                    .map((chunkLoader) -> {
                        getChunkManager().loadChunkLoader(chunkLoader);
                        return chunkLoader;
                    }).map((_item) -> 1).reduce(count, Integer::sum);
            getLogger().info("Loaded " + getDataStore().getChunkLoaderData().size() + " chunkloaders.");
            getLogger().info("Loaded " + getDataStore().getPlayerData().size() + " players.");
            getLogger().info("Activated " + count + " chunk loaders.");
        }).submit(this);
    }

    @Listener
    public void onServerStopping(GameStoppingServerEvent event) throws IOException, ObjectMappingException {
        //saveData();
        getDataStore().getChunkLoaderData().stream().forEachOrdered((cl) -> {
            getChunkManager().unloadChunkLoader(cl);
        });
    }

    public void loadChunks() {
        for (ChunkLoader chunk : plugin.getDataStore().getChunkLoaderData()) {
            if (chunk.isLoadable()) {
                plugin.getChunkManager().loadChunkLoader(chunk);
            }
        }
    }

    public void unloadChunks() {
        for (ChunkLoader chunk : plugin.getDataStore().getChunkLoaderData()) {
            if (chunk.isLoadable()) {
                plugin.getChunkManager().unloadChunkLoader(chunk);
            }
        }
    }

    public static BetterChunkLoader getInstance() {
        return plugin;
    }

    public Config getConfig() {
        return config;
    }

    public ChunkManager getChunkManager() {
        return chunkManager;
    }

    public Logger getLogger() {
        return logger;
    }

    public PaginationService getPaginationService() {
        return game.getServiceManager().provide(PaginationService.class).get();
    }

    public IDataStore getDataStore() {
        return dataStoreManager.getDataStore();
    }
}
