package com.playdeca.armorabilities.listeners;

import com.playdeca.armorabilities.ArmorAbilities;
import com.playdeca.armorabilities.data.Ability;
import com.playdeca.armorabilities.utils.ArmorUtils; 
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.inventory.SmithItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.Material;
import net.kyori.adventure.text.Component;

public class SmithingTableListener implements Listener {

    private final ArmorAbilities plugin;

    public SmithingTableListener(ArmorAbilities armorAbilities) {
        this.plugin = armorAbilities;
    }

    @EventHandler
    public void onSmithingPreparation(PrepareSmithingEvent event) {
        try {
            ItemStack result = event.getResult();
            ItemStack modifier = event.getInventory().getInputMineral();

            if (result == null || modifier == null) {
                return;
            }

            Ability ability = ArmorUtils.getAbilityByMaterial(modifier.getType());
            if (ability != null) {
                ItemMeta meta = result.getItemMeta();
                meta.displayName(Component.text(ability.toString() + " " + formatMaterialName(result.getType())));
                meta.getPersistentDataContainer().set(ArmorUtils.ABILITY_KEY, PersistentDataType.STRING, ability.name());
                result.setItemMeta(meta);
                event.setResult(result); // Use setResult, not setResult on inventory!
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error processing PrepareSmithingEvent: " + e.getMessage());
            e.printStackTrace();
            event.setResult(null);
        }
    }

    //SmithItemEvent
    @EventHandler
    public void onSmithingItem(SmithItemEvent event) {
        try {
            ItemStack result = event.getInventory().getResult();
            ItemStack modifier = event.getInventory().getInputMineral();

            Ability ability = ArmorUtils.getAbilityByMaterial(modifier != null ? modifier.getType() : null);
            if (ability != null && result != null) {
                ItemMeta meta = result.getItemMeta();
                // Only add your ability and display name, do not replace meta!
                meta.displayName(Component.text(ability.toString() + " " + formatMaterialName(result.getType())));
                meta.getPersistentDataContainer().set(ArmorUtils.ABILITY_KEY, PersistentDataType.STRING, ability.name());
                result.setItemMeta(meta);
                event.getInventory().setResult(result);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error processing SmithItemEvent: " + e.getMessage());
            e.printStackTrace();
            event.getInventory().setResult(null);
        }
    }
 
    // Helper to format material name
    private String formatMaterialName(Material material) {
        String[] words = material.name().toLowerCase().split("_");
        StringBuilder formattedName = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                formattedName.append(Character.toUpperCase(word.charAt(0)))
                             .append(word.substring(1))
                             .append(" ");
            }
        }
        return formattedName.toString().trim();
    }
}
