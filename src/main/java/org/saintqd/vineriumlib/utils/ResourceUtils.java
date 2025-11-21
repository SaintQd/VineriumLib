package org.saintqd.vineriumlib.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ResourceUtils {

    /**
     * Запрашивает YAML-ресурсы из папки resources выбранного плагина
     * и размещает их в папке плагина в местоположении, совпадающем с путём к ресурсу.
     *
     * @param plugin Плагин, в котором содержатся требуемые ресурсы
     * @param pluginFile Объект файла плагина
     */
    public static void fetchAllResources(Plugin plugin, File pluginFile) throws IOException {
        List<String> resourcePaths = getResources(pluginFile,Pattern.compile("("+ plugin.getName() +"/(.*)+.yml)"));
        for (String resourcePath : resourcePaths) {
            String parsedOutputPath = plugin.getDataFolder().getPath() +
                    resourcePath.replace(plugin.getName(),"").replace("/",File.separator);
            fetchYamlResource(plugin,"/"+resourcePath,parsedOutputPath);
        }
    }

    /**
     * Запрашивает YAML-ресурс из папки resources выбранного плагина
     * и размещает его в указанном местоположении.
     * Если YAML-файл в указанном местоположении уже существует, в него
     * добавляются недостающие значения из YAML-ресурса.
     *
     * @param plugin Плагин, в котором содержится требуемый ресурс
     * @param resourcePath Путь к ресурсу в файлах плагина
     * @param outputPath Путь к местоположению, в котором будет размещён ресурс
     */
    private static void fetchYamlResource(Plugin plugin, String resourcePath, String outputPath) throws IOException {
        File resourceFile = new File(outputPath);
        if (!resourceFile.exists()) {
            if (resourceFile.mkdirs()) {
                try (InputStream resourceStream = plugin.getClass().getResourceAsStream(resourcePath)) {
                    if (resourceStream != null) {
                        Files.copy(resourceStream, Path.of(outputPath), StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
            else VinUtils.sendDebugMessage(0,"<yellow>Could not create resource file at "+outputPath+"!");
        }
        else {
            String fileNameWithoutExt = outputPath.replaceFirst("[.][^.]+$","");
            if (!outputPath.endsWith(".yml")) return;
            String backupOutputFileName = fileNameWithoutExt+"_bak.yml";
            try (InputStream resourceStream = plugin.getClass().getResourceAsStream(resourcePath)) {
                if (resourceStream != null) {
                    Files.copy(resourceStream, Path.of(backupOutputFileName), StandardCopyOption.REPLACE_EXISTING);
                }
            }
            File file = new File(backupOutputFileName);
            if (file.exists()) {
                YamlConfiguration backupOutputYaml = YamlConfiguration.loadConfiguration(file);
                YamlConfiguration resourceYaml = YamlConfiguration.loadConfiguration(resourceFile);
                for (String key : backupOutputYaml.getKeys(true)) {
                    if (!resourceYaml.contains(key))
                        resourceYaml.set(key,backupOutputYaml.get(key));
                }
                resourceYaml.save(resourceFile);
                file.delete();
            }
        }
    }

    /**
     * for all elements of java.class.path get a List of resources.<br>
     * Pattern pattern = Pattern.compile(".*"); gets all resources.
     *
     * @param pluginFile file of the plugin containing resources
     * @param pattern the pattern to match
     * @return the resources in the order they are found
     */
    private static List<String> getResources(final File pluginFile, final Pattern pattern) {
        final List<String> retval = new ArrayList<>();
        ZipFile zf;
        try{
            zf = new ZipFile(pluginFile);
        } catch(final IOException e){
            throw new RuntimeException(e);
        }
        final Enumeration<? extends ZipEntry> e = zf.entries();
        while(e.hasMoreElements()){
            final ZipEntry ze = e.nextElement();
            final String fileName = ze.getName();
            final boolean accept = pattern.matcher(fileName).matches();
            if(accept){
                retval.add(fileName);
            }
        }
        try{
            zf.close();
        } catch(final IOException e1){
            throw new Error(e1);
        }
        return retval;
    }
}
