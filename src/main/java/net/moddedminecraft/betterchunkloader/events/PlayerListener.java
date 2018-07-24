package net.moddedminecraft.betterchunkloader.events;

import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import net.moddedminecraft.betterchunkloader.Utilities;
import net.moddedminecraft.betterchunkloader.data.ChunkLoader;
import net.moddedminecraft.betterchunkloader.data.PlayerData;
import net.moddedminecraft.betterchunkloader.menu.Menu;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.io.IOException;
import java.util.*;
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

        if (!plugin.playersData.containsKey(player.getUniqueId())) {
            plugin.addPlayerData(new PlayerData(
                    player.getName(),
                    player.getUniqueId(),
                    System.currentTimeMillis(),
                    0,
                    0));
            try {
                plugin.saveData();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Sponge.getScheduler().createTaskBuilder().execute(new Runnable() {
                public void run() {
                    final List<PlayerData> playerData = new ArrayList<PlayerData>(plugin.getPlayerData());
                    for (PlayerData pData : playerData) {
                        if (pData.getUnqiueId().equals(player.getUniqueId()) && !pData.getName().equals(player.getName())) {
                            pData.setName(player.getName());
                            try {
                                plugin.saveData();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }).delay(15, TimeUnit.SECONDS).name("betterchunkloader-s-checkUserNameOnLogin").submit(this.plugin);
        }

        plugin.dataManager.getPlayerDataFor(player.getUniqueId()).ifPresent((playerData) -> {
            playerData.setLastOnline(System.currentTimeMillis());

            try {
                plugin.saveData();
            } catch (IOException | ObjectMappingException e) {
                e.printStackTrace();
            }
        });

        final List<ChunkLoader> clList = plugin.dataManager.getChunkLoadersByOwner(player.getUniqueId());
        for (ChunkLoader chunkLoader : clList) {
            if (chunkLoader.isLoadable()) {
                plugin.getChunkManager().loadChunkLoader(chunkLoader);
            }
        }

        plugin.getLogger().info("Loaded all online chunkloaders for Player: " + player.getName());
    }

    @Listener
    public void onPlayerQuit(ClientConnectionEvent.Disconnect event, @Root Player player) {
        plugin.dataManager.getPlayerDataFor(player.getUniqueId()).ifPresent((playerData) -> {
            playerData.setLastOnline(System.currentTimeMillis());

            try {
                plugin.saveData();
            } catch (IOException | ObjectMappingException e) {
                e.printStackTrace();
            }
        });

        final List<ChunkLoader> clList = plugin.dataManager.getChunkLoadersByOwner(player.getUniqueId());
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

        if (!clickedBlock.getState().getType().equals(ChunkLoader.ONLINE_TYPE) && !clickedBlock.getState().getType().equals(ChunkLoader.ALWAYSON_TYPE)) {
            return;
        }

        Optional<ChunkLoader> chunkLoader = plugin.dataManager.getChunkLoaderAt(clickedBlock.getLocation().get());
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
                        clickedBlock.getState().getType().equals(ChunkLoader.ALWAYSON_TYPE)
                ));
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
                    Optional<PlayerData> playerData = plugin.dataManager.getPlayerDataFor(chunkLoader.get().getOwner());
                    if (playerData.isPresent()) {
                        args.put("ownerName", playerData.get().getName());
                        args.put("ownerUUID", playerData.get().getUnqiueId().toString());
                    }
                    args.put("location", Utilities.getReadableLocation(chunkLoader.get().getWorld(), chunkLoader.get().getLocation()));
                    args.put("chunks", String.valueOf(chunkLoader.get().getChunks()));
                    args.put("type", (chunkLoader.get().isAlwaysOn() ? "Always On" : "Online"));
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
