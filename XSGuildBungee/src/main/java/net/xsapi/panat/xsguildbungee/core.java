package net.xsapi.panat.xsguildbungee;

import net.md_5.bungee.api.plugin.Plugin;
import net.xsapi.panat.xsguildbungee.config.configLoader;
import net.xsapi.panat.xsguildbungee.handler.XSDatabaseHandler;
import net.xsapi.panat.xsguildbungee.handler.XSHandler;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public final class core extends Plugin {

    private static core plugin;

    public static core getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {

        plugin = this;

        try {
            new configLoader();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        XSHandler.initSystem();
        XSDatabaseHandler.createSQLDatabase();

        /*getProxy().getScheduler().schedule(plugin, new Runnable() {
            @Override
            public void run() {

                getLogger().info("REPEATING....");
            }
        }, 0L, 10, TimeUnit.SECONDS);*/

    }

    @Override
    public void onDisable() {

    }
}
