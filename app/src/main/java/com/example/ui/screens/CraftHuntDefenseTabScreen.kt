package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Phishing
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import com.example.domain.BlastDoorUpgradeCost
import com.example.domain.DefenseRepairCost
import com.example.domain.FishingPreview
import com.example.domain.OutpostCalculator
import com.example.model.ActiveDoorUpgrade
import com.example.model.ActiveItemCrafting
import com.example.model.CraftItemCategory
import com.example.model.CraftableItemType
import com.example.model.DayNightPhase
import com.example.model.FishingSpot
import com.example.model.HuntingTarget
import com.example.model.RaiderSiegeThreat
import com.example.model.SiegeCountermeasureType
import com.example.ui.components.formatRate
import com.example.ui.theme.BioluminescentViolet
import com.example.ui.theme.BunkerSlate
import com.example.ui.theme.CryoCyan
import com.example.ui.theme.ElevatedSteel
import com.example.ui.theme.HazardRed
import com.example.ui.theme.ObsidianCharcoal
import com.example.ui.theme.RadTeal
import com.example.ui.theme.RustCopper
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.SurplusGreen
import com.example.ui.theme.TextMutedAsh
import com.example.ui.theme.WarningOrange
import com.example.ui.theme.XpGold
import com.example.viewmodel.OutpostUiState

private enum class ArmorySubView(val label: String) {
    ALL("All"),
    SIEGE_AND_MEDBAY("Siege & Medbay"),
    ARMORY_CRAFT("Armory & Gear"),
    HUNT_AND_FISH("Hunt & Fish")
}

@Composable
fun CraftHuntDefenseTabScreen(
    uiState: OutpostUiState,
    onCraftItem: (CraftableItemType) -> Unit,
    onAccelerateCrafting: () -> Unit = {},
    onRushCrafting: () -> Unit = {},
    onExecuteHunt: (HuntingTarget) -> Unit,
    onExecuteFishing: (FishingSpot) -> Unit,
    onRepelRaid: () -> Unit,
    onExecuteCountermeasure: (SiegeCountermeasureType) -> Unit = {},
    onRepairDefenses: () -> Unit,
    onUpgradeBlastDoor: () -> Unit,
    onAccelerateDoorUpgrade: () -> Unit = {},
    onRushDoorUpgrade: () -> Unit = {},
    onToggleAutoDefense: (Boolean) -> Unit = {},
    onUseMedkit: () -> Unit = {},
    onUseRadAway: () -> Unit = {},
    onTreatHuntingInjuries: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedSubView by remember { mutableStateOf(ArmorySubView.ALL) }
    val state = uiState.state
    val prod = uiState.production
    val huntingItems = CraftableItemType.entries.filter { it.category == CraftItemCategory.HUNTING }
    val defenseItems = CraftableItemType.entries.filter { it.category == CraftItemCategory.DEFENSE }
    val repairCost = OutpostCalculator.computeDefenseRepairCost(state, uiState.skillsMap, uiState.buildingsMap)
    val blastDoorCost = OutpostCalculator.computeBlastDoorUpgradeCost(state, uiState.skillsMap, uiState.workersMap)

    val showSiegeAndMedbay = selectedSubView == ArmorySubView.ALL || selectedSubView == ArmorySubView.SIEGE_AND_MEDBAY
    val showArmoryCraft = selectedSubView == ArmorySubView.ALL || selectedSubView == ArmorySubView.ARMORY_CRAFT
    val showHuntAndFish = selectedSubView == ArmorySubView.ALL || selectedSubView == ArmorySubView.HUNT_AND_FISH

    Column(modifier = modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ArmorySubView.entries) { subView ->
                val isSelected = selectedSubView == subView
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedSubView = subView },
                    label = {
                        Text(
                            text = subView.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RadTeal.copy(alpha = 0.24f),
                        selectedLabelColor = RadTeal
                    )
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Active Blast Door Upgrade Progress Banner (visible when upgrading door!)
            uiState.activeDoorUpgrade?.let { activeDoor ->
                item(key = "active_door_upgrade_banner") {
                    ActiveBlastDoorUpgradeCard(
                        doorUpgrade = activeDoor,
                        canAffordRush = state.gold >= activeDoor.rushGoldCost,
                        onAccelerate = onAccelerateDoorUpgrade,
                        onRush = onRushDoorUpgrade
                    )
                }
            }

            // Active Armory Item Crafting Workbench Banner (visible when crafting gear!)
            uiState.activeCrafting?.let { activeCraft ->
                item(key = "active_armory_crafting_banner") {
                    ActiveArmoryCraftingCard(
                        crafting = activeCraft,
                        canAffordRush = state.gold >= activeCraft.rushGoldCost,
                        onAccelerate = onAccelerateCrafting,
                        onRush = onRushCrafting
                    )
                }
            }

            if (showSiegeAndMedbay) {
                // 1. Daily Base Defense, Day 10 Mega-Siege, Automated Defenses & Timed Blast Door Upgrade Card
                item(key = "defense_status_card") {
                    DefensePerimeterCard(
                        dayCounter = state.dayCounter,
                        totalDefense = prod.totalDefenseRating,
                        todayRequiredDefense = prod.todayRequiredDefense,
                        tomorrowRequiredDefense = prod.tomorrowRequiredDefense,
                        day10RequiredDefense = prod.day10RequiredDefense,
                        daysUntilDay10Siege = prod.daysUntilDay10Siege,
                        blastDoorLevel = state.blastDoorLevel,
                        defenseIntegrityPercent = state.defenseIntegrityPercent,
                        needsDefenseRepair = state.needsDefenseRepair,
                        repairCost = repairCost,
                        blastDoorCost = blastDoorCost,
                        activeDoorUpgrade = uiState.activeDoorUpgrade,
                        lastDamageFeedback = uiState.lastDefenseDamageFeedback,
                        minesCount = state.craftedShrapnelMines,
                        turretsCount = state.sentryTurretCount,
                        plasmaCount = state.plasmaCannonCount,
                        ammoRounds = state.ammoRounds,
                        scrapStock = state.scrap,
                        bioFuelStock = state.bioFuel,
                        raidsRepelled = state.totalRaidsRepelled,
                        luckyNoRepairCount = state.luckyNoRepairCount,
                        isVaultBlackout = prod.isVaultBlackout,
                        isDefenseShutDownByDeficit = prod.isDefenseShutDownByDeficit,
                        deficitDaysCount = state.deficitDaysCount,
                        totalDrainPerSec = prod.totalVaultResourceDrainPerSec,
                        totalGainPerSec = prod.totalGrossResourceGainPerSec,
                        autoDefenseEnabled = state.autoDefenseEnabled,
                        isAutoDefenseActive = prod.isAutoDefenseActive,
                        hasAssignedFedWorkers = state.totalSurvivors > 0 && prod.totalAssignedWorkers > 0 && !prod.isWorkersStarvingOrDehydrated,
                        activePhase = prod.activePhase,
                        activeRaid = uiState.activeRaidThreat,
                        onRepelRaid = onRepelRaid,
                        onExecuteCountermeasure = onExecuteCountermeasure,
                        onRepairDefenses = onRepairDefenses,
                        onUpgradeBlastDoor = onUpgradeBlastDoor,
                        onToggleAutoDefense = onToggleAutoDefense
                    )
                }

                // 1B. Realistic Medbay, Decontamination Showers & Hazmat Gear Card
                item(key = "medbay_and_decon_card") {
                    MedbayAndDeconCard(
                        healthPercent = state.dwellerHealthPercent,
                        radiationMsv = state.radiationMillisieverts,
                        oxygenPercent = state.oxygenPercent,
                        injuredDwellers = state.injuredDwellersCount,
                        medkitsCount = state.medkitsCount,
                        radAwayCount = state.radAwayCount,
                        gasMasksCount = state.gasMasksCount,
                        hazmatSuitsCount = state.hazmatSuitsCount,
                        ammoRounds = state.ammoRounds,
                        onUseMedkit = onUseMedkit,
                        onUseRadAway = onUseRadAway
                    )
                }
            }

            if (showHuntAndFish) {
                item(key = "hardcore_hunting_trauma_item") {
                    HardcoreHuntingTraumaAndAmmoCard(
                        uiState = uiState,
                        onTreatHuntingInjuries = onTreatHuntingInjuries
                    )
                }

                // 2. Underground Crater Lake Fishing Header & Spots
                item(key = "crater_lake_fishing_header") {
                    FishingHeaderCard(totalFishCaught = state.totalFishCaught)
                }

                items(FishingSpot.entries, key = { "fish_${it.id}" }) { spot ->
                    val fishPreview = OutpostCalculator.computeFishingPreview(
                        spot = spot,
                        state = state,
                        skillsMap = uiState.skillsMap,
                        buildingsMap = uiState.buildingsMap
                    )
                    FishingSpotCard(
                        spot = spot,
                        preview = fishPreview,
                        onExecuteFishing = { onExecuteFishing(spot) }
                    )
                }

                // 3. Wasteland Beast Hunting Grounds Header & Targets
                item(key = "hunting_grounds_header") {
                    HuntingGearHeaderCard(
                        spears = state.craftedSpears,
                        snareTraps = state.craftedSnareTraps,
                        crossbowLevel = state.aetherCrossbowLevel,
                        passiveFoodPerSec = prod.passiveFoodFromTrapsPerSec,
                        huntsCompleted = state.totalHuntsCompleted
                    )
                }

                items(HuntingTarget.entries, key = { "hunt_${it.id}" }) { target ->
                    val preview = OutpostCalculator.computeHuntPreview(
                        target = target,
                        state = state,
                        workersMap = uiState.workersMap,
                        skillsMap = uiState.skillsMap,
                        activePhase = prod.activePhase,
                        buildingsMap = uiState.buildingsMap
                    )
                    HuntingTargetCard(
                        target = target,
                        foodReward = preview.foodReward,
                        waterReward = preview.waterReward,
                        goldReward = preview.goldReward,
                        coresReward = preview.coresReward,
                        xpReward = preview.xpReward,
                        canExecute = preview.canExecute,
                        crossbowUnlocked = state.aetherCrossbowLevel >= 1,
                        counterAttackInjuryChancePercent = preview.counterAttackInjuryChancePercent,
                        ammoRecommended = preview.ammoRecommended,
                        ammoAvailable = state.ammoRounds,
                        onExecuteHunt = { onExecuteHunt(target) }
                    )
                }
            }

            if (showArmoryCraft) {
                // 4. Craft Base Defenses, Munitions & Medical Supplies
                item(key = "craft_defense_section") {
                    SectionDividerTitle(
                        title = "CRAFT DEFENSES, AMMO CRATES & MEDICAL PACKS",
                        subtitle = "Forge Ballistic Ammo Crates (+30 Rds), Field Medkits + RadAway, Barricade Mines & Powered Turrets"
                    )
                }

                items(defenseItems, key = { "craft_${it.id}" }) { item ->
                    CraftableItemCard(
                        item = item,
                        uiState = uiState,
                        onCraft = { onCraftItem(item) }
                    )
                }

                // 5. Craft Hunting, Fishing & Hazmat Expedition Gear
                item(key = "craft_hunting_section") {
                    SectionDividerTitle(
                        title = "CRAFT HAZMAT SUITS, GAS MASKS & HUNTING GEAR",
                        subtitle = "Forge Gas Masks (-3% Expedition Death Risk), Lead-Lined Hazmat Suits (-4% Risk), Spears & Traps"
                    )
                }

                items(huntingItems, key = { "craft_${it.id}" }) { item ->
                    CraftableItemCard(
                        item = item,
                        uiState = uiState,
                        onCraft = { onCraftItem(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MedbayAndDeconCard(
    healthPercent: Int,
    radiationMsv: Int,
    oxygenPercent: Int,
    injuredDwellers: Int,
    medkitsCount: Int,
    radAwayCount: Int,
    gasMasksCount: Int,
    hazmatSuitsCount: Int,
    ammoRounds: Int,
    onUseMedkit: () -> Unit,
    onUseRadAway: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("medbay_decon_card"),
        shape = RoundedCornerShape(14.dp),
        color = ElevatedSteel
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (radiationMsv >= 80 || healthPercent < 70) WarningOrange else SurplusGreen.copy(alpha = 0.6f),
                    RoundedCornerShape(14.dp)
                )
                .padding(12.dp),
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
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = "Medbay & Decontamination",
                        tint = SurplusGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "MEDBAY, DECON SHOWERS & SURVIVAL GEAR",
                            style = MaterialTheme.typography.titleSmall,
                            color = SurplusGreen,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Health: $healthPercent% • Rads: $radiationMsv mSv • O2: $oxygenPercent% • Injured: $injuredDwellers",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMutedAsh
                        )
                    }
                }
            }

            Text(
                text = "Stockpile: $medkitsCount Medkit(s) • $radAwayCount RadAway • $gasMasksCount Gas Mask(s) • $hazmatSuitsCount Hazmat Suit(s) • $ammoRounds Ammo Rds",
                style = MaterialTheme.typography.labelSmall,
                color = XpGold,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onUseMedkit,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurplusGreen,
                        contentColor = ObsidianCharcoal
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("use_medkit_button")
                ) {
                    Text(
                        text = if (medkitsCount > 0) "USE MEDKIT ($medkitsCount)" else "FIRST AID (+15% HP)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onUseRadAway,
                    border = BorderStroke(1.dp, RadTeal),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("use_radaway_button")
                ) {
                    Text(
                        text = if (radAwayCount > 0) "USE RADAWAY ($radAwayCount)" else "DECON SHOWER (-25mSv)",
                        style = MaterialTheme.typography.labelSmall,
                        color = RadTeal,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveBlastDoorUpgradeCard(
    doorUpgrade: ActiveDoorUpgrade,
    canAffordRush: Boolean,
    onAccelerate: () -> Unit,
    onRush: () -> Unit
) {
    val progress = ((doorUpgrade.totalSeconds - doorUpgrade.remainingSeconds).toFloat() /
        doorUpgrade.totalSeconds.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

    Surface(
        color = RadTeal.copy(alpha = 0.16f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, RadTeal),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_door_upgrade_card")
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
                        text = "REINFORCING VAULT BLAST DOOR -> LV.${doorUpgrade.targetLevel}",
                        style = MaterialTheme.typography.titleSmall,
                        color = RadTeal,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Door Lv.${doorUpgrade.targetLevel} Upgrade Timer: ${doorUpgrade.remainingSeconds}s / ${doorUpgrade.totalSeconds}s (+20 DEF & 100% Integrity on finish)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    color = ObsidianCharcoal,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, RadTeal)
                ) {
                    Text(
                        text = "${doorUpgrade.remainingSeconds}s",
                        style = MaterialTheme.typography.labelMedium,
                        color = RadTeal,
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
                color = RadTeal,
                trackColor = ObsidianCharcoal
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAccelerate,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RadTeal,
                        contentColor = ObsidianCharcoal
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("accelerate_door_upgrade_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WELD DOOR (-4s)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onRush,
                    enabled = canAffordRush,
                    border = BorderStroke(1.dp, if (canAffordRush) XpGold else TextMutedAsh),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("rush_door_upgrade_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = if (canAffordRush) XpGold else TextMutedAsh,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "RUSH (${doorUpgrade.rushGoldCost.toInt()} GOLD)",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (canAffordRush) XpGold else TextMutedAsh,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveArmoryCraftingCard(
    crafting: ActiveItemCrafting,
    canAffordRush: Boolean,
    onAccelerate: () -> Unit,
    onRush: () -> Unit
) {
    val progress = ((crafting.totalSeconds - crafting.remainingSeconds).toFloat() /
        crafting.totalSeconds.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

    Surface(
        color = crafting.item.accentColor.copy(alpha = 0.16f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, crafting.item.accentColor),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_crafting_card")
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
                        text = "ARMORY WORKBENCH FORGING: ${crafting.item.title.uppercase()}",
                        style = MaterialTheme.typography.titleSmall,
                        color = crafting.item.accentColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Craft Timer: ${crafting.remainingSeconds}s / ${crafting.totalSeconds}s • ${crafting.item.effectSummary}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    color = ObsidianCharcoal,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, crafting.item.accentColor)
                ) {
                    Text(
                        text = "${crafting.remainingSeconds}s",
                        style = MaterialTheme.typography.labelMedium,
                        color = crafting.item.accentColor,
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
                color = crafting.item.accentColor,
                trackColor = ObsidianCharcoal
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAccelerate,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = crafting.item.accentColor,
                        contentColor = ObsidianCharcoal
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("accelerate_crafting_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "FORGE FASTER (-3s)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onRush,
                    enabled = canAffordRush,
                    border = BorderStroke(1.dp, if (canAffordRush) XpGold else TextMutedAsh),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("rush_crafting_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = if (canAffordRush) XpGold else TextMutedAsh,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "RUSH (${crafting.rushGoldCost.toInt()} GOLD)",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (canAffordRush) XpGold else TextMutedAsh,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DefensePerimeterCard(
    dayCounter: Int = 1,
    totalDefense: Int,
    todayRequiredDefense: Int,
    tomorrowRequiredDefense: Int = 28,
    day10RequiredDefense: Int,
    daysUntilDay10Siege: Int,
    blastDoorLevel: Int,
    defenseIntegrityPercent: Int,
    needsDefenseRepair: Boolean,
    repairCost: DefenseRepairCost,
    blastDoorCost: BlastDoorUpgradeCost,
    activeDoorUpgrade: ActiveDoorUpgrade? = null,
    lastDamageFeedback: String?,
    minesCount: Int,
    turretsCount: Int,
    plasmaCount: Int,
    ammoRounds: Int = 20,
    scrapStock: Double = 0.0,
    bioFuelStock: Double = 0.0,
    raidsRepelled: Int,
    luckyNoRepairCount: Int,
    isVaultBlackout: Boolean,
    isDefenseShutDownByDeficit: Boolean = false,
    deficitDaysCount: Int = 0,
    totalDrainPerSec: Double = 0.0,
    totalGainPerSec: Double = 0.0,
    autoDefenseEnabled: Boolean = true,
    isAutoDefenseActive: Boolean = false,
    hasAssignedFedWorkers: Boolean = false,
    activePhase: DayNightPhase = DayNightPhase.SCORCHING_DAY,
    activeRaid: RaiderSiegeThreat?,
    onRepelRaid: () -> Unit,
    onExecuteCountermeasure: (SiegeCountermeasureType) -> Unit = {},
    onRepairDefenses: () -> Unit,
    onUpgradeBlastDoor: () -> Unit,
    onToggleAutoDefense: (Boolean) -> Unit = {}
) {
    val integrityFraction = (defenseIntegrityPercent / 100f).coerceIn(0f, 1f)
    val hasBuiltAutoDefenses = turretsCount > 0 || plasmaCount > 0
    val hasGridPower = !isVaultBlackout

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = ElevatedSteel
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    when {
                        isDefenseShutDownByDeficit -> HazardRed
                        needsDefenseRepair -> WarningOrange
                        activeRaid != null -> HazardRed
                        else -> RadTeal.copy(alpha = 0.65f)
                    },
                    RoundedCornerShape(14.dp)
                )
                .padding(12.dp),
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
                        imageVector = Icons.Default.Security,
                        contentDescription = "Vault City Defense",
                        tint = RadTeal,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = "DAILY BASE DEFENSE, NIGHT SWARMS & AUTO-TURRETS",
                            style = MaterialTheme.typography.titleSmall,
                            color = RadTeal,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Door Lv.$blastDoorLevel • Mines: $minesCount • Turrets: $turretsCount • Plasma: $plasmaCount • Ammo: $ammoRounds Rds",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMutedAsh
                        )
                    }
                }
                Surface(
                    color = RadTeal.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.border(1.dp, RadTeal, RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = "$totalDefense / $todayRequiredDefense DEF",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (totalDefense >= todayRequiredDefense) SurplusGreen else WarningOrange,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (isDefenseShutDownByDeficit) {
                Surface(
                    color = HazardRed.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.5.dp, HazardRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("defense_deficit_shutdown_banner")
                ) {
                    Text(
                        text = "🚨 VAULT DEFENSES SHUT DOWN (0 DEF — RESOURCE DEFICIT ${deficitDaysCount}/2D): Vault takes -${formatRate(totalDrainPerSec)}/s to run vs +${formatRate(totalGainPerSec)}/s gained! Restore positive resource generation within ${(2 - deficitDaysCount).coerceAtLeast(1)} Day(s) or GAME OVER!",
                        style = MaterialTheme.typography.labelSmall,
                        color = HazardRed,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Escalating Daily Attack Difficulty Forecast Banner
            Surface(
                color = ObsidianCharcoal,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, WarningOrange.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "⚠️ ESCALATING WASTELAND SIEGES: Day $dayCounter Threat requires $todayRequiredDefense DEF -> Tomorrow (Day ${dayCounter + 1}) escalates to $tomorrowRequiredDefense DEF! Major Nuclear Warlord Mega-Sieges strike every 10 Days (Day 10, 20, 30...).",
                    style = MaterialTheme.typography.labelSmall,
                    color = WarningOrange,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            if (activePhase == DayNightPhase.RAD_NIGHT || activePhase == DayNightPhase.DUSK) {
                Surface(
                    color = BioluminescentViolet.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BioluminescentViolet),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (activePhase == DayNightPhase.RAD_NIGHT) {
                            "🌙 NIGHT HAZARD ACTIVE (+35% SWARM THREAT & +RAD LEAK): Nocturnal Mutant Swarms attack at nightfall! Keep Automated Defenses powered & manned!"
                        } else {
                            "🌆 DUSK WARNING (+15% THREAT): Night Mutant Swarms approaching — verify Turrets, Power & Fed Workers!"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = BioluminescentViolet,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Automated Defense Grid Control & 3-Part Requirement Checklist (Must Be Built + Fed Workers + Power!)
            Surface(
                color = ObsidianCharcoal,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(
                    1.dp,
                    if (isAutoDefenseActive) SurplusGreen else WarningOrange.copy(alpha = 0.7f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auto_defense_grid_card")
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isAutoDefenseActive) {
                                    "AUTOMATED DEFENSE GRID: ONLINE & ARMED"
                                } else {
                                    "AUTOMATED DEFENSE GRID: OFFLINE (REQUIREMENTS NOT MET)"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isAutoDefenseActive) SurplusGreen else WarningOrange,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Auto-intercepts Day Raiders & Night Mutant Swarms and auto-repairs the Blast Door when all 3 requirements are met:",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMutedAsh
                            )
                        }
                        Switch(
                            checked = autoDefenseEnabled,
                            onCheckedChange = onToggleAutoDefense,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ObsidianCharcoal,
                                checkedTrackColor = SurplusGreen
                            ),
                            modifier = Modifier.testTag("auto_defense_switch")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (hasBuiltAutoDefenses) "✓ 1. Built (${turretsCount}T/${plasmaCount}P)" else "✗ 1. Craft Turret/Plasma",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (hasBuiltAutoDefenses) SurplusGreen else HazardRed,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (hasAssignedFedWorkers) "✓ 2. Fed Crew Assigned" else "✗ 2. Need Fed Workers",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (hasAssignedFedWorkers) SurplusGreen else HazardRed,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (hasGridPower) "✓ 3. Power Online" else "✗ 3. Blackout (0 MW)",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (hasGridPower) SurplusGreen else HazardRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Day 10 Siege Countdown Bar
            Surface(
                color = ObsidianCharcoal,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(
                    1.dp,
                    if (daysUntilDay10Siege == 0) HazardRed else SolarAmber.copy(alpha = 0.55f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (daysUntilDay10Siege == 0) {
                            "DAY 10 NUCLEAR WARLORD MEGA-SIEGE ACTIVE TODAY!"
                        } else {
                            "Day 10 Mega-Siege Countdown: $daysUntilDay10Siege Day(s) Left"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (daysUntilDay10Siege == 0) HazardRed else SolarAmber,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Day 10 Req: $day10RequiredDefense DEF",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (totalDefense >= day10RequiredDefense) SurplusGreen else WarningOrange,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isVaultBlackout && (turretsCount > 0 || plasmaCount > 0)) {
                Text(
                    text = "WARNING: Vault Power Blackout! Powered Gatling Turrets & Plasma Shields are offline (0 DEF) until Power MW is restored!",
                    style = MaterialTheme.typography.labelSmall,
                    color = HazardRed,
                    fontWeight = FontWeight.Bold
                )
            }

            // Active Daily Raid / Night Mutant Swarm / Day 10 Siege Card
            if (activeRaid != null) {
                val defenseReady = totalDefense >= activeRaid.requiredDefenseRating
                val canCounterStrike = defenseReady || minesCount >= 1
                Surface(
                    color = HazardRed.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, HazardRed, RoundedCornerShape(10.dp))
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Daily Base Attack",
                                    tint = HazardRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "${activeRaid.title.uppercase()} (${activeRaid.remainingSeconds}s)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = HazardRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "REQ: ${activeRaid.requiredDefenseRating} DEF",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (defenseReady) SurplusGreen else WarningOrange,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${activeRaid.subtitle} Bounty: +${activeRaid.bountyGoldReward.toInt()} Gold, +${activeRaid.bountyScrapReward.toInt()} Scrap & +${activeRaid.bountyXpReward.toInt()} XP",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (activeRaid.weakenedByPercent > 0) {
                            Text(
                                text = "⚡ Countermeasures Deployed (${activeRaid.countermeasuresUsedCount}): Siege Armor Weakened by -${activeRaid.weakenedByPercent}%!",
                                style = MaterialTheme.typography.labelSmall,
                                color = CryoCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // 3 Active Tactical Siege Countermeasures (Flak Volley, EMP Pulse, Emergency Door Weld)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SiegeCountermeasureType.entries.forEach { cm ->
                                val canAffordCm = ammoRounds >= cm.ammoCost &&
                                    bioFuelStock >= cm.bioFuelCost &&
                                    scrapStock >= cm.scrapCost
                                OutlinedButton(
                                    onClick = { onExecuteCountermeasure(cm) },
                                    enabled = canAffordCm,
                                    border = BorderStroke(1.dp, if (canAffordCm) cm.accentColor else TextMutedAsh),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("countermeasure_button_${cm.id.lowercase()}")
                                ) {
                                    Text(
                                        text = cm.buttonLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (canAffordCm) cm.accentColor else TextMutedAsh,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = onRepelRaid,
                            enabled = canCounterStrike,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (defenseReady) SurplusGreen else WarningOrange,
                                contentColor = ObsidianCharcoal
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("repel_raid_button")
                        ) {
                            Text(
                                text = if (defenseReady) {
                                    "DEFEND BASE & REPEL ATTACK"
                                } else if (minesCount >= 1) {
                                    "DETONATE 1 BARRICADE MINE TO REPEL ATTACK"
                                } else {
                                    "BUILD DEFENSES OR UPGRADE BLAST DOOR BELOW"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "Perimeter clear! ($raidsRepelled Attacks Repelled • $luckyNoRepairCount Lucky Zero-Damage Deflections).",
                    style = MaterialTheme.typography.labelSmall,
                    color = SurplusGreen
                )
            }

            // Post-Attack Vault Door & Defense Integrity Repair + Timed Door Upgrade Panel
            Surface(
                color = if (needsDefenseRepair) WarningOrange.copy(alpha = 0.16f) else ObsidianCharcoal,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(
                    1.dp,
                    if (needsDefenseRepair) WarningOrange else RadTeal.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vault_door_repair_panel")
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
                            text = if (needsDefenseRepair) {
                                "VAULT DOOR & DEFENSES DAMAGED ($defenseIntegrityPercent% INTEGRITY)"
                            } else {
                                "VAULT DOOR & DEFENSES AT 100% INTEGRITY"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (needsDefenseRepair) WarningOrange else SurplusGreen,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "⏱ Lv.${blastDoorCost.targetLevel} Door Time: ${blastDoorCost.upgradeTimeSeconds}s (Next: ${blastDoorCost.nextLevelUpgradeTimeSeconds}s)",
                            style = MaterialTheme.typography.labelSmall,
                            color = SolarAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    LinearProgressIndicator(
                        progress = { integrityFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (needsDefenseRepair) WarningOrange else SurplusGreen,
                        trackColor = BunkerSlate
                    )

                    if (lastDamageFeedback != null) {
                        Text(
                            text = lastDamageFeedback,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (needsDefenseRepair) WarningOrange else SurplusGreen,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "Upgrading the Vault Blast Door takes longer at higher Door Levels (+20 Base DEF & restores 100% Integrity on finish).",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMutedAsh
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onRepairDefenses,
                            enabled = repairCost.needsRepair && repairCost.canAfford,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WarningOrange,
                                contentColor = ObsidianCharcoal
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("repair_defenses_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Handyman,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (!repairCost.needsRepair) {
                                    "100% INTACT"
                                } else {
                                    "REPAIR (${repairCost.scrapCost.toInt()}S + ${repairCost.goldCost.toInt()}G)"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        val isUpgradingDoor = activeDoorUpgrade != null
                        OutlinedButton(
                            onClick = onUpgradeBlastDoor,
                            enabled = blastDoorCost.canAfford && !isUpgradingDoor,
                            border = BorderStroke(1.dp, if (blastDoorCost.canAfford && !isUpgradingDoor) RadTeal else TextMutedAsh),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("upgrade_blast_door_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = if (blastDoorCost.canAfford && !isUpgradingDoor) RadTeal else TextMutedAsh,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isUpgradingDoor) {
                                    "UPGRADING (${activeDoorUpgrade?.remainingSeconds ?: 0}s)"
                                } else {
                                    "DOOR LV.${blastDoorLevel + 1} (${blastDoorCost.scrapCost.toInt()}S+${blastDoorCost.goldCost.toInt()}G • ${blastDoorCost.upgradeTimeSeconds}s)"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (blastDoorCost.canAfford && !isUpgradingDoor) RadTeal else TextMutedAsh,
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
private fun FishingHeaderCard(totalFishCaught: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = BunkerSlate
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CryoCyan.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
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
                        imageVector = Icons.Default.Phishing,
                        contentDescription = "Underground Crater Fishing",
                        tint = CryoCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "UNDERGROUND CRATER LAKE FISHING (FOOD & WATER)",
                        style = MaterialTheme.typography.titleSmall,
                        color = CryoCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "$totalFishCaught Fish Caught",
                    style = MaterialTheme.typography.labelSmall,
                    color = XpGold
                )
            }
            Text(
                text = "Cast a line for free or use Snare Nets & Harpoon Spears to catch Cave Trout, Crater Catfish, and Vault Sturgeon for Food, Water & Gold!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun FishingSpotCard(
    spot: FishingSpot,
    preview: FishingPreview,
    onExecuteFishing: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = BunkerSlate
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, spot.accentColor.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = spot.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = spot.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMutedAsh
                    )
                }
                Surface(
                    color = spot.accentColor.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.border(1.dp, spot.accentColor, RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = "+${preview.foodReward.toInt()} FOOD / +${preview.waterReward.toInt()} WATER",
                        style = MaterialTheme.typography.labelSmall,
                        color = spot.accentColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val reqLabel = when {
                    spot.requiresHarpoonSpear -> "Requires 1 Harpoon Spear"
                    spot.requiresSnareNet -> "Requires 1 Snare Trap/Net"
                    else -> "No Gear Needed (Free Line Cast!)"
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reqLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = SolarAmber
                    )
                    Text(
                        text = "Catch: +${preview.foodReward.toInt()} Food • +${preview.waterReward.toInt()} Water • +${preview.goldReward.toInt()} Gold • +${preview.xpReward.toInt()} XP",
                        style = MaterialTheme.typography.labelSmall,
                        color = XpGold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onExecuteFishing,
                    enabled = preview.canFish,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = spot.accentColor,
                        contentColor = ObsidianCharcoal
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("fish_spot_${spot.id}")
                ) {
                    Text(
                        text = if (preview.canFish) "FISH NOW" else "CRAFT GEAR",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun HuntingGearHeaderCard(
    spears: Int,
    snareTraps: Int,
    crossbowLevel: Int,
    passiveFoodPerSec: Double,
    huntsCompleted: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = BunkerSlate
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurplusGreen.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
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
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = "Wasteland Hunting",
                        tint = SurplusGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "WASTELAND HUNTING GROUNDS (HUNT FOR FOOD)",
                        style = MaterialTheme.typography.titleSmall,
                        color = SurplusGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "$huntsCompleted Hunts",
                    style = MaterialTheme.typography.labelSmall,
                    color = XpGold
                )
            }
            Text(
                text = "Spears: $spears • Active Snare Traps: $snareTraps (+${formatRate(passiveFoodPerSec)} Food/s) • Aether Crossbow: ${if (crossbowLevel > 0) "Mk.$crossbowLevel" else "Not Crafted"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun HuntingTargetCard(
    target: HuntingTarget,
    foodReward: Double,
    waterReward: Double,
    goldReward: Double,
    coresReward: Double,
    xpReward: Double,
    canExecute: Boolean,
    crossbowUnlocked: Boolean,
    counterAttackInjuryChancePercent: Int = 20,
    ammoRecommended: Int = 2,
    ammoAvailable: Int = 0,
    onExecuteHunt: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = BunkerSlate
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, target.accentColor.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = target.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = target.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMutedAsh
                    )
                }
                Surface(
                    color = SurplusGreen.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.border(1.dp, SurplusGreen, RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = "+${foodReward.toInt()} FOOD",
                        style = MaterialTheme.typography.labelMedium,
                        color = SurplusGreen,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val reqParts = buildList {
                    if (target.spearsRequired > 0) add("${target.spearsRequired} Spear(s)")
                    if (target.trapsRequired > 0) add("${target.trapsRequired} Snare Trap(s)")
                    if (target.requiresCrossbow) add("Aether Crossbow")
                    add("$ammoRecommended Ammo Rds")
                }.joinToString(" + ")

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Uses: $reqParts",
                        style = MaterialTheme.typography.labelSmall,
                        color = SolarAmber
                    )
                    Text(
                        text = "Beast Counter-Attack Risk: $counterAttackInjuryChancePercent% (${if (target == HuntingTarget.GLOWING_LEVIATHAN_WYRM || target == HuntingTarget.APEX_RAD_BEHEMOTH) "Rad-Venom" else "Bleeding"}) • ${if (ammoAvailable >= ammoRecommended) "100% Ammo Yield" else "Low Ammo (60% Yield)"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (ammoAvailable < ammoRecommended || counterAttackInjuryChancePercent >= 35) WarningOrange else CryoCyan
                    )
                    val bonusCoreStr = if (coresReward > 0) " • +${coresReward.toInt()} Core" else ""
                    Text(
                        text = "Yields: +${foodReward.toInt()} Food • +${waterReward.toInt()} Water • +${goldReward.toInt()} Gold • +${xpReward.toInt()} XP$bonusCoreStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = XpGold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onExecuteHunt,
                    enabled = canExecute,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = target.accentColor,
                        contentColor = ObsidianCharcoal
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("hunt_target_${target.id}")
                ) {
                    Text(
                        text = if (target.requiresCrossbow && !crossbowUnlocked) {
                            "NEED CROSSBOW"
                        } else if (canExecute) {
                            "HUNT BEAST"
                        } else {
                            "CRAFT GEAR"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CraftableItemCard(
    item: CraftableItemType,
    uiState: OutpostUiState,
    onCraft: () -> Unit
) {
    val state = uiState.state
    val cost = OutpostCalculator.computeCraftItemCost(
        item = item,
        state = state,
        skillsMap = uiState.skillsMap,
        buildingsMap = uiState.buildingsMap,
        workersMap = uiState.workersMap
    )
    val isCraftingThis = uiState.activeCrafting?.item == item
    val isCraftingAnother = uiState.activeCrafting != null && !isCraftingThis
    val ownedSummary = when (item) {
        CraftableItemType.HARPOON_SPEAR -> "In Armory: ${state.craftedSpears} Spears"
        CraftableItemType.SNARE_TRAP -> "Deployed: ${state.craftedSnareTraps} Traps"
        CraftableItemType.AETHER_CROSSBOW -> "Current: Mk.${state.aetherCrossbowLevel}"
        CraftableItemType.SHRAPNEL_MINE -> "Stockpiled: ${state.craftedShrapnelMines} Mines"
        CraftableItemType.AUTO_SENTRY_TURRET -> "Active: ${state.sentryTurretCount} Turrets"
        CraftableItemType.THORIUM_PLASMA_CANNON -> "Active: ${state.plasmaCannonCount} Shields"
        CraftableItemType.BALLISTIC_AMMO_CRATE -> "Stockpile: ${state.ammoRounds} Rds"
        CraftableItemType.GAS_MASK -> "In Locker: ${state.gasMasksCount} Masks"
        CraftableItemType.HAZMAT_SUIT -> "In Locker: ${state.hazmatSuitsCount} Suits"
        CraftableItemType.MEDKIT_RADAWAY_PACK -> "Meds: ${state.medkitsCount}M / ${state.radAwayCount}R"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = BunkerSlate
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, item.accentColor.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                .padding(12.dp),
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
                        contentDescription = item.title,
                        tint = item.accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "${item.title} (⏱ ${cost.craftTimeSeconds}s)",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = item.effectSummary,
                            style = MaterialTheme.typography.labelSmall,
                            color = item.accentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Surface(
                    color = item.accentColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.border(1.dp, item.accentColor, RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = ownedSummary,
                        style = MaterialTheme.typography.labelSmall,
                        color = item.accentColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextMutedAsh
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (cost.scrapCost > 0) {
                        CraftCostBadge("${cost.scrapCost.toInt()} SCRAP", state.scrap >= cost.scrapCost, SolarAmber)
                    }
                    if (cost.fuelCost > 0) {
                        CraftCostBadge("${cost.fuelCost.toInt()} FUEL", state.bioFuel >= cost.fuelCost, RustCopper)
                    }
                    if (cost.goldCost > 0) {
                        CraftCostBadge("${cost.goldCost.toInt()} GOLD", state.gold >= cost.goldCost, XpGold)
                    }
                    if (cost.aetherCost > 0) {
                        CraftCostBadge("${cost.aetherCost.toInt()} AETHER", state.aetherCells >= cost.aetherCost, CryoCyan)
                    }
                    if (cost.thoriumCost > 0) {
                        CraftCostBadge("${cost.thoriumCost.toInt()} THORIUM", state.thoriumRods >= cost.thoriumCost, WarningOrange)
                    }
                    if (cost.coresCost > 0) {
                        CraftCostBadge("${cost.coresCost.toInt()} CORES", state.mutagenCores >= cost.coresCost, BioluminescentViolet)
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onCraft,
                    enabled = cost.canAfford && !isCraftingThis && !isCraftingAnother,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = item.accentColor,
                        contentColor = ObsidianCharcoal
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("craft_item_${item.id}")
                ) {
                    Text(
                        text = when {
                            isCraftingThis -> "FORGING (${uiState.activeCrafting?.remainingSeconds ?: 0}s)"
                            else -> "CRAFT (${cost.craftTimeSeconds}s)"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CraftCostBadge(
    text: String,
    affordable: Boolean,
    color: Color
) {
    Surface(
        color = ObsidianCharcoal,
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.border(
            1.dp,
            if (affordable) color.copy(alpha = 0.55f) else HazardRed.copy(alpha = 0.7f),
            RoundedCornerShape(6.dp)
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (affordable) color else HazardRed,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun SectionDividerTitle(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = SolarAmber,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = TextMutedAsh
        )
    }
}
