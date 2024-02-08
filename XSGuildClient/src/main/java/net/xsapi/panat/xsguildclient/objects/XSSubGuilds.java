package net.xsapi.panat.xsguildclient.objects;

public class XSSubGuilds {

    public int level;
    public String tech;
    public String subServer;

    public XSSubGuilds(String tech,int level,String subServer) {
        this.tech = tech;
        this.level = level;
        this.subServer = subServer;
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
