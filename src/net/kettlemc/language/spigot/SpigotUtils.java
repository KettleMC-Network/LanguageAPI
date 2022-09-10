package net.kettlemc.language.spigot;

import net.kettlemc.language.platform.Platform;

public class SpigotUtils {

    public static boolean isBungeeEnabled() {
        if (Platform.get() == Platform.BUKKIT) {
            try {
                return Class.forName("org.spigotmc.SpigotConfig").getField("bungee").getBoolean(null);
            } catch (Exception ignored) {
                System.out.println("Couldn't access bungee field in SpigotConfig!");
                return true;
            }
        }
        return false;
    }
}
