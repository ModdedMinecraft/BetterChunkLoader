package net.moddedminecraft.betterchunkloader.database;

import com.flowpowered.math.vector.Vector3i;
import com.zaxxer.hikari.HikariDataSource;
import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import net.moddedminecraft.betterchunkloader.Permissions;
import net.moddedminecraft.betterchunkloader.data.ChunkLoader;
import net.moddedminecraft.betterchunkloader.data.PlayerData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class H2DataStore implements IDataStore {

    private final BetterChunkLoader plugin;
    private final Optional<HikariDataSource> dataSource;

    public H2DataStore(BetterChunkLoader plugin) {
        this.plugin = plugin;
        this.dataSource = getDataSource();
    }

    @Override
    public String getDatabaseName() {
        return "H2";
    }

    @Override
    public boolean load() {
        if (!dataSource.isPresent()) {
            plugin.getLogger().error("Selected datastore: 'H2' is not avaiable please select another datastore.");
            return false;
        }
        try (Connection connection = getConnection()) {
            connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS " + plugin.getConfig().getCore().h2Prefix + "chunkloaders ("
                    + "  uuid VARCHAR(36) NOT NULL PRIMARY KEY,"
                    + "  world VARCHAR(36) NOT NULL,"
                    + "  owner VARCHAR(36) NOT NULL,"
                    + "  location VARCHAR(1000) NOT NULL,"
                    + "  chunk VARCHAR(1000) NOT NULL,"
                    + "  r TINYINT(3) UNSIGNED NOT NULL,"
                    + "  creation BIGINT(20) NOT NULL,"
                    + "  alwaysOn BOOLEAN NOT NULL,"
                    + "  server VARCHAR(36) NOT NULL"
                    + ");");

            connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS " + plugin.getConfig().getCore().h2Prefix + "playerdata ("
                    + "username VARCHAR(16) NOT NULL,"
                    + "uuid VARCHAR(36) NOT NULL PRIMARY KEY, "
                    + "lastOnline BIGINT(20) NOT NULL, "
                    + "onlineAmount SMALLINT(6) UNSIGNED NOT NULL, "
                    + "alwaysOnAmount SMALLINT(6) UNSIGNED NOT NULL"
                    + ");");

            getConnection().commit();
        } catch (SQLException ex) {
            plugin.getLogger().error("Unable to create tables", ex);
            return false;
        }
        return true;
    }

    @Override
    public List<ChunkLoader> getChunkLoaderData() {
        List<ChunkLoader> cList = new ArrayList<>();

        try (Connection connection = getConnection()) {
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM " + plugin.getConfig().getCore().h2Prefix + "chunkloaders");
            while (rs.next()) {
                Optional<Vector3i> locationVector = plugin.serializer.deserialize(rs.getString("location"));
                Optional<Vector3i> chunkVector = plugin.serializer.deserialize(rs.getString("chunk"));
                if (locationVector.isPresent() && chunkVector.isPresent()) {
                    ChunkLoader chunkLoader = new ChunkLoader(
                            UUID.fromString(rs.getString("uuid")),
                            UUID.fromString(rs.getString("world")),
                            UUID.fromString(rs.getString("owner")),
                            locationVector.get(),
                            chunkVector.get(),
                            rs.getInt("r"),
                            rs.getLong("creation"),
                            rs.getBoolean("alwaysOn"),
                            rs.getString("server")
                    );
                    cList.add(chunkLoader);
                }
            }
            return cList;
        } catch (SQLException ex) {
            plugin.getLogger().info("H2: Couldn't read chunkloaders from H2 database.", ex);
            return new ArrayList<>();
        }
    }

    @Override
    public List<ChunkLoader> getChunkLoaders(World world) {
        List<ChunkLoader> clList = new ArrayList<>();
        try (Connection connection = getConnection()) {
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM " + plugin.getConfig().getCore().h2Prefix + "chunkloaders WHERE world = '" + world.getUniqueId().toString() + "' AND server = '" + plugin.getConfig().getCore().server + "'");
            while (rs.next()) {
                Optional<Vector3i> optLocation = plugin.serializer.deserialize(rs.getString("location"));
                Optional<Vector3i> optVector = plugin.serializer.deserialize(rs.getString("chunk"));
                if (optLocation.isPresent() && optVector.isPresent()) {
                    ChunkLoader chunkLoader = new ChunkLoader(
                            UUID.fromString(rs.getString("uuid")),
                            UUID.fromString(rs.getString("world")),
                            UUID.fromString(rs.getString("owner")),
                            optLocation.get(),
                            optVector.get(),
                            rs.getInt("r"),
                            rs.getLong("creation"),
                            rs.getBoolean("alwaysOn"),
                            rs.getString("server")
                    );
                    clList.add(chunkLoader);
                }
            }
            return clList;
        } catch (SQLException ex) {
            plugin.getLogger().info("H2: Couldn't read chunk loaders data from H2 database.", ex);
            return new ArrayList<>();
        }
    }

    public boolean removeChunkLoader(UUID uuid) {
        try (Connection connection = getConnection()) {
            return connection.createStatement().executeUpdate("DELETE FROM " + plugin.getConfig().getCore().h2Prefix + "chunkloaders WHERE uuid = '" + uuid + "' LIMIT 1") > 0;
        } catch (SQLException ex) {
            plugin.getLogger().error("H2: Error removing ChunkLoader.", ex);
        }
        return false;
    }

    @Override
    public boolean getChunkLoaderExist(UUID uuid) {
        try (Connection connection = getConnection()) {
        ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM " + plugin.getConfig().getCore().h2Prefix + "chunkloaders WHERE uuid = '" + uuid + "' LIMIT 1");
            return rs.next();
        } catch (SQLException ex) {
            plugin.getLogger().info("H2: Couldn't read chunk loaders data from H2 database.", ex);
            return false;
        }
    }

    @Override
    public List<ChunkLoader> getChunkLoadersByType(UUID owner, Boolean isAlwaysOn) {
        List<ChunkLoader> clList = new ArrayList<>();
        try (Connection connection = getConnection()) {
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM " + plugin.getConfig().getCore().h2Prefix + "chunkloaders WHERE owner = '" + owner.toString() + "' AND alwaysOn = '" + isAlwaysOn + "'");
            while (rs.next()) {
                Optional<Vector3i> optLocation = plugin.serializer.deserialize(rs.getString("location"));
                Optional<Vector3i> optVector = plugin.serializer.deserialize(rs.getString("chunk"));
                if (optLocation.isPresent() && optVector.isPresent()) {
                    ChunkLoader chunkLoader = new ChunkLoader(
                            UUID.fromString(rs.getString("uuid")),
                            UUID.fromString(rs.getString("world")),
                            UUID.fromString(rs.getString("owner")),
                            optLocation.get(),
                            optVector.get(),
                            rs.getInt("r"),
                            rs.getLong("creation"),
                            rs.getBoolean("alwaysOn"),
                            rs.getString("server")
                    );
                    clList.add(chunkLoader);
                }
            }
            return clList;
        } catch (SQLException ex) {
            plugin.getLogger().info("H2: Couldn't read chunk loaders data from H2 database.", ex);
            return new ArrayList<>();
        }
    }

    @Override
    public List<ChunkLoader> getChunkLoadersByType(Boolean isAlwaysOn) {
        List<ChunkLoader> clList = new ArrayList<>();
        try (Connection connection = getConnection()) {
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM " + plugin.getConfig().getCore().h2Prefix + "chunkloaders WHERE alwaysOn = '" + isAlwaysOn + "'");
            while (rs.next()) {
                Optional<Vector3i> optLocation = plugin.serializer.deserialize(rs.getString("location"));
                Optional<Vector3i> optVector = plugin.serializer.deserialize(rs.getString("chunk"));
                if (optLocation.isPresent() && optVector.isPresent()) {
                    ChunkLoader chunkLoader = new ChunkLoader(
                            UUID.fromString(rs.getString("uuid")),
                            UUID.fromString(rs.getString("world")),
                            UUID.fromString(rs.getString("owner")),
                            optLocation.get(),
                            optVector.get(),
                            rs.getInt("r"),
                            rs.getLong("creation"),
                            rs.getBoolean("alwaysOn"),
                            rs.getString("server")
                    );
                    clList.add(chunkLoader);
                }
            }
            return clList;
        } catch (SQLException ex) {
            plugin.getLogger().info("H2: Couldn't read chunk loaders data from H2 database.", ex);
            return new ArrayList<>();
        }
    }

    @Override
    public List<ChunkLoader> getChunkLoadersByOwner(UUID ownerUUID) {
        List<ChunkLoader> clList = new ArrayList<>();
        try (Connection connection = getConnection()) {
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM " + plugin.getConfig().getCore().h2Prefix + "chunkloaders WHERE owner = '" + ownerUUID.toString() + "'");
            while (rs.next()) {
                Optional<Vector3i> optLocation = plugin.serializer.deserialize(rs.getString("location"));
                Optional<Vector3i> optVector = plugin.serializer.deserialize(rs.getString("chunk"));
                if (optLocation.isPresent() && optVector.isPresent()) {
                    ChunkLoader chunkLoader = new ChunkLoader(
                            UUID.fromString(rs.getString("uuid")),
                            UUID.fromString(rs.getString("world")),
                            UUID.fromString(rs.getString("owner")),
                            optLocation.get(),
                            optVector.get(),
                            rs.getInt("r"),
                            rs.getLong("creation"),
                            rs.getBoolean("alwaysOn"),
                            rs.getString("server")
                    );
                    clList.add(chunkLoader);
                }
            }
            return clList;
        }  catch (SQLException ex) {
            plugin.getLogger().info("H2: Couldn't read player data from H2 database.", ex);
            return new ArrayList<>();
        }
    }

    @Override
    public List<ChunkLoader> getChunkLoadersAt(World world, Vector3i chunk) {
        List<ChunkLoader> chunkloaders = new ArrayList<>();
        getChunkLoaders(world).stream().filter((chunkLoader) -> (chunkLoader.getChunk().equals(chunk))).forEachOrdered((chunkLoader) -> {
            chunkloaders.add(chunkLoader);
        });
        return chunkloaders;
    }

    @Override
    public Optional<ChunkLoader> getChunkLoaderAt(Location<World> location) {
        List<ChunkLoader> chunkloaders = getChunkLoaders(location.getExtent());
        if (chunkloaders == null || chunkloaders.isEmpty()) {
            return Optional.empty();
        }
        for (ChunkLoader chunkLoader : chunkloaders) {
            if (chunkLoader.getLocation().equals(location.getBlockPosition())) {
                return Optional.of(chunkLoader);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<PlayerData> getPlayerData() {
        List<PlayerData> playerList = new ArrayList<>();

        try (Connection connection = getConnection()) {
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM " + plugin.getConfig().getCore().h2Prefix + "playerdata");
            while (rs.next()) {
                PlayerData playerData = new PlayerData(
                        rs.getString("username"),
                        UUID.fromString(rs.getString("uuid")),
                        rs.getLong("lastonline"),
                        rs.getInt("onlineamount"),
                        rs.getInt("alwaysonamount")
                );
                playerList.add(playerData);
            }
            return playerList;
        } catch (SQLException ex) {
            plugin.getLogger().info("H2: Couldn't read playerdata from H2 database.", ex);
            return new ArrayList<>();
        }
    }

    @Override
    public Optional<PlayerData> getPlayerDataFor(UUID uuid) {
        try (Connection connection = getConnection()) {
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM " + plugin.getConfig().getCore().h2Prefix + "playerdata WHERE uuid = '" + uuid.toString() + "'");
            PlayerData playerData = null;
            while (rs.next()) {
                playerData = new PlayerData(
                        rs.getString("username"),
                        UUID.fromString(rs.getString("uuid")),
                        rs.getLong("lastonline"),
                        rs.getInt("onlineamount"),
                        rs.getInt("alwaysonamount")
                );
            }
            if (playerData == null) {
                plugin.getLogger().info("H2: Couldn't read player data from H2 database.");
                return Optional.empty();
            } else {
                return Optional.of(playerData);
            }
        }  catch (SQLException ex) {
            plugin.getLogger().info("H2: Couldn't read player data from H2 database.", ex);
            return Optional.empty();
        }
    }

    @Override
    public boolean addPlayerData(PlayerData playerData) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO " + plugin.getConfig().getCore().h2Prefix + "playerdata VALUES (?, ?, ?, ?, ?);");
            statement.setString(1, playerData.getName());
            statement.setString(2, playerData.getUnqiueId().toString());
            statement.setLong(3, playerData.getLastOnline());
            statement.setInt(4, playerData.getOnlineChunks());
            statement.setInt(5, playerData.getAlwaysOnChunks());
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            plugin.getLogger().error("H2: Error adding ticketdata", ex);
        }
        return false;
    }

    @Override
    public boolean addChunkLoaderData(ChunkLoader chunkloader) {
        try (Connection connection = getConnection()) {
            Optional<String> locationStr = plugin.serializer.serialize(chunkloader.getLocation());
            Optional<String> vectorStr = plugin.serializer.serialize(chunkloader.getChunk());
            if (locationStr.isPresent() && vectorStr.isPresent()) {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO " + plugin.getConfig().getCore().h2Prefix + "chunkloaders VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");
                statement.setString(1, chunkloader.getUniqueId().toString());
                statement.setString(2, chunkloader.getWorld().toString());
                statement.setString(3, chunkloader.getOwner().toString());
                statement.setObject(4, locationStr.get());
                statement.setObject(5, vectorStr.get());
                statement.setInt(6, chunkloader.getRadius());
                statement.setLong(7, chunkloader.getCreation());
                statement.setBoolean(8, chunkloader.isAlwaysOn());
                statement.setString(9, chunkloader.getServer());
                return statement.executeUpdate() > 0;
            }
        } catch (SQLException ex) {
            plugin.getLogger().error("H2: Error adding playerdata", ex);
        }
        return false;
    }

    @Override
    public boolean updatePlayerData(PlayerData playerData) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("MERGE INTO " + plugin.getConfig().getCore().h2Prefix + "playerdata VALUES (?, ?, ?, ?, ?);");
            statement.setString(1, playerData.getName());
            statement.setString(2, playerData.getUnqiueId().toString());
            statement.setLong(3, playerData.getLastOnline());
            statement.setInt(4, playerData.getOnlineChunks());
            statement.setInt(5, playerData.getAlwaysOnChunks());
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            plugin.getLogger().error("H2: Error updating ticketdata", ex);
        }
        return false;
    }

    @Override
    public boolean updateChunkLoaderData(ChunkLoader chunkloader) {
        try (Connection connection = getConnection()) {
            Optional<String> locationStr = plugin.serializer.serialize(chunkloader.getLocation());
            Optional<String> vectorStr = plugin.serializer.serialize(chunkloader.getChunk());
            if (locationStr.isPresent() && vectorStr.isPresent()) {
                PreparedStatement statement = connection.prepareStatement("MERGE INTO " + plugin.getConfig().getCore().h2Prefix + "chunkloaders VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");
                statement.setString(1, chunkloader.getUniqueId().toString());
                statement.setString(2, chunkloader.getWorld().toString());
                statement.setString(3, chunkloader.getOwner().toString());
                statement.setObject(4, locationStr.get());
                statement.setObject(5, vectorStr.get());
                statement.setInt(6, chunkloader.getRadius());
                statement.setLong(7, chunkloader.getCreation());
                statement.setBoolean(8, chunkloader.isAlwaysOn());
                statement.setString(9, chunkloader.getServer());
                return statement.executeUpdate() > 0;
            }
        } catch (SQLException ex) {
            plugin.getLogger().error("H2: Error updating playerdata", ex);
        }
        return false;
    }

    @Override
    public int getAvailableChunks(UUID uuid, Boolean alwayson) {
        Player player = getPlayerFromUUID(uuid);
        if (player.hasPermission(Permissions.UNLLIMITED_CHUNKS)) {
            return 999;
        } else {
            final PlayerData playerData = plugin.getDataStore().getPlayerDataFor(uuid).get();
            int alwaysonavailable = plugin.getConfig().getCore().defaultAlwaysOn + playerData.getAlwaysOnChunks();
            int onlineavailable = plugin.getConfig().getCore().defaultOnline + playerData.getOnlineChunks();

            for (ChunkLoader chunk : plugin.getDataStore().getChunkLoaderData()) {
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

    @Override
    public int getUsedChunks(UUID uuid, Boolean alwayson) {
        final PlayerData playerData = plugin.getDataStore().getPlayerDataFor(uuid).get();

        int used = 0;

        for (ChunkLoader chunk : getChunkLoaderData()) {
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

    public boolean hasColumn(String tableName, String columnName) {
        try (Connection connection = getConnection()) {
            DatabaseMetaData md = connection.getMetaData();
            ResultSet rs = md.getColumns(null, null, tableName, columnName);
            return rs.next();
        } catch (SQLException ex) {
            plugin.getLogger().error("H2: Error checking if column exists.", ex);
        }
        return false;
    }

    public Optional<HikariDataSource> getDataSource() {
        try {
            HikariDataSource ds = new HikariDataSource();
            ds.setDriverClassName("org.h2.Driver");
            ds.setJdbcUrl("jdbc:h2://" + new File(plugin.configDir, plugin.getConfig().getCore().databaseFile).getAbsolutePath());
            ds.setConnectionTimeout(1000);
            ds.setLoginTimeout(5);
            ds.setAutoCommit(true);
            return Optional.ofNullable(ds);
        } catch (SQLException ex) {
            plugin.getLogger().error("H2: Failed to get datastore.", ex);
            return Optional.empty();
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.get().getConnection();
    }

}
