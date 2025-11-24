package com.nixendcreator; // Обновлённый пакет

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Logger;

public class ShulkerPreviewBlocker extends JavaPlugin implements Listener {

    private FileConfiguration config;
    private Logger logger;

    @Override
    public void onEnable() {
        this.logger = getLogger();
        saveDefaultConfig(); // Creates config.yml if it doesn't exist
        reloadConfig(); // Loads the config
        getServer().getPluginManager().registerEvents(this, this);
        logger.info("ShulkerPreviewBlocker v" + getDescription().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        logger.info("ShulkerPreviewBlocker disabled!");
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        this.config = getConfig();
    }

    // Обработчик события открытия инвентаря
    @EventHandler(priority = EventPriority.HIGHEST) // Высокий приоритет, чтобы перехватить до других плагинов
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (!config.getBoolean("enabled", true)) {
            return; // Plugin is disabled via config
        }

        if (!(e.getPlayer() instanceof Player)) {
            return; // Игнорируем открытие инвентаря не игроком
        }

        Player player = (Player) e.getPlayer();
        World world = player.getWorld();
        Inventory inventory = e.getInventory();

        // Проверяем, открывается ли инвентарь шалкера
        if (inventory.getType() == InventoryType.SHULKER_BOX) {
            List<String> restrictedWorlds = config.getStringList("restricted-worlds");
            boolean isRestrictedWorld = restrictedWorlds.contains("ALL") || restrictedWorlds.contains(world.getName());

            if (isRestrictedWorld) {
                String bypassPermission = config.getString("bypass-permission", "shulker.preview.bypass");
                String selfPermission = config.getString("self-permission", "shulker.preview.self");

                // Проверяем, есть ли у игрока право обойти все ограничения
                if (player.hasPermission(bypassPermission)) {
                    return; // Разрешено
                }

                // --- НОВАЯ ЛОГИКА ---
                // Проверяем, является ли инвентарь, который открывается, инвентарем шалкера, установленного в мире, и принадлежащего игроку.
                org.bukkit.inventory.InventoryHolder holder = inventory.getHolder();
                if (holder instanceof ShulkerBox) {
                    ShulkerBox shulkerBlock = (ShulkerBox) holder;
                    // Проверяем, является ли игрок, открывающий инвентарь, владельцем шалкера в мире
                    if (shulkerBlock.getOwner() != null && shulkerBlock.getOwner().equals(player.getUniqueId())) {
                        // Игрок - владелец шалкера в мире. Проверяем разрешение 'self'.
                        if (player.hasPermission(selfPermission)) {
                            return; // Разрешено открывать свой шалкер в мире
                        }
                        // Если нет разрешения 'self', блокируем.
                    } else {
                        // Игрок НЕ владелец шалкера в мире. Блокируем.
                    }
                } else {
                    // Инвентарь открыт из другого источника (например, из предмета в инвентаре игрока).
                    // В этом случае мы не можем определить владельца шалкера-предмета напрямую через Holder.
                    // Согласно упрощённой логике, если у игрока есть 'self-permission', он может открывать шалкеры из своего инвентаря.
                    if (player.hasPermission(selfPermission)) {
                         return; // Разрешено открывать шалкер из своего инвентаря
                    }
                    // Если нет разрешения 'self', блокируем.
                }
                // Если мы дошли до этой точки, ни bypass, ни self не сработали.
                e.setCancelled(true); // Отменяем открытие инвентаря

                // Send blocked message
                String message = config.getString("blocked-message", "§cYou cannot view the contents of this shulker box.");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));

                // Log attempt if enabled
                if (config.getBoolean("log-attempts", true)) {
                    logger.info(String.format(
                        "[ShulkerPreviewBlocker] Player %s tried to open a shulker box inventory in world %s without permission.",
                        player.getName(), world.getName()
                    ));
                }
            }
        }
    }

    // Обработчик события клика по инвентарю - проверяем, открывается ли шалкер из инвентаря игрока
    @EventHandler(priority = EventPriority.HIGHEST) // Высокий приоритет
    public void onInventoryClick(InventoryClickEvent e) {
        if (!config.getBoolean("enabled", true)) {
            return; // Plugin is disabled via config
        }

        if (!(e.getWhoClicked() instanceof Player)) {
            return; // Игнорируем клики не игроком
        }

        Player player = (Player) e.getWhoClicked();
        World world = player.getWorld();
        ItemStack clickedItem = e.getCurrentItem();

        // Проверяем, что кликнутый инвентарь - это инвентарь игрока (где находятся слоты шлема, нагрудника, ботинок, основной инвентарь, горячая панель).
        // И что клик - правой кнопкой, который может открыть инвентарь шалкера.
        // Bukkit вызывает InventoryClickEvent перед InventoryOpenEvent.
        // Мы отменяем клик, который приведёт к открытию инвентаря шалкера из инвентаря игрока, если у игрока нет разрешения bypass или self.
        if (e.getClickedInventory() != null && e.getClickedInventory().getType() == InventoryType.PLAYER) {
            if (isShulkerBox(clickedItem)) {
                 List<String> restrictedWorlds = config.getStringList("restricted-worlds");
                 boolean isRestrictedWorld = restrictedWorlds.contains("ALL") || restrictedWorlds.contains(world.getName());

                 if (isRestrictedWorld) {
                     String bypassPermission = config.getString("bypass-permission", "shulker.preview.bypass");
                     String selfPermission = config.getString("self-permission", "shulker.preview.self");

                     // Проверяем, есть ли у игрока право обойти все ограничения ИЛИ право на свои шалкеры
                     if (!player.hasPermission(bypassPermission) && !player.hasPermission(selfPermission)) {
                         // Отменяем клик, который может привести к открытию инвентаря шалкера
                         e.setCancelled(true);

                         // Send blocked message
                         String message = config.getString("blocked-message", "§cYou cannot view the contents of this shulker box.");
                         player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));

                         // Log attempt if enabled
                         if (config.getBoolean("log-attempts", true)) {
                             logger.info(String.format(
                                 "[ShulkerPreviewBlocker] Player %s tried to open a shulker box in their inventory in world %s without permission.",
                                 player.getName(), world.getName()
                             ));
                         }
                     }
                 }
            }
        }
    }

    // Обработчик события установки блока - проверяем, устанавливает ли игрок шалкер-бокс
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!config.getBoolean("enabled", true)) {
            return; // Plugin is disabled via config
        }

        Player player = e.getPlayer();
        World world = player.getWorld();
        ItemStack placedItem = e.getItemInHand();

        // Проверяем, является ли устанавливаемый блок шалкер-боксом
        if (isShulkerBox(placedItem)) {
            List<String> restrictedWorlds = config.getStringList("restricted-worlds");
            boolean isRestrictedWorld = restrictedWorlds.contains("ALL") || restrictedWorlds.contains(world.getName());

            if (isRestrictedWorld) {
                String bypassPermission = config.getString("bypass-permission", "shulker.preview.bypass");
                String selfPermission = config.getString("self-permission", "shulker.preview.self");

                // Проверяем, есть ли у игрока право обойти все ограничения ИЛИ право ставить свои шалкеры
                if (!player.hasPermission(bypassPermission) && !player.hasPermission(selfPermission)) {
                    e.setCancelled(true); // Отменяем установку блока

                    // Send blocked message
                    String message = config.getString("blocked-place-message", "§cYou cannot place this shulker box.");
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));

                    // Log attempt if enabled
                    if (config.getBoolean("log-attempts", true)) {
                        logger.info(String.format(
                            "[ShulkerPreviewBlocker] Player %s tried to place a shulker box in world %s without permission.",
                            player.getName(), world.getName()
                        ));
                    }
                }
            }
        }
    }

    private boolean isShulkerBox(ItemStack item) {
        if (item == null || item.getType() == null) return false;

        Material type = item.getType();
        return type.name().endsWith("_SHULKER_BOX");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("sppreload")) {
            if (sender.hasPermission("shulker.preview.reload")) {
                reloadConfig();
                sender.sendMessage(ChatColor.GREEN + "ShulkerPreviewBlocker config reloaded.");
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
        }
        return false;
    }
}
