package net.xsapi.panat.xsguildbungee.handler;

import net.xsapi.panat.xsguildbungee.core;
import net.xsapi.panat.xsguildbungee.listener.playerSwitch;

public class XSHandler {

    public static void loadEvent() {
        core.getPlugin().getProxy().getPluginManager().registerListener(core.getPlugin(),new playerSwitch());
    }

}
