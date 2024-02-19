package net.xsapi.panat.xsguildclient.commands;

import net.xsapi.panat.xsguildclient.core;
import net.xsapi.panat.xsguildclient.utils.XSTabComplete;

public class commandsLoader {

    public commandsLoader() {
        core.getPlugin().getCommand("xsguild").setExecutor(new commands());
        core.getPlugin().getCommand("xsguild").setTabCompleter(new XSTabComplete());
    }

}
