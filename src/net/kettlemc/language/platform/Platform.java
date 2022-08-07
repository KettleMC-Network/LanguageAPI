package net.kettlemc.language.platform;

public enum Platform {

    BUKKIT("org.bukkit.Bukkit"),
    VELOCITY("com.velocitypowered.proxy.Velocity"),
    UNKNOWN(null);

    private String clazz;

    Platform(String clazz) {
        this.clazz = clazz;
    }

    static Platform platform;

    public String getClassName() {
        return this.clazz;
    }

    public static Platform get() {
        if (platform == null) {
            for (Platform value : values()) {
                if (classExists(value.getClassName())) {
                    platform = value;
                }
            }
            platform = UNKNOWN;
        }
        return platform;
    }

    public static boolean classExists(String clazz) {
        if (clazz == null)
            return false;
        try {
            Class.forName(clazz);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

}
