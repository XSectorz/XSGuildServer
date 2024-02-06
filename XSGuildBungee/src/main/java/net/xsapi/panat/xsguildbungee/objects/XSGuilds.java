package net.xsapi.panat.xsguildbungee.objects;

import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.HashMap;

public class XSGuilds {

    public String guildName;
    public HashMap<String,String> members = new HashMap<>();
    public HashMap<String,XSSubGuilds> subGuilds = new HashMap<>();

    public String leader;
    public ArrayList<String> subleader;

    public XSGuilds(String guildName) {
        this.guildName = guildName;

    }

    public String getLeader() {
        return leader;
    }

    public ArrayList<String> getSubleader() {
        return subleader;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }

    public void setSubleader(ArrayList<String> subleader) {
        this.subleader = subleader;
    }

    public String getGuildName() {
        return guildName;
    }

    public String getRealName() {
        return ChatColor.stripColor(guildName);
    }

    public HashMap<String, String> getMembers() {
        return members;
    }

    public HashMap<String, XSSubGuilds> getSubGuilds() {
        return subGuilds;
    }
}
