package org.saintqd.vineriumlib.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.saintqd.vineriumlib.VineriumLib;
import org.saintqd.vineriumlib.utils.VinUtils;

public class VinLibCommandsManager {

    public static void setupCommands(VineriumLib plugin) {
        LifecycleEventManager<Plugin> manager = plugin.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(
                    Commands.literal("vincamera")
                            .executes(commandContext -> {
                                commandContext.getSource().getSender().sendMessage(VineriumLib.inst().getLangManager().parseLangString(plugin,"notEnoughArguments"));
                                return Command.SINGLE_SUCCESS;
                            })
                            .then(Commands.literal("debug")
                                    .requires(predicate -> predicate.getSender().hasPermission("vineriumcamera.admin"))
                                    .executes(ctx -> {
                                        changeDebugLevelCommand(ctx.getSource().getSender(),0);
                                        return Command.SINGLE_SUCCESS;
                                    })
                                    .then(Commands.literal("level")
                                            .requires(predicate -> predicate.getSender().hasPermission("vineriumcamera.admin"))
                                            .executes(ctx -> {
                                                changeDebugLevelCommand(ctx.getSource().getSender(),ctx.getArgument("level", Integer.class));
                                                return Command.SINGLE_SUCCESS;
                                            })
                                    )
                            )
                            .build(),
                    "Основная команда."
            );

        });
    }

    private static void changeDebugLevelCommand(CommandSender sender, int level) {

        VineriumLib.inst().setDebugLevel(level);
        sender.sendMessage(VinUtils.parseString("<gray>Debug level set to <blue>"+level+"<gray>."));
    }

}
