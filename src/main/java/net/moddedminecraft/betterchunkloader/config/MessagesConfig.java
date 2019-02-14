package net.moddedminecraft.betterchunkloader.config;

import com.google.common.reflect.TypeToken;
import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@ConfigSerializable
public class MessagesConfig {

    public final BetterChunkLoader plugin;

    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    private static CommentedConfigurationNode config;

    private Path fileLoc;

    public MessagesConfig(BetterChunkLoader main) throws IOException {
        plugin = main;
        fileLoc = plugin.Configdir.resolve("messages.conf");
        loader = HoconConfigurationLoader.builder().setPath(fileLoc).build();
        config = loader.load();
        try {
            configCheck();
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
    }

    public String prefix = "&8[&6Chunkloader&8] &r";

    public String serverNameError = "&cPlease set the server name in the core configuration before setting up a chunkloader.";

    //ChunkLoader
    public String creationHelp = "&aIron and Diamond blocks can be converted into chunk loaders. Right click it with a blaze rod.";
    public String noPermissionCreate = "&cYou don't have the permission to create a chunkloader of that type.";
    public String noPermissionEdit = "&cYou don't have the permission to edit this chunkloader.";
    public String invalidOption = "&cThat is an invalid option.";
    public String notEnough = "&cNot enough chunks Needed: &e{needed}&c Available: &e{available}&c.";
    public String createSuccess = "&aCreated chunkloader, your balance was modified.";
    public String createFailure = "&aFailed to create chunkloader, your balance was not modified.";
    public String removeSuccess = "&cRemoved chunkloader, updated the balance of the owner.";
    public String removeFailure = "&cFailed to remove chunkloader, didn't update the balance of the owner.";
    public String updateSuccess = "&eUpdated chunkloader, your balance was modified.";
    public String updateFailure = "&eFailed to update chunkloader, your balance was not modified.";
    public String ownerNotify = "&cYour chunk loader at &e{location}&c has been removed by &e{player}&c, your balance has been modified.";

    public String infoTitle = "&8[&6Chunkloader&8]&r";
    public String infoPadding = "&8=";
    public List<String> infoItems = Arrays.asList(
            "&eInfo",
            "    &aOwner: &e{ownerName}",
            "    &aLocation: &e{location}",
            "    &aType: &e{type}",
            "    &aChunks: &e{chunks}",
            "    &aServer: &e{server}"
    );

    //Commands
    public String noPlayerExists = "&cThat player does not exist.";
    public String usageTitle = "&8[&6Chunkloader&8]&r";
    public String usagePadding = "&8=";
    public List<String> usageItems = Arrays.asList(
            "&eUsage:",
            "    &e/bcl balance ?<player>",
            "    &e/bcl chunks <add|set|remove> <player> <online|alwayson>",
            "    &e/bcl delete <type> ?<player>",
            "    &e/bcl info",
            "    &e/bcl list <type> ?<player>",
            "    &e/bcl purge",
            "    &e/bcl reload ?<core|messages|datastore>"
    );
    public String balanceNoPermission = "&cYou don't have permission to see the balance of other players.";
    public String balanceTitleSelf = "&8[&6Chunkloader Balance&8]&r";
    public String balanceTitleOther = "&8[&6{player}'s Chunkloader Balance&8]&r";
    public String balancePadding = "&8=";
    public List<String> balanceItems = Arrays.asList(
            "&7Total Used &8/ &7Total Available",
            "&3Online: &6{onlineused} &8/ &6 {online}",
            "&3Always On: &6{alwaysonused} &8/ &6 {alwayson}"
    );
    public String balanceFailure = "&cUnable to get the balance.";
    public String chunksUsage = "&eUsage: /bcl chunks <add|set|remove> <player> <online|alwayson> <amount>";
    public String chunksAddSuccess = "&aAdded &e{chunks}&a {type} chunks to &e{target}'s&a balance!";
    public String chunksAddFailure = "&cUnable to add &e{chunks}&c {type} chunks to &e{target}'s&c balance as it would become negative or would exceed the limit of &e{limit}&c.";
    public String chunksRemoveSuccess = "&aRemoved &e{chunks}&a {type} chunks from &e{target}'s&a balance!";
    public String chunksRemoveFailure = "&cUnable to remove &e{chunks}&c {type} chunks from &e{target}'s&c balance as it would become negative or would exceed the limit of &e{limit}&c.";
    public String chunksSetSuccess = "&aSet &e{target}'s&a {type} chunk balance to &e{chunks}&a.";
    public String chunksSetFailure = "&cUnable to set &e{target}'s&c {type} chunk balance to &e{chunks}&c as it would become negative or would exceed the limit of &e{limit}&c.";
    public String chunksDeleteUsage = "&eUsage: /bcl delete <type> ?<player>";
    public String chunksDeleteInvalidType = "&cInvalid chunkloader type: {type}, options are: (online|alwayson).";
    public String chunksDeleteConsoleError = "&cOnly players can remove their own chunkloaders, please specify a player name.";
    public String chunksDeleteOwnSuccess = "&aRemoved all your &e{type}&a chunkloaders.";
    public String chunksDeleteOwnFailure = "&cYou don't have any &e{type}&c chunkloaders.";
    public String chunksDeleteOthersSuccess = "&aRemoved &e{type}&a chunk loaders from &e{player}&a.";
    public String chunksDeleteOthersFailure = "&cPlayer &e{player}&c has no &e{type}&c chunkloaders.";
    public String chunksDeleteOthersNoPermission = "&cYou do not have permission to delete the chunkloaders of other players.";
    public String chunksInfoTitle = "&8[&6Chunkloader&8]&r";
    public String chunksInfoPadding = "&8=";
    public List<String> chunksInfoItems = Arrays.asList(
            "&eChunkloading Statistics:",
            "    &e{onlineLoaders}&a &aOnline loaders loading &e{onlineChunks}&a chunks.",
            "    &e{alwaysOnLoaders} &aAlways On loaders loading &e{alwaysOnChunks}&a chunks.",
            "    &e{playerCount}&a player(s) loading chunks!"
    );
    public String chunksInfoFailure = "&cNo statistics available!";
    public String chunksListTitleAll = "&8[&6All Chunkloaders&8]&r";
    public String chunksListTitleAlwaysOn = "&8[&6Always On Chunkloaders&8]&r";
    public String chunksListTitleOnlineOnly = "&8[&6Online Only Chunkloaders&8]&r";
    public String chunksListTitleSelf = "&8[&6Your Chunkloaders&8]&r";

    public String chunksListPadding = "&8=";
    public String chunksListTeleport = "&eTeleported to chunkloader at &6{location}";

        public String chunksListEditAction = "&8[&6Edit&8]";

            public String chunksListHoverEditAction = "&eClick here to edit this chunkloader";
            public String chunksListHoverAll =
                    "&3Owner: &6{owner}\n" +
                            "&3Type: &6{type}\n" +
                            "&3Loc: &6{location}\n"+
                            "&3Radius: &6{radius}\n"+
                            "&3Chunks: &6{chunks}"+
                            "&3Server: &6{server}";
            public String chunksListHoverAlwayson =
                    "&3Owner: &6{owner}\n" +
                            "&3Type: &6{type}\n" +
                            "&3Loc: &6{location}\n"+
                            "&3Radius: &6{radius}\n"+
                            "&3Chunks: &6{chunks}"+
                            "&3Server: &6{server}";
            public String chunksListHoverOnline =
                    "&3Owner: &6{owner}\n" +
                            "&3Type: &6{type}\n" +
                            "&3Loc: &6{location}\n"+
                            "&3Radius: &6{radius}\n"+
                            "&3Chunks: &6{chunks}"+
                            "&3Server: &6{server}";
            public String chunksListHoverSelf =
                    "&3Owner: &6{owner}\n" +
                            "&3Type: &6{type}\n" +
                            "&3Loc: &6{location}\n"+
                            "&3Radius: &6{radius}\n"+
                            "&3Chunks: &6{chunks}"+
                            "&3Server: &6{server}";

        public List<String> chunksListAll = Arrays.asList(
                "&3Owner: &6{ownerabr}",
                " &3Type: &6{type}",
                " &3Loaded: &6{loaded}",
                " &3Server: &6{server}"
        );
        public List<String> chunksListAlwayson = Arrays.asList(
                "&3Owner: &6{ownerabr}",
                " &3Type: &6{type}",
                " &3Loaded: &6{loaded}",
                " &3Server: &6{server}"
        );
        public List<String> chunksListOnline = Arrays.asList(
                "&3Owner: &6{ownerabr}",
                " &3Type: &6{type}",
                " &3Loaded: &6{loaded}",
                " &3Server: &6{server}"
        );
        public List<String> chunksListSelf = Arrays.asList(
                "&3Owner: &6{ownerabr}",
                " &3Type: &6{type}",
                " &3Loaded: &6{loaded}",
                " &3Server: &6{server}"
        );

        public String chunksListNoChunkLoadersFound = "&eThere is currently no chunkloaders.";
            public String chunksListNoPermission = "&cYou don't have permission to see others chunkloaders.";
            public String chunksListNoPlayer = "&cPlayer was specified but no player was found.";



    public String chunksPurgeUsage = "&aAll invalid chunk loaders have been removed!";
    public String chunksPurgeSuccess = "&aAll invalid chunk loaders have been removed!";
    public String chunksPurgeFailure = "&cUnable to remove invalid chunk loaders, none present.";
    public String chunksReloadUsage = "&eUsage: /bcl reload <core|messages|datastore>";
    public String chunksReloadSuccess = "&aReload success for: &e{type}&a.";
    public String chunksReloadFailure = "&cReload failed for: &e{type}&c, check console for more information.";

    public String menuChunkTitle1 = "Size: 1x1 Chunks";
    public String menuChunkTitle2 = "Size: 3x3 Chunks";
    public String menuChunkTitle3 = "Size: 5x5 Chunks";
    public String menuChunkTitle4 = "Size: 7x7 Chunks";
    public String menuChunkTitle5 = "Size: 9x9 Chunks";
    public String menuChunkTitle6 = "Size: 11x11 Chunks";
    public String menuChunkTitle7 = "Size: 13x13 Chunks";


    private void configCheck() throws IOException, ObjectMappingException {
        if (!fileLoc.toFile().exists()) {
            fileLoc.toFile().createNewFile();
        }

        prefix = check(config.getNode("Prefix"), prefix).getString();

        serverNameError = check(config.getNode("ChunkLoader", "ServerNameError"), serverNameError).getString();

        //Menu
        menuChunkTitle1 = check(config.getNode("Menu", "Chunks", "1"), menuChunkTitle1).getString();
        menuChunkTitle2 = check(config.getNode("Menu", "Chunks", "2"), menuChunkTitle2).getString();
        menuChunkTitle3 = check(config.getNode("Menu", "Chunks", "3"), menuChunkTitle3).getString();
        menuChunkTitle4 = check(config.getNode("Menu", "Chunks", "4"), menuChunkTitle4).getString();
        menuChunkTitle5 = check(config.getNode("Menu", "Chunks", "5"), menuChunkTitle5).getString();
        menuChunkTitle6 = check(config.getNode("Menu", "Chunks", "6"), menuChunkTitle6).getString();
        menuChunkTitle7 = check(config.getNode("Menu", "Chunks", "7"), menuChunkTitle7).getString();

        //ChunkLoader
        creationHelp = check(config.getNode("ChunkLoader", "CreationHelp"), creationHelp).getString();
        noPermissionCreate = check(config.getNode("ChunkLoader", "NoPermissionCreate"), noPermissionCreate).getString();
        noPermissionEdit = check(config.getNode("ChunkLoader", "NoPermissionEdit"), noPermissionEdit).getString();
        invalidOption = check(config.getNode("ChunkLoader", "InvalidOption"), invalidOption).getString();
        notEnough = check(config.getNode("ChunkLoader", "NotEnough"), notEnough).getString();
        createSuccess = check(config.getNode("ChunkLoader", "CreateSuccess"), createSuccess).getString();
        createFailure = check(config.getNode("ChunkLoader", "CreateFailure"), createFailure).getString();
        removeSuccess = check(config.getNode("ChunkLoader", "RemoveSuccess"), removeSuccess).getString();
        removeFailure = check(config.getNode("ChunkLoader", "RemoveFailure"), removeFailure).getString();
        updateSuccess = check(config.getNode("ChunkLoader", "UpdateSuccess"), updateSuccess).getString();
        updateFailure = check(config.getNode("ChunkLoader", "UpdateFailure"), updateFailure).getString();
        ownerNotify = check(config.getNode("ChunkLoader", "OwnerNotify"), ownerNotify).getString();

        infoTitle = check(config.getNode("ChunkLoader", "Info", "Title"), infoTitle).getString();
        infoPadding = check(config.getNode("ChunkLoader", "Info", "Padding"), infoPadding).getString();
        infoItems = check(config.getNode("ChunkLoader", "Info", "Items"), infoItems).getList(TypeToken.of(String.class));

        //Commands
        noPlayerExists = check(config.getNode("Commands", "NoPlayerExists"), noPlayerExists).getString();

        usageTitle = check(config.getNode("Commands", "Usage", "Title"), usageTitle).getString();
        usagePadding = check(config.getNode("Commands", "Usage", "Padding"), usagePadding).getString();
        usageItems = check(config.getNode("Commands", "Usage", "Items"), usageItems).getList(TypeToken.of(String.class));

        balanceNoPermission = check(config.getNode("Commands", "Balance", "NoPermission"), balanceNoPermission).getString();
        balanceTitleSelf = check(config.getNode("Commands", "Balance", "Success", "TitleSelf"), balanceTitleSelf).getString();
        balanceTitleOther = check(config.getNode("Commands", "Balance", "Success", "TitleOther"), balanceTitleOther).getString();
        balancePadding = check(config.getNode("Commands", "Balance", "Success", "Padding"), balancePadding).getString();
        balanceItems = check(config.getNode("Commands", "Balance", "Success", "Items"), balanceItems).getList(TypeToken.of(String.class));
        balanceFailure = check(config.getNode("Commands", "Balance", "Failure"), balanceFailure).getString();

        chunksUsage = check(config.getNode("Commands", "Chunks", "Usage"), chunksUsage).getString();
        chunksAddSuccess = check(config.getNode("Commands", "Chunks", "Add", "Success"), chunksAddSuccess).getString();
        chunksAddFailure = check(config.getNode("Commands", "Chunks", "Add", "Failure"), chunksAddFailure).getString();
        chunksRemoveSuccess = check(config.getNode("Commands", "Chunks", "Remove", "Success"), chunksRemoveSuccess).getString();
        chunksRemoveFailure = check(config.getNode("Commands", "Chunks", "Remove", "Failure"), chunksRemoveFailure).getString();

        chunksSetSuccess = check(config.getNode("Commands", "Chunks", "Set", "Success"), chunksSetSuccess).getString();
        chunksSetFailure = check(config.getNode("Commands", "Chunks", "Set", "Failure"), chunksSetFailure).getString();

        chunksDeleteUsage = check(config.getNode("Commands", "Delete", "Usage"), chunksDeleteUsage).getString();
        chunksDeleteInvalidType = check(config.getNode("Commands", "Delete", "InvalidType"), chunksDeleteInvalidType).getString();
        chunksDeleteConsoleError = check(config.getNode("Commands", "Delete", "ConsoleError"), chunksDeleteConsoleError).getString();
        chunksDeleteOwnSuccess = check(config.getNode("Commands", "Delete", "Own", "Success"), chunksDeleteOwnSuccess).getString();
        chunksDeleteOwnFailure = check(config.getNode("Commands", "Delete", "Own", "Failure"), chunksDeleteOwnFailure).getString();
        chunksDeleteOthersSuccess = check(config.getNode("Commands", "Delete", "Others", "Success"), chunksDeleteOthersSuccess).getString();
        chunksDeleteOthersFailure = check(config.getNode("Commands", "Delete", "Others", "Failure"), chunksDeleteOthersFailure).getString();
        chunksDeleteOthersNoPermission = check(config.getNode("Commands", "Delete", "Others", "NoPermission"), chunksDeleteOthersNoPermission).getString();

        chunksInfoTitle = check(config.getNode("Commands", "Info", "Success", "Title"), chunksInfoTitle).getString();
        chunksInfoPadding = check(config.getNode("Commands", "Info", "Success", "Padding"), chunksInfoPadding).getString();
        chunksInfoItems = check(config.getNode("Commands", "Info", "Success", "Items"), chunksInfoItems).getList(TypeToken.of(String.class));
        chunksInfoFailure = check(config.getNode("Commands", "Info", "Failure"), chunksInfoFailure).getString();

        chunksPurgeUsage = check(config.getNode("Commands", "Purge", "Usage"), chunksPurgeUsage).getString();
        chunksPurgeSuccess = check(config.getNode("Commands", "Purge", "Success"), chunksPurgeSuccess).getString();
        chunksPurgeFailure = check(config.getNode("Commands", "Purge", "Failure"), chunksPurgeFailure).getString();

        chunksReloadUsage = check(config.getNode("Commands", "Reload", "Usage"), chunksReloadUsage).getString();
        chunksReloadSuccess = check(config.getNode("Commands", "Reload", "Success"), chunksReloadSuccess).getString();
        chunksReloadFailure = check(config.getNode("Commands", "Reload", "Failure"), chunksReloadFailure).getString();


        chunksListNoChunkLoadersFound = check(config.getNode("Commands", "List", "NoChunkloadersFound"), chunksListNoChunkLoadersFound).getString();
        chunksListNoPermission = check(config.getNode("Commands", "List", "NoPermission"), chunksListNoPermission).getString();
        chunksListNoPlayer = check(config.getNode("Commands", "List", "NoPlayer"), chunksListNoPlayer).getString();

        chunksListPadding = check(config.getNode("Commands", "Success", "Padding"), chunksListPadding).getString();
        chunksListTeleport = check(config.getNode("Commands", "Success", "Teleport"), chunksListTeleport).getString();

        chunksListTitleAll = check(config.getNode("Commands", "Success", "Title", "TitleAll"), chunksListTitleAll).getString();
        chunksListTitleAlwaysOn = check(config.getNode("Commands", "Success", "Title", "TitleAlwaysOn"), chunksListTitleAlwaysOn).getString();
        chunksListTitleOnlineOnly = check(config.getNode("Commands", "Success", "Title", "TitleOnlineOnly"), chunksListTitleOnlineOnly).getString();
        chunksListTitleSelf = check(config.getNode("Commands", "Success", "Title", "TitleSelf"), chunksListTitleSelf).getString();

        chunksListAll = check(config.getNode("Commands", "Success", "Format", "All"), chunksListAll).getList(TypeToken.of(String.class));
        chunksListAlwayson = check(config.getNode("Commands", "Success", "Format", "Alwayson"), chunksListAlwayson).getList(TypeToken.of(String.class));
        chunksListEditAction = check(config.getNode("Commands", "Success", "Format", "EditAction"), chunksListEditAction).getString();
        chunksListOnline = check(config.getNode("Commands", "Success", "Format", "Online"), chunksListOnline).getList(TypeToken.of(String.class));
        chunksListSelf = check(config.getNode("Commands", "Success", "Format", "Self"), chunksListSelf).getList(TypeToken.of(String.class));

        chunksListHoverAll = check(config.getNode("Commands", "Success", "Format", "Hover", "All"), chunksListHoverAll).getString();
        chunksListHoverAlwayson = check(config.getNode("Commands", "Success", "Format", "Hover", "AlwaysOn"), chunksListHoverAlwayson).getString();
        chunksListHoverEditAction = check(config.getNode("Commands", "Success", "Format", "Hover", "EditAction"), chunksListHoverEditAction).getString();
        chunksListHoverOnline = check(config.getNode("Commands", "Success", "Format", "Hover", "Online"), chunksListHoverOnline).getString();
        chunksListHoverSelf = check(config.getNode("Commands", "Success", "Format", "Hover", "Self"), chunksListHoverSelf).getString();

        loader.save(config);
    }

    private CommentedConfigurationNode check(CommentedConfigurationNode node, Object defaultValue, String comment) {
        if (node.isVirtual()) {
            node.setValue(defaultValue).setComment(comment);
        }
        return node;
    }

    private CommentedConfigurationNode check(CommentedConfigurationNode node, Object defaultValue) {
        if (node.isVirtual()) {
            node.setValue(defaultValue);
        }
        return node;
    }
}
