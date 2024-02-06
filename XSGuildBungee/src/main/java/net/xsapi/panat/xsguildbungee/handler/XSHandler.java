package net.xsapi.panat.xsguildbungee.handler;

import net.xsapi.panat.xsguildbungee.core;
import net.xsapi.panat.xsguildbungee.listener.playerSwitch;

public class XSHandler {

    private static String subChannel = "xsguilds:channel";

    public static String getSubChannel() {
        return subChannel;
    }

    public static void loadEvent() {
        core.getPlugin().getProxy().getPluginManager().registerListener(core.getPlugin(),new playerSwitch());
    }

    public static void subChannel() {
        XSRedisHandler.subscribeToChannelAsync(getSubChannel());
    }

    public static void initSystem() {
        XSRedisHandler.redisConnection();
        loadEvent();
        subChannel();
    }

}
