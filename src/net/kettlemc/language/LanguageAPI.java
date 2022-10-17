package net.kettlemc.language;

import com.github.almightysatan.jo2sql.SqlBuilder;
import com.github.almightysatan.jo2sql.SqlProvider;
import net.kettlemc.konfiguration.Configuration;
import net.kettlemc.language.entity.LanguageEntity;
import net.kettlemc.language.file.ConfigManager;
import net.kettlemc.language.file.FileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class LanguageAPI {

    public static final Path CONFIG_PATH = Paths.get("plugins/LanguageAPI/");
    public static final Path LANGUAGE_PATH = CONFIG_PATH.resolve("languages/");

    public static final Configuration CONFIGURATION = new Configuration(CONFIG_PATH.resolve("config.json"));

    public static final Logger LOGGER = LoggerFactory.getLogger(LanguageAPI.class.getSimpleName());
    public static final String MESSAGE_NAMESPACE = "langapi";
    public static final String MESSAGE_IDENTIFIER = "switch";

    private static SqlProvider sqlProvider;
    private static boolean setup;

    // Can be set in the config file
    private static String prefix;
    private static Locale defaultLang = Locale.GERMAN;

    private String id;
    private FileManager manager;

    private LanguageAPI(String id) {
        this.id = id;
        this.manager = new FileManager(id);
    }

    private static void loadSQLClient() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        String host = ConfigManager.MYSQL_HOST.getValue();
        String database = ConfigManager.MYSQL_DATABASE.getValue();
        String user = ConfigManager.MYSQL_USER.getValue();
        String password = ConfigManager.MYSQL_PASSWORD.getValue();
        long port = ConfigManager.MYSQL_PORT.getValue();

        sqlProvider = new SqlBuilder().mariadb(host + ":" + port, user, password, database);
    }

    public static SqlProvider getSqlProvider() {
        return sqlProvider;
    }

    public static Locale getDefaultLang() {
        return defaultLang;
    }

    public static String getPrefix() {
        return prefix;
    }

    public String getMessage(String path, Locale locale) {
        return this.manager.getMessage(path, locale);
    }

    public String getMessage(String path, String uuid) {
        return getMessage(path, LanguageEntity.getEntity(uuid).getLanguage());
    }

    public Locale getLanguage(String uuid) {
        return LanguageEntity.getEntity(uuid).getLanguage();
    }

    public String getLanguageString(String uuid) {
        return getLanguage(uuid).toLanguageTag();
    }

    public boolean doesFileExist(Locale language) {
        return this.manager.doesFileExist(language);
    }

    public void loadMessages() {
        this.manager.loadAllLanguages();
    }

    public static LanguageAPI registerAPI(String id) {
        if (!setup) {
            LOGGER.info("Initial Setup...");
            LOGGER.info("Loading SQL connnection...");
            loadSQLClient();
            setup = true;
        }

        LOGGER.info("Loaded API for plugin with id '" + id + "'.");
        return new LanguageAPI(id);
    }
/*
    public void save() {
        this.manager.save();
    }
*/
}