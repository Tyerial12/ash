package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
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
    val dayCounter: Int = 1,
    val commandLevel: Int = 1,
    val prestigeLevel: Int = 0,
    val scrap: Double = 0.0,
    val scrapPerSec: Double = 0.0,
    val water: Double = 25.0,
    val waterPerSec: Double = 0.0,
    val rations: Double = 25.0,
    val rationsPerSec: Double = 0.0,
    val bioFuel: Double = 10.0,
    val bioFuelPerSec: Double = 0.0,
    val gold: Double = 0.0,
    val goldPerSec: Double = 0.0,
    val ammoRounds: Int = 25,
    val totalDefenseRating: Int = 20,
    val todayRequiredDefense: Int = 28,
    val defenseIntegrityPercent: Int = 60,
    val totalRaidsRepelled: Int = 0,
    val lastAttackSucceeded: Boolean = true,
    val lastAttackSummary: String = "Awaiting First Siege • Defenses Ready",
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
    val oxygenPercent: Int = 100,
    val radiationMillisieverts: Int = 0,
    val deepCoreDepthMeters: Int = 0,
    val activeBossTitle: String = "",
    val statusAlertHeadline: String = "🟢 SYSTEMS NOMINAL • TAP TO OPEN VAULT",
    val isCriticalAlert: Boolean = false
)

class VaultStatusAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // 1. Render immediately from fast SharedPreferences snapshot
        val cachedSnapshot = readSnapshotFromPrefs(context)
        updateWidgetViews(context, appWidgetManager, appWidgetIds, cachedSnapshot)

        // 2. Refresh asynchronously from Room database so offline/reboot state is always up-to-date
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val freshSnapshot = loadSnapshotFromDatabase(context)
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
        if (intent.action == ACTION_REFRESH_VAULT_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, VaultStatusAppWidgetProvider::class.java)
            val ids = appWidgetManager.getAppWidgetIds(componentName)
            if (ids.isNotEmpty()) {
                onUpdate(context, appWidgetManager, ids)
            }
        }
    }

    companion object {
        const val ACTION_REFRESH_VAULT_WIDGET = "com.example.widget.ACTION_REFRESH_VAULT_WIDGET"
        private const val PREFS_NAME = "vault_widget_telemetry_prefs"

        fun buildWidgetSnapshot(
            state: OutpostStateEntity,
            production: ProductionSnapshot
        ): VaultWidgetSnapshot {
            val activeBoss = SubterraneanBossType.fromId(state.activeSubterraneanBossId)
            val (headline, isCritical) = when {
                state.isGameOver ->
                    "💀 VAULT FALLEN (DAY ${state.dayCounter}) • TAP TO REBUILD" to true
                production.isVaultBlackout ->
                    "⚡ POWER BLACKOUT (${state.blackoutDaysCount}/6D) • RESTORE MW!" to true
                production.isDefenseShutDownByDeficit ->
                    "🚨 DEFICIT SHUTDOWN (${state.deficitDaysCount}/2D) • 0 DEF!" to true
                production.isWorkersStarvingOrDehydrated ->
                    "🍖 CREW STARVING / DEHYDRATED • ROOMS HALTED!" to true
                activeBoss != null ->
                    "🕳 BOSS AWAKENED: ${activeBoss.title.uppercase()} (${state.subterraneanBossHpPercent}% HP)" to true
                state.sickDwellersCount > 0 || state.bleedingDwellersCount > 0 || state.venomInfectedDwellersCount > 0 ->
                    "☣ MEDBAY ALERT: ${state.sickDwellersCount} SICK / CREW TRAUMA (${state.dwellerHealthPercent}% HP)" to true
                state.activePipeLeaksCount > 0 || state.activeCaveInHazard ->
                    "🔧 HAZARD ALERT: ${state.activePipeLeaksCount} PIPE LEAK(S) • TAP TO REPAIR" to true
                production.totalDefenseRating < production.todayRequiredDefense ->
                    "⚔ SIEGE WARNING: ${production.totalDefenseRating}/${production.todayRequiredDefense} DEF READY" to false
                else ->
                    "🟢 SYSTEMS NOMINAL • ${production.onlineRoomsCount}/${production.totalBuiltRoomsCount} ROOMS ONLINE" to false
            }

            return VaultWidgetSnapshot(
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
                statusAlertHeadline = headline,
                isCriticalAlert = isCritical
            )
        }

        fun pushLiveStateToWidgets(
            context: Context,
            state: OutpostStateEntity,
            production: ProductionSnapshot
        ) {
            runCatching {
                val appContext = context.applicationContext
                val snapshot = buildWidgetSnapshot(state, production)
                saveSnapshotToPrefs(appContext, snapshot)
                val appWidgetManager = AppWidgetManager.getInstance(appContext)
                val componentName = ComponentName(appContext, VaultStatusAppWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                    updateWidgetViews(appContext, appWidgetManager, appWidgetIds, snapshot)
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

        private suspend fun loadSnapshotFromDatabase(context: Context): VaultWidgetSnapshot? {
            val dao = OutpostDatabase.getDatabase(context).outpostDao()
            val state = dao.getOutpostStateOnce() ?: return null
            val buildingsMap = dao.getBuildingsOnce().associate { it.buildingTypeId to it.level }
            val workersMap = dao.getWorkerClassesOnce().associateBy { it.workerClassId }
            val skillsMap = dao.getSkillNodesOnce().associate { it.nodeId to it.currentRank }
            val prod = OutpostCalculator.computeProductionSnapshot(
                state = state,
                buildingsMap = buildingsMap,
                workersMap = workersMap,
                skillsMap = skillsMap,
                nowMillis = System.currentTimeMillis()
            )
            return buildWidgetSnapshot(state, prod)
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

            val prestigeBadge = if (snapshot.prestigeLevel > 0) " • ★P${snapshot.prestigeLevel}" else ""
            val titleText = "VAULT CITY • DAY ${snapshot.dayCounter} • LV.${snapshot.commandLevel}$prestigeBadge"

            val attackStatusText = if (snapshot.lastAttackSucceeded) {
                "🛡 ATTACK SUCCEEDED: ${snapshot.lastAttackSummary} • ${snapshot.totalRaidsRepelled} Repelled"
            } else {
                "🚨 ATTACK FAILED: ${snapshot.lastAttackSummary} • Door ${snapshot.defenseIntegrityPercent}%"
            }

            val dwellersStatusText = buildString {
                append("👥 DWELLERS: ${snapshot.totalSurvivors}/${snapshot.maxWorkerCapacity}")
                append(" (Max ${OutpostCalculator.MAX_VAULT_DWELLERS} • +4/Lv)")
                append(" • HP ${snapshot.dwellerHealthPercent}% • STAM ${snapshot.dwellerStaminaPercent}%")
                if (snapshot.sickDwellersCount > 0) {
                    append(" • ☣ ${snapshot.sickDwellersCount} Sick")
                }
            }

            val powerStateLabel = if (snapshot.isVaultBlackout) "BLACKOUT" else "ONLINE"
            val systemsFooter = buildString {
                append("⚙ SYSTEM STATUS: POWER $powerStateLabel (${snapshot.totalMwGenerated.roundToInt()}/${snapshot.totalMwDemand.roundToInt()} MW)")
                append(" • ROOMS ${snapshot.onlineRoomsCount}/${snapshot.totalBuiltRoomsCount}")
                append(" • O2 ${snapshot.oxygenPercent}% • ☢ ${snapshot.radiationMillisieverts}mSv")
                if (snapshot.deepCoreDepthMeters > 0) {
                    append(" • ⛏ ${snapshot.deepCoreDepthMeters}m")
                }
            }

            for (widgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_vault_status)
                views.setTextViewText(R.id.widget_vault_title, titleText)
                views.setTextViewText(R.id.widget_status_alert, snapshot.statusAlertHeadline)
                views.setTextColor(
                    R.id.widget_status_alert,
                    if (snapshot.isCriticalAlert) Color.parseColor("#FF5252") else Color.parseColor("#66BB6A")
                )

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
                    "${formatCompactWidget(snapshot.gold)} (${formatSignedRate(snapshot.goldPerSec)})"
                )
                views.setTextViewText(
                    R.id.widget_res_defense,
                    "${snapshot.totalDefenseRating}/${snapshot.todayRequiredDefense} DEF • ${snapshot.ammoRounds} Rds"
                )
                views.setTextViewText(R.id.widget_attack_status, attackStatusText)
                views.setTextColor(
                    R.id.widget_attack_status,
                    if (snapshot.lastAttackSucceeded) Color.parseColor("#66BB6A") else Color.parseColor("#FF5252")
                )
                views.setTextViewText(R.id.widget_dwellers_status, dwellersStatusText)
                views.setTextViewText(R.id.widget_systems_footer, systemsFooter)

                views.setOnClickPendingIntent(R.id.widget_root_container, launchPendingIntent)
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
                .putString("statusAlertHeadline", snap.statusAlertHeadline)
                .putBoolean("isCriticalAlert", snap.isCriticalAlert)
                .apply()
        }

        private fun readSnapshotFromPrefs(context: Context): VaultWidgetSnapshot {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return VaultWidgetSnapshot(
                dayCounter = prefs.getInt("dayCounter", 1),
                commandLevel = prefs.getInt("commandLevel", 1),
                prestigeLevel = prefs.getInt("prestigeLevel", 0),
                scrap = prefs.getFloat("scrap", 0f).toDouble(),
                scrapPerSec = prefs.getFloat("scrapPerSec", 0f).toDouble(),
                water = prefs.getFloat("water", 25f).toDouble(),
                waterPerSec = prefs.getFloat("waterPerSec", 0f).toDouble(),
                rations = prefs.getFloat("rations", 25f).toDouble(),
                rationsPerSec = prefs.getFloat("rationsPerSec", 0f).toDouble(),
                bioFuel = prefs.getFloat("bioFuel", 10f).toDouble(),
                bioFuelPerSec = prefs.getFloat("bioFuelPerSec", 0f).toDouble(),
                gold = prefs.getFloat("gold", 0f).toDouble(),
                goldPerSec = prefs.getFloat("goldPerSec", 0f).toDouble(),
                ammoRounds = prefs.getInt("ammoRounds", 25),
                totalDefenseRating = prefs.getInt("totalDefenseRating", 20),
                todayRequiredDefense = prefs.getInt("todayRequiredDefense", 28),
                defenseIntegrityPercent = prefs.getInt("defenseIntegrityPercent", 60),
                totalRaidsRepelled = prefs.getInt("totalRaidsRepelled", 0),
                lastAttackSucceeded = prefs.getBoolean("lastAttackSucceeded", true),
                lastAttackSummary = prefs.getString("lastAttackSummary", "Awaiting First Siege • Defenses Ready")
                    ?: "Awaiting First Siege • Defenses Ready",
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
                oxygenPercent = prefs.getInt("oxygenPercent", 100),
                radiationMillisieverts = prefs.getInt("radiationMillisieverts", 0),
                deepCoreDepthMeters = prefs.getInt("deepCoreDepthMeters", 0),
                activeBossTitle = prefs.getString("activeBossTitle", "") ?: "",
                statusAlertHeadline = prefs.getString("statusAlertHeadline", "🟢 SYSTEMS NOMINAL • TAP TO OPEN VAULT")
                    ?: "🟢 SYSTEMS NOMINAL • TAP TO OPEN VAULT",
                isCriticalAlert = prefs.getBoolean("isCriticalAlert", false)
            )
        }
    }
}
