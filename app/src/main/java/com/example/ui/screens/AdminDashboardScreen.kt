package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.PanelRequest
import com.example.data.model.SupportTicket
import com.example.data.model.TransactionRecord
import com.example.data.model.UiState
import com.example.data.model.UserProfile
import com.example.ui.components.BlueOutlineButton
import com.example.ui.components.GoldPrimaryButton
import com.example.ui.components.KpiMetricCard
import com.example.ui.components.LuxuryCard
import com.example.ui.components.StatusPill
import com.example.ui.components.mahadevFieldColors
import com.example.ui.theme.BlueContainer
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.SoftGold
import com.example.ui.theme.StatusActiveGreen
import com.example.ui.theme.StatusDangerRed
import com.example.ui.theme.StatusPendingAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AdminDashboardScreen(
    usersState: UiState<List<UserProfile>>,
    requestsState: UiState<List<PanelRequest>>,
    transactionsState: UiState<List<TransactionRecord>>,
    ticketsState: UiState<List<SupportTicket>>,
    onNavigateToUsers: () -> Unit,
    onNavigateToRequests: () -> Unit,
    onNavigateToReports: () -> Unit,
    onLockAdmin: () -> Unit
) {
    val users = (usersState as? UiState.Success)?.data ?: emptyList()
    val requests = (requestsState as? UiState.Success)?.data ?: emptyList()
    val transactions = (transactionsState as? UiState.Success)?.data ?: emptyList()
    val tickets = (ticketsState as? UiState.Success)?.data ?: emptyList()

    val activeUsers = users.count { it.status == "active" }
    val inactiveUsers = users.count { it.status != "active" }
    val pendingRequests = requests.count { it.status == "pending" }
    val openTickets = tickets.count { it.status != "resolved" }
    val totalCredits = transactions.filter { it.type == "credit" }.sumOf { it.amount }
    val totalDebits = transactions.filter { it.type == "debit" }.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            LuxuryCard(goldAccent = true) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("ADMIN COMMAND CENTER", style = MaterialTheme.typography.titleLarge, color = RoyalGold)
                            Text("Live Database Statistics & Access Control", style = MaterialTheme.typography.bodySmall, color = ElectricBlue)
                        }
                        BlueOutlineButton(
                            text = "Lock",
                            onClick = onLockAdmin,
                            icon = Icons.Filled.Lock,
                            modifier = Modifier.testTag("lock_admin_button")
                        )
                    }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiMetricCard(
                    title = "Total Users",
                    value = users.size.toString(),
                    icon = Icons.Filled.Group,
                    accentColor = ElectricBlue,
                    subtitle = "$activeUsers active • $inactiveUsers paused",
                    modifier = Modifier.weight(1f)
                )
                KpiMetricCard(
                    title = "Pending Requests",
                    value = pendingRequests.toString(),
                    icon = Icons.Filled.PendingActions,
                    accentColor = StatusPendingAmber,
                    subtitle = "${requests.size} total requests",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiMetricCard(
                    title = "Total Credits",
                    value = "₹${"%,.0f".format(totalCredits)}",
                    icon = Icons.Filled.AccountBalanceWallet,
                    accentColor = StatusActiveGreen,
                    subtitle = "Debits: ₹${"%,.0f".format(totalDebits)}",
                    modifier = Modifier.weight(1f)
                )
                KpiMetricCard(
                    title = "Open Tickets",
                    value = openTickets.toString(),
                    icon = Icons.Filled.SupportAgent,
                    accentColor = RoyalGold,
                    subtitle = "${tickets.size} total tickets",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Visual Breakdown Chart using Canvas
        item {
            LuxuryCard {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Live System Health & Volume Breakdown", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    val totalUsersCount = users.size.coerceAtLeast(1)
                    val activeRatio = activeUsers.toFloat() / totalUsersCount.toFloat()
                    val volumeTotal = (totalCredits + totalDebits).coerceAtLeast(1.0)
                    val creditRatio = (totalCredits / volumeTotal).toFloat()

                    BarBreakdownRow(
                        label = "Active User Ratio ($activeUsers / ${users.size})",
                        ratio = activeRatio,
                        primaryColor = StatusActiveGreen,
                        trackColor = StatusDangerRed.copy(alpha = 0.45f)
                    )
                    BarBreakdownRow(
                        label = "Credits vs Debits Volume",
                        ratio = creditRatio,
                        primaryColor = RoyalGold,
                        trackColor = ElectricBlue.copy(alpha = 0.55f)
                    )
                }
            }
        }

        // Quick Admin Shortcuts
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GoldPrimaryButton(
                    text = "Manage Users",
                    onClick = onNavigateToUsers,
                    icon = Icons.Filled.Group,
                    modifier = Modifier.weight(1f)
                )
                BlueOutlineButton(
                    text = "Review Requests ($pendingRequests)",
                    onClick = onNavigateToRequests,
                    icon = Icons.Filled.PendingActions,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            BlueOutlineButton(
                text = "Open Financial Reports & Support Queue",
                onClick = onNavigateToReports,
                icon = Icons.Filled.AdminPanelSettings,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun BarBreakdownRow(
    label: String,
    ratio: Float,
    primaryColor: Color,
    trackColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text("${(ratio * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = primaryColor)
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
        ) {
            drawRoundRect(
                color = trackColor,
                topLeft = Offset.Zero,
                size = Size(size.width, size.height),
                cornerRadius = CornerRadius(12f, 12f)
            )
            val fillWidth = (size.width * ratio.coerceIn(0f, 1f))
            if (fillWidth > 0f) {
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset.Zero,
                    size = Size(fillWidth, size.height),
                    cornerRadius = CornerRadius(12f, 12f)
                )
            }
        }
    }
}

@Composable
fun AdminUsersScreen(
    usersState: UiState<List<UserProfile>>,
    currentAdminUid: String,
    onUpdateUser: (targetUser: UserProfile, status: String, accessLevel: String, role: String) -> Unit,
    onSendNotification: (targetUserId: String, title: String, message: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("all") }
    var notifyTarget by remember { mutableStateOf<UserProfile?>(null) }
    var manageTarget by remember { mutableStateOf<UserProfile?>(null) }

    val allUsers = (usersState as? UiState.Success)?.data ?: emptyList()
    val filtered = allUsers.filter { u ->
        val matchesStatus = statusFilter == "all" || u.status == statusFilter
        val q = searchQuery.trim().lowercase()
        val matchesSearch = q.isEmpty() ||
            u.fullName.lowercase().contains(q) ||
            u.email.lowercase().contains(q) ||
            u.phone.lowercase().contains(q)
        matchesStatus && matchesSearch
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("All Users Management", style = MaterialTheme.typography.headlineMedium, color = RoyalGold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search name, email or phone") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = ElectricBlue) },
                colors = mahadevFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_users_search")
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("all", "active", "inactive", "suspended").forEach { key ->
                    FilterChip(
                        selected = statusFilter == key,
                        onClick = { statusFilter = key },
                        label = { Text(key.uppercase(), style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldContainer,
                            selectedLabelColor = RoyalGold
                        )
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                LuxuryCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.Group, null, tint = TextSecondary, modifier = Modifier.size(34.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No matching users found", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    }
                }
            }
        } else {
            items(filtered, key = { it.userId }) { user ->
                LuxuryCard(goldAccent = user.isAdminRole) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.fullName, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                Text(user.email, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                if (user.phone.isNotBlank()) {
                                    Text(user.phone, style = MaterialTheme.typography.labelSmall, color = ElectricBlue)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                StatusPill(label = user.status, statusKey = user.status)
                                Text(
                                    text = "₹${"%,.2f".format(user.walletBalance)}",
                                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFamily),
                                    color = RoyalGold
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            StatusPill(label = user.role, statusKey = user.role)
                            StatusPill(label = user.accessLevel, statusKey = user.accessLevel)
                            if (user.otpVerified) {
                                StatusPill(label = "OTP OK", statusKey = "verified")
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val nextStatus = if (user.isActive) "inactive" else "active"
                            GoldPrimaryButton(
                                text = if (user.isActive) "Deactivate" else "Activate",
                                onClick = {
                                    onUpdateUser(user, nextStatus, user.accessLevel, user.role)
                                },
                                enabled = user.userId != currentAdminUid,
                                icon = if (user.isActive) Icons.Filled.Block else Icons.Filled.CheckCircle,
                                modifier = Modifier.weight(1f)
                            )
                            BlueOutlineButton(
                                text = "Access",
                                onClick = { manageTarget = user },
                                icon = Icons.Filled.VerifiedUser,
                                modifier = Modifier.weight(1f)
                            )
                            BlueOutlineButton(
                                text = "Notify",
                                onClick = { notifyTarget = user },
                                icon = Icons.Filled.Notifications,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }

    manageTarget?.let { target ->
        ManageUserAccessDialog(
            user = target,
            isSelf = target.userId == currentAdminUid,
            onSave = { status, level, role ->
                onUpdateUser(target, status, level, role)
                manageTarget = null
            },
            onDismiss = { manageTarget = null }
        )
    }

    notifyTarget?.let { target ->
        SendUserNotificationDialog(
            user = target,
            onSend = { title, message ->
                onSendNotification(target.userId, title, message)
                notifyTarget = null
            },
            onDismiss = { notifyTarget = null }
        )
    }
}

@Composable
private fun ManageUserAccessDialog(
    user: UserProfile,
    isSelf: Boolean,
    onSave: (status: String, accessLevel: String, role: String) -> Unit,
    onDismiss: () -> Unit
) {
    var status by remember { mutableStateOf(user.status) }
    var accessLevel by remember { mutableStateOf(user.accessLevel) }
    var role by remember { mutableStateOf(user.role) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MidnightSurface,
        titleContentColor = RoyalGold,
        title = { Text("User Access Control: ${user.fullName}", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Account Status", style = MaterialTheme.typography.labelLarge, color = ElectricBlue)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("active", "inactive", "suspended").forEach { s ->
                        FilterChip(
                            selected = status == s,
                            onClick = { if (!isSelf || s == "active") status = s },
                            label = { Text(s.uppercase(), style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Text("Access Level", style = MaterialTheme.typography.labelLarge, color = ElectricBlue)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("standard", "premium", "restricted").forEach { lvl ->
                        FilterChip(
                            selected = accessLevel == lvl,
                            onClick = { accessLevel = lvl },
                            label = { Text(lvl.uppercase(), style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Text("Role Privileges", style = MaterialTheme.typography.labelLarge, color = ElectricBlue)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("user", "admin").forEach { r ->
                        FilterChip(
                            selected = role == r,
                            onClick = { if (!isSelf || r == "admin") role = r },
                            label = { Text(r.uppercase(), style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            GoldPrimaryButton(text = "Apply Changes", onClick = { onSave(status, accessLevel, role) })
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
private fun SendUserNotificationDialog(
    user: UserProfile,
    onSend: (title: String, message: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MidnightSurface,
        titleContentColor = RoyalGold,
        title = { Text("Notify ${user.fullName}", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Notification Title") },
                    singleLine = true,
                    colors = mahadevFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message Body") },
                    minLines = 3,
                    colors = mahadevFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            GoldPrimaryButton(
                text = "Send Alert",
                onClick = { onSend(title, message) },
                icon = Icons.Filled.Send,
                enabled = title.trim().length >= 2 && message.trim().length >= 2
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}
