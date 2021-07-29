package io.github.noeppi_noeppi.mods.minemention;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public enum MentionType {

    PLAYER(Style.EMPTY.withColor(TextColor.fromRgb(0x00AEFF))),
    GROUP(Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE)),
    INVALID(Style.EMPTY.withColor(ChatFormatting.RED));
    
    private final Style style;

    MentionType(Style style) {
        this.style = style;
    }

    public Style getStyle() {
        return this.style;
    }
}
