package net.xsapi.panat.xsguildbungee.handler;

import net.md_5.bungee.api.ChatColor;
import net.xsapi.panat.xsguildbungee.config.mainConfig;
import net.xsapi.panat.xsguildbungee.core;
import net.xsapi.panat.xsguildbungee.objects.XSGuilds;
import net.xsapi.panat.xsguildbungee.objects.XSSubGuilds;

import java.sql.*;
import java.util.ArrayList;
import java.util.Map;

public class XSDatabaseHandler {

    private static String JDBC_URL;
    private static String USER;
    private static String PASS;
    private static String DB_NAME;
    private static String GLOBAL_TABLE = "xsguilds_bungee_main";

    public static String getDbName() {
        return DB_NAME;
    }

    public static String getGlobalTable() {
        return GLOBAL_TABLE;
    }

    public static String getJdbcUrl() {
        return JDBC_URL;
    }

    public static String getPASS() {
        return PASS;
    }

    public static String getUSER() {
        return USER;
    }

    private final static String MAIN_SQL_QUERY = " ("
            + "id INT PRIMARY KEY AUTO_INCREMENT, "
            + "Guild VARCHAR(32), "
            + "GuildName TEXT, "
            + "Players TEXT, "
            + "Balance DOUBLE, "
            + "GuildLevel INT"
            + ")";

    private final static String SUB_SQL_QUERY = " ("
            + "id INT PRIMARY KEY AUTO_INCREMENT, "
            + "Reference INT, "
            + "Level INT, "
            + "Balance DOUBLE, "
            + "Tech TEXT"
            + ")";

    public static void sqlConnection(String table,String query) {
        String host = mainConfig.getConfig().getString("database.host");
        DB_NAME =  mainConfig.getConfig().getString("database.dbName");
        JDBC_URL = "jdbc:mysql://" + host +  "/" + getDbName();
        USER = mainConfig.getConfig().getString("database.user");
        PASS = mainConfig.getConfig().getString("database.password");


        try {
            Connection connection = DriverManager.getConnection(getJdbcUrl(),getUSER(),getPASS());

            Statement statement = connection.createStatement();

            String createTableQuery = "CREATE TABLE IF NOT EXISTS " + table + query;
            statement.executeUpdate(createTableQuery);
            statement.close();
            connection.close();

            //core.getPlugin().getLogger().info("[XSGUILDS] Database : Connected");
        } catch (SQLException e) {
            //core.getPlugin().getLogger().info("[XSGUILDS] Database : Not Connected");
            e.printStackTrace();
        }
    }

    public static void createSQLDatabase() {
        sqlConnection(getGlobalTable(),MAIN_SQL_QUERY);

        for(String servers : mainConfig.getConfig().getSection("guilds-group").getKeys()) {
            core.getPlugin().getLogger().info("[XSGUILDS] " + servers);
            sqlConnection("xsguilds_bungee_" + servers,SUB_SQL_QUERY);
        }
    }

    public static void createGuild(String guildRealName,String guildName,String leader) {
        try {
            Connection connection = DriverManager.getConnection(getJdbcUrl(),getUSER(),getPASS());

            String checkGuild = "SELECT EXISTS(SELECT * FROM " + getGlobalTable() + " WHERE Guild = ?) AS exist";
            PreparedStatement preparedStatement = connection.prepareStatement(checkGuild);
            preparedStatement.setString(1, guildName);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                boolean exists = resultSet.getBoolean("exist");

                if (!exists) {
                    createMainGuildServer(connection, guildRealName,guildName,leader);
                } else {
                    //core.getPlugin().getLogger().info("Guild already exists");
                }
            }

            resultSet.close();
            preparedStatement.close();
            connection.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private static void createMainGuildServer(Connection connection,String guild,String guildName,String leader) {
        String insertQuery = "INSERT INTO " + "xsguilds_bungee_main" + " (Guild, GuildName, Players, Balance, GuildLevel) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatementInsert = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatementInsert.setString(1, guild);
            preparedStatementInsert.setString(2, guildName);
            preparedStatementInsert.setString(3, "[LEADER:" + leader + "]");
            preparedStatementInsert.setDouble(4, 0);
            preparedStatementInsert.setInt(5, 1);
            preparedStatementInsert.executeUpdate();

            XSGuildsHandler.getPlayers().put(leader,guild); //guild = guild real name

            try (ResultSet generatedKeys = preparedStatementInsert.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    for (String servers : mainConfig.getConfig().getSection("guilds-group").getKeys()) {
                        //core.getPlugin().getLogger().info("CREATE : " + servers);
                        createSubGuildServer(connection, servers, id);
                    }
                    XSGuildsHandler.createTemplateData(id,guild,guildName,leader);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private static void createSubGuildServer(Connection connection,String subGroup,int ref) {
        String insertQuery = "INSERT INTO " + ("xsguilds_bungee_"+subGroup) + " (Reference, Level, Balance, Tech) "
                + "VALUES (?, ?, ?, ?)";

        try (PreparedStatement preparedStatementInsert = connection.prepareStatement(insertQuery)) {
            preparedStatementInsert.setInt(1, ref);
            preparedStatementInsert.setInt(2, 1);
            preparedStatementInsert.setDouble(3, 0);
            preparedStatementInsert.setString(4, "");
            preparedStatementInsert.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateMainGuild(Connection connection,XSGuilds xsGuilds) {
        String updateQuery ="UPDATE xsguilds_bungee_main" + " SET Players = ?, Balance = ?, GuildLevel = ? WHERE id = ?";

        ArrayList<String> members = new ArrayList<>();

        for(Map.Entry<String,String> member : xsGuilds.getMembers().entrySet()) {
            members.add(member.getValue()+":"+member.getKey());
        }

        try (PreparedStatement preparedStatementInsert = connection.prepareStatement(updateQuery)) {
            preparedStatementInsert.setString(1, members.toString());
            preparedStatementInsert.setDouble(2, xsGuilds.getBalance());
            preparedStatementInsert.setInt(3, xsGuilds.getGuildLevel());
            preparedStatementInsert.setInt(4, xsGuilds.getGuildID());
            preparedStatementInsert.executeUpdate();

            //core.getPlugin().getLogger().info("Current Balance " + xsGuilds.getBalance());

            for (String servers : mainConfig.getConfig().getSection("guilds-group").getKeys()) {
                XSSubGuilds xsSubGuilds = xsGuilds.getSubGuilds().get(servers);
                updateSubGuild(connection,servers,xsSubGuilds.getLevel(), xsSubGuilds.getBalance(), xsSubGuilds.getTech(),xsGuilds.getGuildID());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void updateSubGuild(Connection connection,String subGroup,int level,double balance,String tech,int ref) {
        String updateQuery = "UPDATE " + ("xsguilds_bungee_"+subGroup) + " SET Level = ?, Balance = ?, Tech = ? WHERE Reference = ?";

        try (PreparedStatement preparedStatementInsert = connection.prepareStatement(updateQuery)) {
            preparedStatementInsert.setInt(1, level);
            preparedStatementInsert.setDouble(2, balance);
            preparedStatementInsert.setString(3, tech);
            preparedStatementInsert.setInt(4, ref);
            preparedStatementInsert.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
