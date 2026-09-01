package org.saintqd.vineriumlib.managers;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.intellij.lang.annotations.Subst;
import org.saintqd.vineriumlib.utils.VinUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.util.HashMap;

public class LangManager {

    public static final LangManager INSTANCE = new LangManager();

    private final HashMap<Key,String> langLines;

    public LangManager() {
        this.langLines = new HashMap<>();
    }

    public void registerLangLines(HashMap<Key,String> langLines) {
        this.langLines.putAll(langLines);
    }

    public HashMap<Key,String> getLangLines() {
        return langLines;
    }

    public HashMap<Key,String> loadLanguageFile(Plugin plugin, String path) {
        HashMap<Key,String> langLines = new HashMap<>();
        File langFile = new File(path);
        if (!langFile.exists()) {
            VinUtils.sendDebugMessage(0,"<yellow>Lang file "+langFile+" does not exist!");
            return langLines;
        }
        YamlConfiguration langFileYaml = YamlConfiguration.loadConfiguration(langFile);
        ConfigurationSection langFileConfig = langFileYaml.getConfigurationSection("Lang");
        for (String identifier : langFileConfig.getKeys(false)) {
            @Subst("vineriumlib.value") String keyValue = identifier.toLowerCase();
            Key langKey = Key.key(plugin,keyValue);
            langLines.put(langKey,langFileConfig.getString(identifier));
        }
        return langLines;
    }

    public Component parseLangString(Plugin plugin, String identifier, String... args) {
        @Subst("vineriumlib.value") String keyValue = identifier.toLowerCase();
        Key key = Key.key(plugin,keyValue);
        return parseLangString(key,identifier,args);
    }

    public Component parseLangString(Key key, String identifier, String... args) {
        identifier = identifier.toLowerCase();
        String line;
        //if (langLinesPerPlugin.getOrDefault(plugin,new HashMap<>()).containsKey(identifier))
        //    line = langLinesPerPlugin.getOrDefault(plugin,new HashMap<>()).get(identifier);
        if (langLines.containsKey(key))
            line = langLines.get(key);
        else
            return VinUtils.parseString(identifier);
        if (args.length == 0) VinUtils.parseString(line);

        int index = 1;
        for (String arg : args) {
            line = line.replace("{"+index+"}",arg);
            index++;
        }
        return VinUtils.parseString(line);
    }

    public String getRawLangString(Plugin plugin, String identifier) {
        @Subst("vineriumlib.value") String keyValue = identifier.toLowerCase();
        Key key = Key.key(plugin,keyValue);
        String line = langLines.get(key);
        if (line != null)
            return line;
        else return identifier;
    }
}
