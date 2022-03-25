package net.kettlemc.language.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kettlemc.language.LanguageAPI;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Configuration {

    private static Configuration instance;

    private JSONObject config;
    private File file;

    private Configuration(String path) {
        if (isLoaded())
            return;

        try {
            this.file = new File(path);

            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            String content = new String(Files.readAllBytes(Paths.get(file.toURI())), "UTF-8");
            this.config = content.isEmpty() ? new JSONObject() : (JSONObject) new JSONParser().parse(content);

        } catch (Exception e) {
            LanguageAPI.LOGGER.severe("Couldn't load the config file!");
            e.printStackTrace();
        }
    }

    private void saveToFile() {
        try {
            FileWriter writer = new FileWriter(file);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            writer.write(gson.toJson(config));
            writer.close();
        } catch (IOException e) {
            LanguageAPI.LOGGER.severe("Couldn't save the config file!");
            e.printStackTrace();
        }
    }

    public static Configuration getConfig(String path) {
        if (!isLoaded()) {
            instance = new Configuration(path);
        }
        return instance;
    }

    public static boolean isLoaded() {
        return instance != null;
    }

    public Object getValue(String path, Object defaultValue) {
        if (!isLoaded()) {
            LanguageAPI.LOGGER.warning("Tried to access config while it wasn't loaded.");
            return null;
        }

        if (config.containsKey(path))
            return config.get(path);

        config.put(path, defaultValue);
        saveToFile();

        return defaultValue;
    }

    public String getString(String path, String defaultValue) {
        return (String) getValue(path, defaultValue);
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        return (boolean) getValue(path, defaultValue);
    }

    public long getLong(String path, long defaultValue) {
        return (long) getValue(path, defaultValue);
    }

}
