# Meteor-Module

[![Release](https://img.shields.io/github/v/release/gabrielsk12/Meteor-Module)](https://github.com/gabrielsk12/Meteor-Module/releases/latest)
[![Build](https://img.shields.io/github/actions/workflow/status/gabrielsk12/Meteor-Module/release.yml)](https://github.com/gabrielsk12/Meteor-Module/actions)

AI-powered Meteor Client add-on modules with legit behavior, pathfinding bots, and ML integrations.

## Features

### 🤖 Automation Bots
- **LawnBot**: Clears grass/flowers/snow/leaves with legit pacing and tool selection
- **GardenBot**: A* pathfinding-enabled farming bot (harvest/replant with human-like movement)
- **TorchGridBot**: Grid-based torch placement with combined light evaluation and optional pathfinding
- **WarehouseBot**: Finds storage (chests/barrels), pathfinds there, and deposits items with keep-filters
- **EverythingBot**: Multi-task orchestrator (lawn, torch, farm, mine)

### 🎯 Legit Behavior
- Human-like pacing and reaction times (Humanizer)
- Line-of-sight and FOV checks (Visibility)
- Gentle velocity control and rotations (PlayerMovement)
- Per-module action throttling (LegitController)

### 🧠 AI Systems (Advanced)
- Behavior Trees for decision-making
- GOAP (Goal-Oriented Action Planning)
- A* pathfinding with octile heuristic and safety constraints
- ML integration (PlayerBehaviorLearner with GPU acceleration support)

### ⚔️ Combat Enhancements
- Advanced Criticals system with AI/ML strategy selection
- Legit-aware KillAura, Velocity, Surround, AutoTotem, AdvancedCrystalAura

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
