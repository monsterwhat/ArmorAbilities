package com.playdeca.armorabilities.data;

import org.bukkit.Bukkit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import com.playdeca.armorabilities.utils.ArmorUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Extra data for each ability, all taken from the config and stored here so that the config doesn't need to be re-read
 */
public class AbilityInfo {

    private final Ability ability;
    private final String madeWith;
    private final String types;

    /**
     * Create the ability info for an ability.
     * Adds armor recipes for each armor type.
     */
    AbilityInfo(Ability ability, String item, List<String> types) {
        types.forEach(type -> getArmor(type).forEach(material -> ArmorUtils.addArmorRecipe(material, ability, item)));
        this.types = types.toString();
        this.ability = ability;
        this.madeWith = item;
    }

    /** @return the ability that belongs to this info object */
    public Ability getAbility() {
        return ability;
    }

    /**
     * Message all the information about this ability to the given player.
     * @param player the player to send the message to
     */
    public void messageInfo(Player player) {
        player.sendMessage(Component.text(ability.getDescription()));
        player.sendMessage(
            Component.text("The ", NamedTextColor.WHITE)
                .append(Component.text(ability.toString(), NamedTextColor.GOLD))
                .append(Component.text(" ability is made with ", NamedTextColor.WHITE))
                .append(Component.text(madeWith, NamedTextColor.GOLD))
        );
        player.sendMessage(
            Component.text("It can be made on these armor types: ", NamedTextColor.GRAY)
                .append(Component.text(types, NamedTextColor.GRAY))
        );
    }

    /**
     * Get a list of materials belonging to the given armor string.
     * @param armorType the armor type
     * @return the materials relating to the given string
     */
    private ArrayList<Material> getArmor(String armorType) {
        ArrayList<Material> armorSet = new ArrayList<>(4);
        switch (armorType) {
            case "leather" -> armorSet.addAll(List.of(Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS));
            case "iron" -> armorSet.addAll(List.of(Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS));
            case "gold" -> armorSet.addAll(List.of(Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS));
            case "chainmail" -> armorSet.addAll(List.of(Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS));
            case "diamond" -> armorSet.addAll(List.of(Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS));
            case "netherite" -> armorSet.addAll(List.of(Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS));
            case "turtle" -> armorSet.add(Material.TURTLE_HELMET);
            case "elytra" -> armorSet.add(Material.ELYTRA);
            case "shield" -> armorSet.add(Material.SHIELD);
            case "pumpkin" -> armorSet.addAll(List.of(Material.PUMPKIN, Material.CARVED_PUMPKIN));
            case "skull" -> armorSet.addAll(List.of(Material.SKELETON_SKULL, Material.ZOMBIE_HEAD, Material.PLAYER_HEAD, Material.WITHER_SKELETON_SKULL));
            default -> Bukkit.getLogger().warning("[ArmorAbilities]: Unknown armor type \"" + armorType + "\" set for " + ability);
        }
        return armorSet;
    }
}
