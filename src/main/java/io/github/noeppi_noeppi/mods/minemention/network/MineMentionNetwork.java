package io.github.noeppi_noeppi.mods.minemention.network;

import io.github.noeppi_noeppi.libx.mod.ModX;
import io.github.noeppi_noeppi.libx.network.NetworkX;
import io.github.noeppi_noeppi.mods.minemention.DefaultMentions;
import io.github.noeppi_noeppi.mods.minemention.api.SpecialMentions;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.PacketDistributor;

public class MineMentionNetwork extends NetworkX {

    public MineMentionNetwork(ModX mod) {
        super(mod);
    }

    @Override
    protected String getProtocolVersion() {
        return "1";
    }

    @Override
    protected void registerPackets() {
        this.register(new SpecialMentionUpdateSerializer(), () -> SpecialMentionUpdateHandler::handle, NetworkDirection.PLAY_TO_CLIENT);
    }
    
    public void updateSpecialMentions(ServerPlayerEntity player) {
        this.instance.send(PacketDistributor.PLAYER.with(() -> player), new SpecialMentionUpdateSerializer.SpecialMentionUpdateMessage(SpecialMentions.getSyncPacket(player), DefaultMentions.getDefaultMentionString(player)));
    }
}
