package io.github.noeppi_noeppi.mods.minemention.network;

import com.google.common.collect.ImmutableMap;
import io.github.noeppi_noeppi.mods.minemention.client.ClientMentions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;
import org.moddingx.libx.network.PacketHandler;
import org.moddingx.libx.network.PacketSerializer;

import java.util.Map;
import java.util.function.Supplier;

public record SpecialMentionUpdateMessage(Map<String, Component> specialMentions, Component defaultMentions) {
    
    public static class Serializer implements PacketSerializer<SpecialMentionUpdateMessage> {

        @Override
        public Class<SpecialMentionUpdateMessage> messageClass() {
            return SpecialMentionUpdateMessage.class;
        }

        @Override
        public void encode(SpecialMentionUpdateMessage msg, FriendlyByteBuf buffer) {
            buffer.writeVarInt(msg.specialMentions.size());
            for (Map.Entry<String, Component> entry : msg.specialMentions.entrySet()) {
                buffer.writeUtf(entry.getKey());
                buffer.writeComponent(entry.getValue());
            }
            buffer.writeComponent(msg.defaultMentions);
        }

        @Override
        public SpecialMentionUpdateMessage decode(FriendlyByteBuf buffer) {
            ImmutableMap.Builder<String, Component> builder = ImmutableMap.builder();
            int size = buffer.readVarInt();
            for (int i = 0; i< size; i++) {
                builder.put(buffer.readUtf(), buffer.readComponent());
            }
            return new SpecialMentionUpdateMessage(builder.build(), buffer.readComponent());
        }
    }
    
    public static class Handler implements PacketHandler<SpecialMentionUpdateMessage> {

        @Override
        public Target target() {
            return Target.MAIN_THREAD;
        }

        @Override
        public boolean handle(SpecialMentionUpdateMessage msg, Supplier<NetworkEvent.Context> ctx) {
            ClientMentions.update(msg.specialMentions);
            ClientMentions.updateDefault(msg.defaultMentions);
            return true;
        }
    }
}
