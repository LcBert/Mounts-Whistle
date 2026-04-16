# Mounts Whistle

`Mounts Whistle` is a mod that adds a whistle capable of binding a mount, spawning or despawning it, and optionally auto-riding it.

## Overview

- One whistle can be bound to one mount at a time.
- The whistle works with the mounts enabled in the configuration.
- It supports toggling Auto-Ride.
- It can automatically equip a saddle and apply mount attribute modifiers.

## Usage

### Binding a mount

1. Hold the whistle in your hand.
2. **Right-click** a supported mount that is not already tamed.
3. The whistle stores the mount and its main data.

### Spawning or despawning the mount

- Press the default keybind **V** to spawn or despawn the bound mount if it is in the curios slot.
- Alternatively, use the whistle with **right-click in the air**.

### Auto-Ride

- Press **Shift + right-click** with the whistle in hand to enable or disable Auto-Ride.
- When enabled, the player automatically mounts the summoned mount.


## Configuration

The server configuration is saved in:

```text
config/mounts_whistle_server.json
```

Main options available:

- `mountsList`: list of mounts supported by the whistle (leave blank to all mounts).
- `protection.enableAutoRide`: enables or disables Auto-Ride.
- `protection.whistleShare`: allows or disallows sharing the whistle between players.
- `protection.onlyRideOwner`: allows only the owner to ride their mount.
- `protection.mountInvulnerable`: makes the mount invulnerable.
- `despawn.despawnDistance`: maximum distance used for despawn mount.
- `despawn.despawnTime`: despawn time in seconds.
- `inventory.equipSaddle`: automatically equips a saddle when the mount is summoned.
- `inventory.dropSaddle`: drops the saddle on despawn or death.
- `inventory.dropArmor`: drops the armor on despawn or death.
- `attributeModifier.baseSpeedAttribute`: base speed value for summoned mounts.
- `attributeModifier.baseJumpAttribute`: base jump value for summoned mounts.
- `attributeModifier.enchantModifier.speedModifier`: speed bonus from enchantments.
- `attributeModifier.enchantModifier.jumpModifier`: jump bonus from enchantments.

## Enchantments
Mod add 2 enchantments for the whistle with max level 3:
- `speed_enchant`: increases the speed of the summoned mount.
- `jump_enchant`: increases the jump of the summoned mount.

## Version

- Mod: `0.3`
- Mod ID: `mounts_whistle`
- Author: `lucab`
- License: `MIT`
