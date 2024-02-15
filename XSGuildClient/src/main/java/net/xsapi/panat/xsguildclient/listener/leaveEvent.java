package net.xsapi.panat.xsguildclient.listener;

import net.xsapi.panat.xsguildclient.handler.XSGuildsHandler;
import net.xsapi.panat.xsguildclient.handler.XSHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class leaveEvent implements Listener {

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        if(XSGuildsHandler.getPlayers().containsKey(p.getName())) {
            XSGuildsHandler.getPlayers().remove(p.getName());
        }

        if(XSHandler.getPlayerInGuildChat().contains(p.getName())) {
            XSHandler.getPlayerInGuildChat().remove(p.getName());
        }
    }
}
