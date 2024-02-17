package net.xsapi.panat.xsguildclient.handler;

import net.xsapi.panat.xsguildclient.config.menuConfig;
import net.xsapi.panat.xsguildclient.objects.XSGuilds;
import net.xsapi.panat.xsguildclient.utils.XSUtils;
import net.xsapi.panat.xsguildclient.utils.XS_FILE;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;

public class XSMenuHandler {

    private static HashMap<Player,HashMap<Integer, String>> actionClicked = new HashMap<>();

    public static HashMap<Player,HashMap<Integer, String>> getActionClicked() {
        return actionClicked;
    }

    public static void openMenu(Player p, XS_FILE xsFile, XSGuilds xsGuilds) {

        int size = 9 * menuConfig.getConfig(xsFile).getStringList("configuration.style").size();
        String title = XSUtils.decodeText(menuConfig.getConfig(xsFile).getString("configuration.title"));
        Inventory inv = Bukkit.createInventory(null, size, title);

        String server = XSGuildsHandler.getPlayers().get(p.getName()).split("<SPLIT>")[0];
        HashMap<String, ItemStack> items = new HashMap<>();
        getActionClicked().put(p,new HashMap<>());

        for(String item : menuConfig.getConfig(xsFile).getConfigurationSection("configuration.items").getKeys(false)) {
            ItemStack it = XSUtils.decodeItemFromConfig(item,xsFile);
            items.put(item,it);
        }

        for(int rows = 0 ; rows < menuConfig.getConfig(xsFile).getStringList("configuration.style").size() ; rows++) {
            for(int symbol = 0 ; symbol < menuConfig.getConfig(xsFile).getStringList("configuration.style").get(rows).split(" ").length ; symbol++) {
                String key = menuConfig.getConfig(xsFile).getStringList("configuration.style").get(rows).split(" ")[symbol];
                ItemStack item = items.get(key);
                int slot = symbol+(rows*9);

                if(menuConfig.getConfig(xsFile).get("configuration.items."+key+".action") != null) {
                    XSMenuHandler.getActionClicked().get(p).put(slot,menuConfig.getConfig(xsFile).getString("configuration.items."+key+".action"));
                }

                ArrayList<String> lore = new ArrayList<>();

                if(item.hasItemMeta() && item.getItemMeta().hasLore()) {
                    for(String str : item.getItemMeta().getLore()) {
                        lore.add(XSUtils.decodeStringWithPlaceholder(str,xsGuilds,server));
                    }
                    ItemMeta meta = item.getItemMeta();
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
                if(item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(XSUtils.decodeStringWithPlaceholder(item.getItemMeta().getDisplayName(),xsGuilds,server));
                    item.setItemMeta(meta);
                }

                inv.setItem(slot,item);
            }
        }

        p.openInventory(inv);
    }

}
