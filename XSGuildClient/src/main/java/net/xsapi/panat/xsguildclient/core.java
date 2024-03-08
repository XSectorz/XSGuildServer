package net.xsapi.panat.xsguildclient;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.xsapi.panat.xsguildclient.handler.XSHandler;
import net.xsapi.panat.xsguildclient.handler.XSRedisHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

public final class core extends JavaPlugin {

    private static core plugin;

    public static core getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        Bukkit.broadcastMessage("[Client] Enabled!");

        plugin = this;

        XSHandler.initSystem();
        XSHandler.loadOnlinePlayerData();
        XSHandler.APILoader();
       // getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    @Override
    public void onDisable() {
        //getServer().getMessenger().unregisterIncomingPluginChannel(this);
        XSRedisHandler.destroyThreads();
        XSHandler.unregisterPlaceholder();
    }

}
