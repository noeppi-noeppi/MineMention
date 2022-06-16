package io.github.noeppi_noeppi.mods.minemention.mentions;

import io.github.noeppi_noeppi.mods.minemention.api.SpecialMention;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Predicate;

public class EveryoneMention implements SpecialMention {

    public static final EveryoneMention INSTANCE = new EveryoneMention();
    
    private EveryoneMention() {
        
    }

    @Override
    public MutableComponent description() {
        return Component.translatable("minemention.everyone");
    }

    @Override
    public Predicate<ServerPlayer> selectPlayers(ServerPlayer sender) {
        return player -> true;
    }
}
