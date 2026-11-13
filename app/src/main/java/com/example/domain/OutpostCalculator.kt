package com.example.domain

import com.example.data.local.OutpostStateEntity
import com.example.data.local.WorkerClassEntity
import com.example.model.BuildingType
import com.example.model.CommandLevelProgression
import com.example.model.CraftableItemType
import com.example.model.DayNightPhase
import com.example.model.FishingSpot
import com.example.model.GridLoadBreakerMode
import com.example.model.HuntingTarget
import com.example.model.PowerStationType
import com.example.model.RareSyndicateItem
import com.example.model.ResourceType
import com.example.model.SalvageNodeType
import com.example.model.SkillNodeDefinition
import com.example.model.SubterraneanBossType
import com.example.model.TradeTown
import com.example.model.TurretAmmoType
import com.example.model.VaultCascadeStage
import com.example.model.VaultDirectiveStatus
import com.example.model.VaultOperationalPosture
import com.example.model.VaultRationingPolicy
import com.example.model.VaultTechEra
import com.example.model.RoomMergeTier
import com.example.model.WastelandExpeditionType
import com.example.model.WastelandSeason
import com.example.model.WorkerClassType
import com.example.model.computeSpecialProfile
import com.example.model.primarySpecialStat
import kotlin.math.pow
import kotlin.math.roundToInt

data class ProductionSnapshot(
    val scrapPerSec: Double,
    val waterPerSec: Double,
    val rationsPerSec: Double,
    val bioFuelPerSec: Double,
    val researchPerSec: Double,
    val mutagenCoresPerSec: Double,
    val xpPerSec: Double,
    val storageCap: Double,
    val maxWorkerCapacity: Int,
    val maxPickaxeLevelAllowed: Int,
    val dailyRoomFoodBonus: Double,
    val dailyRoomWaterBonus: Double,
    val dailyRoomScrapBonus: Double,
    val totalAssignedWorkers: Int,
    val citizenMoraleMultiplier: Double,
    val citizenMoralePercent: Int,
    val citizenFoodUpkeepPerSec: Double,
    val citizenWaterUpkeepPerSec: Double,
    val dailyFoodUpkeepPerDay: Double,
    val dailyWaterUpkeepPerDay: Double,
    val totalMegawattGenerated: Double,
    val totalMegawattDemand: Double,
    val isVaultBlackout: Boolean,
    val isVaultBrownout: Boolean = false,
    val powerGridEfficiencyMultiplier: Double,
    val oxygenDeltaPerSec: Double = 0.0,
    val radiationDeltaPerSec: Double = 0.0,
    val totalDefenseRating: Int,
    val todayRequiredDefense: Int,
    val tomorrowRequiredDefense: Int = 28,
    val day10RequiredDefense: Int,
    val daysUntilDay10Siege: Int,
    val passiveFoodFromTrapsPerSec: Double,
    val salvageComboMultiplier: Double,
    val activePhase: DayNightPhase,
    val solarFlareActive: Boolean,
    val eclipseShroudActive: Boolean,
    val thoriumOverdriveActive: Boolean,
    val commandLevelBonusMultiplier: Double,
    val uniqueDwellerBonusMultiplier: Double = 1.0,
    val prestigeBonusMultiplier: Double = 1.0,
    val isAutoDefenseActive: Boolean = false,
    val isWorkersStarvingOrDehydrated: Boolean = false,
    val onlineRoomsCount: Int = 0,
    val totalBuiltRoomsCount: Int = 0,
    val roomScrapUpkeepPerSec: Double = 0.0,
    val roomWaterUpkeepPerSec: Double = 0.0,
    val roomFuelUpkeepPerSec: Double = 0.0,
    val dailyRoomScrapUpkeep: Double = 0.0,
    val dailyRoomWaterUpkeep: Double = 0.0,
    val dailyRoomFuelUpkeep: Double = 0.0,
    val vaultLevelResourceUpkeep: RoomResourceUpkeep = RoomResourceUpkeep(),
    val totalGrossResourceGainPerSec: Double = 0.0,
    val totalVaultResourceDrainPerSec: Double = 0.0,
    val netResourceBalancePerSec: Double = 0.0,
    val isMakingPositiveResources: Boolean = false,
    val reactorFuelUpkeepPerSec: Double = 0.0,
    val storeRoomStorageBonus: Double = 0.0,
    val isVaultResourceDeficit: Boolean = false,
    val isDefenseShutDownByDeficit: Boolean = false,
    val isEarlyBunkerGraceActive: Boolean = false,
    val operationalPosture: VaultOperationalPosture = VaultOperationalPosture.STANDARD,
    val deficitDaysRemainingBeforeGameOver: Int = 6,
    val blackoutDaysRemainingBeforeGameOver: Int = 6,
    val isPreparationPeaceWindow: Boolean = true,
    val preparationDaysRemaining: Int = 10,
    val firstAttackDay11RequiredDefense: Int = 14,
    val manualActionStaminaMultiplier: Double = 1.0,
    val dwellerUpkeepEscalationTier: Int = 0,
    val dwellerUpkeepEscalationMultiplier: Double = 1.0,
    val fiveMinDwellerFoodCost: Double = 0.0,
    val fiveMinDwellerWaterCost: Double = 0.0,
    val pipeLeakWaterDrainPerSec: Double = 0.0,
    val pipeLeakScrapDrainPerSec: Double = 0.0,
    val roomWearPenaltyMultiplier: Double = 1.0,
    val pickaxeDurabilityMultiplier: Double = 1.0,
    val foodSpoilagePercentPerDay: Double = 4.5,
    val dailyFoodSpoilageLoss: Double = 0.0,
    val isCryoPreservationActive: Boolean = false,
    val edenCorePhase: Int = 0,
    val certificationTier: Int = 0,
    val isSelfSustainedCertified: Boolean = false,
    val consecutiveDaysStable: Int = 0,
    val sabotagedBuildingId: String = "",
    val sabotagedBuildingPenaltyPercent: Int = 0,
    val activeGridBreakerMode: GridLoadBreakerMode = GridLoadBreakerMode.BALANCED,
    val activeAmmoType: TurretAmmoType = TurretAmmoType.STANDARD_FMJ,
    val activeSubterraneanBoss: SubterraneanBossType? = null,
    val staminaEfficiencyMultiplier: Double = 1.0,
    val goldPerSec: Double = 0.0,
    val dailyRoomGoldBonus: Double = 0.0,
    val dailyRoomFuelBonus: Double = 0.0,
    val goldMineSmallFuelPerSec: Double = 0.0,
    val dailyAutoRepairScrapCost: Double = 0.0,
    val needsAutomatedSystemRepair: Boolean = false,
    val relicOutputBonusPercent: Double = 0.0,
    val relicDefenseBonus: Int = 0,
    val relicMegawattBonus: Double = 0.0,
    val relicStorageBonus: Double = 0.0,
    val relicAutoRepairDiscountPercent: Int = 0,
    val isStoreRoomFull: Boolean = false,
    val isAnyStoreRoomResourceHalted: Boolean = false,
    val isScrapHalted: Boolean = false,
    val isWaterHalted: Boolean = false,
    val isRationsHalted: Boolean = false,
    val isFuelHalted: Boolean = false,
    val isResearchHalted: Boolean = false,
    val haltedResourcesSummary: String = "",
    val totalStoredResources: Double = 0.0,
    val storeRoomResumeThreshold: Double = 0.0,
    val storageSlowdownMultiplier: Double = 1.0,
    val storageFillPercent: Int = 0,
    val dwellerGearDefenseBonus: Int = 0,
    val hasWaterPurifierRoom: Boolean = false,
    val hasHydroponicsRoom: Boolean = false,
    val isRoomsLackingScrap: Boolean = false,
    val roomScrapOperationalMultiplier: Double = 1.0,
    val roomUpgradesStorageBonus: Double = 0.0,
    // Hardcore Bunker Governance, Seasons, Mothballing & Directives
    val activeRationingPolicy: VaultRationingPolicy = VaultRationingPolicy.STANDARD_RATIONS,
    val activeWastelandSeason: WastelandSeason = WastelandSeason.SCORCHING_FALLOUT,
    val nextWastelandSeason: WastelandSeason = WastelandSeason.ACID_MONSOON,
    val daysRemainingInSeason: Int = 5,
    val vaultTechEra: VaultTechEra = VaultTechEra.ERA_1_SURVIVAL_BUNKER,
    val cascadeStage: VaultCascadeStage = VaultCascadeStage.STAGE_0_STABLE,
    val cascadeCrisisSummary: String = "All life-support, power, and maintenance loops stable.",
    val mothballedRoomsCount: Int = 0,
    val mothballedMwSaved: Int = 0,
    val mothballedBuildingIds: Set<String> = emptySet(),
    val currentDirective: VaultDirectiveStatus = VaultDirectiveStatus(
        tier = 1,
        title = "Directive I: Hydro & Food Autonomy",
        objectiveSummary = "Construct Water Purifier (Lv.1+) & Hydroponics Greenhouse (Lv.1+).",
        progressText = "0/1",
        progressFraction = 0f,
        isCompleted = false,
        goldReward = 65.0,
        researchReward = 40.0,
        coresReward = 1.0,
        permanentBlueprintSummary = "+2% Permanent All-Vault Output & +25 Max Resource Storage Cap"
    ),
    val directiveOutputBonusPercent: Int = 0,
    val directiveStorageBonus: Double = 0.0
) {
    val vaultCascadeStage: VaultCascadeStage
        get() = cascadeStage
    val moralePercent: Int
        get() = citizenMoralePercent
}

data class AutomatedDailyRepairPlan(
    val totalRoomLevels: Int = 0,
    val needsAutomatedRepair: Boolean = false,
    val scrapCost: Double = 0.0,
    val waterCost: Double = 0.0,
    val fuelCost: Double = 0.0,
    val doorIntegrityRestore: Int = 0,
    val generatorWearReduction: Int = 0,
    val roomWearReduction: Int = 0,
    val pipeLeaksSealed: Int = 0,
    val clearsSabotage: Boolean = false
)

data class EdenCoreModuleCost(
    val phaseNumber: Int,
    val title: String,
    val description: String,
    val scrapCost: Double,
    val waterCost: Double = 0.0,
    val rationsCost: Double = 0.0,
    val fuelCost: Double = 0.0,
    val researchCost: Double = 0.0,
    val goldCost: Double = 0.0,
    val thoriumCost: Double = 0.0,
    val aetherCost: Double = 0.0,
    val coresCost: Double = 0.0,
    val requiredBuildingSummary: String,
    val canAfford: Boolean,
    val requirementsMet: Boolean,
    val isBuilt: Boolean
)

data class CertificationTierPreview(
    val tierNumber: Int,
    val title: String,
    val requirementSummary: String,
    val isMet: Boolean,
    val isCertified: Boolean
)

data class OfflineNotificationSchedule(
    val secondsUntilRoomUpgradeComplete: Long? = null,
    val upgradingRoomTitle: String = "",
    val upgradingRoomTargetLevel: Int = 0,
    val secondsUntilResourcesAtMax: Long? = null,
    val maxResourceSummary: String = "",
    val secondsUntilPowerOutage: Long? = null,
    val powerOutageReasonSummary: String = ""
)

data class RoomResourceUpkeep(
    val scrapPerSec: Double = 0.0,
    val waterPerSec: Double = 0.0,
    val fuelPerSec: Double = 0.0,
    val dailyScrap: Double = 0.0,
    val dailyWater: Double = 0.0,
    val dailyFuel: Double = 0.0
)

data class SurvivalDayEvaluation(
    val nextBlackoutDaysCount: Int,
    val nextDeficitDaysCount: Int,
    val isGameOver: Boolean,
    val gameOverReason: String
)

data class PrestigePreview(
    val canAscend: Boolean,
    val coresToEarn: Int,
    val currentPrestigeLevel: Int,
    val currentPrestigeCores: Int,
    val currentPrestigeMultiplier: Double,
    val nextPrestigeMultiplier: Double,
    val totalBuiltRoomLevels: Int,
    val requirementSummary: String
)

data class DefenseRepairCost(
    val currentIntegrityPercent: Int,
    val missingIntegrityPercent: Int,
    val scrapCost: Double,
    val goldCost: Double,
    val needsRepair: Boolean,
    val canAfford: Boolean
)

data class BlastDoorUpgradeCost(
    val currentLevel: Int,
    val targetLevel: Int = 2,
    val scrapCost: Double,
    val goldCost: Double,
    val defenseBonusNextLevel: Int,
    val upgradeTimeSeconds: Int = 12,
    val nextLevelUpgradeTimeSeconds: Int = 24,
    val rushGoldCost: Double = 8.0,
    val canAfford: Boolean
)

data class FishingPreview(
    val spot: FishingSpot,
    val foodReward: Double,
    val waterReward: Double,
    val goldReward: Double,
    val xpReward: Double,
    val canFish: Boolean
)

data class RescueBeaconCost(
    val bioFuelCost: Double,
    val rationsCost: Double,
    val waterCost: Double,
    val hasHousingSpace: Boolean,
    val canAfford: Boolean
)

data class BuildingUpgradeCost(
    val scrapCost: Double,
    val waterCost: Double,
    val fuelCost: Double,
    val coresCost: Double,
    val roomNumber: Int = 1,
    val targetRoomLevel: Int = 1,
    val currentMegawattDraw: Double = 0.0,
    val nextLevelMegawattDraw: Double = 0.0,
    val additionalMegawattDraw: Double = 0.0,
    val currentRoomUpkeep: RoomResourceUpkeep = RoomResourceUpkeep(),
    val nextLevelRoomUpkeep: RoomResourceUpkeep = RoomResourceUpkeep(),
    val storageCapBonusCurrent: Double = 0.0,
    val storageCapBonusNextLevel: Double = 0.0,
    val additionalStorageCapGain: Double = 0.0,
    val exceedsCurrentStorageCap: Boolean = false,
    val buildTimeSeconds: Int = 8,
    val nextLevelBuildTimeSeconds: Int = 20,
    val rushGoldCost: Double = 10.0,
    val willExceedPowerGrid: Boolean = false,
    val canAfford: Boolean,
    val meetsLevelRequirement: Boolean
)

data class PowerStationUpgradeCost(
    val currentLevel: Int,
    val scrapCost: Double,
    val goldCost: Double,
    val thoriumCost: Double,
    val aetherCost: Double,
    val coresCost: Double,
    val currentFuelUpkeepPerSec: Double = 0.0,
    val nextLevelFuelUpkeepPerSec: Double = 0.0,
    val canAfford: Boolean
)

data class ConvoyUpgradeCost(
    val currentLevel: Int,
    val scrapCost: Double,
    val goldCost: Double,
    val thoriumCost: Double,
    val aetherCost: Double,
    val canAfford: Boolean
)

data class CraftItemCost(
    val scrapCost: Double,
    val fuelCost: Double,
    val goldCost: Double,
    val aetherCost: Double,
    val thoriumCost: Double,
    val coresCost: Double,
    val craftTimeSeconds: Int = 8,
    val rushGoldCost: Double = 6.0,
    val canAfford: Boolean
)

data class HuntPreview(
    val target: HuntingTarget,
    val foodReward: Double,
    val waterReward: Double,
    val goldReward: Double,
    val coresReward: Double,
    val xpReward: Double,
    val counterAttackInjuryChancePercent: Int = 12,
    val ammoRecommended: Int = 2,
    val canExecute: Boolean
)

data class SkillUpgradeCost(
    val skillPointCost: Int,
    val researchCost: Double,
    val goldCost: Double,
    val coresCost: Double,
    val prestigeCoreCost: Int = 0,
    val canAfford: Boolean,
    val prerequisiteMet: Boolean,
    val isMaxed: Boolean
)

data class SalvageYieldPreview(
    val primaryAmount: Double,
    val secondaryAmount: Double,
    val goldAmount: Double,
    val tapPrimaryAmount: Double,
    val tapGoldAmount: Double,
    val xpReward: Double,
    val cacheDropCount: Int,
    val bonusMutagenCores: Double,
    val rareJackpotChancePercent: Int,
    val tapFuelAmount: Double = 0.0,
    val bonusFuelFromGoldMine: Double = 0.0
)

object OutpostCalculator {

    const val BASE_STORAGE_CAP = 150.0
    private const val BASE_MAX_CITIZENS = 4
    const val MAX_VAULT_DWELLERS = 30 // Maximum Dwellers allowed in the Vault (30 Max • +4 per Hab Block level)
    const val DWELLER_FEED_INTERVAL_SECONDS = 60 // Every 60 seconds real-time feeding cycle
    const val DWELLER_ESCALATION_INTERVAL_SECONDS = 120 // Every 2 minutes Dwellers consume more resources
    const val PREPARATION_PEACE_DAYS = 10 // Strictly zero attacks on Days 1 to 10; first attack on Day 11!
    const val FIRST_ATTACK_DAY = 11
    const val MAX_DEFICIT_DAYS_BEFORE_GAME_OVER = 6 // 6 days out of resources / deficit = Game Over!
    const val MAX_BLACKOUT_DAYS_BEFORE_GAME_OVER = 6 // 6 days without power = Game Over!
    const val MAX_DYNAMO_CRANKS_PER_BLACKOUT = 4 // Can manually crank for power 4 times every time power goes out!
    const val MANUAL_ACTION_STAMINA_COST = 4 // Every manual mining/forage/boil action costs 4% Stamina so every action matters!

    // Small Outpost Bonuses per Discovered Pre-Fall Relic
    const val RELIC_OUTPUT_BONUS_PER_RELIC = 0.015 // +1.5% Outpost Resource Output per Relic
    const val RELIC_DEFENSE_BONUS_PER_RELIC = 2 // +2 Outpost Defense Rating per Relic
    const val RELIC_MEGAWATT_BONUS_PER_RELIC = 0.3 // +0.3 MW Clean Auxiliary Power per Relic
    const val RELIC_STORAGE_BONUS_PER_RELIC = 15.0 // +15 Storage Capacity per Relic
    const val RELIC_MORALE_BONUS_PER_RELIC = 0.01 // +1% Crew Morale per Relic
    const val RELIC_AUTO_REPAIR_DISCOUNT_PER_RELIC = 0.01 // -1% Daily Auto-Repair Scrap Cost per Relic
    const val SCRAP_GATHER_RATE_MULTIPLIER = 0.80 // 20% lower Scrap gather rate across automated & manual gathering
    const val STORE_ROOM_RESUME_THRESHOLD_RATIO = 0.50 // Resources stay in Storage-Full Slowdown until Store Room drops to half (50%) capacity
    const val STORAGE_FULL_SLOWDOWN_MULTIPLIER = 0.25 // Resources gather at 25% speed (-75% slower) when storage shelf is full or latched above 50% capacity
    const val STORAGE_NEAR_FULL_SLOWDOWN_MULTIPLIER = 0.55 // Resources gather at 55% speed (-45% slower) when storage shelf is >= 90% full
    const val DWELLER_ARMOR_DEFENSE_BONUS = 12 // +12 Extra Vault Defense per Dweller Armor salvaged on Expeditions
    const val DWELLER_WEAPON_DEFENSE_BONUS = 15 // +15 Extra Vault Defense per Dweller Weapon salvaged on Expeditions
    const val PICKAXE_SWING_HEAT_DELTA = 0 // Swinging pickaxe does NOT cause heat!
    const val MOTHBALLED_UPKEEP_MULTIPLIER = 0.20 // Mothballed (Standby) rooms draw 0 MW and take -80% daily maintenance!
    const val DIRECTIVE_OUTPUT_BONUS_PER_TIER = 0.02 // +2% Permanent Output per claimed Overseer Directive Tier
    const val DIRECTIVE_STORAGE_BONUS_PER_TIER = 25.0 // +25 Permanent Storage Cap per claimed Overseer Directive Tier

    fun parseMothballedSet(csv: String): Set<String> {
        if (csv.isBlank()) return emptySet()
        return csv.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    fun toggleMothballedBuildingCsv(csv: String, buildingId: String): String {
        val current = parseMothballedSet(csv).toMutableSet()
        if (buildingId in current) {
            current.remove(buildingId)
        } else {
            current.add(buildingId)
        }
        return current.sorted().joinToString(",")
    }

    fun computeVaultTechEra(
        state: OutpostStateEntity,
        buildingsMap: Map<String, Int>
    ): VaultTechEra {
        val hasPurifier = (buildingsMap[BuildingType.ATMOSPHERIC_CONDENSER.id] ?: 0) >= 1
        val hasHydroponics = (buildingsMap[BuildingType.HYDROPONIC_GREENHOUSE.id] ?: 0) >= 1
        val hasGeothermalOrCryo = (buildingsMap[BuildingType.GEOTHERMAL_TAP.id] ?: 0) >= 1 ||
            (buildingsMap[BuildingType.CRYO_STASIS_VAULT.id] ?: 0) >= 1
        return when {
            state.edenCorePhase >= 1 || state.commandLevel >= 12 || state.prestigeLevel >= 1 ->
                VaultTechEra.ERA_5_EDEN_METROPOLIS
            state.thoriumReactorLevel >= 1 || state.aetherSubstationLevel >= 1 || state.commandLevel >= 8 ->
                VaultTechEra.ERA_4_ATOMIC_AUTOMATION
            hasGeothermalOrCryo || state.deepCoreDepthMeters >= 150 || state.commandLevel >= 5 ->
                VaultTechEra.ERA_3_DEEP_THERMAL
            (hasPurifier && hasHydroponics) || state.totalSurvivors >= 2 || state.commandLevel >= 2 ->
                VaultTechEra.ERA_2_HYDRO_INDUSTRIAL
            else -> VaultTechEra.ERA_1_SURVIVAL_BUNKER
        }
    }

    fun computeVaultCascadeStage(
        state: OutpostStateEntity,
        isVaultBlackout: Boolean,
        isVaultBrownout: Boolean,
        isVaultResourceDeficit: Boolean,
        isWorkersStarvingOrDehydrated: Boolean,
        isRoomsLackingScrap: Boolean,
        citizenMoralePercent: Int
    ): Pair<VaultCascadeStage, String> {
        var severity = 0
        val reasons = mutableListOf<String>()
        if (isVaultBlackout) {
            severity += 2
            reasons.add("Power Grid Blackout (Crank Dynamo or Mothball rooms)")
        } else if (isVaultBrownout) {
            severity += 1
            reasons.add("Grid Brownout")
        }
        if (isVaultResourceDeficit) {
            severity += 2
            reasons.add("Resource Deficit (Mothball high-level rooms or enact Half-Rations)")
        }
        if (isWorkersStarvingOrDehydrated) {
            severity += 2
            reasons.add("Crew Starving/Dehydrated")
        }
        if (isRoomsLackingScrap) {
            severity += 1
            reasons.add("0 Scrap for Room Operations (-35% room output)")
        }
        if (state.activePipeLeaksCount > 0) {
            severity += 1
            reasons.add("${state.activePipeLeaksCount} Coolant Pipe Leak(s)")
        }
        if (state.roomStructuralWearPercent >= 35 || state.generatorWearPercent >= 45) {
            severity += 1
            reasons.add("High Machinery/Generator Wear")
        }
        if (state.dwellerStaminaPercent < 35 || citizenMoralePercent < 55) {
            severity += 1
            reasons.add("Crew Fatigue/Low Morale (Enable Rest Rotation or Feast Rations)")
        }
        if (state.oxygenPercent < 60 || state.radiationMillisieverts >= 120) {
            severity += 1
            reasons.add("Hypoxia/Radiation Hazard")
        }

        val stage = when {
            severity <= 0 -> VaultCascadeStage.STAGE_0_STABLE
            severity == 1 -> VaultCascadeStage.STAGE_1_WARNING
            severity == 2 -> VaultCascadeStage.STAGE_2_SUBSYSTEM_STRAIN
            else -> VaultCascadeStage.STAGE_3_CRITICAL_CASCADE
        }
        val summary = if (reasons.isEmpty()) {
            "All life-support, power, and daily room maintenance loops operating within safe tolerances."
        } else {
            reasons.joinToString(" • ")
        }
        return stage to summary
    }

    fun computeCurrentVaultDirective(
        state: OutpostStateEntity,
        buildingsMap: Map<String, Int>,
        totalDefenseRating: Int
    ): VaultDirectiveStatus {
        val claimedTier = state.claimedDirectiveTier.coerceAtLeast(0)
        val nextTier = claimedTier + 1
        val totalRoomLevels = buildingsMap.values.filter { it > 0 }.sum()
        val hasPurifier = (buildingsMap[BuildingType.ATMOSPHERIC_CONDENSER.id] ?: 0) >= 1
        val hasHydro = (buildingsMap[BuildingType.HYDROPONIC_GREENHOUSE.id] ?: 0) >= 1
        val totalGenLv = state.bioTurbineLevel + state.solarTowerLevel + state.thoriumReactorLevel + state.aetherSubstationLevel

        return when (nextTier) {
            1 -> {
                val stepsDone = (if (hasPurifier) 1 else 0) + (if (hasHydro) 1 else 0) + (if (totalGenLv >= 1 || totalRoomLevels >= 3) 1 else 0)
                VaultDirectiveStatus(
                    tier = 1,
                    title = "Directive I: Core Life-Support Lifelines",
                    objectiveSummary = "Build Water Purifier (#3), Hydroponics (#4), and 1+ Power Generator (or 3 Total Room Levels).",
                    progressText = "$stepsDone / 3 Milestones (Purifier: ${if (hasPurifier) "✓" else "✗"}, Hydroponics: ${if (hasHydro) "✓" else "✗"}, Power/Rooms: ${if (totalGenLv >= 1 || totalRoomLevels >= 3) "✓" else "✗"})",
                    progressFraction = (stepsDone / 3f).coerceIn(0f, 1f),
                    isCompleted = stepsDone >= 3,
                    goldReward = 35.0,
                    researchReward = 25.0,
                    coresReward = 1.0,
                    permanentBlueprintSummary = "+2% Permanent All-Vault Output & +25 Max Resource Storage Cap"
                )
            }
            2 -> {
                val crewTarget = 3
                val roomsTarget = 5
                val crewProgress = state.totalSurvivors.coerceAtMost(crewTarget)
                val roomsProgress = totalRoomLevels.coerceAtMost(roomsTarget)
                val frac = ((crewProgress.toFloat() / crewTarget) * 0.5f + (roomsProgress.toFloat() / roomsTarget) * 0.5f).coerceIn(0f, 1f)
                val done = state.totalSurvivors >= crewTarget && totalRoomLevels >= roomsTarget
                VaultDirectiveStatus(
                    tier = 2,
                    title = "Directive II: Colony Staffing & Shelving",
                    objectiveSummary = "Recruit at least 3 Vault Dwellers and upgrade 5+ Total Room Levels.",
                    progressText = "Dwellers: ${state.totalSurvivors}/$crewTarget • Room Levels: $totalRoomLevels/$roomsTarget",
                    progressFraction = frac,
                    isCompleted = done,
                    goldReward = 60.0,
                    researchReward = 45.0,
                    coresReward = 2.0,
                    permanentBlueprintSummary = "+2% Permanent All-Vault Output & +25 Max Resource Storage Cap"
                )
            }
            3 -> {
                val defTarget = 110
                val depthTarget = 150
                val defFrac = (totalDefenseRating.toFloat() / defTarget).coerceIn(0f, 1f)
                val depthFrac = (state.deepCoreDepthMeters.toFloat() / depthTarget).coerceIn(0f, 1f)
                val done = totalDefenseRating >= defTarget && state.deepCoreDepthMeters >= depthTarget
                VaultDirectiveStatus(
                    tier = 3,
                    title = "Directive III: Fortress Gate & Deep Borehole",
                    objectiveSummary = "Reach 110+ Defense Rating and drill to 150m+ Deep-Core Depth.",
                    progressText = "Defense: $totalDefenseRating/$defTarget DEF • Borehole: ${state.deepCoreDepthMeters}m/${depthTarget}m",
                    progressFraction = (defFrac * 0.5f + depthFrac * 0.5f).coerceIn(0f, 1f),
                    isCompleted = done,
                    goldReward = 95.0,
                    researchReward = 70.0,
                    coresReward = 3.0,
                    permanentBlueprintSummary = "+2% Permanent All-Vault Output & +25 Max Resource Storage Cap"
                )
            }
            4 -> {
                val dayTarget = 6
                val roomsTarget = 9
                val dayFrac = (state.dayCounter.toFloat() / dayTarget).coerceIn(0f, 1f)
                val roomFrac = (totalRoomLevels.toFloat() / roomsTarget).coerceIn(0f, 1f)
                val done = state.dayCounter >= dayTarget && totalRoomLevels >= roomsTarget
                VaultDirectiveStatus(
                    tier = 4,
                    title = "Directive IV: Seasonal Endurance & Multi-Floor Grid",
                    objectiveSummary = "Reach Day 6+ (Season 2) and maintain 9+ Total Room Levels.",
                    progressText = "Day: ${state.dayCounter}/$dayTarget • Total Room Levels: $totalRoomLevels/$roomsTarget",
                    progressFraction = (dayFrac * 0.5f + roomFrac * 0.5f).coerceIn(0f, 1f),
                    isCompleted = done,
                    goldReward = 140.0,
                    researchReward = 100.0,
                    coresReward = 4.0,
                    permanentBlueprintSummary = "+2% Permanent All-Vault Output & +25 Max Resource Storage Cap"
                )
            }
            else -> {
                val extraTier = nextTier - 4
                val targetRooms = 11 + extraTier * 2
                val targetDepth = 300 + extraTier * 150
                val roomFrac = (totalRoomLevels.toFloat() / targetRooms).coerceIn(0f, 1f)
                val depthFrac = (state.deepCoreDepthMeters.toFloat() / targetDepth).coerceIn(0f, 1f)
                val done = totalRoomLevels >= targetRooms || state.deepCoreDepthMeters >= targetDepth
                VaultDirectiveStatus(
                    tier = nextTier,
                    title = "Directive #$nextTier: Master Overseer Mandate Mk.$extraTier",
                    objectiveSummary = "Reach $targetRooms+ Total Room Levels OR drill to ${targetDepth}m+ Deep-Core Depth.",
                    progressText = "Room Levels: $totalRoomLevels/$targetRooms • Borehole: ${state.deepCoreDepthMeters}m/${targetDepth}m",
                    progressFraction = maxOf(roomFrac, depthFrac).coerceIn(0f, 1f),
                    isCompleted = done,
                    goldReward = 150.0 + extraTier * 35.0,
                    researchReward = 110.0 + extraTier * 25.0,
                    coresReward = (4 + extraTier).toDouble(),
                    permanentBlueprintSummary = "+2% Permanent All-Vault Output & +25 Max Resource Storage Cap"
                )
            }
        }
    }

    fun computePickaxeSwingNextHeat(currentHeat: Int): Int {
        return (currentHeat + PICKAXE_SWING_HEAT_DELTA).coerceIn(0, 100)
    }

    fun computeDwellerGearDefenseBonus(state: OutpostStateEntity): Int {
        return state.dwellerArmorCount.coerceAtLeast(0) * DWELLER_ARMOR_DEFENSE_BONUS +
            state.dwellerWeaponsCount.coerceAtLeast(0) * DWELLER_WEAPON_DEFENSE_BONUS
    }

    /**
     * Slower early room scaling: Rooms start at Level 0 in a degraded trickle state (15% efficiency),
     * moving to 28% at Lv.1, 42% at Lv.2, 60% at Lv.3, and reaching strong self-sustaining surplus only at Lv.4+ (85%+)!
     */
    fun computeRoomLevelOutputMultiplier(level: Int): Double {
        val safeLevel = level.coerceAtLeast(0)
        return when (safeLevel) {
            0 -> 0.15
            1 -> 0.28
            2 -> 0.42
            3 -> 0.60
            4 -> 0.85
            5 -> 1.15
            else -> 1.15 + (safeLevel - 5) * 0.25
        }
    }

    /**
     * Workers assigned to a facility also scale from Level 0 (worst: 35% pace)
     * up to 50% at Lv.1, 65% at Lv.2, 80% at Lv.3, 100% at Lv.4, and exceeding 120%+ at Lv.5+!
     */
    fun computeWorkerLowRoomPacingMultiplier(primaryRoomLevel: Int): Double {
        val safeLevel = primaryRoomLevel.coerceAtLeast(0)
        return when (safeLevel) {
            0 -> 0.35
            1 -> 0.50
            2 -> 0.65
            3 -> 0.80
            4 -> 1.00
            else -> 1.00 + (safeLevel - 4) * 0.18
        }
    }

    /**
     * Effective total output multiplier for a room at [level], starting at Level 0 (worst makeshift trickle)
     * and gradually improving with each upgrade level!
     */
    fun computeRoomEffectiveTotalOutputMultiplier(level: Int): Double {
        val safeLevel = level.coerceAtLeast(0)
        val effectiveUnits = if (safeLevel == 0) 0.25 else safeLevel.toDouble()
        return ((effectiveUnits * computeRoomLevelOutputMultiplier(safeLevel)) * 1000.0).roundToInt() / 1000.0
    }

    /**
     * Higher-level reactors take progressively (super-linearly) more Bio-Fuel per second to run!
     * The higher the level, the more fuel it demands to operate!
     */
    fun computeReactorFuelUpkeepForStation(station: PowerStationType, level: Int): Double {
        if (level <= 0 || station.fuelUpkeepPerLevel <= 0.0) return 0.0
        val extraLevels = level - 1
        val escalation = level * (1.0 + extraLevels * 0.25) + extraLevels * extraLevels * 0.10
        return ((station.fuelUpkeepPerLevel * escalation) * 100.0).roundToInt() / 100.0
    }

    fun computeTotalReactorFuelUpkeepPerSec(
        state: OutpostStateEntity,
        operationalPosture: VaultOperationalPosture = VaultOperationalPosture.fromId(state.operationalPostureId)
    ): Double {
        val rawFuel = computeReactorFuelUpkeepForStation(PowerStationType.BIO_SLUDGE_TURBINE, state.bioTurbineLevel) +
            computeReactorFuelUpkeepForStation(PowerStationType.SOLAR_HELIOSTAT_TOWER, state.solarTowerLevel) +
            computeReactorFuelUpkeepForStation(PowerStationType.THORIUM_FISSION_REACTOR, state.thoriumReactorLevel) +
            computeReactorFuelUpkeepForStation(PowerStationType.AETHER_RESONANCE_SUBSTATION, state.aetherSubstationLevel)
        return ((rawFuel * operationalPosture.upkeepMultiplier) * 100.0).roundToInt() / 100.0
    }

    /**
     * Vault Supply Store Room storage bonus that scales upward with each Store Room upgrade level.
     */
    fun computeStoreRoomStorageBonus(storeRoomLevel: Int): Double {
        if (storeRoomLevel <= 0) return 0.0
        val extraLevels = storeRoomLevel - 1
        return storeRoomLevel * BuildingType.VAULT_STORE_ROOM.storageBonusPerLevel * (1.0 + extraLevels * 0.25)
    }

    /**
     * Every room upgrade in the Vault adds structural resource shelving (+45 at Lv.1, +110 at Lv.2, +205 at Lv.3, +330 at Lv.4, +485 at Lv.5),
     * plus the dedicated storage bonus for Vault Store Room, Silo, and Cryo-Stasis Vault.
     */
    fun computeSingleRoomStorageCapContribution(buildingType: BuildingType, level: Int): Double {
        if (level <= 0) return 0.0
        val extraLevels = level - 1
        val structuralRoomShelving = level * 45.0 + extraLevels * extraLevels * 20.0
        val dedicatedStorage = when (buildingType) {
            BuildingType.VAULT_STORE_ROOM -> computeStoreRoomStorageBonus(level)
            else -> level * buildingType.storageBonusPerLevel
        }
        return structuralRoomShelving + dedicatedStorage
    }

    fun computeRoomUpgradesStorageBonus(buildingsMap: Map<String, Int>): Double {
        var total = 0.0
        for (bType in BuildingType.entries) {
            val lv = buildingsMap[bType.id] ?: 0
            if (lv > 0) {
                val extraLevels = lv - 1
                total += lv * 45.0 + extraLevels * extraLevels * 20.0
            }
        }
        return total
    }

    /**
     * Total primary resources currently stored in the Vault Supply Store Room.
     */
    fun computeTotalStoredResources(state: OutpostStateEntity): Double {
        val s = if (state.scrap.isFinite()) state.scrap.coerceAtLeast(0.0) else 0.0
        val w = if (state.water.isFinite()) state.water.coerceAtLeast(0.0) else 0.0
        val r = if (state.rations.isFinite()) state.rations.coerceAtLeast(0.0) else 0.0
        val f = if (state.bioFuel.isFinite()) state.bioFuel.coerceAtLeast(0.0) else 0.0
        val t = if (state.researchData.isFinite()) state.researchData.coerceAtLeast(0.0) else 0.0
        return ((s + w + r + f + t) * 100.0).roundToInt() / 100.0
    }

    /**
     * Half-capacity threshold (50% of storageCap) at or below which a halted resource automatically restarts production.
     */
    fun computeStoreRoomResumeThreshold(storageCap: Double): Double {
        if (!storageCap.isFinite() || storageCap <= 0.0) return 0.0
        return ((storageCap * STORE_ROOM_RESUME_THRESHOLD_RATIO) * 100.0).roundToInt() / 100.0
    }

    /**
     * Per-resource hysteresis check:
     * - A resource halts gathering when it reaches 100% of Store Room capacity (currentAmount >= storageCap),
     *   or when it was already halted (wasShelfHalted || wasStoreRoomHalted) and is still above 50% capacity.
     * - As soon as THAT resource falls to or below half of Store Room capacity (currentAmount <= storageCap * 0.50),
     *   it immediately resumes production (even if other resources like Scrap are still above 50%)!
     */
    fun isResourceShelfHalted(
        currentAmount: Double,
        wasShelfHalted: Boolean,
        wasStoreRoomHalted: Boolean,
        storageCap: Double
    ): Boolean {
        if (!storageCap.isFinite() || storageCap <= 0.0) return false
        val safeAmount = if (currentAmount.isFinite()) currentAmount.coerceAtLeast(0.0) else 0.0
        if (safeAmount >= storageCap) return true
        if (wasShelfHalted || wasStoreRoomHalted) {
            val resumeThreshold = computeStoreRoomResumeThreshold(storageCap)
            return safeAmount > resumeThreshold
        }
        return false
    }

    /**
     * Returns true when all primary Store Room resources are halted (or when every primary resource is above 50% while Store Room is halted).
     * Individual resources that fall to or below 50% of storageCap automatically resume via [isResourceShelfHalted].
     */
    fun isStoreRoomFull(state: OutpostStateEntity, storageCap: Double): Boolean {
        if (!storageCap.isFinite() || storageCap <= 0.0) return false
        val scrapHalted = isResourceShelfHalted(state.scrap, state.isScrapGatheringHalted, state.isStoreRoomGatheringHalted, storageCap)
        val waterHalted = isResourceShelfHalted(state.water, state.isWaterGatheringHalted, state.isStoreRoomGatheringHalted, storageCap)
        val rationsHalted = isResourceShelfHalted(state.rations, state.isRationsGatheringHalted, state.isStoreRoomGatheringHalted, storageCap)
        val fuelHalted = isResourceShelfHalted(state.bioFuel, state.isFuelGatheringHalted, state.isStoreRoomGatheringHalted, storageCap)
        return scrapHalted && waterHalted && rationsHalted && fuelHalted
    }

    /**
     * Storage-Full Slowdown Multiplier:
     * - When a resource shelf reaches 100% of Store Room capacity (or remains latched above 50% capacity),
     *   gathering slows to [STORAGE_FULL_SLOWDOWN_MULTIPLIER] (25% speed / -75% slower) instead of halting to 0!
     * - When a resource shelf is >= 90% of Store Room capacity, gathering slows to [STORAGE_NEAR_FULL_SLOWDOWN_MULTIPLIER] (55% speed).
     * - Below 90% (and once unlatched at <= 50% capacity), gathering runs at 100% full speed (1.0x).
     */
    fun computeResourceStorageSlowdownMultiplier(
        currentAmount: Double,
        isShelfHalted: Boolean,
        storageCap: Double
    ): Double {
        if (!storageCap.isFinite() || storageCap <= 0.0) return 1.0
        if (isShelfHalted) return STORAGE_FULL_SLOWDOWN_MULTIPLIER
        val safeAmount = if (currentAmount.isFinite()) currentAmount.coerceAtLeast(0.0) else 0.0
        return when {
            safeAmount >= storageCap -> STORAGE_FULL_SLOWDOWN_MULTIPLIER
            safeAmount >= storageCap * 0.90 -> STORAGE_NEAR_FULL_SLOWDOWN_MULTIPLIER
            else -> 1.0
        }
    }

    /**
     * Higher Vault Command Level increases the Vault's overall resource appetite!
     */
    fun computeVaultLevelUpkeepMultiplier(commandLevel: Int): Double {
        if (commandLevel <= 1) return 1.0
        val extraLv = commandLevel - 1
        return 1.0 + extraLv * 0.14 + extraLv * extraLv * 0.025
    }

    /**
     * Direct Vault Command Core resource upkeep that scales upward as Vault Level increases.
     */
    fun computeVaultLevelResourceUpkeep(
        commandLevel: Int,
        hydroDiscountMult: Double = 1.0
    ): RoomResourceUpkeep {
        if (commandLevel <= 1) return RoomResourceUpkeep()
        val extraLv = commandLevel - 1
        val scrapPerSec = ((extraLv * (0.05 + extraLv * 0.02)) * 100.0).roundToInt() / 100.0
        val waterPerSec = ((extraLv * (0.04 + extraLv * 0.018) * hydroDiscountMult) * 100.0).roundToInt() / 100.0
        val fuelPerSec = if (commandLevel >= 3) {
            (((extraLv - 1) * (0.015 + (extraLv - 1) * 0.008)) * 100.0).roundToInt() / 100.0
        } else 0.0

        val dailyScrap = ((extraLv * (1.6 + extraLv * 0.8)) * 10.0).roundToInt() / 10.0
        val dailyWater = ((extraLv * (1.3 + extraLv * 0.65) * hydroDiscountMult) * 10.0).roundToInt() / 10.0
        val dailyFuel = if (commandLevel >= 3) {
            (((extraLv - 1) * (0.5 + (extraLv - 1) * 0.25)) * 10.0).roundToInt() / 10.0
        } else 0.0

        return RoomResourceUpkeep(
            scrapPerSec = scrapPerSec,
            waterPerSec = waterPerSec,
            fuelPerSec = fuelPerSec,
            dailyScrap = dailyScrap,
            dailyWater = dailyWater,
            dailyFuel = dailyFuel
        )
    }

    /**
     * Non-linear / Exponential Power Draw at Higher Room Levels:
     * Higher room levels require progressively more Megawatts (MW) per level due to high-voltage
     * industrial machinery, UV crop lamps, and deep-core life support.
     */
    fun computeBuildingTotalMegawatts(buildingType: BuildingType, level: Int): Double {
        if (level <= 0) return 0.0
        val highLevelEscalation = 1.0 + (level - 1) * 0.16
        return (buildingType.megawattDemandPerLevel * level * highLevelEscalation * 10.0).roundToInt() / 100.0 * 10.0
    }

    /**
     * Non-linear / Exponential Vault Room Maintenance at Higher Room Levels, Higher Vault Levels, AND Each Passing Day:
     * Room maintenance takes progressively more resources (Scrap, Cooling Water, and Bio-Fuel) per level each day
     * to keep rooms operational while staying balanced for hardcore play.
     */
    fun computeBuildingResourceUpkeep(
        buildingType: BuildingType,
        level: Int,
        hydroDiscountMult: Double = 1.0,
        commandLevel: Int = 1,
        dayCounter: Int = 1
    ): RoomResourceUpkeep {
        if (level <= 0) return RoomResourceUpkeep()
        val extraLevels = (level - 1).coerceAtLeast(0)
        val vaultLevelScale = computeVaultLevelUpkeepMultiplier(commandLevel)
        val elapsedDays = (dayCounter.coerceAtLeast(1) - 1)
        val dayMaintenanceScale = 1.0 + elapsedDays * 0.04
        // Super-linear escalation for Lv.1, Lv.2, Lv.3, Lv.4, Lv.5+ multiplied by Vault Level & Day Counter
        val highLevelScale = (
            level * (1.0 + extraLevels * 0.55) +
                extraLevels * extraLevels * 0.35 +
                extraLevels * extraLevels * extraLevels * 0.12
            ) * vaultLevelScale

        val baseWater = buildingType.waterUpkeepPerLevel * highLevelScale * hydroDiscountMult
        val highLevelCoolantWater = if (level >= 2) {
            extraLevels * (0.06 + extraLevels * 0.04 + extraLevels * extraLevels * 0.015) * vaultLevelScale * hydroDiscountMult
        } else 0.0
        val waterPerSec = ((baseWater + highLevelCoolantWater) * 100.0).roundToInt() / 100.0

        val scrapPerSec = if (level >= 2 && buildingType != BuildingType.SALVAGE_YARD) {
            ((extraLevels * (0.07 + extraLevels * 0.045 + extraLevels * extraLevels * 0.015) * vaultLevelScale) * 100.0).roundToInt() / 100.0
        } else 0.0

        val fuelPerSec = if (level >= 2 && buildingType != BuildingType.BIO_SLUDGE_REFINERY) {
            ((extraLevels * (0.02 + extraLevels * 0.015 + extraLevels * extraLevels * 0.006) * vaultLevelScale) * 100.0).roundToInt() / 100.0
        } else 0.0

        val dailyScrap = (
            level * (2.6 + extraLevels * 4.2 + extraLevels * extraLevels * 2.2 + extraLevels * extraLevels * extraLevels * 0.55) *
                vaultLevelScale * dayMaintenanceScale * 10.0
            ).roundToInt() / 10.0
        val dailyWater = (
            level * (1.8 + extraLevels * 3.1 + extraLevels * extraLevels * 1.6 + extraLevels * extraLevels * extraLevels * 0.40) *
                vaultLevelScale * dayMaintenanceScale * hydroDiscountMult * 10.0
            ).roundToInt() / 10.0
        val dailyFuel = (
            level * (0.4 + extraLevels * 1.1 + extraLevels * extraLevels * 0.65 + extraLevels * extraLevels * extraLevels * 0.18) *
                vaultLevelScale * dayMaintenanceScale * 10.0
            ).roundToInt() / 10.0

        return RoomResourceUpkeep(
            scrapPerSec = scrapPerSec,
            waterPerSec = waterPerSec,
            fuelPerSec = fuelPerSec,
            dailyScrap = dailyScrap,
            dailyWater = dailyWater,
            dailyFuel = dailyFuel
        )
    }

    /**
     * Room maintenance resources (Scrap, Cooling Water, and Bio-Fuel) are required every day depending on built Room Levels
     * (each higher level takes more resources per day) to keep rooms 100% fully operational AND to automatically repair
     * automated systems (Automated Defense/Blast Door, Automated Power Generators, and Room Machinery / Coolant Pipes / Sabotage).
     */
    fun computeDailyAutomatedRepairPlan(
        state: OutpostStateEntity,
        buildingsMap: Map<String, Int>,
        rawDailyRoomScrapBudget: Double? = null
    ): AutomatedDailyRepairPlan {
        val totalRoomLevels = buildingsMap.values.filter { it > 0 }.sum()
        val doorDamaged = state.needsDefenseRepair || state.defenseIntegrityPercent < 100
        val generatorsWorn = state.generatorWearPercent > 0
        val roomsOrPipesDamaged = state.roomStructuralWearPercent > 0 ||
            state.activePipeLeaksCount > 0 ||
            state.sabotagedBuildingId.isNotEmpty()
        val needsAutomatedRepair = totalRoomLevels > 0 || doorDamaged || generatorsWorn || roomsOrPipesDamaged

        if (!needsAutomatedRepair) {
            return AutomatedDailyRepairPlan(
                totalRoomLevels = totalRoomLevels,
                needsAutomatedRepair = false,
                scrapCost = 0.0,
                waterCost = 0.0,
                fuelCost = 0.0
            )
        }

        val relicsCount = state.discoveredRelicsCount.coerceAtLeast(0)
        val relicDiscountMult = (1.0 - (relicsCount * RELIC_AUTO_REPAIR_DISCOUNT_PER_RELIC).coerceAtMost(0.25))
        val mothballedSet = parseMothballedSet(state.mothballedBuildingsCsv)
        var calcWaterSum = 0.0
        var calcFuelSum = 0.0
        var calcScrapSum = 0.0
        for (bType in BuildingType.entries) {
            val lv = buildingsMap[bType.id] ?: 0
            if (lv > 0) {
                val mothballMult = if (bType.id in mothballedSet) MOTHBALLED_UPKEEP_MULTIPLIER else 1.0
                val up = computeBuildingResourceUpkeep(bType, lv, 1.0, state.commandLevel, state.dayCounter)
                calcScrapSum += up.dailyScrap * mothballMult
                calcWaterSum += up.dailyWater * mothballMult
                calcFuelSum += up.dailyFuel * mothballMult
            }
        }
        val minScrapFloor = if (mothballedSet.isNotEmpty() && totalRoomLevels > 0) 0.7 else 3.5
        val baseRoomLevelScrap = rawDailyRoomScrapBudget ?: if (calcScrapSum <= 0.0) minScrapFloor else calcScrapSum

        val effectiveScrapCost = ((baseRoomLevelScrap.coerceAtLeast(minScrapFloor) * relicDiscountMult) * 10.0).roundToInt() / 10.0
        val effectiveWaterCost = ((calcWaterSum * relicDiscountMult) * 10.0).roundToInt() / 10.0
        val effectiveFuelCost = ((calcFuelSum * relicDiscountMult) * 10.0).roundToInt() / 10.0
        val effectiveLv = totalRoomLevels.coerceAtLeast(1)

        val doorRestore = if (doorDamaged) (10 + effectiveLv * 4).coerceAtMost(100) else 0
        val genWearReduce = if (generatorsWorn) (8 + effectiveLv * 3).coerceAtMost(80) else 0
        val roomWearReduce = if (state.roomStructuralWearPercent > 0) (10 + effectiveLv * 3).coerceAtMost(80) else 0
        val leaksSealed = if (state.activePipeLeaksCount > 0) (1 + effectiveLv / 3).coerceAtMost(state.activePipeLeaksCount) else 0
        val clearsSabotage = state.sabotagedBuildingId.isNotEmpty() && effectiveLv >= 1

        return AutomatedDailyRepairPlan(
            totalRoomLevels = totalRoomLevels,
            needsAutomatedRepair = true,
            scrapCost = effectiveScrapCost,
            waterCost = effectiveWaterCost,
            fuelCost = effectiveFuelCost,
            doorIntegrityRestore = doorRestore,
            generatorWearReduction = genWearReduce,
            roomWearReduction = roomWearReduce,
            pipeLeaksSealed = leaksSealed,
            clearsSabotage = clearsSabotage
        )
    }

    /**
     * Calculates construction time in seconds based on BOTH:
     * 1) The Room Level being constructed (targetRoomLevel = currentLevel + 1; higher level = much longer time)
     * 2) The Room Number (#1 through #8; higher room number = longer base time)
     */
    fun computeRoomBuildTimeSeconds(
        buildingType: BuildingType,
        targetRoomLevel: Int,
        workersMap: Map<String, WorkerClassEntity> = emptyMap(),
        skillsMap: Map<String, Int> = emptyMap()
    ): Int {
        val safeTargetLevel = targetRoomLevel.coerceAtLeast(1)
        val roomNumberOffset = buildingType.ordinal * 4
        val baseRoomSeconds = buildingType.baseBuildTimeSeconds + roomNumberOffset
        // Higher room levels take much longer to construct (exponential + linear level escalation)
        val levelEscalation = safeTargetLevel.toDouble().pow(1.65) + (safeTargetLevel - 1) * 0.85

        val scrapperCount = workersMap[WorkerClassType.SCRAPPER.id]?.assignedCount ?: 0
        val rankPrefab = skillsMap["IND_MODULAR_PREFAB"] ?: 0
        val rankNano = skillsMap["LEG_NANO_SCHEMATICS"] ?: 0
        val legacySpeedMult = (1.0 - rankNano * 0.12).coerceAtLeast(0.40)
        val speedDivisor = 1.0 + scrapperCount * 0.12 + rankPrefab * 0.10

        val rawBuildSeconds = (baseRoomSeconds * levelEscalation * legacySpeedMult) / speedDivisor
        val minimumSecondsForRoomAndLevel = ((10 + buildingType.ordinal * 3 + safeTargetLevel * safeTargetLevel * 5) * legacySpeedMult).roundToInt().coerceAtLeast(4)
        return rawBuildSeconds.roundToInt().coerceAtLeast(minimumSecondsForRoomAndLevel)
    }

    /**
     * Calculates Armory item crafting time in seconds, reduced by Bio-Fuel & Munitions Refinery level,
     * assigned Scrappers, Modular Prefab skill rank, and Overseer Legacy Nano-Schematics.
     */
    fun computeCraftTimeSeconds(
        item: CraftableItemType,
        state: OutpostStateEntity = OutpostStateEntity(),
        buildingsMap: Map<String, Int> = emptyMap(),
        workersMap: Map<String, WorkerClassEntity> = emptyMap(),
        skillsMap: Map<String, Int> = emptyMap()
    ): Int {
        val refineryLv = buildingsMap[BuildingType.BIO_SLUDGE_REFINERY.id] ?: 0
        val scrapperCount = workersMap[WorkerClassType.SCRAPPER.id]?.assignedCount ?: 0
        val rankPrefab = skillsMap["IND_MODULAR_PREFAB"] ?: 0
        val rankNano = skillsMap["LEG_NANO_SCHEMATICS"] ?: 0
        val legacySpeedMult = (1.0 - rankNano * 0.12).coerceAtLeast(0.40)
        val speedDivisor = 1.0 + refineryLv * 0.12 + scrapperCount * 0.08 + rankPrefab * 0.10
        val crossbowScale = if (item == CraftableItemType.AETHER_CROSSBOW) {
            1.0 + state.aetherCrossbowLevel * 0.35
        } else 1.0
        return ((item.baseCraftTimeSeconds * crossbowScale * legacySpeedMult) / speedDivisor).roundToInt().coerceAtLeast(3)
    }

    /**
     * Maximum Pickaxe Level unlocked by the Vault Ore & Gold Smeltery (SALVAGE_YARD).
     * Base Lv.2, +2 Pickaxe levels per Smeltery level!
     */
    fun computeMaxPickaxeLevel(buildingsMap: Map<String, Int>): Int {
        val smelteryLevel = buildingsMap[BuildingType.SALVAGE_YARD.id] ?: 0
        return 2 + smelteryLevel * 2
    }

    /**
     * Effective Dweller Expedition Death Chance, reduced by:
     * - Wasteland Radar & Recon Array (-1% per level)
     * - Charcoal Filter Gas Mask (-3% when in stock)
     * - Lead-Lined Hazmat Suit (-4% when in stock)
     * And increased during dangerous Bioluminescent Night (+6% Night Ambush Risk)!
     */
    fun computeEffectiveExpeditionDeathChance(
        type: WastelandExpeditionType,
        buildingsMap: Map<String, Int>,
        state: OutpostStateEntity? = null
    ): Int {
        val radarLevel = buildingsMap[BuildingType.SIGNAL_RESEARCH_ARRAY.id] ?: 0
        val gasMaskReduction = if ((state?.gasMasksCount ?: 0) > 0) 3 else 0
        val hazmatReduction = if ((state?.hazmatSuitsCount ?: 0) > 0) 4 else 0
        val armorReduction = ((state?.dwellerArmorCount ?: 0) / 2).coerceAtMost(2)
        val isDangerousNight = state != null && DayNightPhase.fromCycleProgress(state.cycleProgress) == DayNightPhase.RAD_NIGHT
        val nightHazardAdder = if (isDangerousNight) 6 else 0
        val minFloor = if (gasMaskReduction > 0 || hazmatReduction > 0 || armorReduction > 0) 2 else 3
        return (type.deathChancePercent + nightHazardAdder - radarLevel - gasMaskReduction - hazmatReduction - armorReduction).coerceAtLeast(minFloor)
    }

    fun computeProductionSnapshot(
        state: OutpostStateEntity,
        buildingsMap: Map<String, Int>,
        workersMap: Map<String, WorkerClassEntity>,
        skillsMap: Map<String, Int>,
        nowMillis: Long
    ): ProductionSnapshot {
        val activePhase = DayNightPhase.fromCycleProgress(state.cycleProgress)
        val solarFlareActive = state.solarFlareActiveUntilMillis > nowMillis
        val eclipseShroudActive = state.eclipseShroudActiveUntilMillis > nowMillis
        val thoriumOverdriveActive = state.thoriumOverdriveUntilMillis > nowMillis

        val commandBonusMult = CommandLevelProgression.globalProductionBonus(state.commandLevel)
        val salvageComboMult = 1.0 + state.salvageComboCount.coerceIn(0, 15) * 0.08
        val fieldMomentumWorkerBonus = 1.0 + state.salvageComboCount.coerceIn(0, 15) * 0.04

        val rankForgeOverdrive = skillsMap["IND_FORGE_OVERDRIVE"] ?: 0
        val rankAstral = skillsMap["CHR_ASTRAL_CONVERGENCE"] ?: 0
        val rankPhaseCap = skillsMap["CHR_PHASE_CAPACITORS"] ?: 0
        val rankSolarCollectors = skillsMap["CHR_SOLAR_COLLECTORS"] ?: 0
        val rankBiolumSensors = skillsMap["CHR_BIOLUM_SENSORS"] ?: 0
        val rankLegacyCryo = skillsMap["LEG_CRYO_AWAKENING"] ?: 0
        val rankLegacyHydro = skillsMap["LEG_HYDRO_RECYCLING"] ?: 0
        val rankLegacyFusion = skillsMap["LEG_FUSION_OVERCLOCK"] ?: 0

        val savantEntity = workersMap[WorkerClassType.TECH_SAVANT.id]
        val savantLv = savantEntity?.classLevel ?: 1
        val savantCount = savantEntity?.assignedCount ?: 0

        val hydroEntity = workersMap[WorkerClassType.HYDRO_ENGINEER.id]
        val hydroLv = hydroEntity?.classLevel ?: 1
        val hydroCount = hydroEntity?.assignedCount ?: 0

        val scrapperEntity = workersMap[WorkerClassType.SCRAPPER.id]
        val scrapperCount = scrapperEntity?.assignedCount ?: 0

        val stalkerEntity = workersMap[WorkerClassType.NIGHT_STALKER.id]
        val stalkerCount = stalkerEntity?.assignedCount ?: 0

        val totalAssigned = workersMap.values.sumOf { it.assignedCount }
        val operationalPosture = VaultOperationalPosture.fromId(state.operationalPostureId)
        val rationingPolicy = VaultRationingPolicy.fromId(state.rationingPolicyId)
        val mothballedSet = parseMothballedSet(state.mothballedBuildingsCsv)
        val activeSeason = WastelandSeason.forDay(state.dayCounter)
        val nextSeason = WastelandSeason.nextSeasonForDay(state.dayCounter)
        val daysRemainingInSeason = WastelandSeason.daysRemainingInSeason(state.dayCounter)
        val claimedDirectives = state.claimedDirectiveTier.coerceAtLeast(0)
        val directiveOutputBonusPercent = claimedDirectives * 2
        val directiveStorageBonus = claimedDirectives * DIRECTIVE_STORAGE_BONUS_PER_TIER
        val breakerMode = GridLoadBreakerMode.fromId(state.gridLoadBreakerMode)
        val activeAmmoType = TurretAmmoType.fromId(state.selectedAmmoTypeId)

        // 1. Calculate Vault Power Supply (MW) vs Escalating Room Demand (MW) & Generator Wear
        // Mothballed (Standby) rooms draw 0 MW, and an empty Vault (0 active rooms & 0 powered turrets) has 0 MW room demand!
        var rawMegawattDemand = 0.0
        var totalBuiltRoomsCount = 0
        var mothballedRoomsCount = 0
        var mothballedMwSavedRaw = 0.0
        for (bType in BuildingType.entries) {
            val bLevel = buildingsMap[bType.id] ?: 0
            if (bLevel > 0) {
                totalBuiltRoomsCount += 1
                if (bType.id in mothballedSet) {
                    mothballedRoomsCount += 1
                    mothballedMwSavedRaw += computeBuildingTotalMegawatts(bType, bLevel)
                } else {
                    rawMegawattDemand += computeBuildingTotalMegawatts(bType, bLevel)
                }
            }
        }
        val mothballedMwSaved = mothballedMwSavedRaw.roundToInt()
        // Powered defense emplacements also draw MW
        val turretMwDemand = state.sentryTurretCount * 2.5 + state.plasmaCannonCount * 5.0
        rawMegawattDemand += turretMwDemand
        if (rawMegawattDemand > 0.0) {
            rawMegawattDemand += 2.0 // Base Vault Airlock & Life-Support load when rooms/turrets are active
        }
        val megawattDemand = ((rawMegawattDemand * operationalPosture.megawattDemandMultiplier * breakerMode.megawattDemandMultiplier) * 10.0).roundToInt() / 10.0

        val solarPhaseFactor = (if (solarFlareActive) 1.65 else activePhase.solarPowerMultiplier) *
            (if (activeSeason == WastelandSeason.SCORCHING_FALLOUT) 1.0 else activeSeason.solarMwMultiplier)
        // Realistic Fuel Burn: Bio-Sludge Turbine generates 100% MW with Bio-Fuel, or 80% backup methane-vapor MW if fuel dips to 0!
        val turbineFuelFactor = if (state.bioTurbineLevel <= 0 || state.bioFuel > 0.1) 1.0 else 0.80
        val bioTurbineMw = state.bioTurbineLevel * PowerStationType.BIO_SLUDGE_TURBINE.baseMegawattsPerLevel * turbineFuelFactor
        val solarTowerMw = state.solarTowerLevel * PowerStationType.SOLAR_HELIOSTAT_TOWER.baseMegawattsPerLevel * solarPhaseFactor
        val thoriumReactorMw = state.thoriumReactorLevel * PowerStationType.THORIUM_FISSION_REACTOR.baseMegawattsPerLevel *
            (1.0 + rankBiolumSensors * 0.20)
        val aetherSubstationMw = state.aetherSubstationLevel * PowerStationType.AETHER_RESONANCE_SUBSTATION.baseMegawattsPerLevel
        val legacyFusionMw = rankLegacyFusion * 10.0

        val refineryGeneratorMw = (buildingsMap[BuildingType.BIO_SLUDGE_REFINERY.id] ?: 0) * 10.0
        val geothermalLevel = buildingsMap[BuildingType.GEOTHERMAL_TAP.id] ?: 0
        val geothermalBaselineMw = geothermalLevel * 9.0 // Room #9 Deep-Core Geothermal Tap: +9.0 MW/Lv 24/7 clean power!
        val mutagenLabLevel = buildingsMap[BuildingType.NOCTURNAL_EXTRACTION_RIG.id] ?: 0
        val reactorEngineerMw = savantCount * 5.5
        val activeCrankStack = if (thoriumOverdriveActive) {
            state.activeDynamoCrankStack.coerceIn(1, MAX_DYNAMO_CRANKS_PER_BLACKOUT)
        } else 0
        // Generator & Dynamo Wear reduces crank effectiveness if neglected (> 50% wear causes gear slipping)
        val dynamoWearFactor = if (state.generatorWearPercent > 50) {
            (1.0 - (state.generatorWearPercent - 50) * 0.01).coerceIn(0.65, 1.0)
        } else 1.0
        val emergencyDynamoBoostMw = if (thoriumOverdriveActive) (16.0 * activeCrankStack * dynamoWearFactor) else 0.0

        val hasBuiltPowerGenerator = state.bioTurbineLevel > 0 ||
            state.solarTowerLevel > 0 ||
            state.thoriumReactorLevel > 0 ||
            state.aetherSubstationLevel > 0 ||
            geothermalLevel > 0 ||
            refineryGeneratorMw > 0.0
        val auxiliaryCoreMw = when {
            hasBuiltPowerGenerator -> 6.0 + (state.commandLevel.coerceAtLeast(1) - 1) * 1.5
            rawMegawattDemand <= 0.0 && !thoriumOverdriveActive -> 8.0
            else -> 0.0
        }

        // Generator Wear reduces MW output by up to -16% until tuned up (mitigated by Room #9 Geothermal Tap -10%/Lv)
        val effectiveWearPercent = (state.generatorWearPercent - geothermalLevel * 8).coerceIn(0, 80)
        val wearPenaltyMult = (1.0 - effectiveWearPercent * 0.002).coerceIn(0.84, 1.0)

        val gridStationBonusMult = (1.0 + state.aetherSubstationLevel * 0.20) *
            (1.0 + mutagenLabLevel * 0.12) *
            (1.0 + rankSolarCollectors * 0.18) *
            (if (savantCount > 0 && savantLv >= 5) 1.15 else 1.0) *
            (if (thoriumOverdriveActive) 1.40 else 1.0) *
            wearPenaltyMult

        val relicsCount = state.discoveredRelicsCount.coerceAtLeast(0)
        val relicOutputBonusPercent = ((relicsCount * RELIC_OUTPUT_BONUS_PER_RELIC * 100.0) * 10.0).roundToInt() / 10.0
        val relicMegawattBonus = ((relicsCount * RELIC_MEGAWATT_BONUS_PER_RELIC) * 10.0).roundToInt() / 10.0
        val relicStorageBonus = relicsCount * RELIC_STORAGE_BONUS_PER_RELIC
        val relicAutoRepairDiscountPercent = (relicsCount * 1).coerceAtMost(25)

        val totalMegawattGenerated =
            (auxiliaryCoreMw + bioTurbineMw + solarTowerMw + thoriumReactorMw + aetherSubstationMw + legacyFusionMw +
                refineryGeneratorMw + geothermalBaselineMw + reactorEngineerMw + emergencyDynamoBoostMw) *
                gridStationBonusMult + relicMegawattBonus

        // Strict Vault Power Rule: If Required MW > 0 and Generated MW < Required MW, the Vault is in BLACKOUT!
        val isVaultBlackout = megawattDemand > 0.0 && totalMegawattGenerated < megawattDemand
        val isVaultBrownout = megawattDemand > 0.0 && totalMegawattGenerated > 0.0 && totalMegawattGenerated < megawattDemand
        val rawGridRatio = if (megawattDemand <= 0.0) 1.5 else totalMegawattGenerated / megawattDemand
        val powerGridEfficiencyMultiplier = when {
            isVaultBlackout -> 0.0
            rawGridRatio >= 1.25 -> 1.35 * (1.0 + state.thoriumReactorLevel * 0.08) * (1.0 + rankAstral * 0.40)
            else -> (1.05 + (rawGridRatio - 1.0) * 0.8) * (1.0 + state.thoriumReactorLevel * 0.08)
        }

        val siloLevel = buildingsMap[BuildingType.REINFORCED_SILO.id] ?: 0
        val smelteryLevel = buildingsMap[BuildingType.SALVAGE_YARD.id] ?: 0
        val condenserLevel = buildingsMap[BuildingType.ATMOSPHERIC_CONDENSER.id] ?: 0
        val farmLevel = buildingsMap[BuildingType.HYDROPONIC_GREENHOUSE.id] ?: 0
        val habLevel = buildingsMap[BuildingType.SURVIVOR_HAB_BLOCK.id] ?: 0
        val cryoVaultLevel = buildingsMap[BuildingType.CRYO_STASIS_VAULT.id] ?: 0
        val storeRoomLevel = buildingsMap[BuildingType.VAULT_STORE_ROOM.id] ?: 0
        val storeRoomStorageBonus = computeStoreRoomStorageBonus(storeRoomLevel)

        // Realistic Oxygen & Radiation Life-Support Drift Rates
        val oxygenScrubberSupply = if (isVaultBlackout) {
            if (isVaultBrownout) 0.4 else 0.0
        } else {
            1.2 + habLevel * 0.9 + farmLevel * 1.1 + hydroCount * 0.5 + breakerMode.oxygenBonusPerSec
        }
        val oxygenConsumption = 0.5 + state.totalSurvivors * 0.35
        val oxygenDeltaPerSec = ((oxygenScrubberSupply - oxygenConsumption) * 10.0).roundToInt() / 10.0

        val ambientRadLeak = (if (state.needsDefenseRepair) 1.5 else 0.3) +
            when (activePhase) {
                DayNightPhase.RAD_NIGHT -> 1.8 // Dangerous Night Time fallout surge!
                DayNightPhase.DUSK -> 0.8
                else -> 0.2
            }
        val medbayRadScrubbing = if (isVaultBlackout) 0.0 else (siloLevel * 1.4 + hydroCount * 0.6)
        val radiationDeltaPerSec = ((ambientRadLeak - medbayRadScrubbing) * 10.0).roundToInt() / 10.0

        // Daily Food & Water Needed for Workers (reduced by Hab Block -5%/Lv, Legacy Hydro -10%/rk, and Room #10 Cryo-Stasis Vault -8%/Lv!)
        val habUpkeepDiscount = (1.0 - habLevel * 0.05).coerceAtLeast(0.65)
        val cryoUpkeepDiscount = (1.0 - cryoVaultLevel * 0.08).coerceAtLeast(0.60)
        val legacyRationDiscount = (1.0 - rankLegacyHydro * 0.10).coerceAtLeast(0.50)
        val combinedRationDiscount = habUpkeepDiscount * cryoUpkeepDiscount * legacyRationDiscount
        val isWorkersStarvingOrDehydrated = state.totalSurvivors > 0 && (state.rations <= 0.0 || state.water <= 0.0)

        // 2. Daily Base Defense Rating, Bulkhead Silo Bonus, Ammo-Fed Turrets, Cryo Traps, Relics (+2 DEF each), Expedition Dweller Armor & Weapons & Door Integrity Scaling
        val rankHighOctane = skillsMap["IND_HIGH_OCTANE_CRACKING"] ?: 0
        val cryoTrapBonusDef = state.activeCryoTrapsCount.coerceAtLeast(0) * 14
        val relicBonusDef = relicsCount * RELIC_DEFENSE_BONUS_PER_RELIC
        val dwellerGearBonusDef = computeDwellerGearDefenseBonus(state)
        val unpoweredDefense = state.blastDoorLevel * 20 +
            state.craftedShrapnelMines * CraftableItemType.SHRAPNEL_MINE.defenseRatingBonus +
            cryoTrapBonusDef +
            relicBonusDef +
            dwellerGearBonusDef +
            (if (isWorkersStarvingOrDehydrated) 0 else (scrapperCount * 10 + stalkerCount * 16))
        val turretAmmoFactor = if (state.ammoRounds > 0) 1.0 else 0.55
        val hasBuiltAutoDefenses = state.sentryTurretCount > 0 || state.plasmaCannonCount > 0
        val hasAssignedWorkers = state.totalSurvivors > 0 && totalAssigned > 0 && !isWorkersStarvingOrDehydrated
        val isAutoDefenseActive = state.autoDefenseEnabled && !isVaultBlackout && hasAssignedWorkers && hasBuiltAutoDefenses
        val poweredBulkheadAndTurretDefense = if (isVaultBlackout) {
            0
        } else {
            val bulkheadDef = siloLevel * BuildingType.REINFORCED_SILO.defenseBonusPerLevel
            val autoTurretDef = if (hasAssignedWorkers && state.autoDefenseEnabled) {
                ((state.sentryTurretCount * CraftableItemType.AUTO_SENTRY_TURRET.defenseRatingBonus +
                    state.plasmaCannonCount * CraftableItemType.THORIUM_PLASMA_CANNON.defenseRatingBonus) * turretAmmoFactor).roundToInt()
            } else {
                0
            }
            bulkheadDef + autoTurretDef
        }
        val integrityFactor = (state.defenseIntegrityPercent.coerceIn(20, 100) / 100.0)
        val legacyDefMult = 1.0 + rankLegacyFusion * 0.25
        val ammoAndBreakerDefMult = activeAmmoType.defenseBonusMultiplier * breakerMode.defenseBonusMultiplier
        val totalDefenseRating = ((unpoweredDefense + poweredBulkheadAndTurretDefense) * (1.0 + rankHighOctane * 0.20) * legacyDefMult * ammoAndBreakerDefMult * integrityFactor).roundToInt()

        val currentDay = state.dayCounter.coerceAtLeast(1)
        val isPreparationPeaceWindow = currentDay <= PREPARATION_PEACE_DAYS
        val preparationDaysRemaining = (FIRST_ATTACK_DAY - currentDay).coerceAtLeast(0)
        val firstAttackDay11RequiredDefense = computeDailyRaidRequiredDefense(FIRST_ATTACK_DAY, DayNightPhase.SCORCHING_DAY)
        val isDay10Multiple = currentDay % 10 == 0
        val nextSiegeMilestoneDay = if (isDay10Multiple) currentDay else ((currentDay / 10) + 1) * 10
        val daysUntilDay10Siege = (nextSiegeMilestoneDay - currentDay).coerceAtLeast(0)
        val day10RequiredDefense = computeDay10SiegeRequiredDefense(nextSiegeMilestoneDay)
        val todayRequiredDefense = computeDailyRaidRequiredDefense(currentDay, activePhase)
        val tomorrowRequiredDefense = computeDailyRaidRequiredDefense(currentDay + 1, DayNightPhase.SCORCHING_DAY)
        val manualActionStaminaMult = when {
            state.dwellerStaminaPercent <= 15 -> 0.50
            state.dwellerStaminaPercent < 35 -> 0.78
            else -> 1.0
        }

        val hydroStorageBonus = if (hydroCount > 0 && hydroLv >= 20) 1.15 else 1.0
        val roomUpgradesStorageBonus = computeRoomUpgradesStorageBonus(buildingsMap)
        val commandLevelStorageBonus = (state.commandLevel.coerceAtLeast(1) - 1) * 45.0
        val storageCap = (
            BASE_STORAGE_CAP +
                roomUpgradesStorageBonus +
                siloLevel * BuildingType.REINFORCED_SILO.storageBonusPerLevel +
                cryoVaultLevel * BuildingType.CRYO_STASIS_VAULT.storageBonusPerLevel +
                storeRoomStorageBonus +
                relicStorageBonus +
                directiveStorageBonus +
                commandLevelStorageBonus
            ) * (1.0 + rankForgeOverdrive * 0.25) * hydroStorageBonus

        val maxWorkerCapacity = (BASE_MAX_CITIZENS + habLevel * BuildingType.SURVIVOR_HAB_BLOCK.populationBonusPerLevel)
            .coerceAtMost(MAX_VAULT_DWELLERS)
        val maxPickaxeLevelAllowed = computeMaxPickaxeLevel(buildingsMap)
        val totalStoredResources = computeTotalStoredResources(state)
        val storeRoomResumeThreshold = computeStoreRoomResumeThreshold(storageCap)
        val isScrapHalted = isResourceShelfHalted(state.scrap, state.isScrapGatheringHalted, state.isStoreRoomGatheringHalted, storageCap)
        val isWaterHalted = isResourceShelfHalted(state.water, state.isWaterGatheringHalted, state.isStoreRoomGatheringHalted, storageCap)
        val isRationsHalted = isResourceShelfHalted(state.rations, state.isRationsGatheringHalted, state.isStoreRoomGatheringHalted, storageCap)
        val isFuelHalted = isResourceShelfHalted(state.bioFuel, state.isFuelGatheringHalted, state.isStoreRoomGatheringHalted, storageCap)
        val isResearchHalted = isResourceShelfHalted(state.researchData, state.isResearchGatheringHalted, state.isStoreRoomGatheringHalted, storageCap)
        val isStoreRoomFull = isScrapHalted && isWaterHalted && isRationsHalted && isFuelHalted
        val isAnyStoreRoomResourceHalted = isScrapHalted || isWaterHalted || isRationsHalted || isFuelHalted || isResearchHalted
        val scrapStorageSlowdownMult = computeResourceStorageSlowdownMultiplier(state.scrap, isScrapHalted, storageCap)
        val waterStorageSlowdownMult = computeResourceStorageSlowdownMultiplier(state.water, isWaterHalted, storageCap)
        val rationsStorageSlowdownMult = computeResourceStorageSlowdownMultiplier(state.rations, isRationsHalted, storageCap)
        val fuelStorageSlowdownMult = computeResourceStorageSlowdownMultiplier(state.bioFuel, isFuelHalted, storageCap)
        val researchStorageSlowdownMult = computeResourceStorageSlowdownMultiplier(state.researchData, isResearchHalted, storageCap)
        val overallStorageSlowdownMult = when {
            isStoreRoomFull -> STORAGE_FULL_SLOWDOWN_MULTIPLIER
            isAnyStoreRoomResourceHalted -> STORAGE_FULL_SLOWDOWN_MULTIPLIER
            else -> minOf(
                scrapStorageSlowdownMult,
                waterStorageSlowdownMult,
                rationsStorageSlowdownMult,
                fuelStorageSlowdownMult
            )
        }
        val highestShelfRatio = if (storageCap > 0.0) {
            maxOf(state.scrap, state.water, state.rations, state.bioFuel, state.researchData).coerceAtLeast(0.0) / storageCap
        } else 0.0
        val storageFillPercent = (highestShelfRatio * 100.0).roundToInt().coerceIn(0, 100)
        val haltedResourcesSummary = buildList {
            if (isScrapHalted) add("Scrap")
            if (isWaterHalted) add("Water")
            if (isRationsHalted) add("Food")
            if (isFuelHalted) add("Fuel")
            if (isResearchHalted) add("Research")
        }.joinToString(", ")

        val isCondenserMothballed = BuildingType.ATMOSPHERIC_CONDENSER.id in mothballedSet
        val isFarmMothballed = BuildingType.HYDROPONIC_GREENHOUSE.id in mothballedSet
        val isSmelteryMothballed = BuildingType.SALVAGE_YARD.id in mothballedSet
        val isCryoMothballed = BuildingType.CRYO_STASIS_VAULT.id in mothballedSet
        val hasWaterPurifierRoom = condenserLevel >= 1 && !isCondenserMothballed
        val hasHydroponicsRoom = farmLevel >= 1 && !isFarmMothballed
        val activeBuiltRoomsCount = (totalBuiltRoomsCount - mothballedRoomsCount).coerceAtLeast(0)
        val isRoomsLackingScrap = activeBuiltRoomsCount > 0 && state.scrap <= 0.0
        val roomScrapOperationalMultiplier = if (isRoomsLackingScrap) 0.65 else 1.0

        // Daily Powered Room Supply Bonuses (Water requires active Water Purifier Room, Food requires active Hydroponics Room; slowed when storage shelf is full!)
        val activeCryoFoodBonus = if (isCryoMothballed) 0.0 else (cryoVaultLevel * 2.4 * computeRoomLevelOutputMultiplier(cryoVaultLevel))
        val dailyRoomFoodBonus = if (isVaultBlackout || !hasHydroponicsRoom) {
            0.0
        } else {
            ((farmLevel * 3.8 * computeRoomLevelOutputMultiplier(farmLevel) + activeCryoFoodBonus) *
                activeSeason.hydroponicsFoodMultiplier * roomScrapOperationalMultiplier * rationsStorageSlowdownMult * 10.0).roundToInt() / 10.0
        }
        val dailyRoomWaterBonus = if (isVaultBlackout || !hasWaterPurifierRoom) {
            0.0
        } else {
            val seasonWaterMult = if (activeSeason == WastelandSeason.SCORCHING_FALLOUT) 1.0 else activeSeason.waterCondenserMultiplier
            ((condenserLevel * 2.8 * computeRoomLevelOutputMultiplier(condenserLevel) * seasonWaterMult * roomScrapOperationalMultiplier * waterStorageSlowdownMult) * 10.0).roundToInt() / 10.0
        }
        val dailyRoomScrapBonus = if (isVaultBlackout || isSmelteryMothballed) {
            0.0
        } else {
            ((smelteryLevel * 4.5 * SCRAP_GATHER_RATE_MULTIPLIER * computeRoomLevelOutputMultiplier(smelteryLevel) * roomScrapOperationalMultiplier * scrapStorageSlowdownMult) * 10.0).roundToInt() / 10.0
        }
        val dailyRoomGoldBonus = if (isVaultBlackout || isSmelteryMothballed) {
            0.0
        } else {
            val goldSlowdownMult = if (isStoreRoomFull) STORAGE_FULL_SLOWDOWN_MULTIPLIER else 1.0
            ((smelteryLevel * 1.4 * computeRoomLevelOutputMultiplier(smelteryLevel) * roomScrapOperationalMultiplier * goldSlowdownMult) * 10.0).roundToInt() / 10.0
        }
        val dailyRoomFuelBonus = if (isVaultBlackout || isSmelteryMothballed) {
            0.0
        } else {
            ((smelteryLevel * 0.75 * computeRoomLevelOutputMultiplier(smelteryLevel) * roomScrapOperationalMultiplier * fuelStorageSlowdownMult) * 10.0).roundToInt() / 10.0
        }

        // 3. Daily & 5-Minute Food/Water Upkeep per Worker + Rationing Decree Multiplier
        val isEarlyDaysWindow = currentDay <= 10
        val maxEscalationTier = if (isEarlyDaysWindow) 10 else 30
        val stepPerTier = if (isEarlyDaysWindow) 0.08 else 0.14 // Forgiving +8%/2m in Days 1-10, +14%/2m after Day 10
        val dwellerUpkeepEscalationTier = (state.dwellerAppetiteEscalationSeconds.coerceAtLeast(0) / DWELLER_ESCALATION_INTERVAL_SECONDS).coerceAtMost(maxEscalationTier)
        val dwellerUpkeepEscalationMultiplier = 1.0 + dwellerUpkeepEscalationTier * stepPerTier
        val rationPolicyUpkeepMult = rationingPolicy.foodWaterUpkeepMultiplier
        val dailyFoodUpkeepPerDay = ((state.totalSurvivors * 2.0 + totalAssigned * 0.5) * combinedRationDiscount * dwellerUpkeepEscalationMultiplier * rationPolicyUpkeepMult * 10.0).roundToInt() / 10.0
        val dailyWaterUpkeepPerDay = ((state.totalSurvivors * 2.0 + totalAssigned * 0.5) * combinedRationDiscount * dwellerUpkeepEscalationMultiplier * rationPolicyUpkeepMult * 10.0).roundToInt() / 10.0
        val fiveMinDwellerFoodCost = ((state.totalSurvivors * 2.5 + totalAssigned * 0.75) * combinedRationDiscount * dwellerUpkeepEscalationMultiplier * rationPolicyUpkeepMult * 10.0).roundToInt() / 10.0
        val fiveMinDwellerWaterCost = ((state.totalSurvivors * 2.5 + totalAssigned * 0.75) * combinedRationDiscount * dwellerUpkeepEscalationMultiplier * rationPolicyUpkeepMult * 10.0).roundToInt() / 10.0
        val citizenFoodUpkeep = (state.totalSurvivors * 0.10 + totalAssigned * 0.03) * combinedRationDiscount * dwellerUpkeepEscalationMultiplier * operationalPosture.upkeepMultiplier * rationPolicyUpkeepMult
        val citizenWaterUpkeep = (state.totalSurvivors * 0.08 + totalAssigned * 0.03) * combinedRationDiscount * dwellerUpkeepEscalationMultiplier * operationalPosture.upkeepMultiplier * rationPolicyUpkeepMult

        var moraleMult = 1.0 + habLevel * 0.05 + (relicsCount * RELIC_MORALE_BONUS_PER_RELIC).coerceAtMost(0.20) +
            (rationingPolicy.moraleBonusDelta / 100.0)
        if (state.totalSurvivors > 0) {
            if (!isWorkersStarvingOrDehydrated && state.rations >= dailyFoodUpkeepPerDay && state.water >= dailyWaterUpkeepPerDay) {
                moraleMult += 0.15
            } else {
                moraleMult -= 0.30
            }
            if (!isVaultBlackout) {
                moraleMult += 0.10
            } else {
                moraleMult -= 0.25
            }
            if (totalDefenseRating >= todayRequiredDefense && !state.needsDefenseRepair) {
                moraleMult += 0.10
            }
            if (state.oxygenPercent < 50) moraleMult -= 0.18
            if (state.radiationMillisieverts > 150) moraleMult -= 0.18
            if (state.dwellerHealthPercent < 60) moraleMult -= 0.15
            if (state.dwellerStaminaPercent < 35) moraleMult -= 0.18
            if (state.sickDwellersCount > 0) moraleMult -= (state.sickDwellersCount * 0.06).coerceAtMost(0.24)
        }
        val citizenMoraleMultiplier = moraleMult.coerceIn(0.55, 1.45)
        val citizenMoralePercent = (citizenMoraleMultiplier * 100).roundToInt()
        val uniqueDwellerBonusMultiplier = 1.0 + state.uniqueDwellersCount * 0.40
        val prestigeBonusMultiplier = (1.0 + state.prestigeLevel * 0.25) * (1.0 + rankLegacyCryo * 0.20) *
            (1.0 + relicsCount * RELIC_OUTPUT_BONUS_PER_RELIC) *
            (1.0 + claimedDirectives * DIRECTIVE_OUTPUT_BONUS_PER_TIER)
        val onlineRoomsCount = if (isVaultBlackout || state.totalSurvivors <= 0 || totalAssigned <= 0 || isWorkersStarvingOrDehydrated) {
            0
        } else {
            activeBuiltRoomsCount
        }

        // Calculate total Vault Level + Room Resource Upkeep across the Vault (with Days 1-10 Early Grace, Operational Posture & Mothballing -80% discount!)
        val earlyDaysUpkeepFactor = if (currentDay <= 10 && state.commandLevel <= 3) 0.55 else 1.0
        val isEarlyBunkerGraceActive = currentDay <= 3 && state.commandLevel <= 2 && totalBuiltRoomsCount < 2
        val postureAndGraceUpkeepMult = earlyDaysUpkeepFactor * operationalPosture.upkeepMultiplier
        val hydroUpkeepMult = (if (hydroCount > 0 && hydroLv >= 15) 0.70 else 1.0) * legacyRationDiscount * cryoUpkeepDiscount
        val vaultLevelUpkeep = computeVaultLevelResourceUpkeep(state.commandLevel, hydroUpkeepMult)
        var totalRoomScrapUpkeepPerSec = 0.0 // Scrap is only used daily to repair automated systems (or lost to active pipe leaks)
        var totalRoomWaterUpkeepPerSec = vaultLevelUpkeep.waterPerSec * postureAndGraceUpkeepMult
        var totalRoomFuelUpkeepPerSec = vaultLevelUpkeep.fuelPerSec * postureAndGraceUpkeepMult
        var rawRoomScrapBudget = 0.0
        var totalDailyRoomWaterUpkeep = vaultLevelUpkeep.dailyWater * postureAndGraceUpkeepMult
        var totalDailyRoomFuelUpkeep = vaultLevelUpkeep.dailyFuel * postureAndGraceUpkeepMult

        for (bType in BuildingType.entries) {
            val bLevel = buildingsMap[bType.id] ?: 0
            if (bLevel <= 0) continue
            val mothballMult = if (bType.id in mothballedSet) MOTHBALLED_UPKEEP_MULTIPLIER else 1.0
            val roomUpkeep = computeBuildingResourceUpkeep(bType, bLevel, hydroUpkeepMult, state.commandLevel, currentDay)
            totalRoomWaterUpkeepPerSec += roomUpkeep.waterPerSec * postureAndGraceUpkeepMult * mothballMult
            totalRoomFuelUpkeepPerSec += roomUpkeep.fuelPerSec * postureAndGraceUpkeepMult * mothballMult
            rawRoomScrapBudget += roomUpkeep.dailyScrap * postureAndGraceUpkeepMult * mothballMult
            totalDailyRoomWaterUpkeep += roomUpkeep.dailyWater * postureAndGraceUpkeepMult * mothballMult
            totalDailyRoomFuelUpkeep += roomUpkeep.dailyFuel * postureAndGraceUpkeepMult * mothballMult
        }

        val dailyAutoRepairPlan = computeDailyAutomatedRepairPlan(
            state = state,
            buildingsMap = buildingsMap,
            rawDailyRoomScrapBudget = if (rawRoomScrapBudget > 0.0) rawRoomScrapBudget else if (mothballedRoomsCount > 0) 0.7 else 3.5
        )
        val totalDailyRoomScrapUpkeep = dailyAutoRepairPlan.scrapCost

        // Hardcore Building Hazards: Harsher Coolant Pipe Leaks & Room Structural Wear
        val pipeLeakWaterDrainPerSec = ((state.activePipeLeaksCount.coerceAtLeast(0) * 0.55) * 100.0).roundToInt() / 100.0
        val pipeLeakScrapDrainPerSec = ((state.activePipeLeaksCount.coerceAtLeast(0) * 0.40) * 100.0).roundToInt() / 100.0
        totalRoomWaterUpkeepPerSec += pipeLeakWaterDrainPerSec
        totalRoomScrapUpkeepPerSec += pipeLeakScrapDrainPerSec
        val roomWearPenaltyMult = (1.0 - state.roomStructuralWearPercent.coerceIn(0, 80) * 0.004).coerceIn(0.68, 1.0)
        val pickaxeDurabilityMult = when {
            state.pickaxeDurabilityPercent <= 0 -> 0.35
            state.pickaxeDurabilityPercent < 30 -> 0.68
            else -> 1.0
        }

        val reactorFuelUpkeep = computeTotalReactorFuelUpkeepPerSec(state, operationalPosture)
        val totalVaultResourceDrainPerSec =
            ((totalRoomScrapUpkeepPerSec + totalRoomWaterUpkeepPerSec + totalRoomFuelUpkeepPerSec +
                citizenFoodUpkeep + citizenWaterUpkeep + reactorFuelUpkeep) * 100.0).roundToInt() / 100.0
        val deficitDaysRemaining = (MAX_DEFICIT_DAYS_BEFORE_GAME_OVER - state.deficitDaysCount).coerceIn(0, MAX_DEFICIT_DAYS_BEFORE_GAME_OVER)
        val blackoutDaysRemaining = (MAX_BLACKOUT_DAYS_BEFORE_GAME_OVER - state.blackoutDaysCount).coerceIn(0, MAX_BLACKOUT_DAYS_BEFORE_GAME_OVER)
        val vaultTechEra = computeVaultTechEra(state, buildingsMap)

        // STRICT ZERO-DWELLER, WORKER STARVATION/DEHYDRATION, OR VAULT BLACKOUT RULE:
        if (state.totalSurvivors <= 0 || totalAssigned <= 0 || isVaultBlackout || isWorkersStarvingOrDehydrated) {
            val haltedDeficit = !isAnyStoreRoomResourceHalted && totalVaultResourceDrainPerSec > 0.05 && !isEarlyBunkerGraceActive
            val haltedDefenseRating = if (haltedDeficit) 0 else totalDefenseRating
            val haltedNetBalance = ((-totalVaultResourceDrainPerSec) * 100.0).roundToInt() / 100.0
            val (haltedCascadeStage, haltedCascadeSummary) = computeVaultCascadeStage(
                state = state,
                isVaultBlackout = isVaultBlackout,
                isVaultBrownout = isVaultBrownout,
                isVaultResourceDeficit = haltedDeficit,
                isWorkersStarvingOrDehydrated = isWorkersStarvingOrDehydrated,
                isRoomsLackingScrap = isRoomsLackingScrap,
                citizenMoralePercent = citizenMoralePercent
            )
            val haltedDirective = computeCurrentVaultDirective(state, buildingsMap, haltedDefenseRating)
            return ProductionSnapshot(
                scrapPerSec = 0.0,
                waterPerSec = if (state.totalSurvivors > 0 && state.water > 0.0) -citizenWaterUpkeep else 0.0,
                rationsPerSec = if (state.totalSurvivors > 0 && state.rations > 0.0) -citizenFoodUpkeep else 0.0,
                bioFuelPerSec = if (state.bioFuel > 0.0 && reactorFuelUpkeep > 0.0) -reactorFuelUpkeep else 0.0,
                researchPerSec = 0.0,
                mutagenCoresPerSec = 0.0,
                xpPerSec = 0.0,
                storageCap = storageCap,
                maxWorkerCapacity = maxWorkerCapacity,
                maxPickaxeLevelAllowed = maxPickaxeLevelAllowed,
                dailyRoomFoodBonus = dailyRoomFoodBonus,
                dailyRoomWaterBonus = dailyRoomWaterBonus,
                dailyRoomScrapBonus = dailyRoomScrapBonus,
                totalAssignedWorkers = totalAssigned,
                citizenMoraleMultiplier = citizenMoraleMultiplier,
                citizenMoralePercent = citizenMoralePercent,
                citizenFoodUpkeepPerSec = citizenFoodUpkeep,
                citizenWaterUpkeepPerSec = citizenWaterUpkeep,
                dailyFoodUpkeepPerDay = dailyFoodUpkeepPerDay,
                dailyWaterUpkeepPerDay = dailyWaterUpkeepPerDay,
                totalMegawattGenerated = totalMegawattGenerated,
                totalMegawattDemand = megawattDemand,
                isVaultBlackout = isVaultBlackout,
                isVaultBrownout = isVaultBrownout,
                powerGridEfficiencyMultiplier = powerGridEfficiencyMultiplier,
                oxygenDeltaPerSec = oxygenDeltaPerSec,
                radiationDeltaPerSec = radiationDeltaPerSec,
                totalDefenseRating = haltedDefenseRating,
                todayRequiredDefense = todayRequiredDefense,
                tomorrowRequiredDefense = tomorrowRequiredDefense,
                day10RequiredDefense = day10RequiredDefense,
                daysUntilDay10Siege = daysUntilDay10Siege,
                passiveFoodFromTrapsPerSec = 0.0,
                salvageComboMultiplier = salvageComboMult,
                activePhase = activePhase,
                solarFlareActive = solarFlareActive,
                eclipseShroudActive = eclipseShroudActive,
                thoriumOverdriveActive = thoriumOverdriveActive,
                commandLevelBonusMultiplier = commandBonusMult,
                uniqueDwellerBonusMultiplier = uniqueDwellerBonusMultiplier,
                prestigeBonusMultiplier = prestigeBonusMultiplier,
                isAutoDefenseActive = false,
                isWorkersStarvingOrDehydrated = isWorkersStarvingOrDehydrated,
                onlineRoomsCount = 0,
                totalBuiltRoomsCount = totalBuiltRoomsCount,
                roomScrapUpkeepPerSec = totalRoomScrapUpkeepPerSec,
                roomWaterUpkeepPerSec = totalRoomWaterUpkeepPerSec,
                roomFuelUpkeepPerSec = totalRoomFuelUpkeepPerSec,
                dailyRoomScrapUpkeep = totalDailyRoomScrapUpkeep,
                dailyRoomWaterUpkeep = totalDailyRoomWaterUpkeep,
                dailyRoomFuelUpkeep = totalDailyRoomFuelUpkeep,
                vaultLevelResourceUpkeep = vaultLevelUpkeep,
                totalGrossResourceGainPerSec = 0.0,
                totalVaultResourceDrainPerSec = totalVaultResourceDrainPerSec,
                netResourceBalancePerSec = haltedNetBalance,
                isMakingPositiveResources = haltedNetBalance >= 0.0 && totalVaultResourceDrainPerSec <= 0.05,
                reactorFuelUpkeepPerSec = reactorFuelUpkeep,
                storeRoomStorageBonus = storeRoomStorageBonus,
                isVaultResourceDeficit = haltedDeficit,
                isDefenseShutDownByDeficit = haltedDeficit,
                isEarlyBunkerGraceActive = isEarlyBunkerGraceActive,
                operationalPosture = operationalPosture,
                deficitDaysRemainingBeforeGameOver = deficitDaysRemaining,
                blackoutDaysRemainingBeforeGameOver = blackoutDaysRemaining,
                isPreparationPeaceWindow = isPreparationPeaceWindow,
                preparationDaysRemaining = preparationDaysRemaining,
                firstAttackDay11RequiredDefense = firstAttackDay11RequiredDefense,
                manualActionStaminaMultiplier = manualActionStaminaMult,
                dwellerUpkeepEscalationTier = dwellerUpkeepEscalationTier,
                dwellerUpkeepEscalationMultiplier = dwellerUpkeepEscalationMultiplier,
                fiveMinDwellerFoodCost = fiveMinDwellerFoodCost,
                fiveMinDwellerWaterCost = fiveMinDwellerWaterCost,
                pipeLeakWaterDrainPerSec = pipeLeakWaterDrainPerSec,
                pipeLeakScrapDrainPerSec = pipeLeakScrapDrainPerSec,
                roomWearPenaltyMultiplier = roomWearPenaltyMult,
                pickaxeDurabilityMultiplier = pickaxeDurabilityMult,
                activeGridBreakerMode = breakerMode,
                activeAmmoType = activeAmmoType,
                activeSubterraneanBoss = SubterraneanBossType.fromId(state.activeSubterraneanBossId),
                staminaEfficiencyMultiplier = if (state.dwellerStaminaPercent < 35) 0.72 else 1.0,
                goldPerSec = 0.0,
                dailyRoomGoldBonus = dailyRoomGoldBonus,
                dailyRoomFuelBonus = dailyRoomFuelBonus,
                goldMineSmallFuelPerSec = 0.0,
                dailyAutoRepairScrapCost = dailyAutoRepairPlan.scrapCost,
                needsAutomatedSystemRepair = dailyAutoRepairPlan.needsAutomatedRepair,
                relicOutputBonusPercent = relicOutputBonusPercent,
                relicDefenseBonus = relicBonusDef,
                relicMegawattBonus = relicMegawattBonus,
                relicStorageBonus = relicStorageBonus,
                relicAutoRepairDiscountPercent = relicAutoRepairDiscountPercent,
                isStoreRoomFull = isStoreRoomFull,
                isAnyStoreRoomResourceHalted = isAnyStoreRoomResourceHalted,
                isScrapHalted = isScrapHalted,
                isWaterHalted = isWaterHalted,
                isRationsHalted = isRationsHalted,
                isFuelHalted = isFuelHalted,
                isResearchHalted = isResearchHalted,
                haltedResourcesSummary = haltedResourcesSummary,
                totalStoredResources = totalStoredResources,
                storeRoomResumeThreshold = storeRoomResumeThreshold,
                storageSlowdownMultiplier = overallStorageSlowdownMult,
                storageFillPercent = storageFillPercent,
                dwellerGearDefenseBonus = dwellerGearBonusDef,
                hasWaterPurifierRoom = hasWaterPurifierRoom,
                hasHydroponicsRoom = hasHydroponicsRoom,
                isRoomsLackingScrap = isRoomsLackingScrap,
                roomScrapOperationalMultiplier = roomScrapOperationalMultiplier,
                roomUpgradesStorageBonus = roomUpgradesStorageBonus,
                activeRationingPolicy = rationingPolicy,
                activeWastelandSeason = activeSeason,
                nextWastelandSeason = nextSeason,
                daysRemainingInSeason = daysRemainingInSeason,
                vaultTechEra = vaultTechEra,
                cascadeStage = haltedCascadeStage,
                cascadeCrisisSummary = haltedCascadeSummary,
                mothballedRoomsCount = mothballedRoomsCount,
                mothballedMwSaved = mothballedMwSaved,
                mothballedBuildingIds = mothballedSet,
                currentDirective = haltedDirective,
                directiveOutputBonusPercent = directiveOutputBonusPercent,
                directiveStorageBonus = directiveStorageBonus
            )
        }

        var globalMult = commandBonusMult *
            uniqueDwellerBonusMultiplier *
            prestigeBonusMultiplier *
            citizenMoraleMultiplier *
            powerGridEfficiencyMultiplier *
            fieldMomentumWorkerBonus *
            operationalPosture.outputMultiplier *
            rationingPolicy.outputMultiplier *
            (1.0 + rankForgeOverdrive * 0.30) *
            (1.0 + rankAstral * 0.35)

        if (savantCount > 0 && savantLv >= 10) globalMult *= 1.10
        if (hydroCount > 0 && hydroLv >= 20) globalMult *= 1.40

        val activeClassesCount = workersMap.values.count { it.assignedCount > 0 }
        val rankCrossSynergy = skillsMap["WRK_CROSS_CLASS_SYNERGY"] ?: 0
        if (activeClassesCount > 1 && rankCrossSynergy > 0) {
            globalMult *= (1.0 + (activeClassesCount - 1) * rankCrossSynergy * 0.12)
        }

        val flarePotency = 1.45 * (1.0 + rankPhaseCap * 0.20)
        val shroudPotency = 1.60 * (1.0 + rankPhaseCap * 0.20)

        val isDaylight = activePhase == DayNightPhase.DAWN || activePhase == DayNightPhase.SCORCHING_DAY
        val daylightSkillMult = if (isDaylight || solarFlareActive) 1.0 + rankSolarCollectors * 0.15 else 1.0
        val rankNocturnalDom = skillsMap["CHR_NOCTURNAL_DOMINION"] ?: 0
        val nightSkillMult = if (activePhase == DayNightPhase.RAD_NIGHT || eclipseShroudActive) {
            1.0 + rankNocturnalDom * 0.28
        } else 1.0

        val effectiveScrapPhase = if (solarFlareActive) {
            maxOf(activePhase.scrapMultiplier, DayNightPhase.SCORCHING_DAY.scrapMultiplier) * flarePotency
        } else activePhase.scrapMultiplier

        val effectiveWaterPhase = if (solarFlareActive) {
            maxOf(activePhase.waterMultiplier, DayNightPhase.DAWN.waterMultiplier) * (1.0 + 0.20 * (1.0 + rankPhaseCap * 0.20))
        } else activePhase.waterMultiplier

        val effectiveRationsPhase = if (solarFlareActive) {
            maxOf(activePhase.rationsMultiplier, DayNightPhase.SCORCHING_DAY.rationsMultiplier) * flarePotency
        } else activePhase.rationsMultiplier

        val effectiveResearchPhase = if (eclipseShroudActive) {
            maxOf(activePhase.researchMultiplier, DayNightPhase.RAD_NIGHT.researchMultiplier) * shroudPotency
        } else activePhase.researchMultiplier

        val effectiveMutagenPhase = if (eclipseShroudActive) {
            maxOf(activePhase.mutagenSpawnMultiplier, DayNightPhase.RAD_NIGHT.mutagenSpawnMultiplier) * shroudPotency
        } else activePhase.mutagenSpawnMultiplier

        var scrapBase = 0.0
        var waterBase = 0.0
        var rationsBase = 0.0
        var bioFuelBase = 0.0
        var researchBase = 0.0
        var mutagenBase = 0.0
        var goldBase = 0.0
        var goldMineFuelTrickle = 0.0
        var buildingWaterUpkeep = 0.0

        for (bType in BuildingType.entries) {
            val level = buildingsMap[bType.id] ?: 0
            if (level > 0 && bType.id in mothballedSet) continue
            val favoredWorker = bType.favoredWorkerClass
            val assignedFavored = if (favoredWorker != null) {
                workersMap[favoredWorker.id]?.assignedCount ?: 0
            } else 0

            // At Level 0 (worst makeshift state), a room only trickles if its favored worker is actively assigned
            if (level <= 0 && assignedFavored <= 0) continue

            val staffingFactor = when {
                favoredWorker == null -> 0.45
                assignedFavored <= 0 -> 0.35 // Powered rooms still run automated machinery at 35% when Dwellers are in the Vault!
                else -> 0.75 + assignedFavored * 0.65
            }

            // Rooms gradually get better from Level 0 (worst: 20% eff) upward (+18% eff each level), modified by room wear & operational Scrap!
            val effectiveRoomUnits = computeRoomEffectiveTotalOutputMultiplier(level)
            val rawOut = effectiveRoomUnits * bType.baseOutputPerLevel * staffingFactor * roomWearPenaltyMult * roomScrapOperationalMultiplier
            when (bType.primaryResource) {
                ResourceType.SCRAP -> scrapBase += rawOut
                ResourceType.WATER -> if (hasWaterPurifierRoom) waterBase += rawOut
                ResourceType.RATIONS -> if (hasHydroponicsRoom) rationsBase += rawOut
                ResourceType.BIO_FUEL -> bioFuelBase += rawOut
                ResourceType.RESEARCH_DATA -> researchBase += rawOut
                ResourceType.MUTAGEN_CORES -> mutagenBase += rawOut
                else -> {}
            }

            // Upgraded Vault Gold Mine & Ore Smeltery (SALVAGE_YARD) gathers real small amounts of Gold & Bio-Fuel per upgrade level!
            if (bType == BuildingType.SALVAGE_YARD && level > 0) {
                goldBase += (0.06 + level * 0.09 + effectiveRoomUnits * 0.14) * staffingFactor * roomWearPenaltyMult * roomScrapOperationalMultiplier
                goldMineFuelTrickle += (0.04 + level * 0.05) * roomWearPenaltyMult * roomScrapOperationalMultiplier
            }

            if (level > 0 && bType.waterUpkeepPerLevel > 0.0 && assignedFavored > 0) {
                val upkeepDiscount = if (bType == BuildingType.HYDROPONIC_GREENHOUSE && hydroCount > 0 && hydroLv >= 15) 0.5 else 1.0
                buildingWaterUpkeep += level * bType.waterUpkeepPerLevel * upkeepDiscount
            }
        }

        val rankGuildDrills = skillsMap["WRK_GUILD_DRILLS"] ?: 0
        val rankCircadian = skillsMap["WRK_CIRCADIAN_AUTO_SHIFT"] ?: 0
        val autoShiftBonus = if (state.autoShiftEnabled) 1.15 + rankCircadian * 0.10 else 1.0
        val radarRoomLv = buildingsMap[BuildingType.SIGNAL_RESEARCH_ARRAY.id] ?: 0
        val refineryRoomLv = buildingsMap[BuildingType.BIO_SLUDGE_REFINERY.id] ?: 0

        for (wType in WorkerClassType.entries) {
            val entity = workersMap[wType.id] ?: WorkerClassEntity(wType.id, 0, 1, 0.0)
            val count = entity.assignedCount
            if (count <= 0) continue

            val cLevel = entity.classLevel
            val classLevelMult = 1.0 + (cLevel - 1) * 0.14

            var phaseFactor = if (isDaylight || solarFlareActive) {
                wType.dayProductivityMultiplier
            } else {
                wType.nightProductivityMultiplier
            }

            if (wType == WorkerClassType.SCRAPPER && cLevel >= 15 && !isDaylight) {
                phaseFactor = maxOf(phaseFactor, 0.92)
            }
            if (wType == WorkerClassType.NIGHT_STALKER && cLevel >= 10 && isDaylight) {
                phaseFactor = maxOf(phaseFactor, 1.0)
            }
            if (phaseFactor < 1.0 && rankCircadian > 0) {
                phaseFactor = (phaseFactor + rankCircadian * 0.15).coerceAtMost(1.05)
            }

            // Workers operating in low-level rooms gather resources slower until their primary room is upgraded
            val primaryFacilityLevel = when (wType) {
                WorkerClassType.SCRAPPER -> maxOf(smelteryLevel, siloLevel)
                WorkerClassType.HYDRO_ENGINEER -> maxOf(condenserLevel, farmLevel, refineryRoomLv)
                WorkerClassType.NIGHT_STALKER -> mutagenLabLevel
                WorkerClassType.TECH_SAVANT -> maxOf(radarRoomLv, geothermalLevel)
            }
            val lowRoomPacingMult = computeWorkerLowRoomPacingMultiplier(primaryFacilityLevel)

            var workerOut = count * wType.baseOutputPerSecond * classLevelMult * phaseFactor *
                (1.0 + rankGuildDrills * 0.15) * autoShiftBonus * lowRoomPacingMult

            when (wType) {
                WorkerClassType.SCRAPPER -> {
                    if (cLevel >= 5) workerOut *= 1.25
                    if (cLevel >= 20) workerOut *= 1.45
                    scrapBase += workerOut
                    if (cLevel >= 10) bioFuelBase += count * 0.20 * classLevelMult * lowRoomPacingMult
                }
                WorkerClassType.HYDRO_ENGINEER -> {
                    if (cLevel >= 5) workerOut *= 1.25
                    // Automated Water requires the Water Purifier Room (ATMOSPHERIC_CONDENSER);
                    // Automated Food requires the Hydroponics Room (HYDROPONIC_GREENHOUSE).
                    // Without those rooms, you can only get Food and Water manually!
                    if (hasWaterPurifierRoom) {
                        waterBase += workerOut * roomScrapOperationalMultiplier
                    }
                    if (hasHydroponicsRoom) {
                        rationsBase += workerOut * 0.55 * roomScrapOperationalMultiplier
                    }
                    if (cLevel >= 10) bioFuelBase += count * 0.30 * classLevelMult * lowRoomPacingMult
                }
                WorkerClassType.NIGHT_STALKER -> {
                    if (cLevel >= 5) workerOut *= 1.30
                    if (cLevel >= 20 && (activePhase == DayNightPhase.RAD_NIGHT || eclipseShroudActive)) {
                        workerOut *= 2.0
                    }
                    mutagenBase += workerOut
                    if (cLevel >= 15 && (!isDaylight || eclipseShroudActive)) {
                        scrapBase += count * 0.85 * classLevelMult * lowRoomPacingMult
                        bioFuelBase += count * 0.35 * classLevelMult * lowRoomPacingMult
                    }
                }
                WorkerClassType.TECH_SAVANT -> {
                    if (cLevel >= 5) workerOut *= 1.25
                    if (cLevel >= 20) workerOut *= 1.35
                    researchBase += workerOut
                }
            }
        }

        // Sift real small amounts of Gold whenever Scrap is being gathered!
        if (scrapBase > 0.0) {
            goldBase += scrapBase * (0.03 + smelteryLevel * 0.015)
        }

        val rankRotarySaws = skillsMap["IND_ROTARY_SAWS"] ?: 0
        val rankHydroFiltration = skillsMap["IND_HYDRO_FILTRATION"] ?: 0
        val rankSovereign = skillsMap["WRK_SOVEREIGN_COMMAND"] ?: 0

        val unhaltedPassiveFoodFromTraps = if (!hasHydroponicsRoom) {
            0.0
        } else {
            (
                state.craftedSnareTraps * CraftableItemType.SNARE_TRAP.passiveFoodPerSec * 0.5 +
                    state.aetherCrossbowLevel * CraftableItemType.AETHER_CROSSBOW.passiveFoodPerSec * 0.5
                ) * (1.0 + rankHydroFiltration * 0.22) * (1.0 + farmLevel * 0.25) * roomScrapOperationalMultiplier
        }
        val passiveFoodFromTraps = if (!hasHydroponicsRoom) 0.0 else (unhaltedPassiveFoodFromTraps * rationsStorageSlowdownMult)

        // PER-RESOURCE STORAGE SLOWDOWN & 50% CAPACITY FULL-SPEED RESUME:
        // Each resource slows down to 25% speed when it reaches storageCap, and automatically returns to 100% speed when THAT resource falls to <= 50% of storageCap!
        val unhaltedScrapPerSec = scrapBase * SCRAP_GATHER_RATE_MULTIPLIER * (1.0 + rankRotarySaws * 0.18) * effectiveScrapPhase * daylightSkillMult * nightSkillMult * globalMult
        val grossScrapPerSec = unhaltedScrapPerSec * scrapStorageSlowdownMult
        val scrapPerSec = (grossScrapPerSec - totalRoomScrapUpkeepPerSec).coerceAtLeast(-2.5)

        val unhaltedWaterPerSec = if (!hasWaterPurifierRoom) {
            0.0
        } else {
            waterBase * (1.0 + rankHydroFiltration * 0.22) * effectiveWaterPhase * daylightSkillMult * nightSkillMult * globalMult
        }
        val grossWaterPerSec = if (!hasWaterPurifierRoom) 0.0 else (unhaltedWaterPerSec * waterStorageSlowdownMult)
        val waterPerSec = (grossWaterPerSec - totalRoomWaterUpkeepPerSec - citizenWaterUpkeep).coerceAtLeast(-2.5)

        val unhaltedRationsPerSec = if (!hasHydroponicsRoom) {
            0.0
        } else {
            rationsBase * (1.0 + rankHydroFiltration * 0.22) * effectiveRationsPhase * daylightSkillMult * nightSkillMult * globalMult + unhaltedPassiveFoodFromTraps
        }
        val grossRationsPerSec = if (!hasHydroponicsRoom) 0.0 else (unhaltedRationsPerSec * rationsStorageSlowdownMult)
        val rationsPerSec = (grossRationsPerSec - citizenFoodUpkeep).coerceAtLeast(-2.5)

        val unhaltedGoldMineFuelPerSec = ((goldMineFuelTrickle) * 100.0).roundToInt() / 100.0
        val goldMineSmallFuelPerSec = ((unhaltedGoldMineFuelPerSec * fuelStorageSlowdownMult) * 100.0).roundToInt() / 100.0
        val unhaltedFuelPerSec = bioFuelBase * (1.0 + rankHighOctane * 0.25) * daylightSkillMult * nightSkillMult * globalMult + unhaltedGoldMineFuelPerSec
        val grossFuelPerSec = (bioFuelBase * (1.0 + rankHighOctane * 0.25) * daylightSkillMult * nightSkillMult * globalMult * fuelStorageSlowdownMult) + goldMineSmallFuelPerSec
        val bioFuelPerSec = if (bioFuelBase > 0.0 || goldMineSmallFuelPerSec > 0.0 || totalRoomFuelUpkeepPerSec > 0.0 || reactorFuelUpkeep > 0.0) {
            (grossFuelPerSec - reactorFuelUpkeep - totalRoomFuelUpkeepPerSec)
                .coerceAtLeast(-4.0)
        } else 0.0
        val goldStoreSlowdown = if (isStoreRoomFull) STORAGE_FULL_SLOWDOWN_MULTIPLIER else 1.0
        val goldPerSec = ((goldBase * (1.0 + rankSovereign * 0.20) * daylightSkillMult * nightSkillMult * globalMult * goldStoreSlowdown) * 100.0).roundToInt() / 100.0

        val totalGrossResourceGainPerSec =
            ((grossScrapPerSec + grossWaterPerSec + grossRationsPerSec + grossFuelPerSec) * 100.0).roundToInt() / 100.0
        val potentialGrossResourceGainPerSec =
            ((unhaltedScrapPerSec + unhaltedWaterPerSec + unhaltedRationsPerSec + unhaltedFuelPerSec) * 100.0).roundToInt() / 100.0
        val netResourceBalancePerSec =
            ((totalGrossResourceGainPerSec - totalVaultResourceDrainPerSec) * 100.0).roundToInt() / 100.0
        val isMakingPositiveResources = if (isAnyStoreRoomResourceHalted) {
            potentialGrossResourceGainPerSec >= totalVaultResourceDrainPerSec
        } else {
            totalGrossResourceGainPerSec >= totalVaultResourceDrainPerSec
        }
        val isVaultResourceDeficit = !isEarlyBunkerGraceActive &&
            !isAnyStoreRoomResourceHalted &&
            totalVaultResourceDrainPerSec > 0.05 &&
            totalVaultResourceDrainPerSec > totalGrossResourceGainPerSec
        val isDefenseShutDownByDeficit = isVaultResourceDeficit
        val effectiveTotalDefenseRating = if (isDefenseShutDownByDeficit) 0 else totalDefenseRating
        val effectiveAutoDefenseActive = if (isDefenseShutDownByDeficit) false else isAutoDefenseActive

        val researchPerSec = (researchBase * (1.0 + rankSovereign * 0.25) * effectiveResearchPhase * daylightSkillMult * nightSkillMult * globalMult * researchStorageSlowdownMult)
        val mutagenCoresPerSec = (mutagenBase * (1.0 + rankBiolumSensors * 0.20) * effectiveMutagenPhase * nightSkillMult * globalMult * goldStoreSlowdown)

        val arrayLevel = buildingsMap[BuildingType.SIGNAL_RESEARCH_ARRAY.id] ?: 0
        val xpFromWorkers = totalAssigned * 0.55
        val xpFromPowerGrid = (state.bioTurbineLevel + state.solarTowerLevel * 2 + state.thoriumReactorLevel * 4 + state.aetherSubstationLevel * 5) * 0.25
        val xpMult = citizenMoraleMultiplier *
            operationalPosture.outputMultiplier *
            (1.0 + arrayLevel * 0.15) *
            (1.0 + savantCount * 0.15) *
            (1.0 + rankHighOctane * 0.15) *
            (1.0 + rankSovereign * 0.20)
        val xpPerSec = (xpFromWorkers + xpFromPowerGrid) * xpMult

        val (activeCascadeStage, activeCascadeSummary) = computeVaultCascadeStage(
            state = state,
            isVaultBlackout = isVaultBlackout,
            isVaultBrownout = isVaultBrownout,
            isVaultResourceDeficit = isVaultResourceDeficit,
            isWorkersStarvingOrDehydrated = isWorkersStarvingOrDehydrated,
            isRoomsLackingScrap = isRoomsLackingScrap,
            citizenMoralePercent = citizenMoralePercent
        )
        val activeDirective = computeCurrentVaultDirective(state, buildingsMap, effectiveTotalDefenseRating)

        return ProductionSnapshot(
            scrapPerSec = scrapPerSec,
            waterPerSec = waterPerSec,
            rationsPerSec = rationsPerSec,
            bioFuelPerSec = bioFuelPerSec,
            researchPerSec = researchPerSec,
            mutagenCoresPerSec = mutagenCoresPerSec,
            xpPerSec = xpPerSec,
            storageCap = storageCap,
            maxWorkerCapacity = maxWorkerCapacity,
            maxPickaxeLevelAllowed = maxPickaxeLevelAllowed,
            dailyRoomFoodBonus = dailyRoomFoodBonus,
            dailyRoomWaterBonus = dailyRoomWaterBonus,
            dailyRoomScrapBonus = dailyRoomScrapBonus,
            totalAssignedWorkers = totalAssigned,
            citizenMoraleMultiplier = citizenMoraleMultiplier,
            citizenMoralePercent = citizenMoralePercent,
            citizenFoodUpkeepPerSec = citizenFoodUpkeep,
            citizenWaterUpkeepPerSec = citizenWaterUpkeep,
            dailyFoodUpkeepPerDay = dailyFoodUpkeepPerDay,
            dailyWaterUpkeepPerDay = dailyWaterUpkeepPerDay,
            totalMegawattGenerated = totalMegawattGenerated,
            totalMegawattDemand = megawattDemand,
            isVaultBlackout = isVaultBlackout,
            isVaultBrownout = isVaultBrownout,
            powerGridEfficiencyMultiplier = powerGridEfficiencyMultiplier,
            oxygenDeltaPerSec = oxygenDeltaPerSec,
            radiationDeltaPerSec = radiationDeltaPerSec,
            totalDefenseRating = effectiveTotalDefenseRating,
            todayRequiredDefense = todayRequiredDefense,
            tomorrowRequiredDefense = tomorrowRequiredDefense,
            day10RequiredDefense = day10RequiredDefense,
            daysUntilDay10Siege = daysUntilDay10Siege,
            passiveFoodFromTrapsPerSec = passiveFoodFromTraps,
            salvageComboMultiplier = salvageComboMult,
            activePhase = activePhase,
            solarFlareActive = solarFlareActive,
            eclipseShroudActive = eclipseShroudActive,
            thoriumOverdriveActive = thoriumOverdriveActive,
            commandLevelBonusMultiplier = commandBonusMult,
            uniqueDwellerBonusMultiplier = uniqueDwellerBonusMultiplier,
            prestigeBonusMultiplier = prestigeBonusMultiplier,
            isAutoDefenseActive = effectiveAutoDefenseActive,
            isWorkersStarvingOrDehydrated = isWorkersStarvingOrDehydrated,
            onlineRoomsCount = onlineRoomsCount,
            totalBuiltRoomsCount = totalBuiltRoomsCount,
            roomScrapUpkeepPerSec = totalRoomScrapUpkeepPerSec,
            roomWaterUpkeepPerSec = totalRoomWaterUpkeepPerSec,
            roomFuelUpkeepPerSec = totalRoomFuelUpkeepPerSec,
            dailyRoomScrapUpkeep = totalDailyRoomScrapUpkeep,
            dailyRoomWaterUpkeep = totalDailyRoomWaterUpkeep,
            dailyRoomFuelUpkeep = totalDailyRoomFuelUpkeep,
            vaultLevelResourceUpkeep = vaultLevelUpkeep,
            totalGrossResourceGainPerSec = totalGrossResourceGainPerSec,
            totalVaultResourceDrainPerSec = totalVaultResourceDrainPerSec,
            netResourceBalancePerSec = netResourceBalancePerSec,
            isMakingPositiveResources = isMakingPositiveResources,
            reactorFuelUpkeepPerSec = reactorFuelUpkeep,
            storeRoomStorageBonus = storeRoomStorageBonus,
            roomUpgradesStorageBonus = roomUpgradesStorageBonus,
            isVaultResourceDeficit = isVaultResourceDeficit,
            isDefenseShutDownByDeficit = isDefenseShutDownByDeficit,
            isEarlyBunkerGraceActive = isEarlyBunkerGraceActive,
            operationalPosture = operationalPosture,
            deficitDaysRemainingBeforeGameOver = deficitDaysRemaining,
            blackoutDaysRemainingBeforeGameOver = blackoutDaysRemaining,
            isPreparationPeaceWindow = isPreparationPeaceWindow,
            preparationDaysRemaining = preparationDaysRemaining,
            firstAttackDay11RequiredDefense = firstAttackDay11RequiredDefense,
            manualActionStaminaMultiplier = manualActionStaminaMult,
            dwellerUpkeepEscalationTier = dwellerUpkeepEscalationTier,
            dwellerUpkeepEscalationMultiplier = dwellerUpkeepEscalationMultiplier,
            fiveMinDwellerFoodCost = fiveMinDwellerFoodCost,
            pipeLeakWaterDrainPerSec = pipeLeakWaterDrainPerSec,
            pipeLeakScrapDrainPerSec = pipeLeakScrapDrainPerSec,
            roomWearPenaltyMultiplier = roomWearPenaltyMult,
            pickaxeDurabilityMultiplier = pickaxeDurabilityMult,
            foodSpoilagePercentPerDay = (4.5 * (1.0 - (cryoVaultLevel / 5.0).coerceIn(0.0, 1.0)) * 10.0).roundToInt() / 10.0,
            dailyFoodSpoilageLoss = ((state.rations * 0.045 * (1.0 - (cryoVaultLevel / 5.0).coerceIn(0.0, 1.0))) * 10.0).roundToInt() / 10.0,
            isCryoPreservationActive = cryoVaultLevel >= 5,
            edenCorePhase = state.edenCorePhase,
            certificationTier = state.certificationTier,
            isSelfSustainedCertified = state.isSelfSustainedCertified,
            consecutiveDaysStable = state.consecutiveDaysStable,
            sabotagedBuildingId = state.sabotagedBuildingId,
            sabotagedBuildingPenaltyPercent = state.sabotagedBuildingPenaltyPercent,
            activeGridBreakerMode = breakerMode,
            activeAmmoType = activeAmmoType,
            activeSubterraneanBoss = SubterraneanBossType.fromId(state.activeSubterraneanBossId),
            staminaEfficiencyMultiplier = if (state.dwellerStaminaPercent < 35) 0.72 else 1.0,
            goldPerSec = goldPerSec,
            dailyRoomGoldBonus = dailyRoomGoldBonus,
            dailyRoomFuelBonus = dailyRoomFuelBonus,
            goldMineSmallFuelPerSec = goldMineSmallFuelPerSec,
            dailyAutoRepairScrapCost = dailyAutoRepairPlan.scrapCost,
            needsAutomatedSystemRepair = dailyAutoRepairPlan.needsAutomatedRepair,
            relicOutputBonusPercent = relicOutputBonusPercent,
            relicDefenseBonus = relicBonusDef,
            relicMegawattBonus = relicMegawattBonus,
            relicStorageBonus = relicStorageBonus,
            relicAutoRepairDiscountPercent = relicAutoRepairDiscountPercent,
            isStoreRoomFull = isStoreRoomFull,
            isAnyStoreRoomResourceHalted = isAnyStoreRoomResourceHalted,
            isScrapHalted = isScrapHalted,
            isWaterHalted = isWaterHalted,
            isRationsHalted = isRationsHalted,
            isFuelHalted = isFuelHalted,
            isResearchHalted = isResearchHalted,
            haltedResourcesSummary = haltedResourcesSummary,
            totalStoredResources = totalStoredResources,
            storeRoomResumeThreshold = storeRoomResumeThreshold,
            storageSlowdownMultiplier = overallStorageSlowdownMult,
            storageFillPercent = storageFillPercent,
            dwellerGearDefenseBonus = dwellerGearBonusDef,
            hasWaterPurifierRoom = hasWaterPurifierRoom,
            hasHydroponicsRoom = hasHydroponicsRoom,
            isRoomsLackingScrap = isRoomsLackingScrap,
            roomScrapOperationalMultiplier = roomScrapOperationalMultiplier,
            activeRationingPolicy = rationingPolicy,
            activeWastelandSeason = activeSeason,
            nextWastelandSeason = nextSeason,
            daysRemainingInSeason = daysRemainingInSeason,
            vaultTechEra = vaultTechEra,
            cascadeStage = activeCascadeStage,
            cascadeCrisisSummary = activeCascadeSummary,
            mothballedRoomsCount = mothballedRoomsCount,
            mothballedMwSaved = mothballedMwSaved,
            mothballedBuildingIds = mothballedSet,
            currentDirective = activeDirective,
            directiveOutputBonusPercent = directiveOutputBonusPercent,
            directiveStorageBonus = directiveStorageBonus
        )
    }

    fun computePrestigePreview(
        state: OutpostStateEntity,
        buildingsMap: Map<String, Int>,
        skillsMap: Map<String, Int> = emptyMap()
    ): PrestigePreview {
        val totalRoomLevels = buildingsMap.values.sum()
        val canAscend = state.commandLevel >= 3 || totalRoomLevels >= 4 || state.dayCounter >= 5
        val coresFromLevel = (state.commandLevel / 2).coerceAtLeast(1)
        val coresFromRooms = totalRoomLevels / 3
        val coresFromDays = state.dayCounter / 6
        val coresToEarn = if (canAscend) {
            (1 + coresFromLevel + coresFromRooms + coresFromDays).coerceAtLeast(2)
        } else {
            0
        }
        val rankCryo = skillsMap["LEG_CRYO_AWAKENING"] ?: 0
        val currentMult = (1.0 + state.prestigeLevel * 0.25) * (1.0 + rankCryo * 0.20)
        val nextMult = (1.0 + (state.prestigeLevel + 1) * 0.25) * (1.0 + rankCryo * 0.20)
        val reqSummary = if (canAscend) {
            "Ready for Overseer Ascension! Reset Vault rooms to claim +$coresToEarn Prestige Cores & Prestige Rank ${state.prestigeLevel + 1}."
        } else {
            "Requires Command Lv.3+ (now Lv.${state.commandLevel}), 4+ Total Room Levels (now $totalRoomLevels), or Day 5+ (now Day ${state.dayCounter})."
        }
        return PrestigePreview(
            canAscend = canAscend,
            coresToEarn = coresToEarn,
            currentPrestigeLevel = state.prestigeLevel,
            currentPrestigeCores = state.prestigeCores,
            currentPrestigeMultiplier = currentMult,
            nextPrestigeMultiplier = nextMult,
            totalBuiltRoomLevels = totalRoomLevels,
            requirementSummary = reqSummary
        )
    }

    fun computeDefenseRepairCost(
        state: OutpostStateEntity,
        skillsMap: Map<String, Int>,
        buildingsMap: Map<String, Int> = emptyMap()
    ): DefenseRepairCost {
        val currentIntegrity = state.defenseIntegrityPercent.coerceIn(20, 100)
        val missing = (100 - currentIntegrity).coerceIn(0, 80)
        val needsRepair = missing > 0 || state.needsDefenseRepair
        val rankPrefab = (skillsMap["IND_MODULAR_PREFAB"] ?: 0).coerceIn(0, 20)
        val refineryLv = (buildingsMap[BuildingType.BIO_SLUDGE_REFINERY.id] ?: 0).coerceIn(0, 50)
        val discount = ((1.0 - rankPrefab * 0.08) * (1.0 - refineryLv * 0.10)).coerceIn(0.45, 1.0)
        val safeDoorLv = state.blastDoorLevel.coerceIn(1, 100)
        val safeTurrets = state.sentryTurretCount.coerceIn(0, 500)
        val scale = (1.0 + (safeDoorLv - 1) * 0.25 + safeTurrets * 0.15).coerceAtLeast(1.0)
        val rawScrap = (10.0 + missing * 0.45) * scale * discount
        val scrapCost = if (needsRepair && rawScrap.isFinite()) {
            rawScrap.roundToInt().toDouble().coerceAtLeast(6.0)
        } else 0.0
        val rawGold = (5.0 + missing * 0.20) * scale * discount
        val goldCost = if (needsRepair && rawGold.isFinite()) {
            rawGold.roundToInt().toDouble().coerceAtLeast(3.0)
        } else 0.0
        val safeScrapStock = if (state.scrap.isFinite()) state.scrap else 0.0
        val safeGoldStock = if (state.gold.isFinite()) state.gold else 0.0
        val canAfford = needsRepair && safeScrapStock >= scrapCost && safeGoldStock >= goldCost

        return DefenseRepairCost(
            currentIntegrityPercent = currentIntegrity,
            missingIntegrityPercent = missing,
            scrapCost = scrapCost,
            goldCost = goldCost,
            needsRepair = needsRepair,
            canAfford = canAfford
        )
    }

    /**
     * Calculates Vault Blast Door upgrade time in seconds based on the target Door Level
     * (higher door levels take significantly longer to armor-plate and hydraulic-seal).
     */
    fun computeBlastDoorUpgradeTimeSeconds(
        targetDoorLevel: Int,
        workersMap: Map<String, WorkerClassEntity> = emptyMap(),
        skillsMap: Map<String, Int> = emptyMap()
    ): Int {
        val safeTargetLevel = targetDoorLevel.coerceIn(2, 100)
        val upgradeStep = safeTargetLevel - 1 // Lv.1->2 is step 1, Lv.2->3 is step 2, etc.
        val baseDoorSeconds = 12
        val levelEscalation = upgradeStep.toDouble().pow(1.65) + (upgradeStep - 1) * 1.15

        val scrapperCount = (workersMap[WorkerClassType.SCRAPPER.id]?.assignedCount ?: 0).coerceIn(0, MAX_VAULT_DWELLERS)
        val rankPrefab = (skillsMap["IND_MODULAR_PREFAB"] ?: 0).coerceIn(0, 20)
        val rankNano = (skillsMap["LEG_NANO_SCHEMATICS"] ?: 0).coerceIn(0, 20)
        val legacySpeedMult = (1.0 - rankNano * 0.12).coerceIn(0.40, 1.0)
        val speedDivisor = (1.0 + scrapperCount * 0.10 + rankPrefab * 0.10).coerceAtLeast(1.0)

        val rawDoorSeconds = (baseDoorSeconds * levelEscalation * legacySpeedMult) / speedDivisor
        val rawMinSeconds = (8 + upgradeStep * upgradeStep * 4) * legacySpeedMult
        val minSecondsForLevel = if (rawMinSeconds.isFinite()) rawMinSeconds.roundToInt().coerceAtLeast(5) else 5
        val safeRounded = if (rawDoorSeconds.isFinite()) rawDoorSeconds.roundToInt() else minSecondsForLevel
        return safeRounded.coerceAtLeast(minSecondsForLevel)
    }

    fun computeBlastDoorUpgradeCost(
        state: OutpostStateEntity,
        skillsMap: Map<String, Int>,
        workersMap: Map<String, WorkerClassEntity> = emptyMap()
    ): BlastDoorUpgradeCost {
        val safeCurrentDoorLv = state.blastDoorLevel.coerceIn(1, 99)
        val rankPrefab = (skillsMap["IND_MODULAR_PREFAB"] ?: 0).coerceIn(0, 20)
        val discount = (1.0 - rankPrefab * 0.08).coerceIn(0.55, 1.0)
        val growth = 1.55.pow((safeCurrentDoorLv - 1).toDouble()).let { if (it.isFinite()) it else 1000.0 }
        val rawScrapCost = 30.0 * growth * discount
        val rawGoldCost = 16.0 * growth * discount
        val scrapCost = if (rawScrapCost.isFinite()) rawScrapCost.roundToInt().toDouble().coerceAtLeast(12.0) else 30.0
        val goldCost = if (rawGoldCost.isFinite()) rawGoldCost.roundToInt().toDouble().coerceAtLeast(6.0) else 16.0
        val targetLevel = safeCurrentDoorLv + 1
        val upgradeTimeSeconds = computeBlastDoorUpgradeTimeSeconds(
            targetDoorLevel = targetLevel,
            workersMap = workersMap,
            skillsMap = skillsMap
        )
        val nextLevelUpgradeTimeSeconds = computeBlastDoorUpgradeTimeSeconds(
            targetDoorLevel = targetLevel + 1,
            workersMap = workersMap,
            skillsMap = skillsMap
        ).coerceAtLeast(upgradeTimeSeconds + 4)
        val rawRushGold = 5.0 + upgradeTimeSeconds * 0.75
        val rushGoldCost = if (rawRushGold.isFinite()) rawRushGold.roundToInt().toDouble().coerceAtLeast(5.0) else 15.0
        val safeScrapStock = if (state.scrap.isFinite()) state.scrap else 0.0
        val safeGoldStock = if (state.gold.isFinite()) state.gold else 0.0
        val canAfford = safeScrapStock >= scrapCost && safeGoldStock >= goldCost

        return BlastDoorUpgradeCost(
            currentLevel = safeCurrentDoorLv,
            targetLevel = targetLevel,
            scrapCost = scrapCost,
            goldCost = goldCost,
            defenseBonusNextLevel = 20,
            upgradeTimeSeconds = upgradeTimeSeconds,
            nextLevelUpgradeTimeSeconds = nextLevelUpgradeTimeSeconds,
            rushGoldCost = rushGoldCost,
            canAfford = canAfford
        )
    }

    fun computeFishingPreview(
        spot: FishingSpot,
        state: OutpostStateEntity,
        skillsMap: Map<String, Int>,
        buildingsMap: Map<String, Int> = emptyMap()
    ): FishingPreview {
        val rankHydro = skillsMap["IND_HYDRO_FILTRATION"] ?: 0
        val purifierLv = buildingsMap[BuildingType.ATMOSPHERIC_CONDENSER.id] ?: 0
        val uniqueMult = 1.0 + state.uniqueDwellersCount * 0.40
        val mult = (1.0 + rankHydro * 0.22) * (1.0 + purifierLv * 0.25) * uniqueMult * (1.0 + (state.commandLevel - 1) * 0.06)
        val food = (spot.baseFoodReward * mult).roundToInt().toDouble()
        val water = (spot.baseWaterReward * mult).roundToInt().toDouble()
        val gold = (spot.baseGoldReward * mult).roundToInt().toDouble()
        val xp = (spot.baseXpReward * (1.0 + state.commandLevel * 0.08)).roundToInt().toDouble()
        val canFish = (!spot.requiresSnareNet || state.craftedSnareTraps >= 1) &&
            (!spot.requiresHarpoonSpear || state.craftedSpears >= 1)

        return FishingPreview(
            spot = spot,
            foodReward = food,
            waterReward = water,
            goldReward = gold,
            xpReward = xp,
            canFish = canFish
        )
    }

    fun computeRescueBeaconCost(
        state: OutpostStateEntity,
        maxWorkerCapacity: Int,
        skillsMap: Map<String, Int>
    ): RescueBeaconCost {
        val rankDrills = skillsMap["WRK_GUILD_DRILLS"] ?: 0
        val discount = (1.0 - rankDrills * 0.10).coerceAtLeast(0.55)
        val scale = (1.0 + state.totalSurvivors * 0.32) * discount

        val fuelCost = (10.0 * scale).roundToInt().toDouble().coerceAtLeast(6.0)
        val rationsCost = (16.0 * scale).roundToInt().toDouble().coerceAtLeast(10.0)
        val waterCost = (12.0 * scale).roundToInt().toDouble().coerceAtLeast(8.0)
        val effectiveCap = maxWorkerCapacity.coerceAtMost(MAX_VAULT_DWELLERS)
        val hasSpace = state.totalSurvivors < effectiveCap
        val canAfford = hasSpace &&
            state.bioFuel >= fuelCost &&
            state.rations >= rationsCost &&
            state.water >= waterCost

        return RescueBeaconCost(
            bioFuelCost = fuelCost,
            rationsCost = rationsCost,
            waterCost = waterCost,
            hasHousingSpace = hasSpace,
            canAfford = canAfford
        )
    }

    fun computeCraftItemCost(
        item: CraftableItemType,
        state: OutpostStateEntity,
        skillsMap: Map<String, Int>,
        buildingsMap: Map<String, Int> = emptyMap(),
        workersMap: Map<String, WorkerClassEntity> = emptyMap()
    ): CraftItemCost {
        val rankPrefab = skillsMap["IND_MODULAR_PREFAB"] ?: 0
        val discount = (1.0 - rankPrefab * 0.08).coerceAtLeast(0.65)
        val scale = if (item == CraftableItemType.AETHER_CROSSBOW) 1.40.pow(state.aetherCrossbowLevel.toDouble()) else 1.0
        val scrapCost = (item.scrapCost * scale * discount).roundToInt().toDouble()
        val fuelCost = (item.fuelCost * scale * discount).roundToInt().toDouble()
        val goldCost = (item.goldCost * scale * discount).roundToInt().toDouble()
        val aetherCost = if (item.aetherCost > 0) (item.aetherCost + (if (item == CraftableItemType.AETHER_CROSSBOW) state.aetherCrossbowLevel else 0)).toDouble() else 0.0
        val thoriumCost = item.thoriumCost
        val coresCost = item.coresCost

        val craftTimeSeconds = computeCraftTimeSeconds(
            item = item,
            state = state,
            buildingsMap = buildingsMap,
            workersMap = workersMap,
            skillsMap = skillsMap
        )
        val rushGoldCost = (4.0 + craftTimeSeconds * 0.65).roundToInt().toDouble()

        val canAfford = state.scrap >= scrapCost &&
            state.bioFuel >= fuelCost &&
            state.gold >= goldCost &&
            state.aetherCells >= aetherCost &&
            state.thoriumRods >= thoriumCost &&
            state.mutagenCores >= coresCost

        return CraftItemCost(
            scrapCost = scrapCost,
            fuelCost = fuelCost,
            goldCost = goldCost,
            aetherCost = aetherCost,
            thoriumCost = thoriumCost,
            coresCost = coresCost,
            craftTimeSeconds = craftTimeSeconds,
            rushGoldCost = rushGoldCost,
            canAfford = canAfford
        )
    }

    fun computeHuntPreview(
        target: HuntingTarget,
        state: OutpostStateEntity,
        workersMap: Map<String, WorkerClassEntity>,
        skillsMap: Map<String, Int>,
        activePhase: DayNightPhase,
        buildingsMap: Map<String, Int> = emptyMap()
    ): HuntPreview {
        val rankRotary = skillsMap["IND_ROTARY_SAWS"] ?: 0
        val farmLv = buildingsMap[BuildingType.HYDROPONIC_GREENHOUSE.id] ?: 0
        val stalkerCount = workersMap[WorkerClassType.NIGHT_STALKER.id]?.assignedCount ?: 0
        val uniqueMult = 1.0 + state.uniqueDwellersCount * 0.40
        val crossbowMult = 1.0 + state.aetherCrossbowLevel * 0.40
        val phaseBonus = when (activePhase) {
            DayNightPhase.DUSK -> 1.25
            DayNightPhase.RAD_NIGHT -> if (target == HuntingTarget.APEX_RAD_BEHEMOTH) 1.35 else 1.15
            else -> 1.0
        }
        val totalHuntMult = (1.0 + rankRotary * 0.25) * (1.0 + farmLv * 0.25) * (1.0 + stalkerCount * 0.15) * uniqueMult * crossbowMult * phaseBonus

        val ammoRec = when (target) {
            HuntingTarget.DUNE_RAZORBACK -> 2
            HuntingTarget.CANYON_PACK_STALKER -> 3
            HuntingTarget.APEX_RAD_BEHEMOTH -> 5
            HuntingTarget.GLOWING_LEVIATHAN_WYRM -> 8
        }
        val ammoYieldMult = if (state.ammoRounds >= ammoRec) 1.0 else 0.60
        val food = (target.baseFoodReward * totalHuntMult * ammoYieldMult).roundToInt().toDouble()
        val water = (target.baseWaterReward * totalHuntMult * ammoYieldMult).roundToInt().toDouble()
        val gold = (target.baseGoldReward * uniqueMult * (1.0 + state.commandLevel * 0.08)).roundToInt().toDouble()
        val cores = target.baseCoresReward
        val xp = (target.baseXpReward * (1.0 + state.commandLevel * 0.10)).roundToInt().toDouble()

        val baseInjuryRisk = when (target) {
            HuntingTarget.DUNE_RAZORBACK -> 14
            HuntingTarget.CANYON_PACK_STALKER -> 22
            HuntingTarget.APEX_RAD_BEHEMOTH -> 30
            HuntingTarget.GLOWING_LEVIATHAN_WYRM -> 38
        }
        val ammoScarcityPenalty = if (state.ammoRounds < ammoRec) 14 else 0
        val gearProtection = state.aetherCrossbowLevel * 4 + stalkerCount * 3 + (if (state.hazmatSuitsCount > 0) 6 else 0)
        val effectiveInjuryRisk = (baseInjuryRisk + ammoScarcityPenalty - gearProtection).coerceIn(4, 65)

        val canExecute = state.craftedSpears >= target.spearsRequired &&
            state.craftedSnareTraps >= target.trapsRequired &&
            (!target.requiresCrossbow || state.aetherCrossbowLevel >= 1)

        return HuntPreview(
            target = target,
            foodReward = food,
            waterReward = water,
            goldReward = gold,
            coresReward = cores,
            xpReward = xp,
            counterAttackInjuryChancePercent = effectiveInjuryRisk,
            ammoRecommended = ammoRec,
            canExecute = canExecute
        )
    }

    fun computeTownSellQuote(
        town: TradeTown,
        resourceType: ResourceType,
        batchAmount: Double,
        activePhase: DayNightPhase,
        skillsMap: Map<String, Int>
    ): Double {
        val baseGoldPerUnit = when (resourceType) {
            ResourceType.SCRAP -> 0.55
            ResourceType.WATER -> 0.65
            ResourceType.RATIONS -> 0.70
            ResourceType.BIO_FUEL -> 0.90
            ResourceType.RESEARCH_DATA -> 1.20
            else -> 0.50
        }
        val isFavoredResource = resourceType == town.favoredResource1 || resourceType == town.favoredResource2
        val favoredMult = if (isFavoredResource) 1.35 else 1.0
        val phaseMult = if (activePhase == town.favoredPhase) 1.25 else 1.0
        val rankMerchant = skillsMap["WRK_GUILD_DRILLS"] ?: 0
        val rankMonopoly = skillsMap["WRK_SOVEREIGN_COMMAND"] ?: 0
        val skillMult = (1.0 + rankMerchant * 0.15) * (1.0 + rankMonopoly * 0.30)

        return (batchAmount * baseGoldPerUnit * favoredMult * phaseMult * skillMult).roundToInt().toDouble().coerceAtLeast(5.0)
    }

    fun computeRareItemGoldPrice(
        item: RareSyndicateItem,
        skillsMap: Map<String, Int>
    ): Double {
        val rankSynergy = skillsMap["WRK_CROSS_CLASS_SYNERGY"] ?: 0
        val discount = (1.0 - rankSynergy * 0.06).coerceAtLeast(0.70)
        return (item.baseGoldCost * discount).roundToInt().toDouble()
    }

    fun computePowerStationUpgradeCost(
        station: PowerStationType,
        state: OutpostStateEntity,
        skillsMap: Map<String, Int>
    ): PowerStationUpgradeCost {
        val currentLevel = when (station) {
            PowerStationType.BIO_SLUDGE_TURBINE -> state.bioTurbineLevel
            PowerStationType.SOLAR_HELIOSTAT_TOWER -> state.solarTowerLevel
            PowerStationType.THORIUM_FISSION_REACTOR -> state.thoriumReactorLevel
            PowerStationType.AETHER_RESONANCE_SUBSTATION -> state.aetherSubstationLevel
        }
        val rankPrefab = skillsMap["IND_MODULAR_PREFAB"] ?: 0
        val discount = (1.0 - rankPrefab * 0.08).coerceAtLeast(0.60)
        val growth = 1.60.pow(currentLevel.toDouble())

        val scrapCost = (station.baseScrapCost * 1.20 * growth * discount).roundToInt().toDouble()
        val goldCost = (station.baseGoldCost * 1.20 * growth * discount).roundToInt().toDouble()
        val thoriumCost = if (station.baseThoriumCost > 0) (station.baseThoriumCost + currentLevel).toDouble() else 0.0
        val aetherCost = if (station.baseAetherCost > 0) (station.baseAetherCost + currentLevel).toDouble() else 0.0
        val coresCost = if (station.baseCoresCost > 0) (station.baseCoresCost + currentLevel).toDouble() else 0.0

        val currentFuelUpkeep = computeReactorFuelUpkeepForStation(station, currentLevel)
        val nextLevelFuelUpkeep = computeReactorFuelUpkeepForStation(station, currentLevel + 1)

        val canAfford = state.scrap >= scrapCost &&
            state.gold >= goldCost &&
            state.thoriumRods >= thoriumCost &&
            state.aetherCells >= aetherCost &&
            state.mutagenCores >= coresCost

        return PowerStationUpgradeCost(
            currentLevel = currentLevel,
            scrapCost = scrapCost,
            goldCost = goldCost,
            thoriumCost = thoriumCost,
            aetherCost = aetherCost,
            coresCost = coresCost,
            currentFuelUpkeepPerSec = currentFuelUpkeep,
            nextLevelFuelUpkeepPerSec = nextLevelFuelUpkeep,
            canAfford = canAfford
        )
    }

    fun computeConvoyUpgradeCost(state: OutpostStateEntity): ConvoyUpgradeCost {
        val level = state.convoyLevel
        val growth = 1.50.pow((level - 1).toDouble())
        val scrapCost = (55.0 * growth).roundToInt().toDouble()
        val goldCost = (45.0 * growth).roundToInt().toDouble()
        val thoriumCost = level.toDouble()
        val aetherCost = level.toDouble()
        val canAfford = state.scrap >= scrapCost &&
            state.gold >= goldCost &&
            state.thoriumRods >= thoriumCost &&
            state.aetherCells >= aetherCost

        return ConvoyUpgradeCost(
            currentLevel = level,
            scrapCost = scrapCost,
            goldCost = goldCost,
            thoriumCost = thoriumCost,
            aetherCost = aetherCost,
            canAfford = canAfford
        )
    }

    fun computeSalvageNodeYield(
        nodeType: SalvageNodeType,
        state: OutpostStateEntity,
        buildingsMap: Map<String, Int>,
        workersMap: Map<String, WorkerClassEntity>,
        skillsMap: Map<String, Int>,
        activePhase: DayNightPhase
    ): SalvageYieldPreview {
        val rigMult = 1.0 + (state.salvageRigLevel - 1) * 0.42
        val uniqueMult = 1.0 + state.uniqueDwellersCount * 0.40
        val comboMult = 1.0 + state.salvageComboCount.coerceIn(0, 15) * 0.08
        val rankRotary = skillsMap["IND_ROTARY_SAWS"] ?: 0
        val rankMerchant = skillsMap["WRK_GUILD_DRILLS"] ?: 0
        val rankMonopoly = skillsMap["WRK_SOVEREIGN_COMMAND"] ?: 0
        val skillMult = (1.0 + rankRotary * 0.25) * uniqueMult * comboMult

        val yardLv = buildingsMap[BuildingType.SALVAGE_YARD.id] ?: 0
        val condenserLv = buildingsMap[BuildingType.ATMOSPHERIC_CONDENSER.id] ?: 0
        val hydroLv = buildingsMap[BuildingType.HYDROPONIC_GREENHOUSE.id] ?: 0
        val refineryLv = buildingsMap[BuildingType.BIO_SLUDGE_REFINERY.id] ?: 0
        val arrayLv = buildingsMap[BuildingType.SIGNAL_RESEARCH_ARRAY.id] ?: 0
        val mutagenRigLv = buildingsMap[BuildingType.NOCTURNAL_EXTRACTION_RIG.id] ?: 0

        val goldSkillMult = (1.0 + rankMerchant * 0.15) * (1.0 + rankMonopoly * 0.30) * (1.0 + yardLv * 0.25) * uniqueMult * comboMult

        val scrapperEntity = workersMap[WorkerClassType.SCRAPPER.id]
        val scrapperPerkMult = if ((scrapperEntity?.assignedCount ?: 0) > 0 && (scrapperEntity?.classLevel ?: 1) >= 5) 1.20 else 1.0

        fun bonusForResource(res: ResourceType): Double = when (res) {
            ResourceType.SCRAP -> SCRAP_GATHER_RATE_MULTIPLIER * (1.0 + yardLv * 0.25) * scrapperPerkMult * activePhase.scrapMultiplier
            ResourceType.WATER -> (1.0 + condenserLv * 0.25) * activePhase.waterMultiplier
            ResourceType.RATIONS -> (1.0 + hydroLv * 0.25) * activePhase.rationsMultiplier
            ResourceType.BIO_FUEL -> (1.0 + refineryLv * 0.30)
            ResourceType.RESEARCH_DATA -> (1.0 + arrayLv * 0.30) * activePhase.researchMultiplier
            ResourceType.MUTAGEN_CORES -> (1.0 + mutagenRigLv * 0.40) * activePhase.mutagenSpawnMultiplier
            else -> 1.0
        }

        val pickaxeDurabilityMult = when {
            state.pickaxeDurabilityPercent <= 0 -> 0.35
            state.pickaxeDurabilityPercent < 30 -> 0.68
            else -> 1.0
        }
        val staminaManualMult = when {
            state.dwellerStaminaPercent <= 15 -> 0.50
            state.dwellerStaminaPercent < 35 -> 0.78
            else -> 1.0
        }
        val caveInMult = if (state.activeCaveInHazard) 0.45 else 1.0
        val nodeStorageSlowdownMult = when {
            state.isStoreRoomGatheringHalted -> STORAGE_FULL_SLOWDOWN_MULTIPLIER
            nodeType.primaryResource == ResourceType.SCRAP && state.isScrapGatheringHalted -> STORAGE_FULL_SLOWDOWN_MULTIPLIER
            nodeType.primaryResource == ResourceType.WATER && state.isWaterGatheringHalted -> STORAGE_FULL_SLOWDOWN_MULTIPLIER
            nodeType.primaryResource == ResourceType.RATIONS && state.isRationsGatheringHalted -> STORAGE_FULL_SLOWDOWN_MULTIPLIER
            nodeType.primaryResource == ResourceType.BIO_FUEL && state.isFuelGatheringHalted -> STORAGE_FULL_SLOWDOWN_MULTIPLIER
            nodeType.primaryResource == ResourceType.RESEARCH_DATA && state.isResearchGatheringHalted -> STORAGE_FULL_SLOWDOWN_MULTIPLIER
            else -> 1.0
        }
        val hardcoreMiningConditionMult = pickaxeDurabilityMult * caveInMult * staminaManualMult * nodeStorageSlowdownMult

        val primaryAmount = (nodeType.basePrimaryYield * rigMult * skillMult * bonusForResource(nodeType.primaryResource) * hardcoreMiningConditionMult)
            .roundToInt().toDouble().coerceAtLeast(1.0)
        val secondaryAmount = (nodeType.baseSecondaryYield * rigMult * skillMult * bonusForResource(nodeType.secondaryResource) * hardcoreMiningConditionMult)
            .roundToInt().toDouble().coerceAtLeast(1.0)

        // Finding Scrap yields a real small amount of Gold alongside the vein's base Gold trace
        val hasScrapInNode = nodeType.primaryResource == ResourceType.SCRAP || nodeType.secondaryResource == ResourceType.SCRAP
        val scrapGoldTraceBonus = if (hasScrapInNode) (0.6 + yardLv * 0.25) else 0.0
        val rawGoldAmount = (nodeType.baseGoldYield + scrapGoldTraceBonus) * rigMult * goldSkillMult *
            (1.0 + (state.commandLevel - 1) * 0.08) * hardcoreMiningConditionMult
        val goldAmount = ((rawGoldAmount * 10.0).roundToInt() / 10.0).coerceAtLeast(1.5)

        val tapPrimaryAmount = (primaryAmount * 0.30).roundToInt().toDouble().coerceAtLeast(1.0)
        val rawTapGold = (goldAmount * 0.28) + (if (hasScrapInNode) 0.2 else 0.0)
        val tapGoldAmount = ((rawTapGold * 10.0).roundToInt() / 10.0).coerceAtLeast(0.5)
        val tapFuelAmount = (((0.15 + yardLv * 0.12 + (state.salvageRigLevel - 1) * 0.06) * hardcoreMiningConditionMult) * 10.0).roundToInt() / 10.0
        val bonusFuelFromGoldMine = (((0.45 + yardLv * 0.35 + (state.salvageRigLevel - 1) * 0.15) * hardcoreMiningConditionMult) * 10.0).roundToInt() / 10.0

        val xpReward = ((16.0 + state.salvageRigLevel * 5.0 + state.commandLevel * 4.0) * comboMult).roundToInt().toDouble()
        val bonusCores = if (nodeType.isNocturnal && mutagenRigLv > 0) mutagenRigLv.toDouble() else 0.0
        val rareJackpotChancePercent = (15 + state.salvageRigLevel * 7 + (if (nodeType.isNocturnal) 15 else 0)).coerceAtMost(75)

        return SalvageYieldPreview(
            primaryAmount = primaryAmount,
            secondaryAmount = secondaryAmount,
            goldAmount = goldAmount,
            tapPrimaryAmount = tapPrimaryAmount,
            tapGoldAmount = tapGoldAmount,
            xpReward = xpReward,
            cacheDropCount = nodeType.cacheDropCount,
            bonusMutagenCores = bonusCores,
            rareJackpotChancePercent = rareJackpotChancePercent,
            tapFuelAmount = tapFuelAmount,
            bonusFuelFromGoldMine = bonusFuelFromGoldMine
        )
    }

    fun computeSalvageRigUpgradeCost(rigLevel: Int): Pair<Double, Double> {
        // Manual mining and Pickaxe upgrades cost 0 Scrap (gated by Smeltery room level and small Bio-Fuel at Lv.2+)
        val scrapCost = 0.0
        val fuelCost = if (rigLevel >= 2) (14.0 * 1.48.pow((rigLevel - 2).toDouble())).roundToInt().toDouble() else 0.0
        return scrapCost to fuelCost
    }

    fun computeBuildingUpgradeCost(
        buildingType: BuildingType,
        currentLevel: Int,
        state: OutpostStateEntity,
        skillsMap: Map<String, Int>,
        workersMap: Map<String, WorkerClassEntity> = emptyMap(),
        currentTotalMwGenerated: Double = 100.0,
        currentTotalMwDemand: Double = 4.0,
        currentStorageCap: Double = Double.MAX_VALUE
    ): BuildingUpgradeCost {
        val rankPrefab = skillsMap["IND_MODULAR_PREFAB"] ?: 0
        val rankNano = skillsMap["LEG_NANO_SCHEMATICS"] ?: 0
        val discount = ((1.0 - rankPrefab * 0.08) * (1.0 - rankNano * 0.06)).coerceAtLeast(0.50)

        // Rebalanced room upgrade cost curve aligned around Vault resource caps unlocked by room upgrades
        val highLevelEscalation = 1.0 +
            currentLevel * 0.35 +
            (currentLevel * currentLevel) * 0.16
        val factor = buildingType.costGrowthFactor.pow(currentLevel.toDouble()) * highLevelEscalation * discount

        val scrapCost = (buildingType.baseScrapCost * factor).roundToInt().toDouble()
        val baseWater = if (buildingType.baseWaterCost > 0.0) {
            buildingType.baseWaterCost * (1.0 + currentLevel * 0.15)
        } else if (currentLevel >= 1) {
            // Upgrading any room to Lv.2+ requires progressively more Cooling Water per level
            12.0 + buildingType.ordinal * 2.5 + (currentLevel - 1) * 4.0
        } else 0.0
        val waterCost = (baseWater * factor).roundToInt().toDouble()

        val baseFuel = if (buildingType.baseFuelCost > 0.0) {
            buildingType.baseFuelCost * (1.0 + currentLevel * 0.15)
        } else if (currentLevel >= 2) {
            // Upgrading any room to Lv.3+ requires progressively more Industrial Bio-Fuel per level
            10.0 + buildingType.ordinal * 2.2 + (currentLevel - 2) * 3.8
        } else 0.0
        val fuelCost = (baseFuel * factor).roundToInt().toDouble()

        val coresCost = if (buildingType.baseCoresCost > 0.0) {
            (buildingType.baseCoresCost * factor).roundToInt().toDouble()
        } else if (currentLevel >= 4) {
            (currentLevel - 3).toDouble()
        } else 0.0

        val currentMw = computeBuildingTotalMegawatts(buildingType, currentLevel)
        val nextMw = computeBuildingTotalMegawatts(buildingType, currentLevel + 1)
        val deltaMw = ((nextMw - currentMw) * 10.0).roundToInt() / 10.0
        val willExceedPowerGrid = (currentTotalMwDemand + deltaMw) > currentTotalMwGenerated

        val currentRoomUpkeep = computeBuildingResourceUpkeep(
            buildingType = buildingType,
            level = currentLevel,
            hydroDiscountMult = 1.0,
            commandLevel = state.commandLevel,
            dayCounter = state.dayCounter
        )
        val nextLevelRoomUpkeep = computeBuildingResourceUpkeep(
            buildingType = buildingType,
            level = currentLevel + 1,
            hydroDiscountMult = 1.0,
            commandLevel = state.commandLevel,
            dayCounter = state.dayCounter
        )

        val storageCapBonusCurrent = computeSingleRoomStorageCapContribution(buildingType, currentLevel)
        val storageCapBonusNextLevel = computeSingleRoomStorageCapContribution(buildingType, currentLevel + 1)
        val additionalStorageCapGain = (storageCapBonusNextLevel - storageCapBonusCurrent).coerceAtLeast(0.0)
        val exceedsCurrentStorageCap = scrapCost > currentStorageCap ||
            waterCost > currentStorageCap ||
            fuelCost > currentStorageCap

        val targetRoomLevel = currentLevel + 1
        val roomNumber = buildingType.ordinal + 1

        // Construction Time depends on BOTH the Room Level (Lv.1, Lv.2, Lv.3...) and the Room Number (#1..#8)!
        val buildTimeSeconds = computeRoomBuildTimeSeconds(
            buildingType = buildingType,
            targetRoomLevel = targetRoomLevel,
            workersMap = workersMap,
            skillsMap = skillsMap
        )
        val nextLevelBuildTimeSeconds = computeRoomBuildTimeSeconds(
            buildingType = buildingType,
            targetRoomLevel = targetRoomLevel + 1,
            workersMap = workersMap,
            skillsMap = skillsMap
        ).coerceAtLeast(buildTimeSeconds + 4)
        val rushGoldCost = (6.0 + buildTimeSeconds * 0.8).roundToInt().toDouble()

        val meetsLevel = state.commandLevel >= buildingType.requiredCommandLevel
        val canAfford = meetsLevel &&
            state.scrap >= scrapCost &&
            state.water >= waterCost &&
            state.bioFuel >= fuelCost &&
            state.mutagenCores >= coresCost

        return BuildingUpgradeCost(
            scrapCost = scrapCost,
            waterCost = waterCost,
            fuelCost = fuelCost,
            coresCost = coresCost,
            roomNumber = roomNumber,
            targetRoomLevel = targetRoomLevel,
            currentMegawattDraw = currentMw,
            nextLevelMegawattDraw = nextMw,
            additionalMegawattDraw = deltaMw,
            currentRoomUpkeep = currentRoomUpkeep,
            nextLevelRoomUpkeep = nextLevelRoomUpkeep,
            storageCapBonusCurrent = storageCapBonusCurrent,
            storageCapBonusNextLevel = storageCapBonusNextLevel,
            additionalStorageCapGain = additionalStorageCapGain,
            exceedsCurrentStorageCap = exceedsCurrentStorageCap,
            buildTimeSeconds = buildTimeSeconds,
            nextLevelBuildTimeSeconds = nextLevelBuildTimeSeconds,
            rushGoldCost = rushGoldCost,
            willExceedPowerGrid = willExceedPowerGrid,
            canAfford = canAfford,
            meetsLevelRequirement = meetsLevel
        )
    }

    fun computeSkillUpgradeCost(
        node: SkillNodeDefinition,
        currentRank: Int,
        state: OutpostStateEntity,
        skillsMap: Map<String, Int>
    ): SkillUpgradeCost {
        val isMaxed = currentRank >= node.maxRank
        val prereqMet = node.prerequisiteId == null || (skillsMap[node.prerequisiteId] ?: 0) > 0
        val factor = 1.48.pow(currentRank.toDouble())

        val researchCost = if (node.baseResearchCost > 0.0) (node.baseResearchCost * factor).roundToInt().toDouble() else 0.0
        val goldCost = if (node.baseGoldCost > 0.0) (node.baseGoldCost * factor).roundToInt().toDouble() else 0.0
        val coresCost = if (node.baseCoresCost > 0.0) {
            (node.baseCoresCost + currentRank * 1.5).roundToInt().toDouble()
        } else 0.0
        val prestigeCoreCost = if (node.prestigeCoreCostPerRank > 0) {
            node.prestigeCoreCostPerRank + currentRank
        } else 0

        val canAfford = !isMaxed &&
            prereqMet &&
            state.availableSkillPoints >= node.skillPointCostPerRank &&
            state.researchData >= researchCost &&
            state.gold >= goldCost &&
            state.mutagenCores >= coresCost &&
            state.prestigeCores >= prestigeCoreCost

        return SkillUpgradeCost(
            skillPointCost = node.skillPointCostPerRank,
            researchCost = researchCost,
            goldCost = goldCost,
            coresCost = coresCost,
            prestigeCoreCost = prestigeCoreCost,
            canAfford = canAfford,
            prerequisiteMet = prereqMet,
            isMaxed = isMaxed
        )
    }

    /**
     * Named Nuclear Warlord Bosses for every 10-Day Mega-Siege milestone (Day 10, 20, 30, 40+).
     */
    fun computeDay10BossTitle(milestoneDay: Int): String = when {
        milestoneDay <= 10 -> "Ironclad Goliath Siege Tank"
        milestoneDay == 20 -> "Rad-Behemoth Siegebreaker Herd"
        milestoneDay == 30 -> "Enclave Orbital Strike Dreadnought"
        else -> "Apex Nuclear Warlord Titan Mk.${milestoneDay / 10}"
    }

    /**
     * Milestone Day 10 / 20 / 30 Nuclear Warlord Mega-Sieges scale super-linearly with each 10-day milestone.
     */
    fun computeDay10SiegeRequiredDefense(milestoneDay: Int): Int {
        val tier = (milestoneDay.coerceAtLeast(10) / 10).coerceAtLeast(1)
        val tierExtra = tier - 1
        return 210 + tierExtra * 250 + tierExtra * tierExtra * 65
    }

    /**
     * 10-Day Preparation Peace Window:
     * Strictly zero Raider or Night Swarm attacks occur on Days 1 to 10 (returns 0 Required Defense).
     * Starting on Day 11 (FIRST_ATTACK_DAY), daily attacks on the Vault begin and get progressively harder each passing day,
     * with Mega-Siege Warlord waves on Day 20, Day 30, Day 40+ and Nocturnal Mutant Swarm multipliers at Dusk (+15%) and Night (+35%).
     */
    fun computeDailyRaidRequiredDefense(
        dayNumber: Int,
        activePhase: DayNightPhase = DayNightPhase.SCORCHING_DAY
    ): Int {
        val safeDay = dayNumber.coerceAtLeast(1)
        if (safeDay <= PREPARATION_PEACE_DAYS) {
            return 0
        }
        val isDay10Multiple = safeDay % 10 == 0
        val baseRequiredDefense = if (isDay10Multiple) {
            computeDay10SiegeRequiredDefense(safeDay)
        } else {
            val elapsedAttackDays = (safeDay - FIRST_ATTACK_DAY).coerceAtLeast(0)
            val linearGrowth = elapsedAttackDays * 9
            val acceleratingCurve = (elapsedAttackDays.toDouble().pow(1.52) * 3.8).roundToInt()
            val warbandTierStep = (elapsedAttackDays / 3) * 12
            14 + linearGrowth + acceleratingCurve + warbandTierStep
        }
        val nightThreatMultiplier = when (activePhase) {
            DayNightPhase.RAD_NIGHT -> 1.35
            DayNightPhase.DUSK -> 1.15
            else -> 1.0
        }
        return (baseRequiredDefense * nightThreatMultiplier).roundToInt()
    }

    /**
     * Evaluates the dual hardcore survival 6-Day Game Over conditions at the end of each Vault day:
     * 1) If the Vault runs out of critical resources or takes more resources to run than it is gaining,
     *    Vault Defenses shut down immediately and after 6 Days -> GAME OVER!
     * 2) If the Vault runs out of power (isVaultBlackout), after 6 Days without power -> GAME OVER!
     */
    fun evaluateSurvivalEndOfDay(
        state: OutpostStateEntity,
        snapshot: ProductionSnapshot
    ): SurvivalDayEvaluation {
        val isResourceDepletedOrDeficit = !snapshot.isEarlyBunkerGraceActive && (
            snapshot.isVaultResourceDeficit ||
                snapshot.isWorkersStarvingOrDehydrated ||
                (snapshot.totalBuiltRoomsCount > 0 && snapshot.isRoomsLackingScrap)
            )
        val nextDeficitDays = if (isResourceDepletedOrDeficit) {
            state.deficitDaysCount + 1
        } else {
            0
        }
        val nextBlackoutDays = if (snapshot.isVaultBlackout) {
            state.blackoutDaysCount + 1
        } else {
            0
        }

        val deficitGameOver = nextDeficitDays >= MAX_DEFICIT_DAYS_BEFORE_GAME_OVER
        val blackoutGameOver = nextBlackoutDays >= MAX_BLACKOUT_DAYS_BEFORE_GAME_OVER
        val isGameOver = deficitGameOver || blackoutGameOver

        val reason = when {
            deficitGameOver ->
                "VAULT COLLAPSED (6-DAY RESOURCE DEPLETION): Your Vault ran out of critical survival resources or remained in resource deficit (-${snapshot.totalVaultResourceDrainPerSec}/s vs +${snapshot.totalGrossResourceGainPerSec}/s) for 6 consecutive days."
            blackoutGameOver ->
                "VAULT COLLAPSED (6-DAY POWER BLACKOUT): Your Vault remained out of power (${snapshot.totalMegawattGenerated.roundToInt()}/${snapshot.totalMegawattDemand.roundToInt()} MW) for 6 consecutive days, causing total life-support failure."
            else -> ""
        }

        return SurvivalDayEvaluation(
            nextBlackoutDaysCount = nextBlackoutDays,
            nextDeficitDaysCount = nextDeficitDays,
            isGameOver = isGameOver,
            gameOverReason = reason
        )
    }

    /**
     * Computes background phone notification ETAs when the game is not loaded:
     * 1) When a Vault Room is done upgrading
     * 2) When Vault Resources reach max capacity
     * 3) When the Vault runs out of power (Blackout)
     */
    fun computeOfflineNotificationSchedule(
        state: OutpostStateEntity,
        production: ProductionSnapshot,
        activeConstructionBuildingId: String = state.activeConstructionBuildingId,
        activeConstructionTargetLevel: Int = state.activeConstructionTargetLevel,
        activeConstructionRemainingSeconds: Int = state.activeConstructionRemainingSeconds,
        nowMillis: Long = System.currentTimeMillis()
    ): OfflineNotificationSchedule {
        // 1. Room Upgrade Complete ETA
        val bType = BuildingType.entries.firstOrNull { it.id == activeConstructionBuildingId }
        val secondsUntilRoomDone = if (bType != null && activeConstructionRemainingSeconds > 0) {
            activeConstructionRemainingSeconds.coerceAtLeast(1).toLong()
        } else {
            null
        }
        val roomTitle = bType?.displayName ?: ""

        // 2. Resources At Max Capacity ETA
        val cap = production.storageCap
        val alreadyAtMaxResources = buildList {
            if (state.scrap >= cap - 0.5) add("Scrap")
            if (state.water >= cap - 0.5) add("Water")
            if (state.rations >= cap - 0.5) add("Food Rations")
            if (state.bioFuel >= cap - 0.5) add("Bio-Fuel")
        }
        var secondsUntilMax: Long? = null
        var maxResourceLabel = ""
        if (alreadyAtMaxResources.isNotEmpty()) {
            secondsUntilMax = 3L
            maxResourceLabel = "${alreadyAtMaxResources.joinToString(", ")} at Max (${cap.toInt()})"
        } else {
            val candidates = mutableListOf<Pair<Long, String>>()
            if (production.scrapPerSec > 0.02 && state.scrap < cap) {
                val secs = ((cap - state.scrap) / production.scrapPerSec).roundToInt().toLong().coerceAtLeast(2L)
                candidates.add(secs to "Scrap Metal (${cap.toInt()} Cap)")
            }
            if (production.waterPerSec > 0.02 && state.water < cap) {
                val secs = ((cap - state.water) / production.waterPerSec).roundToInt().toLong().coerceAtLeast(2L)
                candidates.add(secs to "Purified Water (${cap.toInt()} Cap)")
            }
            if (production.rationsPerSec > 0.02 && state.rations < cap) {
                val secs = ((cap - state.rations) / production.rationsPerSec).roundToInt().toLong().coerceAtLeast(2L)
                candidates.add(secs to "Food Rations (${cap.toInt()} Cap)")
            }
            if (production.bioFuelPerSec > 0.02 && state.bioFuel < cap) {
                val secs = ((cap - state.bioFuel) / production.bioFuelPerSec).roundToInt().toLong().coerceAtLeast(2L)
                candidates.add(secs to "Bio-Fuel (${cap.toInt()} Cap)")
            }
            val earliest = candidates.minByOrNull { it.first }
            if (earliest != null) {
                secondsUntilMax = earliest.first
                maxResourceLabel = earliest.second
            }
        }

        // 3. Vault Ran Out Of Power ETA
        var secondsUntilPowerOut: Long? = null
        var powerOutReason = ""
        if (production.isVaultBlackout) {
            secondsUntilPowerOut = 3L
            powerOutReason = "Vault Power Blackout (${production.totalMegawattGenerated.roundToInt()}/${production.totalMegawattDemand.roundToInt()} MW)!"
        } else {
            val powerOutCandidates = mutableListOf<Pair<Long, String>>()
            if (state.thoriumOverdriveUntilMillis > nowMillis) {
                val overdriveLeftSecs = ((state.thoriumOverdriveUntilMillis - nowMillis) / 1000L).coerceAtLeast(2L)
                val crankStack = state.activeDynamoCrankStack.coerceIn(1, MAX_DYNAMO_CRANKS_PER_BLACKOUT)
                val approxMwWithoutDynamo = (production.totalMegawattGenerated / 1.40) - (16.0 * crankStack)
                if (approxMwWithoutDynamo < production.totalMegawattDemand) {
                    powerOutCandidates.add(
                        overdriveLeftSecs to "Emergency Dynamo Crank expired — Vault dropped below ${production.totalMegawattDemand.roundToInt()} MW!"
                    )
                }
            }
            if (production.bioFuelPerSec < -0.02 && state.bioFuel > 0.0 && state.bioTurbineLevel > 0) {
                val fuelDrySecs = (state.bioFuel / (-production.bioFuelPerSec)).roundToInt().toLong().coerceAtLeast(2L)
                val turbineLossMw = state.bioTurbineLevel * PowerStationType.BIO_SLUDGE_TURBINE.baseMegawattsPerLevel * 0.20
                val reason = if (production.totalMegawattGenerated - turbineLossMw < production.totalMegawattDemand) {
                    "Reactor Bio-Fuel depleted — Vault Power dropped below ${production.totalMegawattDemand.roundToInt()} MW!"
                } else {
                    "Reactor Bio-Fuel tank dry — Bio-Sludge Turbine running on 80% backup vapor mode!"
                }
                powerOutCandidates.add(fuelDrySecs to reason)
            }
            val earliestOutage = powerOutCandidates.minByOrNull { it.first }
            if (earliestOutage != null) {
                secondsUntilPowerOut = earliestOutage.first
                powerOutReason = earliestOutage.second
            }
        }

        return OfflineNotificationSchedule(
            secondsUntilRoomUpgradeComplete = secondsUntilRoomDone,
            upgradingRoomTitle = roomTitle,
            upgradingRoomTargetLevel = activeConstructionTargetLevel,
            secondsUntilResourcesAtMax = secondsUntilMax,
            maxResourceSummary = maxResourceLabel,
            secondsUntilPowerOutage = secondsUntilPowerOut,
            powerOutageReasonSummary = powerOutReason
        )
    }

    /**
     * Costs and requirements for the 4 Eden Core modules
     */
    fun computeEdenCoreModuleCosts(
        state: OutpostStateEntity,
        buildingsMap: Map<String, Int>
    ): List<EdenCoreModuleCost> {
        val geoLv = buildingsMap[BuildingType.GEOTHERMAL_TAP.id] ?: 0
        val farmLv = buildingsMap[BuildingType.HYDROPONIC_GREENHOUSE.id] ?: 0
        val cryoLv = buildingsMap[BuildingType.CRYO_STASIS_VAULT.id] ?: 0
        val thoriumLv = state.thoriumReactorLevel
        val arrayLv = buildingsMap[BuildingType.SIGNAL_RESEARCH_ARRAY.id] ?: 0

        return listOf(
            EdenCoreModuleCost(
                phaseNumber = 1,
                title = "Phase I: Geothermal Core Sink",
                description = "Channels deep mantle thermal heat into a perpetual heat sink. Requires Lv.4+ Geothermal Tap.",
                scrapCost = 400.0,
                fuelCost = 200.0,
                goldCost = 40.0,
                requiredBuildingSummary = "Geothermal Tap Lv.4+ (Currently Lv.$geoLv)",
                canAfford = state.scrap >= 400.0 && state.bioFuel >= 200.0 && state.gold >= 40.0,
                requirementsMet = geoLv >= 4,
                isBuilt = state.edenCorePhase >= 1
            ),
            EdenCoreModuleCost(
                phaseNumber = 2,
                title = "Phase II: Closed Biosphere Seed Vault",
                description = "Creates a zero-loss cryogenic genetic seed bank. Requires Lv.4+ Hydro Farm & Lv.4+ Cryo Vault.",
                scrapCost = 250.0,
                waterCost = 350.0,
                rationsCost = 350.0,
                goldCost = 50.0,
                requiredBuildingSummary = "Hydro Farm Lv.4+ & Cryo Vault Lv.4+ (Currently Farm Lv.$farmLv, Cryo Lv.$cryoLv)",
                canAfford = state.scrap >= 250.0 && state.water >= 350.0 && state.rations >= 350.0 && state.gold >= 50.0,
                requirementsMet = farmLv >= 4 && cryoLv >= 4,
                isBuilt = state.edenCorePhase >= 2
            ),
            EdenCoreModuleCost(
                phaseNumber = 3,
                title = "Phase III: Aether Resonance Dynamo",
                description = "Self-regenerating resonance converter. Requires Lv.3+ Thorium Reactor & rare Syndicate cells.",
                scrapCost = 300.0,
                goldCost = 60.0,
                thoriumCost = 3.0,
                aetherCost = 2.0,
                requiredBuildingSummary = "Thorium Reactor Lv.3+ (Currently Lv.$thoriumLv)",
                canAfford = state.scrap >= 300.0 && state.gold >= 60.0 && state.thoriumRods >= 3.0 && state.aetherCells >= 2.0,
                requirementsMet = thoriumLv >= 3,
                isBuilt = state.edenCorePhase >= 3
            ),
            EdenCoreModuleCost(
                phaseNumber = 4,
                title = "Phase IV: Perpetual Overseer AI",
                description = "Quantum autonomous command matrix managing life-support. Requires Lv.4+ Signal Array & Mutagen Cores.",
                scrapCost = 350.0,
                researchCost = 500.0,
                goldCost = 75.0,
                coresCost = 3.0,
                requiredBuildingSummary = "Signal Array Lv.4+ (Currently Lv.$arrayLv)",
                canAfford = state.scrap >= 350.0 && state.researchData >= 500.0 && state.gold >= 75.0 && state.mutagenCores >= 3.0,
                requirementsMet = arrayLv >= 4,
                isBuilt = state.edenCorePhase >= 4
            )
        )
    }

    /**
     * Evaluates criteria for the 5 Self-Sufficiency Certification Tiers
     */
    fun computeCertificationPreviews(
        state: OutpostStateEntity,
        production: ProductionSnapshot
    ): List<CertificationTierPreview> {
        val tier1Met = !production.isVaultBlackout && production.totalMegawattGenerated >= production.totalMegawattDemand * 1.20
        val tier2Met = tier1Met && production.waterPerSec > 0.05 && state.activePipeLeaksCount == 0
        val tier3Met = tier2Met && production.rationsPerSec > 0.05 && production.isCryoPreservationActive
        val tier4Met = tier3Met && state.totalRaidsRepelled >= 5 && state.defenseIntegrityPercent >= 80
        val tier5Met = tier4Met && state.edenCorePhase >= 4 && state.consecutiveDaysStable >= 3 && production.isMakingPositiveResources

        return listOf(
            CertificationTierPreview(
                tierNumber = 1,
                title = "Tier I: Energy Autonomy",
                requirementSummary = "Generate >= +20% surplus Megawatts with 0 blackouts (${production.totalMegawattGenerated.roundToInt()}/${production.totalMegawattDemand.roundToInt()} MW)",
                isMet = tier1Met,
                isCertified = state.certificationTier >= 1
            ),
            CertificationTierPreview(
                tierNumber = 2,
                title = "Tier II: Closed Hydrology",
                requirementSummary = "Net positive Water balance (+0.05/s) and 0 active coolant pipe leaks",
                isMet = tier2Met,
                isCertified = state.certificationTier >= 2
            ),
            CertificationTierPreview(
                tierNumber = 3,
                title = "Tier III: Biosphere Equilibrium",
                requirementSummary = "Net positive Food balance (+0.05/s) and 100% Cryo-Preservation (Cryo Vault Lv.5)",
                isMet = tier3Met,
                isCertified = state.certificationTier >= 3
            ),
            CertificationTierPreview(
                tierNumber = 4,
                title = "Tier IV: Fortress Perimeter",
                requirementSummary = "Repel at least 5 attacks and maintain Blast Door integrity >= 80%",
                isMet = tier4Met,
                isCertified = state.certificationTier >= 4
            ),
            CertificationTierPreview(
                tierNumber = 5,
                title = "Tier V: Eden Core Master Autonomy",
                requirementSummary = "Complete all 4 Eden Core modules and maintain 3 consecutive days of stability",
                isMet = tier5Met,
                isCertified = state.certificationTier >= 5
            )
        )
    }

    /**
     * Maps a Vault room's upgrade level to its Fallout Shelter 1x / 2x / 3x Room Merge tier.
     */
    fun computeRoomMergeTier(level: Int): RoomMergeTier {
        return when {
            level <= 0 -> RoomMergeTier.UNBUILT_0X
            level == 1 -> RoomMergeTier.SINGLE_1X
            level == 2 -> RoomMergeTier.DOUBLE_2X
            else -> RoomMergeTier.TRIPLE_3X
        }
    }

    /**
     * Resolves the active Fallout Shelter room incident type when a room has an active incident/sabotage.
     */
    fun resolveActiveIncidentType(
        sabotagedBuildingId: String,
        dayCounter: Int = 1
    ): com.example.model.VaultIncidentType? {
        if (sabotagedBuildingId.isBlank()) return null
        val index = kotlin.math.abs(sabotagedBuildingId.hashCode() + dayCounter) % com.example.model.VaultIncidentType.entries.size
        return com.example.model.VaultIncidentType.entries[index]
    }

    /**
     * Computes Fallout Shelter "RUSH ROOM" instant payout & incident risk percentage.
     * Higher matching S.P.E.C.I.A.L. stats + Luck reduce incident risk and boost instant resource & Bottle Cap/Gold payout!
     */
    fun computeRushRoomPreview(
        buildingType: BuildingType,
        level: Int,
        state: OutpostStateEntity,
        workersMap: Map<String, WorkerClassEntity>,
        consecutiveRushes: Int = 0
    ): com.example.model.RushRoomPreview {
        val mergeTier = computeRoomMergeTier(level)
        val specialStat = buildingType.primarySpecialStat
        val favoredWorker = buildingType.favoredWorkerClass
        val workerEntity = favoredWorker?.let { workersMap[it.id] }
        val assignedCount = workerEntity?.assignedCount ?: (if (state.totalSurvivors > 0) 1 else 0)
        val workerLv = workerEntity?.classLevel ?: 1
        val hasUnique = state.uniqueDwellersCount > 0

        val profile = (favoredWorker ?: WorkerClassType.SCRAPPER).computeSpecialProfile(workerLv, hasUnique)
        val statValue = profile.statFor(specialStat)
        val luckValue = profile.luck
        val effectiveSpecialScore = (statValue + assignedCount.coerceAtMost(4)).coerceIn(1, 14)

        // Base Rush failure chance starts at 38%, lowered by matching SPECIAL & Luck, increased by consecutive rushes
        val rawRisk = 42 - (effectiveSpecialScore * 2) - luckValue + (consecutiveRushes * 14) +
            (if (state.dwellerStaminaPercent < 35) 10 else 0)
        val clampedRisk = rawRisk.coerceIn(12, 78)

        val mergeMult = 1.0 + (mergeTier.outputMergeBonusPercent / 100.0)
        val specialMult = 1.0 + (effectiveSpecialScore * 0.06)
        val baseAmount = (6.0 + level.coerceAtLeast(1) * 4.5) * mergeMult * specialMult
        val roundedResource = ((baseAmount * 10.0).roundToInt() / 10.0).coerceAtLeast(4.0)
        val capsGold = ((2.0 + level.coerceAtLeast(1) * 1.5 + luckValue * 0.4) * mergeMult * 10.0).roundToInt() / 10.0
        val xpReward = (18.0 + level.coerceAtLeast(1) * 4.0)

        val resLabel = when (buildingType) {
            BuildingType.SALVAGE_YARD -> "Scrap"
            BuildingType.ATMOSPHERIC_CONDENSER -> "Purified Water"
            BuildingType.HYDROPONIC_GREENHOUSE, BuildingType.CRYO_STASIS_VAULT -> "Food Rations"
            BuildingType.BIO_SLUDGE_REFINERY, BuildingType.GEOTHERMAL_TAP -> "Bio-Fuel"
            BuildingType.SIGNAL_RESEARCH_ARRAY, BuildingType.NOCTURNAL_EXTRACTION_RIG -> "Research Data"
            BuildingType.SURVIVOR_HAB_BLOCK, BuildingType.REINFORCED_SILO, BuildingType.VAULT_STORE_ROOM -> "Scrap & Supplies"
        }

        val isSabotaged = state.sabotagedBuildingId == buildingType.id
        val canRush = level > 0 && !isSabotaged
        val note = when {
            level <= 0 -> "Repair room to Lv.1 first to unlock Fallout Shelter Room Rushing!"
            isSabotaged -> "Incident active in this room! Extinguish/clear incident before rushing again."
            else -> "⚡ Rush (${clampedRisk}% Risk): Instant +${roundedResource.toInt()} $resLabel, +${capsGold.toInt()} Gold Caps & +${xpReward.toInt()} XP (${specialStat.code}:$effectiveSpecialScore)"
        }

        return com.example.model.RushRoomPreview(
            buildingType = buildingType,
            roomLevel = level,
            mergeTier = mergeTier,
            primarySpecial = specialStat,
            matchingSpecialScore = effectiveSpecialScore,
            incidentRiskPercent = clampedRisk,
            instantResourceAmount = roundedResource,
            instantResourceLabel = resLabel,
            bonusGoldCaps = capsGold,
            xpReward = xpReward,
            canRush = canRush,
            statusNote = note
        )
    }
}


