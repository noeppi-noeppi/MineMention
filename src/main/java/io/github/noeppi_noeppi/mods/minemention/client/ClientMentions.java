package io.github.noeppi_noeppi.mods.minemention.client;

import com.google.common.collect.ImmutableMap;
import io.github.noeppi_noeppi.mods.minemention.MentionType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class ClientMentions {

    private static Map<String, Component> special = ImmutableMap.of();
    private static Component currentDefault = new TextComponent("error: not yet received");
    
    public static MentionType getType(String key, Collection<String> players) {
        if (special.containsKey(key)) {
            return MentionType.GROUP;
        } else if (players.contains(key)) {
            return MentionType.PLAYER;
        } else {
            return MentionType.INVALID;
        }
    }
    
    public static List<Pair<String, Component>> suggest(Collection<String> players) {
        List<Pair<String, Component>> pairs = new ArrayList<>();
        List<String> specialKeys = new ArrayList<>(special.keySet());
        specialKeys.sort(Comparator.comparing(String::toLowerCase));
        for (String key : specialKeys) {
            pairs.add(Pair.of(key, special.get(key)));
        }
        List<String> playerNames = new ArrayList<>(players);
        playerNames.sort(Comparator.comparing(String::toLowerCase));
        for (String player : playerNames) {
            pairs.add(Pair.of(player, new TextComponent("Mention player " + player)));
        }
        return pairs;
    }
    
    public static void update(Map<String, Component> specialMentions) {
        special = ImmutableMap.copyOf(specialMentions);
    }

    public static Component getCurrentDefault() {
        return currentDefault;
    }

    public static void updateDefault(Component d) {
        currentDefault = d;
    }
}
