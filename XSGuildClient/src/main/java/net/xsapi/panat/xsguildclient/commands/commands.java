package net.xsapi.panat.xsguildclient.commands;

import net.xsapi.panat.xsguildclient.config.mainConfig;
import net.xsapi.panat.xsguildclient.config.messagesConfig;
import net.xsapi.panat.xsguildclient.handler.XSGuildsHandler;
import net.xsapi.panat.xsguildclient.handler.XSHandler;
import net.xsapi.panat.xsguildclient.handler.XSRedisHandler;
import net.xsapi.panat.xsguildclient.objects.XSGuilds;
import net.xsapi.panat.xsguildclient.utils.XSDATA_TYPE;
import net.xsapi.panat.xsguildclient.utils.XSUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

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
                } else if(args.length == 1) {
                    if(args[0].equalsIgnoreCase("leave")) {

                    } else if(args[0].equalsIgnoreCase("disband")) {

                        if(!XSGuildsHandler.getPlayers().containsKey(p.getName())) {
                            p.sendMessage(XSUtils.decodeTextFromConfig("no_guild"));
                            return false;
                        }
                        String server = XSGuildsHandler.getPlayers().get(p.getName()).split("<SPLIT>")[0];
                        String guild = XSGuildsHandler.getPlayers().get(p.getName()).split("<SPLIT>")[1];
                        XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);

                        if(xsGuilds.getLeader().equalsIgnoreCase(p.getName())) {

                            if(xsGuilds.getMembers().size() > 1) {
                                p.sendMessage(XSUtils.decodeTextFromConfig("disband_only_last"));
                                return false;
                            }

                            XSGuildsHandler.getPlayers().remove(p.getName());
                            XSGuildsHandler.getGuildList().remove(guild);

                            XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+"_bungeecord",XSDATA_TYPE.DISBAND+"<SPLIT>" + server + ";" + guild);
                            p.sendMessage(XSUtils.decodeTextFromConfig("disband"));
                            return true;
                        } else {
                            p.sendMessage(XSUtils.decodeTextFromConfig("disband_only_leader"));
                            return false;
                        }

                    }
                } else if(args.length == 2) {
                    if(args[0].equalsIgnoreCase("create")) {

                        if(XSGuildsHandler.getPlayers().containsKey(p.getName())) {
                            p.sendMessage(XSUtils.decodeTextFromConfig("already_in_guild"));
                            return false;
                        }
                        String name = args[1].replace('&','§');

                        name = name.replaceAll("<[^<>]*>", "");
                        name = ChatColor.stripColor(name);

                        if(name.length() < mainConfig.customConfig.getInt("configuration.min_length")) {
                            p.sendMessage(XSUtils.decodeTextFromConfig("min_name"));
                            return false;
                        }
                        if(name.length() > mainConfig.customConfig.getInt("configuration.max_length")) {
                            p.sendMessage(XSUtils.decodeTextFromConfig("max_name"));
                            return false;
                        }

                        String nameWithColor = args[1].replace('&','§');

                        XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+"_bungeecord", XSDATA_TYPE.CREATE +"<SPLIT>" + p.getName() + ";" + ChatColor.stripColor(name) + ";" + nameWithColor);
                        p.sendMessage(XSUtils.decodeTextFromConfig("create").replace("%guild_name%",nameWithColor));
                        return true;
                    } else if(args[0].equalsIgnoreCase("invite")) {
                        String playerName = args[1];

                        if(!XSGuildsHandler.getPlayers().containsKey(p.getName())) {
                            p.sendMessage(XSUtils.decodeTextFromConfig("no_guild"));
                            return false;
                        }

                        String guild = XSGuildsHandler.getPlayers().get(p.getName()).split("<SPLIT>")[1];
                        XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);

                        if(!(xsGuilds.getLeader().equalsIgnoreCase(p.getName()) || xsGuilds.getSubleader().contains(p.getName()))) {
                            p.sendMessage(XSUtils.decodeTextFromConfig("required_permission_to_do"));
                            return false;
                        }

                        if(XSGuildsHandler.getPlayers().containsKey(playerName)) {
                            p.sendMessage(XSUtils.decodeTextFromConfig("other_already_in_guild"));
                            return false;
                        }

                        XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+"_bungeecord",XSDATA_TYPE.INVITE+"<SPLIT>"+XSHandler.getServername()+";"+playerName+";"+guild+";"+p.getName());
                        return true;

                    } else if(args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("decline")) {
                        String guildName = args[1];
                        XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+"_bungeecord",XSDATA_TYPE.INVITE_RESPOND+"<SPLIT>"+args[0]+";"+guildName+";"+p.getName());
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
