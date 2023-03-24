package com.playdeca.armorabilities.utils;

import com.playdeca.armorabilities.ArmorAbilities;
import com.playdeca.armorabilities.data.Ability;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public final class ArmorUtils {

    public static final Pattern WORD = Pattern.compile("\\s+");
    private static final Pattern SPACE = Pattern.compile("_", Pattern.LITERAL);

    private ArmorUtils() {
    }

    /**
     * Create the shapeless recipe for the given ability with the given materials
     *
     * @param armor   the armor
     * @param ability the ability
     * @param item    the item to add to the armor to create the ability
     */
    public static void addArmorRecipe(Material armor, Ability ability, String item) {

        Material material = Material.matchMaterial(item);
        if ((material == null) || (material == Material.AIR)) {
            throw new IllegalArgumentException("Could not match material for item: " + item);
        }

        String[] newNames = WORD.split(SPACE.matcher(armor.name()).replaceAll(" ").toLowerCase());

        for (int i = 0; i < newNames.length; i++) {
            newNames[i] = newNames[i].substring(0, 1).toUpperCase() + newNames[i].substring(1);
        }

        List<String> nameInOrder = new ArrayList<>(Arrays.asList(newNames));

        if (nameInOrder.size() < 2) {
            nameInOrder.add(newNames[0]);
        }

        ItemStack result = new ItemStack(armor);
        String newName = ability.toString() + ' ' + nameInOrder.get(1);
        ItemMeta itemMeta;

        if (result.getItemMeta() == null) {
            itemMeta = Bukkit.getItemFactory().getItemMeta(result.getType());
        }
        else {
            itemMeta = result.getItemMeta();
        }

        itemMeta.setDisplayName(newName);
        result.setItemMeta(itemMeta);
        NamespacedKey bukkitKey = new NamespacedKey(ArmorAbilities.getInstance(), armor.name() + '_' + ability + '_' + item);

        ShapelessRecipe recipe = new ShapelessRecipe(bukkitKey, result);
        recipe.addIngredient(material);
        recipe.addIngredient(armor);
        Bukkit.getServer().addRecipe(recipe);
    }

}
