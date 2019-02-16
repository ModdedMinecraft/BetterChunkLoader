package net.moddedminecraft.betterchunkloader.database.transfer;

import com.zaxxer.hikari.HikariDataSource;
import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import net.moddedminecraft.betterchunkloader.data.PlayerData;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class TransferMYSQLDataStore implements TransferIDataStore {

    private final BetterChunkLoader plugin;
    private final Optional<HikariDataSource> dataSource;

    public TransferMYSQLDataStore(BetterChunkLoader plugin) {
        this.plugin = plugin;
        this.dataSource = getDataSource();
    }

    @Override
    public String getDatabaseName() {
        return "MySQL";
    }

    @Override
    public boolean load() {
        if (!dataSource.isPresent()) {
            plugin.getLogger().error("Selected datastore: 'MySQL' is not avaiable please select another datastore.");
            return false;
        }
        try (Connection connection = getConnection()) {
            return true;
        } catch (SQLException ex) {
            plugin.getLogger().error("Unable to create tables", ex);
            return false;
        }
    }

    @Override
    public List<PlayerData> getPlayerData() {
        List<PlayerData> playerList = new ArrayList<>();

        try (Connection connection = getConnection()) {
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM " + "bcl_playersdata");
            while (rs.next()) {
                PlayerData playerData = new PlayerData(
                        "blank",
                        UUID.fromString(rs.getString("pid")),
                        System.currentTimeMillis(),
                        rs.getInt("onlineonly"),
                        rs.getInt("alwayson")
                );
                playerList.add(playerData);
            }
            return playerList;
        } catch (SQLException ex) {
            plugin.getLogger().info("MySQL: Couldn't read playerdata from MySQL database.", ex);
            return new ArrayList<>();
        }
    }

    public Optional<HikariDataSource> getDataSource() {
        try {
            HikariDataSource ds = new HikariDataSource();
            ds.setDriverClassName("org.mariadb.jdbc.Driver");
            ds.setJdbcUrl("jdbc:mariadb://"
                    + plugin.getConfig().getCore().transferMysqlHost
                    + ":" + "3306"
                    + "/" +  plugin.getConfig().getCore().transferMysqlDatabase);
            ds.addDataSourceProperty("user", plugin.getConfig().getCore().transferMysqlUser);
            ds.addDataSourceProperty("password", plugin.getConfig().getCore().transferMysqlPass);
            ds.setConnectionTimeout(1000);
            ds.setLoginTimeout(5);
            ds.setAutoCommit(true);
            return Optional.ofNullable(ds);
        } catch (SQLException ex) {
            plugin.getLogger().error("MySQL: Failed to get datastore.", ex);
            return Optional.empty();
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.get().getConnection();
    }
}