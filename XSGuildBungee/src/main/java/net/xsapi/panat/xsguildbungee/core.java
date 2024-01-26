package net.xsapi.panat.xsguildbungee;

import net.md_5.bungee.api.plugin.Plugin;
import net.xsapi.panat.xsguildbungee.config.configLoader;
import net.xsapi.panat.xsguildbungee.handler.XSDatabaseHandler;
import net.xsapi.panat.xsguildbungee.handler.XSHandler;

import java.io.IOException;

public final class core extends Plugin {

    private static core plugin;

    public static core getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {

        getLogger().info("[PLUGIN] ENABLED!");
        plugin = this;

        try {
            new configLoader();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        XSHandler.loadEvent();
        XSDatabaseHandler.sqlConnection();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
