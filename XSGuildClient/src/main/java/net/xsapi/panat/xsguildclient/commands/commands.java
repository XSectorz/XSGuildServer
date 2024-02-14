package net.xsapi.panat.xsguildclient.commands;

import net.xsapi.panat.xsguildclient.config.mainConfig;
import net.xsapi.panat.xsguildclient.config.messagesConfig;
import net.xsapi.panat.xsguildclient.handler.XSGuildsHandler;
import net.xsapi.panat.xsguildclient.handler.XSHandler;
import net.xsapi.panat.xsguildclient.handler.XSRedisHandler;
import net.xsapi.panat.xsguildclient.objects.XSGuilds;
import net.xsapi.panat.xsguildclient.utils.XSDATA_TYPE;
import net.xsapi.panat.xsguildclient.utils.XSGUILD_POSITIONS;
import net.xsapi.panat.xsguildclient.utils.XSUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                    if(args[0].equalsIgnoreCase("disband")) {

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

                            XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+"_bungeecord",XSDATA_TYPE.DISBAND+"<SPLIT>" + server + ";" + guild + ";" + p.getName());

                            if(XSHandler.getPlayerInGuildChat().contains(p.getName())) {
                                XSHandler.getPlayerInGuildChat().remove(p.getName());
                            }

                            p.sendMessage(XSUtils.decodeTextFromConfig("disband"));
                            return true;
                        } else {
                            p.sendMessage(XSUtils.decodeTextFromConfig("disband_only_leader"));
                            return false;
                        }

                    } else if(args[0].equalsIgnoreCase("leave")) {
                        if(!XSGuildsHandler.getPlayers().containsKey(p.getName())) {
                            p.sendMessage(XSUtils.decodeTextFromConfig("no_guild"));
                            return false;
                        }

                        String guild = XSGuildsHandler.getPlayers().get(p.getName()).split("<SPLIT>")[1];
                        XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);

                        if(xsGuilds.getLeader().equalsIgnoreCase(p.getName())) {
                            p.sendMessage(XSUtils.decodeTextFromConfig("guild_leave_leader"));
                            return false;
                        }

                        p.sendMessage(XSUtils.decodeTextFromConfig("guild_leave"));
                        XSGuildsHandler.getPlayers().remove(p.getName());
                        XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+"_bungeecord", XSDATA_TYPE.LEAVE_GUILD +"<SPLIT>" + p.getName() + ";" + guild);
                        if(XSHandler.getPlayerInGuildChat().contains(p.getName())) {
                            XSHandler.getPlayerInGuildChat().remove(p.getName());
                        }
                    } else if(args[0].equalsIgnoreCase("balance")) {

                        if(!XSGuildsHandler.getPlayers().containsKey(p.getName())) {
                            p.sendMessage(XSUtils.decodeTextFromConfig("no_guild"));
                            return false;
                        }

                        String guild = XSGuildsHandler.getPlayers().get(p.getName()).split("<SPLIT>")[1];
                        XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);
                        String patterns = "%guild_balance_[a-zA-Z0-9_]+%";
                        String patternsMax = "%guild_balance_max_[a-zA-Z0-9_]+%";
                        Pattern pattern = Pattern.compile(patterns);
                        Pattern patternMax = Pattern.compile(patternsMax);
                        DecimalFormat df = new DecimalFormat("#.##");

                        for(String text : messagesConfig.customConfig.getStringList("system.balance_check")) {
                            Matcher m = pattern.matcher(text);
                            Matcher mMax = patternMax.matcher(text);
                            if(m.find()) {

                                String subServer = m.group().replace("guild_balance_","").replace("%","");
                                double bal;

                                if(subServer.equalsIgnoreCase("main")) {
                                    bal = xsGuilds.getBalance();
                                } else {
                                    bal = xsGuilds.getSubGuilds().get(subServer).getBalance();
                                }

                                text = text.replace("%guild_balance_"+subServer+"%",df.format(bal));
                            }
                            if(mMax.find()) {
                                String subServer = mMax.group().replace("guild_balance_max_","").replace("%","");
                                double maxBal;

                                if(subServer.equalsIgnoreCase("main")) {
                                    maxBal = xsGuilds.getMaxBalance();
                                } else {
                                    maxBal = xsGuilds.getSubGuilds().get(subServer).getMaxBalance();
                                }
                                text = text.replace("%guild_balance_max_"+subServer+"%",df.format(maxBal));
                            }
                            text = text.replace("%guild_name%",guild);
                            text = XSUtils.decodeText(text);
                            p.sendMessage(text);
                        }

                    } else if(args[0].equalsIgnoreCase("chat")) {
                        if(!XSGuildsHandler.getPlayers().containsKey(p.getName())) {
                            p.sendMessage(XSUtils.decodeTextFromConfig("no_guild"));
                            return false;
                        }

                        String toggle;
                        if(!XSHandler.getPlayerInGuildChat().contains(p.getName())) {
                            toggle = messagesConfig.customConfig.getString("system.toggles.status_on");
                            XSHandler.getPlayerInGuildChat().add(p.getName());
                            XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+"_bungeecord", XSDATA_TYPE.GUILD_CHAT_CHANGE_STATE +"<SPLIT>" + p.getName() + ";YES");
                        } else {
                            toggle = messagesConfig.customConfig.getString("system.toggles.status_off");
                            XSHandler.getPlayerInGuildChat().remove(p.getName());
                            XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+"_bungeecord", XSDATA_TYPE.GUILD_CHAT_CHANGE_STATE +"<SPLIT>" + p.getName() + ";NO");
                        }

                        String text =  messagesConfig.customConfig.getString("system.chat_toggle").replace("%prefix%",messagesConfig.customConfig.getString("system.prefix"));

                        text = text.replace("%toggle_type%",toggle);

                        p.sendMessage(XSUtils.decodeText(text));
                        return true;
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

                    } else if(args[0].equalsIgnoreCase("uninvite")) {
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

                        if(!xsGuilds.getPendingInvite().containsKey(playerName)) {
                            p.sendMessage(XSUtils.decodeTextFromConfig("uninvite_not_found"));
                            return false;
                        }
                        p.sendMessage(XSUtils.decodeTextFromConfig("uninvite_success").replace("%player_name%",playerName));
                        XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+"_bungeecord",XSDATA_TYPE.UNINVITE+"<SPLIT>"+guild+";"+playerName);
                        return true;
                    } else if(args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("decline")) {
                        String guildName = args[1];
                        XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+"_bungeecord",XSDATA_TYPE.INVITE_RESPOND+"<SPLIT>"+args[0]+";"+guildName+";"+p.getName());
                        return true;
                    } else if(args[0].equalsIgnoreCase("kick")) {
                        String target = args[1];

                        if(!XSGuildsHandler.getPlayers().containsKey(p.getName())) {
                            p.sendMessage(XSUtils.decodeTextFromConfig("no_guild"));
                            return false;
                        }

                        String guild = XSGuildsHandler.getPlayers().get(p.getName()).split("<SPLIT>")[1];
                        XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);

                        if(!xsGuilds.getLeader().equalsIgnoreCase(p.getName())) {
                            p.sendMessage(XSUtils.decodeTextFromConfig("required_permission_to_do"));
                            return false;
                        }

                        if(!xsGuilds.getMembers().containsKey(target)) {
                            p.sendMessage(XSUtils.decodeTextFromConfig("player_not_in_your_guild"));
                            return false;
                        }

                        if(target.equalsIgnoreCase(p.getName())) {
                            p.sendMessage(XSUtils.decodeTextFromConfig("guild_kick_self"));
                            return false;
                        }

                        p.sendMessage(XSUtils.decodeTextFromConfig("guild_kick_sender").replace("%player_name%",target));
                        XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+"_bungeecord",XSDATA_TYPE.KICK_REQUEST+"<SPLIT>"+guild+";"+target);
                        return true;

                    } else if(args[0].equalsIgnoreCase("transfer")) {
                        String target = args[1];

                        if(!XSGuildsHandler.getPlayers().containsKey(p.getName())) {
                            p.sendMessage(XSUtils.decodeTextFromConfig("no_guild"));
                            return false;
                        }

                        String guild = XSGuildsHandler.getPlayers().get(p.getName()).split("<SPLIT>")[1];
                        XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);

                        if(!xsGuilds.getLeader().equalsIgnoreCase(p.getName())) {
                            p.sendMessage(XSUtils.decodeTextFromConfig("required_permission_to_do"));
                            return false;
                        }

                        if(target.equalsIgnoreCase(p.getName())) {
                            p.sendMessage(XSUtils.decodeTextFromConfig("guild_transfer_self"));
                            return false;
                        }

                        if(!xsGuilds.getMembers().containsKey(target)) {
                            p.sendMessage(XSUtils.decodeTextFromConfig("player_not_in_your_guild"));
                            return false;
                        }

                        p.sendMessage(XSUtils.decodeTextFromConfig("guild_transfer_sender").replace("%player_name%",target));
                        XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+"_bungeecord",XSDATA_TYPE.TRANSFER_LEADER_REQUEST+"<SPLIT>"+guild+";"+target);
                        return true;
                    } else if(args[0].equalsIgnoreCase("promote")) {
                        String target = args[1];
                        if(!XSGuildsHandler.getPlayers().containsKey(p.getName())) {
                            p.sendMessage(XSUtils.decodeTextFromConfig("no_guild"));
                            return false;
                        }

                        String guild = XSGuildsHandler.getPlayers().get(p.getName()).split("<SPLIT>")[1];
                        XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);

                        if(!xsGuilds.getLeader().equalsIgnoreCase(p.getName())) {
                            p.sendMessage(XSUtils.decodeTextFromConfig("required_permission_to_do"));
                            return false;
                        }

                        if(target.equalsIgnoreCase(p.getName())) {
                            p.sendMessage(XSUtils.decodeTextFromConfig("promote_self"));
                            return false;
                        }

                        if(!xsGuilds.getMembers().containsKey(target)) {
                            p.sendMessage(XSUtils.decodeTextFromConfig("player_not_in_your_guild"));
                            return false;
                        }

                        if(xsGuilds.getMembers().get(target).equalsIgnoreCase(XSGUILD_POSITIONS.SUB_LEADER.toString())) {
                            p.sendMessage(XSUtils.decodeTextFromConfig("promote_cant"));
                            return false;
                        }

                        if(xsGuilds.getSubleader().size() == 4) {
                            p.sendMessage(XSUtils.decodeTextFromConfig("promote_sub_leader_max"));
                            return false;
                        }

                        int currentIndexRank = XSGUILD_POSITIONS.valueOf(xsGuilds.getMembers().get(target)).ordinal();

                        String nextRank = String.valueOf(XSGUILD_POSITIONS.values()[currentIndexRank-1]).toLowerCase();
                        String configRank =  messagesConfig.customConfig.getString("system.ranks."+nextRank);
                        String rankWithColor = XSUtils.decodeText(configRank);

                        p.sendMessage(XSUtils.decodeTextFromConfig("promote_sender").replace("%player_name%",target).replace("%guild_rank%",rankWithColor));

                        XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+"_bungeecord",XSDATA_TYPE.PROMOTE_REQUEST+"<SPLIT>"+guild+";"+target+";"+nextRank);
                        return true;
                    } else if(args[0].equalsIgnoreCase("demote")) {
                        String target = args[1];
                        if(!XSGuildsHandler.getPlayers().containsKey(p.getName())) {
                            p.sendMessage(XSUtils.decodeTextFromConfig("no_guild"));
                            return false;
                        }

                        String guild = XSGuildsHandler.getPlayers().get(p.getName()).split("<SPLIT>")[1];
                        XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);

                        if(!xsGuilds.getLeader().equalsIgnoreCase(p.getName())) {
                            p.sendMessage(XSUtils.decodeTextFromConfig("required_permission_to_do"));
                            return false;
                        }

                        if(target.equalsIgnoreCase(p.getName())) {
                            p.sendMessage(XSUtils.decodeTextFromConfig("demote_self"));
                            return false;
                        }

                        if(!xsGuilds.getMembers().containsKey(target)) {
                            p.sendMessage(XSUtils.decodeTextFromConfig("player_not_in_your_guild"));
                            return false;
                        }

                        if(xsGuilds.getMembers().get(target).equalsIgnoreCase(XSGUILD_POSITIONS.NEW_MEMBER.toString())) {
                            p.sendMessage(XSUtils.decodeTextFromConfig("demote_cant"));
                            return false;
                        }

                        int currentIndexRank = XSGUILD_POSITIONS.valueOf(xsGuilds.getMembers().get(target)).ordinal();
                        String nextRank = String.valueOf(XSGUILD_POSITIONS.values()[currentIndexRank+1]).toLowerCase();
                        String configRank =  messagesConfig.customConfig.getString("system.ranks."+nextRank);
                        String rankWithColor = XSUtils.decodeText(configRank);

                        p.sendMessage(XSUtils.decodeTextFromConfig("demote_sender").replace("%player_name%",target).replace("%guild_rank%",rankWithColor));
                        XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+"_bungeecord",XSDATA_TYPE.DEMOTE_REQUEST+"<SPLIT>"+guild+";"+target+";"+nextRank);
                        return true;
                    }
                } else if(args.length == 3) { //xsguilds deposit <points/coins> <amount>
                     if(args[0].equalsIgnoreCase("deposit")) {
                         String type = args[1];
                         if(!XSGuildsHandler.getPlayers().containsKey(p.getName())) {
                             p.sendMessage(XSUtils.decodeTextFromConfig("no_guild"));
                             return false;
                         }

                         if(type.equalsIgnoreCase("points") || type.equalsIgnoreCase("coins")) {

                             double amount;

                             try {
                                 amount = Double.parseDouble(args[2]);
                             } catch (NumberFormatException nf) {
                                 p.sendMessage(XSUtils.decodeTextFromConfig("not_a_number"));
                                 return false;
                             }

                             if(amount <= 0) {
                                 p.sendMessage(XSUtils.decodeTextFromConfig("more_than_zero"));
                                 return false;
                             }
                             double balance;
                             String guild = XSGuildsHandler.getPlayers().get(p.getName()).split("<SPLIT>")[1];
                             XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);
                             if(type.equalsIgnoreCase("points")) {
                                 balance = (double) XSHandler.getSCPoint().look(p.getUniqueId());
                                 if(balance < amount) {
                                     p.sendMessage(XSUtils.decodeTextFromConfig("points_balance_cant_afford"));
                                     return false;
                                 }

                                 if(amount+xsGuilds.getBalance() > xsGuilds.getMaxBalance()) {
                                     p.sendMessage(XSUtils.decodeTextFromConfig("deposit_out_of_capacity"));
                                     return false;
                                 }

                                 XSHandler.getSCPoint().take(p.getUniqueId(),(int) amount);
                                 p.sendMessage(XSUtils.decodeTextFromConfig("deposit_points").replace("%amount%",String.valueOf(amount)));
                                 XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+"_bungeecord",XSDATA_TYPE.DEPOSIT_POINTS+"<SPLIT>"+guild+";"+amount);
                             } else {
                                 balance = XSHandler.getEconomy().getBalance(p);
                                 if(balance < amount) {
                                     p.sendMessage(XSUtils.decodeTextFromConfig("coins_balance_cant_afford"));
                                     return false;
                                 }
                                 String server = XSGuildsHandler.getPlayers().get(p.getName()).split("<SPLIT>")[0];

                                 if(amount+xsGuilds.getSubGuilds().get(server).getBalance() > xsGuilds.getSubGuilds().get(server).getMaxBalance()) {
                                     p.sendMessage(XSUtils.decodeTextFromConfig("deposit_out_of_capacity"));
                                     return false;
                                 }

                                 XSHandler.getEconomy().withdrawPlayer(p,amount);

                                 p.sendMessage(XSUtils.decodeTextFromConfig("deposit_coins").replace("%amount%",String.valueOf(amount)));
                                 XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+"_bungeecord",XSDATA_TYPE.DEPOSIT_COINS+"<SPLIT>"+server+";"+guild+";"+amount);
                                 return true;
                             }
                             return true;

                         } else {
                             p.sendMessage(XSUtils.decodeTextFromConfig("format_not_match"));
                             return false;
                         }

                    } else if(args[0].equalsIgnoreCase("withdraw")) {
                         String type = args[1];
                         if(!XSGuildsHandler.getPlayers().containsKey(p.getName())) {
                             p.sendMessage(XSUtils.decodeTextFromConfig("no_guild"));
                             return false;
                         }

                         if(type.equalsIgnoreCase("points") || type.equalsIgnoreCase("coins")) {

                             double amount;

                             try {
                                 amount = Double.parseDouble(args[2]);
                             } catch (NumberFormatException nf) {
                                 p.sendMessage(XSUtils.decodeTextFromConfig("not_a_number"));
                                 return false;
                             }

                             if(amount <= 0) {
                                 p.sendMessage(XSUtils.decodeTextFromConfig("more_than_zero"));
                                 return false;
                             }
                             double balance;
                             String guild = XSGuildsHandler.getPlayers().get(p.getName()).split("<SPLIT>")[1];
                             XSGuilds xsGuilds = XSGuildsHandler.getGuildList().get(guild);

                             if(type.equalsIgnoreCase("points")) {
                                 balance = xsGuilds.getBalance();
                                 if(balance < amount) {
                                     p.sendMessage(XSUtils.decodeTextFromConfig("withdraw_cant_afford"));
                                     return false;
                                 }

                                 XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+"_bungeecord",XSDATA_TYPE.WITHDRAW_POINTS+"<SPLIT>"+guild+";"+amount+";"+p.getName());
                             } else {
                                 String server = XSGuildsHandler.getPlayers().get(p.getName()).split("<SPLIT>")[0];

                                 balance = xsGuilds.getSubGuilds().get(server).getBalance();
                                 if(balance < amount) {
                                     p.sendMessage(XSUtils.decodeTextFromConfig("withdraw_cant_afford"));
                                     return false;
                                 }

                                 XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+"_bungeecord",XSDATA_TYPE.WITHDRAW_COINS+"<SPLIT>"+server+";"+guild+";"+amount+";"+p.getName());
                             }
                             return true;

                         } else {
                             p.sendMessage(XSUtils.decodeTextFromConfig("format_not_match"));
                             return false;
                         }
                    }
                }
            }
        }
        return false;
    }
}
