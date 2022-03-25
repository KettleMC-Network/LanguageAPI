package net.kettlemc.language.velocity;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.LegacyChannelIdentifier;
import net.kettlemc.language.LanguageAPI;
import net.kettlemc.language.entity.LanguageEntity;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Plugin(id = "languageapi", name = "LanguageAPI", version = "1.0", url = "https://kettlemc.net", description = "LanguageAPI for translating messages", authors = { "LeStegii" })
public class VelocityAdapter implements SimpleCommand {

    /**
     * Sends a message to a player/the console
     * @param api The LanguageAPI you want to send the message from
     * @param receiver The CommandSource you want to send the message to
     * @param path The message path you want to send to the CommandSource
     **/
    public static void sendMessage(LanguageAPI api, CommandSource receiver, String path) {
        receiver.sendMessage(color(getMessage(api, receiver, path)));
    }

    /**
     * @param api The LanguageAPI you want to send the message from
     * @param sender The CommandSource you want to send the message to
     * @param path The message path you want to send to the CommandSource
     * @return Colored and translated message
     **/
    public static String getMessage(LanguageAPI api, CommandSource sender, String path) {
        String message;
        if (sender instanceof Player) {
            message = api.getMessage(path, ((Player) sender).getUniqueId().toString());
        } else {
            message = api.getMessage(path, LanguageAPI.getDefaultLang());
        }
        return message;
    }

    /**
     * @param sender CommandSource you want to get the UUID from
     * @return UUID of the provided entity or null if not a player
     **/
    public static String getUUID(CommandSource sender) {
        String uuid = null;
        if (sender instanceof Player)
            uuid = ((Player) sender).getUniqueId().toString();
        return uuid;
    }

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
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Loaded as a velocity plugin.");
        if (LanguageAPI.isEnableVelocity()) {
            commandManager.register(getCommandMeta("language", "lang", "sprache"), this);
        }
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
        LanguageEntity entity = LanguageEntity.getEntity(event.getPlayer().getUniqueId().toString());

        if (!entity.isLoaded())
            entity.loadLanguage();

        String uuid = getUUID(event.getPlayer());
        this.server.getScheduler().buildTask(this, () -> {
            event.getPlayer().sendMessage(color(LanguageAPI.getPrefix() + this.api.getMessage("language.join.selected", Locale.ENGLISH).replace("%language%", api.getLanguageString(uuid).toUpperCase())));
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

    public static TextComponent color(String input) {
        return LegacyComponentSerializer.legacy('&').deserialize(input.replace('§', '&'));
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (!(sender instanceof Player)) {
            return;
        }
        Player player = (Player) sender;
        if (args.length >= 1) {
            Locale locale = Locale.forLanguageTag(args[0]);
            if (api.doesFileExist(locale)) {
                LanguageEntity entity = LanguageEntity.getEntity(player.getUniqueId().toString());
                entity.setLanguage(locale);
                updateSubServers(player, locale);
                entity.saveStats();
                player.sendMessage(color(LanguageAPI.getPrefix() + getMessage(api, player, "language.command.set").replace("%language%", api.getLanguageString(getUUID(player)))));
                return;
            }
        }
        player.sendMessage(color(LanguageAPI.getPrefix() + getMessage(api, player, "language.command.invalid-language")));
    }
}
