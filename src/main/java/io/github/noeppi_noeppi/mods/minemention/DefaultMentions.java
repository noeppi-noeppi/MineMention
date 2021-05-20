package io.github.noeppi_noeppi.mods.minemention;

import com.google.common.collect.ImmutableList;
import io.github.noeppi_noeppi.mods.minemention.api.SpecialMention;
import io.github.noeppi_noeppi.mods.minemention.api.SpecialMentions;
import io.github.noeppi_noeppi.mods.minemention.mentions.OnePlayerMention;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DefaultMentions {

    public static List<SpecialMention> getMentions(ServerPlayerEntity player, List<SpecialMention> explicit) {
        if (!explicit.isEmpty()) {
            return explicit;
        }
        return getMentionStrings(player).stream()
                .map(m -> SpecialMentions.getMention(m, player))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    public static IFormattableTextComponent getDefaultMentionString(ServerPlayerEntity player) {
        List<Pair<String, SpecialMention>> list = getMentionStrings(player).stream()
                .map(m -> Pair.of(m, SpecialMentions.getMention(m, player)))
                .filter(m -> m.getRight() != null)
                .filter(m -> m.getRight().available(player))
                .collect(Collectors.toList());
        IFormattableTextComponent text = new StringTextComponent("");
        List<Pair<String, SpecialMention>> special = list.stream().filter(m -> !(m.getRight() instanceof OnePlayerMention)).collect(Collectors.toList());
        List<Pair<String, SpecialMention>> players = list.stream().filter(m -> m.getRight() instanceof OnePlayerMention).collect(Collectors.toList());
        special.sort(Comparator.comparing(m -> m.getLeft().toLowerCase()));
        special.sort(Comparator.comparing(m -> m.getLeft().toLowerCase()));
        for (Pair<String, SpecialMention> mention : special) {
            text = text.appendSibling(new StringTextComponent(" ")).appendSibling(new StringTextComponent("@" + mention.getLeft()).mergeStyle(MentionType.GROUP.getStyle()));
        }
        for (Pair<String, SpecialMention> mention : players) {
            text = text.appendSibling(new StringTextComponent(" ")).appendSibling(new StringTextComponent("@" + mention.getLeft()).mergeStyle(MentionType.PLAYER.getStyle()));
        }
        return text;
    }
    
    private static List<String> getMentionStrings(ServerPlayerEntity player) {
        if (player.getPersistentData().contains("minemention_default", Constants.NBT.TAG_LIST)) {
            ListNBT list = player.getPersistentData().getList("minemention_default", Constants.NBT.TAG_STRING);
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
    
    public static void updateMentionStrings(ServerPlayerEntity player, @Nullable List<String> strings) {
        if (strings == null) {
            player.getPersistentData().remove("minemention_default");
        } else {
            ListNBT list = new ListNBT();
            for (String str : strings) {
                list.add(StringNBT.valueOf(str));
            }
            player.getPersistentData().put("minemention_default", list);
        }
    }
}
