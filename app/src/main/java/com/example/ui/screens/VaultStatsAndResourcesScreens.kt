package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.OutpostCalculator
import com.example.model.BuildingType
import com.example.model.HireableCandidate
import com.example.model.PowerStationType
import com.example.model.RareSyndicateItem
import com.example.model.ResourceType
import com.example.model.SkillBranch
import com.example.model.SkillNodeDefinition
import com.example.model.TradeTown
import com.example.model.VaultOperationalPosture
import com.example.model.WastelandExpeditionType
import com.example.model.WorkerClassType
import com.example.notifications.VaultNotificationHelper
import com.example.ui.theme.AetherViolet
import com.example.ui.theme.BioGreen
import com.example.ui.theme.CyanHydro
import com.example.ui.theme.HazardRed
import com.example.ui.theme.RustOrange
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.VaultCardBorder
import com.example.ui.theme.VaultDarkSurface
import com.example.ui.theme.VaultElevatedCard
import com.example.viewmodel.OutpostUiState
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun HardcoreMiningHazardsCard(
    uiState: OutpostUiState,
    onSharpenPickaxe: () -> Unit,
    onCoolMiningShaft: () -> Unit,
    onClearCaveIn: () -> Unit
) {
    val state = uiState.state
    val snap = uiState.production
    val durability = state.pickaxeDurabilityPercent.coerceIn(0, 100)
    val heat = state.miningShaftHeatPercent.coerceIn(0, 100)
    val hasCaveIn = state.activeCaveInHazard
    val sharpenScrapCost = (10.0 + state.salvageRigLevel * 3.0).roundToInt()

    val cardAccent = when {
        hasCaveIn || durability <= 20 || heat >= 80 -> HazardRed
        durability <= 55 || heat >= 50 -> RustOrange
        else -> SolarAmber
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hardcore_mining_hazards_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VaultElevatedCard),
        border = BorderStroke(1.5.dp, cardAccent.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        imageVector = Icons.Default.Gavel,
                        contentDescription = "Hardcore Mining",
                        tint = cardAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "HARDCORE MINING TELEMETRY & HAZARDS",
                            style = MaterialTheme.typography.titleSmall,
                            color = cardAccent,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Swinging Pickaxe wears tip (-3%/swing) & builds shaft heat (+7%/swing). 80%+ Heat risks Cave-Ins!",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = cardAccent.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, cardAccent.copy(alpha = 0.55f))
                ) {
                    Text(
                        text = "YIELD ${(snap.pickaxeDurabilityMultiplier * (if (hasCaveIn) 0.55 else 1.0) * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        color = cardAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = VaultDarkSurface,
                    border = BorderStroke(1.dp, if (durability <= 30) HazardRed else SolarAmber.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "PICKAXE EDGE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$durability%",
                            style = MaterialTheme.typography.titleSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (durability <= 30) HazardRed else BioGreen
                        )
                        LinearProgressIndicator(
                            progress = { durability / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (durability <= 30) HazardRed else BioGreen,
                            trackColor = VaultElevatedCard
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = VaultDarkSurface,
                    border = BorderStroke(1.dp, if (heat >= 75) HazardRed else RustOrange.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "SHAFT HEAT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$heat°C",
                            style = MaterialTheme.typography.titleSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (heat >= 75) HazardRed else CyanHydro
                        )
                        LinearProgressIndicator(
                            progress = { heat / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (heat >= 75) HazardRed else RustOrange,
                            trackColor = VaultElevatedCard
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = VaultDarkSurface,
                    border = BorderStroke(1.dp, if (hasCaveIn) HazardRed else BioGreen.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "TUNNEL ROOF",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (hasCaveIn) "CAVE-IN!" else "STABLE",
                            style = MaterialTheme.typography.titleSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (hasCaveIn) HazardRed else BioGreen
                        )
                        Text(
                            text = if (hasCaveIn) "-45% Vein Yield" else "100% Access",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (hasCaveIn) HazardRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onSharpenPickaxe,
                    enabled = durability < 100 && state.scrap >= sharpenScrapCost,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("sharpen_pickaxe_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = SolarAmber.copy(alpha = 0.2f),
                        contentColor = SolarAmber
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "SHARPEN (-$sharpenScrapCost SCRAP)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                FilledTonalButton(
                    onClick = onCoolMiningShaft,
                    enabled = heat > 0 && state.water >= 8.0,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("cool_mining_shaft_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = CyanHydro.copy(alpha = 0.2f),
                        contentColor = CyanHydro
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "COOL SHAFT (-8 WATER)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            if (hasCaveIn) {
                Button(
                    onClick = onClearCaveIn,
                    enabled = state.scrap >= 14.0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("clear_cave_in_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HazardRed,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = "Clear Cave-In",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SHORE UP CAVE-IN COLLAPSE (-14 SCRAP • RESTORE 100% YIELD)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
fun HardcoreBuildingMaintenanceCard(
    uiState: OutpostUiState,
    onWeldRoomPipes: () -> Unit
) {
    val state = uiState.state
    val snap = uiState.production
    val wear = state.roomStructuralWearPercent.coerceIn(0, 80)
    val pipeLeaks = state.activePipeLeaksCount.coerceAtLeast(0)
    val weldCost = (12.0 + pipeLeaks * 6.0 + wear * 0.25).roundToInt()
    val hasIssues = wear > 0 || pipeLeaks > 0

    val borderAccent = when {
        pipeLeaks > 0 || wear >= 40 -> HazardRed
        wear > 15 -> RustOrange
        else -> CyanHydro
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hardcore_building_maintenance_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VaultElevatedCard),
        border = BorderStroke(1.5.dp, borderAccent.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        imageVector = Icons.Default.Build,
                        contentDescription = "Building Maintenance",
                        tint = borderAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "GRADUAL ROOM SCALING (LV.0 WORST → LV.10+) & PIPE INTEGRITY",
                            style = MaterialTheme.typography.titleSmall,
                            color = borderAccent,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Lv.0 (20% Worst) → Lv.1 (38%) → Lv.2 (56%) → Lv.3 (74%) → Lv.4 (92%) → Lv.5 (110%) +18%/Lv!",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = borderAccent.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, borderAccent.copy(alpha = 0.55f))
                ) {
                    Text(
                        text = "EFF ${(snap.roomWearPenaltyMultiplier * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        color = borderAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = VaultDarkSurface,
                    border = BorderStroke(1.dp, if (wear >= 35) HazardRed else VaultCardBorder)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "ROOM STRUCTURAL WEAR",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$wear% WEAR (-${(wear * 0.5).roundToInt()}% Output)",
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (wear >= 35) HazardRed else BioGreen
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = VaultDarkSurface,
                    border = BorderStroke(1.dp, if (pipeLeaks > 0) HazardRed else VaultCardBorder)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "COOLANT PIPE LEAKS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (pipeLeaks > 0) {
                                "$pipeLeaks LEAKS (-${String.format(Locale.US, "%.2f", snap.pipeLeakWaterDrainPerSec)} H2O/s)"
                            } else {
                                "0 LEAKS (SEALED)"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (pipeLeaks > 0) HazardRed else BioGreen
                        )
                    }
                }
            }

            Button(
                onClick = onWeldRoomPipes,
                enabled = hasIssues && state.scrap >= weldCost,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("weld_room_pipes_btn"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (pipeLeaks > 0) HazardRed else CyanHydro,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = "Weld Pipes",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (hasIssues) {
                        "WELD PIPE LEAKS & REINFORCE ROOMS (-$weldCost SCRAP)"
                    } else {
                        "ALL VAULT ROOM BULKHEADS & PIPES AT 100% INTEGRITY"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun HardcoreHuntingTraumaAndAmmoCard(
    uiState: OutpostUiState,
    onTreatHuntingInjuries: () -> Unit
) {
    val state = uiState.state
    val bleeding = state.bleedingDwellersCount
    val venom = state.venomInfectedDwellersCount
    val hasTrauma = bleeding > 0 || venom > 0 || state.injuredDwellersCount > 0
    val accent = if (hasTrauma) HazardRed else BioGreen

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hardcore_hunting_trauma_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VaultElevatedCard),
        border = BorderStroke(1.5.dp, accent.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = "Hunting Trauma",
                        tint = accent,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "HARDCORE HUNTING: AMMO SCARCITY & BEAST TRAUMA",
                            style = MaterialTheme.typography.titleSmall,
                            color = accent,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Hunts consume Ballistic Ammo for full yield (+100% vs 60% low-ammo) & risk Beast Bleeding/Venom!",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SolarAmber.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, SolarAmber.copy(alpha = 0.55f))
                ) {
                    Text(
                        text = "${state.ammoRounds} AMMO",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        color = SolarAmber,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = VaultDarkSurface,
                    border = BorderStroke(1.dp, if (bleeding > 0) HazardRed else VaultCardBorder)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "BEAST BLEEDING",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$bleeding HUNTERS",
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (bleeding > 0) HazardRed else BioGreen
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = VaultDarkSurface,
                    border = BorderStroke(1.dp, if (venom > 0) AetherViolet else VaultCardBorder)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "RAD-VENOM SPORES",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$venom INFECTED",
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (venom > 0) AetherViolet else BioGreen
                        )
                    }
                }
            }

            if (hasTrauma) {
                Button(
                    onClick = onTreatHuntingInjuries,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("treat_hunting_injuries_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HazardRed,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (state.medkitsCount > 0 || state.radAwayCount > 0) {
                            "SUTURE BLEEDING & PURGE VENOM (-1 MEDKIT/RADAWAY • +25% HEALTH)"
                        } else {
                            "EMERGENCY FIELD SUTURE (-8 WATER & -8 FOOD • +25% HEALTH)"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
fun VaultStatsTabScreen(
    uiState: OutpostUiState,
    onSetPosture: (VaultOperationalPosture) -> Unit,
    onWeldRoomPipes: () -> Unit,
    onTreatHuntingInjuries: () -> Unit,
    onUseMedkit: () -> Unit,
    onUseRadAway: () -> Unit,
    onEmergencyManualCrank: () -> Unit,
    onUnlockOrUpgradeSkill: (SkillNodeDefinition) -> Unit,
    onPerformPrestige: () -> Unit,
    isGoogleSignedIn: Boolean = true,
    onRequestGoogleSignIn: () -> Unit = {},
    onPublishVault: () -> Unit = {}
) {
    val context = LocalContext.current
    val state = uiState.state
    val snap = uiState.production
    var showSkillTreeSection by rememberSaveable { mutableStateOf(false) }
    var notificationStatusText by rememberSaveable {
        mutableStateOf("Auto-arms when game closes: Max Resources, Out of Power & Room Upgrade Done.")
    }

    val schedule = OutpostCalculator.computeOfflineNotificationSchedule(
        state = state,
        production = snap,
        activeConstructionBuildingId = uiState.activeConstruction?.buildingType?.id ?: state.activeConstructionBuildingId,
        activeConstructionTargetLevel = uiState.activeConstruction?.targetLevel ?: state.activeConstructionTargetLevel,
        activeConstructionRemainingSeconds = uiState.activeConstruction?.remainingSeconds ?: state.activeConstructionRemainingSeconds
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("vault_stats_screen_list"),
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = { showSkillTreeSection = false },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("vault_stats_mode_telemetry_btn"),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (!showSkillTreeSection) CyanHydro.copy(alpha = 0.25f) else VaultElevatedCard,
                        contentColor = if (!showSkillTreeSection) CyanHydro else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("VAULT TELEMETRY & ALERTS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                }

                FilledTonalButton(
                    onClick = { showSkillTreeSection = true },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("vault_stats_mode_skills_btn"),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (showSkillTreeSection) AetherViolet.copy(alpha = 0.25f) else VaultElevatedCard,
                        contentColor = if (showSkillTreeSection) AetherViolet else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(Icons.Default.Hub, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "SKILL TREE (${state.availableSkillPoints} SP)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        if (showSkillTreeSection) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VaultDarkSurface)
                ) {
                    Box(modifier = Modifier.height(740.dp)) {
                        SkillTreeTabScreen(
                            uiState = uiState,
                            onUnlockSkill = onUnlockOrUpgradeSkill,
                            onPerformPrestige = onPerformPrestige,
                            isGoogleSignedIn = isGoogleSignedIn,
                            onRequestGoogleSignIn = onRequestGoogleSignIn
                        )
                    }
                }
            }
        } else {
            // 0. Public Game & Optional Google Sign-In Status Card (Required only for Prestige, Cloud Save & Multiplayer)
            item {
                val openDropsCount = uiState.publicSupplyDrops.count { it.status == "open" }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vault_google_cloud_status_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VaultElevatedCard),
                    border = BorderStroke(
                        1.5.dp,
                        if (isGoogleSignedIn) BioGreen.copy(alpha = 0.7f) else SolarAmber.copy(alpha = 0.7f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = "Public Vault Network",
                                tint = if (isGoogleSignedIn) BioGreen else SolarAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (isGoogleSignedIn) {
                                    "PUBLIC VAULT NETWORK LIVE (${uiState.publicVaults.size} PUBLIC VAULTS • $openDropsCount SUPPLY DROPS)"
                                } else {
                                    "LOCAL OFFLINE MODE (SIGN IN FOR PUBLIC MULTIPLAYER, CLOUD SAVE & PRESTIGE)"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isGoogleSignedIn) BioGreen else SolarAmber,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Text(
                            text = if (isGoogleSignedIn) {
                                (uiState.publicNetworkFeedback ?: "").ifBlank {
                                    "Your Vault is live on the Public Wasteland Network! You can Ascend (Prestige), Save Progress to the Cloud, and trade Multiplayer Supply Drops."
                                }
                            } else {
                                "You can play all Mining, Building, Hunting & Survival mechanics offline without Google. Sign in with Google anytime to make your Vault public on the Global Leaderboard, Save to Cloud & Prestige!"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isGoogleSignedIn) {
                                Button(
                                    onClick = onPublishVault,
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .testTag("stats_publish_public_vault_btn"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BioGreen,
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "PUBLISH VAULT PUBLICLY",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            } else {
                                Button(
                                    onClick = onRequestGoogleSignIn,
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .testTag("stats_google_sign_in_btn"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SolarAmber,
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Text(
                                        text = "SIGN IN (PUBLIC & CLOUD)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            FilledTonalButton(
                                onClick = {
                                    val shareText = "Join Vault City (Public Nuclear Survival RPG)! My Vault is on Day ${state.dayCounter} (Command Lv.${state.commandLevel} • ${state.totalSurvivors}/15 Dwellers). Play & join the Public Vault Network: https://ais-pre-w3ronk3kb6rlcg5e54wro5-274786541862.us-east5.run.app"
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "Vault City — Public Survival RPG")
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    runCatching {
                                        context.startActivity(Intent.createChooser(sendIntent, "Share Public Game"))
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("stats_share_public_game_btn"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = CyanHydro.copy(alpha = 0.22f),
                                    contentColor = CyanHydro
                                )
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SHARE PUBLIC GAME",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }

            // 1. Background Phone Notifications Card (Max Resources, Power Outage, Room Upgrade Done)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vault_phone_notifications_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VaultElevatedCard),
                    border = BorderStroke(1.5.dp, CyanHydro.copy(alpha = 0.65f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Phone Alerts",
                                    tint = CyanHydro,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "OFFLINE PHONE NOTIFICATIONS (AUTO-ARMED)",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = CyanHydro,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = notificationStatusText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = VaultDarkSurface,
                                border = BorderStroke(1.dp, BioGreen.copy(alpha = 0.45f))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("ROOM UPGRADE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = schedule.secondsUntilRoomUpgradeComplete?.let { "${it}s ETA" } ?: "IDLE",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = BioGreen
                                    )
                                }
                            }
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = VaultDarkSurface,
                                border = BorderStroke(1.dp, SolarAmber.copy(alpha = 0.45f))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("MAX STORAGE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = schedule.secondsUntilResourcesAtMax?.let {
                                            if (it <= 1L) "FULL NOW" else "${it}s ETA"
                                        } ?: "NET <= 0",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SolarAmber
                                    )
                                }
                            }
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = VaultDarkSurface,
                                border = BorderStroke(1.dp, HazardRed.copy(alpha = 0.45f))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("OUT OF POWER", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = schedule.secondsUntilPowerOutage?.let {
                                            if (it <= 1L) "OUT NOW!" else "${it}s ETA"
                                        } ?: "STABLE",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (schedule.secondsUntilPowerOutage != null) HazardRed else BioGreen
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                VaultNotificationHelper.scheduleOfflineNotifications(
                                    context = context,
                                    state = state,
                                    production = snap,
                                    activeConstruction = uiState.activeConstruction
                                )
                                notificationStatusText = "Armed Offline Phone Alerts! You will be notified when away for Room Done, Max Storage, or Power Outage."
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("arm_offline_notifications_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanHydro,
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SYNC & ARM OFFLINE PHONE NOTIFICATIONS NOW",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // 2. Survival Clocks & Hardcore Vault Status Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vault_survival_clocks_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VaultElevatedCard),
                    border = BorderStroke(1.dp, VaultCardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "HARDCORE SURVIVAL CLOCKS & LIFE-SUPPORT TELEMETRY",
                            style = MaterialTheme.typography.titleSmall,
                            color = SolarAmber,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatTileBox(
                                modifier = Modifier.weight(1f),
                                label = "60S CREW FEEDING",
                                value = "${state.dwellerFeedCycleSecondsRemaining}s",
                                subtitle = "-${snap.fiveMinDwellerFoodCost.roundToInt()} Food / -${snap.fiveMinDwellerWaterCost.roundToInt()} H2O",
                                accent = if (state.dwellerFeedCycleSecondsRemaining <= 12) HazardRed else BioGreen
                            )
                            StatTileBox(
                                modifier = Modifier.weight(1f),
                                label = "6-DAY BLACKOUT",
                                value = "${state.blackoutDaysCount}/6 Days",
                                subtitle = "${state.manualDynamoCranksRemaining}/4 Cranks Left",
                                accent = if (snap.isVaultBlackout) HazardRed else BioGreen
                            )
                            StatTileBox(
                                modifier = Modifier.weight(1f),
                                label = "2-DAY DEFICIT",
                                value = "${state.deficitDaysCount}/2 Days",
                                subtitle = if (snap.isVaultResourceDeficit) "DEFICIT ACTIVE" else "POSITIVE FLOW",
                                accent = if (snap.isVaultResourceDeficit) HazardRed else BioGreen
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatTileBox(
                                modifier = Modifier.weight(1f),
                                label = "OXYGEN & RADS",
                                value = "${state.oxygenPercent}% O2",
                                subtitle = "${state.radiationMillisieverts} mSv Rads",
                                accent = if (state.oxygenPercent < 50 || state.radiationMillisieverts > 150) HazardRed else CyanHydro
                            )
                            StatTileBox(
                                modifier = Modifier.weight(1f),
                                label = "CREW HEALTH",
                                value = "${state.dwellerHealthPercent}% HP",
                                subtitle = "${state.bleedingDwellersCount} Bleed • ${state.venomInfectedDwellersCount} Venom",
                                accent = if (state.dwellerHealthPercent < 60 || state.bleedingDwellersCount > 0 || state.venomInfectedDwellersCount > 0) HazardRed else BioGreen
                            )
                            StatTileBox(
                                modifier = Modifier.weight(1f),
                                label = "10-DAY BOSS SIEGE",
                                value = "${snap.daysUntilDay10Siege}d Left",
                                subtitle = "${snap.totalDefenseRating} / ${snap.todayRequiredDefense} DEF",
                                accent = if (snap.totalDefenseRating < snap.todayRequiredDefense) RustOrange else AetherViolet
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = onUseMedkit,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("stats_use_medkit_btn"),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = BioGreen.copy(alpha = 0.2f),
                                    contentColor = BioGreen
                                )
                            ) {
                                Text("MEDKIT (${state.medkitsCount})", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                            }
                            FilledTonalButton(
                                onClick = onUseRadAway,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("stats_use_radaway_btn"),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = CyanHydro.copy(alpha = 0.2f),
                                    contentColor = CyanHydro
                                )
                            ) {
                                Text("RADAWAY (${state.radAwayCount})", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                            }
                            FilledTonalButton(
                                onClick = onEmergencyManualCrank,
                                enabled = state.manualDynamoCranksRemaining > 0,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("stats_crank_dynamo_btn"),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = SolarAmber.copy(alpha = 0.2f),
                                    contentColor = SolarAmber
                                )
                            ) {
                                Text("CRANK (${state.manualDynamoCranksRemaining}/4)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }

            // 3. Vault Operational Posture Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VaultElevatedCard),
                    border = BorderStroke(1.dp, VaultCardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "VAULT OPERATIONAL POSTURE (${snap.operationalPosture.title.uppercase()})",
                            style = MaterialTheme.typography.titleSmall,
                            color = CyanHydro,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            VaultOperationalPosture.entries.forEach { posture ->
                                val selected = snap.operationalPosture == posture
                                FilledTonalButton(
                                    onClick = { onSetPosture(posture) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .testTag("stats_posture_${posture.id}"),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = if (selected) SolarAmber.copy(alpha = 0.24f) else VaultDarkSurface,
                                        contentColor = if (selected) SolarAmber else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) {
                                    Text(
                                        text = posture.shortLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Hardcore Building Maintenance & Hunting Trauma Summary
            item {
                HardcoreBuildingMaintenanceCard(
                    uiState = uiState,
                    onWeldRoomPipes = onWeldRoomPipes
                )
            }
            item {
                HardcoreHuntingTraumaAndAmmoCard(
                    uiState = uiState,
                    onTreatHuntingInjuries = onTreatHuntingInjuries
                )
            }
        }
    }
}

@Composable
private fun StatTileBox(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    subtitle: String,
    accent: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = VaultDarkSurface,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                color = accent,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CurrentResourcesTabScreen(
    uiState: OutpostUiState,
    onDismantleCache: (Boolean) -> Unit,
    onSmeltGold: () -> Unit,
    onIssueEmergencyRations: () -> Unit,
    onSellResourceToTown: (TradeTown, ResourceType, Double) -> Unit,
    onBuyRareSyndicateItem: (RareSyndicateItem) -> Unit
) {
    val state = uiState.state
    val snap = uiState.production
    val storeRoomLv = uiState.buildingsMap[BuildingType.VAULT_STORE_ROOM.id] ?: 0
    val siloLv = uiState.buildingsMap[BuildingType.REINFORCED_SILO.id] ?: 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("current_resources_screen_list"),
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Positive vs. Deficit Net Resource Balance Ledger Card
        item {
            val isAllPositive = snap.isMakingPositiveResources
            val ledgerAccent = if (isAllPositive) BioGreen else HazardRed
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("resources_net_balance_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VaultElevatedCard),
                border = BorderStroke(1.5.dp, ledgerAccent.copy(alpha = 0.7f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                imageVector = if (isAllPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = "Net Resource Flow",
                                tint = ledgerAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = if (isAllPositive) {
                                        "POSITIVE RESOURCE GENERATION (MAKING MORE THAN TAKING)"
                                    } else {
                                        "RESOURCE DEFICIT WARNING (TAKING MORE THAN MAKING!)"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    color = ledgerAccent,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Includes Gradual Room Output, 60s Crew Feeding, Pipe Leaks (${state.activePipeLeaksCount}) & Reactor Fuel Burn",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    val scrapDrain = snap.roomScrapUpkeepPerSec + snap.pipeLeakScrapDrainPerSec
                    val waterDrain = snap.roomWaterUpkeepPerSec + snap.citizenWaterUpkeepPerSec + snap.pipeLeakWaterDrainPerSec
                    val foodDrain = snap.citizenFoodUpkeepPerSec
                    val fuelDrain = snap.roomFuelUpkeepPerSec + snap.reactorFuelUpkeepPerSec
                    NetResourceLedgerRow(
                        name = "Scrap Metal",
                        current = state.scrap,
                        cap = snap.storageCap,
                        grossGain = snap.scrapPerSec + scrapDrain,
                        drain = scrapDrain,
                        net = snap.scrapPerSec,
                        color = RustOrange
                    )
                    NetResourceLedgerRow(
                        name = "Purified Water",
                        current = state.water,
                        cap = snap.storageCap,
                        grossGain = snap.waterPerSec + waterDrain,
                        drain = waterDrain,
                        net = snap.waterPerSec,
                        color = CyanHydro
                    )
                    NetResourceLedgerRow(
                        name = "Vault Food Rations",
                        current = state.rations,
                        cap = snap.storageCap,
                        grossGain = snap.rationsPerSec + foodDrain,
                        drain = foodDrain,
                        net = snap.rationsPerSec,
                        color = BioGreen
                    )
                    NetResourceLedgerRow(
                        name = "Refined Bio-Fuel",
                        current = state.bioFuel,
                        cap = snap.storageCap,
                        grossGain = snap.bioFuelPerSec + fuelDrain,
                        drain = fuelDrain,
                        net = snap.bioFuelPerSec,
                        color = SolarAmber
                    )
                    NetResourceLedgerRow(
                        name = "Research Data",
                        current = state.researchData,
                        cap = snap.storageCap,
                        grossGain = snap.researchPerSec,
                        drain = 0.0,
                        net = snap.researchPerSec,
                        color = AetherViolet
                    )
                }
            }
        }

        // 2. Vault Storage Capacity & Rare Strategic Stockpiles Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("resources_storage_and_rare_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VaultElevatedCard),
                border = BorderStroke(1.dp, VaultCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = "Storage Cap",
                                tint = SolarAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "VAULT STORAGE CAPACITY: ${snap.storageCap.toInt()} MAX PER RESOURCE",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = SolarAmber,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Vault Store Room Lv.$storeRoomLv (+420/Lv) • Medbay & Silo Lv.$siloLv (+220/Lv)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatTileBox(
                            modifier = Modifier.weight(1f),
                            label = "GOLD BULLION",
                            value = "${state.gold.toInt()}",
                            subtitle = "Trade Currency",
                            accent = SolarAmber
                        )
                        StatTileBox(
                            modifier = Modifier.weight(1f),
                            label = "THORIUM RODS",
                            value = "${state.thoriumRods.toInt()}",
                            subtitle = "Fission Fuel",
                            accent = BioGreen
                        )
                        StatTileBox(
                            modifier = Modifier.weight(1f),
                            label = "AETHER & CORES",
                            value = "${state.aetherCells.toInt()} / ${state.mutagenCores.toInt()}",
                            subtitle = "Aether / Mutagen",
                            accent = AetherViolet
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { onDismantleCache(true) },
                            enabled = state.rawSalvageCaches > 0,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("resources_crush_caches_btn"),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = RustOrange.copy(alpha = 0.2f),
                                contentColor = RustOrange
                            )
                        ) {
                            Text(
                                text = "CRUSH CACHES (${state.rawSalvageCaches})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        FilledTonalButton(
                            onClick = onSmeltGold,
                            enabled = state.scrap >= 18.0 && state.water >= 10.0 && state.bioFuel >= 6.0,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("resources_smelt_gold_btn"),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = SolarAmber.copy(alpha = 0.2f),
                                contentColor = SolarAmber
                            )
                        ) {
                            Text(
                                text = "SMELT GOLD (-18S/-10W/-6F)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Button(
                        onClick = onIssueEmergencyRations,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("resources_emergency_rations_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BioGreen,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = "EMERGENCY RELIEF DROP (+14 FOOD & +14 WATER FOR CREW)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // 3. Quick Wasteland Town Resource Exchange & Syndicate Rare Store
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("resources_quick_trade_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VaultElevatedCard),
                border = BorderStroke(1.dp, VaultCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "CARAVAN COMMODITY EXCHANGE (SELL SURPLUS FOR GOLD)",
                        style = MaterialTheme.typography.titleSmall,
                        color = SolarAmber,
                        fontWeight = FontWeight.ExtraBold
                    )
                    val town = TradeTown.entries.first()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(ResourceType.SCRAP, ResourceType.WATER, ResourceType.RATIONS, ResourceType.BIO_FUEL).forEach { resType ->
                            val quote = OutpostCalculator.computeTownSellQuote(
                                town = town,
                                resourceType = resType,
                                batchAmount = 25.0,
                                activePhase = snap.activePhase,
                                skillsMap = uiState.skillsMap
                            )
                            val stock = when (resType) {
                                ResourceType.SCRAP -> state.scrap
                                ResourceType.WATER -> state.water
                                ResourceType.RATIONS -> state.rations
                                ResourceType.BIO_FUEL -> state.bioFuel
                                else -> 0.0
                            }
                            FilledTonalButton(
                                onClick = { onSellResourceToTown(town, resType, 25.0) },
                                enabled = stock >= 25.0,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("sell_25_${resType.name.lowercase()}"),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "-25 ${resType.shortLabel}\n+${quote.toInt()}G",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = VaultCardBorder)
                    Text(
                        text = "SYNDICATE RARE RELICS (BUY WITH GOLD)",
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherViolet,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RareSyndicateItem.entries.forEach { item ->
                            val price = OutpostCalculator.computeRareItemGoldPrice(item, uiState.skillsMap)
                            FilledTonalButton(
                                onClick = { onBuyRareSyndicateItem(item) },
                                enabled = state.gold >= price,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("resources_buy_rare_${item.id}"),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "+${item.batchSize.toInt()} ${item.resourceType.shortLabel}\n-${price.toInt()}G",
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
}

@Composable
private fun NetResourceLedgerRow(
    name: String,
    current: Double,
    cap: Double,
    grossGain: Double,
    drain: Double,
    net: Double,
    color: Color
) {
    val isPositive = net >= 0.0
    val netColor = if (isPositive) BioGreen else HazardRed
    val sign = if (isPositive) "+" else "-"
    val fillFraction = (current / cap.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = VaultDarkSurface,
        border = BorderStroke(1.dp, if (isPositive) color.copy(alpha = 0.35f) else HazardRed.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$name (${current.toInt()} / ${cap.toInt()})",
                    style = MaterialTheme.typography.labelMedium,
                    color = color,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "$sign${String.format(Locale.US, "%.2f", abs(net))}/s (${if (isPositive) "POSITIVE" else "DEFICIT"})",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace,
                    color = netColor,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            LinearProgressIndicator(
                progress = { fillFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = VaultElevatedCard
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Gross Making: +${String.format(Locale.US, "%.2f", grossGain)}/s",
                    style = MaterialTheme.typography.labelSmall,
                    color = BioGreen
                )
                Text(
                    text = "Total Taking: -${String.format(Locale.US, "%.2f", drain)}/s",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (drain > 0.0) RustOrange else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CrewAndPowerTabScreen(
    uiState: OutpostUiState,
    onAssignWorker: (WorkerClassType, Int) -> Unit,
    onHireCandidate: (HireableCandidate) -> Unit,
    onRefreshBoard: () -> Unit,
    onSendExpedition: (WastelandExpeditionType) -> Unit,
    onToggleAutoShift: (Boolean) -> Unit,
    onOptimizeCrewNow: () -> Unit,
    onSellToTown: (TradeTown, ResourceType) -> Unit,
    onBuyRareItem: (RareSyndicateItem) -> Unit,
    onDispatchConvoy: (TradeTown) -> Unit,
    onUpgradeConvoy: () -> Unit,
    onUpgradePowerStation: (PowerStationType) -> Unit,
    onInjectThoriumOverdrive: () -> Unit,
    onTriggerSolarFlare: () -> Unit,
    onTriggerEclipseShroud: () -> Unit,
    isAuthenticated: Boolean,
    userEmail: String?,
    authErrorMessage: String?,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onPublishVault: () -> Unit,
    onBroadcastSupplyDrop: () -> Unit,
    onClaimSupplyDrop: (com.example.data.remote.PublicSupplyDrop) -> Unit
) {
    var showPowerGridSubTab by rememberSaveable { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = { showPowerGridSubTab = false },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("crew_power_subtab_crew"),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (!showPowerGridSubTab) BioGreen.copy(alpha = 0.24f) else VaultElevatedCard,
                    contentColor = if (!showPowerGridSubTab) BioGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "DWELLERS (${uiState.state.totalSurvivors}/15)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            FilledTonalButton(
                onClick = { showPowerGridSubTab = true },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("crew_power_subtab_power"),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (showPowerGridSubTab) SolarAmber.copy(alpha = 0.24f) else VaultElevatedCard,
                    contentColor = if (showPowerGridSubTab) SolarAmber else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(Icons.Default.ElectricBolt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "POWER & PUBLIC NET (${uiState.production.totalMegawattGenerated.roundToInt()}/${uiState.production.totalMegawattDemand.roundToInt()} MW)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            if (!showPowerGridSubTab) {
                SurvivorsTabScreen(
                    uiState = uiState,
                    onHireCandidate = onHireCandidate,
                    onRefreshBoard = onRefreshBoard,
                    onSendExpedition = onSendExpedition,
                    onAdjustWorker = onAssignWorker,
                    onToggleAutoShift = onToggleAutoShift,
                    onOptimizeCrew = onOptimizeCrewNow
                )
            } else {
                TradePowerTabScreen(
                    uiState = uiState,
                    onSellToTown = onSellToTown,
                    onBuyRareItem = onBuyRareItem,
                    onDispatchConvoy = onDispatchConvoy,
                    onUpgradeConvoy = onUpgradeConvoy,
                    onUpgradePowerStation = onUpgradePowerStation,
                    onInjectThoriumOverdrive = onInjectThoriumOverdrive,
                    onTriggerSolarFlare = onTriggerSolarFlare,
                    onTriggerEclipseShroud = onTriggerEclipseShroud,
                    onPublishVault = onPublishVault,
                    onBroadcastSupplyDrop = onBroadcastSupplyDrop,
                    onClaimSupplyDrop = onClaimSupplyDrop,
                    isGoogleSignedIn = isAuthenticated,
                    userEmail = userEmail,
                    authErrorMessage = authErrorMessage,
                    onRequestGoogleSignIn = onSignInClick,
                    onSignOutGoogle = onSignOutClick
                )
            }
        }
    }
}
