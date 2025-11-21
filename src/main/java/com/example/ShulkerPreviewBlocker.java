package com.example;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.block.ShulkerBox;
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

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!config.getBoolean("enabled", true)) {
            return; // Plugin is disabled via config
        }

        if (e.getClickedInventory() == null || !(e.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) e.getWhoClicked();
        World world = player.getWorld();
        ItemStack item = e.getCurrentItem();

        // Check if it's a shulker box in a container inventory (not a chest, shulker, etc. being viewed)
        if (e.getSlotType() == InventoryType.SlotType.CONTAINER && isShulkerBox(item)) {
            List<String> restrictedWorlds = config.getStringList("restricted-worlds");
            boolean isRestrictedWorld = restrictedWorlds.contains("ALL") || restrictedWorlds.contains(world.getName());

            if (isRestrictedWorld) {
                String permission = config.getString("bypass-permission", "shulker.preview.bypass");
                if (!player.hasPermission(permission)) {
                    e.setCancelled(true);

                    // Send blocked message
                    String message = config.getString("blocked-message", "§cВы не можете просматривать содержимое этого шалкера.");
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));

                    // Log attempt if enabled
                    if (config.getBoolean("log-attempts", true)) {
                        logger.info(String.format(
                            "[ShulkerPreviewBlocker] Player %s tried to open a shulker box in world %s without permission.",
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
