package net.xsapi.panat.xsguildbungee.handler;

import net.xsapi.panat.xsguildbungee.config.mainConfig;
import net.xsapi.panat.xsguildbungee.core;
import net.xsapi.panat.xsguildbungee.listener.playerSwitch;
import net.xsapi.panat.xsguildbungee.objects.XSGuilds;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class XSHandler {

    private static String subChannel = "xsguilds:channel";
    private static ArrayList<String> playerInGuildChat = new ArrayList<>();

    public static String getSubChannel() {
        return subChannel;
    }


    public static ArrayList<String> getPlayerInGuildChat() {
        return playerInGuildChat;
    }
    public static void loadEvent() {
        core.getPlugin().getProxy().getPluginManager().registerListener(core.getPlugin(),new playerSwitch());
    }

    public static void subChannel() {
        /*for(String server : mainConfig.getConfig().getSection("guilds-group").getKeys()) {
            for(String subserver : mainConfig.getConfig().getStringList("guilds-group." + server)) {
                XSRedisHandler.subscribeToChannelAsync(getSubChannel() + "_" + subserver);
            }
        }*/
        XSRedisHandler.subscribeToChannelAsync(getSubChannel() + "_bungeecord");
    }

    public static void forceSaved() {
        try {
            Connection connection = DriverManager.getConnection(XSDatabaseHandler.getJdbcUrl(),XSDatabaseHandler.getUSER(),XSDatabaseHandler.getPASS());
            for(Map.Entry<String, XSGuilds> guild : XSGuildsHandler.getGuildList().entrySet()) {
               // core.getPlugin().getLogger().info("GUILD: " + guild.getKey() + " BAL " + guild.getValue().getBalance());
                XSDatabaseHandler.updateMainGuild(connection,guild.getValue());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void initSystem() {
        XSRedisHandler.redisConnection();
        loadEvent();
        subChannel();
    }



}
