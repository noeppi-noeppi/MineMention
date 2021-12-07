package io.github.noeppi_noeppi.mods.minemention.network;

import io.github.noeppi_noeppi.mods.minemention.client.ClientMentions;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SpecialMentionUpdateHandler {
    
    public static void handle(SpecialMentionUpdateSerializer.SpecialMentionUpdateMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientMentions.update(msg.specialMentions);
            ClientMentions.updateDefault(msg.defaultMentions);
        });
        ctx.get().setPacketHandled(true);
    }
}
