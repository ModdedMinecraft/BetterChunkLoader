package net.moddedminecraft.betterchunkloader.menu;

import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import net.moddedminecraft.betterchunkloader.Permissions;
import net.moddedminecraft.betterchunkloader.data.ChunkLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class Menu {

    private final BetterChunkLoader plugin;

    private static final ItemType REMOVE_TYPE = Sponge.getRegistry().getType(ItemType.class, BetterChunkLoader.getInstance().getConfig().getCore().removeItemType).orElse(ItemTypes.REDSTONE_TORCH);
    private static final ItemType ACTIVE_TYPE = Sponge.getRegistry().getType(ItemType.class, BetterChunkLoader.getInstance().getConfig().getCore().activeItemType).orElse(ItemTypes.POTION);
    private static final ItemType INACTIVE_TYPE = Sponge.getRegistry().getType(ItemType.class, BetterChunkLoader.getInstance().getConfig().getCore().inactiveItemType).orElse(ItemTypes.GLASS_BOTTLE);

    public Menu(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    public void showMenu(Player player, ChunkLoader chunkLoader) {
        plugin.getDataStore().getPlayerDataFor(chunkLoader.getOwner()).ifPresent((playerData) -> {
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

            if (!player.hasPermission(Permissions.UNLLIMITED_CHUNKS)) maxRadius = plugin.getConfig().getCore().maxSize;

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
            player.openInventory(inventory);
        });
    }

    public Consumer<ClickInventoryEvent> createMenuListener(ChunkLoader chunkLoader) {
        return event -> {

        };
    }

    public void addMenuOption(Inventory inventory, SlotPos slotPos, ItemType icon, HashMap<Key, Object> keys) {
        ItemStack itemStack = ItemStack.builder().itemType(icon).quantity(1).build();
        keys.entrySet().forEach((entry) -> {
            itemStack.offer(entry.getKey(), entry.getValue());
        });
        inventory.query(slotPos).first().set(itemStack);
    }
}
