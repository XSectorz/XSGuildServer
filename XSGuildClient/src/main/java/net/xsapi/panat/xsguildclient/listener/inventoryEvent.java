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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.HashMap;

public class inventoryEvent implements Listener {

    public void handleClick(ClickType clickType,Player p,int slot) {
        HashMap<Integer, String> data = new HashMap<>();

        if(clickType.equals(ClickType.LEFT)) {
            data = XSMenuHandler.getLeftActionClicked().get(p);
        } else if(clickType.equals(ClickType.RIGHT)) {
            data = XSMenuHandler.getRightActionClicked().get(p);
        }

        if(data.containsKey(slot)) {
            String action = data.get(slot);
            if(action.equalsIgnoreCase("close")) {
                p.closeInventory();
            } else if(action.startsWith("upgradeGuild")) {
                String type = action.split(":")[1];

                double reqCoins = 0;
                double reqPoints = 0;
                double havePoints = 0;
                double haveCoins = 0;

                String guild = XSGuildsHandler.getPlayers().get(p.getName()).split("<SPLIT>")[1];
                XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);
                String server = XSGuildsHandler.getPlayers().get(p.getName()).split("<SPLIT>")[0];
                XSSubGuilds xsSubGuilds = xsGuilds.getSubGuilds().get(server);
                int nextLvl = 0;

                if(type.equalsIgnoreCase("sub")) {
                    if(!XSHandler.getSubClanUpgrades().containsKey(xsGuilds.getSubGuilds().get(server).getLevel()+1)) {
                        p.sendMessage(XSUtils.decodeTextFromConfig("upgrade_max"));
                        return;
                    }
                    reqCoins = XSHandler.getSubClanUpgrades().get(xsGuilds.getSubGuilds().get(server).getLevel()+1).getPriceCoins();
                    reqPoints = XSHandler.getSubClanUpgrades().get(xsGuilds.getSubGuilds().get(server).getLevel()+1).getPricePoints();
                    havePoints = xsGuilds.getBalance();
                    haveCoins = xsSubGuilds.getBalance();
                } else if(type.equalsIgnoreCase("main")) {
                    if(!XSHandler.getMainClanUpgrades().containsKey(xsGuilds.getGuildLevel()+1)) {
                        p.sendMessage(XSUtils.decodeTextFromConfig("upgrade_max"));
                        return;
                    }
                    reqPoints = XSHandler.getMainClanUpgrades().get(xsGuilds.getGuildLevel()+1).getPricePoints();
                    havePoints = xsGuilds.getBalance();
                }


                if(havePoints < reqPoints || haveCoins < reqCoins) {
                    p.sendMessage(XSUtils.decodeTextFromConfig("upgrade_resource_not_match"));
                    return;
                }

                if(type.equalsIgnoreCase("sub")) {
                    nextLvl = xsSubGuilds.getLevel()+1;
                    XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+"_bungeecord", XSDATA_TYPE.UPGRADE_REQ+"<SPLIT>"+server+";"+guild+";"+nextLvl);
                } else if (type.equalsIgnoreCase("main")) {
                    nextLvl = xsGuilds.getGuildLevel()+1;
                    XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+"_bungeecord", XSDATA_TYPE.UPGRADE_MAIN_REQ+"<SPLIT>"+guild+";"+nextLvl);
                }
                p.closeInventory();

            } else if(action.equalsIgnoreCase("upgrade:menu")) {
                String guild = XSGuildsHandler.getPlayers().get(p.getName()).split("<SPLIT>")[1];
                XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);
                XSMenuHandler.openMenu(p, XS_FILE.UPGRADE_MENU,xsGuilds);
            } else if(action.equalsIgnoreCase("upgrade:mainmenu")) {
                String guild = XSGuildsHandler.getPlayers().get(p.getName()).split("<SPLIT>")[1];
                XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);
                XSMenuHandler.openMenu(p, XS_FILE.UPGRADE_MAIN_MENU,xsGuilds);
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if(e.getView().getTitle().equalsIgnoreCase(XSUtils.decodeText(menuConfig.getConfig(XS_FILE.MAIN_MENU).getString("configuration.title")))
        || e.getView().getTitle().equalsIgnoreCase(XSUtils.decodeText(menuConfig.getConfig(XS_FILE.UPGRADE_MENU).getString("configuration.title")))
        || e.getView().getTitle().equalsIgnoreCase(XSUtils.decodeText(menuConfig.getConfig(XS_FILE.UPGRADE_MAIN_MENU).getString("configuration.title")))) {

           /* for(Map.Entry<Integer,String> val : XSMenuHandler.getActionClicked().get(p).entrySet()) {
                Bukkit.broadcastMessage(val.getKey() + "; " + val.getValue());
            }*/
            e.setCancelled(true);
            if(!XSMenuHandler.getLeftActionClicked().containsKey(p)) {
                p.closeInventory();
                return;
            }

            handleClick(e.getClick(),p,e.getSlot());

        }
    }
}
