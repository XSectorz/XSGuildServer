package net.xsapi.panat.xsguildclient.utils;

import net.xsapi.panat.xsguildclient.handler.XSGuildsHandler;
import net.xsapi.panat.xsguildclient.objects.XSGuilds;
import net.xsapi.panat.xsguildclient.objects.XSSubGuilds;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class XSTabComplete implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        Player p = (Player) sender;

        if (command.getName().equalsIgnoreCase("xsguild")) {

            if(args.length == 1) {
                completions.add("create");
                completions.add("invite");
                completions.add("kick");
                completions.add("transfer");
                completions.add("sethome");
                completions.add("delhome");
                completions.add("home");
                completions.add("promote");
                completions.add("demote");
                completions.add("menu");
                completions.add("leave");
                completions.add("disband");
                completions.add("chat");
                completions.add("deposit");
                completions.add("withdraw");
            } else if (args.length == 2) {
                if(args[0].equalsIgnoreCase("home")) {
                    if(!XSGuildsHandler.getPlayers().containsKey(p.getName())) {
                        return completions;
                    }

                    String guild = XSGuildsHandler.getPlayers().get(p.getName()).split("<SPLIT>")[1];
                    XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);
                    String server = XSGuildsHandler.getPlayers().get(p.getName()).split("<SPLIT>")[0];
                    XSSubGuilds xsSubGuilds = xsGuilds.getSubGuilds().get(server);

                    if(xsSubGuilds.getHomeList().isEmpty()) {
                        return completions;
                    }
                    Set<String> keySet = xsSubGuilds.getHomeList().keySet();
                    return new ArrayList<>(keySet);
                } else if(args[0].equalsIgnoreCase("deposit") || args[0].equalsIgnoreCase("withdraw")) {
                    completions.add("points");
                    completions.add("coins");
                }
            }
        }

        return completions;
    }
}
