package com.playdeca.armorabilities.data;

import com.playdeca.armorabilities.ArmorAbilities;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import com.playdeca.armorabilities.utils.ArmorUtils;

import org.bukkit.Bukkit;
import java.util.*;

public class AbilityManager {

    private final Map<String, Map<Ability, Integer>> abilities = new HashMap<>(16);
    private final Set<String> scubaActive = new HashSet<>(5);
    private final Set<String> lavaActive = new HashSet<>(5);
    private final ArmorAbilities plugin;

    public AbilityManager(ArmorAbilities plugin) {
        this.plugin = plugin;
    }

    /**
     * Returns the Ability enum for a given name, or null if invalid.
     */
    public static Ability getAbility(String name) {
        try {
            return Ability.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Reduces a potion effect to a given duration if it is longer.
     */
    private static void reducePotionEffect(Player player, PotionEffectType type, int duration) {
        try {
            for (PotionEffect potionEffect : player.getActivePotionEffects()) {
                if (potionEffect.getType() == type && potionEffect.getDuration() > duration) {
                    int amplifier = potionEffect.getAmplifier();
                    player.removePotionEffect(type);
                    player.addPotionEffect(new PotionEffect(type, duration, amplifier));
                    return;
                }
            }
        } catch (Exception e) {
            //Log using bukkit logger
            Bukkit.getLogger().severe("Error reducing potion effect for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();

        }
    }

    /**
     * Updates a player's abilities and applies/removes passive effects as
     * needed.
     */
    public void updateAbilityAmounts(Player player) {
        try {
            Map<Ability, Integer> oldAbilities = getAbilities(player);

            String[] armorNames = new String[4];
            int i = 0;
            for (ItemStack piece : new ItemStack[]{
                player.getInventory().getHelmet(),
                player.getInventory().getChestplate(),
                player.getInventory().getLeggings(),
                player.getInventory().getBoots()}) {
                if (piece != null && piece.hasItemMeta()) {
                    var meta = piece.getItemMeta();
                    if (meta.getPersistentDataContainer().has(ArmorUtils.ABILITY_KEY, org.bukkit.persistence.PersistentDataType.STRING)) {
                        String abilityName = meta.getPersistentDataContainer().get(ArmorUtils.ABILITY_KEY, org.bukkit.persistence.PersistentDataType.STRING);
                        if (abilityName != null) {
                            armorNames[i++] = abilityName;
                        }
                    }  
                }
            }

            Map<Ability, Integer> newAbilities = getAbilityAmounts(armorNames);
            abilities.put(player.getName(), newAbilities);

            int duration = Integer.MAX_VALUE;

            // MOON (Jump Boost)
            if (oldAbilities.containsKey(Ability.MOON) && !newAbilities.containsKey(Ability.MOON)) {
                player.removePotionEffect(PotionEffectType.JUMP_BOOST);
            } else if (newAbilities.containsKey(Ability.MOON) && player.hasPermission("armorabilities.jump")) {
                int jumpAmt = newAbilities.get(Ability.MOON);
                player.removePotionEffect(PotionEffectType.JUMP_BOOST);
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, duration, plugin.getData().getJumpNum() * jumpAmt));
            }

            // SPEED (Speed & Haste)
            if (oldAbilities.containsKey(Ability.SPEED) && !newAbilities.containsKey(Ability.SPEED)) {
                player.removePotionEffect(PotionEffectType.SPEED);
                player.removePotionEffect(PotionEffectType.HASTE);
            } else if (newAbilities.containsKey(Ability.SPEED)) {
                int speedAmt = newAbilities.get(Ability.SPEED);
                if (player.hasPermission("armorabilities.speed")) {
                    player.removePotionEffect(PotionEffectType.SPEED);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, plugin.getData().getSpeedNum() * speedAmt));
                }
                if (player.hasPermission("armorabilities.haste")) {
                    int fastDig = plugin.getData().getSpeedHasteNum();
                    if (speedAmt == 4) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, duration, fastDig));
                    } else if (oldAbilities.containsKey(Ability.SPEED) && oldAbilities.get(Ability.SPEED) == 4) {
                        player.removePotionEffect(PotionEffectType.HASTE);
                    }
                }
            }

            // SCUBA (Water Breathing, Night Vision, Haste)
            if (oldAbilities.containsKey(Ability.SCUBA) && !newAbilities.containsKey(Ability.SCUBA)) {
                player.removePotionEffect(PotionEffectType.WATER_BREATHING);
                player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                player.removePotionEffect(PotionEffectType.HASTE);
            } else if (newAbilities.containsKey(Ability.SCUBA) && player.hasPermission("armorabilities.scuba")) {
                if (!Objects.equals(newAbilities.get(Ability.SCUBA), oldAbilities.get(Ability.SCUBA))) {
                    if (player.getEyeLocation().getBlock().getType() == Material.WATER) {
                        int scubaAmt = newAbilities.get(Ability.SCUBA);
                        if (scubaAmt == 4) {
                            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                            player.removePotionEffect(PotionEffectType.HASTE);
                            int fastDig = plugin.getData().getScubaHasteNum();
                            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, duration, fastDig));
                            player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 2400, 1));
                            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, duration, 1));
                        } else if (oldAbilities.containsKey(Ability.SCUBA) && oldAbilities.get(Ability.SCUBA) == 4) {
                            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                            player.removePotionEffect(PotionEffectType.HASTE);
                        }
                    }
                }
            }

            // MINER (Haste)
            if (oldAbilities.containsKey(Ability.MINER) && !newAbilities.containsKey(Ability.MINER)) {
                player.removePotionEffect(PotionEffectType.HASTE);
            } else if (newAbilities.containsKey(Ability.MINER) && player.hasPermission("armorabilities.miner")) {
                if (!Objects.equals(newAbilities.get(Ability.MINER), oldAbilities.get(Ability.MINER))) {
                    int hasteNum = plugin.getData().getMinerHasteNum();
                    player.removePotionEffect(PotionEffectType.HASTE);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, duration, hasteNum));
                }
            }

            // LAVA (Fire Resistance)
            if (oldAbilities.containsKey(Ability.LAVA) && !newAbilities.containsKey(Ability.LAVA)) {
                player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
            } else if (newAbilities.containsKey(Ability.LAVA) && player.hasPermission("armorabilities.lavaswim")) {
                if (!Objects.equals(newAbilities.get(Ability.LAVA), oldAbilities.get(Ability.LAVA))) {
                    if (player.getLocation().getBlock().getType() == Material.LAVA) {
                        if (oldAbilities.containsKey(Ability.LAVA) && newAbilities.get(Ability.LAVA) < oldAbilities.get(Ability.LAVA)) {
                            int lavaAmt = newAbilities.get(Ability.LAVA);
                            int length = plugin.getData().getLavaTime() * lavaAmt * lavaAmt * 20;
                            reducePotionEffect(player, PotionEffectType.FIRE_RESISTANCE, length);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Log using bukkit logger
            Bukkit.getLogger().severe("Error updating abilities for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Converts armor names to a map of abilities and their strengths.
     */
    public Map<Ability, Integer> getAbilityAmounts(String... names) {
        try {

            Map<Ability, Integer> abilityAmounts = new EnumMap<>(Ability.class);
            for (String name : names) {
                if (name != null) {
                    Ability ability = getAbility(name);
                    if (ability != null) {
                        abilityAmounts.put(ability, abilityAmounts.getOrDefault(ability, 0) + 1);
                    }
                }
            }
            // Remove abilities that require a full set but don't have all 4 pieces
            abilityAmounts.entrySet().removeIf(entry -> entry.getKey().requiresFullSet() && entry.getValue() != 4);
            return abilityAmounts;

        } catch (Exception e) {
            // Log using bukkit logger
            Bukkit.getLogger().severe("Error getting ability amounts: " + e.getMessage());
            e.printStackTrace();
            // Return an empty map in case of error
            return Collections.emptyMap();
        }
    }

    /**
     * Gets all abilities currently active on a player.
     */
    public Map<Ability, Integer> getAbilities(Player player) {
        return abilities.getOrDefault(player.getName(), new EnumMap<>(Ability.class));
    }

    /**
     * Sets the player as currently scuba diving.
     */
    public void addScuba(Player player) {
        scubaActive.add(player.getName());
    }

    /**
     * Returns true if the player is currently scuba diving.
     */
    public boolean isScuba(Player player) {
        return scubaActive.contains(player.getName());
    }

    /**
     * Removes a player from scuba diving.
     */
    public void removeScuba(Player player) {
        scubaActive.remove(player.getName());
    }

    /**
     * Sets the player as swimming in lava.
     */
    public void addLava(Player player) {
        lavaActive.add(player.getName());
    }

    /**
     * Returns true if the player is currently swimming in lava.
     */
    public boolean isLava(Player player) {
        return lavaActive.contains(player.getName());
    }

    /**
     * Removes a player from swimming in lava.
     */
    public void removeLava(Player player) {
        lavaActive.remove(player.getName());
    }

    /**
     * Removes all ability tracking for a player (e.g. on logout).
     */
    public void removeAbilities(Player player) {
        abilities.remove(player.getName());
        removeScuba(player);
        removeLava(player);
    }
}
