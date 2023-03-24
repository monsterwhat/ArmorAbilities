package com.playdeca.armorabilities.data;

import com.playdeca.armorabilities.ArmorAbilities;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import com.playdeca.armorabilities.utils.ArmorUtils;

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
     * Silently obtain the ability with the given name
     *
     * @param name the name of the ability
     *
     * @return the ability
     */
    public static Ability getAbility(String name) {
        try {
            return Ability.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Reduces a potion effect to a given duration if it is longer
     *
     * @param player   the player who has the potion effect
     * @param type     the type of potion effect
     * @param duration the maximum duration this potion effect should now be
     */
    private static void reducePotionEffect(Player player, PotionEffectType type, int duration) {
        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            if (potionEffect.getType() == type) {
                if (potionEffect.getDuration() > duration) {
                    int amplifier = potionEffect.getAmplifier();
                    player.removePotionEffect(potionEffect.getType());
                    player.addPotionEffect(new PotionEffect(type, duration, amplifier));
                }
                return;
            }
        }
    }

    /**
     * Update which abilities the given player has, and change their passive effects accordingly
     *
     * @param player the player to update
     */
    public void updateAbilityAmounts(Player player) {

        Map<Ability, Integer> oldAbilities = getAbilities(player);

        String[] armorNames = new String[4];
        int i = 0;

        //check which ability effects the player should have
        ItemStack head = player.getInventory().getHelmet();
        if ((head != null)) {
            Objects.requireNonNull(head.getItemMeta()).getDisplayName();
            armorNames[i] = ArmorUtils.WORD.split(head.getItemMeta().getDisplayName())[0];
            i++;
        }

        ItemStack chest = player.getInventory().getChestplate();
        if ((chest != null)) {
            Objects.requireNonNull(chest.getItemMeta()).getDisplayName();
            armorNames[i] = ArmorUtils.WORD.split(chest.getItemMeta().getDisplayName())[0];
            i++;
        }

        ItemStack legs = player.getInventory().getLeggings();
        if ((legs != null)) {
            Objects.requireNonNull(legs.getItemMeta()).getDisplayName();
            armorNames[i] = ArmorUtils.WORD.split(legs.getItemMeta().getDisplayName())[0];
            i++;
        }

        ItemStack feet = player.getInventory().getBoots();
        if ((feet != null)) {
            Objects.requireNonNull(feet.getItemMeta()).getDisplayName();
            armorNames[i] = ArmorUtils.WORD.split(feet.getItemMeta().getDisplayName())[0];
        }

        Map<Ability, Integer> newAbilities = getAbilityAmounts(armorNames);
        abilities.put(player.getName(), newAbilities);

        //adjust effects
        int durationOfAbilities = Integer.MAX_VALUE;
        if (oldAbilities.containsKey(Ability.MOON) && !newAbilities.containsKey(Ability.MOON)) {
            player.removePotionEffect(PotionEffectType.JUMP);

        } else if (newAbilities.containsKey(Ability.MOON) && player.hasPermission("armorabilities.jump")) {

            int jumpAmt = newAbilities.get(Ability.MOON);
            player.removePotionEffect(PotionEffectType.JUMP);
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, durationOfAbilities,
                                                    plugin.getData().getJumpNum() * jumpAmt));
        }

        if (oldAbilities.containsKey(Ability.SPEED) && !newAbilities.containsKey(Ability.SPEED)) {
            player.removePotionEffect(PotionEffectType.SPEED);
            player.removePotionEffect(PotionEffectType.FAST_DIGGING);

        } else if (newAbilities.containsKey(Ability.SPEED)) {

            if (player.hasPermission("armorabilities.speed")) {
                int speedAmt = newAbilities.get(Ability.SPEED);
                player.removePotionEffect(PotionEffectType.SPEED);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, durationOfAbilities,
                                                        plugin.getData().getSpeedNum() * speedAmt));
            }

            if (player.hasPermission("armorabilities.haste")) {
                int fastDig = plugin.getData().getSpeedHasteNum();
                if (newAbilities.get(Ability.SPEED) == 4) {
                    player.addPotionEffect(
                            new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, fastDig));
                } else if (oldAbilities.containsKey(Ability.SPEED) && (oldAbilities.get(Ability.SPEED) == 4)) {
                    player.removePotionEffect(PotionEffectType.FAST_DIGGING);
                }
            }
        }

        if (oldAbilities.containsKey(Ability.SCUBA) && !newAbilities.containsKey(Ability.SCUBA)) {
            player.removePotionEffect(PotionEffectType.WATER_BREATHING);
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            player.removePotionEffect(PotionEffectType.FAST_DIGGING);
        } else if (newAbilities.containsKey(Ability.SCUBA) && player.hasPermission("armorabilities.scuba")) {

            //check if value is different
            if (!newAbilities.get(Ability.SCUBA).equals(oldAbilities.get(Ability.SCUBA))) {

                //check if they are underwater
                if ((player.getEyeLocation().getBlock().getType() == Material.WATER) ) {

                    if (newAbilities.get(Ability.SCUBA) == 4) {
                        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                        player.removePotionEffect(PotionEffectType.FAST_DIGGING);
                        int fastDig = plugin.getData().getScubaHasteNum();
                        player.addPotionEffect(
                                new PotionEffect(PotionEffectType.FAST_DIGGING, durationOfAbilities, fastDig));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 2400, 1));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, durationOfAbilities, 1));

                    } else if (oldAbilities.containsKey(Ability.SCUBA) && (oldAbilities.get(Ability.SCUBA) == 4)) {
                        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                        player.removePotionEffect(PotionEffectType.FAST_DIGGING);
                    }
                }
            }
        }

        if (oldAbilities.containsKey(Ability.MINER) && !newAbilities.containsKey(Ability.MINER)) {
            player.removePotionEffect(PotionEffectType.FAST_DIGGING);

        } else if (newAbilities.containsKey(Ability.MINER) && player.hasPermission("armorabilities.miner")) {

            if (!newAbilities.get(Ability.MINER).equals(oldAbilities.get(Ability.MINER))) {

                int hasteNum = plugin.getData().getMinerHasteNum();
                player.removePotionEffect(PotionEffectType.FAST_DIGGING);
                player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, durationOfAbilities, hasteNum));
            }
        }

        if (oldAbilities.containsKey(Ability.LAVA) && !newAbilities.containsKey(Ability.MINER)) {
            player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
        } else if (newAbilities.containsKey(Ability.LAVA) && player.hasPermission("armorabilities.lavaswim")) {

            //check if value is different
            if (!newAbilities.get(Ability.LAVA).equals(oldAbilities.get(Ability.LAVA))) {

                //check if they are in lava
                if ((player.getLocation().getBlock().getType() == Material.LAVA) ) {

                    //we only want to decrease the effect if needed, so there must be an old effect higher than new
                    if (oldAbilities.containsKey(Ability.LAVA) &&
                        (newAbilities.get(Ability.LAVA) < oldAbilities.get(Ability.LAVA))) {
                        int lavaAmt = newAbilities.get(Ability.LAVA);
                        int length = plugin.getData().getLavaTime() * lavaAmt * lavaAmt * 20;
                        reducePotionEffect(player, PotionEffectType.FIRE_RESISTANCE, length);
                    }
                }
            }
        }
    }

    /**
     * turn the provided names into a map containing the abilities and their strengths
     *
     * @param names the name(s) of the ability items
     *
     * @return the map
     */
    public Map<Ability, Integer> getAbilityAmounts(String... names) {

        //check the strength of each ability effect
        Map<Ability, Integer> abilityAmounts = new EnumMap<>(Ability.class);

        for (String name : names) {
            if (name != null) {
                Ability ability = getAbility(name);
                if (ability != null) {
                    if (abilityAmounts.containsKey(ability))
                        abilityAmounts.put(ability, abilityAmounts.get(ability) + 1);
                    else
                        abilityAmounts.put(ability, 1);
                }
            }
        }

        //remove any which don't have the full set which require it

        abilityAmounts.entrySet().removeIf(entry -> entry.getKey().requiresFullSet() && (entry.getValue() != 4));

        return abilityAmounts;
    }

    /**
     * Get all the abilities currently active on a player
     *
     * @param player the player
     *
     * @return the abilities
     */
    public Map<Ability, Integer> getAbilities(Player player) {
        return abilities.containsKey(player.getName()) ?
                abilities.get(player.getName()) : new EnumMap<>(Ability.class);
    }

    /**
     * Sets the player as currently scuba diving
     *
     * @param player the player
     */
    public void addScuba(Player player) {
        scubaActive.add(player.getName());
    }

    /**
     * Gets if the player is currently scuba diving
     *
     * @param player the player
     *
     * @return if they are scuba diving
     */
    public boolean isScuba(Player player) {
        return scubaActive.contains(player.getName());
    }

    /**
     * Removes a player from scuba diving
     *
     * @param player the player
     */
    public void removeScuba(Player player) {
        scubaActive.remove(player.getName());
    }

    /**
     * Sets the player as swimming in lava
     *
     * @param player the player
     */
    public void addLava(Player player) {
        lavaActive.add(player.getName());
    }

    /**
     * Gets if the player is currently swimming in lava
     *
     * @param player the player
     *
     * @return if they are swimming in lava
     */
    public boolean isLava(Player player) {
        return lavaActive.contains(player.getName());
    }

    /**
     * Removes a player from swimming in lava
     *
     * @param player the player
     */
    public void removeLava(Player player) {
        lavaActive.remove(player.getName());
    }

    /**
     * Remove the abilities from a player because they have left the server
     *
     * @param player the player
     */
    public void removeAbilities(Player player) {
        abilities.remove(player.getName());
        removeScuba(player);
        removeLava(player);
    }
}
