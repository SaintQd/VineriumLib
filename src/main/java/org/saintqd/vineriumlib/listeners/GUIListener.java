package org.saintqd.vineriumlib.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.saintqd.vineriumlib.gui.data.CustomGUI;
import org.saintqd.vineriumlib.gui.holders.VinGUIHolder;

import java.util.Base64;

public class GUIListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(final InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();

        if (!(event.getInventory().getHolder() instanceof VinGUIHolder)) return;

        if (player.getOpenInventory().getTopInventory().getHolder() instanceof VinGUIHolder vinGUIHolder) {
            vinGUIHolder.getGui().processClick(event);
        }
    }

    @EventHandler
    public void onInventoryDrag(final InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof VinGUIHolder)) return;
        for (int slot : event.getRawSlots())
            if (slot <= event.getInventory().getSize() - 1)
                event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof VinGUIHolder vinGUIHolder)) return;
        if (vinGUIHolder.getGui() instanceof CustomGUI customGUI) {
            if (!event.getPlayer().getPersistentDataContainer().has(CustomGUI.getPendingReplacementKey())) {
                CustomGUI.restoreInventory((Player) event.getPlayer());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!(event.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof VinGUIHolder vinGUIHolder)) return;
        if (!event.getPlayer().getPersistentDataContainer().has(CustomGUI.getReplacedInventoryKey())) return;
        event.getDrops().clear();
        String[] itemString = event.getPlayer().getPersistentDataContainer().get(CustomGUI.getReplacedInventoryKey(), PersistentDataType.STRING).split(";");
        for (String itemData : itemString) {
            String[] itemInfo = itemData.split(":");
            if (itemInfo[0].isEmpty())
                break;
            ItemStack itemStack = ItemStack.deserializeBytes(Base64.getDecoder().decode(itemInfo[1]));
            event.getDrops().add(itemStack);
        }
        event.getPlayer().getPersistentDataContainer().remove(CustomGUI.getReplacedInventoryKey());
        event.getPlayer().getPersistentDataContainer().remove(CustomGUI.getPendingReplacementKey());
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (player.getPersistentDataContainer().has(CustomGUI.getReplacedInventoryKey()))
                event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!event.getPlayer().getPersistentDataContainer().has(CustomGUI.getReplacedInventoryKey())) return;
        CustomGUI.restoreInventory(event.getPlayer());
    }
}
