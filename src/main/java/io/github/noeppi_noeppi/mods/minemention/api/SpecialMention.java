package io.github.noeppi_noeppi.mods.minemention.api;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.IFormattableTextComponent;

import java.util.function.Predicate;

public interface SpecialMention {
    
    IFormattableTextComponent description();
    Predicate<ServerPlayerEntity> selectPlayers(ServerPlayerEntity sender);
    default boolean available(ServerPlayerEntity sender) {
        return true;
    }
}
