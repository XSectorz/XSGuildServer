package net.xsapi.panat.xsguildclient.listener;

import net.xsapi.panat.xsguildclient.core;
import net.xsapi.panat.xsguildclient.handler.XSHandler;
import net.xsapi.panat.xsguildclient.handler.XSRedisHandler;
import net.xsapi.panat.xsguildclient.utils.XSDATA_TYPE;
import net.xsapi.panat.xsguildclient.utils.XSUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class joinEvent implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        XSRedisHandler.sendRedisMessage(XSHandler.getSubChannel()+"_bungeecord", XSDATA_TYPE.REQ_DATA+"<SPLIT>" + XSHandler.getServername() + ";" + p.getName());

        if(XSHandler.getTeleportHomeData().containsKey(p.getName())&& (System.currentTimeMillis() -Long.parseLong(XSHandler.getTeleportHomeData().get(p.getName()).split("<SPLIT>")[0]) <= 10000L)) {

            //Bukkit.getLogger().info("Current: " + System.currentTimeMillis());
            //Bukkit.getLogger().info("Temp: " + Long.parseLong(XSHandler.getTeleportHomeData().get(p.getName()).split("<SPLIT>")[0]));
            //Bukkit.getLogger().info("Diff: " + (System.currentTimeMillis() -Long.parseLong(XSHandler.getTeleportHomeData().get(p.getName()).split("<SPLIT>")[0]) <= 10000L));

            Bukkit.getScheduler().scheduleSyncDelayedTask(core.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    String homeData = XSHandler.getTeleportHomeData().get(p.getName()).split("<SPLIT>")[1];
                    String homeN = homeData.split(":")[0];
                    String world = homeData.split(":")[2];
                    double locX = Double.parseDouble(homeData.split(":")[3]);
                    double locY = Double.parseDouble(homeData.split(":")[4]);
                    double locZ = Double.parseDouble(homeData.split(":")[5]);
                    float yaw = Float.parseFloat(homeData.split(":")[6]);
                    float pitch = Float.parseFloat(homeData.split(":")[7]);
                    Location loc = new Location(Bukkit.getWorld(world), locX, locY, locZ, yaw, pitch);
                    //Bukkit.getLogger().info("Player teleported: " + loc.toString());
                    p.teleport(loc);
                    p.sendMessage(XSUtils.decodeTextFromConfig("home_success").replace("%home_name%",homeN));
                    XSHandler.getTeleportHomeData().remove(p.getName());
                }
            }, 10L);
        }

    }
}
