package com.playdeca.armorabilities.utils;

import com.playdeca.armorabilities.data.Ability;
import org.bukkit.Material; 
import org.bukkit.NamespacedKey;
import org.bukkit.Bukkit;

import java.util.regex.Pattern; 

public final class ArmorUtils {

    public static final Pattern WORD = Pattern.compile("\\s+");
    public static final NamespacedKey ABILITY_KEY = new NamespacedKey("armorabilities", "ability");

    private ArmorUtils() {
    }

    /**
     * Create the shapeless recipe for the given ability with the given
     * materials
     *
     * @param armor the armor
     * @param ability the ability
     * @param item the item to add to the armor to create the ability
     */
    public static void addArmorRecipe(Material armor, Ability ability, String item) {
        // Do nothing; crafting recipes are disabled.
        // Remove or comment out the log line to prevent console spam.
        // Bukkit.getLogger().info("[ArmorAbilities] Crafting recipes for abilities are disabled.");
    }

   
    public static Ability getAbilityByMaterial(Material material) {
        try { 
            return switch (material) {
                case REDSTONE ->
                    Ability.SPEED;
                case NETHER_QUARTZ_ORE ->
                    Ability.MOON;
                case LAPIS_LAZULI ->
                    Ability.SCUBA;
                case RESIN_BRICK ->
                    Ability.LAVA;
                case NETHERITE_INGOT ->
                    Ability.RAGE;
                case AMETHYST_SHARD ->
                    Ability.PEACE;
                case EMERALD_ORE ->
                    Ability.ASSASSIN;
                case DIAMOND ->
                    Ability.CREEPER;
                case GOLD_INGOT ->
                    Ability.SPIDER;
                case COPPER_INGOT ->
                    Ability.VAMPIRE;
                case IRON_INGOT ->
                    Ability.MINER;
                default ->
                    null;
            };
        } catch (Exception e) {
            // Log the exception for debugging purposes using bukkit's logger
            Bukkit.getLogger().severe("Error getting ability by material: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

    }
 
}
