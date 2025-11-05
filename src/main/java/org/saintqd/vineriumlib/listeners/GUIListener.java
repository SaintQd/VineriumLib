package org.saintqd.vineriumlib.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.saintqd.vineriumlib.gui.holders.VinGUIHolder;

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
}
