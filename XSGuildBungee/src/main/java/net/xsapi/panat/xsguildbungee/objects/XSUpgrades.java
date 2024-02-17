package net.xsapi.panat.xsguildbungee.objects;

import java.util.ArrayList;
import java.util.HashMap;

public class XSUpgrades {

    private int level;
    private double priceCoins = 0;
    private double pricePoints = 0;

    private String type;
    private HashMap<String,String> nextUpgrades = new HashMap<>();

    public XSUpgrades(String type,double priceCoins,double pricePoints) {
        this.type = type;
        this.priceCoins = priceCoins;
        this.pricePoints = pricePoints;
    }

    public HashMap<String,String> getNextUpgrades() {
        return nextUpgrades;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getPriceCoins() {
        return priceCoins;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setPriceCoins(double priceCoins) {
        this.priceCoins = priceCoins;
    }

    public double getPricePoints() {
        return pricePoints;
    }

    public void setPricePoints(double pricePoints) {
        this.pricePoints = pricePoints;
    }
}
