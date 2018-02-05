package net.moddedminecraft.betterchunkloader.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.Arrays;
import java.util.List;

@ConfigSerializable
public class MessagesConfig {

    @Setting("Prefix")
    public String prefix = "&8[&6Chunkloader&8] &r";

    @Setting("ChunkLoader")
    public ChunkLoader chunkLoader = new ChunkLoader();

    @Setting("Commands")
    public Commands commands = new Commands();

    @ConfigSerializable
    public static class ChunkLoader {

        @Setting("CreationHelp")
        public String creationHelp = "&aIron and Diamond blocks can be converted into chunk loaders. Right click it with a blaze rod.";

        @Setting("NoPermissionCreate")
        public String noPermissionCreate = "&cYou don't have the permission to create a chunkloader of that type.";

        @Setting("NoPermissionEdit")
        public String noPermissionEdit = "&cYou don't have the permission to edit this chunkloader.";

        @Setting("Info")
        public Info info = new Info();

        @ConfigSerializable
        public static class Info {

            @Setting("Title")
            public String title = "&8[&6Chunkloader&8]&r";

            @Setting("Padding")
            public String padding = "&8=";

            @Setting("Items")
            public List<String> items = Arrays.asList(
                    "&eInfo",
                    "    &aOwner: &e{ownerName}",
                    "    &aLocation: &e{location}",
                    "    &aType: &e{type}",
                    "    &aChunks: &e{chunks}"
            );
        }

        @Setting("InvalidOption")
        public String invalidOption = "&cThat is an invalid option.";

        @Setting("NotEnough")
        public String notEnough = "&cNot enough chunks Needed: &e{needed}&c Available: &e{available}&c.";

        @Setting("CreateSuccess")
        public String createSuccess = "&aCreated chunkloader, your balance was modified.";

        @Setting("CreateFailure")
        public String createFailure = "&aFailed to create chunkloader, your balance was not modified.";

        @Setting("RemoveSuccess")
        public String removeSuccess = "&cRemoved chunkloader, updated the balance of the owner.";

        @Setting("RemoveFailure")
        public String removeFailure = "&cFailed to remove chunkloader, didn't update the balance of the owner.";

        @Setting("UpdateSuccess")
        public String updateSuccess = "&eUpdated chunkloader, your balance was modified.";

        @Setting("UpdateFailure")
        public String updateFailure = "&eFailed to update chunkloader, your balance was not modified.";

        @Setting("OwnerNotify")
        public String ownerNotify = "&cYour chunk loader at &e{location}&c has been removed by &e{player}&c, your balance has been modified.";
    }

    @ConfigSerializable
    public static class Commands {

        @Setting("NoPlayerExists")
        public String noPlayerExists = "&cThat player does not exist.";

        @Setting("Usage")
        public Usage usage = new Usage();

        @ConfigSerializable
        public static class Usage {

            @Setting("Title")
            public String title = "&8[&6Chunkloader&8]&r";

            @Setting("Padding")
            public String padding = "&8=";

            @Setting("Items")
            public List<String> items = Arrays.asList(
                    "&eUsage:",
                    "    &e/bcl balance ?<player>",
                    "    &e/bcl chunks <add|set|remove> <player> <online|alwayson>",
                    "    &e/bcl delete <type> ?<player>",
                    "    &e/bcl info",
                    "    &e/bcl list <type> ?<player>",
                    "    &e/bcl purge",
                    "    &e/bcl reload ?<core|messages|datastore>"
            );
        }

        @Setting("Balance")
        public CBalance balance = new CBalance();

        @Setting("Chunks")
        public CChunks chunks = new CChunks();

        @Setting("Delete")
        public CDelete delete = new CDelete();

        @Setting("Info")
        public CInfo info = new CInfo();

        @Setting("List")
        public CList list = new CList();

        @Setting("Reload")
        public CReload reload = new CReload();

        @Setting("Purge")
        public CPurge purge = new CPurge();

        @ConfigSerializable
        public static class CBalance {

            @Setting("NoPermission")
            public String noPermission = "&cYou don't have permission to see the balance of other players.";

            @Setting("Success")
            public Success success = new Success();

            @ConfigSerializable
            public static class Success {

                @Setting("TitleSelf")
                public String titleSelf = "&8[&6Chunkloader Balance&8]&r";

                @Setting("TitleOther")
                public String titleOther = "&8[&6{player}'s Chunkloader Balance&8]&r";

                @Setting("Padding")
                public String padding = "&8=";

                @Setting("Items")
                public List<String> items = Arrays.asList(
                        "&7Total Used &8/ &7Total Available",
                        "&3Online: &6{onlineused} &8/ &6 {online}",
                        "&3Always On: &6{alwaysonused} &8/ &6 {alwayson}"
                );
            }
            @Setting("Failure")
            public String failure = "&cUnable to get the balance.";
        }

        @ConfigSerializable
        public static class CChunks {

            @Setting("Usage")
            public String usage = "&eUsage: /bcl chunks <add|set|remove> <player> <online|alwayson> <amount>";

            @Setting("Add")
            public Add add = new Add();

            @Setting("Remove")
            public Remove remove = new Remove();

            @Setting("Set")
            public Set set = new Set();

            @ConfigSerializable
            public static class Add {

                @Setting("Success")
                public String success = "&aAdded &e{chunks}&a {type} chunks to &e{target}'s&a balance!";

                @Setting("Failure")
                public String failure = "&cUnable to add &e{chunks}&c {type} chunks to &e{target}'s&c balance as it would become negative or would exceed the limit of &e{limit}&c.";

            }

            @ConfigSerializable
            public static class Remove {

                @Setting("Success")
                public String success = "&aRemoved &e{chunks}&a {type} chunks from &e{target}'s&a balance!";

                @Setting("Failure")
                public String failure = "&cUnable to remove &e{chunks}&c {type} chunks from &e{target}'s&c balance as it would become negative or would exceed the limit of &e{limit}&c.";

            }

            @ConfigSerializable
            public static class Set {

                @Setting("Success")
                public String success = "&aSet &e{target}'s&a {type} chunk balance to &e{chunks}&a.";

                @Setting("Failure")
                public String failure = "&cUnable to set &e{target}'s&c {type} chunk balance to &e{chunks}&c as it would become negative or would exceed the limit of &e{limit}&c.";

            }
        }

        @ConfigSerializable
        public static class CDelete {

            @Setting("Usage")
            public String usage = "&eUsage: /bcl delete <type> ?<player>";

            @Setting("InvalidType")
            public String invalidType = "&cInvalid chunkloader type: {type}, options are: (online|alwayson).";

            @Setting("ConsoleError")
            public String consoleError = "&cOnly players can remove their own chunkloaders, please specify a player name.";

            @Setting("Own")
            public Own own = new Own();

            @Setting("Others")
            public Others others = new Others();

            @ConfigSerializable
            public static class Own {

                @Setting("Success")
                public String success = "&aRemoved all your &e{type}&a chunkloaders.";

                @Setting("Failure")
                public String failure = "&cYou don't have any &e{type}&c chunkloaders.";
            }

            @ConfigSerializable
            public static class Others {

                @Setting("Success")
                public String success = "&aRemoved &e{type}&a chunk loaders from &e{player}&a.";

                @Setting("Failure")
                public String failure = "&cPlayer &e{player}&c has no &e{type}&c chunkloaders.";

                @Setting("NoPermission")
                public String noPermission = "&cYou do not have permission to delete the chunkloaders of other players.";
            }
        }

        @ConfigSerializable
        public static class CInfo {

            @Setting("Success")
            public Success success = new Success();

            @ConfigSerializable
            public static class Success {

                @Setting("Title")
                public String title = "&8[&6Chunkloader&8]&r";

                @Setting("Padding")
                public String padding = "&8=";

                @Setting("Items")
                public List<String> items = Arrays.asList(
                        "&eChunkloading Statistics:",
                        "    &e{onlineLoaders}&a &aOnline loaders loading &e{onlineChunks}&a chunks.",
                        "    &e{alwaysOnLoaders} &aAlways On loaders loading &e{alwaysOnChunks}&a chunks.",
                        "    &e{playerCount}&a player(s) loading chunks!"
                );
            }
            @Setting("Failure")
            public String failure = "&cNo statistics available!";

        }

        @ConfigSerializable
        public static class CList {

            @Setting("Success")
            public Success success = new Success();

            @ConfigSerializable
            public static class Success {

                @Setting("Title")
                public Title title = new Title();

                @ConfigSerializable
                public static class Title {

                    @Setting("TitleAll")
                    public String all = "&8[&6All Chunkloaders&8]&r";

                    @Setting("TitleAlwaysOn")
                    public String alwaysOn = "&8[&6Always On Chunkloaders&8]&r";

                    @Setting("TitleOnlineOnly")
                    public String onlineOnly = "&8[&6Online Only Chunkloaders&8]&r";

                    @Setting("TitleSelf")
                    public String self = "&8[&6Your Chunkloaders&8]&r";
                }

                @Setting("Padding")
                public String padding = "&8=";

                @Setting("Teleport")
                public String teleport = "&eTeleported to chunkloader at &6{location}";

                @Setting("Format")
                public Format format = new Format();

                @ConfigSerializable
                public static class Format {

                    @Setting("EditAction")
                    public String editAction = "&8[&6Edit&8] &f- ";

                    @Setting("Hover")
                    public Hover hover = new Hover();

                    @ConfigSerializable
                    public static class Hover {

                        @Setting("All")
                        public String all =
                                "&3Owner: &6{owner}\n" +
                                        "&3Type: &6{type}\n" +
                                        "&3Loc: &6{location}\n"+
                                        "&3Radius: &6{radius}\n"+
                                        "&3Chunks: &6{chunks}";

                        @Setting("AlwaysOn")
                        public String alwayson =
                                "&3Owner: &6{owner}\n" +
                                        "&3Type: &6{type}\n" +
                                        "&3Loc: &6{location}\n"+
                                        "&3Radius: &6{radius}\n"+
                                        "&3Chunks: &6{chunks}";

                        @Setting("Online")
                        public String online =
                                "&3Owner: &6{owner}\n" +
                                        "&3Type: &6{type}\n" +
                                        "&3Loc: &6{location}\n"+
                                        "&3Radius: &6{radius}\n"+
                                        "&3Chunks: &6{chunks}";

                        @Setting("Self")
                        public String self =
                                "&3Owner: &6{owner}\n" +
                                        "&3Type: &6{type}\n" +
                                        "&3Loc: &6{location}\n"+
                                        "&3Radius: &6{radius}\n"+
                                        "&3Chunks: &6{chunks}";
                    }

                    @Setting("All")
                    public List<String> all = Arrays.asList(
                            "&3Owner: &6{owner}",
                            " &3Type: &6{type}",
                            " &3Loaded: &6{loaded}"
                    );

                    @Setting("Alwayson")
                    public List<String> alwayson = Arrays.asList(
                            "&3Owner: &6{owner}",
                            " &3Type: &6{type}",
                            " &3Loaded: &6{loaded}"
                    );

                    @Setting("Online")
                    public List<String> online = Arrays.asList(
                            "&3Owner: &6{owner}",
                            " &3Type: &6{type}",
                            " &3Loaded: &6{loaded}"
                    );

                    @Setting("Self")
                    public List<String> self = Arrays.asList(
                            "&3Owner: &6{owner}",
                            " &3Type: &6{type}",
                            " &3Loaded: &6{loaded}"
                    );
                }
            }

            @Setting("NoChunkloadersFound")
            public String noChunkLoadersFound = "&eThere is currently no chunkloaders.";

            @Setting("NoPermission")
            public String noPermission = "&cYou don't have permission to see others chunkloaders.";

            @Setting("NoPlayer")
            public String noPlayer = "&cPlayer was specified but no player was found.";

        }

        @ConfigSerializable
        public static class CPurge {

            @Setting("Success")
            public String success = "&aAll invalid chunk loaders have been removed!";

            @Setting("Failure")
            public String failure = "&cUnable to remove invalid chunk loaders, none present.";

        }

        @ConfigSerializable
        public static class CReload {

            @Setting("Usage")
            public String usage = "&eUsage: /bcl reload <core|messages|datastore>";

            @Setting("Success")
            public String success = "&aReload success for: &e{type}&a.";

            @Setting("Failure")
            public String failure = "&cReload failed for: &e{type}&c, check console for more information.";

        }
    }
}
