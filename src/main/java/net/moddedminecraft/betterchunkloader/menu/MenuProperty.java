package net.moddedminecraft.betterchunkloader.menu;

import net.moddedminecraft.betterchunkloader.data.ChunkLoader;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.item.inventory.property.AbstractInventoryProperty;

public class MenuProperty  extends AbstractInventoryProperty<String, ChunkLoader> {

    public static final String PROPERTY_NAME = "chunkloader";

    public MenuProperty (ChunkLoader chunkLoader) {
        super(chunkLoader);
    }

    public static MenuProperty of(ChunkLoader chunkLoader) {
        return new MenuProperty(chunkLoader);
    }

    @Override
    public int compareTo(Property<?, ?> o) {
        return 0;
    }
}
