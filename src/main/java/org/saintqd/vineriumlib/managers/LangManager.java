package org.saintqd.vineriumlib.managers;

import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.saintqd.vineriumlib.utils.VinUtils;

import java.io.File;
import java.util.HashMap;

public class LangManager {

    HashMap<Plugin,HashMap<String,String>> langLinesPerPlugin;

    public LangManager() {
        this.langLinesPerPlugin = new HashMap<>();
    }

    public void registerLangLines(Plugin plugin, HashMap<String,String> langLines) {
        langLinesPerPlugin.put(plugin,langLines);
    }

    public HashMap<String,String> getLangLines(Plugin plugin) {
        return langLinesPerPlugin.getOrDefault(plugin,new HashMap<>());
    }

    public HashMap<String,String> loadLanguageFile(String path) {
        HashMap<String,String> langLines = new HashMap<>();
        File langFile = new File(path);
        if (!langFile.exists()) {
            VinUtils.sendDebugMessage(0,"<yellow>Lang file "+langFile+" does not exist!");
            return langLines;
        }
        YamlConfiguration langFileYaml = YamlConfiguration.loadConfiguration(langFile);
        ConfigurationSection langFileConfig = langFileYaml.getConfigurationSection("Lang");
        for (String identifier : langFileConfig.getKeys(false)) {
            langLines.put(identifier,langFileConfig.getString(identifier));
        }
        return langLines;
    }

    public Component parseLangString(Plugin plugin, String identifier, String... args) {
        if (!langLinesPerPlugin.getOrDefault(plugin,new HashMap<>()).containsKey(identifier))
            return VinUtils.parseString(identifier);
        String line = langLinesPerPlugin.getOrDefault(plugin,new HashMap<>()).get(identifier);
        if (args.length == 0) VinUtils.parseString(line);

        int index = 1;
        for (String arg : args) {
            line = line.replace("{"+index+"}",arg);
            index++;
        }
        return VinUtils.parseString(line);
    }
}
