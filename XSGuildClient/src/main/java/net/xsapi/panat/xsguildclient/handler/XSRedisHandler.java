package net.xsapi.panat.xsguildclient.handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.xsapi.panat.xsguildclient.config.mainConfig;
import net.xsapi.panat.xsguildclient.config.messagesConfig;
import net.xsapi.panat.xsguildclient.core;
import net.xsapi.panat.xsguildclient.objects.XSGuilds;
import net.xsapi.panat.xsguildclient.utils.XSDATA_TYPE;
import net.xsapi.panat.xsguildclient.utils.XSUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;
import java.util.HashMap;

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
        redisHost = mainConfig.customConfig.getString("redis.host");
        redisPort = mainConfig.customConfig.getInt("redis.port");
        redisPass = mainConfig.customConfig.getString("redis.password");

        try {
            Jedis jedis = new Jedis(getHostRedis(), getRedisPort());
            if(!getRedisPass().isEmpty()) {
                jedis.auth(getRedisPass());
            }
            jedis.close();
            core.getPlugin().getLogger().info("[XSGUILDS] Redis Server : Connected");
        } catch (Exception e) {
            core.getPlugin().getLogger().info("[XSGUILDS] Redis Server : Not Connected");
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

                        if(channel.equalsIgnoreCase(XSHandler.getSubChannel()+XSHandler.getServername())) {
                            core.getPlugin().getLogger().info("GET MESSAGE " + message);
                            String type = message.split("<SPLIT>")[0];
                            String arguments = message.split("<SPLIT>")[1];

                            if(type.equalsIgnoreCase(XSDATA_TYPE.LOAD_DATA.toString())) { //player + ";" + servers + ";" + guild
                                //Bukkit.broadcastMessage(arguments.split(";")[0]);
                                String player = arguments.split(";")[0];
                                if(arguments.split(";").length == 2) { //NOT HAVE GUILD
                                    Bukkit.broadcastMessage("PLAYER: " + player + " NOT HAVE GUILD" );
                                } else {
                                    //XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+"_bungeecord",XSDATA_TYPE.DEBUG+"<SPLIT>Recieved data from " + XSHandler.getServername());
                                    String server = arguments.split(";")[1];
                                    String guild = arguments.split(";")[2];
                                    Bukkit.broadcastMessage("PLAYER: " + player + " SERVER-> " + server + " GUILD-> " + guild + " TEST");
                                    XSGuildsHandler.getPlayers().put(player,server+"<SPLIT>"+guild);
                                }

                            } else if(type.equalsIgnoreCase(XSDATA_TYPE.GET_GUILD.toString())) {
                                XSGuildsHandler.loadGuildData(arguments);
                            } else if(type.equalsIgnoreCase(XSDATA_TYPE.UPDATED.toString())) {
                                String server = arguments.split(";")[0];
                                String jsonGuild = arguments.split(";")[1];
                                Gson gson = new Gson();
                                XSGuilds xsGuilds = gson.fromJson(jsonGuild, XSGuilds.class);
                                XSGuildsHandler.getGuildList().put(xsGuilds.getGuildRealName(),xsGuilds);
                                Bukkit.broadcastMessage("UPDATED GUILD DATA TO SERVER");
                                if(Bukkit.getPlayer(xsGuilds.getLeader()) != null) {
                                    XSGuildsHandler.getPlayers().put(xsGuilds.getLeader(),server+"<SPLIT>"+xsGuilds.getGuildRealName());
                                    Bukkit.broadcastMessage("PLAYER ONLINE UPDATED TO " + server+"<SPLIT>"+xsGuilds.getGuildRealName());
                                }
                            } else if(type.equalsIgnoreCase(XSDATA_TYPE.INVITE_RETURN.toString())) {
                                String result = arguments.split(";")[0];
                                String senderName = arguments.split(";")[1];
                                try {
                                    Player sender = Bukkit.getPlayer(senderName);
                                    assert sender != null;
                                    if(result.equalsIgnoreCase("NULL_PLAYER")) {
                                        sender.sendMessage(XSUtils.decodeTextFromConfig("null_player"));
                                    } else if(result.equalsIgnoreCase("SENT")) {
                                        String targetName = arguments.split(";")[2];
                                        sender.sendMessage(XSUtils.decodeTextFromConfig("invite_send").replace("%player_name%",targetName));
                                    } else if(result.equalsIgnoreCase("ALREADY_SENT")) {
                                        sender.sendMessage(XSUtils.decodeTextFromConfig("already_sent"));
                                    }
                                } catch (Exception ignored) {

                                }
                            } else if(type.equalsIgnoreCase(XSDATA_TYPE.INVITE_GET.toString())) {
                                String guild = arguments.split(";")[0];
                                String player = arguments.split(";")[1];
                                try {
                                    Player target = Bukkit.getPlayer(player);
                                    assert target != null;
                                    Audience targetAudience = (Audience) target;

                                    for(String msg : messagesConfig.customConfig.getStringList("system.get_invite")) {
                                        msg = msg.replace("%guild_name%",guild);
                                        targetAudience.sendMessage(MiniMessage.builder().build().deserialize(msg));
                                    }

                                } catch (Exception ignored) {

                                }
                            } else if(type.equalsIgnoreCase(XSDATA_TYPE.INVITE_RESPOND_RETURN.toString())) {
                                String typeResond = arguments.split(";")[0];
                                String player = arguments.split(";")[1];

                                if(typeResond.equalsIgnoreCase("DECLINE_FROM")) {
                                    String leader = arguments.split(";")[2];
                                    try {
                                        Player leaderTarget = Bukkit.getPlayer(leader);
                                        assert leaderTarget != null;
                                        leaderTarget.sendMessage(XSUtils.decodeTextFromConfig("decline_invite").replace("%player_name%",player));
                                    } catch (Exception ignored) {

                                    }
                                } else {
                                    try {
                                        Player target = Bukkit.getPlayer(player);
                                        assert target != null;
                                        if(typeResond.equalsIgnoreCase("GUILD_NULL")) {
                                            target.sendMessage(XSUtils.decodeTextFromConfig("guild_null"));
                                        } else if(typeResond.equalsIgnoreCase("GUILD_NOT_INVITE")) {
                                            target.sendMessage(XSUtils.decodeTextFromConfig("guild_not_invite"));
                                        } else if(typeResond.equalsIgnoreCase("DECLINE")) {
                                            target.sendMessage(XSUtils.decodeTextFromConfig("decline_guild"));
                                        }
                                    } catch (Exception ignored) {

                                    }
                                }
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
