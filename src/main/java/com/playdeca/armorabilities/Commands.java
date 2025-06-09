package com.playdeca.armorabilities;

import com.playdeca.armorabilities.data.Ability;
import com.playdeca.armorabilities.data.AbilityInfo;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import java.util.Map;

class Commands implements CommandExecutor {

    private final ArmorAbilities plugin;

    Commands(ArmorAbilities plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String commandLabel, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command only works in-game.");
            return false;
        }
 
        // Accept /ability, /armorabilities, /aa
        String cmdName = cmd.getName().toLowerCase();
        boolean isAbilityCmd = cmdName.equals("ability") || cmdName.equals("armorabilities") || cmdName.equals("aa");

        // /ability
        if (args.length == 0) {
            if (isAbilityCmd) {
                player.sendMessage(Component.text("ArmorAbilities", NamedTextColor.GOLD, TextDecoration.BOLD));
                player.sendMessage(Component.text("A Minecraft plugin that grants unique abilities to trimmed armor.", NamedTextColor.AQUA));
                player.sendMessage(Component.text("Developed by: gigosaurus, Random6894, MonsterWhat", NamedTextColor.GRAY, TextDecoration.ITALIC));
                String version = "";
                if (Bukkit.getPluginManager().getPlugin("ArmorAbilities") instanceof org.bukkit.plugin.java.JavaPlugin javaPlugin) {
                    version = javaPlugin.getPluginMeta().getVersion();
                }
                player.sendMessage(Component.text("Current version: " + version, NamedTextColor.DARK_GRAY));
                player.sendMessage(
                    Component.text("Use /" + cmdName + " help for a list of commands.", NamedTextColor.YELLOW)
                        .clickEvent(ClickEvent.runCommand("/" + cmdName + " help"))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to run /" + cmdName + " help")))
                );
                return true;
            }
        }

        // /ability <subcommand>
        if (args.length == 1) {
            String sub = args[0].toLowerCase();
            if (isAbilityCmd) {
                switch (sub) {
                    case "list" -> {
                        player.sendMessage(Component.text("Available Abilities:", NamedTextColor.GOLD));
                        for (AbilityInfo abilityInfo : plugin.getData().getInfo()) {
                            player.sendMessage(
                                Component.text(" • ", NamedTextColor.DARK_GRAY)
                                    .append(Component.text(abilityInfo.getAbility().toString(), NamedTextColor.YELLOW)
                                        .clickEvent(ClickEvent.runCommand("/" + cmdName + " info " + abilityInfo.getAbility().toString().toLowerCase()))
                                        .hoverEvent(HoverEvent.showText(Component.text("Click for info about " + abilityInfo.getAbility()))))
                            );
                        }
                        return true;
                    }
                    case "help" -> {
                        player.sendMessage(Component.text("ArmorAbilities Help", NamedTextColor.GOLD, TextDecoration.BOLD));
                        player.sendMessage(
                            Component.text("/" + cmdName + " list", NamedTextColor.AQUA)
                                .clickEvent(ClickEvent.runCommand("/" + cmdName + " list"))
                                .hoverEvent(HoverEvent.showText(Component.text("Show all available abilities")))
                        );
                        player.sendMessage(
                            Component.text("/" + cmdName + " info <name>", NamedTextColor.AQUA)
                                .clickEvent(ClickEvent.suggestCommand("/" + cmdName + " info "))
                                .hoverEvent(HoverEvent.showText(Component.text("Show info about an ability")))
                        );
                        player.sendMessage(
                            Component.text("/" + cmdName + " check", NamedTextColor.AQUA)
                                .clickEvent(ClickEvent.runCommand("/" + cmdName + " check"))
                                .hoverEvent(HoverEvent.showText(Component.text("Update your potion effects")))
                        );
                        player.sendMessage(
                            Component.text("/" + cmdName + " debuff", NamedTextColor.AQUA)
                                .clickEvent(ClickEvent.runCommand("/" + cmdName + " debuff"))
                                .hoverEvent(HoverEvent.showText(Component.text("Remove all potion effects")))
                        );
                        player.sendMessage(
                            Component.text("/" + cmdName + " checkall", NamedTextColor.AQUA)
                                .clickEvent(ClickEvent.runCommand("/" + cmdName + " checkall"))
                                .hoverEvent(HoverEvent.showText(Component.text("Update everyone's potion effects")))
                        );
                        player.sendMessage(
                            Component.text("/" + cmdName + " reload", NamedTextColor.AQUA)
                                .clickEvent(ClickEvent.runCommand("/" + cmdName + " reload"))
                                .hoverEvent(HoverEvent.showText(Component.text("Reload the plugin (admin only)")))
                        );
                        return true;
                    }
                    case "debuff" -> {
                        for (PotionEffect effect : player.getActivePotionEffects()) {
                            player.removePotionEffect(effect.getType());
                        }
                        player.sendMessage(Component.text("You have removed all potion effects.", NamedTextColor.GREEN));
                        return true;
                    }
                    case "check" -> {
                        plugin.getManager().updateAbilityAmounts(player);
                        player.sendMessage(Component.text("You have updated all potion effects.", NamedTextColor.GREEN));
                        return true;
                    }
                    case "checkall" -> {
                        for (Player aPlayer : Bukkit.getOnlinePlayers()) {
                            plugin.getManager().updateAbilityAmounts(aPlayer);
                        }
                        player.sendMessage(Component.text("You have updated everyone's potion effects.", NamedTextColor.GREEN));
                        return true;
                    }
                    case "reload" -> {
                        if (player.hasPermission("armorabilities.reload")) {
                            plugin.reloadConfig();
                            player.sendMessage(Component.text("ArmorAbilities config reloaded.", NamedTextColor.GREEN));
                        } else {
                            player.sendMessage(Component.text("You do not have permission to reload.", NamedTextColor.RED));
                        }
                        return true;
                    }
                    case "current" -> {
                        Map<Ability, Integer> playerAbilities = plugin.getManager().getAbilities(player);
                        if (!playerAbilities.isEmpty()) {
                            player.sendMessage(Component.text("You are using the following abilities:", NamedTextColor.GOLD));
                            for (Ability ability : playerAbilities.keySet()) {
                                player.sendMessage(
                                    Component.text(" • ", NamedTextColor.DARK_GRAY)
                                        .append(Component.text(ability.toString(), NamedTextColor.GOLD)
                                            .clickEvent(ClickEvent.runCommand("/" + cmdName + " info " + ability.toString().toLowerCase()))
                                            .hoverEvent(HoverEvent.showText(Component.text("Click for info about " + ability))))
                                );
                            }
                        } else {
                            player.sendMessage(Component.text("You currently have no ability.", NamedTextColor.GRAY));
                        }
                        return true;
                    }
                    default -> {
                        // Try to check another player's abilities
                        Player other = Bukkit.getServer().getPlayer(args[0]);
                        if (other == null) {
                            player.sendMessage(Component.text(args[0] + " is not online!", NamedTextColor.RED));
                        } else {
                            Map<Ability, Integer> otherAbilities = plugin.getManager().getAbilities(other);
                            if (!otherAbilities.isEmpty()) {
                                player.sendMessage(
                                    Component.text()
                                        .append(Component.text(other.getName(), NamedTextColor.YELLOW, TextDecoration.ITALIC))
                                        .append(Component.text(" is using the following abilities:"))
                                );
                                for (Ability ability : otherAbilities.keySet()) {
                                    player.sendMessage(
                                        Component.text(" • ", NamedTextColor.DARK_GRAY)
                                            .append(Component.text(ability.toString(), NamedTextColor.GOLD)
                                                .clickEvent(ClickEvent.runCommand("/" + cmdName + " info " + ability.toString().toLowerCase()))
                                                .hoverEvent(HoverEvent.showText(Component.text("Click for info about " + ability))))
                                    );
                                }
                            } else {
                                player.sendMessage(
                                    Component.text()
                                        .append(Component.text(other.getName(), NamedTextColor.YELLOW, TextDecoration.ITALIC))
                                        .append(Component.text(" currently has no ability."))
                                );
                            }
                        }
                        return true;
                    }
                }
            }
        }

        // /ability info <name>
        if (args.length == 2 && isAbilityCmd && args[0].equalsIgnoreCase("info")) {
            AbilityInfo info = plugin.getData().getInfo(args[1]);
            if (info == null) {
                player.sendMessage(Component.text("There is no ability with that name.", NamedTextColor.RED));
            } else {
                info.messageInfo(player);
            }
            return true;
        }

        // Too many arguments or unknown usage
        player.sendMessage(Component.text("Unknown or too many arguments. Use /" + cmdName + " help.", NamedTextColor.RED));
        return false;
    }
}
