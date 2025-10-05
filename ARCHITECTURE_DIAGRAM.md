# Gabriel_SK ULTIMATE v3.0 - System Architecture Diagram

```
═══════════════════════════════════════════════════════════════════════════════
                        GABRIEL_SK ULTIMATE v3.0
                    Mathematically Perfect | AI-Powered
═══════════════════════════════════════════════════════════════════════════════

┌─────────────────────────────────────────────────────────────────────────────┐
│                            USER INTERFACE LAYER                             │
├─────────────────────────────────────────────────────────────────────────────┤
│  Meteor GUI                Commands                  Keybinds                │
│  (Right Shift)             (.panic, .legit, etc)     (Customizable)         │
│                                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │   Combat     │  │    Travel    │  │   Hunting    │  │    Safety    │  │
│  │  15 modules  │  │  15 modules  │  │  10 modules  │  │  12 modules  │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │   Utility    │  │   Security   │  │    Packet    │  │      AI      │  │
│  │  15 modules  │  │   8 modules  │  │   3 modules  │  │   2 modules  │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         MODULE MANAGEMENT LAYER                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                          GabrielSKAddon.java                                │
│  • Module registration & lifecycle                                          │
│  • Settings management                                                       │
│  • Event distribution                                                        │
│  • Background optimization thread                                            │
│  • Performance tracking                                                      │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                    ┌─────────────────┼─────────────────┐
                    │                 │                 │
                    ▼                 ▼                 ▼
┌──────────────────────┐  ┌──────────────────────┐  ┌──────────────────────┐
│   AI SYSTEMS LAYER   │  │   UTILITIES LAYER    │  │  MATHEMATICAL LAYER  │
├──────────────────────┤  ├──────────────────────┤  ├──────────────────────┤
│                      │  │                      │  │                      │
│  Behavior Trees      │  │  PlayerMovement      │  │  MathUtils          │
│  ┌────────────────┐  │  │  ┌────────────────┐  │  │  ┌────────────────┐ │
│  │ Selector       │  │  │  │ Smooth Rotate  │  │  │  │ Fast Trig      │ │
│  │ Sequence       │  │  │  │ Strafe Around  │  │  │  │ Fast InvSqrt   │ │
│  │ Parallel       │  │  │  │ Human Movement │  │  │  │ Interpolation  │ │
│  │ Decorators     │  │  │  │ Reaction Time  │  │  │  │ Prediction     │ │
│  └────────────────┘  │  │  └────────────────┘  │  │  └────────────────┘ │
│                      │  │                      │  │                      │
│  GOAP Planning       │  │  CombatUtils        │  │  Vector Operations   │
│  ┌────────────────┐  │  │  ┌────────────────┐  │  │  ┌────────────────┐ │
│  │ World State    │  │  │  │ Damage Calc    │  │  │  │ Distance       │ │
│  │ Actions        │  │  │  │ Raycasting     │  │  │  │ Rotation       │ │
│  │ Goals          │  │  │  │ Target Find    │  │  │  │ Direction      │ │
│  │ A* Planner     │  │  │  │ Crystal Place  │  │  │  │ Normalize      │ │
│  └────────────────┘  │  │  └────────────────┘  │  │  └────────────────┘ │
│                      │  │                      │  │                      │
│  A* Pathfinding      │  │                      │  │  Physics & Geometry  │
│  ┌────────────────┐  │  │                      │  │  ┌────────────────┐ │
│  │ Node Search    │  │  │                      │  │  │ Trajectory     │ │
│  │ Heuristic      │  │  │                      │  │  │ Explosion      │ │
│  │ Cost Calc      │  │  │                      │  │  │ Interception   │ │
│  │ Path Cache     │  │  │                      │  │  │ Angle Calc     │ │
│  └────────────────┘  │  │                      │  │  └────────────────┘ │
│                      │  │                      │  │                      │
└──────────────────────┘  └──────────────────────┘  └──────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                        EXAMPLE MODULE: CRYSTAL AURA                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │  1. FIND TARGET                                                        │ │
│  │     ↓ CombatUtils.findNearestEnemy(range)                             │ │
│  │     ↓ Uses MathUtils.fastDistance() for efficiency                    │ │
│  │     ↓ Returns closest living enemy within range                       │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                ↓                                             │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │  2. PREDICT MOVEMENT                                                   │ │
│  │     ↓ MathUtils.predictPosition(target, ticksAhead)                   │ │
│  │     ↓ Uses velocity vector + trajectory physics                       │ │
│  │     ↓ Returns future position (3-10 ticks ahead)                      │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                ↓                                             │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │  3. FIND BEST POSITIONS                                                │ │
│  │     ↓ Search 3D space around predicted position                       │ │
│  │     ↓ Check canPlaceCrystal() for each position                       │ │
│  │     ↓ Calculate damage: CombatUtils.calculateCrystalDamage()          │ │
│  │     ↓ Score = targetDamage - selfDamage * 0.5                         │ │
│  │     ↓ Sort by score, take top N positions                             │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                ↓                                             │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │  4. DAMAGE CALCULATION (Cached for Performance)                       │ │
│  │     ↓ Check cache first (50ms expiry)                                 │ │
│  │     ↓ If miss: Calculate with raycasting                              │ │
│  │     ↓ Raycast: 27 rays through entity bounding box                    │ │
│  │     ↓ Calculate exposure (0.0 = blocked, 1.0 = exposed)               │ │
│  │     ↓ Apply explosion formula (Minecraft physics)                     │ │
│  │     ↓ Apply armor reduction (Minecraft formula)                       │ │
│  │     ↓ Cache result for 50ms                                           │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                ↓                                             │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │  5. SAFETY CHECKS                                                      │ │
│  │     ↓ Check selfDamage < maxSelfDamage                                │ │
│  │     ↓ If anti-suicide: check health + absorption                      │ │
│  │     ↓ If lethal: skip this position                                   │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                ↓                                             │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │  6. SMOOTH ROTATION (Human-Like)                                      │ │
│  │     ↓ PlayerMovement.humanRotate(crystalPos)                          │ │
│  │     ↓ Calculate yaw/pitch to target                                   │ │
│  │     ↓ Add Gaussian noise (humans aren't perfect)                      │ │
│  │     ↓ Interpolate with Bezier curve                                   │ │
│  │     ↓ Distance-based speed (fast→slow)                                │ │
│  │     ↓ Apply rotation smoothly                                         │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                ↓                                             │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │  7. PLACE CRYSTAL                                                      │ │
│  │     ↓ Switch to crystal (if autoSwitch enabled)                       │ │
│  │     ↓ Create BlockHitResult (position + direction)                    │ │
│  │     ↓ mc.interactionManager.interactBlock()                           │ │
│  │     ↓ Place up to N crystals per tick                                 │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                ↓                                             │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │  8. BREAK CRYSTALS                                                     │ │
│  │     ↓ Find all crystals in range                                      │ │
│  │     ↓ Sort by damage to target (highest first)                        │ │
│  │     ↓ Rotate to crystal                                               │ │
│  │     ↓ mc.interactionManager.attackEntity()                            │ │
│  │     ↓ Break up to N crystals per tick                                 │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                                                              │
│  Result: Mathematically optimal crystal placement with human-like behavior  │
└─────────────────────────────────────────────────────────────────────────────┘

═══════════════════════════════════════════════════════════════════════════════
                            PERFORMANCE FLOW
═══════════════════════════════════════════════════════════════════════════════

┌─────────────────────────────────────────────────────────────────────────────┐
│  OPTIMIZATION STRATEGIES                                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  1. PRECOMPUTATION (Init Time)                                              │
│     • Trigonometric tables (4096 entries)                                   │
│     • Constant values                                                        │
│     • Pattern templates                                                      │
│     • Lookup structures                                                      │
│                                                                              │
│  2. CACHING (Runtime)                                                        │
│     ┌─────────────┬──────────────┬─────────────┐                           │
│     │ Damage      │ Pathfinding  │ Targets     │                           │
│     │ 50ms TTL    │ Until change │ Per tick    │                           │
│     │ 50x faster  │ Instant      │ 10x faster  │                           │
│     └─────────────┴──────────────┴─────────────┘                           │
│                                                                              │
│  3. PARALLEL PROCESSING                                                      │
│     ┌────────────────┬────────────────┬────────────────┐                   │
│     │ Main Thread    │ Pathfinding    │ Optimization   │                   │
│     │ Game logic     │ A* search      │ Cache cleanup  │                   │
│     │ Rendering      │ Async          │ Every 5 sec    │                   │
│     └────────────────┴────────────────┴────────────────┘                   │
│                                                                              │
│  4. EFFICIENT ALGORITHMS                                                     │
│     • A* pathfinding: O(E log V) - optimal                                  │
│     • Distance squared: O(1) - no sqrt                                      │
│     • Hash maps: O(1) - instant lookup                                      │
│     • Priority queues: O(log n) - fast sorting                              │
│                                                                              │
│  5. MEMORY MANAGEMENT                                                        │
│     • Concurrent maps (thread-safe)                                         │
│     • Object pooling (reuse)                                                │
│     • Automatic cleanup (GC friendly)                                       │
│     • Bounded collections (no memory leaks)                                 │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘

═══════════════════════════════════════════════════════════════════════════════
                            AI DECISION FLOW
═══════════════════════════════════════════════════════════════════════════════

┌─────────────────────────────────────────────────────────────────────────────┐
│  ADVANCED AI BOT - DECISION TREE                                            │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│                          🤖 AI Bot Tick()                                    │
│                                 │                                            │
│                                 ▼                                            │
│                      ┌──────────────────────┐                               │
│                      │  Update World State  │                               │
│                      │  • hasEnemy          │                               │
│                      │  • inDanger          │                               │
│                      │  • health/hunger     │                               │
│                      └──────────────────────┘                               │
│                                 │                                            │
│                                 ▼                                            │
│                      ┌──────────────────────┐                               │
│                      │   Behavior Tree      │                               │
│                      │   Root (Selector)    │                               │
│                      └──────────────────────┘                               │
│                                 │                                            │
│         ┌───────────────────────┼───────────────────────┐                   │
│         ▼                       ▼                       ▼                   │
│   ┌──────────┐            ┌──────────┐          ┌──────────┐              │
│   │Emergency?│            │ Combat?  │          │  Plan?   │              │
│   └──────────┘            └──────────┘          └──────────┘              │
│         │ YES                   │ YES                 │ YES                 │
│         ▼                       ▼                     ▼                     │
│   ┌──────────┐            ┌──────────┐          ┌──────────┐              │
│   │ RETREAT  │            │ ATTACK   │          │ EXECUTE  │              │
│   │ • Find   │            │ • Target │          │ • Action │              │
│   │   safe   │            │ • Strafe │          │ • Next   │              │
│   │   dir    │            │ • Attack │          │   step   │              │
│   │ • Path   │            │ • Predict│          │          │              │
│   └──────────┘            └──────────┘          └──────────┘              │
│                                                        │ NO                  │
│                                                        ▼                     │
│                                                  ┌──────────┐              │
│                                                  │ GOAP     │              │
│                                                  │ Planning │              │
│                                                  └──────────┘              │
│                                                        │                     │
│                    ┌───────────────────────────────────┤                    │
│                    │                                   │                    │
│                    ▼                                   ▼                    │
│             ┌─────────────┐                     ┌─────────────┐           │
│             │ Determine   │                     │   Find      │           │
│             │   Goal      │────────────────────▶│  Actions    │           │
│             │ • Survive   │                     │ • A* search │           │
│             │ • Kill      │                     │ • Sequence  │           │
│             │ • Explore   │                     └─────────────┘           │
│             └─────────────┘                            │                    │
│                                                        ▼                     │
│                                                  ┌──────────┐              │
│                                                  │ Execute  │              │
│                                                  │  Plan    │              │
│                                                  └──────────┘              │
│                                                        │                     │
│                                                        ▼                     │
│                                                  ┌──────────┐              │
│                                                  │   A*     │              │
│                                                  │  Path    │              │
│                                                  └──────────┘              │
│                                                        │                     │
│                                                        ▼                     │
│                                                  ┌──────────┐              │
│                                                  │  Move    │              │
│                                                  │ Execute  │              │
│                                                  └──────────┘              │
│                                                                              │
│  Result: Dynamic, adaptive, intelligent behavior in ANY situation           │
└─────────────────────────────────────────────────────────────────────────────┘

═══════════════════════════════════════════════════════════════════════════════
                              DATA FLOW
═══════════════════════════════════════════════════════════════════════════════

Minecraft Game Tick (20 ticks/second)
         │
         ▼
Meteor Client Events
         │
         ├─▶ TickEvent.Pre ──────────▶ All Modules tick()
         │                                     │
         ├─▶ PacketEvent.Receive ────▶ Packet modules ──▶ Anti-detection
         │                                     │
         ├─▶ Render3DEvent ──────────▶ ESP modules ────▶ Rendering
         │                                     │
         └─▶ AttackEntityEvent ──────▶ Combat modules ──▶ Damage calc
                                              │
                                              ▼
                              Uses AI/Math/Utils layers
                                              │
                                              ▼
                              Caches results (50ms)
                                              │
                                              ▼
                              Executes actions
                                              │
                                              ▼
                              Sends packets to server

═══════════════════════════════════════════════════════════════════════════════
```

**Key Takeaways**:
1. **Layered Architecture**: Clean separation of concerns
2. **AI-Driven**: Behavior Trees + GOAP + A* = Intelligent behavior
3. **Mathematically Perfect**: Optimized algorithms, exact calculations
4. **Human-Like**: Gaussian delays, Bezier curves, natural variations
5. **Performance Optimized**: Caching, parallel processing, fast algorithms
6. **Easy to Use**: Organized categories, simple settings, clear documentation

**Gabriel_SK ULTIMATE v3.0** - The future of Minecraft client development! 🚀
