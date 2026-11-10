package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.OutpostRepository
import com.example.data.local.EventLogEntity
import com.example.data.local.OutpostStateEntity
import com.example.data.local.WorkerClassEntity
import com.example.domain.OutpostCalculator
import com.example.domain.ProductionSnapshot
import com.example.model.ActiveBuildingConstruction
import com.example.model.ActiveConvoyRun
import com.example.model.ActiveDoorUpgrade
import com.example.model.ActiveDwellerExpedition
import com.example.model.ActiveItemCrafting
import com.example.model.BuildingType
import com.example.model.CaravanCandidateGenerator
import com.example.model.CommandLevelProgression
import com.example.model.CraftableItemType
import com.example.model.DayNightPhase
import com.example.model.FishingSpot
import com.example.model.HireableCandidate
import com.example.model.HuntingTarget
import com.example.model.LevelUpBannerState
import com.example.model.OfflineReport
import com.example.model.PickaxeTier
import com.example.model.PowerStationType
import com.example.model.RaiderSiegeThreat
import com.example.model.RareAnomaly
import com.example.model.RareSyndicateItem
import com.example.model.ResourceType
import com.example.model.SalvageNodeSlot
import com.example.model.SalvageNodeType
import com.example.model.SkillBranch
import com.example.data.remote.PublicSupplyDrop
import com.example.data.remote.PublicVaultProfile
import com.example.data.remote.PublicVaultRepository
import com.example.model.SkillNodeDefinition
import com.example.model.SiegeCountermeasureType
import com.example.model.TradeTown
import com.example.model.VaultMapStation
import com.example.model.VaultOperationalPosture
import com.example.model.WastelandExpeditionType
import com.example.model.WorkerClassType
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

data class OutpostUiState(
    val state: OutpostStateEntity = OutpostStateEntity(),
    val buildingsMap: Map<String, Int> = emptyMap(),
    val workersMap: Map<String, WorkerClassEntity> = emptyMap(),
    val skillsMap: Map<String, Int> = emptyMap(),
    val eventLogs: List<EventLogEntity> = emptyList(),
    val production: ProductionSnapshot = OutpostCalculator.computeProductionSnapshot(
        OutpostStateEntity(),
        emptyMap(),
        emptyMap(),
        emptyMap(),
        System.currentTimeMillis()
    ),
    val salvageNodes: List<SalvageNodeSlot> = emptyList(),
    val hireableCandidates: List<HireableCandidate> = emptyList(),
    val lastSalvageFeedback: String? = null,
    val lastDefenseDamageFeedback: String? = null,
    val lastExpeditionFeedback: String? = null,
    val activeConvoy: ActiveConvoyRun? = null,
    val activeExpedition: ActiveDwellerExpedition? = null,
    val activeConstruction: ActiveBuildingConstruction? = null,
    val activeCrafting: ActiveItemCrafting? = null,
    val activeDoorUpgrade: ActiveDoorUpgrade? = null,
    val activeAnomaly: RareAnomaly? = null,
    val activeRaidThreat: RaiderSiegeThreat? = null,
    val offlineReport: OfflineReport? = null,
    val levelUpBanner: LevelUpBannerState? = null,
    val selectedSkillBranch: SkillBranch = SkillBranch.INDUSTRIAL_SALVAGE,
    val overseerStation: VaultMapStation = VaultMapStation.SURFACE_MINE_SHAFT,
    val isOverseerWalking: Boolean = false,
    val publicVaults: List<PublicVaultProfile> = emptyList(),
    val publicSupplyDrops: List<PublicSupplyDrop> = emptyList(),
    val publicNetworkFeedback: String? = null,
    val nowMillis: Long = System.currentTimeMillis()
)

private data class TransientOverlayState(
    val salvageNodes: List<SalvageNodeSlot>,
    val hireableCandidates: List<HireableCandidate>,
    val lastSalvageFeedback: String? = null,
    val lastDefenseDamageFeedback: String? = null,
    val lastExpeditionFeedback: String? = null,
    val activeConvoy: ActiveConvoyRun? = null,
    val activeExpedition: ActiveDwellerExpedition? = null,
    val activeConstruction: ActiveBuildingConstruction? = null,
    val activeCrafting: ActiveItemCrafting? = null,
    val activeDoorUpgrade: ActiveDoorUpgrade? = null,
    val activeAnomaly: RareAnomaly? = null,
    val activeRaidThreat: RaiderSiegeThreat? = null,
    val offlineReport: OfflineReport? = null,
    val levelUpBanner: LevelUpBannerState? = null,
    val selectedSkillBranch: SkillBranch = SkillBranch.INDUSTRIAL_SALVAGE,
    val overseerStation: VaultMapStation = VaultMapStation.SURFACE_MINE_SHAFT,
    val isOverseerWalking: Boolean = false,
    val publicVaults: List<PublicVaultProfile> = emptyList(),
    val publicSupplyDrops: List<PublicSupplyDrop> = emptyList(),
    val publicNetworkFeedback: String? = null,
    val nowMillis: Long = System.currentTimeMillis()
)

class OutpostViewModel(
    private val repository: OutpostRepository,
    private val publicVaultRepository: PublicVaultRepository? = null
) : ViewModel() {

    private var publicVaultsJob: Job? = null
    private var publicDropsJob: Job? = null
    private var activeSyncedUserId: String? = null

    private val overlayFlow = MutableStateFlow(
        TransientOverlayState(
            salvageNodes = listOf(
                SalvageNodeSlot(0, SalvageNodeType.ARMORED_CONVOY_WRECK, 0, SalvageNodeType.ARMORED_CONVOY_WRECK.maxDurability),
                SalvageNodeSlot(1, SalvageNodeType.DEW_VAPOR_GEYSER, 0, SalvageNodeType.DEW_VAPOR_GEYSER.maxDurability),
                SalvageNodeSlot(2, SalvageNodeType.OVERGROWN_BIO_DOME, 0, SalvageNodeType.OVERGROWN_BIO_DOME.maxDurability)
            ),
            hireableCandidates = CaravanCandidateGenerator.generateBoard(0, 1),
            activeRaidThreat = createDailyRaidThreat(dayNumber = 1, commandLevel = 1, plasmaCount = 0)
        )
    )
    private var offlineCheckCompleted = false
    private var tickCounter = 0

    val uiState: StateFlow<OutpostUiState> = combine(
        repository.outpostStateFlow,
        repository.buildingsFlow,
        repository.workersFlow,
        repository.skillsFlow,
        combine(repository.logsFlow, overlayFlow) { logs, overlay -> logs to overlay }
    ) { stateEntity, buildingsList, workersList, skillsList, (logs, overlay) ->
        val safeState = stateEntity ?: OutpostStateEntity()
        val bMap = buildingsList.associate { it.buildingTypeId to it.level }
        val wMap = workersList.associateBy { it.workerClassId }
        val sMap = skillsList.associate { it.nodeId to it.currentRank }

        val snapshot = OutpostCalculator.computeProductionSnapshot(
            state = safeState,
            buildingsMap = bMap,
            workersMap = wMap,
            skillsMap = sMap,
            nowMillis = overlay.nowMillis
        )

        OutpostUiState(
            state = safeState,
            buildingsMap = bMap,
            workersMap = wMap,
            skillsMap = sMap,
            eventLogs = logs,
            production = snapshot,
            salvageNodes = overlay.salvageNodes,
            hireableCandidates = overlay.hireableCandidates,
            lastSalvageFeedback = overlay.lastSalvageFeedback,
            lastDefenseDamageFeedback = overlay.lastDefenseDamageFeedback,
            lastExpeditionFeedback = overlay.lastExpeditionFeedback,
            activeConvoy = overlay.activeConvoy,
            activeExpedition = overlay.activeExpedition,
            activeConstruction = overlay.activeConstruction,
            activeCrafting = overlay.activeCrafting,
            activeDoorUpgrade = overlay.activeDoorUpgrade,
            activeAnomaly = overlay.activeAnomaly,
            activeRaidThreat = overlay.activeRaidThreat,
            offlineReport = overlay.offlineReport,
            levelUpBanner = overlay.levelUpBanner,
            selectedSkillBranch = overlay.selectedSkillBranch,
            overseerStation = overlay.overseerStation,
            isOverseerWalking = overlay.isOverseerWalking,
            publicVaults = overlay.publicVaults,
            publicSupplyDrops = overlay.publicSupplyDrops,
            publicNetworkFeedback = overlay.publicNetworkFeedback,
            nowMillis = overlay.nowMillis
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = OutpostUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            evaluateOfflineEarningsIfNeeded()
            startSimulationLoop()
        }
    }

    private suspend fun evaluateOfflineEarningsIfNeeded() {
        if (offlineCheckCompleted) return
        offlineCheckCompleted = true

        val savedState = repository.getStateSnapshot() ?: return
        val rankDrills = repository.getSkillSnapshot("WRK_GUILD_DRILLS")?.currentRank ?: 0
        val discountMult = (1.0 - rankDrills * 0.10).coerceAtLeast(0.50)
        overlayFlow.update {
            it.copy(
                hireableCandidates = CaravanCandidateGenerator.generateBoard(
                    savedState.totalSurvivors,
                    savedState.commandLevel,
                    discountMult
                )
            )
        }

        val now = System.currentTimeMillis()
        val elapsedSeconds = ((now - savedState.lastSavedTimestampMillis) / 1000L).coerceIn(0L, 43_200L)

        // Restore or finish persisted active room construction across app restarts!
        var baseSavedState = savedState
        if (savedState.activeConstructionBuildingId.isNotBlank() && savedState.activeConstructionRemainingSeconds > 0) {
            val bType = BuildingType.entries.find { it.id == savedState.activeConstructionBuildingId } ?: BuildingType.SALVAGE_YARD
            val remAfterOffline = (savedState.activeConstructionRemainingSeconds - elapsedSeconds.toInt())
            if (remAfterOffline <= 0) {
                repository.updateBuildingLevel(bType.id, savedState.activeConstructionTargetLevel)
                val buildDoneMsg = "OFFLINE BUILD COMPLETE: ${bType.displayName} reached Lv.${savedState.activeConstructionTargetLevel} while away!"
                repository.logEvent("BUILD COMPLETE", buildDoneMsg)
                baseSavedState = savedState.copy(
                    activeConstructionBuildingId = "",
                    activeConstructionTargetLevel = 0,
                    activeConstructionRemainingSeconds = 0,
                    activeConstructionTotalSeconds = 0,
                    activeConstructionRushGoldCost = 0.0
                )
                overlayFlow.update { it.copy(activeConstruction = null, lastSalvageFeedback = buildDoneMsg) }
            } else {
                baseSavedState = savedState.copy(activeConstructionRemainingSeconds = remAfterOffline)
                overlayFlow.update {
                    it.copy(
                        activeConstruction = ActiveBuildingConstruction(
                            buildingType = bType,
                            targetLevel = savedState.activeConstructionTargetLevel,
                            remainingSeconds = remAfterOffline,
                            totalSeconds = savedState.activeConstructionTotalSeconds.coerceAtLeast(remAfterOffline),
                            rushGoldCost = savedState.activeConstructionRushGoldCost
                        )
                    )
                }
            }
        }

        val currentUi = uiState.value
        val snap = OutpostCalculator.computeProductionSnapshot(
            state = baseSavedState,
            buildingsMap = currentUi.buildingsMap,
            workersMap = currentUi.workersMap,
            skillsMap = currentUi.skillsMap,
            nowMillis = now
        )

        // If 0 workers are hired/assigned, no automatic offline resources are generated!
        if (elapsedSeconds < 20L || baseSavedState.totalSurvivors <= 0 || snap.totalAssignedWorkers <= 0) {
            repository.updateOutpostState(baseSavedState.copy(lastSavedTimestampMillis = now))
            return
        }

        val offlineFactor = 0.65
        val scrapGain = (snap.scrapPerSec * elapsedSeconds * offlineFactor).coerceAtMost(snap.storageCap)
        val waterGain = (snap.waterPerSec * elapsedSeconds * offlineFactor).coerceAtMost(snap.storageCap)
        val rationsGain = (snap.rationsPerSec.coerceAtLeast(0.0) * elapsedSeconds * offlineFactor).coerceAtMost(snap.storageCap)
        val fuelGain = (snap.bioFuelPerSec * elapsedSeconds * offlineFactor).coerceAtMost(snap.storageCap)
        val researchGain = (snap.researchPerSec * elapsedSeconds * offlineFactor).coerceAtMost(snap.storageCap)
        val goldGain = 0.0 // Gold must be salvaged or earned via active trade/convoys

        val cyclesCompleted = (elapsedSeconds / 240L).toInt()
        val autoAnomalies = (elapsedSeconds / 180L).toInt().coerceAtMost(10)
        val coresGain = snap.mutagenCoresPerSec * elapsedSeconds * offlineFactor
        val xpGain = snap.xpPerSec * elapsedSeconds * offlineFactor

        val updatedState = applyXpAndResourceDelta(
            baseState = baseSavedState.copy(lastSavedTimestampMillis = now),
            scrapDelta = scrapGain,
            waterDelta = waterGain,
            rationsDelta = rationsGain,
            fuelDelta = fuelGain,
            researchDelta = researchGain,
            goldDelta = goldGain,
            coresDelta = coresGain,
            xpDelta = xpGain,
            storageCap = snap.storageCap
        )
        val levelsGained = updatedState.commandLevel - savedState.commandLevel
        repository.updateOutpostState(updatedState)

        overlayFlow.update {
            it.copy(
                offlineReport = OfflineReport(
                    elapsedSeconds = elapsedSeconds,
                    cyclesCompleted = cyclesCompleted,
                    scrapEarned = scrapGain,
                    waterEarned = waterGain,
                    rationsEarned = rationsGain,
                    fuelEarned = fuelGain,
                    researchEarned = researchGain,
                    goldEarned = goldGain,
                    mutagenCoresEarned = coresGain,
                    outpostXpEarned = xpGain,
                    levelsGained = levelsGained,
                    anomaliesAutoHarvested = autoAnomalies
                )
            )
        }
    }

    private fun startSimulationLoop() {
        viewModelScope.launch {
            while (isActive) {
                delay(1_000L)
                tickCounter++
                val now = System.currentTimeMillis()
                val current = uiState.value
                val state = current.state
                if (state.isGameOver) {
                    overlayFlow.update { it.copy(nowMillis = now) }
                    continue
                }
                val snap = current.production

                val prevProgress = state.cycleProgress
                val nextProgressRaw = prevProgress + (1f / 90f) // 90s per nuclear survival day
                val wrapped = nextProgressRaw >= 1f
                val nextProgress = if (wrapped) nextProgressRaw - 1f else nextProgressRaw
                val nextDayCounter = if (wrapped) state.dayCounter + 1 else state.dayCounter

                val prevPhase = DayNightPhase.fromCycleProgress(prevProgress)
                val nextPhase = DayNightPhase.fromCycleProgress(nextProgress)

                if (prevPhase != nextPhase) {
                    repository.logEvent("CYCLE", "Sky shifted to ${nextPhase.displayName}: ${nextPhase.tagline}")
                    if (state.autoShiftEnabled && state.totalSurvivors > 0) {
                        autoRebalanceWorkersForPhase(nextPhase, state.totalSurvivors, current.workersMap)
                    }
                    refreshNodesForPhaseTransition(nextPhase)
                    // Night Time is much more dangerous: entering RAD_NIGHT spawns a Nocturnal Mutant Swarm Assault!
                    if (nextPhase == DayNightPhase.RAD_NIGHT && overlayFlow.value.activeRaidThreat == null) {
                        val nightSwarm = createNightMutantSwarmThreat(
                            dayNumber = nextDayCounter,
                            commandLevel = state.commandLevel,
                            plasmaCount = state.plasmaCannonCount
                        )
                        overlayFlow.update {
                            it.copy(
                                activeRaidThreat = nightSwarm,
                                lastDefenseDamageFeedback = "NIGHT DANGER ALERT: ${nightSwarm.title} emerging from the radioactive dark (Req ${nightSwarm.requiredDefenseRating} DEF)!"
                            )
                        }
                        repository.logEvent("NIGHT SWARM", "${nightSwarm.title} attacked the Vault at nightfall!")
                    }
                }

                // 60-Second Dweller Food & Water Feeding Cycle + 2-Minute (120s) Appetite Escalation
                var fiveMinFoodDeduct = 0.0
                var fiveMinWaterDeduct = 0.0
                val clampedCurrentFeedSecs = state.dwellerFeedCycleSecondsRemaining.coerceIn(1, OutpostCalculator.DWELLER_FEED_INTERVAL_SECONDS)
                var nextFeedSeconds = clampedCurrentFeedSecs
                var nextEscalationSeconds = state.dwellerAppetiteEscalationSeconds
                if (state.totalSurvivors > 0) {
                    nextEscalationSeconds += 1
                    if (nextEscalationSeconds % OutpostCalculator.DWELLER_ESCALATION_INTERVAL_SECONDS == 0) {
                        val newTier = nextEscalationSeconds / OutpostCalculator.DWELLER_ESCALATION_INTERVAL_SECONDS
                        val pctStep = if (nextDayCounter <= 10) 8 else 14
                        repository.logEvent(
                            "CREW APPETITE",
                            "2-Minute Dweller Appetite Escalation (Tier $newTier): Vault Dwellers now require +${newTier * pctStep}% more Food & Water!"
                        )
                    }
                    if (clampedCurrentFeedSecs <= 1) {
                        fiveMinFoodDeduct = snap.fiveMinDwellerFoodCost
                        fiveMinWaterDeduct = snap.fiveMinDwellerWaterCost
                        nextFeedSeconds = OutpostCalculator.DWELLER_FEED_INTERVAL_SECONDS
                        if (state.rations >= fiveMinFoodDeduct && state.water >= fiveMinWaterDeduct && nextEscalationSeconds >= OutpostCalculator.DWELLER_ESCALATION_INTERVAL_SECONDS) {
                            nextEscalationSeconds = (nextEscalationSeconds - OutpostCalculator.DWELLER_ESCALATION_INTERVAL_SECONDS).coerceAtLeast(0)
                        }
                        repository.logEvent(
                            "60S FEEDING",
                            "60-Second Dweller Feeding Cycle: Crew consumed -${fiveMinFoodDeduct.roundToInt()} Food & -${fiveMinWaterDeduct.roundToInt()} Water!"
                        )
                    } else {
                        nextFeedSeconds = clampedCurrentFeedSecs - 1
                    }
                }

                // Daily Upkeep & Powered Vault Room Daily Bonuses + Escalating Room Upkeep on New Day
                var newDayFoodDeduct = 0.0
                var newDayWaterDeduct = snap.dailyRoomWaterUpkeep
                var newDayScrapDeduct = snap.dailyRoomScrapUpkeep
                var newDayFuelDeduct = snap.dailyRoomFuelUpkeep
                var newDayRoomScrapBonus = 0.0
                var newDayRoomWaterBonus = 0.0
                var newDayRoomFoodBonus = 0.0
                if (wrapped) {
                    if (!snap.isVaultBlackout) {
                        newDayRoomScrapBonus = snap.dailyRoomScrapBonus
                        newDayRoomWaterBonus = snap.dailyRoomWaterBonus
                        newDayRoomFoodBonus = snap.dailyRoomFoodBonus
                    }
                    if (state.totalSurvivors > 0) {
                        newDayFoodDeduct = snap.dailyFoodUpkeepPerDay
                        newDayWaterDeduct += snap.dailyWaterUpkeepPerDay
                    }
                    if (state.totalSurvivors > 0 || snap.totalBuiltRoomsCount > 0) {
                        repository.logEvent(
                            "NEW DAY",
                            "Day $nextDayCounter dawned: Crew & Lv Rooms consumed -${newDayFoodDeduct.roundToInt()} Food, -${newDayWaterDeduct.roundToInt()} Water, -${newDayScrapDeduct.roundToInt()} Scrap & -${newDayFuelDeduct.roundToInt()} Fuel."
                        )
                    }
                } else {
                    newDayWaterDeduct = 0.0
                    newDayScrapDeduct = 0.0
                    newDayFuelDeduct = 0.0
                }

                // Tick Active Vault Room Construction Timer
                val currentBuild = overlayFlow.value.activeConstruction
                var buildCompleteXpAward = 0.0
                var nextConstructId = state.activeConstructionBuildingId
                var nextConstructTargetLv = state.activeConstructionTargetLevel
                var nextConstructRemSecs = state.activeConstructionRemainingSeconds
                var nextConstructTotalSecs = state.activeConstructionTotalSeconds
                var nextConstructRushCost = state.activeConstructionRushGoldCost
                if (currentBuild != null) {
                    if (currentBuild.remainingSeconds <= 1) {
                        repository.updateBuildingLevel(currentBuild.buildingType.id, currentBuild.targetLevel)
                        buildCompleteXpAward = 26.0 + currentBuild.targetLevel * 14.0
                        val doneMsg = "CONSTRUCTION COMPLETE: ${currentBuild.buildingType.displayName} reached Lv.${currentBuild.targetLevel} (+${buildCompleteXpAward.toInt()} XP)!"
                        nextConstructId = ""
                        nextConstructTargetLv = 0
                        nextConstructRemSecs = 0
                        nextConstructTotalSecs = 0
                        nextConstructRushCost = 0.0
                        overlayFlow.update {
                            it.copy(
                                activeConstruction = null,
                                lastSalvageFeedback = doneMsg
                            )
                        }
                        repository.logEvent("BUILD COMPLETE", doneMsg)
                    } else {
                        val nextRem = currentBuild.remainingSeconds - 1
                        nextConstructId = currentBuild.buildingType.id
                        nextConstructTargetLv = currentBuild.targetLevel
                        nextConstructRemSecs = nextRem
                        nextConstructTotalSecs = currentBuild.totalSeconds
                        nextConstructRushCost = currentBuild.rushGoldCost
                        overlayFlow.update {
                            it.copy(
                                activeConstruction = currentBuild.copy(
                                    remainingSeconds = nextRem
                                )
                            )
                        }
                    }
                }

                // Tick Active Armory Item Crafting Timer
                val currentCraft = overlayFlow.value.activeCrafting
                var craftCompleteXpAward = 0.0
                var craftedSpearsDelta = 0
                var craftedTrapsDelta = 0
                var craftedCrossbowDelta = 0
                var craftedMinesDelta = 0
                var craftedTurretsDelta = 0
                var craftedPlasmaDelta = 0
                var craftedAmmoDelta = 0
                var craftedGasMasksDelta = 0
                var craftedHazmatDelta = 0
                var craftedMedkitsDelta = 0
                var craftedRadAwayDelta = 0
                if (currentCraft != null) {
                    if (currentCraft.remainingSeconds <= 1) {
                        val cItem = currentCraft.item
                        craftCompleteXpAward = 18.0
                        when (cItem) {
                            CraftableItemType.HARPOON_SPEAR -> craftedSpearsDelta = 2
                            CraftableItemType.SNARE_TRAP -> craftedTrapsDelta = 1
                            CraftableItemType.AETHER_CROSSBOW -> craftedCrossbowDelta = 1
                            CraftableItemType.SHRAPNEL_MINE -> craftedMinesDelta = 2
                            CraftableItemType.AUTO_SENTRY_TURRET -> craftedTurretsDelta = 1
                            CraftableItemType.THORIUM_PLASMA_CANNON -> craftedPlasmaDelta = 1
                            CraftableItemType.BALLISTIC_AMMO_CRATE -> craftedAmmoDelta = 30
                            CraftableItemType.GAS_MASK -> craftedGasMasksDelta = 1
                            CraftableItemType.HAZMAT_SUIT -> craftedHazmatDelta = 1
                            CraftableItemType.MEDKIT_RADAWAY_PACK -> {
                                craftedMedkitsDelta = 1
                                craftedRadAwayDelta = 1
                            }
                        }
                        val craftDoneMsg = "CRAFT COMPLETE: ${cItem.title} ready in Armory (${cItem.effectSummary} • +18 XP)!"
                        overlayFlow.update {
                            it.copy(
                                activeCrafting = null,
                                lastDefenseDamageFeedback = craftDoneMsg
                            )
                        }
                        repository.logEvent("CRAFT COMPLETE", craftDoneMsg)
                    } else {
                        overlayFlow.update {
                            it.copy(
                                activeCrafting = currentCraft.copy(
                                    remainingSeconds = currentCraft.remainingSeconds - 1
                                )
                            )
                        }
                    }
                }

                // Tick Active Vault Blast Door Upgrade Timer
                val currentDoorUpgrade = overlayFlow.value.activeDoorUpgrade
                var doorUpgradeCompleteXpAward = 0.0
                var newBlastDoorLevelFromTimer: Int? = null
                if (currentDoorUpgrade != null) {
                    if (currentDoorUpgrade.remainingSeconds <= 1) {
                        newBlastDoorLevelFromTimer = currentDoorUpgrade.targetLevel
                        doorUpgradeCompleteXpAward = 40.0 * currentDoorUpgrade.targetLevel
                        val doorDoneMsg = "BLAST DOOR UPGRADE COMPLETE: Reinforced Vault Blast Door reached Lv.${currentDoorUpgrade.targetLevel} (+20 Base Defense & 100% Integrity)!"
                        overlayFlow.update {
                            it.copy(
                                activeDoorUpgrade = null,
                                lastDefenseDamageFeedback = doorDoneMsg
                            )
                        }
                        repository.logEvent("BLAST DOOR", doorDoneMsg)
                    } else {
                        overlayFlow.update {
                            it.copy(
                                activeDoorUpgrade = currentDoorUpgrade.copy(
                                    remainingSeconds = currentDoorUpgrade.remainingSeconds - 1
                                )
                            )
                        }
                    }
                }

                // Gentle decay of manual salvage combo every 10s
                val nextCombo = if (tickCounter % 10 == 0 && state.salvageComboCount > 0) {
                    state.salvageComboCount - 1
                } else state.salvageComboCount

                // Scrappers automatically dismantle Raw Salvage Caches ONLY when hired & assigned & Vault is powered!
                val scrapperEntity = current.workersMap[WorkerClassType.SCRAPPER.id]
                val hiredScrappers = scrapperEntity?.assignedCount ?: 0
                val autoDismantleInterval = if ((scrapperEntity?.classLevel ?: 1) >= 20) 2 else 4
                var cachesBrokenThisTick = 0
                var cacheScrapBonus = 0.0
                var cacheWaterBonus = 0.0
                var cacheRationsBonus = 0.0
                if (!snap.isVaultBlackout && hiredScrappers > 0 && state.rawSalvageCaches > 0 && tickCounter % autoDismantleInterval == 0) {
                    cachesBrokenThisTick = minOf(state.rawSalvageCaches, 1 + hiredScrappers / 3)
                    val yardLv = current.buildingsMap[BuildingType.SALVAGE_YARD.id] ?: 0
                    val storeLv = current.buildingsMap[BuildingType.VAULT_STORE_ROOM.id] ?: 0
                    val mult = (1.0 + yardLv * 0.25 + (state.salvageRigLevel - 1) * 0.25) * (1.0 + storeLv * 0.10)
                    cacheScrapBonus = cachesBrokenThisTick * 5.0 * mult
                    cacheWaterBonus = cachesBrokenThisTick * 3.0 * mult
                    cacheRationsBonus = cachesBrokenThisTick * 3.0 * mult
                }

                // Tick Active Convoy Run
                val currentConvoy = overlayFlow.value.activeConvoy
                var convoyGoldAward = 0.0
                var convoyThoriumAward = 0.0
                var convoyAetherAward = 0.0
                var convoyCoresAward = 0.0
                var convoyXpAward = 0.0
                if (currentConvoy != null) {
                    if (currentConvoy.remainingSeconds <= 1) {
                        convoyGoldAward = currentConvoy.expectedGoldReward
                        convoyXpAward = currentConvoy.expectedXpReward
                        when (currentConvoy.expectedRareResource) {
                            ResourceType.THORIUM_RODS -> convoyThoriumAward = currentConvoy.expectedRareAmount
                            ResourceType.AETHER_CELLS -> convoyAetherAward = currentConvoy.expectedRareAmount
                            ResourceType.MUTAGEN_CORES -> convoyCoresAward = currentConvoy.expectedRareAmount
                            else -> {}
                        }
                        overlayFlow.update { it.copy(activeConvoy = null) }
                        repository.logEvent(
                            "CONVOY",
                            "Convoy returned from ${currentConvoy.destination.displayName} with +${convoyGoldAward.toInt()} Gold & +${currentConvoy.expectedRareAmount.toInt()} ${currentConvoy.expectedRareResource.shortLabel}!"
                        )
                    } else {
                        overlayFlow.update {
                            it.copy(activeConvoy = currentConvoy.copy(remainingSeconds = currentConvoy.remainingSeconds - 1))
                        }
                    }
                }

                // Tick Active Dweller Wasteland Expedition (Radar Array + Gas Mask/Hazmat Suit reduces Death Chance & Ammo boosts loot!)
                val currentExpedition = overlayFlow.value.activeExpedition
                var expGoldAward = 0.0
                var expFoodAward = 0.0
                var expWaterAward = 0.0
                var expScrapAward = 0.0
                var expFuelAward = 0.0
                var expCoresAward = 0.0
                var expXpAward = 0.0
                var expSurvivorDelta = 0
                var expLostIncrement = 0
                var expCompletedIncrement = 0
                var expAmmoUsed = 0
                var expGasMaskUsed = 0
                var expHazmatUsed = 0
                var expRadiationDelta = 0
                if (currentExpedition != null) {
                    if (currentExpedition.remainingSeconds <= 1) {
                        val expType = currentExpedition.type
                        val effectiveDeathChance = OutpostCalculator.computeEffectiveExpeditionDeathChance(
                            expType,
                            current.buildingsMap,
                            state
                        )
                        val radarLv = current.buildingsMap[BuildingType.SIGNAL_RESEARCH_ARRAY.id] ?: 0
                        val hasAmmoSupport = state.ammoRounds >= 6
                        if (hasAmmoSupport) expAmmoUsed = 6
                        if (state.hazmatSuitsCount > 0) {
                            expHazmatUsed = 1
                            expRadiationDelta = 3
                        } else if (state.gasMasksCount > 0) {
                            expGasMaskUsed = 1
                            expRadiationDelta = 7
                        } else {
                            expRadiationDelta = 16
                        }
                        val ammoLootBonus = if (hasAmmoSupport) 1.25 else 1.0
                        val radarLootMult = (1.0 + radarLv * 0.20) * snap.uniqueDwellerBonusMultiplier * ammoLootBonus
                        val died = Random.nextInt(100) < effectiveDeathChance
                        if (died && state.totalSurvivors > 0) {
                            expSurvivorDelta = -1
                            expLostIncrement = 1
                            removeOneAssignedWorker(current.workersMap)
                            val deathMsg = "TRAGEDY ($effectiveDeathChance% DEATH ROLL): A Dweller died in the nuclear wastes on ${expType.title}! (${(state.totalSurvivors - 1).coerceAtLeast(0)} Dwellers left)"
                            overlayFlow.update { it.copy(activeExpedition = null, lastExpeditionFeedback = deathMsg) }
                            repository.logEvent("EXPEDITION DEATH", deathMsg)
                        } else {
                            expGoldAward = (expType.goldReward * radarLootMult).roundToInt().toDouble()
                            expFoodAward = (expType.foodReward * radarLootMult).roundToInt().toDouble()
                            expWaterAward = (expType.waterReward * radarLootMult).roundToInt().toDouble()
                            expScrapAward = (expType.scrapReward * radarLootMult).roundToInt().toDouble()
                            expFuelAward = (expType.fuelReward * radarLootMult).roundToInt().toDouble()
                            expCoresAward = expType.coresReward
                            expXpAward = (expType.xpReward * radarLootMult).roundToInt().toDouble()
                            expCompletedIncrement = 1
                            val gearNote = when {
                                expHazmatUsed > 0 -> " [Hazmat Protected]"
                                expGasMaskUsed > 0 -> " [Gas Mask Protected]"
                                else -> ""
                            }
                            val winMsg = "EXPEDITION SURVIVED (${100 - effectiveDeathChance}%)$gearNote: Dweller returned from ${expType.title} with +${expGoldAward.toInt()} Gold, +${expFoodAward.toInt()} Food & +${expWaterAward.toInt()} Water!"
                            overlayFlow.update { it.copy(activeExpedition = null, lastExpeditionFeedback = winMsg) }
                            repository.logEvent("EXPEDITION", winMsg)
                        }
                    } else {
                        overlayFlow.update {
                            it.copy(activeExpedition = currentExpedition.copy(remainingSeconds = currentExpedition.remainingSeconds - 1))
                        }
                    }
                }

                // Tick Daily Base Attack, Dangerous Night Swarms, Automated Defenses & Auto-Repair
                var raidGoldDelta = 0.0
                var raidScrapDelta = 0.0
                var raidXpDelta = 0.0
                var raidCoresDelta = 0.0
                var raidRepelledIncrement = 0
                var raidAmmoConsumed = 0
                var raidMinesConsumed = 0
                var raidInjuryIncrement = 0
                var raidRadSpike = 0
                var nextIntegrity = state.defenseIntegrityPercent
                var nextNeedsRepair = state.needsDefenseRepair
                var nextLuckyNoRepair = state.luckyNoRepairCount
                var nextLastAttackDay = state.lastAttackDayResolved

                // Automated Defenses strictly require: 1) Built Turrets/Plasma, 2) Assigned Workers, 3) Vault Power (no Blackout)
                val hasActiveAutoDefenseGrid = snap.isAutoDefenseActive

                val currentRaid = overlayFlow.value.activeRaidThreat
                if (currentRaid != null) {
                    val autoInterceptReady = hasActiveAutoDefenseGrid &&
                        currentRaid.remainingSeconds <= 42 &&
                        (snap.totalDefenseRating >= currentRaid.requiredDefenseRating || state.craftedShrapnelMines >= 1)

                    if (autoInterceptReady || currentRaid.remainingSeconds <= 1 || wrapped) {
                        val wonByDefense = snap.totalDefenseRating >= currentRaid.requiredDefenseRating
                        val wonByAutoMine = !wonByDefense && autoInterceptReady && state.craftedShrapnelMines >= 1
                        val wonDefense = wonByDefense || wonByAutoMine
                        if (wonByAutoMine) raidMinesConsumed = 1
                        val dayAmmoSurge = (currentRaid.dayNumber - 1) / 2
                        raidAmmoConsumed = minOf(state.ammoRounds, state.sentryTurretCount * 4 + state.plasmaCannonCount * 6 + dayAmmoSurge)
                        if (wonDefense) {
                            raidGoldDelta = currentRaid.bountyGoldReward
                            raidScrapDelta = currentRaid.bountyScrapReward
                            raidXpDelta = currentRaid.bountyXpReward
                            if (currentRaid.isNightMutantSwarm) raidCoresDelta = 1.0
                            raidRepelledIncrement = 1
                        } else {
                            // Breach losses scale up as days go by!
                            val daySeverityBonus = ((currentRaid.dayNumber - 1) * 0.015).coerceAtMost(0.25)
                            val lossMult = (if (currentRaid.isNightMutantSwarm) 0.22 else 0.15) + daySeverityBonus
                            val maxScrapLoss = 35.0 + currentRaid.dayNumber * 5.0
                            val maxGoldLoss = 22.0 + currentRaid.dayNumber * 3.5
                            raidScrapDelta = -minOf(state.scrap * lossMult, maxScrapLoss)
                            raidGoldDelta = -minOf(state.gold * (0.15 + daySeverityBonus * 0.75), maxGoldLoss)
                            if (state.totalSurvivors > 0) raidInjuryIncrement = 1 + (currentRaid.dayNumber / 7).coerceAtMost(2)
                            if (currentRaid.isNightMutantSwarm) raidRadSpike = 18 + (currentRaid.dayNumber * 2).coerceAtMost(30)
                        }

                        // Automated Turret Interception increases Flawless Deflection chance from 15% to 35% and reduces door damage!
                        val deflectionChance = if (autoInterceptReady && state.ammoRounds >= 4) 35 else 15
                        val luckyNoRepair = Random.nextInt(100) < deflectionChance
                        val damageNote: String
                        if (luckyNoRepair) {
                            nextLuckyNoRepair += 1
                            damageNote = "FLAWLESS DEFLECTION ($deflectionChance% CHANCE)! Vault Door & Defenses took 0% damage!"
                        } else {
                            val dayDamageAdder = ((currentRaid.dayNumber - 1) * 1.8).roundToInt().coerceAtMost(35)
                            val doorMitigation = ((state.blastDoorLevel - 1) * 2).coerceAtMost(28)
                            val rawBaseDmg = when {
                                wonDefense && autoInterceptReady -> Random.nextInt(8, 17) + dayDamageAdder / 2
                                wonDefense -> Random.nextInt(18, 28) + dayDamageAdder
                                currentRaid.isNightMutantSwarm -> Random.nextInt(28, 42) + dayDamageAdder
                                else -> Random.nextInt(25, 38) + dayDamageAdder
                            }
                            val baseDmg = (rawBaseDmg - doorMitigation).coerceIn(6, 75)
                            nextIntegrity = (state.defenseIntegrityPercent - baseDmg).coerceAtLeast(20)
                            nextNeedsRepair = true
                            damageNote = "Door Integrity -$baseDmg% (Day ${currentRaid.dayNumber} Siege Force • Now $nextIntegrity%)."
                        }
                        if (!currentRaid.isNightMutantSwarm) {
                            nextLastAttackDay = currentRaid.dayNumber
                        }

                        val interceptPrefix = if (autoInterceptReady) "AUTO-DEFENSE INTERCEPTED: " else ""
                        val coreBonusNote = if (raidCoresDelta > 0) ", +1 Mutagen Core" else ""
                        val outcomeSummary = if (wonDefense) {
                            "${interceptPrefix}Repelled ${currentRaid.title} (+${raidGoldDelta.toInt()} Gold, +${raidScrapDelta.toInt()} Scrap$coreBonusNote)! $damageNote"
                        } else {
                            "${currentRaid.title} breached Vault Door (${snap.totalDefenseRating}/${currentRaid.requiredDefenseRating} DEF)! $damageNote"
                        }
                        repository.logEvent("DAILY DEFENSE", outcomeSummary)
                        overlayFlow.update {
                            it.copy(
                                activeRaidThreat = if (wrapped) {
                                    createDailyRaidThreat(nextDayCounter, state.commandLevel, state.plasmaCannonCount)
                                } else null,
                                lastDefenseDamageFeedback = outcomeSummary
                            )
                        }
                    } else {
                        overlayFlow.update {
                            it.copy(activeRaidThreat = currentRaid.copy(remainingSeconds = currentRaid.remainingSeconds - 1))
                        }
                    }
                } else if (wrapped || nextLastAttackDay < nextDayCounter) {
                    overlayFlow.update {
                        it.copy(
                            activeRaidThreat = createDailyRaidThreat(
                                dayNumber = nextDayCounter,
                                commandLevel = state.commandLevel,
                                plasmaCount = state.plasmaCannonCount
                            )
                        )
                    }
                }

                // Automated Perimeter & Blast Door Repair (strictly requires Built Auto-Defenses + Assigned Workers + Power!)
                var autoRepairScrapUsed = 0.0
                if (tickCounter % 4 == 0 &&
                    hasActiveAutoDefenseGrid &&
                    (nextNeedsRepair || nextIntegrity < 100) &&
                    state.scrap >= 3.0
                ) {
                    autoRepairScrapUsed = 3.0
                    nextIntegrity = (nextIntegrity + 12 + hiredScrappers * 3).coerceAtMost(100)
                    if (nextIntegrity >= 100) {
                        nextNeedsRepair = false
                        overlayFlow.update {
                            it.copy(
                                lastDefenseDamageFeedback = "AUTO-REPAIR COMPLETE: Crew & Defense Grid restored Vault Blast Door to 100% Integrity!"
                            )
                        }
                    }
                }

                // Realistic Shelter Life-Support (O2, Radiation, Dweller Health, Generator Wear & Hardcore Hazards)
                var nextOxygen = state.oxygenPercent
                var nextRadiation = (state.radiationMillisieverts + expRadiationDelta + raidRadSpike).coerceIn(0, 1000)
                var nextHealth = state.dwellerHealthPercent
                var nextInjured = (state.injuredDwellersCount + raidInjuryIncrement).coerceIn(0, state.totalSurvivors.coerceAtLeast(1))
                var nextGeneratorWear = state.generatorWearPercent
                var nextShaftHeat = state.miningShaftHeatPercent
                var nextRoomWear = state.roomStructuralWearPercent
                var nextPipeLeaks = state.activePipeLeaksCount

                if (tickCounter % 3 == 0) {
                    nextOxygen = (state.oxygenPercent + snap.oxygenDeltaPerSec.roundToInt()).coerceIn(15, 100)
                    nextRadiation = (nextRadiation + snap.radiationDeltaPerSec.roundToInt()).coerceIn(0, 1000)
                    nextShaftHeat = (state.miningShaftHeatPercent - 2).coerceAtLeast(0)

                    val medbayLv = current.buildingsMap[BuildingType.REINFORCED_SILO.id] ?: 0
                    val hydroCount = current.workersMap[WorkerClassType.HYDRO_ENGINEER.id]?.assignedCount ?: 0
                    val starvingOrDehydrated = state.totalSurvivors > 0 && (state.rations < 1.0 || state.water < 1.0)
                    val hypoxicOrIrradiated = nextOxygen < 40 || nextRadiation > 150
                    val hasUntreatedBeastTrauma = state.bleedingDwellersCount > 0 || state.venomInfectedDwellersCount > 0

                    if (starvingOrDehydrated || hypoxicOrIrradiated || hasUntreatedBeastTrauma) {
                        val traumaPenalty = (state.bleedingDwellersCount + state.venomInfectedDwellersCount).coerceAtMost(3)
                        nextHealth = (state.dwellerHealthPercent - 2 - traumaPenalty).coerceAtLeast(15)
                    } else if (!snap.isVaultBlackout && (medbayLv > 0 || hydroCount > 0)) {
                        nextHealth = (state.dwellerHealthPercent + 2 + medbayLv).coerceAtMost(100)
                        if (nextHealth >= 85 && nextInjured > 0) {
                            nextInjured = (nextInjured - 1).coerceAtLeast(0)
                        }
                    }
                }

                if (tickCounter % 8 == 0) {
                    val hasRunningGenerators = state.bioTurbineLevel > 0 || state.solarTowerLevel > 0 || state.thoriumReactorLevel > 0
                    if (hiredScrappers > 0 && !snap.isVaultBlackout) {
                        // Assigned Scrappers automatically maintain & tune Vault Generators!
                        nextGeneratorWear = (state.generatorWearPercent - hiredScrappers * 2).coerceAtLeast(0)
                    } else if (hasRunningGenerators) {
                        nextGeneratorWear = (state.generatorWearPercent + 1).coerceAtMost(80)
                    }
                }

                if (tickCounter % 12 == 0 && snap.totalBuiltRoomsCount > 0) {
                    if (hiredScrappers >= 2 && !snap.isVaultBlackout) {
                        nextRoomWear = (state.roomStructuralWearPercent - 1).coerceAtLeast(0)
                    } else {
                        nextRoomWear = (state.roomStructuralWearPercent + 1 + (snap.totalBuiltRoomsCount / 4)).coerceAtMost(80)
                        if (nextRoomWear >= 30 && nextPipeLeaks < 4 && Random.nextInt(100) < 22) {
                            nextPipeLeaks = (nextPipeLeaks + 1).coerceAtMost(4)
                            val leakWarn = "PIPE LEAK DETECTED: Vault Room Structural Wear (${nextRoomWear}%) ruptured a coolant pipe ($nextPipeLeaks active leak(s) draining Water & Scrap)!"
                            overlayFlow.update { it.copy(lastSalvageFeedback = leakWarn) }
                            repository.logEvent("PIPE LEAK", leakWarn)
                        }
                    }
                }

                val refineryLv = current.buildingsMap[BuildingType.BIO_SLUDGE_REFINERY.id] ?: 0
                val newDayAmmoFab = if (wrapped && !snap.isVaultBlackout) {
                    refineryLv * 12
                } else if (tickCounter % 6 == 0 && !snap.isVaultBlackout && refineryLv > 0) {
                    refineryLv * 2
                } else 0

                // Track 2-Day Resource Deficit Shutdown & 6-Day Power Blackout Game Over Clocks
                val survivalEval = if (wrapped) {
                    OutpostCalculator.evaluateSurvivalEndOfDay(state, snap)
                } else {
                    null
                }
                val nextDeficitDays = when {
                    !snap.isVaultResourceDeficit -> 0
                    survivalEval != null -> survivalEval.nextDeficitDaysCount
                    else -> state.deficitDaysCount
                }
                val nextBlackoutDays = when {
                    !snap.isVaultBlackout -> 0
                    survivalEval != null -> survivalEval.nextBlackoutDaysCount
                    else -> state.blackoutDaysCount
                }
                val nextDeficitSeconds = if (snap.isVaultResourceDeficit) state.deficitSecondsAccumulated + 1 else 0
                val nextBlackoutSeconds = if (snap.isVaultBlackout) state.blackoutSecondsAccumulated + 1 else 0
                val nextIsGameOver = survivalEval?.isGameOver ?: false
                val nextGameOverReason = survivalEval?.gameOverReason ?: ""

                if (wrapped && nextDeficitDays == 1 && !nextIsGameOver) {
                    val warn = "CRITICAL DEFICIT (DAY 1/2): Vault takes more resources to run than it gains! Vault Defenses are SHUT DOWN — restore resource generation within 1 Day or GAME OVER!"
                    overlayFlow.update { it.copy(lastDefenseDamageFeedback = warn) }
                    repository.logEvent("DEFICIT SHUTDOWN", warn)
                }
                if (wrapped && nextBlackoutDays in 1 until OutpostCalculator.MAX_BLACKOUT_DAYS_BEFORE_GAME_OVER && !nextIsGameOver) {
                    val rem = OutpostCalculator.MAX_BLACKOUT_DAYS_BEFORE_GAME_OVER - nextBlackoutDays
                    val warn = "POWER BLACKOUT (DAY $nextBlackoutDays/6): Restore Vault Power within $rem Day(s) or total life-support failure ends the game!"
                    overlayFlow.update { it.copy(lastSalvageFeedback = warn) }
                    repository.logEvent("BLACKOUT CLOCK", warn)
                }
                if (nextIsGameOver) {
                    repository.logEvent("GAME OVER", nextGameOverReason)
                }

                val isOverdriveStillActive = state.thoriumOverdriveUntilMillis > now
                val nextActiveCrankStack = if (isOverdriveStillActive) state.activeDynamoCrankStack else 0
                // Reset manual cranks to 4 whenever permanent (non-crank) Vault Power is online so every new power outage has 4 cranks!
                val nextManualDynamoCranks = if (!snap.isVaultBlackout && !isOverdriveStillActive) {
                    OutpostCalculator.MAX_DYNAMO_CRANKS_PER_BLACKOUT
                } else {
                    state.manualDynamoCranksRemaining
                }

                var updatedState = applyXpAndResourceDelta(
                    baseState = state.copy(
                        totalSurvivors = (state.totalSurvivors + expSurvivorDelta).coerceAtLeast(0),
                        totalDwellersLost = state.totalDwellersLost + expLostIncrement,
                        totalExpeditionsCompleted = state.totalExpeditionsCompleted + expCompletedIncrement,
                        blastDoorLevel = newBlastDoorLevelFromTimer ?: state.blastDoorLevel,
                        defenseIntegrityPercent = if (newBlastDoorLevelFromTimer != null) 100 else nextIntegrity,
                        needsDefenseRepair = if (newBlastDoorLevelFromTimer != null) false else nextNeedsRepair,
                        luckyNoRepairCount = nextLuckyNoRepair,
                        lastAttackDayResolved = nextLastAttackDay,
                        oxygenPercent = nextOxygen,
                        radiationMillisieverts = nextRadiation,
                        dwellerHealthPercent = nextHealth,
                        injuredDwellersCount = nextInjured,
                        generatorWearPercent = nextGeneratorWear,
                        craftedSpears = state.craftedSpears + craftedSpearsDelta,
                        craftedSnareTraps = state.craftedSnareTraps + craftedTrapsDelta,
                        aetherCrossbowLevel = state.aetherCrossbowLevel + craftedCrossbowDelta,
                        craftedShrapnelMines = (state.craftedShrapnelMines - raidMinesConsumed + craftedMinesDelta).coerceAtLeast(0),
                        sentryTurretCount = state.sentryTurretCount + craftedTurretsDelta,
                        plasmaCannonCount = state.plasmaCannonCount + craftedPlasmaDelta,
                        ammoRounds = (state.ammoRounds - expAmmoUsed - raidAmmoConsumed + newDayAmmoFab + craftedAmmoDelta).coerceAtLeast(0),
                        gasMasksCount = (state.gasMasksCount - expGasMaskUsed + craftedGasMasksDelta).coerceAtLeast(0),
                        hazmatSuitsCount = (state.hazmatSuitsCount - expHazmatUsed + craftedHazmatDelta).coerceAtLeast(0),
                        medkitsCount = state.medkitsCount + craftedMedkitsDelta,
                        radAwayCount = state.radAwayCount + craftedRadAwayDelta,
                        salvageComboCount = nextCombo,
                        rawSalvageCaches = (state.rawSalvageCaches - cachesBrokenThisTick).coerceAtLeast(0),
                        totalCachesDismantled = state.totalCachesDismantled + cachesBrokenThisTick,
                        totalConvoysCompleted = state.totalConvoysCompleted + (if (convoyGoldAward > 0) 1 else 0),
                        totalRaidsRepelled = state.totalRaidsRepelled + raidRepelledIncrement,
                        thoriumRods = state.thoriumRods + convoyThoriumAward,
                        aetherCells = state.aetherCells + convoyAetherAward,
                        cycleProgress = nextProgress,
                        dayCounter = nextDayCounter,
                        blackoutDaysCount = nextBlackoutDays,
                        blackoutSecondsAccumulated = nextBlackoutSeconds,
                        deficitDaysCount = nextDeficitDays,
                        deficitSecondsAccumulated = nextDeficitSeconds,
                        dwellerFeedCycleSecondsRemaining = nextFeedSeconds,
                        dwellerAppetiteEscalationSeconds = nextEscalationSeconds,
                        manualDynamoCranksRemaining = nextManualDynamoCranks,
                        activeDynamoCrankStack = nextActiveCrankStack,
                        miningShaftHeatPercent = nextShaftHeat,
                        roomStructuralWearPercent = nextRoomWear,
                        activePipeLeaksCount = nextPipeLeaks,
                        activeConstructionBuildingId = nextConstructId,
                        activeConstructionTargetLevel = nextConstructTargetLv,
                        activeConstructionRemainingSeconds = nextConstructRemSecs,
                        activeConstructionTotalSeconds = nextConstructTotalSecs,
                        activeConstructionRushGoldCost = nextConstructRushCost,
                        isGameOver = nextIsGameOver,
                        gameOverReason = nextGameOverReason,
                        lastSavedTimestampMillis = now
                    ),
                    scrapDelta = snap.scrapPerSec + cacheScrapBonus + raidScrapDelta + expScrapAward + newDayRoomScrapBonus - newDayScrapDeduct - autoRepairScrapUsed,
                    waterDelta = snap.waterPerSec + cacheWaterBonus + expWaterAward + newDayRoomWaterBonus - newDayWaterDeduct - fiveMinWaterDeduct,
                    rationsDelta = snap.rationsPerSec + cacheRationsBonus + expFoodAward + newDayRoomFoodBonus - newDayFoodDeduct - fiveMinFoodDeduct,
                    fuelDelta = snap.bioFuelPerSec + expFuelAward - newDayFuelDeduct,
                    researchDelta = snap.researchPerSec,
                    goldDelta = convoyGoldAward + raidGoldDelta + expGoldAward,
                    coresDelta = snap.mutagenCoresPerSec + convoyCoresAward + expCoresAward + raidCoresDelta,
                    xpDelta = snap.xpPerSec + convoyXpAward + raidXpDelta + expXpAward + buildCompleteXpAward + craftCompleteXpAward + doorUpgradeCompleteXpAward,
                    storageCap = snap.storageCap
                )

                if (updatedState.commandLevel > state.commandLevel) {
                    val gained = updatedState.commandLevel - state.commandLevel
                    val spAwarded = gained * 2
                    val goldBonus = gained * 30.0
                    updatedState = updatedState.copy(
                        availableSkillPoints = updatedState.availableSkillPoints + spAwarded,
                        totalSkillPointsEarned = updatedState.totalSkillPointsEarned + spAwarded,
                        gold = updatedState.gold + goldBonus
                    )
                    val title = CommandLevelProgression.rankTitleForLevel(updatedState.commandLevel)
                    overlayFlow.update {
                        it.copy(
                            levelUpBanner = LevelUpBannerState(
                                title = "VAULT LEVEL ${updatedState.commandLevel} / 100 REACHED!",
                                subtitle = "Overseer Rank: $title",
                                rewardSummary = "+$spAwarded Skill Points • +${goldBonus.toInt()} Gold • +${((CommandLevelProgression.globalProductionBonus(updatedState.commandLevel) - 1.0) * 100).roundToInt()}% Output",
                                isCommandLevel = true
                            )
                        )
                    }
                    repository.logEvent("LEVEL UP", "Vault reached Level ${updatedState.commandLevel}/100 ($title)! Earned +$spAwarded SP & +${goldBonus.toInt()} Gold.")
                }

                repository.updateOutpostState(updatedState)

                if (tickCounter % 4 == 0) {
                    progressWorkerClassXp(current.workersMap, current.skillsMap, nextPhase)
                    updateOrSpawnAnomaly(nextPhase, updatedState.commandLevel, current.skillsMap)
                }

                overlayFlow.update { it.copy(nowMillis = now) }
            }
        }
    }

    private suspend fun removeOneAssignedWorker(workersMap: Map<String, WorkerClassEntity>) {
        val assignedClass = WorkerClassType.entries.firstOrNull {
            (workersMap[it.id]?.assignedCount ?: 0) > 0
        } ?: return
        val entity = workersMap[assignedClass.id] ?: return
        repository.updateWorkerClass(
            entity.copy(assignedCount = (entity.assignedCount - 1).coerceAtLeast(0))
        )
    }

    private fun createDailyRaidThreat(
        dayNumber: Int,
        commandLevel: Int,
        plasmaCount: Int
    ): RaiderSiegeThreat {
        val isDay10MegaSiege = dayNumber % 10 == 0
        val reqDef = OutpostCalculator.computeDailyRaidRequiredDefense(
            dayNumber = dayNumber,
            activePhase = DayNightPhase.SCORCHING_DAY
        )
        val scale = (1.0 + (dayNumber - 1) * 0.32 + (commandLevel - 1) * 0.20) * (if (isDay10MegaSiege) 2.6 else 1.0)
        val plasmaMult = 1.0 + plasmaCount * 0.25
        val goldBounty = (20.0 * scale * plasmaMult).roundToInt().toDouble()
        val scrapBounty = (24.0 * scale).roundToInt().toDouble()
        val xpBounty = (28.0 * scale).roundToInt().toDouble()

        val bossTitle = if (isDay10MegaSiege) OutpostCalculator.computeDay10BossTitle(dayNumber) else ""
        val raidTitle = when {
            isDay10MegaSiege -> "DAY $dayNumber BOSS: ${bossTitle.uppercase()}!"
            dayNumber >= 15 -> "Day $dayNumber Cyber-Enclave Assault Battalion"
            dayNumber >= 9 -> "Day $dayNumber Deathclaw & Heavy Raider Siege"
            dayNumber >= 5 -> "Day $dayNumber Armored Raider Warband"
            else -> "Day $dayNumber Wasteland Raider Attack"
        }
        val raidSubtitle = when {
            isDay10MegaSiege -> "10-Day Warlord Boss Siege ($reqDef DEF Req)! Deploy Tactical Countermeasures (Flak, EMP, Door Weld)!"
            dayNumber >= 9 -> "Escalating Day $dayNumber Heavy Siege ($reqDef DEF Req) — Attacks grow deadlier each day!"
            else -> "Daily base attack ($reqDef DEF Req) — Raider warbands grow stronger every passing day!"
        }

        return RaiderSiegeThreat(
            id = System.currentTimeMillis() + dayNumber,
            title = raidTitle,
            subtitle = raidSubtitle,
            requiredDefenseRating = reqDef,
            bountyGoldReward = goldBounty,
            bountyScrapReward = scrapBounty,
            bountyXpReward = xpBounty,
            remainingSeconds = 45,
            dayNumber = dayNumber,
            isDay10MegaSiege = isDay10MegaSiege,
            isNightMutantSwarm = false,
            bossClassTitle = bossTitle
        )
    }

    private fun createNightMutantSwarmThreat(
        dayNumber: Int,
        commandLevel: Int,
        plasmaCount: Int
    ): RaiderSiegeThreat {
        val nightReqDef = OutpostCalculator.computeDailyRaidRequiredDefense(
            dayNumber = dayNumber,
            activePhase = DayNightPhase.RAD_NIGHT
        )
        val scale = (1.0 + (dayNumber - 1) * 0.35 + (commandLevel - 1) * 0.22) * 1.40
        val plasmaMult = 1.0 + plasmaCount * 0.30
        val goldBounty = (24.0 * scale * plasmaMult).roundToInt().toDouble()
        val scrapBounty = (28.0 * scale).roundToInt().toDouble()
        val xpBounty = (35.0 * scale).roundToInt().toDouble()

        return RaiderSiegeThreat(
            id = System.currentTimeMillis() + dayNumber * 100L,
            title = "NIGHT $dayNumber FERAL MUTANT SWARM!",
            subtitle = "Lethal Day $dayNumber Nocturnal Swarm ($nightReqDef DEF Req • +35% Night Threat • +1 Mutagen Core)!",
            requiredDefenseRating = nightReqDef,
            bountyGoldReward = goldBounty,
            bountyScrapReward = scrapBounty,
            bountyXpReward = xpBounty,
            remainingSeconds = 45,
            dayNumber = dayNumber,
            isDay10MegaSiege = false,
            isNightMutantSwarm = true
        )
    }

    private fun refreshNodesForPhaseTransition(phase: DayNightPhase) {
        val isNocturnalPhase = phase == DayNightPhase.DUSK || phase == DayNightPhase.RAD_NIGHT
        overlayFlow.update { current ->
            val updatedSlots = current.salvageNodes.mapIndexed { index, slot ->
                if (index == 2) {
                    val newType = if (isNocturnalPhase) {
                        listOf(
                            SalvageNodeType.BIOLUMINESCENT_METEOR_VEIN,
                            SalvageNodeType.ENCRYPTED_ORBITAL_POD,
                            SalvageNodeType.RADIOACTIVE_SLUDGE_TANKER
                        ).random()
                    } else {
                        SalvageNodeType.OVERGROWN_BIO_DOME
                    }
                    SalvageNodeSlot(index, newType, 0, newType.maxDurability)
                } else slot
            }
            current.copy(salvageNodes = updatedSlots)
        }
    }

    private fun pickReplacementNode(slotIndex: Int, phase: DayNightPhase): SalvageNodeType {
        val isNight = phase == DayNightPhase.DUSK || phase == DayNightPhase.RAD_NIGHT
        return when (slotIndex) {
            0 -> if (isNight && Random.nextFloat() < 0.45f) {
                SalvageNodeType.RADIOACTIVE_SLUDGE_TANKER
            } else {
                SalvageNodeType.ARMORED_CONVOY_WRECK
            }
            1 -> if (isNight && Random.nextFloat() < 0.45f) {
                SalvageNodeType.ENCRYPTED_ORBITAL_POD
            } else {
                SalvageNodeType.DEW_VAPOR_GEYSER
            }
            else -> if (isNight) {
                if (Random.nextBoolean()) SalvageNodeType.BIOLUMINESCENT_METEOR_VEIN else SalvageNodeType.ENCRYPTED_ORBITAL_POD
            } else {
                SalvageNodeType.OVERGROWN_BIO_DOME
            }
        }
    }

    private suspend fun progressWorkerClassXp(
        workersMap: Map<String, WorkerClassEntity>,
        skillsMap: Map<String, Int>,
        activePhase: DayNightPhase
    ) {
        val rankAcademy = skillsMap["WRK_VETERAN_ACADEMY"] ?: 0
        val savantEntity = workersMap[WorkerClassType.TECH_SAVANT.id]
        val mentorBonus = if ((savantEntity?.assignedCount ?: 0) > 0 && (savantEntity?.classLevel ?: 1) >= 15) 1.35 else 1.0

        for (wType in WorkerClassType.entries) {
            val entity = workersMap[wType.id] ?: continue
            if (entity.assignedCount <= 0) continue

            val favoredMult = if (activePhase in wType.favoredPhases) 1.5 else 1.0
            val xpGain = entity.assignedCount * 2.4 * favoredMult * (1.0 + rankAcademy * 0.25) * mentorBonus
            var newXp = entity.classXp + xpGain
            var newLevel = entity.classLevel
            var req = WorkerClassType.xpForNextClassLevel(newLevel)

            var leveledUp = false
            while (newXp >= req && newLevel < 50) {
                newXp -= req
                newLevel++
                req = WorkerClassType.xpForNextClassLevel(newLevel)
                leveledUp = true
            }

            repository.updateWorkerClass(
                entity.copy(classLevel = newLevel, classXp = newXp)
            )

            if (leveledUp) {
                val unlockedPerk = wType.perks.firstOrNull { it.requiredLevel == newLevel }
                val perkNote = unlockedPerk?.let { "Unlocked Veteran Perk: ${it.title}!" }
                    ?: "+14% Class Productivity unlocked!"
                overlayFlow.update {
                    it.copy(
                        levelUpBanner = LevelUpBannerState(
                            title = "${wType.displayName.uppercase()} LV. $newLevel",
                            subtitle = "Survivor Guild Mastery Promotion",
                            rewardSummary = perkNote,
                            isCommandLevel = false
                        )
                    )
                }
                repository.logEvent("GUILD", "${wType.displayName} reached Class Level $newLevel! $perkNote")
            }
        }
    }

    private fun updateOrSpawnAnomaly(
        phase: DayNightPhase,
        commandLevel: Int,
        skillsMap: Map<String, Int>
    ) {
        val currentAnomaly = overlayFlow.value.activeAnomaly
        if (currentAnomaly != null) {
            val remaining = currentAnomaly.remainingSeconds - 4
            if (remaining <= 0) {
                overlayFlow.update { it.copy(activeAnomaly = null) }
            } else {
                overlayFlow.update {
                    it.copy(activeAnomaly = currentAnomaly.copy(remainingSeconds = remaining))
                }
            }
            return
        }

        val rankSensors = skillsMap["CHR_BIOLUM_SENSORS"] ?: 0
        val rankNocturnal = skillsMap["CHR_NOCTURNAL_DOMINION"] ?: 0
        val baseChance = 0.22f * phase.mutagenSpawnMultiplier.toFloat() * (1f + rankSensors * 0.22f)

        if (Random.nextFloat() < baseChance.coerceAtMost(0.85f)) {
            val levelScale = 1.0 + (commandLevel - 1) * 0.25
            val extraNightCores = if (phase == DayNightPhase.RAD_NIGHT) rankNocturnal.toDouble() else 0.0

            val anomaly = when (phase) {
                DayNightPhase.DAWN -> RareAnomaly(
                    id = System.currentTimeMillis(),
                    title = "Geyser Condensation Surge",
                    subtitle = "High-pressure subterranean water & gold cache surfaced",
                    phase = phase,
                    scrapReward = (22.0 * levelScale).roundToInt().toDouble(),
                    waterReward = (42.0 * levelScale).roundToInt().toDouble(),
                    goldReward = (16.0 * levelScale).roundToInt().toDouble(),
                    researchReward = (12.0 * levelScale).roundToInt().toDouble(),
                    mutagenCoresReward = 1.0,
                    xpReward = (22.0 * levelScale).roundToInt().toDouble(),
                    remainingSeconds = 28
                )
                DayNightPhase.SCORCHING_DAY -> RareAnomaly(
                    id = System.currentTimeMillis(),
                    title = "Armored Convoy Drop-Pod",
                    subtitle = "Pre-collapse cargo pod exposed by shifting dunes",
                    phase = phase,
                    scrapReward = (52.0 * levelScale).roundToInt().toDouble(),
                    waterReward = (15.0 * levelScale).roundToInt().toDouble(),
                    goldReward = (24.0 * levelScale).roundToInt().toDouble(),
                    researchReward = (18.0 * levelScale).roundToInt().toDouble(),
                    mutagenCoresReward = 1.0,
                    xpReward = (26.0 * levelScale).roundToInt().toDouble(),
                    remainingSeconds = 28
                )
                DayNightPhase.DUSK -> RareAnomaly(
                    id = System.currentTimeMillis(),
                    title = "Encrypted Orbital Drone",
                    subtitle = "Low-altitude syndicate relay carrying Gold & Tech",
                    phase = phase,
                    scrapReward = (30.0 * levelScale).roundToInt().toDouble(),
                    waterReward = (24.0 * levelScale).roundToInt().toDouble(),
                    goldReward = (28.0 * levelScale).roundToInt().toDouble(),
                    researchReward = (34.0 * levelScale).roundToInt().toDouble(),
                    mutagenCoresReward = 1.0,
                    xpReward = (30.0 * levelScale).roundToInt().toDouble(),
                    remainingSeconds = 28
                )
                DayNightPhase.RAD_NIGHT -> RareAnomaly(
                    id = System.currentTimeMillis(),
                    title = "Bioluminescent Spore Meteor",
                    subtitle = "Radioactive crystalline impact rich in Mutagen Cores & Gold",
                    phase = phase,
                    scrapReward = (26.0 * levelScale).roundToInt().toDouble(),
                    waterReward = (20.0 * levelScale).roundToInt().toDouble(),
                    goldReward = (35.0 * levelScale).roundToInt().toDouble(),
                    researchReward = (40.0 * levelScale).roundToInt().toDouble(),
                    mutagenCoresReward = 2.0 + extraNightCores,
                    xpReward = (40.0 * levelScale).roundToInt().toDouble(),
                    remainingSeconds = 32
                )
            }
            overlayFlow.update { it.copy(activeAnomaly = anomaly) }
        }
    }

    // --- ARMORY CRAFTING, HUNTING FOR FOOD & PERIMETER DEFENSE ACTIONS ---

    fun craftItem(item: CraftableItemType) {
        viewModelScope.launch {
            val current = uiState.value
            if (current.activeCrafting != null) {
                overlayFlow.update {
                    it.copy(
                        lastDefenseDamageFeedback = "Armory Workbench busy crafting ${current.activeCrafting.item.title} (${current.activeCrafting.remainingSeconds}s left)! Forge Faster or Rush to finish."
                    )
                }
                return@launch
            }
            val state = current.state
            val cost = OutpostCalculator.computeCraftItemCost(
                item = item,
                state = state,
                skillsMap = current.skillsMap,
                buildingsMap = current.buildingsMap,
                workersMap = current.workersMap
            )
            if (!cost.canAfford) return@launch

            val updated = state.copy(
                scrap = state.scrap - cost.scrapCost,
                bioFuel = state.bioFuel - cost.fuelCost,
                gold = state.gold - cost.goldCost,
                aetherCells = state.aetherCells - cost.aetherCost,
                thoriumRods = state.thoriumRods - cost.thoriumCost,
                mutagenCores = state.mutagenCores - cost.coresCost
            )
            repository.updateOutpostState(updated)
            overlayFlow.update {
                it.copy(
                    activeCrafting = ActiveItemCrafting(
                        item = item,
                        remainingSeconds = cost.craftTimeSeconds,
                        totalSeconds = cost.craftTimeSeconds,
                        rushGoldCost = cost.rushGoldCost
                    ),
                    lastDefenseDamageFeedback = "Started crafting ${item.title} (${cost.craftTimeSeconds}s timer)..."
                )
            }
            repository.logEvent("ARMORY", "Started crafting ${item.title} (${cost.craftTimeSeconds}s timer).")
        }
    }

    fun accelerateItemCrafting() {
        viewModelScope.launch {
            val current = uiState.value
            val activeCraft = current.activeCrafting ?: return@launch
            val state = current.state
            val shaveSeconds = 3

            if (activeCraft.remainingSeconds <= shaveSeconds) {
                completeActiveItemCraft(activeCraft.item, state, current.production.storageCap, bonusGoldDeducted = 0.0)
            } else {
                val nextRemaining = activeCraft.remainingSeconds - shaveSeconds
                val updated = applyXpAndResourceDelta(
                    baseState = state,
                    xpDelta = 3.0,
                    storageCap = current.production.storageCap
                )
                repository.updateOutpostState(updated)
                overlayFlow.update {
                    it.copy(
                        activeCrafting = activeCraft.copy(remainingSeconds = nextRemaining)
                    )
                }
            }
        }
    }

    fun rushItemCraftingWithGold() {
        viewModelScope.launch {
            val current = uiState.value
            val activeCraft = current.activeCrafting ?: return@launch
            val state = current.state
            if (state.gold < activeCraft.rushGoldCost) return@launch
            completeActiveItemCraft(activeCraft.item, state, current.production.storageCap, bonusGoldDeducted = activeCraft.rushGoldCost)
        }
    }

    private suspend fun completeActiveItemCraft(
        item: CraftableItemType,
        state: OutpostStateEntity,
        storageCap: Double,
        bonusGoldDeducted: Double
    ) {
        var updated = state.copy(gold = (state.gold - bonusGoldDeducted).coerceAtLeast(0.0))
        updated = when (item) {
            CraftableItemType.HARPOON_SPEAR -> updated.copy(craftedSpears = state.craftedSpears + 2)
            CraftableItemType.SNARE_TRAP -> updated.copy(craftedSnareTraps = state.craftedSnareTraps + 1)
            CraftableItemType.AETHER_CROSSBOW -> updated.copy(aetherCrossbowLevel = state.aetherCrossbowLevel + 1)
            CraftableItemType.SHRAPNEL_MINE -> updated.copy(craftedShrapnelMines = state.craftedShrapnelMines + 2)
            CraftableItemType.AUTO_SENTRY_TURRET -> updated.copy(sentryTurretCount = state.sentryTurretCount + 1)
            CraftableItemType.THORIUM_PLASMA_CANNON -> updated.copy(plasmaCannonCount = state.plasmaCannonCount + 1)
            CraftableItemType.BALLISTIC_AMMO_CRATE -> updated.copy(ammoRounds = state.ammoRounds + 30)
            CraftableItemType.GAS_MASK -> updated.copy(gasMasksCount = state.gasMasksCount + 1)
            CraftableItemType.HAZMAT_SUIT -> updated.copy(hazmatSuitsCount = state.hazmatSuitsCount + 1)
            CraftableItemType.MEDKIT_RADAWAY_PACK -> updated.copy(
                medkitsCount = state.medkitsCount + 1,
                radAwayCount = state.radAwayCount + 1
            )
        }
        updated = applyXpAndResourceDelta(
            baseState = updated,
            xpDelta = 18.0,
            storageCap = storageCap
        )
        repository.updateOutpostState(updated)
        val doneMsg = "CRAFT COMPLETE: ${item.title} forged (${item.effectSummary} • +18 XP)!"
        overlayFlow.update {
            it.copy(
                activeCrafting = null,
                lastDefenseDamageFeedback = doneMsg
            )
        }
        repository.logEvent("CRAFT COMPLETE", doneMsg)
    }

    fun toggleAutoDefense(enabled: Boolean) {
        viewModelScope.launch {
            val state = uiState.value.state
            repository.updateOutpostState(state.copy(autoDefenseEnabled = enabled))
            val msg = if (enabled) {
                "AUTOMATED DEFENSES ONLINE: Powered Turrets, Mines & Scrappers will automatically intercept attacks and repair the Blast Door!"
            } else {
                "AUTOMATED DEFENSES PAUSED: Manual defense & repair mode active."
            }
            overlayFlow.update { it.copy(lastDefenseDamageFeedback = msg) }
            repository.logEvent("AUTO DEFENSE", msg)
        }
    }

    fun executeHunt(target: HuntingTarget) {
        viewModelScope.launch {
            val current = uiState.value
            val state = current.state
            val snap = current.production
            val preview = OutpostCalculator.computeHuntPreview(
                target = target,
                state = state,
                workersMap = current.workersMap,
                skillsMap = current.skillsMap,
                activePhase = snap.activePhase,
                buildingsMap = current.buildingsMap
            )
            if (!preview.canExecute) return@launch

            val canRescueCitizen = state.totalSurvivors < snap.maxWorkerCapacity &&
                (target == HuntingTarget.APEX_RAD_BEHEMOTH ||
                    (target == HuntingTarget.CANYON_PACK_STALKER && Random.nextFloat() < 0.45f))
            val rescuedCount = if (canRescueCitizen) 1 else 0
            val rescuedUnique = rescuedCount > 0 && Random.nextInt(100) < CaravanCandidateGenerator.UNIQUE_DWELLER_CHANCE_PERCENT

            // Hardcore Hunting: Ammo consumption & Beast Counter-Attack Injury Roll!
            val ammoConsumed = minOf(state.ammoRounds, preview.ammoRecommended)
            val beastCounterAttacked = Random.nextInt(100) < preview.counterAttackInjuryChancePercent
            val isVenomBeast = target == HuntingTarget.GLOWING_LEVIATHAN_WYRM || target == HuntingTarget.APEX_RAD_BEHEMOTH
            val bleedDelta = if (beastCounterAttacked && !isVenomBeast) 1 else 0
            val venomDelta = if (beastCounterAttacked && isVenomBeast) 1 else 0

            val updated = applyXpAndResourceDelta(
                baseState = state.copy(
                    totalSurvivors = state.totalSurvivors + rescuedCount,
                    uniqueDwellersCount = state.uniqueDwellersCount + (if (rescuedUnique) 1 else 0),
                    craftedSpears = (state.craftedSpears - target.spearsRequired).coerceAtLeast(0),
                    craftedSnareTraps = (state.craftedSnareTraps - target.trapsRequired).coerceAtLeast(0),
                    ammoRounds = (state.ammoRounds - ammoConsumed).coerceAtLeast(0),
                    bleedingDwellersCount = (state.bleedingDwellersCount + bleedDelta).coerceAtMost(5),
                    venomInfectedDwellersCount = (state.venomInfectedDwellersCount + venomDelta).coerceAtMost(5),
                    totalHuntsCompleted = state.totalHuntsCompleted + 1,
                    totalGoldSalvaged = state.totalGoldSalvaged + preview.goldReward
                ),
                rationsDelta = preview.foodReward,
                waterDelta = preview.waterReward,
                goldDelta = preview.goldReward,
                coresDelta = preview.coresReward,
                xpDelta = preview.xpReward + (if (rescuedCount > 0) 25.0 else 0.0) + (if (rescuedUnique) 40.0 else 0.0),
                storageCap = snap.storageCap
            )
            repository.updateOutpostState(updated)

            if (rescuedCount > 0) {
                val targetGuild = WorkerClassType.entries.minByOrNull {
                    current.workersMap[it.id]?.assignedCount ?: 0
                } ?: WorkerClassType.NIGHT_STALKER
                val existingClass = current.workersMap[targetGuild.id]
                    ?: WorkerClassEntity(targetGuild.id, 0, 1, 0.0)
                repository.updateWorkerClass(
                    existingClass.copy(
                        assignedCount = existingClass.assignedCount + 1,
                        classXp = existingClass.classXp + 35.0
                    )
                )
            }

            val rescueNote = if (rescuedCount > 0) " Rescued +1 Citizen!" else ""
            val injuryNote = when {
                venomDelta > 0 -> " WARNING: Beast Counter-Attack inflicted Rad-Venom on 1 Hunter!"
                bleedDelta > 0 -> " WARNING: Beast Counter-Attack lacerated 1 Hunter (Bleeding)!"
                else -> ""
            }
            val huntMsg = "Hunted ${target.title} (-$ammoConsumed Ammo): +${preview.foodReward.toInt()} Food, +${preview.waterReward.toInt()} Water & +${preview.goldReward.toInt()} Gold!$rescueNote$injuryNote"
            overlayFlow.update { it.copy(lastDefenseDamageFeedback = huntMsg) }
            repository.logEvent("HUNT", huntMsg)
        }
    }

    fun executeFishing(spot: FishingSpot) {
        viewModelScope.launch {
            val current = uiState.value
            val state = current.state
            val preview = OutpostCalculator.computeFishingPreview(spot, state, current.skillsMap, current.buildingsMap)
            if (!preview.canFish) return@launch

            val spearsUsed = if (spot.requiresHarpoonSpear) 1 else 0
            val updated = applyXpAndResourceDelta(
                baseState = state.copy(
                    craftedSpears = (state.craftedSpears - spearsUsed).coerceAtLeast(0),
                    totalFishCaught = state.totalFishCaught + 1,
                    totalGoldSalvaged = state.totalGoldSalvaged + preview.goldReward
                ),
                rationsDelta = preview.foodReward,
                waterDelta = preview.waterReward,
                goldDelta = preview.goldReward,
                xpDelta = preview.xpReward,
                storageCap = current.production.storageCap
            )
            repository.updateOutpostState(updated)
            repository.logEvent(
                "FISHING",
                "Caught ${spot.title}: +${preview.foodReward.toInt()} Food, +${preview.waterReward.toInt()} Water & +${preview.goldReward.toInt()} Gold!"
            )
        }
    }

    fun repelRaiderSiege() {
        viewModelScope.launch {
            val current = uiState.value
            val state = current.state
            val raid = current.activeRaidThreat ?: createDailyRaidThreat(
                dayNumber = state.dayCounter,
                commandLevel = state.commandLevel,
                plasmaCount = state.plasmaCannonCount
            )
            val hasEnoughDefense = current.production.totalDefenseRating >= raid.requiredDefenseRating
            val canDetonateMine = state.craftedShrapnelMines >= 1
            if (!hasEnoughDefense && !canDetonateMine) return@launch

            val minesUsed = if (hasEnoughDefense) 0 else 1
            val dayAmmoSurge = (raid.dayNumber - 1) / 2
            val turretAmmoBurn = minOf(state.ammoRounds, state.sentryTurretCount * 4 + state.plasmaCannonCount * 6 + dayAmmoSurge)

            // Post-Attack Vault Door & Defense Damage Roll (scales up with raid.dayNumber, mitigated by Blast Door level!)
            val luckyNoRepair = Random.nextInt(100) < 15
            val dayDamageAdder = ((raid.dayNumber - 1) * 1.8).roundToInt().coerceAtMost(35)
            val doorMitigation = ((state.blastDoorLevel - 1) * 2).coerceAtMost(28)
            val dmg = if (luckyNoRepair) 0 else (Random.nextInt(18, 30) + dayDamageAdder - doorMitigation).coerceIn(6, 75)
            val nextIntegrity = (state.defenseIntegrityPercent - dmg).coerceIn(20, 100)
            val nextNeedsRepair = !luckyNoRepair || state.needsDefenseRepair
            val repairNote = if (luckyNoRepair) {
                "LUCKY DEFLECTION (15% CHANCE)! Vault Door & Defenses took 0% damage — No repair needed!"
            } else {
                "Vault Door & Defenses took -$dmg% Day ${raid.dayNumber} siege damage (Now $nextIntegrity%) — You must repair your defenses!"
            }

            val updated = applyXpAndResourceDelta(
                baseState = state.copy(
                    craftedShrapnelMines = (state.craftedShrapnelMines - minesUsed).coerceAtLeast(0),
                    ammoRounds = (state.ammoRounds - turretAmmoBurn).coerceAtLeast(0),
                    totalRaidsRepelled = state.totalRaidsRepelled + 1,
                    defenseIntegrityPercent = nextIntegrity,
                    needsDefenseRepair = nextNeedsRepair,
                    luckyNoRepairCount = state.luckyNoRepairCount + (if (luckyNoRepair) 1 else 0),
                    lastAttackDayResolved = state.dayCounter
                ),
                scrapDelta = raid.bountyScrapReward,
                goldDelta = raid.bountyGoldReward,
                xpDelta = raid.bountyXpReward,
                storageCap = current.production.storageCap
            )
            overlayFlow.update {
                it.copy(
                    activeRaidThreat = null,
                    lastDefenseDamageFeedback = "Repelled ${raid.title} (+${raid.bountyGoldReward.toInt()} Gold, +${raid.bountyScrapReward.toInt()} Scrap)! $repairNote"
                )
            }
            repository.updateOutpostState(updated)
            repository.logEvent(
                "DEFENSE",
                "Repelled ${raid.title}! +${raid.bountyGoldReward.toInt()} Gold, +${raid.bountyScrapReward.toInt()} Scrap. $repairNote"
            )
        }
    }

    fun repairVaultDefenses() {
        viewModelScope.launch {
            val current = uiState.value
            val state = current.state
            val cost = OutpostCalculator.computeDefenseRepairCost(state, current.skillsMap, current.buildingsMap)
            if (!cost.canAfford) return@launch

            val updated = applyXpAndResourceDelta(
                baseState = state.copy(
                    scrap = state.scrap - cost.scrapCost,
                    gold = state.gold - cost.goldCost,
                    defenseIntegrityPercent = 100,
                    needsDefenseRepair = false,
                    totalRepairsCompleted = state.totalRepairsCompleted + 1
                ),
                xpDelta = 28.0,
                storageCap = current.production.storageCap
            )
            repository.updateOutpostState(updated)
            val msg = "REPAIRED VAULT DOOR & DEFENSES to 100% Integrity (-${cost.scrapCost.toInt()} Scrap, -${cost.goldCost.toInt()} Gold, +28 XP)!"
            overlayFlow.update { it.copy(lastDefenseDamageFeedback = msg) }
            repository.logEvent("REPAIR", msg)
        }
    }

    fun upgradeBlastDoor() {
        viewModelScope.launch {
            val current = uiState.value
            if (current.activeDoorUpgrade != null) {
                overlayFlow.update {
                    it.copy(
                        lastDefenseDamageFeedback = "Blast Door already being reinforced to Lv.${current.activeDoorUpgrade.targetLevel} (${current.activeDoorUpgrade.remainingSeconds}s left)! Weld Door or Rush to finish faster."
                    )
                }
                return@launch
            }
            val state = current.state
            val cost = OutpostCalculator.computeBlastDoorUpgradeCost(
                state = state,
                skillsMap = current.skillsMap,
                workersMap = current.workersMap
            )
            if (!cost.canAfford) return@launch

            val targetLevel = cost.targetLevel
            val updated = applyXpAndResourceDelta(
                baseState = state.copy(
                    scrap = state.scrap - cost.scrapCost,
                    gold = state.gold - cost.goldCost
                ),
                xpDelta = 10.0,
                storageCap = current.production.storageCap
            )
            repository.updateOutpostState(updated)
            val msg = "Started reinforcing Vault Blast Door to Lv.$targetLevel (${cost.upgradeTimeSeconds}s timer)..."
            overlayFlow.update {
                it.copy(
                    activeDoorUpgrade = ActiveDoorUpgrade(
                        targetLevel = targetLevel,
                        remainingSeconds = cost.upgradeTimeSeconds,
                        totalSeconds = cost.upgradeTimeSeconds,
                        rushGoldCost = cost.rushGoldCost
                    ),
                    lastDefenseDamageFeedback = msg
                )
            }
            repository.logEvent("BLAST DOOR", msg)
        }
    }

    fun accelerateBlastDoorUpgrade() {
        viewModelScope.launch {
            val current = uiState.value
            val activeDoor = current.activeDoorUpgrade ?: return@launch
            val state = current.state
            val shaveSeconds = 4

            if (activeDoor.remainingSeconds <= shaveSeconds) {
                val xpGain = 40.0 * activeDoor.targetLevel
                val updated = applyXpAndResourceDelta(
                    baseState = state.copy(
                        blastDoorLevel = activeDoor.targetLevel,
                        defenseIntegrityPercent = 100,
                        needsDefenseRepair = false
                    ),
                    xpDelta = xpGain,
                    storageCap = current.production.storageCap
                )
                repository.updateOutpostState(updated)
                val doneMsg = "BLAST DOOR UPGRADE COMPLETE: Reinforced Vault Blast Door reached Lv.${activeDoor.targetLevel} (+20 Base Defense & 100% Integrity)!"
                overlayFlow.update {
                    it.copy(
                        activeDoorUpgrade = null,
                        lastDefenseDamageFeedback = doneMsg
                    )
                }
                repository.logEvent("BLAST DOOR", doneMsg)
            } else {
                val nextRemaining = activeDoor.remainingSeconds - shaveSeconds
                val updated = applyXpAndResourceDelta(
                    baseState = state,
                    xpDelta = 4.0,
                    storageCap = current.production.storageCap
                )
                repository.updateOutpostState(updated)
                overlayFlow.update {
                    it.copy(
                        activeDoorUpgrade = activeDoor.copy(remainingSeconds = nextRemaining)
                    )
                }
            }
        }
    }

    fun rushBlastDoorUpgradeWithGold() {
        viewModelScope.launch {
            val current = uiState.value
            val activeDoor = current.activeDoorUpgrade ?: return@launch
            val state = current.state
            if (state.gold < activeDoor.rushGoldCost) return@launch

            val xpGain = 42.0 * activeDoor.targetLevel
            val updated = applyXpAndResourceDelta(
                baseState = state.copy(
                    gold = state.gold - activeDoor.rushGoldCost,
                    blastDoorLevel = activeDoor.targetLevel,
                    defenseIntegrityPercent = 100,
                    needsDefenseRepair = false
                ),
                xpDelta = xpGain,
                storageCap = current.production.storageCap
            )
            repository.updateOutpostState(updated)
            val doneMsg = "RUSHED BLAST DOOR UPGRADE: Vault Blast Door Lv.${activeDoor.targetLevel} completed immediately (-${activeDoor.rushGoldCost.toInt()} Gold, +20 Base Defense & 100% Integrity)!"
            overlayFlow.update {
                it.copy(
                    activeDoorUpgrade = null,
                    lastDefenseDamageFeedback = doneMsg
                )
            }
            repository.logEvent("BLAST DOOR RUSHED", doneMsg)
        }
    }

    fun advanceDayManually() {
        viewModelScope.launch {
            val current = uiState.value
            val state = current.state
            val snap = current.production
            val nextDay = state.dayCounter + 1

            // Deduct Daily Food & Water Upkeep for each hired Dweller + High-Level Room Resource Upkeep!
            val foodUpkeep = snap.dailyFoodUpkeepPerDay
            val waterUpkeep = snap.dailyWaterUpkeepPerDay + snap.dailyRoomWaterUpkeep
            val roomScrapUpkeep = snap.dailyRoomScrapUpkeep
            val roomFuelUpkeep = snap.dailyRoomFuelUpkeep

            // Advance active construction by 15s (or finish it)
            var buildFinishXp = 0.0
            val activeBuild = overlayFlow.value.activeConstruction
            var nextActiveBuild = activeBuild
            if (activeBuild != null) {
                if (activeBuild.remainingSeconds <= 15) {
                    repository.updateBuildingLevel(activeBuild.buildingType.id, activeBuild.targetLevel)
                    buildFinishXp = 26.0 + activeBuild.targetLevel * 14.0
                    nextActiveBuild = null
                    repository.logEvent(
                        "BUILD COMPLETE",
                        "Completed ${activeBuild.buildingType.displayName} Lv.${activeBuild.targetLevel} as Day $nextDay dawned!"
                    )
                } else {
                    nextActiveBuild = activeBuild.copy(remainingSeconds = activeBuild.remainingSeconds - 15)
                }
            }

            // Resolve today's base attack if not yet fought
            var raidGoldDelta = 0.0
            var raidScrapDelta = 0.0
            var raidXpDelta = 0.0
            var raidRepelledDelta = 0
            var nextIntegrity = state.defenseIntegrityPercent
            var nextNeedsRepair = state.needsDefenseRepair
            var nextLuckyCount = state.luckyNoRepairCount
            var defenseFeedback: String? = overlayFlow.value.lastDefenseDamageFeedback

            if (state.lastAttackDayResolved < state.dayCounter) {
                val raid = current.activeRaidThreat ?: createDailyRaidThreat(
                    state.dayCounter,
                    state.commandLevel,
                    state.plasmaCannonCount
                )
                val won = snap.totalDefenseRating >= raid.requiredDefenseRating
                if (won) {
                    raidGoldDelta = raid.bountyGoldReward
                    raidScrapDelta = raid.bountyScrapReward
                    raidXpDelta = raid.bountyXpReward
                    raidRepelledDelta = 1
                } else {
                    val daySeverityBonus = ((state.dayCounter - 1) * 0.015).coerceAtMost(0.25)
                    raidScrapDelta = -minOf(state.scrap * (0.15 + daySeverityBonus), 25.0 + state.dayCounter * 5.0)
                    raidGoldDelta = -minOf(state.gold * (0.12 + daySeverityBonus * 0.75), 15.0 + state.dayCounter * 3.5)
                }
                val luckyNoRepair = Random.nextInt(100) < 15
                if (luckyNoRepair) {
                    nextLuckyCount += 1
                    defenseFeedback = "Day ${state.dayCounter} Attack Resolved: LUCKY DEFLECTION (15%) — No repair needed!"
                } else {
                    val siloLv = current.buildingsMap[BuildingType.REINFORCED_SILO.id] ?: 0
                    val dayDamageAdder = ((state.dayCounter - 1) * 1.8).roundToInt().coerceAtMost(35)
                    val doorMitigation = ((state.blastDoorLevel - 1) * 2).coerceAtMost(28)
                    val rawDmg = (if (won) Random.nextInt(18, 29) else Random.nextInt(25, 38)) + dayDamageAdder - doorMitigation
                    val dmg = (rawDmg * (1.0 - siloLv * 0.04).coerceAtLeast(0.60)).roundToInt().coerceIn(8, 75)
                    nextIntegrity = (state.defenseIntegrityPercent - dmg).coerceAtLeast(20)
                    nextNeedsRepair = true
                    defenseFeedback = "Day ${state.dayCounter} Attack Resolved: Vault Door & Defenses took -$dmg% damage (Now $nextIntegrity%) — Must Repair!"
                }
            }

            // Evaluate 2-Day Resource Deficit & 6-Day Power Blackout Game Over Rules
            val survivalEval = OutpostCalculator.evaluateSurvivalEndOfDay(state, snap)
            val nextEscalationSecs = if (state.totalSurvivors > 0) {
                state.dwellerAppetiteEscalationSeconds + 90
            } else {
                state.dwellerAppetiteEscalationSeconds
            }
            val nextFeedSecs = if (state.totalSurvivors > 0) {
                val rem = state.dwellerFeedCycleSecondsRemaining - 90
                if (rem <= 0) OutpostCalculator.DWELLER_FEED_INTERVAL_SECONDS else rem
            } else {
                state.dwellerFeedCycleSecondsRemaining
            }
            val extraFiveMinFood = if (state.totalSurvivors > 0 && state.dwellerFeedCycleSecondsRemaining <= 90) snap.fiveMinDwellerFoodCost else 0.0
            val extraFiveMinWater = if (state.totalSurvivors > 0 && state.dwellerFeedCycleSecondsRemaining <= 90) snap.fiveMinDwellerWaterCost else 0.0

            // Grant daily Vault room production if Vault has enough Power (not in Blackout)!
            val dailyProdSeconds = if (snap.isVaultBlackout) 0.0 else 15.0
            val dailyScrapBonus = if (snap.isVaultBlackout) 0.0 else snap.dailyRoomScrapBonus
            val dailyWaterBonus = if (snap.isVaultBlackout) 0.0 else snap.dailyRoomWaterBonus
            val dailyFoodBonus = if (snap.isVaultBlackout) 0.0 else snap.dailyRoomFoodBonus
            val updated = applyXpAndResourceDelta(
                baseState = state.copy(
                    dayCounter = nextDay,
                    cycleProgress = 0.18f,
                    lastAttackDayResolved = state.dayCounter,
                    totalRaidsRepelled = state.totalRaidsRepelled + raidRepelledDelta,
                    defenseIntegrityPercent = nextIntegrity,
                    needsDefenseRepair = nextNeedsRepair,
                    luckyNoRepairCount = nextLuckyCount,
                    blackoutDaysCount = survivalEval.nextBlackoutDaysCount,
                    deficitDaysCount = survivalEval.nextDeficitDaysCount,
                    dwellerFeedCycleSecondsRemaining = nextFeedSecs,
                    dwellerAppetiteEscalationSeconds = nextEscalationSecs,
                    isGameOver = survivalEval.isGameOver,
                    gameOverReason = survivalEval.gameOverReason
                ),
                scrapDelta = snap.scrapPerSec * dailyProdSeconds + dailyScrapBonus + raidScrapDelta - roomScrapUpkeep,
                waterDelta = snap.waterPerSec.coerceAtLeast(0.0) * dailyProdSeconds + dailyWaterBonus - waterUpkeep - extraFiveMinWater,
                rationsDelta = snap.rationsPerSec.coerceAtLeast(0.0) * dailyProdSeconds + dailyFoodBonus - foodUpkeep - extraFiveMinFood,
                fuelDelta = snap.bioFuelPerSec.coerceAtLeast(0.0) * dailyProdSeconds - roomFuelUpkeep,
                researchDelta = snap.researchPerSec * dailyProdSeconds,
                goldDelta = raidGoldDelta,
                xpDelta = snap.xpPerSec * dailyProdSeconds + raidXpDelta + buildFinishXp + 15.0,
                storageCap = snap.storageCap
            )
            repository.updateOutpostState(updated)
            if (survivalEval.isGameOver) {
                repository.logEvent("GAME OVER", survivalEval.gameOverReason)
            }
            overlayFlow.update {
                it.copy(
                    activeConstruction = nextActiveBuild,
                    activeRaidThreat = if (survivalEval.isGameOver) null else createDailyRaidThreat(nextDay, updated.commandLevel, updated.plasmaCannonCount),
                    lastDefenseDamageFeedback = if (survivalEval.isGameOver) survivalEval.gameOverReason else defenseFeedback
                )
            }
            repository.logEvent(
                "NEW DAY",
                "Advanced to Day $nextDay (Upkeep: -${foodUpkeep.roundToInt()} Food, -${waterUpkeep.roundToInt()} Water • Room Supply: +${dailyFoodBonus.toInt()} Food, +${dailyWaterBonus.toInt()} Water, +${dailyScrapBonus.toInt()} Scrap)."
            )
        }
    }

    fun restartGame(keepPrestige: Boolean = false) {
        viewModelScope.launch {
            val freshState = repository.resetEntireGame(keepPrestige = keepPrestige)
            val rankDrills = if (keepPrestige) (repository.getSkillSnapshot("WRK_GUILD_DRILLS")?.currentRank ?: 0) else 0
            val discountMult = (1.0 - rankDrills * 0.10).coerceAtLeast(0.50)
            overlayFlow.update { ov ->
                ov.copy(
                    salvageNodes = listOf(
                        SalvageNodeSlot(0, SalvageNodeType.ARMORED_CONVOY_WRECK, 0, SalvageNodeType.ARMORED_CONVOY_WRECK.maxDurability),
                        SalvageNodeSlot(1, SalvageNodeType.DEW_VAPOR_GEYSER, 0, SalvageNodeType.DEW_VAPOR_GEYSER.maxDurability),
                        SalvageNodeSlot(2, SalvageNodeType.OVERGROWN_BIO_DOME, 0, SalvageNodeType.OVERGROWN_BIO_DOME.maxDurability)
                    ),
                    hireableCandidates = CaravanCandidateGenerator.generateBoard(
                        freshState.totalSurvivors,
                        freshState.commandLevel,
                        discountMult
                    ),
                    lastSalvageFeedback = if (keepPrestige) {
                        "Legacy Restart Complete: Day 1 started with Prestige Rank ${freshState.prestigeLevel} & ${freshState.prestigeCores} Prestige Cores preserved!"
                    } else {
                        "Full Fresh Restart Complete: Day 1 started with your Rusty Pickaxe!"
                    },
                    lastDefenseDamageFeedback = null,
                    lastExpeditionFeedback = null,
                    activeConvoy = null,
                    activeExpedition = null,
                    activeConstruction = null,
                    activeCrafting = null,
                    activeDoorUpgrade = null,
                    activeAnomaly = null,
                    activeRaidThreat = null,
                    offlineReport = null,
                    levelUpBanner = null
                )
            }
        }
    }

    fun crankEmergencyDynamo() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val current = uiState.value
            val state = current.state
            val prod = current.production
            if (state.manualDynamoCranksRemaining <= 0) {
                val exhaustedMsg = "OUT OF MANUAL CRANKS (0/${OutpostCalculator.MAX_DYNAMO_CRANKS_PER_BLACKOUT} Left): You can manual crank 4 times per power outage! Build or fuel Reactors to restore permanent power."
                overlayFlow.update { it.copy(lastSalvageFeedback = exhaustedMsg) }
                return@launch
            }
            val nextCranksLeft = (state.manualDynamoCranksRemaining - 1).coerceAtLeast(0)
            val currentStack = if (state.thoriumOverdriveUntilMillis > now) state.activeDynamoCrankStack else 0
            val nextStack = (currentStack + 1).coerceIn(1, OutpostCalculator.MAX_DYNAMO_CRANKS_PER_BLACKOUT)
            val totalCrankMw = nextStack * 16.0
            val restoresPower = (prod.totalMegawattGenerated + 16.0) >= prod.totalMegawattDemand
            val updated = applyXpAndResourceDelta(
                baseState = state.copy(
                    thoriumOverdriveUntilMillis = now + 45_000L,
                    manualDynamoCranksRemaining = nextCranksLeft,
                    activeDynamoCrankStack = nextStack,
                    blackoutDaysCount = if (restoresPower) 0 else state.blackoutDaysCount,
                    blackoutSecondsAccumulated = if (restoresPower) 0 else state.blackoutSecondsAccumulated
                ),
                xpDelta = 10.0,
                storageCap = prod.storageCap
            )
            repository.updateOutpostState(updated)
            val usedCount = OutpostCalculator.MAX_DYNAMO_CRANKS_PER_BLACKOUT - nextCranksLeft
            val msg = "Emergency Hand-Crank Dynamo ($usedCount/${OutpostCalculator.MAX_DYNAMO_CRANKS_PER_BLACKOUT} Cranks • $nextCranksLeft left): +${totalCrankMw.toInt()} MW Power for 45s!"
            overlayFlow.update {
                it.copy(lastSalvageFeedback = msg)
            }
            repository.logEvent("VAULT POWER", msg)
        }
    }

    fun maintainGenerators() {
        viewModelScope.launch {
            val current = uiState.value
            val state = current.state
            val scrapUsed = if (state.scrap >= 6.0) 6.0 else 0.0
            val updated = applyXpAndResourceDelta(
                baseState = state.copy(
                    scrap = (state.scrap - scrapUsed).coerceAtLeast(0.0),
                    generatorWearPercent = 0,
                    totalRepairsCompleted = state.totalRepairsCompleted + 1
                ),
                xpDelta = 12.0,
                storageCap = current.production.storageCap
            )
            repository.updateOutpostState(updated)
            val msg = "TUNED GENERATORS: Reset Generator Wear to 0% (+100% Peak MW Efficiency & +12 XP)!"
            overlayFlow.update { it.copy(lastSalvageFeedback = msg) }
            repository.logEvent("GENERATOR TUNE-UP", msg)
        }
    }

    fun scrubVaultAirManual() {
        viewModelScope.launch {
            val current = uiState.value
            val state = current.state
            val newO2 = (state.oxygenPercent + 25).coerceAtMost(100)
            val updated = applyXpAndResourceDelta(
                baseState = state.copy(oxygenPercent = newO2),
                xpDelta = 8.0,
                storageCap = current.production.storageCap
            )
            repository.updateOutpostState(updated)
            val msg = "SCRUBBED VAULT AIR: Emergency O2 Bellows pumped Vault Oxygen to $newO2% (+8 XP)!"
            overlayFlow.update { it.copy(lastSalvageFeedback = msg) }
            repository.logEvent("LIFE SUPPORT", msg)
        }
    }

    fun useRadAway() {
        viewModelScope.launch {
            val current = uiState.value
            val state = current.state
            val hasRadAway = state.radAwayCount > 0
            val radReduction = if (hasRadAway) 50 else 25
            val newRads = (state.radiationMillisieverts - radReduction).coerceAtLeast(0)
            val newHealth = (state.dwellerHealthPercent + 10).coerceAtMost(100)
            val updated = applyXpAndResourceDelta(
                baseState = state.copy(
                    radAwayCount = (state.radAwayCount - (if (hasRadAway) 1 else 0)).coerceAtLeast(0),
                    radiationMillisieverts = newRads,
                    dwellerHealthPercent = newHealth
                ),
                xpDelta = 10.0,
                storageCap = current.production.storageCap
            )
            repository.updateOutpostState(updated)
            val msg = if (hasRadAway) {
                "USED RADAWAY: Flushed -$radReduction mSv Radiation (Now ${newRads} mSv) & restored +10% Dweller Health!"
            } else {
                "DECON SHOWER FLUSH: Scrubbed -$radReduction mSv Radiation (Now ${newRads} mSv)!"
            }
            overlayFlow.update {
                it.copy(
                    lastSalvageFeedback = msg,
                    lastDefenseDamageFeedback = msg
                )
            }
            repository.logEvent("MEDBAY", msg)
        }
    }

    fun useMedkit() {
        viewModelScope.launch {
            val current = uiState.value
            val state = current.state
            val hasMedkit = state.medkitsCount > 0
            val healAmount = if (hasMedkit) 35 else 15
            val newHealth = (state.dwellerHealthPercent + healAmount).coerceAtMost(100)
            val updated = applyXpAndResourceDelta(
                baseState = state.copy(
                    medkitsCount = (state.medkitsCount - (if (hasMedkit) 1 else 0)).coerceAtLeast(0),
                    dwellerHealthPercent = newHealth,
                    injuredDwellersCount = 0
                ),
                xpDelta = 10.0,
                storageCap = current.production.storageCap
            )
            repository.updateOutpostState(updated)
            val msg = if (hasMedkit) {
                "APPLIED FIELD MEDKIT: Healed all injured Dwellers & restored Health to $newHealth% (+10 XP)!"
            } else {
                "EMERGENCY FIRST AID: Stabilized Dwellers & restored Health to $newHealth% (+10 XP)!"
            }
            overlayFlow.update {
                it.copy(
                    lastSalvageFeedback = msg,
                    lastDefenseDamageFeedback = msg
                )
            }
            repository.logEvent("MEDBAY", msg)
        }
    }

    fun sendDwellerExpedition(type: WastelandExpeditionType) {
        viewModelScope.launch {
            val current = uiState.value
            val state = current.state
            if (state.totalSurvivors <= 0 || current.activeExpedition != null) return@launch
            val effectiveDeathChance = OutpostCalculator.computeEffectiveExpeditionDeathChance(
                type,
                current.buildingsMap,
                state
            )

            overlayFlow.update {
                it.copy(
                    activeExpedition = ActiveDwellerExpedition(
                        type = type,
                        remainingSeconds = type.durationSeconds,
                        totalSeconds = type.durationSeconds
                    ),
                    lastExpeditionFeedback = "Sent 1 Dweller on ${type.title} (${type.durationSeconds}s • $effectiveDeathChance% Death Chance)..."
                )
            }
            repository.logEvent(
                "EXPEDITION",
                "Dispatched 1 Dweller on ${type.title} ($effectiveDeathChance% Fatality Risk)."
            )
        }
    }

    // --- INTER-TOWN GOLD TRADE, RARE RESOURCES, CONVOYS & POWER STATIONS ---

    fun sellResourceToTown(town: TradeTown, resourceType: ResourceType, batchAmount: Double = 30.0) {
        viewModelScope.launch {
            val current = uiState.value
            val state = current.state
            val available = when (resourceType) {
                ResourceType.SCRAP -> state.scrap
                ResourceType.WATER -> state.water
                ResourceType.RATIONS -> state.rations
                ResourceType.BIO_FUEL -> state.bioFuel
                ResourceType.RESEARCH_DATA -> state.researchData
                else -> 0.0
            }
            if (available < batchAmount) return@launch

            val goldEarned = OutpostCalculator.computeTownSellQuote(
                town = town,
                resourceType = resourceType,
                batchAmount = batchAmount,
                activePhase = current.production.activePhase,
                skillsMap = current.skillsMap
            )

            val updated = applyXpAndResourceDelta(
                baseState = state,
                scrapDelta = if (resourceType == ResourceType.SCRAP) -batchAmount else 0.0,
                waterDelta = if (resourceType == ResourceType.WATER) -batchAmount else 0.0,
                rationsDelta = if (resourceType == ResourceType.RATIONS) -batchAmount else 0.0,
                fuelDelta = if (resourceType == ResourceType.BIO_FUEL) -batchAmount else 0.0,
                researchDelta = if (resourceType == ResourceType.RESEARCH_DATA) -batchAmount else 0.0,
                goldDelta = goldEarned,
                xpDelta = 8.0,
                storageCap = current.production.storageCap
            )
            repository.updateOutpostState(updated)
            repository.logEvent(
                "TRADE",
                "Sold ${batchAmount.toInt()} ${resourceType.shortLabel} to ${town.displayName} for +${goldEarned.toInt()} Gold!"
            )
        }
    }

    fun buyRareSyndicateItem(item: RareSyndicateItem) {
        viewModelScope.launch {
            val current = uiState.value
            val state = current.state
            val goldPrice = OutpostCalculator.computeRareItemGoldPrice(item, current.skillsMap)
            if (state.gold < goldPrice) return@launch

            val updated = state.copy(
                gold = state.gold - goldPrice,
                thoriumRods = state.thoriumRods + (if (item.resourceType == ResourceType.THORIUM_RODS) item.batchSize else 0.0),
                aetherCells = state.aetherCells + (if (item.resourceType == ResourceType.AETHER_CELLS) item.batchSize else 0.0),
                mutagenCores = state.mutagenCores + (if (item.resourceType == ResourceType.MUTAGEN_CORES) item.batchSize else 0.0)
            )
            repository.updateOutpostState(updated)
            repository.logEvent(
                "SYNDICATE",
                "Purchased +${item.batchSize.toInt()} ${item.title} for ${goldPrice.toInt()} Gold."
            )
        }
    }

    fun dispatchConvoy(town: TradeTown) {
        viewModelScope.launch {
            val current = uiState.value
            if (current.activeConvoy != null) return@launch
            val state = current.state
            val fuelCost = 12.0 + (state.convoyLevel - 1) * 4.0
            val rationsCost = 10.0 + (state.convoyLevel - 1) * 3.0
            if (state.bioFuel < fuelCost || state.rations < rationsCost) return@launch

            val rankAcademy = current.skillsMap["WRK_VETERAN_ACADEMY"] ?: 0
            val rankMonopoly = current.skillsMap["WRK_SOVEREIGN_COMMAND"] ?: 0
            val stalkerEntity = current.workersMap[WorkerClassType.NIGHT_STALKER.id]
            val stalkerBonus = if ((stalkerEntity?.assignedCount ?: 0) > 0 && (stalkerEntity?.classLevel ?: 1) >= 5) 1.15 else 1.0
            val turretBonus = 1.0 + state.sentryTurretCount * 0.10

            val convoyMult = (1.0 + (state.convoyLevel - 1) * 0.45) *
                (1.0 + rankAcademy * 0.20) *
                (1.0 + rankMonopoly * 0.30) *
                stalkerBonus * turretBonus

            val expectedGold = (town.baseGoldPayout * convoyMult).roundToInt().toDouble()
            val expectedRareAmount = (1.0 + state.convoyLevel / 2 + (if (rankMonopoly >= 2) 1 else 0)).toDouble()
            val expectedXp = (32.0 + state.convoyLevel * 12.0).roundToInt().toDouble()
            val duration = (town.baseConvoyDurationSeconds - (state.convoyLevel - 1) * 2).coerceAtLeast(10)

            repository.updateOutpostState(
                state.copy(
                    bioFuel = state.bioFuel - fuelCost,
                    rations = state.rations - rationsCost
                )
            )
            overlayFlow.update {
                it.copy(
                    activeConvoy = ActiveConvoyRun(
                        destination = town,
                        totalSeconds = duration,
                        remainingSeconds = duration,
                        expectedGoldReward = expectedGold,
                        expectedRareResource = town.bonusRareDropType,
                        expectedRareAmount = expectedRareAmount,
                        expectedXpReward = expectedXp
                    )
                )
            }
            repository.logEvent("CONVOY", "Dispatched Armored Convoy Lv.${state.convoyLevel} to ${town.displayName} (${duration}s).")
        }
    }

    fun upgradeConvoy() {
        viewModelScope.launch {
            val current = uiState.value
            val state = current.state
            val cost = OutpostCalculator.computeConvoyUpgradeCost(state)
            if (!cost.canAfford) return@launch

            val newLevel = state.convoyLevel + 1
            val updated = applyXpAndResourceDelta(
                baseState = state.copy(
                    convoyLevel = newLevel,
                    scrap = state.scrap - cost.scrapCost,
                    gold = state.gold - cost.goldCost,
                    thoriumRods = state.thoriumRods - cost.thoriumCost,
                    aetherCells = state.aetherCells - cost.aetherCost
                ),
                xpDelta = 45.0 * newLevel,
                storageCap = current.production.storageCap
            )
            repository.updateOutpostState(updated)
            repository.logEvent("CONVOY", "Upgraded Armored Trade Convoy to Level $newLevel using Thorium & Aether!")
        }
    }

    fun upgradePowerStation(station: PowerStationType) {
        viewModelScope.launch {
            val current = uiState.value
            val state = current.state
            val cost = OutpostCalculator.computePowerStationUpgradeCost(station, state, current.skillsMap)
            if (!cost.canAfford) return@launch

            val newLevel = cost.currentLevel + 1
            val updatedBase = when (station) {
                PowerStationType.BIO_SLUDGE_TURBINE -> state.copy(bioTurbineLevel = newLevel)
                PowerStationType.SOLAR_HELIOSTAT_TOWER -> state.copy(solarTowerLevel = newLevel)
                PowerStationType.THORIUM_FISSION_REACTOR -> state.copy(thoriumReactorLevel = newLevel)
                PowerStationType.AETHER_RESONANCE_SUBSTATION -> state.copy(aetherSubstationLevel = newLevel)
            }.copy(
                scrap = state.scrap - cost.scrapCost,
                gold = state.gold - cost.goldCost,
                thoriumRods = state.thoriumRods - cost.thoriumCost,
                aetherCells = state.aetherCells - cost.aetherCost,
                mutagenCores = state.mutagenCores - cost.coresCost,
                manualDynamoCranksRemaining = OutpostCalculator.MAX_DYNAMO_CRANKS_PER_BLACKOUT
            )

            val updated = applyXpAndResourceDelta(
                baseState = updatedBase,
                xpDelta = 40.0 * newLevel,
                storageCap = current.production.storageCap
            )
            repository.updateOutpostState(updated)
            repository.logEvent("POWER GRID", "Constructed/Upgraded ${station.displayName} to Level $newLevel!")
        }
    }

    fun injectThoriumRodOverdrive() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val state = uiState.value.state
            if (state.thoriumRods < 1.0 || state.thoriumOverdriveUntilMillis > now) return@launch

            repository.updateOutpostState(
                state.copy(
                    thoriumRods = state.thoriumRods - 1.0,
                    thoriumOverdriveUntilMillis = now + 60_000L
                )
            )
            repository.logEvent("POWER GRID", "Injected 1 Thorium Fission Rod: +60% Power Station Megawatts for 60s!")
        }
    }

    // --- MANUAL SALVAGE NODES (DIRECT GOLD & RESOURCE GATHERING) & WORKER HIRING ---

    fun harvestSalvageNode(slotIndex: Int) {
        viewModelScope.launch {
            val current = uiState.value
            val slot = current.salvageNodes.getOrNull(slotIndex) ?: return@launch
            val state = current.state
            val snap = current.production

            val newCombo = (state.salvageComboCount + 1).coerceAtMost(15)
            // Hardcore Mining: Pickaxe Durability (-3% per swing), Shaft Heat (+7% per swing), and Cave-In Hazard!
            val nextDurability = (state.pickaxeDurabilityPercent - 3).coerceAtLeast(0)
            val nextHeat = (state.miningShaftHeatPercent + 7).coerceAtMost(100)
            val triggeredCaveIn = state.activeCaveInHazard || (nextHeat >= 80 && Random.nextInt(100) < 25)

            val stateWithCombo = state.copy(
                salvageComboCount = newCombo,
                pickaxeDurabilityPercent = nextDurability,
                miningShaftHeatPercent = nextHeat,
                activeCaveInHazard = triggeredCaveIn
            )

            val yieldPreview = OutpostCalculator.computeSalvageNodeYield(
                nodeType = slot.nodeType,
                state = stateWithCombo,
                buildingsMap = current.buildingsMap,
                workersMap = current.workersMap,
                skillsMap = current.skillsMap,
                activePhase = snap.activePhase
            )

            val nextStep = slot.currentProgress + 1
            if (nextStep < slot.maxProgress) {
                val tapPrimary = yieldPreview.tapPrimaryAmount
                val tapGold = yieldPreview.tapGoldAmount
                val updatedState = applyXpAndResourceDelta(
                    baseState = stateWithCombo.copy(
                        totalManualScavenges = state.totalManualScavenges + 1,
                        totalGoldSalvaged = state.totalGoldSalvaged + tapGold
                    ),
                    scrapDelta = if (slot.nodeType.primaryResource == ResourceType.SCRAP) tapPrimary else 1.0,
                    waterDelta = if (slot.nodeType.primaryResource == ResourceType.WATER) tapPrimary else 0.0,
                    rationsDelta = if (slot.nodeType.primaryResource == ResourceType.RATIONS) tapPrimary else 0.0,
                    fuelDelta = if (slot.nodeType.primaryResource == ResourceType.BIO_FUEL) tapPrimary else 0.0,
                    researchDelta = if (slot.nodeType.primaryResource == ResourceType.RESEARCH_DATA) tapPrimary else 0.0,
                    goldDelta = tapGold,
                    xpDelta = 4.0,
                    storageCap = snap.storageCap
                )
                repository.updateOutpostState(updatedState)
                val hazardSuffix = when {
                    triggeredCaveIn && !state.activeCaveInHazard -> " • CAVE-IN TRIGGERED (80%+ Heat)!"
                    nextDurability <= 20 -> " • Pickaxe Dull ($nextDurability%)!"
                    else -> ""
                }
                overlayFlow.update { ov ->
                    ov.copy(
                        lastSalvageFeedback = "Salvaged +${tapPrimary.toInt()} ${slot.nodeType.primaryResource.shortLabel} & +${tapGold.toInt()} GOLD (${nextStep}/${slot.maxProgress})$hazardSuffix",
                        salvageNodes = ov.salvageNodes.map {
                            if (it.slotIndex == slotIndex) it.copy(currentProgress = nextStep) else it
                        }
                    )
                }
            } else {
                var scrapGain = 0.0
                var waterGain = 0.0
                var rationsGain = 0.0
                var fuelGain = 0.0
                var researchGain = 0.0
                var coresGain = yieldPreview.bonusMutagenCores
                var thoriumBonus = 0.0
                var aetherBonus = 0.0

                fun addRes(type: ResourceType, amount: Double) {
                    when (type) {
                        ResourceType.SCRAP -> scrapGain += amount
                        ResourceType.WATER -> waterGain += amount
                        ResourceType.RATIONS -> rationsGain += amount
                        ResourceType.BIO_FUEL -> fuelGain += amount
                        ResourceType.RESEARCH_DATA -> researchGain += amount
                        ResourceType.MUTAGEN_CORES -> coresGain += amount
                        else -> {}
                    }
                }
                addRes(slot.nodeType.primaryResource, yieldPreview.primaryAmount)
                addRes(slot.nodeType.secondaryResource, yieldPreview.secondaryAmount)

                // Roll for Rare Relic Jackpot on Node Strip!
                var jackpotLabel = ""
                if (Random.nextInt(100) < yieldPreview.rareJackpotChancePercent) {
                    when (Random.nextInt(3)) {
                        0 -> {
                            thoriumBonus = 1.0
                            jackpotLabel = " + 1 THORIUM ROD!"
                        }
                        1 -> {
                            aetherBonus = 1.0
                            jackpotLabel = " + 1 AETHER CAPACITOR!"
                        }
                        else -> {
                            coresGain += 1.0
                            jackpotLabel = " + 1 MUTAGEN CORE!"
                        }
                    }
                }

                val updatedState = applyXpAndResourceDelta(
                    baseState = stateWithCombo.copy(
                        rawSalvageCaches = state.rawSalvageCaches + yieldPreview.cacheDropCount,
                        thoriumRods = state.thoriumRods + thoriumBonus,
                        aetherCells = state.aetherCells + aetherBonus,
                        totalManualScavenges = state.totalManualScavenges + 1,
                        totalNodesStripped = state.totalNodesStripped + 1,
                        totalGoldSalvaged = state.totalGoldSalvaged + yieldPreview.goldAmount
                    ),
                    scrapDelta = scrapGain,
                    waterDelta = waterGain,
                    rationsDelta = rationsGain,
                    fuelDelta = fuelGain,
                    researchDelta = researchGain,
                    goldDelta = yieldPreview.goldAmount,
                    coresDelta = coresGain,
                    xpDelta = yieldPreview.xpReward,
                    storageCap = snap.storageCap
                )
                repository.updateOutpostState(updatedState)

                val replacementType = pickReplacementNode(slotIndex, snap.activePhase)
                val feedbackMsg = "STRIPPED ${slot.nodeType.title.uppercase()}: +${yieldPreview.goldAmount.toInt()} GOLD, +${yieldPreview.primaryAmount.toInt()} ${slot.nodeType.primaryResource.shortLabel}, +${yieldPreview.cacheDropCount} Cache(s)$jackpotLabel"
                overlayFlow.update { ov ->
                    ov.copy(
                        lastSalvageFeedback = feedbackMsg,
                        salvageNodes = ov.salvageNodes.map {
                            if (it.slotIndex == slotIndex) {
                                SalvageNodeSlot(slotIndex, replacementType, 0, replacementType.maxDurability)
                            } else it
                        }
                    )
                }
                repository.logEvent("SALVAGE", feedbackMsg)
            }
        }
    }

    fun dismantleSalvageCache(dismantleAll: Boolean = false) {
        viewModelScope.launch {
            val current = uiState.value
            val state = current.state
            if (state.rawSalvageCaches <= 0) return@launch

            val countToOpen = if (dismantleAll) state.rawSalvageCaches.coerceAtMost(10) else 1
            val yardLv = current.buildingsMap[BuildingType.SALVAGE_YARD.id] ?: 0
            val refineryLv = current.buildingsMap[BuildingType.BIO_SLUDGE_REFINERY.id] ?: 0
            val arrayLv = current.buildingsMap[BuildingType.SIGNAL_RESEARCH_ARRAY.id] ?: 0
            val comboMult = 1.0 + state.salvageComboCount.coerceIn(0, 15) * 0.05
            val mult = (1.0 + (state.salvageRigLevel - 1) * 0.35) * (1.0 + yardLv * 0.25) * comboMult

            val scrapGain = (countToOpen * 8.0 * mult).roundToInt().toDouble()
            val waterGain = (countToOpen * 5.5 * mult).roundToInt().toDouble()
            val rationsGain = (countToOpen * 5.5 * mult).roundToInt().toDouble()
            val fuelGain = (countToOpen * 3.0 * (1.0 + refineryLv * 0.25)).roundToInt().toDouble()
            val researchGain = (countToOpen * 3.0 * (1.0 + arrayLv * 0.25)).roundToInt().toDouble()
            val goldGain = (countToOpen * 10.0 * mult).roundToInt().toDouble()
            val xpGain = countToOpen * 8.0

            val updated = applyXpAndResourceDelta(
                baseState = state.copy(
                    rawSalvageCaches = state.rawSalvageCaches - countToOpen,
                    totalCachesDismantled = state.totalCachesDismantled + countToOpen,
                    totalGoldSalvaged = state.totalGoldSalvaged + goldGain
                ),
                scrapDelta = scrapGain,
                waterDelta = waterGain,
                rationsDelta = rationsGain,
                fuelDelta = fuelGain,
                researchDelta = researchGain,
                goldDelta = goldGain,
                xpDelta = xpGain,
                storageCap = current.production.storageCap
            )
            repository.updateOutpostState(updated)
            overlayFlow.update {
                it.copy(
                    lastSalvageFeedback = "Dismantled $countToOpen Cache(s): +${goldGain.toInt()} GOLD, +${scrapGain.toInt()} SCRAP, +${waterGain.toInt()} WATER, +${rationsGain.toInt()} FOOD!"
                )
            }
        }
    }

    fun smeltSalvageForGold() {
        viewModelScope.launch {
            val current = uiState.value
            val state = current.state
            val scrapCost = 18.0
            val waterCost = 10.0
            val fuelCost = 6.0
            if (state.scrap < scrapCost || state.water < waterCost || state.bioFuel < fuelCost) return@launch

            val yardLv = current.buildingsMap[BuildingType.SALVAGE_YARD.id] ?: 0
            val refineryLv = current.buildingsMap[BuildingType.BIO_SLUDGE_REFINERY.id] ?: 0
            val rankMerchant = current.skillsMap["WRK_GUILD_DRILLS"] ?: 0
            val goldYield = (28.0 * (1.0 + yardLv * 0.15 + refineryLv * 0.15 + rankMerchant * 0.15)).roundToInt().toDouble()

            val updated = applyXpAndResourceDelta(
                baseState = state.copy(
                    scrap = state.scrap - scrapCost,
                    water = state.water - waterCost,
                    bioFuel = state.bioFuel - fuelCost,
                    totalGoldSalvaged = state.totalGoldSalvaged + goldYield
                ),
                goldDelta = goldYield,
                xpDelta = 18.0,
                storageCap = current.production.storageCap
            )
            repository.updateOutpostState(updated)
            overlayFlow.update {
                it.copy(lastSalvageFeedback = "Smelted Gathered Salvage into +${goldYield.toInt()} GOLD Bullion & +18 XP!")
            }
            repository.logEvent("SMELTER", "Smelted salvaged Scrap, Water & Fuel into +${goldYield.toInt()} Gold Sovereigns!")
        }
    }

    fun prospectRichVein() {
        viewModelScope.launch {
            val current = uiState.value
            val state = current.state
            val fuelCost = 12.0
            val techCost = 12.0
            if (state.bioFuel < fuelCost || state.researchData < techCost) return@launch

            val updated = state.copy(
                bioFuel = state.bioFuel - fuelCost,
                researchData = state.researchData - techCost,
                salvageComboCount = (state.salvageComboCount + 5).coerceAtMost(15)
            )
            repository.updateOutpostState(updated)

            overlayFlow.update { ov ->
                ov.copy(
                    lastSalvageFeedback = "Deep-Seismic Prospecting complete: High-Gold Veins exposed & +5x Salvage Combo!",
                    salvageNodes = listOf(
                        SalvageNodeSlot(0, SalvageNodeType.ENCRYPTED_ORBITAL_POD, 0, SalvageNodeType.ENCRYPTED_ORBITAL_POD.maxDurability),
                        SalvageNodeSlot(1, SalvageNodeType.RADIOACTIVE_SLUDGE_TANKER, 0, SalvageNodeType.RADIOACTIVE_SLUDGE_TANKER.maxDurability),
                        SalvageNodeSlot(2, SalvageNodeType.BIOLUMINESCENT_METEOR_VEIN, 0, SalvageNodeType.BIOLUMINESCENT_METEOR_VEIN.maxDurability)
                    )
                )
            }
            repository.logEvent("PROSPECT", "Used gathered Fuel & Tech to expose 3 high-value Gold & Core veins!")
        }
    }

    fun upgradeSalvageRig() {
        viewModelScope.launch {
            val current = uiState.value
            val state = current.state
            val maxAllowed = current.production.maxPickaxeLevelAllowed
            if (state.salvageRigLevel >= maxAllowed) {
                overlayFlow.update {
                    it.copy(
                        lastSalvageFeedback = "Pickaxe capped at Lv.$maxAllowed! Build or upgrade Vault Ore & Gold Smeltery (+2 Max Pickaxe Lv per Room Lv)."
                    )
                }
                return@launch
            }
            val (scrapCost, fuelCost) = OutpostCalculator.computeSalvageRigUpgradeCost(state.salvageRigLevel)
            if (state.scrap < scrapCost || state.bioFuel < fuelCost) return@launch

            val newRigLv = state.salvageRigLevel + 1
            val updated = applyXpAndResourceDelta(
                baseState = state.copy(
                    salvageRigLevel = newRigLv,
                    scrap = state.scrap - scrapCost,
                    bioFuel = state.bioFuel - fuelCost
                ),
                xpDelta = 24.0 * newRigLv,
                storageCap = current.production.storageCap
            )
            repository.updateOutpostState(updated)
            repository.logEvent("RIG UPGRADE", "Upgraded Personal Plasma Pickaxe to Mk.$newRigLv (+42% Salvage & Gold Yield)!")
        }
    }

    fun hireCandidate(candidate: HireableCandidate) {
        viewModelScope.launch {
            val current = uiState.value
            val state = current.state
            val snap = current.production

            val effectiveMaxDwellers = minOf(snap.maxWorkerCapacity, OutpostCalculator.MAX_VAULT_DWELLERS)
            if (state.totalSurvivors >= effectiveMaxDwellers) return@launch
            if (state.rations < candidate.rationsCost ||
                state.water < candidate.waterCost ||
                state.scrap < candidate.scrapCost ||
                state.gold < candidate.goldSigningFee
            ) return@launch

            // 8% Rare Chance to gain a Unique Vault Dweller (either already rolled on the board or awakened upon hiring!)
            val isUniqueDwellerGained = candidate.isUniqueDweller ||
                (Random.nextInt(100) < CaravanCandidateGenerator.UNIQUE_DWELLER_CHANCE_PERCENT)
            val newUniqueCount = state.uniqueDwellersCount + (if (isUniqueDwellerGained) 1 else 0)

            val newTotalSurvivors = (state.totalSurvivors + 1).coerceAtMost(OutpostCalculator.MAX_VAULT_DWELLERS)
            val updatedState = applyXpAndResourceDelta(
                baseState = state.copy(
                    totalSurvivors = newTotalSurvivors,
                    uniqueDwellersCount = newUniqueCount,
                    rations = state.rations - candidate.rationsCost,
                    water = state.water - candidate.waterCost,
                    scrap = state.scrap - candidate.scrapCost,
                    gold = state.gold - candidate.goldSigningFee
                ),
                xpDelta = if (isUniqueDwellerGained) 65.0 else 30.0,
                storageCap = snap.storageCap
            )
            repository.updateOutpostState(updatedState)

            val wType = candidate.specialtyClass
            val existingClass = current.workersMap[wType.id] ?: WorkerClassEntity(wType.id, 0, 1, 0.0)
            val extraUniqueClassXp = if (isUniqueDwellerGained && !candidate.isUniqueDweller) candidate.bonusClassXp else 0.0
            var newXp = existingClass.classXp + candidate.bonusClassXp + extraUniqueClassXp
            var newLv = existingClass.classLevel
            var req = WorkerClassType.xpForNextClassLevel(newLv)
            while (newXp >= req && newLv < 50) {
                newXp -= req
                newLv++
                req = WorkerClassType.xpForNextClassLevel(newLv)
            }
            repository.updateWorkerClass(
                existingClass.copy(
                    assignedCount = existingClass.assignedCount + 1,
                    classLevel = newLv,
                    classXp = newXp
                )
            )

            val rankDrills = current.skillsMap["WRK_GUILD_DRILLS"] ?: 0
            val discountMult = (1.0 - rankDrills * 0.10).coerceAtLeast(0.50)
            overlayFlow.update { ov ->
                val remaining = ov.hireableCandidates.filterNot { it.id == candidate.id }
                val nextBoard = if (remaining.isEmpty()) {
                    CaravanCandidateGenerator.generateBoard(newTotalSurvivors, updatedState.commandLevel, discountMult)
                } else {
                    val replacement = CaravanCandidateGenerator.generateBoard(newTotalSurvivors, updatedState.commandLevel, discountMult).first()
                    remaining + replacement
                }
                val uniqueBanner = if (isUniqueDwellerGained) {
                    LevelUpBannerState(
                        title = "★ UNIQUE VAULT DWELLER GAINED (8% RARE)!",
                        subtitle = "${candidate.name} \"${candidate.callsign}\" (${wType.displayName})",
                        rewardSummary = "+40% All-Resource Gathering Ability (Total Unique Bonus: +${newUniqueCount * 40}%)",
                        isCommandLevel = true
                    )
                } else ov.levelUpBanner
                ov.copy(
                    hireableCandidates = nextBoard,
                    levelUpBanner = uniqueBanner
                )
            }

            if (isUniqueDwellerGained) {
                repository.logEvent(
                    "UNIQUE DWELLER",
                    "★ RARE 8% ROLL! Gained Unique Vault Dweller ${candidate.name} \"${candidate.callsign}\" (+40% All-Resource Gathering Ability, $newUniqueCount Unique Dweller(s) total)!"
                )
            } else {
                repository.logEvent(
                    "HIRED",
                    "Hired ${candidate.name} \"${candidate.callsign}\" as a ${wType.displayName} (${newTotalSurvivors}/${snap.maxWorkerCapacity} Workers)."
                )
            }
        }
    }

    fun refreshHiringBoard() {
        viewModelScope.launch {
            val current = uiState.value
            val state = current.state
            if (state.scrap < 6.0) return@launch

            repository.updateOutpostState(state.copy(scrap = state.scrap - 6.0))
            val rankDrills = current.skillsMap["WRK_GUILD_DRILLS"] ?: 0
            val discountMult = (1.0 - rankDrills * 0.10).coerceAtLeast(0.50)
            overlayFlow.update {
                it.copy(
                    hireableCandidates = CaravanCandidateGenerator.generateBoard(
                        state.totalSurvivors,
                        state.commandLevel,
                        discountMult
                    )
                )
            }
            repository.logEvent("CARAVAN", "Broadcasted beacon (-6 Scrap): New worker contracts arrived (8% Unique Dweller chance per slot)!")
        }
    }

    fun launchRescueBeacon() {
        viewModelScope.launch {
            val current = uiState.value
            val state = current.state
            val snap = current.production
            val effectiveMaxDwellers = minOf(snap.maxWorkerCapacity, OutpostCalculator.MAX_VAULT_DWELLERS)
            if (state.totalSurvivors >= effectiveMaxDwellers) return@launch
            val cost = OutpostCalculator.computeRescueBeaconCost(
                state = state,
                maxWorkerCapacity = effectiveMaxDwellers,
                skillsMap = current.skillsMap
            )
            if (!cost.canAfford) return@launch

            val isUniqueRescued = Random.nextInt(100) < CaravanCandidateGenerator.UNIQUE_DWELLER_CHANCE_PERCENT
            val newUniqueCount = state.uniqueDwellersCount + (if (isUniqueRescued) 1 else 0)
            val newTotalCitizens = (state.totalSurvivors + 1).coerceAtMost(OutpostCalculator.MAX_VAULT_DWELLERS)
            val updatedState = applyXpAndResourceDelta(
                baseState = state.copy(
                    totalSurvivors = newTotalCitizens,
                    uniqueDwellersCount = newUniqueCount,
                    bioFuel = state.bioFuel - cost.bioFuelCost,
                    rations = state.rations - cost.rationsCost,
                    water = state.water - cost.waterCost
                ),
                xpDelta = if (isUniqueRescued) 70.0 else 36.0,
                storageCap = snap.storageCap
            )
            repository.updateOutpostState(updatedState)

            val targetGuild = WorkerClassType.entries.minByOrNull {
                current.workersMap[it.id]?.assignedCount ?: 0
            } ?: WorkerClassType.SCRAPPER
            val existingClass = current.workersMap[targetGuild.id]
                ?: WorkerClassEntity(targetGuild.id, 0, 1, 0.0)
            var newXp = existingClass.classXp + (if (isUniqueRescued) 100.0 else 40.0)
            var newLv = existingClass.classLevel
            var req = WorkerClassType.xpForNextClassLevel(newLv)
            while (newXp >= req && newLv < 50) {
                newXp -= req
                newLv++
                req = WorkerClassType.xpForNextClassLevel(newLv)
            }
            repository.updateWorkerClass(
                existingClass.copy(
                    assignedCount = existingClass.assignedCount + 1,
                    classLevel = newLv,
                    classXp = newXp
                )
            )

            if (isUniqueRescued) {
                overlayFlow.update {
                    it.copy(
                        levelUpBanner = LevelUpBannerState(
                            title = "★ UNIQUE VAULT DWELLER RESCUED (8% RARE)!",
                            subtitle = "Elite ${targetGuild.displayName} Joined Vault City",
                            rewardSummary = "+40% All-Resource Gathering Ability (Total Unique Bonus: +${newUniqueCount * 40}%)",
                            isCommandLevel = true
                        )
                    )
                }
            }
            repository.logEvent(
                "RESCUE BEACON",
                if (isUniqueRescued) {
                    "★ RARE 8% ROLL! Rescue Beacon guided a Unique Vault Dweller into Vault City (+40% All-Resource Gathering Ability)!"
                } else {
                    "Signal Beacon guided a stranded survivor into the outpost (+1 ${targetGuild.displayName}, $newTotalCitizens/${snap.maxWorkerCapacity} Citizens)!"
                }
            )
        }
    }

    fun upgradeBuilding(buildingType: BuildingType) {
        viewModelScope.launch {
            val current = uiState.value
            if (current.activeConstruction != null) {
                overlayFlow.update {
                    it.copy(
                        lastSalvageFeedback = "Vault Construction Crew busy building ${current.activeConstruction.buildingType.displayName} (${current.activeConstruction.remainingSeconds}s left)! Hammer & Weld or Rush to finish faster."
                    )
                }
                return@launch
            }
            val state = current.state
            val currentLevel = current.buildingsMap[buildingType.id] ?: 0
            val cost = OutpostCalculator.computeBuildingUpgradeCost(
                buildingType = buildingType,
                currentLevel = currentLevel,
                state = state,
                skillsMap = current.skillsMap,
                workersMap = current.workersMap,
                currentTotalMwGenerated = current.production.totalMegawattGenerated,
                currentTotalMwDemand = current.production.totalMegawattDemand
            )
            if (!cost.canAfford) return@launch

            val newLevel = currentLevel + 1
            val updatedState = applyXpAndResourceDelta(
                baseState = state.copy(
                    scrap = state.scrap - cost.scrapCost,
                    water = state.water - cost.waterCost,
                    bioFuel = state.bioFuel - cost.fuelCost,
                    mutagenCores = state.mutagenCores - cost.coresCost,
                    activeConstructionBuildingId = buildingType.id,
                    activeConstructionTargetLevel = newLevel,
                    activeConstructionRemainingSeconds = cost.buildTimeSeconds,
                    activeConstructionTotalSeconds = cost.buildTimeSeconds,
                    activeConstructionRushGoldCost = cost.rushGoldCost
                ),
                xpDelta = 10.0,
                storageCap = current.production.storageCap
            )
            repository.updateOutpostState(updatedState)
            overlayFlow.update {
                it.copy(
                    activeConstruction = ActiveBuildingConstruction(
                        buildingType = buildingType,
                        targetLevel = newLevel,
                        remainingSeconds = cost.buildTimeSeconds,
                        totalSeconds = cost.buildTimeSeconds,
                        rushGoldCost = cost.rushGoldCost
                    ),
                    lastSalvageFeedback = "Started constructing ${buildingType.displayName} Lv.$newLevel (${cost.buildTimeSeconds}s • +${cost.additionalMegawattDraw} MW draw)!"
                )
            }
            repository.logEvent(
                "BUILD STARTED",
                "Started constructing ${buildingType.displayName} Lv.$newLevel (${cost.buildTimeSeconds}s build time, ${cost.nextLevelMegawattDraw} MW total draw)."
            )
        }
    }

    fun accelerateBuildingConstruction() {
        viewModelScope.launch {
            val current = uiState.value
            val activeBuild = current.activeConstruction ?: return@launch
            val state = current.state
            val shaveSeconds = 4

            if (activeBuild.remainingSeconds <= shaveSeconds) {
                repository.updateBuildingLevel(activeBuild.buildingType.id, activeBuild.targetLevel)
                val xpGain = 26.0 + activeBuild.targetLevel * 14.0
                val updated = applyXpAndResourceDelta(
                    baseState = state.copy(
                        activeConstructionBuildingId = "",
                        activeConstructionTargetLevel = 0,
                        activeConstructionRemainingSeconds = 0,
                        activeConstructionTotalSeconds = 0,
                        activeConstructionRushGoldCost = 0.0
                    ),
                    xpDelta = xpGain,
                    storageCap = current.production.storageCap
                )
                repository.updateOutpostState(updated)
                val doneMsg = "CONSTRUCTION COMPLETE: ${activeBuild.buildingType.displayName} reached Lv.${activeBuild.targetLevel} (+${xpGain.toInt()} XP)!"
                overlayFlow.update {
                    it.copy(
                        activeConstruction = null,
                        lastSalvageFeedback = doneMsg
                    )
                }
                repository.logEvent("BUILD COMPLETE", doneMsg)
            } else {
                val nextRemaining = activeBuild.remainingSeconds - shaveSeconds
                val updated = applyXpAndResourceDelta(
                    baseState = state.copy(
                        activeConstructionRemainingSeconds = nextRemaining
                    ),
                    xpDelta = 4.0,
                    storageCap = current.production.storageCap
                )
                repository.updateOutpostState(updated)
                overlayFlow.update {
                    it.copy(
                        activeConstruction = activeBuild.copy(remainingSeconds = nextRemaining)
                    )
                }
            }
        }
    }

    fun rushBuildingConstructionWithGold() {
        viewModelScope.launch {
            val current = uiState.value
            val activeBuild = current.activeConstruction ?: return@launch
            val state = current.state
            if (state.gold < activeBuild.rushGoldCost) return@launch

            repository.updateBuildingLevel(activeBuild.buildingType.id, activeBuild.targetLevel)
            val xpGain = 30.0 + activeBuild.targetLevel * 15.0
            val updated = applyXpAndResourceDelta(
                baseState = state.copy(
                    gold = state.gold - activeBuild.rushGoldCost,
                    activeConstructionBuildingId = "",
                    activeConstructionTargetLevel = 0,
                    activeConstructionRemainingSeconds = 0,
                    activeConstructionTotalSeconds = 0,
                    activeConstructionRushGoldCost = 0.0
                ),
                xpDelta = xpGain,
                storageCap = current.production.storageCap
            )
            repository.updateOutpostState(updated)
            val doneMsg = "RUSHED CONSTRUCTION: ${activeBuild.buildingType.displayName} Lv.${activeBuild.targetLevel} finished immediately (-${activeBuild.rushGoldCost.toInt()} Gold, +${xpGain.toInt()} XP)!"
            overlayFlow.update {
                it.copy(
                    activeConstruction = null,
                    lastSalvageFeedback = doneMsg
                )
            }
            repository.logEvent("BUILD RUSHED", doneMsg)
        }
    }

    // --- HARDCORE MINING, BUILDING & HUNTING MAINTENANCE ACTIONS ---

    fun sharpenPickaxe() {
        viewModelScope.launch {
            val current = uiState.value
            val state = current.state
            if (state.pickaxeDurabilityPercent >= 100) return@launch
            val scrapCost = (10.0 + state.salvageRigLevel * 3.0).roundToInt().toDouble()
            if (state.scrap < scrapCost) {
                overlayFlow.update {
                    it.copy(lastSalvageFeedback = "Need ${scrapCost.toInt()} Scrap to sharpen & re-tip Pickaxe!")
                }
                return@launch
            }
            val updated = applyXpAndResourceDelta(
                baseState = state.copy(
                    scrap = state.scrap - scrapCost,
                    pickaxeDurabilityPercent = 100
                ),
                xpDelta = 12.0,
                storageCap = current.production.storageCap
            )
            repository.updateOutpostState(updated)
            val msg = "PICKAXE SHARPENED: Restored Pickaxe Durability to 100% (-${scrapCost.toInt()} Scrap, +12 XP)!"
            overlayFlow.update { it.copy(lastSalvageFeedback = msg) }
            repository.logEvent("MINING", msg)
        }
    }

    fun coolMiningShaft() {
        viewModelScope.launch {
            val current = uiState.value
            val state = current.state
            if (state.miningShaftHeatPercent <= 0) return@launch
            val waterCost = 8.0
            if (state.water < waterCost) {
                overlayFlow.update {
                    it.copy(lastSalvageFeedback = "Need 8 Water to flush & cool the deep mining shaft!")
                }
                return@launch
            }
            val newHeat = (state.miningShaftHeatPercent - 55).coerceAtLeast(0)
            val updated = applyXpAndResourceDelta(
                baseState = state.copy(
                    water = state.water - waterCost,
                    miningShaftHeatPercent = newHeat
                ),
                xpDelta = 10.0,
                storageCap = current.production.storageCap
            )
            repository.updateOutpostState(updated)
            val msg = "SHAFT THERMAL FLUSH: Cooled bedrock shaft heat to $newHeat% (-8 Water, +10 XP)!"
            overlayFlow.update { it.copy(lastSalvageFeedback = msg) }
            repository.logEvent("MINING", msg)
        }
    }

    fun clearCaveInHazard() {
        viewModelScope.launch {
            val current = uiState.value
            val state = current.state
            if (!state.activeCaveInHazard) return@launch
            val scrapCost = 14.0
            if (state.scrap < scrapCost) {
                overlayFlow.update {
                    it.copy(lastSalvageFeedback = "Need 14 Scrap for steel shoring beams to clear the Cave-In!")
                }
                return@launch
            }
            val updated = applyXpAndResourceDelta(
                baseState = state.copy(
                    scrap = state.scrap - scrapCost,
                    activeCaveInHazard = false,
                    miningShaftHeatPercent = (state.miningShaftHeatPercent - 35).coerceAtLeast(0)
                ),
                xpDelta = 18.0,
                storageCap = current.production.storageCap
            )
            repository.updateOutpostState(updated)
            val msg = "CAVE-IN SHORED UP: Cleared rubble collapse & restored 100% vein mining access (-14 Scrap, +18 XP)!"
            overlayFlow.update { it.copy(lastSalvageFeedback = msg) }
            repository.logEvent("MINING", msg)
        }
    }

    fun weldRoomPipesAndWear() {
        viewModelScope.launch {
            val current = uiState.value
            val state = current.state
            if (state.activePipeLeaksCount <= 0 && state.roomStructuralWearPercent <= 0) return@launch
            val scrapCost = (12.0 + state.activePipeLeaksCount * 6.0 + state.roomStructuralWearPercent * 0.25).roundToInt().toDouble()
            if (state.scrap < scrapCost) {
                overlayFlow.update {
                    it.copy(lastSalvageFeedback = "Need ${scrapCost.toInt()} Scrap to weld coolant pipe leaks & reinforce room bulkheads!")
                }
                return@launch
            }
            val updated = applyXpAndResourceDelta(
                baseState = state.copy(
                    scrap = state.scrap - scrapCost,
                    activePipeLeaksCount = 0,
                    roomStructuralWearPercent = 0
                ),
                xpDelta = 20.0,
                storageCap = current.production.storageCap
            )
            repository.updateOutpostState(updated)
            val msg = "PIPES WELDED & ROOMS REINFORCED: 0 Pipe Leaks & 0% Room Structural Wear (-${scrapCost.toInt()} Scrap, +20 XP)!"
            overlayFlow.update { it.copy(lastSalvageFeedback = msg) }
            repository.logEvent("MAINTENANCE", msg)
        }
    }

    fun treatHuntingInjuries() {
        viewModelScope.launch {
            val current = uiState.value
            val state = current.state
            if (state.bleedingDwellersCount <= 0 && state.venomInfectedDwellersCount <= 0 && state.injuredDwellersCount <= 0) return@launch
            val useMedkit = state.medkitsCount > 0
            val useRadAway = state.radAwayCount > 0
            val fallbackWater = if (!useMedkit && !useRadAway) 8.0 else 0.0
            val fallbackRations = if (!useMedkit && !useRadAway) 8.0 else 0.0
            if (!useMedkit && !useRadAway && (state.water < fallbackWater || state.rations < fallbackRations)) {
                overlayFlow.update {
                    it.copy(lastDefenseDamageFeedback = "Need 1 Medkit/RadAway or 8 Water + 8 Food to suture bleeding & purge beast venom!")
                }
                return@launch
            }
            val updated = applyXpAndResourceDelta(
                baseState = state.copy(
                    medkitsCount = (state.medkitsCount - (if (useMedkit) 1 else 0)).coerceAtLeast(0),
                    radAwayCount = (state.radAwayCount - (if (!useMedkit && useRadAway) 1 else 0)).coerceAtLeast(0),
                    water = (state.water - fallbackWater).coerceAtLeast(0.0),
                    rations = (state.rations - fallbackRations).coerceAtLeast(0.0),
                    bleedingDwellersCount = 0,
                    venomInfectedDwellersCount = 0,
                    injuredDwellersCount = 0,
                    dwellerHealthPercent = (state.dwellerHealthPercent + 25).coerceAtMost(100)
                ),
                xpDelta = 16.0,
                storageCap = current.production.storageCap
            )
            repository.updateOutpostState(updated)
            val msg = "HUNTER TRAUMA TREATED: Sutured all Beast Bleeding & neutralized Rad-Venom (+25% Dweller Health, +16 XP)!"
            overlayFlow.update {
                it.copy(
                    lastDefenseDamageFeedback = msg,
                    lastSalvageFeedback = msg
                )
            }
            repository.logEvent("MEDBAY", msg)
        }
    }

    fun assignWorker(workerType: WorkerClassType, delta: Int) {
        viewModelScope.launch {
            val current = uiState.value
            val entity = current.workersMap[workerType.id] ?: WorkerClassEntity(workerType.id, 0, 1, 0.0)
            val totalAssigned = current.production.totalAssignedWorkers
            val idlePool = current.state.totalSurvivors - totalAssigned

            if (delta > 0 && idlePool <= 0) return@launch
            if (delta < 0 && entity.assignedCount <= 0) return@launch

            val newCount = (entity.assignedCount + delta).coerceAtLeast(0)
            repository.updateWorkerClass(entity.copy(assignedCount = newCount))
        }
    }

    fun toggleAutoShift(enabled: Boolean) {
        viewModelScope.launch {
            val state = uiState.value.state
            repository.updateOutpostState(state.copy(autoShiftEnabled = enabled))
            if (enabled && state.totalSurvivors > 0) {
                val phase = DayNightPhase.fromCycleProgress(state.cycleProgress)
                autoRebalanceWorkersForPhase(phase, state.totalSurvivors, uiState.value.workersMap)
                repository.logEvent("GUILD", "Circadian Auto-Shift enabled: Hired crew aligned to ${phase.displayName}.")
            }
        }
    }

    fun optimizeCrewForCurrentPhase() {
        viewModelScope.launch {
            val current = uiState.value
            val phase = current.production.activePhase
            autoRebalanceWorkersForPhase(phase, current.state.totalSurvivors, current.workersMap)
            repository.logEvent("GUILD", "Re-assigned ${current.state.totalSurvivors} hired workers for ${phase.displayName}.")
        }
    }

    private suspend fun autoRebalanceWorkersForPhase(
        phase: DayNightPhase,
        totalSurvivors: Int,
        workersMap: Map<String, WorkerClassEntity>
    ) {
        if (totalSurvivors <= 0) return
        val weights = when (phase) {
            DayNightPhase.DAWN -> mapOf(
                WorkerClassType.SCRAPPER to 0.30,
                WorkerClassType.HYDRO_ENGINEER to 0.45,
                WorkerClassType.NIGHT_STALKER to 0.05,
                WorkerClassType.TECH_SAVANT to 0.20
            )
            DayNightPhase.SCORCHING_DAY -> mapOf(
                WorkerClassType.SCRAPPER to 0.50,
                WorkerClassType.HYDRO_ENGINEER to 0.25,
                WorkerClassType.NIGHT_STALKER to 0.05,
                WorkerClassType.TECH_SAVANT to 0.20
            )
            DayNightPhase.DUSK -> mapOf(
                WorkerClassType.SCRAPPER to 0.20,
                WorkerClassType.HYDRO_ENGINEER to 0.30,
                WorkerClassType.NIGHT_STALKER to 0.25,
                WorkerClassType.TECH_SAVANT to 0.25
            )
            DayNightPhase.RAD_NIGHT -> mapOf(
                WorkerClassType.SCRAPPER to 0.15,
                WorkerClassType.HYDRO_ENGINEER to 0.20,
                WorkerClassType.NIGHT_STALKER to 0.40,
                WorkerClassType.TECH_SAVANT to 0.25
            )
        }

        var remaining = totalSurvivors
        val allocations = mutableMapOf<WorkerClassType, Int>()
        val entries = WorkerClassType.entries
        for (i in 0 until entries.size - 1) {
            val wType = entries[i]
            val count = (totalSurvivors * (weights[wType] ?: 0.25)).roundToInt().coerceIn(0, remaining)
            allocations[wType] = count
            remaining -= count
        }
        allocations[entries.last()] = remaining.coerceAtLeast(0)

        for ((wType, count) in allocations) {
            val existing = workersMap[wType.id] ?: WorkerClassEntity(wType.id, 0, 1, 0.0)
            repository.updateWorkerClass(existing.copy(assignedCount = count))
        }
    }

    fun selectSkillBranch(branch: SkillBranch) {
        overlayFlow.update { it.copy(selectedSkillBranch = branch) }
    }

    fun unlockOrUpgradeSkill(node: SkillNodeDefinition) {
        viewModelScope.launch {
            val current = uiState.value
            val currentRank = current.skillsMap[node.id] ?: 0
            val cost = OutpostCalculator.computeSkillUpgradeCost(
                node = node,
                currentRank = currentRank,
                state = current.state,
                skillsMap = current.skillsMap
            )
            if (!cost.canAfford) return@launch

            val newRank = currentRank + 1
            repository.updateSkillRank(node.id, newRank)
            repository.updateOutpostState(
                current.state.copy(
                    availableSkillPoints = (current.state.availableSkillPoints - cost.skillPointCost).coerceAtLeast(0),
                    researchData = (current.state.researchData - cost.researchCost).coerceAtLeast(0.0),
                    gold = (current.state.gold - cost.goldCost).coerceAtLeast(0.0),
                    mutagenCores = (current.state.mutagenCores - cost.coresCost).coerceAtLeast(0.0),
                    prestigeCores = (current.state.prestigeCores - cost.prestigeCoreCost).coerceAtLeast(0)
                )
            )
            repository.logEvent(
                "SKILL TREE",
                "Researched ${node.title} (Rank $newRank/${node.maxRank}): ${node.effectSummaryPerRank}"
            )
        }
    }

    fun walkOverseerToStation(station: VaultMapStation) {
        viewModelScope.launch {
            val currentStation = overlayFlow.value.overseerStation
            if (currentStation == station) {
                overlayFlow.update {
                    it.copy(lastSalvageFeedback = "Overseer stationed at ${station.shortTitle} — Ready to interact!")
                }
                return@launch
            }
            val floorDist = abs(station.floorIndex - currentStation.floorIndex)
            val wingDist = abs(station.wingColumn - currentStation.wingColumn)
            val walkDurationMs = ((floorDist * 140L) + (wingDist * 110L)).coerceIn(150L, 650L)

            overlayFlow.update {
                it.copy(
                    isOverseerWalking = true,
                    lastSalvageFeedback = "Overseer walking via Vault Elevator to ${station.shortTitle}..."
                )
            }
            delay(walkDurationMs)
            overlayFlow.update {
                it.copy(
                    overseerStation = station,
                    isOverseerWalking = false,
                    lastSalvageFeedback = "Arrived at ${station.shortTitle}! You can now upgrade, mine, or fish here."
                )
            }
        }
    }

    fun issueEmergencyWorkerRations() {
        viewModelScope.launch {
            val current = uiState.value
            val state = current.state
            val cap = current.production.storageCap
            val scrapFee = if (state.scrap >= 6.0) 6.0 else 0.0
            val updated = applyXpAndResourceDelta(
                baseState = state.copy(
                    scrap = (state.scrap - scrapFee).coerceAtLeast(0.0),
                    dwellerHealthPercent = (state.dwellerHealthPercent + 10).coerceAtMost(100)
                ),
                waterDelta = 14.0,
                rationsDelta = 14.0,
                xpDelta = 8.0,
                storageCap = cap
            )
            repository.updateOutpostState(updated)
            val msg = "EMERGENCY WORKER RATIONS ISSUED: +14 Food & +14 Water distributed to Vault Crew! Workers resumed room & defense operations."
            overlayFlow.update { it.copy(lastSalvageFeedback = msg) }
            repository.logEvent("WORKER RATIONS", msg)
        }
    }

    fun performOverseerPrestige(isGoogleSignedIn: Boolean = true) {
        viewModelScope.launch {
            if (!isGoogleSignedIn) {
                val msg = "GOOGLE SIGN-IN REQUIRED: Sign in with Google to Prestige, Save Progress to Cloud & use Multiplayer!"
                overlayFlow.update {
                    it.copy(
                        lastSalvageFeedback = msg,
                        publicNetworkFeedback = msg
                    )
                }
                return@launch
            }
            val current = uiState.value
            val state = current.state
            val preview = OutpostCalculator.computePrestigePreview(
                state = state,
                buildingsMap = current.buildingsMap,
                skillsMap = current.skillsMap
            )
            if (!preview.canAscend) {
                overlayFlow.update {
                    it.copy(lastSalvageFeedback = preview.requirementSummary)
                }
                return@launch
            }

            val rankCryo = current.skillsMap["LEG_CRYO_AWAKENING"] ?: 0
            val newPrestigeLevel = state.prestigeLevel + 1
            val newPrestigeCores = state.prestigeCores + preview.coresToEarn
            val newTotalPrestigeCores = state.totalPrestigeCoresEarned + preview.coresToEarn

            val startScrap = 25.0 + rankCryo * 40.0
            val startFood = 16.0 + rankCryo * 25.0
            val startWater = 16.0 + rankCryo * 25.0
            val startFuel = 10.0 + rankCryo * 15.0
            val startGold = 15.0 + newPrestigeLevel * 10.0

            val ascendedState = OutpostStateEntity(
                id = 1,
                commandLevel = 1,
                currentXp = 0.0,
                availableSkillPoints = 1 + newPrestigeLevel,
                totalSkillPointsEarned = 1 + newPrestigeLevel,
                scrap = startScrap,
                water = startWater,
                rations = startFood,
                bioFuel = startFuel,
                researchData = (rankCryo * 15.0),
                gold = startGold,
                thoriumRods = state.thoriumRods,
                aetherCells = state.aetherCells,
                mutagenCores = state.mutagenCores,
                salvageRigLevel = 1 + (newPrestigeLevel / 2).coerceAtMost(3),
                blastDoorLevel = 1 + (newPrestigeLevel / 3).coerceAtMost(3),
                defenseIntegrityPercent = 100,
                needsDefenseRepair = false,
                oxygenPercent = 100,
                radiationMillisieverts = 0,
                dwellerHealthPercent = 100,
                generatorWearPercent = 0,
                ammoRounds = 30 + newPrestigeLevel * 10,
                medkitsCount = 2,
                radAwayCount = 2,
                prestigeLevel = newPrestigeLevel,
                prestigeCores = newPrestigeCores,
                totalPrestigeCoresEarned = newTotalPrestigeCores,
                uniqueDwellersCount = state.uniqueDwellersCount, // Preserve rare Unique Dweller lineage bonus!
                totalRaidsRepelled = state.totalRaidsRepelled,
                totalExpeditionsCompleted = state.totalExpeditionsCompleted,
                lastSavedTimestampMillis = System.currentTimeMillis()
            )

            repository.performPrestigeReset(ascendedState)

            val rankHydro = current.skillsMap["LEG_HYDRO_RECYCLING"] ?: 0
            overlayFlow.update {
                it.copy(
                    activeConstruction = null,
                    activeCrafting = null,
                    activeDoorUpgrade = null,
                    activeConvoy = null,
                    activeExpedition = null,
                    activeRaidThreat = createDailyRaidThreat(dayNumber = 1, commandLevel = 1, plasmaCount = 0),
                    hireableCandidates = CaravanCandidateGenerator.generateBoard(
                        totalHiredSoFar = 0,
                        commandLevel = 1,
                        discountMult = 1.0,
                        bonusUniqueChancePercent = rankHydro * 3
                    ),
                    levelUpBanner = LevelUpBannerState(
                        title = "★ OVERSEER ASCENSION: PRESTIGE RANK $newPrestigeLevel!",
                        subtitle = "Earned +${preview.coresToEarn} Permanent Prestige Cores ($newPrestigeCores Available)",
                        rewardSummary = "Global Production x${String.format("%.2f", preview.nextPrestigeMultiplier)} • Spend Prestige Cores in the Overseer Legacy Skill Branch!",
                        isCommandLevel = true
                    ),
                    lastSalvageFeedback = "ASCENDED TO PRESTIGE RANK $newPrestigeLevel! Earned +${preview.coresToEarn} Prestige Cores!"
                )
            }
            repository.logEvent(
                "PRESTIGE",
                "★ OVERSEER ASCENSION COMPLETE! Reached Prestige Rank $newPrestigeLevel and earned +${preview.coresToEarn} Permanent Prestige Cores ($newPrestigeCores total)!"
            )
        }
    }

    fun stopPublicNetworkSync() {
        activeSyncedUserId = null
        publicVaultsJob?.cancel()
        publicDropsJob?.cancel()
        publicVaultsJob = null
        publicDropsJob = null
        overlayFlow.update {
            it.copy(
                publicVaults = emptyList(),
                publicSupplyDrops = emptyList(),
                publicNetworkFeedback = "Signed out of Google. Playing in Local Offline Mode (Cloud Save, Prestige & Multiplayer disabled)."
            )
        }
    }

    fun startPublicNetworkSync(userId: String, overseerName: String) {
        val pubRepo = publicVaultRepository ?: return
        if (activeSyncedUserId == userId && publicVaultsJob?.isActive == true) return
        activeSyncedUserId = userId
        publicVaultsJob?.cancel()
        publicDropsJob?.cancel()

        publicVaultsJob = viewModelScope.launch {
            pubRepo.observePublicVaults()
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val rootError = firestoreError ?: error
                    overlayFlow.update {
                        it.copy(publicNetworkFeedback = "Public Vault Directory notice: ${rootError.message ?: "Offline"}")
                    }
                }
                .collect { vaults ->
                    overlayFlow.update { it.copy(publicVaults = vaults) }
                }
        }

        publicDropsJob = viewModelScope.launch {
            pubRepo.observePublicSupplyDrops()
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val rootError = firestoreError ?: error
                    overlayFlow.update {
                        it.copy(publicNetworkFeedback = "Supply Drop feed notice: ${rootError.message ?: "Offline"}")
                    }
                }
                .collect { drops ->
                    overlayFlow.update { it.copy(publicSupplyDrops = drops) }
                }
        }

        publishMyVaultToNetwork(overseerName = overseerName, vaultName = "Vault City #${userId.take(4).uppercase()}")
    }

    fun publishMyVaultToNetwork(overseerName: String, vaultName: String) {
        val pubRepo = publicVaultRepository ?: return
        viewModelScope.launch {
            val current = uiState.value
            val s = current.state
            val p = current.production
            val result = pubRepo.publishOrUpdateVaultProfile(
                overseerName = overseerName,
                vaultName = vaultName,
                prestigeLevel = s.prestigeLevel,
                commandLevel = s.commandLevel,
                dayCounter = s.dayCounter,
                totalSurvivors = s.totalSurvivors,
                uniqueDwellersCount = s.uniqueDwellersCount,
                totalDefenseRating = p.totalDefenseRating
            )
            result.onSuccess {
                val msg = "Synced $vaultName (Prestige ${s.prestigeLevel} • Lv.${s.commandLevel} • Day ${s.dayCounter}) to the Public Wasteland Leaderboard!"
                overlayFlow.update { it.copy(publicNetworkFeedback = msg) }
                repository.logEvent("PUBLIC NETWORK", msg)
            }.onFailure { err ->
                overlayFlow.update {
                    it.copy(publicNetworkFeedback = "Could not sync Vault: ${err.localizedMessage ?: "Check connection"}")
                }
            }
        }
    }

    fun broadcastSupplyDrop(
        senderName: String,
        resourceType: ResourceType,
        amount: Int = 25,
        message: String = "Surplus Vault City supplies for fellow Overseers!"
    ) {
        val pubRepo = publicVaultRepository ?: return
        viewModelScope.launch {
            val current = uiState.value
            val s = current.state
            val safeAmount = amount.coerceIn(10, 200)
            val hasEnough = when (resourceType) {
                ResourceType.SCRAP -> s.scrap >= safeAmount
                ResourceType.WATER -> s.water >= safeAmount
                ResourceType.RATIONS -> s.rations >= safeAmount
                ResourceType.BIO_FUEL -> s.bioFuel >= safeAmount
                else -> false
            }
            if (!hasEnough) {
                overlayFlow.update {
                    it.copy(publicNetworkFeedback = "Need at least $safeAmount ${resourceType.shortLabel} to broadcast a Public Supply Drop!")
                }
                return@launch
            }

            val updatedLocal = applyXpAndResourceDelta(
                baseState = s,
                scrapDelta = if (resourceType == ResourceType.SCRAP) -safeAmount.toDouble() else 0.0,
                waterDelta = if (resourceType == ResourceType.WATER) -safeAmount.toDouble() else 0.0,
                rationsDelta = if (resourceType == ResourceType.RATIONS) -safeAmount.toDouble() else 0.0,
                fuelDelta = if (resourceType == ResourceType.BIO_FUEL) -safeAmount.toDouble() else 0.0,
                goldDelta = 18.0,
                xpDelta = 35.0,
                storageCap = current.production.storageCap
            )
            repository.updateOutpostState(updatedLocal)

            val res = pubRepo.createSupplyDrop(
                senderName = senderName,
                resourceType = resourceType.name,
                amount = safeAmount,
                message = message
            )
            res.onSuccess {
                val msg = "Broadcasted Public Supply Drop ($safeAmount ${resourceType.shortLabel}) to other players! Earned +18 Gold & +35 XP."
                overlayFlow.update { it.copy(publicNetworkFeedback = msg) }
                repository.logEvent("SUPPLY DROP", msg)
            }.onFailure { err ->
                overlayFlow.update {
                    it.copy(publicNetworkFeedback = "Failed to broadcast Supply Drop: ${err.localizedMessage ?: "Error"}")
                }
            }
        }
    }

    fun claimPublicSupplyDrop(drop: PublicSupplyDrop, claimerName: String) {
        val pubRepo = publicVaultRepository ?: return
        if (drop.status != "open" || drop.id.isBlank()) return
        viewModelScope.launch {
            val res = pubRepo.claimSupplyDrop(dropId = drop.id, claimerName = claimerName)
            res.onSuccess {
                val current = uiState.value
                val amt = drop.amount.toDouble().coerceIn(5.0, 500.0)
                val updated = applyXpAndResourceDelta(
                    baseState = current.state,
                    scrapDelta = if (drop.resourceType == "SCRAP") amt else 0.0,
                    waterDelta = if (drop.resourceType == "WATER") amt else 0.0,
                    rationsDelta = if (drop.resourceType == "RATIONS") amt else 0.0,
                    fuelDelta = if (drop.resourceType == "BIO_FUEL") amt else 0.0,
                    xpDelta = 25.0,
                    storageCap = current.production.storageCap
                )
                repository.updateOutpostState(updated)
                val msg = "Claimed Public Supply Drop from ${drop.senderName}: +${amt.toInt()} ${drop.resourceType} & +25 XP!"
                overlayFlow.update { it.copy(publicNetworkFeedback = msg) }
                repository.logEvent("SUPPLY CLAIMED", msg)
            }.onFailure { err ->
                overlayFlow.update {
                    it.copy(publicNetworkFeedback = "Supply drop already claimed or unavailable: ${err.localizedMessage ?: ""}")
                }
            }
        }
    }

    fun triggerSolarFlare() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val current = uiState.value
            val state = current.state
            val fuelCost = 20.0
            if (state.solarFlareCooldownUntilMillis > now || state.bioFuel < fuelCost) return@launch

            val rankCap = current.skillsMap["CHR_PHASE_CAPACITORS"] ?: 0
            val savantLv = current.workersMap[WorkerClassType.TECH_SAVANT.id]?.classLevel ?: 1
            val savantCount = current.workersMap[WorkerClassType.TECH_SAVANT.id]?.assignedCount ?: 0
            val cooldownFactor = (1.0 - rankCap * 0.10) * (if (savantCount > 0 && savantLv >= 20) 0.80 else 1.0)
            val cooldownMs = (90_000L * cooldownFactor).toLong()

            repository.updateOutpostState(
                state.copy(
                    bioFuel = state.bioFuel - fuelCost,
                    solarFlareActiveUntilMillis = now + 35_000L,
                    solarFlareCooldownUntilMillis = now + cooldownMs
                )
            )
            repository.logEvent("PHASE SHIFT", "Ignited Solar Flare! Solar Power Towers (+65% MW) & Scrap surge for 35s.")
        }
    }

    fun triggerEclipseShroud() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val current = uiState.value
            val state = current.state
            val fuelCost = 25.0
            if (state.eclipseShroudCooldownUntilMillis > now || state.bioFuel < fuelCost) return@launch

            val rankCap = current.skillsMap["CHR_PHASE_CAPACITORS"] ?: 0
            val savantLv = current.workersMap[WorkerClassType.TECH_SAVANT.id]?.classLevel ?: 1
            val savantCount = current.workersMap[WorkerClassType.TECH_SAVANT.id]?.assignedCount ?: 0
            val cooldownFactor = (1.0 - rankCap * 0.10) * (if (savantCount > 0 && savantLv >= 20) 0.80 else 1.0)
            val cooldownMs = (105_000L * cooldownFactor).toLong()

            repository.updateOutpostState(
                state.copy(
                    bioFuel = state.bioFuel - fuelCost,
                    eclipseShroudActiveUntilMillis = now + 35_000L,
                    eclipseShroudCooldownUntilMillis = now + cooldownMs
                )
            )
            repository.logEvent("PHASE SHIFT", "Deployed Eclipse Shroud! Nocturnal Stalkers & Mutagen Core yields surge for 35s.")
        }
    }

    fun claimRareAnomaly() {
        viewModelScope.launch {
            val current = uiState.value
            val anomaly = current.activeAnomaly ?: return@launch
            val state = current.state

            val updatedState = applyXpAndResourceDelta(
                baseState = state.copy(totalAnomaliesClaimed = state.totalAnomaliesClaimed + 1),
                scrapDelta = anomaly.scrapReward,
                waterDelta = anomaly.waterReward,
                goldDelta = anomaly.goldReward,
                researchDelta = anomaly.researchReward,
                coresDelta = anomaly.mutagenCoresReward,
                xpDelta = anomaly.xpReward,
                storageCap = current.production.storageCap
            )
            overlayFlow.update { it.copy(activeAnomaly = null) }
            repository.updateOutpostState(updatedState)
            repository.logEvent(
                "ANOMALY",
                "Harvested ${anomaly.title}: +${anomaly.goldReward.toInt()} Gold, +${anomaly.mutagenCoresReward.toInt()} Cores & +${anomaly.xpReward.toInt()} XP!"
            )
        }
    }

    fun setOperationalPosture(posture: VaultOperationalPosture) {
        viewModelScope.launch {
            val current = uiState.value
            val updated = current.state.copy(operationalPostureId = posture.id)
            repository.updateOutpostState(updated)
            val msg = "VAULT POSTURE -> ${posture.title.uppercase()}: ${posture.summary}"
            overlayFlow.update { it.copy(lastSalvageFeedback = msg) }
            repository.logEvent("POSTURE", msg)
        }
    }

    fun executeSiegeCountermeasure(type: SiegeCountermeasureType) {
        viewModelScope.launch {
            val current = uiState.value
            val state = current.state
            val activeRaid = overlayFlow.value.activeRaidThreat ?: return@launch

            if (state.ammoRounds < type.ammoCost ||
                state.bioFuel < type.bioFuelCost ||
                state.scrap < type.scrapCost
            ) {
                overlayFlow.update {
                    it.copy(lastDefenseDamageFeedback = "Need resources for ${type.buttonLabel}!")
                }
                return@launch
            }

            val reductionFactor = (100 - type.defenseReductionPercent).coerceIn(45, 95) / 100.0
            val newReqDef = (activeRaid.requiredDefenseRating * reductionFactor).roundToInt().coerceAtLeast(10)
            val newRemainingSec = if (type == SiegeCountermeasureType.FLAK_VOLLEY) {
                (activeRaid.remainingSeconds + 10).coerceAtMost(60)
            } else {
                activeRaid.remainingSeconds
            }
            val newWeakenedPct = (activeRaid.weakenedByPercent + type.defenseReductionPercent).coerceAtMost(65)

            val newIntegrity = (state.defenseIntegrityPercent + type.bonusIntegrityRestore).coerceIn(20, 100)
            val updatedState = state.copy(
                ammoRounds = (state.ammoRounds - type.ammoCost).coerceAtLeast(0),
                bioFuel = (state.bioFuel - type.bioFuelCost).coerceAtLeast(0.0),
                scrap = (state.scrap - type.scrapCost).coerceAtLeast(0.0),
                defenseIntegrityPercent = newIntegrity,
                needsDefenseRepair = newIntegrity < 100
            )
            repository.updateOutpostState(updatedState)

            val updatedRaid = activeRaid.copy(
                requiredDefenseRating = newReqDef,
                remainingSeconds = newRemainingSec,
                countermeasuresUsedCount = activeRaid.countermeasuresUsedCount + 1,
                weakenedByPercent = newWeakenedPct
            )
            val msg = "COUNTERMEASURE DEPLOYED (${type.id}): ${activeRaid.title} weakened to $newReqDef DEF Req" +
                if (type.bonusIntegrityRestore > 0) " & Blast Door restored to $newIntegrity% Integrity!" else "!"
            overlayFlow.update {
                it.copy(
                    activeRaidThreat = updatedRaid,
                    lastDefenseDamageFeedback = msg
                )
            }
            repository.logEvent("COUNTERMEASURE", msg)
        }
    }

    fun callEmergencyReliefDrop() {
        viewModelScope.launch {
            val current = uiState.value
            val state = current.state
            val goldCost = 25.0
            if (state.gold < goldCost) {
                overlayFlow.update {
                    it.copy(lastSalvageFeedback = "Need 25 Gold Sovereigns to call an Emergency Faction Air-Drop!")
                }
                return@launch
            }
            val updated = applyXpAndResourceDelta(
                baseState = state.copy(
                    dwellerAppetiteEscalationSeconds = (state.dwellerAppetiteEscalationSeconds - 240).coerceAtLeast(0)
                ),
                scrapDelta = 45.0,
                waterDelta = 50.0,
                rationsDelta = 50.0,
                fuelDelta = 40.0,
                goldDelta = -goldCost,
                xpDelta = 30.0,
                storageCap = current.production.storageCap
            )
            repository.updateOutpostState(updated)
            val msg = "EMERGENCY FACTION AIR-DROP RECEIVED (-25 Gold): +45 Scrap, +50 Food, +50 Water, +40 Fuel & -2 Appetite Tiers!"
            overlayFlow.update { it.copy(lastSalvageFeedback = msg) }
            repository.logEvent("AIR-DROP", msg)
        }
    }

    fun dismissOfflineReport() {
        overlayFlow.update { it.copy(offlineReport = null) }
    }

    fun dismissLevelUpBanner() {
        overlayFlow.update { it.copy(levelUpBanner = null) }
    }

    private fun applyXpAndResourceDelta(
        baseState: OutpostStateEntity,
        scrapDelta: Double = 0.0,
        waterDelta: Double = 0.0,
        rationsDelta: Double = 0.0,
        fuelDelta: Double = 0.0,
        researchDelta: Double = 0.0,
        goldDelta: Double = 0.0,
        coresDelta: Double = 0.0,
        xpDelta: Double = 0.0,
        storageCap: Double
    ): OutpostStateEntity {
        var newXp = baseState.currentXp + xpDelta
        var newLevel = baseState.commandLevel
        var required = CommandLevelProgression.xpRequiredForLevel(newLevel)

        while (newXp >= required && newLevel < CommandLevelProgression.MAX_COMMAND_LEVEL) {
            newXp -= required
            newLevel++
            required = CommandLevelProgression.xpRequiredForLevel(newLevel)
        }

        return baseState.copy(
            commandLevel = newLevel,
            currentXp = newXp,
            scrap = (baseState.scrap + scrapDelta).coerceIn(0.0, storageCap),
            water = (baseState.water + waterDelta).coerceIn(0.0, storageCap),
            rations = (baseState.rations + rationsDelta).coerceIn(0.0, storageCap),
            bioFuel = (baseState.bioFuel + fuelDelta).coerceIn(0.0, storageCap),
            researchData = (baseState.researchData + researchDelta).coerceIn(0.0, storageCap),
            gold = (baseState.gold + goldDelta).coerceAtLeast(0.0),
            mutagenCores = (baseState.mutagenCores + coresDelta).coerceAtLeast(0.0)
        )
    }

    class Factory(
        private val repository: OutpostRepository,
        private val publicVaultRepository: PublicVaultRepository? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OutpostViewModel(repository, publicVaultRepository) as T
        }
    }
}
