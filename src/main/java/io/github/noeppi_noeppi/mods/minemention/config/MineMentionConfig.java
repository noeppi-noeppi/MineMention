package io.github.noeppi_noeppi.mods.minemention.config;

import net.minecraft.resources.ResourceLocation;
import org.moddingx.libx.annotation.config.RegisterConfig;
import org.moddingx.libx.config.Config;

import java.util.Map;

@RegisterConfig("mentions")
public class MineMentionConfig {

    @Config({
            "A map of special mention values to registered special mentions.",
            "When a special mention is available that is not present in this map, its default value will be used.",
            "To remove a mention key that is used because it's a default, set it to minemention:none",
            "A redirection of the `everyone` mention is not possible and will be ignored."
    })
    public static Map<String, ResourceLocation> mentions = Map.of();
}
