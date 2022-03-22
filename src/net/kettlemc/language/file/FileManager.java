package net.kettlemc.language.file;

import net.kettlemc.language.LanguageAPI;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;

public class FileManager {

    private String id;
    private HashMap<Locale, HashMap> languages = new HashMap<>();

    public FileManager(String id) {
        this.id = id;
    }

    public HashMap getHashMapFromFile(Locale locale) {
        try {
            HashMap<String, String> messages = new HashMap<>();
            File file = getFile(locale);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
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

    private File getFile(Locale language) {
        return new File("languages/" + this.id  + "/" + language.toLanguageTag() + ".json");
    }

    public String getMessage(String path, Locale language) {
        if (!isLoaded(language))
            loadLanguage(language);
        String message = (String) languages.get(language).get(path);
        if (message == null) {
            LanguageAPI.LOGGER.warning("Couldn't find translation  '" + language.toLanguageTag() +"' for path '" + path + "'.");

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
