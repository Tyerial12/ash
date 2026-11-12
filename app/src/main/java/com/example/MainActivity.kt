package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.OutpostRepository
import com.example.data.local.OutpostDatabase
import com.example.data.remote.PublicVaultRepository
import com.example.model.LevelUpBannerState
import com.example.model.OfflineReport
import com.example.model.ResourceType
import com.example.notifications.VaultNotificationHelper
import com.example.ui.components.VaultAlertsTabScreen
import com.example.ui.components.WastelandSkyHeader
import com.example.ui.components.countActiveVaultAlerts
import com.example.ui.components.formatCompact
import com.example.widget.VaultStatusAppWidgetProvider
import com.example.ui.screens.CraftHuntDefenseTabScreen
import com.example.ui.screens.CrewAndPowerTabScreen
import com.example.ui.screens.CurrentResourcesTabScreen
import com.example.ui.screens.OutpostTabScreen
import com.example.ui.screens.VaultStatsTabScreen
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
    VAULT(R.string.tab_vault_core, Icons.Filled.Apartment, Icons.Outlined.Apartment, "nav_tab_vault"),
    OPERATIONS(R.string.tab_operations, Icons.Filled.Gavel, Icons.Outlined.Gavel, "nav_tab_operations"),
    CREW(R.string.tab_dwellers_expeditions, Icons.Filled.Groups, Icons.Outlined.Groups, "nav_tab_crew"),
    DEFENSE(R.string.tab_defense_security, Icons.Filled.Security, Icons.Outlined.Security, "nav_tab_defense"),
    LOGISTICS(R.string.tab_logistics_trade, Icons.Filled.Inventory2, Icons.Outlined.Inventory2, "nav_tab_logistics"),
    ALERTS(R.string.tab_alerts, Icons.Filled.NotificationsActive, Icons.Outlined.Notifications, "nav_tab_alerts")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        VaultNotificationHelper.ensureNotificationChannel(applicationContext)

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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val auth = remember {
        runCatching { FirebaseAuth.getInstance() }.getOrNull()
    }
    var currentUser by remember { mutableStateOf<FirebaseUser?>(auth?.currentUser) }
    var authErrorMessage by remember { mutableStateOf<String?>(null) }

    val credentialManager = remember {
        runCatching { CredentialManager.create(context) }.getOrNull()
    }
    val webClientId = remember {
        runCatching { context.getString(R.string.default_web_client_id) }.getOrDefault("")
    }

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

    // Only start Firestore Cloud Sync & Multiplayer listeners when signed into Google!
    LaunchedEffect(currentUser) {
        val user = currentUser
        if (user != null) {
            viewModel.startPublicNetworkSync(
                userId = user.uid,
                overseerName = user.displayName ?: "Overseer"
            )
        }
    }

    suspend fun handleSignInResult(credential: Credential) {
        if (auth == null) {
            authErrorMessage = "Cloud authentication is unavailable on this device."
            return
        }
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()
            currentUser = authResult.user
            authErrorMessage = null
        } else {
            authErrorMessage = "Unexpected credential type."
        }
    }

    fun launchGoogleSignIn(activityContext: Context) {
        if (credentialManager == null || webClientId.isBlank()) {
            authErrorMessage = "Google Sign-In client not configured."
            return
        }
        coroutineScope.launch {
            authErrorMessage = null
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
                // User cancelled Google Sign-In dialog
            } catch (e: Exception) {
                authErrorMessage = e.localizedMessage ?: "Google Sign-In failed."
            }
        }
    }

    // You do NOT have to log into Google to play the game!
    // Without Google Sign-In, Prestige, Cloud Save, and Multiplayer are gated.
    AshfallOutpostApp(
        viewModel = viewModel,
        isGoogleSignedIn = currentUser != null,
        overseerName = currentUser?.displayName ?: "Overseer",
        userEmail = currentUser?.email ?: currentUser?.displayName,
        userId = currentUser?.uid ?: "local",
        authErrorMessage = authErrorMessage,
        onRequestGoogleSignIn = { launchGoogleSignIn(context) },
        onSignOutGoogle = {
            auth?.signOut()
            currentUser = null
            viewModel.stopPublicNetworkSync()
        }
    )
}

@Composable
fun AshfallOutpostApp(
    viewModel: OutpostViewModel,
    isGoogleSignedIn: Boolean = false,
    overseerName: String = "Overseer",
    userEmail: String? = null,
    userId: String = "local",
    authErrorMessage: String? = null,
    onRequestGoogleSignIn: () -> Unit = {},
    onSignOutGoogle: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val latestUiState by rememberUpdatedState(uiState)

    var currentTab by rememberSaveable { mutableStateOf(OutpostDestination.VAULT) }
    var showRestartDialog by rememberSaveable { mutableStateOf(false) }

    // Request POST_NOTIFICATIONS runtime permission on Android 13+ so offline phone alerts can post
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                runCatching {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    // Schedule offline phone notifications when the game leaves foreground (ON_STOP)
    // and cancel pending alarms when the game is loaded (ON_START), while keeping the Home Screen Phone Widget live-synced
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    VaultNotificationHelper.cancelScheduledOfflineNotifications(context)
                    val snapshotState = latestUiState
                    VaultStatusAppWidgetProvider.pushLiveStateToWidgets(
                        context = context,
                        state = snapshotState.state,
                        production = snapshotState.production
                    )
                }
                Lifecycle.Event.ON_STOP -> {
                    val snapshotState = latestUiState
                    VaultNotificationHelper.scheduleOfflineNotifications(
                        context = context,
                        state = snapshotState.state,
                        production = snapshotState.production,
                        activeConstruction = snapshotState.activeConstruction
                    )
                    VaultStatusAppWidgetProvider.pushLiveStateToWidgets(
                        context = context,
                        state = snapshotState.state,
                        production = snapshotState.production
                    )
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    BackHandler(enabled = currentTab != OutpostDestination.VAULT) {
        currentTab = OutpostDestination.VAULT
    }

    val state = uiState.state
    val prod = uiState.production

    // Keep the installed Home Screen Phone Widget synchronized as resources and status change,
    // AND automatically refresh the widget every 10 seconds!
    LaunchedEffect(
        state.dayCounter,
        state.commandLevel,
        state.scrap.toInt() / 5,
        state.water.toInt() / 5,
        state.rations.toInt() / 5,
        state.bioFuel.toInt() / 2,
        state.gold.toInt() / 5,
        state.ammoRounds,
        prod.isVaultBlackout,
        prod.isDefenseShutDownByDeficit,
        prod.isStoreRoomFull,
        prod.totalDefenseRating,
        state.dwellerHealthPercent,
        state.dwellerStaminaPercent,
        state.oxygenPercent
    ) {
        VaultStatusAppWidgetProvider.pushLiveStateToWidgets(
            context = context,
            state = state,
            production = prod
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(VaultStatusAppWidgetProvider.AUTO_REFRESH_INTERVAL_MILLIS)
            val snapshotState = latestUiState
            VaultStatusAppWidgetProvider.pushLiveStateToWidgets(
                context = context,
                state = snapshotState.state,
                production = snapshotState.production
            )
        }
    }

    if (state.isGameOver) {
        VaultGameOverDialog(
            dayReached = state.dayCounter,
            commandLevel = state.commandLevel,
            prestigeCores = state.prestigeCores,
            reason = state.gameOverReason,
            isGoogleSignedIn = isGoogleSignedIn,
            onRequestGoogleSignIn = onRequestGoogleSignIn,
            onFullReset = {
                showRestartDialog = false
                viewModel.restartGame(keepPrestige = false)
            },
            onLegacyReset = {
                if (isGoogleSignedIn) {
                    showRestartDialog = false
                    viewModel.restartGame(keepPrestige = true)
                } else {
                    onRequestGoogleSignIn()
                }
            }
        )
    } else if (showRestartDialog) {
        RestartGameConfirmDialog(
            dayCounter = state.dayCounter,
            commandLevel = state.commandLevel,
            prestigeCores = state.prestigeCores,
            isGoogleSignedIn = isGoogleSignedIn,
            onRequestGoogleSignIn = onRequestGoogleSignIn,
            onDismiss = { showRestartDialog = false },
            onFullReset = {
                showRestartDialog = false
                viewModel.restartGame(keepPrestige = false)
            },
            onLegacyReset = {
                if (isGoogleSignedIn) {
                    showRestartDialog = false
                    viewModel.restartGame(keepPrestige = true)
                } else {
                    onRequestGoogleSignIn()
                }
            }
        )
    }

    uiState.offlineReport?.let { report ->
        OfflineReportDialog(
            report = report,
            onDismiss = viewModel::dismissOfflineReport
        )
    }

    val idleSurvivors = (state.totalSurvivors - prod.totalAssignedWorkers).coerceAtLeast(0)
    val canHireCandidate = state.totalSurvivors < prod.maxWorkerCapacity &&
        uiState.hireableCandidates.any { state.gold >= it.goldSigningFee }
    val activeAlertCount = countActiveVaultAlerts(state, prod, uiState.activeRaidThreat)

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
                                    when (dest) {
                                        OutpostDestination.VAULT -> {
                                            if (state.availableSkillPoints > 0) {
                                                Badge(
                                                    containerColor = RadTeal,
                                                    contentColor = BunkerCharcoal
                                                ) {
                                                    Text("${state.availableSkillPoints}SP")
                                                }
                                            }
                                        }
                                        OutpostDestination.OPERATIONS -> {
                                            if (uiState.activeConstruction != null) {
                                                Badge(
                                                    containerColor = SolarAmber,
                                                    contentColor = BunkerCharcoal
                                                ) {
                                                    Text("${uiState.activeConstruction?.remainingSeconds ?: 0}s")
                                                }
                                            }
                                        }
                                        OutpostDestination.CREW -> {
                                            if (idleSurvivors > 0 || canHireCandidate || uiState.activeExpedition != null) {
                                                Badge(
                                                    containerColor = when {
                                                        idleSurvivors > 0 -> WarningOrange
                                                        canHireCandidate -> XpGold
                                                        else -> RadTeal
                                                    },
                                                    contentColor = BunkerCharcoal
                                                ) {
                                                    Text(
                                                        when {
                                                            idleSurvivors > 0 -> "${idleSurvivors}IDLE"
                                                            canHireCandidate -> "HIRE"
                                                            else -> "${uiState.activeExpedition?.remainingSeconds ?: 0}s"
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                        OutpostDestination.DEFENSE -> {
                                            if (uiState.activeCrafting != null || uiState.activeDoorUpgrade != null) {
                                                Badge(containerColor = SolarAmber, contentColor = BunkerCharcoal) {
                                                    Text(
                                                        "${uiState.activeCrafting?.remainingSeconds ?: uiState.activeDoorUpgrade?.remainingSeconds ?: 0}s"
                                                    )
                                                }
                                            }
                                        }
                                        OutpostDestination.LOGISTICS -> {
                                            if (state.rawSalvageCaches > 0) {
                                                Badge(
                                                    containerColor = SurplusGreen,
                                                    contentColor = BunkerCharcoal
                                                ) {
                                                    Text("${state.rawSalvageCaches}")
                                                }
                                            }
                                        }
                                        OutpostDestination.ALERTS -> {
                                            if (activeAlertCount > 0) {
                                                Badge(
                                                    containerColor = HazardRed,
                                                    contentColor = TextPrimary
                                                ) {
                                                    Text("$activeAlertCount")
                                                }
                                            }
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
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
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
                    activeExpedition = uiState.activeExpedition,
                    activeAlertCount = activeAlertCount,
                    onOpenAlertsTab = { currentTab = OutpostDestination.ALERTS },
                    onRequestRestartGame = { showRestartDialog = true },
                    onQuickStabilizeAll = viewModel::quickStabilizeAllHazards,
                    onForageRations = viewModel::forageSubterraneanRations,
                    onBoilWater = viewModel::boilAquiferWater,
                    onFieldPressAmmo = viewModel::fieldPressEmergencyAmmo,
                    onCrankDynamo = viewModel::crankEmergencyDynamo,
                    onScrubAir = viewModel::scrubVaultAirManual
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
                        OutpostDestination.VAULT -> VaultStatsTabScreen(
                            uiState = uiState,
                            onSetPosture = viewModel::setOperationalPosture,
                            onWeldRoomPipes = viewModel::weldRoomPipesAndWear,
                            onTreatHuntingInjuries = viewModel::treatHuntingInjuries,
                            onUseMedkit = viewModel::useMedkit,
                            onUseRadAway = viewModel::useRadAway,
                            onEmergencyManualCrank = viewModel::crankEmergencyDynamo,
                            onUnlockOrUpgradeSkill = viewModel::unlockOrUpgradeSkill,
                            onPerformPrestige = {
                                if (isGoogleSignedIn) {
                                    viewModel.performOverseerPrestige()
                                } else {
                                    onRequestGoogleSignIn()
                                }
                            },
                            onCommissionEdenCoreModule = viewModel::commissionEdenCoreModule,
                            onAuditCertificationTier = viewModel::auditCertificationTier,
                            onNavigateToTab = { tabKey ->
                                when (tabKey) {
                                    "operations" -> currentTab = OutpostDestination.OPERATIONS
                                    "crew" -> currentTab = OutpostDestination.CREW
                                    "defense" -> currentTab = OutpostDestination.DEFENSE
                                    "logistics" -> currentTab = OutpostDestination.LOGISTICS
                                    "alerts" -> currentTab = OutpostDestination.ALERTS
                                    else -> currentTab = OutpostDestination.VAULT
                                }
                            },
                            onQuickStabilizeAll = viewModel::quickStabilizeAllHazards,
                            onForageRations = viewModel::forageSubterraneanRations,
                            onBoilWater = viewModel::boilAquiferWater,
                            onFieldPressAmmo = viewModel::fieldPressEmergencyAmmo,
                            onScrubAir = viewModel::scrubVaultAirManual,
                            onIssueEmergencyRations = viewModel::issueEmergencyWorkerRations,
                            onToggleRestRotation = viewModel::toggleRestRotation,
                            onCureSickAndExhaustedCrew = viewModel::cureSickAndExhaustedCrew,
                            onSetGridLoadBreakerMode = viewModel::setGridLoadBreakerMode,
                            isGoogleSignedIn = isGoogleSignedIn,
                            onRequestGoogleSignIn = onRequestGoogleSignIn,
                            onPublishVault = {
                                if (isGoogleSignedIn) {
                                    viewModel.publishMyVaultToNetwork(
                                        overseerName = overseerName,
                                        vaultName = "Vault City #${userId.take(4).uppercase()}"
                                    )
                                } else {
                                    onRequestGoogleSignIn()
                                }
                            }
                        )
                        OutpostDestination.OPERATIONS -> OutpostTabScreen(
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
                            onIssueEmergencyRations = viewModel::issueEmergencyWorkerRations,
                            onSetOperationalPosture = viewModel::setOperationalPosture,
                            onCallEmergencyReliefDrop = viewModel::callEmergencyReliefDrop,
                            onRequestRestartGame = { showRestartDialog = true },
                            onSharpenPickaxe = viewModel::sharpenPickaxe,
                            onCoolMiningShaft = viewModel::coolMiningShaft,
                            onClearCaveIn = viewModel::clearCaveInHazard,
                            onWeldRoomPipes = viewModel::weldRoomPipesAndWear,
                            onRepairSabotagedBuilding = viewModel::repairSabotagedBuilding,
                            onExecuteDeepCoreDrill = viewModel::executeDeepCoreDrill,
                            onStrikeSubterraneanBoss = viewModel::strikeSubterraneanBoss,
                            onSelectRationingPolicy = viewModel::selectRationingPolicy,
                            onToggleBuildingMothball = viewModel::toggleBuildingMothball,
                            onMothballNonEssentialRooms = viewModel::mothballAllNonEssentialRooms,
                            onResumeAllMothballedRooms = viewModel::resumeAllMothballedRooms,
                            onClaimVaultDirective = viewModel::claimCurrentVaultDirective
                        )
                        OutpostDestination.CREW -> CrewAndPowerTabScreen(
                            uiState = uiState,
                            onAssignWorker = viewModel::assignWorker,
                            onHireCandidate = viewModel::hireCandidate,
                            onRefreshBoard = viewModel::refreshHiringBoard,
                            onSendExpedition = viewModel::sendDwellerExpedition,
                            onToggleAutoShift = viewModel::toggleAutoShift,
                            onOptimizeCrewNow = viewModel::optimizeCrewForCurrentPhase,
                            onLaunchRescueBeacon = viewModel::launchRescueBeacon,
                            onIssueEmergencyRations = viewModel::issueEmergencyWorkerRations,
                            onToggleRestRotation = viewModel::toggleRestRotation,
                            onCureSickAndExhaustedCrew = viewModel::cureSickAndExhaustedCrew,
                            onSellToTown = { town, res -> viewModel.sellResourceToTown(town, res, 25.0) },
                            onBuyRareItem = viewModel::buyRareSyndicateItem,
                            onDispatchConvoy = viewModel::dispatchConvoy,
                            onUpgradeConvoy = viewModel::upgradeConvoy,
                            onUpgradePowerStation = viewModel::upgradePowerStation,
                            onInjectThoriumOverdrive = viewModel::injectThoriumRodOverdrive,
                            onTriggerSolarFlare = viewModel::triggerSolarFlare,
                            onTriggerEclipseShroud = viewModel::triggerEclipseShroud,
                            onUnlockOrUpgradeSkill = viewModel::unlockOrUpgradeSkill,
                            onPerformPrestige = {
                                if (isGoogleSignedIn) {
                                    viewModel.performOverseerPrestige()
                                } else {
                                    onRequestGoogleSignIn()
                                }
                            },
                            isAuthenticated = isGoogleSignedIn,
                            userEmail = userEmail,
                            authErrorMessage = authErrorMessage,
                            onSignInClick = onRequestGoogleSignIn,
                            onSignOutClick = onSignOutGoogle,
                            onPublishVault = {
                                if (isGoogleSignedIn) {
                                    viewModel.publishMyVaultToNetwork(
                                        overseerName = overseerName,
                                        vaultName = "Vault City #${userId.take(4).uppercase()}"
                                    )
                                } else {
                                    onRequestGoogleSignIn()
                                }
                            },
                            onBroadcastSupplyDrop = {
                                if (isGoogleSignedIn) {
                                    viewModel.broadcastSupplyDrop(
                                        senderName = overseerName,
                                        resourceType = ResourceType.SCRAP,
                                        amount = 25
                                    )
                                } else {
                                    onRequestGoogleSignIn()
                                }
                            },
                            onClaimSupplyDrop = { drop ->
                                if (isGoogleSignedIn) {
                                    viewModel.claimPublicSupplyDrop(drop, overseerName)
                                } else {
                                    onRequestGoogleSignIn()
                                }
                            }
                        )
                        OutpostDestination.DEFENSE -> CraftHuntDefenseTabScreen(
                            uiState = uiState,
                            onCraftItem = viewModel::craftItem,
                            onAccelerateCrafting = viewModel::accelerateItemCrafting,
                            onRushCrafting = viewModel::rushItemCraftingWithGold,
                            onExecuteHunt = viewModel::executeHunt,
                            onExecuteFishing = viewModel::executeFishing,
                            onRepelRaid = viewModel::repelRaiderSiege,
                            onExecuteCountermeasure = viewModel::executeSiegeCountermeasure,
                            onRepairDefenses = viewModel::repairVaultDefenses,
                            onUpgradeBlastDoor = viewModel::upgradeBlastDoor,
                            onAccelerateDoorUpgrade = viewModel::accelerateBlastDoorUpgrade,
                            onRushDoorUpgrade = viewModel::rushBlastDoorUpgradeWithGold,
                            onToggleAutoDefense = viewModel::toggleAutoDefense,
                            onUseMedkit = viewModel::useMedkit,
                            onUseRadAway = viewModel::useRadAway,
                            onTreatHuntingInjuries = viewModel::treatHuntingInjuries,
                            onFieldPressAmmo = viewModel::fieldPressEmergencyAmmo,
                            onSelectTurretAmmoType = viewModel::selectTurretAmmoType,
                            onArmPerimeterCryoTrap = viewModel::armPerimeterCryoTrap
                        )
                        OutpostDestination.LOGISTICS -> CurrentResourcesTabScreen(
                            uiState = uiState,
                            onDismantleCache = viewModel::dismantleSalvageCache,
                            onSmeltGold = viewModel::smeltSalvageForGold,
                            onIssueEmergencyRations = viewModel::issueEmergencyWorkerRations,
                            onSellResourceToTown = { town, res, amt -> viewModel.sellResourceToTown(town, res, amt) },
                            onBuyRareSyndicateItem = viewModel::buyRareSyndicateItem
                        )
                        OutpostDestination.ALERTS -> VaultAlertsTabScreen(
                            uiState = uiState,
                            onNavigateToCrisis = { target ->
                                when {
                                    target.startsWith("operations") -> currentTab = OutpostDestination.OPERATIONS
                                    target.startsWith("crew") -> currentTab = OutpostDestination.CREW
                                    target.startsWith("defense") -> currentTab = OutpostDestination.DEFENSE
                                    target.startsWith("logistics") -> currentTab = OutpostDestination.LOGISTICS
                                    else -> currentTab = OutpostDestination.VAULT
                                }
                            },
                            onQuickRepairSabotage = viewModel::repairSabotagedBuilding,
                            onQuickCrankDynamo = viewModel::crankEmergencyDynamo,
                            onQuickTuneGenerators = viewModel::maintainGenerators,
                            onQuickStabilizeAll = viewModel::quickStabilizeAllHazards,
                            onForageRations = viewModel::forageSubterraneanRations,
                            onBoilWater = viewModel::boilAquiferWater,
                            onScrubAir = viewModel::scrubVaultAirManual
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RestartGameConfirmDialog(
    dayCounter: Int,
    commandLevel: Int,
    prestigeCores: Int,
    isGoogleSignedIn: Boolean = false,
    onRequestGoogleSignIn: () -> Unit = {},
    onDismiss: () -> Unit,
    onFullReset: () -> Unit,
    onLegacyReset: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SteelPanel,
        title = {
            Text(
                text = "RESTART VAULT SURVIVAL?",
                style = MaterialTheme.typography.titleLarge,
                color = WarningOrange,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "You are currently on Day $dayCounter (Vault Command Lv.$commandLevel). Choose how you want to restart your bunker:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Button(
                    onClick = onFullReset,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HazardRed,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_restart_full_button")
                ) {
                    Text(
                        text = "FULL FRESH RESET (DAY 1 • WIPE ALL)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = if (isGoogleSignedIn) onLegacyReset else onRequestGoogleSignIn,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SolarAmber,
                        contentColor = BunkerCharcoal
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_restart_legacy_button")
                ) {
                    Text(
                        text = if (isGoogleSignedIn) {
                            "KEEP PRESTIGE RESET ($prestigeCores CORES & LEGACY)"
                        } else {
                            "SIGN IN WITH GOOGLE TO PRESTIGE RESET"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElevatedSlate,
                    contentColor = TextPrimary
                ),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("cancel_restart_button")
            ) {
                Text("CANCEL", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun VaultGameOverDialog(
    dayReached: Int,
    commandLevel: Int,
    prestigeCores: Int,
    reason: String,
    isGoogleSignedIn: Boolean = false,
    onRequestGoogleSignIn: () -> Unit = {},
    onFullReset: () -> Unit,
    onLegacyReset: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Mandatory Game Over choice */ },
        containerColor = SteelPanel,
        title = {
            Text(
                text = "☠️ VAULT FALLEN — GAME OVER (DAY $dayReached)",
                style = MaterialTheme.typography.titleLarge,
                color = HazardRed,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    color = HazardRed.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, HazardRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("game_over_reason_card")
                ) {
                    Text(
                        text = reason.ifEmpty {
                            "Your Vault succumbed to the nuclear wasteland on Day $dayReached (Vault Command Lv.$commandLevel)."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Text(
                    text = "Launch a new Vault expedition from Day 1 with your pickaxe:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Button(
                    onClick = if (isGoogleSignedIn) onLegacyReset else onRequestGoogleSignIn,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SolarAmber,
                        contentColor = BunkerCharcoal
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("game_over_restart_legacy_button")
                ) {
                    Text(
                        text = if (isGoogleSignedIn) {
                            "NEW VAULT (KEEP $prestigeCores PRESTIGE CORES)"
                        } else {
                            "SIGN IN WITH GOOGLE TO KEEP PRESTIGE CORES"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = onFullReset,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HazardRed,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("game_over_restart_full_button")
                ) {
                    Text(
                        text = "HARDCORE WIPE (FRESH DAY 1 RESET)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {}
    )
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
