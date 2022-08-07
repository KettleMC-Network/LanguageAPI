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

    public HashMap getHashMapFromFile(Locale locale) {
        try {
            HashMap<String, String> messages = new HashMap<>();
            File file = getFile(locale);
            createFileIfNotExists(file);
            LanguageAPI.LOGGER.info("Loading file " + file.getAbsolutePath());
            String content = new String(Files.readAllBytes(Paths.get(file.toURI())), "UTF-8");
            JSONObject json = content.isEmpty() ? new JSONObject() : (JSONObject) new JSONParser().parse(content);
            for (Object key : json.keySet()) {
                if (key instanceof String)
                    messages.put((String) key, (String) json.get(key));
            }
            return messages;
        } catch (Exception exception) {
            LanguageAPI.LOGGER.severe("Couldn't read the language file!");
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

    public void loadAllLanguages() {
        File file = new File(LanguageAPI.LANGUAGE_PATH + this.id + "/");
        createFolder(file);
        LanguageAPI.LOGGER.info("Checking " + file.getAbsolutePath() + " for any language files...");
        for (File subFile : file.listFiles()) {
            LanguageAPI.LOGGER.info("Detected file " + subFile.getName() + ".");
            loadLanguage(Locale.forLanguageTag(subFile.getName().replace(".json", "")));
        }
    }

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
        return new File(LanguageAPI.LANGUAGE_PATH + this.id + "/" + language.toLanguageTag() + ".json");
    }

    public String getMessage(String path, Locale language) {
        if (!isLoaded(language))
            loadLanguage(language);
        String message = (String) languages.get(language).get(path);
        if (message == null) {
            LanguageAPI.LOGGER.warning("Couldn't find translation '" + language.toLanguageTag() +"' for path '" + path + "'.");
            /*
            LanguageAPI.LOGGER.info("Creating default String for '" + path + "'.");
            languages.values().forEach(lang -> {
                lang.put(path, LanguageAPI.NOT_TRANSLATED);
            });
            */

            // If there isn't a translation for the requested language, the default language will be requested
            message = (String) languages.get(LanguageAPI.getDefaultLang()).get(path);
        }
        return message == null ? path : message;
    }

    public void loadLanguage(Locale language) {
        languages.put(language, getHashMapFromFile(language));
    }

    public boolean isLoaded(Locale language) {
        return languages.containsKey(language);
    }

    public boolean doesFileExist(Locale language) {
        return getFile(language).exists();
    }

}