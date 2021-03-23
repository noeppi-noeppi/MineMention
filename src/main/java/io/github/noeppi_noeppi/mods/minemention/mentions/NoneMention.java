package io.github.noeppi_noeppi.mods.minemention.mentions;

import io.github.noeppi_noeppi.mods.minemention.api.SpecialMention;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.function.Predicate;

public class NoneMention implements SpecialMention {

    public static final NoneMention INSTANCE = new NoneMention();
    
    private NoneMention() {
        
    }

    @Override
    public IFormattableTextComponent description() {
        return new StringTextComponent("");
    }

    @Override
    public Predicate<ServerPlayerEntity> selectPlayers(ServerPlayerEntity sender) {
        return player -> false;
    }

    @Override
    public boolean available(ServerPlayerEntity sender) {
        return false;
    }
}
