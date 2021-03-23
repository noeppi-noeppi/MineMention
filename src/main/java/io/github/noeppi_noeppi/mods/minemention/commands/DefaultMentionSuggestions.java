package io.github.noeppi_noeppi.mods.minemention.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.noeppi_noeppi.mods.minemention.api.SpecialMentions;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DefaultMentionSuggestions {
    
    public static CompletableFuture<Suggestions> suggest(CommandContext<CommandSource> context, SuggestionsBuilder builder) {
        try {
            String current = context.getInput().substring(builder.getStart());
            String others;
            if (current.contains(" ")) {
                others = current.substring(0, current.lastIndexOf(' ') + 1);
                current = current.substring(current.lastIndexOf(' ') + 1);
            } else {
                others = "";
            }
            Map<String, ITextComponent> suggestions = SpecialMentions.getSyncPacket(context.getSource().asPlayer());
            for (Map.Entry<String, ITextComponent> entry : suggestions.entrySet()) {
                if (entry.getKey().toLowerCase().startsWith(current.toLowerCase())) {
                    builder.suggest(others + entry.getKey(), () -> entry.getValue().getString());
                }
            }
            for (ServerPlayerEntity player : context.getSource().asPlayer().getServerWorld().getServer().getPlayerList().getPlayers()) {
                if (player.getGameProfile().getName().toLowerCase().startsWith(current.toLowerCase())) {
                    builder.suggest(others + player.getGameProfile().getName());
                }
            }
            return builder.buildFuture();
        } catch (CommandSyntaxException e) {
            return Suggestions.empty();
        }
    }
}
