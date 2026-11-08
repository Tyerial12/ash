# Vault City — Public Multi-User Network, Prestige Skill Tree, Daily Worker Rations & Mobile Telemetry Tabs Plan

This plan makes **Vault City** a **public multi-user survival game** backed by **Firebase Firestore & Google Sign-In**, introduces an **Overseer Ascension Prestige System** tightly integrated with a **Legacy Skill Tree Branch**, enforces **Mandatory Daily Food & Water Rations for Workers**, and redesigns the mobile interface with a **Collapsible Top Telemetry Drawer** featuring dedicated tabs for **Resources Gathered** and **Current Systems Online**.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> **Confirmed Design & Gameplay Decisions**
> - **Public Multi-User Wasteland Network (Firebase Firestore & Google Sign-In)**:
>   - Players sign in with **Google Sign-In** (via Jetpack `CredentialManager`) to publish their Vault to the live **Public Wasteland Vault Directory & Global Overseer Leaderboard** (`public_vaults` collection), send and claim **Public Supply Drops / Caravan Aid Packages** (`public_supply_drops` collection) with other real players, and automatically **Cloud-Sync** their personal Vault progress (`users/{userId}/vault_state`).
> - **Overseer Ascension Prestige & Skill Tree Synergy**: Resetting the Vault (available once Command Level $\ge 5$ or key rooms are restored) converts current Vault progress into **Permanent Prestige Cores** and increments your **Overseer Prestige Rank** (displayed publicly on the Global Vault Leaderboard). Prestige Cores are spent in a **4th Skill Tree Branch (`OVERSEER LEGACY`)** whose upgrades permanently persist across all future Vault resets.
> - **Mobile-Friendly Telemetry Drawer (`Resources Gathered` & `Current Systems Online` Tabs)**: The top HUD features a compact status bar that expands into a two-tab mobile telemetry drawer:
>   1. **Resources Gathered Tab**: Complete breakdown of primary resources, net `/s` production rates, storage capacity bars, tactical survival gear, Prestige Cores, and the **Daily Worker Food & Water Ration Ledger**.
>   2. **Current Systems Online Tab**: Real-time diagnostics for **(1) Power Grid MW Load, Generator Wear & Blackout Status**, **(2) Automated Defense Grid (Built + Workers + Power checklist), Turret Readiness & Door Integrity**, **(3) Life Support $\text{O}_2$ Scrubbers, Radiation & Room Automation**, and **(4) Active Timers for Room Construction, Item Crafting & Expeditions**.
> - **Daily Food & Water Needed for Workers**: Hired and assigned workers require **Daily Food & Water Rations** every Vault Day (`2.0 Food` and `2.0 Water` base per worker/day, plus active shift exertion, reduced by Living Quarters and Legacy Skills). If Food or Water runs dry (`0` stock when rations are needed), **workers become famished/dehydrated**—halting worker room automation and automated defense manning until emergency Food & Water are secured.
> - **Crafting Timers, Room-Level Build Scaling, Dangerous Night & Strict Auto-Defense**: Preserves item crafting timers (with Forge Faster `-3s` and Gold Rush), steeper room-level construction timers, dangerous Night Time (`RAD_NIGHT`) mutant swarms, and the strict rule that **Automated Defenses must be built, staffed by fed workers, and powered**.

---

## 1. Overview & Core Concept

- **What It Does**: **Vault City** is a realistic post-nuclear shelter survival, automation, and multiplayer community game. You begin alone in a broken, unpowered Vault and manually scavenge, purify water, and repair rooms until you recruit Vault Dwellers (with an **8% base chance** for **Unique Vault Dwellers** granting **+40% production**). Workers require **daily Food and Water rations** to stay healthy and keep rooms and automated defenses online. Through the **Public Wasteland Network**, your Vault is visible to other players around the world: you can climb the **Public Overseer Leaderboard** (ranked by Prestige Level, Command Level, and Days Survived), broadcast **Public Supply Drops** to help other Vaults, claim shared wasteland crates, and ascend via the **Prestige System** to unlock permanent **Legacy Skill Tree** masteries.
- **Target Audience / Persona**: Mobile strategy and survival players who want deep resource interdependence (Power MW, Daily Worker Food/Water Rations, $\text{O}_2$, Radiation, Ammo, and Prestige meta-progression) connected to a live public community of fellow Vault Overseers.
- **Key Value**: Combines tactile survival management and long-term Prestige progression with a live **Public Multi-User Wasteland Network** and a streamlined mobile telemetry drawer.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Google Sign-In & Public Wasteland Network (`Grid & Trade / Public Network`)**:
   - **Authentication Gate**: Uses Jetpack `CredentialManager` (`GetSignInWithGoogleOption`) so every player has a verified Overseer identity (`displayName`, `userId`) for cloud saves and public multi-user interactions.
   - **Public Wasteland Vault Directory & Leaderboard**:
     - Real-time Firestore feed (`public_vaults`) displaying other players' active Vaults: Overseer Name, Vault Title, **Prestige Rank**, **Command Level**, **Days Survived**, **Defense Rating**, and **Total Survivors** (including Unique Dwellers).
     - Players can tap **Publish / Sync My Vault** (also synced automatically on key milestones and Prestige Ascensions) to update their public standing.
   - **Public Caravan Supply Drops (`public_supply_drops`)**:
     - Players can post a **Public Supply Drop** (packaging surplus Scrap, Food, Water, or Bio-Fuel into the shared Wasteland Network for Gold & XP rewards) and claim unclaimed supply drops posted by other Overseers.

2. **Top Mobile Telemetry Drawer (`Resources Gathered` | `Current Systems Online`)**:
   - **Always-Visible Compact Strip**: Displays Command Level, Prestige Rank badge, Day/Night Phase, Power MW, $\text{O}_2\%$, Rads, Door Integrity, and a quick-toggle pill switching between **Compact View**, **Resources Gathered Tab**, and **Systems Online Tab**.
   - **Tab 1 — `RESOURCES GATHERED`**:
     - **Primary Stockpiles & Storage Cap**: Clean 2-column grid of `Scrap`, `Water`, `Food (Rations)`, and `Bio-Fuel` with live `+X.X/s` net rates and visual fill bars against `Storage Capacity`.
     - **Daily Worker Ration Forecast Banner**: Shows exact **Daily Food Needed (`X.X/day`)** and **Daily Water Needed (`X.X/day`)** for your current workforce, how many days of rations remain at current stockpiles, and an instant warning if workers are starving/dehydrated.
     - **Tactical & Prestige Vault Reserves**: Displays `Gold`, `Ammo Rounds`, `RadAway`, `Medkits`, `Gas Masks`, `Hazmat Suits`, `Mutagen Cores`, and `Prestige Cores`.
   - **Tab 2 — `SYSTEMS ONLINE`**:
     - **Power Grid & Generator Telemetry**: Live `Generated MW vs. Required MW`, Grid Efficiency multiplier, Generator Wear `%`, and `ONLINE / BROWNOUT / BLACKOUT` status badge.
     - **Automated Defense Grid Telemetry**: Shows whether Automated Defense is `ACTIVE` or `OFFLINE` alongside its 3 mandatory operational checks (**Built Turrets/Plasma**, **Fed Workers Assigned**, **Power MW Online**), Blast Door Integrity `%`, and Night Swarm Alert status.
     - **Life Support & Room Automation Status**: Shows $\text{O}_2$ Scrubber net rate, Radiation scrubbing rate, Worker Ration status (`FED & HYDRATED` vs `STARVING / DEHYDRATED — WORK HALTED`), and count of active vs. unpowered/unstaffed Vault Rooms.
     - **Active Operations Timers**: Live countdown progress bars for active **Room Construction**, **Armory Item Crafting**, **Trade Convoy**, and **Wasteland Expedition** right in one unified dashboard.

3. **Daily Worker Food & Water Ration Loop**:
   - Each hired survivor requires base daily rations (`2.0 Food/day` + `2.0 Water/day`, plus `+0.5 Food/Water` per actively assigned specialist shift, discounted by Living Quarters and Legacy Skills).
   - Every new Vault Day (and continuously via per-second upkeep), rations are deducted from the Vault's `Food` and `Water` stores.
   - If a day starts with insufficient Food or Water—or if `Food == 0` or `Water == 0` while workers are assigned—**Worker Starvation/Dehydration** triggers:
     - Unfed/dehydrated workers cannot run automated rooms or automated turrets until `Food > 0` and `Water > 0` are restored (or the player taps **Issue Emergency Rations / Purify Water**).
     - Full daily rations grant a **Well-Fed Crew Morale Bonus (`+15%` output)** and steady Dweller HP recovery.

4. **Overseer Ascension (Prestige System) & Skill Tree Integration**:
   - Inside the **Tech (`SkillTreeTabScreen`)** screen, a prominent **Overseer Ascension Prestige Card** sits above the Skill Branch tabs, showing:
     - Current **Prestige Rank**, owned **Prestige Cores**, and the permanent **Prestige Production & Build Speed Multiplier**.
     - **Claimable Prestige Cores** calculated from current Command Level, total room levels built, and days survived (unlocks at **Command Level 5+** or **Total Room Levels $\ge 6$**).
     - An **ASCEND VAULT (PRESTIGE RESET)** button with a confirmation dialog explaining what resets (Rooms, basic resources, day counter, standard skills) and what is permanently kept (**Prestige Rank**, **Prestige Cores**, **Overseer Legacy Skill Branch ranks**, and **Public Leaderboard Prestige Standing**).
   - **4th Skill Tree Branch — `OVERSEER LEGACY` (`SkillBranch.OVERSEER_LEGACY`)**:
     - Purchased exclusively with **Prestige Cores** (never resets on Ascension):
       1. **Cryo-Supply Drop (Tier 1)**: Start every Prestige Vault with `+40 Scrap, +25 Food, +25 Water, +15 Bio-Fuel` per rank and `+20% Global Resource Output` per rank.
       2. **Nano-Fabrication Schematics (Tier 2)**: Reduces **Room Construction Time** and **Armory Item Crafting Time** by `-12%` per rank (up to `-60%`).
       3. **Hydro-Ration Recycling (Tier 3)**: Reduces **Daily Worker Food & Water Consumption** by `-10%` per rank and increases **Unique Dweller Spawn Chance** by `+3%` per rank (from `8%` up to `23%`).
       4. **Overseer Fusion Core (Tier 4)**: Grants `+10 Base Vault MW` (preventing early blackouts) and `+25% All Defense & Turret Rating` per rank.

### Visual Identity & Theme

- **Aesthetic Direction**: Rugged **Vault-Tec Industrial Telemetry UI** with high-contrast dark slate surfaces (`#10151C`), armored steel cards (`#18202C`), and color-coded status pills.
- **Color Palette & Mood**:
  - `SurplusGreen` (`#2CE69B`) for online systems, fed workforce, and positive resource rates.
  - `SolarAmber` (`#FFBE0B`) and `XpGold` (`#FFD166`) for Prestige Cores, Overseer Ascension badges, and power load warnings.
  - `CryoCyan` (`#00E5FF`) for $\text{O}_2$, water hydration, and telemetry tabs.
  - `HazardRed` (`#FF3864`) for Blackout, Starvation/Dehydration alerts, and Night Mutant Swarms.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Public Multi-User Wasteland Network via Firebase Firestore + Google Sign-In**
  - *Chosen Approach*: Provision Firebase Firestore & Auth (`CredentialManager` Google Sign-In) with hardened `firestore.rules` so authenticated players can view all public Vaults on the Global Overseer Leaderboard (`public_vaults/{userId}`), post/claim shared Supply Drops (`public_supply_drops/{dropId}`), and back up their own Vault state (`users/{userId}/vault_backups/latest`).
  - *Why*: Makes the game genuinely public and interactive for multiple users while enforcing strict per-user write ownership and schema validation in Firestore security rules.
- **Decision 2: Collapsible Top Telemetry Drawer with `Resources Gathered` & `Systems Online` Tabs**
  - *Chosen Approach*: Integrate a 2-tab expandable drawer directly into `WastelandSkyHeader` that can be switched or collapsed with a single tap from any screen.
  - *Why*: Gives players immediate access to all resource rates, daily worker food/water demands, online systems, and active build/craft timers without crowding the bottom navigation bar.
- **Decision 3: Strict Daily Worker Food & Water Requirement & Permanent `OVERSEER LEGACY` Skill Branch**
  - *Chosen Approach*: Workers consume daily Food & Water rations and pause room/turret operation if rations hit `0`; Prestige resets award Prestige Cores spent in the permanent `OVERSEER LEGACY` skill branch.
  - *Why*: Connects daily survival pressure directly to long-term Prestige progression.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & Component Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                  MainActivity (Auth Gate + Scaffold Shell)                  │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │        WastelandSkyHeader (Collapsible Mobile Telemetry Drawer)       │  │
│  │  ┌─────────────────────────────────┬───────────────────────────────┐  │  │
│  │  │   Tab 1: RESOURCES GATHERED     │   Tab 2: SYSTEMS ONLINE       │  │  │
│  │  │ • Primary Stockpiles & Rates/s  │ • Power MW, Wear & Blackout   │  │  │
│  │  │ • Storage Cap Progress Bars     │ • Auto-Defense 3-Check Status │  │  │
│  │  │ • Daily Worker Food/Water Need  │ • O2, Rads, Ration & Rooms    │  │  │
│  │  │ • Tactical Gear & Prestige Core │ • Build/Craft/Exp Timers      │  │  │
│  │  └─────────────────────────────────┴───────────────────────────────┘  │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
│                                      │                                      │
│  ┌───────────┬───────────────┬───────┴───────┬───────────────┬───────────┐  │
│  │ 1. Vault  │ 2. Armory     │ 3. Crew       │ 4. Grid/Public│ 5. Tech   │  │
│  │ Rooms &   │ Timed Crafts, │ Hire Dwellers │ Power, Trade  │ 4-Branch  │  │
│  │ Salvage   │ Night Swarms  │ (8% Unique) & │ & Public      │ SkillTree │  │
│  │ Yard      │ & Auto-Def    │ Daily Rations │ Vault Network │ +Prestige │  │
│  └───────────┴───────────────┴───────────────┴───────────────┴───────────┘  │
└───────────────────┬─────────────────────────────────────┬───────────────────┘
                    ▼                                     ▼
┌─────────────────────────────────────────┐ ┌─────────────────────────────────┐
│     Local Simulation & Persistence      │ │ Public Cloud Network (Firebase) │
│ • OutpostViewModel & OutpostCalculator  │ │ • Google Sign-In (CredentialMgr)│
│ • Daily Worker Food/Water Ration Gate   │ │ • public_vaults/{userId}        │
│ • Prestige Reset & LEG_* Skill Branch   │ │   (Live Public Leaderboard)     │
│ • Room SQLite (OutpostDatabase v9)      │ │ • public_supply_drops/{dropId}  │
└─────────────────────────────────────────┘ └─────────────────────────────────┘
```

### Data Model & State Updates

1. **Public Cloud Collections (`firebase-blueprint.json` & `firestore.rules`)**:
   - `public_vaults/{userId}`: Readable by any authenticated user (`request.auth != null`), writable only by the owner (`request.auth.uid == userId`). Stores `userId`, `overseerName`, `vaultName`, `prestigeLevel`, `prestigeCores`, `commandLevel`, `dayCounter`, `totalSurvivors`, `uniqueDwellersCount`, `totalDefenseRating`, `updatedAt`.
   - `public_supply_drops/{dropId}`: Readable by any authenticated user; creatable by any authenticated user (`senderUid == request.auth.uid`); updatable to mark claimed or deleted by sender. Stores `senderUid`, `senderName`, `resourceType`, `amount`, `message`, `claimedByUid`, `claimedByName`, `createdAt`, `updatedAt`.
2. **Local Persistence (`OutpostStateEntity` & `OutpostRepository`)**:
   - Add `val prestigeLevel: Int = 0`, `val prestigeCores: Int = 0`, `val totalPrestigeCoresEarned: Int = 0`, and `val autoDefenseEnabled: Boolean = true` to `OutpostStateEntity`.
   - Add `suspend fun performPrestigeReset(newState: OutpostStateEntity)` in `OutpostRepository` that resets `BuildingProgressEntity` levels to `0`, resets `WorkerClassEntity` assigned counts to `0`, deletes non-Legacy skill nodes (`!nodeId.startsWith("LEG_")`), and saves the ascended `OutpostStateEntity`.
3. **Skill Tree & Prestige Calculations (`SkillBranch`, `SkillTreeCatalog`, `OutpostCalculator`)**:
   - Add `OVERSEER_LEGACY("Overseer Legacy", "Permanent Prestige Core upgrades that persist through Vault Ascension", XpGold)` to `SkillBranch`.
   - Add 4 Legacy Skill Nodes (`LEG_CRYO_AWAKENING`, `LEG_NANO_SCHEMATICS`, `LEG_HYDRO_RECYCLING`, `LEG_FUSION_OVERCLOCK`) to `SkillTreeCatalog`.
   - Update `computeProductionSnapshot(...)` to enforce **Daily Worker Food & Water Rations** (`isWorkersStarvingOrDehydrated: Boolean`), **Strict 3-Part Auto-Defense** (`isAutoDefenseActive: Boolean`), and apply Legacy Skill bonuses.
