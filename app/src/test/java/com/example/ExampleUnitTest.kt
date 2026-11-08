package com.example

import com.example.data.local.OutpostStateEntity
import com.example.data.local.WorkerClassEntity
import com.example.domain.OutpostCalculator
import com.example.model.BuildingType
import com.example.model.CaravanCandidateGenerator
import com.example.model.CommandLevelProgression
import com.example.model.CraftableItemType
import com.example.model.DayNightPhase
import com.example.model.FishingSpot
import com.example.model.SalvageNodeType
import com.example.model.SkillBranch
import com.example.model.SkillTreeCatalog
import com.example.model.VaultMapStation
import com.example.model.WastelandExpeditionType
import com.example.model.WorkerClassType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun startWithNothingButAPickaxe_andZeroWorkersProducesZeroAutoResources() {
        val starterState = OutpostStateEntity()
        assertEquals(0.0, starterState.scrap, 0.001)
        assertEquals(0.0, starterState.gold, 0.001)
        assertEquals(0, starterState.totalSurvivors)
        assertEquals(1, starterState.salvageRigLevel)
        // You start alone in an empty, broken Vault that must be repaired on your own
        assertEquals(60, starterState.defenseIntegrityPercent)
        assertTrue(starterState.needsDefenseRepair)
        assertEquals(35, starterState.generatorWearPercent)
        assertEquals(78, starterState.oxygenPercent)
        assertEquals(15, starterState.radiationMillisieverts)
        assertEquals(20, starterState.ammoRounds)
        assertEquals(1, starterState.medkitsCount)
        assertEquals(1, starterState.radAwayCount)

        val buildings = mapOf(
            BuildingType.SALVAGE_YARD.id to 2,
            BuildingType.ATMOSPHERIC_CONDENSER.id to 1,
            BuildingType.HYDROPONIC_GREENHOUSE.id to 1
        )
        val emptyWorkers = emptyMap<String, WorkerClassEntity>()

        val soloProd = OutpostCalculator.computeProductionSnapshot(
            state = starterState,
            buildingsMap = buildings,
            workersMap = emptyWorkers,
            skillsMap = emptyMap(),
            nowMillis = 1000L
        )
        assertEquals(0.0, soloProd.scrapPerSec, 0.0001)
        assertEquals(0.0, soloProd.waterPerSec, 0.0001)
        assertEquals(0.0, soloProd.rationsPerSec, 0.0001)

        val baseSalvage = OutpostCalculator.computeSalvageNodeYield(
            nodeType = SalvageNodeType.ARMORED_CONVOY_WRECK,
            state = starterState,
            buildingsMap = emptyMap(),
            workersMap = emptyWorkers,
            skillsMap = emptyMap(),
            activePhase = DayNightPhase.SCORCHING_DAY
        )
        assertTrue("Pickaxe mining yields Gold directly", baseSalvage.goldAmount >= 14.0)
        assertTrue("Each Pickaxe swing yields Gold", baseSalvage.tapGoldAmount >= 3.0)
    }

    @Test
    fun realisticPowerWearAndTacticalGear_affectMegawattsAndExpeditionSurvival() {
        val tunedGenState = OutpostStateEntity(
            bioTurbineLevel = 2,
            solarTowerLevel = 2,
            bioFuel = 100.0,
            generatorWearPercent = 0
        )
        val wornGenState = tunedGenState.copy(generatorWearPercent = 60)

        val tunedSnap = OutpostCalculator.computeProductionSnapshot(
            state = tunedGenState,
            buildingsMap = emptyMap(),
            workersMap = emptyMap(),
            skillsMap = emptyMap(),
            nowMillis = 1000L
        )
        val wornSnap = OutpostCalculator.computeProductionSnapshot(
            state = wornGenState,
            buildingsMap = emptyMap(),
            workersMap = emptyMap(),
            skillsMap = emptyMap(),
            nowMillis = 1000L
        )
        assertTrue("Worn generators produce fewer MW than tuned generators", wornSnap.totalMegawattGenerated < tunedSnap.totalMegawattGenerated)

        val equippedState = OutpostStateEntity(
            gasMasksCount = 2,
            hazmatSuitsCount = 2
        )
        val gearedRisk = OutpostCalculator.computeEffectiveExpeditionDeathChance(
            type = WastelandExpeditionType.entries.first(),
            buildingsMap = mapOf(BuildingType.SIGNAL_RESEARCH_ARRAY.id to 2),
            state = equippedState
        )
        assertEquals("Radar Lv.2 (-2%), Gas Mask (-3%), and Hazmat Suit (-4%) reduce 10% risk down to 2% floor", 2, gearedRisk)

        val richState = OutpostStateEntity(scrap = 500.0, water = 500.0, bioFuel = 500.0, gold = 500.0, mutagenCores = 50.0)
        val ammoCost = OutpostCalculator.computeCraftItemCost(CraftableItemType.BALLISTIC_AMMO_CRATE, richState, emptyMap())
        val maskCost = OutpostCalculator.computeCraftItemCost(CraftableItemType.GAS_MASK, richState, emptyMap())
        val hazmatCost = OutpostCalculator.computeCraftItemCost(CraftableItemType.HAZMAT_SUIT, richState, emptyMap())
        val medkitCost = OutpostCalculator.computeCraftItemCost(CraftableItemType.MEDKIT_RADAWAY_PACK, richState, emptyMap())
        assertTrue(ammoCost.canAfford && maskCost.canAfford && hazmatCost.canAfford && medkitCost.canAfford)
    }

    @Test
    fun vaultRequiresPowerAndFoodWater_blackoutOrStarvationHaltsAutomatedRoomsAndTurrets() {
        val stateWithoutPower = OutpostStateEntity(
            commandLevel = 2,
            totalSurvivors = 2,
            rations = 20.0,
            water = 20.0,
            bioTurbineLevel = 0,
            solarTowerLevel = 0,
            sentryTurretCount = 2,
            blastDoorLevel = 1,
            defenseIntegrityPercent = 100,
            autoDefenseEnabled = true
        )
        val buildings = mapOf(
            BuildingType.SALVAGE_YARD.id to 2,
            BuildingType.SURVIVOR_HAB_BLOCK.id to 1
        )
        val workers = mapOf(
            WorkerClassType.SCRAPPER.id to WorkerClassEntity(WorkerClassType.SCRAPPER.id, assignedCount = 2, classLevel = 1)
        )

        val blackoutSnap = OutpostCalculator.computeProductionSnapshot(
            state = stateWithoutPower,
            buildingsMap = buildings,
            workersMap = workers,
            skillsMap = emptyMap(),
            nowMillis = 1000L
        )
        assertTrue("Vault should be in blackout when MW demand exceeds 0 MW generated", blackoutSnap.isVaultBlackout)
        assertFalse("Automated defenses must be offline during blackout", blackoutSnap.isAutoDefenseActive)
        assertEquals("During Vault Blackout, automated room Scrap/s must be 0.0", 0.0, blackoutSnap.scrapPerSec, 0.0001)

        val poweredState = stateWithoutPower.copy(bioTurbineLevel = 2, solarTowerLevel = 1, bioFuel = 50.0, generatorWearPercent = 0)
        val poweredSnap = OutpostCalculator.computeProductionSnapshot(
            state = poweredState,
            buildingsMap = buildings,
            workersMap = workers,
            skillsMap = emptyMap(),
            nowMillis = 1000L
        )
        assertFalse("Vault should be online when Power Generators supply enough MW", poweredSnap.isVaultBlackout)
        assertTrue("Powered Vault rooms with fed & hydrated assigned Dwellers produce Scrap/s", poweredSnap.scrapPerSec > 0.0)
        assertTrue("Automated defenses are active when Turrets are built, Workers are fed & assigned, and Power is online", poweredSnap.isAutoDefenseActive)
        assertTrue("Powered & manned Gatling Turrets add to total Defense Rating when online", poweredSnap.totalDefenseRating > blackoutSnap.totalDefenseRating)

        // Verify Daily Food & Water requirement: Starving or Dehydrated workers halt room production & turret manning
        val starvingState = poweredState.copy(rations = 0.0)
        val starvingSnap = OutpostCalculator.computeProductionSnapshot(
            state = starvingState,
            buildingsMap = buildings,
            workersMap = workers,
            skillsMap = emptyMap(),
            nowMillis = 1000L
        )
        assertTrue("Workers should be flagged starving when rations == 0", starvingSnap.isWorkersStarvingOrDehydrated)
        assertEquals("Starving workers cannot run automated Vault rooms", 0.0, starvingSnap.scrapPerSec, 0.0001)
        assertFalse("Starving workers cannot man automated turrets", starvingSnap.isAutoDefenseActive)
    }

    @Test
    fun blastDoorUpgradeAndItemCraftingTimers_scaleByLevelAndSupportLegacyDiscounts() {
        val lv2DoorTime = OutpostCalculator.computeBlastDoorUpgradeTimeSeconds(targetDoorLevel = 2)
        val lv3DoorTime = OutpostCalculator.computeBlastDoorUpgradeTimeSeconds(targetDoorLevel = 3)
        val lv4DoorTime = OutpostCalculator.computeBlastDoorUpgradeTimeSeconds(targetDoorLevel = 4)
        val lv5DoorTime = OutpostCalculator.computeBlastDoorUpgradeTimeSeconds(targetDoorLevel = 5)

        assertTrue(
            "Upgrading Blast Door must take longer depending on the level of the door (Lv.2 < Lv.3 < Lv.4 < Lv.5)",
            lv2DoorTime < lv3DoorTime && lv3DoorTime < lv4DoorTime && lv4DoorTime < lv5DoorTime
        )

        val richState = OutpostStateEntity(scrap = 900.0, gold = 900.0, aetherCells = 50.0, blastDoorLevel = 1)
        val doorCostLv1To2 = OutpostCalculator.computeBlastDoorUpgradeCost(richState, emptyMap())
        val doorCostLv3To4 = OutpostCalculator.computeBlastDoorUpgradeCost(richState.copy(blastDoorLevel = 3), emptyMap())
        assertEquals(lv2DoorTime, doorCostLv1To2.upgradeTimeSeconds)
        assertEquals(lv4DoorTime, doorCostLv3To4.upgradeTimeSeconds)
        assertTrue("Door Lv.4 upgrade takes longer than Door Lv.2 upgrade", doorCostLv3To4.upgradeTimeSeconds > doorCostLv1To2.upgradeTimeSeconds)

        // Verify item crafting timers and Legacy Forge skill discount
        val baseCrossbowCraftTime = OutpostCalculator.computeCraftTimeSeconds(
            item = CraftableItemType.AETHER_CROSSBOW,
            state = richState,
            skillsMap = emptyMap()
        )
        val legacySkills = mapOf("LEG_NANO_SCHEMATICS" to 3)
        val discountedCrossbowCraftTime = OutpostCalculator.computeCraftTimeSeconds(
            item = CraftableItemType.AETHER_CROSSBOW,
            state = richState,
            skillsMap = legacySkills
        )
        assertTrue("Aether Crossbow requires crafting time in seconds", baseCrossbowCraftTime >= 10)
        assertTrue("Legacy Nano Schematics skill reduces crafting time", discountedCrossbowCraftTime < baseCrossbowCraftTime)
    }

    @Test
    fun overseerAscensionPrestige_and2dVaultMapStations_workAsExpected() {
        val earlyState = OutpostStateEntity(commandLevel = 2, dayCounter = 2)
        val earlyPrestige = OutpostCalculator.computePrestigePreview(earlyState, emptyMap())
        assertFalse("Cannot prestige before Command Lv.3 or 4 Rooms built", earlyPrestige.canAscend)

        val readyState = OutpostStateEntity(commandLevel = 7, dayCounter = 12, prestigeLevel = 0)
        val readyBuildings = mapOf(
            BuildingType.SALVAGE_YARD.id to 2,
            BuildingType.ATMOSPHERIC_CONDENSER.id to 2,
            BuildingType.HYDROPONIC_GREENHOUSE.id to 1,
            BuildingType.SURVIVOR_HAB_BLOCK.id to 1
        )
        val readyPrestige = OutpostCalculator.computePrestigePreview(readyState, readyBuildings)
        assertTrue("Can prestige when Command Lv.3+ and Rooms built", readyPrestige.canAscend)
        assertTrue("Prestige awards Permanent Prestige Cores", readyPrestige.coresToEarn >= 2)
        assertEquals(1.25, readyPrestige.nextPrestigeMultiplier, 0.01)

        val legacyNodes = SkillTreeCatalog.nodes.filter { it.branch == SkillBranch.OVERSEER_LEGACY }
        assertEquals("Overseer Legacy branch has 4 permanent Prestige Core skills", 4, legacyNodes.size)
        assertTrue("All Legacy skills cost Prestige Cores", legacyNodes.all { it.prestigeCoreCostPerRank > 0 })

        // Verify 2D Vault Cutaway stations cover Surface mining, Crater Lake fishing, and all 8 Vault rooms
        assertEquals(10, VaultMapStation.entries.size)
        assertEquals(8, VaultMapStation.entries.count { it.buildingType != null })
    }

    @Test
    fun dailyDefenseAndPostAttackRepair_scalesDefenseIntegrityAndCalculatesRepairCost() {
        val intactState = OutpostStateEntity(
            dayCounter = 5,
            blastDoorLevel = 2,
            craftedShrapnelMines = 2,
            defenseIntegrityPercent = 100,
            needsDefenseRepair = false,
            scrap = 100.0,
            gold = 100.0
        )
        val damagedState = intactState.copy(
            defenseIntegrityPercent = 65,
            needsDefenseRepair = true
        )

        val intactSnap = OutpostCalculator.computeProductionSnapshot(
            state = intactState,
            buildingsMap = emptyMap(),
            workersMap = emptyMap(),
            skillsMap = emptyMap(),
            nowMillis = 1000L
        )
        val damagedSnap = OutpostCalculator.computeProductionSnapshot(
            state = damagedState,
            buildingsMap = emptyMap(),
            workersMap = emptyMap(),
            skillsMap = emptyMap(),
            nowMillis = 1000L
        )

        assertTrue("Damaged Vault Door & Defenses should have lower effective Defense Rating", damagedSnap.totalDefenseRating < intactSnap.totalDefenseRating)
        assertEquals(5, intactSnap.daysUntilDay10Siege)

        val repairCost = OutpostCalculator.computeDefenseRepairCost(damagedState, emptyMap())
        assertTrue("Damaged defenses should require repair", repairCost.needsRepair)
        assertTrue("35% missing integrity should cost Scrap and Gold to repair", repairCost.scrapCost > 0.0 && repairCost.goldCost > 0.0)
        assertTrue(repairCost.canAfford)
    }

    @Test
    fun fishingHuntingAndDwellerUpkeep_workAsExpected() {
        val state = OutpostStateEntity(
            totalSurvivors = 3,
            craftedSpears = 1,
            craftedSnareTraps = 1
        )
        val noHabSnap = OutpostCalculator.computeProductionSnapshot(
            state = state,
            buildingsMap = emptyMap(),
            workersMap = emptyMap(),
            skillsMap = emptyMap(),
            nowMillis = 1000L
        )
        assertEquals("Each Dweller consumes 2 Food per new Day without Hab Block", 6.0, noHabSnap.dailyFoodUpkeepPerDay, 0.01)
        assertEquals("Each Dweller consumes 2 Water per new Day without Hab Block", 6.0, noHabSnap.dailyWaterUpkeepPerDay, 0.01)

        val habSnap = OutpostCalculator.computeProductionSnapshot(
            state = state,
            buildingsMap = mapOf(BuildingType.SURVIVOR_HAB_BLOCK.id to 2),
            workersMap = emptyMap(),
            skillsMap = emptyMap(),
            nowMillis = 1000L
        )
        assertTrue("Vault Living Quarters reduces Daily Dweller Food Upkeep (-5%/Lv)", habSnap.dailyFoodUpkeepPerDay < noHabSnap.dailyFoodUpkeepPerDay)

        val troutFish = OutpostCalculator.computeFishingPreview(
            spot = FishingSpot.CRATER_SHALLOWS_TROUT,
            state = state,
            skillsMap = emptyMap()
        )
        assertTrue("Blind Cave Trout requires no gear and yields Food & Water", troutFish.canFish && troutFish.foodReward > 0 && troutFish.waterReward > 0)

        val candidates = CaravanCandidateGenerator.generateBoard(totalHiredSoFar = 0, commandLevel = 1)
        assertTrue("Survivors are hired with Gold only", candidates.all { it.goldSigningFee >= 12.0 && it.rationsCost == 0.0 && it.waterCost == 0.0 })

        assertTrue("All Wasteland Expeditions have a 10% base Dweller death chance", WastelandExpeditionType.entries.all { it.deathChancePercent == 10 })
        val reducedDeath = OutpostCalculator.computeEffectiveExpeditionDeathChance(
            WastelandExpeditionType.entries.first(),
            mapOf(BuildingType.SIGNAL_RESEARCH_ARRAY.id to 3)
        )
        assertEquals("Radar Array Lv.3 reduces expedition death chance from 10% to 7%", 7, reducedDeath)
    }

    @Test
    fun buildingMechanics_escalatePowerDrawAtHigherLevels_andRequireConstructionTime() {
        val lv1Mw = OutpostCalculator.computeBuildingTotalMegawatts(BuildingType.SALVAGE_YARD, 1)
        val lv2Mw = OutpostCalculator.computeBuildingTotalMegawatts(BuildingType.SALVAGE_YARD, 2)
        val lv3Mw = OutpostCalculator.computeBuildingTotalMegawatts(BuildingType.SALVAGE_YARD, 3)

        assertEquals(4.0, lv1Mw, 0.01)
        assertTrue("Level 2 room draws more than 2x Level 1 MW due to high-level power escalation", lv2Mw > lv1Mw * 2.0)
        assertTrue("Incremental MW draw at Level 3 is higher than at Level 2", (lv3Mw - lv2Mw) > (lv2Mw - lv1Mw))

        val state = OutpostStateEntity(commandLevel = 5, scrap = 500.0, water = 500.0, bioFuel = 500.0)
        val lv0Cost = OutpostCalculator.computeBuildingUpgradeCost(
            buildingType = BuildingType.SALVAGE_YARD,
            currentLevel = 0,
            state = state,
            skillsMap = emptyMap()
        )
        val lv2Cost = OutpostCalculator.computeBuildingUpgradeCost(
            buildingType = BuildingType.SALVAGE_YARD,
            currentLevel = 2,
            state = state,
            skillsMap = emptyMap()
        )
        assertTrue("Building a room requires construction time in seconds", lv0Cost.buildTimeSeconds >= 5)
        assertTrue("Higher room levels require longer construction time", lv2Cost.buildTimeSeconds > lv0Cost.buildTimeSeconds)
        assertTrue("Next room level build time is always longer than current target level", lv0Cost.nextLevelBuildTimeSeconds > lv0Cost.buildTimeSeconds)

        // Verify higher room levels (Lv.1 < Lv.2 < Lv.3 < Lv.4) always take longer
        val yardLv1Time = OutpostCalculator.computeRoomBuildTimeSeconds(BuildingType.SALVAGE_YARD, targetRoomLevel = 1)
        val yardLv2Time = OutpostCalculator.computeRoomBuildTimeSeconds(BuildingType.SALVAGE_YARD, targetRoomLevel = 2)
        val yardLv3Time = OutpostCalculator.computeRoomBuildTimeSeconds(BuildingType.SALVAGE_YARD, targetRoomLevel = 3)
        val yardLv4Time = OutpostCalculator.computeRoomBuildTimeSeconds(BuildingType.SALVAGE_YARD, targetRoomLevel = 4)
        assertTrue("Higher room level must take longer to build (Lv.1 < Lv.2 < Lv.3 < Lv.4)", yardLv1Time < yardLv2Time && yardLv2Time < yardLv3Time && yardLv3Time < yardLv4Time)

        // Verify higher room numbers (Room #1 .. Room #8) take longer at the same target level
        val roomTimesAtLv1 = BuildingType.entries.map { OutpostCalculator.computeRoomBuildTimeSeconds(it, targetRoomLevel = 1) }
        for (i in 0 until roomTimesAtLv1.size - 1) {
            assertTrue(
                "Room #${i + 2} should have a longer build time than Room #${i + 1}",
                roomTimesAtLv1[i + 1] > roomTimesAtLv1[i]
            )
        }

        assertEquals("Pickaxe max level starts at 2 without Smeltery", 2, OutpostCalculator.computeMaxPickaxeLevel(emptyMap()))
        assertEquals("Smeltery Lv.2 unlocks Pickaxe Lv.6", 6, OutpostCalculator.computeMaxPickaxeLevel(mapOf(BuildingType.SALVAGE_YARD.id to 2)))
    }

    @Test
    fun uniqueVaultDwellers_haveEightPercentRareChance_andBoostResourceGatheringAbility() {
        assertEquals(8, CaravanCandidateGenerator.UNIQUE_DWELLER_CHANCE_PERCENT)

        val uniqueBoard = CaravanCandidateGenerator.generateBoard(
            totalHiredSoFar = 0,
            commandLevel = 1,
            forceUniqueRoll = true
        )
        assertTrue("Forced unique roll produces Unique Vault Dwellers with +40% resource yield bonus", uniqueBoard.all { it.isUniqueDweller && it.resourceYieldBonusPercent == 40 })

        val baseState = OutpostStateEntity(
            commandLevel = 2,
            totalSurvivors = 2,
            uniqueDwellersCount = 0,
            bioTurbineLevel = 2,
            solarTowerLevel = 1,
            bioFuel = 50.0,
            generatorWearPercent = 0
        )
        val uniqueState = baseState.copy(uniqueDwellersCount = 1)
        val buildings = mapOf(BuildingType.SALVAGE_YARD.id to 1, BuildingType.SURVIVOR_HAB_BLOCK.id to 1)
        val workers = mapOf(WorkerClassType.SCRAPPER.id to WorkerClassEntity(WorkerClassType.SCRAPPER.id, assignedCount = 2, classLevel = 1))

        val baseProd = OutpostCalculator.computeProductionSnapshot(baseState, buildings, workers, emptyMap(), 1000L)
        val uniqueProd = OutpostCalculator.computeProductionSnapshot(uniqueState, buildings, workers, emptyMap(), 1000L)
        assertTrue("Unique Vault Dweller boosts automated Scrap/s by +40%", uniqueProd.scrapPerSec > baseProd.scrapPerSec * 1.35)

        val baseSalvage = OutpostCalculator.computeSalvageNodeYield(
            SalvageNodeType.ARMORED_CONVOY_WRECK, baseState, buildings, workers, emptyMap(), DayNightPhase.SCORCHING_DAY
        )
        val uniqueSalvage = OutpostCalculator.computeSalvageNodeYield(
            SalvageNodeType.ARMORED_CONVOY_WRECK, uniqueState, buildings, workers, emptyMap(), DayNightPhase.SCORCHING_DAY
        )
        assertTrue("Unique Vault Dweller boosts manual salvage & gold yields", uniqueSalvage.primaryAmount > baseSalvage.primaryAmount && uniqueSalvage.goldAmount > baseSalvage.goldAmount)
    }

    @Test
    fun maxLevelIs100_withHardSteepXpCurve() {
        assertEquals(100, CommandLevelProgression.MAX_COMMAND_LEVEL)
        val lv1Xp = CommandLevelProgression.xpRequiredForLevel(1)
        val lv50Xp = CommandLevelProgression.xpRequiredForLevel(50)
        val lv100Xp = CommandLevelProgression.xpRequiredForLevel(100)
        assertEquals(320.0, lv1Xp, 0.1)
        assertTrue("Level 50 requires substantial XP", lv50Xp > 500_000.0)
        assertTrue("Level 100 is very challenging to reach", lv100Xp > 4_000_000.0)
    }

    @Test
    fun higherLevelVaultRooms_takeMoreResourcesToUpgrade_andConsumeMoreOngoingAndDailyUpkeep() {
        val state = OutpostStateEntity(commandLevel = 10, scrap = 5000.0, water = 5000.0, bioFuel = 5000.0)
        val costLv0To1 = OutpostCalculator.computeBuildingUpgradeCost(BuildingType.SALVAGE_YARD, 0, state, emptyMap())
        val costLv1To2 = OutpostCalculator.computeBuildingUpgradeCost(BuildingType.SALVAGE_YARD, 1, state, emptyMap())
        val costLv2To3 = OutpostCalculator.computeBuildingUpgradeCost(BuildingType.SALVAGE_YARD, 2, state, emptyMap())
        val costLv3To4 = OutpostCalculator.computeBuildingUpgradeCost(BuildingType.SALVAGE_YARD, 3, state, emptyMap())

        assertTrue(
            "Higher room levels cost progressively more Scrap to upgrade",
            costLv0To1.scrapCost < costLv1To2.scrapCost &&
                costLv1To2.scrapCost < costLv2To3.scrapCost &&
                costLv2To3.scrapCost < costLv3To4.scrapCost
        )
        assertTrue(
            "Higher-level upgrades unlock Cooling Water (Lv.2+) and Bio-Fuel (Lv.3+) requirements",
            costLv1To2.waterCost > 0.0 && costLv2To3.fuelCost > 0.0 && costLv3To4.fuelCost > costLv2To3.fuelCost
        )

        val upkeepLv1 = OutpostCalculator.computeBuildingResourceUpkeep(BuildingType.SALVAGE_YARD, 1)
        val upkeepLv2 = OutpostCalculator.computeBuildingResourceUpkeep(BuildingType.SALVAGE_YARD, 2)
        val upkeepLv3 = OutpostCalculator.computeBuildingResourceUpkeep(BuildingType.SALVAGE_YARD, 3)
        val upkeepLv4 = OutpostCalculator.computeBuildingResourceUpkeep(BuildingType.SALVAGE_YARD, 4)

        assertTrue(
            "Higher room levels take super-linearly more daily Scrap & Water upkeep",
            upkeepLv1.dailyScrap < upkeepLv2.dailyScrap &&
                upkeepLv2.dailyScrap < upkeepLv3.dailyScrap &&
                upkeepLv3.dailyScrap < upkeepLv4.dailyScrap &&
                (upkeepLv4.dailyScrap - upkeepLv3.dailyScrap) > (upkeepLv2.dailyScrap - upkeepLv1.dailyScrap)
        )
        assertTrue(
            "Rooms at Level 2+ require daily Cooling Water & Rooms at Level 3+ require Bio-Fuel upkeep",
            upkeepLv2.dailyWater > upkeepLv1.dailyWater && upkeepLv3.dailyFuel > 0.0 && upkeepLv4.dailyFuel > upkeepLv3.dailyFuel
        )
    }

    @Test
    fun attacksOnVault_getProgressivelyHarderAsDaysGoBy_withAcceleratingDifficultyCurve() {
        val day1Def = OutpostCalculator.computeDailyRaidRequiredDefense(1, DayNightPhase.SCORCHING_DAY)
        val day2Def = OutpostCalculator.computeDailyRaidRequiredDefense(2, DayNightPhase.SCORCHING_DAY)
        val day3Def = OutpostCalculator.computeDailyRaidRequiredDefense(3, DayNightPhase.SCORCHING_DAY)
        val day5Def = OutpostCalculator.computeDailyRaidRequiredDefense(5, DayNightPhase.SCORCHING_DAY)
        val day7Def = OutpostCalculator.computeDailyRaidRequiredDefense(7, DayNightPhase.SCORCHING_DAY)
        val day8Def = OutpostCalculator.computeDailyRaidRequiredDefense(8, DayNightPhase.SCORCHING_DAY)
        val day10MegaDef = OutpostCalculator.computeDailyRaidRequiredDefense(10, DayNightPhase.SCORCHING_DAY)
        val day20MegaDef = OutpostCalculator.computeDailyRaidRequiredDefense(20, DayNightPhase.SCORCHING_DAY)

        assertTrue(
            "Daily Vault attacks must require strictly more Defense Rating as days go by",
            day1Def < day2Def && day2Def < day3Def && day3Def < day5Def && day5Def < day7Def && day7Def < day8Def && day8Def < day10MegaDef && day10MegaDef < day20MegaDef
        )
        assertTrue(
            "Attack difficulty accelerates on later days ((Day 8 - Day 7) > (Day 2 - Day 1))",
            (day8Def - day7Def) > (day2Def - day1Def)
        )

        val day5NightDef = OutpostCalculator.computeDailyRaidRequiredDefense(5, DayNightPhase.RAD_NIGHT)
        assertTrue("Night Mutant Swarms are +35% harder than daytime attacks on the same day", day5NightDef > day5Def)
    }
}

