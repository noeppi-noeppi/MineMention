package io.github.noeppi_noeppi.mods.minemention;

import io.github.noeppi_noeppi.mods.minemention.api.SpecialMentions;
import io.github.noeppi_noeppi.mods.minemention.mentions.EveryoneMention;
import io.github.noeppi_noeppi.mods.minemention.mentions.HereMention;
import io.github.noeppi_noeppi.mods.minemention.mentions.NearMention;
import io.github.noeppi_noeppi.mods.minemention.mentions.NoneMention;
import io.github.noeppi_noeppi.mods.minemention.network.MineMentionNetwork;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.moddingx.libx.mod.ModX;

@Mod("minemention")
public final class MineMention extends ModX {
    
    public static final Logger logger = LogManager.getLogger();
    
    private static MineMention instance;
    private static MineMentionNetwork network;
    
    public MineMention() {
        instance = this;
        network = new MineMentionNetwork(this);
        
        MinecraftForge.EVENT_BUS.register(new EventListener());
    }

    @Override
    protected void setup(FMLCommonSetupEvent fmlCommonSetupEvent) {
        SpecialMentions.registerMention(new ResourceLocation(this.modid, "none"), null, NoneMention.INSTANCE);
        SpecialMentions.registerMention(new ResourceLocation(this.modid, "everyone"), "everyone", EveryoneMention.INSTANCE);
        SpecialMentions.registerMention(new ResourceLocation(this.modid, "here"), "here", HereMention.INSTANCE);
        SpecialMentions.registerMention(new ResourceLocation(this.modid, "near"), "near", NearMention.INSTANCE);
    }

    @Override
    protected void clientSetup(FMLClientSetupEvent fmlClientSetupEvent) {

    }
    
    public static MineMention getInstance() {
        return instance;
    }

    public static MineMentionNetwork getNetwork() {
        return network;
    }
}
