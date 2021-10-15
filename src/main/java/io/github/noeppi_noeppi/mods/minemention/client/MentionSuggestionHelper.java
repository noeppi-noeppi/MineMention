package io.github.noeppi_noeppi.mods.minemention.client;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.noeppi_noeppi.mods.minemention.MentionType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

public class MentionSuggestionHelper extends CommandSuggestions {

    private final CommandSuggestions commands;
    private final List<Triple<Integer, Integer, MentionType>> highlights = new ArrayList<>();
    
    // Factory method, so we can get the parent formatter for commands before the super constructor
    // replaces that with a no-op formatter.
    public static MentionSuggestionHelper create(CommandSuggestions commands, Minecraft mc, Screen screen, Font font, int min, int max) {
        BiFunction<String, Integer, FormattedCharSequence> parentFormatter = commands.input.formatter;
        return new MentionSuggestionHelper(commands, mc, screen, commands.input, font, min, max, parentFormatter);
    }
    
    private MentionSuggestionHelper(CommandSuggestions commands, Minecraft mc, Screen screen, EditBox inputField, Font font, int min, int max, BiFunction<String, Integer, FormattedCharSequence> parentFormatter) {
        super(mc, screen, inputField, font, false, false, min, max, true, 0xD0000000);
        this.commands = commands;
        this.input.setFormatter((str, clipFront) -> {
            if (str.strip().startsWith("/") || commands.commandsOnly) {
                return parentFormatter.apply(str, clipFront);
            } else {
                return this.formatMentionChat(str, clipFront);
            }
        });
    }

    public void updateCommandInfo() {
        String input = this.input.getValue();

        if (!this.keepSuggestions) {
            this.input.setSuggestion(null);
            this.suggestions = null;
        }

        this.commandUsage.clear();
        StringReader reader = new StringReader(input);
        String cs = input.substring(0, this.input.getCursorPosition());

        reader.skipWhitespace();
        if ((reader.canRead() && reader.peek() == '/') || this.commands.commandsOnly) {
            // We're inside a command. We won't do our magic here.
            this.pendingSuggestions = SharedSuggestionProvider.suggest(Collections.emptyList(), new SuggestionsBuilder(cs, getLastWordIndex(cs)));
            return;
        }
        this.highlights.clear();
        Collection<String> players = this.minecraft.player == null ? Collections.emptyList() : this.minecraft.player.connection.getSuggestionsProvider().getOnlinePlayerNames();
        while (true) {
            reader.skipWhitespace();
            if (!reader.canRead()) break;
            if (reader.read() == '@') {
                int at = reader.getCursor();
                String str = reader.readUnquotedString();
                this.highlights.add(Triple.of(at - 1, reader.getCursor(), ClientMentions.getType(str, players)));
            }
        }

        reader = new StringReader(cs);
        String current = null;
        int at = -1;
        while (true) {
            reader.skipWhitespace();
            if (!reader.canRead()) break;
            if (reader.read() == '@') {
                at = reader.getCursor();
                current = reader.readUnquotedString();
            } else {
                current = null;
                at = -1;
            }
        }
        if (current != null) {
            this.pendingSuggestions = SuggestionUtil.suggest(ClientMentions.suggest(players), new SuggestionsBuilder(cs, at));
            this.pendingSuggestions.thenRun(() -> {
                if (this.allowSuggestions && this.minecraft.options.autoSuggestions) {
                    this.showSuggestions(false);
                }
            });
        } else {
            this.pendingSuggestions = SharedSuggestionProvider.suggest(Collections.emptyList(), new SuggestionsBuilder(cs, getLastWordIndex(cs)));
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.suggestions != null && this.suggestions.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (this.screen.getFocused() == this.input && keyCode == 258) {
            this.showSuggestions(true);
            // False here so the command suggestion helper can also update
            // the command suggestion helper will always catch this code so
            // we can return false here.
            return false;
        } else {
            return false;
        }
    }

    @Nonnull
    public FormattedCharSequence formatMentionChat(@Nonnull String command, int maxLength) {
        List<FormattedCharSequence> list = new ArrayList<>();
        int current = maxLength;
        for (Triple<Integer, Integer, MentionType> entry : this.highlights) {
            if (current <= entry.getMiddle()) {
                if (current < entry.getLeft() && Math.min(command.length(), entry.getLeft() - maxLength) - Math.max(0, current - maxLength) >= 0) {
                    list.add(FormattedCharSequence.forward(command.substring(Math.max(0, current - maxLength), Math.min(command.length(), entry.getLeft() - maxLength)), Style.EMPTY));
                }
                if (Math.min(command.length(), entry.getMiddle() - maxLength) - Math.max(0, entry.getLeft() - maxLength) >= 0) {
                    list.add(FormattedCharSequence.forward(command.substring(Math.max(0, entry.getLeft() - maxLength), Math.min(command.length(), entry.getMiddle() - maxLength)), entry.getRight().getStyle()));
                }
                current = entry.getMiddle();
            }
        }
        if (current - maxLength <= command.length()) {
            list.add(FormattedCharSequence.forward(command.substring(Math.max(0, current - maxLength)), Style.EMPTY));
        }
        return FormattedCharSequence.composite(list);
    }
}
