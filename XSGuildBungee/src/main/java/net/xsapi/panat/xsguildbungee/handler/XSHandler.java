package net.xsapi.panat.xsguildbungee.handler;

import net.xsapi.panat.xsguildbungee.config.mainConfig;
import net.xsapi.panat.xsguildbungee.core;
import net.xsapi.panat.xsguildbungee.listener.playerSwitch;

import java.util.HashMap;

public class XSHandler {

    private static String subChannel = "xsguilds:channel";

    public static String getSubChannel() {
        return subChannel;
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

    public static void initSystem() {
        XSRedisHandler.redisConnection();
        loadEvent();
        subChannel();
    }

}
