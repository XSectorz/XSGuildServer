package net.xsapi.panat.xsguildclient.listener;

import net.xsapi.panat.xsguildclient.config.menuConfig;
import net.xsapi.panat.xsguildclient.handler.XSGuildsHandler;
import net.xsapi.panat.xsguildclient.handler.XSHandler;
import net.xsapi.panat.xsguildclient.handler.XSMenuHandler;
import net.xsapi.panat.xsguildclient.handler.XSRedisHandler;
import net.xsapi.panat.xsguildclient.objects.XSGuilds;
import net.xsapi.panat.xsguildclient.objects.XSSubGuilds;
import net.xsapi.panat.xsguildclient.utils.XSDATA_TYPE;
import net.xsapi.panat.xsguildclient.utils.XSUtils;
import net.xsapi.panat.xsguildclient.utils.XS_FILE;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Map;

public class inventoryEvent implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if(e.getView().getTitle().equalsIgnoreCase(XSUtils.decodeText(menuConfig.getConfig(XS_FILE.MAIN_MENU).getString("configuration.title")))
        || e.getView().getTitle().equalsIgnoreCase(XSUtils.decodeText(menuConfig.getConfig(XS_FILE.UPGRADE_MENU).getString("configuration.title")))) {

           /* for(Map.Entry<Integer,String> val : XSMenuHandler.getActionClicked().get(p).entrySet()) {
                Bukkit.broadcastMessage(val.getKey() + "; " + val.getValue());
            }*/
            e.setCancelled(true);
            if(!XSMenuHandler.getActionClicked().containsKey(p)) {
                p.closeInventory();
                return;
            }

            if(XSMenuHandler.getActionClicked().get(p).containsKey(e.getSlot())) {
                String action = XSMenuHandler.getActionClicked().get(p).get(e.getSlot());
                if(action.equalsIgnoreCase("close")) {
                    p.closeInventory();
                } else if(action.equalsIgnoreCase("upgrade")) {
                    String guild = XSGuildsHandler.getPlayers().get(p.getName()).split("<SPLIT>")[1];
                    XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);
                    String server = XSGuildsHandler.getPlayers().get(p.getName()).split("<SPLIT>")[0];
                    XSSubGuilds xsSubGuilds = xsGuilds.getSubGuilds().get(server);

                    if(!XSHandler.getSubClanUpgrades().containsKey(xsGuilds.getSubGuilds().get(server).getLevel()+1)) {
                        p.sendMessage(XSUtils.decodeTextFromConfig("upgrade_max"));
                        return;
                    }

                    double reqCoins = XSHandler.getSubClanUpgrades().get(xsGuilds.getSubGuilds().get(server).getLevel()+1).getPriceCoins();
                    double reqPoints = XSHandler.getSubClanUpgrades().get(xsGuilds.getSubGuilds().get(server).getLevel()+1).getPricePoints();
                    double havePoints = xsGuilds.getBalance();
                    double haveCoins = xsSubGuilds.getBalance();

                    if(havePoints < reqPoints || haveCoins < reqCoins) {
                        p.sendMessage(XSUtils.decodeTextFromConfig("upgrade_resource_not_match"));
                        return;
                    }
                    int nextLvl = xsSubGuilds.getLevel()+1;
                    p.closeInventory();
                    XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+"_bungeecord", XSDATA_TYPE.UPGRADE_REQ+"<SPLIT>"+server+";"+guild+";"+nextLvl);
                } else if(action.equalsIgnoreCase("upgrade:menu")) {
                    String guild = XSGuildsHandler.getPlayers().get(p.getName()).split("<SPLIT>")[1];
                    XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);
                    XSMenuHandler.openMenu(p, XS_FILE.UPGRADE_MENU,xsGuilds);
                }

            }

        }
    }
}
