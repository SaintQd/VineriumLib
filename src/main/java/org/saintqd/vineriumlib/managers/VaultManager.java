package org.saintqd.vineriumlib.managers;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.saintqd.vineriumlib.VineriumLib;
import org.saintqd.vineriumlib.utils.VinUtils;

public class VaultManager {

    private Chat chatProvider = null;
    private Economy economyProvider = null;
    private Permission permissionProvider = null;

    public void loadVault() {
        if (!setupPermissions()) {
            VinUtils.sendDebugMessage(0,"<yellow>Permissions provider isn't found. Permission support won't be loaded.");
        }
        if (!setupEconomy()) {
            VinUtils.sendDebugMessage(0,"<yellow>Economy provider isn't found. Economy support won't be loaded.");
        }
        if (!setupChat()) {
            VinUtils.sendDebugMessage(0,"<yellow>Chat provider isn't found. Chat support won't be loaded.");
        }
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = VineriumLib.inst().getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp != null)
            chatProvider = rsp.getProvider();
        return chatProvider != null;
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = VineriumLib.inst().getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null)
            economyProvider = rsp.getProvider();
        return economyProvider != null;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = VineriumLib.inst().getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp != null)
            permissionProvider = rsp.getProvider();
        return permissionProvider != null;
    }

    public Chat getChatProvider() {
        return chatProvider;
    }

    public Economy getEconomyProvider() {
        return economyProvider;
    }

    public Permission getPermissionProvider() {
        return permissionProvider;
    }
}
