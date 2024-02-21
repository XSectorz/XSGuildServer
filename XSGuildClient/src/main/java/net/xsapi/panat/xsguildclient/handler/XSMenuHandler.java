package net.xsapi.panat.xsguildclient.handler;

import net.xsapi.panat.xsguildclient.config.menuConfig;
import net.xsapi.panat.xsguildclient.objects.XSGuilds;
import net.xsapi.panat.xsguildclient.utils.XSPERMS_TYPE;
import net.xsapi.panat.xsguildclient.utils.XSUtils;
import net.xsapi.panat.xsguildclient.utils.XS_FILE;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class XSMenuHandler {

    private static HashMap<Player,HashMap<Integer, String>> leftActionClicked = new HashMap<>();
    private static HashMap<Player,HashMap<Integer, String>> rightActionClicked = new HashMap<>();
    private static HashMap<UUID, Inventory> playerOpenInventory = new HashMap<>();
    private static HashMap<Player,Integer> playerPage = new HashMap<>();
    private static HashMap<Player,ArrayList<String>> permsDataPage = new HashMap<>();

    public static HashMap<Player,HashMap<Integer, String>> getLeftActionClicked() {
        return leftActionClicked;
    }
    public static HashMap<Player,HashMap<Integer, String>> getRightActionClicked() {
        return rightActionClicked;
    }
    public static HashMap<Player,Integer> getPlayerPage() {
        return playerPage;
    }
    public static HashMap<Player,ArrayList<String>> getPermsDataPage() {
        return permsDataPage;
    }

    public static HashMap<UUID, Inventory> getPlayerOpenInventory() {
        return playerOpenInventory;
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

            int slot = 0;
            for(int i = index ; i < memberSlot.size()* getPlayerPage().get(p) ; i++) {

                if(i >= xsGuilds.getClanmates().size()) {
                    break;
                }

                if(xsGuilds.getMembers().get(xsGuilds.getClanmates().get(i)).equalsIgnoreCase("MEMBER")) {
                    inv.setItem(Integer.parseInt(memberSlot.get(slot)), XSUtils.decodePlaceholderItems(XSUtils.decodeItemFromConfig("condition_configuration.member_profile",xsFile,
                            xsGuilds.getClanmates().get(i)),xsGuilds,server,xsGuilds.getClanmates().get(i)));
                } else {
                    inv.setItem(Integer.parseInt(memberSlot.get(slot)), XSUtils.decodePlaceholderItems(XSUtils.decodeItemFromConfig("condition_configuration.new_member_profile",xsFile,
                            xsGuilds.getClanmates().get(i)),xsGuilds,server,xsGuilds.getClanmates().get(i)));
                }
                slot++;
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
        } else if(xsFile.equals(XS_FILE.PERMISSION_MENU)) {

            if(!getPlayerOpenInventory().containsKey(p.getUniqueId())) {
                getPlayerOpenInventory().put(p.getUniqueId(),inv);
            }
            updateInventoryContents(p,XS_FILE.PERMISSION_MENU,xsGuilds,server);
        }

        p.openInventory(inv);
    }

    public static void updateInventoryContents(Player p,XS_FILE xsFile,XSGuilds xsGuilds,String server) {
        Inventory inv = getPlayerOpenInventory().get(p.getUniqueId());

        List<String> infoSlot = menuConfig.getConfig(xsFile).getStringList("condition_configuration.infoUpgrade_slot");
        int index = (getPlayerPage().get(p)*infoSlot.size())-infoSlot.size();
        int slot = 0;

        for(String slotData : infoSlot) {
            inv.setItem(Integer.parseInt(slotData),new ItemStack(Material.AIR));
        }
        ArrayList<String> dataPerms = new ArrayList<>();
        for(int i = index ; i < infoSlot.size()* getPlayerPage().get(p) ; i++) {

            if(i >= XSPERMS_TYPE.values().length) {
                break;
            }

            ItemStack it = XSUtils.decodePlaceholderItems(XSUtils.decodeItemFromConfig("condition_configuration." + XSPERMS_TYPE.values()[i],xsFile,p.getName()),xsGuilds,server,p.getName());
            dataPerms.add(XSPERMS_TYPE.values()[i].toString());
            inv.setItem(Integer.parseInt(infoSlot.get(slot)),it);
            slot++;
        }
        getPermsDataPage().put(p,dataPerms);

        int backSlot = menuConfig.getConfig(xsFile).getInt("condition_configuration.back_slot");
        int nextSlot = menuConfig.getConfig(xsFile).getInt("condition_configuration.next_slot");

        if(getPlayerPage().get(p) > 1) {
            inv.setItem(backSlot, XSUtils.decodePlaceholderItems(XSUtils.decodeItemFromConfig("condition_configuration.back_available",xsFile,p.getName()),xsGuilds,server,p.getName()));
        } else {
            inv.setItem(backSlot, XSUtils.decodePlaceholderItems(XSUtils.decodeItemFromConfig("condition_configuration.back_un_available",xsFile,p.getName()),xsGuilds,server,p.getName()));
        }

        if(index+infoSlot.size() < XSPERMS_TYPE.values().length) {
            inv.setItem(nextSlot, XSUtils.decodePlaceholderItems(XSUtils.decodeItemFromConfig("condition_configuration.next_available",xsFile,p.getName()),xsGuilds,server,p.getName()));
        } else {
            inv.setItem(nextSlot, XSUtils.decodePlaceholderItems(XSUtils.decodeItemFromConfig("condition_configuration.next_un_available",xsFile,p.getName()),xsGuilds,server,p.getName()));
        }

        for(String s : menuConfig.getConfig(xsFile).getStringList("condition_configuration.blank_slot")) {
            inv.setItem(Integer.parseInt(s),new ItemStack(Material.AIR));
        }

        ArrayList<String> rank = new ArrayList<>(Arrays.asList("SUB_LEADER","MEMBER","NEW_MEMBER"));


        for(int slotIndex = 0 ; slotIndex < dataPerms.size() ; slotIndex++) {
            for(int i = 0 ; i < rank.size() ; i++) {
                int slotToplace = Integer.parseInt(infoSlot.get(slotIndex))+(9*(i+1));
                //Bukkit.broadcastMessage("RANK: " + rank.get(i));
                //Bukkit.broadcastMessage("PERM_TYPE: " + dataPerms.get(slotIndex));
                if(xsGuilds.getPermission().get(rank.get(i)).get(dataPerms.get(slotIndex))) {
                    inv.setItem(slotToplace, XSUtils.decodePlaceholderItems(XSUtils.decodeItemFromConfig("condition_configuration.OwnPermission",xsFile,p.getName()),xsGuilds,server,p.getName()));
                } else {
                    inv.setItem(slotToplace, XSUtils.decodePlaceholderItems(XSUtils.decodeItemFromConfig("condition_configuration.NotOwnPermission",xsFile,p.getName()),xsGuilds,server,p.getName()));
                }
            }
        }

        p.updateInventory();
    }

}
