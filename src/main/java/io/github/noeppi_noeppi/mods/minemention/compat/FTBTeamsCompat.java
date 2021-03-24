package io.github.noeppi_noeppi.mods.minemention.compat;

import com.feed_the_beast.mods.ftbteams.api.FTBTeamsAPI;
import com.feed_the_beast.mods.ftbteams.event.PlayerJoinedTeamEvent;
import com.feed_the_beast.mods.ftbteams.event.PlayerLeftTeamEvent;
import com.feed_the_beast.mods.ftbteams.event.TeamDeletedEvent;
import io.github.noeppi_noeppi.mods.minemention.MineMention;
import io.github.noeppi_noeppi.mods.minemention.api.SpecialMention;
import io.github.noeppi_noeppi.mods.minemention.api.SpecialMentions;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class FTBTeamsCompat {
    
    public static void init() {
        MinecraftForge.EVENT_BUS.register(new Events());
    }
    
    public static void setup() {
        SpecialMentions.registerMention(new ResourceLocation(MineMention.getInstance().modid, "ftb"), "team", new FTBTeamMention());
    }

    public static class Events {
        
        @SubscribeEvent
        public void joinTeam(PlayerJoinedTeamEvent event) {
            SpecialMentions.notifyAvailabilityChange(event.getPlayer());
        }
        
        @SubscribeEvent
        public void leftTeam(PlayerLeftTeamEvent event) {
            if (FTBTeamsAPI.INSTANCE.isManagerLoaded()) {
                MinecraftServer server = FTBTeamsAPI.INSTANCE.getManager().getServer();
                @Nullable
                ServerPlayerEntity player = server.getPlayerList().getPlayerByUUID(event.getProfile().getId());
                SpecialMentions.notifyAvailabilityChange(player);
            }
        }
        
        @SubscribeEvent
        public void deletedTeam(TeamDeletedEvent event) {
            if (FTBTeamsAPI.INSTANCE.isManagerLoaded()) {
                MinecraftServer server = FTBTeamsAPI.INSTANCE.getManager().getServer();
                event.getMembers().forEach(profile -> {
                    @Nullable
                    ServerPlayerEntity player = server.getPlayerList().getPlayerByUUID(profile.getId());
                    SpecialMentions.notifyAvailabilityChange(player);
                });
            }
        }
    }
    
    public static class FTBTeamMention implements SpecialMention {

        @Override
        public IFormattableTextComponent description() {
            return new TranslationTextComponent("minemention.ftb");
        }

        @Override
        public Predicate<ServerPlayerEntity> selectPlayers(ServerPlayerEntity sender) {
            return player -> FTBTeamsAPI.INSTANCE.isManagerLoaded() && FTBTeamsAPI.INSTANCE.getManager().arePlayersInSameTeam(sender, player);
        }

        @Override
        public boolean available(ServerPlayerEntity sender) {
            
            return FTBTeamsAPI.INSTANCE.isManagerLoaded() && FTBTeamsAPI.INSTANCE.getManager().getTeam(sender).isPresent();
        }
    }
}
