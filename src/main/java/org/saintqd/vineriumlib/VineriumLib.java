package org.saintqd.vineriumlib;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.saintqd.vineriumlib.listeners.GUIListener;
import org.saintqd.vineriumlib.managers.LangManager;
import org.saintqd.vineriumlib.managers.VaultManager;
import org.saintqd.vineriumlib.utils.VinUtils;

public class VineriumLib extends JavaPlugin {

    private static VineriumLib plugin;
    private int debugLevel = 0;
    private VaultManager vaultManager = null;
    private LangManager langManager = null;

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        setupDefaultConfig();

        this.debugLevel = 0;
        this.langManager = new LangManager();

        getServer().getPluginManager().registerEvents(new GUIListener(), this);

        Plugin vaultPlugin = Bukkit.getPluginManager().getPlugin("Vault");
        if (vaultPlugin != null && vaultPlugin.isEnabled()) {
            vaultManager = new VaultManager();
            vaultManager.loadVault();
        }
        else {
            vaultManager = null;
            VinUtils.sendDebugMessage(0,"<yellow>Could not find Vault! Chat, Economy and Permissions won't be supported.");
        }
    }

    @Override
    public void onDisable() {
        VinUtils.updateJarFile(this,this.getFile());
    }

    public static VineriumLib inst() {
        return plugin;
    }

    private void setupDefaultConfig() {

    }

    public int getDebugLevel() {
        return debugLevel;
    }

    public VaultManager getVaultManager() {
        return vaultManager;
    }

    public LangManager getLangManager() {
        return langManager;
    }
}
