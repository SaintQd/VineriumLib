package org.saintqd.vineriumlib.gui;

import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

public class VinGUIButton {

    private Consumer<InventoryClickEvent> eventConsumer;
    public VinGUIButton consumer(Consumer<InventoryClickEvent> consumer) {
        this.eventConsumer = consumer;
        return this;
    }
    public Consumer<InventoryClickEvent> getEventConsumer() {
        return eventConsumer;
    }
}
