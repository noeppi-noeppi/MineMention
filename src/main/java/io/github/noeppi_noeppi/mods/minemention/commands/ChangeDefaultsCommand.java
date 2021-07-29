package io.github.noeppi_noeppi.mods.minemention.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.noeppi_noeppi.libx.command.CommandUtil;
import io.github.noeppi_noeppi.mods.minemention.DefaultMentions;
import io.github.noeppi_noeppi.mods.minemention.MineMention;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TranslatableComponent;

public class ChangeDefaultsCommand implements Command<CommandSourceStack> {

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String str = CommandUtil.getArgumentOrDefault(context, "defaults", String.class, null);
        if (str == null) {
            DefaultMentions.updateMentionStrings(context.getSource().getPlayerOrException(), null);
        } else {
            ImmutableList.Builder<String> defaults = ImmutableList.builder();
            for (String mention : str.split(" ")) {
                if (!mention.isEmpty() && !mention.contains(" ")) {
                    defaults.add(mention);
                }
            }
            DefaultMentions.updateMentionStrings(context.getSource().getPlayerOrException(), defaults.build());
        }
        MineMention.getNetwork().updateSpecialMentions(context.getSource().getPlayerOrException());
        context.getSource().sendSuccess(new TranslatableComponent("minemention.defaults").withStyle(ChatFormatting.GREEN), false);
        return 0;
    }
}
