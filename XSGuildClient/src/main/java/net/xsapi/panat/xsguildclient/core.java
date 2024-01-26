package net.xsapi.panat.xsguildclient;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.xsapi.panat.xsguildclient.handler.XSHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

public final class core extends JavaPlugin implements PluginMessageListener {

    private static core plugin;

    public static core getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        Bukkit.broadcastMessage("[Client] Enabled!");

        plugin = this;
    }

    @Override
    public void onDisable() {
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (channel.equals(XSHandler.getSubChannel())) {
            ByteArrayDataInput in = ByteStreams.newDataInput(message);
            String receivedString = in.readUTF();
            Bukkit.broadcastMessage(receivedString);
        }
    }
}
