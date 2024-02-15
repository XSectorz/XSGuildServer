package net.xsapi.panat.xsguildbungee.utils.tasks;

import net.xsapi.panat.xsguildbungee.config.mainConfig;
import net.xsapi.panat.xsguildbungee.core;
import net.xsapi.panat.xsguildbungee.handler.XSDatabaseHandler;
import net.xsapi.panat.xsguildbungee.handler.XSGuildsHandler;
import net.xsapi.panat.xsguildbungee.objects.XSGuilds;
import net.xsapi.panat.xsguildbungee.objects.XSSubGuilds;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class task_save {
    public task_save() {
        core.getPlugin().getProxy().getScheduler().schedule(core.getPlugin(), new Runnable() {
            @Override
            public void run() {

                try {
                    Connection connection = DriverManager.getConnection(XSDatabaseHandler.getJdbcUrl(),XSDatabaseHandler.getUSER(),XSDatabaseHandler.getPASS());
                    for(Map.Entry<String,XSGuilds> guild : XSGuildsHandler.getGuildList().entrySet()) {
                        XSDatabaseHandler.updateMainGuild(connection,guild.getValue());
                        //core.getPlugin().getLogger().info("Saved : " + guild.getKey());
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
               // core.getPlugin().getLogger().info("Saved complete!....");

            }
        }, 0L, mainConfig.getConfig().getInt("auto-saved"), TimeUnit.SECONDS);
    }
}
