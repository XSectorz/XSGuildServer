package net.xsapi.panat.xsguildbungee.handler;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.config.ServerInfo;
import net.xsapi.panat.xsguildbungee.core;
import net.xsapi.panat.xsguildbungee.listener.playerSwitch;

public class XSHandler {

    private static String subChannel = "xsguilds:channel";

    public static String getSubChannel() {
        return subChannel;
    }

    public static void loadEvent() {
        core.getPlugin().getProxy().getPluginManager().registerListener(core.getPlugin(),new playerSwitch());
    }

    public static void subChannel() {
        core.getPlugin().getProxy().registerChannel(getSubChannel());
    }

    public static void sendCustomString(String subChannel,String serverName, String customString) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(customString);
        ServerInfo serverInfo = core.getPlugin().getProxy().getServerInfo(serverName);
        serverInfo.sendData(subChannel, out.toByteArray());
    }

}
