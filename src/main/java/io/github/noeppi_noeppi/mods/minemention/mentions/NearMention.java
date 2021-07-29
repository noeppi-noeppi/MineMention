package io.github.noeppi_noeppi.mods.minemention.mentions;

import io.github.noeppi_noeppi.mods.minemention.api.SpecialMention;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Predicate;

public class NearMention implements SpecialMention {
    
    public static final NearMention INSTANCE = new NearMention();
    
    private NearMention() {
        
    }
    @Override
    public MutableComponent description() {
        return new TranslatableComponent("minemention.near");
    }

    @Override
    public Predicate<ServerPlayer> selectPlayers(ServerPlayer sender) {
        return player -> sender.level.dimension().equals(player.level.dimension()) && sender.position().distanceToSqr(player.position()) <= 100 * 100;
    }
}
