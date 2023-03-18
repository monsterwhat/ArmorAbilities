package com.gigosaurus.armorabilities.listeners;

import com.gigosaurus.armorabilities.ArmorAbilities;
import com.gigosaurus.armorabilities.data.Ability;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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

    private final ArrayList<Location> explosionLocations = new ArrayList<>(0);
    private final ArmorAbilities plugin;

    public CombatListeners(ArmorAbilities armorAbilities) {
        plugin = armorAbilities;
    }

    private static void strikeLightning(Entity entity) {
        try{
            entity.getWorld().strikeLightningEffect(entity.getLocation());
            entity.getLocation().getBlock().setType(Material.AIR);
        } catch (NullPointerException e) {
            // Do nothing
            // Correction: why not?
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Map<Ability, Integer> abilities = plugin.getManager().getAbilities(player);

        if (player.hasPermission("armorabilities.creeper") && abilities.containsKey(Ability.CREEPER)) {

            //create an explosion and then drop their items so that they don't get destroyed by the explosion
            explosionLocations.add(player.getLocation().getBlock().getLocation());
            player.getWorld().createExplosion(player.getLocation(), plugin.getData().getCreeperAbilityExplosion());
            ItemStack[] items = player.getInventory().getContents();
            event.getDrops().clear();
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new DropItems(items, player), 10L);
        }
        plugin.getTask().addPlayers();
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        Entity mob = event.getEntity();
        Entity target = event.getTarget();
        if (!(target instanceof Player)) {
            return;
        }
        Player player = (Player) event.getTarget();
        Map<Ability, Integer> abilities = plugin.getManager().getAbilities(player);

        if (player.hasPermission("armorabilities.peace") && abilities.containsKey(Ability.PEACE)) {

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
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        Map<Ability, Integer> abilities = plugin.getManager().getAbilities(player);

        //stop fall damage
        if ((event.getCause() == DamageCause.FALL) && abilities.containsKey(Ability.MOON) &&
            (abilities.get(Ability.MOON) == 4) && player.hasPermission("armorabilities.nofalldamage")) {
            event.setCancelled(true);
        }

        //stop lava/fire damage
        if (((event.getCause() == DamageCause.FIRE) || (event.getCause() == DamageCause.FIRE_TICK) ||
             (event.getCause() == DamageCause.LAVA)) && abilities.containsKey(Ability.LAVA) &&
            (abilities.get(Ability.LAVA) == 4) && player.hasPermission("armorabilities.nofiredamage")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityAttack(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Map<Ability, Integer> victimAbilities = plugin.getManager().getAbilities(player);

            if (event.getDamager() instanceof Player) {
                Player attacker = (Player) event.getDamager();
                Map<Ability, Integer> attackerAbilities = plugin.getManager().getAbilities(attacker);
                if (victimAbilities.containsKey(Ability.RAGE) && attackerAbilities.containsKey(Ability.RAGE)) {
                    return;
                }
            }

            //strike attacker with lightning
            if (player.hasPermission("armorabilities.rage") && victimAbilities.containsKey(Ability.RAGE)) {
                Entity attacker = event.getDamager();
                Player defend = (Player) event.getEntity();
                if (!defend.hasPermission("armorabilities.rage")) {
                    return;
                }
                if (attacker instanceof Arrow) {
                    Arrow arrow = (Arrow) attacker;
                    if (arrow.getShooter() instanceof LivingEntity) {
                        LivingEntity shooter = (LivingEntity) arrow.getShooter();
                        strikeLightning(shooter);
                        shooter.damage(plugin.getData().getRageLightningDamage(), player);
                        shooter.setFireTicks(plugin.getData().getRageFireTime() * 20);
                    }
                } else {
                    if (attacker instanceof LivingEntity) {
                        LivingEntity attack = (LivingEntity) event.getDamager();
                        strikeLightning(attack);
                        attack.damage(plugin.getData().getRageLightningDamage(), player);
                        attack.setFireTicks(plugin.getData().getRageFireTime() * 20);
                    }
                }
            }
        }

        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            Map<Ability, Integer> abilities = plugin.getManager().getAbilities(player);

            //give attacker health proportional to the damage dealt
            if (player.hasPermission("armorabilities.vampire") && abilities.containsKey(Ability.VAMPIRE)) {
                long time = player.getWorld().getTime();
                if ((time > 12300L) || (time < 23850L)) {
                    int damage = (int) (event.getDamage() / (100.0 / plugin.getData().getVampirePercent()));
                    if ((player.getHealth() + damage) <= 20) {
                        player.setHealth(player.getHealth() + damage);
                    } else {
                        player.setHealth(20);
                    }
                }
            }

            //add extra damage for assassins
            if (player.hasPermission("armorabilities.assassin") && abilities.containsKey(Ability.ASSASSIN) &&
                player.isSneaking()) {
                int damage = (int) (event.getDamage() + plugin.getData().getAssassinDamage());
                event.setDamage(damage);
            }
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        if (explosionLocations.contains(event.getLocation().getBlock().getLocation()) &&
            !plugin.getData().isCreeperBlockDamage()) {
            event.blockList().clear();
            explosionLocations.remove(event.getLocation());
        }
    }

    private static final class DropItems implements Runnable {

        private final ItemStack[] allContents;
        private final Player player;

        private DropItems(ItemStack[] allContents, Player player) {
            this.allContents = allContents;
            this.player = player;
        }

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
