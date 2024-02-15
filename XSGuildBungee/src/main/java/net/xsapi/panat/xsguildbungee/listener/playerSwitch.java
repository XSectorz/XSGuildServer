package net.xsapi.panat.xsguildbungee.listener;

import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.xsapi.panat.xsguildbungee.config.mainConfig;
import net.xsapi.panat.xsguildbungee.core;
import net.xsapi.panat.xsguildbungee.handler.XSHandler;
import net.xsapi.panat.xsguildbungee.handler.XSRedisHandler;

public class playerSwitch implements Listener {

    @EventHandler
    public void onJoinSwitch(ServerConnectEvent e) {
        /*for(String sublist : mainConfig.getConfig().getSection("guilds-group").getKeys()) {
            if(mainConfig.getConfig().getStringList("guilds-group." + sublist).contains(e.getTarget().getName())) {
                XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+e.getTarget().getName(),"LOAD_DATA:" + e.getPlayer().getName() + ";" + "GUILD");
                break;
            }
        }*/

    }
}
