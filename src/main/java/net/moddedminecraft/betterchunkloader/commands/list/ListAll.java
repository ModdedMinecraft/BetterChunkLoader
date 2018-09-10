package net.moddedminecraft.betterchunkloader.commands.list;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import net.moddedminecraft.betterchunkloader.Permissions;
import net.moddedminecraft.betterchunkloader.Utilities;
import net.moddedminecraft.betterchunkloader.data.ChunkLoader;
import net.moddedminecraft.betterchunkloader.data.PlayerData;
import net.moddedminecraft.betterchunkloader.menu.Menu;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.*;
import java.util.function.Consumer;

public class ListAll implements CommandExecutor {

    private final BetterChunkLoader plugin;

    public ListAll(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource sender, CommandContext commandContext) throws CommandException {
        Optional<String> loaderTypeOp = commandContext.<String>getOne("type");

        List<ChunkLoader> chunkLoaders = new ArrayList<>();

        for (World world : Sponge.getServer().getWorlds()) {
            chunkLoaders.addAll(plugin.getDataStore().getChunkLoaders(world));
        }

        List<Text> readableCLs = new ArrayList<>();
        for(ChunkLoader chunkLoader : chunkLoaders) {
            if (loaderTypeOp.isPresent()) {
                String loaderType = loaderTypeOp.get();
                if (loaderType.equalsIgnoreCase("alwayson") && chunkLoader.isAlwaysOn()) {
                    readableCLs.add(getReadableChunkLoader(chunkLoader, sender));
                } else if (loaderType.equalsIgnoreCase("online") && !chunkLoader.isAlwaysOn()) {
                    readableCLs.add(getReadableChunkLoader(chunkLoader, sender));
                }
            } else {
                readableCLs.add(getReadableChunkLoader(chunkLoader, sender));
            }
        }
        if (readableCLs.isEmpty()) {
            readableCLs.add(Utilities.parseMessage(plugin.getConfig().getMessages().chunksListNoChunkLoadersFound));
        }

        plugin.getPaginationService().builder()
                .contents(readableCLs)
                .title(Utilities.parseMessage(plugin.getConfig().getMessages().chunksListTitleAll))
                .padding(Utilities.parseMessage(plugin.getConfig().getMessages().chunksListPadding))
                .sendTo(sender);

        return CommandResult.success();
    }

    public Text getReadableChunkLoader(ChunkLoader chunkLoader, CommandSource sender) {
        Optional<PlayerData> playerData = plugin.getDataStore().getPlayerDataFor(chunkLoader.getOwner());

        String type = chunkLoader.isAlwaysOn() ? "Always On" : "Online Only";
        String loaded = chunkLoader.isLoadable() ? "&aTrue" : "&cFalse";
        String playerName = "null";

        if (playerData.isPresent()) {
            playerName = playerData.get().getName();
        }

        HashMap<String, String> args = new HashMap<>();
        args.put("ownerabr", StringUtils.abbreviate(playerName, 11));
        args.put("owner", playerName);
        args.put("type", type);
        args.put("location", Utilities.getReadableLocation(chunkLoader.getWorld(), chunkLoader.getLocation()));
        args.put("radius", chunkLoader.getRadius().toString());
        args.put("chunks", chunkLoader.getChunks().toString());
        args.put("loaded", loaded);
        args.put("server", chunkLoader.getServer());

        Text.Builder send = Text.builder();

        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (chunkLoader.canEdit(player)) {
                send.append(Text.builder().append(Utilities.parseMessage(plugin.getConfig().getMessages().chunksListEditAction))
                        .onClick(TextActions.executeCallback(editLoader(chunkLoader)))
                        .onHover(TextActions.showText(Utilities.parseMessage(plugin.getConfig().getMessages().chunksListHoverEditAction))).build());
                send.append(Utilities.parseMessage(" &f- "));
            }
        }

        if (sender.hasPermission(Permissions.TELEPORT)) {
            send.append(Utilities.parseMessageList(plugin.getConfig().getMessages().chunksListAll, args))
                    .onHover(TextActions.showText(Utilities.parseMessage(plugin.getConfig().getMessages().chunksListHoverAll, args)))
                    .onClick(TextActions.executeCallback(teleportTo(chunkLoader.getWorld(), chunkLoader.getLocation())));
        } else {
            send.append(Utilities.parseMessageList(plugin.getConfig().getMessages().chunksListAll, args))
                    .onHover(TextActions.showText(Utilities.parseMessage(plugin.getConfig().getMessages().chunksListHoverAll, args)));
        }

        return send.build();
    }

    private Consumer<CommandSource> editLoader(ChunkLoader chunkLoader) {
        return consumer -> {
          Player player = (Player) consumer;
            new Menu(plugin).showMenu(player, chunkLoader);
        };
    }

    private Consumer<CommandSource> teleportTo(UUID worldUUID, Vector3i vector3i) {
        return consumer -> {
            Player player = (Player) consumer;
            World world = null;
            if (Sponge.getServer().getWorld(worldUUID).isPresent()) {
                world = Sponge.getServer().getWorld(worldUUID).get();
            }
            if (world != null) {
                Location loc = new Location(world, vector3i);
                Vector3d vect = new Vector3d(0, 0, 0);
                player.setLocationAndRotation(loc, vect);

                HashMap<String, String> args = new HashMap<>();
                args.put("location", Utilities.getReadableLocation(worldUUID, vector3i));

                player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().chunksListTeleport, args));
            }
        };
    }
}
