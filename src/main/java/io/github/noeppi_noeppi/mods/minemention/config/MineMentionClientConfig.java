package io.github.noeppi_noeppi.mods.minemention.config;

import org.moddingx.libx.annotation.config.RegisterConfig;
import org.moddingx.libx.config.Config;

@RegisterConfig(value = "client", client = true)
public class MineMentionClientConfig {
    
    @Config("Sets where the default mentions should be displayed when the chat gui is open.")
    public static DisplayLocation displayLocation = DisplayLocation.RIGHT;

    public enum DisplayLocation {
        HIDDEN, LEFT, RIGHT
    }
}
