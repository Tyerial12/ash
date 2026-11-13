package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.OutpostStateEntity
import com.example.data.local.WorkerClassEntity
import com.example.domain.OutpostCalculator
import com.example.model.ActiveDwellerExpedition
import com.example.model.CaravanCandidateGenerator
import com.example.model.HireableCandidate
import com.example.model.SpecialStat
import com.example.model.WastelandExpeditionType
import com.example.model.WorkerClassType
import com.example.model.computeSpecialProfile
import com.example.model.primarySpecialStat
import com.example.ui.theme.BioluminescentViolet
import com.example.ui.theme.BunkerCharcoal
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.HazardRed
import com.example.ui.theme.PanelBorder
import com.example.ui.theme.RadTeal
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.SteelPanel
import com.example.ui.theme.SurplusGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningOrange
import com.example.ui.theme.XpGold
import com.example.viewmodel.OutpostUiState
import java.util.Locale
import kotlin.math.roundToInt

private enum class CrewSubView(val label: String) {
    ALL("All"),
    CREW_AND_HIRE("Crew & Hire"),
    EXPEDITIONS("Expeditions")
}

@Composable
fun SurvivorsTabScreen(
    uiState: OutpostUiState,
    onHireCandidate: (HireableCandidate) -> Unit,
    onRefreshBoard: () -> Unit,
    onSendExpedition: (WastelandExpeditionType) -> Unit,
    onAdjustWorker: (WorkerClassType, Int) -> Unit,
    onToggleAutoShift: (Boolean) -> Unit,
    onOptimizeCrew: () -> Unit,
    onLaunchRescueBeacon: () -> Unit = {},
    onIssueEmergencyRations: () -> Unit = {},
    onToggleRestRotation: () -> Unit = {},
    onCureSickAndExhaustedCrew: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state = uiState.state
    val prod = uiState.production
    val idleSurvivors = (state.totalSurvivors - prod.totalAssignedWorkers).coerceAtLeast(0)
    val hasHousingSpace = state.totalSurvivors < prod.maxWorkerCapacity
    var subView by rememberSaveable { mutableStateOf(CrewSubView.ALL) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "crew_subview_switcher") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CrewSubView.entries.forEach { tab ->
                    val selected = subView == tab
                    FilterChip(
                        selected = selected,
                        onClick = { subView = tab },
                        label = {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SolarAmber.copy(alpha = 0.22f),
                            selectedLabelColor = SolarAmber,
                            containerColor = SteelPanel,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = PanelBorder,
                            selectedBorderColor = SolarAmber
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 1. Vault City Dweller Census & Daily Food/Water Upkeep Card
        if (subView == CrewSubView.ALL || subView == CrewSubView.CREW_AND_HIRE) {
            item(key = "workforce_overview") {
                Surface(
                    color = SteelPanel,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, SolarAmber.copy(alpha = 0.7f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ARES OUTPOST SURVIVOR CENSUS & DAILY UPKEEP",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = SolarAmber,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Active Survivors: ${state.totalSurvivors} / ${prod.maxWorkerCapacity} Bunk Cap (15 Max Outpost Cap) • Assigned: ${prod.totalAssignedWorkers} • Injured: ${state.injuredDwellersCount} • Lost: ${state.totalDwellersLost}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = String.format(
                                        Locale.US,
                                        "Each New Day Consumption: -%.0f Rations & -%.0f Water (-2 Rations & -2 Water per Survivor each Day!)",
                                        prod.dailyFoodUpkeepPerDay,
                                        prod.dailyWaterUpkeepPerDay
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (prod.citizenMoralePercent >= 100) SurplusGreen else WarningOrange
                                )
                                Text(
                                    text = "★ Specialist Leaders (${CaravanCandidateGenerator.UNIQUE_DWELLER_CHANCE_PERCENT}% Rare Chance): ${state.uniqueDwellersCount} (+${state.uniqueDwellersCount * 40}% All-Resource Output)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BioluminescentViolet,
                                    fontWeight = FontWeight.Bold
                                )
                                if (state.totalSurvivors == 0) {
                                    Text(
                                        text = "You arrived ALONE at the fortified perimeter of Ares Outpost (0 Survivors)! Build Sector #1 (Survivor Bunkhouse) in Quarry & Base, mine Gold Ore in the quarry, and recruit Survivors below or fire the Rescue Beacon.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = XpGold
                                    )
                                } else if (state.totalSurvivors >= OutpostCalculator.MAX_VAULT_DWELLERS) {
                                    Text(
                                        text = "Maximum Ares Outpost Survivor limit reached (${state.totalSurvivors}/${OutpostCalculator.MAX_VAULT_DWELLERS} Max Outpost Cap)!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SolarAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else if (!hasHousingSpace) {
                                    Text(
                                        text = "Survivor Bunkhouse full (${state.totalSurvivors}/${prod.maxWorkerCapacity} • ${OutpostCalculator.MAX_VAULT_DWELLERS} Max Outpost Cap)! Upgrade Bunkhouse in Quarry & Base to house more Survivors.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = HazardRed
                                    )
                                }
                            }

                            if (state.totalSurvivors > 0) {
                                OutlinedButton(
                                    onClick = onOptimizeCrew,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("optimize_crew_button")
                                ) {
                                    Text(
                                        text = "AUTO-ASSIGN",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = RadTeal,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Rescue Beacon & Feed Crew Row
                        val canLaunchBeacon = hasHousingSpace &&
                            state.scrap >= 35.0 &&
                            state.rations >= 15.0 &&
                            state.researchData >= 6.0
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onLaunchRescueBeacon,
                                enabled = canLaunchBeacon,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RadTeal,
                                    contentColor = BunkerCharcoal,
                                    disabledContainerColor = BunkerCharcoal,
                                    disabledContentColor = TextMuted
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("launch_rescue_beacon_btn")
                            ) {
                                Text(
                                    text = if (!hasHousingSpace) "NEED QUARTERS LV.1" else "RESCUE BEACON (-35S/-15F/-6T)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = onIssueEmergencyRations,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SurplusGreen,
                                    contentColor = BunkerCharcoal
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("crew_emergency_rations_btn")
                            ) {
                                Text(
                                    text = "FEED CREW (+14F/+14W)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Hardcore Sleep Stamina & Quarantine Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onToggleRestRotation,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("survivors_sleep_shift_btn")
                            ) {
                                Text(
                                    text = if (state.isRestRotationActive) {
                                        "SLEEP ON (${state.dwellerStaminaPercent}% STAM)"
                                    } else {
                                        "SLEEP SHIFT (${state.dwellerStaminaPercent}% STAM)"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (state.dwellerStaminaPercent < 35) HazardRed else RadTeal,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = onCureSickAndExhaustedCrew,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (state.sickDwellersCount > 0 || state.dwellerStaminaPercent < 35) HazardRed else SolarAmber,
                                    contentColor = if (state.sickDwellersCount > 0 || state.dwellerStaminaPercent < 35) TextPrimary else BunkerCharcoal
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("survivors_cure_sick_btn")
                            ) {
                                Text(
                                    text = "CURE SICK (${state.sickDwellersCount} SICK)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Circadian Auto-Shift Toggle Row
                        Surface(
                            color = ElevatedSlate,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, PanelBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoMode,
                                        contentDescription = "Circadian Auto-Shift",
                                        tint = RadTeal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Outpost Auto-Shift Protocol",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Automatically rotates Survivors between Day Mining & Night Recon shifts.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                                Switch(
                                    checked = state.autoShiftEnabled,
                                    onCheckedChange = onToggleAutoShift,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = BunkerCharcoal,
                                        checkedTrackColor = RadTeal
                                    ),
                                    modifier = Modifier.testTag("auto_shift_switch")
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Send Hired Dwellers on Wasteland Expeditions (Gas Masks, Hazmat Suits & Ammo Burn!)
        if (subView == CrewSubView.ALL || subView == CrewSubView.EXPEDITIONS) {
            item(key = "dweller_expeditions_card") {
                DwellerExpeditionsCard(
                    state = state,
                    buildingsMap = uiState.buildingsMap,
                    totalSurvivors = state.totalSurvivors,
                    totalDwellersLost = state.totalDwellersLost,
                    totalExpeditionsCompleted = state.totalExpeditionsCompleted,
                    activeExpedition = uiState.activeExpedition,
                    lastExpeditionFeedback = uiState.lastExpeditionFeedback,
                    onSendExpedition = onSendExpedition
                )
            }
        }

        // 3. Hire Survivors with Gold (Drifter Recruitment Board)
        if (subView == CrewSubView.ALL || subView == CrewSubView.CREW_AND_HIRE) {
            item(key = "caravan_contract_board_item") {
            Surface(
                color = SteelPanel,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, XpGold.copy(alpha = 0.75f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("caravan_contract_board")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "HIRE SURVIVORS WITH GOLD (8% RARE UNIQUE SURVIVOR CHANCE)",
                                style = MaterialTheme.typography.titleSmall,
                                color = XpGold,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Hire wasteland survivors with Gold! Rare 8% chance to gain a Unique Specialist Survivor with +40% higher resource-gathering ability across the Outpost.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        OutlinedButton(
                            onClick = onRefreshBoard,
                            enabled = state.scrap >= 6.0,
                            border = BorderStroke(1.dp, RadTeal),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("refresh_caravan_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Survivors",
                                tint = RadTeal,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SCOUT (6S)",
                                style = MaterialTheme.typography.labelSmall,
                                color = RadTeal,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.hireableCandidates.forEachIndexed { index, candidate ->
                            val canAffordCandidate = hasHousingSpace &&
                                state.gold >= candidate.goldSigningFee

                            Surface(
                                color = if (candidate.isUniqueDweller) BioluminescentViolet.copy(alpha = 0.14f) else ElevatedSlate,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    width = if (candidate.isUniqueDweller) 1.5.dp else 1.dp,
                                    color = when {
                                        candidate.isUniqueDweller -> BioluminescentViolet
                                        canAffordCandidate -> candidate.specialtyClass.accentColor
                                        else -> PanelBorder
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "${candidate.name} \"${candidate.callsign}\"",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = if (candidate.isUniqueDweller) XpGold else TextPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Surface(
                                                color = candidate.specialtyClass.accentColor.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(5.dp)
                                            ) {
                                                Text(
                                                    text = candidate.specialtyClass.displayName,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = candidate.specialtyClass.accentColor,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            if (candidate.isUniqueDweller) {
                                                Surface(
                                                    color = BioluminescentViolet.copy(alpha = 0.25f),
                                                    shape = RoundedCornerShape(5.dp),
                                                    border = BorderStroke(1.dp, BioluminescentViolet)
                                                ) {
                                                    Text(
                                                        text = "★ UNIQUE (8% RARE • +40% YIELD)",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = BioluminescentViolet,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        val candProfile = candidate.specialtyClass.computeSpecialProfile(
                                            classLevel = 1,
                                            isUniqueDweller = candidate.isUniqueDweller
                                        )
                                        val primaryStat = candidate.specialtyClass.primarySpecialStat
                                        Text(
                                            text = "S.P.E.C.I.A.L. [${primaryStat.code}]: ${candProfile.compactSummary()}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = primaryStat.accentColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = candidate.traitSummary,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (candidate.isUniqueDweller) TextPrimary else TextSecondary
                                        )
                                        Text(
                                            text = "Hire Cost: ${candidate.goldSigningFee.toInt()} GOLD • Daily Upkeep: -2 Food & -2 Water/Day",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = XpGold,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = { onHireCandidate(candidate) },
                                        enabled = canAffordCandidate,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = XpGold,
                                            contentColor = BunkerCharcoal,
                                            disabledContainerColor = BunkerCharcoal,
                                            disabledContentColor = TextMuted
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .height(48.dp)
                                            .testTag(if (index == 0) "recruit_survivor_button" else "hire_candidate_$index")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.GroupAdd,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (!hasHousingSpace) "BUILD QUARTERS" else "HIRE (${candidate.goldSigningFee.toInt()}G)",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
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

        // 4. Vault Dweller Role Assignments
        if (subView == CrewSubView.ALL || subView == CrewSubView.CREW_AND_HIRE) {
            items(WorkerClassType.entries, key = { it.id }) { workerType ->
                val entity = uiState.workersMap[workerType.id]
                    ?: WorkerClassEntity(workerType.id, 0, 1, 0.0)
                val isFavoredNow = workerType.favoredPhases.contains(prod.activePhase)

                SurvivorClassCard(
                    workerType = workerType,
                    entity = entity,
                    idleSurvivors = idleSurvivors,
                    isFavoredPhaseActive = isFavoredNow,
                    onIncrement = { onAdjustWorker(workerType, +1) },
                    onDecrement = { onAdjustWorker(workerType, -1) }
                )
            }
        }
    }
}

@Composable
private fun DwellerExpeditionsCard(
    state: OutpostStateEntity,
    buildingsMap: Map<String, Int>,
    totalSurvivors: Int,
    totalDwellersLost: Int,
    totalExpeditionsCompleted: Int,
    activeExpedition: ActiveDwellerExpedition?,
    lastExpeditionFeedback: String?,
    onSendExpedition: (WastelandExpeditionType) -> Unit
) {
    val sampleRisk = OutpostCalculator.computeEffectiveExpeditionDeathChance(
        WastelandExpeditionType.entries.first(),
        buildingsMap,
        state
    )
    Surface(
        color = SteelPanel,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, WarningOrange.copy(alpha = 0.75f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dweller_expeditions_card")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = "Wasteland Expeditions",
                        tint = WarningOrange,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = "SURFACE SURVIVOR EXPEDITIONS ($sampleRisk% EFFECTIVE RISK)",
                            style = MaterialTheme.typography.titleSmall,
                            color = WarningOrange,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Send a Survivor on surface expeditions to scavenge Survivor Armor (+12 DEF) & Weapons (+15 DEF) for permanent extra Outpost Defense!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                Surface(
                    color = HazardRed.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, HazardRed)
                ) {
                    Text(
                        text = "$sampleRisk% FATALITY",
                        style = MaterialTheme.typography.labelSmall,
                        color = HazardRed,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            val totalGearDefBonus = OutpostCalculator.computeDwellerGearDefenseBonus(state)
            Surface(
                color = RadTeal.copy(alpha = 0.14f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, RadTeal.copy(alpha = 0.65f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🛡 Survivor Armor: ${state.dwellerArmorCount} (+${state.dwellerArmorCount * OutpostCalculator.DWELLER_ARMOR_DEFENSE_BONUS} DEF) • 🔫 Survivor Weapons: ${state.dwellerWeaponsCount} (+${state.dwellerWeaponsCount * OutpostCalculator.DWELLER_WEAPON_DEFENSE_BONUS} DEF) • Total Extra DEF: +$totalGearDefBonus",
                    style = MaterialTheme.typography.labelSmall,
                    color = RadTeal,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            Text(
                text = "Expedition Gear Stock: Gas Masks: ${state.gasMasksCount} • Hazmat Suits: ${state.hazmatSuitsCount} • Ammo: ${state.ammoRounds} rds • Survived: $totalExpeditionsCompleted • Lost: $totalDwellersLost",
                style = MaterialTheme.typography.labelSmall,
                color = XpGold,
                fontWeight = FontWeight.Bold
            )

            if (lastExpeditionFeedback != null) {
                val isDeath = lastExpeditionFeedback.contains("KILLED", ignoreCase = true)
                Surface(
                    color = (if (isDeath) HazardRed else SurplusGreen).copy(alpha = 0.16f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isDeath) HazardRed else SurplusGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = lastExpeditionFeedback,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDeath) HazardRed else SurplusGreen,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            if (activeExpedition != null) {
                val progress = 1f - (activeExpedition.remainingSeconds.toFloat() / activeExpedition.totalSeconds.coerceAtLeast(1))
                Surface(
                    color = ElevatedSlate,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, activeExpedition.type.accentColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "ACTIVE: ${activeExpedition.type.title.uppercase()}",
                                style = MaterialTheme.typography.labelMedium,
                                color = activeExpedition.type.accentColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${activeExpedition.remainingSeconds}s remaining ($sampleRisk% Death Risk)",
                                style = MaterialTheme.typography.labelSmall,
                                color = WarningOrange,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = activeExpedition.type.accentColor,
                            trackColor = BunkerCharcoal
                        )
                    }
                }
            }

            WastelandExpeditionType.entries.forEach { exp ->
                val canDispatch = totalSurvivors > 0 && activeExpedition == null
                val effectiveDeathRisk = OutpostCalculator.computeEffectiveExpeditionDeathChance(exp, buildingsMap, state)
                Surface(
                    color = ElevatedSlate,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, exp.accentColor.copy(alpha = 0.65f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${exp.title} (${exp.durationSeconds}s • $effectiveDeathRisk% Death Chance)",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = exp.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            val expDefBonus = exp.armorReward * OutpostCalculator.DWELLER_ARMOR_DEFENSE_BONUS +
                                exp.weaponsReward * OutpostCalculator.DWELLER_WEAPON_DEFENSE_BONUS
                            Text(
                                text = "Combat Gear Haul: +${exp.armorReward} Armor & +${exp.weaponsReward} Weapon(s) (+$expDefBonus Extra DEF) • +${exp.foodReward.toInt()}F • +${exp.waterReward.toInt()}W • +${exp.scrapReward.toInt()}S • +${exp.goldReward.toInt()}G",
                                style = MaterialTheme.typography.labelSmall,
                                color = XpGold
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { onSendExpedition(exp) },
                            enabled = canDispatch,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = exp.accentColor,
                                contentColor = BunkerCharcoal,
                                disabledContainerColor = BunkerCharcoal,
                                disabledContentColor = TextMuted
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("send_expedition_${exp.id.lowercase()}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (totalSurvivors == 0) "NEED SURVIVOR" else "SEND ($effectiveDeathRisk% RISK)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SurvivorClassCard(
    workerType: WorkerClassType,
    entity: WorkerClassEntity,
    idleSurvivors: Int,
    isFavoredPhaseActive: Boolean,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val icon: ImageVector = when (workerType) {
        WorkerClassType.SCRAPPER -> Icons.Default.Engineering
        WorkerClassType.HYDRO_ENGINEER -> Icons.Default.WaterDrop
        WorkerClassType.NIGHT_STALKER -> Icons.Default.Nightlight
        WorkerClassType.TECH_SAVANT -> Icons.Default.Memory
    }

    val nextLevelXp = WorkerClassType.xpForNextClassLevel(entity.classLevel)
    val xpFraction = (entity.classXp / nextLevelXp.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f)

    Surface(
        color = SteelPanel,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isFavoredPhaseActive) workerType.accentColor.copy(alpha = 0.75f) else PanelBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("worker_card_${workerType.id.lowercase()}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(workerType.accentColor.copy(alpha = 0.18f))
                            .border(1.dp, workerType.accentColor, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = workerType.displayName,
                            tint = workerType.accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = workerType.displayName,
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = workerType.accentColor.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, workerType.accentColor)
                            ) {
                                Text(
                                    text = "CLASS LV.${entity.classLevel}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = workerType.accentColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isFavoredPhaseActive) "FAVORED SHIFT ACTIVE (+50% CLASS XP)"
                            else "Standard Shift Conditions",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isFavoredPhaseActive) SurplusGreen else TextSecondary
                        )
                    }
                }

                // Shift Assignment Stepper (- / Count / +)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilledIconButton(
                        onClick = onDecrement,
                        enabled = entity.assignedCount > 0,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = ElevatedSlate,
                            contentColor = TextPrimary,
                            disabledContainerColor = ElevatedSlate.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("unassign_${workerType.id.lowercase()}")
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Unassign ${workerType.displayName}")
                    }

                    Text(
                        text = "${entity.assignedCount}",
                        style = MaterialTheme.typography.titleMedium,
                        color = workerType.accentColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    FilledIconButton(
                        onClick = onIncrement,
                        enabled = idleSurvivors > 0,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = workerType.accentColor,
                            contentColor = BunkerCharcoal,
                            disabledContainerColor = ElevatedSlate.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("assign_${workerType.id.lowercase()}")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Assign ${workerType.displayName}")
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            val specialProfile = workerType.computeSpecialProfile(entity.classLevel, isUniqueDweller = false)
            val primaryStat = workerType.primarySpecialStat
            Surface(
                color = BunkerCharcoal,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, primaryStat.accentColor.copy(alpha = 0.55f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "FALLOUT S.P.E.C.I.A.L. [${primaryStat.code} - ${primaryStat.fullName.uppercase()}]: ${primaryStat.roomRoleSummary}",
                        style = MaterialTheme.typography.labelSmall,
                        color = primaryStat.accentColor,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SpecialStat.entries.forEach { stat ->
                            val value = specialProfile.statFor(stat)
                            val isPrimary = stat == primaryStat
                            Surface(
                                color = if (isPrimary) stat.accentColor.copy(alpha = 0.25f) else ElevatedSlate,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isPrimary) stat.accentColor else PanelBorder
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 3.dp)
                                ) {
                                    Text(
                                        text = stat.code,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isPrimary) stat.accentColor else TextSecondary,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "$value",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = workerType.roleSummary,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = ElevatedSlate,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, PanelBorder)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Class Mastery XP: ${entity.classXp.roundToInt()} / ${nextLevelXp.roundToInt()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = XpGold
                        )
                        Text(
                            text = "+${((entity.classLevel - 1) * 14)}% Class Bonus",
                            style = MaterialTheme.typography.labelSmall,
                            color = SurplusGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { xpFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = workerType.accentColor,
                        trackColor = BunkerCharcoal
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "VETERAN CLASS PERKS",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                workerType.perks.forEach { perk ->
                    val unlocked = entity.classLevel >= perk.requiredLevel
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = if (unlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (unlocked) SurplusGreen else TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Lv.${perk.requiredLevel} ${perk.title}: ",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (unlocked) workerType.accentColor else TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = perk.description,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (unlocked) TextPrimary else TextMuted
                        )
                    }
                }
            }
        }
    }
}
