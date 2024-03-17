package net.xsapi.panat.xsguildclient.handler;

import net.milkbowl.vault.economy.Economy;
import net.xsapi.panat.xsguildclient.commands.commandsLoader;
import net.xsapi.panat.xsguildclient.config.configLoader;
import net.xsapi.panat.xsguildclient.config.mainConfig;
import net.xsapi.panat.xsguildclient.core;
import net.xsapi.panat.xsguildclient.listener.chatEvent;
import net.xsapi.panat.xsguildclient.listener.inventoryEvent;
import net.xsapi.panat.xsguildclient.listener.joinEvent;
import net.xsapi.panat.xsguildclient.listener.leaveEvent;
import net.xsapi.panat.xsguildclient.objects.XSUpgrades;
import net.xsapi.panat.xsguildclient.placeholder.XSPlaceholders;
import net.xsapi.panat.xsguildclient.utils.XSDATA_TYPE;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashMap;


public class XSHandler {

    private static String subChannel = "xsguilds:channel";
    private static String servername = "";
    private static Economy econ = null;
    private static PlayerPointsAPI ppAPI = null;

    private static XSPlaceholders xsPlaceholders;

    private static double createPrice;

    private static ArrayList<String> playerInGuildChat = new ArrayList<>();

    private static HashMap<Integer, XSUpgrades> mainClanUpgrades = new HashMap<>();
    private static HashMap<Integer, XSUpgrades> subClanUpgrades = new HashMap<>();

    private static HashMap<String,String> teleportHomeData = new HashMap<>();

    public static ArrayList<String> getPlayerInGuildChat() {
        return playerInGuildChat;
    }
    public static String getSubChannel() {
        return subChannel;
    }

    public static String getServername() {
        return servername;
    }

    public static double getCreatePrice() {
        return createPrice;
    }

    public static void setCreatePrice(double createPrice) {
        XSHandler.createPrice = createPrice;
    }

    public static HashMap<Integer, XSUpgrades> getMainClanUpgrades() {
        return mainClanUpgrades;
    }

    public static HashMap<Integer, XSUpgrades> getSubClanUpgrades() {
        return subClanUpgrades;
    }

    public static HashMap<String,String> getTeleportHomeData() {
        return teleportHomeData;
    }

    public static void setMainClanUpgrades(HashMap<Integer, XSUpgrades> mainClanUpgrades) {
        XSHandler.mainClanUpgrades = mainClanUpgrades;
    }

    public static void setSubClanUpgrades(HashMap<Integer, XSUpgrades> subClanUpgrades) {
        XSHandler.subClanUpgrades = subClanUpgrades;
    }

    private static void subChannel() {
        servername = mainConfig.customConfig.getString("configuration.server");
        XSRedisHandler.subscribeToChannelAsync(getSubChannel()+getServername());
    }

    private static void loadEvent() {
        Bukkit.getPluginManager().registerEvents(new joinEvent(),core.getPlugin());
        Bukkit.getPluginManager().registerEvents(new leaveEvent(),core.getPlugin());
        Bukkit.getPluginManager().registerEvents(new chatEvent(),core.getPlugin());
        Bukkit.getPluginManager().registerEvents(new inventoryEvent(),core.getPlugin());
    }

    public static void sendPlayerToServer(Player player, String server) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            out.writeUTF("Connect");
            out.writeUTF(server);
            player.sendPluginMessage(core.getPlugin(), "BungeeCord", b.toByteArray());
            b.close();
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void registerPlaceholder() {
        xsPlaceholders = new XSPlaceholders();
        xsPlaceholders.register();
    }

    public static void loadOnlinePlayerData() {
        for(Player p : Bukkit.getOnlinePlayers()) {
            XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+"_bungeecord", XSDATA_TYPE.REQ_DATA+"<SPLIT>" + XSHandler.getServername() + ";" + p.getName());
        }
    }

    public static void unregisterPlaceholder() {
        xsPlaceholders.unregister();
    }

    public static void initSystem() {
        new configLoader();
        new commandsLoader();
        XSRedisHandler.redisConnection();
        subChannel();
        loadEvent();
        loadGuildData();
        registerPlaceholder();
    }

    public static void loadGuildData() {
        XSRedisHandler.sendRedisMessage(getSubChannel()+"_bungeecord", XSDATA_TYPE.REQ_GUILD+"<SPLIT>" + getServername());
    }

    private static boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = core.getPlugin().getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return true;
    }

    private static boolean setupSCCoin() {
        if (Bukkit.getPluginManager().getPlugin("PlayerPoints") != null) {
            ppAPI = PlayerPoints.getInstance().getAPI();
        }
        if (ppAPI != null) {
            return true;
        } else {
            return false;
        }
    }

    public static Economy getEconomy() {
        return econ;
    }
    public static PlayerPointsAPI getSCPoint() {
        return ppAPI;
    }

    public static void APILoader() {
        if (!setupEconomy()) {
            Bukkit.getConsoleSender().sendMessage("§x§f§f§5§8§5§8[XSGuilds-Client] Disabled due to no Vault dependency found!");
            Bukkit.getPluginManager().disablePlugin(core.getPlugin());
            return;
        } else {
            Bukkit.getConsoleSender().sendMessage("§x§f§f§c§e§2§2[XSGuilds-Client] Vault: §x§5§d§f§f§6§3Hook");
        }

        if(!setupSCCoin()) {
            Bukkit.getConsoleSender().sendMessage("§x§f§f§c§e§2§2[XSGuilds-Client] PlayerPoint: §x§f§f§5§8§5§8Not Hook");
        } else {
            Bukkit.getConsoleSender().sendMessage("§x§f§f§c§e§2§2[XSGuilds-Client] PlayerPoint: §x§5§d§f§f§6§3Hook");
        }

    }


}
