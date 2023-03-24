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

    Commands(ArmorAbilities plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command only works in-game.");
            return false;
        }

        Player player = (Player) sender;
        Map<Ability, Integer> playerAbilities = plugin.getManager().getAbilities(player);

        if (args.length == 0) {
            if (cmd.getName().equalsIgnoreCase("ability")) {
                if (playerAbilities.size() > 1) {
                    player.sendMessage("You are using the following abilities : ");
                    for (Ability ability : playerAbilities.keySet()) {
                        player.sendMessage(ChatColor.GOLD + " " + ability);
                    }
                    player.sendMessage("");
                } else if (playerAbilities.size() == 1) {
                    player.sendMessage(
                            "You are using the " + ChatColor.GOLD + playerAbilities.keySet().iterator().next() +
                            ChatColor.WHITE + " ability.");
                } else {
                    sender.sendMessage("You currently have no ability.");
                }
            }
            return true;
        }

        if (args.length == 1) {
            if (cmd.getName().equalsIgnoreCase("ability") && args[0].equalsIgnoreCase("list")) {
                for (AbilityInfo abilityInfo : plugin.getData().getInfo()) {
                    player.sendMessage(ChatColor.GOLD + abilityInfo.getAbility().toString());
                }
            } else if (cmd.getName().equalsIgnoreCase("ability") && args[0].equalsIgnoreCase("debuff")) {
                for (PotionEffect effect : player.getActivePotionEffects()) {
                    player.removePotionEffect(effect.getType());
                }
                player.sendMessage("You have removed all potion effects.");
            } else if (cmd.getName().equalsIgnoreCase("ability") && args[0].equalsIgnoreCase("check")) {
                plugin.getManager().updateAbilityAmounts(player);
                player.sendMessage("You have updated all potion effects, ");
            } else if (cmd.getName().equalsIgnoreCase("ability") && args[0].equalsIgnoreCase("checkall")) {
                for (Player aPlayer : Bukkit.getOnlinePlayers()) {
                    plugin.getManager().updateAbilityAmounts(aPlayer);
                }
                player.sendMessage("You have updated everyone's potion effects, ");
            } else if (cmd.getName().equalsIgnoreCase("ability") && (args[0].length() > 1)) {
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
                AbilityInfo info = plugin.getData().getInfo(args[1]);
                if (info == null) {
                    player.sendMessage(ChatColor.RED + "There is no ability with that name.");
                } else {
                    info.messageInfo(player);
                }
            }
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Too many arguments.");
        return false;
    }
}
