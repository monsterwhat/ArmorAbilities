package com.playdeca.armorabilities.listeners;

import com.playdeca.armorabilities.data.Ability;
import com.playdeca.armorabilities.utils.ArmorUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

public class CraftingListener implements Listener {

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        CraftingInventory inv = event.getInventory();
        var recipe = event.getRecipe();
        if (!(recipe instanceof org.bukkit.inventory.ShapelessRecipe shapeless)) return;

        String[] entry = ArmorUtils.LEGACY_RECIPES.get(shapeless.getKey());
        if (entry == null) return;

        try {
            Ability ability = Ability.valueOf(entry[0]);
            int armorSlots = 0;
            int specialSlots = 0;
            for (ItemStack item : inv.getMatrix()) {
                if (item == null || item.getType().isAir()) continue;
                if (ArmorUtils.getAbilityByMaterial(item.getType()) != null) specialSlots++;
                else armorSlots++;
            }
            if (armorSlots != 1 || specialSlots != 1) {
                inv.setResult(null);
                return;
            }
            ItemStack result = inv.getResult();
            if (result == null) return;
            ArmorUtils.tagLegacyPiece(result, ability);
            inv.setResult(result);
        } catch (Exception e) {
            com.playdeca.armorabilities.ArmorAbilities.getInstance().getLogger()
                    .severe("Error preparing legacy craft: " + e.getMessage());
        }
    }
}