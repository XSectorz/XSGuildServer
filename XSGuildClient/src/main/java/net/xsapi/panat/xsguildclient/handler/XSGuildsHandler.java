package net.xsapi.panat.xsguildclient.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.xsapi.panat.xsguildclient.objects.XSGuilds;
import net.xsapi.panat.xsguildclient.objects.XSSubGuilds;
import org.bukkit.Bukkit;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class XSGuildsHandler {

    public static HashMap<String, XSGuilds> guildList = new HashMap<>();

    public static HashMap<String, XSGuilds> getGuildList() {
        return guildList;
    }

    public static void loadGuildData(String json) {
        Gson gson = new Gson();

        HashMap<String, XSGuilds> guildList = gson.fromJson(json, new TypeToken<HashMap<String, XSGuilds>>(){}.getType());

        for (Map.Entry<String, XSGuilds> guild : guildList.entrySet()) {
            Bukkit.getLogger().info("GUILD: " + guild.getKey());
            Bukkit.getLogger().info("DATA: ");
            for (Map.Entry<String, XSSubGuilds> subguild : guild.getValue().getSubGuilds().entrySet()) {
                Bukkit.getLogger().info("SERVER: " + subguild.getValue().getSubServer());
                Bukkit.getLogger().info("LEVEL: " + subguild.getValue().getLevel());
                Bukkit.getLogger().info("TECH: " + subguild.getValue().getTech());
            }
            Bukkit.getLogger().info("--------------------------");
            getGuildList().put(guild.getKey(),guild.getValue());
        }
    }
}
