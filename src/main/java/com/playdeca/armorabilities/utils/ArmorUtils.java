package com.playdeca.armorabilities.utils;

import com.playdeca.armorabilities.data.Ability;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class ArmorUtils {

    public static final Pattern WORD = Pattern.compile("\\s+");
    public static final NamespacedKey ABILITY_KEY = new NamespacedKey("armorabilities", "ability");
    public static final NamespacedKey LEGACY_KEY = new NamespacedKey("armorabilities", "legacy");

    /** Registered legacy crafting recipes: recipe key -> {abilityName, armorMaterialName}. */
    public static final Map<NamespacedKey, String[]> LEGACY_RECIPES = new HashMap<>();

    private ArmorUtils() {
    }

    /**
     * Register a shapeless legacy crafting recipe: armor piece + special item
     * produces an untagged placeholder result; CraftingListener tags the actual
     * crafted piece with the ability and the legacy marker.
     */
    public static void addArmorRecipe(Material armor, Ability ability, String item) {
        Material special = Material.matchMaterial(item);
        if (special == null) {
            Bukkit.getLogger().warning("[ArmorAbilities]: Unknown special item \"" + item + "\" set for " + ability);
            return;
        }
        NamespacedKey key = new NamespacedKey("armorabilities",
                "legacy_" + ability.name().toLowerCase() + "_" + armor.name().toLowerCase());
        if (Bukkit.getRecipe(key) != null || LEGACY_RECIPES.containsKey(key)) return;

        ShapelessRecipe recipe = new ShapelessRecipe(key, new ItemStack(armor));
        recipe.addIngredient(armor);
        recipe.addIngredient(special);
        Bukkit.addRecipe(recipe);
        LEGACY_RECIPES.put(key, new String[]{ability.name(), armor.name()});
    }

    /** Tag an armor piece as a legacy-tier ability piece. */
    public static void tagLegacyPiece(ItemStack piece, Ability ability) {
        ItemMeta meta = piece.getItemMeta();
        meta.displayName(Component.text("Legacy " + ability + " " + prettyMaterial(piece.getType())));
        meta.getPersistentDataContainer().set(ABILITY_KEY, PersistentDataType.STRING, ability.name());
        meta.getPersistentDataContainer().set(LEGACY_KEY, PersistentDataType.BYTE, (byte) 1);
        piece.setItemMeta(meta);
    }

    public static boolean isLegacyPiece(ItemStack piece) {
        if (piece == null || !piece.hasItemMeta()) return false;
        var pdc = piece.getItemMeta().getPersistentDataContainer();
        return pdc.has(ABILITY_KEY, PersistentDataType.STRING)
                && pdc.has(LEGACY_KEY, PersistentDataType.BYTE)
                && pdc.get(LEGACY_KEY, PersistentDataType.BYTE) == (byte) 1;
    }

    private static String prettyMaterial(Material material) {
        String[] words = material.name().toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(' ');
        }
        return sb.toString().trim();
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
