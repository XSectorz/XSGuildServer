package net.xsapi.panat.xsguildclient.commands;

import net.xsapi.panat.xsguildclient.core;

public class commandsLoader {

    public commandsLoader() {
        core.getPlugin().getCommand("xsguild").setExecutor(new commands());
    }

}
