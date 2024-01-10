package com.playdeca.armorabilities;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashSet;
import java.util.Set;

public class AbilityCheckerTask extends BukkitRunnable {

    private final Set<Player> players = new HashSet<>(5);

    private final ArmorAbilities plugin;

    AbilityCheckerTask(ArmorAbilities plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : players) {
            if (player.isOnline()) {
                plugin.getManager().updateAbilityAmounts(player);
            }
        }
        players.clear();
    }

    public void addPlayers() {
        players.addAll(Bukkit.getOnlinePlayers());
    }
}
