# 🌎 LanguageAPI

A simple language api for translating plugins into multiple languages. The messages are saved in json files and the language of a player gets saved in a SQL database.

### How to use?
```java
// Registering your plugin
LanguageAPI api = LanguageAPI.registerAPI("YourPluginID");
```
Every plugin has an ID. The language files for a plugin will be saved in `languages/<ID>/<langcode>.json`.

```java
// Getting a translated message
api.getMessage("path.to.message", Locale.GERMAN);
api.getMessage("path.to.other.message", player.getUniqueId.toString());
```

If you want to use Spigot/Velocity exclusive methods, see SpigotAdapter/VelocityAdapter.

If you're running on Spigot you can use the Skript addon this plugin provides.
```yaml
[the] translated message %path% of %player%
send translated message "path.to.message" of player to player
```

Gradle Setup based on the [CFW](https://github.com/CuukyOfficial/CFW)/[VaroPlugin](https://github.com/CuukyOfficial/VaroPlugin) by Cuuky