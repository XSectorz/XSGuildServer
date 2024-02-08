package net.xsapi.panat.xsguildbungee.handler;

import com.google.gson.Gson;
import net.xsapi.panat.xsguildbungee.config.mainConfig;
import net.xsapi.panat.xsguildbungee.core;
import net.xsapi.panat.xsguildbungee.utils.XSDATA_TYPE;
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


                            core.getPlugin().getLogger().info("[bungeecord] GET MESSAGE " + message);

                            String type = message.split("<SPLIT>")[0];
                            String arguments = message.split("<SPLIT>")[1];

                            if(type.equalsIgnoreCase(XSDATA_TYPE.CREATE.toString())) {
                                String leader = arguments.split(";")[0];
                                String guildRealName = arguments.split(";")[1];
                                String guildName = arguments.split(";")[2];
                                XSDatabaseHandler.createGuild(guildRealName,guildName,leader);
                                 //core.getPlugin().getLogger().info("Create guild " + guildName);
                            } else if(type.equalsIgnoreCase(XSDATA_TYPE.REQ_DATA.toString())) {
                                String server = arguments.split(";")[0];
                                String players = arguments.split(";")[1];

                                for(String servers : mainConfig.getConfig().getSection("guilds-group").getKeys()) {
                                    if(mainConfig.getConfig().getStringList("guilds-group." + servers).contains(server)) {
                                        XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+server,XSDATA_TYPE.LOAD_DATA+"<SPLIT>" + players + ";" + servers + ";GUILD");
                                    }
                                }
                            } else  if(type.equalsIgnoreCase(XSDATA_TYPE.REQ_GUILD.toString())) {
                                String server = arguments.split(";")[0];
                                Gson gson = new Gson();

                                String guildJson = gson.toJson(XSGuildsHandler.getGuildList());
                                core.getPlugin().getLogger().info(guildJson);
                                XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+server,XSDATA_TYPE.GET_GUILD+"<SPLIT>" + guildJson);
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
