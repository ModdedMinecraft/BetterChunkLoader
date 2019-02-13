package net.moddedminecraft.betterchunkloader.menu;

import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import net.moddedminecraft.betterchunkloader.data.ChunkLoader;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.scheduler.Task;

import java.util.function.Consumer;

public class MenuUpdateTask implements Consumer<Task> {

        private Player player;
        private ChunkLoader chunkLoader;
        private Inventory inventory;

        public MenuUpdateTask(Player player, ChunkLoader chunkLoader, Inventory inventory) {
            this.player = player;
            this.chunkLoader = chunkLoader;
            this.inventory = inventory;
        }

        @Override
        public void accept(Task task) {
            new Menu(BetterChunkLoader.getInstance()).updateMenu(player, chunkLoader, inventory);
        }
}
