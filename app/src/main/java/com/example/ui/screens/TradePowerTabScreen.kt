package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.remote.PublicSupplyDrop
import com.example.data.remote.PublicVaultProfile
import com.example.domain.OutpostCalculator
import com.example.model.PowerStationType
import com.example.model.RareSyndicateItem
import com.example.model.ResourceType
import com.example.model.TradeTown
import com.example.ui.theme.BioluminescentViolet
import com.example.ui.theme.BunkerSlate
import com.example.ui.theme.CryoCyan
import com.example.ui.theme.ElevatedSteel
import com.example.ui.theme.HazardRed
import com.example.ui.theme.ObsidianCharcoal
import com.example.ui.theme.RadTeal
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.SurplusGreen
import com.example.ui.theme.TextMutedAsh
import com.example.ui.theme.WarningOrange
import com.example.ui.theme.XpGold
import com.example.viewmodel.OutpostUiState
import kotlin.math.roundToInt

@Composable
fun TradePowerTabScreen(
    uiState: OutpostUiState,
    onSellToTown: (TradeTown, ResourceType) -> Unit,
    onBuyRareItem: (RareSyndicateItem) -> Unit,
    onDispatchConvoy: (TradeTown) -> Unit,
    onUpgradeConvoy: () -> Unit,
    onUpgradePowerStation: (PowerStationType) -> Unit,
    onInjectThoriumOverdrive: () -> Unit,
    onTriggerSolarFlare: () -> Unit,
    onTriggerEclipseShroud: () -> Unit,
    onPublishVault: () -> Unit = {},
    onBroadcastSupplyDrop: () -> Unit = {},
    onClaimSupplyDrop: (PublicSupplyDrop) -> Unit = {},
    isGoogleSignedIn: Boolean = true,
    userEmail: String? = null,
    authErrorMessage: String? = null,
    onRequestGoogleSignIn: () -> Unit = {},
    onSignOutGoogle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedSection by rememberSaveable { mutableIntStateOf(0) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "section_switcher") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { selectedSection = 0 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedSection == 0) XpGold else BunkerSlate,
                        contentColor = if (selectedSection == 0) ObsidianCharcoal else XpGold
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("trade_subtab_button")
                ) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("TRADE", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { selectedSection = 1 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedSection == 1) WarningOrange else BunkerSlate,
                        contentColor = if (selectedSection == 1) ObsidianCharcoal else WarningOrange
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("power_subtab_button")
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("POWER GRID", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { selectedSection = 2 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedSection == 2) RadTeal else BunkerSlate,
                        contentColor = if (selectedSection == 2) ObsidianCharcoal else RadTeal
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("public_network_subtab_button")
                ) {
                    Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PUBLIC NET", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (selectedSection == 0) {
            item(key = "convoy_command_card") {
                ConvoyCommandCard(
                    uiState = uiState,
                    onDispatchConvoy = onDispatchConvoy,
                    onUpgradeConvoy = onUpgradeConvoy
                )
            }

            item(key = "town_market_header") {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "SELL SURPLUS TO NEIGHBORING TOWNS FOR GOLD",
                        style = MaterialTheme.typography.titleSmall,
                        color = XpGold,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Each town pays +35% Gold for its favored commodities and +25% bonus during its peak circadian phase.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMutedAsh
                    )
                }
            }

            items(TradeTown.entries, key = { "town_${it.id}" }) { town ->
                TownTradeCard(
                    town = town,
                    uiState = uiState,
                    onSellResource = { res -> onSellToTown(town, res) }
                )
            }

            item(key = "syndicate_exchange_header") {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "BUY RARE RESOURCES WITH GOLD",
                        style = MaterialTheme.typography.titleSmall,
                        color = CryoCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Spend Gold Sovereigns on Thorium Fuel Rods, Aether Capacitors & Mutagen Cores for Convoys, Power Stations & Armory.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMutedAsh
                    )
                }
            }

            items(RareSyndicateItem.entries, key = { "rare_${it.id}" }) { item ->
                RareSyndicateItemCard(
                    item = item,
                    uiState = uiState,
                    onBuy = { onBuyRareItem(item) }
                )
            }
        } else if (selectedSection == 1) {
            item(key = "power_grid_summary") {
                PowerGridSummaryCard(
                    uiState = uiState,
                    onInjectThoriumOverdrive = onInjectThoriumOverdrive
                )
            }

            items(PowerStationType.entries, key = { "station_${it.id}" }) { station ->
                PowerStationCard(
                    station = station,
                    uiState = uiState,
                    onUpgrade = { onUpgradePowerStation(station) }
                )
            }

            item(key = "chrono_phase_powers") {
                ChronoPhaseBeaconsCard(
                    uiState = uiState,
                    onTriggerSolarFlare = onTriggerSolarFlare,
                    onTriggerEclipseShroud = onTriggerEclipseShroud
                )
            }
        } else {
            item(key = "public_network_header_card") {
                PublicWastelandNetworkCard(
                    uiState = uiState,
                    isGoogleSignedIn = isGoogleSignedIn,
                    userEmail = userEmail,
                    authErrorMessage = authErrorMessage,
                    onRequestGoogleSignIn = onRequestGoogleSignIn,
                    onSignOutGoogle = onSignOutGoogle,
                    onPublishVault = onPublishVault,
                    onBroadcastSupplyDrop = onBroadcastSupplyDrop
                )
            }

            if (isGoogleSignedIn) {
                item(key = "public_supply_drops_header") {
                    Text(
                        text = "LIVE MULTIPLAYER SUPPLY DROPS (${uiState.publicSupplyDrops.size})",
                        style = MaterialTheme.typography.titleSmall,
                        color = SurplusGreen,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (uiState.publicSupplyDrops.isEmpty()) {
                    item(key = "no_supply_drops") {
                        Surface(
                            color = BunkerSlate,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No unclaimed supply drops in orbit. Broadcast a care package above to aid fellow Overseers!",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMutedAsh,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                } else {
                    items(uiState.publicSupplyDrops, key = { "drop_${it.id}" }) { drop ->
                        PublicSupplyDropCard(
                            drop = drop,
                            onClaim = { onClaimSupplyDrop(drop) }
                        )
                    }
                }

                item(key = "public_leaderboard_header") {
                    Text(
                        text = "GLOBAL MULTIPLAYER VAULT LEADERBOARD (${uiState.publicVaults.size})",
                        style = MaterialTheme.typography.titleSmall,
                        color = XpGold,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (uiState.publicVaults.isEmpty()) {
                    item(key = "no_public_vaults") {
                        Surface(
                            color = BunkerSlate,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No Vaults published yet. Click SAVE VAULT TO CLOUD above to broadcast your shelter to the global Wasteland!",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMutedAsh,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                } else {
                    itemsIndexed(uiState.publicVaults, key = { _, v -> "vault_${v.id.ifBlank { v.userId }}" }) { index, entry ->
                        PublicLeaderboardEntryCard(rank = index + 1, entry = entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun PublicWastelandNetworkCard(
    uiState: OutpostUiState,
    isGoogleSignedIn: Boolean = true,
    userEmail: String? = null,
    authErrorMessage: String? = null,
    onRequestGoogleSignIn: () -> Unit = {},
    onSignOutGoogle: () -> Unit = {},
    onPublishVault: () -> Unit,
    onBroadcastSupplyDrop: () -> Unit
) {
    val state = uiState.state
    val canBroadcastDrop = state.scrap >= 30.0 && state.rations >= 15.0 && state.water >= 15.0

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cloud_multiplayer_network_card"),
        shape = RoundedCornerShape(14.dp),
        color = ElevatedSteel
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (isGoogleSignedIn) RadTeal.copy(alpha = 0.65f) else SolarAmber, RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isGoogleSignedIn) {
                        "CLOUD SAVE & MULTIPLAYER SATELLITE UPLINK (ONLINE)"
                    } else {
                        "GOOGLE SIGN-IN REQUIRED FOR CLOUD SAVE, PRESTIGE & MULTIPLAYER"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isGoogleSignedIn) RadTeal else SolarAmber,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (isGoogleSignedIn) {
                    OutlinedButton(
                        onClick = onSignOutGoogle,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("google_sign_out_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("SIGN OUT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (!isGoogleSignedIn) {
                Text(
                    text = "You are playing in Local Offline Mode. Without logging into Google, you cannot Prestige, Save Progress to the Cloud, or use Multiplayer (Supply Drops & Global Leaderboard).",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMutedAsh
                )
                authErrorMessage?.let { err ->
                    Text(
                        text = err,
                        style = MaterialTheme.typography.labelSmall,
                        color = HazardRed,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = onRequestGoogleSignIn,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SolarAmber,
                        contentColor = ObsidianCharcoal
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("google_sign_in_button")
                ) {
                    Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SIGN IN WITH GOOGLE (UNLOCK PRESTIGE, CLOUD SAVE & MULTIPLAYER)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = "Signed in as ${userEmail ?: "Google Overseer"} • Save your Vault progress to the Cloud, Prestige, or trade Multiplayer Supply Drops!",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMutedAsh
                )

                uiState.publicNetworkFeedback?.let { msg ->
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.labelSmall,
                        color = SurplusGreen,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onPublishVault,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RadTeal,
                            contentColor = ObsidianCharcoal
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("publish_vault_button")
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SAVE VAULT TO CLOUD", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onBroadcastSupplyDrop,
                        enabled = canBroadcastDrop,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurplusGreen,
                            contentColor = ObsidianCharcoal
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("broadcast_supply_drop_button")
                    ) {
                        Icon(Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SEND DROP (30S/15F/15W)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PublicSupplyDropCard(
    drop: PublicSupplyDrop,
    onClaim: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = BunkerSlate
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurplusGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "DROP FROM ${drop.senderName.uppercase()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = SurplusGreen,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "+${drop.amount} ${drop.resourceType}",
                    style = MaterialTheme.typography.bodySmall,
                    color = XpGold
                )
                Text(
                    text = drop.message,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMutedAsh
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onClaim,
                enabled = drop.status == "open",
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurplusGreen,
                    contentColor = ObsidianCharcoal
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("claim_supply_drop_${drop.id}")
            ) {
                Text(
                    text = if (drop.status == "open") "CLAIM" else "CLAIMED",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PublicLeaderboardEntryCard(
    rank: Int,
    entry: PublicVaultProfile
) {
    val rankColor = when (rank) {
        1 -> XpGold
        2 -> CryoCyan
        3 -> SolarAmber
        else -> TextMutedAsh
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = BunkerSlate
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, rankColor.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = rankColor,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "#$rank ${entry.overseerName} • ${entry.vaultName}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Prestige P${entry.prestigeLevel} • Lv.${entry.commandLevel} • Day ${entry.dayCounter} • ${entry.totalSurvivors} Dwellers",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMutedAsh
                    )
                }
            }
            Surface(
                color = ObsidianCharcoal,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.border(1.dp, RadTeal.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = "DEF ${entry.totalDefenseRating} • ★ ${entry.uniqueDwellersCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = RadTeal,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConvoyCommandCard(
    uiState: OutpostUiState,
    onDispatchConvoy: (TradeTown) -> Unit,
    onUpgradeConvoy: () -> Unit
) {
    val state = uiState.state
    val activeConvoy = uiState.activeConvoy
    val upgradeCost = OutpostCalculator.computeConvoyUpgradeCost(state)
    val dispatchFuelCost = (12.0 + (state.convoyLevel - 1) * 4.0).toInt()
    val dispatchRationsCost = (10.0 + (state.convoyLevel - 1) * 3.0).toInt()
    val canDispatch = activeConvoy == null && state.bioFuel >= dispatchFuelCost && state.rations >= dispatchRationsCost

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = ElevatedSteel
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, XpGold.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ARMORED TRADE CONVOY • MK.${state.convoyLevel}",
                        style = MaterialTheme.typography.titleSmall,
                        color = XpGold,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Runs Completed: ${state.totalConvoysCompleted} • Dispatch Cost: $dispatchFuelCost Fuel & $dispatchRationsCost Food",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMutedAsh
                    )
                }
                Button(
                    onClick = onUpgradeConvoy,
                    enabled = upgradeCost.canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SolarAmber,
                        contentColor = ObsidianCharcoal
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("upgrade_convoy_button")
                ) {
                    Text(
                        text = "UPGRADE (${upgradeCost.goldCost.toInt()}G + ${upgradeCost.thoriumCost.toInt()}TH + ${upgradeCost.aetherCost.toInt()}AE)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (activeConvoy != null) {
                val fraction = 1f - (activeConvoy.remainingSeconds.toFloat() / activeConvoy.totalSeconds.coerceAtLeast(1))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ObsidianCharcoal, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "EN ROUTE TO ${activeConvoy.destination.displayName.uppercase()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = activeConvoy.destination.accentColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${activeConvoy.remainingSeconds}s remaining",
                            style = MaterialTheme.typography.labelSmall,
                            color = XpGold
                        )
                    }
                    LinearProgressIndicator(
                        progress = { fraction.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = activeConvoy.destination.accentColor,
                        trackColor = BunkerSlate
                    )
                    Text(
                        text = "Expected Haul: +${activeConvoy.expectedGoldReward.toInt()} Gold & +${activeConvoy.expectedRareAmount.toInt()} ${activeConvoy.expectedRareResource.displayName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SurplusGreen
                    )
                }
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TradeTown.entries.forEach { town ->
                        OutlinedButton(
                            onClick = { onDispatchConvoy(town) },
                            enabled = canDispatch,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("dispatch_convoy_${town.id}")
                        ) {
                            Text(
                                text = "CONVOY -> ${town.displayName.uppercase()} (+${town.bonusRareDropType.shortLabel})",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (canDispatch) town.accentColor else TextMutedAsh,
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
private fun TownTradeCard(
    town: TradeTown,
    uiState: OutpostUiState,
    onSellResource: (ResourceType) -> Unit
) {
    val state = uiState.state
    val activePhase = uiState.production.activePhase
    val isPeakPhase = activePhase == town.favoredPhase
    val sellBatch = 30.0

    val quote1 = OutpostCalculator.computeTownSellQuote(town, town.favoredResource1, sellBatch, activePhase, uiState.skillsMap)
    val quote2 = OutpostCalculator.computeTownSellQuote(town, town.favoredResource2, sellBatch, activePhase, uiState.skillsMap)

    fun hasAmount(res: ResourceType): Boolean = when (res) {
        ResourceType.SCRAP -> state.scrap >= sellBatch
        ResourceType.WATER -> state.water >= sellBatch
        ResourceType.RATIONS -> state.rations >= sellBatch
        ResourceType.BIO_FUEL -> state.bioFuel >= sellBatch
        ResourceType.RESEARCH_DATA -> state.researchData >= sellBatch
        else -> false
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = BunkerSlate
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, town.accentColor.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${town.displayName} (${town.distanceKm} km)",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = town.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMutedAsh
                    )
                }
                if (isPeakPhase) {
                    Surface(
                        color = SurplusGreen.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.border(1.dp, SurplusGreen, RoundedCornerShape(6.dp))
                    ) {
                        Text(
                            text = "PEAK DEMAND +25%",
                            style = MaterialTheme.typography.labelSmall,
                            color = SurplusGreen,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val canSell1 = hasAmount(town.favoredResource1)
                Button(
                    onClick = { onSellResource(town.favoredResource1) },
                    enabled = canSell1,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = town.accentColor,
                        contentColor = ObsidianCharcoal
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("sell_${town.id}_${town.favoredResource1.name}")
                ) {
                    Text(
                        text = "SELL 30 ${town.favoredResource1.shortLabel} (+${quote1.toInt()}G)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                val canSell2 = hasAmount(town.favoredResource2)
                Button(
                    onClick = { onSellResource(town.favoredResource2) },
                    enabled = canSell2,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = XpGold,
                        contentColor = ObsidianCharcoal
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("sell_${town.id}_${town.favoredResource2.name}")
                ) {
                    Text(
                        text = "SELL 30 ${town.favoredResource2.shortLabel} (+${quote2.toInt()}G)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun RareSyndicateItemCard(
    item: RareSyndicateItem,
    uiState: OutpostUiState,
    onBuy: () -> Unit
) {
    val state = uiState.state
    val price = OutpostCalculator.computeRareItemGoldPrice(item, uiState.skillsMap)
    val canAfford = state.gold >= price
    val owned = when (item.resourceType) {
        ResourceType.THORIUM_RODS -> state.thoriumRods.toInt()
        ResourceType.AETHER_CELLS -> state.aetherCells.toInt()
        ResourceType.MUTAGEN_CORES -> state.mutagenCores.toInt()
        else -> 0
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = BunkerSlate
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, item.resourceType.color.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = item.resourceType.color,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${item.title} (Owned: $owned)",
                        style = MaterialTheme.typography.titleSmall,
                        color = item.resourceType.color,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.usageSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMutedAsh
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onBuy,
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(
                    containerColor = XpGold,
                    contentColor = ObsidianCharcoal
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("buy_rare_${item.id}")
            ) {
                Text(
                    text = "BUY (${price.toInt()} GOLD)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PowerGridSummaryCard(
    uiState: OutpostUiState,
    onInjectThoriumOverdrive: () -> Unit
) {
    val state = uiState.state
    val prod = uiState.production
    val overdriveRemaining = ((state.thoriumOverdriveUntilMillis - uiState.nowMillis) / 1000L).coerceAtLeast(0L)
    val canOverdrive = state.thoriumRods >= 1.0 && overdriveRemaining == 0L
    val effPercent = (prod.powerGridEfficiencyMultiplier * 100).roundToInt()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = ElevatedSteel
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, WarningOrange.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "OUTPOST POWER GRID: ${prod.totalMegawattGenerated.roundToInt()} MW / ${prod.totalMegawattDemand.roundToInt()} MW LOAD",
                        style = MaterialTheme.typography.titleSmall,
                        color = WarningOrange,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Grid Output Multiplier: $effPercent% (Maintain >125% MW supply for Grid Overclock!)",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (effPercent >= 100) SurplusGreen else HazardRed
                    )
                }
            }

            Button(
                onClick = onInjectThoriumOverdrive,
                enabled = canOverdrive,
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarningOrange,
                    contentColor = ObsidianCharcoal
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("thorium_overdrive_button")
            ) {
                Text(
                    text = if (overdriveRemaining > 0L) {
                        "THORIUM CORE OVERDRIVE ACTIVE (${overdriveRemaining}s) • +60% MW"
                    } else {
                        "INJECT 1 THORIUM ROD (+60% GRID MW FOR 60s) [OWNED: ${state.thoriumRods.toInt()}]"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PowerStationCard(
    station: PowerStationType,
    uiState: OutpostUiState,
    onUpgrade: () -> Unit
) {
    val state = uiState.state
    val cost = OutpostCalculator.computePowerStationUpgradeCost(station, state, uiState.skillsMap)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = BunkerSlate
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, station.accentColor.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = station.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "+${station.baseMegawattsPerLevel.toInt()} MW/Lv • ${station.description}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMutedAsh
                    )
                    Text(
                        text = "🔥 Reactor Fuel Burn: -${cost.currentFuelUpkeepPerSec}/s Fuel -> Lv.${cost.currentLevel + 1}: -${cost.nextLevelFuelUpkeepPerSec}/s Fuel (Higher reactor levels burn more fuel!)",
                        style = MaterialTheme.typography.labelSmall,
                        color = WarningOrange,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Surface(
                    color = station.accentColor.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.border(1.dp, station.accentColor, RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = "LV. ${cost.currentLevel}",
                        style = MaterialTheme.typography.labelMedium,
                        color = station.accentColor,
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
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    StationCostPill("${cost.scrapCost.toInt()} SCRAP", state.scrap >= cost.scrapCost, SolarAmber)
                    StationCostPill("${cost.goldCost.toInt()} GOLD", state.gold >= cost.goldCost, XpGold)
                    if (cost.thoriumCost > 0) {
                        StationCostPill("${cost.thoriumCost.toInt()} THORIUM", state.thoriumRods >= cost.thoriumCost, WarningOrange)
                    }
                    if (cost.aetherCost > 0) {
                        StationCostPill("${cost.aetherCost.toInt()} AETHER", state.aetherCells >= cost.aetherCost, CryoCyan)
                    }
                    if (cost.coresCost > 0) {
                        StationCostPill("${cost.coresCost.toInt()} CORES", state.mutagenCores >= cost.coresCost, BioluminescentViolet)
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onUpgrade,
                    enabled = cost.canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = station.accentColor,
                        contentColor = ObsidianCharcoal
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("upgrade_station_${station.id}")
                ) {
                    Text(
                        text = if (cost.currentLevel == 0) "BUILD" else "UPGRADE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ChronoPhaseBeaconsCard(
    uiState: OutpostUiState,
    onTriggerSolarFlare: () -> Unit,
    onTriggerEclipseShroud: () -> Unit
) {
    val state = uiState.state
    val now = uiState.nowMillis
    val flareCd = ((state.solarFlareCooldownUntilMillis - now) / 1000L).coerceAtLeast(0L)
    val shroudCd = ((state.eclipseShroudCooldownUntilMillis - now) / 1000L).coerceAtLeast(0L)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = BunkerSlate
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, RadTeal.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "CIRCADIAN PHASE SHIFT BEACONS",
                style = MaterialTheme.typography.titleSmall,
                color = RadTeal,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onTriggerSolarFlare,
                    enabled = flareCd == 0L && state.bioFuel >= 20.0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SolarAmber,
                        contentColor = ObsidianCharcoal
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("solar_flare_button")
                ) {
                    Icon(Icons.Default.WbSunny, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (flareCd > 0L) "FLARE (${flareCd}s)" else "SOLAR FLARE (20 FUEL)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onTriggerEclipseShroud,
                    enabled = shroudCd == 0L && state.bioFuel >= 25.0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BioluminescentViolet,
                        contentColor = ObsidianCharcoal
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("eclipse_shroud_button")
                ) {
                    Icon(Icons.Default.NightlightRound, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (shroudCd > 0L) "SHROUD (${shroudCd}s)" else "ECLIPSE (25 FUEL)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StationCostPill(
    label: String,
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
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (affordable) color else HazardRed,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
