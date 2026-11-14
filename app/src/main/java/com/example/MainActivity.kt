package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.model.UiState
import com.example.data.repository.MahadevRepository
import com.example.ui.auth.authStateFlow
import com.example.ui.auth.signOutUser
import com.example.ui.components.AdminPinLockDialog
import com.example.ui.components.MahadevBrandHeader
import com.example.ui.components.OtpVerificationDialog
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AdminLedgerAndReportsScreen
import com.example.ui.screens.AdminRequestsScreen
import com.example.ui.screens.AdminUsersScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.OnboardingSignupScreen
import com.example.ui.screens.UserDashboardScreen
import com.example.ui.screens.UserNotificationsScreen
import com.example.ui.screens.UserProfileScreen
import com.example.ui.screens.UserSupportScreen
import com.example.ui.theme.CardBorderBlue
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.MahadevPanelTheme
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MahadevViewModel
import com.example.ui.viewmodel.PanelMode
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MahadevPanelTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        key(user.uid) {
            MahadevAuthenticatedRoot(firebaseUser = user)
        }
    }
}

private data class NavTabItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int = 0
)

@Composable
fun MahadevAuthenticatedRoot(firebaseUser: FirebaseUser) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val viewModel: MahadevViewModel = viewModel(
        key = firebaseUser.uid,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                MahadevViewModel(
                    repository = MahadevRepository(db),
                    currentUserId = firebaseUser.uid,
                    authEmail = firebaseUser.email.orEmpty(),
                    authDisplayName = firebaseUser.displayName.orEmpty()
                )
            }
        }
    )

    val profileState by viewModel.userProfileState.collectAsStateWithLifecycle()
    val adminRecord by viewModel.adminRecord.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isUserAdmin.collectAsStateWithLifecycle()
    val isAdminUnlocked by viewModel.isAdminUnlocked.collectAsStateWithLifecycle()
    val panelMode by viewModel.panelMode.collectAsStateWithLifecycle()
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()
    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()
    val otpChallenge by viewModel.activeOtpChallenge.collectAsStateWithLifecycle()

    val userRequestsState by viewModel.userRequestsState.collectAsStateWithLifecycle()
    val userTransactionsState by viewModel.userTransactionsState.collectAsStateWithLifecycle()
    val userNotificationsState by viewModel.userNotificationsState.collectAsStateWithLifecycle()
    val userSupportTicketsState by viewModel.userSupportTicketsState.collectAsStateWithLifecycle()

    val allUsersState by viewModel.allUsersState.collectAsStateWithLifecycle()
    val allRequestsState by viewModel.allRequestsState.collectAsStateWithLifecycle()
    val allTransactionsState by viewModel.allTransactionsState.collectAsStateWithLifecycle()
    val allSupportTicketsState by viewModel.allSupportTicketsState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showAdminPinDialog by remember { mutableStateOf(false) }
    var onboardingOtpVerified by remember { mutableStateOf(false) }

    var userTabIndex by remember { mutableIntStateOf(0) }
    var adminTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(actionMessage) {
        val msg = actionMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearActionMessage()
        }
    }

    BackHandler(enabled = panelMode == PanelMode.ADMIN || userTabIndex != 0 || adminTabIndex != 0) {
        if (panelMode == PanelMode.ADMIN) {
            if (adminTabIndex != 0) {
                adminTabIndex = 0
            } else {
                viewModel.switchPanelMode(PanelMode.USER)
            }
        } else if (userTabIndex != 0) {
            userTabIndex = 0
        }
    }

    val openAdminRequest: () -> Unit = {
        if (isAdminUnlocked) {
            viewModel.switchPanelMode(PanelMode.ADMIN)
        } else {
            showAdminPinDialog = true
        }
    }

    val handleSignOut: () -> Unit = {
        signOutUser(credentialManager = credentialManager, scope = scope)
    }

    when (val pState = profileState) {
        is UiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ObsidianBlack),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = RoyalGold)
            }
        }

        is UiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ObsidianBlack)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Unable to load Mahadev Panel profile", color = RoyalGold)
                    Text(pState.message, color = TextSecondary)
                    com.example.ui.components.BlueOutlineButton(text = "Sign Out", onClick = handleSignOut)
                }
            }
        }

        is UiState.Success -> {
            val profile = pState.data
            if (profile == null) {
                Scaffold(
                    containerColor = ObsidianBlack,
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        OnboardingSignupScreen(
                            initialName = firebaseUser.displayName.orEmpty(),
                            email = firebaseUser.email.orEmpty(),
                            isAdminAccount = isAdmin,
                            isBusy = isBusy,
                            otpVerified = onboardingOtpVerified,
                            onRequestOtp = { hint -> viewModel.generateOtpChallenge(hint) },
                            onCompleteSignup = { name, phone, pin ->
                                viewModel.registerProfile(name, phone, pin, onboardingOtpVerified)
                            },
                            onSignOut = handleSignOut
                        )
                    }
                }
            } else {
                val unreadNotifications = (userNotificationsState as? UiState.Success)?.data?.count { !it.isRead } ?: 0
                val pendingAdminRequests = (allRequestsState as? UiState.Success)?.data?.count { it.status == "pending" } ?: 0

                val userTabs = listOf(
                    NavTabItem("Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
                    NavTabItem("Alerts", Icons.Filled.Notifications, Icons.Outlined.Notifications, unreadNotifications),
                    NavTabItem("Support", Icons.Filled.SupportAgent, Icons.Outlined.SupportAgent),
                    NavTabItem("Profile", Icons.Filled.Person, Icons.Outlined.Person)
                )

                val adminTabs = listOf(
                    NavTabItem("Stats", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
                    NavTabItem("Users", Icons.Filled.Group, Icons.Outlined.Group),
                    NavTabItem("Requests", Icons.Filled.PendingActions, Icons.Outlined.PendingActions, pendingAdminRequests),
                    NavTabItem("Reports", Icons.Filled.Assessment, Icons.Outlined.Dashboard)
                )

                val activeTabs = if (panelMode == PanelMode.ADMIN && isAdmin) adminTabs else userTabs
                val selectedIndex = if (panelMode == PanelMode.ADMIN && isAdmin) adminTabIndex else userTabIndex

                BoxWithConstraints(modifier = Modifier.fillMaxSize().background(ObsidianBlack)) {
                    val useRail = maxWidth >= 600.dp

                    Scaffold(
                        containerColor = ObsidianBlack,
                        contentWindowInsets = WindowInsets.safeDrawing,
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        topBar = {
                            Surface(
                                color = MidnightSurface,
                                border = BorderStroke(1.dp, CardBorderBlue)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .statusBarsPadding()
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    MahadevBrandHeader(
                                        subtitle = if (panelMode == PanelMode.ADMIN && isAdmin) "Admin Console" else "User Portal",
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isAdmin) {
                                        Surface(
                                            onClick = {
                                                if (panelMode == PanelMode.ADMIN) {
                                                    viewModel.switchPanelMode(PanelMode.USER)
                                                } else {
                                                    openAdminRequest()
                                                }
                                            },
                                            shape = RoundedCornerShape(999.dp),
                                            color = if (panelMode == PanelMode.ADMIN) GoldContainer else MidnightSurface,
                                            border = BorderStroke(1.dp, if (panelMode == PanelMode.ADMIN) RoyalGold else GoldBorder),
                                            modifier = Modifier.testTag("mode_switch_chip")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.AdminPanelSettings,
                                                    contentDescription = "Switch Panel Mode",
                                                    tint = RoyalGold,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = if (panelMode == PanelMode.ADMIN) "User App" else "Admin",
                                                    color = RoyalGold,
                                                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        bottomBar = {
                            if (!useRail) {
                                NavigationBar(
                                    containerColor = MidnightSurface,
                                    tonalElevation = 0.dp,
                                    modifier = Modifier.navigationBarsPadding()
                                ) {
                                    activeTabs.forEachIndexed { idx, tab ->
                                        val isSelected = selectedIndex == idx
                                        NavigationBarItem(
                                            selected = isSelected,
                                            onClick = {
                                                if (panelMode == PanelMode.ADMIN && isAdmin) adminTabIndex = idx
                                                else userTabIndex = idx
                                            },
                                            icon = {
                                                BadgedBox(
                                                    badge = {
                                                        if (tab.badgeCount > 0) {
                                                            Badge(containerColor = RoyalGold, contentColor = ObsidianBlack) {
                                                                Text(tab.badgeCount.toString())
                                                            }
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                                        contentDescription = tab.label
                                                    )
                                                }
                                            },
                                            label = { Text(tab.label) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = RoyalGold,
                                                selectedTextColor = RoyalGold,
                                                indicatorColor = GoldContainer,
                                                unselectedIconColor = TextSecondary,
                                                unselectedTextColor = TextSecondary
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            if (useRail) {
                                NavigationRail(
                                    containerColor = MidnightSurface,
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    activeTabs.forEachIndexed { idx, tab ->
                                        val isSelected = selectedIndex == idx
                                        NavigationRailItem(
                                            selected = isSelected,
                                            onClick = {
                                                if (panelMode == PanelMode.ADMIN && isAdmin) adminTabIndex = idx
                                                else userTabIndex = idx
                                            },
                                            icon = {
                                                BadgedBox(
                                                    badge = {
                                                        if (tab.badgeCount > 0) {
                                                            Badge(containerColor = RoyalGold, contentColor = ObsidianBlack) {
                                                                Text(tab.badgeCount.toString())
                                                            }
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                                        contentDescription = tab.label
                                                    )
                                                }
                                            },
                                            label = { Text(tab.label) },
                                            colors = NavigationRailItemDefaults.colors(
                                                selectedIconColor = RoyalGold,
                                                selectedTextColor = RoyalGold,
                                                indicatorColor = GoldContainer,
                                                unselectedIconColor = TextSecondary,
                                                unselectedTextColor = TextSecondary
                                            )
                                        )
                                    }
                                }
                            }

                            Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                                if (panelMode == PanelMode.ADMIN && isAdmin) {
                                    when (adminTabIndex) {
                                        0 -> AdminDashboardScreen(
                                            usersState = allUsersState,
                                            requestsState = allRequestsState,
                                            transactionsState = allTransactionsState,
                                            ticketsState = allSupportTicketsState,
                                            onNavigateToUsers = { adminTabIndex = 1 },
                                            onNavigateToRequests = { adminTabIndex = 2 },
                                            onNavigateToReports = { adminTabIndex = 3 },
                                            onLockAdmin = { viewModel.lockAdminConsole() }
                                        )
                                        1 -> AdminUsersScreen(
                                            usersState = allUsersState,
                                            currentAdminUid = firebaseUser.uid,
                                            onUpdateUser = { u, st, lvl, role -> viewModel.adminUpdateUser(u, st, lvl, role) },
                                            onSendNotification = { uid, t, m -> viewModel.adminSendNotification(uid, t, m) }
                                        )
                                        2 -> AdminRequestsScreen(
                                            requestsState = allRequestsState,
                                            onResolveRequest = { req, app, note -> viewModel.adminResolveRequest(req, app, note) }
                                        )
                                        else -> AdminLedgerAndReportsScreen(
                                            usersState = allUsersState,
                                            requestsState = allRequestsState,
                                            transactionsState = allTransactionsState,
                                            ticketsState = allSupportTicketsState,
                                            onRecordTransaction = { u, t, c, amt, note ->
                                                viewModel.adminRecordTransaction(u, t, c, amt, note)
                                            },
                                            onReplyTicket = { ticket, st, reply ->
                                                viewModel.adminReplyTicket(ticket, st, reply)
                                            }
                                        )
                                    }
                                } else {
                                    when (userTabIndex) {
                                        0 -> UserDashboardScreen(
                                            profile = profile,
                                            isAdmin = isAdmin,
                                            requestsState = userRequestsState,
                                            transactionsState = userTransactionsState,
                                            unreadNotifications = unreadNotifications,
                                            onRequestOtp = { viewModel.generateOtpChallenge(profile.phone.ifBlank { profile.email }) },
                                            onCreateRequest = { t, title, desc, amt ->
                                                viewModel.submitRequest(profile, t, title, desc, amt)
                                            },
                                            onOpenNotifications = { userTabIndex = 1 },
                                            onOpenSupport = { userTabIndex = 2 },
                                            onOpenAdminConsole = openAdminRequest
                                        )
                                        1 -> UserNotificationsScreen(
                                            notificationsState = userNotificationsState,
                                            onMarkRead = { viewModel.markNotificationRead(it) }
                                        )
                                        2 -> UserSupportScreen(
                                            ticketsState = userSupportTicketsState,
                                            onCreateTicket = { subj, msg, prio ->
                                                viewModel.submitSupportTicket(profile, subj, msg, prio)
                                            }
                                        )
                                        else -> UserProfileScreen(
                                            profile = profile,
                                            isAdmin = isAdmin,
                                            isBusy = isBusy,
                                            onUpdateProfile = { name, phone, pin ->
                                                viewModel.updateProfile(name, phone, profile.otpVerified, pin, profile.pinHash)
                                            },
                                            onRequestOtp = { viewModel.generateOtpChallenge(profile.phone.ifBlank { profile.email }) },
                                            onOpenAdminConsole = openAdminRequest,
                                            onSignOut = handleSignOut
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

    otpChallenge?.let { challenge ->
        OtpVerificationDialog(
            challenge = challenge,
            onVerify = { code ->
                if (viewModel.verifyOtpCode(code)) {
                    val activeProfile = (profileState as? UiState.Success)?.data
                    if (activeProfile == null) {
                        onboardingOtpVerified = true
                    } else {
                        viewModel.markOtpVerifiedOnProfile(activeProfile)
                    }
                }
            },
            onResend = { viewModel.generateOtpChallenge(challenge.destinationHint) },
            onDismiss = { viewModel.clearOtpChallenge() }
        )
    }

    if (showAdminPinDialog) {
        val currentProfile = (profileState as? UiState.Success)?.data
        val hasPin = !adminRecord?.adminPinHash.isNullOrBlank() || !currentProfile?.pinHash.isNullOrBlank()
        AdminPinLockDialog(
            hasExistingPin = hasPin,
            onSubmitPin = { rawPin ->
                viewModel.verifyOrSetAdminPin(rawPin)
                showAdminPinDialog = false
            },
            onDismiss = { showAdminPinDialog = false }
        )
    }
}
