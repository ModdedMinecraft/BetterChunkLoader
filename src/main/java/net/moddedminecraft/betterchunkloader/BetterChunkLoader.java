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
import net.moddedminecraft.betterchunkloader.events.PlayerListener;
import net.moddedminecraft.betterchunkloader.events.WorldListener;
import net.moddedminecraft.betterchunkloader.menu.MenuListener;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.service.user.UserStorageService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Plugin(
        id = "betterchunkloader",
        name = "BetterChunkLoader",
        description = "Fork of ShadowKittens Sponge port of KaiKikuchi's BetterChunkLoader plugin.",
        authors = {
                "leelawd93"
        },
        version = "1.2.2"
)
public class BetterChunkLoader {

    private static BetterChunkLoader plugin;

    private Config config;
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

    @Inject
    public GuiceObjectMapperFactory factory;

    public Map<UUID, PlayerData> playersData;
    public Map<UUID, ChunkLoader> chunkLoaderData;

    public DataManager dataManager;
    public VectorSerializer serializer;

    @Listener
    public void Init(GameInitializationEvent event) throws IOException, ObjectMappingException {
        plugin = this;
        config = new Config(this);
        dataManager = new DataManager(this);
        serializer = new VectorSerializer(this);

        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(ChunkLoader.class), new ChunkLoaderSerializer(plugin));
        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(PlayerData.class), new PlayerDataSerializer());
    }

    @Listener
    public void onServerAboutStart(GameAboutToStartServerEvent event) throws IOException, ObjectMappingException {
        if (config.loadCore() && config.loadMessages()) {

            loadData();

            getLogger().info("Loaded " + getChunkLoaderData().size() + " chunkloaders.");
            getLogger().info("Loaded " + getPlayerData().size() + " players.");

            getLogger().info("Registering Listeners...");

            chunkManager = new ChunkManager(this);

            new PlayerListener(this).register();
            new WorldListener(this).register();
            new MenuListener(this).register();
            new CommandManager(this).register();

            getLogger().info("Load complete.");
        }
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        int count = 0;
        count = getChunkLoaderData().stream().filter((chunkLoader) -> (chunkLoader.isLoadable())).map((chunkLoader) -> {
            getChunkManager().loadChunkLoader(chunkLoader);
            return chunkLoader;
        }).map((_item) -> 1).reduce(count, Integer::sum);
        getLogger().info("Activated " + count + " chunk loaders.");
    }

    @Listener
    public void onServerStopping(GameStoppingServerEvent event) throws IOException, ObjectMappingException {
        saveData();
        getChunkLoaderData().stream().forEachOrdered((cl) -> {
            getChunkManager().unloadChunkLoader(cl);
        });
    }

    public void loadChunks() {
        final List<ChunkLoader> chunks = new ArrayList<ChunkLoader>(plugin.getChunkLoaderData());
        for (ChunkLoader chunk : chunks) {
            if (chunk.isLoadable()) {
                plugin.getChunkManager().loadChunkLoader(chunk);
            }
        }
    }

    public void unloadChunks() {
        final List<ChunkLoader> chunks = new ArrayList<ChunkLoader>(plugin.getChunkLoaderData());
        for (ChunkLoader chunk : chunks) {
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

    public Game getGame() {
        return game;
    }

    public void addPlayerData(PlayerData pData) {
        this.playersData.put(pData.getUnqiueId(), pData);
    }

    public Collection<PlayerData> getPlayerData() {
        return Collections.unmodifiableCollection(this.playersData.values());
    }

    public Collection<ChunkLoader> getChunkLoaderData() {
        return Collections.unmodifiableCollection(this.chunkLoaderData.values());
    }

    public PaginationService getPaginationService() {
        return game.getServiceManager().provide(PaginationService.class).get();
    }

    private HoconConfigurationLoader getChunkLoaderDataLoader() {
        return HoconConfigurationLoader.builder().setPath(this.Configdir.resolve("ChunkLoaderData.conf")).build();
    }

    private HoconConfigurationLoader getPlayerDataLoader() {
        return HoconConfigurationLoader.builder().setPath(this.Configdir.resolve("PlayerData.conf")).build();
    }

    public int getAvailableChunks(UUID uuid, Boolean alwayson) {
        Player player = getPlayerFromUUID(uuid);
        if (player.hasPermission(Permissions.UNLLIMITED_CHUNKS)) {
            return 999;
        } else {
            final List<ChunkLoader> chunks = new ArrayList<ChunkLoader>(getChunkLoaderData());
            final PlayerData playerData = dataManager.getPlayerDataFor(uuid).get();
            int alwaysonavailable = getConfig().getCore().chunkLoader.alwaysOn.defaultAlwaysOn + playerData.getAlwaysOnChunks();
            int onlineavailable = getConfig().getCore().chunkLoader.online.defaultOnline + playerData.getOnlineChunks();

            for (ChunkLoader chunk : chunks) {
                if (chunk.getOwner().equals(playerData.getUnqiueId())) {
                    if (chunk.isAlwaysOn() && alwayson) {
                        alwaysonavailable = alwaysonavailable - chunk.getChunks();
                    }
                    if (!chunk.isAlwaysOn() && !alwayson) {
                        onlineavailable = onlineavailable - chunk.getChunks();
                    }
                }
            }

            if (alwayson) {
                return alwaysonavailable;
            } else {
                return onlineavailable;
            }
        }
    }

    public int getUsedChunks(UUID uuid, Boolean alwayson) {
        final List<ChunkLoader> chunks = new ArrayList<ChunkLoader>(getChunkLoaderData());
        final PlayerData playerData = dataManager.getPlayerDataFor(uuid).get();

        int used = 0;

        for (ChunkLoader chunk : chunks) {
            if (chunk.getOwner().equals(playerData.getUnqiueId())) {
                if (chunk.isAlwaysOn() && alwayson) {
                    used = used + chunk.getChunks();
                } else if (!chunk.isAlwaysOn() && !alwayson) {
                    used = used + chunk.getChunks();
                }
            }
        }
        return used;
    }

    private Player getPlayerFromUUID(UUID uuid) {
        Optional<Player> onlinePlayer = Sponge.getServer().getPlayer(uuid);
        if (onlinePlayer.isPresent()) {
            return Sponge.getServer().getPlayer(uuid).get().getPlayer().get();
        }
        Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
        return userStorage.get().get(uuid).get().getPlayer().get();
    }

    synchronized public void loadData() throws IOException, ObjectMappingException {
        HoconConfigurationLoader loader = getChunkLoaderDataLoader();
        ConfigurationNode rootNode = loader.load();

        List<ChunkLoader> chunkList = rootNode.getNode("ChunkLoaders").getList(TypeToken.of(ChunkLoader.class));
        chunkLoaderData = new HashMap<UUID, ChunkLoader>();
        for (ChunkLoader chunk : chunkList) {
            plugin.chunkLoaderData.put(chunk.getUniqueId(), chunk);
        }

        HoconConfigurationLoader playerloader = getPlayerDataLoader();
        ConfigurationNode playerrootNode = playerloader.load();

        List<PlayerData> playersDataList = playerrootNode.getNode("PlayersData").getList(TypeToken.of(PlayerData.class));
        playersData = new HashMap<UUID, PlayerData>();
        for (PlayerData pd : playersDataList) {
            plugin.playersData.put(pd.getUnqiueId(), pd);
        }
    }

    synchronized public void saveData() throws IOException, ObjectMappingException {
        HoconConfigurationLoader loader = getChunkLoaderDataLoader();
        ConfigurationNode rootNode = loader.load();

        rootNode.getNode("ChunkLoaders").setValue(ChunkLoaderSerializer.token, new ArrayList<ChunkLoader>(chunkLoaderData.values()));
        loader.save(rootNode);

        HoconConfigurationLoader playerloader = getPlayerDataLoader();
        ConfigurationNode playerrootNode = playerloader.load();

        playerrootNode.getNode("PlayersData").setValue(PlayerDataSerializer.token, new ArrayList<PlayerData>(playersData.values()));
        playerloader.save(playerrootNode);

    }
}
