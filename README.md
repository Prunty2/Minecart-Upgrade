# 🚂 Minecart Upgrade

> **Transform your Minecraft rail systems into high-speed transit networks!**

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Mod%20Loader-Fabric-blue)](https://fabricmc.net/)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21+-orange)](https://adoptium.net/)

**Minecart Upgrade** is a lightweight Fabric mod that revolutionizes minecart travel in Minecraft. Say goodbye to the vanilla speed limit and experience the thrill of high-speed rail travel with intelligent physics handling that keeps your carts safely on track!

---

## 📋 Table of Contents

- [✨ Features](#-features)
- [🎮 How It Works](#-how-it-works)
- [⚙️ Configuration](#️-configuration)
- [📥 Installation](#-installation)
- [🔧 Technical Details](#-technical-details)
- [❓ FAQ](#-faq)
- [📝 License](#-license)

---

## ✨ Features

### 🚀 Configurable Maximum Speed
- Raise the default minecart speed limit from **8 blocks/second** to whatever you desire
- Default mod speed: **12 blocks/second** (50% faster than vanilla!)
- Adjust speeds in real-time without restarting your game or world

### 🛤️ Smart Rail Handling
- **Automatic Re-railing**: Minecarts intelligently snap back onto rails if they start to derail at high speeds
- **Direction Locking**: Prevents minecarts from unexpectedly reversing direction
- **Curve & Slope Protection**: Automatically reduces speed on curves and slopes when traveling above 25 blocks/second to prevent derailment

### 🔥 Furnace Minecart Integration
- Furnace minecarts benefit from the increased speed cap too
- **Push Boost System**: Regular minecarts pushed by furnace minecarts receive a temporary speed boost lasting 1 second (20 ticks)
- Allows for train-like behavior with powered furnace carts pushing cargo

### 💧 Water Handling
- Minecarts automatically reduce to **50% speed** when passing through water
- Prevents unrealistic behavior and maintains gameplay balance

### 🔄 Live Synchronization
- Game rule and config file are always kept in sync
- Changes apply instantly to all minecarts in loaded chunks
- No world restart required!

---

## 🎮 How It Works

### Basic Usage

1. **Install the mod** (see [Installation](#-installation))
2. **Launch your world** - the mod works immediately with default settings
3. **Build your rail systems** as normal - minecarts will now travel faster!

### Adjusting Speed In-Game

Use the game rule command to change the maximum speed on the fly:

```
/gamerule minecartMaxSpeed <speed>
```

**Examples:**
| Command | Result |
|---------|--------|
| `/gamerule minecartMaxSpeed 8` | Vanilla speed |
| `/gamerule minecartMaxSpeed 12` | Default mod speed (50% faster) |
| `/gamerule minecartMaxSpeed 20` | Rapid transit speed |
| `/gamerule minecartMaxSpeed 50` | Extreme speed (for the brave!) |

> 💡 **Tip**: The speed is measured in **blocks per second**. Vanilla Minecraft caps minecarts at 8 blocks/second.

### Building High-Speed Rail Lines

For the best experience at high speeds:

| Recommendation | Reason |
|----------------|--------|
| ✅ Use **straight rails** | Maximum speed potential |
| ✅ Use **powered rails** frequently | Maintain momentum |
| ⚠️ Be cautious with **curves** | Auto-limited to 25 b/s |
| ⚠️ Be cautious with **slopes** | Auto-limited to 25 b/s |
| 🔥 Use **furnace minecarts** | Can push other carts at high speed |

---

## ⚙️ Configuration

### Game Rule (Recommended)

The easiest way to configure the mod in-game:

```
/gamerule minecartMaxSpeed <blocksPerSecond>
```

- **Parameter**: Speed in blocks per second (decimal values supported, e.g., `18.5`)
- **Default**: `12.0`
- **Minimum**: `0.1`
- **Changes apply instantly** to all loaded minecarts

### Config File

Located at: `config/minecartupgrade.json`

```json
{
  "maxBlocksPerSecond": 12.0
}
```

#### Config File Behavior:

| Scenario | Behavior |
|----------|----------|
| Edit before launching world | Value is loaded and synced to game rule |
| Edit while world is running | Changes won't apply until next server start |
| Change game rule in-game | Config file updates automatically |

> 🔄 **Sync Guarantee**: The game rule and config file are always kept in sync. Changing one will update the other!

---

## 📥 Installation

### Requirements

| Requirement | Version |
|-------------|---------|
| Minecraft | 1.21.1 |
| Fabric Loader | ≥0.15.11 |
| Fabric API | Any compatible version |
| Java | 21 or higher |

### Steps

1. **Install Fabric Loader** for Minecraft 1.21.1
   - Download from [fabricmc.net](https://fabricmc.net/use/installer/)
   
2. **Install Fabric API**
   - Download from [Modrinth](https://modrinth.com/mod/fabric-api) or [CurseForge](https://www.curseforge.com/minecraft/mc-mods/fabric-api)

3. **Download Minecart Upgrade**
   - Get the latest release for your Minecraft version

4. **Install the mod**
   - Place both `.jar` files in your `mods` folder:
     - Windows: `%appdata%\.minecraft\mods`
     - macOS: `~/Library/Application Support/minecraft/mods`
     - Linux: `~/.minecraft/mods`

5. **Launch Minecraft** with the Fabric profile and enjoy! 🎉

---

## 🔧 Technical Details

### Speed Limiting System

The mod implements intelligent speed limiting to prevent physics issues:

| Track Type | Speed Behavior |
|------------|----------------|
| Straight rails | Full configured speed |
| Curved rails | Capped at 25 b/s if configured speed exceeds it |
| Ascending/Descending rails | Capped at 25 b/s if configured speed exceeds it |
| Rails in water | 50% of normal speed |

### Re-railing Algorithm

When a minecart would normally derail:

1. **Direction Detection**: The mod remembers the last valid rail direction
2. **Rail Search**: Scans ahead (up to 48 blocks based on speed) for valid rails
3. **Smart Snapping**: Projects the minecart onto the nearest valid rail segment
4. **Velocity Preservation**: Maintains speed and aligns direction to the new track

### Furnace Minecart Boost

When a furnace minecart collides with another minecart:
- The pushed cart receives a **20-tick boost window**
- During this window, the cart can travel at the full configured speed
- Allows train-style configurations with a furnace cart pushing cargo carts

---

## ❓ FAQ

<details>
<summary><b>Q: Will this work on servers?</b></summary>

**A:** Yes! The mod works on both client and server. For multiplayer, only the server needs the mod installed. Clients will automatically benefit from the increased speeds.
</details>

<details>
<summary><b>Q: Do minecarts derail at high speeds?</b></summary>

**A:** The mod includes smart re-railing technology that keeps minecarts on track. However, at extremely high speeds (50+ b/s), complex track layouts may still cause issues. The mod automatically limits speed on curves and slopes to 25 b/s to prevent most derailments.
</details>

<details>
<summary><b>Q: Can I use this with other minecart mods?</b></summary>

**A:** Generally yes, but compatibility depends on the other mod. If another mod also modifies minecart physics, there may be conflicts. Test in a development world first!
</details>

<details>
<summary><b>Q: Why is my speed capped at 25 b/s on curves?</b></summary>

**A:** This is a safety feature! At speeds above 25 blocks/second, minecarts can behave erratically on curves and slopes. The mod automatically reduces speed in these situations to keep your cart on the rails.
</details>

<details>
<summary><b>Q: Can I disable the mod for specific worlds?</b></summary>

**A:** You can set the game rule to vanilla speed (`/gamerule minecartMaxSpeed 8`) for a vanilla-like experience while keeping the mod installed.
</details>

<details>
<summary><b>Q: Will this affect performance?</b></summary>

**A:** The mod is very lightweight and should have negligible impact on performance, even with many minecarts.
</details>

---

## 📝 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

```
MIT License - Copyright (c) 2025 Liam
```

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome! Feel free to check the issues page.

---

## 💜 Support

If you enjoy this mod, consider:
- ⭐ Starring the repository
- 🐛 Reporting bugs you find
- 💡 Suggesting new features
- 📢 Sharing with friends!

---

<div align="center">

**Made with ❤️ for the Minecraft community**

🚂 *Happy railroading!* 🛤️

</div>
