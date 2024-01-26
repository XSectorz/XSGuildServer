package net.xsapi.panat.xsguildbungee.handler;

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

    public static void sqlConnection() {
        String host = mainConfig.getConfig().getString("database.host");
        DB_NAME =  mainConfig.getConfig().getString("database.dbName");
        JDBC_URL = "jdbc:mysql://" + host +  "/" + DB_NAME;
        USER = mainConfig.getConfig().getString("database.user");
        PASS = mainConfig.getConfig().getString("database.password");


        try {
            Connection connection = DriverManager.getConnection(JDBC_URL,USER,PASS);

            Statement statement = connection.createStatement();

            String createTableQuery = "CREATE TABLE IF NOT EXISTS " + getGlobalTable() + " ("
                    + "id INT PRIMARY KEY AUTO_INCREMENT, "
                    + "Guild VARCHAR(32), "
                    + "GuildName TEXT, "
                    + "Players TEXT"
                    + ")";
            statement.executeUpdate(createTableQuery);
            statement.close();
            connection.close();

            core.getPlugin().getLogger().info("§x§E§7§F§F§0§0[XSGUILDS] Database : §x§6§0§F§F§0§0Connected");
        } catch (SQLException e) {
            core.getPlugin().getLogger().info("§x§E§7§F§F§0§0[XSGUILDS] Database : §x§C§3§0§C§2§ANot Connected");
            e.printStackTrace();
        }
    }
}
