package net.xsapi.panat.xsguildbungee.objects;

import java.util.HashMap;

public class XSSubGuilds {

    public int level;
    public String tech;
    public String subServer;
    public double balance = 0;
    public double maxBalance = 0;
    public int maxHome = 0;

    public HashMap<String,String> homeList = new HashMap<>();

    public XSSubGuilds(String tech,int level,String subServer) {
        this.tech = tech;
        this.level = level;
        this.subServer = subServer;
    }

    public int getMaxHome() {
        return maxHome;
    }

    public void setMaxHome(int maxHome) {
        this.maxHome = maxHome;
    }

    public HashMap<String, String> getHomeList() {
        return homeList;
    }

    public double getMaxBalance() {
        return maxBalance;
    }

    public void setMaxBalance(double maxBalance) {
        this.maxBalance = maxBalance;
    }
    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setTech(String tech) {
        this.tech = tech;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getTech() {
        return tech;
    }

    public int getLevel() {
        return level;
    }

    public String getSubServer() {
        return subServer;
    }
}
