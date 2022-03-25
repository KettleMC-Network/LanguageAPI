package net.kettlemc.language.spigot;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.kettlemc.language.LanguageAPI;
import net.kettlemc.language.entity.LanguageEntity;
import net.kettlemc.language.spigot.skript.SkriptLangAddon;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.Locale;

public class SpigotAdapter extends JavaPlugin implements CommandExecutor, Listener, PluginMessageListener {

    private SkriptLangAddon skriptLangAddon;
    private LanguageAPI api;

    /**
     * Sends a message to a player/the console
     * @param api The LanguageAPI you want to send the message from
     * @param receiver The CommandSender you want to send the message to
     * @param path The message path you want to send to the commandsender
     **/
    public static void sendMessage(LanguageAPI api, CommandSender receiver, String path) {
        receiver.sendMessage(getColoredMessage(api, receiver, path));
    }

    /**
     * Sends a message to all players and the console
     * @param api The LanguageAPI you want to send the message from
     * @param path The message path you want to send to the commandsender
     **/
    public static void broadcast(LanguageAPI api, String path) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendMessage(api, player, path);
        }
        sendMessage(api, Bukkit.getConsoleSender(), path);
    }

    /**
     * @param api The LanguageAPI you want to send the message from
     * @param sender The CommandSender you want to send the message to
     * @param path The message path you want to send to the commandsender
     * @return Colored and translated message
     **/
    public static String getColoredMessage(LanguageAPI api, CommandSender sender, String path) {
        String message;
        if (sender instanceof Player) {
            message = api.getMessage(path, ((Player) sender).getUniqueId().toString());
        } else {
            message = api.getMessage(path, LanguageAPI.getDefaultLang());
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * @param sender CommandSender you want to get the UUID from
     * @return UUID of the provided entity or null if not a player
     **/
    public static String getUUID(CommandSender sender) {
        String uuid = null;
        if (sender instanceof Player)
            uuid = ((Player) sender).getUniqueId().toString();
        return uuid;
    }

    public void onEnable() {
        this.api = LanguageAPI.registerAPI(this.getName());
        this.getLogger().info("Loaded as a bukkit plugin.");
        this.getLogger().info("Registering commands and listeners...");
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getServer().getMessenger().registerIncomingPluginChannel(this, LanguageAPI.MESSAGE_NAMESPACE + ":" + LanguageAPI.MESSAGE_IDENTIFIER, this);
        if (LanguageAPI.isEnableSpigot())
            this.getCommand("language").setExecutor(this);
        if (Bukkit.getPluginManager().getPlugin("Skript") != null) {
            this.getLogger().info("Loading Skript Addon...");
            this.skriptLangAddon = new SkriptLangAddon(this);
            this.skriptLangAddon.registerAddon();
        }

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player) sender;

        if (args.length >= 1) {
            Locale locale = Locale.forLanguageTag(args[0]);
            if (api.doesFileExist(locale)) {
                LanguageEntity entity = LanguageEntity.getEntity(player.getUniqueId().toString());
                entity.setLanguage(locale);
                entity.saveStats();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageAPI.getPrefix()) + getColoredMessage(api, player, "language.command.set").replace("%language%", api.getLanguageString(getUUID(player))));
                return true;
            }
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageAPI.getPrefix()) + getColoredMessage(api, player, "language.command.invalid-language"));

        return false;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        LanguageEntity entity = LanguageEntity.getEntity(event.getPlayer().getUniqueId().toString());
        entity.loadLanguage();

        if (LanguageAPI.isEnableSpigot()) {
            String uuid = getUUID(event.getPlayer());
            Bukkit.getScheduler().runTaskLaterAsynchronously(this, new Runnable() {
                @Override
                public void run() {
                    event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageAPI.getPrefix()) + getColoredMessage(api, event.getPlayer(), "language.join.selected").replace("%language%", api.getLanguageString(uuid)));
                }
            }, 20L);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        LanguageEntity entity = LanguageEntity.getEntity(event.getPlayer().getUniqueId().toString());
        LanguageEntity.getEntities().remove(entity);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        LanguageAPI.LOGGER.info("Received plugin message for '" + player.getUniqueId().toString() + "' on channel '" + channel + "'.");
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subChannel = in.readUTF();
        if (subChannel.equalsIgnoreCase(LanguageAPI.MESSAGE_IDENTIFIER)) {
            String uuid = in.readUTF();
            String locale = in.readUTF();
            LanguageEntity.getEntity(uuid).setLanguage(Locale.forLanguageTag(locale));
            LanguageAPI.LOGGER.info("Received plugin message for '" + player.getUniqueId().toString() + "' with language-code '" + locale + "'.");
        }
    }

}
