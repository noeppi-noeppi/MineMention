package io.github.noeppi_noeppi.mods.minemention;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.StringReader;
import io.github.noeppi_noeppi.mods.minemention.api.SpecialMention;
import io.github.noeppi_noeppi.mods.minemention.api.SpecialMentions;
import io.github.noeppi_noeppi.mods.minemention.client.ClientMentions;
import io.github.noeppi_noeppi.mods.minemention.commands.MineMentionCommands;
import io.github.noeppi_noeppi.mods.minemention.mentions.OnePlayerMention;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.moddingx.libx.event.ConfigLoadedEvent;
import org.moddingx.libx.render.RenderHelper;
import org.moddingx.libx.util.game.ComponentUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class EventListener {
    
    private boolean needsUpdate;
    
    @SubscribeEvent
    public void playerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer) {
            MineMention.getNetwork().updateSpecialMentions((ServerPlayer) event.getEntity());
        }
    }
    
    @SubscribeEvent
    public void serverTick(TickEvent.LevelTickEvent event) {
        if (this.needsUpdate && event.phase == TickEvent.Phase.START && event.level instanceof ServerLevel level) {
            this.needsUpdate = false;
            for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                MineMention.getNetwork().updateSpecialMentions(player);
            }
        }
    }
    
    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        MineMentionCommands.register(event.getDispatcher());
    }
    
    @SubscribeEvent
    public void configLoad(ConfigLoadedEvent event) {
        if (event.getConfigClass() == MineMentionConfig.class && event.getReason() != ConfigLoadedEvent.LoadReason.INITIAL) {
            this.needsUpdate = true;
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void serverChat(ServerChatEvent event) {
        if (event.getMessage().trim().startsWith("\\") || event.getPlayer() == null) return; 
        List<SpecialMention> mentions = new ArrayList<>();
        Set<ServerPlayer> playersToPing = new HashSet<>();
        MutableComponent text = Component.empty();
        StringReader reader = new StringReader(event.getMessage());
        StringBuilder current = new StringBuilder();
        PlayerList playerList = event.getPlayer().getLevel().getServer().getPlayerList();
        while (reader.canRead()) {
            char chr = reader.read();
            if (chr == '@') {
                String mentionStr = reader.readUnquotedString();
                SpecialMention mention = SpecialMentions.getMention(mentionStr, event.getPlayer());
                if (mention == null) {
                    current.append("@").append(mentionStr);
                } else if (mention instanceof OnePlayerMention) {
                    ServerPlayer player = playerList.getPlayerByName(((OnePlayerMention) mention).name);
                    if (player != null) {
                        mentions.add(mention);
                        if (event.getPlayer() != player) {
                            playersToPing.add(player);
                        }
                        text = text.append(ForgeHooks.newChatWithLinks(current.toString()));
                        current = new StringBuilder();
                        text = text.append(Component.literal("@" + mentionStr)
                                .withStyle(MentionType.PLAYER.getStyle())
                                .withStyle(Style.EMPTY
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("minemention.reply")))
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "@" + mentionStr + " "))
                                )
                        );
                    } else {
                        current.append("@").append(mentionStr);
                    }
                } else {
                    mentions.add(mention);
                    text = text.append(ForgeHooks.newChatWithLinks(current.toString()));
                    current = new StringBuilder();
                    text = text.append(Component.literal("@" + mentionStr)
                            .withStyle(MentionType.GROUP.getStyle())
                            .withStyle(Style.EMPTY
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("minemention.reply")))
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "@" + mentionStr + " "))
                            )
                    );
                }
            } else {
                current.append(chr);
            }
        }
        text = text.append(ForgeHooks.newChatWithLinks(current.toString()));

        MutableComponent tooltip = Component.translatable("minemention.reply");
        if (mentions.isEmpty()) {
            tooltip = tooltip.append(Component.literal("\n")
                    .append(Component.translatable("minemention.sent"))
                    .append(DefaultMentions.getDefaultMentionString(event.getPlayer())));
        }

        MutableComponent send = Component.literal("<").append(event.getPlayer().getDisplayName().copy()
                .withStyle(Style.EMPTY
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "@" + event.getPlayer().getGameProfile().getName() + " "))
                )
        ).append(Component.literal("> ")).append(text);

        event.setComponent(text);
        event.setCanceled(true);
        mentions = DefaultMentions.getMentions(event.getPlayer(), mentions);
        List<Predicate<ServerPlayer>> predicates = mentions.stream().map(m -> m.selectPlayers(event.getPlayer())).collect(ImmutableList.toImmutableList());
        // Always show message to sender.
        Predicate<ServerPlayer> predicate = player -> player == event.getPlayer() || predicates.stream().anyMatch(p -> p.test(player));
        MineMention.logger.info(ComponentUtil.getConsoleString(send));
        playerList.getPlayers().stream().filter(predicate).forEach(player -> {
            player.sendSystemMessage(send);
            if (playersToPing.contains(player)) {
                player.playNotifySound(SoundEvents.NOTE_BLOCK_BELL, SoundSource.MASTER, 2.5f, 0.8f);
            }
        });
    }
    
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void renderChat(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (VanillaGuiOverlay.CHAT_PANEL.id().equals(event.getOverlay().id()) && mc.screen instanceof ChatScreen) {
            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();
            Font font = mc.font;
            int width = font.width(ClientMentions.getCurrentDefault());
            poseStack.translate(event.getWindow().getGuiScaledWidth() - (width + 6), event.getWindow().getGuiScaledHeight() - (2 * (font.lineHeight + 6)), 0);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderTexture(0, RenderHelper.TEXTURE_WHITE);
            RenderSystem.setShaderColor(0, 0, 0, (float) (double) mc.options.textBackgroundOpacity().get());
            GuiComponent.blit(poseStack, 0, 0, 0, 0, width + 4, font.lineHeight + 4, 256, 256);
            RenderSystem.disableBlend();
            font.drawShadow(poseStack, ClientMentions.getCurrentDefault(), 2, 2, 0xFFFFFF);
            poseStack.popPose();
        }
    }
}
