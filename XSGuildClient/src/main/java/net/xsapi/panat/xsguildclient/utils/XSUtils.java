package net.xsapi.panat.xsguildclient.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.xsapi.panat.xsguildclient.config.menuConfig;
import net.xsapi.panat.xsguildclient.config.messagesConfig;
import net.xsapi.panat.xsguildclient.handler.XSHandler;
import net.xsapi.panat.xsguildclient.handler.XSMenuHandler;
import net.xsapi.panat.xsguildclient.objects.XSGuilds;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XSUtils {

    public static String decodeText(String str) {
        Component parsedMessage = MiniMessage.builder().build().deserialize(str);
        String legacy = LegacyComponentSerializer.legacyAmpersand().serialize(parsedMessage);
        return legacy.replace('&', '§');
    }

    public static String decodeTextFromConfig(String section) {
        String text = Objects.requireNonNull(messagesConfig.customConfig.getString("system." + section));
        text = text.replace("%prefix%", Objects.requireNonNull(messagesConfig.customConfig.getString("system.prefix")));
        Component parsedMessage = MiniMessage.builder().build().deserialize(text);
        String legacy = LegacyComponentSerializer.legacyAmpersand().serialize(parsedMessage);
        return legacy.replace('&', '§');
    }

    public static String decodeTextNotReplace(String str) {
        Component parsedMessage = MiniMessage.builder().build().deserialize(str);
        return LegacyComponentSerializer.legacyAmpersand().serialize(parsedMessage);
    }

    public static String decodeStringWithPlaceholder(String str, XSGuilds xsGuilds,String server) {
        String patternsBalance = "%guild_balance_[a-zA-Z0-9_()]+%";
        String patternsBalanceMax = "%guild_max_balance_[a-zA-Z0-9_()]+%";
        String patternsMember = "%guild_home_[a-zA-Z0-9_()]+%";
        String patternsLevel = "%guild_level_[a-zA-Z0-9_()]+%";
        String patternsMemberMax = "%guild_max_home_[a-zA-Z0-9_()]+%";
        String patternsLevelNext = "%guild_next_level_[a-zA-Z0-9_()]+%";
        String patternsBalNext = "%guild_max_balance_next_[a-zA-Z0-9_()]+%";
        String patternsHomeNext = "%guild_max_home_next_[a-zA-Z0-9_()]+%";

        Pattern pattern = Pattern.compile(patternsBalance);
        Pattern patternMax = Pattern.compile(patternsBalanceMax);
        Pattern patternLevel = Pattern.compile(patternsLevel);
        Pattern patternHome = Pattern.compile(patternsMember);
        Pattern patternHomeMax = Pattern.compile(patternsMemberMax);
        Pattern patternLvlNext = Pattern.compile(patternsLevelNext);
        Pattern patternBalNext = Pattern.compile(patternsBalNext);
        Pattern patternHomeNext = Pattern.compile(patternsHomeNext);

        Matcher m = pattern.matcher(str);
        Matcher mMax = patternMax.matcher(str);
        Matcher mHome = patternHome.matcher(str);
        Matcher mHomeMax = patternHomeMax.matcher(str);
        Matcher mLevel = patternLevel.matcher(str);
        Matcher mLevelNext = patternLvlNext.matcher(str);
        Matcher mBalNext = patternBalNext.matcher(str);
        Matcher mHomeNext = patternHomeNext.matcher(str);

        DecimalFormat df = new DecimalFormat("#.##");
        if(mLevelNext.find()) {
            String lvl;
            String serverGroup = mLevelNext.group().replace("guild_next_level_","").replace("%","");

            if(serverGroup.equalsIgnoreCase("main")) {
                if(!XSHandler.getMainClanUpgrades().containsKey(xsGuilds.getGuildLevel()+1)) { //reach max level
                    lvl = XSUtils.decodeTextFromConfig("upgrade_max_placeholder");
                } else {
                    lvl = String.valueOf(xsGuilds.getGuildLevel()+1);
                }
                str = str.replace("%guild_next_level_main%",lvl);
            } else {
                if(!XSHandler.getSubClanUpgrades().containsKey(xsGuilds.getSubGuilds().get(server).getLevel()+1)) { //reach max level
                    lvl = XSUtils.decodeTextFromConfig("upgrade_max_placeholder");
                } else {
                    lvl = String.valueOf(xsGuilds.getSubGuilds().get(server).getLevel()+1);
                }
                str = str.replace("%guild_next_level_(server)%",lvl);
            }
        }
        if(str.contains("%guild_max_members_next_main%")) {
            String nextMember;
            if(!XSHandler.getMainClanUpgrades().containsKey(xsGuilds.getGuildLevel()+1)) { //reach max level
                nextMember = XSUtils.decodeTextFromConfig("upgrade_max_placeholder");
            } else {
                nextMember = XSHandler.getMainClanUpgrades().get(xsGuilds.getGuildLevel()+1).getNextUpgrades().get("MEMBERS");
            }
            str = str.replace("%guild_max_members_next_main%",nextMember);
        }
        if(mBalNext.find()) {
            String balNext;

            String serverGroup = mBalNext.group().replace("guild_max_balance_next_","").replace("%","");

            if(serverGroup.equalsIgnoreCase("main")) {
                if(!XSHandler.getMainClanUpgrades().containsKey(xsGuilds.getGuildLevel()+1)) { //reach max level
                    balNext = XSUtils.decodeTextFromConfig("upgrade_max_placeholder");
                } else {
                    balNext = XSHandler.getMainClanUpgrades().get(xsGuilds.getGuildLevel()+1).getNextUpgrades().get("BANK_CAPACITY");
                }
                str = str.replace("%guild_max_balance_next_main%",balNext);
            } else {
                if(!XSHandler.getSubClanUpgrades().containsKey(xsGuilds.getSubGuilds().get(server).getLevel()+1)) { //reach max level
                    balNext = XSUtils.decodeTextFromConfig("upgrade_max_placeholder");
                } else {
                    balNext = XSHandler.getSubClanUpgrades().get(xsGuilds.getSubGuilds().get(server).getLevel()+1).getNextUpgrades().get("BANK_CAPACITY");
                }
                str = str.replace("%guild_max_balance_next_(server)%",balNext);
            }
        }
        if(mHomeNext.find()) {
            String homeNext;

            if(!XSHandler.getSubClanUpgrades().containsKey(xsGuilds.getSubGuilds().get(server).getLevel()+1)) { //reach max level
                homeNext = XSUtils.decodeTextFromConfig("upgrade_max_placeholder");
            } else {
                homeNext = XSHandler.getSubClanUpgrades().get(xsGuilds.getSubGuilds().get(server).getLevel()+1).getNextUpgrades().get("HOME");
            }
            str = str.replace("%guild_max_home_next_(server)%",homeNext);
        }
        if(m.find()) {
            String subServer = m.group().replace("guild_balance_","").replace("%","");
            double bal;
            if(subServer.equalsIgnoreCase("main")) {
                bal = xsGuilds.getBalance();
            } else if(subServer.equalsIgnoreCase("(server)")) {
                bal = xsGuilds.getSubGuilds().get(server).getBalance();
                subServer = "(server)";
            } else {
                bal = xsGuilds.getSubGuilds().get(subServer).getBalance();
            }

            str = str.replace("%guild_balance_"+subServer+"%",df.format(bal));
        }
        if(mLevel.find()) {
            String subServer = mLevel.group().replace("guild_level_","").replace("%","");
            int lvl;
            if(subServer.equalsIgnoreCase("main")) {
                lvl = xsGuilds.getGuildLevel();
            } else if(subServer.equalsIgnoreCase("(server)")) {
                lvl = xsGuilds.getSubGuilds().get(server).getLevel();
                subServer = "(server)";
            } else {
                lvl = xsGuilds.getSubGuilds().get(subServer).getLevel();
            }
            str = str.replace("%guild_level_"+subServer+"%",String.valueOf(lvl));
        }
        if(mHome.find()) {
            String subServer = mHome.group().replace("guild_home_","").replace("%","");
            int home = 0;
            if(subServer.equalsIgnoreCase("(server)")) {
                home =xsGuilds.getSubGuilds().get(server).getHomeList().size();
                subServer = "(server)";
            } else {
                home = xsGuilds.getSubGuilds().get(subServer).getHomeList().size();
            }
            str = str.replace("%guild_home_"+subServer+"%",String.valueOf(home));

        }
        if(mHomeMax.find()) {
            String subServer = mHomeMax.group().replace("guild_max_home_","").replace("%","");
            int home = 0;
            if(subServer.equalsIgnoreCase("(server)")) {
                home = xsGuilds.getSubGuilds().get(server).getMaxHome();
                subServer = "(server)";
            } else {
                home = xsGuilds.getSubGuilds().get(subServer).getMaxHome();
            }
            str = str.replace("%guild_max_home_"+subServer+"%",String.valueOf(home));
        }
        if(mMax.find()) {
            String subServer = mMax.group().replace("guild_max_balance_","").replace("%","");
            double maxBal;

            if(subServer.equalsIgnoreCase("main")) {
                maxBal = xsGuilds.getMaxBalance();
            } else if(subServer.equalsIgnoreCase("(server)")) {
                maxBal = xsGuilds.getSubGuilds().get(server).getMaxBalance();
                subServer = "(server)";
            } else {
                maxBal = xsGuilds.getSubGuilds().get(subServer).getMaxBalance();
            }
            str = str.replace("%guild_max_balance_"+subServer+"%",df.format(maxBal));
        }
        if(str.contains("%required_coins%")) {
            String reqCoins;
            if(!XSHandler.getSubClanUpgrades().containsKey(xsGuilds.getSubGuilds().get(server).getLevel()+1)) { //reach max level
                reqCoins = "-";
            } else {
                reqCoins = df.format(XSHandler.getSubClanUpgrades().get(xsGuilds.getSubGuilds().get(server).getLevel()+1).getPriceCoins());
            }
            str = str.replace("%required_coins%",reqCoins);
        }
        if(str.contains("%required_points%")) {
            String reqPoints;
            if(!XSHandler.getSubClanUpgrades().containsKey(xsGuilds.getSubGuilds().get(server).getLevel()+1)) { //reach max level
                reqPoints = "-";
            } else {
                reqPoints = df.format(XSHandler.getSubClanUpgrades().get(xsGuilds.getSubGuilds().get(server).getLevel()+1).getPricePoints());
            }
            str = str.replace("%required_points%",reqPoints);
        }
        if(str.contains("%required_main_points%")) {
            String reqPoints;
            if(!XSHandler.getMainClanUpgrades().containsKey(xsGuilds.getGuildLevel()+1)) { //reach max level
                reqPoints = "-";
            } else {
                reqPoints = df.format(XSHandler.getMainClanUpgrades().get(xsGuilds.getGuildLevel()+1).getPricePoints());
            }
            str = str.replace("%required_main_points%",reqPoints);
        }

        str = str.replace("%guild_name%",xsGuilds.getGuildName());
        str = str.replace("%guild_members%",String.valueOf(xsGuilds.getMembers().size()));
        str = str.replace("%guild_max_members%",String.valueOf(xsGuilds.getMaxMembers()));
        str = str.replace("%guild_level_main%",String.valueOf(xsGuilds.getGuildLevel()));

        return str;
    }

    public static ItemStack decodeItemFromConfig(String symbol, XS_FILE xsFile) {

        Configuration conf = menuConfig.getConfig(xsFile);
        String display = XSUtils.decodeText(conf.getString("configuration.items."+symbol+".display"));
        Material mat;
        int modelData = 0;

        if(conf.get("configuration.items."+symbol+".customModelData") != null) {
            modelData = (conf.getInt("configuration.items."+symbol+".customModelData"));
        }

        int amount = (conf.getInt("configuration.items."+symbol+".amount"));
        ArrayList<String> lores = new ArrayList<>();

        for(String lore : conf.getStringList("configuration.items."+symbol+".lore")) {
            lores.add(XSUtils.decodeText(lore));
        }

        ItemStack it;

        if(conf.getString("configuration.items."+symbol+".material").startsWith("custom_head")) {
            mat = Material.PLAYER_HEAD;
            String value = conf.getString("configuration.items."+symbol+".material").replace("custom_head-","");

            it = new ItemStack(mat, 1);
            SkullMeta meta = (SkullMeta) it.getItemMeta();
            GameProfile profile = new GameProfile(UUID.randomUUID(), "");
            profile.getProperties().put("textures", new Property("textures", value));
            Field profileField;
            meta.setDisplayName(display);
            meta.setLore(lores);
            meta.setCustomModelData(modelData);
            try {
                assert meta != null;
                profileField = meta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(meta, profile);
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            }
            it.setItemMeta(meta);
        } else {
            mat = Material.valueOf(conf.getString("configuration.items."+symbol+".material"));
            it = new ItemStack(mat,amount);
            if(it.getType() != Material.AIR) {
                ItemMeta meta = it.getItemMeta();

                meta.setDisplayName(display);
                meta.setLore(lores);
                meta.setCustomModelData(modelData);
                it.setItemMeta(meta);
            }
        }


        return it;
    }
}
