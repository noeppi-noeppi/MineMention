package io.github.noeppi_noeppi.mods.minemention;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.StringReader;
import io.github.noeppi_noeppi.libx.event.ConfigLoadedEvent;
import io.github.noeppi_noeppi.libx.render.RenderHelper;
import io.github.noeppi_noeppi.libx.util.ComponentUtil;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class EventListener {
    
    private boolean needsUpdate;
    
    @SubscribeEvent
    public void playerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayer) {
            MineMention.getNetwork().updateSpecialMentions((ServerPlayer) event.getPlayer());
        }
    }
    
    @SubscribeEvent
    public void serverTick(TickEvent.WorldTickEvent event) {
        if (this.needsUpdate && event.phase == TickEvent.Phase.START && event.world instanceof ServerLevel) {
            this.needsUpdate = false;
            for (ServerPlayer player : ((ServerLevel) event.world).getServer().getPlayerList().getPlayers()) {
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
        if (event.getMessage().trim().startsWith("\\")) return; 
        List<SpecialMention> mentions = new ArrayList<>();
        MutableComponent text = new TextComponent("");
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
                    if (playerList.getPlayerByName(((OnePlayerMention) mention).name) != null) {
                        mentions.add(mention);
                        text = text.append(ForgeHooks.newChatWithLinks(current.toString()));
                        current = new StringBuilder();
                        text = text.append(new TextComponent("@" + mentionStr)
                                .withStyle(MentionType.PLAYER.getStyle())
                                .withStyle(Style.EMPTY
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("minemention.reply")))
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
                    text = text.append(new TextComponent("@" + mentionStr)
                            .withStyle(MentionType.GROUP.getStyle())
                            .withStyle(Style.EMPTY
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("minemention.reply")))
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "@" + mentionStr + " "))
                            )
                    );
                }
            } else {
                current.append(chr);
            }
        }
        text = text.append(ForgeHooks.newChatWithLinks(current.toString()));

        MutableComponent tooltip = new TranslatableComponent("minemention.reply");
        if (mentions.isEmpty()) {
            tooltip = tooltip.append(new TextComponent("\n")
                    .append(new TranslatableComponent("minemention.sent"))
                    .append(DefaultMentions.getDefaultMentionString(event.getPlayer())));
        }

        MutableComponent send = new TextComponent("<").append(event.getPlayer().getDisplayName().copy()
                .withStyle(Style.EMPTY
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "@" + event.getPlayer().getGameProfile().getName() + " "))
                )
        ).append(new TextComponent("> ")).append(text);

        event.setComponent(text);
        event.setCanceled(true);
        mentions = DefaultMentions.getMentions(event.getPlayer(), mentions);
        List<Predicate<ServerPlayer>> predicates = mentions.stream().map(m -> m.selectPlayers(event.getPlayer())).collect(ImmutableList.toImmutableList());
        // Always show message to sender.
        Predicate<ServerPlayer> predicate = player -> player == event.getPlayer() || predicates.stream().anyMatch(p -> p.test(player));
        MineMention.logger.info(ComponentUtil.getConsoleString(send));
        playerList.getPlayers().stream().filter(predicate).forEach(player -> player.sendMessage(send, event.getPlayer().getGameProfile().getId()));
    }
    
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void renderChat(RenderGameOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.getType() == RenderGameOverlayEvent.ElementType.CHAT && mc.screen instanceof ChatScreen) {
            PoseStack poseStack = event.getMatrixStack();
            poseStack.pushPose();
            Font font = mc.font;
            int width = font.width(ClientMentions.getCurrentDefault());
            poseStack.translate(event.getWindow().getGuiScaledWidth() - (width + 6), event.getWindow().getGuiScaledHeight() - (2 * (font.lineHeight + 6)), 0);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderTexture(0, RenderHelper.TEXTURE_WHITE);
            RenderSystem.setShaderColor(0, 0, 0, (float) mc.options.textBackgroundOpacity);
            GuiComponent.blit(poseStack, 0, 0, 0, 0, width + 4, font.lineHeight + 4, 256, 256);
            RenderSystem.disableBlend();
            font.drawShadow(poseStack, ClientMentions.getCurrentDefault(), 2, 2, 0xFFFFFF);
            poseStack.popPose();
        }
    }
}
