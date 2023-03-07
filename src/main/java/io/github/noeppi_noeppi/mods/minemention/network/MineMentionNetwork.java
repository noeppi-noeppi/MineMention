package io.github.noeppi_noeppi.mods.minemention.network;

import io.github.noeppi_noeppi.mods.minemention.DefaultMentions;
import io.github.noeppi_noeppi.mods.minemention.api.SpecialMentions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.network.NetworkX;

public class MineMentionNetwork extends NetworkX {

    public MineMentionNetwork(ModX mod) {
        super(mod);
    }

    @Override
    protected Protocol getProtocol() {
        return Protocol.of("3");
    }

    @Override
    protected void registerPackets() {
        this.registerGame(NetworkDirection.PLAY_TO_CLIENT, new SpecialMentionUpdateMessage.Serializer(), () -> SpecialMentionUpdateMessage.Handler::new);
    }
    
    public void updateSpecialMentions(ServerPlayer player) {
        this.channel.send(PacketDistributor.PLAYER.with(() -> player), new SpecialMentionUpdateMessage(SpecialMentions.getSyncPacket(player), DefaultMentions.getDefaultMentionString(player)));
    }
}
