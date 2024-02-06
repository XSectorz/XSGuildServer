package net.xsapi.panat.xsguildclient.handler;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.xsapi.panat.xsguildclient.commands.commandsLoader;
import net.xsapi.panat.xsguildclient.config.configLoader;
import net.xsapi.panat.xsguildclient.core;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class XSHandler {

    private static String subChannel = "xsguilds:channel";


    public static String getSubChannel() {
        return subChannel;
    }

    public static void subChannel() {
        XSRedisHandler.subscribeToChannelAsync(getSubChannel());
    }

    public static void initSystem() {
        new configLoader();
        new commandsLoader();
        XSRedisHandler.redisConnection();
        subChannel();
    }


}
