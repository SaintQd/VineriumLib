package org.saintqd.vineriumlib.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.saintqd.vineriumlib.VineriumLib;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class VinUtils {

    public static void updateJarFile(Plugin plugin, File oldJarFile) {
        String pluginName = plugin.getName();
        File newJarFile = new File(Bukkit.getWorldContainer().getAbsolutePath()+File.separator+"plugins"+File.separator+pluginName+".jar");
        if (oldJarFile.exists() && newJarFile.exists()) {
            if (oldJarFile.getName().equals(newJarFile.getName())) {
                int counter = 1;
                while (counter <= 10) {
                    newJarFile = new File(Bukkit.getWorldContainer().getAbsolutePath() + File.separator + "plugins" + File.separator + pluginName + counter + ".jar");
                    if (newJarFile.exists()) {
                        oldJarFile.deleteOnExit();
                        VineriumLib.inst().getLogger().info("Found new JAR plugin file: "+newJarFile.getName()+", will rewrite on quit.");
                        return;
                    }
                    counter++;
                }
            }
            else {
                oldJarFile.deleteOnExit();
                VineriumLib.inst().getLogger().info("Found new JAR plugin file, will rewrite on quit.");
            }
        }
    }

    public static Component parseString(String text) {
        if (text.contains("<italic>") || text.contains("<i>"))
            return MiniMessage.miniMessage().deserialize(text);
        else
            return MiniMessage.miniMessage().deserialize(text).decoration(TextDecoration.ITALIC, false);
    }

    public static List<Component> parseStringList(List<String> text) {
        List<Component> components = new ArrayList<>();
        text.forEach(line -> components.add(parseString(line)));
        return components;
    }

    public static void sendDebugMessage(int selectedDebugLevel, String message) {
        int debugLevel = VineriumLib.inst().getDebugLevel();
        if (debugLevel >= selectedDebugLevel) {
            VineriumLib.inst().getServer().getConsoleSender().sendMessage(VinUtils.parseString(
                    "<blue>["+VineriumLib.inst().getName()+" [Debug - Level "+ debugLevel + "/" + selectedDebugLevel + "] <gray>"+message));
        }
    }

    public static Player checkForPlayerPresent(CommandSender sender, Player player) {
        if (!(sender instanceof Player) && player == null) {
            sender.sendMessage(parseString("<red>Данное действие может быть совершено только игроком."));
            return null;
        }
        if (sender instanceof Player && player == null)
            return (Player) sender;
        return player;
    }

    public static NamespacedKey parseNamespace(String pair) {
        if (pair.contains(":")) {
            String[] keyData = pair.split(":");
            return new NamespacedKey(keyData[0],keyData[1]);
        }
        else
            return NamespacedKey.minecraft(pair);
    }

    public static long getCurrentTick() {
        return System.currentTimeMillis() / 50;
    }

    public static List<Path> listFilesInFolder(String path) {
        List<Path> fileList;
        try (Stream<Path> filePaths = Files.walk(Paths.get(path))) {
            fileList = filePaths.toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileList;
    }
}
