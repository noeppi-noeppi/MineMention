package io.github.noeppi_noeppi.mods.minemention.mentions;

import io.github.noeppi_noeppi.mods.minemention.api.SpecialMention;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.function.Predicate;

public class OnePlayerMention implements SpecialMention {

    public final String name;

    public OnePlayerMention(String name) {
        this.name = name;
    }

    @Override
    public IFormattableTextComponent description() {
        return new TranslationTextComponent("minemention.one_player", this.name);
    }

    @Override
    public Predicate<ServerPlayerEntity> selectPlayers(ServerPlayerEntity sender) {
        return player -> this.name.equals(player.getGameProfile().getName());
    }

    @Override
    public boolean available(ServerPlayerEntity sender) {
        return sender.getServerWorld().getServer().getPlayerList().getPlayerByUsername(this.name) != null;
    }
}
