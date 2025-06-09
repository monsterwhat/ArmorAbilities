package com.playdeca.armorabilities.listeners;

import com.playdeca.armorabilities.ArmorAbilities;
import com.playdeca.armorabilities.data.Ability;
import com.playdeca.armorabilities.data.AbilityInfo;
import com.playdeca.armorabilities.data.AbilityManager;
import com.playdeca.armorabilities.utils.ArmorUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class InventoryClick implements Listener {

    private final ArmorAbilities plugin;

    public InventoryClick(ArmorAbilities armorAbilities) {
        this.plugin = armorAbilities;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        // Handle anvil renaming protection for ability items
        if (event.getView().getType() == InventoryType.ANVIL && event.getRawSlot() == 2 && event.getCurrentItem() != null) {
            ItemStack input = event.getCurrentItem();
            ItemStack output = event.getInventory().getItem(0);

            AbilityInfo inputAbility = getInfoFromItem(input);
            AbilityInfo outputAbility = getInfoFromItem(output);

            if (inputAbility != outputAbility) {
                String msg = (inputAbility == null)
                        ? "You cannot rename your /ability equipment!"
                        : "You cannot rename your equipment to start with an /ability";
                event.getWhoClicked().sendMessage(Component.text(msg, NamedTextColor.RED));
                event.setCancelled(true);
            }
        }

        // Update ability tracking if armor is equipped or moved
        if (event.getSlotType() == SlotType.ARMOR || event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            plugin.getTask().addPlayers();
        }
    }

    private AbilityInfo getInfoFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        var meta = item.getItemMeta();
        if (meta.getPersistentDataContainer().has(ArmorUtils.ABILITY_KEY, org.bukkit.persistence.PersistentDataType.STRING)) {
            String abilityName = meta.getPersistentDataContainer().get(ArmorUtils.ABILITY_KEY, org.bukkit.persistence.PersistentDataType.STRING);
            if (abilityName != null) {
                return plugin.getData().getInfo(abilityName);
            }
        }
        return null;
    } 

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item != null && item.hasItemMeta()) {
            var meta = item.getItemMeta();
            if (meta.getPersistentDataContainer().has(ArmorUtils.ABILITY_KEY, org.bukkit.persistence.PersistentDataType.STRING)) {
                String abilityName = meta.getPersistentDataContainer().get(ArmorUtils.ABILITY_KEY, org.bukkit.persistence.PersistentDataType.STRING);
                Ability ability = AbilityManager.getAbility(abilityName);
                if (ability != null) {
                    plugin.getTask().addPlayers();
                }
            }
        }
    }
}
