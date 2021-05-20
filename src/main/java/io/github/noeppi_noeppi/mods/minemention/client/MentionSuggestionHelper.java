package io.github.noeppi_noeppi.mods.minemention.client;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.noeppi_noeppi.mods.minemention.MentionType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.CommandSuggestionHelper;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.Style;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MentionSuggestionHelper extends CommandSuggestionHelper {

    private final CommandSuggestionHelper commands;
    private final List<Triple<Integer, Integer, MentionType>> highlights = new ArrayList<>();
    
    public MentionSuggestionHelper(CommandSuggestionHelper commands, Minecraft mc, Screen screen, TextFieldWidget inputField, FontRenderer font, int min, int max) {
        super(mc, screen, inputField, font, false, false, min, max, true, 0xD0000000);
        this.commands = commands;
        this.inputField.setTextFormatter((str, clipFront) -> {
            if (str.trim().startsWith("/") || commands.commandsOnly) {
                return commands.getParsedSuggestion(str, clipFront);
            } else {
                return this.getParsedSuggestion(str, clipFront);
            }
        });
    }

    public void init() {
        String s = this.inputField.getText();
        if (this.parseResults != null && !this.parseResults.getReader().getString().equals(s)) {
            this.parseResults = null;
        }

        if (!this.isApplyingSuggestion) {
            this.inputField.setSuggestion(null);
            this.suggestions = null;
        }

        this.exceptionList.clear();
        StringReader reader = new StringReader(s);
        String cs = s.substring(0, this.inputField.getCursorPosition());

        reader.skipWhitespace();
        if ((reader.canRead() && reader.peek() == '/') || this.commands.commandsOnly) {
            // We're inside a command. We won't do our magic here.
            this.suggestionsFuture = ISuggestionProvider.suggest(Collections.emptyList(), new SuggestionsBuilder(cs, getLastWhitespace(cs)));
            return;
        }
        this.highlights.clear();
        Collection<String> players = this.mc.player == null ? Collections.emptyList() : this.mc.player.connection.getSuggestionProvider().getPlayerNames();
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
            List<String> list = new ArrayList<>();
            list.add("team");
            list.addAll(players);
            this.suggestionsFuture = SuggestionUtil.suggest(ClientMentions.suggest(players), new SuggestionsBuilder(cs, at));
            this.suggestionsFuture.thenRun(() -> {
                if (this.autoSuggest && this.mc.gameSettings.autoSuggestCommands) {
                    this.updateSuggestions(false);
                }
            });
        } else {
            this.suggestionsFuture = ISuggestionProvider.suggest(Collections.emptyList(), new SuggestionsBuilder(cs, getLastWhitespace(cs)));
        }
    }

    @Override
    public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.suggestions != null && this.suggestions.onKeyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (this.screen.getListener() == this.inputField && keyCode == 258) {
            this.updateSuggestions(true);
            // False here so the command suggestion helper can also update
            // the command suggestion helper will always catch this code so
            // we can return false here.
            return false;
        } else {
            return false;
        }
    }

    @Nonnull
    @Override
    public IReorderingProcessor getParsedSuggestion(@Nonnull String command, int clipFront) {
        List<IReorderingProcessor> list = new ArrayList<>();
        int current = clipFront;
        for (Triple<Integer, Integer, MentionType> entry : this.highlights) {
            if (current <= entry.getMiddle()) {
                if (current < entry.getLeft() && Math.min(command.length(), entry.getLeft() - clipFront) - Math.max(0, current - clipFront) >= 0) {
                    list.add(IReorderingProcessor.fromString(command.substring(Math.max(0, current - clipFront), Math.min(command.length(), entry.getLeft() - clipFront)), Style.EMPTY));
                }
                if (Math.min(command.length(), entry.getMiddle() - clipFront) - Math.max(0, entry.getLeft() - clipFront) >= 0) {
                    list.add(IReorderingProcessor.fromString(command.substring(Math.max(0, entry.getLeft() - clipFront), Math.min(command.length(), entry.getMiddle() - clipFront)), entry.getRight().getStyle()));
                }
                current = entry.getMiddle();
            }
        }
        if (current - clipFront <= command.length()) {
            list.add(IReorderingProcessor.fromString(command.substring(Math.max(0, current - clipFront)), Style.EMPTY));
        }
        return IReorderingProcessor.fromList(list);
    }
}
