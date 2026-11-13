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
import com.example.model.GridLoadBreakerMode
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
                            text = "MANUAL MINING TELEMETRY (0 SCRAP COST • 0 SWING HEAT)",
                            style = MaterialTheme.typography.titleSmall,
                            color = cardAccent,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Swinging Pickaxe causes 0 Heat! Manual mining, Pickaxe sharpening & Cave-In clearing cost 0 Scrap.",
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
                    enabled = durability < 100,
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
                        text = if (durability < 100) "SHARPEN (FREE • 0 SCRAP)" else "PICKAXE SHARP (100%)",
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
                    enabled = true,
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
                        text = "CLEAR CAVE-IN COLLAPSE (FREE • 0 SCRAP • RESTORE 100% YIELD)",
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
                            text = "DAILY ROOM-LEVEL SCRAP UPKEEP & AUTO-REPAIR",
                            style = MaterialTheme.typography.titleSmall,
                            color = borderAccent,
                            fontWeight = FontWeight.ExtraBold
                        )
                        val waterFoodGateNote = when {
                            !snap.hasWaterPurifierRoom && !snap.hasHydroponicsRoom ->
                                " • Water & Food: MANUAL ONLY (Build Water Purifier & Hydroponics for auto production)"
                            !snap.hasWaterPurifierRoom ->
                                " • Water: MANUAL ONLY (Build Water Purifier Room for auto Water)"
                            !snap.hasHydroponicsRoom ->
                                " • Food: MANUAL ONLY (Build Hydroponics Room for auto Food)"
                            else -> ""
                        }
                        Text(
                            text = if (snap.isRoomsLackingScrap) {
                                "LOW SCRAP: Rooms running at ${(snap.roomScrapOperationalMultiplier * 100).roundToInt()}% output! Needs ${snap.dailyAutoRepairScrapCost.toInt()} Scrap/day (Room Lv.${uiState.buildingsMap.values.sum()}) to stay 100% operational$waterFoodGateNote"
                            } else if (snap.needsAutomatedSystemRepair) {
                                "Requires ${snap.dailyAutoRepairScrapCost.toInt()} Scrap/day (scaled by Room Lv.${uiState.buildingsMap.values.sum()}; each level takes more Scrap/day) to keep rooms 100% operational & auto-repair!$waterFoodGateNote"
                            } else {
                                "No Rooms Built Yet (0 Scrap/day). Each built room level takes more Scrap per day to stay fully operational.$waterFoodGateNote"
                            },
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
                        text = "EFF ${(snap.roomWearPenaltyMultiplier * snap.roomScrapOperationalMultiplier * 100).roundToInt()}%",
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
                        "WELD PIPE LEAKS & REINFORCE SECTORS (-$weldCost SCRAP)"
                    } else {
                        "ALL OUTPOST SECTOR BULKHEADS & PIPES AT 100% INTEGRITY"
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
    onTreatHuntingInjuries: () -> Unit,
    onFieldPressAmmo: () -> Unit = {}
) {
    val state = uiState.state
    val bleeding = state.bleedingDwellersCount
    val venom = state.venomInfectedDwellersCount
    val hasTrauma = bleeding > 0 || venom > 0 || state.injuredDwellersCount > 0
    val accent = if (hasTrauma) HazardRed else BioGreen
    val canPressAmmo = (state.scrap >= 10.0 && state.bioFuel >= 2.0) || state.scrap >= 14.0

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onFieldPressAmmo,
                    enabled = canPressAmmo,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("field_press_ammo_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = SolarAmber.copy(alpha = 0.22f),
                        contentColor = SolarAmber
                    )
                ) {
                    Text(
                        text = "FIELD-PRESS +20 AMMO (-10S/-2F)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                if (hasTrauma) {
                    Button(
                        onClick = onTreatHuntingInjuries,
                        modifier = Modifier
                            .weight(1f)
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
                                "SUTURE TRAUMA (-1 MED)"
                            } else {
                                "FIELD SUTURE (-8W/-8F)"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VaultStatsTabScreen(
    uiState: OutpostUiState,
    onSetPosture: (VaultOperationalPosture) -> Unit = {},
    onWeldRoomPipes: () -> Unit = {},
    onTreatHuntingInjuries: () -> Unit = {},
    onUseMedkit: () -> Unit = {},
    onUseRadAway: () -> Unit = {},
    onEmergencyManualCrank: () -> Unit = {},
    onUnlockOrUpgradeSkill: (SkillNodeDefinition) -> Unit = {},
    onPerformPrestige: () -> Unit = {},
    onCommissionEdenCoreModule: (Int) -> Unit = {},
    onAuditCertificationTier: (Int) -> Unit = {},
    onNavigateToTab: (String) -> Unit = {},
    onQuickStabilizeAll: () -> Unit = {},
    onForageRations: () -> Unit = {},
    onBoilWater: () -> Unit = {},
    onFieldPressAmmo: () -> Unit = {},
    onScrubAir: () -> Unit = {},
    onIssueEmergencyRations: () -> Unit = {},
    onToggleRestRotation: () -> Unit = {},
    onCureSickAndExhaustedCrew: () -> Unit = {},
    onSetGridLoadBreakerMode: (GridLoadBreakerMode) -> Unit = {},
    isGoogleSignedIn: Boolean = true,
    onRequestGoogleSignIn: () -> Unit = {},
    onPublishVault: () -> Unit = {}
) {
    val state = uiState.state
    val snap = uiState.production
    val mothballedSet = OutpostCalculator.parseMothballedSet(state.mothballedBuildingsCsv)
    val totalRoomLevels = uiState.buildingsMap.values.sum()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("vault_stats_screen_list"),
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Read-Only Vault Telemetry Header & 10-Day Preparation / Siege Readiness Card
        item {
            val peaceActive = snap.isPreparationPeaceWindow
            val headerAccent = when {
                peaceActive -> CyanHydro
                snap.totalDefenseRating >= snap.todayRequiredDefense -> BioGreen
                else -> HazardRed
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vault_telemetry_overview_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VaultElevatedCard),
                border = BorderStroke(1.5.dp, headerAccent.copy(alpha = 0.8f))
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
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Outpost Telemetry",
                                tint = headerAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "ARES OUTPOST INFORMATION & RESOURCE LEVELS TELEMETRY",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = headerAccent,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Read-Only Commander Status Monitor • Every Action & Resource Counts",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = headerAccent.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, headerAccent.copy(alpha = 0.55f))
                        ) {
                            Text(
                                text = "DAY ${state.dayCounter} • LV.${state.commandLevel}",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                color = headerAccent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // 10-Day Preparation Countdown or Day 11+ Siege Readiness Banner
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vault_preparation_window_banner"),
                        shape = RoundedCornerShape(10.dp),
                        color = VaultDarkSurface,
                        border = BorderStroke(1.dp, headerAccent.copy(alpha = 0.65f))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (peaceActive) {
                                        "10-DAY PREPARATION WINDOW: DAY ${state.dayCounter}/10 (0 ATTACKS)"
                                    } else {
                                        "HOSTILE SIEGE ERA (DAY ${state.dayCounter}): DAILY RAIDER ATTACKS ACTIVE"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = headerAccent,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = if (peaceActive) {
                                        "${snap.preparationDaysRemaining}d Until Day 11"
                                    } else {
                                        "${snap.totalDefenseRating}/${snap.todayRequiredDefense} DEF"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = headerAccent,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            LinearProgressIndicator(
                                progress = {
                                    if (peaceActive) {
                                        (state.dayCounter.toFloat() / OutpostCalculator.PREPARATION_PEACE_DAYS.toFloat()).coerceIn(0f, 1f)
                                    } else {
                                        (snap.totalDefenseRating.toFloat() / snap.todayRequiredDefense.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = headerAccent,
                                trackColor = VaultElevatedCard
                            )
                            Text(
                                text = if (peaceActive) {
                                    "Strictly zero Raider or Mutant attacks on Days 1–10. Manually mine veins to build your Water Purifier, Hydroponics, Bio-Refinery & Turrets before the first attack on Day 11 (Requires ${snap.firstAttackDay11RequiredDefense} DEF; Current Outpost Defense: ${snap.totalDefenseRating} DEF)."
                                } else {
                                    "First attack window passed (Day 11+). Current Outpost Defense: ${snap.totalDefenseRating} DEF vs Today's Required ${snap.todayRequiredDefense} DEF • Perimeter Gate Lv.${state.blastDoorLevel} (${state.defenseIntegrityPercent}% Integrity) • Next 10-Day Boss in ${snap.daysUntilDay10Siege}d."
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatTileBox(
                            modifier = Modifier.weight(1f),
                            label = "WASTELAND SEASON",
                            value = snap.activeWastelandSeason.shortLabel,
                            subtitle = snap.activeWastelandSeason.modifierSummary,
                            accent = SolarAmber
                        )
                        StatTileBox(
                            modifier = Modifier.weight(1f),
                            label = "OUTPOST TECH ERA",
                            value = snap.vaultTechEra.shortBadge,
                            subtitle = "Global x${String.format(Locale.US, "%.2f", snap.commandLevelBonusMultiplier * snap.prestigeBonusMultiplier)}",
                            accent = AetherViolet
                        )
                        StatTileBox(
                            modifier = Modifier.weight(1f),
                            label = "CASCADE STATUS",
                            value = snap.cascadeStage.shortBadge,
                            subtitle = "Morale ${snap.citizenMoralePercent}%",
                            accent = if (snap.cascadeStage.stageNumber == 0) BioGreen else HazardRed
                        )
                    }
                }
            }
        }

        // 2. Dual 6-Day Game Over Survival Clocks Card (Read-Only)
        item {
            val blackoutDays = state.blackoutDaysCount
            val deficitDays = state.deficitDaysCount
            val clocksCritical = snap.isVaultBlackout || snap.isVaultResourceDeficit || blackoutDays > 0 || deficitDays > 0
            val clockAccent = if (clocksCritical) HazardRed else BioGreen

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vault_survival_clocks_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VaultElevatedCard),
                border = BorderStroke(1.5.dp, clockAccent.copy(alpha = 0.75f))
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
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = "6-Day Game Over Clocks",
                                tint = clockAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "DUAL 6-DAY GAME OVER SURVIVAL CLOCKS",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = clockAccent,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Running out of Outpost Resources OR Power for 6 consecutive days triggers Game Over.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Clock A: 6-Day Resource Depletion / Deficit Clock
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = VaultDarkSurface,
                        border = BorderStroke(1.dp, if (snap.isVaultResourceDeficit) HazardRed else BioGreen.copy(alpha = 0.45f))
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
                                    text = "RESOURCE DEPLETION CLOCK (FOOD / WATER / SCRAP)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (snap.isVaultResourceDeficit) HazardRed else BioGreen,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "$deficitDays / ${OutpostCalculator.MAX_DEFICIT_DAYS_BEFORE_GAME_OVER} DAYS",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (snap.isVaultResourceDeficit) HazardRed else BioGreen,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            LinearProgressIndicator(
                                progress = {
                                    (deficitDays.toFloat() / OutpostCalculator.MAX_DEFICIT_DAYS_BEFORE_GAME_OVER.toFloat()).coerceIn(0f, 1f)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (snap.isVaultResourceDeficit) HazardRed else BioGreen,
                                trackColor = VaultElevatedCard
                            )
                            Text(
                                text = if (snap.isVaultResourceDeficit) {
                                    "WARNING: Outpost is depleted or in net resource deficit (${OutpostCalculator.MAX_DEFICIT_DAYS_BEFORE_GAME_OVER - deficitDays} day(s) until collapse)! Defenses are shut down until positive supply is restored."
                                } else {
                                    "NOMINAL: Core resources are stocked and net production is stable (0/6 Days)."
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Clock B: 6-Day Power Blackout Clock
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = VaultDarkSurface,
                        border = BorderStroke(1.dp, if (snap.isVaultBlackout) HazardRed else CyanHydro.copy(alpha = 0.45f))
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
                                    text = "POWER GRID BLACKOUT CLOCK (${snap.totalMegawattGenerated.roundToInt()}/${snap.totalMegawattDemand.roundToInt()} MW)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (snap.isVaultBlackout) HazardRed else CyanHydro,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "$blackoutDays / ${OutpostCalculator.MAX_BLACKOUT_DAYS_BEFORE_GAME_OVER} DAYS",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (snap.isVaultBlackout) HazardRed else CyanHydro,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            LinearProgressIndicator(
                                progress = {
                                    (blackoutDays.toFloat() / OutpostCalculator.MAX_BLACKOUT_DAYS_BEFORE_GAME_OVER.toFloat()).coerceIn(0f, 1f)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (snap.isVaultBlackout) HazardRed else CyanHydro,
                                trackColor = VaultElevatedCard
                            )
                            Text(
                                text = if (snap.isVaultBlackout) {
                                    "BLACKOUT ACTIVE: Automated rooms & turrets are offline (${OutpostCalculator.MAX_BLACKOUT_DAYS_BEFORE_GAME_OVER - blackoutDays} day(s) until life-support failure • ${state.manualDynamoCranksRemaining}/4 Hand-Cranks left)."
                                } else {
                                    "GRID ENERGIZED: ${snap.totalMegawattGenerated.roundToInt()} MW generated vs ${snap.totalMegawattDemand.roundToInt()} MW demand (${state.manualDynamoCranksRemaining}/4 Emergency Cranks ready)."
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

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
                            label = "2M APPETITE TIER",
                            value = "Tier ${snap.dwellerUpkeepEscalationTier}",
                            subtitle = "x${String.format(Locale.US, "%.2f", snap.dwellerUpkeepEscalationMultiplier)} Crew Upkeep",
                            accent = SolarAmber
                        )
                        StatTileBox(
                            modifier = Modifier.weight(1f),
                            label = "FOOD ROT / DAY",
                            value = "${snap.foodSpoilagePercentPerDay}%/d",
                            subtitle = if (snap.isCryoPreservationActive) "Cryo Preserved" else "-${snap.dailyFoodSpoilageLoss.roundToInt()} Food/d",
                            accent = if (snap.isCryoPreservationActive) BioGreen else RustOrange
                        )
                    }
                }
            }
        }

        // 3. Complete Vault Resource Levels, Storage Caps & Net Rates Card (Read-Only)
        item {
            val isAllPositive = snap.isMakingPositiveResources
            val ledgerAccent = if (isAllPositive) BioGreen else HazardRed
            val scrapDrain = snap.roomScrapUpkeepPerSec + snap.pipeLeakScrapDrainPerSec
            val waterDrain = snap.roomWaterUpkeepPerSec + snap.citizenWaterUpkeepPerSec + snap.pipeLeakWaterDrainPerSec
            val foodDrain = snap.citizenFoodUpkeepPerSec
            val fuelDrain = snap.roomFuelUpkeepPerSec + snap.reactorFuelUpkeepPerSec

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vault_resource_levels_only_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VaultElevatedCard),
                border = BorderStroke(1.5.dp, ledgerAccent.copy(alpha = 0.75f))
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
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = "Resource Levels",
                                tint = ledgerAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "OUTPOST RESOURCE LEVELS & STORAGE CAP LEDGER",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = ledgerAccent,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Max Cap: ${snap.storageCap.toInt()} per resource • Full Shelf = Slowed to 25% Speed (100% Speed at ≤${snap.storeRoomResumeThreshold.toInt()})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ledgerAccent.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, ledgerAccent.copy(alpha = 0.55f))
                        ) {
                            Text(
                                text = if (isAllPositive) "NET POSITIVE" else "NET DEFICIT",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                color = ledgerAccent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    NetResourceLedgerRow(
                        name = "Scrap Metal ${if (snap.isScrapHalted) "[SLOWED 25% AT CAP]" else "[ACTIVE]"}",
                        current = state.scrap,
                        cap = snap.storageCap,
                        grossGain = snap.scrapPerSec + scrapDrain,
                        drain = scrapDrain,
                        net = snap.scrapPerSec,
                        color = RustOrange
                    )
                    NetResourceLedgerRow(
                        name = "Purified Water ${if (!snap.hasWaterPurifierRoom) "[MANUAL ONLY]" else if (snap.isWaterHalted) "[SLOWED 25% AT CAP]" else "[AUTO ONLINE]"}",
                        current = state.water,
                        cap = snap.storageCap,
                        grossGain = snap.waterPerSec + waterDrain,
                        drain = waterDrain,
                        net = snap.waterPerSec,
                        color = CyanHydro
                    )
                    NetResourceLedgerRow(
                        name = "Outpost Food Rations ${if (!snap.hasHydroponicsRoom) "[MANUAL ONLY]" else if (snap.isRationsHalted) "[SLOWED 25% AT CAP]" else "[AUTO ONLINE]"}",
                        current = state.rations,
                        cap = snap.storageCap,
                        grossGain = snap.rationsPerSec + foodDrain,
                        drain = foodDrain,
                        net = snap.rationsPerSec,
                        color = BioGreen
                    )
                    NetResourceLedgerRow(
                        name = "Refined Bio-Fuel ${if ((uiState.buildingsMap[BuildingType.BIO_SLUDGE_REFINERY.id] ?: 0) <= 0) "[MANUAL ONLY]" else if (snap.isFuelHalted) "[SLOWED 25% AT CAP]" else "[AUTO ONLINE]"}",
                        current = state.bioFuel,
                        cap = snap.storageCap,
                        grossGain = snap.bioFuelPerSec + fuelDrain,
                        drain = fuelDrain,
                        net = snap.bioFuelPerSec,
                        color = SolarAmber
                    )
                    NetResourceLedgerRow(
                        name = "Research Data ${if ((uiState.buildingsMap[BuildingType.SIGNAL_RESEARCH_ARRAY.id] ?: 0) <= 0) "[MANUAL ONLY]" else if (snap.isResearchHalted) "[SLOWED 25% AT CAP]" else "[AUTO ONLINE]"}",
                        current = state.researchData,
                        cap = snap.storageCap,
                        grossGain = snap.researchPerSec,
                        drain = 0.0,
                        net = snap.researchPerSec,
                        color = AetherViolet
                    )

                    HorizontalDivider(color = VaultCardBorder)

                    Text(
                        text = "STRATEGIC CURRENCIES, MUNITIONS & RARE STOCKPILES",
                        style = MaterialTheme.typography.labelSmall,
                        color = SolarAmber,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatTileBox(
                            modifier = Modifier.weight(1f),
                            label = "GOLD BULLION",
                            value = "${state.gold.toInt()}",
                            subtitle = "Total: ${state.totalGoldSalvaged.toInt()}G",
                            accent = SolarAmber
                        )
                        StatTileBox(
                            modifier = Modifier.weight(1f),
                            label = "BALLISTIC AMMO",
                            value = "${state.ammoRounds}",
                            subtitle = "${snap.activeAmmoType.shortLabel} Loaded",
                            accent = RustOrange
                        )
                        StatTileBox(
                            modifier = Modifier.weight(1f),
                            label = "THORIUM RODS",
                            value = "${state.thoriumRods.toInt()}",
                            subtitle = "Reactor Fuel",
                            accent = BioGreen
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatTileBox(
                            modifier = Modifier.weight(1f),
                            label = "AETHER / CORES",
                            value = "${state.aetherCells.toInt()} / ${state.mutagenCores.toInt()}",
                            subtitle = "Aether / Mutagen",
                            accent = AetherViolet
                        )
                        StatTileBox(
                            modifier = Modifier.weight(1f),
                            label = "MEDKIT / RADAWAY",
                            value = "${state.medkitsCount} / ${state.radAwayCount}",
                            subtitle = "Medical Stock",
                            accent = CyanHydro
                        )
                        StatTileBox(
                            modifier = Modifier.weight(1f),
                            label = "CACHES & RELICS",
                            value = "${state.rawSalvageCaches} / ${state.discoveredRelicsCount}",
                            subtitle = "+${snap.relicOutputBonusPercent.roundToInt()}% Relic Bonus",
                            accent = BioGreen
                        )
                    }
                }
            }
        }

        // 4. Daily Room-Level Scrap Maintenance & Automated System Status Table (Read-Only)
        item {
            val scrapShortage = snap.isRoomsLackingScrap
            val tableAccent = if (scrapShortage) HazardRed else SolarAmber
            val totalDailyScrapRequired = snap.dailyRoomScrapUpkeep + snap.dailyAutoRepairScrapCost

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vault_room_scrap_upkeep_table_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VaultElevatedCard),
                border = BorderStroke(1.5.dp, tableAccent.copy(alpha = 0.75f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
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
                                imageVector = Icons.Default.Build,
                                contentDescription = "Room Scrap Maintenance",
                                tint = tableAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "DAILY ROOM-LEVEL SCRAP MAINTENANCE LEDGER",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = tableAccent,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Each room level requires more Scrap per day to stay online (${String.format(Locale.US, "%.1f", totalDailyScrapRequired)} Scrap/day total for Room Lv.$totalRoomLevels).",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = tableAccent.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, tableAccent.copy(alpha = 0.55f))
                        ) {
                            Text(
                                text = "OUTPUT ${(snap.roomScrapOperationalMultiplier * snap.roomWearPenaltyMultiplier * 100).roundToInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                color = tableAccent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    BuildingType.entries.forEach { bType ->
                        val lv = uiState.buildingsMap[bType.id] ?: 0
                        val isMothballed = bType.id in mothballedSet
                        val isSabotaged = state.sabotagedBuildingId == bType.id
                        val upkeepMult = if (isMothballed) OutpostCalculator.MOTHBALLED_UPKEEP_MULTIPLIER else 1.0
                        val roomUpkeep = OutpostCalculator.computeBuildingResourceUpkeep(
                            buildingType = bType,
                            level = lv,
                            hydroDiscountMult = 1.0,
                            commandLevel = state.commandLevel,
                            dayCounter = state.dayCounter
                        )
                        val roomScrapPerDay = roomUpkeep.dailyScrap * upkeepMult
                        val roomWaterPerDay = roomUpkeep.dailyWater * upkeepMult
                        val roomFuelPerDay = roomUpkeep.dailyFuel * upkeepMult
                        val roomMw = if (lv <= 0 || isMothballed) 0 else OutpostCalculator.computeBuildingTotalMegawatts(bType, lv).roundToInt()
                        val roomCapBonus = OutpostCalculator.computeSingleRoomStorageCapContribution(bType, lv).toInt()

                        val statusLabel = when {
                            lv <= 0 -> "OFFLINE (MANUAL ONLY)"
                            isMothballed -> "STANDBY (-80% UPKEEP)"
                            snap.isVaultBlackout -> "UNPOWERED (BLACKOUT)"
                            isSabotaged -> "SABOTAGED (-50%)"
                            scrapShortage -> "LOW SCRAP (${(snap.roomScrapOperationalMultiplier * 100).roundToInt()}%)"
                            else -> "ONLINE (100% AUTO)"
                        }
                        val statusColor = when {
                            lv <= 0 -> MaterialTheme.colorScheme.onSurfaceVariant
                            isMothballed -> SolarAmber
                            snap.isVaultBlackout || isSabotaged || scrapShortage -> HazardRed
                            else -> BioGreen
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = VaultDarkSurface,
                            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.35f))
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${bType.displayName} (Lv.$lv)",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = statusLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = statusColor,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                Text(
                                    text = if (lv > 0) {
                                        "Daily Upkeep: -${String.format(Locale.US, "%.1f", roomScrapPerDay)} Scrap/d" +
                                            (if (roomWaterPerDay > 0.0) ", -${String.format(Locale.US, "%.1f", roomWaterPerDay)} H2O/d" else "") +
                                            (if (roomFuelPerDay > 0.0) ", -${String.format(Locale.US, "%.1f", roomFuelPerDay)} Fuel/d" else "") +
                                            " • Power: ${roomMw} MW • Cap Bonus: +$roomCapBonus"
                                    } else {
                                        "Unbuilt (Lv.0): 0 Scrap/d • Build Lv.1 in Mine & Build to bring automated system online (+${bType.megawattDemandPerLevel.roundToInt()} MW draw)"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Power Grid, Life-Support, Mining Stamina & Crew Vitals Telemetry Card (Read-Only)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vault_vitals_and_grid_telemetry_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VaultElevatedCard),
                border = BorderStroke(1.dp, VaultCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "POWER GRID, CREW VITALS & MANUAL MINING TELEMETRY",
                        style = MaterialTheme.typography.titleSmall,
                        color = CyanHydro,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatTileBox(
                            modifier = Modifier.weight(1f),
                            label = "MANUAL STAMINA",
                            value = "${state.dwellerStaminaPercent}%",
                            subtitle = "Tap Yield ${(snap.manualActionStaminaMultiplier * 100).roundToInt()}% (-${OutpostCalculator.MANUAL_ACTION_STAMINA_COST}%/Tap)",
                            accent = if (state.dwellerStaminaPercent < 35) HazardRed else BioGreen
                        )
                        StatTileBox(
                            modifier = Modifier.weight(1f),
                            label = "PICKAXE & SHAFT",
                            value = "Mk.${state.salvageRigLevel} (${state.pickaxeDurabilityPercent}%)",
                            subtitle = "Heat ${state.miningShaftHeatPercent}°C • ${state.deepCoreDepthMeters}m",
                            accent = if (state.pickaxeDurabilityPercent <= 25 || state.activeCaveInHazard) HazardRed else SolarAmber
                        )
                        StatTileBox(
                            modifier = Modifier.weight(1f),
                            label = "OXYGEN & RADS",
                            value = "${state.oxygenPercent}% O2",
                            subtitle = "${state.radiationMillisieverts} mSv Rads",
                            accent = if (state.oxygenPercent < 50 || state.radiationMillisieverts > 150) HazardRed else CyanHydro
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatTileBox(
                            modifier = Modifier.weight(1f),
                            label = "SURVIVOR CREW",
                            value = "${state.totalSurvivors}/${snap.maxWorkerCapacity}",
                            subtitle = "${snap.totalAssignedWorkers} Assigned • ${state.uniqueDwellersCount} Unique",
                            accent = BioGreen
                        )
                        StatTileBox(
                            modifier = Modifier.weight(1f),
                            label = "CREW HEALTH",
                            value = "${state.dwellerHealthPercent}% HP",
                            subtitle = "${state.sickDwellersCount} Sick • ${state.bleedingDwellersCount + state.venomInfectedDwellersCount} Trauma",
                            accent = if (state.dwellerHealthPercent < 60 || state.sickDwellersCount > 0) HazardRed else BioGreen
                        )
                        StatTileBox(
                            modifier = Modifier.weight(1f),
                            label = "WEAR & LEAKS",
                            value = "${state.roomStructuralWearPercent}% Wear",
                            subtitle = "${state.activePipeLeaksCount} Pipe Leaks • Gen ${state.generatorWearPercent}%",
                            accent = if (state.activePipeLeaksCount > 0 || state.roomStructuralWearPercent >= 35) HazardRed else CyanHydro
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatTileBox(
                            modifier = Modifier.weight(1f),
                            label = "RATIONING LAW",
                            value = snap.activeRationingPolicy.shortLabel,
                            subtitle = snap.activeRationingPolicy.effectSummary,
                            accent = SolarAmber
                        )
                        StatTileBox(
                            modifier = Modifier.weight(1f),
                            label = "GRID BREAKER",
                            value = snap.activeGridBreakerMode.shortLabel,
                            subtitle = "${snap.totalMegawattGenerated.roundToInt()}/${snap.totalMegawattDemand.roundToInt()} MW",
                            accent = CyanHydro
                        )
                        StatTileBox(
                            modifier = Modifier.weight(1f),
                            label = "EDEN & PRESTIGE",
                            value = "Eden ${state.edenCorePhase}/4",
                            subtitle = "Cert ${state.certificationTier}/5 • Prest ${state.prestigeLevel}",
                            accent = AetherViolet
                        )
                    }
                }
            }
        }

        // 6. Read-Only Overseer Terminal Event Log
        item {
            VaultEventLogTerminalCard(uiState = uiState)
        }
    }
}

@Composable
private fun VaultEventLogTerminalCard(
    uiState: OutpostUiState
) {
    val logs = uiState.eventLogs.take(6)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vault_event_log_terminal_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VaultElevatedCard),
        border = BorderStroke(1.dp, VaultCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "ARES OUTPOST COMMANDER TERMINAL EVENT LOG",
                style = MaterialTheme.typography.titleSmall,
                color = CyanHydro,
                fontWeight = FontWeight.ExtraBold
            )
            if (logs.isEmpty()) {
                Text(
                    text = "System initialized. Swing your Pickaxe in Mine & Build to begin salvaging.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                logs.forEach { entry ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = VaultDarkSurface,
                        border = BorderStroke(1.dp, VaultCardBorder)
                    ) {
                        Text(
                            text = "• ${entry.message}",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
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
                                    text = "Includes Precursor Chains, 60s Feeding, Pipe Leaks (${state.activePipeLeaksCount}) & Food Rot (${snap.foodSpoilagePercentPerDay}%/Day)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (snap.sabotagedBuildingId.isNotBlank()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = HazardRed.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, HazardRed)
                        ) {
                            Text(
                                text = "SABOTAGE PENALTY: Raiders crippled ${snap.sabotagedBuildingId} (-${snap.sabotagedBuildingPenaltyPercent}% output)! Repair in Rooms view.",
                                style = MaterialTheme.typography.labelSmall,
                                color = HazardRed,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = VaultDarkSurface,
                        border = BorderStroke(1.dp, if (snap.isCryoPreservationActive) BioGreen else RustOrange)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (snap.isCryoPreservationActive) {
                                    "CRYO-PRESERVATION: 100% PRESERVED (0% Rot Loss)"
                                } else {
                                    "FOOD SPOILAGE: ${snap.foodSpoilagePercentPerDay}% Rot/Day (-${snap.dailyFoodSpoilageLoss.roundToInt()} Rations/Day • Build Cryo Silo Lv.5 to stop rot)"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (snap.isCryoPreservationActive) BioGreen else RustOrange
                            )
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
                        name = "Outpost Food Rations",
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
                                    text = when {
                                        snap.isStoreRoomFull ->
                                            "SUPPLY DEPOT FULL (CAP ${snap.storageCap.toInt()}/EA): AUTO-RESUMES AT ≤${snap.storeRoomResumeThreshold.toInt()} (50%)"
                                        snap.isAnyStoreRoomResourceHalted ->
                                            "${snap.haltedResourcesSummary.uppercase()} SLOWED AT CAP (RESUMES AT ≤${snap.storeRoomResumeThreshold.toInt()} • OTHERS ACTIVE)"
                                        else ->
                                            "OUTPOST SUPPLY DEPOT: MAX ${snap.storageCap.toInt()} PER RESOURCE (RESUME AT ≤${snap.storeRoomResumeThreshold.toInt()})"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (snap.isStoreRoomFull) HazardRed else SolarAmber,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "When a resource reaches Store Room capacity (${snap.storageCap.toInt()}), it pauses and automatically resumes production as soon as that resource falls to half capacity (≤${snap.storeRoomResumeThreshold.toInt()} / 50%) — no Gold needed!",
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
fun PhysiologicalVitalsAndQuarantineCard(
    uiState: OutpostUiState,
    onToggleRestRotation: () -> Unit,
    onCureSickAndExhaustedCrew: () -> Unit,
    onIssueEmergencyRations: () -> Unit
) {
    val state = uiState.state
    val snap = uiState.production
    val stamina = state.dwellerStaminaPercent.coerceIn(0, 100)
    val sickCount = state.sickDwellersCount
    val traumaCount = state.bleedingDwellersCount + state.venomInfectedDwellersCount + state.injuredDwellersCount
    val isExhaustedOrSick = stamina < 35 || sickCount > 0 || traumaCount > 0
    val accent = when {
        sickCount > 0 || stamina < 35 || snap.isWorkersStarvingOrDehydrated -> HazardRed
        state.isRestRotationActive -> CyanHydro
        else -> BioGreen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("physiological_vitals_quarantine_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VaultElevatedCard),
        border = BorderStroke(1.5.dp, accent.copy(alpha = 0.75f))
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
                        contentDescription = "Physiological Vitals",
                        tint = accent,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "DEEP PHYSIOLOGICAL VITALS: HUNGER, THIRST, SLEEP & SICKNESS",
                            style = MaterialTheme.typography.titleSmall,
                            color = accent,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Crew need 60s Food/Water, Bunk Sleep Stamina (${stamina}%) & Quarantine vs Dysentery/Rad-Fever!",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accent.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.55f))
                ) {
                    Text(
                        text = if (state.isRestRotationActive) "SLEEP SHIFT" else "WORK SHIFT",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        color = accent,
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
                    border = BorderStroke(1.dp, if (snap.isWorkersStarvingOrDehydrated) HazardRed else BioGreen.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("HUNGER & THIRST", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${state.rations.toInt()}F • ${state.water.toInt()}W (${state.dwellerFeedCycleSecondsRemaining}s)",
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (snap.isWorkersStarvingOrDehydrated) HazardRed else BioGreen
                        )
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = VaultDarkSurface,
                    border = BorderStroke(1.dp, if (stamina < 35) HazardRed else CyanHydro.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("SLEEP STAMINA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "$stamina% (${(snap.staminaEfficiencyMultiplier * 100).roundToInt()}% Eff)",
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (stamina < 35) HazardRed else CyanHydro
                        )
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = VaultDarkSurface,
                    border = BorderStroke(1.dp, if (sickCount > 0 || traumaCount > 0) HazardRed else SolarAmber.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("SICK / TRAUMA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "$sickCount SICK / $traumaCount WOUND",
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (sickCount > 0 || traumaCount > 0) HazardRed else BioGreen
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onToggleRestRotation,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("toggle_rest_rotation_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (state.isRestRotationActive) CyanHydro.copy(alpha = 0.25f) else SolarAmber.copy(alpha = 0.2f),
                        contentColor = if (state.isRestRotationActive) CyanHydro else SolarAmber
                    )
                ) {
                    Text(
                        text = if (state.isRestRotationActive) "RESUME FULL SHIFT" else "ROTATE SLEEP SHIFT (+25% STAM)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Button(
                    onClick = onCureSickAndExhaustedCrew,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("cure_sick_exhausted_crew_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isExhaustedOrSick) HazardRed else BioGreen,
                        contentColor = if (isExhaustedOrSick) Color.White else Color.Black
                    )
                ) {
                    Text(
                        text = if (state.medkitsCount > 0 || state.radAwayCount > 0) {
                            "QUARANTINE CURE (-1 MED)"
                        } else {
                            "CURE & RESTORE (-6W/-6F)"
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
fun PowerGridLoadBalancerCard(
    uiState: OutpostUiState,
    onSetGridLoadBreakerMode: (GridLoadBreakerMode) -> Unit,
    onWeldRoomPipes: () -> Unit,
    onEmergencyManualCrank: () -> Unit
) {
    val state = uiState.state
    val snap = uiState.production
    val activeMode = snap.activeGridBreakerMode
    val mwGen = snap.totalMegawattGenerated
    val mwDemand = snap.totalMegawattDemand
    val accent = when {
        snap.isVaultBlackout -> HazardRed
        snap.isVaultBrownout -> RustOrange
        else -> SolarAmber
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("power_grid_load_balancer_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VaultElevatedCard),
        border = BorderStroke(1.5.dp, accent.copy(alpha = 0.75f))
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
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = "Power Grid Load Balancer",
                        tint = accent,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "STRICT POWER GRID LOAD BALANCER & CIRCUIT BREAKERS",
                            style = MaterialTheme.typography.titleSmall,
                            color = accent,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Active Breaker: ${activeMode.shortLabel} (${activeMode.effectSummary})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accent.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.55f))
                ) {
                    Text(
                        text = "${mwGen.roundToInt()}/${mwDemand.roundToInt()} MW",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        color = accent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GridLoadBreakerMode.entries.forEach { mode ->
                    val selected = mode == activeMode
                    FilledTonalButton(
                        onClick = { onSetGridLoadBreakerMode(mode) },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("grid_breaker_${mode.id.lowercase()}_btn"),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (selected) SolarAmber.copy(alpha = 0.28f) else VaultDarkSurface,
                            contentColor = if (selected) SolarAmber else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(
                            text = mode.shortLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (snap.isVaultBlackout || state.activePipeLeaksCount > 0 || state.roomStructuralWearPercent >= 25) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (snap.isVaultBlackout && state.manualDynamoCranksRemaining > 0) {
                        Button(
                            onClick = onEmergencyManualCrank,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HazardRed, contentColor = Color.White)
                        ) {
                            Text(
                                text = "CRANK DYNAMO (${state.manualDynamoCranksRemaining}/4)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                    if (state.activePipeLeaksCount > 0 || state.roomStructuralWearPercent >= 25) {
                        FilledTonalButton(
                            onClick = onWeldRoomPipes,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = CyanHydro.copy(alpha = 0.22f),
                                contentColor = CyanHydro
                            )
                        ) {
                            Text(
                                text = "WELD ${state.activePipeLeaksCount} LEAKS & WEAR (${state.roomStructuralWearPercent}%)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
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
    onLaunchRescueBeacon: () -> Unit = {},
    onIssueEmergencyRations: () -> Unit = {},
    onToggleRestRotation: () -> Unit = {},
    onCureSickAndExhaustedCrew: () -> Unit = {},
    onSellToTown: (TradeTown, ResourceType) -> Unit,
    onBuyRareItem: (RareSyndicateItem) -> Unit,
    onDispatchConvoy: (TradeTown) -> Unit,
    onUpgradeConvoy: () -> Unit,
    onUpgradePowerStation: (PowerStationType) -> Unit,
    onInjectThoriumOverdrive: () -> Unit,
    onTriggerSolarFlare: () -> Unit,
    onTriggerEclipseShroud: () -> Unit,
    onUnlockOrUpgradeSkill: (SkillNodeDefinition) -> Unit = {},
    onPerformPrestige: () -> Unit = {},
    isAuthenticated: Boolean,
    userEmail: String?,
    authErrorMessage: String?,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onPublishVault: () -> Unit,
    onBroadcastSupplyDrop: () -> Unit,
    onClaimSupplyDrop: (com.example.data.remote.PublicSupplyDrop) -> Unit
) {
    var selectedSubTab by rememberSaveable { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilledTonalButton(
                onClick = { selectedSubTab = 0 },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("crew_power_subtab_crew"),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (selectedSubTab == 0) BioGreen.copy(alpha = 0.24f) else VaultElevatedCard,
                    contentColor = if (selectedSubTab == 0) BioGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "SURVIVORS (${uiState.state.totalSurvivors}/${uiState.production.maxWorkerCapacity})",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }

            FilledTonalButton(
                onClick = { selectedSubTab = 1 },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("crew_power_subtab_power"),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (selectedSubTab == 1) SolarAmber.copy(alpha = 0.24f) else VaultElevatedCard,
                    contentColor = if (selectedSubTab == 1) SolarAmber else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(Icons.Default.ElectricBolt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "POWER (${uiState.production.totalMegawattGenerated.roundToInt()}/${uiState.production.totalMegawattDemand.roundToInt()} MW)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }

            FilledTonalButton(
                onClick = { selectedSubTab = 2 },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("crew_power_subtab_skills"),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (selectedSubTab == 2) AetherViolet.copy(alpha = 0.24f) else VaultElevatedCard,
                    contentColor = if (selectedSubTab == 2) AetherViolet else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(Icons.Default.Hub, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "SKILLS (${uiState.state.availableSkillPoints} SP)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (selectedSubTab) {
                0 -> SurvivorsTabScreen(
                    uiState = uiState,
                    onHireCandidate = onHireCandidate,
                    onRefreshBoard = onRefreshBoard,
                    onSendExpedition = onSendExpedition,
                    onAdjustWorker = onAssignWorker,
                    onToggleAutoShift = onToggleAutoShift,
                    onOptimizeCrew = onOptimizeCrewNow,
                    onLaunchRescueBeacon = onLaunchRescueBeacon,
                    onIssueEmergencyRations = onIssueEmergencyRations,
                    onToggleRestRotation = onToggleRestRotation,
                    onCureSickAndExhaustedCrew = onCureSickAndExhaustedCrew
                )
                1 -> TradePowerTabScreen(
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
                else -> SkillTreeTabScreen(
                    uiState = uiState,
                    onUnlockSkill = onUnlockOrUpgradeSkill,
                    onPerformPrestige = onPerformPrestige,
                    isGoogleSignedIn = isAuthenticated,
                    onRequestGoogleSignIn = onSignInClick
                )
            }
        }
    }
}
