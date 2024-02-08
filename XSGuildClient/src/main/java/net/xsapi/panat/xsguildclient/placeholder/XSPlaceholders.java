package net.xsapi.panat.xsguildclient.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.xsapi.panat.xsguildclient.config.mainConfig;
import net.xsapi.panat.xsguildclient.handler.XSGuildsHandler;
import net.xsapi.panat.xsguildclient.objects.XSGuilds;
import net.xsapi.panat.xsguildclient.utils.XSUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class XSPlaceholders extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "XSGuilds";
    }

    @Override
    public @NotNull String getAuthor() {
        return "PanatXsectorZ";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {

        if(params.equalsIgnoreCase("guild")) {
            String text = mainConfig.customConfig.getString("configuration.format.nonGuild");
            if(XSGuildsHandler.getPlayers().containsKey(player.getName())) {
                text = mainConfig.customConfig.getString("configuration.format.default");
                //XSGuildsHandler.getPlayers().get(player.getName()) ===> server:guild
                String server = XSGuildsHandler.getPlayers().get(player.getName()).split("<SPLIT>")[0];
                String guild = XSGuildsHandler.getPlayers().get(player.getName()).split("<SPLIT>")[1];
                XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);
                text = text.replace("%guild_name%",xsGuilds.getGuildName());
                text = text.replace("%guild_level%",xsGuilds.getSubGuilds().get(server).getLevel()+"");
                text = text.replace("§","&");

                return XSUtils.decodeTextNotReplace(text);
            }

            return XSUtils.decodeText(text);
        }
            return null;
    }
}
