package net.moddedminecraft.betterchunkloader.menu;

import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import net.moddedminecraft.betterchunkloader.Utilities;
import net.moddedminecraft.betterchunkloader.data.ChunkLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class MenuListener {
    private final BetterChunkLoader plugin;

    public MenuListener(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    public void register() {
        Sponge.getEventManager().registerListeners(plugin, this);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onMenuInteract(ClickInventoryEvent event) {
        Optional<MenuProperty> optMenuProperty = event.getTargetInventory().getProperty(MenuProperty.class, MenuProperty.PROPERTY_NAME);
        if (!optMenuProperty.isPresent()) {
            return;
        }

        MenuProperty menuProperty = optMenuProperty.get();
        ChunkLoader chunkLoader = menuProperty.getValue();

        if (chunkLoader == null) {
            return;
        }
        if (event.getCause().last(Player.class).isPresent()) {
            Player player = event.getCause().last(Player.class).get();

            event.setCancelled(true);

            if (!chunkLoader.canCreate(player)) {
                player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().createFailure));
            }

            if (!chunkLoader.canEdit(player)) {
                player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().noPermissionEdit));
                return;
            }

            plugin.dataManager.getPlayerDataFor(player.getUniqueId()).ifPresent((playerData) -> {

                Optional<SlotPos> slotPos = getSlotPos(event.getCursorTransaction());
                Optional<Integer> radius = getRadius(event.getCursorTransaction());
                Optional<Integer> chunks = getChunks(event.getCursorTransaction());

                if (!slotPos.isPresent()) {
                    player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().invalidOption));
                    return;
                }

                int available = plugin.getAvailableChunks(playerData.getUnqiueId(), chunkLoader.isAlwaysOn());

                switch (slotPos.get().getX()) {
                    case 0: {
                        plugin.getChunkManager().unloadChunkLoader(chunkLoader);
                        plugin.chunkLoaderData.remove(chunkLoader.getUniqueId()); //TODO Move?

                        try {
                            plugin.saveData();
                        } catch (IOException | ObjectMappingException e) {
                            player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().removeFailure));
                            e.printStackTrace();
                            break;
                        }

                        player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().removeSuccess));
                        return;
                    }
                    default: {

                        HashMap<String, String> args = new HashMap<>();
                        args.put("playerName", player.getName());
                        args.put("playerUUID", player.getUniqueId().toString());
                        args.put("ownerName", playerData.getName());
                        args.put("ownerUUID", playerData.getUnqiueId().toString());
                        args.put("chunks", String.valueOf(chunkLoader.getChunks()));
                        args.put("available", String.valueOf(available));


                        if (chunks.get() > (available + chunkLoader.getChunks()) - 1) {
                            args.put("needed", String.valueOf(chunks.get()));
                            player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().notEnough, args));
                            break;
                        } else {
                            int oldRadius = chunkLoader.getRadius();
                            plugin.getChunkManager().unloadChunkLoader(chunkLoader);

                            chunkLoader.setRadius(radius.get());
                            chunkLoader.setCreation(System.currentTimeMillis());
                            plugin.chunkLoaderData.put(chunkLoader.getUniqueId(), chunkLoader);

                            try {
                                plugin.saveData();
                            } catch (IOException | ObjectMappingException e) {

                                if (chunkLoader.getRadius() < 0) {
                                    plugin.getLogger().info(player.getName() + " failed to create new chunk loader at " + Utilities.getReadableLocation(chunkLoader.getWorld(), chunkLoader.getLocation()) + " with radius " + chunkLoader.getRadius());
                                    player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().createFailure));
                                } else {
                                    plugin.getLogger().info(player.getName() + " failed to edit " + playerData.getName() + "'s chunk loader at " + Utilities.getReadableLocation(chunkLoader.getWorld(), chunkLoader.getLocation()) + " radius from " + oldRadius + " to " + radius.get());
                                    player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().updateSuccess, args));
                                }

                                e.printStackTrace();
                                break;
                            }

                            plugin.getChunkManager().loadChunkLoader(chunkLoader);
                            if (oldRadius < 0) {
                                plugin.getLogger().info(player.getName() + " made a new chunk loader at " + Utilities.getReadableLocation(chunkLoader.getWorld(), chunkLoader.getLocation()) + " with radius " + chunkLoader.getRadius());
                                player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().createSuccess));
                            } else {
                                plugin.getLogger().info(player.getName() + " edited " + playerData.getName() + "'s chunk loader at " + Utilities.getReadableLocation(chunkLoader.getWorld(), chunkLoader.getLocation()) + " radius from " + oldRadius + " to " + radius.get());
                                player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().updateSuccess, args));
                            }
                            break;
                        }
                    }
                }
            });
        }
    }

    // All of this should be replaced when Sponge implements it's custom inventory data API.
    public Optional<SlotPos> getSlotPos(Transaction<ItemStackSnapshot> transaction) {
        if (transaction.isValid()) {
            try { //SlotPos: X,Y
                List<Text> lore = transaction.getFinal().getOrElse(Keys.ITEM_LORE, new ArrayList<>());
                for (Text text : lore) {
                    if (text.toPlain().contains("SlotPos:")) {
                        String[] values = text.toPlain().substring(9).split(",");
                        return Optional.ofNullable(new SlotPos(Integer.parseInt(values[0]), Integer.parseInt(values[1])));
                    }
                }
            } catch (NumberFormatException ex) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public Optional<Integer> getRadius(Transaction<ItemStackSnapshot> transaction) {
        if (transaction.isValid()) {
            try { //Radius: V
                List<Text> lore = transaction.getFinal().getOrElse(Keys.ITEM_LORE, new ArrayList<>());
                for (Text text : lore) {
                    if (text.toPlain().contains("Radius:")) {
                        return Optional.ofNullable(Integer.parseInt(text.toPlain().substring(8)));
                    }
                }
            } catch (NumberFormatException ex) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public Optional<Integer> getChunks(Transaction<ItemStackSnapshot> transaction) {
        if (transaction.isValid()) {
            try { //Chunks: V
                List<Text> lore = transaction.getFinal().getOrElse(Keys.ITEM_LORE, new ArrayList<>());
                for (Text text : lore) {
                    if (text.toPlain().contains("Chunks:")) {
                        return Optional.ofNullable(Integer.parseInt(text.toPlain().substring(8)));
                    }
                }
            } catch (NumberFormatException ex) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

}
