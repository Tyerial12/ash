package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.OutpostCalculator
import com.example.model.BuildingType
import com.example.model.VaultMapStation
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
import kotlin.math.roundToInt

@Composable
fun Vault3dHologramMapCard(
    uiState: OutpostUiState,
    onSelectStation: (VaultMapStation) -> Unit,
    onUpgradeBuilding: (BuildingType) -> Unit,
    onMineAtSurface: () -> Unit,
    onFishAtSump: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state = uiState.state
    val prod = uiState.production
    var cameraYawIndex by remember { mutableIntStateOf(0) } // -1 = Left Iso, 0 = Deep 3D Cutaway, 1 = Right Iso
    var inspectedStation by remember {
        mutableStateOf(uiState.overseerStation)
    }

    val builtRoomsCount = BuildingType.entries.count { (uiState.buildingsMap[it.id] ?: 0) > 0 }
    val storeRoomLevel = uiState.buildingsMap[BuildingType.VAULT_STORE_ROOM.id] ?: 0
    val floors = remember {
        VaultMapStation.entries.groupBy { it.floorIndex }.toSortedMap()
    }

    Surface(
        color = SteelPanel,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, CryoCyan.copy(alpha = 0.85f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("vault_3d_hologram_map_card")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header & Built Rooms + Storage Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "3D Vault Map",
                            tint = CryoCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "3D ISOMETRIC VAULT MAP & ROOM LEVELS",
                            style = MaterialTheme.typography.titleSmall,
                            color = CryoCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "3D subterranean blueprint showing where every Vault room is located (Floors B1–B6), built status & live room levels.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Surface(
                    color = SolarAmber.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, SolarAmber)
                ) {
                    Text(
                        text = "$builtRoomsCount/${BuildingType.entries.size} BUILT",
                        style = MaterialTheme.typography.labelSmall,
                        color = SolarAmber,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Net Resource Balance & Store Room Capacity Banner inside 3D Map
            val isPositive = prod.isMakingPositiveResources
            val netSign = if (prod.netResourceBalancePerSec >= 0.0) "+" else ""
            Surface(
                color = (if (isPositive) SurplusGreen else HazardRed).copy(alpha = 0.14f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (isPositive) SurplusGreen else HazardRed),
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
                        text = if (isPositive) {
                            "🟢 POSITIVE RESOURCES ($netSign${prod.netResourceBalancePerSec}/s Net)"
                        } else {
                            "🔴 TAKING MORE THAN MAKING (${prod.netResourceBalancePerSec}/s Net)"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isPositive) SurplusGreen else HazardRed,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "📦 Store Rm Lv.$storeRoomLevel (+${prod.storeRoomStorageBonus.toInt()}) • Max Cap ${prod.storageCap.toInt()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = XpGold,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 3D Camera Perspective Switcher (-25° Left Iso / 0° Center 3D / +25° Right Iso)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val angles = listOf(
                    Triple(-1, "📐 LEFT 3D ISO", "vault_3d_angle_left"),
                    Triple(0, "🧊 DEEP 3D CORE", "vault_3d_angle_center"),
                    Triple(1, "📐 RIGHT 3D ISO", "vault_3d_angle_right")
                )
                angles.forEach { (yaw, label, tag) ->
                    val selected = cameraYawIndex == yaw
                    Surface(
                        color = if (selected) CryoCyan.copy(alpha = 0.24f) else BunkerCharcoal,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            width = if (selected) 1.5.dp else 1.dp,
                            color = if (selected) CryoCyan else PanelBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { cameraYawIndex = yaw }
                            .testTag(tag)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) CryoCyan else TextSecondary,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // 3D Isometric Extruded Vault Hologram Canvas
            Surface(
                color = BunkerCharcoal,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CryoCyan.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(410.dp)
                    .testTag("vault_3d_canvas")
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().height(410.dp)) {
                    draw3dVaultBunkerScene(
                        uiState = uiState,
                        floors = floors,
                        cameraYawIndex = cameraYawIndex,
                        inspectedStation = inspectedStation
                    )
                }
            }

            // Interactive 3D Floor & Room Selector Grid (Tap any room to inspect its 3D location, level & upgrade)
            Text(
                text = "TAP ANY VAULT ROOM BELOW TO INSPECT IN 3D & UPGRADE:",
                style = MaterialTheme.typography.labelSmall,
                color = SolarAmber,
                fontWeight = FontWeight.Bold
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vault_3d_room_grid")
            ) {
                floors.forEach { (floorIdx, stationsOnFloor) ->
                    val leftStation = stationsOnFloor.firstOrNull { it.wingColumn == 0 }
                    val rightStation = stationsOnFloor.firstOrNull { it.wingColumn == 1 }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Floor Depth Badge
                        Surface(
                            color = if (inspectedStation.floorIndex == floorIdx) CryoCyan.copy(alpha = 0.22f) else BunkerCharcoal,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(
                                1.dp,
                                if (inspectedStation.floorIndex == floorIdx) CryoCyan else PanelBorder
                            ),
                            modifier = Modifier
                                .width(46.dp)
                                .height(54.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (floorIdx == 0) "SURF" else "B$floorIdx",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (inspectedStation.floorIndex == floorIdx) CryoCyan else TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${floorIdx * 30}m",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                        }

                        if (leftStation != null) {
                            Vault3dStationButton(
                                station = leftStation,
                                uiState = uiState,
                                isSelected = inspectedStation == leftStation,
                                onSelect = {
                                    inspectedStation = leftStation
                                    onSelectStation(leftStation)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (rightStation != null) {
                            Vault3dStationButton(
                                station = rightStation,
                                uiState = uiState,
                                isSelected = inspectedStation == rightStation,
                                onSelect = {
                                    inspectedStation = rightStation
                                    onSelectStation(rightStation)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            // Companion 3D Reactor & Storage Spine readout on Floor B6 Right Wing
                            val totalReactorLevels = state.bioTurbineLevel + state.solarTowerLevel +
                                state.thoriumReactorLevel + state.aetherSubstationLevel
                            Surface(
                                color = ElevatedSlate,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, WarningOrange.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Reactor Core Bay",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = WarningOrange,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "LV.$totalReactorLevels",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = XpGold,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "Fuel Burn: -${prod.reactorFuelUpkeepPerSec}/s",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Selected 3D Room Inspector & Direct Build/Upgrade Deck
            val selBuilding = inspectedStation.buildingType
            val selLevel = selBuilding?.let { uiState.buildingsMap[it.id] ?: 0 } ?: 1
            val selCost = selBuilding?.let {
                OutpostCalculator.computeBuildingUpgradeCost(
                    buildingType = it,
                    currentLevel = selLevel,
                    state = state,
                    skillsMap = uiState.skillsMap,
                    workersMap = uiState.workersMap,
                    currentTotalMwGenerated = prod.totalMegawattGenerated,
                    currentTotalMwDemand = prod.totalMegawattDemand
                )
            }

            Surface(
                color = ElevatedSlate,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.5.dp, inspectedStation.accentColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vault_3d_selected_room_deck")
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val wingLabel = if (inspectedStation.wingColumn == 0) "Left Wing" else "Right Wing"
                    val floorLabel = if (inspectedStation.floorIndex == 0) "Surface Level (0m)" else "Floor B${inspectedStation.floorIndex} (-${inspectedStation.floorIndex * 30}m)"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "3D FOCUS: ${inspectedStation.shortTitle.uppercase()} ($floorLabel • $wingLabel)",
                                style = MaterialTheme.typography.titleSmall,
                                color = inspectedStation.accentColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = selBuilding?.gameplayNeedSummary
                                    ?: "Active surface/sump gathering station for manual Scrap, Gold, Food & Water.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                        Surface(
                            color = inspectedStation.accentColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, inspectedStation.accentColor)
                        ) {
                            Text(
                                text = if (selBuilding == null) "ACTIVE" else if (selLevel <= 0) "UNBUILT (LV.0)" else "BUILT LV.$selLevel",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selBuilding != null && selLevel <= 0) WarningOrange else inspectedStation.accentColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            when (inspectedStation) {
                                VaultMapStation.SURFACE_MINE_SHAFT -> onMineAtSurface()
                                VaultMapStation.UNDERGROUND_FISHING_SUMP -> onFishAtSump()
                                else -> selBuilding?.let(onUpgradeBuilding)
                            }
                        },
                        enabled = when (inspectedStation) {
                            VaultMapStation.SURFACE_MINE_SHAFT, VaultMapStation.UNDERGROUND_FISHING_SUMP -> true
                            else -> (selCost?.canAfford == true) && uiState.activeConstruction == null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = inspectedStation.accentColor,
                            contentColor = BunkerCharcoal
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("vault_3d_upgrade_selected_button")
                    ) {
                        val btnLabel = when (inspectedStation) {
                            VaultMapStation.SURFACE_MINE_SHAFT -> "⛏ MINE ORE AT SURFACE SHAFT (+SCRAP & +GOLD)"
                            VaultMapStation.UNDERGROUND_FISHING_SUMP -> "🎣 FISH AT SUB-VAULT SUMP (+FOOD & +WATER)"
                            else -> {
                                val sCost = selCost?.scrapCost?.toInt() ?: 0
                                val bTime = selCost?.buildTimeSeconds ?: 10
                                if (selLevel <= 0) {
                                    "🔨 BUILD ${inspectedStation.shortTitle.uppercase()} IN 3D VAULT (${sCost} Scrap • ${bTime}s)"
                                } else {
                                    "⬆ UPGRADE ${inspectedStation.shortTitle.uppercase()} TO LV.${selLevel + 1} (${sCost} Scrap • ${bTime}s)"
                                }
                            }
                        }
                        Text(
                            text = btnLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Vault3dStationButton(
    station: VaultMapStation,
    uiState: OutpostUiState,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bType = station.buildingType
    val level = bType?.let { uiState.buildingsMap[it.id] ?: 0 } ?: 1
    val isBuilt = bType == null || level > 0

    Surface(
        color = when {
            isSelected -> station.accentColor.copy(alpha = 0.24f)
            isBuilt -> ElevatedSlate
            else -> BunkerCharcoal
        },
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = when {
                isSelected -> XpGold
                isBuilt -> station.accentColor.copy(alpha = 0.8f)
                else -> PanelBorder
            }
        ),
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onSelect)
            .testTag("vault_3d_room_${station.id.lowercase()}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
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
                    color = if (isBuilt) TextPrimary else TextMuted,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = when {
                        bType == null -> "ACTIVE"
                        level <= 0 -> "LV.0"
                        else -> "LV.$level"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        bType == null -> SurplusGreen
                        level <= 0 -> WarningOrange
                        else -> XpGold
                    },
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = when {
                    bType == null -> "Surface / Sump Station"
                    level <= 0 -> "Unbuilt • Tap to Construct"
                    bType == BuildingType.VAULT_STORE_ROOM -> "Built • +${OutpostCalculator.computeStoreRoomStorageBonus(level).toInt()} Cap"
                    else -> "Built • Floor B${station.floorIndex} Online"
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (isBuilt) station.accentColor else TextSecondary,
                maxLines = 1
            )
        }
    }
}

private fun DrawScope.draw3dVaultBunkerScene(
    uiState: OutpostUiState,
    floors: Map<Int, List<VaultMapStation>>,
    cameraYawIndex: Int,
    inspectedStation: VaultMapStation
) {
    val w = size.width
    val h = size.height

    // 3D Extrusion vector (dx, dy) controlled by cameraYawIndex (-1, 0, 1)
    val extrudeDx = when (cameraYawIndex) {
        -1 -> -16f
        1 -> 16f
        else -> 11f
    }
    val extrudeDy = -10f

    // Subtle subterranean rock strata grid lines
    val floorCount = floors.size.coerceAtLeast(1)
    val topMargin = 24f
    val bottomMargin = 14f
    val usableH = h - topMargin - bottomMargin
    val rowStep = usableH / floorCount
    val boxHeight = (rowStep * 0.68f).coerceIn(28f, 44f)

    val centerShaftWidth = 44f
    val centerShaftLeft = (w - centerShaftWidth) / 2f
    val wingWidth = ((w - centerShaftWidth - 64f) / 2f).coerceAtLeast(95f)
    val leftWingX = centerShaftLeft - 10f - wingWidth
    val rightWingX = centerShaftLeft + centerShaftWidth + 10f

    val titlePaint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.WHITE
        textSize = 23f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
    }
    val badgePaint = Paint().apply {
        isAntiAlias = true
        color = XpGold.toArgb()
        textSize = 22f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
    }
    val subPaint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.LTGRAY
        textSize = 18f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
    }

    // Draw Central 3D Elevator Spine
    val spineTop = topMargin
    val spineBottom = h - bottomMargin
    drawRect(
        color = Color(0xFF141E24),
        topLeft = Offset(centerShaftLeft, spineTop),
        size = Size(centerShaftWidth, spineBottom - spineTop)
    )
    drawRect(
        color = CryoCyan.copy(alpha = 0.45f),
        topLeft = Offset(centerShaftLeft, spineTop),
        size = Size(centerShaftWidth, spineBottom - spineTop),
        style = Stroke(width = 2f)
    )

    floors.entries.forEachIndexed { idx, (floorIdx, stationsOnFloor) ->
        val rowY = topMargin + idx * rowStep + 8f

        // Geological depth line
        drawLine(
            color = PanelBorder.copy(alpha = 0.35f),
            start = Offset(12f, rowY + boxHeight + 6f),
            end = Offset(w - 12f, rowY + boxHeight + 6f),
            strokeWidth = 1f
        )

        // Floor label inside central elevator shaft
        val isOverseerFloor = uiState.overseerStation.floorIndex == floorIdx
        drawRect(
            color = if (isOverseerFloor) XpGold.copy(alpha = 0.30f) else Color(0xFF1B262D),
            topLeft = Offset(centerShaftLeft + 4f, rowY),
            size = Size(centerShaftWidth - 8f, boxHeight)
        )
        drawContext.canvas.nativeCanvas.drawText(
            if (floorIdx == 0) "SRF" else "B$floorIdx",
            centerShaftLeft + 8f,
            rowY + boxHeight * 0.62f,
            badgePaint.apply {
                color = if (isOverseerFloor) XpGold.toArgb() else CryoCyan.toArgb()
            }
        )

        val leftStation = stationsOnFloor.firstOrNull { it.wingColumn == 0 }
        val rightStation = stationsOnFloor.firstOrNull { it.wingColumn == 1 }

        if (leftStation != null) {
            draw3dRoomModule(
                station = leftStation,
                uiState = uiState,
                x = leftWingX,
                y = rowY,
                w = wingWidth,
                h = boxHeight,
                extrudeDx = extrudeDx,
                extrudeDy = extrudeDy,
                isInspected = inspectedStation == leftStation,
                titlePaint = titlePaint,
                badgePaint = badgePaint,
                subPaint = subPaint
            )
            // Power conduit connecting Left Room to Central Shaft
            drawLine(
                color = leftStation.accentColor.copy(alpha = 0.6f),
                start = Offset(leftWingX + wingWidth, rowY + boxHeight / 2f),
                end = Offset(centerShaftLeft, rowY + boxHeight / 2f),
                strokeWidth = 3f
            )
        }

        if (rightStation != null) {
            draw3dRoomModule(
                station = rightStation,
                uiState = uiState,
                x = rightWingX,
                y = rowY,
                w = wingWidth,
                h = boxHeight,
                extrudeDx = extrudeDx,
                extrudeDy = extrudeDy,
                isInspected = inspectedStation == rightStation,
                titlePaint = titlePaint,
                badgePaint = badgePaint,
                subPaint = subPaint
            )
            // Power conduit connecting Right Room to Central Shaft
            drawLine(
                color = rightStation.accentColor.copy(alpha = 0.6f),
                start = Offset(centerShaftLeft + centerShaftWidth, rowY + boxHeight / 2f),
                end = Offset(rightWingX, rowY + boxHeight / 2f),
                strokeWidth = 3f
            )
        } else {
            // Floor B6 Right Wing: 3D Reactor Fuel Core Visualization
            val totalReactorLv = uiState.state.bioTurbineLevel + uiState.state.solarTowerLevel +
                uiState.state.thoriumReactorLevel + uiState.state.aetherSubstationLevel
            draw3dExtrudedCube(
                x = rightWingX,
                y = rowY,
                w = wingWidth,
                h = boxHeight,
                dx = extrudeDx,
                dy = extrudeDy,
                frontColor = if (totalReactorLv > 0) WarningOrange.copy(alpha = 0.26f) else Color(0xFF151C21),
                topColor = if (totalReactorLv > 0) WarningOrange.copy(alpha = 0.42f) else Color(0xFF1E282F),
                sideColor = if (totalReactorLv > 0) RustCopper.copy(alpha = 0.36f) else Color(0xFF10161A),
                strokeColor = if (totalReactorLv > 0) WarningOrange else PanelBorder,
                isDashed = totalReactorLv == 0
            )
            drawContext.canvas.nativeCanvas.drawText(
                "REACTOR CORE",
                rightWingX + 8f,
                rowY + boxHeight * 0.46f,
                titlePaint.apply { color = WarningOrange.toArgb(); textSize = 19f }
            )
            drawContext.canvas.nativeCanvas.drawText(
                "LV.$totalReactorLv (-${uiState.production.reactorFuelUpkeepPerSec}/s F)",
                rightWingX + 8f,
                rowY + boxHeight * 0.84f,
                subPaint.apply { color = XpGold.toArgb(); textSize = 17f }
            )
        }
    }
}

private fun DrawScope.draw3dRoomModule(
    station: VaultMapStation,
    uiState: OutpostUiState,
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    extrudeDx: Float,
    extrudeDy: Float,
    isInspected: Boolean,
    titlePaint: Paint,
    badgePaint: Paint,
    subPaint: Paint
) {
    val bType = station.buildingType
    val level = bType?.let { uiState.buildingsMap[it.id] ?: 0 } ?: 1
    val isBuilt = bType == null || level > 0
    val accent = station.accentColor

    val frontColor = when {
        isInspected -> accent.copy(alpha = 0.38f)
        isBuilt -> accent.copy(alpha = 0.22f)
        else -> Color(0xFF131A1F)
    }
    val topColor = when {
        isInspected -> accent.copy(alpha = 0.55f)
        isBuilt -> accent.copy(alpha = 0.36f)
        else -> Color(0xFF1B242B)
    }
    val sideColor = when {
        isInspected -> accent.copy(alpha = 0.45f)
        isBuilt -> accent.copy(alpha = 0.28f)
        else -> Color(0xFF0E1418)
    }
    val borderColor = when {
        isInspected -> XpGold
        isBuilt -> accent
        else -> PanelBorder.copy(alpha = 0.65f)
    }

    draw3dExtrudedCube(
        x = x,
        y = y,
        w = w,
        h = h,
        dx = extrudeDx,
        dy = extrudeDy,
        frontColor = frontColor,
        topColor = topColor,
        sideColor = sideColor,
        strokeColor = borderColor,
        isDashed = !isBuilt
    )

    // Draw glowing interior windows if built
    if (isBuilt) {
        val winCount = (level.coerceIn(1, 5))
        for (i in 0 until winCount) {
            drawRect(
                color = accent.copy(alpha = 0.40f),
                topLeft = Offset(x + 8f + i * 14f, y + h - 9f),
                size = Size(9f, 4f)
            )
        }
    }

    // Room Title & Level Label inside the 3D Room Front Face
    val shortName = station.shortTitle.take(15)
    drawContext.canvas.nativeCanvas.drawText(
        shortName,
        x + 7f,
        y + h * 0.46f,
        titlePaint.apply {
            color = if (isBuilt) android.graphics.Color.WHITE else android.graphics.Color.GRAY
            textSize = 19f
        }
    )

    val statusText = when {
        bType == null -> "ACTIVE"
        level <= 0 -> "UNBUILT (LV.0)"
        else -> "BUILT • LV.$level"
    }
    drawContext.canvas.nativeCanvas.drawText(
        statusText,
        x + 7f,
        y + h * 0.84f,
        badgePaint.apply {
            color = when {
                bType == null -> SurplusGreen.toArgb()
                level <= 0 -> WarningOrange.toArgb()
                else -> XpGold.toArgb()
            }
            textSize = 17f
        }
    )
}

private fun DrawScope.draw3dExtrudedCube(
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    dx: Float,
    dy: Float,
    frontColor: Color,
    topColor: Color,
    sideColor: Color,
    strokeColor: Color,
    isDashed: Boolean
) {
    val strokeStyle = if (isDashed) {
        Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f), 0f))
    } else {
        Stroke(width = 2f)
    }

    // 1. Top 3D Roof Face
    val topPath = Path().apply {
        moveTo(x, y)
        lineTo(x + dx, y + dy)
        lineTo(x + w + dx, y + dy)
        lineTo(x + w, y)
        close()
    }
    drawPath(topPath, color = topColor)
    drawPath(topPath, color = strokeColor, style = strokeStyle)

    // 2. Side 3D Wall Face (Right wall if dx >= 0, Left wall if dx < 0)
    val sidePath = Path().apply {
        if (dx >= 0f) {
            moveTo(x + w, y)
            lineTo(x + w + dx, y + dy)
            lineTo(x + w + dx, y + h + dy)
            lineTo(x + w, y + h)
        } else {
            moveTo(x, y)
            lineTo(x + dx, y + dy)
            lineTo(x + dx, y + h + dy)
            lineTo(x, y + h)
        }
        close()
    }
    drawPath(sidePath, color = sideColor)
    drawPath(sidePath, color = strokeColor, style = strokeStyle)

    // 3. Front 3D Room Face
    drawRect(
        color = frontColor,
        topLeft = Offset(x, y),
        size = Size(w, h)
    )
    drawRect(
        color = strokeColor,
        topLeft = Offset(x, y),
        size = Size(w, h),
        style = strokeStyle
    )
}
