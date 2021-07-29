package io.github.noeppi_noeppi.mods.minemention.mentions;

import io.github.noeppi_noeppi.mods.minemention.api.SpecialMention;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Predicate;

public class HereMention implements SpecialMention {

    public static final HereMention INSTANCE = new HereMention();
    
    private HereMention() {
        
    }

    @Override
    public MutableComponent description() {
        return new TranslatableComponent("minemention.here");
    }

    @Override
    public Predicate<ServerPlayer> selectPlayers(ServerPlayer sender) {
        return player -> sender.level.dimension().equals(player.level.dimension());
    }
}
