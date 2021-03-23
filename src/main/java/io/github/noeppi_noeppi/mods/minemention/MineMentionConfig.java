package io.github.noeppi_noeppi.mods.minemention;

import com.google.common.collect.ImmutableMap;
import io.github.noeppi_noeppi.libx.config.Config;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

public class MineMentionConfig {

    @Config(value = {
            "A map of special mention values to registered special mentions.",
            "When a special mention is available that is not present in this map, its default value will be used.",
            "To remove a mention key that is used because it's a default, set it to minemention:none",
            "A redirection of the `everyone` mention is not possible and will be ignored."
    }, elementType = ResourceLocation.class)
    public static Map<String, ResourceLocation> mentions = ImmutableMap.of();
}