package net.kettlemc.language.file;

import net.kettlemc.konfiguration.entry.BooleanConfigEntry;
import net.kettlemc.konfiguration.entry.IntegerConfigEntry;
import net.kettlemc.konfiguration.entry.StringConfigEntry;
import net.kettlemc.language.LanguageAPI;

public class ConfigManager {

    public static StringConfigEntry PREFIX = new StringConfigEntry(LanguageAPI.CONFIGURATION, "&4LanguageAPI &8» &7", "messages.prefix");
    public static StringConfigEntry MESSAGE_NOT_TRANSLATED_MESSAGE = new StringConfigEntry(LanguageAPI.CONFIGURATION, "This message has not yet been translated.", "messages.not-translated");

    public static BooleanConfigEntry USE_MINIMESSAGES = new BooleanConfigEntry(LanguageAPI.CONFIGURATION, false, "settings.enable-minimessages");

    public static StringConfigEntry MYSQL_HOST = new StringConfigEntry(LanguageAPI.CONFIGURATION, "host", "mysql.host");
    public static StringConfigEntry MYSQL_DATABASE = new StringConfigEntry(LanguageAPI.CONFIGURATION, "database", "mysql.database");
    public static IntegerConfigEntry MYSQL_PORT = new IntegerConfigEntry(LanguageAPI.CONFIGURATION, 3306,  "mysql.port");
    public static StringConfigEntry MYSQL_USER = new StringConfigEntry(LanguageAPI.CONFIGURATION, "user", "mysql.user");
    public static StringConfigEntry MYSQL_PASSWORD = new StringConfigEntry(LanguageAPI.CONFIGURATION, "password", "mysql.password");

}
