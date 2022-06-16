package io.github.noeppi_noeppi.mods.minemention;

import com.google.common.collect.ImmutableList;
import io.github.noeppi_noeppi.mods.minemention.api.SpecialMention;
import io.github.noeppi_noeppi.mods.minemention.api.SpecialMentions;
import io.github.noeppi_noeppi.mods.minemention.mentions.OnePlayerMention;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DefaultMentions {

    public static List<SpecialMention> getMentions(ServerPlayer player, List<SpecialMention> explicit) {
        if (!explicit.isEmpty()) {
            return explicit;
        }
        return getMentionStrings(player).stream()
                .map(m -> SpecialMentions.getMention(m, player))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    public static MutableComponent getDefaultMentionString(ServerPlayer player) {
        List<Pair<String, SpecialMention>> list = getMentionStrings(player).stream()
                .map(m -> Pair.of(m, SpecialMentions.getMention(m, player)))
                .filter(m -> m.getRight() != null)
                .filter(m -> m.getRight().available(player))
                .toList();
        MutableComponent text = Component.empty();
        List<Pair<String, SpecialMention>> special = list.stream().filter(m -> !(m.getRight() instanceof OnePlayerMention)).collect(Collectors.toList());
        List<Pair<String, SpecialMention>> players = list.stream().filter(m -> m.getRight() instanceof OnePlayerMention).collect(Collectors.toList());
        special.sort(Comparator.comparing(m -> m.getLeft().toLowerCase()));
        players.sort(Comparator.comparing(m -> m.getLeft().toLowerCase()));
        for (Pair<String, SpecialMention> mention : special) {
            text = text.append(Component.literal(" ")).append(Component.literal("@" + mention.getLeft()).withStyle(MentionType.GROUP.getStyle()));
        }
        for (Pair<String, SpecialMention> mention : players) {
            text = text.append(Component.literal(" ")).append(Component.literal("@" + mention.getLeft()).withStyle(MentionType.PLAYER.getStyle()));
        }
        return text;
    }
    
    private static List<String> getMentionStrings(ServerPlayer player) {
        if (player.getPersistentData().contains("minemention_default", Tag.TAG_LIST)) {
            ListTag list = player.getPersistentData().getList("minemention_default", Tag.TAG_STRING);
            ImmutableList.Builder<String> strings = ImmutableList.builder();
            if (list.isEmpty()) {
                strings.add("everyone");
            } else {
                for (int i = 0; i < list.size(); i++) {
                    strings.add(list.getString(i));
                }
            }
            return strings.build();
        } else {
            return ImmutableList.of("everyone");
        }
    }
    
    public static void updateMentionStrings(ServerPlayer player, @Nullable List<String> strings) {
        if (strings == null) {
            player.getPersistentData().remove("minemention_default");
        } else {
            ListTag list = new ListTag();
            for (String str : strings) {
                list.add(StringTag.valueOf(str));
            }
            player.getPersistentData().put("minemention_default", list);
        }
    }
}
