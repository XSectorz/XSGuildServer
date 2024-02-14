package net.xsapi.panat.xsguildclient.objects;

import java.util.HashMap;

public class XSSubGuilds {

    public int level;
    public String tech;
    public String subServer;

    public double balance;
    public double maxBalance;

    public int maxHome;
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
