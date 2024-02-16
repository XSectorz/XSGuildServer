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

    public static void openMenu(Player p, XS_FILE xsFile, XSGuilds xsGuilds) {

        int size = 9 * menuConfig.getConfig(xsFile).getStringList("configuration.style").size();
        String title = XSUtils.decodeText(menuConfig.getConfig(xsFile).getString("configuration.title"));
        Inventory inv = Bukkit.createInventory(null, size, title);


        HashMap<String, ItemStack> items = new HashMap<>();

        for(String item : menuConfig.getConfig(xsFile).getConfigurationSection("configuration.items").getKeys(false)) {
            ItemStack it = XSUtils.decodeItemFromConfig(item,xsFile);
            items.put(item,it);
        }

        for(int rows = 0 ; rows < menuConfig.getConfig(xsFile).getStringList("configuration.style").size() ; rows++) {
            for(int symbol = 0 ; symbol < menuConfig.getConfig(xsFile).getStringList("configuration.style").get(rows).split(" ").length ; symbol++) {
                ItemStack item = items.get(menuConfig.getConfig(xsFile).getStringList("configuration.style").get(rows).split(" ")[symbol]);

                ArrayList<String> lore = new ArrayList<>();

                if(item.hasItemMeta() && item.getItemMeta().hasLore()) {
                    for(String str : item.getItemMeta().getLore()) {
                        lore.add(XSUtils.decodeStringWithPlaceholder(str,xsGuilds));
                    }
                    ItemMeta meta = item.getItemMeta();
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
                if(item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(XSUtils.decodeStringWithPlaceholder(item.getItemMeta().getDisplayName(),xsGuilds));
                    item.setItemMeta(meta);
                }

                inv.setItem(symbol+(rows*9),item);
            }
        }

        p.openInventory(inv);
    }

}
