package io.github.noeppi_noeppi.mods.minemention;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.StringReader;
import io.github.noeppi_noeppi.libx.event.ConfigLoadedEvent;
import io.github.noeppi_noeppi.libx.render.RenderHelper;
import io.github.noeppi_noeppi.libx.util.TextComponentUtil;
import io.github.noeppi_noeppi.mods.minemention.api.SpecialMention;
import io.github.noeppi_noeppi.mods.minemention.api.SpecialMentions;
import io.github.noeppi_noeppi.mods.minemention.client.ClientMentions;
import io.github.noeppi_noeppi.mods.minemention.commands.MineMentionCommands;
import io.github.noeppi_noeppi.mods.minemention.mentions.OnePlayerMention;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
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
        if (event.getPlayer() instanceof ServerPlayerEntity) {
            MineMention.getNetwork().updateSpecialMentions((ServerPlayerEntity) event.getPlayer());
        }
    }
    
    @SubscribeEvent
    public void serverTick(TickEvent.WorldTickEvent event) {
        if (this.needsUpdate && event.phase == TickEvent.Phase.START && event.world instanceof ServerWorld) {
            this.needsUpdate = false;
            for (ServerPlayerEntity player : ((ServerWorld) event.world).getServer().getPlayerList().getPlayers()) {
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
        IFormattableTextComponent text = new StringTextComponent("");
        StringReader reader = new StringReader(event.getMessage());
        StringBuilder current = new StringBuilder();
        PlayerList playerList = event.getPlayer().getServerWorld().getServer().getPlayerList();
        while (reader.canRead()) {
            char chr = reader.read();
            if (chr == '@') {
                String mentionStr = reader.readUnquotedString();
                SpecialMention mention = SpecialMentions.getMention(mentionStr, event.getPlayer());
                if (mention == null) {
                    current.append("@").append(mentionStr);
                } else if (mention instanceof OnePlayerMention) {
                    if (playerList.getPlayerByUsername(((OnePlayerMention) mention).name) != null) {
                        mentions.add(mention);
                        text = text.appendSibling(new StringTextComponent(current.toString()));
                        current = new StringBuilder();
                        text = text.appendSibling(new StringTextComponent("@" + mentionStr)
                                .mergeStyle(MentionType.PLAYER.getStyle())
                                .mergeStyle(Style.EMPTY
                                        .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("minemention.reply")))
                                        .setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "@" + mentionStr + " "))
                                )
                        );
                    } else {
                        current.append("@").append(mentionStr);
                    }
                } else {
                    mentions.add(mention);
                    text = text.appendSibling(new StringTextComponent(current.toString()));
                    current = new StringBuilder();
                    text = text.appendSibling(new StringTextComponent("@" + mentionStr)
                            .mergeStyle(MentionType.GROUP.getStyle())
                            .mergeStyle(Style.EMPTY
                                    .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("minemention.reply")))
                                    .setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "@" + mentionStr + " "))
                            )
                    );
                }
            } else {
                current.append(chr);
            }
        }
        text = text.appendSibling(new StringTextComponent(current.toString()));

        IFormattableTextComponent tooltip = new TranslationTextComponent("minemention.reply");
        if (mentions.isEmpty()) {
            tooltip = tooltip.appendSibling(new StringTextComponent("\n")
                    .appendSibling(new TranslationTextComponent("minemention.sent"))
                    .appendSibling(DefaultMentions.getDefaultMentionString(event.getPlayer())));
        }

        IFormattableTextComponent send = new StringTextComponent("<").appendSibling(event.getPlayer().getDisplayName().deepCopy()
                .mergeStyle(Style.EMPTY
                        .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip))
                        .setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "@" + event.getPlayer().getGameProfile().getName() + " "))
                )
        ).appendSibling(new StringTextComponent("> ")).appendSibling(text);

        event.setComponent(text);
        event.setCanceled(true);
        mentions = DefaultMentions.getMentions(event.getPlayer(), mentions);
        //noinspection UnstableApiUsage
        List<Predicate<ServerPlayerEntity>> predicates = mentions.stream().map(m -> m.selectPlayers(event.getPlayer())).collect(ImmutableList.toImmutableList());
        // Always show message to sender.
        Predicate<ServerPlayerEntity> predicate = player -> player == event.getPlayer() || predicates.stream().anyMatch(p -> p.test(player));
        MineMention.logger.info(TextComponentUtil.getConsoleString(send));
        playerList.getPlayers().stream().filter(predicate).forEach(player -> player.sendMessage(send, event.getPlayer().getGameProfile().getId()));
    }
    
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void renderChat(RenderGameOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.getType() == RenderGameOverlayEvent.ElementType.CHAT && mc.currentScreen instanceof ChatScreen) {
            MatrixStack matrixStack = event.getMatrixStack();
            matrixStack.push();
            FontRenderer font = mc.fontRenderer;
            int width = font.getStringPropertyWidth(ClientMentions.getCurrentDefault());
            matrixStack.translate(event.getWindow().getScaledWidth() - (width + 6), event.getWindow().getScaledHeight() - (2 * (font.FONT_HEIGHT + 6)), 0);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            mc.getTextureManager().bindTexture(RenderHelper.TEXTURE_WHITE);
            //noinspection deprecation
            RenderSystem.color4f(0, 0, 0, (float) mc.gameSettings.accessibilityTextBackgroundOpacity);
            AbstractGui.blit(matrixStack, 0, 0, 0, 0, width + 4, font.FONT_HEIGHT + 4, 256, 256);
            RenderSystem.disableBlend();
            font.drawTextWithShadow(matrixStack, ClientMentions.getCurrentDefault(), 2, 2, 0xFFFFFF);
            matrixStack.pop();
        }
    }
}
