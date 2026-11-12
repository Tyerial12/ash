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
import com.example.model.PowerStationType
import com.example.model.SalvageNodeType
import com.example.model.SiegeCountermeasureType
import com.example.model.SkillBranch
import com.example.model.SkillTreeCatalog
import com.example.model.VaultMapStation
import com.example.model.VaultOperationalPosture
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
        assertTrue("Pickaxe mining yields real small amounts of Gold directly", baseSalvage.goldAmount in 1.5..8.0)
        assertTrue("Each Pickaxe swing yields a real small amount of Gold", baseSalvage.tapGoldAmount in 0.5..3.0)
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

        // Verify 2D & 3D Vault Cutaway stations cover Surface mining, Crater Lake fishing, and all 11 Vault rooms (Floors B1-B6)
        assertEquals(13, VaultMapStation.entries.size)
        assertEquals(11, VaultMapStation.entries.count { it.buildingType != null })
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

        // Verify resource caps are rebalanced around room upgrades:
        // Base storage cap starts at 150.0, and each room upgrade increases total Vault storage cap!
        assertEquals(150.0, OutpostCalculator.BASE_STORAGE_CAP, 0.01)
        assertTrue(
            "Every room upgrade grants additional resource storage capacity (Lv.0->1 < Lv.1->2 < Lv.2->3)",
            costLv0To1.additionalStorageCapGain > 0.0 &&
                costLv1To2.additionalStorageCapGain > costLv0To1.additionalStorageCapGain &&
                costLv2To3.additionalStorageCapGain > costLv1To2.additionalStorageCapGain
        )
        val starterState = OutpostStateEntity(commandLevel = 1)
        val snapNoRooms = OutpostCalculator.computeProductionSnapshot(starterState, emptyMap(), emptyMap(), emptyMap(), 1000L)
        val snapRoomsLv1 = OutpostCalculator.computeProductionSnapshot(
            starterState,
            mapOf(
                BuildingType.SALVAGE_YARD.id to 1,
                BuildingType.ATMOSPHERIC_CONDENSER.id to 1,
                BuildingType.HYDROPONIC_GREENHOUSE.id to 1
            ),
            emptyMap(),
            emptyMap(),
            1000L
        )
        val snapRoomsLv2 = OutpostCalculator.computeProductionSnapshot(
            starterState,
            mapOf(
                BuildingType.SALVAGE_YARD.id to 2,
                BuildingType.ATMOSPHERIC_CONDENSER.id to 2,
                BuildingType.HYDROPONIC_GREENHOUSE.id to 2
            ),
            emptyMap(),
            emptyMap(),
            1000L
        )
        assertTrue(
            "Upgrading standard Vault rooms expands storageCap around room upgrades (0 rooms < Lv.1 rooms < Lv.2 rooms)",
            snapNoRooms.storageCap < snapRoomsLv1.storageCap &&
                snapRoomsLv1.storageCap < snapRoomsLv2.storageCap &&
                snapRoomsLv1.roomUpgradesStorageBonus > 0.0 &&
                snapRoomsLv2.roomUpgradesStorageBonus > snapRoomsLv1.roomUpgradesStorageBonus
        )
        assertTrue(
            "Lv.1 room upgrades fit within starter base storageCap, and Lv.2/Lv.3 room upgrades fit within the storageCap unlocked by upgrading rooms",
            costLv0To1.scrapCost <= snapNoRooms.storageCap &&
                costLv1To2.scrapCost <= snapRoomsLv1.storageCap &&
                costLv2To3.scrapCost <= snapRoomsLv2.storageCap
        )

        val upkeepLv1 = OutpostCalculator.computeBuildingResourceUpkeep(BuildingType.SALVAGE_YARD, 1, dayCounter = 1)
        val upkeepLv2 = OutpostCalculator.computeBuildingResourceUpkeep(BuildingType.SALVAGE_YARD, 2, dayCounter = 1)
        val upkeepLv3 = OutpostCalculator.computeBuildingResourceUpkeep(BuildingType.SALVAGE_YARD, 3, dayCounter = 1)
        val upkeepLv4 = OutpostCalculator.computeBuildingResourceUpkeep(BuildingType.SALVAGE_YARD, 4, dayCounter = 1)

        assertTrue(
            "Higher room levels take super-linearly more daily Scrap, Water & Bio-Fuel maintenance each day",
            upkeepLv1.dailyScrap < upkeepLv2.dailyScrap &&
                upkeepLv2.dailyScrap < upkeepLv3.dailyScrap &&
                upkeepLv3.dailyScrap < upkeepLv4.dailyScrap &&
                (upkeepLv4.dailyScrap - upkeepLv3.dailyScrap) > (upkeepLv2.dailyScrap - upkeepLv1.dailyScrap) &&
                upkeepLv1.dailyWater > 0.0 &&
                upkeepLv1.dailyWater < upkeepLv2.dailyWater &&
                upkeepLv2.dailyWater < upkeepLv3.dailyWater &&
                upkeepLv1.dailyFuel > 0.0 &&
                upkeepLv1.dailyFuel < upkeepLv2.dailyFuel &&
                upkeepLv2.dailyFuel < upkeepLv3.dailyFuel &&
                upkeepLv3.dailyFuel < upkeepLv4.dailyFuel
        )

        // Verify room maintenance takes more resources per level as each day passes (Day 1 < Day 5 < Day 10)
        val upkeepLv2Day1 = OutpostCalculator.computeBuildingResourceUpkeep(BuildingType.SALVAGE_YARD, 2, dayCounter = 1)
        val upkeepLv2Day5 = OutpostCalculator.computeBuildingResourceUpkeep(BuildingType.SALVAGE_YARD, 2, dayCounter = 5)
        val upkeepLv2Day10 = OutpostCalculator.computeBuildingResourceUpkeep(BuildingType.SALVAGE_YARD, 2, dayCounter = 10)
        assertTrue(
            "Room maintenance takes more Scrap, Water, and Fuel per level each passing day (Day 1 < Day 5 < Day 10)",
            upkeepLv2Day1.dailyScrap < upkeepLv2Day5.dailyScrap &&
                upkeepLv2Day5.dailyScrap < upkeepLv2Day10.dailyScrap &&
                upkeepLv2Day1.dailyWater < upkeepLv2Day5.dailyWater &&
                upkeepLv2Day5.dailyWater < upkeepLv2Day10.dailyWater &&
                upkeepLv2Day1.dailyFuel < upkeepLv2Day5.dailyFuel &&
                upkeepLv2Day5.dailyFuel < upkeepLv2Day10.dailyFuel
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

    @Test
    fun higherVaultCommandLevel_andRoomLevels_increaseOngoingResourceUpkeep() {
        val vaultUpkeepLv1 = OutpostCalculator.computeVaultLevelResourceUpkeep(commandLevel = 1)
        val vaultUpkeepLv3 = OutpostCalculator.computeVaultLevelResourceUpkeep(commandLevel = 3)
        val vaultUpkeepLv6 = OutpostCalculator.computeVaultLevelResourceUpkeep(commandLevel = 6)

        assertEquals(0.0, vaultUpkeepLv1.dailyScrap, 0.001)
        assertTrue("Vault Command Level 3 takes ongoing Scrap & Cooling Water upkeep", vaultUpkeepLv3.dailyScrap > 0.0 && vaultUpkeepLv3.dailyWater > 0.0)
        assertTrue("Vault Command Level 6 takes more Scrap, Water & Bio-Fuel than Level 3", vaultUpkeepLv6.dailyScrap > vaultUpkeepLv3.dailyScrap && vaultUpkeepLv6.dailyFuel > 0.0)

        val roomAtVaultLv1 = OutpostCalculator.computeBuildingResourceUpkeep(BuildingType.SALVAGE_YARD, level = 2, hydroDiscountMult = 1.0, commandLevel = 1)
        val roomAtVaultLv5 = OutpostCalculator.computeBuildingResourceUpkeep(BuildingType.SALVAGE_YARD, level = 2, hydroDiscountMult = 1.0, commandLevel = 5)
        assertTrue("Same Room Level consumes more resources when overall Vault Command Level is higher", roomAtVaultLv5.dailyScrap > roomAtVaultLv1.dailyScrap)
    }

    @Test
    fun vaultResourceDeficit_shutsDownDefensesImmediately_andEndsGameAfter2Days() {
        // High-upkeep Vault with 0 assigned workers (so 0 gross resource gain/sec, but high Vault + Room upkeep/sec)
        val deficitState = OutpostStateEntity(
            commandLevel = 5,
            blastDoorLevel = 3,
            craftedShrapnelMines = 4,
            sentryTurretCount = 2,
            defenseIntegrityPercent = 100,
            bioTurbineLevel = 2,
            solarTowerLevel = 2,
            bioFuel = 100.0
        )
        val buildings = mapOf(
            BuildingType.SALVAGE_YARD.id to 3,
            BuildingType.ATMOSPHERIC_CONDENSER.id to 2
        )
        val deficitSnap = OutpostCalculator.computeProductionSnapshot(
            state = deficitState,
            buildingsMap = buildings,
            workersMap = emptyMap(),
            skillsMap = emptyMap(),
            nowMillis = 1000L
        )

        assertTrue("Vault should be in resource deficit when upkeep/sec exceeds gross gain/sec", deficitSnap.isVaultResourceDeficit)
        assertTrue("Defenses must immediately shut down during resource deficit", deficitSnap.isDefenseShutDownByDeficit)
        assertEquals("Total Defense Rating drops to 0 when Vault takes more to run than it gains", 0, deficitSnap.totalDefenseRating)
        assertFalse("Automated Defense Grid shuts down during resource deficit", deficitSnap.isAutoDefenseActive)

        val day1Eval = OutpostCalculator.evaluateSurvivalEndOfDay(deficitState, deficitSnap)
        assertEquals(1, day1Eval.nextDeficitDaysCount)
        assertFalse("Day 1 of resource deficit warns player but does not end game yet", day1Eval.isGameOver)

        val day2Eval = OutpostCalculator.evaluateSurvivalEndOfDay(
            deficitState.copy(deficitDaysCount = 1),
            deficitSnap
        )
        assertEquals(2, day2Eval.nextDeficitDaysCount)
        assertTrue("2 consecutive days of resource deficit triggers Game Over", day2Eval.isGameOver)
        assertTrue(day2Eval.gameOverReason.isNotEmpty())
    }

    @Test
    fun powerOutageBlackout_endsGameAfter6Days_andResetsWhenPowerRestored() {
        val blackoutState = OutpostStateEntity(
            commandLevel = 2,
            bioTurbineLevel = 0,
            solarTowerLevel = 0,
            blackoutDaysCount = 4
        )
        val buildings = mapOf(BuildingType.SALVAGE_YARD.id to 2)
        val blackoutSnap = OutpostCalculator.computeProductionSnapshot(
            state = blackoutState,
            buildingsMap = buildings,
            workersMap = emptyMap(),
            skillsMap = emptyMap(),
            nowMillis = 1000L
        )
        assertTrue(blackoutSnap.isVaultBlackout)

        val day5Eval = OutpostCalculator.evaluateSurvivalEndOfDay(blackoutState, blackoutSnap)
        assertEquals(5, day5Eval.nextBlackoutDaysCount)
        assertFalse("Day 5 of blackout does not end game yet", day5Eval.isGameOver)

        val day6Eval = OutpostCalculator.evaluateSurvivalEndOfDay(
            blackoutState.copy(blackoutDaysCount = 5),
            blackoutSnap
        )
        assertEquals(6, day6Eval.nextBlackoutDaysCount)
        assertTrue("6 consecutive days without power triggers Game Over", day6Eval.isGameOver)

        // Restoring power resets blackoutDaysCount to 0
        val poweredState = blackoutState.copy(bioTurbineLevel = 2, solarTowerLevel = 2, bioFuel = 100.0, generatorWearPercent = 0)
        val poweredSnap = OutpostCalculator.computeProductionSnapshot(
            state = poweredState,
            buildingsMap = buildings,
            workersMap = emptyMap(),
            skillsMap = emptyMap(),
            nowMillis = 1000L
        )
        assertFalse(poweredSnap.isVaultBlackout)
        val recoveredEval = OutpostCalculator.evaluateSurvivalEndOfDay(poweredState, poweredSnap)
        assertEquals(0, recoveredEval.nextBlackoutDaysCount)
    }

    @Test
    fun dwellersConsumeFoodAndWaterEvery5Minutes_andEscalateAppetiteEvery2Minutes() {
        val baseDwellerState = OutpostStateEntity(
            totalSurvivors = 4,
            dwellerAppetiteEscalationSeconds = 0
        )
        val tier2DwellerState = baseDwellerState.copy(dwellerAppetiteEscalationSeconds = 240) // 4 minutes = Tier 2 (+28%)
        val tier4DwellerState = baseDwellerState.copy(dwellerAppetiteEscalationSeconds = 480) // 8 minutes = Tier 4 (+56%)

        val snap0 = OutpostCalculator.computeProductionSnapshot(baseDwellerState, emptyMap(), emptyMap(), emptyMap(), 1000L)
        val snap2 = OutpostCalculator.computeProductionSnapshot(tier2DwellerState, emptyMap(), emptyMap(), emptyMap(), 1000L)
        val snap4 = OutpostCalculator.computeProductionSnapshot(tier4DwellerState, emptyMap(), emptyMap(), emptyMap(), 1000L)

        assertTrue("Dwellers require Food and Water every 5 minutes", snap0.fiveMinDwellerFoodCost > 0.0 && snap0.fiveMinDwellerWaterCost > 0.0)
        assertTrue(
            "Dwellers take more resources every 2 minutes (0m < 4m < 8m)",
            snap0.fiveMinDwellerFoodCost < snap2.fiveMinDwellerFoodCost &&
                snap2.fiveMinDwellerFoodCost < snap4.fiveMinDwellerFoodCost &&
                snap0.dailyFoodUpkeepPerDay < snap2.dailyFoodUpkeepPerDay &&
                snap2.dailyFoodUpkeepPerDay < snap4.dailyFoodUpkeepPerDay
        )
    }

    @Test
    fun earlyBunkerGrace_operationalPostures_andFloorB5Rooms_workAsExpected() {
        // 1. Early Bunker Grace (Days 1-3, Command Lv <= 2, < 2 rooms built) pauses 2-day deficit game over
        val earlyGraceState = OutpostStateEntity(
            dayCounter = 2,
            commandLevel = 1,
            deficitDaysCount = 0
        )
        val earlySnap = OutpostCalculator.computeProductionSnapshot(
            state = earlyGraceState,
            buildingsMap = mapOf(BuildingType.SALVAGE_YARD.id to 1),
            workersMap = emptyMap(),
            skillsMap = emptyMap(),
            nowMillis = 1000L
        )
        assertTrue("Early Bunker Grace is active on Day 1-3 with < 2 rooms built", earlySnap.isEarlyBunkerGraceActive)
        assertFalse("Early Bunker Grace prevents Vault Resource Deficit from shutting down defenses", earlySnap.isVaultResourceDeficit)
        val earlyDayEval = OutpostCalculator.evaluateSurvivalEndOfDay(earlyGraceState, earlySnap)
        assertEquals("Early Bunker Grace keeps deficit days count at 0", 0, earlyDayEval.nextDeficitDaysCount)

        // 2. Operational Postures: ECO_CONSERVATION cuts resource drain by -40% compared to STANDARD
        val midGameState = OutpostStateEntity(
            dayCounter = 12,
            commandLevel = 4,
            totalSurvivors = 4,
            operationalPostureId = VaultOperationalPosture.STANDARD.id,
            bioTurbineLevel = 2,
            solarTowerLevel = 2,
            bioFuel = 100.0
        )
        val midBuildings = mapOf(
            BuildingType.SALVAGE_YARD.id to 2,
            BuildingType.ATMOSPHERIC_CONDENSER.id to 2
        )
        val standardSnap = OutpostCalculator.computeProductionSnapshot(midGameState, midBuildings, emptyMap(), emptyMap(), 1000L)
        val ecoSnap = OutpostCalculator.computeProductionSnapshot(
            midGameState.copy(operationalPostureId = VaultOperationalPosture.ECO_CONSERVATION.id),
            midBuildings,
            emptyMap(),
            emptyMap(),
            1000L
        )
        val overclockSnap = OutpostCalculator.computeProductionSnapshot(
            midGameState.copy(operationalPostureId = VaultOperationalPosture.OVERCLOCK.id),
            midBuildings,
            emptyMap(),
            emptyMap(),
            1000L
        )
        assertTrue("ECO_CONSERVATION reduces total vault resource drain/sec by 40%", ecoSnap.totalVaultResourceDrainPerSec < standardSnap.totalVaultResourceDrainPerSec * 0.65)
        assertTrue("OVERCLOCK increases vault resource drain/sec", overclockSnap.totalVaultResourceDrainPerSec > standardSnap.totalVaultResourceDrainPerSec)

        // 3. Floor B5 Rooms #9 (Geothermal Tap) & #10 (Cryo-Stasis Vault)
        val b5Buildings = midBuildings + mapOf(
            BuildingType.GEOTHERMAL_TAP.id to 2,
            BuildingType.CRYO_STASIS_VAULT.id to 2
        )
        val b5Snap = OutpostCalculator.computeProductionSnapshot(midGameState, b5Buildings, emptyMap(), emptyMap(), 1000L)
        assertTrue("Room #9 Deep-Core Geothermal Tap generates +7.5 MW/Lv 24/7 power", b5Snap.totalMegawattGenerated >= standardSnap.totalMegawattGenerated + 15.0)
        assertTrue("Room #10 Cryo-Stasis Vault cuts 5-minute Dweller Food/Water upkeep and adds +400/Lv storage", b5Snap.fiveMinDwellerFoodCost < standardSnap.fiveMinDwellerFoodCost && b5Snap.storageCap >= standardSnap.storageCap + 800.0)

        // 4. Named 10-Day Warlord Bosses & Tactical Countermeasures
        assertTrue(OutpostCalculator.computeDay10BossTitle(10).contains("Goliath", ignoreCase = true))
        assertTrue(OutpostCalculator.computeDay10BossTitle(20).contains("Behemoth", ignoreCase = true))
        assertEquals(3, SiegeCountermeasureType.entries.size)
    }

    @Test
    fun sixtySecFeeding_storeRoom_reactorFuelScaling_fourManualCranks_andNetBalance_workAsExpected() {
        // 1. Dwellers have to eat every 60 seconds
        assertEquals(60, OutpostCalculator.DWELLER_FEED_INTERVAL_SECONDS)
        assertEquals(60, OutpostStateEntity().dwellerFeedCycleSecondsRemaining)

        // 2. Upgradable Store Room (VAULT_STORE_ROOM) increases max resources that can be stored
        val baseState = OutpostStateEntity(commandLevel = 2)
        val snapNoStore = OutpostCalculator.computeProductionSnapshot(baseState, emptyMap(), emptyMap(), emptyMap(), 1000L)
        val snapStoreLv1 = OutpostCalculator.computeProductionSnapshot(
            baseState,
            mapOf(BuildingType.VAULT_STORE_ROOM.id to 1),
            emptyMap(),
            emptyMap(),
            1000L
        )
        val snapStoreLv2 = OutpostCalculator.computeProductionSnapshot(
            baseState,
            mapOf(BuildingType.VAULT_STORE_ROOM.id to 2),
            emptyMap(),
            emptyMap(),
            1000L
        )
        val snapStoreLv3 = OutpostCalculator.computeProductionSnapshot(
            baseState,
            mapOf(BuildingType.VAULT_STORE_ROOM.id to 3),
            emptyMap(),
            emptyMap(),
            1000L
        )
        assertTrue(
            "Upgrading Vault Supply Store Room progressively increases max stored resources (Lv.0 < Lv.1 < Lv.2 < Lv.3)",
            snapNoStore.storageCap < snapStoreLv1.storageCap &&
                snapStoreLv1.storageCap < snapStoreLv2.storageCap &&
                snapStoreLv2.storageCap < snapStoreLv3.storageCap
        )
        assertEquals(1000.0, snapStoreLv1.storeRoomStorageBonus, 0.01)
        assertTrue(snapStoreLv2.storeRoomStorageBonus > snapStoreLv1.storeRoomStorageBonus * 2.0)

        // 3. Higher-level reactors take progressively more Bio-Fuel
        val reactorFuelLv1 = OutpostCalculator.computeReactorFuelUpkeepForStation(PowerStationType.THORIUM_FISSION_REACTOR, 1)
        val reactorFuelLv2 = OutpostCalculator.computeReactorFuelUpkeepForStation(PowerStationType.THORIUM_FISSION_REACTOR, 2)
        val reactorFuelLv3 = OutpostCalculator.computeReactorFuelUpkeepForStation(PowerStationType.THORIUM_FISSION_REACTOR, 3)
        assertTrue(
            "Higher-level reactors consume super-linearly more Bio-Fuel (Lv.1 < Lv.2 < Lv.3)",
            reactorFuelLv1 > 0.0 &&
                reactorFuelLv1 < reactorFuelLv2 &&
                reactorFuelLv2 < reactorFuelLv3 &&
                (reactorFuelLv3 - reactorFuelLv2) > (reactorFuelLv2 - reactorFuelLv1)
        )

        // 4. Manual crank for power 4 times every time power goes out
        assertEquals(4, OutpostCalculator.MAX_DYNAMO_CRANKS_PER_BLACKOUT)
        assertEquals(4, OutpostStateEntity().manualDynamoCranksRemaining)
        val crank1State = OutpostStateEntity(thoriumOverdriveUntilMillis = 5000L, activeDynamoCrankStack = 1, generatorWearPercent = 0)
        val crank4State = OutpostStateEntity(thoriumOverdriveUntilMillis = 5000L, activeDynamoCrankStack = 4, generatorWearPercent = 0)
        val crank1Snap = OutpostCalculator.computeProductionSnapshot(crank1State, emptyMap(), emptyMap(), emptyMap(), 1000L)
        val crank4Snap = OutpostCalculator.computeProductionSnapshot(crank4State, emptyMap(), emptyMap(), emptyMap(), 1000L)
        assertTrue("4 stacked manual dynamo cranks generate 4x the MW of 1 crank", crank4Snap.totalMegawattGenerated > crank1Snap.totalMegawattGenerated * 3.5)

        // 5. Positive vs Deficit Net Resource Balance
        val positiveWorkers = mapOf(
            WorkerClassType.SCRAPPER.id to WorkerClassEntity(WorkerClassType.SCRAPPER.id, assignedCount = 2, classLevel = 1),
            WorkerClassType.HYDRO_ENGINEER.id to WorkerClassEntity(WorkerClassType.HYDRO_ENGINEER.id, assignedCount = 2, classLevel = 1)
        )
        val positiveState = OutpostStateEntity(
            commandLevel = 1,
            totalSurvivors = 4,
            rations = 50.0,
            water = 50.0,
            bioFuel = 50.0,
            bioTurbineLevel = 2,
            generatorWearPercent = 0
        )
        val positiveBuildings = mapOf(
            BuildingType.SALVAGE_YARD.id to 1,
            BuildingType.ATMOSPHERIC_CONDENSER.id to 1
        )
        val positiveSnap = OutpostCalculator.computeProductionSnapshot(positiveState, positiveBuildings, positiveWorkers, emptyMap(), 1000L)
        assertTrue("Active powered Vault with workers is making positive resources", positiveSnap.isMakingPositiveResources && positiveSnap.netResourceBalancePerSec > 0.0)

        val negativeState = OutpostStateEntity(
            commandLevel = 5,
            totalSurvivors = 0,
            bioTurbineLevel = 3,
            thoriumReactorLevel = 2
        )
        val negativeSnap = OutpostCalculator.computeProductionSnapshot(negativeState, positiveBuildings, emptyMap(), emptyMap(), 1000L)
        assertFalse("Unstaffed high-level Vault with running reactors is taking more than it is making", negativeSnap.isMakingPositiveResources)
        assertTrue(negativeSnap.netResourceBalancePerSec < 0.0)

        // 6. Max Dwellers is 30 (+4 per room level) & Rooms gradually get better from Level 0 (worst) upward
        assertEquals(30, OutpostCalculator.MAX_VAULT_DWELLERS)
        val habLv12Snap = OutpostCalculator.computeProductionSnapshot(
            baseState,
            mapOf(BuildingType.SURVIVOR_HAB_BLOCK.id to 12),
            emptyMap(),
            emptyMap(),
            1000L
        )
        assertEquals("Max Dwellers is strictly capped at 30 even with high-level Living Quarters", 30, habLv12Snap.maxWorkerCapacity)

        val lv0Mult = OutpostCalculator.computeRoomLevelOutputMultiplier(0)
        val lv1Mult = OutpostCalculator.computeRoomLevelOutputMultiplier(1)
        val lv2Mult = OutpostCalculator.computeRoomLevelOutputMultiplier(2)
        val lv3Mult = OutpostCalculator.computeRoomLevelOutputMultiplier(3)
        val lv4Mult = OutpostCalculator.computeRoomLevelOutputMultiplier(4)
        val lv5Mult = OutpostCalculator.computeRoomLevelOutputMultiplier(5)
        assertTrue(
            "Rooms gradually get better each level with Level 0 being worst (0.15) -> Lv.1 (0.28) -> Lv.2 (0.42) -> Lv.3 (0.60) -> Lv.4 (0.85) -> Lv.5 (1.15)",
            lv0Mult == 0.15 && lv0Mult < lv1Mult && lv1Mult < lv2Mult && lv2Mult < lv3Mult && lv3Mult < lv4Mult && lv4Mult < lv5Mult && lv5Mult > 1.0
        )

        val eff0 = OutpostCalculator.computeRoomEffectiveTotalOutputMultiplier(0)
        val eff1 = OutpostCalculator.computeRoomEffectiveTotalOutputMultiplier(1)
        val eff2 = OutpostCalculator.computeRoomEffectiveTotalOutputMultiplier(2)
        val eff3 = OutpostCalculator.computeRoomEffectiveTotalOutputMultiplier(3)
        assertTrue(
            "Effective total room output multiplier is worst at Level 0 (> 0.0) and increases every level",
            eff0 > 0.0 && eff0 < eff1 && eff1 < eff2 && eff2 < eff3
        )

        // 7. Store Room full halts gathering, and each individual resource automatically resumes when IT falls to HALF (<= 50% of storageCap) without needing Gold!
        val fullStoreState = positiveState.copy(
            scrap = positiveSnap.storageCap,
            water = positiveSnap.storageCap,
            rations = positiveSnap.storageCap,
            bioFuel = positiveSnap.storageCap,
            researchData = positiveSnap.storageCap,
            isStoreRoomGatheringHalted = true,
            isScrapGatheringHalted = true,
            isWaterGatheringHalted = true,
            isRationsGatheringHalted = true,
            isFuelGatheringHalted = true,
            isResearchGatheringHalted = true
        )
        val fullStoreSnap = OutpostCalculator.computeProductionSnapshot(fullStoreState, positiveBuildings, positiveWorkers, emptyMap(), 1000L)
        assertTrue("Store Room is marked full when all primary resources reach storageCap", fullStoreSnap.isStoreRoomFull)
        assertEquals("Store Room resume threshold is half (50%) of storageCap", positiveSnap.storageCap * 0.50, fullStoreSnap.storeRoomResumeThreshold, 0.01)
        assertEquals("When Store Room is full, total gross resource gathering per sec is 0.0", 0.0, fullStoreSnap.totalGrossResourceGainPerSec, 0.001)
        assertEquals("When Store Room is full, Gold/s gathering is 0.0", 0.0, fullStoreSnap.goldPerSec, 0.001)
        assertEquals("When Store Room is full, Research/s gathering is 0.0", 0.0, fullStoreSnap.researchPerSec, 0.001)
        assertEquals("When Store Room is full, Mutagen/s gathering is 0.0", 0.0, fullStoreSnap.mutagenCoresPerSec, 0.001)
        assertEquals("When Store Room is full, Daily Room Scrap Bonus is 0.0", 0.0, fullStoreSnap.dailyRoomScrapBonus, 0.001)
        assertFalse("Full Store Room does not trigger a false resource deficit shutdown", fullStoreSnap.isVaultResourceDeficit)

        // Dropping slightly below full (e.g. 80% of storageCap, still above 50% half threshold) keeps gathering HALTED so alert doesn't flicker!
        val aboveHalfState = fullStoreState.copy(
            scrap = positiveSnap.storageCap * 0.80,
            water = positiveSnap.storageCap * 0.80,
            rations = positiveSnap.storageCap * 0.80,
            bioFuel = positiveSnap.storageCap * 0.80
        )
        val aboveHalfSnap = OutpostCalculator.computeProductionSnapshot(aboveHalfState, positiveBuildings, positiveWorkers, emptyMap(), 1000L)
        assertTrue("Resources above half capacity (80% > 50%) stay halted once full", aboveHalfSnap.isStoreRoomFull)
        assertEquals("Gathering remains 0.0/s while all resources are still above half capacity", 0.0, aboveHalfSnap.totalGrossResourceGainPerSec, 0.001)

        // When Water & Food Rations fall to half of storage room capacity (<= 50%) from natural upkeep while Scrap is STILL at 90% (> 50%),
        // Water & Food Rations immediately resume production without needing to spend Gold or drain Scrap!
        val waterAndFoodAtHalfState = fullStoreState.copy(
            scrap = positiveSnap.storageCap * 0.90, // Still above 50% -> Scrap stays halted!
            water = positiveSnap.storageCap * 0.50, // Fell to half (50%) -> Water resumes!
            rations = positiveSnap.storageCap * 0.45, // Fell below half (45%) -> Food resumes!
            bioFuel = positiveSnap.storageCap * 0.85 // Still above 50% -> Fuel stays halted!
        )
        val partialResumeSnap = OutpostCalculator.computeProductionSnapshot(waterAndFoodAtHalfState, positiveBuildings, positiveWorkers, emptyMap(), 1000L)
        assertTrue("Scrap (90% > 50%) stays halted", partialResumeSnap.isScrapHalted)
        assertFalse("Water (50% <= 50%) automatically resumes production", partialResumeSnap.isWaterHalted)
        assertFalse("Food Rations (45% <= 50%) automatically resumes production", partialResumeSnap.isRationsHalted)
        assertTrue("Water & Food resume positive gross gathering even while Scrap is still halted at 90%", partialResumeSnap.totalGrossResourceGainPerSec > 0.0)
        assertFalse("Store Room is no longer completely locked once Water & Food fall to half capacity", partialResumeSnap.isStoreRoomFull)
        assertFalse("Partial Store Room halt does not trigger a false resource deficit shutdown", partialResumeSnap.isVaultResourceDeficit)
    }

    @Test
    fun hardcoreMiningBuildingHunting_andOfflineNotificationSchedule_workAsExpected() {
        // 1. Hardcore Mining: Pickaxe Durability & Cave-In Hazards reduce salvage yield
        val freshPickaxeState = OutpostStateEntity(
            pickaxeDurabilityPercent = 100,
            activeCaveInHazard = false
        )
        val wornCaveInState = OutpostStateEntity(
            pickaxeDurabilityPercent = 10,
            activeCaveInHazard = true
        )
        val freshYield = OutpostCalculator.computeSalvageNodeYield(
            SalvageNodeType.ARMORED_CONVOY_WRECK, freshPickaxeState, emptyMap(), emptyMap(), emptyMap(), DayNightPhase.SCORCHING_DAY
        )
        val wornYield = OutpostCalculator.computeSalvageNodeYield(
            SalvageNodeType.ARMORED_CONVOY_WRECK, wornCaveInState, emptyMap(), emptyMap(), emptyMap(), DayNightPhase.SCORCHING_DAY
        )
        assertTrue("Dull pickaxe + Cave-In significantly reduces mining yield", wornYield.primaryAmount < freshYield.primaryAmount)

        // 2. Hardcore Building: Pipe Leaks drain Water/Scrap & Room Structural Wear reduces room efficiency
        val intactRoomState = OutpostStateEntity(
            commandLevel = 2,
            totalSurvivors = 2,
            rations = 50.0,
            water = 50.0,
            bioFuel = 50.0,
            bioTurbineLevel = 2,
            generatorWearPercent = 0,
            activePipeLeaksCount = 0,
            roomStructuralWearPercent = 0
        )
        val leakingWornState = intactRoomState.copy(
            activePipeLeaksCount = 3,
            roomStructuralWearPercent = 50
        )
        val workers = mapOf(
            WorkerClassType.SCRAPPER.id to WorkerClassEntity(WorkerClassType.SCRAPPER.id, assignedCount = 2, classLevel = 1)
        )
        val buildings = mapOf(BuildingType.SALVAGE_YARD.id to 2)
        val intactSnap = OutpostCalculator.computeProductionSnapshot(intactRoomState, buildings, workers, emptyMap(), 1000L)
        val leakingSnap = OutpostCalculator.computeProductionSnapshot(leakingWornState, buildings, workers, emptyMap(), 1000L)
        assertTrue("Pipe leaks drain Water and Scrap per second", leakingSnap.pipeLeakWaterDrainPerSec > 0.0 && leakingSnap.pipeLeakScrapDrainPerSec > 0.0)
        assertTrue("Room structural wear and pipe leaks reduce net Scrap/s", leakingSnap.scrapPerSec < intactSnap.scrapPerSec)

        // 3. Hardcore Hunting: Ammo Scarcity & Beast Counter-Attack Injury Chance
        val stockedHunterState = OutpostStateEntity(craftedSpears = 2, craftedSnareTraps = 2, ammoRounds = 20)
        val emptyAmmoState = stockedHunterState.copy(ammoRounds = 0)
        val stockedHunt = OutpostCalculator.computeHuntPreview(
            com.example.model.HuntingTarget.CANYON_PACK_STALKER, stockedHunterState, emptyMap(), emptyMap(), DayNightPhase.SCORCHING_DAY
        )
        val lowAmmoHunt = OutpostCalculator.computeHuntPreview(
            com.example.model.HuntingTarget.CANYON_PACK_STALKER, emptyAmmoState, emptyMap(), emptyMap(), DayNightPhase.SCORCHING_DAY
        )
        assertTrue("Hunting without recommended Ballistic Ammo reduces food reward", lowAmmoHunt.foodReward < stockedHunt.foodReward)
        assertTrue("Beast counter-attack injury chance is higher without sufficient ammo", lowAmmoHunt.counterAttackInjuryChancePercent > stockedHunt.counterAttackInjuryChancePercent)

        // 4. Offline Phone Notification Schedule (Room Upgrade Complete, Max Resources, Out of Power)
        val schedule = OutpostCalculator.computeOfflineNotificationSchedule(
            state = intactRoomState.copy(scrap = 100.0, bioFuel = 10.0),
            production = intactSnap,
            activeConstructionBuildingId = BuildingType.SALVAGE_YARD.id,
            activeConstructionTargetLevel = 3,
            activeConstructionRemainingSeconds = 42
        )
        assertEquals(42L, schedule.secondsUntilRoomUpgradeComplete)
        assertTrue("Positive net Scrap/s schedules a Max Resources ETA", (schedule.secondsUntilResourcesAtMax ?: 0L) > 0L)
        assertTrue("Fuel consumption schedules an Out of Power ETA", (schedule.secondsUntilPowerOutage ?: 0L) > 0L)
    }

    @Test
    fun dwellersHiring_andMax50Cap_workProperly() {
        val board = CaravanCandidateGenerator.generateBoard(
            totalHiredSoFar = 4,
            commandLevel = 3,
            discountMult = 1.0
        )
        assertTrue("Caravan generates at least 3 candidates", board.size >= 3)
        val candidate = board.first()
        assertTrue("Candidate has valid hiring cost", candidate.goldSigningFee > 0.0)

        // Verify cap calculation (Base 4 + Lv.7 * 4 = 32 -> capped at 30 capacity)
        val livingQuarters = mapOf(BuildingType.SURVIVOR_HAB_BLOCK.id to 7)
        val prodAt30 = OutpostCalculator.computeProductionSnapshot(
            state = OutpostStateEntity(totalSurvivors = 29),
            buildingsMap = livingQuarters,
            workersMap = emptyMap(),
            skillsMap = emptyMap(),
            nowMillis = 1000L
        )
        assertEquals(30, OutpostCalculator.MAX_VAULT_DWELLERS)
        assertEquals(4, BuildingType.SURVIVOR_HAB_BLOCK.populationBonusPerLevel)
        assertEquals("Can hire up to 30 dwellers (+4 per room level)", 30, prodAt30.maxWorkerCapacity)

        val cappedState = OutpostStateEntity(totalSurvivors = 30)
        val beaconCost = OutpostCalculator.computeRescueBeaconCost(
            state = cappedState,
            maxWorkerCapacity = 30,
            skillsMap = emptyMap()
        )
        assertFalse("At 30 Max Dwellers cap, beacon cannot recruit more dwellers", beaconCost.hasHousingSpace)
    }

    @Test
    fun repelRaiderSiege_andDefendTab_operateWithoutCrash() {
        val state = OutpostStateEntity(
            dayCounter = 3,
            commandLevel = 2,
            defenseIntegrityPercent = 100,
            craftedShrapnelMines = 2,
            ammoRounds = 30,
            gold = 50.0,
            scrap = 100.0
        )
        val raidThreat = OutpostCalculator.computeDailyRaidRequiredDefense(
            dayNumber = 3,
            activePhase = DayNightPhase.SCORCHING_DAY
        )
        assertTrue("Day 3 raid defense requirement is calculated", raidThreat > 0)

        val blastDoorCost = OutpostCalculator.computeBlastDoorUpgradeCost(
            state = state,
            skillsMap = emptyMap(),
            workersMap = emptyMap()
        )
        assertEquals(2, blastDoorCost.targetLevel)
        assertTrue("Blast door upgrade time is scaled", blastDoorCost.upgradeTimeSeconds > 0)

        val repairCost = OutpostCalculator.computeDefenseRepairCost(
            state = state.copy(defenseIntegrityPercent = 70, needsDefenseRepair = true),
            skillsMap = emptyMap(),
            buildingsMap = emptyMap()
        )
        assertTrue("Repair is needed when integrity < 100", repairCost.needsRepair)
        assertTrue("Repair costs scrap and gold", repairCost.scrapCost > 0.0 && repairCost.goldCost > 0.0)

        // Verify extreme or corrupted state values (NaN resources, negative door level, out-of-range integrity) never crash
        val corruptedState = state.copy(
            blastDoorLevel = -5,
            defenseIntegrityPercent = -100,
            sentryTurretCount = -10,
            scrap = Double.NaN,
            gold = Double.NEGATIVE_INFINITY
        )
        val safeRepairCost = OutpostCalculator.computeDefenseRepairCost(
            state = corruptedState,
            skillsMap = mapOf("IND_MODULAR_PREFAB" to 999),
            buildingsMap = mapOf(BuildingType.BIO_SLUDGE_REFINERY.id to 999)
        )
        assertTrue("Corrupted state repair cost remains finite and bounded", safeRepairCost.scrapCost.isFinite() && safeRepairCost.goldCost.isFinite())
        assertFalse("NaN/negative resources cannot afford repair", safeRepairCost.canAfford)

        val safeDoorCost = OutpostCalculator.computeBlastDoorUpgradeCost(
            state = corruptedState,
            skillsMap = mapOf("IND_MODULAR_PREFAB" to -50),
            workersMap = emptyMap()
        )
        assertEquals(2, safeDoorCost.targetLevel)
        assertTrue("Corrupted state door upgrade cost remains finite", safeDoorCost.scrapCost.isFinite() && safeDoorCost.goldCost.isFinite())
        assertFalse("NaN/negative resources cannot afford door upgrade", safeDoorCost.canAfford)
    }

    @Test
    fun goldMineGathersGoldAndSmallFuel_andPhoneWidgetSnapshotReflectsVaultStatus() {
        val baseState = OutpostStateEntity(
            totalSurvivors = 2,
            rations = 50.0,
            water = 50.0,
            bioFuel = 25.0,
            bioTurbineLevel = 4,
            salvageRigLevel = 2
        )
        val workers = mapOf(
            WorkerClassType.SCRAPPER.id to WorkerClassEntity(WorkerClassType.SCRAPPER.id, assignedCount = 2, classLevel = 1)
        )
        val snapLv1 = OutpostCalculator.computeProductionSnapshot(
            state = baseState,
            buildingsMap = mapOf(BuildingType.SALVAGE_YARD.id to 1),
            workersMap = workers,
            skillsMap = emptyMap(),
            nowMillis = 1000L
        )
        val snapLv3 = OutpostCalculator.computeProductionSnapshot(
            state = baseState,
            buildingsMap = mapOf(BuildingType.SALVAGE_YARD.id to 3),
            workersMap = workers,
            skillsMap = emptyMap(),
            nowMillis = 1000L
        )

        assertTrue("Gold Mine Lv.1 gathers Gold/s", snapLv1.goldPerSec > 0.0)
        assertTrue("Gold Mine Lv.1 gathers small Bio-Fuel/s", snapLv1.goldMineSmallFuelPerSec > 0.0)
        assertTrue("Upgrading Gold Mine increases Gold/s", snapLv3.goldPerSec > snapLv1.goldPerSec)
        assertTrue("Upgrading Gold Mine increases small Bio-Fuel/s trickle", snapLv3.goldMineSmallFuelPerSec > snapLv1.goldMineSmallFuelPerSec)
        assertTrue("Upgrading Gold Mine increases daily Gold & Fuel bonus", snapLv3.dailyRoomGoldBonus > snapLv1.dailyRoomGoldBonus && snapLv3.dailyRoomFuelBonus > snapLv1.dailyRoomFuelBonus)

        val yieldLv1 = OutpostCalculator.computeSalvageNodeYield(
            nodeType = com.example.model.SalvageNodeType.entries.first(),
            state = baseState,
            buildingsMap = mapOf(BuildingType.SALVAGE_YARD.id to 1),
            workersMap = workers,
            skillsMap = emptyMap(),
            activePhase = DayNightPhase.SCORCHING_DAY
        )
        val yieldLv3 = OutpostCalculator.computeSalvageNodeYield(
            nodeType = com.example.model.SalvageNodeType.entries.first(),
            state = baseState,
            buildingsMap = mapOf(BuildingType.SALVAGE_YARD.id to 3),
            workersMap = workers,
            skillsMap = emptyMap(),
            activePhase = DayNightPhase.SCORCHING_DAY
        )
        assertTrue("Pickaxe swing and vein break gather Gold and small Bio-Fuel", yieldLv1.tapGoldAmount > 0.0 && yieldLv1.tapFuelAmount > 0.0 && yieldLv1.bonusFuelFromGoldMine > 0.0)
        assertTrue("Upgrading Gold Mine increases Pickaxe small Bio-Fuel yield", yieldLv3.tapFuelAmount > yieldLv1.tapFuelAmount && yieldLv3.bonusFuelFromGoldMine > yieldLv1.bonusFuelFromGoldMine)

        val widgetSnap = com.example.widget.VaultStatusAppWidgetProvider.buildWidgetSnapshot(
            baseState.copy(
                gold = 42.0,
                scrap = 88.0,
                totalRaidsRepelled = 3,
                lastAttackSucceeded = true,
                lastAttackSummary = "SUCCEEDED: Repelled Ash Raider Vanguard"
            ),
            snapLv3
        )
        assertEquals(88.0, widgetSnap.scrap, 0.01)
        assertEquals(42.0, widgetSnap.gold, 0.01)
        assertTrue("Phone widget snapshot includes Gold/s rate", widgetSnap.goldPerSec > 0.0)
        assertTrue("Phone widget snapshot shows whether last attack succeeded or failed", widgetSnap.lastAttackSucceeded)
        assertTrue(widgetSnap.lastAttackSummary.contains("SUCCEEDED"))
        assertEquals(2, widgetSnap.totalSurvivors)
        assertEquals(4, widgetSnap.maxWorkerCapacity)
        assertEquals(1, widgetSnap.onlineRoomsCount)
        assertEquals("Widget auto-refresh interval is 10 seconds (10,000 ms)", 10_000L, com.example.widget.VaultStatusAppWidgetProvider.AUTO_REFRESH_INTERVAL_MILLIS)
    }

    @Test
    fun dailyScrapAutoRepairsAutomatedSystemsOnlyDependingOnRoomLevel_andRelicsGrantSmallOutpostBonuses() {
        val intactAutomatedState = OutpostStateEntity(
            commandLevel = 2,
            totalSurvivors = 2,
            scrap = 100.0,
            rations = 50.0,
            water = 50.0,
            bioFuel = 50.0,
            bioTurbineLevel = 5,
            defenseIntegrityPercent = 100,
            needsDefenseRepair = false,
            generatorWearPercent = 0,
            roomStructuralWearPercent = 0,
            activePipeLeaksCount = 0,
            sabotagedBuildingId = ""
        )
        val buildingsLv1 = mapOf(BuildingType.SALVAGE_YARD.id to 1)
        val buildingsLv3 = mapOf(BuildingType.SALVAGE_YARD.id to 3, BuildingType.ATMOSPHERIC_CONDENSER.id to 2)
        val workers = mapOf(
            WorkerClassType.SCRAPPER.id to WorkerClassEntity(WorkerClassType.SCRAPPER.id, assignedCount = 1, classLevel = 1),
            WorkerClassType.HYDRO_ENGINEER.id to WorkerClassEntity(WorkerClassType.HYDRO_ENGINEER.id, assignedCount = 1, classLevel = 1)
        )

        // When no rooms are built (emptyMap) and no automated systems are damaged, 0 Scrap is needed per day
        val noRoomsIntactPlan = OutpostCalculator.computeDailyAutomatedRepairPlan(intactAutomatedState, emptyMap())
        assertFalse("When no rooms are built and systems are intact, no daily Scrap is needed", noRoomsIntactPlan.needsAutomatedRepair)
        assertEquals("0 Scrap is used daily when no rooms are built and systems are intact", 0.0, noRoomsIntactPlan.scrapCost, 0.001)

        // Built rooms ALWAYS require daily Scrap to stay fully operational, and each higher level takes more Scrap per day!
        val intactPlanLv1 = OutpostCalculator.computeDailyAutomatedRepairPlan(intactAutomatedState, buildingsLv1)
        val intactPlanLv3 = OutpostCalculator.computeDailyAutomatedRepairPlan(intactAutomatedState, buildingsLv3)
        val intactSnap = OutpostCalculator.computeProductionSnapshot(intactAutomatedState, buildingsLv3, workers, emptyMap(), 1000L)
        assertTrue("Built rooms require daily Scrap to stay fully operational", intactPlanLv1.needsAutomatedRepair && intactPlanLv3.needsAutomatedRepair)
        assertTrue("Each room level takes more Scrap per day (Lv.3 > Lv.1 > 0)", intactPlanLv3.scrapCost > intactPlanLv1.scrapCost && intactPlanLv1.scrapCost > 0.0)
        assertTrue("Daily room scrap upkeep is positive when rooms are built", intactSnap.dailyRoomScrapUpkeep > 0.0)
        assertFalse("Rooms with Scrap in storage are 100% operational", intactSnap.isRoomsLackingScrap)
        assertEquals(1.0, intactSnap.roomScrapOperationalMultiplier, 0.001)

        // When Scrap runs out (scrap == 0.0), built rooms drop below 100% operational output
        val noScrapSnap = OutpostCalculator.computeProductionSnapshot(intactAutomatedState.copy(scrap = 0.0), buildingsLv3, workers, emptyMap(), 1000L)
        assertTrue("Rooms without Scrap are flagged as lacking operational Scrap", noScrapSnap.isRoomsLackingScrap)
        assertTrue("Rooms without Scrap operate at reduced multiplier (< 1.0)", noScrapSnap.roomScrapOperationalMultiplier < 1.0)
        assertTrue("Rooms with Scrap produce more Scrap/s than rooms lacking Scrap", intactSnap.scrapPerSec > noScrapSnap.scrapPerSec)

        // Verify Water Purifier Room & Hydroponics Room Gating:
        // Without Water Purifier Room (ATMOSPHERIC_CONDENSER Lv.0) and Hydroponics Room (HYDROPONIC_GREENHOUSE Lv.0),
        // automated Water and Food production are 0 (can only get Food and Water manually without the rooms)!
        val noWaterOrFoodRoomsSnap = OutpostCalculator.computeProductionSnapshot(
            intactAutomatedState.copy(craftedSnareTraps = 3, aetherCrossbowLevel = 1),
            mapOf(BuildingType.SALVAGE_YARD.id to 1),
            workers,
            emptyMap(),
            1000L
        )
        assertFalse("Water Purifier Room is not built", noWaterOrFoodRoomsSnap.hasWaterPurifierRoom)
        assertFalse("Hydroponics Room is not built", noWaterOrFoodRoomsSnap.hasHydroponicsRoom)
        assertEquals("Daily room Water bonus is 0 without Water Purifier room", 0.0, noWaterOrFoodRoomsSnap.dailyRoomWaterBonus, 0.001)
        assertEquals("Daily room Food bonus is 0 without Hydroponics room", 0.0, noWaterOrFoodRoomsSnap.dailyRoomFoodBonus, 0.001)
        assertEquals("Passive trap food is 0 without Hydroponics room", 0.0, noWaterOrFoodRoomsSnap.passiveFoodFromTrapsPerSec, 0.001)
        assertTrue("Net Water/s has 0 gross production without Water Purifier room (only dweller upkeep drain)", noWaterOrFoodRoomsSnap.waterPerSec <= 0.0)
        assertTrue("Net Food/s has 0 gross production without Hydroponics room (only dweller upkeep drain)", noWaterOrFoodRoomsSnap.rationsPerSec <= 0.0)

        // Meanwhile, manual Water and Food gathering (Fishing, Hunting, Salvage Nodes) still works without the rooms!
        val manualFish = OutpostCalculator.computeFishingPreview(FishingSpot.CRATER_SHALLOWS_TROUT, intactAutomatedState, emptyMap())
        val manualDewNode = OutpostCalculator.computeSalvageNodeYield(
            SalvageNodeType.DEW_VAPOR_GEYSER, intactAutomatedState, emptyMap(), emptyMap(), emptyMap(), DayNightPhase.SCORCHING_DAY
        )
        assertTrue("Can still get Water and Food manually via Fishing without rooms", manualFish.waterReward > 0.0 && manualFish.foodReward > 0.0)
        assertTrue("Can still get Water and Food manually via Salvage Nodes without rooms", manualDewNode.primaryAmount > 0.0 && manualDewNode.secondaryAmount > 0.0)

        // Building Water Purifier & Hydroponics rooms unlocks automated Water & Food production!
        val withWaterAndFoodRoomsSnap = OutpostCalculator.computeProductionSnapshot(
            intactAutomatedState.copy(craftedSnareTraps = 3, aetherCrossbowLevel = 1),
            mapOf(
                BuildingType.SALVAGE_YARD.id to 1,
                BuildingType.ATMOSPHERIC_CONDENSER.id to 1,
                BuildingType.HYDROPONIC_GREENHOUSE.id to 1
            ),
            workers,
            emptyMap(),
            1000L
        )
        assertTrue("Water Purifier Room is built", withWaterAndFoodRoomsSnap.hasWaterPurifierRoom)
        assertTrue("Hydroponics Room is built", withWaterAndFoodRoomsSnap.hasHydroponicsRoom)
        assertTrue("Automated Water/s is positive once Water Purifier room is built", withWaterAndFoodRoomsSnap.waterPerSec > 0.0 && withWaterAndFoodRoomsSnap.dailyRoomWaterBonus > 0.0)
        assertTrue("Automated Food/s is positive once Hydroponics room is built", withWaterAndFoodRoomsSnap.rationsPerSec > 0.0 && withWaterAndFoodRoomsSnap.dailyRoomFoodBonus > 0.0 && withWaterAndFoodRoomsSnap.passiveFoodFromTrapsPerSec > 0.0)

        val damagedAutomatedState = intactAutomatedState.copy(
            defenseIntegrityPercent = 70,
            needsDefenseRepair = true,
            generatorWearPercent = 35,
            roomStructuralWearPercent = 25,
            activePipeLeaksCount = 1
        )
        val planLv1 = OutpostCalculator.computeDailyAutomatedRepairPlan(damagedAutomatedState, buildingsLv1)
        val planLv3 = OutpostCalculator.computeDailyAutomatedRepairPlan(damagedAutomatedState, buildingsLv3)
        assertTrue("Damaged automated systems trigger daily Scrap auto-repair", planLv1.needsAutomatedRepair && planLv3.needsAutomatedRepair)
        assertTrue("Higher room levels use more Scrap per day to repair automated systems", planLv3.scrapCost > planLv1.scrapCost)
        assertTrue(
            "Higher room levels restore more Door Integrity, Generator Wear, and Room Wear per day",
            planLv3.doorIntegrityRestore > planLv1.doorIntegrityRestore &&
                planLv3.generatorWearReduction > planLv1.generatorWearReduction &&
                planLv3.roomWearReduction > planLv1.roomWearReduction
        )

        // Verify Relics grant small bonuses across the Outpost (+1.5% Output, +2 DEF, +0.3 MW, +15 Storage, +1% Morale, -1% Auto-Repair Cost)
        val stateWith3Relics = damagedAutomatedState.copy(discoveredRelicsCount = 3)
        val snap0Relics = OutpostCalculator.computeProductionSnapshot(damagedAutomatedState, buildingsLv3, workers, emptyMap(), 1000L)
        val snap3Relics = OutpostCalculator.computeProductionSnapshot(stateWith3Relics, buildingsLv3, workers, emptyMap(), 1000L)

        assertEquals(4.5, snap3Relics.relicOutputBonusPercent, 0.01)
        assertEquals(6, snap3Relics.relicDefenseBonus)
        assertEquals(0.9, snap3Relics.relicMegawattBonus, 0.01)
        assertEquals(45.0, snap3Relics.relicStorageBonus, 0.01)
        assertEquals(3, snap3Relics.relicAutoRepairDiscountPercent)
        assertTrue("Relics give a small boost to Scrap/s output", snap3Relics.scrapPerSec > snap0Relics.scrapPerSec)
        assertTrue("Relics give a small boost to Outpost MW generated", snap3Relics.totalMegawattGenerated > snap0Relics.totalMegawattGenerated)
        assertTrue("Relics give a small boost to Outpost Storage Capacity", snap3Relics.storageCap > snap0Relics.storageCap)
        assertTrue("Relics slightly reduce daily auto-repair Scrap cost", snap3Relics.dailyAutoRepairScrapCost < snap0Relics.dailyAutoRepairScrapCost)

        // Verify manual mining Pickaxe upgrades cost 0.0 Scrap at all levels, swinging Pickaxe causes 0 Heat, & Scrap gather rate is lowered by 20% (0.80x)
        val (rigLv1ScrapCost, _) = OutpostCalculator.computeSalvageRigUpgradeCost(1)
        val (rigLv4ScrapCost, _) = OutpostCalculator.computeSalvageRigUpgradeCost(4)
        assertEquals("Manual mining Pickaxe Lv.1->2 upgrade costs 0 Scrap", 0.0, rigLv1ScrapCost, 0.001)
        assertEquals("Manual mining Pickaxe Lv.4->5 upgrade costs 0 Scrap", 0.0, rigLv4ScrapCost, 0.001)
        assertEquals("Swinging Pickaxe causes 0 Heat", 0, OutpostCalculator.PICKAXE_SWING_HEAT_DELTA)
        assertEquals("Swinging Pickaxe does not increase shaft heat", 25, OutpostCalculator.computePickaxeSwingNextHeat(25))
        assertEquals("Scrap gather rate multiplier is lowered by 20% (0.80)", 0.80, OutpostCalculator.SCRAP_GATHER_RATE_MULTIPLIER, 0.001)

        // Verify Wasteland Expeditions reward Dweller Armor & Weapons that add extra Outpost Defense
        val expType = com.example.model.WastelandExpeditionType.SUPERMARKET_SUPPLY_RUN
        assertTrue("Expeditions grant Dweller Armor and Weapons", expType.armorReward >= 1 && expType.weaponsReward >= 1)
        val stateWithExpeditionGear = intactAutomatedState.copy(dwellerArmorCount = 2, dwellerWeaponsCount = 2)
        val snapWithGear = OutpostCalculator.computeProductionSnapshot(stateWithExpeditionGear, buildingsLv3, workers, emptyMap(), 1000L)
        assertEquals("2 Armor (24 DEF) + 2 Weapons (30 DEF) = +54 Extra Defense", 54, snapWithGear.dwellerGearDefenseBonus)
        assertTrue("Dweller Armor and Weapons salvaged from expeditions increase total Outpost Defense", snapWithGear.totalDefenseRating > intactSnap.totalDefenseRating)

        // Verify dedicated Alerts tab destination and active alert counter
        assertEquals("nav_tab_alerts", OutpostDestination.ALERTS.tag)
        val alertCountWithoutRooms = com.example.ui.components.countActiveVaultAlerts(intactAutomatedState, noWaterOrFoodRoomsSnap, null)
        assertTrue("Alerts counter includes missing Water Purifier / Hydroponics room manual-only alert", alertCountWithoutRooms > 0)
    }
}

