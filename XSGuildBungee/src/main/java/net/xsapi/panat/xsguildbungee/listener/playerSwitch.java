package net.xsapi.panat.xsguildbungee.listener;

import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.xsapi.panat.xsguildbungee.core;

public class playerSwitch implements Listener {

    @EventHandler
    public void onJoinSwitch(ServerConnectEvent e) {
        core.getPlugin().getLogger().info("PLAYER : " + e.getPlayer() + " ---> " + e.getTarget());

    }
}
