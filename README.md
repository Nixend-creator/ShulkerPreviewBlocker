# ShulkerPreviewBlocker

A custom Spigot/Paper plugin designed to block the opening of Shulker Box *inventories* and the *placement* of Shulker Boxes in the world, unless the player has a specific permission. This helps prevent players using client-side mods (that rely on opening the inventory to view contents) from easily viewing the contents of Shulker Boxes or placing them in restricted areas.

## Features

- Blocks opening Shulker Box inventories (in the world and from player inventory).
- Blocks placing Shulker Boxes in the world.
- Configurable via `config.yml`.
- Toggle plugin functionality on/off.
- Customizable blocked messages.
- Optional logging of blocked attempts.
- Restrict blocking to specific worlds.
- Permission-based bypass system allowing general access or access to own shulkers.
- Command to reload the configuration (`/sppreload`).

## Commands

- `/sppreload` - Reloads the configuration file. Requires `shulker.preview.reload` permission.

## Permissions

- `shulker.preview.bypass` - Allows the player to bypass ALL shulker preview and placement restrictions. Default: `false`
- `shulker.preview.self` - Allows player to interact with shulker boxes in their own inventory and place them. Default: `false`
- `shulker.preview.reload` - Allows the player to reload the config. Default: `op`

## Configuration

The plugin uses a `config.yml` file located in the `plugins/ShulkerPreviewBlocker/` directory after the first run.

```yaml
# Enable/Disable the plugin functionality
enabled: true
# Message sent to player when they try to open a shulker or place one without permission
blocked-message: "§cYou cannot view the contents of this shulker box."
# Message sent when they try to place a shulker specifically
blocked-place-message: "§cYou cannot place this shulker box."
# Log attempts to open shulkers or place them without permission to console
log-attempts: true
# Worlds where the restriction is active.
# Use "ALL" to apply to all worlds.
# Example: ["world", "world_nether"]
restricted-worlds:
  - "ALL"
# Permission required to bypass ALL restrictions
bypass-permission: "shulker.preview.bypass"
# Permission required to interact with shulkers in own inventory and place them
self-permission: "shulker.preview.self"
