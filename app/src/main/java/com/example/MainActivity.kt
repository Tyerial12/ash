package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.OutpostRepository
import com.example.data.local.OutpostDatabase
import com.example.data.remote.PublicVaultRepository
import com.example.model.LevelUpBannerState
import com.example.model.OfflineReport
import com.example.model.ResourceType
import com.example.ui.components.WastelandSkyHeader
import com.example.ui.components.formatCompact
import com.example.ui.screens.CraftHuntDefenseTabScreen
import com.example.ui.screens.OutpostTabScreen
import com.example.ui.screens.SkillTreeTabScreen
import com.example.ui.screens.SurvivorsTabScreen
import com.example.ui.screens.TradePowerTabScreen
import com.example.ui.theme.BioluminescentViolet
import com.example.ui.theme.BunkerCharcoal
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.HazardRed
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PanelBorder
import com.example.ui.theme.RadTeal
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.SteelPanel
import com.example.ui.theme.SurplusGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningOrange
import com.example.ui.theme.XpGold
import com.example.viewmodel.OutpostViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.roundToInt

enum class OutpostDestination(
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    OUTPOST(R.string.tab_outpost, Icons.Filled.Apartment, Icons.Outlined.Apartment, "nav_tab_outpost"),
    ARMORY(R.string.tab_craft_hunt, Icons.Filled.Security, Icons.Outlined.Security, "nav_tab_armory"),
    SURVIVORS(R.string.tab_survivors, Icons.Filled.Groups, Icons.Outlined.Groups, "nav_tab_survivors"),
    NETWORK(R.string.tab_network, Icons.Filled.Public, Icons.Outlined.Public, "nav_tab_network"),
    SKILL_TREE(R.string.tab_skill_tree, Icons.Filled.AccountTree, Icons.Outlined.AccountTree, "nav_tab_skills")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = OutpostDatabase.getDatabase(applicationContext)
        val repository = OutpostRepository(database.outpostDao())
        val publicVaultRepository = runCatching {
            PublicVaultRepository(applicationContext)
        }.getOrNull()

        setContent {
            MyApplicationTheme {
                val outpostViewModel: OutpostViewModel = viewModel(
                    factory = OutpostViewModel.Factory(repository, publicVaultRepository)
                )
                VaultAuthGate(viewModel = outpostViewModel)
            }
        }
    }
}

@Composable
fun VaultAuthGate(
    viewModel: OutpostViewModel
) {
    val auth = remember {
        runCatching { FirebaseAuth.getInstance() }.getOrNull()
    }
    var currentUser by remember { mutableStateOf<FirebaseUser?>(auth?.currentUser) }

    DisposableEffect(auth) {
        if (auth == null) {
            onDispose { }
        } else {
            val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                currentUser = firebaseAuth.currentUser
            }
            auth.addAuthStateListener(listener)
            onDispose { auth.removeAuthStateListener(listener) }
        }
    }

    LaunchedEffect(currentUser) {
        val user = currentUser
        if (user != null) {
            viewModel.startPublicNetworkSync(
                userId = user.uid,
                overseerName = user.displayName ?: "Overseer"
            )
        }
    }

    if (auth != null && currentUser == null) {
        GoogleSignInScreen(
            auth = auth,
            onSignedIn = { user ->
                currentUser = user
                viewModel.startPublicNetworkSync(
                    userId = user.uid,
                    overseerName = user.displayName ?: "Overseer"
                )
            }
        )
    } else {
        AshfallOutpostApp(
            viewModel = viewModel,
            overseerName = currentUser?.displayName ?: "Overseer",
            userId = currentUser?.uid ?: "local"
        )
    }
}

@Composable
fun GoogleSignInScreen(
    auth: FirebaseAuth,
    onSignedIn: (FirebaseUser) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val credentialManager = remember { CredentialManager.create(context) }
    val webClientId = remember { context.getString(R.string.default_web_client_id) }

    suspend fun handleSignInResult(credential: Credential) {
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()
            authResult.user?.let(onSignedIn)
        } else {
            errorMessage = "Unexpected credential type."
        }
    }

    suspend fun launchBottomSheetSignIn(activityContext: Context) {
        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(webClientId)
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(true)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            val result = credentialManager.getCredential(activityContext, request)
            handleSignInResult(result.credential)
        } catch (_: NoCredentialException) {
            // Silent check found no saved credentials; user can tap button
        } catch (_: GetCredentialCancellationException) {
            // User dismissed bottom sheet
        } catch (e: Exception) {
            errorMessage = e.localizedMessage
        }
    }

    suspend fun launchButtonSignIn(activityContext: Context) {
        isLoading = true
        errorMessage = null
        try {
            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(
                serverClientId = webClientId
            ).build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()
            val result = credentialManager.getCredential(activityContext, request)
            handleSignInResult(result.credential)
        } catch (_: GetCredentialCancellationException) {
            // User cancelled
        } catch (e: Exception) {
            errorMessage = e.localizedMessage ?: "Google Sign-In failed."
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        launchBottomSheetSignIn(context)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BunkerCharcoal
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = SteelPanel,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, SolarAmber),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Public,
                        contentDescription = "Vault City Satellite Network",
                        tint = SolarAmber,
                        modifier = Modifier.size(44.dp)
                    )
                    Text(
                        text = "VAULT CITY PUBLIC OVERSEER NETWORK",
                        style = MaterialTheme.typography.titleMedium,
                        color = SolarAmber,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Sign in with Google to connect your underground shelter to the live Global Wasteland Leaderboard, broadcast public supply drops, and sync Overseer Ascension.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    errorMessage?.let { msg ->
                        Text(
                            text = msg,
                            color = HazardRed,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                launchButtonSignIn(context)
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SolarAmber,
                            contentColor = BunkerCharcoal
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("google_sign_in_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = BunkerCharcoal
                            )
                        } else {
                            Text(
                                text = "Sign in with Google",
                                style = MaterialTheme.typography.labelLarge,
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
fun AshfallOutpostApp(
    viewModel: OutpostViewModel,
    overseerName: String = "Overseer",
    userId: String = "local"
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentTab by rememberSaveable { mutableStateOf(OutpostDestination.OUTPOST) }

    BackHandler(enabled = currentTab != OutpostDestination.OUTPOST) {
        currentTab = OutpostDestination.OUTPOST
    }

    uiState.offlineReport?.let { report ->
        OfflineReportDialog(
            report = report,
            onDismiss = viewModel::dismissOfflineReport
        )
    }

    val state = uiState.state
    val prod = uiState.production
    val idleSurvivors = (state.totalSurvivors - prod.totalAssignedWorkers).coerceAtLeast(0)
    val canHireCandidate = state.totalSurvivors < prod.maxWorkerCapacity &&
        uiState.hireableCandidates.any { state.gold >= it.goldSigningFee }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BunkerCharcoal,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                containerColor = SteelPanel,
                contentColor = TextPrimary
            ) {
                OutpostDestination.entries.forEach { dest ->
                    val isSelected = currentTab == dest
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = dest },
                        modifier = Modifier.testTag(dest.tag),
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (dest == OutpostDestination.OUTPOST && (prod.isVaultBlackout || prod.isWorkersStarvingOrDehydrated || state.rawSalvageCaches > 0)) {
                                        Badge(
                                            containerColor = if (prod.isVaultBlackout || prod.isWorkersStarvingOrDehydrated) HazardRed else SolarAmber,
                                            contentColor = if (prod.isVaultBlackout || prod.isWorkersStarvingOrDehydrated) TextPrimary else BunkerCharcoal
                                        ) {
                                            Text(
                                                when {
                                                    prod.isVaultBlackout -> "MW!"
                                                    prod.isWorkersStarvingOrDehydrated -> "FOOD!"
                                                    else -> "${state.rawSalvageCaches}"
                                                }
                                            )
                                        }
                                    } else if (dest == OutpostDestination.ARMORY && (uiState.activeRaidThreat != null || state.needsDefenseRepair || uiState.activeDoorUpgrade != null)) {
                                        Badge(
                                            containerColor = if (uiState.activeRaidThreat != null) HazardRed else WarningOrange,
                                            contentColor = TextPrimary
                                        ) {
                                            Text(
                                                when {
                                                    uiState.activeRaidThreat != null -> "!"
                                                    uiState.activeDoorUpgrade != null -> "${uiState.activeDoorUpgrade?.remainingSeconds ?: 0}s"
                                                    else -> "FIX"
                                                }
                                            )
                                        }
                                    } else if (dest == OutpostDestination.SURVIVORS && (idleSurvivors > 0 || canHireCandidate || uiState.activeExpedition != null)) {
                                        Badge(containerColor = SurplusGreen, contentColor = BunkerCharcoal) {
                                            Text(
                                                when {
                                                    uiState.activeExpedition != null -> "${uiState.activeExpedition?.remainingSeconds ?: 0}s"
                                                    idleSurvivors > 0 -> "$idleSurvivors"
                                                    else -> "!"
                                                }
                                            )
                                        }
                                    } else if (dest == OutpostDestination.NETWORK && uiState.publicSupplyDrops.isNotEmpty()) {
                                        Badge(containerColor = SurplusGreen, contentColor = BunkerCharcoal) {
                                            Text("${uiState.publicSupplyDrops.size}")
                                        }
                                    } else if (dest == OutpostDestination.SKILL_TREE && (state.availableSkillPoints > 0 || state.prestigeCores > 0)) {
                                        Badge(containerColor = RadTeal, contentColor = BunkerCharcoal) {
                                            Text("${state.availableSkillPoints + state.prestigeCores}")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isSelected) dest.selectedIcon else dest.unselectedIcon,
                                    contentDescription = stringResource(dest.labelRes)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = stringResource(dest.labelRes),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SolarAmber,
                            selectedTextColor = SolarAmber,
                            indicatorColor = ElevatedSlate,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp)
            ) {
                WastelandSkyHeader(
                    state = state,
                    production = prod,
                    activeConstruction = uiState.activeConstruction,
                    activeCrafting = uiState.activeCrafting,
                    activeDoorUpgrade = uiState.activeDoorUpgrade,
                    activeExpedition = uiState.activeExpedition
                )

                AnimatedVisibility(
                    visible = uiState.levelUpBanner != null,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    uiState.levelUpBanner?.let { banner ->
                        LevelUpNotificationBanner(
                            banner = banner,
                            onDismiss = viewModel::dismissLevelUpBanner
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (currentTab) {
                        OutpostDestination.OUTPOST -> OutpostTabScreen(
                            uiState = uiState,
                            onHarvestNode = viewModel::harvestSalvageNode,
                            onDismantleCaches = viewModel::dismantleSalvageCache,
                            onSmeltSalvageForGold = viewModel::smeltSalvageForGold,
                            onProspectRichVein = viewModel::prospectRichVein,
                            onUpgradeSalvageRig = viewModel::upgradeSalvageRig,
                            onUpgradeBuilding = viewModel::upgradeBuilding,
                            onAccelerateConstruction = viewModel::accelerateBuildingConstruction,
                            onRushConstruction = viewModel::rushBuildingConstructionWithGold,
                            onUpgradePowerStation = viewModel::upgradePowerStation,
                            onCrankDynamo = viewModel::crankEmergencyDynamo,
                            onMaintainGenerators = viewModel::maintainGenerators,
                            onScrubAir = viewModel::scrubVaultAirManual,
                            onFlushRadiation = viewModel::useRadAway,
                            onAdvanceDay = viewModel::advanceDayManually,
                            onClaimAnomaly = viewModel::claimRareAnomaly,
                            onWalkToStation = viewModel::walkOverseerToStation,
                            onFishSpot = viewModel::executeFishing,
                            onIssueEmergencyRations = viewModel::issueEmergencyWorkerRations
                        )
                        OutpostDestination.ARMORY -> CraftHuntDefenseTabScreen(
                            uiState = uiState,
                            onCraftItem = viewModel::craftItem,
                            onAccelerateCrafting = viewModel::accelerateItemCrafting,
                            onRushCrafting = viewModel::rushItemCraftingWithGold,
                            onExecuteHunt = viewModel::executeHunt,
                            onExecuteFishing = viewModel::executeFishing,
                            onRepelRaid = viewModel::repelRaiderSiege,
                            onRepairDefenses = viewModel::repairVaultDefenses,
                            onUpgradeBlastDoor = viewModel::upgradeBlastDoor,
                            onAccelerateDoorUpgrade = viewModel::accelerateBlastDoorUpgrade,
                            onRushDoorUpgrade = viewModel::rushBlastDoorUpgradeWithGold,
                            onToggleAutoDefense = viewModel::toggleAutoDefense,
                            onUseMedkit = viewModel::useMedkit,
                            onUseRadAway = viewModel::useRadAway
                        )
                        OutpostDestination.SURVIVORS -> SurvivorsTabScreen(
                            uiState = uiState,
                            onHireCandidate = viewModel::hireCandidate,
                            onRefreshBoard = viewModel::refreshHiringBoard,
                            onSendExpedition = viewModel::sendDwellerExpedition,
                            onAdjustWorker = viewModel::assignWorker,
                            onToggleAutoShift = viewModel::toggleAutoShift,
                            onOptimizeCrew = viewModel::optimizeCrewForCurrentPhase
                        )
                        OutpostDestination.NETWORK -> TradePowerTabScreen(
                            uiState = uiState,
                            onSellToTown = { town, res -> viewModel.sellResourceToTown(town, res) },
                            onBuyRareItem = viewModel::buyRareSyndicateItem,
                            onDispatchConvoy = viewModel::dispatchConvoy,
                            onUpgradeConvoy = viewModel::upgradeConvoy,
                            onUpgradePowerStation = viewModel::upgradePowerStation,
                            onInjectThoriumOverdrive = viewModel::injectThoriumRodOverdrive,
                            onTriggerSolarFlare = viewModel::triggerSolarFlare,
                            onTriggerEclipseShroud = viewModel::triggerEclipseShroud,
                            onPublishVault = {
                                viewModel.publishMyVaultToNetwork(
                                    overseerName = overseerName,
                                    vaultName = "Vault City #${userId.take(4).uppercase()}"
                                )
                            },
                            onBroadcastSupplyDrop = {
                                viewModel.broadcastSupplyDrop(
                                    senderName = overseerName,
                                    resourceType = ResourceType.SCRAP,
                                    amount = 30
                                )
                            },
                            onClaimSupplyDrop = { drop ->
                                viewModel.claimPublicSupplyDrop(
                                    drop = drop,
                                    claimerName = overseerName
                                )
                            }
                        )
                        OutpostDestination.SKILL_TREE -> SkillTreeTabScreen(
                            uiState = uiState,
                            onUnlockSkill = viewModel::unlockOrUpgradeSkill,
                            onPerformPrestige = viewModel::performOverseerPrestige
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelUpNotificationBanner(
    banner: LevelUpBannerState,
    onDismiss: () -> Unit
) {
    val accent = if (banner.isCommandLevel) XpGold else RadTeal
    Surface(
        color = ElevatedSlate,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, accent),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable(onClick = onDismiss)
            .testTag("level_up_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.MilitaryTech,
                    contentDescription = "Level Up",
                    tint = accent,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = banner.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = accent,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${banner.subtitle} • ${banner.rewardSummary}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                }
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss Level Up Banner",
                    tint = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun OfflineReportDialog(
    report: OfflineReport,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SteelPanel,
        title = {
            Text(
                text = stringResource(R.string.offline_report_title),
                style = MaterialTheme.typography.titleLarge,
                color = SolarAmber
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Your Vault City shelter operated across ${report.cyclesCompleted} day cycle(s) (${report.elapsedSeconds}s away):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Surface(
                    color = ElevatedSlate,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, PanelBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "+${formatCompact(report.scrapEarned)} Scrap Metal",
                            style = MaterialTheme.typography.labelLarge,
                            color = SolarAmber
                        )
                        Text(
                            text = "+${formatCompact(report.waterEarned)} Purified Water",
                            style = MaterialTheme.typography.labelLarge,
                            color = RadTeal
                        )
                        Text(
                            text = "+${formatCompact(report.rationsEarned)} Food Rations",
                            style = MaterialTheme.typography.labelLarge,
                            color = SurplusGreen
                        )
                        if (report.goldEarned > 0.1) {
                            Text(
                                text = "+${report.goldEarned.toInt()} Gold Sovereigns",
                                style = MaterialTheme.typography.labelLarge,
                                color = XpGold
                            )
                        }
                        if (report.mutagenCoresEarned > 0.1) {
                            Text(
                                text = "+${formatCompact(report.mutagenCoresEarned)} Mutagen Cores (${report.anomaliesAutoHarvested} Anomalies)",
                                style = MaterialTheme.typography.labelLarge,
                                color = BioluminescentViolet
                            )
                        }
                        Text(
                            text = "+${report.outpostXpEarned.roundToInt()} Vault XP" +
                                if (report.levelsGained > 0) " (+${report.levelsGained} Level-Up!)" else "",
                            style = MaterialTheme.typography.labelLarge,
                            color = XpGold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SolarAmber,
                    contentColor = BunkerCharcoal
                ),
                modifier = Modifier.testTag("claim_offline_report_button")
            ) {
                Text(
                    text = stringResource(R.string.offline_report_claim),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}
