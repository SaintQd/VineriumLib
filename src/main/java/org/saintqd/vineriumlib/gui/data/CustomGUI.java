package org.saintqd.vineriumlib.gui.data;

import com.google.common.base.Enums;
import com.google.common.base.Optional;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.datacomponent.item.PotionContents;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.intellij.lang.annotations.Subst;
import org.saintqd.vineriumlib.VineriumLib;
import org.saintqd.vineriumlib.gui.VinGUI;
import org.saintqd.vineriumlib.gui.VinGUIButton;
import org.saintqd.vineriumlib.gui.holders.VinGUIHolder;
import org.saintqd.vineriumlib.managers.VaultManager;
import org.saintqd.vineriumlib.utils.VinUtils;

import java.io.File;
import java.util.*;

public class CustomGUI extends VinGUI {

    private final static NamespacedKey REPLACED_INVENTORY_KEY = new NamespacedKey(VineriumLib.inst(),"replaced_inventory");
    private final static NamespacedKey PENDING_REPLACEMENT_KEY = new NamespacedKey(VineriumLib.inst(),"pending_replacement");

    private final ConfigurationSection menuConfig;

    public CustomGUI(Player player, NamespacedKey guiKey) {
        super(player);
        File file = new File(VineriumLib.inst().getCustomGUIManager().getGuiPaths().get(guiKey));
        if (file.exists()) {
            menuConfig = YamlConfiguration.loadConfiguration(file).getConfigurationSection(guiKey.getKey());
        }
        else menuConfig = null;
    }

    public static NamespacedKey getReplacedInventoryKey() {
        return REPLACED_INVENTORY_KEY;
    }

    public static NamespacedKey getPendingReplacementKey() {
        return PENDING_REPLACEMENT_KEY;
    }

    public ConfigurationSection getConfig() {
        return menuConfig;
    }

    @Override
    public void openInventory(Player player) {
        if (menuConfig.getBoolean("ReplaceInventory",false))
            replacePlayerInventory();
        super.openInventory(player);
        getPlayer().getPersistentDataContainer().remove(PENDING_REPLACEMENT_KEY);
    }

    @Override
    public void processClick(InventoryClickEvent event) {
        // Не совершаем действий, если игрок выбрасывает предметы из своего инвентаря,
        //  и если при этом отключено расширение меню на инвентарь
        boolean replaceInventory = menuConfig.getBoolean("ReplaceInventory",false);
        if (!replaceInventory && ((event.getClick().equals(ClickType.DROP) || event.getClick().equals(ClickType.CONTROL_DROP))
                && event.getView().getBottomInventory() == event.getClickedInventory()))
            return;
        // Отменяем и совершаем действие, если игрок кликнул на слот в инвентаре меню,
        //  или же если включено расширение меню на инвентарь
        if (event.getView().getTopInventory() == event.getClickedInventory() || replaceInventory)
            event.setCancelled(true);
        // Отменяем и совершаем действие, если игрок совершил Shift-клик
        // или нажал на клавишу цифры, целевой инвентарь не имеет значения
        if (event.getClick().isShiftClick() || event.getClick().isKeyboardClick())
            event.setCancelled(true);
        int slot = event.getRawSlot();
        VinGUIButton button = getButtons().get(slot);
        if (button != null)
            button.getEventConsumer().accept(event);
    }

    public void setMainMenu() {
        if (menuConfig == null) {
            setInventory(Bukkit.createInventory(new VinGUIHolder(this), 36, Component.empty()));
            getItems().clear();
            getButtons().clear();
            return;
        }

        Component displayName = VinUtils.parseString(menuConfig.getString("DisplayName",""));
        int guiSize = menuConfig.getInt("Size",36);
        setInventory(Bukkit.createInventory(new VinGUIHolder(this), guiSize, displayName));
        getButtons().clear();
        if (!menuConfig.contains("Icons"))
            return;
        ConfigurationSection iconsConfig = menuConfig.getConfigurationSection("Icons");
        if (iconsConfig == null) return;

        for (String iconName : iconsConfig.getKeys(false)) {
            ItemStack guiItem = ItemStack.of(Material.STONE);
            List<Component> guiItemLore = new ArrayList<>();

            if (!menuConfig.contains("Icons."+iconName+".Display"))
                continue;
            ConfigurationSection iconConfig = menuConfig.getConfigurationSection("Icons."+iconName);
            if (iconConfig == null) continue;
            ConfigurationSection iconDisplayConfig = iconConfig.getConfigurationSection("Display");
            if (iconDisplayConfig != null) {
                String itemId = iconDisplayConfig.getString("Id", null);

                int amount = iconDisplayConfig.getInt("Amount", 1);
                if (iconDisplayConfig.contains("Id")) {
                    Material guiMaterial = Material.valueOf(itemId);
                    guiItem = ItemStack.of(guiMaterial, amount);
                }
                else guiItem = ItemStack.of(Material.STONE, amount);

                if (iconDisplayConfig.contains("Name")) {
                    String parsedName = VineriumLib.inst().isPlaceholderAPIEnabled()
                            ? PlaceholderAPI.setPlaceholders(getPlayer(),iconDisplayConfig.getString("Name", ""))
                            : iconDisplayConfig.getString("Name", "");
                    guiItem.setData(DataComponentTypes.CUSTOM_NAME, VinUtils.parseString(parsedName));
                }

                PotionContents.Builder potionContents = guiItem.getType().name().endsWith("POTION") ? PotionContents.potionContents() : null;

                if (iconDisplayConfig.contains("Color") && potionContents != null) {
                    String hexColor = iconDisplayConfig.getString("Color");
                    if (hexColor.length() == 6)
                        potionContents.customColor(Color.fromRGB(Integer.parseInt(hexColor,16)));
                    else if (hexColor.length() == 8)
                        potionContents.customColor(Color.fromARGB(Integer.parseInt(hexColor,16)));
                    else
                        potionContents.customColor(Color.fromRGB(Integer.parseInt("ffffff",16)));
                }
                if (potionContents != null)
                    guiItem.setData(DataComponentTypes.POTION_CONTENTS,potionContents.build());

                if (guiItem.hasData(DataComponentTypes.LORE))
                    guiItemLore.addAll(guiItem.getData(DataComponentTypes.LORE).lines());
                if (iconDisplayConfig.contains("Lore"))
                    if (!guiItemLore.isEmpty())
                        guiItemLore.add(Component.space());
                iconDisplayConfig.getStringList("Lore").forEach(line -> guiItemLore.add(VineriumLib.inst().isPlaceholderAPIEnabled()
                        ? VinUtils.parseString(PlaceholderAPI.setPlaceholders(getPlayer(), line))
                        : VinUtils.parseString(line)
                ));

                if (iconDisplayConfig.contains("ItemModel")) {
                    guiItem.setData(DataComponentTypes.ITEM_MODEL, NamespacedKey.minecraft(iconDisplayConfig.getString("ItemModel")));

                    if (iconDisplayConfig.contains("CustomModelData")) {
                        CustomModelData.Builder customModelData = CustomModelData.customModelData();
                        if (iconDisplayConfig.contains("CustomModelData.Strings"))
                            customModelData.addString(iconDisplayConfig.getString("CustomModelData.Strings",""));
                        if (iconDisplayConfig.contains("CustomModelData.Floats"))
                            customModelData.addFloat(Float.parseFloat(iconDisplayConfig.getString("CustomModelData.Floats","0.0f")));
                        guiItem.setData(DataComponentTypes.CUSTOM_MODEL_DATA,customModelData.build());
                    }
                }
            }

            boolean purchaseCheck = true;
            if (iconConfig.contains("Price")) {
                guiItemLore.add(Component.space());
                if (iconConfig.contains("Price.Money") && VineriumLib.inst().getVaultManager() != null
                && VineriumLib.inst().getVaultManager().getEconomyProvider() != null) {
                    VaultManager vaultManager = VineriumLib.inst().getVaultManager();
                    double price = iconConfig.getDouble("Price.Money");
                    if (vaultManager.getEconomyProvider().getBalance(getPlayer()) >= price)
                        guiItemLore.add(VineriumLib.inst().getLangManager().parseLangString(VineriumLib.inst(),"custom_gui_price_money_true",Double.toString(price)));
                    else {
                        purchaseCheck = false;
                        guiItemLore.add(VineriumLib.inst().getLangManager().parseLangString(VineriumLib.inst(),"custom_gui_price_money_falce",Double.toString(price)));
                    }
                }
                if (purchaseCheck)
                    guiItemLore.add(VineriumLib.inst().getLangManager().parseLangString(VineriumLib.inst(),"custom_gui_purchase_check"));
            }

            guiItem.setData(DataComponentTypes.LORE, ItemLore.lore().addLines(guiItemLore).build());

            if (iconDisplayConfig.contains("Glint")) {
                guiItem.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE,true);
            }
            TooltipDisplay.Builder tooltipDisplay = TooltipDisplay.tooltipDisplay();
            tooltipDisplay.addHiddenComponents(DataComponentTypes.ENCHANTMENTS);
            tooltipDisplay.addHiddenComponents(DataComponentTypes.ATTRIBUTE_MODIFIERS);
            guiItem.setData(DataComponentTypes.TOOLTIP_DISPLAY,tooltipDisplay);

            VinGUIButton actionButton = null;
            if (iconConfig.contains("Actions") && purchaseCheck) {
                actionButton = new VinGUIButton().consumer(event -> performActions(iconName));
            }

            if (iconConfig.contains("Location")) {
                int location = iconConfig.getInt("Location");
                if (location < guiSize)
                    getInventory().setItem(location, guiItem);
                getItems().put(location,guiItem);
                getButtons().put(location,actionButton);
            }
            else if (iconConfig.contains("Locations")) {
                for (String locationString : iconConfig.getStringList("Locations")) {
                    int location = Integer.parseInt(locationString);
                    if (location < guiSize)
                        getInventory().setItem(location, guiItem);
                    getItems().put(location,guiItem);
                    getButtons().put(location,actionButton);
                }
            }
        }
    }

    private void performActions(String iconName) {
        VaultManager vaultManager = VineriumLib.inst().getVaultManager();
        ConfigurationSection iconConfig = menuConfig.getConfigurationSection("Icons."+iconName);
        if (iconConfig == null)
            return;
        if (iconConfig.contains("Price")) {
            boolean purchaseCheck = true;
            ConfigurationSection priceConfig = iconConfig.getConfigurationSection("Price");
            if (priceConfig.contains("Money")) {
                double price = priceConfig.getDouble("Money");
                if (vaultManager != null && vaultManager.getEconomyProvider() != null
                && vaultManager.getEconomyProvider().getBalance(getPlayer()) < price)
                    purchaseCheck = false;
            }
            if (!purchaseCheck) {
                getPlayer().sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumLib.inst(),"custom_gui_purchase_check_deny"));
                setMainMenu();
                openInventory(getPlayer());
                return;
            }

            String[] soundData = VineriumLib.inst().getConfig()
                    .getString("CustomGUI.PurchaseSound", "entity.player.levelup").split(",");
            String purchaseSound = soundData[0];
            float volume = soundData.length > 1 ? Float.parseFloat(soundData[1]) : 1.0f;
            float pitch = soundData.length > 2 ? Float.parseFloat(soundData[2]) : 1.0f;
            getPlayer().playSound(getPlayer(),purchaseSound,SoundCategory.PLAYERS,volume,pitch);

            if (priceConfig.contains("Money") && vaultManager != null && vaultManager.getEconomyProvider() != null)
                vaultManager.getEconomyProvider().withdrawPlayer(getPlayer(),priceConfig.getDouble("Money"));
            getPlayer().sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumLib.inst(),"custom_gui_purchase_success"));
            setMainMenu();
            openInventory(getPlayer());
        }
        ConfigurationSection actionConfig = iconConfig.getConfigurationSection("Actions");
        if (actionConfig == null) return;
        if (actionConfig.contains("Message")) {
            for (String message : actionConfig.getStringList("Message")) {
                String parsedMessage = parseMessagePlaceholders(iconConfig,message);
                getPlayer().sendMessage(VinUtils.parseString(parsedMessage));
            }
        }
        if (actionConfig.contains("CloseMenu")) {
            restoreInventory(getPlayer());
            getPlayer().closeInventory();
        }
        if (actionConfig.contains("ConsoleCommands"))
            for (String command : actionConfig.getStringList("ConsoleCommands")) {
                String commandParsed = parseMessagePlaceholders(iconConfig,command);
                Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), commandParsed);
            }
        if (actionConfig.contains("PlayerCommands"))
            for (String command : actionConfig.getStringList("PlayerCommands")) {
                String commandParsed = parseMessagePlaceholders(iconConfig,command);
                getPlayer().performCommand(commandParsed);
            }
        if (actionConfig.contains("Items")) {
            List<ItemStack> items = new ArrayList<>();
            for (String itemString : actionConfig.getStringList("Items")) {
                String[] itemData = itemString.split(",");
                Optional<Material> material = Enums.getIfPresent(Material.class,itemData[0]);
                if (material.isPresent()) {
                    ItemStack item = ItemStack.of(material.get());
                    if (itemData.length > 1)
                        item.setAmount(Integer.parseInt(itemData[1]));
                }
            }
            if (!getPlayer().give(items).leftovers().isEmpty())
                getPlayer().sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumLib.inst(),"give_leftovers"));
        }
        if (actionConfig.contains("Menu")) {
            @Subst("vineriumlib:menu") String keyString = actionConfig.getString("Menu","");
            NamespacedKey key = NamespacedKey.fromString(keyString);
            CustomGUI customGUI = new CustomGUI(getPlayer(),key);
            if (!customGUI.getConfig().getBoolean("ReplaceInventory",false))
                restoreInventory(getPlayer());
            customGUI.setMainMenu();
            customGUI.openInventory(getPlayer());
        }
    }

    public void replacePlayerInventory() {
        getPlayer().getPersistentDataContainer().set(PENDING_REPLACEMENT_KEY,PersistentDataType.BOOLEAN,true);

        if (getPlayer().getPersistentDataContainer().has(REPLACED_INVENTORY_KEY))
            return;

        int index = 0;
        StringBuilder stringBuilder = new StringBuilder();
        for (ItemStack inventoryItem : getPlayer().getInventory().getContents()) {
            if (inventoryItem != null) {
                stringBuilder.append(";");
                stringBuilder.append(index);
                stringBuilder.append(":");
                stringBuilder.append(Base64.getEncoder().encodeToString(inventoryItem.serializeAsBytes()));
            }
            index++;
        }
        if (!stringBuilder.isEmpty())
            stringBuilder.deleteCharAt(0);
        getPlayer().getPersistentDataContainer().set(REPLACED_INVENTORY_KEY,PersistentDataType.STRING,stringBuilder.toString());
        getPlayer().getInventory().clear();
        int guiSize = menuConfig.getInt("Size",36);
        for (int slot : getItems().keySet()) {
            if (slot < guiSize) continue;
            int parsedSlot = slot - guiSize;
            if (parsedSlot < 9)
                parsedSlot += 9;
            else if (parsedSlot >= 27)
                parsedSlot -= 27;
            getPlayer().getInventory().setItem(parsedSlot,getItems().get(slot).clone());
        }
    }

    public static void restoreInventory(Player player) {
        if (!player.getPersistentDataContainer().has(REPLACED_INVENTORY_KEY)) return;
        player.getInventory().clear();
        String[] itemString = player.getPersistentDataContainer().get(REPLACED_INVENTORY_KEY,PersistentDataType.STRING).split(";");
        player.getPersistentDataContainer().remove(REPLACED_INVENTORY_KEY);
        player.getPersistentDataContainer().remove(PENDING_REPLACEMENT_KEY);
        for (String itemData : itemString) {
            String[] itemInfo = itemData.split(":");
            if (itemInfo[0].isEmpty())
                break;
            int index = Integer.parseInt(itemInfo[0]);
            ItemStack itemStack = ItemStack.deserializeBytes(Base64.getDecoder().decode(itemInfo[1]));
            player.getInventory().setItem(index,itemStack);
        }
    }

    private String parseMessagePlaceholders(ConfigurationSection iconConfig, String message) {
        if (iconConfig.contains("Price.Money") && VineriumLib.inst().getVaultManager() != null && VineriumLib.inst().getVaultManager().getEconomyProvider() != null)
            message = message.replace("%money%",Double.toString(iconConfig.getDouble("Price.Money")));
        if (VineriumLib.inst().isPlaceholderAPIEnabled())
            message = PlaceholderAPI.setPlaceholders(getPlayer(), message);
        return message;
    }
}
