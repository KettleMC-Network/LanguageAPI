package net.kettlemc.language;

import net.kettlemc.language.entity.LanguageEntity;
import net.kettlemc.language.file.Configuration;
import net.kettlemc.language.file.FileManager;
import net.kettlemc.language.mysql.MySQLClient;

import java.util.Locale;
import java.util.logging.Logger;

public class LanguageAPI {

    private static final String CONFIG_PATH = "plugins/LanguageAPI/config.json";
    public static final Logger LOGGER = Logger.getLogger("LanguageAPI");
    public static final String MESSAGE_NAMESPACE = "langapi";
    public static final String MESSAGE_IDENTIFIER = "switch";
    private static final Configuration configuration = Configuration.getConfig(CONFIG_PATH);

    private static MySQLClient mysqlClient;
    private static boolean setup;

    // Can be set in the config file
    private static String prefix;
    private static boolean enableSpigot;
    private static boolean enableVelocity;
    private static Locale defaultLang;

    private String id;
    private FileManager manager;

    private LanguageAPI(String id) {
        this.id = id;
        this.manager = new FileManager(id);
    }

    private static void loadSQLClient() {
        String host = configuration.getString("sql.host", "localhost");
        String database = configuration.getString("sql.database", "language");
        String user = configuration.getString("sql.user", "user");
        String password = configuration.getString("sql.password", "password");
        long port = configuration.getLong("sql.port", 3306);

        mysqlClient = new MySQLClient(host, port, database, user, password);
    }

    public static MySQLClient getMySQLClient() {
        return mysqlClient;
    }

    public static Locale getDefaultLang() {
        return defaultLang;
    }

    public static Configuration getConfiguration() {
        return configuration;
    }

    public static boolean isEnableSpigot() {
        return enableSpigot;
    }

    public static boolean isEnableVelocity() {
        return enableVelocity;
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

    private static void loadConfig() {
        defaultLang = Locale.forLanguageTag(configuration.getString("settings.default-lang", "de"));
        enableSpigot = configuration.getBoolean("settings.enable-spigot", false);
        enableVelocity = configuration.getBoolean("settings.enable-velocity", false);
        prefix = configuration.getString("settings.prefix", "&4Language &8» &7");
    }

    public static LanguageAPI registerAPI(String id) {
        if (!setup) {
            LOGGER.info("Initial Setup...");
            LOGGER.info("Loading configuration...");
            loadConfig();
            LOGGER.info("Loading SQL connnection...");
            loadSQLClient();
            setup = true;
        }

        LOGGER.info("Loaded API for plugin with id '" + id + "'.");
        return new LanguageAPI(id);

    }

}