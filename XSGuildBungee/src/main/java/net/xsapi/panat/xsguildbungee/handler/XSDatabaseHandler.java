package net.xsapi.panat.xsguildbungee.handler;

import net.md_5.bungee.api.ChatColor;
import net.xsapi.panat.xsguildbungee.config.mainConfig;
import net.xsapi.panat.xsguildbungee.core;

import java.sql.*;

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
            + "Players TEXT"
            + ")";

    private final static String SUB_SQL_QUERY = " ("
            + "id INT PRIMARY KEY AUTO_INCREMENT, "
            + "Reference INT, "
            + "Level INT, "
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

    public static void createGuild(String guildName,String leader) {
        try {
            Connection connection = DriverManager.getConnection(getJdbcUrl(),getUSER(),getPASS());

            String checkPlayerQuery = "SELECT EXISTS(SELECT * FROM " + getGlobalTable() + " WHERE Guild = ?) AS exist";
            PreparedStatement preparedStatement = connection.prepareStatement(checkPlayerQuery);
            preparedStatement.setString(1, guildName);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                boolean exists = resultSet.getBoolean("exist");

                if (!exists) {
                    createMainGuildServer(connection, ChatColor.stripColor(guildName),guildName,leader);

                    String getIDQuery = "SELECT id FROM " + getGlobalTable() + " WHERE Guild = ?";
                    PreparedStatement preparedStatementGetID = connection.prepareStatement(getIDQuery);
                    preparedStatementGetID.setString(1, guildName);
                    ResultSet resultSearch = preparedStatementGetID.executeQuery();

                    while (resultSearch.next()) {
                        int id = resultSearch.getInt("id");
                        for(String servers : mainConfig.getConfig().getSection("guilds-group").getKeys()) {
                            createSubGuildServer(connection,servers,id);
                        }
                    }
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
        String insertQuery = "INSERT INTO " + "xsguilds_bungee_main" + " (Guild, GuildName, Players) "
                + "VALUES (?, ?, ?)";

        try (PreparedStatement preparedStatementInsert = connection.prepareStatement(insertQuery)) {
            preparedStatementInsert.setString(1, guild);
            preparedStatementInsert.setString(2, guildName);
            preparedStatementInsert.setString(3, "[LEADER:" + leader + "]");
            preparedStatementInsert.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private static void createSubGuildServer(Connection connection,String subGroup,int ref) {
        String insertQuery = "INSERT INTO " + ("xsguilds_bungee_"+subGroup) + " (Reference, Level, Tech) "
                + "VALUES (?, ?, ?)";

        try (PreparedStatement preparedStatementInsert = connection.prepareStatement(insertQuery)) {
            preparedStatementInsert.setInt(1, ref);
            preparedStatementInsert.setInt(2, 1);
            preparedStatementInsert.setString(3, "");
            preparedStatementInsert.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
