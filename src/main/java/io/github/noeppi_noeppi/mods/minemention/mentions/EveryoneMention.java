package io.github.noeppi_noeppi.mods.minemention.mentions;

import io.github.noeppi_noeppi.mods.minemention.api.SpecialMention;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.function.Predicate;

public class EveryoneMention implements SpecialMention {

    public static final EveryoneMention INSTANCE = new EveryoneMention();
    
    private EveryoneMention() {
        
    }

    @Override
    public IFormattableTextComponent description() {
        return new TranslationTextComponent("minemention.everyone");
    }

    @Override
    public Predicate<ServerPlayerEntity> selectPlayers(ServerPlayerEntity sender) {
        return player -> true;
    }
}
