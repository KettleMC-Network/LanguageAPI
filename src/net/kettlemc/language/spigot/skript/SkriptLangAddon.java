package net.kettlemc.language.spigot.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import net.kettlemc.language.LanguageAPI;
import net.kettlemc.language.spigot.SpigotAdapter;

import java.io.IOException;

public class SkriptLangAddon {

    private static LanguageAPI api;

    private SpigotAdapter plugin;
    private SkriptAddon addon;

    public SkriptLangAddon(SpigotAdapter plugin) {
        this.plugin = plugin;
        api = LanguageAPI.registerAPI("Skript");
    }

    public void registerAddon() {
        this.addon = Skript.registerAddon(this.plugin);
        try {
            this.addon.loadClasses("net.kettlemc.language.spigot.skript", new String[] { "elements" });
        } catch (IOException e) {
            e.printStackTrace();
        }
        plugin.getLogger().info("Skript Addon enabled.");
    }

    public static LanguageAPI getApi() {
        return api;
    }
}
