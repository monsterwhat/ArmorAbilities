package com.gigosaurus.armorabilities.listeners;

import com.gigosaurus.armorabilities.ArmorAbilities;
import com.gigosaurus.armorabilities.data.Ability;
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
import java.util.HashMap;
import java.util.Map;

public class PlayerMoveListeners implements Listener {

    private final Map<String, ArrayList<Block>> vineMap = new HashMap<>(0);
    private final ArrayList<Material> noVine = new ArrayList<>(38);

    private final ArmorAbilities plugin;

    public PlayerMoveListeners(ArmorAbilities armorAbilities) {
        plugin = armorAbilities;
        //defineNoVineBlocks();
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
                    player.removePotionEffect(PotionEffectType.FAST_DIGGING);
                    player.removePotionEffect(PotionEffectType.NIGHT_VISION);

                    int fastDig = plugin.getData().getScubaHasteNum();
                    player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, fastDig));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1));

                } else {
                    player.removePotionEffect(PotionEffectType.FAST_DIGGING);
                    player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                }
            }
        }

        //add potion effects if the player is not underwater
        if (abilities.containsKey(Ability.MINER) && player.hasPermission("armorabilities.miner")) {

            if ((headLoc.getBlock().getType() == Material.WATER)) {
                player.removePotionEffect(PotionEffectType.FAST_DIGGING);
                player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            } else {
                player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 20000, plugin.getData().getMinerHasteNum()));
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

                    /*
                    int counter = 0;

                    for (Material id : noVine) {
                        if ((temp.getType() != Material.AIR) && (temp.getType() != id)) {
                            counter++;
                        }
                    }

                    if ((counter != noVine.size()) ||
                        ((opp.getType() != Material.AIR) && (opp.getType() != Material.FERN) &&
                         (opp.getType() != Material.DANDELION) && (opp.getType() != Material.POPPY))) {
                        break;
                    }
                     */

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
            if ((event.getTo().getBlock().getType() == Material.LAVA) ) {
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

    private ArrayList<Block> getVines(Player player) {
        if (vineMap.containsKey(player.getName())) {
            return vineMap.get(player.getName());
        }
        return new ArrayList<>(1);
    }

    private void setVines(Player player, ArrayList<Block> vines) {
        vineMap.put(player.getName(), vines);
    }

    private void addVines(Player player, Block vine) {
        ArrayList<Block> updated = getVines(player);
        updated.add(vine);
        setVines(player, updated);
    }

    private void defineNoVineBlocks() {

        //Could in theory be replaced with
        //checking if the block is not
        //a full, opaque cube?

        noVine.add(Material.GLASS_PANE);
        noVine.add(Material.BRICK_SLAB);
        noVine.add(Material.ACACIA_SLAB);
        noVine.add(Material.BIRCH_SLAB);
        noVine.add(Material.DARK_OAK_SLAB);
        noVine.add(Material.JUNGLE_SLAB);
        noVine.add(Material.OAK_SLAB);
        noVine.add(Material.SPRUCE_SLAB);
        noVine.add(Material.SANDSTONE_SLAB);
        noVine.add(Material.RED_SANDSTONE_SLAB);
        noVine.add(Material.ACACIA_STAIRS);
        noVine.add(Material.BIRCH_STAIRS);
        noVine.add(Material.DARK_OAK_STAIRS);
        noVine.add(Material.JUNGLE_STAIRS);
        noVine.add(Material.OAK_STAIRS);
        noVine.add(Material.SPRUCE_STAIRS);
        noVine.add(Material.COBBLESTONE_STAIRS);
        noVine.add(Material.BRICK_STAIRS);
        noVine.add(Material.STONE_BRICK_STAIRS);
        noVine.add(Material.NETHER_BRICK_STAIRS);
        noVine.add(Material.SANDSTONE_STAIRS);
        noVine.add(Material.RED_SANDSTONE_STAIRS);
        noVine.add(Material.ACACIA_FENCE);
        noVine.add(Material.BIRCH_FENCE);
        noVine.add(Material.DARK_OAK_FENCE);
        noVine.add(Material.JUNGLE_FENCE);
        noVine.add(Material.OAK_FENCE);
        noVine.add(Material.SPRUCE_FENCE);
        noVine.add(Material.ACACIA_FENCE_GATE);
        noVine.add(Material.BIRCH_FENCE_GATE);
        noVine.add(Material.DARK_OAK_FENCE_GATE);
        noVine.add(Material.JUNGLE_FENCE_GATE);
        noVine.add(Material.OAK_FENCE_GATE);
        noVine.add(Material.SPRUCE_FENCE_GATE);
        noVine.add(Material.NETHER_BRICK_FENCE);
        noVine.add(Material.LADDER);
        noVine.add(Material.VINE);
        noVine.add(Material.RED_BED);
        noVine.add(Material.BLACK_BED);
        noVine.add(Material.IRON_BARS);
        noVine.add(Material.SNOW);
        noVine.add(Material.SPRUCE_SIGN);
        noVine.add(Material.LEVER);
        noVine.add(Material.OAK_TRAPDOOR);
        noVine.add(Material.PISTON_HEAD);
        noVine.add(Material.MOVING_PISTON);
        noVine.add(Material.TRIPWIRE_HOOK);
        noVine.add(Material.REPEATER);
        noVine.add(Material.OAK_BOAT);
        noVine.add(Material.MINECART);
        noVine.add(Material.CAKE);
        noVine.add(Material.WATER);
        noVine.add(Material.LAVA);
    }
}
