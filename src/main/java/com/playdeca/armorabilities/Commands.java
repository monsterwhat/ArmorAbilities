package com.playdeca.armorabilities;

import com.playdeca.armorabilities.data.Ability;
import com.playdeca.armorabilities.data.AbilityInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.Map;

class Commands implements CommandExecutor {

    private final ArmorAbilities plugin;

    // Constructor
    Commands(ArmorAbilities plugin) {
        this.plugin = plugin;
    }
    // Override the onCommand method
    @Override
    public boolean onCommand(CommandSender sender,Command cmd,String commandLabel, String[] args) {
        // Check if the sender is a player
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command only works in-game.");
            return false;
        }
        // Get the player's abilities
        Map<Ability, Integer> playerAbilities = plugin.getManager().getAbilities(player);
        // If the command has no arguments
        if (args.length == 0) {
            // Check if the command is the ability command
            if (cmd.getName().equalsIgnoreCase("ability")) {
                if (playerAbilities.size() > 1) {
                    // If the player has more than one ability
                    player.sendMessage("You are using the following abilities : ");
                    for (Ability ability : playerAbilities.keySet()) {
                        player.sendMessage(ChatColor.GOLD + " " + ability);
                    }
                    player.sendMessage("");
                    // If the player has only one ability
                } else if (playerAbilities.size() == 1) {
                    player.sendMessage(
                            "You are using the " + ChatColor.GOLD + playerAbilities.keySet().iterator().next() +
                            ChatColor.WHITE + " ability.");
                } else {
                    // If the player has no abilities
                    sender.sendMessage("You currently have no ability.");
                }
            }
            return true;
        }
        // If the command has one argument
        if (args.length == 1) {
            // If the command is the ability command and the argument is "list"
            if (cmd.getName().equalsIgnoreCase("ability") && args[0].equalsIgnoreCase("list")) {
                for (AbilityInfo abilityInfo : plugin.getData().getInfo()) {
                    player.sendMessage(ChatColor.GOLD + abilityInfo.getAbility().toString());
                }
            // If the command is the ability command and the argument is "debuff"
            } else if (cmd.getName().equalsIgnoreCase("ability") && args[0].equalsIgnoreCase("debuff")) {
                // Remove all potion effects from the player
                for (PotionEffect effect : player.getActivePotionEffects()) {
                    player.removePotionEffect(effect.getType());
                }
                player.sendMessage("You have removed all potion effects.");
                // If the command is the ability command and the argument is "check"
            } else if (cmd.getName().equalsIgnoreCase("ability") && args[0].equalsIgnoreCase("check")) {
                // Update the player's ability amounts
                plugin.getManager().updateAbilityAmounts(player);
                player.sendMessage("You have updated all potion effects, ");
                // If the command is the ability command and the argument is "checkall"
            } else if (cmd.getName().equalsIgnoreCase("ability") && args[0].equalsIgnoreCase("checkall")) {
                // Update the ability amounts of all online players
                for (Player aPlayer : Bukkit.getOnlinePlayers()) {
                    plugin.getManager().updateAbilityAmounts(aPlayer);
                }
                player.sendMessage("You have updated everyone's potion effects, ");
            } else if (cmd.getName().equalsIgnoreCase("ability") && (args[0].length() > 1)) {
                // Check the abilities of another player
                Player other = Bukkit.getServer().getPlayer(args[0]);
                if (other == null) {
                    sender.sendMessage(ChatColor.RED + args[0] + " is not online!");
                } else {
                    if (playerAbilities.size() > 1) {
                        player.sendMessage(
                                ChatColor.ITALIC + args[0] + ChatColor.WHITE + " is using the following abilities : ");
                        for (Ability ability : playerAbilities.keySet()) {
                            player.sendMessage(ChatColor.GOLD + " " + ability);
                        }
                    } else if (playerAbilities.size() == 1) {
                        player.sendMessage(
                                ChatColor.ITALIC + args[0] + ChatColor.WHITE + " is using the " + ChatColor.GOLD +
                                playerAbilities.keySet().iterator().next() + ChatColor.WHITE + " ability.");
                    } else {
                        sender.sendMessage(ChatColor.ITALIC + args[0] + ChatColor.WHITE + " currently has no ability.");
                    }
                }
            }
            return true;
        }

        if (args.length == 2) {
            if (cmd.getName().equalsIgnoreCase("ability") && args[0].equalsIgnoreCase("info")) {
                // Show information about a specific ability
                AbilityInfo info = plugin.getData().getInfo(args[1]);
                if (info == null) {
                    player.sendMessage(ChatColor.RED + "There is no ability with that name.");
                } else {
                    info.messageInfo(player);
                }
            }
            return true;
        }
        // If the number of arguments is incorrect, send an error message
        sender.sendMessage(ChatColor.RED + "Too many arguments.");
        return false;
    }
}
