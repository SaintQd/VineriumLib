package org.saintqd.vineriumlib.managers;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.saintqd.vineriumlib.utils.VinUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

public class CustomGUIManager {

    private final HashMap<NamespacedKey,String> guiPaths = new HashMap<>();

    public void registerGuis(Plugin plugin) {
        Path path = Paths.get(plugin.getDataFolder().getPath() + File.separator + "CustomGUIs");
        if (!Files.exists(path)) return;
        List<Path> filePaths = VinUtils.listFilesInFolder(path.toString());
        for (Path filePath : filePaths) {
            File file = new File(filePath.toString());
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            for (String guiName : config.getKeys(false)) {
                NamespacedKey key = new NamespacedKey(plugin,guiName.toLowerCase());
                guiPaths.put(key,filePath.toString());
            }
        }
    }

    public void unregisterGuis(Plugin plugin) {
        guiPaths.keySet().removeIf(key -> key.namespace().equals(plugin.getName().toLowerCase()));
    }

    public HashMap<NamespacedKey, String> getGuiPaths() {
        return guiPaths;
    }

}
