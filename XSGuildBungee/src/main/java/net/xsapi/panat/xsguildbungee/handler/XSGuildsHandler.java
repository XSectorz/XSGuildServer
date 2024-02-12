package net.xsapi.panat.xsguildbungee.handler;

import com.google.gson.Gson;
import net.md_5.bungee.api.ChatColor;
import net.xsapi.panat.xsguildbungee.config.mainConfig;
import net.xsapi.panat.xsguildbungee.core;
import net.xsapi.panat.xsguildbungee.objects.XSGuilds;
import net.xsapi.panat.xsguildbungee.objects.XSSubGuilds;
import net.xsapi.panat.xsguildbungee.utils.XSDATA_TYPE;

import java.sql.*;
import java.util.HashMap;

public class XSGuildsHandler {
    public static HashMap<String, XSGuilds> guildList = new HashMap<>();
    public static HashMap<String,String> players = new HashMap<>();

    public static HashMap<String, XSGuilds> getGuildList() {
        return guildList;
    }

    public static HashMap<String, String> getPlayers() {
        return players;
    }

    public static String getServer(String playerSubserver) {
        for(String servers : mainConfig.getConfig().getSection("guilds-group").getKeys()) {
            if(mainConfig.getConfig().getStringList("guilds-group." + servers).contains(playerSubserver)) {
                return servers;
            }
        }
        return "";
    }

    public static void createTemplateData(int id,String guildRealName,String guildName,String leader) {
        XSGuilds xsGuilds = new XSGuilds(id,guildRealName,guildName,1);

        xsGuilds.getMembers().put(leader,"LEADER");
        xsGuilds.setLeader(leader);

        for(String servers : mainConfig.getConfig().getSection("guilds-group").getKeys()) {
            xsGuilds.getSubGuilds().put(servers,loadSubGuild(servers,id));
        }
        getGuildList().put(guildRealName,xsGuilds);
    }

    public static void forceLoad() {

        for(String servers : mainConfig.getConfig().getSection("guilds-group").getKeys()) {
            for(String subserver : mainConfig.getConfig().getStringList("guilds-group." + servers)) {
                Gson gson = new Gson();
                String guildJson = gson.toJson(XSGuildsHandler.getGuildList());
                XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+subserver, XSDATA_TYPE.FORCE_LOAD_ALL+"<SPLIT>"+guildJson);
            }
        }
    }

    public static void loadData() {
        try {
            Connection connection = DriverManager.getConnection(XSDatabaseHandler.getJdbcUrl(),
                    XSDatabaseHandler.getUSER(),XSDatabaseHandler.getPASS());

            String getAllGuild = "SELECT * FROM " + XSDatabaseHandler.getGlobalTable();
            PreparedStatement preparedStatement = connection.prepareStatement(getAllGuild);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                int guildID = resultSet.getInt("id");
                String guildRealName = resultSet.getString("Guild");
                String guildName = resultSet.getString("GuildName");
                String members = resultSet.getString("Players");
                int guildLevel = resultSet.getInt("GuildLevel");
                //core.getPlugin().getLogger().info(guildID+"");
                //core.getPlugin().getLogger().info(guildName);
                //core.getPlugin().getLogger().info(members);
                //core.getPlugin().getLogger().info("---------------");

                XSGuilds xsGuilds = new XSGuilds(guildID,guildRealName,guildName,guildLevel);

                members = members.replace("[","").replace("]","");

                for(String player : members.split(",")) {
                    player = player.replace(" ", "");
                    String rank = player.split(":")[0];
                    String name = player.split(":")[1];

                    xsGuilds.getMembers().put(name,rank);
                    getPlayers().put(name,guildRealName);
                    if(rank.equalsIgnoreCase("LEADER")) {
                        xsGuilds.setLeader(name);
                    } else if(rank.equalsIgnoreCase("SUB-LEADER")) {
                        xsGuilds.getSubleader().add(name);
                    }
                }

                for(String servers : mainConfig.getConfig().getSection("guilds-group").getKeys()) {
                    xsGuilds.getSubGuilds().put(servers,loadSubGuild(servers,guildID));
                }
                getGuildList().put(guildRealName,xsGuilds);
                //core.getPlugin().getLogger().info("---------------------");
            }

            resultSet.close();
            preparedStatement.close();
            connection.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeGuildFromDatabase(XSGuilds xsGuilds) {
        try {
            Connection connection = DriverManager.getConnection(XSDatabaseHandler.getJdbcUrl(),
                    XSDatabaseHandler.getUSER(),XSDatabaseHandler.getPASS());

            String getAllGuild = "DELETE FROM xsguilds_bungee_main WHERE Guild = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(getAllGuild);
            preparedStatement.setString(1, xsGuilds.getRealName());

            for(String server : mainConfig.getConfig().getSection("guilds-group").getKeys()) {
                removeSubGuildFromDatabase(server, xsGuilds.getGuildID());
            }

            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeSubGuildFromDatabase(String server,int id) {
        try {
            Connection connection = DriverManager.getConnection(XSDatabaseHandler.getJdbcUrl(),
                    XSDatabaseHandler.getUSER(),XSDatabaseHandler.getPASS());

            String getAllGuild = "DELETE FROM xsguilds_bungee_" + server + " WHERE Reference = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(getAllGuild);
            preparedStatement.setInt(1,id);
            preparedStatement.executeUpdate();

            preparedStatement.close();
            connection.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static XSSubGuilds loadSubGuild(String server,int refID) {

        XSSubGuilds xsSubGuilds = null;

        try {
            Connection connection = DriverManager.getConnection(XSDatabaseHandler.getJdbcUrl(),
                    XSDatabaseHandler.getUSER(),XSDatabaseHandler.getPASS());

            String getAllGuild = "SELECT * FROM " + "xsguilds_bungee_" + server + " WHERE Reference = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(getAllGuild);
            preparedStatement.setInt(1,refID);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int guildLevel = resultSet.getInt("Level");
                String guildTech = resultSet.getString("Tech");

               // core.getPlugin().getLogger().info("SUB GUILD: " + server);
               // core.getPlugin().getLogger().info(guildLevel+"");
               // core.getPlugin().getLogger().info(guildTech);
               // core.getPlugin().getLogger().info("--------------------------");
                xsSubGuilds = new XSSubGuilds(guildTech,guildLevel,server);
            }

            resultSet.close();
            preparedStatement.close();
            connection.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return xsSubGuilds;
    }

}
