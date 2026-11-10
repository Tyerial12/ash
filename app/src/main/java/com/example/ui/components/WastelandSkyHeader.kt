package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.OutpostStateEntity
import com.example.domain.ProductionSnapshot
import com.example.model.ActiveBuildingConstruction
import com.example.model.ActiveDoorUpgrade
import com.example.model.ActiveDwellerExpedition
import com.example.model.ActiveItemCrafting
import com.example.model.CommandLevelProgression
import com.example.model.DayNightPhase
import com.example.model.ResourceType
import com.example.ui.theme.BioluminescentViolet
import com.example.ui.theme.BunkerSlate
import com.example.ui.theme.CryoCyan
import com.example.ui.theme.HazardRed
import com.example.ui.theme.ObsidianCharcoal
import com.example.ui.theme.RadTeal
import com.example.ui.theme.RustCopper
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.SurplusGreen
import com.example.ui.theme.TextMutedAsh
import com.example.ui.theme.WarningOrange
import com.example.ui.theme.XpGold
import java.util.Locale
import kotlin.math.roundToInt

private enum class HeaderTelemetryTab(val label: String) {
    COMPACT("Compact"),
    RESOURCES_GATHERED("Resources Gathered"),
    SYSTEMS_ONLINE("Systems Online")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WastelandSkyHeader(
    state: OutpostStateEntity,
    production: ProductionSnapshot,
    activeConstruction: ActiveBuildingConstruction? = null,
    activeCrafting: ActiveItemCrafting? = null,
    activeDoorUpgrade: ActiveDoorUpgrade? = null,
    activeExpedition: ActiveDwellerExpedition? = null,
    onRequestRestartGame: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableStateOf(HeaderTelemetryTab.COMPACT) }
    val phase = production.activePhase
    val xpReq = CommandLevelProgression.xpRequiredForLevel(state.commandLevel)
    val xpFraction = (state.currentXp / xpReq).toFloat().coerceIn(0f, 1f)
    val isBlackout = production.isVaultBlackout
    val isBrownout = production.isVaultBrownout
    val isNightDanger = phase == DayNightPhase.RAD_NIGHT

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        color = BunkerSlate,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = when {
                        isBlackout || production.isWorkersStarvingOrDehydrated -> HazardRed.copy(alpha = 0.85f)
                        isNightDanger || state.needsDefenseRepair -> WarningOrange.copy(alpha = 0.8f)
                        else -> phase.accentColor.copy(alpha = 0.45f)
                    },
                    shape = RoundedCornerShape(14.dp)
                )
        ) {
            // ROW 1: Compact Vault Status & Life-Support Strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_wasteland_outpost_1794107188307),
                    contentDescription = "Vault City Skyline",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    alpha = 0.28f
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    phase.accentColor.copy(alpha = 0.22f),
                                    ObsidianCharcoal.copy(alpha = 0.94f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                color = XpGold.copy(alpha = 0.22f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.border(1.dp, XpGold, RoundedCornerShape(6.dp))
                            ) {
                                Text(
                                    text = if (state.prestigeLevel > 0) {
                                        "★P${state.prestigeLevel} • LV.${state.commandLevel}"
                                    } else {
                                        "LV.${state.commandLevel}/100"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = XpGold,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = "DAY ${state.dayCounter} • ${phase.displayName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isNightDanger) BioluminescentViolet else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val siegeBadgeColor = when {
                                production.isDefenseShutDownByDeficit || isBlackout || production.isWorkersStarvingOrDehydrated -> HazardRed
                                production.daysUntilDay10Siege == 0 || isNightDanger -> WarningOrange
                                else -> SolarAmber
                            }
                            Text(
                                text = when {
                                    production.isDefenseShutDownByDeficit -> "DEFICIT ${state.deficitDaysCount}/2D!"
                                    isBlackout -> "NO POWER ${state.blackoutDaysCount}/6D!"
                                    production.isWorkersStarvingOrDehydrated -> "CREW STARVING!"
                                    production.daysUntilDay10Siege == 0 -> "10D MEGA-SIEGE!"
                                    isNightDanger -> "NIGHT SWARM +35%"
                                    else -> "10D SIEGE: ${production.daysUntilDay10Siege}D"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = siegeBadgeColor,
                                fontWeight = FontWeight.Bold
                            )

                            Surface(
                                color = HazardRed.copy(alpha = 0.22f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, HazardRed.copy(alpha = 0.85f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable(onClick = onRequestRestartGame)
                                    .testTag("restart_game_header_button")
                            ) {
                                Text(
                                    text = "RESTART",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = HazardRed,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Life-Support Telemetry Pills Row
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        val powerStatusLabel = when {
                            isBrownout -> "⚡ ${production.totalMegawattGenerated.roundToInt()}/${production.totalMegawattDemand.roundToInt()}MW (LOW)"
                            isBlackout -> "⚡ ${production.totalMegawattGenerated.roundToInt()}/${production.totalMegawattDemand.roundToInt()}MW (OUT)"
                            else -> "⚡ ${production.totalMegawattGenerated.roundToInt()}/${production.totalMegawattDemand.roundToInt()}MW"
                        }
                        val powerColor = when {
                            isBrownout -> WarningOrange
                            isBlackout -> HazardRed
                            else -> SurplusGreen
                        }
                        StatusMiniText(powerStatusLabel, powerColor)

                        val o2Color = when {
                            state.oxygenPercent < 45 -> HazardRed
                            state.oxygenPercent < 75 -> WarningOrange
                            else -> CryoCyan
                        }
                        StatusMiniText("O2: ${state.oxygenPercent}%", o2Color)

                        val radColor = when {
                            state.radiationMillisieverts >= 120 -> HazardRed
                            state.radiationMillisieverts >= 35 -> WarningOrange
                            else -> SurplusGreen
                        }
                        StatusMiniText("RAD: ${state.radiationMillisieverts}mSv", radColor)

                        val autoDefLabel = when {
                            production.isDefenseShutDownByDeficit -> "DEF: SHUT DOWN (0)"
                            production.isAutoDefenseActive -> "AUTO-DEF: ON"
                            else -> "AUTO-DEF: OFF"
                        }
                        val autoDefColor = when {
                            production.isDefenseShutDownByDeficit -> HazardRed
                            production.isAutoDefenseActive -> SurplusGreen
                            else -> WarningOrange
                        }
                        StatusMiniText(autoDefLabel, autoDefColor)

                        val doorColor = if (state.needsDefenseRepair) WarningOrange else RadTeal
                        StatusMiniText("DOOR: ${state.defenseIntegrityPercent}%", doorColor)
                    }

                    LinearProgressIndicator(
                        progress = { xpFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = XpGold,
                        trackColor = ObsidianCharcoal
                    )
                }
            }

            // HARDCORE DOOMSDAY & 5-MINUTE DWELLER FEEDING STATUS STRIP
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ObsidianCharcoal.copy(alpha = 0.96f))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                StatusMiniText(
                    text = "⚔ 10D SIEGE: ${production.daysUntilDay10Siege}D (${production.day10RequiredDefense} DEF)",
                    color = if (production.daysUntilDay10Siege <= 2) WarningOrange else SolarAmber
                )
                StatusMiniText(
                    text = "⚡ OUTAGE: ${state.blackoutDaysCount}/6D",
                    color = if (isBlackout) HazardRed else SurplusGreen
                )
                StatusMiniText(
                    text = "🛡 DEFICIT: ${state.deficitDaysCount}/2D",
                    color = if (production.isDefenseShutDownByDeficit) HazardRed else SurplusGreen
                )
                val appetitePct = ((production.dwellerUpkeepEscalationMultiplier - 1.0) * 100).roundToInt()
                StatusMiniText(
                    text = "🍖 60S FEED: ${state.dwellerFeedCycleSecondsRemaining}s (+$appetitePct%/2m)",
                    color = if (state.dwellerFeedCycleSecondsRemaining <= 15 && state.totalSurvivors > 0) WarningOrange else CryoCyan
                )
            }

            // Always-Visible Net Resource Balance Readout (Positive Surplus vs Taking More Than Making)
            val isPositiveBalance = production.isMakingPositiveResources
            val netSign = if (production.netResourceBalancePerSec >= 0.0) "+" else ""
            Surface(
                color = (if (isPositiveBalance) SurplusGreen else HazardRed).copy(alpha = 0.16f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp)
                    .testTag("header_net_resource_balance_badge"),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, if (isPositiveBalance) SurplusGreen.copy(alpha = 0.75f) else HazardRed)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isPositiveBalance) {
                            "🟢 MAKING POSITIVE RESOURCES ($netSign${formatRate(production.netResourceBalancePerSec)}/s NET)"
                        } else {
                            "🔴 TAKING MORE THAN MAKING (${formatRate(production.netResourceBalancePerSec)}/s NET)"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isPositiveBalance) SurplusGreen else HazardRed,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Gain +${formatRate(production.totalGrossResourceGainPerSec)}/s vs Drain -${formatRate(production.totalVaultResourceDrainPerSec)}/s • Cap ${production.storageCap.toInt()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (production.isDefenseShutDownByDeficit || isBlackout) {
                Surface(
                    color = HazardRed.copy(alpha = 0.18f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, HazardRed)
                ) {
                    Text(
                        text = when {
                            isBlackout && production.isDefenseShutDownByDeficit ->
                                "🚨 BLACKOUT (${state.blackoutDaysCount}/6D) & DEFICIT (${state.deficitDaysCount}/2D): Defenses SHUT DOWN (0 DEF)! Crank Dynamo or build Power & Resource rooms before Game Over!"
                            production.isDefenseShutDownByDeficit ->
                                "🚨 RESOURCE DEFICIT (${state.deficitDaysCount}/2D): Vault takes ${formatRate(production.totalVaultResourceDrainPerSec)}/s vs +${formatRate(production.totalGrossResourceGainPerSec)}/s gained! Defenses SHUT DOWN (0 DEF) — Game Over in ${production.deficitDaysRemainingBeforeGameOver}D!"
                            else ->
                                "🚨 POWER BLACKOUT (${state.blackoutDaysCount}/6D): Out of Power! Game ends in ${production.blackoutDaysRemainingBeforeGameOver} Day(s) unless MW is restored!"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = HazardRed,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // ROW 2: Mobile-Friendly Collapsible Telemetry Drawer Tab Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ObsidianCharcoal.copy(alpha = 0.92f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderTelemetryTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    val tag = when (tab) {
                        HeaderTelemetryTab.COMPACT -> "header_tab_compact"
                        HeaderTelemetryTab.RESOURCES_GATHERED -> "header_tab_resources"
                        HeaderTelemetryTab.SYSTEMS_ONLINE -> "header_tab_systems"
                    }
                    Surface(
                        color = if (isSelected) SolarAmber.copy(alpha = 0.22f) else BunkerSlate,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) SolarAmber else TextMutedAsh.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                selectedTab = if (selectedTab == tab && tab != HeaderTelemetryTab.COMPACT) {
                                    HeaderTelemetryTab.COMPACT
                                } else {
                                    tab
                                }
                            }
                            .testTag(tag)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) SolarAmber else TextMutedAsh,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // ROW 3: Always-Visible Compact Primary Resource Strip
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ObsidianCharcoal.copy(alpha = 0.82f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                ResourceChip("SCRAP", state.scrap, production.scrapPerSec, ResourceType.SCRAP.color)
                ResourceChip("WATER", state.water, production.waterPerSec, ResourceType.WATER.color)
                ResourceChip("FOOD", state.rations, production.rationsPerSec, ResourceType.RATIONS.color)
                ResourceChip("FUEL", state.bioFuel, production.bioFuelPerSec, ResourceType.BIO_FUEL.color)
                RareBadgeChip("GOLD", state.gold.toInt(), XpGold)
                RareBadgeChip("AMMO", state.ammoRounds, SolarAmber)
                if (state.prestigeCores > 0) {
                    RareBadgeChip("★CORES", state.prestigeCores, XpGold)
                }
            }

            // EXPANDABLE TELEMETRY DRAWER: Resources Gathered OR Current Systems Online
            AnimatedVisibility(visible = selectedTab != HeaderTelemetryTab.COMPACT) {
                Surface(
                    color = ObsidianCharcoal.copy(alpha = 0.96f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    when (selectedTab) {
                        HeaderTelemetryTab.RESOURCES_GATHERED -> ResourcesGatheredDrawerPanel(
                            state = state,
                            production = production
                        )
                        HeaderTelemetryTab.SYSTEMS_ONLINE -> CurrentSystemsOnlineDrawerPanel(
                            state = state,
                            production = production,
                            activeConstruction = activeConstruction,
                            activeCrafting = activeCrafting,
                            activeDoorUpgrade = activeDoorUpgrade,
                            activeExpedition = activeExpedition
                        )
                        HeaderTelemetryTab.COMPACT -> {}
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResourcesGatheredDrawerPanel(
    state: OutpostStateEntity,
    production: ProductionSnapshot
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SolarAmber.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
            .padding(8.dp)
            .testTag("drawer_resources_gathered"),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RESOURCES GATHERED & DAILY WORKER UPKEEP",
                style = MaterialTheme.typography.labelMedium,
                color = SolarAmber,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Storage Cap: ${production.storageCap.toInt()} (+${production.storeRoomStorageBonus.toInt()} Store Rm)",
                style = MaterialTheme.typography.labelSmall,
                color = RadTeal,
                fontWeight = FontWeight.Bold
            )
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ResourceChip("SCRAP", state.scrap, production.scrapPerSec, SolarAmber)
            ResourceChip("WATER", state.water, production.waterPerSec, CryoCyan)
            ResourceChip("FOOD", state.rations, production.rationsPerSec, SurplusGreen)
            ResourceChip("FUEL", state.bioFuel, production.bioFuelPerSec, RustCopper)
            ResourceChip("TECH", state.researchData, production.researchPerSec, RadTeal)
            ResourceChip("MUTAGEN", state.mutagenCores, production.mutagenCoresPerSec, BioluminescentViolet)
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            RareBadgeChip("GOLD", state.gold.toInt(), XpGold)
            RareBadgeChip("THORIUM", state.thoriumRods.toInt(), WarningOrange)
            RareBadgeChip("AETHER", state.aetherCells.toInt(), CryoCyan)
            RareBadgeChip("CRATES", state.rawSalvageCaches, SolarAmber)
            RareBadgeChip("PRESTIGE CORES", state.prestigeCores, XpGold)
        }

        val upkeepColor = when {
            production.isWorkersStarvingOrDehydrated || production.isDefenseShutDownByDeficit || !production.isMakingPositiveResources -> HazardRed
            else -> SurplusGreen
        }
        Text(
            text = if (production.isWorkersStarvingOrDehydrated) {
                "ALERT: Workers need Food & Water daily and every 60 seconds! Stock is 0 — All worker rooms & automated turrets are HALTED until fed!"
            } else {
                "Vault Drain: -${formatRate(production.totalVaultResourceDrainPerSec)}/s (Reactors -${formatRate(production.reactorFuelUpkeepPerSec)}/s Fuel) vs Gain +${formatRate(production.totalGrossResourceGainPerSec)}/s • 60s Feed (${state.dwellerFeedCycleSecondsRemaining}s): -${production.fiveMinDwellerFoodCost.roundToInt()}F/-${production.fiveMinDwellerWaterCost.roundToInt()}W (2m Tier ${production.dwellerUpkeepEscalationTier})"
            },
            style = MaterialTheme.typography.labelSmall,
            color = upkeepColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CurrentSystemsOnlineDrawerPanel(
    state: OutpostStateEntity,
    production: ProductionSnapshot,
    activeConstruction: ActiveBuildingConstruction?,
    activeCrafting: ActiveItemCrafting?,
    activeDoorUpgrade: ActiveDoorUpgrade?,
    activeExpedition: ActiveDwellerExpedition?
) {
    val hasBuiltTurrets = state.sentryTurretCount > 0 || state.plasmaCannonCount > 0
    val hasFedAssignedWorkers = state.totalSurvivors > 0 && production.totalAssignedWorkers > 0 && !production.isWorkersStarvingOrDehydrated
    val hasGridPower = !production.isVaultBlackout

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, RadTeal.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
            .padding(8.dp)
            .testTag("drawer_systems_online"),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = "CURRENT SYSTEMS ONLINE & LIVE TELEMETRY",
            style = MaterialTheme.typography.labelMedium,
            color = RadTeal,
            fontWeight = FontWeight.Bold
        )

        // 1. Power Grid MW Load, Generator Wear, and Blackout Status
        val gridStatus = when {
            production.isVaultBlackout -> "BLACKOUT (OFFLINE)"
            production.isVaultBrownout -> "BROWNOUT (LOW MW)"
            else -> "ONLINE (${(production.powerGridEfficiencyMultiplier * 100).roundToInt()}% EFF)"
        }
        Text(
            text = "⚡ Power Grid: ${production.totalMegawattGenerated.roundToInt()}/${production.totalMegawattDemand.roundToInt()} MW • $gridStatus • Gen Wear: ${state.generatorWearPercent}%",
            style = MaterialTheme.typography.labelSmall,
            color = if (production.isVaultBlackout) HazardRed else SurplusGreen,
            fontWeight = FontWeight.Bold
        )

        // 2. Automated Defense Grid Status (Requires Built Turrets + Fed Workers + Power)
        val defStatus = if (production.isAutoDefenseActive) "ONLINE & ARMED" else "OFFLINE (CHECK REQUIREMENTS)"
        Text(
            text = "🛡 Auto-Defense Grid: $defStatus • Built(${if (hasBuiltTurrets) "YES" else "NO"}) • Fed Crew(${if (hasFedAssignedWorkers) "YES" else "NO"}) • Power(${if (hasGridPower) "YES" else "NO"}) • Door Lv.${state.blastDoorLevel} (${state.defenseIntegrityPercent}%)",
            style = MaterialTheme.typography.labelSmall,
            color = if (production.isAutoDefenseActive) SurplusGreen else WarningOrange,
            fontWeight = FontWeight.Bold
        )

        // 3. Life Support O2 Scrubbers, Radiation Levels, and Room Automation Status
        Text(
            text = "🫁 Life Support & Rooms: ${production.onlineRoomsCount}/${production.totalBuiltRoomsCount} Rooms Online • O2 ${state.oxygenPercent}% (${formatRate(production.oxygenDeltaPerSec)}/s) • Rads ${state.radiationMillisieverts}mSv (${formatRate(production.radiationDeltaPerSec)}/s)",
            style = MaterialTheme.typography.labelSmall,
            color = CryoCyan
        )

        // 4. Active Timers for Room Construction, Blast Door Upgrade, Item Crafting, and Wasteland Expeditions
        val buildTimerStr = activeConstruction?.let {
            "Build ${it.buildingType.displayName} Lv.${it.targetLevel}: ${it.remainingSeconds}s"
        } ?: "Build: Idle"
        val doorTimerStr = activeDoorUpgrade?.let {
            "Door Lv.${it.targetLevel}: ${it.remainingSeconds}s"
        } ?: "Door: Idle"
        val craftTimerStr = activeCrafting?.let {
            "Craft ${it.item.title}: ${it.remainingSeconds}s"
        } ?: "Armory: Idle"
        val expTimerStr = activeExpedition?.let {
            "Expedition: ${it.remainingSeconds}s"
        } ?: "Expedition: Idle"

        Text(
            text = "⏱ Active Timers: $buildTimerStr • $doorTimerStr • $craftTimerStr • $expTimerStr",
            style = MaterialTheme.typography.labelSmall,
            color = XpGold,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatusMiniText(
    text: String,
    color: Color
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun ResourceChip(
    label: String,
    amount: Double,
    rate: Double,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 2.dp, vertical = 1.dp)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelSmall,
            color = TextMutedAsh
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = formatCompact(amount),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
        val sign = if (rate >= 0) "+" else ""
        Text(
            text = "($sign${formatRate(rate)})",
            style = MaterialTheme.typography.labelSmall,
            color = if (rate >= 0) SurplusGreen else WarningOrange
        )
    }
}

@Composable
private fun RareBadgeChip(
    label: String,
    amount: Int,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = amount.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

fun formatCompact(value: Double): String {
    return when {
        value >= 10_000 -> String.format(Locale.US, "%.1fk", value / 1000.0)
        value >= 100 -> value.toInt().toString()
        else -> String.format(Locale.US, "%.1f", value)
    }
}

fun formatRate(value: Double): String {
    return String.format(Locale.US, "%.1f", value)
}
