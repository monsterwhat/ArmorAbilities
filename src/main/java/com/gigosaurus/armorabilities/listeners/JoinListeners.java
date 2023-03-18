package com.gigosaurus.armorabilities.listeners;

import com.gigosaurus.armorabilities.ArmorAbilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinListeners implements Listener {

    private final ArmorAbilities plugin;

    public JoinListeners(ArmorAbilities armorAbilities) {
        plugin = armorAbilities;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // plugin.getTask().addPlayer(event.getPlayer());
        plugin.getTask().addPlayers();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getManager().removeAbilities(event.getPlayer());
    }
}
