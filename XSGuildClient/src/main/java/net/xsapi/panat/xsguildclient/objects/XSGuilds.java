package net.xsapi.panat.xsguildclient.objects;

import net.md_5.bungee.api.ChatColor;
import net.xsapi.panat.xsguildclient.objects.XSSubGuilds;

import java.util.ArrayList;
import java.util.HashMap;

public class XSGuilds {

    public int guildID;
    public int guildLevel;
    public String guildName;
    public String guildRealName;
    public HashMap<String,String> members = new HashMap<>();
    public HashMap<String,XSSubGuilds> subGuilds = new HashMap<>();

    public String leader;
    public ArrayList<String> subleader;

    public XSGuilds(int guildID,String guildRealName,String guildName,int guildLevel) {
        this.guildID = guildID;
        this.guildName = guildName;
        this.guildLevel = guildLevel;
        this.guildRealName = guildRealName;
    }

    public String getGuildRealName() {
        return guildRealName;
    }

    public int getGuildID() {
        return guildID;
    }

    public int getGuildLevel() {
        return guildLevel;
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
