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
    // Holds explosion locations for creeper ability
    private final ArrayList<Location> explosionLocations = new ArrayList<>(0);
    private final ArmorAbilities plugin;

    public CombatListeners(ArmorAbilities armorAbilities) {
        plugin = armorAbilities;
    }

    /**
     * Strikes lightning at the given entity's location and removes the block at that location.
     * Used for the rage ability retaliation.
     */
    private static void strikeLightning(Entity entity) {
        try {
            entity.getWorld().strikeLightningEffect(entity.getLocation());
            entity.getLocation().getBlock().setType(Material.AIR);
        } catch (NullPointerException e) {
            Bukkit.getLogger().warning("Could not strike lightning at entity location");
        }
    }

    /**
     * Handles player death for the creeper ability:
     * - If the player has the creeper ability and permission, creates an explosion at their location,
     *   clears their drops, and drops their items after a short delay.
     * - Always updates the ability tracking task.
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Map<Ability, Integer> abilities = plugin.getManager().getAbilities(player);

        if (player.hasPermission("armorabilities.creeper") && abilities.containsKey(Ability.CREEPER)) {
            explosionLocations.add(player.getLocation().getBlock().getLocation());
            player.getWorld().createExplosion(player.getLocation(), plugin.getData().getCreeperAbilityExplosion());
            ItemStack[] items = player.getInventory().getContents();
            event.getDrops().clear();
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new DropItems(items, player), 10L);
        }
        plugin.getTask().addPlayers();
    }

    /**
     * Handles mob targeting for the peace ability:
     * - If a player with the peace ability and permission is targeted by a mob,
     *   and the mob's last damage cause was not an entity attack or projectile,
     *   cancels the targeting event.
     */
    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        Entity mob = event.getEntity();
        Entity target = event.getTarget();
        if (!(target instanceof Player player)) return;

        Map<Ability, Integer> abilities = plugin.getManager().getAbilities(player);
        if (player.hasPermission("armorabilities.peace") && abilities.containsKey(Ability.PEACE)) {
            if ((mob.getLastDamageCause() == null) ||
                ((mob.getLastDamageCause().getCause() != DamageCause.ENTITY_ATTACK) &&
                 (mob.getLastDamageCause().getCause() != DamageCause.PROJECTILE))) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Handles damage prevention for Moon and Lava abilities:
     * - Cancels fall damage if the player has Moon ability (level 4) and permission.
     * - Cancels fire/lava damage if the player has Lava ability (level 4) and permission.
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Map<Ability, Integer> abilities = plugin.getManager().getAbilities(player);

        if ((event.getCause() == DamageCause.FALL) && abilities.containsKey(Ability.MOON) &&
            (abilities.get(Ability.MOON) == 4) && player.hasPermission("armorabilities.nofalldamage")) {
            event.setCancelled(true);
        }

        if (((event.getCause() == DamageCause.FIRE) || (event.getCause() == DamageCause.FIRE_TICK) ||
             (event.getCause() == DamageCause.LAVA)) && abilities.containsKey(Ability.LAVA) &&
            (abilities.get(Ability.LAVA) == 4) && player.hasPermission("armorabilities.nofiredamage")) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles combat-related ability effects:
     * - Rage: If a player with the rage ability is attacked, retaliate with lightning and fire against the attacker (arrow shooter or melee attacker).
     *   If both attacker and defender have rage, skip retaliation.
     * - Vampire: If a player with the vampire ability attacks, heal them for a percentage of the damage dealt.
     * - Assassin: If a player with the assassin ability attacks while sneaking, deal extra damage.
     */
    @EventHandler
    public void onEntityAttack(EntityDamageByEntityEvent event) {
        // Defender logic
        if (event.getEntity() instanceof Player defender) {
            Map<Ability, Integer> victimAbilities = plugin.getManager().getAbilities(defender);

            // If the attacker is also a player and both have the rage ability, do nothing
            if (event.getDamager() instanceof Player attacker) {
                Map<Ability, Integer> attackerAbilities = plugin.getManager().getAbilities(attacker);
                if (victimAbilities.containsKey(Ability.RAGE) && attackerAbilities.containsKey(Ability.RAGE)) {
                    return;
                }
            }

            // Rage retaliation
            if (defender.hasPermission("armorabilities.rage") && victimAbilities.containsKey(Ability.RAGE)) {
                Entity attacker = event.getDamager();
                if (!defender.hasPermission("armorabilities.rage")) return;

                if (attacker instanceof Arrow arrow && arrow.getShooter() instanceof LivingEntity shooter) {
                    strikeLightning(shooter);
                    shooter.damage(plugin.getData().getRageLightningDamage(), defender);
                    shooter.setFireTicks(plugin.getData().getRageFireTime() * 20);
                } else if (attacker instanceof LivingEntity livingAttacker) {
                    strikeLightning(livingAttacker);
                    livingAttacker.damage(plugin.getData().getRageLightningDamage(), defender);
                    livingAttacker.setFireTicks(plugin.getData().getRageFireTime() * 20);
                }
            }
        }

        // Attacker logic
        if (event.getDamager() instanceof Player attacker) {
            Map<Ability, Integer> abilities = plugin.getManager().getAbilities(attacker);

            // Vampire: Heal on hit
            if (attacker.hasPermission("armorabilities.vampire") && abilities.containsKey(Ability.VAMPIRE)) {
                int heal = (int) (event.getDamage() / (100.0 / plugin.getData().getVampirePercent()));
                attacker.setHealth(Math.min(attacker.getHealth() + heal, 20));
            }

            // Assassin: Extra damage while sneaking
            if (attacker.hasPermission("armorabilities.assassin") &&
                abilities.containsKey(Ability.ASSASSIN) && attacker.isSneaking()) {
                event.setDamage(event.getDamage() + plugin.getData().getAssassinDamage());
            }
        }
    }

    /**
     * Handles explosion block protection for creeper ability:
     * - If the explosion location matches a stored protected location and creeper block damage is disabled,
     *   clears the list of affected blocks and removes the location from the list.
     */
    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        Location loc = event.getLocation().getBlock().getLocation();
        if (explosionLocations.contains(loc) && !plugin.getData().isCreeperBlockDamage()) {
            event.blockList().clear();
            explosionLocations.remove(loc);
        }
    }

    /**
     * Drops all items in the player's inventory at their location after death (used for creeper ability).
     */
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
