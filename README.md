# Minecart Upgrade

Minecart Upgrade is a Fabric mod for Minecraft 1.21.10 that raises the maximum speed minecarts can travel on rails. The speed cap is now configurable at runtime through a gamerule and a config file that always stay in sync.

## Configure

- Gamerule: `minecartMaxSpeed` (measured in blocks per second, default 12). Change it with e.g. `/gamerule minecartMaxSpeed 18.5` and the config file and all minecarts will update immediately.
- Config file: `config/minecartupgrade.json` has a single field `maxBlocksPerSecond`. Editing this before launching a world will push the value into the gamerule and all minecarts when the server starts.

## Install

The mod runs like any Fabric mod, just download and pop it in your mods folder.

## Version

The mod is made for v1.21.10, and has backwards compatibility for 1.20.1 and 1.21.1 found on Modrinth.
https://modrinth.com/mod/minecart-upgrade
