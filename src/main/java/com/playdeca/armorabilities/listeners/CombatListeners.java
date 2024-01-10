package com.playdeca.armorabilities.listeners;

import com.playdeca.armorabilities.ArmorAbilities;
import com.playdeca.armorabilities.data.Ability;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Map;

public class CombatListeners implements Listener {
    // Create an array list to hold explosion locations and get the ArmorAbilities plugin instance
    private final ArrayList<Location> explosionLocations = new ArrayList<>(0);
    private final ArmorAbilities plugin;

    public CombatListeners(ArmorAbilities armorAbilities) {
        plugin = armorAbilities;
    }
    // Helper method to strike lightning
    private static void strikeLightning(Entity entity) {
        try{
            // Strike lightning at the given entity's location and remove the block at that location
            entity.getWorld().strikeLightningEffect(entity.getLocation());
            entity.getLocation().getBlock().setType(Material.AIR);
        } catch (NullPointerException e) {
            Bukkit.getLogger().warning("Could not strike lightning at entity location");
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Map<Ability, Integer> abilities = plugin.getManager().getAbilities(player);

        // If the player has the creeper ability and the necessary permission, create an explosion and drop their items
        if (player.hasPermission("armorabilities.creeper") && abilities.containsKey(Ability.CREEPER)) {
            // Add the player's location to the explosion locations array
            explosionLocations.add(player.getLocation().getBlock().getLocation());
            // Create an explosion at the player's location with the creeper ability explosion strength
            player.getWorld().createExplosion(player.getLocation(), plugin.getData().getCreeperAbilityExplosion());
            // Get the player's inventory contents and clear their drops
            ItemStack[] items = player.getInventory().getContents();
            event.getDrops().clear();
            // Delay the drop items task for 10 ticks and pass in the player and their inventory contents
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new DropItems(items, player), 10L);
        }
        // Add players to the task scheduler
        plugin.getTask().addPlayers();
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        Entity mob = event.getEntity();
        Entity target = event.getTarget();
        // If the target is not a player, return
        if (!(target instanceof Player)) {
            return;
        }
        Player player = (Player) event.getTarget();
        Map<Ability, Integer> abilities = plugin.getManager().getAbilities(player);
        // If the player has the peace ability and the necessary permission, stop them from being targeted
        if (player.hasPermission("armorabilities.peace") && abilities.containsKey(Ability.PEACE)) {
            // If the mob's last damage cause was not from an entity attack or projectile, cancel the event
            //stop this player getting targeted
            if ((mob.getLastDamageCause() == null) ||
                ((mob.getLastDamageCause().getCause() != DamageCause.ENTITY_ATTACK) &&
                 (mob.getLastDamageCause().getCause() != DamageCause.PROJECTILE))) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        // If the entity that got damaged is not a player, return
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        // Get the player's abilities
        Map<Ability, Integer> abilities = plugin.getManager().getAbilities(player);

        // Stop fall damage if the player has the Moon ability and permission
        if ((event.getCause() == DamageCause.FALL) && abilities.containsKey(Ability.MOON) &&
            (abilities.get(Ability.MOON) == 4) && player.hasPermission("armorabilities.nofalldamage")) {
            event.setCancelled(true);
        }

        // Stop lava and fire damage if the player has the Lava ability and permission
        if (((event.getCause() == DamageCause.FIRE) || (event.getCause() == DamageCause.FIRE_TICK) ||
             (event.getCause() == DamageCause.LAVA)) && abilities.containsKey(Ability.LAVA) &&
            (abilities.get(Ability.LAVA) == 4) && player.hasPermission("armorabilities.nofiredamage")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityAttack(EntityDamageByEntityEvent event) {
        // If the entity being attacked is a player
        if (event.getEntity() instanceof Player player) {
            // Get the victim's abilities
            Map<Ability, Integer> victimAbilities = plugin.getManager().getAbilities(player);

            // If the attacker is also a player and both have the rage ability, do nothing
            if (event.getDamager() instanceof Player attacker) {
                Map<Ability, Integer> attackerAbilities = plugin.getManager().getAbilities(attacker);
                if (victimAbilities.containsKey(Ability.RAGE) && attackerAbilities.containsKey(Ability.RAGE)) {
                    return;
                }
            }

            // Strike attacker with lightning for the rage ability
            if (player.hasPermission("armorabilities.rage") && victimAbilities.containsKey(Ability.RAGE)) {
                Entity attacker = event.getDamager();
                Player defend = (Player) event.getEntity();
                // If the defender doesn't have the rage ability, do nothing
                if (!defend.hasPermission("armorabilities.rage")) {
                    return;
                }
                // Strike the shooter with lightning if the damager is an arrow
                if (attacker instanceof Arrow arrow) {
                    if (arrow.getShooter() instanceof LivingEntity shooter) {
                        // Strike the attacker with lightning and set them on fire
                        strikeLightning(shooter);
                        shooter.damage(plugin.getData().getRageLightningDamage(), player);
                        shooter.setFireTicks(plugin.getData().getRageFireTime() * 20);
                    }
                } else {
                    // Strike the attacker with lightning if the damager is a living entity
                    if (attacker instanceof LivingEntity) {
                        LivingEntity attack = (LivingEntity) event.getDamager();
                        // Strike the attacker with lightning and set them on fire
                        strikeLightning(attack);
                        attack.damage(plugin.getData().getRageLightningDamage(), player);
                        attack.setFireTicks(plugin.getData().getRageFireTime() * 20);
                    }
                }
            }
        }
        // If the damager is a player
        if (event.getDamager() instanceof Player player) {
            // Get the player's abilities
            Map<Ability, Integer> abilities = plugin.getManager().getAbilities(player);

            // Give the attacker health proportional to the damage dealt for the vampire ability
            if (player.hasPermission("armorabilities.vampire") && abilities.containsKey(Ability.VAMPIRE)) {
                int damage = (int) (event.getDamage() / (100.0 / plugin.getData().getVampirePercent()));
                // Increase the player's health by the calculated amount, up to a maximum of 20
                if ((player.getHealth() + damage) <= 20) {
                    player.setHealth(player.getHealth() + damage);
                } else {
                    player.setHealth(20);
                }
            }

            // Add extra damage for assassins if the player is sneaking
            if (player.hasPermission("armorabilities.assassin") && abilities.containsKey(Ability.ASSASSIN) &&
                player.isSneaking()) {
                int damage = (int) (event.getDamage() + plugin.getData().getAssassinDamage());
                event.setDamage(damage);
            }
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        // Check if the location of the explosion matches any stored in the list of protected locations
        if (explosionLocations.contains(event.getLocation().getBlock().getLocation()) &&
            !plugin.getData().isCreeperBlockDamage()) {
            // If the location is protected and creeper block damage is disabled, clear the list of affected blocks
            event.blockList().clear();
            // Remove the protected location from the list
            explosionLocations.remove(event.getLocation());
        }
    }

    private record DropItems(ItemStack[] allContents, Player player) implements Runnable {

        @Override
            public void run() {
                for (ItemStack itemStack : allContents) {
                    if ((itemStack != null) && (itemStack.getType() != Material.AIR)) {
                        player.getWorld().dropItem(player.getLocation(), itemStack);
                    }
                }
            }
        }
}
