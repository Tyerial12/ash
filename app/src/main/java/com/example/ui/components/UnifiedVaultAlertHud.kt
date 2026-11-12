package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.OutpostStateEntity
import com.example.domain.ProductionSnapshot
import com.example.model.RaiderSiegeThreat
import com.example.ui.theme.BunkerCharcoal
import com.example.ui.theme.CryoCyan
import com.example.ui.theme.HazardRed
import com.example.ui.theme.ObsidianCharcoal
import com.example.ui.theme.RustCopper
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.WarningOrange

import com.example.ui.theme.SurplusGreen
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.PanelBorder
import com.example.ui.theme.RadTeal
import com.example.ui.theme.SteelPanel
import com.example.viewmodel.OutpostUiState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.NotificationsActive
import java.util.Locale
import kotlin.math.roundToInt

data class VaultCrisisAlert(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color,
    val actionLabel: String,
    val onAction: () -> Unit
)

fun countActiveVaultAlerts(
    state: OutpostStateEntity,
    production: ProductionSnapshot,
    activeRaid: RaiderSiegeThreat?
): Int {
    var count = 0
    if (production.isStoreRoomFull || production.isAnyStoreRoomResourceHalted) count++
    if (!production.isMakingPositiveResources && !production.isAnyStoreRoomResourceHalted) count++
    if (production.isVaultBlackout || production.isVaultBrownout) count++
    if (activeRaid != null) count++
    if (state.sabotagedBuildingId.isNotEmpty() && state.sabotagedBuildingPenaltyPercent > 0) count++
    if (state.activePipeLeaksCount > 0) count++
    if (state.activeCaveInHazard) count++
    if (state.generatorWearPercent >= 60) count++
    if (state.bioTurbineLevel > 0 && state.bioFuel <= 2.0 && production.bioFuelPerSec < 0.0) count++
    if (state.bleedingDwellersCount > 0 || state.venomInfectedDwellersCount > 0) count++
    if (production.isWorkersStarvingOrDehydrated) count++
    if (state.oxygenPercent < 55 || state.radiationMillisieverts >= 100) count++
    if (production.activeSubterraneanBoss != null && state.subterraneanBossHpPercent > 0) count++
    if (state.dwellerStaminaPercent < 35 || state.sickDwellersCount > 0) count++
    if (production.isRoomsLackingScrap) count++
    if (!production.hasWaterPurifierRoom || !production.hasHydroponicsRoom) count++
    return count
}

@Composable
fun UnifiedVaultAlertHud(
    state: OutpostStateEntity,
    production: ProductionSnapshot,
    activeRaid: RaiderSiegeThreat?,
    onNavigateToCrisis: (String) -> Unit,
    onQuickRepairSabotage: () -> Unit,
    onQuickCrankDynamo: () -> Unit,
    onQuickTuneGenerators: () -> Unit,
    onQuickStabilizeAll: () -> Unit = {},
    alwaysShowAllInTab: Boolean = false,
    modifier: Modifier = Modifier
) {
    var expandAllCrises by rememberSaveable { mutableStateOf(alwaysShowAllInTab) }
    val activeCrises = mutableListOf<VaultCrisisAlert>()

    // 0. Store Room Full / Individual Resource Shelf At Cap Alert (50% Auto-Resume)
    if (production.isStoreRoomFull) {
        activeCrises.add(
            VaultCrisisAlert(
                id = "crisis_store_room_full",
                title = "STORE ROOM FULL (${production.storageCap.toInt()} CAP PER RESOURCE)",
                subtitle = "All resource shelves are at cap. Each resource automatically resumes production at ≤${production.storeRoomResumeThreshold.toInt()} (50% cap) — no Gold needed!",
                icon = Icons.Default.Inventory2,
                accentColor = WarningOrange,
                actionLabel = "STORAGE",
                onAction = { onNavigateToCrisis("logistics_ledger") }
            )
        )
    } else if (production.isAnyStoreRoomResourceHalted) {
        activeCrises.add(
            VaultCrisisAlert(
                id = "crisis_resource_shelf_halted",
                title = "${production.haltedResourcesSummary.uppercase()} AT CAP (RESUMES AT ≤${production.storeRoomResumeThreshold.toInt()})",
                subtitle = "Paused shelves auto-resume when they drop to ≤${production.storeRoomResumeThreshold.toInt()} (50% of ${production.storageCap.toInt()} cap). Other resources continue producing!",
                icon = Icons.Default.Inventory2,
                accentColor = SolarAmber,
                actionLabel = "STORAGE",
                onAction = { onNavigateToCrisis("logistics_ledger") }
            )
        )
    }

    // 1. Blackout or Brownout Alert (Highest Priority)
    if (production.isVaultBlackout) {
        val crankAvailable = state.manualDynamoCranksRemaining > 0
        activeCrises.add(
            VaultCrisisAlert(
                id = "crisis_blackout",
                title = "GRID BLACKOUT (${production.totalMegawattGenerated.toInt()}/${production.totalMegawattDemand.toInt()} MW)",
                subtitle = if (crankAvailable) "Rooms & turrets offline! Hand-crank dynamo for emergency power." else "Zero fuel / power! Service generators or restore fuel.",
                icon = Icons.Default.ElectricBolt,
                accentColor = HazardRed,
                actionLabel = if (crankAvailable) "CRANK DYNAMO" else "VIEW GRID",
                onAction = if (crankAvailable) onQuickCrankDynamo else { { onNavigateToCrisis("operations_power") } }
            )
        )
    } else if (production.isVaultBrownout) {
        activeCrises.add(
            VaultCrisisAlert(
                id = "crisis_brownout",
                title = "GRID BROWNOUT DEFICIT",
                subtitle = "Power deficit throttling facility output to 40%.",
                icon = Icons.Default.ElectricBolt,
                accentColor = WarningOrange,
                actionLabel = "STABILIZE GRID",
                onAction = { onNavigateToCrisis("operations_power") }
            )
        )
    }

    // 2. Active Raider Threat (120s tactical approach)
    if (activeRaid != null) {
        val ready = production.totalDefenseRating >= activeRaid.requiredDefenseRating
        activeCrises.add(
            VaultCrisisAlert(
                id = "crisis_raid",
                title = "${activeRaid.title.uppercase()} (${activeRaid.remainingSeconds}s)",
                subtitle = if (activeRaid.targetFacilityName.isNotEmpty()) "Targeting: ${activeRaid.targetFacilityName} • Req: ${activeRaid.requiredDefenseRating} DEF" else "Req: ${activeRaid.requiredDefenseRating} DEF",
                icon = Icons.Default.Security,
                accentColor = if (ready) SolarAmber else HazardRed,
                actionLabel = if (ready) "INTERCEPT" else "REINFORCE",
                onAction = { onNavigateToCrisis("defense_perimeter") }
            )
        )
    }

    // 3. Targeted Facility Sabotage
    if (state.sabotagedBuildingId.isNotEmpty() && state.sabotagedBuildingPenaltyPercent > 0) {
        activeCrises.add(
            VaultCrisisAlert(
                id = "crisis_sabotage",
                title = "SABOTAGED: ${state.sabotagedBuildingId.replace('_', ' ')}",
                subtitle = "-50% output penalty from breach! Emergency repair required.",
                icon = Icons.Default.Warning,
                accentColor = HazardRed,
                actionLabel = "REPAIR ROOM",
                onAction = onQuickRepairSabotage
            )
        )
    }

    // 4. Pipe Leaks or Structural Wear Hazard
    if (state.activePipeLeaksCount > 0) {
        activeCrises.add(
            VaultCrisisAlert(
                id = "crisis_leaks",
                title = "${state.activePipeLeaksCount} ACTIVE PIPE LEAKS",
                subtitle = "Coolant leaking (-${String.format("%.1f", state.activePipeLeaksCount * 0.35)} Water/s). Patch immediately!",
                icon = Icons.Default.Opacity,
                accentColor = CryoCyan,
                actionLabel = "PATCH LEAK",
                onAction = { onNavigateToCrisis("operations_matrix") }
            )
        )
    }

    // 5. Mining Shaft Cave-In Hazard
    if (state.activeCaveInHazard) {
        activeCrises.add(
            VaultCrisisAlert(
                id = "crisis_cave_in",
                title = "MINING SHAFT CAVE-IN COLLAPSE",
                subtitle = "Tunnel rubble blocking veins (-45% Pickaxe yield)! Shore up timber.",
                icon = Icons.Default.Build,
                accentColor = HazardRed,
                actionLabel = "MINE & BUILD",
                onAction = { onNavigateToCrisis("operations_matrix") }
            )
        )
    }

    // 6. Worn / Jammed Dynamo Gears
    if (state.generatorWearPercent >= 60) {
        activeCrises.add(
            VaultCrisisAlert(
                id = "crisis_dynamo_wear",
                title = "DYNAMO WEAR CRITICAL (${state.generatorWearPercent}%)",
                subtitle = "Flywheels damaged by cranking! Tune with 6-8 Scrap.",
                icon = Icons.Default.Build,
                accentColor = WarningOrange,
                actionLabel = "TUNE GEARS",
                onAction = onQuickTuneGenerators
            )
        )
    }

    // 7. Fuel Starvation Warning
    if (state.bioTurbineLevel > 0 && state.bioFuel <= 2.0 && production.bioFuelPerSec < 0.0) {
        activeCrises.add(
            VaultCrisisAlert(
                id = "crisis_low_fuel",
                title = "FUEL STARVATION CRITICAL (${String.format("%.1f", state.bioFuel)} FUEL)",
                subtitle = "Turbines burning out! Refill Bio-Fuel or generators will trip.",
                icon = Icons.Default.LocalGasStation,
                accentColor = RustCopper,
                actionLabel = "REFINERY",
                onAction = { onNavigateToCrisis("operations_matrix") }
            )
        )
    }

    // 8. Hunter Bleeding / Rad-Venom Trauma
    if (state.bleedingDwellersCount > 0 || state.venomInfectedDwellersCount > 0) {
        activeCrises.add(
            VaultCrisisAlert(
                id = "crisis_trauma",
                title = "HUNTER TRAUMA (${state.bleedingDwellersCount} BLEED / ${state.venomInfectedDwellersCount} VENOM)",
                subtitle = "Beast wounds draining crew health & morale! Suture in Medbay.",
                icon = Icons.Default.Warning,
                accentColor = HazardRed,
                actionLabel = "MEDBAY",
                onAction = { onNavigateToCrisis("defense_perimeter") }
            )
        )
    }

    // 9. Starvation / Dehydration Danger
    if (production.isWorkersStarvingOrDehydrated) {
        activeCrises.add(
            VaultCrisisAlert(
                id = "crisis_starvation",
                title = "DWELLER STARVATION / DEHYDRATION",
                subtitle = "Rations or water depleted! All automated production is frozen.",
                icon = Icons.Default.Restaurant,
                accentColor = HazardRed,
                actionLabel = "SUPPLIES",
                onAction = { onNavigateToCrisis("logistics_ledger") }
            )
        )
    }

    // 10. Low Oxygen or Severe Radiation
    if (state.oxygenPercent < 55 || state.radiationMillisieverts >= 100) {
        activeCrises.add(
            VaultCrisisAlert(
                id = "crisis_life_support",
                title = "LIFE SUPPORT HAZARD (O2 ${state.oxygenPercent}% • ${state.radiationMillisieverts} mSv)",
                subtitle = "Scrub air or flush radiation immediately to protect Dwellers!",
                icon = Icons.Default.Warning,
                accentColor = WarningOrange,
                actionLabel = "STABILIZE",
                onAction = onQuickStabilizeAll
            )
        )
    }

    // 11. Subterranean Boss Awakened
    val activeBoss = production.activeSubterraneanBoss
    if (activeBoss != null && state.subterraneanBossHpPercent > 0) {
        activeCrises.add(
            VaultCrisisAlert(
                id = "crisis_subterranean_boss",
                title = "DEEP BOSS: ${activeBoss.title.uppercase()} (${state.subterraneanBossHpPercent}% HP)",
                subtitle = "${activeBoss.hazardDescription} • Strike in Mine & Build!",
                icon = Icons.Default.Security,
                accentColor = HazardRed,
                actionLabel = "STRIKE BOSS",
                onAction = { onNavigateToCrisis("operations_matrix") }
            )
        )
    }

    // 12. Crew Exhaustion / Sickness Outbreak
    if (state.dwellerStaminaPercent < 35 || state.sickDwellersCount > 0) {
        activeCrises.add(
            VaultCrisisAlert(
                id = "crisis_stamina_sickness",
                title = "CREW EXHAUSTION / SICKNESS (STAMINA ${state.dwellerStaminaPercent}% • ${state.sickDwellersCount} SICK)",
                subtitle = "Fatigue or dysentery throttling Vault production! Rotate bunks or cure crew.",
                icon = Icons.Default.Warning,
                accentColor = WarningOrange,
                actionLabel = "QUARANTINE",
                onAction = { onNavigateToCrisis("logistics_ledger") }
            )
        )
    }

    // 13. Rooms Lacking Daily Scrap Operational Upkeep
    if (production.isRoomsLackingScrap) {
        activeCrises.add(
            VaultCrisisAlert(
                id = "crisis_rooms_lacking_scrap",
                title = "ROOMS LACKING DAILY SCRAP (${state.scrap.toInt()}/${production.dailyRoomScrapUpkeep.toInt()} SCRAP/DAY)",
                subtitle = "Rooms require ${production.dailyRoomScrapUpkeep.toInt()} Scrap/day across all room levels to stay 100% operational (currently ${(production.roomScrapOperationalMultiplier * 100).roundToInt()}% output).",
                icon = Icons.Default.Build,
                accentColor = WarningOrange,
                actionLabel = "MINE SCRAP",
                onAction = { onNavigateToCrisis("operations_matrix") }
            )
        )
    }

    // 14. Water Purifier / Hydroponics Room Missing (Manual-Only Gathering Alert)
    if (!production.hasWaterPurifierRoom || !production.hasHydroponicsRoom) {
        val missingRoomsTitle = when {
            !production.hasWaterPurifierRoom && !production.hasHydroponicsRoom -> "WATER & FOOD IN MANUAL-ONLY MODE"
            !production.hasWaterPurifierRoom -> "WATER IN MANUAL-ONLY MODE (BUILD WATER PURIFIER)"
            else -> "FOOD IN MANUAL-ONLY MODE (BUILD HYDROPONICS ROOM)"
        }
        val missingRoomsSubtitle = when {
            !production.hasWaterPurifierRoom && !production.hasHydroponicsRoom ->
                "Build Water Purifier (Atmospheric Condenser) & Hydroponics Room for automated Water/Food. Until built, gather Water & Food manually!"
            !production.hasWaterPurifierRoom ->
                "Automated Water is locked until you build the Water Purifier room. Boil water or salvage manually!"
            else ->
                "Automated Food is locked until you build the Hydroponics Greenhouse room. Forage, fish, or hunt manually!"
        }
        activeCrises.add(
            VaultCrisisAlert(
                id = "crisis_manual_water_food_only",
                title = missingRoomsTitle,
                subtitle = missingRoomsSubtitle,
                icon = if (!production.hasWaterPurifierRoom) Icons.Default.Opacity else Icons.Default.Restaurant,
                accentColor = SolarAmber,
                actionLabel = "BUILD ROOMS",
                onAction = { onNavigateToCrisis("operations_matrix") }
            )
        )
    }

    if (alwaysShowAllInTab && activeCrises.isEmpty()) {
        Surface(
            color = ObsidianCharcoal,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, SurplusGreen.copy(alpha = 0.6f)),
            modifier = modifier
                .fillMaxWidth()
                .testTag("vault_alert_hud_nominal")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "✓ ALL VAULT SYSTEMS & STORAGE SHELVES NOMINAL",
                        style = MaterialTheme.typography.labelMedium,
                        color = SurplusGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "No active crises, leaks, or capped storage shelves. Resources pause at ${production.storageCap.toInt()} cap and auto-resume at ≤${production.storeRoomResumeThreshold.toInt()} (50%).",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }
        return
    }

    var lastNonEmptyCrises by androidx.compose.runtime.remember {
        mutableStateOf<List<VaultCrisisAlert>>(emptyList())
    }
    if (activeCrises.isNotEmpty()) {
        lastNonEmptyCrises = activeCrises
    }
    val displayCrises = if (activeCrises.isNotEmpty()) activeCrises else lastNonEmptyCrises

    AnimatedVisibility(
        visible = activeCrises.isNotEmpty(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier.fillMaxWidth()
    ) {
        val topCrisis = displayCrises.firstOrNull() ?: return@AnimatedVisibility
        val showExpandedList = alwaysShowAllInTab || expandAllCrises
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (alwaysShowAllInTab) 0.dp else 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                color = ObsidianCharcoal,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.5.dp, topCrisis.accentColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { topCrisis.onAction() }
                    .testTag("vault_alert_hud_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = topCrisis.icon,
                            contentDescription = null,
                            tint = topCrisis.accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = topCrisis.title,
                                style = MaterialTheme.typography.labelMedium,
                                color = topCrisis.accentColor,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = topCrisis.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary,
                                maxLines = 2
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = topCrisis.onAction,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = topCrisis.accentColor,
                                contentColor = BunkerCharcoal
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("alert_hud_action_button")
                        ) {
                            Text(
                                text = topCrisis.actionLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = onQuickStabilizeAll,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SolarAmber,
                                contentColor = BunkerCharcoal
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("alert_hud_auto_stabilize_button")
                        ) {
                            Text(
                                text = "FIX ALL",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (displayCrises.size > 1) {
                if (!alwaysShowAllInTab) {
                    Surface(
                        color = BunkerCharcoal,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, WarningOrange.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandAllCrises = !expandAllCrises }
                            .testTag("alert_hud_expand_all_pill")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (expandAllCrises) {
                                    "▼ HIDE ${(displayCrises.size - 1).coerceAtLeast(1)} ADDITIONAL VAULT ALERTS"
                                } else {
                                    "▶ +${(displayCrises.size - 1).coerceAtLeast(1)} OTHER CRITICAL VAULT ALERTS (Tap to expand all)"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = WarningOrange,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "TAP TO ${if (expandAllCrises) "COLLAPSE" else "INSPECT"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SolarAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = showExpandedList) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        displayCrises.drop(1).forEach { crisis ->
                            Surface(
                                color = ObsidianCharcoal.copy(alpha = 0.95f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, crisis.accentColor.copy(alpha = 0.75f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { crisis.onAction() }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = crisis.title,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = crisis.accentColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = crisis.subtitle,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextPrimary,
                                            maxLines = 2
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "[${crisis.actionLabel}]",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = crisis.accentColor,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VaultAlertsTabScreen(
    uiState: OutpostUiState,
    onNavigateToCrisis: (String) -> Unit,
    onQuickRepairSabotage: () -> Unit,
    onQuickCrankDynamo: () -> Unit,
    onQuickTuneGenerators: () -> Unit,
    onQuickStabilizeAll: () -> Unit,
    onForageRations: () -> Unit,
    onBoilWater: () -> Unit,
    onScrubAir: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state = uiState.state
    val production = uiState.production
    val activeAlertCount = countActiveVaultAlerts(state, production, uiState.activeRaidThreat)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("vault_alerts_tab_screen"),
        contentPadding = PaddingValues(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Dedicated Alerts Center Header Card
        item {
            Surface(
                color = SteelPanel,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    1.5.dp,
                    if (activeAlertCount > 0) WarningOrange else SurplusGreen.copy(alpha = 0.7f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("alerts_tab_summary_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Vault Alerts Center",
                            tint = if (activeAlertCount > 0) WarningOrange else SurplusGreen,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (activeAlertCount > 0) {
                                    "VAULT ALERTS CENTER ($activeAlertCount ACTIVE)"
                                } else {
                                    "VAULT ALERTS CENTER (ALL CLEAR)"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                color = if (activeAlertCount > 0) WarningOrange else SurplusGreen,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "All Vault warnings, storage shelf halts, room scrap upkeep alerts, and crisis actions are consolidated here.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onQuickStabilizeAll,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SolarAmber,
                            contentColor = BunkerCharcoal
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("alerts_tab_fix_all_button")
                    ) {
                        Text(
                            text = "FIX ALL",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // 2. Net Resource Balance & Store Room Shelf Status Banner (Moved from Top Header)
        item {
            val isStoreFull = production.isStoreRoomFull
            val isShelfHalted = production.isAnyStoreRoomResourceHalted
            val isPositiveBalance = production.isMakingPositiveResources
            val netSign = if (production.netResourceBalancePerSec >= 0.0) "+" else ""
            val balanceColor = when {
                isStoreFull -> WarningOrange
                isShelfHalted -> SolarAmber
                isPositiveBalance -> SurplusGreen
                else -> HazardRed
            }

            Surface(
                color = balanceColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, balanceColor.copy(alpha = 0.85f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("header_net_resource_balance_badge")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when {
                                isStoreFull ->
                                    "STORE ROOM FULL (${production.storageCap.toInt()} CAP/RES): Auto-resumes at ≤${production.storeRoomResumeThreshold.toInt()} (50%)"
                                isShelfHalted ->
                                    "SHELF CAP (${production.haltedResourcesSummary}): Paused until ≤${production.storeRoomResumeThreshold.toInt()} (50% of ${production.storageCap.toInt()})"
                                isPositiveBalance ->
                                    "MAKING POSITIVE: $netSign${String.format(Locale.US, "%.2f", production.netResourceBalancePerSec)}/s Net (+${String.format(Locale.US, "%.1f", production.totalGrossResourceGainPerSec)} vs -${String.format(Locale.US, "%.1f", production.totalVaultResourceDrainPerSec)}/s)"
                                else ->
                                    "TAKING MORE THAN MAKING: ${String.format(Locale.US, "%.2f", production.netResourceBalancePerSec)}/s Deficit (+${String.format(Locale.US, "%.1f", production.totalGrossResourceGainPerSec)} vs -${String.format(Locale.US, "%.1f", production.totalVaultResourceDrainPerSec)}/s)"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = balanceColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Daily Room Operational Scrap Upkeep: ${production.dailyRoomScrapUpkeep.toInt()} Scrap/day (${(production.roomScrapOperationalMultiplier * 100).roundToInt()}% Room Efficiency) • Water: ${if (production.hasWaterPurifierRoom) "Auto+Manual" else "Manual Only"} • Food: ${if (production.hasHydroponicsRoom) "Auto+Manual" else "Manual Only"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                        Text(
                            text = "${production.vaultCascadeStage.title} • Season: ${production.activeWastelandSeason.title} (Next: ${production.nextWastelandSeason.shortLabel} in ${production.daysRemainingInSeason}d) • Ration Law: ${production.activeRationingPolicy.shortLabel} • Standby Rooms: ${production.mothballedRoomsCount} (-${production.mothballedMwSaved} MW)",
                            style = MaterialTheme.typography.labelSmall,
                            color = SolarAmber,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CAP ${production.storageCap.toInt()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 3. Emergency Blackout / Deficit / Starvation Alert Box (Moved from Top Header)
        if (production.isVaultBlackout || production.isDefenseShutDownByDeficit || production.isWorkersStarvingOrDehydrated) {
            item {
                Surface(
                    color = HazardRed.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.5.dp, HazardRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("alerts_tab_emergency_deficit_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = when {
                                production.isDefenseShutDownByDeficit && production.isVaultBlackout ->
                                    "BLACKOUT (${state.blackoutDaysCount}/6D) & RESOURCE DEFICIT (${state.deficitDaysCount}/6D): ALL ROOMS & DEFENSES SHUT DOWN!"
                                production.isDefenseShutDownByDeficit ->
                                    "RESOURCE DEPLETION / DEFICIT (${state.deficitDaysCount}/6D): VAULT LACKS RESOURCES — DEFENSES SHUT DOWN (6-DAY GAME OVER CLOCK)!"
                                production.isVaultBlackout ->
                                    "POWER BLACKOUT (${state.blackoutDaysCount}/6D): ALL AUTOMATED ROOMS & TURRETS HALTED (6-DAY GAME OVER CLOCK)!"
                                else ->
                                    "DWELLER STARVATION / DEHYDRATION: ALL WORKER PRODUCTION HALTED! FORAGE FOOD OR BOIL WATER!"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = HazardRed,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onForageRations,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SurplusGreen,
                                    contentColor = BunkerCharcoal
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("alerts_tab_forage_food_btn")
                            ) {
                                Text("FORAGE FOOD", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = onBoilWater,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RadTeal,
                                    contentColor = BunkerCharcoal
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("alerts_tab_boil_water_btn")
                            ) {
                                Text("BOIL WATER", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = onScrubAir,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CryoCyan,
                                    contentColor = BunkerCharcoal
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("alerts_tab_scrub_air_btn")
                            ) {
                                Text("SCRUB O2", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 4. All Active Vault Crises & System Alerts (Always Expanded in Alerts Tab)
        item {
            UnifiedVaultAlertHud(
                state = state,
                production = production,
                activeRaid = uiState.activeRaidThreat,
                onNavigateToCrisis = onNavigateToCrisis,
                onQuickRepairSabotage = onQuickRepairSabotage,
                onQuickCrankDynamo = onQuickCrankDynamo,
                onQuickTuneGenerators = onQuickTuneGenerators,
                onQuickStabilizeAll = onQuickStabilizeAll,
                alwaysShowAllInTab = true
            )
        }

        // 5. Latest Action & Dispatch Alerts
        val feedbackItems = listOfNotNull(
            uiState.lastSalvageFeedback,
            uiState.lastDefenseDamageFeedback,
            uiState.lastExpeditionFeedback
        )
        if (feedbackItems.isNotEmpty()) {
            item {
                Surface(
                    color = SteelPanel,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, PanelBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("alerts_tab_recent_feedback_card")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "RECENT DISPATCH & FIELD ALERTS",
                            style = MaterialTheme.typography.labelMedium,
                            color = SolarAmber,
                            fontWeight = FontWeight.ExtraBold
                        )
                        feedbackItems.forEach { msg ->
                            Text(
                                text = "• $msg",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // 6. Vault Event & Alert Log History
        if (uiState.eventLogs.isNotEmpty()) {
            item {
                Text(
                    text = "VAULT ALERT & EVENT LOG FEED",
                    style = MaterialTheme.typography.labelMedium,
                    color = RadTeal,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            items(uiState.eventLogs.take(20), key = { it.id }) { logEntry ->
                Surface(
                    color = ElevatedSlate,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, PanelBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = logEntry.category,
                                style = MaterialTheme.typography.labelMedium,
                                color = SolarAmber,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = logEntry.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}


