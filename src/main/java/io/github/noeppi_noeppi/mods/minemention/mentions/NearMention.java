package io.github.noeppi_noeppi.mods.minemention.mentions;

import io.github.noeppi_noeppi.mods.minemention.api.SpecialMention;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.function.Predicate;

public class NearMention implements SpecialMention {
    
    public static final NearMention INSTANCE = new NearMention();
    
    private NearMention() {
        
    }
    @Override
    public IFormattableTextComponent description() {
        return new TranslationTextComponent("minemention.near");
    }

    @Override
    public Predicate<ServerPlayerEntity> selectPlayers(ServerPlayerEntity sender) {
        return player -> sender.world.getDimensionKey().equals(player.world.getDimensionKey()) && sender.getPositionVec().squareDistanceTo(player.getPositionVec()) <= 100 * 100;
    }
}
