package org.saintqd.vineriumlib.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class VinGUI {

    private final Player player;
    private Inventory inventory;
    private final HashMap<Integer, ItemStack> items;
    private final HashMap<Integer,VinGUIButton> buttons;

    public VinGUI(Player player) {
        this.player = player;
        this.inventory = null;
        this.items = new HashMap<>();
        this.buttons = new HashMap<>();
    }

    public Player getPlayer() {
        return player;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void openInventory() {
        openInventory(player);
    }

    public void openInventory(Player player) {
        player.openInventory(inventory);
    }

    public HashMap<Integer, ItemStack> getItems() {
        return items;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public HashMap<Integer, VinGUIButton> getButtons() {
        return buttons;
    }

    public void processClick(InventoryClickEvent event) {
        // Не совершаем действий, если игрок выбрасывает предметы из своего инвентаря
        if ((event.getClick().equals(ClickType.DROP) || event.getClick().equals(ClickType.CONTROL_DROP))
                && event.getView().getBottomInventory() == event.getClickedInventory())
            return;
        // Отменяем и совершаем действие, если игрок кликнул на слот в инвентаре меню
        if (event.getView().getTopInventory() == event.getClickedInventory())
            event.setCancelled(true);
        // Отменяем и совершаем действие, если игрок совершил Shift-клик
        // или нажал на клавишу цифры, целевой инвентарь не имеет значения
        if (event.getClick().isShiftClick() || event.getClick().isKeyboardClick())
            event.setCancelled(true);
        int slot = event.getRawSlot();
        VinGUIButton button = buttons.get(slot);
        if (button != null)
            button.getEventConsumer().accept(event);
    }
}
