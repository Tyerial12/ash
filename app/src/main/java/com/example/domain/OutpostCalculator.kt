package com.example.domain

import com.example.data.local.OutpostStateEntity
import com.example.data.local.WorkerClassEntity
import com.example.model.BuildingType
import com.example.model.CommandLevelProgression
import com.example.model.CraftableItemType
import com.example.model.DayNightPhase
import com.example.model.FishingSpot
import com.example.model.HuntingTarget
import com.example.model.PowerStationType
import com.example.model.RareSyndicateItem
import com.example.model.ResourceType
import com.example.model.SalvageNodeType
import com.example.model.SkillNodeDefinition
import com.example.model.TradeTown
import com.example.model.VaultOperationalPosture
import com.example.model.WastelandExpeditionType
import com.example.model.WorkerClassType
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
    val isVaultResourceDeficit: Boolean = false,
    val isDefenseShutDownByDeficit: Boolean = false,
    val isEarlyBunkerGraceActive: Boolean = false,
    val operationalPosture: VaultOperationalPosture = VaultOperationalPosture.STANDARD,
    val deficitDaysRemainingBeforeGameOver: Int = 2,
    val blackoutDaysRemainingBeforeGameOver: Int = 6,
    val dwellerUpkeepEscalationTier: Int = 0,
    val dwellerUpkeepEscalationMultiplier: Double = 1.0,
    val fiveMinDwellerFoodCost: Double = 0.0,
    val fiveMinDwellerWaterCost: Double = 0.0
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
    val rareJackpotChancePercent: Int
)

object OutpostCalculator {

    private const val BASE_STORAGE_CAP = 500.0
    private const val BASE_MAX_CITIZENS = 3
    const val DWELLER_FEED_INTERVAL_SECONDS = 300 // 5 minutes real-time feeding cycle
    const val DWELLER_ESCALATION_INTERVAL_SECONDS = 120 // Every 2 minutes Dwellers consume more resources
    const val MAX_DEFICIT_DAYS_BEFORE_GAME_OVER = 2
    const val MAX_BLACKOUT_DAYS_BEFORE_GAME_OVER = 6

    /**
     * Higher Vault Command Level increases the Vault's overall resource appetite!
     */
    fun computeVaultLevelUpkeepMultiplier(commandLevel: Int): Double {
        if (commandLevel <= 1) return 1.0
        val extraLv = commandLevel - 1
        return 1.0 + extraLv * 0.22 + extraLv * extraLv * 0.04
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
        val scrapPerSec = ((extraLv * (0.08 + extraLv * 0.035)) * 100.0).roundToInt() / 100.0
        val waterPerSec = ((extraLv * (0.06 + extraLv * 0.025) * hydroDiscountMult) * 100.0).roundToInt() / 100.0
        val fuelPerSec = if (commandLevel >= 3) {
            (((extraLv - 1) * (0.04 + (extraLv - 1) * 0.02)) * 100.0).roundToInt() / 100.0
        } else 0.0

        val dailyScrap = ((extraLv * (2.2 + extraLv * 1.1)) * 10.0).roundToInt() / 10.0
        val dailyWater = ((extraLv * (1.8 + extraLv * 0.9) * hydroDiscountMult) * 10.0).roundToInt() / 10.0
        val dailyFuel = if (commandLevel >= 3) {
            (((extraLv - 1) * (1.4 + (extraLv - 1) * 0.7)) * 10.0).roundToInt() / 10.0
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
     * Higher room levels require significantly more Megawatts (MW) per level due to high-voltage
     * industrial machinery, UV crop lamps, and deep-core life support.
     * Lv.1 = 1.0x base, Lv.2 = 2.76x base (+1.76x step), Lv.3 = 5.28x base (+2.52x step), etc.
     */
    fun computeBuildingTotalMegawatts(buildingType: BuildingType, level: Int): Double {
        if (level <= 0) return 0.0
        val highLevelEscalation = 1.0 + (level - 1) * 0.38
        return (buildingType.megawattDemandPerLevel * level * highLevelEscalation * 10.0).roundToInt() / 100.0 * 10.0
    }

    /**
     * Non-linear / Exponential Vault Resource Upkeep at Higher Room Levels AND Higher Vault Levels:
     * The higher level your Vault and Vault rooms are, the more resources (Scrap maintenance, Cooling Water,
     * and Industrial Bio-Fuel) they consume per second and per day to stay operational.
     */
    fun computeBuildingResourceUpkeep(
        buildingType: BuildingType,
        level: Int,
        hydroDiscountMult: Double = 1.0,
        commandLevel: Int = 1
    ): RoomResourceUpkeep {
        if (level <= 0) return RoomResourceUpkeep()
        val extraLevels = (level - 1).coerceAtLeast(0)
        val vaultLevelScale = computeVaultLevelUpkeepMultiplier(commandLevel)
        // Super-linear escalation for Lv.2, Lv.3, Lv.4, Lv.5+ multiplied by Vault Level
        val highLevelScale = (level * (1.0 + extraLevels * 0.55) + extraLevels * extraLevels * 0.35) * vaultLevelScale

        val baseWater = buildingType.waterUpkeepPerLevel * highLevelScale * hydroDiscountMult
        val highLevelCoolantWater = if (level >= 2) {
            extraLevels * (0.06 + extraLevels * 0.04) * vaultLevelScale * hydroDiscountMult
        } else 0.0
        val waterPerSec = ((baseWater + highLevelCoolantWater) * 100.0).roundToInt() / 100.0

        val scrapPerSec = if (level >= 2 && buildingType != BuildingType.SALVAGE_YARD) {
            ((extraLevels * (0.07 + extraLevels * 0.05) * vaultLevelScale) * 100.0).roundToInt() / 100.0
        } else 0.0

        val fuelPerSec = if (level >= 2 && buildingType != BuildingType.BIO_SLUDGE_REFINERY) {
            ((extraLevels * (0.04 + extraLevels * 0.03) * vaultLevelScale) * 100.0).roundToInt() / 100.0
        } else 0.0

        val dailyScrap = (level * (1.2 + extraLevels * 2.0 + extraLevels * extraLevels * 0.8) * vaultLevelScale * 10.0).roundToInt() / 10.0
        val dailyWater = (level * (1.0 + extraLevels * 1.6 + extraLevels * extraLevels * 0.6) * vaultLevelScale * hydroDiscountMult * 10.0).roundToInt() / 10.0
        val dailyFuel = if (level >= 2) {
            ((extraLevels * (1.0 + extraLevels * 1.3) * vaultLevelScale) * 10.0).roundToInt() / 10.0
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
        val isDangerousNight = state != null && DayNightPhase.fromCycleProgress(state.cycleProgress) == DayNightPhase.RAD_NIGHT
        val nightHazardAdder = if (isDangerousNight) 6 else 0
        val minFloor = if (gasMaskReduction > 0 || hazmatReduction > 0) 2 else 3
        return (type.deathChancePercent + nightHazardAdder - radarLevel - gasMaskReduction - hazmatReduction).coerceAtLeast(minFloor)
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

        // 1. Calculate Vault Power Supply (MW) vs Escalating Room Demand (MW) & Generator Wear
        var rawMegawattDemand = 4.0 // Base Vault Airlock & Life-Support load
        var totalBuiltRoomsCount = 0
        for (bType in BuildingType.entries) {
            val bLevel = buildingsMap[bType.id] ?: 0
            if (bLevel > 0) {
                totalBuiltRoomsCount += 1
                rawMegawattDemand += computeBuildingTotalMegawatts(bType, bLevel)
            }
        }
        // Powered defense emplacements also draw MW
        rawMegawattDemand += state.sentryTurretCount * 3.0 + state.plasmaCannonCount * 6.0
        val megawattDemand = ((rawMegawattDemand * operationalPosture.megawattDemandMultiplier) * 10.0).roundToInt() / 10.0

        val solarPhaseFactor = if (solarFlareActive) 1.65 else activePhase.solarPowerMultiplier
        // Realistic Fuel Burn: Bio-Sludge Turbine generates full MW when Bio-Fuel is available (or 45% backup vapor mode if dry)
        val turbineFuelFactor = if (state.bioTurbineLevel <= 0 || state.bioFuel > 0.1) 1.0 else 0.45
        val bioTurbineMw = state.bioTurbineLevel * PowerStationType.BIO_SLUDGE_TURBINE.baseMegawattsPerLevel * turbineFuelFactor
        val solarTowerMw = state.solarTowerLevel * PowerStationType.SOLAR_HELIOSTAT_TOWER.baseMegawattsPerLevel * solarPhaseFactor
        val thoriumReactorMw = state.thoriumReactorLevel * PowerStationType.THORIUM_FISSION_REACTOR.baseMegawattsPerLevel *
            (1.0 + rankBiolumSensors * 0.20)
        val aetherSubstationMw = state.aetherSubstationLevel * PowerStationType.AETHER_RESONANCE_SUBSTATION.baseMegawattsPerLevel
        val legacyFusionMw = rankLegacyFusion * 10.0

        val refineryGeneratorMw = (buildingsMap[BuildingType.BIO_SLUDGE_REFINERY.id] ?: 0) * 8.0
        val geothermalLevel = buildingsMap[BuildingType.GEOTHERMAL_TAP.id] ?: 0
        val geothermalBaselineMw = geothermalLevel * 7.5 // Room #9 Deep-Core Geothermal Tap: +7.5 MW/Lv 24/7 clean power!
        val mutagenLabLevel = buildingsMap[BuildingType.NOCTURNAL_EXTRACTION_RIG.id] ?: 0
        val reactorEngineerMw = savantCount * 5.0
        val emergencyDynamoBoostMw = if (thoriumOverdriveActive) 16.0 else 0.0

        // Generator Wear reduces MW output by up to -32% until tuned up (mitigated by Room #9 Geothermal Tap -10%/Lv)
        val effectiveWearPercent = (state.generatorWearPercent - geothermalLevel * 8).coerceIn(0, 80)
        val wearPenaltyMult = (1.0 - effectiveWearPercent * 0.004).coerceIn(0.68, 1.0)

        val gridStationBonusMult = (1.0 + state.aetherSubstationLevel * 0.20) *
            (1.0 + mutagenLabLevel * 0.12) *
            (1.0 + rankSolarCollectors * 0.18) *
            (if (savantCount > 0 && savantLv >= 5) 1.15 else 1.0) *
            (if (thoriumOverdriveActive) 1.40 else 1.0) *
            wearPenaltyMult

        val totalMegawattGenerated =
            (bioTurbineMw + solarTowerMw + thoriumReactorMw + aetherSubstationMw + legacyFusionMw +
                refineryGeneratorMw + geothermalBaselineMw + reactorEngineerMw + emergencyDynamoBoostMw) *
                gridStationBonusMult

        // Strict Vault Power Rule: If Generated MW < Required MW, the Vault is in BLACKOUT (or BROWNOUT if partial MW > 0)!
        val isVaultBlackout = totalMegawattGenerated < megawattDemand
        val isVaultBrownout = totalMegawattGenerated > 0.0 && totalMegawattGenerated < megawattDemand
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

        // Realistic Oxygen & Radiation Life-Support Drift Rates
        val oxygenScrubberSupply = if (isVaultBlackout) {
            if (isVaultBrownout) 0.4 else 0.0
        } else {
            1.2 + habLevel * 0.9 + farmLevel * 1.1 + hydroCount * 0.5
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

        // 2. Daily Base Defense Rating, Bulkhead Silo Bonus, Ammo-Fed Turrets & Door Integrity Scaling
        val rankHighOctane = skillsMap["IND_HIGH_OCTANE_CRACKING"] ?: 0
        val unpoweredDefense = state.blastDoorLevel * 20 +
            state.craftedShrapnelMines * CraftableItemType.SHRAPNEL_MINE.defenseRatingBonus +
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
        val totalDefenseRating = ((unpoweredDefense + poweredBulkheadAndTurretDefense) * (1.0 + rankHighOctane * 0.20) * legacyDefMult * integrityFactor).roundToInt()

        val currentDay = state.dayCounter.coerceAtLeast(1)
        val isDay10Multiple = currentDay % 10 == 0
        val nextSiegeMilestoneDay = if (isDay10Multiple) currentDay else ((currentDay / 10) + 1) * 10
        val daysUntilDay10Siege = (nextSiegeMilestoneDay - currentDay).coerceAtLeast(0)
        val day10RequiredDefense = computeDay10SiegeRequiredDefense(nextSiegeMilestoneDay)
        val todayRequiredDefense = computeDailyRaidRequiredDefense(currentDay, activePhase)
        val tomorrowRequiredDefense = computeDailyRaidRequiredDefense(currentDay + 1, DayNightPhase.SCORCHING_DAY)

        val hydroStorageBonus = if (hydroCount > 0 && hydroLv >= 20) 1.15 else 1.0
        val storageCap = (
            BASE_STORAGE_CAP +
                siloLevel * BuildingType.REINFORCED_SILO.storageBonusPerLevel +
                cryoVaultLevel * BuildingType.CRYO_STASIS_VAULT.storageBonusPerLevel +
                state.commandLevel * 140.0
            ) * (1.0 + rankForgeOverdrive * 0.25) * hydroStorageBonus

        val maxWorkerCapacity = BASE_MAX_CITIZENS + habLevel * BuildingType.SURVIVOR_HAB_BLOCK.populationBonusPerLevel
        val maxPickaxeLevelAllowed = computeMaxPickaxeLevel(buildingsMap)

        // Daily Powered Room Supply Bonuses (granted each new day when Vault is powered!)
        val dailyRoomFoodBonus = if (isVaultBlackout) 0.0 else (farmLevel * 8.0 + cryoVaultLevel * 5.0)
        val dailyRoomWaterBonus = if (isVaultBlackout) 0.0 else condenserLevel * 6.0
        val dailyRoomScrapBonus = if (isVaultBlackout) 0.0 else smelteryLevel * 10.0

        // 3. Daily & 5-Minute Food/Water Upkeep per Worker + Forgiving Days 1-10 2-Minute Appetite Escalation
        val isEarlyDaysWindow = currentDay <= 10
        val maxEscalationTier = if (isEarlyDaysWindow) 10 else 30
        val stepPerTier = if (isEarlyDaysWindow) 0.08 else 0.14 // Forgiving +8%/2m in Days 1-10, +14%/2m after Day 10
        val dwellerUpkeepEscalationTier = (state.dwellerAppetiteEscalationSeconds.coerceAtLeast(0) / DWELLER_ESCALATION_INTERVAL_SECONDS).coerceAtMost(maxEscalationTier)
        val dwellerUpkeepEscalationMultiplier = 1.0 + dwellerUpkeepEscalationTier * stepPerTier
        val dailyFoodUpkeepPerDay = ((state.totalSurvivors * 2.0 + totalAssigned * 0.5) * combinedRationDiscount * dwellerUpkeepEscalationMultiplier * 10.0).roundToInt() / 10.0
        val dailyWaterUpkeepPerDay = ((state.totalSurvivors * 2.0 + totalAssigned * 0.5) * combinedRationDiscount * dwellerUpkeepEscalationMultiplier * 10.0).roundToInt() / 10.0
        val fiveMinDwellerFoodCost = ((state.totalSurvivors * 2.5 + totalAssigned * 0.75) * combinedRationDiscount * dwellerUpkeepEscalationMultiplier * 10.0).roundToInt() / 10.0
        val fiveMinDwellerWaterCost = ((state.totalSurvivors * 2.5 + totalAssigned * 0.75) * combinedRationDiscount * dwellerUpkeepEscalationMultiplier * 10.0).roundToInt() / 10.0
        val citizenFoodUpkeep = (state.totalSurvivors * 0.10 + totalAssigned * 0.03) * combinedRationDiscount * dwellerUpkeepEscalationMultiplier * operationalPosture.upkeepMultiplier
        val citizenWaterUpkeep = (state.totalSurvivors * 0.08 + totalAssigned * 0.03) * combinedRationDiscount * dwellerUpkeepEscalationMultiplier * operationalPosture.upkeepMultiplier

        var moraleMult = 1.0 + habLevel * 0.05
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
        }
        val citizenMoraleMultiplier = moraleMult.coerceIn(0.55, 1.45)
        val citizenMoralePercent = (citizenMoraleMultiplier * 100).roundToInt()
        val uniqueDwellerBonusMultiplier = 1.0 + state.uniqueDwellersCount * 0.40
        val prestigeBonusMultiplier = (1.0 + state.prestigeLevel * 0.25) * (1.0 + rankLegacyCryo * 0.20)
        val onlineRoomsCount = if (isVaultBlackout || state.totalSurvivors <= 0 || totalAssigned <= 0 || isWorkersStarvingOrDehydrated) {
            0
        } else {
            totalBuiltRoomsCount
        }

        // Calculate total Vault Level + Room Resource Upkeep across the Vault (with Days 1-10 Early Grace & Operational Posture)
        val earlyDaysUpkeepFactor = if (currentDay <= 10 && state.commandLevel <= 3) 0.55 else 1.0
        val isEarlyBunkerGraceActive = currentDay <= 3 && state.commandLevel <= 2 && totalBuiltRoomsCount < 2
        val postureAndGraceUpkeepMult = earlyDaysUpkeepFactor * operationalPosture.upkeepMultiplier
        val hydroUpkeepMult = (if (hydroCount > 0 && hydroLv >= 15) 0.70 else 1.0) * legacyRationDiscount * cryoUpkeepDiscount
        val vaultLevelUpkeep = computeVaultLevelResourceUpkeep(state.commandLevel, hydroUpkeepMult)
        var totalRoomScrapUpkeepPerSec = vaultLevelUpkeep.scrapPerSec * postureAndGraceUpkeepMult
        var totalRoomWaterUpkeepPerSec = vaultLevelUpkeep.waterPerSec * postureAndGraceUpkeepMult
        var totalRoomFuelUpkeepPerSec = vaultLevelUpkeep.fuelPerSec * postureAndGraceUpkeepMult
        var totalDailyRoomScrapUpkeep = vaultLevelUpkeep.dailyScrap * postureAndGraceUpkeepMult
        var totalDailyRoomWaterUpkeep = vaultLevelUpkeep.dailyWater * postureAndGraceUpkeepMult
        var totalDailyRoomFuelUpkeep = vaultLevelUpkeep.dailyFuel * postureAndGraceUpkeepMult

        for (bType in BuildingType.entries) {
            val bLevel = buildingsMap[bType.id] ?: 0
            if (bLevel <= 0) continue
            val roomUpkeep = computeBuildingResourceUpkeep(bType, bLevel, hydroUpkeepMult, state.commandLevel)
            totalRoomScrapUpkeepPerSec += roomUpkeep.scrapPerSec * postureAndGraceUpkeepMult
            totalRoomWaterUpkeepPerSec += roomUpkeep.waterPerSec * postureAndGraceUpkeepMult
            totalRoomFuelUpkeepPerSec += roomUpkeep.fuelPerSec * postureAndGraceUpkeepMult
            totalDailyRoomScrapUpkeep += roomUpkeep.dailyScrap * postureAndGraceUpkeepMult
            totalDailyRoomWaterUpkeep += roomUpkeep.dailyWater * postureAndGraceUpkeepMult
            totalDailyRoomFuelUpkeep += roomUpkeep.dailyFuel * postureAndGraceUpkeepMult
        }

        val bioTurbineFuelUpkeep = state.bioTurbineLevel * PowerStationType.BIO_SLUDGE_TURBINE.fuelUpkeepPerLevel * operationalPosture.upkeepMultiplier
        val totalVaultResourceDrainPerSec =
            ((totalRoomScrapUpkeepPerSec + totalRoomWaterUpkeepPerSec + totalRoomFuelUpkeepPerSec +
                citizenFoodUpkeep + citizenWaterUpkeep + bioTurbineFuelUpkeep) * 100.0).roundToInt() / 100.0
        val deficitDaysRemaining = (MAX_DEFICIT_DAYS_BEFORE_GAME_OVER - state.deficitDaysCount).coerceIn(0, MAX_DEFICIT_DAYS_BEFORE_GAME_OVER)
        val blackoutDaysRemaining = (MAX_BLACKOUT_DAYS_BEFORE_GAME_OVER - state.blackoutDaysCount).coerceIn(0, MAX_BLACKOUT_DAYS_BEFORE_GAME_OVER)

        // STRICT ZERO-DWELLER, WORKER STARVATION/DEHYDRATION, OR VAULT BLACKOUT RULE:
        if (state.totalSurvivors <= 0 || totalAssigned <= 0 || isVaultBlackout || isWorkersStarvingOrDehydrated) {
            val haltedDeficit = totalVaultResourceDrainPerSec > 0.05 && !isEarlyBunkerGraceActive
            val haltedDefenseRating = if (haltedDeficit) 0 else totalDefenseRating
            return ProductionSnapshot(
                scrapPerSec = 0.0,
                waterPerSec = if (state.totalSurvivors > 0 && state.water > 0.0) -citizenWaterUpkeep else 0.0,
                rationsPerSec = if (state.totalSurvivors > 0 && state.rations > 0.0) -citizenFoodUpkeep else 0.0,
                bioFuelPerSec = 0.0,
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
                isVaultResourceDeficit = haltedDeficit,
                isDefenseShutDownByDeficit = haltedDeficit,
                isEarlyBunkerGraceActive = isEarlyBunkerGraceActive,
                operationalPosture = operationalPosture,
                deficitDaysRemainingBeforeGameOver = deficitDaysRemaining,
                blackoutDaysRemainingBeforeGameOver = blackoutDaysRemaining,
                dwellerUpkeepEscalationTier = dwellerUpkeepEscalationTier,
                dwellerUpkeepEscalationMultiplier = dwellerUpkeepEscalationMultiplier,
                fiveMinDwellerFoodCost = fiveMinDwellerFoodCost,
                fiveMinDwellerWaterCost = fiveMinDwellerWaterCost
            )
        }

        var globalMult = commandBonusMult *
            uniqueDwellerBonusMultiplier *
            prestigeBonusMultiplier *
            citizenMoraleMultiplier *
            powerGridEfficiencyMultiplier *
            fieldMomentumWorkerBonus *
            operationalPosture.outputMultiplier *
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
        var buildingWaterUpkeep = 0.0

        for (bType in BuildingType.entries) {
            val level = buildingsMap[bType.id] ?: 0
            if (level <= 0) continue

            val favoredWorker = bType.favoredWorkerClass
            val assignedFavored = if (favoredWorker != null) {
                workersMap[favoredWorker.id]?.assignedCount ?: 0
            } else 0

            val staffingFactor = when {
                favoredWorker == null -> 0.45
                assignedFavored <= 0 -> 0.35 // Powered rooms still run automated machinery at 35% when Dwellers are in the Vault!
                else -> 0.75 + assignedFavored * 0.65
            }

            // Higher room levels also output more per level (+18% compounding efficiency per room level)
            val highLevelRoomOutputBoost = 1.0 + (level - 1) * 0.18
            val rawOut = level * bType.baseOutputPerLevel * highLevelRoomOutputBoost * staffingFactor
            when (bType.primaryResource) {
                ResourceType.SCRAP -> scrapBase += rawOut
                ResourceType.WATER -> waterBase += rawOut
                ResourceType.RATIONS -> rationsBase += rawOut
                ResourceType.BIO_FUEL -> bioFuelBase += rawOut
                ResourceType.RESEARCH_DATA -> researchBase += rawOut
                ResourceType.MUTAGEN_CORES -> mutagenBase += rawOut
                else -> {}
            }

            if (bType.waterUpkeepPerLevel > 0.0 && assignedFavored > 0) {
                val upkeepDiscount = if (bType == BuildingType.HYDROPONIC_GREENHOUSE && hydroCount > 0 && hydroLv >= 15) 0.5 else 1.0
                buildingWaterUpkeep += level * bType.waterUpkeepPerLevel * upkeepDiscount
            }
        }

        val rankGuildDrills = skillsMap["WRK_GUILD_DRILLS"] ?: 0
        val rankCircadian = skillsMap["WRK_CIRCADIAN_AUTO_SHIFT"] ?: 0
        val autoShiftBonus = if (state.autoShiftEnabled) 1.15 + rankCircadian * 0.10 else 1.0

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

            var workerOut = count * wType.baseOutputPerSecond * classLevelMult * phaseFactor *
                (1.0 + rankGuildDrills * 0.15) * autoShiftBonus

            when (wType) {
                WorkerClassType.SCRAPPER -> {
                    if (cLevel >= 5) workerOut *= 1.25
                    if (cLevel >= 20) workerOut *= 1.45
                    scrapBase += workerOut
                    if (cLevel >= 10) bioFuelBase += count * 0.40 * classLevelMult
                }
                WorkerClassType.HYDRO_ENGINEER -> {
                    if (cLevel >= 5) workerOut *= 1.25
                    waterBase += workerOut
                    rationsBase += workerOut * 0.55
                    if (cLevel >= 10) bioFuelBase += count * 0.65 * classLevelMult
                }
                WorkerClassType.NIGHT_STALKER -> {
                    if (cLevel >= 5) workerOut *= 1.30
                    if (cLevel >= 20 && (activePhase == DayNightPhase.RAD_NIGHT || eclipseShroudActive)) {
                        workerOut *= 2.0
                    }
                    mutagenBase += workerOut
                    if (cLevel >= 15 && (!isDaylight || eclipseShroudActive)) {
                        scrapBase += count * 1.8 * classLevelMult
                        bioFuelBase += count * 0.8 * classLevelMult
                    }
                }
                WorkerClassType.TECH_SAVANT -> {
                    if (cLevel >= 5) workerOut *= 1.25
                    if (cLevel >= 20) workerOut *= 1.35
                    researchBase += workerOut
                }
            }
        }

        val rankRotarySaws = skillsMap["IND_ROTARY_SAWS"] ?: 0
        val rankHydroFiltration = skillsMap["IND_HYDRO_FILTRATION"] ?: 0
        val rankSovereign = skillsMap["WRK_SOVEREIGN_COMMAND"] ?: 0

        val passiveFoodFromTraps = (
            state.craftedSnareTraps * CraftableItemType.SNARE_TRAP.passiveFoodPerSec +
            state.aetherCrossbowLevel * CraftableItemType.AETHER_CROSSBOW.passiveFoodPerSec
        ) * (1.0 + rankHydroFiltration * 0.22) * (1.0 + farmLevel * 0.25)

        val grossScrapPerSec = scrapBase * (1.0 + rankRotarySaws * 0.18) * effectiveScrapPhase * daylightSkillMult * nightSkillMult * globalMult
        val scrapPerSec = (grossScrapPerSec - totalRoomScrapUpkeepPerSec).coerceAtLeast(-2.5)
        val grossWaterPerSec = waterBase * (1.0 + rankHydroFiltration * 0.22) * effectiveWaterPhase * daylightSkillMult * nightSkillMult * globalMult
        val waterPerSec = (grossWaterPerSec - totalRoomWaterUpkeepPerSec - citizenWaterUpkeep).coerceAtLeast(-2.5)

        val grossRationsPerSec = rationsBase * (1.0 + rankHydroFiltration * 0.22) * effectiveRationsPhase * daylightSkillMult * nightSkillMult * globalMult + passiveFoodFromTraps
        val rationsPerSec = (grossRationsPerSec - citizenFoodUpkeep).coerceAtLeast(-2.5)

        val grossFuelPerSec = bioFuelBase * (1.0 + rankHighOctane * 0.25) * daylightSkillMult * nightSkillMult * globalMult
        val bioFuelPerSec = if (bioFuelBase > 0.0 || totalRoomFuelUpkeepPerSec > 0.0 || bioTurbineFuelUpkeep > 0.0) {
            (grossFuelPerSec - bioTurbineFuelUpkeep - totalRoomFuelUpkeepPerSec)
                .coerceAtLeast(-2.0)
        } else 0.0

        val totalGrossResourceGainPerSec =
            ((grossScrapPerSec + grossWaterPerSec + grossRationsPerSec + grossFuelPerSec) * 100.0).roundToInt() / 100.0
        val isVaultResourceDeficit = !isEarlyBunkerGraceActive &&
            totalVaultResourceDrainPerSec > 0.05 &&
            totalVaultResourceDrainPerSec > totalGrossResourceGainPerSec
        val isDefenseShutDownByDeficit = isVaultResourceDeficit
        val effectiveTotalDefenseRating = if (isDefenseShutDownByDeficit) 0 else totalDefenseRating
        val effectiveAutoDefenseActive = if (isDefenseShutDownByDeficit) false else isAutoDefenseActive

        val researchPerSec = researchBase * (1.0 + rankSovereign * 0.25) * effectiveResearchPhase * daylightSkillMult * nightSkillMult * globalMult
        val mutagenCoresPerSec = mutagenBase * (1.0 + rankBiolumSensors * 0.20) * effectiveMutagenPhase * nightSkillMult * globalMult

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
            isVaultResourceDeficit = isVaultResourceDeficit,
            isDefenseShutDownByDeficit = isDefenseShutDownByDeficit,
            isEarlyBunkerGraceActive = isEarlyBunkerGraceActive,
            operationalPosture = operationalPosture,
            deficitDaysRemainingBeforeGameOver = deficitDaysRemaining,
            blackoutDaysRemainingBeforeGameOver = blackoutDaysRemaining,
            dwellerUpkeepEscalationTier = dwellerUpkeepEscalationTier,
            dwellerUpkeepEscalationMultiplier = dwellerUpkeepEscalationMultiplier,
            fiveMinDwellerFoodCost = fiveMinDwellerFoodCost,
            fiveMinDwellerWaterCost = fiveMinDwellerWaterCost
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
        val missing = (100 - currentIntegrity).coerceAtLeast(0)
        val needsRepair = missing > 0 || state.needsDefenseRepair
        val rankPrefab = skillsMap["IND_MODULAR_PREFAB"] ?: 0
        val refineryLv = buildingsMap[BuildingType.BIO_SLUDGE_REFINERY.id] ?: 0
        val discount = ((1.0 - rankPrefab * 0.08) * (1.0 - refineryLv * 0.10)).coerceAtLeast(0.45)
        val scale = 1.0 + (state.blastDoorLevel - 1) * 0.25 + state.sentryTurretCount * 0.15
        val scrapCost = if (needsRepair) {
            ((10.0 + missing * 0.45) * scale * discount).roundToInt().toDouble().coerceAtLeast(6.0)
        } else 0.0
        val goldCost = if (needsRepair) {
            ((5.0 + missing * 0.20) * scale * discount).roundToInt().toDouble().coerceAtLeast(3.0)
        } else 0.0
        val canAfford = needsRepair && state.scrap >= scrapCost && state.gold >= goldCost

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
        val safeTargetLevel = targetDoorLevel.coerceAtLeast(2)
        val upgradeStep = safeTargetLevel - 1 // Lv.1->2 is step 1, Lv.2->3 is step 2, etc.
        val baseDoorSeconds = 12
        val levelEscalation = upgradeStep.toDouble().pow(1.65) + (upgradeStep - 1) * 1.15

        val scrapperCount = workersMap[WorkerClassType.SCRAPPER.id]?.assignedCount ?: 0
        val rankPrefab = skillsMap["IND_MODULAR_PREFAB"] ?: 0
        val rankNano = skillsMap["LEG_NANO_SCHEMATICS"] ?: 0
        val legacySpeedMult = (1.0 - rankNano * 0.12).coerceAtLeast(0.40)
        val speedDivisor = 1.0 + scrapperCount * 0.10 + rankPrefab * 0.10

        val rawDoorSeconds = (baseDoorSeconds * levelEscalation * legacySpeedMult) / speedDivisor
        val minSecondsForLevel = ((8 + upgradeStep * upgradeStep * 4) * legacySpeedMult).roundToInt().coerceAtLeast(5)
        return rawDoorSeconds.roundToInt().coerceAtLeast(minSecondsForLevel)
    }

    fun computeBlastDoorUpgradeCost(
        state: OutpostStateEntity,
        skillsMap: Map<String, Int>,
        workersMap: Map<String, WorkerClassEntity> = emptyMap()
    ): BlastDoorUpgradeCost {
        val rankPrefab = skillsMap["IND_MODULAR_PREFAB"] ?: 0
        val discount = (1.0 - rankPrefab * 0.08).coerceAtLeast(0.55)
        val growth = 1.42.pow((state.blastDoorLevel - 1).toDouble())
        val scrapCost = (24.0 * growth * discount).roundToInt().toDouble()
        val goldCost = (12.0 * growth * discount).roundToInt().toDouble()
        val targetLevel = state.blastDoorLevel + 1
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
        val rushGoldCost = (5.0 + upgradeTimeSeconds * 0.75).roundToInt().toDouble()
        val canAfford = state.scrap >= scrapCost && state.gold >= goldCost

        return BlastDoorUpgradeCost(
            currentLevel = state.blastDoorLevel,
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
        val hasSpace = state.totalSurvivors < maxWorkerCapacity
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

        val food = (target.baseFoodReward * totalHuntMult).roundToInt().toDouble()
        val water = (target.baseWaterReward * totalHuntMult).roundToInt().toDouble()
        val gold = (target.baseGoldReward * uniqueMult * (1.0 + state.commandLevel * 0.08)).roundToInt().toDouble()
        val cores = target.baseCoresReward
        val xp = (target.baseXpReward * (1.0 + state.commandLevel * 0.10)).roundToInt().toDouble()

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
        val growth = 1.48.pow(currentLevel.toDouble())

        val scrapCost = (station.baseScrapCost * growth * discount).roundToInt().toDouble()
        val goldCost = (station.baseGoldCost * growth * discount).roundToInt().toDouble()
        val thoriumCost = if (station.baseThoriumCost > 0) (station.baseThoriumCost + currentLevel).toDouble() else 0.0
        val aetherCost = if (station.baseAetherCost > 0) (station.baseAetherCost + currentLevel).toDouble() else 0.0
        val coresCost = if (station.baseCoresCost > 0) (station.baseCoresCost + currentLevel).toDouble() else 0.0

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
            ResourceType.SCRAP -> (1.0 + yardLv * 0.25) * scrapperPerkMult * activePhase.scrapMultiplier
            ResourceType.WATER -> (1.0 + condenserLv * 0.25) * activePhase.waterMultiplier
            ResourceType.RATIONS -> (1.0 + hydroLv * 0.25) * activePhase.rationsMultiplier
            ResourceType.BIO_FUEL -> (1.0 + refineryLv * 0.30)
            ResourceType.RESEARCH_DATA -> (1.0 + arrayLv * 0.30) * activePhase.researchMultiplier
            ResourceType.MUTAGEN_CORES -> (1.0 + mutagenRigLv * 0.40) * activePhase.mutagenSpawnMultiplier
            else -> 1.0
        }

        val primaryAmount = (nodeType.basePrimaryYield * rigMult * skillMult * bonusForResource(nodeType.primaryResource))
            .roundToInt().toDouble().coerceAtLeast(1.0)
        val secondaryAmount = (nodeType.baseSecondaryYield * rigMult * skillMult * bonusForResource(nodeType.secondaryResource))
            .roundToInt().toDouble().coerceAtLeast(1.0)
        val goldAmount = (nodeType.baseGoldYield * rigMult * goldSkillMult * (1.0 + (state.commandLevel - 1) * 0.12))
            .roundToInt().toDouble().coerceAtLeast(6.0)

        val tapPrimaryAmount = (primaryAmount * 0.28).roundToInt().toDouble().coerceAtLeast(2.0)
        val tapGoldAmount = (goldAmount * 0.25).roundToInt().toDouble().coerceAtLeast(3.0)

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
            rareJackpotChancePercent = rareJackpotChancePercent
        )
    }

    fun computeSalvageRigUpgradeCost(rigLevel: Int): Pair<Double, Double> {
        val scrapCost = (28.0 * 1.45.pow((rigLevel - 1).toDouble())).roundToInt().toDouble()
        val fuelCost = if (rigLevel >= 2) (10.0 * 1.38.pow((rigLevel - 2).toDouble())).roundToInt().toDouble() else 0.0
        return scrapCost to fuelCost
    }

    fun computeBuildingUpgradeCost(
        buildingType: BuildingType,
        currentLevel: Int,
        state: OutpostStateEntity,
        skillsMap: Map<String, Int>,
        workersMap: Map<String, WorkerClassEntity> = emptyMap(),
        currentTotalMwGenerated: Double = 100.0,
        currentTotalMwDemand: Double = 4.0
    ): BuildingUpgradeCost {
        val rankPrefab = skillsMap["IND_MODULAR_PREFAB"] ?: 0
        val rankNano = skillsMap["LEG_NANO_SCHEMATICS"] ?: 0
        val discount = ((1.0 - rankPrefab * 0.08) * (1.0 - rankNano * 0.06)).coerceAtLeast(0.50)

        // Higher-level rooms require significantly more resources to upgrade (exponential + quadratic escalation)
        val highLevelEscalation = 1.0 + currentLevel * 0.25 + (currentLevel * currentLevel) * 0.14
        val factor = buildingType.costGrowthFactor.pow(currentLevel.toDouble()) * highLevelEscalation * discount

        val scrapCost = (buildingType.baseScrapCost * factor).roundToInt().toDouble()
        val baseWater = if (buildingType.baseWaterCost > 0.0) {
            buildingType.baseWaterCost
        } else if (currentLevel >= 1) {
            // Upgrading any room to Lv.2+ requires Cooling Water
            8.0 + buildingType.ordinal * 2.0
        } else 0.0
        val waterCost = (baseWater * factor).roundToInt().toDouble()

        val baseFuel = if (buildingType.baseFuelCost > 0.0) {
            buildingType.baseFuelCost
        } else if (currentLevel >= 2) {
            // Upgrading any room to Lv.3+ requires Industrial Bio-Fuel
            6.0 + buildingType.ordinal * 1.5
        } else 0.0
        val fuelCost = (baseFuel * factor).roundToInt().toDouble()

        val coresCost = (buildingType.baseCoresCost * factor).roundToInt().toDouble()

        val currentMw = computeBuildingTotalMegawatts(buildingType, currentLevel)
        val nextMw = computeBuildingTotalMegawatts(buildingType, currentLevel + 1)
        val deltaMw = ((nextMw - currentMw) * 10.0).roundToInt() / 10.0
        val willExceedPowerGrid = (currentTotalMwDemand + deltaMw) > currentTotalMwGenerated

        val currentRoomUpkeep = computeBuildingResourceUpkeep(buildingType, currentLevel)
        val nextLevelRoomUpkeep = computeBuildingResourceUpkeep(buildingType, currentLevel + 1)

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
     * Daily attacks on the Vault get progressively harder as days go by:
     * Each passing day adds both a base linear increase AND an accelerating exponential/tier step,
     * plus Nocturnal Mutant Swarm multipliers at Dusk (+15%) and Bioluminescent Night (+35%).
     */
    fun computeDailyRaidRequiredDefense(
        dayNumber: Int,
        activePhase: DayNightPhase = DayNightPhase.SCORCHING_DAY
    ): Int {
        val safeDay = dayNumber.coerceAtLeast(1)
        val isDay10Multiple = safeDay % 10 == 0
        val baseRequiredDefense = if (isDay10Multiple) {
            computeDay10SiegeRequiredDefense(safeDay)
        } else {
            val elapsedDays = safeDay - 1
            val linearGrowth = elapsedDays * 9
            val acceleratingCurve = (elapsedDays.toDouble().pow(1.52) * 3.8).roundToInt()
            val warbandTierStep = (elapsedDays / 3) * 12
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
     * Evaluates the dual hardcore survival Game Over conditions at the end of each Vault day:
     * 1) If the Vault takes more resources to run than it is gaining (isVaultResourceDeficit),
     *    Vault Defenses shut down immediately and if resource generation doesn't come back after 2 Days -> GAME OVER!
     *    (Protected by Early Bunker Grace in Days 1-3 while Command Lv <= 2 and < 2 rooms are built).
     * 2) If the Vault runs out of power (isVaultBlackout), after 6 Days without power -> GAME OVER!
     */
    fun evaluateSurvivalEndOfDay(
        state: OutpostStateEntity,
        snapshot: ProductionSnapshot
    ): SurvivalDayEvaluation {
        val nextDeficitDays = if (snapshot.isVaultResourceDeficit && !snapshot.isEarlyBunkerGraceActive) {
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
                "VAULT COLLAPSED (2-DAY RESOURCE DEFICIT): Your Vault required ${snapshot.totalVaultResourceDrainPerSec}/s resources to run while only generating +${snapshot.totalGrossResourceGainPerSec}/s for 2 consecutive days with Vault Defenses shut down."
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
}

