package net.moddedminecraft.betterchunkloader.menu;

import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import net.moddedminecraft.betterchunkloader.Utilities;
import net.moddedminecraft.betterchunkloader.data.ChunkLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.data.Transaction;
import net.moddedminecraft.betterchunkloader.Permissions;
import net.moddedminecraft.betterchunkloader.data.ChunkLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class Menu {

    private final BetterChunkLoader plugin;

    private static final ItemType REMOVE_TYPE = Sponge.getRegistry().getType(ItemType.class, BetterChunkLoader.getInstance().getConfig().getCore().menu.removeItemType).orElse(ItemTypes.REDSTONE_TORCH);
    private static final ItemType ACTIVE_TYPE = Sponge.getRegistry().getType(ItemType.class, BetterChunkLoader.getInstance().getConfig().getCore().menu.activeItemType).orElse(ItemTypes.POTION);
    private static final ItemType INACTIVE_TYPE = Sponge.getRegistry().getType(ItemType.class, BetterChunkLoader.getInstance().getConfig().getCore().menu.inactiveItemType).orElse(ItemTypes.GLASS_BOTTLE);

    public Menu(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    public void showMenu(Player player, ChunkLoader chunkLoader) {
        plugin.dataManager.getPlayerDataFor(chunkLoader.getOwner()).ifPresent((playerData) -> {
            String title = (chunkLoader.getRadius() != -1 ? "BCL: " + playerData.getName() + " Chunks: " + chunkLoader.getChunks() + " " : chunkLoader.isAlwaysOn() ? "Always On Chunk Loader" : "Online Only ChunkLoader");
            if (title.length() > 32) {
                title = title.substring(0, 32);
            }
            Inventory inventory = Inventory.builder()
                    .of(InventoryArchetypes.MENU_ROW)
                    .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(title)))
                    .property(MenuProperty.PROPERTY_NAME, MenuProperty.of(chunkLoader))
                    .listener(ClickInventoryEvent.class, createMenuListener(chunkLoader))
                    .build(plugin);
            if (chunkLoader.getRadius() != -1) {
                SlotPos slotPos = SlotPos.of(0, 0);
                HashMap<Key, Object> keys = new HashMap<>();
                List<Text> lores = new ArrayList<>();
                lores.add(Text.of("SlotPos: " + slotPos.getX() + "," + slotPos.getY()));
                keys.put(Keys.ITEM_LORE, lores);
                keys.put(Keys.DISPLAY_NAME, Text.of("Remove"));
                addMenuOption(inventory, slotPos, REMOVE_TYPE, keys);
            }

            int pos = 2;
            int maxRadius = 7;

            if (!player.hasPermission(Permissions.UNLLIMITED_CHUNKS)) maxRadius = plugin.getConfig().getCore().menu.maxSize;

            if (maxRadius < 0) maxRadius = 0;
            if (maxRadius > 7) maxRadius = 7;

            for (int radius = 0; radius < maxRadius;) {
                Integer chunks = Double.valueOf(Math.pow((2 * radius) + 1, 2)).intValue();
                SlotPos slotPos = SlotPos.of(pos, 0);
                HashMap<Key, Object> keys = new HashMap<>();
                List<Text> lores = new ArrayList<>();
                lores.add(Text.of("SlotPos: " + slotPos.getX() + "," + slotPos.getY()));
                lores.add(Text.of("Radius: " + radius));
                lores.add(Text.of("Chunks: " + chunks));
                keys.put(Keys.ITEM_LORE, lores);
                keys.put(Keys.DISPLAY_NAME, Text.of((chunkLoader.getRadius() == radius ? "Size: " + (radius + 1) + " [Active]" : "Size: " + (radius + 1))));
                addMenuOption(inventory, slotPos, (chunkLoader.getRadius() == radius ? ACTIVE_TYPE : INACTIVE_TYPE), keys);
                pos++;
                radius++;
            }
            player.openInventory(inventory, Cause.of(NamedCause.simulated(player)));
        });
    }

    public Consumer<ClickInventoryEvent> createMenuListener(ChunkLoader chunkLoader) {
        return event -> {
            if (chunkLoader == null) {
                return;
            }
            if (event.getCause().last(Player.class).isPresent()) {
                Player player = event.getCause().last(Player.class).get();

                event.setCancelled(true);

                if (!chunkLoader.canCreate(player)) {
                    player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunkLoader.noPermissionCreate));
                }

                if (!chunkLoader.canEdit(player)) {
                    player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunkLoader.noPermissionEdit));
                    return;
                }

                plugin.dataManager.getPlayerDataFor(player.getUniqueId()).ifPresent((playerData) -> {

                    Optional<SlotPos> slotPos = getSlotPos(event.getCursorTransaction());
                    Optional<Integer> radius = getRadius(event.getCursorTransaction());
                    Optional<Integer> chunks = getChunks(event.getCursorTransaction());

                    if (!slotPos.isPresent()) {
                        player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunkLoader.invalidOption));
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
                                player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunkLoader.removeFailure));
                                e.printStackTrace();
                                break;
                            }

                            player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunkLoader.removeSuccess));
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


                            if (chunks.get() > available + chunkLoader.getChunks()) {
                                args.put("needed", String.valueOf(chunks.get()));
                                player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunkLoader.notEnough, args));
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
                                        player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunkLoader.createFailure));
                                    } else {
                                        plugin.getLogger().info(player.getName() + " failed to edit " + playerData.getName() + "'s chunk loader at " + Utilities.getReadableLocation(chunkLoader.getWorld(), chunkLoader.getLocation()) + " radius from " + oldRadius + " to " + radius.get());
                                        player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunkLoader.updateSuccess, args));
                                    }

                                    e.printStackTrace();
                                    break;
                                }

                                plugin.getChunkManager().loadChunkLoader(chunkLoader);
                                if (oldRadius < 0) {
                                    plugin.getLogger().info(player.getName() + " made a new chunk loader at " + Utilities.getReadableLocation(chunkLoader.getWorld(), chunkLoader.getLocation()) + " with radius " + chunkLoader.getRadius());
                                    player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunkLoader.createSuccess));
                                } else {
                                    plugin.getLogger().info(player.getName() + " edited " + playerData.getName() + "'s chunk loader at " + Utilities.getReadableLocation(chunkLoader.getWorld(), chunkLoader.getLocation()) + " radius from " + oldRadius + " to " + radius.get());
                                    player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunkLoader.updateSuccess, args));
                                }
                                break;
                            }
                        }
                    }
                });
            }
        };
    }

    public void addMenuOption(Inventory inventory, SlotPos slotPos, ItemType icon, HashMap<Key, Object> keys) {
        ItemStack itemStack = ItemStack.builder().itemType(icon).quantity(1).build();
        keys.entrySet().forEach((entry) -> {
            itemStack.offer(entry.getKey(), entry.getValue());
        });
        inventory.query(slotPos).first().set(itemStack);
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


