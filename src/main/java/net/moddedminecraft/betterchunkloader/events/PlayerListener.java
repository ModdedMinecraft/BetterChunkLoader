package net.moddedminecraft.betterchunkloader.events;

import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import net.moddedminecraft.betterchunkloader.Utilities;
import net.moddedminecraft.betterchunkloader.data.ChunkLoader;
import net.moddedminecraft.betterchunkloader.data.PlayerData;
import net.moddedminecraft.betterchunkloader.menu.Menu;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerListener {

    private final BetterChunkLoader plugin;

    public PlayerListener(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    public void register() {
        Sponge.getEventManager().registerListeners(plugin, this);
    }

    @Listener
    public void onPlayerLogin(ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();
        Optional<PlayerData> pData = plugin.getDataStore().getPlayerDataFor(player.getUniqueId());
        if (!pData.isPresent()) {
            plugin.getDataStore().addPlayerData(new PlayerData(
                    player.getName(),
                    player.getUniqueId(),
                    System.currentTimeMillis(),
                    0,
                    0));
        } else {
            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                Optional<PlayerData> playerData = plugin.getDataStore().getPlayerDataFor(player.getUniqueId());
                if (playerData.isPresent() && !playerData.get().getName().equals(player.getName())) {
                    playerData.get().setName(player.getName());
                    plugin.getDataStore().updatePlayerData(playerData.get());
                }
            }).delay(15, TimeUnit.SECONDS).name("betterchunkloader-s-checkUserNameOnLogin").submit(this.plugin);
        }

        plugin.getDataStore().getPlayerDataFor(player.getUniqueId()).ifPresent((playerData) -> {
            playerData.setLastOnline(System.currentTimeMillis());
            plugin.getDataStore().updatePlayerData(playerData);
        });

        final List<ChunkLoader> clList = plugin.getDataStore().getChunkLoadersByOwner(player.getUniqueId());
        for (ChunkLoader chunkLoader : clList) {
            if (chunkLoader.isLoadable() && chunkLoader.getServer().equalsIgnoreCase(plugin.getConfig().getCore().server)) {
                plugin.getChunkManager().loadChunkLoader(chunkLoader);
            }
        }

        plugin.getLogger().info("Loaded all online chunkloaders for Player: " + player.getName());
    }

    @Listener
    public void onPlayerQuit(ClientConnectionEvent.Disconnect event, @Root Player player) {
        plugin.getDataStore().getPlayerDataFor(player.getUniqueId()).ifPresent((playerData) -> {
            playerData.setLastOnline(System.currentTimeMillis());
            plugin.getDataStore().updatePlayerData(playerData);
        });

        final List<ChunkLoader> clList = plugin.getDataStore().getChunkLoadersByOwner(player.getUniqueId());
        for (ChunkLoader chunkLoader : clList) {
            if (!chunkLoader.isAlwaysOn() && chunkLoader.isLoadable()) {
                plugin.getChunkManager().unloadChunkLoader(chunkLoader);
            }
        }
        plugin.getLogger().info("Unloaded all online chunkloaders for Player: " + player.getName());
    }

    @Listener
    public void onPlayerInteract(InteractBlockEvent.Secondary.MainHand event, @Root Player player) {
        if (!event.getCause().containsType(Player.class)) {
            return;
        }
        BlockSnapshot clickedBlock = event.getTargetBlock();

        if (clickedBlock == null || player == null) {
            return;
        }

        if (!clickedBlock.getState().getType().equals(ChunkLoader.ONLINE_TYPE)
                && !clickedBlock.getState().getType().equals(ChunkLoader.ALWAYSON_TYPE)
                && !clickedBlock.getState().getType().equals(ChunkLoader.ADMIN_TYPE)) {
            return;
        }

        Optional<ChunkLoader> chunkLoader = plugin.getDataStore().getChunkLoaderAt(clickedBlock.getLocation().get());
        if (player.getItemInHand(HandTypes.MAIN_HAND).isPresent() && player.getItemInHand(HandTypes.MAIN_HAND).get().getType().getId().equalsIgnoreCase(plugin.getConfig().getCore().wandType)) {
            if (!chunkLoader.isPresent()) {
                chunkLoader = Optional.of(new ChunkLoader(
                        UUID.randomUUID(),
                        clickedBlock.getWorldUniqueId(),
                        player.getUniqueId(),
                        clickedBlock.getLocation().get().getBlockPosition(),
                        clickedBlock.getLocation().get().getChunkPosition(),
                        -1,
                        System.currentTimeMillis(),
                        clickedBlock.getState().getType().equals(ChunkLoader.ALWAYSON_TYPE),
                        plugin.getConfig().getCore().server,
                        clickedBlock.getState().getType().equals(ChunkLoader.ADMIN_TYPE)
                ));
            }
            if (clickedBlock.getState().getType().equals(ChunkLoader.ADMIN_TYPE)) {
                if (!chunkLoader.get().canCreateAdmin(player)) {
                    player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().noPermissionCreate));
                    return;
                }
            }
            if (!chunkLoader.get().canCreate(player)) {
                player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().noPermissionCreate));
                return;
            }
            new Menu(plugin).showMenu(player, chunkLoader.get());
        } else {
            if (!player.getItemInHand(HandTypes.MAIN_HAND).isPresent()) {
                HashMap<String, String> args = new HashMap<>();
                args.put("playerName", player.getName());
                args.put("playerUUID", player.getUniqueId().toString());
                if (chunkLoader.isPresent()) {
                    String type = chunkLoader.get().isAlwaysOn() ? "Always On" : "Online Only";
                    if (chunkLoader.get().isAdmin()) type = "Admin";
                    Optional<PlayerData> playerData = plugin.getDataStore().getPlayerDataFor(chunkLoader.get().getOwner());
                    if (playerData.isPresent()) {
                        args.put("ownerName", playerData.get().getName());
                        args.put("ownerUUID", playerData.get().getUnqiueId().toString());
                    }
                    args.put("location", Utilities.getReadableLocation(chunkLoader.get().getWorld(), chunkLoader.get().getLocation()));
                    args.put("chunks", String.valueOf(chunkLoader.get().getChunks()));
                    args.put("type", type);
                    if (chunkLoader.get().canEdit(player)) {
                        plugin.getPaginationService().builder()
                                .contents(Utilities.parseMessageList(plugin.getConfig().getMessages().infoItems, args))
                                .title(Utilities.parseMessage(plugin.getConfig().getMessages().infoTitle))
                                .padding(Utilities.parseMessage(plugin.getConfig().getMessages().infoPadding))
                                .sendTo(player);
                    } else {
                        player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().noPermissionEdit, args));
                    }
                } else {
                    player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().creationHelp, args));
                }
            }
        }
    }

}
