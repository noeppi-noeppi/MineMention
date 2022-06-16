package io.github.noeppi_noeppi.mods.minemention.mentions;

import io.github.noeppi_noeppi.mods.minemention.api.SpecialMention;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Predicate;

public class OnePlayerMention implements SpecialMention {

    public final String name;

    public OnePlayerMention(String name) {
        this.name = name;
    }

    @Override
    public MutableComponent description() {
        return Component.translatable("minemention.one_player", this.name);
    }

    @Override
    public Predicate<ServerPlayer> selectPlayers(ServerPlayer sender) {
        return player -> this.name.equals(player.getGameProfile().getName());
    }

    @Override
    public boolean available(ServerPlayer sender) {
        return sender.getLevel().getServer().getPlayerList().getPlayerByName(this.name) != null;
    }
}
