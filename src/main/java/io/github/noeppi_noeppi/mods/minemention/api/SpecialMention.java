package io.github.noeppi_noeppi.mods.minemention.api;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.IFormattableTextComponent;

import java.util.function.Predicate;

/**
 * A special mention. You can create a subclass to register a custom special mention
 */
public interface SpecialMention {

    /**
     * Gets the description tooltip that is dispalyed when hovering the suggestion.
     */
    IFormattableTextComponent description();

    /**
     * Gets a predicate that determines which player should be picked to get the message.
     * @param sender The sender of the message
     */
    Predicate<ServerPlayerEntity> selectPlayers(ServerPlayerEntity sender);

    /**
     * Returns whether the mention is available. For example a mod that adds teams could
     * create a mention that is only available when in a team.
     * Whenever this changes, call SpecialMentions.notifyAvailabilityChange
     */
    default boolean available(ServerPlayerEntity sender) {
        return true;
    }
}
