package net.xsapi.panat.xsguildbungee.handler;

import net.md_5.bungee.api.ChatColor;
import net.xsapi.panat.xsguildbungee.config.mainConfig;
import net.xsapi.panat.xsguildbungee.core;
import net.xsapi.panat.xsguildbungee.objects.XSGuilds;
import net.xsapi.panat.xsguildbungee.objects.XSSubGuilds;

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

    public static void loadData() {
        try {
            Connection connection = DriverManager.getConnection(XSDatabaseHandler.getJdbcUrl(),
                    XSDatabaseHandler.getUSER(),XSDatabaseHandler.getPASS());

            String getAllGuild = "SELECT * FROM " + XSDatabaseHandler.getGlobalTable();
            PreparedStatement preparedStatement = connection.prepareStatement(getAllGuild);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                int guildID = resultSet.getInt("id");
                String guilRealName = resultSet.getString("Guild");
                String guildName = resultSet.getString("GuildName");
                String members = resultSet.getString("Players");
                core.getPlugin().getLogger().info(guildID+"");
                core.getPlugin().getLogger().info(guildName);
                core.getPlugin().getLogger().info(members);
                core.getPlugin().getLogger().info("---------------");

                XSGuilds xsGuilds = new XSGuilds(guildName);

                members = members.replace("[","").replace("]","");

                for(String player : members.split(",")) {
                    String rank = player.split(":")[0];
                    String name = player.split(":")[1];

                    xsGuilds.getMembers().put(name,rank);

                    if(rank.equalsIgnoreCase("LEADER")) {
                        xsGuilds.setLeader(name);
                    } else if(rank.equalsIgnoreCase("SUB-LEADER")) {
                        xsGuilds.getSubleader().add(name);
                    }
                }

                for(String servers : mainConfig.getConfig().getSection("guilds-group").getKeys()) {
                    xsGuilds.getSubGuilds().put(servers,loadSubGuild(servers,guildID));
                }
                getGuildList().put(guilRealName,xsGuilds);
                //core.getPlugin().getLogger().info("---------------------");

            }

            resultSet.close();
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
