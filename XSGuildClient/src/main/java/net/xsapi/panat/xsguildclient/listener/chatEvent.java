package net.xsapi.panat.xsguildclient.listener;

import net.xsapi.panat.xsguildclient.config.mainConfig;
import net.xsapi.panat.xsguildclient.config.messagesConfig;
import net.xsapi.panat.xsguildclient.handler.XSGuildsHandler;
import net.xsapi.panat.xsguildclient.handler.XSHandler;
import net.xsapi.panat.xsguildclient.handler.XSRedisHandler;
import net.xsapi.panat.xsguildclient.objects.XSGuilds;
import net.xsapi.panat.xsguildclient.utils.XSDATA_TYPE;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class chatEvent implements Listener {
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        String message = e.getMessage();
        Player p = e.getPlayer();
        if(XSHandler.getPlayerInGuildChat().contains(p.getName())) {
            message = message.replaceAll("<[^<>]*>", "");
            String guild = XSGuildsHandler.getPlayers().get(p.getName()).split("<SPLIT>")[1];
            XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);

            String position = xsGuilds.getMembers().get(p.getName()).toLowerCase();
            String positionText = messagesConfig.customConfig.getString("system.ranks." + position);

            String formatChat = mainConfig.customConfig.getString("configuration.guild_messages.default");

            formatChat = formatChat.replace("%guild_position%",positionText);
            formatChat = formatChat.replace("%player_name%",p.getName());
            formatChat = formatChat.replace("%message%",message);

            XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+"_bungeecord",XSDATA_TYPE.GUILD_MESSAGE_SENT+"<SPLIT>"+guild+";"+formatChat);
            e.setCancelled(true);
        }
    }
}
