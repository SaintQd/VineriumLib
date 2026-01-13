package org.saintqd.vineriumlib;

import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.saintqd.vineriumlib.commands.VinLibCommandsManager;
import org.saintqd.vineriumlib.gui.data.CustomGUI;
import org.saintqd.vineriumlib.listeners.GUIListener;
import org.saintqd.vineriumlib.managers.CustomGUIManager;
import org.saintqd.vineriumlib.managers.LangManager;
import org.saintqd.vineriumlib.managers.VaultManager;
import org.saintqd.vineriumlib.utils.ResourceUtils;
import org.saintqd.vineriumlib.utils.VinUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class VineriumLib extends JavaPlugin {

    private static VineriumLib plugin;
    private int debugLevel = 0;
    private LangManager langManager = null;
    private CustomGUIManager customGUIManager = null;

    // Совместимость с другими плагинами
    private boolean placeholderAPIEnabled = false;
    private VaultManager vaultManager = null;

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        try {
            ResourceUtils.fetchAllResources(this,getFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.debugLevel = 0;
        this.langManager = new LangManager();
        this.customGUIManager = new CustomGUIManager();

        Plugin vaultPlugin = Bukkit.getPluginManager().getPlugin("Vault");
        if (vaultPlugin != null && vaultPlugin.isEnabled()) {
            vaultManager = new VaultManager();
            vaultManager.loadVault();
        }
        else {
            vaultManager = null;
            VinUtils.sendDebugMessage(0,"<yellow>Could not find Vault! Chat, Economy and Permissions won't be supported.");
        }

        Plugin placeholderAPIPlugin = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        if (placeholderAPIPlugin != null && placeholderAPIPlugin.isEnabled())
            placeholderAPIEnabled = true;

        loadData();

        VinLibCommandsManager.setupCommands(this);

        getServer().getPluginManager().registerEvents(new GUIListener(), this);
    }

    @Override
    public void onDisable() {
        VinUtils.updateJarFile(this,this.getFile());
        for (Player player : Bukkit.getOnlinePlayers()) {
            CustomGUI.restoreInventory(player);
        }
    }

    public static VineriumLib inst() {
        return plugin;
    }

    public void loadData() {
        reloadConfig();

        String selectedLang = getConfig().getString("Language");
        HashMap<Key,String> langLines = VineriumLib.inst().getLangManager().loadLanguageFile(this,
                plugin.getDataFolder().getPath() + File.separator + "lang" + File.separator + selectedLang + ".yml");
        VineriumLib.inst().getLangManager().registerLangLines(langLines);

    }

    public void setDebugLevel(int debugLevel) {
        this.debugLevel = debugLevel;
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

    public boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
    }

    public CustomGUIManager getCustomGUIManager() {
        return customGUIManager;
    }
}
