package net.moddedminecraft.betterchunkloader.menu;

import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import net.moddedminecraft.betterchunkloader.Utilities;
import net.moddedminecraft.betterchunkloader.data.ChunkLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.scheduler.Task;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
        Container targetContainer = event.getTargetInventory();
        Inventory targetInventory = targetContainer.first();

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

            plugin.getDataStore().getPlayerDataFor(player.getUniqueId()).ifPresent((playerData) -> {

                Optional<SlotPos> slotPos = getSlotPos(event.getCursorTransaction());
                Optional<Integer> radius = getRadius(event.getCursorTransaction());
                Optional<Integer> chunks = getChunks(event.getCursorTransaction());

                if (!slotPos.isPresent()) {
                    player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().invalidOption));
                    return;
                }

                int available = plugin.getDataStore().getAvailableChunks(playerData.getUnqiueId(), chunkLoader.isAlwaysOn());

                switch (slotPos.get().getX()) {
                    case 0: {
                        plugin.getChunkManager().unloadChunkLoader(chunkLoader);
                        if (!plugin.getDataStore().removeChunkLoader(chunkLoader.getUniqueId())) {
                            player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().removeFailure));
                            return;
                        } else {
                            new Menu(plugin).showMenu(player, new ChunkLoader(
                                    chunkLoader.getUniqueId(),
                                    chunkLoader.getWorld(),
                                    player.getUniqueId(),
                                    chunkLoader.getLocation(),
                                    chunkLoader.getChunk(),
                                    -1,
                                    System.currentTimeMillis(),
                                    chunkLoader.isAlwaysOn(),
                                    plugin.getConfig().getCore().server)
                            );
                            player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().removeSuccess));
                            return;
                        }
                    }
                    default: {

                        HashMap<String, String> args = new HashMap<>();
                        args.put("playerName", player.getName());
                        args.put("playerUUID", player.getUniqueId().toString());
                        args.put("ownerName", playerData.getName());
                        args.put("ownerUUID", playerData.getUnqiueId().toString());
                        args.put("chunks", String.valueOf(chunkLoader.getChunks()));
                        args.put("available", String.valueOf(available));

                        if (plugin.getConfig().getCore().server.isEmpty()) {
                            player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().serverNameError));
                            break;
                        }

                        if (chunks.get() > (available + chunkLoader.getChunks()) - 1) {
                            args.put("needed", String.valueOf(chunks.get()));
                            player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().notEnough, args));
                            break;
                        } else {
                            int oldRadius = chunkLoader.getRadius();
                            plugin.getChunkManager().unloadChunkLoader(chunkLoader);

                            chunkLoader.setRadius(radius.get());
                            chunkLoader.setCreation(System.currentTimeMillis());
                            if (plugin.getDataStore().getChunkLoaderExist(chunkLoader.getUniqueId())) {
                                if (!plugin.getDataStore().updateChunkLoaderData(chunkLoader)) {
                                    plugin.getLogger().info(player.getName() + " failed to edit " + playerData.getName() + "'s chunk loader at " + Utilities.getReadableLocation(chunkLoader.getWorld(), chunkLoader.getLocation()) + " radius from " + oldRadius + " to " + radius.get());
                                    player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().updateSuccess, args));
                                    break;
                                } else {
                                    plugin.getChunkManager().loadChunkLoader(chunkLoader);
                                    plugin.getLogger().info(player.getName() + " edited " + playerData.getName() + "'s chunk loader at " + Utilities.getReadableLocation(chunkLoader.getWorld(), chunkLoader.getLocation()) + " radius from " + oldRadius + " to " + radius.get());
                                    player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().updateSuccess, args));
                                    updateMenu(player, chunkLoader, targetInventory);
                                    break;
                                }
                            } else {
                                if (!plugin.getDataStore().addChunkLoaderData(chunkLoader)) {
                                    plugin.getLogger().info(player.getName() + " failed to create new chunk loader at " + Utilities.getReadableLocation(chunkLoader.getWorld(), chunkLoader.getLocation()) + " with radius " + chunkLoader.getRadius());
                                    player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().createFailure));
                                    break;
                                } else {
                                    plugin.getChunkManager().loadChunkLoader(chunkLoader);
                                    plugin.getLogger().info(player.getName() + " made a new chunk loader at " + Utilities.getReadableLocation(chunkLoader.getWorld(), chunkLoader.getLocation()) + " with radius " + chunkLoader.getRadius());
                                    player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().createSuccess));
                                    updateMenu(player, chunkLoader, targetInventory);
                                    break;
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    private static void updateMenu(final Player player, final ChunkLoader chunkLoader, Inventory inventory) {
        Task.builder().execute(new MenuUpdateTask(player, chunkLoader, inventory))
                .delay(10, TimeUnit.MILLISECONDS)
                .name("updating menu inventory.").submit(BetterChunkLoader.getInstance());
    }

    // All of this should be replaced when Sponge implements it's custom inventory data API.
    public Optional<SlotPos> getSlotPos(Transaction<ItemStackSnapshot> transaction) {
        if (transaction.isValid()) {
            try { //SlotPos: X,Y
                ItemStack stack = transaction.getFinal().createStack();
                DataContainer cont = stack.toContainer();
                DataQuery q1 = DataQuery.of("UnsafeData", "SLOTPOS1");
                DataQuery q2 = DataQuery.of("UnsafeData", "SLOTPOS2");
                if (cont.get(q1).isPresent() && cont.get(q2).isPresent()) {
                    int one = Integer.parseInt(cont.get(q2).get().toString());
                    int two = Integer.parseInt(cont.get(q2).get().toString());
                    return Optional.of(new SlotPos(one, two));
                }
                return Optional.empty();
            } catch (NumberFormatException ex) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public Optional<Integer> getRadius(Transaction<ItemStackSnapshot> transaction) {
        if (transaction.isValid()) {
            try { //Radius: V
                ItemStack stack = transaction.getFinal().createStack();
                DataContainer cont = stack.toContainer();
                DataQuery q1 = DataQuery.of("UnsafeData", "RADIUS");
                if (cont.get(q1).isPresent()) {
                    return Optional.of(Integer.parseInt(cont.get(q1).get().toString()));
                }
                return Optional.empty();
            } catch (NumberFormatException ex) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public Optional<Integer> getChunks(Transaction<ItemStackSnapshot> transaction) {
        if (transaction.isValid()) {
            try { //Chunks: V
                ItemStack stack = transaction.getFinal().createStack();
                DataContainer cont = stack.toContainer();
                DataQuery q1 = DataQuery.of("UnsafeData", "CHUNKS");
                if (cont.get(q1).isPresent()) {
                    return Optional.of(Integer.parseInt(cont.get(q1).get().toString()));
                }
                return Optional.empty();
            } catch (NumberFormatException ex) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

}
