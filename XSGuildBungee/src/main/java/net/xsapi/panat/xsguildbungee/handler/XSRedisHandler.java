package net.xsapi.panat.xsguildbungee.handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.xsapi.panat.xsguildbungee.config.mainConfig;
import net.xsapi.panat.xsguildbungee.core;
import net.xsapi.panat.xsguildbungee.objects.XSGuilds;
import net.xsapi.panat.xsguildbungee.objects.XSSubGuilds;
import net.xsapi.panat.xsguildbungee.utils.XSDATA_TYPE;
import net.xsapi.panat.xsguildbungee.utils.XSGUILD_POSITIONS;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class XSRedisHandler {
    public static String redisHost;
    public static int redisPort;
    public static String redisPass;
    public static ArrayList<Thread> threads = new ArrayList<>();

    public static String getHostRedis() {
        return redisHost;
    }
    public static String getRedisPass() {
        return redisPass;
    }

    public static int getRedisPort() {
        return redisPort;
    }

    public static void redisConnection() {
        redisHost = mainConfig.getConfig().getString("redis.host");
        redisPort = mainConfig.getConfig().getInt("redis.port");
        redisPass = mainConfig.getConfig().getString("redis.password");

        try {
            Jedis jedis = new Jedis(getHostRedis(), getRedisPort());
            if(!getRedisPass().isEmpty()) {
                jedis.auth(getRedisPass());
            }
            jedis.close();
            core.getPlugin().getLogger().info("Redis Server : Connected");
        } catch (Exception e) {
            core.getPlugin().getLogger().info("Redis Server : Not Connected");
            e.printStackTrace();
        }
    }

    public static void subscribeToChannelAsync(String channelName) {
        Thread thread = new Thread(() -> {
            try (Jedis jedis = new Jedis(getHostRedis(), getRedisPort())) {
                if(!getRedisPass().isEmpty()) {
                    jedis.auth(getRedisPass());
                }
                JedisPubSub jedisPubSub = new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        if (Thread.currentThread().isInterrupted()) {
                            return;
                        }
                        if(channel.startsWith(XSHandler.getSubChannel()+"_bungeecord")) {


                           // core.getPlugin().getLogger().info("[bungeecord] GET MESSAGE " + message);

                            String type = message.split("<SPLIT>")[0];
                            String arguments = message.split("<SPLIT>")[1];

                            if(type.equalsIgnoreCase(XSDATA_TYPE.CREATE.toString())) {
                                String leader = arguments.split(";")[0];
                                String guildRealName = arguments.split(";")[1];
                                String guildName = arguments.split(";")[2];
                                XSDatabaseHandler.createGuild(guildRealName,guildName,leader);
                                Gson gson = new Gson();
                                String guildJson = gson.toJson(XSGuildsHandler.getGuildList().get(guildRealName));
                                //core.getPlugin().getLogger().info(guildJson);
                                for(String servers : mainConfig.getConfig().getSection("guilds-group").getKeys()) {
                                    for(String subServer : mainConfig.getConfig().getStringList("guilds-group." + servers)) {
                                        XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+subServer,XSDATA_TYPE.UPDATED+"<SPLIT>"+servers+";"+guildJson);
                                        XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+subServer,XSDATA_TYPE.UPDATE_PLAYER+"<SPLIT>"+servers+";"+guildRealName+";"+subServer+";"+leader);
                                    }
                                }

                                //XSGuildsHandler.getPlayers().put(player,server+"<SPLIT>"+guild+"<SPLIT>"+ mainConfig.customConfig.getString("configuration.server"));
                                 //core.getPlugin().getLogger().info("Create guild " + guildName);
                            } else if(type.equalsIgnoreCase(XSDATA_TYPE.REQ_DATA.toString())) {
                                String server = arguments.split(";")[0];
                                String player = arguments.split(";")[1];

                                /*for(Map.Entry<String,String> list : XSGuildsHandler.getPlayers().entrySet()) {
                                    core.getPlugin().getLogger().info(list.getKey() + " " + list.getValue());
                                }*/
                                for(String servers : mainConfig.getConfig().getSection("guilds-group").getKeys()) {
                                    if(mainConfig.getConfig().getStringList("guilds-group." + servers).contains(server)) {
                                        if(!XSGuildsHandler.getPlayers().containsKey(player)) {
                                            XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+server,XSDATA_TYPE.LOAD_DATA+"<SPLIT>" + player + ";NO_GUILD");
                                        } else {
                                            String guild = XSGuildsHandler.getPlayers().get(player);
                                            String inGuildChat = "NO";

                                            if(XSHandler.getPlayerInGuildChat().contains(player)) {
                                                inGuildChat = "YES";
                                            }

                                            XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+server,XSDATA_TYPE.LOAD_DATA+"<SPLIT>" + player + ";" + servers + ";" + guild+";"+inGuildChat+";"+server);
                                        }
                                        break;
                                    }
                                }
                            } else  if(type.equalsIgnoreCase(XSDATA_TYPE.REQ_GUILD.toString())) {
                                String server = arguments.split(";")[0];
                                Gson gson = new Gson();

                                String guildJson = gson.toJson(XSGuildsHandler.getGuildList());
                                //core.getPlugin().getLogger().info(guildJson);
                                XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+server,XSDATA_TYPE.GET_GUILD+"<SPLIT>" + guildJson);

                                String mainClanUpgradeJson = gson.toJson(XSHandler.mainClanUpgrades);
                                String subClanUpgradeJson = gson.toJson(XSHandler.subClanUpgrades);
                                XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+server, XSDATA_TYPE.SENT_UPGRADES_INFO+"<SPLIT>"+mainClanUpgradeJson+";"+subClanUpgradeJson);;
                                XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+server, XSDATA_TYPE.SET_PRICE+"<SPLIT>"+XSHandler.getCreatePrice());
                            } else if(type.equalsIgnoreCase(XSDATA_TYPE.DISBAND.toString())) {
                                String guild = arguments.split(";")[1];
                                String player = arguments.split(";")[2];

                                XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);
                                XSGuildsHandler.removeGuildFromDatabase(xsGuilds);
                                XSGuildsHandler.getPlayers().remove(xsGuilds.getLeader());
                                XSGuildsHandler.getGuildList().remove(guild);
                                XSGuildsHandler.removeGuildToAllServer(guild);

                                if(XSHandler.getPlayerInGuildChat().contains(player)) {
                                    XSHandler.getPlayerInGuildChat().remove(player);
                                }

                            } else if(type.equalsIgnoreCase(XSDATA_TYPE.INVITE.toString())) {

                                String server = arguments.split(";")[0];
                                String player = arguments.split(";")[1];
                                String guild = arguments.split(";")[2];
                                String sender = arguments.split(";")[3];

                                ProxiedPlayer target = core.getPlugin().getProxy().getPlayer(player);
                                if(target == null || !target.isConnected()) {
                                    XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+server,XSDATA_TYPE.INVITE_RETURN+"<SPLIT>NULL_PLAYER;" + sender);
                                } else {

                                    if(XSGuildsHandler.getPlayers().containsKey(player)) {

                                        if(XSGuildsHandler.getPlayers().get(player).equalsIgnoreCase(guild)) {
                                            XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+server,XSDATA_TYPE.INVITE_RETURN+"<SPLIT>ALREADY_IN_GUILD;" + sender+";"+player);
                                        } else {
                                            XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+server,XSDATA_TYPE.INVITE_RETURN+"<SPLIT>ALREADY_IN_OTHER_GUILD;" + sender+";"+player);
                                        }
                                    } else {
                                        XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);

                                        if(xsGuilds.getPendingInvite().containsKey(player)) {
                                            XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+server,XSDATA_TYPE.INVITE_RETURN+"<SPLIT>ALREADY_SENT;" + sender+";"+player);
                                        } else {
                                            xsGuilds.getPendingInvite().put(player,System.currentTimeMillis());
                                            XSGuildsHandler.updateToAllServer(xsGuilds);
                                            XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+server,XSDATA_TYPE.INVITE_RETURN+"<SPLIT>SENT;" + sender+";"+player);
                                            XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+target.getServer().getInfo().getName(),
                                                    XSDATA_TYPE.INVITE_GET+"<SPLIT>"+guild+";"+player);
                                        }
                                    }
                                }

                            } else if(type.equalsIgnoreCase(XSDATA_TYPE.INVITE_RESPOND.toString())) {
                                String respondType = arguments.split(";")[0];
                                String guild = arguments.split(";")[1];
                                String player = arguments.split(";")[2];

                                ProxiedPlayer target = core.getPlugin().getProxy().getPlayer(player);

                                if(!XSGuildsHandler.getGuildList().containsKey(guild)) {
                                    XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+target.getServer().getInfo().getName(),
                                            XSDATA_TYPE.INVITE_RESPOND_RETURN+"<SPLIT>GUILD_NULL;"+player);
                                } else {
                                    XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);

                                    if(!xsGuilds.getPendingInvite().containsKey(player)) {
                                        XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+target.getServer().getInfo().getName(),
                                                XSDATA_TYPE.INVITE_RESPOND_RETURN+"<SPLIT>GUILD_NOT_INVITE;"+player);
                                    } else {
                                        ProxiedPlayer guildLeader = core.getPlugin().getProxy().getPlayer(xsGuilds.getLeader());

                                        if(respondType.equalsIgnoreCase("accept")) {

                                            xsGuilds.getMembers().put(player,XSGUILD_POSITIONS.NEW_MEMBER.toString());
                                            xsGuilds.getClanmates().add(player);
                                            XSGuildsHandler.getPlayers().put(player,guild);

                                            XSGuildsHandler.updateToAllServer(xsGuilds);
                                            String server = XSGuildsHandler.getServer(target.getServer().getInfo().getName());
                                            XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+target.getServer().getInfo().getName(),
                                                    XSDATA_TYPE.INVITE_RESPOND_RETURN+"<SPLIT>ACCEPT;"+player+";"+server+";"+guild); //send to player that decline

                                            if(guildLeader != null && guildLeader.isConnected()) { //respond to leader
                                                XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+guildLeader.getServer().getInfo().getName(),
                                                        XSDATA_TYPE.INVITE_RESPOND_RETURN+"<SPLIT>ACCEPT_FROM;"+player+";"+guildLeader);
                                            }

                                        } else if(respondType.equalsIgnoreCase("decline")) {

                                            XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+target.getServer().getInfo().getName(),
                                                    XSDATA_TYPE.INVITE_RESPOND_RETURN+"<SPLIT>DECLINE;"+player); //send to player that decline

                                            if(guildLeader != null && guildLeader.isConnected()) { //respond to leader
                                                XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+guildLeader.getServer().getInfo().getName(),
                                                        XSDATA_TYPE.INVITE_RESPOND_RETURN+"<SPLIT>DECLINE_FROM;"+player+";"+guildLeader);
                                            }
                                        }
                                        xsGuilds.getPendingInvite().remove(player);
                                        XSGuildsHandler.updateToAllServer(xsGuilds);

                                    }
                                }
                            } else if(type.equalsIgnoreCase(XSDATA_TYPE.LEAVE_GUILD.toString())) {
                                String player = arguments.split(";")[0];
                                String guild = arguments.split(";")[1];

                                XSGuildsHandler.getPlayers().remove(player);
                                XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);
                                xsGuilds.getMembers().remove(player);
                                if(xsGuilds.getClanmates().contains(player)) {
                                    xsGuilds.getClanmates().remove(player);
                                }

                                if(XSHandler.getPlayerInGuildChat().contains(player)) {
                                    XSHandler.getPlayerInGuildChat().remove(player);
                                }

                                XSGuildsHandler.updateToAllServer(xsGuilds);
                            } else if(type.equalsIgnoreCase(XSDATA_TYPE.KICK_REQUEST.toString())) {
                                String guild = arguments.split(";")[0];
                                String player = arguments.split(";")[1];

                                XSGuildsHandler.getPlayers().remove(player);
                                XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);
                                xsGuilds.getMembers().remove(player);
                                if(XSHandler.getPlayerInGuildChat().contains(player)) {
                                    XSHandler.getPlayerInGuildChat().remove(player);
                                }
                                if(xsGuilds.getClanmates().contains(player)) {
                                    xsGuilds.getClanmates().remove(player);
                                }
                                XSGuildsHandler.updateToAllServer(xsGuilds);

                                ProxiedPlayer target = core.getPlugin().getProxy().getPlayer(player);
                                if(target != null && target.isConnected()) { //respond to target
                                    XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+target.getServer().getInfo().getName(),
                                            XSDATA_TYPE.KICK_RESPOND+"<SPLIT>"+player);
                                }
                            } else if(type.equalsIgnoreCase(XSDATA_TYPE.TRANSFER_LEADER_REQUEST.toString())) {
                                String guild = arguments.split(";")[0];
                                String player = arguments.split(";")[1];

                                XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);

                                String leader = xsGuilds.getLeader();
                                xsGuilds.setLeader(player);
                                xsGuilds.getMembers().put(player, XSGUILD_POSITIONS.LEADER.toString());
                                xsGuilds.getMembers().put(leader, XSGUILD_POSITIONS.MEMBER.toString());
                                xsGuilds.getClanmates().add(leader);
                                if(xsGuilds.getClanmates().contains(player)) {
                                    xsGuilds.getClanmates().remove(player);
                                }

                                XSGuildsHandler.updateToAllServer(xsGuilds);

                                ProxiedPlayer target = core.getPlugin().getProxy().getPlayer(player);
                                if(target != null && target.isConnected()) { //respond to target
                                    XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+target.getServer().getInfo().getName(),
                                            XSDATA_TYPE.TRANSFER_LEADER_RESPOND+"<SPLIT>"+player);
                                }
                            } else if(type.equalsIgnoreCase(XSDATA_TYPE.PROMOTE_REQUEST.toString())) {
                                String guild = arguments.split(";")[0];
                                String player = arguments.split(";")[1];
                                String newRank = arguments.split(";")[2].toUpperCase();

                                XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);
                                if(newRank.equalsIgnoreCase(XSGUILD_POSITIONS.SUB_LEADER.toString())) {
                                    xsGuilds.getSubleader().add(player);
                                }
                                xsGuilds.getMembers().put(player,newRank);
                                if(newRank.equalsIgnoreCase("SUB_LEADER")) {
                                    if(xsGuilds.getClanmates().contains(player)) {
                                        xsGuilds.getClanmates().remove(player);
                                    }
                                }

                                XSGuildsHandler.updateToAllServer(xsGuilds);

                                ProxiedPlayer target = core.getPlugin().getProxy().getPlayer(player);
                                if(target != null && target.isConnected()) { //respond to target
                                    XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+target.getServer().getInfo().getName(),
                                            XSDATA_TYPE.PROMOTE_RESPOND+"<SPLIT>"+player+";"+newRank);
                                }

                            } else if(type.equalsIgnoreCase(XSDATA_TYPE.DEMOTE_REQUEST.toString())) {
                                String guild = arguments.split(";")[0];
                                String player = arguments.split(";")[1];
                                String newRank = arguments.split(";")[2].toUpperCase();

                                XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);
                                if(xsGuilds.getSubleader().contains(player)) {
                                    xsGuilds.getSubleader().remove(player);
                                }
                                xsGuilds.getMembers().put(player,newRank);

                                if(newRank.equalsIgnoreCase("MEMBER") || newRank.equalsIgnoreCase("NEW_MEMBER")) {
                                    if(!xsGuilds.getClanmates().contains(player)) {
                                        xsGuilds.getClanmates().add(player);
                                    }
                                }

                                XSGuildsHandler.updateToAllServer(xsGuilds);

                                ProxiedPlayer target = core.getPlugin().getProxy().getPlayer(player);
                                if(target != null && target.isConnected()) { //respond to target
                                    XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+target.getServer().getInfo().getName(),
                                            XSDATA_TYPE.DEMOTE_RESPOND+"<SPLIT>"+player+";"+newRank);
                                }
                            } else if(type.equalsIgnoreCase(XSDATA_TYPE.DEPOSIT_COINS.toString())) {
                                String server = arguments.split(";")[0];
                                String guild = arguments.split(";")[1];
                                double amount = Double.parseDouble(arguments.split(";")[2]);

                                XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);
                                XSSubGuilds xsSubGuilds = xsGuilds.getSubGuilds().get(server);
                                xsSubGuilds.setBalance(xsSubGuilds.getBalance()+amount);
                                XSGuildsHandler.updateToAllServer(xsGuilds);
                            } else if(type.equalsIgnoreCase(XSDATA_TYPE.DEPOSIT_POINTS.toString())) {
                                String guild = arguments.split(";")[0];
                                double amount = Double.parseDouble(arguments.split(";")[1]);
                                XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);
                                xsGuilds.setBalance(xsGuilds.getBalance()+amount);
                                //core.getPlugin().getLogger().info("DEPOSIT CURRENT BALANCE: " + xsGuilds.getBalance());
                                XSGuildsHandler.updateToAllServer(xsGuilds);
                            } else if(type.equalsIgnoreCase(XSDATA_TYPE.WITHDRAW_POINTS.toString())) {
                                String guild = arguments.split(";")[0];
                                double amount = Double.parseDouble(arguments.split(";")[1]);
                                String player = arguments.split(";")[2];
                                ProxiedPlayer target = core.getPlugin().getProxy().getPlayer(player);

                                if(target != null && target.isConnected()) { //respond to target
                                    XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);
                                    xsGuilds.setBalance(xsGuilds.getBalance()-amount);
                                    XSGuildsHandler.updateToAllServer(xsGuilds);
                                    XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+target.getServer().getInfo().getName(),
                                            XSDATA_TYPE.WITHDRAW_POINTS_RESPOND+"<SPLIT>"+player+";"+amount);
                                }
                            } else if(type.equalsIgnoreCase(XSDATA_TYPE.WITHDRAW_POINTS.toString())) {
                                String server = arguments.split(";")[0];
                                String guild = arguments.split(";")[1];
                                double amount = Double.parseDouble(arguments.split(";")[2]);
                                String player = arguments.split(";")[3];
                                ProxiedPlayer target = core.getPlugin().getProxy().getPlayer(player);

                                if(target != null && target.isConnected()) { //respond to target
                                    XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);
                                    XSSubGuilds xsSubGuilds = xsGuilds.getSubGuilds().get(server);
                                    xsSubGuilds.setBalance(xsSubGuilds.getBalance()-amount);

                                    XSGuildsHandler.updateToAllServer(xsGuilds);
                                    XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+target.getServer().getInfo().getName(),
                                            XSDATA_TYPE.WITHDRAW_COINS_RESPOND+"<SPLIT>"+player+";"+amount);
                                }
                            } else if(type.equalsIgnoreCase(XSDATA_TYPE.UNINVITE.toString())) {
                                String guild = arguments.split(";")[0];
                                String player = arguments.split(";")[1];

                                XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);
                                xsGuilds.getPendingInvite().remove(player);
                                XSGuildsHandler.updateToAllServer(xsGuilds);
                                ProxiedPlayer target = core.getPlugin().getProxy().getPlayer(player);
                                XSGuildsHandler.updateToAllServer(xsGuilds);

                                if(target != null && target.isConnected()) { //respond to target
                                    XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+target.getServer().getInfo().getName(),
                                            XSDATA_TYPE.UNINVITE_RESPOND+"<SPLIT>"+player+";"+xsGuilds.getGuildRealName());
                                }
                            } else if(type.equalsIgnoreCase(XSDATA_TYPE.GUILD_MESSAGE_SENT.toString())) {
                                String guild = arguments.split(";")[0];
                                String msg = arguments.split(";")[1];

                                for(String servers : mainConfig.getConfig().getSection("guilds-group").getKeys()) {
                                    for(String subServer : mainConfig.getConfig().getStringList("guilds-group." + servers)) {
                                        XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+subServer,XSDATA_TYPE.GUILD_MESSAGE_RESPOND+"<SPLIT>"+guild+";"+msg);
                                    }
                                }
                            } else if(type.equalsIgnoreCase(XSDATA_TYPE.GUILD_CHAT_CHANGE_STATE.toString())) {
                                String player = arguments.split(";")[0];
                                String typeArg = arguments.split(";")[1];

                                if(typeArg.equalsIgnoreCase("YES")) {
                                    XSHandler.getPlayerInGuildChat().add(player);
                                } else {
                                    XSHandler.getPlayerInGuildChat().remove(player);
                                }
                            } else if(type.equalsIgnoreCase(XSDATA_TYPE.SETHOME.toString())) {
                                //[HOME_NAME:SERVER:WORLD:LOC_X:LOC_Y:LOC_Z:YAW:PITCH,]
                                String guild = arguments.split(";")[0];
                                String serverGuild = arguments.split(";")[1];
                                String homeName = arguments.split(";")[2];
                                String serverLocation = arguments.split(";")[3];
                                String world = arguments.split(";")[4];
                                String locX = arguments.split(";")[5];
                                String locY = arguments.split(";")[6];
                                String locZ = arguments.split(";")[7];
                                String yaw = arguments.split(";")[8];
                                String pitch = arguments.split(";")[9];

                                XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);
                                XSSubGuilds xsSubGuilds = xsGuilds.getSubGuilds().get(serverGuild);

                                String homeData = homeName+":"+serverLocation+":"+world+":"+locX+":"+locY+":"+locZ+":"+yaw+":"+pitch;
                                xsSubGuilds.getHomeList().put(homeName,homeData);
                                XSGuildsHandler.updateToAllServer(xsGuilds);
                            } else if(type.equalsIgnoreCase(XSDATA_TYPE.DELHOME.toString())) {
                                String serverGuild = arguments.split(";")[0];
                                String guild = arguments.split(";")[1];
                                String homeName = arguments.split(";")[2];

                                XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);
                                XSSubGuilds xsSubGuilds = xsGuilds.getSubGuilds().get(serverGuild);
                                xsSubGuilds.getHomeList().remove(homeName);
                                XSGuildsHandler.updateToAllServer(xsGuilds);
                            } else if(type.equalsIgnoreCase(XSDATA_TYPE.UPGRADE_REQ.toString())) {
                                String server = arguments.split(";")[0];
                                String guild = arguments.split(";")[1];
                                String level = arguments.split(";")[2];
                                XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);
                                XSSubGuilds xsSubGuilds = xsGuilds.getSubGuilds().get(server);

                                double reqPoints = XSHandler.subClanUpgrades.get(Integer.parseInt(level)).getPricePoints();
                                double reqCoins = XSHandler.subClanUpgrades.get(Integer.parseInt(level)).getPriceCoins();
                                xsSubGuilds.setBalance(xsSubGuilds.getBalance()-reqCoins);
                                xsGuilds.setBalance(xsGuilds.getBalance()-reqPoints);

                                int newLvl = Integer.parseInt(level);
                                xsSubGuilds.setLevel(newLvl);
                                xsSubGuilds.setMaxHome(Integer.parseInt(XSHandler.subClanUpgrades.get(newLvl).getNextUpgrades().get("HOME")));
                                xsSubGuilds.setMaxBalance(Integer.parseInt(XSHandler.subClanUpgrades.get(newLvl).getNextUpgrades().get("BANK_CAPACITY")));
                                XSGuildsHandler.updateToAllServer(xsGuilds);
                                for(String subServer : mainConfig.getConfig().getStringList("guilds-group." + server)) {
                                    XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+subServer,XSDATA_TYPE.UPGRADE_RES+"<SPLIT>"+guild+";"+newLvl);
                                }
                                //XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+,XSDATA_TYPE.TELEPORT_TO_HOME+"<SPLIT>"+server+";"+guild+";"+homeN+";"+player);
                            } else if(type.equalsIgnoreCase(XSDATA_TYPE.UPGRADE_MAIN_REQ.toString())) {
                                String guild = arguments.split(";")[0];
                                String level = arguments.split(";")[1];
                                XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);
                                double reqPoints = XSHandler.mainClanUpgrades.get(Integer.parseInt(level)).getPricePoints();
                                xsGuilds.setBalance(xsGuilds.getBalance()-reqPoints);
                                int newLvl = Integer.parseInt(level);
                                xsGuilds.setGuildLevel(newLvl);
                                xsGuilds.setMaxMembers(Integer.parseInt(XSHandler.mainClanUpgrades.get(newLvl).getNextUpgrades().get("MEMBERS")));
                                xsGuilds.setMaxBalance(Integer.parseInt(XSHandler.mainClanUpgrades.get(newLvl).getNextUpgrades().get("BANK_CAPACITY")));
                                XSGuildsHandler.updateToAllServer(xsGuilds);
                                for(String servers : mainConfig.getConfig().getSection("guilds-group").getKeys()) {
                                    for(String subServer : mainConfig.getConfig().getStringList("guilds-group." + servers)) {
                                        XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+subServer,XSDATA_TYPE.UPGRADE_MAIN_RES+"<SPLIT>"+guild+";"+newLvl);
                                    }
                                }
                            } else if(type.equalsIgnoreCase(XSDATA_TYPE.UPDATE_PERMISSION.toString())) {

                                String guild = arguments.split(";")[0];
                                String perms = arguments.split(";")[1];
                                Gson gson = new Gson();
                                HashMap<String,HashMap<String,Boolean>> permData = gson.fromJson(perms, new TypeToken<HashMap<String,HashMap<String,Boolean>>>(){}.getType());
                                XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);
                                xsGuilds.setPermission(permData);
                                XSGuildsHandler.updateToAllServer(xsGuilds);
                            }

                        }
                    }
                };
                jedis.subscribe(jedisPubSub, channelName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
        threads.add(thread);
    }

    public static void sendRedisMessage(String CHName, String message) {

        new Thread(() -> {
            try (Jedis jedis = new Jedis(getHostRedis(), getRedisPort())) {
                if(!getRedisPass().isEmpty()) {
                    jedis.auth(getRedisPass());
                }
                jedis.publish(CHName, message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void destroyThreads() {
        for(Thread thread : threads) {
            thread.interrupt();
        }
    }
}
