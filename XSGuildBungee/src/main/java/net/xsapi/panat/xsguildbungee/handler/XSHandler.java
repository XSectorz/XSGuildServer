package net.xsapi.panat.xsguildbungee.handler;

import com.google.gson.Gson;
import net.xsapi.panat.xsguildbungee.config.mainConfig;
import net.xsapi.panat.xsguildbungee.core;
import net.xsapi.panat.xsguildbungee.listener.playerSwitch;
import net.xsapi.panat.xsguildbungee.objects.XSGuilds;
import net.xsapi.panat.xsguildbungee.objects.XSUpgrades;
import net.xsapi.panat.xsguildbungee.utils.XSDATA_TYPE;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class XSHandler {

    private static String subChannel = "xsguilds:channel";
    private static ArrayList<String> playerInGuildChat = new ArrayList<>();

    public static HashMap<Integer, XSUpgrades> mainClanUpgrades = new HashMap<>();
    public static HashMap<Integer, XSUpgrades> subClanUpgrades = new HashMap<>();

    public static String getSubChannel() {
        return subChannel;
    }

    private static double create_guild_price = 0;

    public static double getCreatePrice() {
        return create_guild_price;
    }

    public static void setCreate_guild_price(double create_guild_price) {
        XSHandler.create_guild_price = create_guild_price;
    }

    public static ArrayList<String> getPlayerInGuildChat() {
        return playerInGuildChat;
    }
    public static void loadEvent() {
        core.getPlugin().getProxy().getPluginManager().registerListener(core.getPlugin(),new playerSwitch());
    }

    public static void loadUpgrades() {
        for(String upgradeMain : mainConfig.getConfig().getSection("upgrades_configuration.main").getKeys()) {
            int level = Integer.parseInt(upgradeMain.replace("level_",""));
            double pointsReq = 0;
            double coinsReq = 0;
            if(mainConfig.getConfig().get("upgrades_configuration.main." + upgradeMain + ".points") != null) {
                pointsReq = mainConfig.getConfig().getDouble("upgrades_configuration.main." + upgradeMain + ".points");
            }
            if(mainConfig.getConfig().get("upgrades_configuration.main." + upgradeMain + ".coins") != null) {
                coinsReq = mainConfig.getConfig().getDouble("upgrades_configuration.main." + upgradeMain + ".coins");
            }

            String bankCapacityUpgrade = String.valueOf(mainConfig.getConfig().getInt("guild_configuration.balance_capacity.main." + upgradeMain));
            String memberUpgrade = String.valueOf(mainConfig.getConfig().getInt("guild_configuration.members.main." + upgradeMain));
            XSUpgrades xsUpgrades = new XSUpgrades("main",coinsReq,pointsReq);
            xsUpgrades.setLevel(level);
            xsUpgrades.getNextUpgrades().put("BANK_CAPACITY",bankCapacityUpgrade);
            xsUpgrades.getNextUpgrades().put("MEMBERS",memberUpgrade);
            mainClanUpgrades.put(level,xsUpgrades);
        }

        for(String upgradeMain : mainConfig.getConfig().getSection("upgrades_configuration.sub").getKeys()) {
            int level = Integer.parseInt(upgradeMain.replace("level_",""));
            double pointsReq = 0;
            double coinsReq = 0;
            if(mainConfig.getConfig().get("upgrades_configuration.sub." + upgradeMain + ".points") != null) {
                pointsReq = mainConfig.getConfig().getDouble("upgrades_configuration.sub." + upgradeMain + ".points");
            }
            if(mainConfig.getConfig().get("upgrades_configuration.sub." + upgradeMain + ".coins") != null) {
                coinsReq = mainConfig.getConfig().getDouble("upgrades_configuration.sub." + upgradeMain + ".coins");
            }

            String bankCapacityUpgrade = String.valueOf(mainConfig.getConfig().getInt("guild_configuration.balance_capacity.sub." + upgradeMain));
            String homeUpgrade = String.valueOf(mainConfig.getConfig().getInt("guild_configuration.home.sub." + upgradeMain));
            XSUpgrades xsUpgrades = new XSUpgrades("sub",coinsReq,pointsReq);
            xsUpgrades.getNextUpgrades().put("BANK_CAPACITY",bankCapacityUpgrade);
            xsUpgrades.getNextUpgrades().put("HOME",homeUpgrade);
            subClanUpgrades.put(level,xsUpgrades);
        }

        Gson gson = new Gson();
        String mainClanUpgradeJson = gson.toJson(mainClanUpgrades);
        String subClanUpgradeJson = gson.toJson(subClanUpgrades);
        //core.getPlugin().getLogger().info(guildJson);
        for(String servers : mainConfig.getConfig().getSection("guilds-group").getKeys()) {
            for(String subServer : mainConfig.getConfig().getStringList("guilds-group." + servers)) {
                XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+subServer, XSDATA_TYPE.SENT_UPGRADES_INFO+"<SPLIT>"+mainClanUpgradeJson+";"+subClanUpgradeJson);
            }
        }

        setCreate_guild_price(mainConfig.getConfig().getInt("create_guild_price"));
    }

    public static void subChannel() {
        /*for(String server : mainConfig.getConfig().getSection("guilds-group").getKeys()) {
            for(String subserver : mainConfig.getConfig().getStringList("guilds-group." + server)) {
                XSRedisHandler.subscribeToChannelAsync(getSubChannel() + "_" + subserver);
            }
        }*/
        XSRedisHandler.subscribeToChannelAsync(getSubChannel() + "_bungeecord");
    }

    public static void forceSaved() {
        try {
            Connection connection = DriverManager.getConnection(XSDatabaseHandler.getJdbcUrl(),XSDatabaseHandler.getUSER(),XSDatabaseHandler.getPASS());
            for(Map.Entry<String, XSGuilds> guild : XSGuildsHandler.getGuildList().entrySet()) {
               // core.getPlugin().getLogger().info("GUILD: " + guild.getKey() + " BAL " + guild.getValue().getBalance());
                XSDatabaseHandler.updateMainGuild(connection,guild.getValue());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void initSystem() {
        XSRedisHandler.redisConnection();
        loadEvent();
        subChannel();
    }



}
