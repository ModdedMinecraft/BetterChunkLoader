package net.moddedminecraft.betterchunkloader.commands;

import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import net.moddedminecraft.betterchunkloader.Permissions;
import net.moddedminecraft.betterchunkloader.commands.list.ListAll;
import net.moddedminecraft.betterchunkloader.commands.list.ListAlwayson;
import net.moddedminecraft.betterchunkloader.commands.list.ListOnline;
import net.moddedminecraft.betterchunkloader.commands.list.ListSelf;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import java.util.HashMap;
import java.util.Map;

public class CommandManager {

    private final BetterChunkLoader plugin;

    public CommandManager(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    public void register() {
        Map<String, String> reloadArgs = new HashMap<String, String>() {
            {
                put("core", "core");
                put("messages", "messages");
                put("data", "data");
            }
        };

        Map<String, String> loaderType = new HashMap<String, String>() {
            {
                put("online", "online");
                put("alwayson", "alwayson");
            }
        };

        Map<String, String> flags = new HashMap<String, String>() {
            {
                put("expired", "expired");
                put("active", "active");
            }
        };

        Map<String, String> changeType = new HashMap<String, String>() {
            {
                put("set", "set");
                put("add", "add");
                put("remove", "remove");
            }
        };


        // /bcl bal (player)
        CommandSpec cmdBalance = CommandSpec.builder()
                .arguments(GenericArguments.optional(GenericArguments.user(Text.of("player"))))
                .executor(new Balance(this.plugin))
                .permission(Permissions.COMMAND_BALANCE + ".base")
                .build();

        // /bcl reload (type)
        CommandSpec cmdReload = CommandSpec.builder()
                .arguments(GenericArguments.optional(GenericArguments.choices(Text.of("type"), reloadArgs)))
                .executor(new Reload(this.plugin))
                .permission(Permissions.COMMAND_RELOAD + ".base")
                .build();

        // /bcl info
        CommandSpec cmdInfo = CommandSpec.builder()
                .executor(new Info(this.plugin))
                .permission(Permissions.COMMAND_INFO + ".base")
                .build();

        // /bcl chunks
        CommandSpec cmdChunks = CommandSpec.builder()
                .arguments(
                        GenericArguments.choices(Text.of("change"), changeType),
                        GenericArguments.user(Text.of("player")),
                        GenericArguments.choices(Text.of("type"), loaderType),
                        GenericArguments.integer(Text.of("value"))
                )
                .executor(new Chunks(this.plugin))
                .permission(Permissions.COMMAND_CHUNKS + ".base")
                .build();

        // /bcl list all [type]
        CommandSpec cmdListAll = CommandSpec.builder()
                .executor(new ListAll(this.plugin))
                .arguments(GenericArguments.optional(GenericArguments.choices(Text.of("type"), loaderType)))
                .permission(Permissions.COMMAND_LIST + ".all")
                .build();

        // /bcl list alwayson [player]
        CommandSpec cmdListAlwaysOn = CommandSpec.builder()
                .arguments(GenericArguments.optional(GenericArguments.user(Text.of("player"))))
                .executor(new ListAlwayson(this.plugin))
                .permission(Permissions.COMMAND_LIST + ".alwayson")
                .build();

        // /bcl list online [player]
        CommandSpec cmdListOnline = CommandSpec.builder()
                .arguments(GenericArguments.optional(GenericArguments.user(Text.of("player"))))
                .executor(new ListOnline(this.plugin))
                .permission(Permissions.COMMAND_LIST + ".online")
                .build();

        // /bcl list
        CommandSpec cmdList = CommandSpec.builder()
                .executor(new ListSelf(this.plugin))
                .child(cmdListAll, "all")
                .child(cmdListAlwaysOn, "alwayson")
                .child(cmdListOnline, "online")
                .permission(Permissions.COMMAND_LIST + ".base")
                .build();

        // /bcl delete
        CommandSpec cmdDelete = CommandSpec.builder()
                .arguments(
                        GenericArguments.choices(Text.of("type"), loaderType),
                        GenericArguments.optional(GenericArguments.user(Text.of("player"))),
                        GenericArguments.optional(GenericArguments.choices(Text.of("flag"), flags))
                )
                .executor(new Delete(this.plugin))
                .permission(Permissions.COMMAND_DELETE + ".base")
                .build();

        // /bcl purge
        CommandSpec cmdPurge = CommandSpec.builder()
                .executor(new Purge(this.plugin))
                .permission(Permissions.COMMAND_PURGE + ".base")
                .build();

        CommandSpec mclCmdSpec = CommandSpec.builder()
                .child(cmdBalance, "balance", "bal")
                .child(cmdInfo, "info", "i")
                .child(cmdList, "list", "ls")
                .child(cmdChunks, "chunks", "c")
                .child(cmdDelete, "delete", "d")
                .child(cmdPurge, "purge")
                .child(cmdReload, "reload")
                .executor(new BCL(this.plugin))
                .build();

        Sponge.getCommandManager().register(this.plugin, mclCmdSpec, "net/moddedminecraft/betterchunkloader", "bcl", "mcl");
    }

}
