package net.xsapi.panat.xsguildclient.handler;

import net.xsapi.panat.xsguildclient.config.mainConfig;
import net.xsapi.panat.xsguildclient.core;
import net.xsapi.panat.xsguildclient.utils.XSDATA_TYPE;
import org.bukkit.Bukkit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;

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
