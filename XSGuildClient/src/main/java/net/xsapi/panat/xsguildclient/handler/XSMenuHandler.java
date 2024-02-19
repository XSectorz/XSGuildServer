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
import java.util.List;
import java.util.Map;

public class XSMenuHandler {

    private static HashMap<Player,HashMap<Integer, String>> leftActionClicked = new HashMap<>();
    private static HashMap<Player,HashMap<Integer, String>> rightActionClicked = new HashMap<>();
    private static HashMap<Player,Integer> playerPage = new HashMap<>();

    public static HashMap<Player,HashMap<Integer, String>> getLeftActionClicked() {
        return leftActionClicked;
    }
    public static HashMap<Player,HashMap<Integer, String>> getRightActionClicked() {
        return rightActionClicked;
    }
    public static HashMap<Player,Integer> getPlayerPage() {
        return playerPage;
    }

    public static void openMenu(Player p, XS_FILE xsFile, XSGuilds xsGuilds) {

        int size = 9 * menuConfig.getConfig(xsFile).getStringList("configuration.style").size();
        String title = XSUtils.decodeText(menuConfig.getConfig(xsFile).getString("configuration.title"));
        Inventory inv = Bukkit.createInventory(null, size, title);

        String server = XSGuildsHandler.getPlayers().get(p.getName()).split("<SPLIT>")[0];
        HashMap<String, ItemStack> items = new HashMap<>();
        getLeftActionClicked().put(p,new HashMap<>());
        getRightActionClicked().put(p,new HashMap<>());

        for(String symbol : menuConfig.getConfig(xsFile).getConfigurationSection("configuration.items").getKeys(false)) {
            ItemStack it = XSUtils.decodeItemFromConfig("configuration.items."+symbol,xsFile,p.getName());
            items.put(symbol,it);
        }


        for(int rows = 0 ; rows < menuConfig.getConfig(xsFile).getStringList("configuration.style").size() ; rows++) {
            for(int symbol = 0 ; symbol < menuConfig.getConfig(xsFile).getStringList("configuration.style").get(rows).split(" ").length ; symbol++) {
                String key = menuConfig.getConfig(xsFile).getStringList("configuration.style").get(rows).split(" ")[symbol];
                ItemStack item = items.get(key);
                int slot = symbol+(rows*9);

                if(menuConfig.getConfig(xsFile).get("configuration.items."+key+".action_left_click") != null) {
                    XSMenuHandler.getLeftActionClicked().get(p).put(slot,menuConfig.getConfig(xsFile).getString("configuration.items."+key+".action_left_click"));
                }

                if(menuConfig.getConfig(xsFile).get("configuration.items."+key+".action_right_click") != null) {
                    XSMenuHandler.getRightActionClicked().get(p).put(slot,menuConfig.getConfig(xsFile).getString("configuration.items."+key+".action_right_click"));
                }

                inv.setItem(slot,XSUtils.decodePlaceholderItems(item,xsGuilds,server,p.getName()));
            }
        }

        if(xsFile.equals(XS_FILE.MEMBERS_MENU)) {
            int leaderSlot = menuConfig.getConfig(xsFile).getInt("condition_configuration.leader_slot");
            ItemStack leaderItem = XSUtils.decodePlaceholderItems(XSUtils.decodeItemFromConfig("condition_configuration.leader_profile",xsFile,xsGuilds.getLeader()),xsGuilds,server,xsGuilds.getLeader());
            inv.setItem(leaderSlot,leaderItem);

            List<String> sub_leaderSlot = menuConfig.getConfig(xsFile).getStringList("condition_configuration.subLeader_slot");
            for(String slot : sub_leaderSlot) {
                inv.setItem(Integer.parseInt(slot), XSUtils.decodePlaceholderItems(XSUtils.decodeItemFromConfig("condition_configuration.barrier",xsFile,p.getName()),xsGuilds,server,""));
            }

            for(int i = 0 ; i < xsGuilds.getSubleader().size() ; i++) {
                inv.setItem(Integer.parseInt(sub_leaderSlot.get(i)), XSUtils.decodePlaceholderItems(XSUtils.decodeItemFromConfig("condition_configuration.subLeader_profile",xsFile,
                        xsGuilds.getSubleader().get(i)),xsGuilds,server,xsGuilds.getSubleader().get(i)));
            }
            List<String> memberSlot = menuConfig.getConfig(xsFile).getStringList("condition_configuration.member_slot");
            int index = (getPlayerPage().get(p)*memberSlot.size())-memberSlot.size();

            for(int i = index ; i < memberSlot.size()* getPlayerPage().get(p) ; i++) {

                if(i >= xsGuilds.getClanmates().size()) {
                    break;
                }

                if(xsGuilds.getMembers().get(xsGuilds.getClanmates().get(i)).equalsIgnoreCase("MEMBER")) {
                    inv.setItem(Integer.parseInt(memberSlot.get(i)), XSUtils.decodePlaceholderItems(XSUtils.decodeItemFromConfig("condition_configuration.member_profile",xsFile,
                            xsGuilds.getClanmates().get(i)),xsGuilds,server,xsGuilds.getClanmates().get(i)));
                } else {
                    inv.setItem(Integer.parseInt(memberSlot.get(i)), XSUtils.decodePlaceholderItems(XSUtils.decodeItemFromConfig("condition_configuration.new_member_profile",xsFile,
                            xsGuilds.getClanmates().get(i)),xsGuilds,server,xsGuilds.getClanmates().get(i)));
                }
            }

            int backSlot = menuConfig.getConfig(xsFile).getInt("condition_configuration.back_slot");
            int nextSlot = menuConfig.getConfig(xsFile).getInt("condition_configuration.next_slot");

            if(getPlayerPage().get(p) > 1) {
                inv.setItem(backSlot, XSUtils.decodePlaceholderItems(XSUtils.decodeItemFromConfig("condition_configuration.back_available",xsFile,p.getName()),xsGuilds,server,p.getName()));
            } else {
                inv.setItem(backSlot, XSUtils.decodePlaceholderItems(XSUtils.decodeItemFromConfig("condition_configuration.back_un_available",xsFile,p.getName()),xsGuilds,server,p.getName()));
            }

            if(index+memberSlot.size() < xsGuilds.getClanmates().size()) {
                inv.setItem(nextSlot, XSUtils.decodePlaceholderItems(XSUtils.decodeItemFromConfig("condition_configuration.next_available",xsFile,p.getName()),xsGuilds,server,p.getName()));
            } else {
                inv.setItem(nextSlot, XSUtils.decodePlaceholderItems(XSUtils.decodeItemFromConfig("condition_configuration.next_un_available",xsFile,p.getName()),xsGuilds,server,p.getName()));
            }
        }

        p.openInventory(inv);
    }

}
