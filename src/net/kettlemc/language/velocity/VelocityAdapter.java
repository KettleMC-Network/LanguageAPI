package net.kettlemc.language.velocity;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.LegacyChannelIdentifier;
import net.kettlemc.language.LanguageAPI;
import net.kettlemc.language.entity.LanguageEntity;
import net.kettlemc.language.file.ConfigManager;
import net.kettlemc.language.file.entry.Message;
import net.kettlemc.language.mysql.SQLHandler;
import net.kettlemc.language.platform.Platform;

import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Plugin(id = "languageapi", name = "LanguageAPI", version = "1.1", url = "https://kettlemc.net", description = "LanguageAPI for translating messages", authors = { "LeStegii" })
public class VelocityAdapter implements SimpleCommand {


    private final ProxyServer server;
    private final Logger logger;
    private final CommandManager commandManager;

    private final LanguageAPI api;

    private final LegacyChannelIdentifier OUTGOING;

    @Inject
    public VelocityAdapter(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {

        this.server = server;
        this.logger = logger;
        this.api = LanguageAPI.registerAPI("LanguageAPI");
        this.commandManager = server.getCommandManager();
        this.server.getChannelRegistrar().register(OUTGOING = new LegacyChannelIdentifier(LanguageAPI.MESSAGE_NAMESPACE + ":" + LanguageAPI.MESSAGE_IDENTIFIER));
        this.api.loadMessages(); // Not needed but recommended so that it doesn't have to load later
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Loading on platform: " + Platform.get().toString());
        logger.info("Loaded as a velocity plugin.");
        commandManager.register(getCommandMeta("language", "lang", "sprache"), this);
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        logger.info("Disabling velocity plugin...");
    }

    @Subscribe
    public EventTask onAsyncLogin(LoginEvent event) {
        return EventTask.withContinuation(task -> {
            SQLHandler.load(event.getPlayer().getUniqueId().toString());
            task.resume();
        });
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
        String uuid = event.getPlayer().getUniqueId().toString();
        this.server.getScheduler().buildTask(this, () -> {
            Message message = new Message("language.join.selected").translate(this.api, Locale.ENGLISH).prefix(ConfigManager.PREFIX.getValue());
            message.replace("%language%", api.getLanguageString(uuid));
            event.getPlayer().sendMessage(message.buildComponent());
        }).delay(2L, TimeUnit.SECONDS).schedule();
    }


    private CommandMeta getCommandMeta(String name, String... aliases) {
        return commandManager.metaBuilder(name).aliases(aliases).build();
    }

    public void updateSubServers(Player player, Locale locale) {
        ServerConnection server = player.getCurrentServer().get();
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(LanguageAPI.MESSAGE_IDENTIFIER);
        out.writeUTF(player.getUniqueId().toString());
        out.writeUTF(locale.toLanguageTag());
        boolean sent = server.sendPluginMessage(OUTGOING, out.toByteArray());
        LanguageAPI.LOGGER.info("Sent plugin message " + OUTGOING.getId() + " for " + player.getUniqueId() + " with content " + locale.toLanguageTag() + ": " + sent);
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (!(sender instanceof Player)) {
            return;
        }

        Player player = (Player) sender;
        String uuid = player.getUniqueId().toString();
        if (args.length >= 1) {

            if (player.hasPermission("languageapi.reload") && args[0].equalsIgnoreCase("reload")) {
                this.api.loadMessages();
                Message message = new Message("language.command.reloaded").translate(this.api, uuid).prefix(ConfigManager.PREFIX.getValue());
                player.sendMessage(message.buildComponent());
                return;
            }

            Locale locale = Locale.forLanguageTag(args[0]);
            if (api.doesFileExist(locale)) {
                LanguageEntity entity = LanguageEntity.getEntity(player.getUniqueId().toString());
                entity.setLanguage(locale);
                updateSubServers(player, locale);
                entity.saveStats();
                Message message = new Message("language.command.set").translate(this.api, uuid).replace("%language%", api.getLanguageString(uuid)).prefix(ConfigManager.PREFIX.getValue());
                player.sendMessage(message.buildComponent());
                return;
            }
        }
        Message message = new Message("language.command.invalid-language").translate(this.api, uuid).prefix(ConfigManager.PREFIX.getValue());
        player.sendMessage(message.buildComponent());
    }
}
