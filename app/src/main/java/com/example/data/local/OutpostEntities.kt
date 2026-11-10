package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "outpost_state")
data class OutpostStateEntity(
    @PrimaryKey val id: Int = 1,
    val commandLevel: Int = 1, // Max Level is 100!
    val currentXp: Double = 0.0,
    val availableSkillPoints: Int = 1,
    val totalSkillPointsEarned: Int = 1,
    // Start with nothing but a Pickaxe (minimal emergency rations & 0 Gold, 0 Dwellers, 0 Rooms)
    val scrap: Double = 0.0,
    val water: Double = 6.0,
    val rations: Double = 6.0,
    val bioFuel: Double = 0.0,
    val researchData: Double = 0.0,
    val gold: Double = 0.0, // Start with 0 Gold — mine with Pickaxe to find Gold & hire survivors!
    val thoriumRods: Double = 0.0,
    val aetherCells: Double = 0.0,
    val mutagenCores: Double = 0.0,
    val rawSalvageCaches: Int = 0,
    val salvageRigLevel: Int = 1, // Pickaxe Level (Lv.1 Rusty Pickaxe)
    val salvageComboCount: Int = 0,
    val totalGoldSalvaged: Double = 0.0,
    // Vault Defenses & Hunting/Fishing Gear (Start in an Empty Broken Vault requiring repair!)
    val blastDoorLevel: Int = 1, // Vault Blast Door Level
    val defenseIntegrityPercent: Int = 60, // Starts damaged at 60% in your empty broken Vault — must repair!
    val needsDefenseRepair: Boolean = true,
    val totalRepairsCompleted: Int = 0,
    val luckyNoRepairCount: Int = 0,
    val craftedSpears: Int = 1,
    val craftedSnareTraps: Int = 0,
    val aetherCrossbowLevel: Int = 0,
    val craftedShrapnelMines: Int = 0,
    val sentryTurretCount: Int = 0,
    val plasmaCannonCount: Int = 0,
    val totalFishCaught: Int = 0,
    val totalHuntsCompleted: Int = 0,
    val totalRaidsRepelled: Int = 0,
    // Realistic Shelter Life-Support, Radiation, Medical Care & Tactical Gear
    val oxygenPercent: Int = 78, // 0..100% — Starts degraded at 78% until O2 scrubbers are repaired/powered
    val radiationMillisieverts: Int = 15, // 0..1000 mSv — Starts with 15 mSv radiation leak in broken Vault
    val dwellerHealthPercent: Int = 100, // 0..100% — Drops from starvation, dehydration, hypoxia, or rads
    val injuredDwellersCount: Int = 0,
    val generatorWearPercent: Int = 35, // 0..80% — Starts worn at 35%; reduces effective MW until tuned
    val ammoRounds: Int = 20, // Consumed by Powered Turrets during raids & Dwellers on expeditions
    val gasMasksCount: Int = 0, // Reduces expedition radiation & fatality risk (-3%)
    val hazmatSuitsCount: Int = 0, // Lead-lined Hazmat Suit (-4% expedition death risk & -70% rads)
    val medkitsCount: Int = 1, // Emergency starter Medkit
    val radAwayCount: Int = 1, // Emergency starter RadAway
    // Vault Power Generators & Convoys
    val convoyLevel: Int = 1,
    val totalConvoysCompleted: Int = 0,
    val bioTurbineLevel: Int = 0, // Starts at 0 — must crank Dynamo or build Generators for Vault Power!
    val solarTowerLevel: Int = 0,
    val thoriumReactorLevel: Int = 0,
    val aetherSubstationLevel: Int = 0,
    val thoriumOverdriveUntilMillis: Long = 0L,
    // Dwellers & Expeditions (10% Death Chance & 8% Rare Unique Vault Dweller Chance)
    val totalSurvivors: Int = 0, // Start alone with 0 Dwellers — repair Living Quarters & hire with Gold to automate!
    val uniqueDwellersCount: Int = 0, // Rare 8% Unique Vault Dwellers with +40% resource gathering ability each!
    val totalDwellersLost: Int = 0,
    val totalExpeditionsCompleted: Int = 0,
    val cycleProgress: Float = 0.15f,
    val dayCounter: Int = 1,
    val lastAttackDayResolved: Int = 0,
    val solarFlareActiveUntilMillis: Long = 0L,
    val solarFlareCooldownUntilMillis: Long = 0L,
    val eclipseShroudActiveUntilMillis: Long = 0L,
    val eclipseShroudCooldownUntilMillis: Long = 0L,
    val autoShiftEnabled: Boolean = false,
    val autoDefenseEnabled: Boolean = true, // Automated turrets, mines & scrapper door repair!
    val prestigeLevel: Int = 0, // Overseer Ascension Prestige Rank
    val prestigeCores: Int = 0, // Permanent Prestige Cores for Overseer Legacy Skill Branch
    val totalPrestigeCoresEarned: Int = 0,
    val totalAnomaliesClaimed: Int = 0,
    val totalManualScavenges: Int = 0,
    val totalNodesStripped: Int = 0,
    val totalCachesDismantled: Int = 0,
    val blackoutDaysCount: Int = 0, // Consecutive days in Power Blackout (6 days without power = Game Over!)
    val blackoutSecondsAccumulated: Int = 0,
    val deficitDaysCount: Int = 0, // Consecutive days where Vault takes more resources to run than it gains (2 days = Game Over!)
    val deficitSecondsAccumulated: Int = 0,
    val dwellerFeedCycleSecondsRemaining: Int = 60, // 60-second real-time Food & Water feeding cycle for Vault Dwellers!
    val dwellerAppetiteEscalationSeconds: Int = 0, // Every 2 minutes (120s) Dwellers take more resources!
    val operationalPostureId: String = "STANDARD", // ECO_CONSERVATION (-40% upkeep), STANDARD (100%), or OVERCLOCK (+30% output)
    val manualDynamoCranksRemaining: Int = 4, // Can manual crank for power 4 times every time power goes out!
    val activeDynamoCrankStack: Int = 0, // Stacked manual dynamo cranks during active boost (+16 MW per crank)
    // Hardcore Mining, Building & Hunting Mechanics
    val pickaxeDurabilityPercent: Int = 100, // 0..100% — Wears down per swing; sharpen with Scrap to restore full yield
    val miningShaftHeatPercent: Int = 0, // 0..100% — Builds up during rapid deep-core mining; cool with Water or risk cave-in
    val activeCaveInHazard: Boolean = false, // Tunnel collapse hazard requiring Scrap shoring beams to clear
    val activePipeLeaksCount: Int = 0, // 0..5 — Coolant & water pipe leaks in Vault rooms draining extra Water/Scrap
    val roomStructuralWearPercent: Int = 0, // 0..80% — Room machinery wear reducing room output until welded
    val bleedingDwellersCount: Int = 0, // Beast counter-attack bleeding trauma from Wasteland Hunts
    val venomInfectedDwellersCount: Int = 0, // Mutant rad-venom infection from Wasteland Hunts
    // Persisted Active Room Construction for Offline Completion & Phone Notifications
    val activeConstructionBuildingId: String = "",
    val activeConstructionTargetLevel: Int = 0,
    val activeConstructionRemainingSeconds: Int = 0,
    val activeConstructionTotalSeconds: Int = 0,
    val activeConstructionRushGoldCost: Double = 0.0,
    val isGameOver: Boolean = false,
    val gameOverReason: String = "",
    val lastSavedTimestampMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "buildings")
data class BuildingEntity(
    @PrimaryKey val buildingTypeId: String,
    val level: Int
)

@Entity(tableName = "worker_classes")
data class WorkerClassEntity(
    @PrimaryKey val workerClassId: String,
    val assignedCount: Int = 0,
    val classLevel: Int = 1,
    val classXp: Double = 0.0
)

@Entity(tableName = "skill_nodes")
data class SkillNodeEntity(
    @PrimaryKey val nodeId: String,
    val currentRank: Int = 0
)

@Entity(tableName = "event_logs")
data class EventLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val category: String,
    val message: String,
    val timestampMillis: Long = System.currentTimeMillis()
)
