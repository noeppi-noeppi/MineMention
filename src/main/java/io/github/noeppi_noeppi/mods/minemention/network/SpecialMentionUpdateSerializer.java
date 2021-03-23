package io.github.noeppi_noeppi.mods.minemention.network;

import com.google.common.collect.ImmutableMap;
import io.github.noeppi_noeppi.libx.network.PacketSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

import java.util.Map;

public class SpecialMentionUpdateSerializer implements PacketSerializer<SpecialMentionUpdateSerializer.SpecialMentionUpdateMessage> {


    @Override
    public Class<SpecialMentionUpdateMessage> messageClass() {
        return SpecialMentionUpdateMessage.class;
    }

    @Override
    public void encode(SpecialMentionUpdateMessage msg, PacketBuffer buffer) {
        buffer.writeVarInt(msg.specialMentions.size());
        for (Map.Entry<String, ITextComponent> entry : msg.specialMentions.entrySet()) {
            buffer.writeString(entry.getKey(), 0x7fff);
            buffer.writeTextComponent(entry.getValue());
        }
        buffer.writeTextComponent(msg.defaultMentions);
    }

    @Override
    public SpecialMentionUpdateMessage decode(PacketBuffer buffer) {
        ImmutableMap.Builder<String, ITextComponent> builder = ImmutableMap.builder();
        int size = buffer.readVarInt();
        for (int i = 0; i< size; i++) {
            builder.put(buffer.readString(0x7fff), buffer.readTextComponent());
        }
        return new SpecialMentionUpdateMessage(builder.build(), buffer.readTextComponent());
    }

    public static class SpecialMentionUpdateMessage {
        
        public final Map<String, ITextComponent> specialMentions;
        public final ITextComponent defaultMentions;

        public SpecialMentionUpdateMessage(Map<String, ITextComponent> specialMentions, ITextComponent defaultMentions) {
            this.specialMentions = specialMentions;
            this.defaultMentions = defaultMentions;
        }
    }
}
