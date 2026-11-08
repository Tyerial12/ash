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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.OutpostCalculator
import com.example.domain.SkillUpgradeCost
import com.example.model.SkillBranch
import com.example.model.SkillNodeDefinition
import com.example.model.SkillTreeCatalog
import com.example.ui.components.formatCompact
import com.example.ui.theme.BioluminescentViolet
import com.example.ui.theme.BunkerCharcoal
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.PanelBorder
import com.example.ui.theme.RadTeal
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.SteelPanel
import com.example.ui.theme.SurplusGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.XpGold
import com.example.viewmodel.OutpostUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillTreeTabScreen(
    uiState: OutpostUiState,
    onUnlockSkill: (SkillNodeDefinition) -> Unit,
    onPerformPrestige: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedBranch by remember { mutableStateOf(SkillBranch.INDUSTRIAL_SALVAGE) }
    val branchNodes = remember(selectedBranch) {
        SkillTreeCatalog.nodes
            .filter { it.branch == selectedBranch }
            .sortedBy { it.tier }
    }
    val prestigePreview = remember(uiState.state, uiState.buildingsMap) {
        OutpostCalculator.computePrestigePreview(uiState.state, uiState.buildingsMap)
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Skill Points & Research Summary Banner
        Surface(
            color = SteelPanel,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, PanelBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountTree,
                        contentDescription = null,
                        tint = selectedBranch.accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "VAULT CITY SKILL TREE • LEVEL ${uiState.state.commandLevel}/100",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = selectedBranch.tagline,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = XpGold.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, XpGold)
                    ) {
                        Text(
                            text = "★ ${uiState.state.prestigeCores} CORES",
                            style = MaterialTheme.typography.labelMedium,
                            color = XpGold,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                    Surface(
                        color = RadTeal.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, RadTeal)
                    ) {
                        Text(
                            text = "${uiState.state.availableSkillPoints} SP AVAIL",
                            style = MaterialTheme.typography.labelMedium,
                            color = RadTeal,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 4 Branch Selector Tabs (Industrial, Bio-Power, Defense, Overseer Legacy)
        SecondaryScrollableTabRow(
            selectedTabIndex = SkillBranch.entries.indexOf(selectedBranch),
            containerColor = BunkerCharcoal,
            contentColor = TextPrimary,
            edgePadding = 12.dp,
            divider = {}
        ) {
            SkillBranch.entries.forEach { branch ->
                val isSelected = branch == selectedBranch
                Tab(
                    selected = isSelected,
                    onClick = { selectedBranch = branch },
                    modifier = Modifier.testTag("skill_branch_${branch.name.lowercase()}"),
                    text = {
                        Text(
                            text = branch.displayName.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) branch.accentColor else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(key = "overseer_ascension_prestige_card") {
                OverseerAscensionPrestigeCard(
                    prestigeLevel = uiState.state.prestigeLevel,
                    prestigeCores = uiState.state.prestigeCores,
                    canPrestige = prestigePreview.canAscend,
                    coresToEarn = prestigePreview.coresToEarn,
                    nextAllYieldBonusPercent = ((prestigePreview.nextPrestigeMultiplier - 1.0) * 100.0).toInt(),
                    requirementText = prestigePreview.requirementSummary,
                    onPerformPrestige = onPerformPrestige
                )
            }

            itemsIndexed(branchNodes, key = { _, node -> node.id }) { index, node ->
                val currentRank = uiState.skillsMap[node.id] ?: 0
                val costBreakdown = OutpostCalculator.computeSkillUpgradeCost(
                    node = node,
                    currentRank = currentRank,
                    state = uiState.state,
                    skillsMap = uiState.skillsMap
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (index > 0) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(16.dp)
                                .background(
                                    if (costBreakdown.prerequisiteMet) selectedBranch.accentColor
                                    else PanelBorder
                                )
                        )
                    }

                    SkillNodeCard(
                        node = node,
                        currentRank = currentRank,
                        cost = costBreakdown,
                        onUpgrade = { onUnlockSkill(node) }
                    )
                }
            }
        }
    }
}

@Composable
private fun OverseerAscensionPrestigeCard(
    prestigeLevel: Int,
    prestigeCores: Int,
    canPrestige: Boolean,
    coresToEarn: Int,
    nextAllYieldBonusPercent: Int,
    requirementText: String,
    onPerformPrestige: () -> Unit
) {
    Surface(
        color = SteelPanel,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, if (canPrestige) XpGold else PanelBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("prestige_ascend_card")
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
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Overseer Ascension Prestige",
                        tint = XpGold,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = "OVERSEER ASCENSION (PRESTIGE TIER P$prestigeLevel)",
                            style = MaterialTheme.typography.titleSmall,
                            color = XpGold,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Reset Vault Rooms & Day Counter to earn Permanent Prestige Cores & +25% Global Yield/Tier. All Skill Tree Ranks & Unique Dwellers persist!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Reward: +$coresToEarn Prestige Cores • Next Global Bonus: +$nextAllYieldBonusPercent% All Yields",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (canPrestige) SurplusGreen else SolarAmber,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = requirementText,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onPerformPrestige,
                    enabled = canPrestige,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = XpGold,
                        contentColor = BunkerCharcoal,
                        disabledContainerColor = ElevatedSlate,
                        disabledContentColor = TextMuted
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("prestige_ascend_button")
                ) {
                    Text(
                        text = if (canPrestige) "ASCEND (+$coresToEarn ★)" else "LOCKED",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SkillNodeCard(
    node: SkillNodeDefinition,
    currentRank: Int,
    cost: SkillUpgradeCost,
    onUpgrade: () -> Unit
) {
    val accent = node.branch.accentColor

    Surface(
        color = SteelPanel,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = 1.dp,
            color = when {
                cost.isMaxed -> SurplusGreen.copy(alpha = 0.8f)
                cost.canAfford -> accent.copy(alpha = 0.85f)
                cost.prerequisiteMet -> PanelBorder
                else -> PanelBorder.copy(alpha = 0.45f)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("skill_node_${node.id.lowercase()}")
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (cost.prerequisiteMet) accent.copy(alpha = 0.18f)
                                else ElevatedSlate
                            )
                            .border(
                                1.dp,
                                if (cost.prerequisiteMet) accent else PanelBorder,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!cost.prerequisiteMet) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        } else if (cost.isMaxed) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Max Rank",
                                tint = SurplusGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = "T${node.tier}",
                                style = MaterialTheme.typography.labelMedium,
                                color = accent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = node.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = node.effectSummaryPerRank,
                            style = MaterialTheme.typography.labelSmall,
                            color = accent
                        )
                    }
                }

                // Rank Badge
                Surface(
                    color = ElevatedSlate,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (currentRank > 0) accent else PanelBorder)
                ) {
                    Text(
                        text = "RANK $currentRank/${node.maxRank}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (currentRank > 0) accent else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = node.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (cost.isMaxed) {
                    Text(
                        text = "MAXIMUM MASTERY ACHIEVED",
                        style = MaterialTheme.typography.labelMedium,
                        color = SurplusGreen,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (cost.skillPointCost > 0) {
                            Surface(
                                color = ElevatedSlate,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, RadTeal.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "${cost.skillPointCost} SP",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RadTeal,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                        if (cost.prestigeCoreCost > 0) {
                            Surface(
                                color = ElevatedSlate,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, XpGold)
                            ) {
                                Text(
                                    text = "★ ${cost.prestigeCoreCost} CORES",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = XpGold,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                        if (cost.researchCost > 0) {
                            Surface(
                                color = ElevatedSlate,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, XpGold.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "${formatCompact(cost.researchCost)} TECH",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = XpGold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                        if (cost.goldCost > 0) {
                            Surface(
                                color = ElevatedSlate,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, XpGold.copy(alpha = 0.65f))
                            ) {
                                Text(
                                    text = "${cost.goldCost.toInt()} GOLD",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = XpGold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                        if (cost.coresCost > 0) {
                            Surface(
                                color = ElevatedSlate,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, BioluminescentViolet.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "${cost.coresCost.toInt()} CORES",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BioluminescentViolet,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = onUpgrade,
                    enabled = cost.canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        contentColor = BunkerCharcoal,
                        disabledContainerColor = ElevatedSlate,
                        disabledContentColor = TextMuted
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("research_skill_${node.id.lowercase()}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Upgrade,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (currentRank == 0) "UNLOCK" else "RANK UP",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
