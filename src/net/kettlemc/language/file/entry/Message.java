package net.kettlemc.language.file.entry;

import net.kettlemc.language.LanguageAPI;
import org.bukkit.ChatColor;

import java.util.Locale;

public class Message {

    private String content;
    private boolean translated;

    public Message(String text) {
        this.content = text;
    }

    /**
     * Replaces text in the message
     * @return Message - The current message
     */
    public Message replace(String toReplace, String replacement) {
        this.content = this.content.replace(toReplace, replacement);
        return this;
    }

    /**
     * Translates the current message using the LanguageAPI
     * @return Message - The current message
     */
    public Message translate(LanguageAPI api, Locale locale) {
        if (!translated && locale != null)
            this.content = api.getMessage(content, locale);
        translated = true;
        return this;
    }

    /**
     * Translates the current message using the LanguageAPI
     * @return Message - The current message
     */
    public Message translate(LanguageAPI api, String uuid) {
        if (!translated && uuid != null)
            this.content = api.getMessage(content, uuid);
        translated = true;
        return this;
    }

    /**
     * Adds a predefined prefix to the message
     * @return Message - The current message
     */
    public Message prefix(String prefix) {
        this.content = prefix + this.content;
        return this;
    }

    /**
     * Converts the message to a Component
     * @return Component - The current message as a component
     */
    public net.kyori.adventure.text.Component buildMiniMessage() {
        return net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(this.content);
    }

    /**
     * Converts the message to a Component
     * @return Component - The current message as a component
     */
    public net.kyori.adventure.text.Component buildComponent() {
        return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacy('&').deserialize(this.content);
    }

    /**
     * Converts the message to a colored String
     * @return String - The current message as a colored string
     */
    public String buildChatColor() {
        return ChatColor.translateAlternateColorCodes('&', this.content);
    }

}
