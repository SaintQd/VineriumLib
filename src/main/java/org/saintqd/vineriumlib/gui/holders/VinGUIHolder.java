package org.saintqd.vineriumlib.gui.holders;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.saintqd.vineriumlib.gui.VinGUI;

public class VinGUIHolder implements InventoryHolder {

    private final VinGUI gui;

    public VinGUIHolder(VinGUI gui) {
        this.gui = gui;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    public VinGUI getGui() {
        return gui;
    }
}
