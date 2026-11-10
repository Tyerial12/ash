# Vault City — Hardcore 6-Tab Architecture & Survival Overhaul Plan

Re-architects **Vault City** around its complete existing bunker simulation while introducing dedicated bottom tabs for **Mining**, **Building**, **Hunting**, **Vault Stats**, **Current Resources**, and **Crew & Power**, gradual **Level 0 (Worst) to Level 10+** room progression, deeper hardcore survival hazards, and **background phone notifications** when the game is not loaded.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural and gameplay decisions incorporate your confirmed preferences from our planning Q&A and your latest requirements:

- **Confirmed Decision 1 — 6 Dedicated Bottom Navigation Tabs**: Reorganize the interface into **6 primary bottom tabs** so every core activity has a dedicated, uncluttered command view:
  1. **Mining** (Bedrock Veins, Pickaxe Durability, Depth Heat & Cave-In Hazards, Ore Smelting)
  2. **Building** (All 11 Vault Rooms across B1–B6, 2D Cutaway & 3D Isometric Maps, Room Wear & Pipe Leak Maintenance)
  3. **Hunting** (Wasteland Beast Hunts, Bleeding/Venom Hazards, Crater Fishing, Armory Crafting & Siege Defense)
  4. **Vault Stats** (Survival Timers, Life-Support Telemetry, 10-Day Boss Siege Readiness, Crew & Skill Tree Access)
  5. **Resources** (Live Stockpiles vs. Store Room Capacity, Positive vs. Deficit Net Balance Ledger, Trade & Relief Drops)
  6. **Crew & Power** (15-Dweller Census, 60s Feeding Cycle, 4-Crank Emergency Dynamo, Reactors & Global Network)
- **Confirmed Decision 2 — Gradual Room Scaling from Level 0 (Worst) Upward**: Every room starts at **Level 0** in a makeshift/degraded state (**20% room output efficiency** and **45% worker gathering pace**) and improves smoothly by **+18% room efficiency** and **+13% worker pace** on every single upgrade (`Lv.0 -> Lv.1 -> Lv.2 -> Lv.3 -> Lv.10+`).
- **Confirmed Decision 3 — Hardcore Mining, Building & Hunting Hazards**:
  - **Mining**: Pickaxe Durability (`0–100%`), Subterranean Depth Heat gauge, and Seismic Cave-In hazards.
  - **Building**: Individual Room Structural Integrity, Machinery Wear, and Coolant Pipe Leaks that drain extra Water/Scrap until welded.
  - **Hunting**: Beast counter-attack risks (Bleeding & Rad-Venom injuries), strict Ammo/Spear scarcity, and perimeter siege defense.
- **Confirmed Decision 4 — Background Phone Notifications When Game Is Not Loaded**: Schedule and trigger native Android status-bar notifications whenever the app is in the background or closed to alert you when **(1) Resources reach Max Capacity**, **(2) the Vault runs out of Power**, or **(3) a Vault Room finishes upgrading**.

---

## 1. Overview & Core Concept

- **What It Does**: **Vault City** is a hardcore post-nuclear underground shelter simulator where you begin alone at **Day 1** with only a **Level 1 Pickaxe**, damaged blast doors, and degraded life support. You must mine bedrock veins for Scrap and Gold, repair and upgrade 11 subterranean rooms across Floors B1–B6, recruit up to **15 Vault Dwellers** (including rare **8% Unique Dwellers**), feed them every **60 seconds**, balance super-linear Reactor Bio-Fuel burn, and survive escalating daily raids and **10-Day Nuclear Warlord Boss Sieges**.
- **Target Audience / Persona**: Players who enjoy high-stakes resource management, deep telemetry dashboards, and tactical survival mechanics where every decision—from pickaxe maintenance and pipe welding to reactor fuel rationing—matters.
- **Key Value**: Splitting the dense bunker controls into dedicated tabs for **Mining**, **Building**, **Hunting**, **Vault Stats**, **Resources**, and **Crew & Power** makes every mechanic immediately accessible while adding tactile hardcore hazards and offline phone alerts so you never lose a Vault to an unnoticed blackout or wasted stockpile cap.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Hardcore Mining Loop (`Mining` Tab)**:
   - Swing your Pickaxe at active bedrock veins to extract Scrap, Water, Food, and Gold Sovereigns.
   - Monitor **Pickaxe Durability (`0–100%`)** and **Shaft Depth Heat (`°C`)**. Rapid swings increase thermal stress and wear down the pickaxe head; if durability drops to `0%`, mining yields drop by `-65%` until you sharpen/reforge the pickaxe with Scrap.
   - Respond to **Seismic Cave-In Alerts** by shoring up the shaft with Scrap or venting shaft heat with Water before a tunnel collapse damages health.
2. **Gradual Room Progression & Maintenance (`Building` Tab)**:
   - Switch between the **Room Cards List**, **2D Bunker Cutaway**, and **3D Isometric Hologram Map** showing all 11 rooms across Surface and Floors B1–B6.
   - Rooms at **Level 0** operate as makeshift stations at **20% efficiency** (the worst tier) and gradually improve with every level (`Lv.1 = 38%`, `Lv.2 = 56%`, `Lv.3 = 74%`, `Lv.4 = 92%`, `Lv.5 = 110%`, `Lv.6 = 128%`, etc.).
   - High-load rooms periodically develop **Coolant Pipe Leaks** or **Structural Wear**, increasing Water/Scrap drain until patched manually or by assigned Scrappers.
3. **Wasteland Hunting, Fishing & Siege Defense (`Hunting` Tab)**:
   - Craft Harpoon Spears, Snare Traps, Ballistic Ammo, Gas Masks, Hazmat Suits, and Medkits.
   - Stalk canyon beasts (*Dune Razorback*, *Canyon Stalker Pack*, *Apex Rad-Behemoth*, *Glowing Leviathan Wyrm*) while managing **Beast Counter-Attack Risks** (Bleeding & Rad-Venom injuries that reduce Dweller health until treated with Medkits or the Medbay).
   - Man the Blast Door and deploy **Tactical Siege Countermeasures** (*Flak Volley*, *EMP Pulse*, *Emergency Weld*) during Daily Raids and 10-Day Warlord Boss Sieges.
4. **Telemetry & Command Diagnostics (`Vault Stats` Tab)**:
   - Inspect all survival clocks in one place: **60s Dweller Feeding Countdown**, **2m Appetite Escalation Tier**, **6-Day Power Outage Clock**, **2-Day Resource Deficit Clock**, and **10-Day Boss Siege Countdown**.
   - Switch **Vault Operational Posture** (*Eco-Conservation*, *Standard*, *Overclock*), view Background Notification status, and access the **Overseer Skill Tree & Prestige Ascension** console.
5. **Stockpile & Net Economy Ledger (`Resources` Tab)**:
   - View real-time progress bars for all primary and rare resources against your **Store Room + Silo Storage Cap**.
   - Inspect an itemized **Positive vs. Deficit Resource Ledger** showing exact gross production per second vs. Dweller feeding, Room upkeep, Command Level upkeep, Pipe Leak drain, and Reactor Bio-Fuel burn, plus Caravan Trading and Emergency Relief Air-Drops.
6. **Workforce, Power Grid & Offline Alerts (`Crew & Power` Tab)**:
   - Manage up to **15 Max Vault Dwellers**, assign worker classes, dispatch Wasteland Expeditions, tune worn generators, upgrade Reactors (with super-linear Bio-Fuel upkeep), and use up to **4 Manual Dynamo Cranks per power outage**.
   - When you background or close the app, **Offline Phone Notifications** automatically alert you when resources hit max capacity, when the Vault runs out of power, or when a room upgrade completes.

### Visual Identity & Theme
- **Aesthetic Direction**: Retro-futuristic CRT Vault-Tec / industrial subterranean command terminal with high-contrast hazard telemetry, phosphor scanlines, and tactile mechanical controls.
- **Color Palette & Mood**:
  - Background Primary (`--background`): `BunkerCharcoal` (`#0D1114`) & `SteelPanel` (`#151C21`)
  - Elevated Surface (`--card`): `ElevatedSlate` (`#1D262D`) with `PanelBorder` (`#2B3842`)
  - Primary Accent (`--primary`): `SolarAmber` (`#FFB300`) & `XpGold` (`#FFD54F`)
  - Secondary Accent (`--secondary`): `RadTeal` (`#00E5FF`) & `CryoCyan` (`#40C4FF`)
  - Semantic Status: `SurplusGreen` (`#00E676`) for positive net production/online grid, `WarningOrange` (`#FF9100`) for wear/heat/pipe leaks, `HazardRed` (`#FF1744`) for blackout/deficit/bleeding alerts, and `BioluminescentViolet` (`#D500F9`) for mutagen/unique dwellers.
- **Typography & Hierarchy**: Bold monospaced/industrial telemetry headers paired with high-legibility compact labels (`sp`-scaled) and color-coded status pills so dense numerical rates (`+X.XX/s` vs `-Y.YY/s`) scan Effortlessly.
- **Component Styling & Layout**: Edge-to-edge `Scaffold` respecting `WindowInsets.safeDrawing` and `WindowInsets.navigationBars`, compact 6-item Material 3 `NavigationBar` with live status badges, and minimum `48.dp` interactive touch targets across all buttons and chips.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1 — Gradual Linear-Plus-Compound Room Progression Starting at Level 0**:
  - *Chosen Approach*: Define `computeRoomLevelOutputMultiplier(level) = 0.20 + level * 0.18` and `computeWorkerLowRoomPacingMultiplier(level) = 0.45 + level * 0.13` so Level 0 is the worst (`20%` room efficiency, `45%` worker pace) and every single upgrade (`Lv.1, Lv.2, Lv.3...`) delivers a predictable, noticeable step up without sudden cliffs.
  - *Why*: Directly fulfills the requirement that rooms start at Level 0 as the worst and gradually get a little better each time you level them, while keeping early resource gathering deliberate and rewarding every room upgrade.
- **Decision 2 — Dual-Path Background Phone Notifications (`AlarmManager` + Live Tick Observer)**:
  - *Chosen Approach*: Combine scheduled `AlarmManager` (`ELAPSED_REALTIME_WAKEUP`) `PendingIntent` broadcasts when the app moves to `ON_STOP` with live background state transition checks in the ViewModel, plus persisted room construction state in Room DB.
  - *Why*: Ensures phone notifications fire reliably when the game is not loaded—both if the app is minimized in the background and if the OS suspends the UI—without requiring restricted exact-alarm permissions.
- **Decision 3 — 6-Tab Dedicated Navigation Bar with Integrated Sub-Consoles**:
  - *Chosen Approach*: Provide 6 top-level bottom navigation tabs (`Mining`, `Building`, `Hunting`, `Vault Stats`, `Resources`, `Crew & Power`), while embedding quick segment switches inside `Vault Stats` (for the Skill Tree & Prestige console) and `Resources` / `Crew & Power` (for Caravan Trade & Public Leaderboard).
  - *Why*: Gives every core gameplay loop requested (`Mining`, `Building`, `Hunting`, `Vault Stats`, `Resources`) its own first-class tab while keeping `Crew & Power`, `Skills`, and `Network` 1 tap away.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & Component Diagram

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                        MainActivity (Edge-to-Edge UI)                        │
│  • POST_NOTIFICATIONS Runtime Permission Launcher (Android 13+)              │
│  • Lifecycle Observer (ON_START cancels alarms / ON_STOP schedules alarms)   │
│  • Persistent Top Telemetry Header (Day/Night, Quick Resource & Net Banner)  │
└──────────────────────────────────────┬───────────────────────────────────────┘
                                       │
         ┌─────────────────────────────┴─────────────────────────────┐
         ▼                                                           ▼
┌─────────────────────────────────────────┐       ┌────────────────────────────┐
│     6-Tab Bottom Navigation Scaffold    │       │  Offline Alert Subsystem   │
├─────────────────────────────────────────┤       ├────────────────────────────┤
│ 1. MiningTab (Veins, Pickaxe Wear/Heat) │       │ • VaultNotificationHelper  │
│ 2. BuildingTab (Rooms B1-B6, 2D/3D Map, │       │ • VaultNotificationReceiver│
│    Gradual Lv.0-10+ Scaling, Pipe Leaks)│       │ • AlarmManager ETA Engine: │
│ 3. HuntingTab (Hunts, Bleed/Venom,      │       │   - Room Upgrade Complete  │
│    Fishing, Armory & Siege Defense)     │       │   - Resources At Max Cap   │
│ 4. VaultStatsTab (Clocks, Life Support, │       │   - Vault Out Of Power     │
│    Postures, Skill Tree & Ascension)    │       └──────────────┬─────────────┘
│ 5. ResourcesTab (Stockpiles, Store Room │                      │
│    Cap, Net Balance Ledger, Trade)      │                      │
│ 6. CrewPowerTab (15 Max Dwellers, 60s   │                      │
│    Feed, 4 Cranks, Reactors & Network)  │                      │
└────────────────────┬────────────────────┘                      │
                     │                                           │
                     ▼                                           ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                 OutpostViewModel & OutpostCalculator (Domain)                │
│  • 1s Hardcore Simulation Loop + Offline Catch-Up Engine                     │
│  • Gradual Room Output (Lv.0 = 20% worst -> +18%/Lv) & Worker Pacing         │
│  • Hardcore Hazards: Pickaxe Durability/Heat/Cave-In, Pipe Leaks, Bleeding   │
│  • Room Persistence (OutpostDatabase) + Firestore Public Leaderboard Sync    │
└──────────────────────────────────────────────────────────────────────────────┘
```

### Data Model & State Additions
- **Persisted Hardcore & Construction Fields in `OutpostStateEntity`**:
  - *Mining Hazards*: `pickaxeDurabilityPercent: Int = 100`, `miningShaftHeatPercent: Int = 0`, `activeCaveInHazard: Boolean = false`
  - *Building Hazards*: `activePipeLeaksCount: Int = 0`, `roomStructuralWearPercent: Int = 0`
  - *Hunting Hazards*: `bleedingDwellersCount: Int = 0`, `venomInfectedDwellersCount: Int = 0`
  - *Persisted Room Construction (for Offline Completion & Notifications)*: `activeConstructionBuildingId: String = ""`, `activeConstructionTargetLevel: Int = 0`, `activeConstructionRemainingSeconds: Int = 0`, `activeConstructionTotalSeconds: Int = 0`, `activeConstructionRushGoldCost: Double = 0.0`
- **Offline Notification ETA Calculator (`OfflineNotificationSchedule`)**:
  - Pure domain calculation in `OutpostCalculator.computeOfflineNotificationSchedule(...)` computing `secondsUntilRoomUpgradeComplete`, `secondsUntilResourcesAtMax`, and `secondsUntilPowerOutage` based on current stockpiles, net production rates, active construction timers, dynamo overdrive expiration, and reactor Bio-Fuel depletion.

### Interactive Component & State Mapping
- **Mining Tab Actions**:
  - `harvestSalvageNode(slotIndex)` -> Checks `activeCaveInHazard` and `miningShaftHeatPercent`, reduces `pickaxeDurabilityPercent`, increases `miningShaftHeatPercent`, rolls for seismic cave-in if heat is high, and awards Scrap/Water/Food/Gold scaled by Pickaxe condition.
  - `sharpenPickaxe()` -> Spends Scrap to restore `pickaxeDurabilityPercent` to `100%`.
  - `coolMiningShaft()` / `clearCaveInHazard()` -> Spends Water to vent shaft heat (`-45%` heat) or Scrap to shore up a tunnel collapse.
- **Building Tab Actions**:
  - `upgradeBuilding(type)` -> Persists active construction to Room DB so countdown continues seamlessly across app restarts and triggers the **Room Upgrade Complete** phone notification if completed while backgrounded.
  - `weldPipeLeaksAndRoomWear()` -> Spends Scrap to seal active coolant pipe leaks (`activePipeLeaksCount = 0`) and restore room structural integrity.
- **Hunting Tab Actions**:
  - `executeHunt(target)` -> Consumes Spears/Traps/Ammo, awards Food/Water/Gold/Cores, and rolls for Beast Counter-Attack (`bleedingDwellersCount` / `venomInfectedDwellersCount` mitigated by Crossbow level, Stalkers, and Hazmat/Armor gear).
  - `treatHuntingInjuries()` -> Uses a Field Medkit or RadAway to cure bleeding and venom status effects on Dwellers.
- **Background Notification Lifecycle**:
  - On `Lifecycle.Event.ON_STOP`: `VaultNotificationHelper.scheduleOfflineNotifications(...)` schedules background alarms for **Resources At Max**, **Vault Out of Power**, and **Room Upgrade Complete**.
  - On `Lifecycle.Event.ON_START`: Cancels pending background alarms so alerts only fire when the game is not loaded.
