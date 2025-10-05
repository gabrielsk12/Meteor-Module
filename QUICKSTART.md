# Gabriel_SK ULTIMATE v3.0 - Quick Start Guide

## 🚀 Easy Setup (5 Minutes)

### Step 1: Installation
1. **Download Meteor Client** (if not installed)
   - Get it from: https://meteorclient.com/
   - Install for your Minecraft version

2. **Place Gabriel_SK addon**
   ```
   .minecraft/
   └── meteor/
       └── addons/
           └── gabrielsk-ultimate-3.0.jar  ← Place here
   ```

3. **Launch Minecraft** with Meteor

### Step 2: First Launch
When you launch, you'll see:
```
╔════════════════════════════════════════╗
║  Gabriel_SK ULTIMATE v3.0 ULTIMATE    ║
║  Initialization Complete!             ║
╠════════════════════════════════════════╣
║  Modules Loaded: 96+                  ║
║  Load Time: ~150ms                    ║
║  AI Systems: ONLINE                   ║
║  Optimization: ENABLED                ║
╠════════════════════════════════════════╣
║  Features:                            ║
║  ✓ Behavior Tree AI                   ║
║  ✓ GOAP Planning                      ║
║  ✓ A* Pathfinding                     ║
║  ✓ Mathematical Optimization          ║
║  ✓ Player-like Movement               ║
║  ✓ Prediction Algorithms              ║
╠════════════════════════════════════════╣
║  Type .gshelp for command list        ║
║  Type .gsinfo for module info         ║
╚════════════════════════════════════════╝
Ready to dominate! 🔥
```

## 📋 Module Categories (Easy to Find!)

### Open GUI: Press `Right Shift`

All modules organized in categories:

```
Gabriel_SK Category
├── 🗡️ Combat (15 modules)
├── 🚀 Travel (15 modules)
├── 🔍 Hunting (10 modules)
├── 🛡️ Safety (12 modules)
├── 🔧 Utility (15 modules)
├── 🔒 Security (8 modules)
├── 📦 Packet Control (3 modules)
├── 🤖 AI & Detection (2 modules)
├── 👁️ Render (10+ modules)
└── ⚙️ Automation (10+ modules)
```

## 🎯 Quick Scenarios

### Scenario 1: PvP Combat (Crystal PvP)
**Goal**: Dominate in crystal PvP fights

**Enable these modules**:
1. Open GUI (`Right Shift`)
2. Navigate to `Gabriel_SK` → `Combat`
3. Enable:
   - ✅ `Advanced Crystal Aura` (perfect crystal placement)
   - ✅ `Auto Totem Plus` (automatic totem switching)
   - ✅ `Surround Plus` (auto surround with obsidian)
   - ✅ `Offhand Switch` (auto crystal/totem switching)

**Settings to adjust**:
- `Advanced Crystal Aura`:
  - Target Range: `12` blocks
  - Min Damage: `6` HP
  - Max Self Damage: `6` HP
  - Anti-Suicide: `ON`
  - Smooth Rotations: `ON` (looks human)
  
**Commands**:
```
.legit              - Set safe values (won't get kicked)
.panic              - Emergency disable all
```

**Result**: You'll automatically:
- Place crystals to damage enemies
- Break crystals at perfect timing
- Surround yourself when in danger
- Switch totems when low health
- All with smooth, human-like movements!

---

### Scenario 2: Fast Travel (800+ km/h)
**Goal**: Travel super fast across the server

**Enable these modules**:
1. `Gabriel_SK` → `Travel`
2. Enable:
   - ✅ `ElytraFly Ultimate` (if you have elytra)
   - ✅ `Entity Speed` (if riding horse/boat)
   - ✅ `Speed` (for ground movement)
   - ✅ `No Fall` (prevent fall damage)

**Settings**:
- `ElytraFly Ultimate`:
  - Mode: `Control` or `Packet`
  - Speed: `800` km/h (yes, really!)
  - Anti-Kick: `ON`
  
- `Entity Speed`:
  - Speed: `10x` (default is safe)
  
**Commands**:
```
.autowalk           - Walk in straight line
.highway build      - Build highways automatically
```

**Result**: Travel at insane speeds without getting kicked!

---

### Scenario 3: Base Hunting (Find Bases)
**Goal**: Find hidden bases and stashes

**Enable these modules**:
1. `Gabriel_SK` → `Hunting`
2. Enable:
   - ✅ `Chunk Trail` (track player movements)
   - ✅ `New Chunks` (find recently generated chunks)
   - ✅ `Stash Finder` (locate chest clusters)
   - ✅ `Portal Tracker` (track nether portals)
   
3. `Gabriel_SK` → `Render`
4. Enable:
   - ✅ `Player ESP` (see players through walls)
   - ✅ `Chest ESP` (see chests through walls)
   - ✅ `Hole ESP` (find PvP holes)

**Settings**:
- `New Chunks`:
  - Detection Range: `256` blocks
  - Alert Sound: `ON`
  
- `Stash Finder`:
  - Min Chests: `5` (adjust based on server)
  - Search Range: `128` blocks

**Commands**:
```
.track <player>     - Track specific player
.logspot find       - Find logout spots
```

**Result**: You'll find bases that others can't!

---

### Scenario 4: Safety & Anti-Detection
**Goal**: Don't get detected by anticheats/staff

**ALWAYS ENABLE**:
1. `Gabriel_SK` → `Packet Control`
   - ✅ `Packet Controller` (prevents server-side flags)
   - ✅ `Anti-Kick` (prevents automatic kicks)
   - ✅ `Packet Throttler` (look more human)

2. `Gabriel_SK` → `Safety`
   - ✅ `Staff Detector` (alerts when staff join/approach)
   - ✅ `Coord Protect` (hide coords in screenshots)
   - ✅ `Anti-Chunk Ban` (prevent chunk-based crashes)

3. `Gabriel_SK` → `AI & Detection`
   - ✅ `Spectator Detector` (detect spectator mode)

**Settings**:
- `Staff Detector`:
  - Detection Range: `64` blocks
  - Auto-Panic: `ON`
  - Show Distance: `ON`
  
- `Packet Controller`:
  - Auto-Correct: `ON`
  - Max Speed: `Safe` (won't flag)

**Commands**:
```
.panic              - Instantly disable everything
.legit              - Switch to safe values
.staffalert add <name> - Add staff member to watch list
```

**Result**: You'll be alerted before staff sees you!

---

### Scenario 5: AI Bot (Automatic Player)
**Goal**: Let AI play for you (AFK grinding, combat, exploration)

**Enable**:
1. `Gabriel_SK` → `AI & Detection`
   - ✅ `Advanced AI Bot`

**Settings**:
- `Advanced AI Bot`:
  - Combat Enabled: `ON` (attacks enemies)
  - Exploration Enabled: `ON` (explores world)
  - Survival Enabled: `ON` (eats food, avoids danger)
  - Attack Range: `3.5` blocks
  - Detection Range: `32` blocks

**What the AI does**:
- ✅ Uses **Behavior Trees** for decision making
- ✅ Uses **GOAP** for dynamic planning
- ✅ Uses **A* Pathfinding** for navigation
- ✅ Fights enemies with strafing and combat strategies
- ✅ Avoids lava, water, cliffs (survival instincts)
- ✅ Explores intelligently (not just random walking)
- ✅ Eats food when hungry
- ✅ Retreats when low health
- ✅ Looks around like a real player

**This is TRUE AI - not hardcoded!** It adapts to any situation dynamically.

---

## 🎮 Essential Commands

### Quick Reference
```
.gshelp                    - Show all commands
.gsinfo                    - Show module information
.panic                     - Emergency disable all
.legit                     - Safe values (no kick)
.staffalert add <name>     - Watch for staff
.staffalert list           - List watched staff
.track <player>            - Track player
.autowalk                  - Auto walk
.dupe                      - Show dupe methods
.performance <mode>        - Set performance mode
```

### Performance Modes
```
.performance extreme       - +100-300 FPS (minimal graphics)
.performance high          - +50-150 FPS (balanced)
.performance balanced      - +30-80 FPS (good quality)
.performance quality       - +15-40 FPS (max quality)
```

### Safety Commands
```
.panic                     - Disable ALL modules instantly
.panic restore             - Re-enable modules
.panic save                - Save current state
.disconnect                - Safe disconnect
```

### Profile System
```
.profile save legit        - Save current as "legit" profile
.profile save hvh          - Save current as "hvh" profile
.profile load legit        - Load "legit" profile
.profile load hvh          - Load "hvh" profile
.profile list              - List all profiles
```

---

## 🔧 Recommended Configurations

### Config 1: Legit (Won't Get Kicked)
**For servers with anticheats**
```
Combat:
- Crystal Aura: Place Delay 2, Break Delay 1
- Kill Aura: Cooldown 100%, Range 3.5

Travel:
- Speed: Mode Vanilla, Speed 1.0x
- Flight: Disabled (too obvious)
- ElytraFly: Speed 100 km/h (safe)

Safety:
- Packet Controller: AUTO
- Anti-Kick: ON
- Staff Detector: ON
```

### Config 2: HvH (High vs High)
**For anarchy servers (2b2t style)**
```
Combat:
- Crystal Aura: Delay 0, Multi-Place ON
- Auto Totem: Instant
- Surround: Instant

Travel:
- ElytraFly: 800+ km/h
- Entity Speed: 10x
- Packet Fly: ON

Hunting:
- All ESP modules: ON
- Detection Range: MAX
```

### Config 3: Exploration
**For base hunting and exploration**
```
Travel:
- ElytraFly: 500 km/h (balance speed/safety)
- Auto Walk: ON
- Highway Builder: ON

Hunting:
- New Chunks: ON
- Chunk Trail: ON
- Stash Finder: ON
- All ESP: ON

Safety:
- Auto Eat: ON
- Anti-Void: ON
- Coord Protect: ON
```

---

## 💡 Pro Tips

### Tip 1: Keybinds
Set up keybinds for quick access:
```
- Panic Button: Delete key
- Crystal Aura: R key
- Auto Totem: T key
- ElytraFly: G key
```

### Tip 2: Always Use Safety
**NEVER forget these**:
- ✅ Packet Controller (prevents flags)
- ✅ Anti-Kick (prevents disconnects)
- ✅ Staff Detector (alerts before ban)

### Tip 3: Performance
If laggy:
1. `.performance extreme` - Max FPS mode
2. Disable unnecessary ESP modules
3. Lower detection ranges

### Tip 4: Profiles
Save different configs:
```
.profile save legit    - For servers with anticheats
.profile save hvh      - For anarchy/PvP
.profile save explore  - For base hunting
```

Switch instantly:
```
.profile load legit    - Switch to legit config
```

### Tip 5: AI Bot
Let AI play for you:
1. Enable `Advanced AI Bot`
2. Set combat/exploration/survival settings
3. Go AFK - AI will handle everything!

The AI uses:
- Behavior Trees (decision making)
- GOAP (planning)
- A* Pathfinding (navigation)
- Prediction algorithms (combat)
- Human-like movement (looks real)

---

## 🆘 Troubleshooting

### Problem: Getting Kicked
**Solution**:
1. `.legit` - Use safe values
2. Enable `Packet Controller`
3. Enable `Anti-Kick`
4. Lower ranges (Speed, Reach, etc.)

### Problem: Low FPS
**Solution**:
1. `.performance extreme` - Max FPS mode
2. Disable ESP modules you don't need
3. Lower render distances in settings

### Problem: Not Dealing Damage (Crystal Aura)
**Solution**:
1. Check `Min Damage` setting (lower it)
2. Check `Max Self Damage` (increase it)
3. Disable `Anti-Suicide` temporarily
4. Make sure you have crystals in hotbar

### Problem: Staff Approaching
**Solution**:
1. `.panic` - Instant disable all
2. Or let `Staff Detector` auto-panic
3. `/disconnect` if too late

### Problem: Module Not Working
**Solution**:
1. Check if it's enabled (GUI)
2. Check keybind conflicts
3. Check settings (some require specific values)
4. Restart Minecraft

---

## 📞 Support

### Get Help
- Discord: [Community Server Link]
- GitHub Issues: [Repository Link]
- Wiki: [Documentation Link]

### Report Bugs
Please include:
1. Module name
2. Settings used
3. Error message (if any)
4. Steps to reproduce

---

## 🎉 You're Ready!

Now you know:
- ✅ How to install
- ✅ How to use each module category
- ✅ Essential commands
- ✅ Recommended configs
- ✅ Pro tips and tricks
- ✅ Troubleshooting

**Go dominate! 🔥**

Remember:
- Use `.panic` for emergencies
- Use `.legit` for safe values
- Enable `Packet Controller` always
- Save your configs with `.profile save`

---

**Gabriel_SK ULTIMATE v3.0** - Making Minecraft easy since 2025 😎
