package io.github.noeppi_noeppi.mods.minemention.network;

import com.google.common.collect.ImmutableMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.moddingx.libx.network.PacketSerializer;

import java.util.Map;

public class SpecialMentionUpdateSerializer implements PacketSerializer<SpecialMentionUpdateSerializer.SpecialMentionUpdateMessage> {


    @Override
    public Class<SpecialMentionUpdateMessage> messageClass() {
        return SpecialMentionUpdateMessage.class;
    }

    @Override
    public void encode(SpecialMentionUpdateMessage msg, FriendlyByteBuf buffer) {
        buffer.writeVarInt(msg.specialMentions.size());
        for (Map.Entry<String, Component> entry : msg.specialMentions.entrySet()) {
            buffer.writeUtf(entry.getKey(), 0x7fff);
            buffer.writeComponent(entry.getValue());
        }
        buffer.writeComponent(msg.defaultMentions);
    }

    @Override
    public SpecialMentionUpdateMessage decode(FriendlyByteBuf buffer) {
        ImmutableMap.Builder<String, Component> builder = ImmutableMap.builder();
        int size = buffer.readVarInt();
        for (int i = 0; i< size; i++) {
            builder.put(buffer.readUtf(0x7fff), buffer.readComponent());
        }
        return new SpecialMentionUpdateMessage(builder.build(), buffer.readComponent());
    }

    public static class SpecialMentionUpdateMessage {
        
        public final Map<String, Component> specialMentions;
        public final Component defaultMentions;

        public SpecialMentionUpdateMessage(Map<String, Component> specialMentions, Component defaultMentions) {
            this.specialMentions = specialMentions;
            this.defaultMentions = defaultMentions;
        }
    }
}
