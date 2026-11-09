# Vault City — Next-Phase Evolution Plan (From Current Game State)

## 1. Current Game State Assessment & Rebalance Goals

**Vault City** currently features:
- **6-Day Power Outage (`OUTAGE X/6D`) Game Over** and **2-Day Resource Deficit (`DEFICIT X/2D`) Defense Shutdown + Game Over**
- **Major Nuclear Warlord Mega-Sieges every 10 Days (`Day 10, 20, 30...`)** and accelerating daily attacks
- **5-Minute Dweller Feeding (`300s`)** and **2-Minute Dweller Appetite Escalation (`120s`)**
- **Vault Command Level & Room Level super-linear resource upkeep**
- **Anytime Restart / Game Over Reset** (Full Fresh Reset vs. Keep Prestige Reset) and **4-Button CRT Sub-Tab Switcher**

Based on your feedback, we will evolve the current game state across **five major pillars** while making **Days 1–10 more forgiving** so players can smoothly bootstrap their bunker before the hardcore mid/late-game ramp kicks in.

---

## 2. Core New Mechanics & Rebalancing

### Pillar 1: Forgiving Early Days (Days 1–10) & Dynamic Economy Rebalance
1. **Days 1–10 Early Bunker Grace Window**:
   - **Resource Deficit Grace**: During Days 1–3 (or while fewer than 2 Vault rooms are built), the **2-Day Resource Deficit Game Over counter is paused** (`EARLY BUNKER GRACE`), giving players time to mine Scrap/Gold, hire their first Dwellers, and bring basic rooms online.
   - **Softer Early Upkeep Curve**: During Days 1–10, Vault Command Level and Room Level resource upkeep is reduced by **50%**, and the **2-Minute Dweller Appetite Escalation** scales at **`+6%` per 2 minutes** (capped at `+60%` in Days 1–10) instead of `+14%`. Completing a **5-Minute Dweller Feeding Cycle with full rations & water** or running a **Decontamination / Medbay cycle** trims `-1` appetite escalation tier so active Overseers can manage Dweller hunger.
   - **Smooth Days 1–9 Attack Ramp**: Daily raid required defense starts at a manageable **`14 DEF` on Day 1** (easily covered by the starter Lv.1 Blast Door once repaired) and scales smoothly through Day 9 before the first **Day 10 Nuclear Warlord Boss Siege (`115 DEF`)**.
2. **Vault Operational Posture Toggle (Eco-Conservation vs. Balanced vs. Overclock)**:
   - Add an instant 3-way **Vault Operational Posture** switch in the Vault Power & Clocks panel and Armory:
     - **ECO-CONSERVATION (`-40% Vault & Room Upkeep, -15% Output`)**: Lets players immediately slash resource drain per second to **exit a Resource Deficit and bring Vault Defenses back online** during an emergency!
     - **STANDARD (`100% Output, 100% Upkeep`)**: Default balanced bunker operation.
     - **OVERCLOCK (`+30% Resource & XP Output, +25% Upkeep & MW`)**: High-yield push mode when power and stockpiles are thriving.

### Pillar 2: Deeper 10-Day Boss Siege Combat & Tactical Countermeasures
1. **Named 10-Day Boss Sieges**:
   - Every 10th Day (`Day 10, 20, 30, 40...`) spawns a named **Nuclear Warlord Boss Siege** with distinct lore, boss armor rating, and high-value victory spoils (`Mutagen Cores`, `Thorium Rods`, `Aether Cells`, and `Gold Sovereigns`):
     - **Day 10**: *Ironclad Goliath Siege Tank*
     - **Day 20**: *Rad-Behemoth Siegebreaker Herd*
     - **Day 30**: *Enclave Orbital Dreadnought Strike*
     - **Day 40+**: *Apex Warlord Titan Division (Tier IV+)*
2. **3 Active Tactical Siege Countermeasures** (usable during any active Daily Raid or Day 10 Boss Siege in the Armory & Defense tab):
   - **BALLISTIC FLAK VOLLEY (`10 Ammo Rds`)**: Fires concentrated turret & rifle salvos to weaken the attacking force (`-25% Required Siege DEF` for the current attack and `+15s` timer extension).
   - **EMP OVERCHARGE PULSE (`25 Bio-Fuel` or `2 Aether Cells`)**: Detonates an electromagnetic shockwave across the blast perimeter (`-35% Required Siege DEF` against mechanized raiders/bosses).
   - **EMERGENCY BLAST WELD (`35 Scrap Metal`)**: Welds structural plates onto the Blast Door mid-siege (`+25% Defense Integrity` immediately, restoring lost DEF rating before impact).

### Pillar 3: More Vault Rooms (#9 & #10) on Deep-Core Floor B5
Expand `BuildingType` and `VaultMapStation` from 8 rooms to **10 buildable rooms across 5 Underground Decks (B1–B5)**:
- **Room #9: Deep-Core Geothermal Tap (Floor B5, unlocks at Command Lv.4)**
  - Harnesses subterranean magma vents to generate **`+6.0 MW/Lv` continuous baseline power** (unaffected by nightfall or surface eclipses) and reduces generator wear accumulation by `-10%/Lv`.
- **Room #10: Cryo-Stasis & Ration Vault (Floor B5, unlocks at Command Lv.5)**
  - Preserves hydroponic harvests and slows Dweller metabolic drain, reducing **5-Minute & Daily Dweller Food/Water consumption by `-8%/Lv`** (up to `-40%`) and increasing maximum Vault storage capacity by `+350/Lv`.

### Pillar 4: Expanded Wasteland Expeditions, Factions & Apex Boss Hunts
- **New Apex Hunting Target**: *Glowing Leviathan Wyrm* (requires Aether Crossbow Lv.2 + 2 Spears; rewards massive Food, Water, Gold, and Mutagen Cores).
- **New High-Tier Wasteland Expedition**: *Sunken Nuclear Submarine Vault* (rewards Thorium Fuel Rods, Aether Cells, and Scrap caches).
- **Emergency Faction Relief Drop**: In the Trade & Power tab (and Power/Deficit emergency panel), players can spend Gold Sovereigns to call an **Emergency Bunker Air-Drop** (`+60 Food, +60 Water, +50 Bio-Fuel, +50 Scrap`) to save a dying Vault from Blackout or Deficit collapse.

### Pillar 5: Enhanced 2D Vault Cutaway Visuals, CRT Animations & Haptic Alerts
- **Floor B5 Deep-Core Visuals**: Update `VaultCutawayMapCard` to render **Floor B5 (Geothermal magma glow & Cryo-Stasis frost chambers)** with interactive tap-to-walk Overseer stations.
- **CRT Radar Sweep & Hazard Pulse**: Add an animated phosphor radar scanline and pulsing red alert border when a Day 10 Boss Siege, Power Blackout, or Resource Deficit is active, paired with tactile `LocalHapticFeedback` on mining, countermeasure strikes, and level-ups.

---

## 3. Planned File Changes

1. **`/app/src/main/java/com/example/model/OutpostModels.kt`**:
   - Add `BuildingType.GEOTHERMAL_TAP` (Room #9) and `BuildingType.CRYO_STASIS_VAULT` (Room #10).
   - Add `VaultMapStation.ROOM_GEOTHERMAL` and `VaultMapStation.ROOM_CRYO_VAULT` on Floor B5.
   - Add `VaultOperationalPosture` enum (`ECO_CONSERVATION`, `STANDARD`, `OVERCLOCK`).
   - Add `SiegeCountermeasureType` enum (`FLAK_VOLLEY`, `EMP_PULSE`, `EMERGENCY_WELD`).
   - Add new `HuntingTarget.GLOWING_LEVIATHAN_WYRM` and `WastelandExpeditionType.SUNKEN_SUBMARINE_REACTOR`.
2. **`/app/src/main/java/com/example/data/local/OutpostEntities.kt` & `OutpostDatabase.kt`**:
   - Add `operationalPostureId: String = "STANDARD"` and `activeRaidWeakenedPercent: Int = 0` to `OutpostStateEntity`; bump Room schema version with destructive migration fallback.
3. **`/app/src/main/java/com/example/domain/OutpostCalculator.kt`**:
   - Implement the **Days 1–10 Early Bunker Grace Window** (50% lower room/vault upkeep in Days 1–10, `+6%/2m` softer dweller appetite escalation in Days 1–10, and Deficit Game Over grace in Days 1–3).
   - Apply `VaultOperationalPosture` multipliers (`ECO_CONSERVATION` cuts Vault & Room resource drain by `-40%` so players can immediately recover from resource deficit).
   - Integrate Room #9 (`GEOTHERMAL_TAP` `+6.0 MW/Lv`) and Room #10 (`CRYO_STASIS_VAULT` `-8%/Lv` dweller food/water upkeep & `+350/Lv` storage).
   - Compute named Day 10/20/30/40+ Boss Siege profiles and tactical countermeasure reductions.
4. **`/app/src/main/java/com/example/viewmodel/OutpostViewModel.kt`**:
   - Add actions: `setOperationalPosture(posture)`, `executeSiegeCountermeasure(type)`, and `callEmergencyReliefDrop()`.
   - Reduce `dwellerAppetiteEscalationSeconds` by 1 tier (`-120s`) whenever a 5-minute feeding cycle completes with sufficient food & water.
5. **`/app/src/main/java/com/example/ui/components/VaultCutawayMapCard.kt`**, **`OutpostTabScreen.kt`**, & **`CraftHuntDefenseTabScreen.kt`**:
   - Render Floor B5 rooms in the 2D Vault Cutaway Map with animated geothermal/cryo effects and haptic feedback.
   - Add the **Vault Operational Posture Switcher (`ECO / STD / OVERCLOCK`)** and **Emergency Supply Air-Drop** button to `OutpostTabScreen`.
   - Add the **3 Tactical Siege Countermeasure buttons (`FLAK VOLLEY`, `EMP PULSE`, `DOOR WELD`)** to active raid/boss cards in `CraftHuntDefenseTabScreen`.
6. **`/app/src/test/java/com/example/ExampleUnitTest.kt`**:
   - Add unit tests covering the Days 1–10 forgiving grace window, `ECO_CONSERVATION` posture deficit recovery, Rooms #9 & #10 benefits, and Tactical Siege Countermeasures.
