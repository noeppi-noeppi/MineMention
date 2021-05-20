package io.github.noeppi_noeppi.mods.minemention.client;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.ITextComponent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class SuggestionUtil {

    // The method in ISuggestionProvider sorts he suggestions. We sort them ourselves to
    // have special mentions over player mentions. So this is required which prevents
    // them from being sorted again.
    public static CompletableFuture<Suggestions> suggest(List<Pair<String, ITextComponent>> suggestions, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase();
        List<Suggestion> result = new ArrayList<>();

        for (Pair<String, ITextComponent> suggestion : suggestions) {
            if (ISuggestionProvider.shouldSuggest(remaining, suggestion.getLeft().toLowerCase(Locale.ROOT))
                    && !suggestion.getLeft().equalsIgnoreCase(remaining)) {
                result.add(new Suggestion(StringRange.between(builder.getStart(), builder.getInput().length()), suggestion.getLeft(), () -> suggestion.getRight().getString()));
            }
        }

        return CompletableFuture.completedFuture(createNotSorted(builder.getInput(), result));
    }

    private static Suggestions createNotSorted(String input, List<Suggestion> suggestions) {
        if (suggestions.isEmpty()) {
            return Suggestions.create(input, Collections.emptyList());
        }
        int start = Integer.MAX_VALUE;
        int end = Integer.MIN_VALUE;
        for (Suggestion suggestion : suggestions) {
            start = Math.min(suggestion.getRange().getStart(), start);
            end = Math.max(suggestion.getRange().getEnd(), end);
        }
        final StringRange range = new StringRange(start, end);
        return new Suggestions(range, suggestions.stream().map(s -> s.expand(input, range)).collect(Collectors.toList()));
    }

}
