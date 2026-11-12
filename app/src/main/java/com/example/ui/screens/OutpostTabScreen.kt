package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.BuildingUpgradeCost
import com.example.domain.OutpostCalculator
import com.example.domain.SalvageYieldPreview
import com.example.model.ActiveBuildingConstruction
import com.example.model.BuildingCategory
import com.example.model.BuildingType
import com.example.model.PickaxeTier
import com.example.model.PowerStationType
import com.example.model.RareAnomaly
import com.example.model.SalvageNodeSlot
import com.example.model.SubterraneanBossType
import com.example.model.WorkerClassType
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Phishing
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.model.FishingSpot
import com.example.model.VaultMapStation
import com.example.model.VaultOperationalPosture
import com.example.ui.components.Vault3dHologramMapCard
import com.example.ui.components.formatCompact
import com.example.ui.theme.BioluminescentViolet
import com.example.ui.theme.BunkerCharcoal
import com.example.ui.theme.CryoCyan
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.HazardRed
import com.example.ui.theme.PanelBorder
import com.example.ui.theme.RadTeal
import com.example.ui.theme.RustCopper
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

private enum class VaultSubView(val label: String, val tag: String) {
    MAP_AND_MINE("⛏ Mine (0S)", "vault_subtab_map"),
    VAULT_3D_MAP("🗺 3D Map", "vault_subtab_3d_map"),
    VAULT_ROOMS("🏗 Rooms", "vault_subtab_rooms"),
    POWER_AND_CLOCKS("⚡ Power", "vault_subtab_power"),
    ALL("📋 All", "vault_subtab_all")
}

@Composable
fun OutpostTabScreen(
    uiState: OutpostUiState,
    onHarvestNode: (Int) -> Unit,
    onDismantleCaches: (Boolean) -> Unit,
    onSmeltSalvageForGold: () -> Unit,
    onProspectRichVein: () -> Unit,
    onUpgradeSalvageRig: () -> Unit,
    onUpgradeBuilding: (BuildingType) -> Unit,
    onAccelerateConstruction: () -> Unit = {},
    onRushConstruction: () -> Unit = {},
    onUpgradePowerStation: (PowerStationType) -> Unit,
    onCrankDynamo: () -> Unit,
    onMaintainGenerators: () -> Unit = {},
    onScrubAir: () -> Unit = {},
    onFlushRadiation: () -> Unit = {},
    onAdvanceDay: () -> Unit,
    onClaimAnomaly: () -> Unit,
    onWalkToStation: (VaultMapStation) -> Unit = {},
    onFishSpot: (FishingSpot) -> Unit = {},
    onIssueEmergencyRations: () -> Unit = {},
    onSetOperationalPosture: (VaultOperationalPosture) -> Unit = {},
    onCallEmergencyReliefDrop: () -> Unit = {},
    onRequestRestartGame: () -> Unit = {},
    onSharpenPickaxe: () -> Unit = {},
    onCoolMiningShaft: () -> Unit = {},
    onClearCaveIn: () -> Unit = {},
    onWeldRoomPipes: () -> Unit = {},
    onRepairSabotagedBuilding: () -> Unit = {},
    onExecuteDeepCoreDrill: () -> Unit = {},
    onStrikeSubterraneanBoss: () -> Unit = {},
    initialSubViewName: String? = null,
    modifier: Modifier = Modifier
) {
    val defaultSubView = remember(initialSubViewName) {
        when (initialSubViewName) {
            "ROOMS" -> VaultSubView.VAULT_ROOMS
            "3D_MAP" -> VaultSubView.VAULT_3D_MAP
            "POWER" -> VaultSubView.POWER_AND_CLOCKS
            else -> VaultSubView.MAP_AND_MINE
        }
    }
    var selectedSubView by remember(initialSubViewName) { mutableStateOf(defaultSubView) }
    var selectedCategory by remember { mutableStateOf<BuildingCategory?>(null) }
    val filteredBuildings = remember(selectedCategory) {
        if (selectedCategory == null) BuildingType.entries
        else BuildingType.entries.filter { it.category == selectedCategory }
    }

    val show2dVaultMap = selectedSubView == VaultSubView.ALL || selectedSubView == VaultSubView.MAP_AND_MINE
    val show3dVaultMap = selectedSubView == VaultSubView.ALL || selectedSubView == VaultSubView.VAULT_3D_MAP || selectedSubView == VaultSubView.VAULT_ROOMS
    val showMineAndFix = selectedSubView == VaultSubView.ALL || selectedSubView == VaultSubView.MAP_AND_MINE
    val showVaultRooms = selectedSubView == VaultSubView.ALL || selectedSubView == VaultSubView.VAULT_ROOMS
    val showPowerGrid = selectedSubView == VaultSubView.ALL || selectedSubView == VaultSubView.POWER_AND_CLOCKS

    Column(modifier = modifier.fillMaxSize()) {
        // Mobile-First CRT Bunker Sub-Tab Switcher Bar (48.dp touch targets, zero horizontal scroll needed)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            VaultSubView.entries.forEach { subView ->
                val isSelected = selectedSubView == subView
                Surface(
                    color = if (isSelected) SolarAmber.copy(alpha = 0.24f) else SteelPanel,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) SolarAmber else PanelBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { selectedSubView = subView }
                        .testTag(subView.tag)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = subView.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) SolarAmber else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Active Rare Anomaly Banner (if spawned)
            uiState.activeAnomaly?.let { anomaly ->
                item(key = "active_anomaly_card") {
                    RareAnomalyCard(
                        anomaly = anomaly,
                        onClaim = onClaimAnomaly
                    )
                }
            }

            // Always show the compact Doomsday, Power & Quick-Fix Control Deck at top of Power & Clocks, Map & Mine, or during emergencies
            if (showPowerGrid || showMineAndFix || uiState.production.isVaultBlackout || uiState.production.isDefenseShutDownByDeficit) {
                item(key = "survival_outpost_status_banner") {
                    VaultPowerAndDayControlCard(
                        uiState = uiState,
                        onCrankDynamo = onCrankDynamo,
                        onMaintainGenerators = onMaintainGenerators,
                        onScrubAir = onScrubAir,
                        onFlushRadiation = onFlushRadiation,
                        onIssueEmergencyRations = onIssueEmergencyRations,
                        onSetOperationalPosture = onSetOperationalPosture,
                        onCallEmergencyReliefDrop = onCallEmergencyReliefDrop,
                        onAdvanceDay = onAdvanceDay,
                        onRequestRestartGame = onRequestRestartGame
                    )
                }
            }

            if (show3dVaultMap) {
                item(key = "interactive_3d_vault_hologram_map") {
                    Vault3dHologramMapCard(
                        uiState = uiState,
                        onSelectStation = onWalkToStation,
                        onUpgradeBuilding = onUpgradeBuilding,
                        onMineAtSurface = { onHarvestNode(0) },
                        onFishAtSump = { onFishSpot(FishingSpot.CRATER_SHALLOWS_TROUT) }
                    )
                }
            }

            // Active Vault Room Construction Progress Card (always visible at top when building!)
            uiState.activeConstruction?.let { activeBuild ->
                item(key = "active_building_construction_banner") {
                    ActiveBuildingConstructionCard(
                        construction = activeBuild,
                        canAffordRush = uiState.state.gold >= activeBuild.rushGoldCost,
                        onAccelerate = onAccelerateConstruction,
                        onRush = onRushConstruction
                    )
                }
            }

            if (showMineAndFix) {
                // 1. Primary Action First: Pickaxe Mining & Bedrock Excavation Deck (0 Scrap Cost!)
                item(key = "salvage_field_deck") {
                    WastelandSalvageFieldDeck(
                        uiState = uiState,
                        onHarvestNode = onHarvestNode,
                        onProspectRichVein = onProspectRichVein,
                        onUpgradeSalvageRig = onUpgradeSalvageRig
                    )
                }

                // 2. 1-Tap Free Mining Maintenance (Sharpen Pickaxe & Clear Cave-In cost 0 Scrap!)
                item(key = "hardcore_mining_hazards_item") {
                    HardcoreMiningHazardsCard(
                        uiState = uiState,
                        onSharpenPickaxe = onSharpenPickaxe,
                        onCoolMiningShaft = onCoolMiningShaft,
                        onClearCaveIn = onClearCaveIn
                    )
                }

                // 3. Deep-Core Borehole Drilling & Pre-Fall Relics (0 Scrap Cost!)
                item(key = "subterranean_deep_core_drilling_item") {
                    SubterraneanDeepCoreDrillingCard(
                        uiState = uiState,
                        onExecuteDeepCoreDrill = onExecuteDeepCoreDrill,
                        onStrikeSubterraneanBoss = onStrikeSubterraneanBoss
                    )
                }

                // 4. Ore Crates & Gold Bullion Smelter
                item(key = "salvage_processing_bench_item") {
                    SalvageProcessingBenchCard(
                        uiState = uiState,
                        onDismantleOne = { onDismantleCaches(false) },
                        onDismantleAll = { onDismantleCaches(true) },
                        onSmeltSalvageForGold = onSmeltSalvageForGold
                    )
                }
            }

            if (show2dVaultMap) {
                // Interactive 2D Underground Cutaway Vault Map (Walk to click each room to upgrade, mine, or fish!)
                item(key = "interactive_2d_vault_cutaway_map") {
                    Interactive2dVaultCutawayCard(
                        uiState = uiState,
                        onWalkToStation = onWalkToStation,
                        onMineOreAtStation = { onHarvestNode(0) },
                        onFishAtStation = { onFishSpot(FishingSpot.CRATER_SHALLOWS_TROUT) },
                        onUpgradeRoomAtStation = onUpgradeBuilding
                    )
                }
            }

            if (showPowerGrid) {
                // 4. Vault Power Generators Section (Vault Requires Power to Run)
                item(key = "vault_power_generators_section") {
                    VaultPowerGeneratorsSectionCard(
                        uiState = uiState,
                        onUpgradePowerStation = onUpgradePowerStation
                    )
                }
            }

            if (showVaultRooms) {
                item(key = "hardcore_building_maintenance_item") {
                    HardcoreBuildingMaintenanceCard(
                        uiState = uiState,
                        onWeldRoomPipes = onWeldRoomPipes
                    )
                }

                if (uiState.state.sabotagedBuildingId.isNotBlank()) {
                    item(key = "sabotaged_room_alert_card") {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = HazardRed.copy(alpha = 0.2f),
                            border = BorderStroke(1.5.dp, HazardRed)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "⚠ FACILITY SABOTAGED: ${uiState.state.sabotagedBuildingId} (-${uiState.state.sabotagedBuildingPenaltyPercent}% Output)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = HazardRed
                                )
                                Text(
                                    text = "Raider sappers bypassed the perimeter and crippled this facility! Repair immediately with Scrap to restore normal output.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                                Button(
                                    onClick = onRepairSabotagedBuilding,
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = HazardRed,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("REPAIR SABOTAGED ROOM (-25 SCRAP)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // 5. Build & Repair Broken Vault Shelter Rooms Header & Category Filter Row
                item(key = "town_structures_header") {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val builtRoomsCount = BuildingType.entries.count { (uiState.buildingsMap[it.id] ?: 0) > 0 }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "REPAIR & UPGRADE VAULT ROOMS ($builtRoomsCount/${BuildingType.entries.size} REPAIRED)",
                                style = MaterialTheme.typography.titleSmall,
                                color = SolarAmber,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Dwellers: ${uiState.state.totalSurvivors}/${uiState.production.maxWorkerCapacity}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (uiState.state.totalSurvivors == 0) WarningOrange else RadTeal,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "You start in an empty, broken Vault! Repair broken rooms on your own, then hire Vault Dwellers to automate production, O2 scrubbing & Medbay care.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedCategory == null,
                                    onClick = { selectedCategory = null },
                                    label = { Text("All Rooms (${BuildingType.entries.size})") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SolarAmber.copy(alpha = 0.22f),
                                        selectedLabelColor = SolarAmber
                                    )
                                )
                            }
                            items(BuildingCategory.entries) { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    label = { Text(category.label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SolarAmber.copy(alpha = 0.22f),
                                        selectedLabelColor = SolarAmber
                                    )
                                )
                            }
                        }
                    }
                }

                // 6. Constructible & Upgradable Vault Rooms
                items(filteredBuildings, key = { it.id }) { buildingType ->
                    val currentLevel = uiState.buildingsMap[buildingType.id] ?: 0
                    val costBreakdown = OutpostCalculator.computeBuildingUpgradeCost(
                        buildingType = buildingType,
                        currentLevel = currentLevel,
                        state = uiState.state,
                        skillsMap = uiState.skillsMap,
                        workersMap = uiState.workersMap,
                        currentTotalMwGenerated = uiState.production.totalMegawattGenerated,
                        currentTotalMwDemand = uiState.production.totalMegawattDemand,
                        currentStorageCap = uiState.production.storageCap
                    )
                    val assignedSynergyWorkers = buildingType.favoredWorkerClass?.let {
                        uiState.workersMap[it.id]?.assignedCount ?: 0
                    } ?: uiState.state.totalSurvivors

                    BuildingStructureCard(
                        type = buildingType,
                        level = currentLevel,
                        cost = costBreakdown,
                        assignedSynergyWorkers = assignedSynergyWorkers,
                        isVaultBlackout = uiState.production.isVaultBlackout,
                        activeConstruction = uiState.activeConstruction,
                        onUpgrade = { onUpgradeBuilding(buildingType) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveBuildingConstructionCard(
    construction: ActiveBuildingConstruction,
    canAffordRush: Boolean,
    onAccelerate: () -> Unit,
    onRush: () -> Unit
) {
    val progress = ((construction.totalSeconds - construction.remainingSeconds).toFloat() /
        construction.totalSeconds.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

    Surface(
        color = SolarAmber.copy(alpha = 0.14f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, SolarAmber),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_construction_card")
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CONSTRUCTING ROOM #${construction.buildingType.ordinal + 1}: ${construction.buildingType.displayName.uppercase()} -> LV.${construction.targetLevel}",
                        style = MaterialTheme.typography.titleSmall,
                        color = SolarAmber,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Room Lv.${construction.targetLevel} Build Timer: ${construction.remainingSeconds}s / ${construction.totalSeconds}s • Tap Hammer & Weld (-4s)!",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                }
                Surface(
                    color = BunkerCharcoal,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, SolarAmber)
                ) {
                    Text(
                        text = "${construction.remainingSeconds}s",
                        style = MaterialTheme.typography.labelMedium,
                        color = SolarAmber,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = SolarAmber,
                trackColor = BunkerCharcoal
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAccelerate,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SolarAmber,
                        contentColor = BunkerCharcoal
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("accelerate_construction_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Construction,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "HAMMER & WELD (-4s)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onRush,
                    enabled = canAffordRush,
                    border = BorderStroke(1.dp, if (canAffordRush) XpGold else PanelBorder),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("rush_construction_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = if (canAffordRush) XpGold else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "RUSH NOW (${construction.rushGoldCost.toInt()} GOLD)",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (canAffordRush) XpGold else TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun Interactive2dVaultCutawayCard(
    uiState: OutpostUiState,
    onWalkToStation: (VaultMapStation) -> Unit,
    onMineOreAtStation: () -> Unit,
    onFishAtStation: () -> Unit,
    onUpgradeRoomAtStation: (BuildingType) -> Unit
) {
    val currentStation = uiState.overseerStation
    val isWalking = uiState.isOverseerWalking
    val floors = remember {
        VaultMapStation.entries.groupBy { it.floorIndex }.toSortedMap()
    }

    Surface(
        color = SteelPanel,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, RadTeal.copy(alpha = 0.85f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vault_2d_cutaway_card")
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "2D VAULT CUTAWAY MAP (WALK TO MINE, FISH & UPGRADE)",
                        style = MaterialTheme.typography.titleSmall,
                        color = RadTeal,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isWalking) {
                            "🚶 Overseer taking Vault Elevator to ${currentStation.shortTitle}..."
                        } else {
                            "📍 Overseer at: ${currentStation.shortTitle} • Tap any room to walk there, then tap again to Mine, Fish, or Upgrade!"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isWalking) XpGold else TextPrimary
                    )
                }
                Surface(
                    color = XpGold.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, XpGold)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = "Overseer Position",
                            tint = XpGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (currentStation.floorIndex == 0) "SURFACE" else "FLOOR B${currentStation.floorIndex}",
                            style = MaterialTheme.typography.labelSmall,
                            color = XpGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 2D Underground Floors + Central Elevator Shaft
            floors.forEach { (floorIdx, stationsOnFloor) ->
                val leftStation = stationsOnFloor.firstOrNull { it.wingColumn == 0 }
                val rightStation = stationsOnFloor.firstOrNull { it.wingColumn == 1 }
                val isOverseerOnThisFloor = currentStation.floorIndex == floorIdx

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (leftStation != null) {
                        Vault2dRoomCell(
                            station = leftStation,
                            uiState = uiState,
                            isOverseerHere = currentStation == leftStation && !isWalking,
                            onCellClick = {
                                if (currentStation != leftStation) {
                                    onWalkToStation(leftStation)
                                } else {
                                    when (leftStation) {
                                        VaultMapStation.SURFACE_MINE_SHAFT -> onMineOreAtStation()
                                        VaultMapStation.UNDERGROUND_FISHING_SUMP -> onFishAtStation()
                                        else -> leftStation.buildingType?.let(onUpgradeRoomAtStation)
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Central Vault Elevator Shaft
                    Surface(
                        color = if (isOverseerOnThisFloor) XpGold.copy(alpha = 0.26f) else BunkerCharcoal,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isOverseerOnThisFloor) XpGold else PanelBorder
                        ),
                        modifier = Modifier
                            .width(42.dp)
                            .height(62.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (floorIdx == 0) "SURF" else "B$floorIdx",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isOverseerOnThisFloor) XpGold else TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isOverseerOnThisFloor) "🧍⬆⬇" else "││",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isOverseerOnThisFloor) XpGold else TextMuted
                            )
                        }
                    }

                    if (rightStation != null) {
                        Vault2dRoomCell(
                            station = rightStation,
                            uiState = uiState,
                            isOverseerHere = currentStation == rightStation && !isWalking,
                            onCellClick = {
                                if (currentStation != rightStation) {
                                    onWalkToStation(rightStation)
                                } else {
                                    when (rightStation) {
                                        VaultMapStation.SURFACE_MINE_SHAFT -> onMineOreAtStation()
                                        VaultMapStation.UNDERGROUND_FISHING_SUMP -> onFishAtStation()
                                        else -> rightStation.buildingType?.let(onUpgradeRoomAtStation)
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        val totalReactorLv = uiState.state.bioTurbineLevel + uiState.state.solarTowerLevel +
                            uiState.state.thoriumReactorLevel + uiState.state.aetherSubstationLevel
                        Surface(
                            color = ElevatedSlate,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, WarningOrange.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .weight(1f)
                                .height(62.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Reactor Fuel Bay",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = WarningOrange,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "LV.$totalReactorLv",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = XpGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "Burn: -${uiState.production.reactorFuelUpkeepPerSec}/s Fuel",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Direct Action Bar for the Station Where the Overseer Currently Stands
            val activeBuilding = currentStation.buildingType
            val currentRoomLv = activeBuilding?.let { uiState.buildingsMap[it.id] ?: 0 } ?: 0
            val activeRoomCost = activeBuilding?.let {
                OutpostCalculator.computeBuildingUpgradeCost(
                    buildingType = it,
                    currentLevel = currentRoomLv,
                    state = uiState.state,
                    skillsMap = uiState.skillsMap,
                    workersMap = uiState.workersMap,
                    currentTotalMwGenerated = uiState.production.totalMegawattGenerated,
                    currentTotalMwDemand = uiState.production.totalMegawattDemand
                )
            }

            Button(
                onClick = {
                    when (currentStation) {
                        VaultMapStation.SURFACE_MINE_SHAFT -> onMineOreAtStation()
                        VaultMapStation.UNDERGROUND_FISHING_SUMP -> onFishAtStation()
                        else -> activeBuilding?.let(onUpgradeRoomAtStation)
                    }
                },
                enabled = !isWalking && when (currentStation) {
                    VaultMapStation.SURFACE_MINE_SHAFT -> true
                    VaultMapStation.UNDERGROUND_FISHING_SUMP -> true
                    else -> (activeRoomCost?.canAfford == true) && uiState.activeConstruction == null
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = currentStation.accentColor,
                    contentColor = BunkerCharcoal
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("vault_2d_station_action_button")
            ) {
                val actionLabel = when (currentStation) {
                    VaultMapStation.SURFACE_MINE_SHAFT ->
                        "⛏ MINE ORE HERE AT SURFACE SHAFT (+SCRAP, +GOLD & +XP)"
                    VaultMapStation.UNDERGROUND_FISHING_SUMP ->
                        "🎣 CAST LINE HERE AT SUB-VAULT SUMP (+FOOD, +WATER & +GOLD)"
                    else -> {
                        val bTime = activeRoomCost?.buildTimeSeconds ?: 10
                        val sCost = activeRoomCost?.scrapCost?.toInt() ?: 0
                        if (currentRoomLv == 0) {
                            "🔧 REPAIR ${currentStation.shortTitle.uppercase()} HERE (${sCost}S • ${bTime}s)"
                        } else {
                            "⬆ UPGRADE ${currentStation.shortTitle.uppercase()} TO LV.${currentRoomLv + 1} (${sCost}S • ${bTime}s)"
                        }
                    }
                }
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun Vault2dRoomCell(
    station: VaultMapStation,
    uiState: OutpostUiState,
    isOverseerHere: Boolean,
    onCellClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bType = station.buildingType
    val roomLevel = bType?.let { uiState.buildingsMap[it.id] ?: 0 } ?: 1
    val isConstructingThis = bType != null && uiState.activeConstruction?.buildingType == bType

    val statusBadge = when {
        station == VaultMapStation.SURFACE_MINE_SHAFT -> "⛏ MINE"
        station == VaultMapStation.UNDERGROUND_FISHING_SUMP -> "🎣 FISH"
        isConstructingThis -> "⏱ ${uiState.activeConstruction?.remainingSeconds ?: 0}s"
        roomLevel <= 0 -> "BROKEN"
        else -> "LV.$roomLevel"
    }

    val actionPrompt = when {
        !isOverseerHere -> "Tap to Walk Here"
        station == VaultMapStation.SURFACE_MINE_SHAFT -> "🧍 HERE • Tap to Mine!"
        station == VaultMapStation.UNDERGROUND_FISHING_SUMP -> "🧍 HERE • Tap to Fish!"
        roomLevel <= 0 -> "🧍 HERE • Tap to Repair!"
        else -> "🧍 HERE • Tap to Upgrade!"
    }

    Surface(
        color = if (isOverseerHere) station.accentColor.copy(alpha = 0.20f) else ElevatedSlate,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            width = if (isOverseerHere) 2.dp else 1.dp,
            color = when {
                isOverseerHere -> XpGold
                roomLevel > 0 -> station.accentColor.copy(alpha = 0.7f)
                else -> WarningOrange.copy(alpha = 0.6f)
            }
        ),
        modifier = modifier
            .height(62.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onCellClick)
            .testTag("vault_2d_station_${station.id.lowercase()}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 5.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = station.shortTitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isOverseerHere) XpGold else TextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = statusBadge,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (roomLevel <= 0 && bType != null) WarningOrange else station.accentColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = actionPrompt,
                style = MaterialTheme.typography.labelSmall,
                color = if (isOverseerHere) SurplusGreen else TextSecondary,
                fontWeight = if (isOverseerHere) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun VaultPowerAndDayControlCard(
    uiState: OutpostUiState,
    onCrankDynamo: () -> Unit,
    onMaintainGenerators: () -> Unit,
    onScrubAir: () -> Unit,
    onFlushRadiation: () -> Unit,
    onIssueEmergencyRations: () -> Unit,
    onSetOperationalPosture: (VaultOperationalPosture) -> Unit = {},
    onCallEmergencyReliefDrop: () -> Unit = {},
    onAdvanceDay: () -> Unit,
    onRequestRestartGame: () -> Unit = {}
) {
    val haptics = LocalHapticFeedback.current
    val state = uiState.state
    val prod = uiState.production
    val isBlackout = prod.isVaultBlackout
    val isBrownout = prod.isVaultBrownout
    val isStarving = prod.isWorkersStarvingOrDehydrated
    val isDeficitShutdown = prod.isDefenseShutDownByDeficit
    val builtRoomsCount = BuildingType.entries.count { (uiState.buildingsMap[it.id] ?: 0) > 0 }
    val activePosture = prod.operationalPosture

    Surface(
        color = if (isBlackout || isStarving || isDeficitShutdown) HazardRed.copy(alpha = 0.14f) else SteelPanel,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            if (isBlackout || isStarving || isDeficitShutdown) HazardRed else SurplusGreen.copy(alpha = 0.70f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("outpost_citizen_status_card")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val gridHeadline = when {
                        isDeficitShutdown -> "DEFENSES SHUT DOWN! (VAULT TAKES MORE TO RUN THAN GAINING • ${state.deficitDaysCount}/2D)"
                        isStarving -> "CREW STARVING / DEHYDRATED! (WORKER ROOMS & TURRETS HALTED)"
                        isBrownout -> "GRID BROWNOUT (${prod.totalMegawattGenerated.roundToInt()}/${prod.totalMegawattDemand.roundToInt()} MW • OUTAGE ${state.blackoutDaysCount}/6D)"
                        isBlackout -> "VAULT POWER BLACKOUT! (${prod.totalMegawattGenerated.roundToInt()}/${prod.totalMegawattDemand.roundToInt()} MW • OUTAGE ${state.blackoutDaysCount}/6D)"
                        else -> "VAULT LIFE-SUPPORT ONLINE (${prod.totalMegawattGenerated.roundToInt()}/${prod.totalMegawattDemand.roundToInt()} MW • WEAR ${state.generatorWearPercent}%)"
                    }
                    Text(
                        text = gridHeadline,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isBlackout || isStarving || isDeficitShutdown) HazardRed else SurplusGreen,
                        fontWeight = FontWeight.Bold
                    )
                    val autoRepairText = if (prod.needsAutomatedSystemRepair) {
                        "-${prod.dailyRoomScrapUpkeep.toInt()}S Room Ops/Repair"
                    } else {
                        "0S Room Ops"
                    }
                    val manualGateSuffix = when {
                        !prod.hasWaterPurifierRoom && !prod.hasHydroponicsRoom -> " • Water & Food: Manual Only (Need Purifier & Hydroponics)"
                        !prod.hasWaterPurifierRoom -> " • Water: Manual Only (Need Water Purifier)"
                        !prod.hasHydroponicsRoom -> " • Food: Manual Only (Need Hydroponics)"
                        else -> ""
                    }
                    Text(
                        text = "Vault Lv.${state.commandLevel} + $builtRoomsCount Rooms Drain -${String.format(Locale.US, "%.1f", prod.totalVaultResourceDrainPerSec)}/s vs Gain +${String.format(Locale.US, "%.1f", prod.totalGrossResourceGainPerSec)}/s • Daily: -${prod.dailyFoodUpkeepPerDay.toInt()}F, -${(prod.dailyWaterUpkeepPerDay + prod.dailyRoomWaterUpkeep).toInt()}W, $autoRepairText, -${prod.dailyRoomFuelUpkeep.toInt()}Fuel$manualGateSuffix",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                }

                Surface(
                    color = (if (state.needsDefenseRepair || isDeficitShutdown) WarningOrange else RadTeal).copy(alpha = 0.18f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, if (state.needsDefenseRepair || isDeficitShutdown) WarningOrange else RadTeal)
                ) {
                    Text(
                        text = if (isDeficitShutdown) "0 DEF!" else "DOOR ${state.defenseIntegrityPercent}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (state.needsDefenseRepair || isDeficitShutdown) WarningOrange else RadTeal,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Clear Positive vs Deficit Net Resource Balance Banner
            val isMakingPositive = prod.isMakingPositiveResources
            val netSign = if (prod.netResourceBalancePerSec >= 0.0) "+" else ""
            Surface(
                color = (if (isMakingPositive) SurplusGreen else HazardRed).copy(alpha = 0.16f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (isMakingPositive) SurplusGreen else HazardRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vault_resource_balance_banner")
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = if (isMakingPositive) {
                            "✅ MAKING POSITIVE RESOURCES: $netSign${String.format(Locale.US, "%.2f", prod.netResourceBalancePerSec)}/s Net Surplus (Making +${String.format(Locale.US, "%.2f", prod.totalGrossResourceGainPerSec)}/s vs Taking -${String.format(Locale.US, "%.2f", prod.totalVaultResourceDrainPerSec)}/s)"
                        } else {
                            "🚨 TAKING MORE THAN YOU ARE MAKING: ${String.format(Locale.US, "%.2f", prod.netResourceBalancePerSec)}/s Net Deficit (Making +${String.format(Locale.US, "%.2f", prod.totalGrossResourceGainPerSec)}/s vs Taking -${String.format(Locale.US, "%.2f", prod.totalVaultResourceDrainPerSec)}/s)"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isMakingPositive) SurplusGreen else HazardRed,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Reactor Fuel Burn: -${String.format(Locale.US, "%.2f", prod.reactorFuelUpkeepPerSec)}/s Bio-Fuel • Max Storage Cap: ${prod.storageCap.toInt()} (+${prod.storeRoomStorageBonus.toInt()} from Store Room)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary
                    )
                }
            }

            // Hardcore Apocalypse Survival Clocks Readout (6-Day Blackout, 2-Day Deficit, 10-Day Mega-Siege, 60s/2-Min Dweller Feeding)
            Surface(
                color = BunkerCharcoal,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, PanelBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "⏱ DOOMSDAY CLOCKS: 6D Power Outage (${state.blackoutDaysCount}/6D) • 2D Resource Deficit (${state.deficitDaysCount}/2D) • Next 10D Mega-Siege: ${prod.daysUntilDay10Siege}D (${prod.day10RequiredDefense} DEF)",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isBlackout || isDeficitShutdown) HazardRed else SolarAmber,
                        fontWeight = FontWeight.Bold
                    )
                    val appetitePct = ((prod.dwellerUpkeepEscalationMultiplier - 1.0) * 100).roundToInt()
                    Text(
                        text = "🍖 60S DWELLER FEEDING: ${state.dwellerFeedCycleSecondsRemaining}s left (-${prod.fiveMinDwellerFoodCost.roundToInt()} Food & -${prod.fiveMinDwellerWaterCost.roundToInt()} Water every 60s) • 2-Min Appetite Tier ${prod.dwellerUpkeepEscalationTier} (+$appetitePct% Drain)",
                        style = MaterialTheme.typography.labelSmall,
                        color = CryoCyan
                    )
                    if (state.dayCounter <= 10) {
                        Text(
                            text = if (prod.isEarlyBunkerGraceActive) {
                                "🛡 EARLY BUNKER GRACE (DAYS 1-3): Resource Deficit Game Over paused while bootstrapping (<2 Rooms) & -45% Early Upkeep!"
                            } else {
                                "🛡 DAYS 1-10 EARLY SURVIVAL GRACE: -45% Early Room Upkeep & softer +8%/2m Dweller Appetite curve active!"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = SurplusGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Vault Operational Posture Switcher (ECO_CONSERVATION / STANDARD / OVERCLOCK)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "VAULT OPERATIONAL POSTURE: ${activePosture.title.uppercase()} (Switch to ECO to cut -40% Upkeep & exit Resource Deficit!)",
                    style = MaterialTheme.typography.labelSmall,
                    color = activePosture.accentColor,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    VaultOperationalPosture.entries.forEach { posture ->
                        val selected = activePosture == posture
                        Surface(
                            color = if (selected) posture.accentColor.copy(alpha = 0.24f) else BunkerCharcoal,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) posture.accentColor else PanelBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onSetOperationalPosture(posture)
                                }
                                .testTag("posture_button_${posture.id.lowercase()}")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = posture.shortLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) posture.accentColor else TextSecondary,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            if (isDeficitShutdown || isBlackout || state.gold >= 25.0) {
                OutlinedButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCallEmergencyReliefDrop()
                    },
                    enabled = state.gold >= 25.0,
                    border = BorderStroke(1.dp, if (state.gold >= 25.0) XpGold else PanelBorder),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("call_emergency_airdrop_button")
                ) {
                    Text(
                        text = "🚁 CALL EMERGENCY AIR-DROP (25 GOLD -> +45S, +50F, +50W, +40 FUEL & -2 APPETITE)",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (state.gold >= 25.0) XpGold else TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isStarving || (state.totalSurvivors > 0 && (state.rations < prod.dailyFoodUpkeepPerDay || state.water < prod.dailyWaterUpkeepPerDay))) {
                Button(
                    onClick = onIssueEmergencyRations,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurplusGreen,
                        contentColor = BunkerCharcoal
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("issue_emergency_rations_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DISTRIBUTE EMERGENCY WORKER RATIONS (+14 FOOD & +14 WATER)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Row 1 of Solo Broken Vault Quick-Fix Grid: Crank Dynamo & Tune Generator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCrankDynamo,
                    enabled = state.manualDynamoCranksRemaining > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBlackout) WarningOrange else CryoCyan,
                        contentColor = BunkerCharcoal,
                        disabledContainerColor = BunkerCharcoal,
                        disabledContentColor = TextMuted
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("emergency_dynamo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (state.manualDynamoCranksRemaining > 0) {
                            "CRANK (${state.manualDynamoCranksRemaining}/4 • +16 MW)"
                        } else {
                            "0/4 CRANKS LEFT"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onMaintainGenerators,
                    border = BorderStroke(1.dp, if (state.generatorWearPercent > 0) SolarAmber else RadTeal),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("maintain_generator_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Handyman,
                        contentDescription = null,
                        tint = if (state.generatorWearPercent > 0) SolarAmber else RadTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (state.generatorWearPercent > 0) {
                            "TUNE GEN (${state.generatorWearPercent}% WEAR)"
                        } else {
                            "GEN TUNED (0% WEAR)"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (state.generatorWearPercent > 0) SolarAmber else RadTeal,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Row 2 of Solo Broken Vault Quick-Fix Grid: Scrub O2, Flush Rads & Next Day
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onScrubAir,
                    border = BorderStroke(1.dp, CryoCyan),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("scrub_air_button")
                ) {
                    Text(
                        text = "SCRUB O2 (${state.oxygenPercent}%)",
                        style = MaterialTheme.typography.labelSmall,
                        color = CryoCyan,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onFlushRadiation,
                    border = BorderStroke(1.dp, if (state.radiationMillisieverts > 20) WarningOrange else SurplusGreen),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("flush_rad_button")
                ) {
                    Text(
                        text = "DECON (${state.radiationMillisieverts}mSv)",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (state.radiationMillisieverts > 20) WarningOrange else SurplusGreen,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onAdvanceDay,
                    border = BorderStroke(1.dp, SolarAmber),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("next_day_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = null,
                        tint = SolarAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "DAY ${state.dayCounter + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SolarAmber,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun VaultPowerGeneratorsSectionCard(
    uiState: OutpostUiState,
    onUpgradePowerStation: (PowerStationType) -> Unit
) {
    val state = uiState.state
    val prod = uiState.production

    Surface(
        color = SteelPanel,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            if (prod.isVaultBlackout) HazardRed else CryoCyan.copy(alpha = 0.65f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vault_power_generators_card")
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "VAULT POWER GENERATORS (REQUIRED FOR VAULT TO RUN)",
                        style = MaterialTheme.typography.titleSmall,
                        color = CryoCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Every Vault room and powered turret requires MW. Build generators here to prevent blackouts!",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Surface(
                    color = (if (prod.isVaultBlackout) HazardRed else SurplusGreen).copy(alpha = 0.18f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${prod.totalMegawattGenerated.roundToInt()} / ${prod.totalMegawattDemand.roundToInt()} MW",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (prod.isVaultBlackout) HazardRed else SurplusGreen,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            PowerStationType.entries.forEach { station ->
                val level = when (station) {
                    PowerStationType.BIO_SLUDGE_TURBINE -> state.bioTurbineLevel
                    PowerStationType.SOLAR_HELIOSTAT_TOWER -> state.solarTowerLevel
                    PowerStationType.THORIUM_FISSION_REACTOR -> state.thoriumReactorLevel
                    PowerStationType.AETHER_RESONANCE_SUBSTATION -> state.aetherSubstationLevel
                }
                val cost = OutpostCalculator.computePowerStationUpgradeCost(station, state, uiState.skillsMap)

                Surface(
                    color = ElevatedSlate,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (cost.canAfford) station.accentColor.copy(alpha = 0.7f) else PanelBorder),
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
                                text = "${station.displayName} (Lv.$level • +${station.baseMegawattsPerLevel.toInt()} MW/Lv)",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = station.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            val costText = buildList {
                                add("${cost.scrapCost.toInt()} Scrap")
                                if (cost.goldCost > 0) add("${cost.goldCost.toInt()} Gold")
                                if (cost.thoriumCost > 0) add("${cost.thoriumCost.toInt()} Thorium")
                                if (cost.aetherCost > 0) add("${cost.aetherCost.toInt()} Aether")
                            }.joinToString(" • ")
                            Text(
                                text = "Cost: $costText",
                                style = MaterialTheme.typography.labelSmall,
                                color = XpGold
                            )
                            Text(
                                text = "🔥 Reactor Fuel Burn: -${cost.currentFuelUpkeepPerSec}/s -> Lv.${level + 1}: -${cost.nextLevelFuelUpkeepPerSec}/s Bio-Fuel",
                                style = MaterialTheme.typography.labelSmall,
                                color = WarningOrange,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { onUpgradePowerStation(station) },
                            enabled = cost.canAfford,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = station.accentColor,
                                contentColor = BunkerCharcoal,
                                disabledContainerColor = BunkerCharcoal,
                                disabledContentColor = TextMuted
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("upgrade_station_${station.id.lowercase()}")
                        ) {
                            Text(
                                text = if (level == 0) "BUILD" else "UPGRADE",
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

@Composable
private fun RareAnomalyCard(
    anomaly: RareAnomaly,
    onClaim: () -> Unit
) {
    Surface(
        color = BioluminescentViolet.copy(alpha = 0.16f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, BioluminescentViolet),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ANOMALY DETECTED: ${anomaly.title.uppercase()} (${anomaly.remainingSeconds}s)",
                    style = MaterialTheme.typography.titleSmall,
                    color = BioluminescentViolet,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${anomaly.subtitle} • +${anomaly.goldReward.toInt()} Gold, +${anomaly.mutagenCoresReward.toInt()} Cores & +${anomaly.xpReward.toInt()} XP",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onClaim,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BioluminescentViolet,
                    contentColor = BunkerCharcoal
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("claim_anomaly_button")
            ) {
                Text(
                    text = "HARVEST",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun WastelandSalvageFieldDeck(
    uiState: OutpostUiState,
    onHarvestNode: (Int) -> Unit,
    onProspectRichVein: () -> Unit,
    onUpgradeSalvageRig: () -> Unit
) {
    val state = uiState.state
    val prod = uiState.production
    val (rigScrapCost, rigFuelCost) = OutpostCalculator.computeSalvageRigUpgradeCost(state.salvageRigLevel)
    val canAffordRig = state.scrap >= rigScrapCost && state.bioFuel >= rigFuelCost
    val canProspect = state.bioFuel >= 12.0 && state.researchData >= 12.0
    val pickaxeTitle = PickaxeTier.nameForLevel(state.salvageRigLevel)

    Surface(
        color = SteelPanel,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, SolarAmber.copy(alpha = 0.75f)),
        modifier = Modifier.fillMaxWidth()
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${pickaxeTitle.uppercase()} (LV.${state.salvageRigLevel} • 0 SCRAP COST)",
                        style = MaterialTheme.typography.titleSmall,
                        color = SolarAmber,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Manual mining costs 0 Scrap! Swing your Pickaxe for free to gather Scrap, Gold, Water & Fuel while Scrap repairs automated rooms daily.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Surface(
                    color = XpGold.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, XpGold)
                ) {
                    Text(
                        text = String.format(Locale.US, "SWING %.2fx", prod.salvageComboMultiplier),
                        style = MaterialTheme.typography.labelSmall,
                        color = XpGold,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Primary Easy-Access "SWING PICKAXE" Hero Button
            Button(
                onClick = { onHarvestNode(0) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SolarAmber,
                    contentColor = BunkerCharcoal
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("scavenge_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Construction,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SWING PICKAXE (MINE GOLD, SMALL FUEL, SCRAP & XP)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (uiState.lastSalvageFeedback != null) {
                Surface(
                    color = XpGold.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, XpGold.copy(alpha = 0.55f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.lastSalvageFeedback,
                        style = MaterialTheme.typography.labelSmall,
                        color = XpGold,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // 3 Interactive Bedrock Mining Veins
            uiState.salvageNodes.forEach { slot ->
                val yieldPreview = OutpostCalculator.computeSalvageNodeYield(
                    nodeType = slot.nodeType,
                    state = state,
                    buildingsMap = uiState.buildingsMap,
                    workersMap = uiState.workersMap,
                    skillsMap = uiState.skillsMap,
                    activePhase = prod.activePhase
                )
                SalvageNodeRowCard(
                    slot = slot,
                    yieldPreview = yieldPreview,
                    onHarvest = { onHarvestNode(slot.slotIndex) }
                )
            }

            val isCappedBySmeltery = state.salvageRigLevel >= prod.maxPickaxeLevelAllowed
            Text(
                text = "Pickaxe Tier Cap: Lv.${state.salvageRigLevel}/${prod.maxPickaxeLevelAllowed} (Build/Upgrade Vault Ore & Gold Smeltery for +2 Max Pickaxe Lv)",
                style = MaterialTheme.typography.labelSmall,
                color = if (isCappedBySmeltery) WarningOrange else RadTeal
            )

            // Upgrade Personal Pickaxe & Prospect Rich Gold Veins Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onUpgradeSalvageRig,
                    enabled = canAffordRig && !isCappedBySmeltery,
                    border = BorderStroke(1.dp, if (canAffordRig && !isCappedBySmeltery) SolarAmber else PanelBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("upgrade_salvage_rig_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Handyman,
                        contentDescription = null,
                        tint = if (canAffordRig && !isCappedBySmeltery) SolarAmber else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val costLabel = if (rigFuelCost > 0) "0S + ${rigFuelCost.toInt()}F" else "FREE • 0S"
                    Text(
                        text = if (isCappedBySmeltery) {
                            "NEED SMELTERY (CAP LV.${prod.maxPickaxeLevelAllowed})"
                        } else {
                            "UPGRADE PICKAXE LV.${state.salvageRigLevel + 1} ($costLabel)"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (canAffordRig && !isCappedBySmeltery) SolarAmber else TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onProspectRichVein,
                    enabled = canProspect,
                    border = BorderStroke(1.dp, if (canProspect) BioluminescentViolet else PanelBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("prospect_veins_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = if (canProspect) BioluminescentViolet else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PROSPECT GOLD VEINS (12F+12T)",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (canProspect) BioluminescentViolet else TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SalvageNodeRowCard(
    slot: SalvageNodeSlot,
    yieldPreview: SalvageYieldPreview,
    onHarvest: () -> Unit
) {
    val node = slot.nodeType
    val progressFraction = (slot.currentProgress.toFloat() / slot.maxProgress.coerceAtLeast(1)).coerceIn(0f, 1f)

    Surface(
        color = ElevatedSlate,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, node.accentColor.copy(alpha = 0.7f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onHarvest)
            .testTag("salvage_node_${slot.slotIndex}")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = node.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = XpGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(5.dp)
                        ) {
                            Text(
                                text = "+${yieldPreview.goldAmount.toInt()} GOLD",
                                style = MaterialTheme.typography.labelSmall,
                                color = XpGold,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Vein Break: +${yieldPreview.primaryAmount.toInt()} ${node.primaryResource.shortLabel} • +${yieldPreview.secondaryAmount.toInt()} ${node.secondaryResource.shortLabel} • +${yieldPreview.bonusFuelFromGoldMine} FUEL • +${node.cacheDropCount} Crate",
                        style = MaterialTheme.typography.labelSmall,
                        color = node.accentColor
                    )
                    Text(
                        text = "Each Pickaxe Swing: +${yieldPreview.tapPrimaryAmount.toInt()} ${node.primaryResource.shortLabel}, +${yieldPreview.tapGoldAmount.toInt()} GOLD & +${yieldPreview.tapFuelAmount} FUEL",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Button(
                    onClick = onHarvest,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = node.accentColor,
                        contentColor = BunkerCharcoal
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("harvest_btn_${slot.slotIndex}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Construction,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "MINE (${slot.currentProgress}/${slot.maxProgress})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = node.accentColor,
                trackColor = BunkerCharcoal
            )
        }
    }
}

@Composable
private fun SalvageProcessingBenchCard(
    uiState: OutpostUiState,
    onDismantleOne: () -> Unit,
    onDismantleAll: () -> Unit,
    onSmeltSalvageForGold: () -> Unit
) {
    val state = uiState.state
    val cacheCount = state.rawSalvageCaches
    val scrapperCount = uiState.workersMap[WorkerClassType.SCRAPPER.id]?.assignedCount ?: 0
    val canSmeltForGold = state.scrap >= 18.0 && state.water >= 10.0 && state.bioFuel >= 6.0

    Surface(
        color = SteelPanel,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (cacheCount > 0 || canSmeltForGold) XpGold.copy(alpha = 0.75f) else PanelBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("salvage_processing_bench")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = XpGold,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "ORE CRUSHER & GOLD SMELTER • $cacheCount CRATE(S)",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (scrapperCount > 0) {
                            "Each Ore Crate yields +10+ Gold, Scrap, Water, Food, Fuel & Tech. Assigned Vault Miners ($scrapperCount) also auto-process!"
                        } else {
                            "Crack open Mined Ore Crates or smelt resources below for Gold to hire Survivors!"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDismantleOne,
                    enabled = cacheCount > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RadTeal,
                        contentColor = BunkerCharcoal,
                        disabledContainerColor = ElevatedSlate,
                        disabledContentColor = TextMuted
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("dismantle_cache_button")
                ) {
                    Text(
                        text = "CRACK 1 CRATE (+GOLD)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onDismantleAll,
                    enabled = cacheCount > 1,
                    border = BorderStroke(1.dp, if (cacheCount > 1) XpGold else PanelBorder),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("dismantle_all_caches_button")
                ) {
                    Text(
                        text = "CRACK ALL ($cacheCount)",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (cacheCount > 1) XpGold else TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onSmeltSalvageForGold,
                enabled = canSmeltForGold,
                colors = ButtonDefaults.buttonColors(
                    containerColor = XpGold,
                    contentColor = BunkerCharcoal,
                    disabledContainerColor = ElevatedSlate,
                    disabledContentColor = TextMuted
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("smelt_gold_button")
            ) {
                Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SMELT ORE -> +28+ GOLD (18 SCRAP + 10 WATER + 6 FUEL)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun BuildingStructureCard(
    type: BuildingType,
    level: Int,
    cost: BuildingUpgradeCost,
    assignedSynergyWorkers: Int,
    isVaultBlackout: Boolean,
    activeConstruction: ActiveBuildingConstruction?,
    onUpgrade: () -> Unit
) {
    val icon: ImageVector = when (type) {
        BuildingType.SALVAGE_YARD -> Icons.Default.Build
        BuildingType.ATMOSPHERIC_CONDENSER -> Icons.Default.WaterDrop
        BuildingType.HYDROPONIC_GREENHOUSE -> Icons.Default.Factory
        BuildingType.SURVIVOR_HAB_BLOCK -> Icons.Default.Apartment
        BuildingType.REINFORCED_SILO -> Icons.Default.Shield
        BuildingType.BIO_SLUDGE_REFINERY -> Icons.Default.Bolt
        BuildingType.SIGNAL_RESEARCH_ARRAY -> Icons.Default.Radar
        BuildingType.NOCTURNAL_EXTRACTION_RIG -> Icons.Default.Science
        BuildingType.GEOTHERMAL_TAP -> Icons.Default.Bolt
        BuildingType.CRYO_STASIS_VAULT -> Icons.Default.Shield
        BuildingType.VAULT_STORE_ROOM -> Icons.Default.Inventory2
    }
    val isConstructingThis = activeConstruction?.buildingType == type
    val isAnotherConstructing = activeConstruction != null && !isConstructingThis
    var showUpgradeConfirm by remember { mutableStateOf(false) }

    Surface(
        color = SteelPanel,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (isConstructingThis) 1.5.dp else 1.dp,
            color = when {
                isConstructingThis -> SolarAmber
                !cost.meetsLevelRequirement -> PanelBorder.copy(alpha = 0.5f)
                cost.canAfford -> type.primaryResource.color.copy(alpha = 0.65f)
                else -> PanelBorder
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("building_card_${type.id.lowercase()}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(type.primaryResource.color.copy(alpha = 0.16f))
                            .border(1.dp, type.primaryResource.color.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (cost.meetsLevelRequirement) icon else Icons.Default.Lock,
                            contentDescription = type.displayName,
                            tint = if (cost.meetsLevelRequirement) type.primaryResource.color else TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Room #${cost.roomNumber}: ${type.displayName}",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                color = if (level == 0) HazardRed.copy(alpha = 0.18f) else ElevatedSlate,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, if (level == 0) WarningOrange else SolarAmber.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = if (level == 0) "BROKEN • ⏱ ${cost.buildTimeSeconds}s" else "LV.$level • ⏱ ${cost.buildTimeSeconds}s",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (level == 0) WarningOrange else SolarAmber,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "⏱ Build Time (Lv.${cost.targetRoomLevel}): ${cost.buildTimeSeconds}s -> Next Lv.${cost.targetRoomLevel + 1}: ${cost.nextLevelBuildTimeSeconds}s",
                            style = MaterialTheme.typography.labelSmall,
                            color = SolarAmber,
                            fontWeight = FontWeight.SemiBold
                        )
                        val powerTag = if (type.megawattDemandPerLevel > 0) {
                            "Power Draw: ${cost.currentMegawattDraw} MW -> ${cost.nextLevelMegawattDraw} MW (+${cost.additionalMegawattDraw} MW at Lv.${level + 1})"
                        } else {
                            "Backup Generator: +8 MW/Lv Power & -10%/Lv Repair Cost"
                        }
                        Text(
                            text = powerTag,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (cost.willExceedPowerGrid) WarningOrange else CryoCyan,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "📦 Resource Cap Bonus: +${cost.storageCapBonusCurrent.toInt()} -> +${cost.storageCapBonusNextLevel.toInt()} (+${cost.additionalStorageCapGain.toInt()} Resource Cap at Lv.${level + 1})",
                            style = MaterialTheme.typography.labelSmall,
                            color = SurplusGreen,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Daily Room Maintenance (Lv.$level -> Lv.${level + 1}, +6%/Day): -${cost.currentRoomUpkeep.dailyScrap.toInt()}S/-${cost.currentRoomUpkeep.dailyWater.toInt()}W/-${cost.currentRoomUpkeep.dailyFuel.toInt()}Fuel -> -${cost.nextLevelRoomUpkeep.dailyScrap.toInt()}S/-${cost.nextLevelRoomUpkeep.dailyWater.toInt()}W/-${cost.nextLevelRoomUpkeep.dailyFuel.toInt()}Fuel/Day",
                            style = MaterialTheme.typography.labelSmall,
                            color = WarningOrange,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (cost.exceedsCurrentStorageCap) {
                            Text(
                                text = "📦 STORAGE CAP GATED: Upgrade other rooms or Vault Store Room to raise Resource Cap for this Lv.${level + 1} upgrade!",
                                style = MaterialTheme.typography.labelSmall,
                                color = SolarAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = type.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                color = type.primaryResource.color.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, type.primaryResource.color.copy(alpha = 0.40f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = type.gameplayNeedSummary,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Pickaxe & Vault Bonus: ${type.manualSalvageBonusDesc}",
                    style = MaterialTheme.typography.labelSmall,
                    color = XpGold
                )
                if (type == BuildingType.SALVAGE_YARD) {
                    val curSmallFuel = if (level > 0) String.format(Locale.US, "%.2f", 0.06 + level * 0.07) else "0.00"
                    val nextSmallFuel = String.format(Locale.US, "%.2f", 0.06 + (level + 1) * 0.07)
                    val curTapFuel = String.format(Locale.US, "%.2f", 0.35 + level * 0.25)
                    val nextTapFuel = String.format(Locale.US, "%.2f", 0.35 + (level + 1) * 0.25)
                    Text(
                        text = "⛽ Gold Mine Fuel & Gold Trickle: Lv.$level (+$curSmallFuel Fuel/s & +$curTapFuel Fuel/Swing) -> Lv.${level + 1} (+$nextSmallFuel Fuel/s, +$nextTapFuel Fuel/Swing & +25% Gold)",
                        style = MaterialTheme.typography.labelSmall,
                        color = SurplusGreen,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (cost.willExceedPowerGrid) {
                    Text(
                        text = "⚡ HIGH-LEVEL POWER WARNING: +${cost.additionalMegawattDraw} MW draw will exceed current Vault Power Grid! Build/upgrade a Power Generator.",
                        style = MaterialTheme.typography.labelSmall,
                        color = WarningOrange,
                        fontWeight = FontWeight.Bold
                    )
                }

                val favored = type.favoredWorkerClass
                if (favored != null) {
                    val speedPct = (OutpostCalculator.computeRoomLevelOutputMultiplier(level) * 100).roundToInt()
                    val nextSpeedPct = (OutpostCalculator.computeRoomLevelOutputMultiplier(level + 1) * 100).roundToInt()
                    val speedNote = when {
                        level <= 0 -> "Lv.0 Worst Makeshift State ($speedPct% Speed -> $nextSpeedPct% at Lv.1; +18% each level!)"
                        level <= 3 -> "Gradual Room Scaling ($speedPct% Speed -> $nextSpeedPct% at Lv.${level + 1}; +18% better each level!)"
                        else -> "High-Level Overdrive ($speedPct% Speed -> $nextSpeedPct% at Lv.${level + 1})"
                    }
                    if (isVaultBlackout && level > 0 && type != BuildingType.BIO_SLUDGE_REFINERY) {
                        Text(
                            text = "VAULT POWER BLACKOUT — Room Offline (0.0/s) until Power MW is restored!",
                            style = MaterialTheme.typography.labelSmall,
                            color = HazardRed,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (assignedSynergyWorkers > 0 && level > 0) {
                        Text(
                            text = "Staffed by $assignedSynergyWorkers ${favored.displayName}(s) • $speedNote",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (level <= 2) SolarAmber else SurplusGreen
                        )
                    } else if (level > 0) {
                        Text(
                            text = "Automated at 35% baseline ($speedNote) • Assign ${favored.displayName}s for full output!",
                            style = MaterialTheme.typography.labelSmall,
                            color = RadTeal
                        )
                    } else {
                        Text(
                            text = "Lv.0 Worst Makeshift Room ($speedNote) • Upgrade gradually to improve yield!",
                            style = MaterialTheme.typography.labelSmall,
                            color = WarningOrange
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (!cost.meetsLevelRequirement) {
                        Surface(
                            color = HazardRed.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, HazardRed.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "Requires Command Lv. ${type.requiredCommandLevel}",
                                style = MaterialTheme.typography.labelSmall,
                                color = HazardRed,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        CostPill("SCRAP", cost.scrapCost, SolarAmber)
                        if (cost.waterCost > 0) CostPill("WATER", cost.waterCost, CryoCyan)
                        if (cost.fuelCost > 0) CostPill("FUEL", cost.fuelCost, RustCopper)
                        if (cost.coresCost > 0) CostPill("CORES", cost.coresCost, BioluminescentViolet)
                    }
                }

                Button(
                    onClick = {
                        if (cost.willExceedPowerGrid || level >= 2) {
                            showUpgradeConfirm = true
                        } else {
                            onUpgrade()
                        }
                    },
                    enabled = cost.canAfford && !isConstructingThis && !isAnotherConstructing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (level == 0) RadTeal else SolarAmber,
                        contentColor = BunkerCharcoal,
                        disabledContainerColor = ElevatedSlate,
                        disabledContentColor = TextMuted
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("upgrade_building_${type.id.lowercase()}")
                ) {
                    Text(
                        text = when {
                            isConstructingThis -> "BUILDING (${activeConstruction?.remainingSeconds ?: 0}s)"
                            level == 0 -> "REPAIR (${cost.buildTimeSeconds}s)"
                            else -> "UPGRADE (${cost.buildTimeSeconds}s)"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showUpgradeConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showUpgradeConfirm = false },
            containerColor = SteelPanel,
            title = {
                Text(
                    text = "CONFIRM: UPGRADE ${type.displayName.uppercase()} (LV.${level + 1})",
                    style = MaterialTheme.typography.titleMedium,
                    color = SolarAmber,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Think before you upgrade! Ensure your Vault resources and power grid can cover ongoing operating costs:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                    Surface(
                        color = BunkerCharcoal,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "• Upfront Cost: ${cost.scrapCost.toInt()} Scrap" +
                                    (if (cost.waterCost > 0) ", ${cost.waterCost.toInt()} Water" else "") +
                                    (if (cost.fuelCost > 0) ", ${cost.fuelCost.toInt()} Fuel" else ""),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextPrimary
                            )
                            Text(
                                text = "• Power Grid Demand: +${cost.additionalMegawattDraw} MW" +
                                    (if (cost.willExceedPowerGrid) " ⚠ EXCEEDS CURRENT GRID! (Will trigger Blackout)" else " (Grid Safe)"),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (cost.willExceedPowerGrid) HazardRed else SurplusGreen,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "• Upkeep Drain: -${cost.nextLevelRoomUpkeep.dailyScrap.toInt()}S/-${cost.nextLevelRoomUpkeep.dailyWater.toInt()}W/-${cost.nextLevelRoomUpkeep.dailyFuel.toInt()}Fuel per day",
                                style = MaterialTheme.typography.labelSmall,
                                color = WarningOrange
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUpgradeConfirm = false
                        onUpgrade()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (cost.willExceedPowerGrid) HazardRed else SolarAmber,
                        contentColor = BunkerCharcoal
                    )
                ) {
                    Text("AUTHORIZE UPGRADE", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showUpgradeConfirm = false }) {
                    Text("CANCEL", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun CostPill(
    label: String,
    amount: Double,
    color: Color
) {
    Surface(
        color = ElevatedSlate,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.45f))
    ) {
        Text(
            text = "${formatCompact(amount)} $label",
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun SubterraneanDeepCoreDrillingCard(
    uiState: OutpostUiState,
    onExecuteDeepCoreDrill: () -> Unit,
    onStrikeSubterraneanBoss: () -> Unit
) {
    val state = uiState.state
    val snap = uiState.production
    val depth = state.deepCoreDepthMeters
    val relics = state.discoveredRelicsCount
    val activeBoss = snap.activeSubterraneanBoss
    val nextBoss = SubterraneanBossType.nextBossForDepth(depth)
    val accent = if (activeBoss != null && state.subterraneanBossHpPercent > 0) HazardRed else BioluminescentViolet

    Surface(
        color = SteelPanel,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, accent.copy(alpha = 0.8f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("subterranean_deep_core_drilling_card")
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
                        contentDescription = "Subterranean Deep-Core Drilling",
                        tint = accent,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "SUBTERRANEAN DEEP-CORE BOREHOLE & RELIC VAULTS",
                            style = MaterialTheme.typography.titleSmall,
                            color = accent,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Drill deep into the crust for Pre-Fall Relics (+1.5% Out, +2 DEF, +0.3 MW, +15 Cap & -1% Repair/Relic) & fight Abyssal Bosses!",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
                Surface(
                    color = accent.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "${depth}m DEEP",
                        style = MaterialTheme.typography.labelSmall,
                        color = accent,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = BunkerCharcoal,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BioluminescentViolet.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("PRE-FALL RELICS (SMALL OUTPOST BONUSES)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text(
                            text = "$relics RELICS (+${String.format(Locale.US, "%.1f", relics * 1.5)}% OUT • +${relics * 2} DEF • +${String.format(Locale.US, "%.1f", relics * 0.3)} MW • +${relics * 15} CAP)",
                            style = MaterialTheme.typography.labelMedium,
                            color = BioluminescentViolet,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Surface(
                    color = BunkerCharcoal,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, SolarAmber.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("NEXT BOSS LAYER", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text(
                            text = nextBoss?.let { "${it.depthThresholdMeters}m: ${it.title}" } ?: "ALL ABYSS LAYERS BREACHED",
                            style = MaterialTheme.typography.labelMedium,
                            color = SolarAmber,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }

            if (activeBoss != null && state.subterraneanBossHpPercent > 0) {
                Surface(
                    color = HazardRed.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.5.dp, HazardRed),
                    modifier = Modifier.fillMaxWidth()
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
                                text = "⚠ BOSS AWAKENED: ${activeBoss.title.uppercase()}",
                                style = MaterialTheme.typography.labelMedium,
                                color = HazardRed,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "HP: ${state.subterraneanBossHpPercent}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = XpGold,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Text(
                            text = "${activeBoss.hazardDescription} • Loot: +${activeBoss.relicRewardCount} Relic(s), +${activeBoss.mutagenCoresReward.toInt()} Cores & +${activeBoss.goldReward.toInt()} Gold",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary
                        )
                        LinearProgressIndicator(
                            progress = { (state.subterraneanBossHpPercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = HazardRed,
                            trackColor = BunkerCharcoal
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onExecuteDeepCoreDrill,
                    enabled = !state.activeCaveInHazard,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BioluminescentViolet,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("execute_deep_core_drill_btn")
                ) {
                    Text(
                        text = "DRILL DEEP CORE (+75m • 0 SCRAP • RELIC ROLL)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                if (activeBoss != null && state.subterraneanBossHpPercent > 0) {
                    Button(
                        onClick = onStrikeSubterraneanBoss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HazardRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("strike_subterranean_boss_btn")
                    ) {
                        Text(
                            text = "STRIKE BOSS (-6 AMMO)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}
