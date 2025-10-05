# 🚀 Gabriel_SK Meteor Modules - Survival SMP Edition

[![Release](https://img.shields.io/github/v/release/gabrielsk12/Meteor-Module)](https://github.com/gabrielsk12/Meteor-Module/releases/latest)
[![Build](https://img.shields.io/github/actions/workflow/status/gabrielsk12/Meteor-Module/release.yml)](https://github.com/gabrielsk12/Meteor-Module/actions)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen)](https://www.minecraft.net/)
[![License](https://img.shields.io/github/license/gabrielsk12/Meteor-Module)](LICENSE)

**16 powerful modules** designed for **Survival SMP** gameplay with smart automation and legit behavior.

## 🎯 Quick Start

1. **Download** the latest [meteor-modules-1.1.0.jar](https://github.com/gabrielsk12/Meteor-Module/releases/latest)
2. **Install** Meteor Client 0.5.7+ for Minecraft 1.21.1
3. **Drop** the JAR into `.minecraft/mods/`
4. **Launch** and find modules in the "Gabriel_SK" category

## ✨ Modules (16 Total)

### 🤖 Automation (11 modules)
- **AutoMiner** ⛏️ - Smart ore mining (diamonds, debris, emeralds, etc.)
- **AutoFish** 🎣 - AFK fishing with perfect timing
- **TreeChopper** 🪓 - Chop entire trees instantly
- **AutoBreed** 🐄 - Automatic animal breeding
- **AutoTotem** 💚 - Keep totem in offhand always
- **GardenBot** 🌾 - A* pathfinding farming bot
- **LawnBot** 🌿 - Clean grass/flowers/snow
- **TorchGridBot** 🔥 - Prevent spawns with torch grids
- **WarehouseBot** 📦 - Auto-sort items to chests
- **EverythingBot** 🎯 - Multi-task automation
- **AutoEat** 🍖 - Eat when hungry

### 🛠️ Utilities (5 modules)
- **AutoTool** 🔧 - Best tool auto-switching
- **Replenish** ♻️ - Auto-refill hotbar
- **AutoLog** 🚨 - Emergency disconnect
- **DeathPosition** 💀 - Save death coords
- **AutoRespawn** ⚡ - Auto respawn

📖 **[Full Feature Documentation →](FEATURES.md)**

## 🎮 Recommended Combos

**Mining Session:**
```
AutoMiner + AutoTool + Replenish + AutoTotem
```

**AFK Fishing:**
```
AutoFish + AutoEat + AutoLog + DeathPosition
```

**Base Maintenance:**
```
LawnBot + TorchGridBot + WarehouseBot
```

## 🔒 Legit Features
- ✅ Human-like delays (100-600ms)
- ✅ Line of sight checks
- ✅ Natural movement patterns
- ✅ A* pathfinding
- ✅ Random timing jitter

## Installation

1. Download the latest release JAR from [Releases](https://github.com/gabrielsk12/Meteor-Module/releases)
2. Place in your `.minecraft/mods/` folder alongside Meteor Client
3. Launch Minecraft with Fabric loader

## Build

```bash
git clone https://github.com/gabrielsk12/Meteor-Module.git
cd Meteor-Module
./gradlew build
```

**Note**: Full compilation currently requires API compatibility updates for the latest Meteor Client. The CI workflow packages source JARs for releases.

## Configuration

Each bot has extensive settings:
- **Radius**: scan/operation range
- **Legit Mode**: enable human-like behavior
  - `require-los`: line-of-sight checks
  - `action-delay-ms`: delay between actions
  - `action-jitter`: randomization factor (0-1)
  - `walk-speed`: movement speed (for pathfinding bots)
- **Filters** (WarehouseBot): keep tools/weapons, food, building blocks

## License

MIT License - see [LICENSE](LICENSE)

**Disclaimer**: Provided as-is. Review your server's rules and network policies before use. Automation and client modifications may violate server terms of service.
