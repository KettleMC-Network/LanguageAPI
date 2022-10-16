package net.kettlemc.language.spigot;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.kettlemc.language.LanguageAPI;
import net.kettlemc.language.entity.LanguageEntity;
import net.kettlemc.language.file.ConfigManager;
import net.kettlemc.language.file.entry.Message;
import net.kettlemc.language.mysql.SQLHandler;
import net.kettlemc.language.platform.Platform;
import net.kettlemc.language.spigot.skript.SkriptLangAddon;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.Locale;
import java.util.Objects;

public class SpigotAdapter extends JavaPlugin implements CommandExecutor, Listener, PluginMessageListener {

    private SkriptLangAddon skriptLangAddon;
    private LanguageAPI api;

    private void loadSkript() {
        this.getLogger().info("Loading as a bukkit plugin.");
        if (Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("Skript")).isEnabled()) {
            this.getLogger().info("Loading Skript Addon...");
            this.skriptLangAddon = new SkriptLangAddon(this);
            this.skriptLangAddon.registerAddon();
        } else {
            this.getLogger().info("Skript not found. Skript Addon disabled.");
        }
    }

    @Override
    public void onEnable() {
        this.getLogger().info("Loading on platform: " + Platform.get().toString());
        this.api = LanguageAPI.registerAPI(this.getName());
        this.getLogger().info("Loaded as a bukkit plugin.");
        this.getLogger().info("Registering commands and listeners...");
        Bukkit.getPluginManager().registerEvents(this, this);
        loadSkript();
        Bukkit.getServer().getMessenger().registerIncomingPluginChannel(this, LanguageAPI.MESSAGE_NAMESPACE + ":" + LanguageAPI.MESSAGE_IDENTIFIER, this);
        if (!SpigotUtils.isBungeeEnabled()) {
            this.getLogger().info("Registering language command...");
            this.getCommand("language").setExecutor(this);
        }
        this.api.loadMessages(); // Not needed but recommended so that it doesn't have to load later
    }

    public void onDisable() {
        this.getLogger().info("Disabled bukkit plugin.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player) sender;
        String uuid = player.getUniqueId().toString();

        if (args.length >= 1) {

            if (player.hasPermission("languageapi.reload") && args[0].equalsIgnoreCase("reload")) {
                this.api.loadMessages();
                Message message = new Message("language.command.reloaded").translate(this.api, uuid).prefix(ConfigManager.PREFIX.getValue());
                player.sendMessage(message.buildChatColor());
                return true;
            }

            Locale locale = Locale.forLanguageTag(args[0]);
            if (api.doesFileExist(locale)) {
                LanguageEntity entity = LanguageEntity.getEntity(player.getUniqueId().toString());
                entity.setLanguage(locale);
                entity.saveStats();
                Message message = new Message("language.command.set").translate(this.api, uuid).replace("%language%", api.getLanguageString(uuid)).prefix(ConfigManager.PREFIX.getValue());
                player.sendMessage(message.buildChatColor());
                return true;
            }
        }
        Message message = new Message("language.command.invalid-language").translate(this.api, uuid).prefix(ConfigManager.PREFIX.getValue());
        return false;
    }

    @EventHandler
    public void onAsyncJoin(AsyncPlayerPreLoginEvent event) {
        String uuid = event.getUniqueId().toString();
        SQLHandler.load(uuid);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        String uuid = event.getPlayer().getUniqueId().toString();
        if (!SpigotUtils.isBungeeEnabled()) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> {
                Message message = new Message("language.join.selected").translate(api, Locale.ENGLISH).prefix(ConfigManager.PREFIX.getValue());
                message.replace("%language%", api.getLanguageString(uuid));
                event.getPlayer().sendMessage(message.buildChatColor());
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
