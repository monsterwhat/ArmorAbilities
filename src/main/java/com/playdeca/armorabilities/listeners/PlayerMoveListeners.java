package com.playdeca.armorabilities.listeners;

import com.playdeca.armorabilities.ArmorAbilities;
import com.playdeca.armorabilities.data.Ability;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerMoveListeners implements Listener {

    private final Map<String, List<Block>> vineMap = new ConcurrentHashMap<>();
    private final ArmorAbilities plugin;

    public PlayerMoveListeners(ArmorAbilities armorAbilities) {
        this.plugin = armorAbilities;
    }

    private static BlockFace yawToFace(float yaw) {
        BlockFace[] axis = {BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST};
        return axis[Math.round(yaw / 90.0F) & 0x3];
    }

    @EventHandler
    public void sneakToggle(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        Map<Ability, Integer> abilities = plugin.getManager().getAbilities(player);

        //make assassins invisible
        if (player.hasPermission("armorabilities.assassin") && abilities.containsKey(Ability.ASSASSIN)) {
            if (event.isSneaking()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
                for (Player players : Bukkit.getOnlinePlayers()) {
                    players.hidePlayer(ArmorAbilities.getInstance(), player);
                }
            } else {
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
                for (Player players : Bukkit.getOnlinePlayers()) {
                    players.showPlayer(ArmorAbilities.getInstance(), player);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location headLoc = player.getEyeLocation();
        Map<Ability, Integer> abilities = plugin.getManager().getAbilities(player);

        //add potion effects if the player is underwater
        if (abilities.containsKey(Ability.SCUBA)) {
            if (player.hasPermission("armorabilities.scuba")) {
                if ((headLoc.getBlock().getType() == Material.WATER) ) {

                    if (!player.hasPotionEffect(PotionEffectType.WATER_BREATHING) && !plugin.getManager().isScuba(player)) {

                        plugin.getManager().addScuba(player);

                        // Total time for the scuba effect to last.
                        int scubaTime =  plugin.getData().getScubaTime() * 200;
                        player.addPotionEffect(
                                new PotionEffect(PotionEffectType.WATER_BREATHING, scubaTime, 1));
                    }
                } else {
                    player.removePotionEffect(PotionEffectType.WATER_BREATHING);
                    plugin.getManager().removeScuba(player);
                }
            }

            if ((abilities.get(Ability.SCUBA) == 4) ) {
                if ((headLoc.getBlock().getType() == Material.WATER || headLoc.getBlock().getType() == Material.WATER)) {
                    player.removePotionEffect(PotionEffectType.HASTE);
                    player.removePotionEffect(PotionEffectType.NIGHT_VISION);

                    int fastDig = plugin.getData().getScubaHasteNum();
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, Integer.MAX_VALUE, fastDig));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1));

                } else {
                    player.removePotionEffect(PotionEffectType.HASTE);
                    player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                }
            }
        }

        //add potion effects if the player is not underwater
        if (abilities.containsKey(Ability.MINER) && player.hasPermission("armorabilities.miner")) {

            if ((headLoc.getBlock().getType() == Material.WATER)) {
                player.removePotionEffect(PotionEffectType.HASTE);
                player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            } else {
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 20000, plugin.getData().getMinerHasteNum()));
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1));
            }
        }

        //allow the player to move up walls
        if (abilities.containsKey(Ability.SPIDER) && player.hasPermission("armorabilities.spider")) {

            BlockFace blockFace = yawToFace(player.getLocation().getYaw());
            Block block = player.getLocation().getBlock().getRelative(blockFace);

            if (block.getType() == Material.AIR) {

                for (int i = 0; i < getVines(player).size(); i++) {
                    player.sendBlockChange(getVines(player).get(i).getLocation(), Material.AIR.createBlockData());
                }
                getVines(player).clear();

            } else {
                for (int i = 0; i < 300; i++) {

                    Block temp = block.getLocation().add(0.0D, i, 0.0D).getBlock();
                    Block opp = player.getLocation().add(0.0D, i, 0.0D).getBlock();
                    Block aboveOpp = opp.getLocation().add(0.0D, 1.0D, 0.0D).getBlock();

                    if (!temp.getType().isSolid())
                        break;

                    if (aboveOpp.getType() == Material.AIR) {
                        player.sendBlockChange(opp.getLocation(), Material.VINE.createBlockData());
                        addVines(player, opp);
                    }
                    player.setFallDistance(0.0F);
                }
            }
        }

        //add potion effects if the player is in lava
        if (abilities.containsKey(Ability.LAVA) && player.hasPermission("armorabilities.lavaswim")) {
            int lavaAmt = abilities.get(Ability.LAVA);
            if ((Objects.requireNonNull(event.getTo()).getBlock().getType() == Material.LAVA) ) {
                if (!player.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE) && !plugin.getManager().isLava(player)) {

                    plugin.getManager().addLava(player);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,
                            plugin.getData().getLavaTime() * lavaAmt * lavaAmt * 20,
                            1));

                }
            } else if ( (event.getFrom().getBlock().getType() != Material.LAVA) ) {

                if (lavaAmt == 4) {
                    player.setFireTicks(-20);
                }

                if (player.getFireTicks() <= 0) {
                    player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
                    plugin.getManager().removeLava(player);
                }
            }
        }
    }

    private List<Block> getVines(Player player) {
        // Always returns a mutable list for the player
        return vineMap.computeIfAbsent(player.getName(), k -> new ArrayList<>());
    }
 
    private void addVines(Player player, Block vine) {
        List<Block> updated = getVines(player);
        updated.add(vine);
        // No need to call setVines, as getVines already ensures the list is in the map
    }

}
