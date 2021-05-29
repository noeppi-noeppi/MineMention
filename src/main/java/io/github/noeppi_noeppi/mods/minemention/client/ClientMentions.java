package io.github.noeppi_noeppi.mods.minemention.client;

import com.google.common.collect.ImmutableMap;
import io.github.noeppi_noeppi.libx.util.Misc;
import io.github.noeppi_noeppi.mods.minemention.MentionType;
import net.minecraft.util.text.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class ClientMentions {

    private static Map<String, ITextComponent> special = ImmutableMap.of();
    private static ITextComponent currentDefault = new StringTextComponent("error: not yet received");
    
    public static MentionType getType(String key, Collection<String> players) {
        if (special.containsKey(key)) {
            return MentionType.GROUP;
        } else if (players.contains(key)) {
            return MentionType.PLAYER;
        } else {
            return MentionType.INVALID;
        }
    }
    
    public static List<Pair<String, ITextComponent>> suggest(Collection<String> players) {
        List<Pair<String, ITextComponent>> pairs = new ArrayList<>();
        List<String> specialKeys = new ArrayList<>(special.keySet());
        specialKeys.sort(Comparator.comparing(String::toLowerCase));
        for (String key : specialKeys) {
            pairs.add(Pair.of(key, special.get(key)));
        }
        List<String> playerNames = new ArrayList<>(players);
        playerNames.sort(Comparator.comparing(String::toLowerCase));
        for (String player : playerNames) {
            pairs.add(Pair.of(player, new StringTextComponent("Mention player " + player)));
        }
        return pairs;
    }
    
    public static void update(Map<String, ITextComponent> specialMentions) {
        special = ImmutableMap.copyOf(specialMentions);
    }

    public static ITextComponent getCurrentDefault() {
        return currentDefault;
    }

    public static void updateDefault(ITextComponent d) {
        currentDefault = d;
    }
}
