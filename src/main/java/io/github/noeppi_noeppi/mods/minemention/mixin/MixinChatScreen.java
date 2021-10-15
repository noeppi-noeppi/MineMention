package io.github.noeppi_noeppi.mods.minemention.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.noeppi_noeppi.mods.minemention.client.MentionSuggestionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class MixinChatScreen {

    private MentionSuggestionHelper mentions;

    @Inject(
            method = "Lnet/minecraft/client/gui/screens/ChatScreen;init()V",
            at = @At("RETURN")
    )
    public void constructor(CallbackInfo ci) {
        this.mentions = MentionSuggestionHelper.create(((ChatScreen) (Object) this).commandSuggestions, ((ChatScreen) (Object) this).minecraft, (ChatScreen) (Object) this, ((ChatScreen) (Object) this).font, 1, 10);
        this.mentions.updateCommandInfo();
    }

    @Inject(
            method = "Lnet/minecraft/client/gui/screens/ChatScreen;resize(Lnet/minecraft/client/Minecraft;II)V",
            at = @At("RETURN")
    )
    public void resize(Minecraft minecraft, int width, int height, CallbackInfo ci) {
        this.mentions.updateCommandInfo();
    }

    @Inject(
            method = "Lnet/minecraft/client/gui/screens/ChatScreen;onEdited(Ljava/lang/String;)V",
            at = @At("RETURN")
    )
    public void onEdited(String str, CallbackInfo ci) {
        this.mentions.setAllowSuggestions(true);
        this.mentions.updateCommandInfo();
    }

    @Inject(
            method = "Lnet/minecraft/client/gui/screens/ChatScreen;keyPressed(III)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (this.mentions.keyPressed(keyCode, scanCode, modifiers)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(
            method = "Lnet/minecraft/client/gui/screens/ChatScreen;mouseScrolled(DDD)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    public void mouseScrolled(double mouseX, double mouseY, double delta, CallbackInfoReturnable<Boolean> cir) {
      if (this.mentions.mouseScrolled(Mth.clamp(delta, -1, 1))) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(
            method = "Lnet/minecraft/client/gui/screens/ChatScreen;mouseClicked(DDI)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    public void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
      if (this.mentions.mouseClicked(mouseX, mouseY, button)) {
          cir.setReturnValue(true);
          cir.cancel();
      }
    }

    @Inject(
            method = "Lnet/minecraft/client/gui/screens/ChatScreen;moveInHistory(I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/CommandSuggestions;setAllowSuggestions(Z)V")
    )
    public void moveInHistory(int msgPos, CallbackInfo ci) {
        this.mentions.setAllowSuggestions(false);
    }

    @Inject(
            method = "Lnet/minecraft/client/gui/screens/ChatScreen;render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/CommandSuggestions;render(Lcom/mojang/blaze3d/vertex/PoseStack;II)V")
    )
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        this.mentions.render(poseStack, mouseX, mouseY);
    }
}
