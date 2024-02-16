package net.xsapi.panat.xsguildclient.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.xsapi.panat.xsguildclient.config.menuConfig;
import net.xsapi.panat.xsguildclient.config.messagesConfig;
import net.xsapi.panat.xsguildclient.handler.XSMenuHandler;
import net.xsapi.panat.xsguildclient.objects.XSGuilds;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
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

    public static String decodeStringWithPlaceholder(String str, XSGuilds xsGuilds) {

        String patternsBalance = "%guild_balance_[a-zA-Z0-9_]+%";
        String patternsBalanceMax = "%guild_balance_max_[a-zA-Z0-9_]+%";
        String patternsMember = "%guild_home_[a-zA-Z0-9_]+%";
        String patternsLevel = "%guild_level_[a-zA-Z0-9_]+%";
        String patternsMemberMax = "%guild_home_max_[a-zA-Z0-9_]+%";
        Pattern pattern = Pattern.compile(patternsBalance);
        Pattern patternMax = Pattern.compile(patternsBalanceMax);
        Pattern patternLevel = Pattern.compile(patternsLevel);
        Pattern patternHome = Pattern.compile(patternsMember);
        Pattern patternHomeMax = Pattern.compile(patternsMemberMax);

        Matcher m = pattern.matcher(str);
        Matcher mMax = patternMax.matcher(str);
        Matcher mHome = patternHome.matcher(str);
        Matcher mHomeMax = patternHomeMax.matcher(str);
        Matcher mLevel = patternLevel.matcher(str);

        DecimalFormat df = new DecimalFormat("#.##");
        if(m.find()) {
            String subServer = m.group().replace("guild_balance_","").replace("%","");
            double bal;

            if(subServer.equalsIgnoreCase("main")) {
                bal = xsGuilds.getBalance();
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
            } else {
                lvl = xsGuilds.getSubGuilds().get(subServer).getLevel();
            }
            str = str.replace("%guild_level_"+subServer+"%",String.valueOf(lvl));
        }
        if(mHome.find()) {
            String subServer = mHome.group().replace("guild_home_","").replace("%","");
            int home = xsGuilds.getSubGuilds().get(subServer).getHomeList().size();
            str = str.replace("%guild_home_"+subServer+"%",String.valueOf(home));
        }
        if(mHomeMax.find()) {
            String subServer = mHomeMax.group().replace("guild_home_max_","").replace("%","");
            int home = xsGuilds.getSubGuilds().get(subServer).getMaxHome();
            str = str.replace("%guild_home_max_"+subServer+"%",String.valueOf(home));
        }
        if(mMax.find()) {
            String subServer = mMax.group().replace("guild_balance_max_","").replace("%","");
            double maxBal;

            if(subServer.equalsIgnoreCase("main")) {
                maxBal = xsGuilds.getMaxBalance();
            } else {
                maxBal = xsGuilds.getSubGuilds().get(subServer).getMaxBalance();
            }
            str = str.replace("%guild_balance_max_"+subServer+"%",df.format(maxBal));
        }

        str = str.replace("%guild_name%",xsGuilds.getGuildName());
        str = str.replace("%guild_members%",String.valueOf(xsGuilds.getMembers().size()));
        str = str.replace("%guild_max_members%",String.valueOf(xsGuilds.getMaxMembers()));
        str = str.replace("%guild_level_main%",String.valueOf(xsGuilds.getGuildLevel()));

        return str;
    }

    public static ItemStack decodeItemFromConfig(String symbol,XS_FILE xsFile) {

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
