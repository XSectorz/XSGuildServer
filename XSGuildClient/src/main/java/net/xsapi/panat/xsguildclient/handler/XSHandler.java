package net.xsapi.panat.xsguildclient.handler;

import net.xsapi.panat.xsguildclient.commands.commandsLoader;
import net.xsapi.panat.xsguildclient.config.configLoader;
import net.xsapi.panat.xsguildclient.config.mainConfig;
import net.xsapi.panat.xsguildclient.core;
import net.xsapi.panat.xsguildclient.listener.joinEvent;
import net.xsapi.panat.xsguildclient.utils.XSDATA_TYPE;
import org.bukkit.Bukkit;


public class XSHandler {

    private static String subChannel = "xsguilds:channel";
    private static String servername = "";


    public static String getSubChannel() {
        return subChannel;
    }

    public static String getServername() {
        return servername;
    }

    private static void subChannel() {
        servername = mainConfig.customConfig.getString("configuration.server");
        XSRedisHandler.subscribeToChannelAsync(getSubChannel()+getServername());
    }

    private static void loadEvent() {
        Bukkit.getPluginManager().registerEvents(new joinEvent(),core.getPlugin());
    }

    public static void initSystem() {
        new configLoader();
        new commandsLoader();
        XSRedisHandler.redisConnection();
        subChannel();
        loadEvent();
        loadGuildData();
    }

    public static void loadGuildData() {
        XSRedisHandler.sendRedisMessage(getSubChannel()+"_bungeecord", XSDATA_TYPE.REQ_GUILD+"<SPLIT>" + getServername());
    }


}
