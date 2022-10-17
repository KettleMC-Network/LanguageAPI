package net.kettlemc.language.file;

import net.kettlemc.language.LanguageAPI;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FileManager {

    private final String id;
    private final Map<Locale, HashMap> languages = new HashMap<>();

    public FileManager(String id) {
        this.id = id;
    }

    public void loadAllLanguages() {
        File directory = LanguageAPI.LANGUAGE_PATH.toFile();
        createFolder(directory);
        for (File langId : directory.listFiles()) {
            LanguageAPI.LOGGER.info("Checking " + langId.getAbsolutePath() + " for any language files...");
            for (File subFile : langId.listFiles()) {
                LanguageAPI.LOGGER.info("Detected file " + subFile.getName() + ".");
                loadLanguage(Locale.forLanguageTag(subFile.getName().replace(".json", "")));
            }
        }
    }

    public HashMap getHashMapFromFile(Locale locale) {
        try {
            HashMap<String, String> messages = new HashMap<>();
            File file = getFile(locale);
            createFileIfNotExists(file);
            LanguageAPI.LOGGER.info("Loading file " + file.getAbsolutePath() + ".");
            String content = new String(Files.readAllBytes(Paths.get(file.toURI())), "UTF-8");
            JSONObject json = content.isEmpty() ? new JSONObject() : (JSONObject) new JSONParser().parse(content);
            for (Object key : json.keySet()) {
                if (key instanceof String)
                    LanguageAPI.LOGGER.info("Loaded path '" + key + "' as '" + json.get(key) + "'.");
                    messages.put((String) key, (String) json.get(key));
            }
            return messages;
        } catch (Exception exception) {
            LanguageAPI.LOGGER.error("Couldn't read the language file for the locale '" + locale.toLanguageTag() + "' (ID: " + this.id + ")!");
            exception.printStackTrace();
            return null;
        }
    }

    /*
        public void saveHashMapToFile(Locale locale) {
            LanguageAPI.LOGGER.info("Saving the language file " + locale.toLanguageTag() + ".");
            if (languages == null || !isLoaded(locale)) {
                LanguageAPI.LOGGER.warning("Couldn't save the language file for " + locale.toLanguageTag() + " (not loaded).");
                return;
            }
            JSONObject json = new JSONObject();
            HashMap language = languages.get(locale);
            language.keySet().forEach(key -> {
                json.put(key, language.get(key));
            });
            try {
                new FileWriter(getFile(locale)).write(json.toJSONString());
                LanguageAPI.LOGGER.info("Saved the language file " + locale.toLanguageTag() + ".");
            } catch (IOException exception) {
                LanguageAPI.LOGGER.severe("Couldn't save the language file " + locale.toLanguageTag() !");
                exception.printStackTrace();
            }
        }
    /*
        public void save() {
            if (languages == null) {
                LanguageAPI.LOGGER.warning("Couldn't save languages (null)!");
                return;
            }
            LanguageAPI.LOGGER.info("Saving all the language files...");
            for (Locale locale : languages.keySet()) {
                saveHashMapToFile(locale);
            }
        }
    */


    private void createFileIfNotExists(File file) {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createFolder(File file) {
        file.getParentFile().mkdirs();
        file.mkdirs();
    }

    private File getFile(Locale language) {
        return LanguageAPI.LANGUAGE_PATH.resolve(this.id).resolve(language.toLanguageTag() + ".json").toFile();
    }

    public String getMessage(String path, Locale language) {
        if (!isLoaded(language))
            loadLanguage(language);
        String message = (String) languages.get(language).get(path);
        if (message == null) {
            LanguageAPI.LOGGER.warn("Couldn't find translation '" + language.toLanguageTag() +"' for path '" + path + "'.");

            // If there isn't a translation for the requested language, the default language will be requested
            message = (String) languages.get(LanguageAPI.getDefaultLang()).get(path);
        }
        return message == null ? ConfigManager.MESSAGE_NOT_TRANSLATED_MESSAGE.getValue() : message;
    }

    public void loadLanguage(Locale language) {
        if (languages.containsKey(language))
            languages.remove(language);
        languages.put(language, getHashMapFromFile(language));
    }

    public boolean isLoaded(Locale language) {
        return languages.containsKey(language);
    }

    public boolean doesFileExist(Locale language) {
        return getFile(language).exists();
    }

}