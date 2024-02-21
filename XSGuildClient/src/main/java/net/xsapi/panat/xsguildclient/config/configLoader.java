package net.xsapi.panat.xsguildclient.config;

import net.xsapi.panat.xsguildclient.utils.XS_FILE;

public class configLoader {
    public  configLoader() {
        new mainConfig().loadConfigu();
        new messagesConfig().loadConfigu();
        new menuConfig().loadConfigu(XS_FILE.MAIN_MENU.toString().toLowerCase());
        new menuConfig().loadConfigu(XS_FILE.UPGRADE_MENU.toString().toLowerCase());
        new menuConfig().loadConfigu(XS_FILE.UPGRADE_MAIN_MENU.toString().toLowerCase());
        new menuConfig().loadConfigu(XS_FILE.MEMBERS_MENU.toString().toLowerCase());
        new menuConfig().loadConfigu(XS_FILE.PERMISSION_MENU.toString().toLowerCase());
    }
}
