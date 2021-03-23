package io.github.noeppi_noeppi.mods.minemention.mentions;

import io.github.noeppi_noeppi.mods.minemention.api.SpecialMention;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.function.Predicate;

public class HereMention implements SpecialMention {

    public static final HereMention INSTANCE = new HereMention();
    
    private HereMention() {
        
    }

    @Override
    public IFormattableTextComponent description() {
        return new TranslationTextComponent("minemention.here");
    }

    @Override
    public Predicate<ServerPlayerEntity> selectPlayers(ServerPlayerEntity sender) {
        return player -> sender.world.getDimensionKey().equals(player.world.getDimensionKey());
    }
}
