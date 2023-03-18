package com.gigosaurus.armorabilities.listeners;

import com.gigosaurus.armorabilities.ArmorAbilities;
import com.gigosaurus.armorabilities.data.Ability;
import com.gigosaurus.armorabilities.data.AbilityInfo;
import com.gigosaurus.armorabilities.data.AbilityManager;
import com.gigosaurus.armorabilities.utils.ArmorUtils;
import org.bukkit.ChatColor;
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

public class InventoryClick implements Listener {

    private final ArmorAbilities plugin;

    public InventoryClick(ArmorAbilities armorAbilities) {
        plugin = armorAbilities;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        //if they are clicking to accept an anvil transaction
        if ((event.getView().getType() == InventoryType.ANVIL) && (event.getRawSlot() == 2) &&
            (event.getCurrentItem() != null) ) {

            //check if their input and output items have the same ability
            ItemStack input = event.getCurrentItem();
            ItemStack output = event.getInventory().getItem(0);

            AbilityInfo inputAbility = getInfoFromItem(input);
            AbilityInfo outputAbility = getInfoFromItem(output);

            if (inputAbility != outputAbility) {
                if (inputAbility == null) {
                    event.getWhoClicked().sendMessage(ChatColor.RED + "You cannot rename your /ability equipment!");
                } else {
                    event.getWhoClicked().sendMessage(
                            ChatColor.RED + "You cannot rename your equipment to " + "start with an /ability");
                }
                event.setCancelled(true);
            }
        }

        if ((event.getSlotType() == SlotType.ARMOR) || (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
//            plugin.getTask().addPlayer((Player) event.getWhoClicked());
            plugin.getTask().addPlayers();
        }
    }

    private AbilityInfo getInfoFromItem(ItemStack item) {
        return ((item == null) || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) ? null :
               plugin.getData().getInfo(ArmorUtils.WORD.split(item.getItemMeta().getDisplayName())[0]);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        //check if a player is equipping an item from the hotbar
        if ((event.getAction() == Action.RIGHT_CLICK_AIR) || (event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            ItemStack item = event.getItem();

            if ((item != null) && item.getItemMeta() != null && item.getItemMeta().hasDisplayName()) {
                Ability ability = AbilityManager.getAbility(ArmorUtils.WORD.split(item.getItemMeta().getDisplayName())[0]);

                if (ability != null) {
                    // plugin.getTask().addPlayer(event.getPlayer());
                    plugin.getTask().addPlayers();
                }
            }
        }
    }
}
