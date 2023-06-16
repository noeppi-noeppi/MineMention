package io.github.noeppi_noeppi.mods.minemention.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.noeppi_noeppi.mods.minemention.api.SpecialMentions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DefaultMentionSuggestions {
    
    public static CompletableFuture<Suggestions> suggest(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        try {
            String current = context.getInput().substring(builder.getStart());
            String others;
            if (current.contains(" ")) {
                others = current.substring(0, current.lastIndexOf(' ') + 1);
                current = current.substring(current.lastIndexOf(' ') + 1);
            } else {
                others = "";
            }
            Map<String, Component> suggestions = SpecialMentions.getSyncPacket(context.getSource().getPlayerOrException());
            for (Map.Entry<String, Component> entry : suggestions.entrySet()) {
                if (entry.getKey().toLowerCase().startsWith(current.toLowerCase())) {
                    builder.suggest(others + entry.getKey(), () -> entry.getValue().getString());
                }
            }
            for (ServerPlayer player : context.getSource().getPlayerOrException().serverLevel().getServer().getPlayerList().getPlayers()) {
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
