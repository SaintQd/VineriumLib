package org.saintqd.vineriumlib.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.saintqd.vineriumlib.VineriumLib;
import org.saintqd.vineriumlib.gui.data.CustomGUI;
import org.saintqd.vineriumlib.utils.VinUtils;

public class VinLibCommandsManager {

    public static void setupCommands(VineriumLib plugin) {
        LifecycleEventManager<Plugin> manager = plugin.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(
                    Commands.literal("vinlib")
                            .executes(commandContext -> {
                                commandContext.getSource().getSender().sendMessage(VineriumLib.inst().getLangManager().parseLangString(plugin,"not_enough_arguments"));
                                return Command.SINGLE_SUCCESS;
                            })
                            .then(Commands.literal("reload")
                                    .requires(predicate -> predicate.getSender().hasPermission("vineriumlib.admin"))
                                    .executes(ctx -> {
                                        reloadCommand(ctx.getSource().getSender());
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                            .then(Commands.literal("debug")
                                    .requires(predicate -> predicate.getSender().hasPermission("vineriumlib.admin"))
                                    .executes(ctx -> {
                                        changeDebugLevelCommand(ctx.getSource().getSender(),0);
                                        return Command.SINGLE_SUCCESS;
                                    })
                                    .then(Commands.argument("level", IntegerArgumentType.integer(0))
                                            .requires(predicate -> predicate.getSender().hasPermission("vineriumlib.admin"))
                                            .executes(ctx -> {
                                                changeDebugLevelCommand(ctx.getSource().getSender(),ctx.getArgument("level", Integer.class));
                                                return Command.SINGLE_SUCCESS;
                                            })
                                    )
                            )
                            .then(Commands.literal("opengui")
                                    .then(Commands.argument("name", ArgumentTypes.namespacedKey())
                                            .suggests((ctx,builder) -> {
                                                String partName = builder.getInput().replace("/vinlib opengui ","");
                                                VineriumLib.inst().getCustomGUIManager().getGuiPaths().forEach((key, guiName) -> {
                                                    String keyString = key.asString();
                                                    if (keyString.startsWith(partName.toLowerCase()))
                                                        builder.suggest(keyString);
                                                });
                                                return builder.buildFuture();
                                            })
                                            .executes(ctx -> {
                                                openCustomGUICommand(
                                                        ctx.getSource().getSender(),
                                                        ctx.getArgument("name", NamespacedKey.class),
                                                        null
                                                );
                                                return Command.SINGLE_SUCCESS;
                                            })
                                            .then(Commands.argument("player", ArgumentTypes.player())
                                                    .requires(predicate -> predicate.getSender().hasPermission("vineriumlib.admin"))
                                                    .executes(ctx -> {
                                                        openCustomGUICommand(
                                                                ctx.getSource().getSender(),
                                                                ctx.getLastChild().getArgument("name",NamespacedKey.class),
                                                                ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst()
                                                        );
                                                        return Command.SINGLE_SUCCESS;
                                                    })
                                            )
                                    )
                            )
                            .build(),
                    "Основная команда."

            );

        });
    }

    private static void reloadCommand(CommandSender sender) {
        VineriumLib.inst().loadData();
        if (sender instanceof Player)
            sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumLib.inst(),"reloadMessage"));
    }

    private static void changeDebugLevelCommand(CommandSender sender, int level) {
        VineriumLib.inst().setDebugLevel(level);
        sender.sendMessage(VinUtils.parseString("<gray>Debug level set to <blue>"+level+"<gray>."));
    }

    private static void openCustomGUICommand(CommandSender sender, NamespacedKey menuKey, Player player) {

        player = VinUtils.checkForPlayerPresent(sender, player);

        if (!VineriumLib.inst().getCustomGUIManager().getGuiPaths().containsKey(menuKey)) {
            sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumLib.inst(),"open_menu_command_does_not_exist",menuKey.asString()));
            return;
        }
        CustomGUI customGUI = new CustomGUI(player,menuKey);
        if (!customGUI.getConfig().getBoolean("PlayerOpen") && !sender.hasPermission("asurecore.admin")) {
            sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumLib.inst(),"no_permission"));
            return;
        }
        if (sender != player) {
            sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumLib.inst(),"open_menu_command_for_player",menuKey.asString(), player.getName()));
        }
        customGUI.setMainMenu();
        customGUI.openInventory(player);
    }

}
