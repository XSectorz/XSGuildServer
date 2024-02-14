package net.xsapi.panat.xsguildclient.handler;

import net.milkbowl.vault.economy.Economy;
import net.xsapi.panat.xsguildclient.commands.commandsLoader;
import net.xsapi.panat.xsguildclient.config.configLoader;
import net.xsapi.panat.xsguildclient.config.mainConfig;
import net.xsapi.panat.xsguildclient.core;
import net.xsapi.panat.xsguildclient.listener.chatEvent;
import net.xsapi.panat.xsguildclient.listener.joinEvent;
import net.xsapi.panat.xsguildclient.listener.leaveEvent;
import net.xsapi.panat.xsguildclient.placeholder.XSPlaceholders;
import net.xsapi.panat.xsguildclient.utils.XSDATA_TYPE;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.ArrayList;


public class XSHandler {

    private static String subChannel = "xsguilds:channel";
    private static String servername = "";
    private static Economy econ = null;
    private static PlayerPointsAPI ppAPI = null;

    private static XSPlaceholders xsPlaceholders;

    private static ArrayList<String> playerInGuildChat = new ArrayList<>();

    public static ArrayList<String> getPlayerInGuildChat() {
        return playerInGuildChat;
    }
    public static String getSubChannel() {
        return subChannel;
    }

    public static String getServername() {
        return servername;
    }


    private static void subChannel() {
        servername = mainConfig.customConfig.getString("configuration.server");
        XSRedisHandler.subscribeToChannelAsync(getSubChannel()+getServername());
    }

    private static void loadEvent() {
        Bukkit.getPluginManager().registerEvents(new joinEvent(),core.getPlugin());
        Bukkit.getPluginManager().registerEvents(new leaveEvent(),core.getPlugin());
        Bukkit.getPluginManager().registerEvents(new chatEvent(),core.getPlugin());
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
