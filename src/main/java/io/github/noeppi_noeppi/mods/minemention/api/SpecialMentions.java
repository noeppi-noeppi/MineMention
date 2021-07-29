package io.github.noeppi_noeppi.mods.minemention.api;

import io.github.noeppi_noeppi.mods.minemention.MineMention;
import io.github.noeppi_noeppi.mods.minemention.MineMentionConfig;
import io.github.noeppi_noeppi.mods.minemention.mentions.EveryoneMention;
import io.github.noeppi_noeppi.mods.minemention.mentions.NoneMention;
import io.github.noeppi_noeppi.mods.minemention.mentions.OnePlayerMention;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SpecialMentions {

    private static final Map<ResourceLocation, SpecialMention> mentions = new HashMap<>();
    private static final Map<String, ResourceLocation> defaultMentionKeys = new HashMap<>();
    private static final Set<String> blacklistDefaultMentions = new HashSet<>();

    /**
     * Registers a custom special mention
     * @param id The id of the mention
     * @param key How the mention should be written in chat by default. Can be changed in the config.
     * @param mention The mention.
     */
    public static void registerMention(ResourceLocation id, @Nullable String key, SpecialMention mention) {
        if ("everyone".equals(key) && mention != EveryoneMention.INSTANCE) throw new IllegalStateException("Registering mentions with the default key 'everyone' is not allowed.");
        if (mentions.containsKey(id)) throw new IllegalStateException("Special mention '" + id + "' registered twice.");
        mentions.put(id, mention);
        if (key != null) {
            if (defaultMentionKeys.containsKey(key)) {
                defaultMentionKeys.remove(key);
                blacklistDefaultMentions.add(key);
                MineMention.logger.warn("Duplicate mention key '" + key + "'. Unless explicitly set in config it will not be available.");
            } else if (!blacklistDefaultMentions.contains(key)) {
                defaultMentionKeys.put(key, id);
            }
        }
    }
    
    @Nullable
    public static SpecialMention getMention(String mention, ServerPlayer player) {
        if ("everyone".equals(mention)) {
            return EveryoneMention.INSTANCE;
        } else if (MineMentionConfig.mentions.containsKey(mention)
                && mentions.containsKey(MineMentionConfig.mentions.get(mention))) {
            if (mentions.get(MineMentionConfig.mentions.get(mention)) == NoneMention.INSTANCE) {
                return null;
            }
            SpecialMention m = mentions.get(MineMentionConfig.mentions.get(mention));
            return m.available(player) ? m : null;
        } else if (defaultMentionKeys.containsKey(mention)
                && mentions.containsKey(defaultMentionKeys.get(mention))) {
            if (mentions.get(defaultMentionKeys.get(mention)) == NoneMention.INSTANCE) {
                return null;
            }
            SpecialMention m = mentions.get(defaultMentionKeys.get(mention));
            return m.available(player) ? m : null;
        } else {
            SpecialMention m = new OnePlayerMention(mention);
            return m.available(player) ? m : null;
        }
    }
    
    public static Map<String, Component> getSyncPacket(ServerPlayer player) {
        Map<String, Component> specialMentions = new HashMap<>();
        // Add defaults first, so later calls with the ones frm the config will replace them
        for (Map.Entry<String, ResourceLocation> entry : defaultMentionKeys.entrySet()) {
            if (mentions.containsKey(entry.getValue())) {
                SpecialMention mention = mentions.get(entry.getValue());
                if (mention == NoneMention.INSTANCE || !mention.available(player)) {
                    specialMentions.remove(entry.getKey());
                } else {
                    specialMentions.put(entry.getKey(), mention.description());
                }
            }
        }
        for (Map.Entry<String, ResourceLocation> entry : MineMentionConfig.mentions.entrySet()) {
            if (mentions.containsKey(entry.getValue())) {
                SpecialMention mention = mentions.get(entry.getValue());
                if (mention == NoneMention.INSTANCE || !mention.available(player)) {
                    specialMentions.remove(entry.getKey());
                } else {
                    specialMentions.put(entry.getKey(), mention.description());
                }
            }
        }
        return specialMentions;
    }

    /**
     * Call this whenever the availability of one of your mentions changes.
     */
    public static void notifyAvailabilityChange(@Nullable ServerPlayer player) {
        if (player != null) {
            MineMention.getNetwork().updateSpecialMentions(player);
        }
    }
}
