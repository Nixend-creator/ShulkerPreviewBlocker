# ShulkerPreviewBlocker

A custom Spigot/Paper plugin designed to block the preview of Shulker Box contents in the player's inventory unless they have a specific permission. This helps prevent players using client-side mods (like JEI, OptiFine, Xaero's) from easily viewing the contents of Shulker Boxes.

## Features

- Blocks Shulker Box previews in the inventory.
- Configurable via `config.yml`.
- Toggle plugin functionality on/off.
- Customizable blocked message.
- Optional logging of blocked attempts.
- Restrict blocking to specific worlds.
- Permission-based bypass system.
- Command to reload the configuration (`/sppreload`).

## Commands

- `/sppreload` - Reloads the configuration file. Requires `shulker.preview.reload` permission.

## Permissions

- `shulker.preview.bypass` - Allows the player to bypass the shulker preview restriction. Default: `false`
- `shulker.preview.reload` - Allows the player to reload the config. Default: `op`

## Configuration

The plugin uses a `config.yml` file located in the `plugins/ShulkerPreviewBlocker/` directory after the first run.

```yaml
# ShulkerPreviewBlocker Configuration

# Enable/Disable the plugin functionality
enabled: true

# Message sent to player when they try to open a shulker without permission
blocked-message: "§cYou cannot view the contents of this shulker box."

# Log attempts to open shulkers without permission to console
log-attempts: true

# Worlds where the restriction is active.
# Use "ALL" to apply to all worlds.
# Example: ["world", "world_nether"]
restricted-worlds:
  - "ALL"

# Permission required to bypass the restriction
bypass-permission: "shulker.preview.bypass"

# Command to reload config
# Use /sppreload
