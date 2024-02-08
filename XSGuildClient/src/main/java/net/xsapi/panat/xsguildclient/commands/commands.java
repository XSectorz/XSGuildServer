package net.xsapi.panat.xsguildclient.commands;

import net.xsapi.panat.xsguildclient.config.messagesConfig;
import net.xsapi.panat.xsguildclient.handler.XSHandler;
import net.xsapi.panat.xsguildclient.handler.XSRedisHandler;
import net.xsapi.panat.xsguildclient.utils.XSDATA_TYPE;
import net.xsapi.panat.xsguildclient.utils.XSUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class commands implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String arg, @NotNull String[] args) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            /* TYPE:ARGUMENT */

            if(command.getName().equalsIgnoreCase("xsguild")) {
                if(args.length == 0) {
                    for(String section : messagesConfig.customConfig.getConfigurationSection("system.helps").getKeys(false)) {
                        p.sendMessage(XSUtils.decodeText(messagesConfig.customConfig.getString("system.helps."+ section)));
                    }
                } else if(args.length == 2) {
                    if(args[0].equalsIgnoreCase("create")) {
                        String name = args[1].replace('&','§');
                        XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+"_bungeecord", XSDATA_TYPE.CREATE +"<SPLIT>" + p.getName() + ";" + ChatColor.stripColor(name) + ";" + name);

                        //Bukkit.broadcastMessage("Name" + ChatColor.stripColor(name));
                    }
                }
            }
        }
        return false;
    }
}
