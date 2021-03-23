package io.github.noeppi_noeppi.mods.minemention.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.noeppi_noeppi.libx.command.CommandUtil;
import io.github.noeppi_noeppi.mods.minemention.DefaultMentions;
import io.github.noeppi_noeppi.mods.minemention.MineMention;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class ChangeDefaultsCommand implements Command<CommandSource> {

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        String str = CommandUtil.getArgumentOrDefault(context, "defaults", String.class, null);
        if (str == null) {
            DefaultMentions.updateMentionStrings(context.getSource().asPlayer(), null);
        } else {
            ImmutableList.Builder<String> defaults = ImmutableList.builder();
            for (String mention : str.split(" ")) {
                if (!mention.isEmpty() && !mention.contains(" ")) {
                    defaults.add(mention);
                }
            }
            DefaultMentions.updateMentionStrings(context.getSource().asPlayer(), defaults.build());
        }
        MineMention.getNetwork().updateSpecialMentions(context.getSource().asPlayer());
        context.getSource().sendFeedback(new TranslationTextComponent("minemention.defaults").mergeStyle(TextFormatting.GREEN), false);
        return 0;
    }
}
