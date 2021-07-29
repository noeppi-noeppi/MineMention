package io.github.noeppi_noeppi.mods.minemention.compat;

//import com.feed_the_beast.mods.ftbteams.api.FTBTeamsAPI;
//import com.feed_the_beast.mods.ftbteams.event.PlayerJoinedTeamEvent;
//import com.feed_the_beast.mods.ftbteams.event.PlayerLeftTeamEvent;
//import com.feed_the_beast.mods.ftbteams.event.TeamDeletedEvent;
import io.github.noeppi_noeppi.mods.minemention.MineMention;
import io.github.noeppi_noeppi.mods.minemention.api.SpecialMention;
import io.github.noeppi_noeppi.mods.minemention.api.SpecialMentions;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;

import java.util.function.Predicate;

public class FTBTeamsCompat {
    
    public static void init() {
        MinecraftForge.EVENT_BUS.register(new Events());
    }
    
    public static void setup() {
        SpecialMentions.registerMention(new ResourceLocation(MineMention.getInstance().modid, "ftb"), "team", new FTBTeamMention());
    }

    public static class Events {
        
//        @SubscribeEvent
//        public void joinTeam(PlayerJoinedTeamEvent event) {
//            SpecialMentions.notifyAvailabilityChange(event.getPlayer());
//        }
//        
//        @SubscribeEvent
//        public void leftTeam(PlayerLeftTeamEvent event) {
//            if (FTBTeamsAPI.INSTANCE.isManagerLoaded()) {
//                MinecraftServer server = FTBTeamsAPI.INSTANCE.getManager().getServer();
//                @Nullable
//                ServerPlayer player = server.getPlayerList().getPlayer(event.getProfile().getId());
//                SpecialMentions.notifyAvailabilityChange(player);
//            }
//        }
//        
//        @SubscribeEvent
//        public void deletedTeam(TeamDeletedEvent event) {
//            if (FTBTeamsAPI.INSTANCE.isManagerLoaded()) {
//                MinecraftServer server = FTBTeamsAPI.INSTANCE.getManager().getServer();
//                event.getMembers().forEach(profile -> {
//                    @Nullable
//                    ServerPlayer player = server.getPlayerList().getPlayer(profile.getId());
//                    SpecialMentions.notifyAvailabilityChange(player);
//                });
//            }
//        }
    }
    
    public static class FTBTeamMention implements SpecialMention {

        @Override
        public MutableComponent description() {
            return new TextComponent("Currently not working");
//            return new TranslatableComponent("minemention.ftb");
        }

        @Override
        public Predicate<ServerPlayer> selectPlayers(ServerPlayer sender) {
            return p -> false;
//            return player -> FTBTeamsAPI.INSTANCE.isManagerLoaded() && FTBTeamsAPI.INSTANCE.getManager().arePlayersInSameTeam(sender, player);
        }

        @Override
        public boolean available(ServerPlayer sender) {
            return false;
//            return FTBTeamsAPI.INSTANCE.isManagerLoaded() && FTBTeamsAPI.INSTANCE.getManager().getTeam(sender).isPresent();
        }
    }
}
