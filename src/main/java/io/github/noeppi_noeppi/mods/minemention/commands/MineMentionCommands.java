package io.github.noeppi_noeppi.mods.minemention.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

public class MineMentionCommands {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(literal("minemention")
                .then(literal("default")
                        .executes(new ChangeDefaultsCommand())
                        .then(argument("defaults", greedyString())
                                .suggests(DefaultMentionSuggestions::suggest)
                                .executes(new ChangeDefaultsCommand())
                ))
        );
        dispatcher.register(literal("d")
                .executes(new ChangeDefaultsCommand())
                .then(argument("defaults", greedyString())
                        .suggests(DefaultMentionSuggestions::suggest)
                        .executes(new ChangeDefaultsCommand())
                )
        );
    }
}
