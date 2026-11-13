package com.example.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.SystemClock
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.local.EventLogEntity
import com.example.data.local.OutpostDatabase
import com.example.data.local.OutpostStateEntity
import com.example.domain.OutpostCalculator
import com.example.domain.ProductionSnapshot
import com.example.model.SubterraneanBossType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

data class VaultWidgetSnapshot(
    val versionName: String = VaultStatusAppWidgetProvider.WIDGET_VERSION_NAME,
    val dayCounter: Int = 1,
    val commandLevel: Int = 1,
    val prestigeLevel: Int = 0,
    val scrap: Double = 0.0,
    val scrapPerSec: Double = 0.0,
    val water: Double = 6.0,
    val waterPerSec: Double = 0.0,
    val rations: Double = 6.0,
    val rationsPerSec: Double = 0.0,
    val bioFuel: Double = 0.0,
    val bioFuelPerSec: Double = 0.0,
    val gold: Double = 0.0,
    val goldPerSec: Double = 0.0,
    val ammoRounds: Int = 20,
    val totalDefenseRating: Int = 20,
    val todayRequiredDefense: Int = 0,
    val defenseIntegrityPercent: Int = 60,
    val totalRaidsRepelled: Int = 0,
    val lastAttackSucceeded: Boolean = true,
    val lastAttackSummary: String = "10-Day Prep Window • 0 Attacks Until Day 11",
    val totalMwGenerated: Double = 8.0,
    val totalMwDemand: Double = 0.0,
    val onlineRoomsCount: Int = 0,
    val totalBuiltRoomsCount: Int = 0,
    val isVaultBlackout: Boolean = false,
    val isDefenseShutDownByDeficit: Boolean = false,
    val isWorkersStarvingOrDehydrated: Boolean = false,
    val totalSurvivors: Int = 0,
    val maxWorkerCapacity: Int = 4,
    val dwellerHealthPercent: Int = 100,
    val dwellerStaminaPercent: Int = 100,
    val sickDwellersCount: Int = 0,
    val oxygenPercent: Int = 78,
    val radiationMillisieverts: Int = 15,
    val deepCoreDepthMeters: Int = 0,
    val activeBossTitle: String = "",
    val isPreparationPeaceWindow: Boolean = true,
    val preparationDaysRemaining: Int = 10,
    val firstAttackDay11RequiredDefense: Int = 14,
    val deficitDaysCount: Int = 0,
    val blackoutDaysCount: Int = 0,
    val dailyRoomScrapUpkeep: Double = 0.0,
    val storageCap: Double = 150.0,
    val storageFillPercent: Int = 4,
    val storageSlowdownMultiplier: Double = 1.0,
    val isAnyResourceSlowedByStorage: Boolean = false,
    val storageStatusLine: String = "📦 Storage: 6/150 (4%) • Speed 100%",
    val manualDynamoCranksRemaining: Int = 4,
    val lastWidgetActionFeedback: String = "Tap ⛏ DRILL, 💧 ICE & FOOD, or ⚡ FIX",
    val statusAlertHeadline: String = "🟢 PREP DAY 1/10 (0 ATTACKS) • 6D CLOCKS: RES 0/6D • PWR 0/6D",
    val isCriticalAlert: Boolean = false
)

data class WidgetQuickActionResult(
    val updatedState: OutpostStateEntity,
    val actionFeedback: String,
    val logCategory: String
)

class VaultStatusAppWidgetProvider : AppWidgetProvider() {

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        scheduleNextAutoRefresh(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        cancelAutoRefresh(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        scheduleNextAutoRefresh(context)
        // 1. Render immediately from fast SharedPreferences snapshot
        val cachedSnapshot = readSnapshotFromPrefs(context)
        updateWidgetViews(context, appWidgetManager, appWidgetIds, cachedSnapshot)

        // 2. Refresh asynchronously from Room database so offline/reboot state is always up-to-date
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val freshSnapshot = loadSnapshotFromDatabase(context, cachedSnapshot.lastWidgetActionFeedback)
                if (freshSnapshot != null) {
                    saveSnapshotToPrefs(context, freshSnapshot)
                    updateWidgetViews(context, appWidgetManager, appWidgetIds, freshSnapshot)
                }
            } catch (_: Throwable) {
                // Keep cached widget view if DB is busy
            } finally {
                runCatching { pendingResult.finish() }
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action ?: return
        if (action == ACTION_REFRESH_VAULT_WIDGET ||
            action == ACTION_WIDGET_QUICK_MINE ||
            action == ACTION_WIDGET_QUICK_SUPPLY ||
            action == ACTION_WIDGET_QUICK_STABILIZE
        ) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, VaultStatusAppWidgetProvider::class.java)
            val ids = appWidgetManager.getAppWidgetIds(componentName)
            if (ids.isEmpty()) {
                cancelAutoRefresh(context)
                return
            }

            if (action == ACTION_REFRESH_VAULT_WIDGET) {
                onUpdate(context, appWidgetManager, ids)
                return
            }

            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val snapshot = executeWidgetActionInDatabase(context, action)
                    if (snapshot != null) {
                        saveSnapshotToPrefs(context, snapshot)
                        scheduleNextAutoRefresh(context)
                        updateWidgetViews(context, appWidgetManager, ids, snapshot)
                    }
                } catch (_: Throwable) {
                    // Fallback to cached snapshot
                } finally {
                    runCatching { pendingResult.finish() }
                }
            }
        }
    }

    companion object {
        const val WIDGET_VERSION_NAME = "v25.0"
        const val ACTION_REFRESH_VAULT_WIDGET = "com.example.widget.ACTION_REFRESH_VAULT_WIDGET"
        const val ACTION_WIDGET_QUICK_MINE = "com.example.widget.ACTION_WIDGET_QUICK_MINE"
        const val ACTION_WIDGET_QUICK_SUPPLY = "com.example.widget.ACTION_WIDGET_QUICK_SUPPLY"
        const val ACTION_WIDGET_QUICK_STABILIZE = "com.example.widget.ACTION_WIDGET_QUICK_STABILIZE"
        const val AUTO_REFRESH_INTERVAL_MILLIS = 10_000L // Auto-refresh widget every 10 seconds
        private const val AUTO_REFRESH_REQUEST_CODE = 709
        private const val PREFS_NAME = "vault_widget_telemetry_prefs"

        fun buildWidgetSnapshot(
            state: OutpostStateEntity,
            production: ProductionSnapshot,
            lastActionFeedback: String = "Tap ⛏ DRILL, 💧 ICE & FOOD, or ⚡ FIX"
        ): VaultWidgetSnapshot {
            val activeBoss = SubterraneanBossType.fromId(state.activeSubterraneanBossId)
            val maxDeficitDays = OutpostCalculator.MAX_DEFICIT_DAYS_BEFORE_GAME_OVER
            val maxBlackoutDays = OutpostCalculator.MAX_BLACKOUT_DAYS_BEFORE_GAME_OVER
            val totalDailyScrapUpkeep = ((production.dailyRoomScrapUpkeep + production.dailyAutoRepairScrapCost) * 10.0).roundToInt() / 10.0
            val highestShelf = maxOf(state.scrap, state.water, state.rations, state.bioFuel).coerceAtLeast(0.0)
            val safeCap = production.storageCap.coerceAtLeast(1.0)
            val fillPct = ((highestShelf / safeCap) * 100.0).roundToInt().coerceIn(0, 100)
            val speedPct = (production.storageSlowdownMultiplier * 100.0).roundToInt()
            val storageStatus = when {
                production.isStoreRoomFull ->
                    "📦 STORAGE FULL (${highestShelf.toInt()}/${safeCap.toInt()}) • Slowed to ${speedPct}% (100% ≤${production.storeRoomResumeThreshold.toInt()})"
                production.isAnyStoreRoomResourceHalted ->
                    "📦 Shelf Full (${production.haltedResourcesSummary}: ${speedPct}% speed until ≤${production.storeRoomResumeThreshold.toInt()})"
                production.storageSlowdownMultiplier < 1.0 ->
                    "📦 Storage Near Cap (${highestShelf.toInt()}/${safeCap.toInt()} • ${fillPct}%) • Speed ${speedPct}%"
                else ->
                    "📦 Storage Cap: ${highestShelf.toInt()}/${safeCap.toInt()} (${fillPct}%) • Speed 100%"
            }

            val (headline, isCritical) = when {
                state.isGameOver ->
                    "💀 OUTPOST FALLEN (DAY ${state.dayCounter}) • TAP TO REBUILD" to true
                production.isVaultBlackout ->
                    "⚡ BLACKOUT (${state.blackoutDaysCount}/${maxBlackoutDays}D) • TAP ⚡ FIX / POWER!" to true
                production.isDefenseShutDownByDeficit ->
                    "🚨 RESOURCE DEFICIT (${state.deficitDaysCount}/${maxDeficitDays}D) • TAP ⛏ DRILL / 💧 ICE!" to true
                production.isWorkersStarvingOrDehydrated ->
                    "🍖 CREW STARVING / DEHYDRATED (${state.deficitDaysCount}/${maxDeficitDays}D) • TAP 💧 ICE & FOOD!" to true
                production.isRoomsLackingScrap && production.totalBuiltRoomsCount > 0 ->
                    "⚙ LOW SCRAP FOR ROOM UPKEEP (-${totalDailyScrapUpkeep}/d) • TAP ⛏ DRILL!" to true
                activeBoss != null ->
                    "🕳 BOSS AWAKENED: ${activeBoss.title.uppercase()} (${state.subterraneanBossHpPercent}% HP)" to true
                state.sickDwellersCount > 0 || state.bleedingDwellersCount > 0 || state.venomInfectedDwellersCount > 0 ->
                    "☣ MEDBAY ALERT: ${state.sickDwellersCount} SICK / TRAUMA (${state.dwellerHealthPercent}% HP)" to true
                state.activePipeLeaksCount > 0 || state.activeCaveInHazard ->
                    "🔧 HAZARD: ${state.activePipeLeaksCount} PIPE LEAK(S) • TAP ⚡ FIX / POWER" to true
                production.isStoreRoomFull ->
                    "📦 STORAGE FULL (${highestShelf.toInt()}/${safeCap.toInt()}) • GATHERING SLOWED (-75%)" to false
                !production.isPreparationPeaceWindow && production.totalDefenseRating < production.todayRequiredDefense ->
                    "⚔ SIEGE WARNING: ${production.totalDefenseRating}/${production.todayRequiredDefense} DEF • RES ${state.deficitDaysCount}/${maxDeficitDays}D" to false
                production.isPreparationPeaceWindow ->
                    "🟢 PREP DAY ${state.dayCounter}/${OutpostCalculator.PREPARATION_PEACE_DAYS} (0 ATTACKS) • 6D CLOCKS: RES ${state.deficitDaysCount}/${maxDeficitDays}D • PWR ${state.blackoutDaysCount}/${maxBlackoutDays}D" to false
                else ->
                    "🟢 NOMINAL • ${production.onlineRoomsCount}/${production.totalBuiltRoomsCount} ROOMS • 6D CLOCKS: RES ${state.deficitDaysCount}/${maxDeficitDays}D • PWR ${state.blackoutDaysCount}/${maxBlackoutDays}D" to false
            }

            return VaultWidgetSnapshot(
                versionName = WIDGET_VERSION_NAME,
                dayCounter = state.dayCounter,
                commandLevel = state.commandLevel,
                prestigeLevel = state.prestigeLevel,
                scrap = state.scrap,
                scrapPerSec = production.scrapPerSec,
                water = state.water,
                waterPerSec = production.waterPerSec,
                rations = state.rations,
                rationsPerSec = production.rationsPerSec,
                bioFuel = state.bioFuel,
                bioFuelPerSec = production.bioFuelPerSec,
                gold = state.gold,
                goldPerSec = production.goldPerSec,
                ammoRounds = state.ammoRounds,
                totalDefenseRating = production.totalDefenseRating,
                todayRequiredDefense = production.todayRequiredDefense,
                defenseIntegrityPercent = state.defenseIntegrityPercent,
                totalRaidsRepelled = state.totalRaidsRepelled,
                lastAttackSucceeded = state.lastAttackSucceeded,
                lastAttackSummary = state.lastAttackSummary,
                totalMwGenerated = production.totalMegawattGenerated,
                totalMwDemand = production.totalMegawattDemand,
                onlineRoomsCount = production.onlineRoomsCount,
                totalBuiltRoomsCount = production.totalBuiltRoomsCount,
                isVaultBlackout = production.isVaultBlackout,
                isDefenseShutDownByDeficit = production.isDefenseShutDownByDeficit,
                isWorkersStarvingOrDehydrated = production.isWorkersStarvingOrDehydrated,
                totalSurvivors = state.totalSurvivors,
                maxWorkerCapacity = production.maxWorkerCapacity,
                dwellerHealthPercent = state.dwellerHealthPercent,
                dwellerStaminaPercent = state.dwellerStaminaPercent,
                sickDwellersCount = state.sickDwellersCount,
                oxygenPercent = state.oxygenPercent,
                radiationMillisieverts = state.radiationMillisieverts,
                deepCoreDepthMeters = state.deepCoreDepthMeters,
                activeBossTitle = activeBoss?.title ?: "",
                isPreparationPeaceWindow = production.isPreparationPeaceWindow,
                preparationDaysRemaining = production.preparationDaysRemaining,
                firstAttackDay11RequiredDefense = production.firstAttackDay11RequiredDefense,
                deficitDaysCount = state.deficitDaysCount,
                blackoutDaysCount = state.blackoutDaysCount,
                dailyRoomScrapUpkeep = totalDailyScrapUpkeep,
                storageCap = production.storageCap,
                storageFillPercent = fillPct,
                storageSlowdownMultiplier = production.storageSlowdownMultiplier,
                isAnyResourceSlowedByStorage = production.isAnyStoreRoomResourceHalted || production.storageSlowdownMultiplier < 1.0,
                storageStatusLine = storageStatus,
                manualDynamoCranksRemaining = state.manualDynamoCranksRemaining,
                lastWidgetActionFeedback = lastActionFeedback,
                statusAlertHeadline = headline,
                isCriticalAlert = isCritical
            )
        }

        /**
         * Pure helper for out-of-game Quick Mine action from the Home Screen Widget:
         * Harvests Scrap, Water, Food, Bio-Fuel & Gold using Pickaxe level and Stamina multiplier,
         * slows down to 25% when a resource shelf is full instead of halting to 0, and clamps to Storage Cap.
         */
        fun applyWidgetQuickMine(
            state: OutpostStateEntity,
            production: ProductionSnapshot,
            nowMillis: Long = System.currentTimeMillis()
        ): WidgetQuickActionResult {
            val cap = production.storageCap.coerceAtLeast(OutpostCalculator.BASE_STORAGE_CAP)
            val stamMult = production.manualActionStaminaMultiplier
            val rigMult = 1.0 + (state.salvageRigLevel.coerceAtLeast(1) - 1) * 0.25
            val pickaxeDurMult = when {
                state.pickaxeDurabilityPercent <= 0 -> 0.40
                state.pickaxeDurabilityPercent < 30 -> 0.70
                else -> 1.0
            }
            val effectiveMult = stamMult * rigMult * pickaxeDurMult * production.uniqueDwellerBonusMultiplier
            val slowMult = OutpostCalculator.STORAGE_FULL_SLOWDOWN_MULTIPLIER

            val scrapSlow = if (production.isScrapHalted) slowMult else 1.0
            val waterSlow = if (production.isWaterHalted) slowMult else 1.0
            val foodSlow = if (production.isRationsHalted) slowMult else 1.0
            val fuelSlow = if (production.isFuelHalted) slowMult else 1.0
            val goldSlow = if (production.isStoreRoomFull) slowMult else 1.0

            val scrapGain = ((2.8 * effectiveMult * OutpostCalculator.SCRAP_GATHER_RATE_MULTIPLIER * scrapSlow) * 10.0).roundToInt() / 10.0
            val waterGain = ((1.8 * effectiveMult * waterSlow) * 10.0).roundToInt() / 10.0
            val foodGain = ((1.8 * effectiveMult * foodSlow) * 10.0).roundToInt() / 10.0
            val fuelGain = ((0.9 * effectiveMult * fuelSlow) * 10.0).roundToInt() / 10.0
            val goldGain = ((1.0 * effectiveMult * goldSlow) * 10.0).roundToInt() / 10.0

            val nextStamina = (state.dwellerStaminaPercent - OutpostCalculator.MANUAL_ACTION_STAMINA_COST).coerceAtLeast(10)
            val nextDurability = (state.pickaxeDurabilityPercent - 2).coerceAtLeast(0)

            val nextScrap = (state.scrap + scrapGain).coerceIn(0.0, cap)
            val nextWater = (state.water + waterGain).coerceIn(0.0, cap)
            val nextFood = (state.rations + foodGain).coerceIn(0.0, cap)
            val nextFuel = (state.bioFuel + fuelGain).coerceIn(0.0, cap)
            val nextGold = (state.gold + goldGain).coerceAtLeast(0.0)

            val nextScrapHalted = OutpostCalculator.isResourceShelfHalted(nextScrap, state.isScrapGatheringHalted, state.isStoreRoomGatheringHalted, cap)
            val nextWaterHalted = OutpostCalculator.isResourceShelfHalted(nextWater, state.isWaterGatheringHalted, state.isStoreRoomGatheringHalted, cap)
            val nextFoodHalted = OutpostCalculator.isResourceShelfHalted(nextFood, state.isRationsGatheringHalted, state.isStoreRoomGatheringHalted, cap)
            val nextFuelHalted = OutpostCalculator.isResourceShelfHalted(nextFuel, state.isFuelGatheringHalted, state.isStoreRoomGatheringHalted, cap)

            val updated = state.copy(
                scrap = nextScrap,
                water = nextWater,
                rations = nextFood,
                bioFuel = nextFuel,
                gold = nextGold,
                totalGoldSalvaged = state.totalGoldSalvaged + goldGain,
                dwellerStaminaPercent = nextStamina,
                pickaxeDurabilityPercent = nextDurability,
                totalManualScavenges = state.totalManualScavenges + 1,
                isScrapGatheringHalted = nextScrapHalted,
                isWaterGatheringHalted = nextWaterHalted,
                isRationsGatheringHalted = nextFoodHalted,
                isFuelGatheringHalted = nextFuelHalted,
                isStoreRoomGatheringHalted = nextScrapHalted && nextWaterHalted && nextFoodHalted && nextFuelHalted,
                lastSavedTimestampMillis = nowMillis
            )

            val slowSuffix = if (production.isAnyStoreRoomResourceHalted) " [Full=Slowed]" else ""
            val feedback = "⛏ DRILLED: +${formatCompactWidget(scrapGain)} Scrap, +${formatCompactWidget(waterGain)} H2O/Food, +${formatCompactWidget(goldGain)}G$slowSuffix"
            return WidgetQuickActionResult(
                updatedState = updated,
                actionFeedback = feedback,
                logCategory = "WIDGET MINE"
            )
        }

        /**
         * Pure helper for out-of-game Quick Supply (💧 ICE & FOOD) action from the Home Screen Widget:
         * Melts Martian ice & harvests emergency bio-rations (+4.5 Water, +4.5 Food, +1.2 Scrap, +15% O2, +10% Stamina).
         */
        fun applyWidgetQuickSupply(
            state: OutpostStateEntity,
            production: ProductionSnapshot,
            nowMillis: Long = System.currentTimeMillis()
        ): WidgetQuickActionResult {
            val cap = production.storageCap.coerceAtLeast(OutpostCalculator.BASE_STORAGE_CAP)
            val slowMult = OutpostCalculator.STORAGE_FULL_SLOWDOWN_MULTIPLIER
            val waterSlow = if (production.isWaterHalted) slowMult else 1.0
            val foodSlow = if (production.isRationsHalted) slowMult else 1.0
            val scrapSlow = if (production.isScrapHalted) slowMult else 1.0

            val waterGain = ((4.5 * production.uniqueDwellerBonusMultiplier * waterSlow) * 10.0).roundToInt() / 10.0
            val foodGain = ((4.5 * production.uniqueDwellerBonusMultiplier * foodSlow) * 10.0).roundToInt() / 10.0
            val scrapGain = ((1.2 * production.uniqueDwellerBonusMultiplier * scrapSlow) * 10.0).roundToInt() / 10.0
            val nextWater = (state.water + waterGain).coerceIn(0.0, cap)
            val nextFood = (state.rations + foodGain).coerceIn(0.0, cap)
            val nextScrap = (state.scrap + scrapGain).coerceIn(0.0, cap)
            val nextO2 = (state.oxygenPercent + 15).coerceAtMost(100)
            val nextStamina = (state.dwellerStaminaPercent + 10).coerceAtMost(100)

            val updated = state.copy(
                water = nextWater,
                rations = nextFood,
                scrap = nextScrap,
                oxygenPercent = nextO2,
                dwellerStaminaPercent = nextStamina,
                miningShaftHeatPercent = (state.miningShaftHeatPercent - 15).coerceAtLeast(0),
                lastSavedTimestampMillis = nowMillis
            )
            val feedback = "💧 ICE & FOOD: +${formatCompactWidget(waterGain)} Water, +${formatCompactWidget(foodGain)} Food, O2 $nextO2%, Stam $nextStamina%"
            return WidgetQuickActionResult(
                updatedState = updated,
                actionFeedback = feedback,
                logCategory = "WIDGET SUPPLY"
            )
        }

        /**
         * Pure helper for out-of-game Quick Stabilize / Crank action from the Home Screen Widget:
         * 1) If in Power Blackout with Dynamo Cranks left -> Cranks Backup Dynamo (+16 MW surge & +4 Fuel).
         * 2) Else if Stamina is low (<= 35%), Pipe Leaks > 0, or Sick Dwellers > 0 -> Rests Crew (+22% Stamina), seals 1 leak & treats 1 sick dweller.
         * 3) Else -> Distills emergency Water & forages Fungi (+3.5 Water, +3.5 Food, -15°C Heat, -4% Stamina).
         */
        fun applyWidgetQuickStabilize(
            state: OutpostStateEntity,
            production: ProductionSnapshot,
            nowMillis: Long = System.currentTimeMillis()
        ): WidgetQuickActionResult {
            val cap = production.storageCap.coerceAtLeast(OutpostCalculator.BASE_STORAGE_CAP)
            return if (production.isVaultBlackout && state.manualDynamoCranksRemaining > 0) {
                val nextCranks = (state.manualDynamoCranksRemaining - 1).coerceAtLeast(0)
                val nextStack = (state.activeDynamoCrankStack + 1).coerceAtMost(OutpostCalculator.MAX_DYNAMO_CRANKS_PER_BLACKOUT)
                val nextFuel = (state.bioFuel + 4.0).coerceIn(0.0, cap)
                val updated = state.copy(
                    bioFuel = nextFuel,
                    manualDynamoCranksRemaining = nextCranks,
                    activeDynamoCrankStack = nextStack,
                    thoriumOverdriveUntilMillis = nowMillis + 75_000L,
                    solarFlareActiveUntilMillis = nowMillis + 60_000L,
                    lastSavedTimestampMillis = nowMillis
                )
                WidgetQuickActionResult(
                    updatedState = updated,
                    actionFeedback = "⚡ DYNAMO CRANKED: +16 MW Surge & +4 Fuel ($nextCranks/4 Cranks left)",
                    logCategory = "WIDGET CRANK"
                )
            } else if (state.dwellerStaminaPercent <= 35 || state.activePipeLeaksCount > 0 || state.sickDwellersCount > 0) {
                val nextStamina = (state.dwellerStaminaPercent + 22).coerceAtMost(100)
                val nextLeaks = (state.activePipeLeaksCount - 1).coerceAtLeast(0)
                val nextSick = (state.sickDwellersCount - 1).coerceAtLeast(0)
                val nextHealth = (state.dwellerHealthPercent + 8).coerceAtMost(100)
                val updated = state.copy(
                    dwellerStaminaPercent = nextStamina,
                    isRestRotationActive = true,
                    activePipeLeaksCount = nextLeaks,
                    sickDwellersCount = nextSick,
                    dwellerHealthPercent = nextHealth,
                    lastSavedTimestampMillis = nowMillis
                )
                WidgetQuickActionResult(
                    updatedState = updated,
                    actionFeedback = "⚡ STABILIZED: Crew Rested (Stam $nextStamina% • HP $nextHealth% • Leaks $nextLeaks)",
                    logCategory = "WIDGET STABILIZE"
                )
            } else {
                val stamMult = production.manualActionStaminaMultiplier
                val slowMult = OutpostCalculator.STORAGE_FULL_SLOWDOWN_MULTIPLIER
                val waterSlow = if (production.isWaterHalted) slowMult else 1.0
                val foodSlow = if (production.isRationsHalted) slowMult else 1.0
                val waterGain = ((3.5 * stamMult * waterSlow) * 10.0).roundToInt() / 10.0
                val foodGain = ((3.5 * stamMult * foodSlow) * 10.0).roundToInt() / 10.0
                val nextStamina = (state.dwellerStaminaPercent - OutpostCalculator.MANUAL_ACTION_STAMINA_COST).coerceAtLeast(10)
                val updated = state.copy(
                    water = (state.water + waterGain).coerceIn(0.0, cap),
                    rations = (state.rations + foodGain).coerceIn(0.0, cap),
                    miningShaftHeatPercent = (state.miningShaftHeatPercent - 15).coerceAtLeast(0),
                    radiationMillisieverts = (state.radiationMillisieverts - 5).coerceAtLeast(0),
                    dwellerStaminaPercent = nextStamina,
                    lastSavedTimestampMillis = nowMillis
                )
                WidgetQuickActionResult(
                    updatedState = updated,
                    actionFeedback = "⚡ FORAGED & BOILED: +${formatCompactWidget(waterGain)} H2O, +${formatCompactWidget(foodGain)} Food (Stam $nextStamina%)",
                    logCategory = "WIDGET FORAGE"
                )
            }
        }

        fun requestUpdateAllWidgets(context: Context) {
            runCatching {
                val appContext = context.applicationContext
                val refreshIntent = Intent(appContext, VaultStatusAppWidgetProvider::class.java).apply {
                    action = ACTION_REFRESH_VAULT_WIDGET
                }
                appContext.sendBroadcast(refreshIntent)
            }
        }

        fun pushLiveStateToWidgets(
            context: Context,
            state: OutpostStateEntity,
            production: ProductionSnapshot
        ) {
            runCatching {
                val appContext = context.applicationContext
                val cachedFeedback = readSnapshotFromPrefs(appContext).lastWidgetActionFeedback
                val snapshot = buildWidgetSnapshot(state, production, cachedFeedback)
                saveSnapshotToPrefs(appContext, snapshot)
                val appWidgetManager = AppWidgetManager.getInstance(appContext)
                val componentName = ComponentName(appContext, VaultStatusAppWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                    scheduleNextAutoRefresh(appContext)
                    updateWidgetViews(appContext, appWidgetManager, appWidgetIds, snapshot)
                }
            }
        }

        fun scheduleNextAutoRefresh(context: Context) {
            runCatching {
                val appContext = context.applicationContext
                val appWidgetManager = AppWidgetManager.getInstance(appContext)
                val componentName = ComponentName(appContext, VaultStatusAppWidgetProvider::class.java)
                val ids = appWidgetManager.getAppWidgetIds(componentName)
                if (ids == null || ids.isEmpty()) return@runCatching

                val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return@runCatching
                val refreshIntent = Intent(appContext, VaultStatusAppWidgetProvider::class.java).apply {
                    action = ACTION_REFRESH_VAULT_WIDGET
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    appContext,
                    AUTO_REFRESH_REQUEST_CODE,
                    refreshIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val triggerAtElapsed = SystemClock.elapsedRealtime() + AUTO_REFRESH_INTERVAL_MILLIS
                alarmManager.setWindow(
                    AlarmManager.ELAPSED_REALTIME,
                    triggerAtElapsed,
                    2_000L,
                    pendingIntent
                )
            }
        }

        fun cancelAutoRefresh(context: Context) {
            runCatching {
                val appContext = context.applicationContext
                val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return@runCatching
                val refreshIntent = Intent(appContext, VaultStatusAppWidgetProvider::class.java).apply {
                    action = ACTION_REFRESH_VAULT_WIDGET
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    appContext,
                    AUTO_REFRESH_REQUEST_CODE,
                    refreshIntent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent)
                    pendingIntent.cancel()
                }
            }
        }

        fun requestPinVaultWidget(context: Context): Boolean {
            return runCatching {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val provider = ComponentName(context, VaultStatusAppWidgetProvider::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appWidgetManager.isRequestPinAppWidgetSupported) {
                    val callbackIntent = Intent(context, VaultStatusAppWidgetProvider::class.java).apply {
                        action = ACTION_REFRESH_VAULT_WIDGET
                    }
                    val successCallback = PendingIntent.getBroadcast(
                        context,
                        702,
                        callbackIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    appWidgetManager.requestPinAppWidget(provider, null, successCallback)
                } else {
                    false
                }
            }.getOrDefault(false)
        }

        private suspend fun loadSnapshotFromDatabase(
            context: Context,
            lastActionFeedback: String
        ): VaultWidgetSnapshot? {
            val dao = OutpostDatabase.getDatabase(context).outpostDao()
            val savedState = dao.getOutpostStateOnce() ?: return null
            val buildingsMap = dao.getBuildingsOnce().associate { it.buildingTypeId to it.level }
            val workersMap = dao.getWorkerClassesOnce().associateBy { it.workerClassId }
            val skillsMap = dao.getSkillNodesOnce().associate { it.nodeId to it.currentRank }
            val now = System.currentTimeMillis()
            val baseProd = OutpostCalculator.computeProductionSnapshot(
                state = savedState,
                buildingsMap = buildingsMap,
                workersMap = workersMap,
                skillsMap = skillsMap,
                nowMillis = now
            )
            val elapsedSecs = ((now - savedState.lastSavedTimestampMillis) / 1000L).coerceIn(0L, 43_200L)
            val projectedState = if (elapsedSecs >= 2L && savedState.totalSurvivors > 0 && baseProd.totalAssignedWorkers > 0) {
                val offlineFactor = 0.65
                val cap = baseProd.storageCap
                savedState.copy(
                    scrap = (savedState.scrap + baseProd.scrapPerSec.coerceAtLeast(0.0) * elapsedSecs * offlineFactor).coerceIn(0.0, cap),
                    water = (savedState.water + baseProd.waterPerSec.coerceAtLeast(0.0) * elapsedSecs * offlineFactor).coerceIn(0.0, cap),
                    rations = (savedState.rations + baseProd.rationsPerSec.coerceAtLeast(0.0) * elapsedSecs * offlineFactor).coerceIn(0.0, cap),
                    bioFuel = (savedState.bioFuel + baseProd.bioFuelPerSec.coerceAtLeast(0.0) * elapsedSecs * offlineFactor).coerceIn(0.0, cap),
                    gold = (savedState.gold + baseProd.goldPerSec.coerceAtLeast(0.0) * elapsedSecs * offlineFactor).coerceAtLeast(0.0)
                )
            } else {
                savedState
            }
            val finalProd = OutpostCalculator.computeProductionSnapshot(
                state = projectedState,
                buildingsMap = buildingsMap,
                workersMap = workersMap,
                skillsMap = skillsMap,
                nowMillis = now
            )
            return buildWidgetSnapshot(projectedState, finalProd, lastActionFeedback)
        }

        private suspend fun executeWidgetActionInDatabase(
            context: Context,
            action: String
        ): VaultWidgetSnapshot? {
            val dao = OutpostDatabase.getDatabase(context).outpostDao()
            val savedState = dao.getOutpostStateOnce() ?: OutpostStateEntity()
            val buildingsMap = dao.getBuildingsOnce().associate { it.buildingTypeId to it.level }
            val workersMap = dao.getWorkerClassesOnce().associateBy { it.workerClassId }
            val skillsMap = dao.getSkillNodesOnce().associate { it.nodeId to it.currentRank }
            val now = System.currentTimeMillis()

            val baseProd = OutpostCalculator.computeProductionSnapshot(
                state = savedState,
                buildingsMap = buildingsMap,
                workersMap = workersMap,
                skillsMap = skillsMap,
                nowMillis = now
            )
            val elapsedSecs = ((now - savedState.lastSavedTimestampMillis) / 1000L).coerceIn(0L, 43_200L)
            val projectedState = if (elapsedSecs >= 2L && savedState.totalSurvivors > 0 && baseProd.totalAssignedWorkers > 0) {
                val offlineFactor = 0.65
                val cap = baseProd.storageCap
                savedState.copy(
                    scrap = (savedState.scrap + baseProd.scrapPerSec.coerceAtLeast(0.0) * elapsedSecs * offlineFactor).coerceIn(0.0, cap),
                    water = (savedState.water + baseProd.waterPerSec.coerceAtLeast(0.0) * elapsedSecs * offlineFactor).coerceIn(0.0, cap),
                    rations = (savedState.rations + baseProd.rationsPerSec.coerceAtLeast(0.0) * elapsedSecs * offlineFactor).coerceIn(0.0, cap),
                    bioFuel = (savedState.bioFuel + baseProd.bioFuelPerSec.coerceAtLeast(0.0) * elapsedSecs * offlineFactor).coerceIn(0.0, cap),
                    gold = (savedState.gold + baseProd.goldPerSec.coerceAtLeast(0.0) * elapsedSecs * offlineFactor).coerceAtLeast(0.0),
                    lastSavedTimestampMillis = now
                )
            } else {
                savedState
            }

            val currentProd = OutpostCalculator.computeProductionSnapshot(
                state = projectedState,
                buildingsMap = buildingsMap,
                workersMap = workersMap,
                skillsMap = skillsMap,
                nowMillis = now
            )

            val result = when (action) {
                ACTION_WIDGET_QUICK_MINE -> applyWidgetQuickMine(projectedState, currentProd, now)
                ACTION_WIDGET_QUICK_SUPPLY -> applyWidgetQuickSupply(projectedState, currentProd, now)
                ACTION_WIDGET_QUICK_STABILIZE -> applyWidgetQuickStabilize(projectedState, currentProd, now)
                else -> WidgetQuickActionResult(projectedState, "↻ Synced v25.0 Telemetry", "WIDGET SYNC")
            }

            dao.upsertOutpostState(result.updatedState)
            dao.insertLog(
                EventLogEntity(
                    category = result.logCategory,
                    message = result.actionFeedback,
                    timestampMillis = now
                )
            )

            val updatedProd = OutpostCalculator.computeProductionSnapshot(
                state = result.updatedState,
                buildingsMap = buildingsMap,
                workersMap = workersMap,
                skillsMap = skillsMap,
                nowMillis = now
            )
            return buildWidgetSnapshot(result.updatedState, updatedProd, result.actionFeedback)
        }

        private fun updateWidgetViews(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray,
            snapshot: VaultWidgetSnapshot
        ) {
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val launchPendingIntent = PendingIntent.getActivity(
                context,
                700,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val refreshIntent = Intent(context, VaultStatusAppWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_VAULT_WIDGET
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                701,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val quickMineIntent = Intent(context, VaultStatusAppWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_QUICK_MINE
            }
            val quickMinePendingIntent = PendingIntent.getBroadcast(
                context,
                703,
                quickMineIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val quickSupplyIntent = Intent(context, VaultStatusAppWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_QUICK_SUPPLY
            }
            val quickSupplyPendingIntent = PendingIntent.getBroadcast(
                context,
                705,
                quickSupplyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val quickStabilizeIntent = Intent(context, VaultStatusAppWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_QUICK_STABILIZE
            }
            val quickStabilizePendingIntent = PendingIntent.getBroadcast(
                context,
                704,
                quickStabilizeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val prestigeBadge = if (snapshot.prestigeLevel > 0) " • ★P${snapshot.prestigeLevel}" else ""
            val titleText = "ARES OUTPOST ${snapshot.versionName} • SOL ${snapshot.dayCounter} • LV.${snapshot.commandLevel}$prestigeBadge"

            val prepAndClocksBanner = if (snapshot.isPreparationPeaceWindow) {
                "⏳ ${snapshot.preparationDaysRemaining}d Peace (Day 11: ${snapshot.firstAttackDay11RequiredDefense} DEF) • Upkeep -${String.format(Locale.US, "%.1f", snapshot.dailyRoomScrapUpkeep)}/d"
            } else {
                "⚔ Siege Req ${snapshot.todayRequiredDefense} DEF • Upkeep -${String.format(Locale.US, "%.1f", snapshot.dailyRoomScrapUpkeep)}/d"
            }

            val attackStatusText = when {
                snapshot.isPreparationPeaceWindow ->
                    "🛡 PREP ${snapshot.preparationDaysRemaining}D"
                snapshot.lastAttackSucceeded ->
                    "🛡 ${snapshot.totalDefenseRating}/${snapshot.todayRequiredDefense} DEF"
                else ->
                    "🚨 DOOR ${snapshot.defenseIntegrityPercent}%"
            }

            val powerStateLabel = if (snapshot.isVaultBlackout) "OUT" else "OK"
            val dwellersStatusText = buildString {
                append("👥 ${snapshot.totalSurvivors}/${snapshot.maxWorkerCapacity}")
                append(" • STAM ${snapshot.dwellerStaminaPercent}%")
                append(" • ⚡ $powerStateLabel (${snapshot.totalMwGenerated.roundToInt()}/${snapshot.totalMwDemand.roundToInt()}MW)")
            }

            val systemsFooter = snapshot.lastWidgetActionFeedback.ifBlank {
                "ROOMS ${snapshot.onlineRoomsCount}/${snapshot.totalBuiltRoomsCount} • O2 ${snapshot.oxygenPercent}% • ☢ ${snapshot.radiationMillisieverts}mSv"
            }

            for (widgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_vault_status)
                views.setTextViewText(R.id.widget_vault_title, titleText)
                views.setTextViewText(R.id.widget_status_alert, snapshot.statusAlertHeadline)
                views.setTextColor(
                    R.id.widget_status_alert,
                    if (snapshot.isCriticalAlert) Color.parseColor("#FF5252") else Color.parseColor("#66BB6A")
                )
                views.setTextViewText(R.id.widget_storage_status, snapshot.storageStatusLine)
                views.setTextColor(
                    R.id.widget_storage_status,
                    if (snapshot.isAnyResourceSlowedByStorage) Color.parseColor("#FFD54F") else Color.parseColor("#FFF3E0")
                )
                views.setProgressBar(R.id.widget_storage_progress, 100, snapshot.storageFillPercent, false)
                views.setTextViewText(R.id.widget_prep_and_clocks_banner, prepAndClocksBanner)

                views.setTextViewText(
                    R.id.widget_res_scrap,
                    "${formatCompactWidget(snapshot.scrap)} (${formatSignedRate(snapshot.scrapPerSec)})"
                )
                views.setTextViewText(
                    R.id.widget_res_water,
                    "${formatCompactWidget(snapshot.water)} (${formatSignedRate(snapshot.waterPerSec)})"
                )
                views.setTextViewText(
                    R.id.widget_res_food,
                    "${formatCompactWidget(snapshot.rations)} (${formatSignedRate(snapshot.rationsPerSec)})"
                )
                views.setTextViewText(
                    R.id.widget_res_fuel,
                    "${formatCompactWidget(snapshot.bioFuel)} (${formatSignedRate(snapshot.bioFuelPerSec)})"
                )
                views.setTextViewText(
                    R.id.widget_res_gold,
                    "🪙 ${formatCompactWidget(snapshot.gold)}G (${formatSignedRate(snapshot.goldPerSec)})"
                )
                views.setTextViewText(
                    R.id.widget_res_defense,
                    "🛡 ${snapshot.totalDefenseRating} DEF • ${snapshot.ammoRounds} Rds"
                )
                views.setTextViewText(R.id.widget_attack_status, attackStatusText)
                views.setTextColor(
                    R.id.widget_attack_status,
                    when {
                        snapshot.isPreparationPeaceWindow -> Color.parseColor("#4DD0E1")
                        snapshot.lastAttackSucceeded -> Color.parseColor("#66BB6A")
                        else -> Color.parseColor("#FF5252")
                    }
                )
                views.setTextViewText(R.id.widget_dwellers_status, dwellersStatusText)
                views.setTextViewText(R.id.widget_systems_footer, systemsFooter)
                views.setTextViewText(R.id.widget_btn_refresh, "↻ ${snapshot.versionName}")

                views.setOnClickPendingIntent(R.id.widget_root_container, launchPendingIntent)
                views.setOnClickPendingIntent(R.id.widget_btn_quick_mine, quickMinePendingIntent)
                views.setOnClickPendingIntent(R.id.widget_btn_quick_supply, quickSupplyPendingIntent)
                views.setOnClickPendingIntent(R.id.widget_btn_quick_stabilize, quickStabilizePendingIntent)
                views.setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPendingIntent)
                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }

        private fun formatCompactWidget(value: Double): String {
            return when {
                value >= 10_000 -> String.format(Locale.US, "%.1fk", value / 1000.0)
                value >= 100 -> value.toInt().toString()
                else -> String.format(Locale.US, "%.1f", value)
            }
        }

        private fun formatSignedRate(rate: Double): String {
            val sign = if (rate >= 0.0) "+" else ""
            return String.format(Locale.US, "%s%.1f/s", sign, rate)
        }

        private fun saveSnapshotToPrefs(context: Context, snap: VaultWidgetSnapshot) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                .putString("versionName", snap.versionName)
                .putInt("dayCounter", snap.dayCounter)
                .putInt("commandLevel", snap.commandLevel)
                .putInt("prestigeLevel", snap.prestigeLevel)
                .putFloat("scrap", snap.scrap.toFloat())
                .putFloat("scrapPerSec", snap.scrapPerSec.toFloat())
                .putFloat("water", snap.water.toFloat())
                .putFloat("waterPerSec", snap.waterPerSec.toFloat())
                .putFloat("rations", snap.rations.toFloat())
                .putFloat("rationsPerSec", snap.rationsPerSec.toFloat())
                .putFloat("bioFuel", snap.bioFuel.toFloat())
                .putFloat("bioFuelPerSec", snap.bioFuelPerSec.toFloat())
                .putFloat("gold", snap.gold.toFloat())
                .putFloat("goldPerSec", snap.goldPerSec.toFloat())
                .putInt("ammoRounds", snap.ammoRounds)
                .putInt("totalDefenseRating", snap.totalDefenseRating)
                .putInt("todayRequiredDefense", snap.todayRequiredDefense)
                .putInt("defenseIntegrityPercent", snap.defenseIntegrityPercent)
                .putInt("totalRaidsRepelled", snap.totalRaidsRepelled)
                .putBoolean("lastAttackSucceeded", snap.lastAttackSucceeded)
                .putString("lastAttackSummary", snap.lastAttackSummary)
                .putFloat("totalMwGenerated", snap.totalMwGenerated.toFloat())
                .putFloat("totalMwDemand", snap.totalMwDemand.toFloat())
                .putInt("onlineRoomsCount", snap.onlineRoomsCount)
                .putInt("totalBuiltRoomsCount", snap.totalBuiltRoomsCount)
                .putBoolean("isVaultBlackout", snap.isVaultBlackout)
                .putBoolean("isDefenseShutDownByDeficit", snap.isDefenseShutDownByDeficit)
                .putBoolean("isWorkersStarvingOrDehydrated", snap.isWorkersStarvingOrDehydrated)
                .putInt("totalSurvivors", snap.totalSurvivors)
                .putInt("maxWorkerCapacity", snap.maxWorkerCapacity)
                .putInt("dwellerHealthPercent", snap.dwellerHealthPercent)
                .putInt("dwellerStaminaPercent", snap.dwellerStaminaPercent)
                .putInt("sickDwellersCount", snap.sickDwellersCount)
                .putInt("oxygenPercent", snap.oxygenPercent)
                .putInt("radiationMillisieverts", snap.radiationMillisieverts)
                .putInt("deepCoreDepthMeters", snap.deepCoreDepthMeters)
                .putString("activeBossTitle", snap.activeBossTitle)
                .putBoolean("isPreparationPeaceWindow", snap.isPreparationPeaceWindow)
                .putInt("preparationDaysRemaining", snap.preparationDaysRemaining)
                .putInt("firstAttackDay11RequiredDefense", snap.firstAttackDay11RequiredDefense)
                .putInt("deficitDaysCount", snap.deficitDaysCount)
                .putInt("blackoutDaysCount", snap.blackoutDaysCount)
                .putFloat("dailyRoomScrapUpkeep", snap.dailyRoomScrapUpkeep.toFloat())
                .putFloat("storageCap", snap.storageCap.toFloat())
                .putInt("storageFillPercent", snap.storageFillPercent)
                .putFloat("storageSlowdownMultiplier", snap.storageSlowdownMultiplier.toFloat())
                .putBoolean("isAnyResourceSlowedByStorage", snap.isAnyResourceSlowedByStorage)
                .putString("storageStatusLine", snap.storageStatusLine)
                .putInt("manualDynamoCranksRemaining", snap.manualDynamoCranksRemaining)
                .putString("lastWidgetActionFeedback", snap.lastWidgetActionFeedback)
                .putString("statusAlertHeadline", snap.statusAlertHeadline)
                .putBoolean("isCriticalAlert", snap.isCriticalAlert)
                .apply()
        }

        private fun readSnapshotFromPrefs(context: Context): VaultWidgetSnapshot {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return VaultWidgetSnapshot(
                versionName = prefs.getString("versionName", WIDGET_VERSION_NAME) ?: WIDGET_VERSION_NAME,
                dayCounter = prefs.getInt("dayCounter", 1),
                commandLevel = prefs.getInt("commandLevel", 1),
                prestigeLevel = prefs.getInt("prestigeLevel", 0),
                scrap = prefs.getFloat("scrap", 0f).toDouble(),
                scrapPerSec = prefs.getFloat("scrapPerSec", 0f).toDouble(),
                water = prefs.getFloat("water", 6f).toDouble(),
                waterPerSec = prefs.getFloat("waterPerSec", 0f).toDouble(),
                rations = prefs.getFloat("rations", 6f).toDouble(),
                rationsPerSec = prefs.getFloat("rationsPerSec", 0f).toDouble(),
                bioFuel = prefs.getFloat("bioFuel", 0f).toDouble(),
                bioFuelPerSec = prefs.getFloat("bioFuelPerSec", 0f).toDouble(),
                gold = prefs.getFloat("gold", 0f).toDouble(),
                goldPerSec = prefs.getFloat("goldPerSec", 0f).toDouble(),
                ammoRounds = prefs.getInt("ammoRounds", 20),
                totalDefenseRating = prefs.getInt("totalDefenseRating", 20),
                todayRequiredDefense = prefs.getInt("todayRequiredDefense", 0),
                defenseIntegrityPercent = prefs.getInt("defenseIntegrityPercent", 60),
                totalRaidsRepelled = prefs.getInt("totalRaidsRepelled", 0),
                lastAttackSucceeded = prefs.getBoolean("lastAttackSucceeded", true),
                lastAttackSummary = prefs.getString("lastAttackSummary", "10-Day Prep Window • 0 Attacks Until Day 11")
                    ?: "10-Day Prep Window • 0 Attacks Until Day 11",
                totalMwGenerated = prefs.getFloat("totalMwGenerated", 8f).toDouble(),
                totalMwDemand = prefs.getFloat("totalMwDemand", 0f).toDouble(),
                onlineRoomsCount = prefs.getInt("onlineRoomsCount", 0),
                totalBuiltRoomsCount = prefs.getInt("totalBuiltRoomsCount", 0),
                isVaultBlackout = prefs.getBoolean("isVaultBlackout", false),
                isDefenseShutDownByDeficit = prefs.getBoolean("isDefenseShutDownByDeficit", false),
                isWorkersStarvingOrDehydrated = prefs.getBoolean("isWorkersStarvingOrDehydrated", false),
                totalSurvivors = prefs.getInt("totalSurvivors", 0),
                maxWorkerCapacity = prefs.getInt("maxWorkerCapacity", 4),
                dwellerHealthPercent = prefs.getInt("dwellerHealthPercent", 100),
                dwellerStaminaPercent = prefs.getInt("dwellerStaminaPercent", 100),
                sickDwellersCount = prefs.getInt("sickDwellersCount", 0),
                oxygenPercent = prefs.getInt("oxygenPercent", 78),
                radiationMillisieverts = prefs.getInt("radiationMillisieverts", 15),
                deepCoreDepthMeters = prefs.getInt("deepCoreDepthMeters", 0),
                activeBossTitle = prefs.getString("activeBossTitle", "") ?: "",
                isPreparationPeaceWindow = prefs.getBoolean("isPreparationPeaceWindow", true),
                preparationDaysRemaining = prefs.getInt("preparationDaysRemaining", 10),
                firstAttackDay11RequiredDefense = prefs.getInt("firstAttackDay11RequiredDefense", 14),
                deficitDaysCount = prefs.getInt("deficitDaysCount", 0),
                blackoutDaysCount = prefs.getInt("blackoutDaysCount", 0),
                dailyRoomScrapUpkeep = prefs.getFloat("dailyRoomScrapUpkeep", 0f).toDouble(),
                storageCap = prefs.getFloat("storageCap", 150f).toDouble(),
                storageFillPercent = prefs.getInt("storageFillPercent", 4),
                storageSlowdownMultiplier = prefs.getFloat("storageSlowdownMultiplier", 1f).toDouble(),
                isAnyResourceSlowedByStorage = prefs.getBoolean("isAnyResourceSlowedByStorage", false),
                storageStatusLine = prefs.getString("storageStatusLine", "📦 Storage Cap: 6/150 (4%) • Speed 100%")
                    ?: "📦 Storage Cap: 6/150 (4%) • Speed 100%",
                manualDynamoCranksRemaining = prefs.getInt("manualDynamoCranksRemaining", 4),
                lastWidgetActionFeedback = prefs.getString("lastWidgetActionFeedback", "Tap ⛏ DRILL, 💧 ICE & FOOD, or ⚡ FIX")
                    ?: "Tap ⛏ DRILL, 💧 ICE & FOOD, or ⚡ FIX",
                statusAlertHeadline = prefs.getString("statusAlertHeadline", "🟢 PREP DAY 1/10 (0 ATTACKS) • 6D CLOCKS: RES 0/6D • PWR 0/6D")
                    ?: "🟢 PREP DAY 1/10 (0 ATTACKS) • 6D CLOCKS: RES 0/6D • PWR 0/6D",
                isCriticalAlert = prefs.getBoolean("isCriticalAlert", false)
            )
        }
    }
}
