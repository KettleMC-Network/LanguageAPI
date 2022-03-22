package net.kettlemc.language.spigot.skript.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.kettlemc.language.spigot.skript.SkriptLangAddon;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class ExprTranslatedMessage extends SimpleExpression<String> {

    private Expression<Player> player;
    private Expression<String> path;

    static {
        Skript.registerExpression(ExprTranslatedMessage.class, String.class, ExpressionType.COMBINED, new String[] { "[the] (translated|localized) (message|string) %string% (for|of) %player%" });
    }

    @Override
    protected String[] get(Event e) {
        Player player = (Player) this.player.getSingle(e);
        String path = (String) this.path.getSingle(e);
        if (player == null)
            return null;

        return new String[] {SkriptLangAddon.getApi().getMessage(path, player.getUniqueId().toString())};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(Event e, boolean debug) {
        return "Translated Message Expression";
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        path = (Expression<String>) exprs[0];
        player = (Expression<Player>) exprs[1];
        return true;
    }
}
