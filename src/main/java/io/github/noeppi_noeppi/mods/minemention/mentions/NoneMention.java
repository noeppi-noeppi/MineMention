package io.github.noeppi_noeppi.mods.minemention.mentions;

import io.github.noeppi_noeppi.mods.minemention.api.SpecialMention;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Predicate;

public class NoneMention implements SpecialMention {

    public static final NoneMention INSTANCE = new NoneMention();
    
    private NoneMention() {
        
    }

    @Override
    public MutableComponent description() {
        return Component.empty();
    }

    @Override
    public Predicate<ServerPlayer> selectPlayers(ServerPlayer sender) {
        return player -> false;
    }

    @Override
    public boolean available(ServerPlayer sender) {
        return false;
    }
}
