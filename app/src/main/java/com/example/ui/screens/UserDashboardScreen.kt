package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.PanelRequest
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
import com.example.ui.theme.CardBorderBlue
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.GoldBorder
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
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun UserDashboardScreen(
    profile: UserProfile,
    isAdmin: Boolean,
    requestsState: UiState<List<PanelRequest>>,
    transactionsState: UiState<List<TransactionRecord>>,
    unreadNotifications: Int,
    onRequestOtp: () -> Unit,
    onCreateRequest: (type: String, title: String, description: String, amountText: String) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenSupport: () -> Unit,
    onOpenAdminConsole: () -> Unit
) {
    var showNewRequestDialog by remember { mutableStateOf(false) }
    var presetRequestType by remember { mutableStateOf("deposit") }

    val requests = (requestsState as? UiState.Success)?.data ?: emptyList()
    val transactions = (transactionsState as? UiState.Success)?.data ?: emptyList()
    val pendingCount = requests.count { it.status == "pending" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Banner & Balance
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                border = BorderStroke(1.dp, GoldBorder),
                colors = CardDefaults.cardColors(containerColor = MidnightSurface)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_banner),
                        contentDescription = "Mahadev Hero Banner",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(195.dp),
                        contentScale = ContentScale.Crop,
                        alpha = 0.5f
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(195.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        ObsidianBlack.copy(alpha = 0.92f),
                                        ObsidianBlack.copy(alpha = 0.55f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Welcome back,",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = profile.fullName.ifBlank { "Member" },
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            StatusPill(label = profile.status, statusKey = profile.status)
                        }

                        Text(
                            text = "PANEL BALANCE",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricBlue
                        )
                        Text(
                            text = "₹${"%,.2f".format(profile.walletBalance)}",
                            style = MaterialTheme.typography.displaySmall.copy(fontFamily = JetBrainsMonoFamily),
                            color = RoyalGold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusPill(
                                label = "Tier: ${profile.accessLevel}",
                                statusKey = profile.accessLevel
                            )
                            StatusPill(
                                label = if (profile.otpVerified) "OTP Verified" else "OTP Pending",
                                statusKey = if (profile.otpVerified) "verified" else "pending"
                            )
                        }
                    }
                }
            }
        }

        // Inactive / Suspended Warning Banner
        if (!profile.isActive) {
            item {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = Color(0xFF2E0D14),
                    border = BorderStroke(1.dp, StatusDangerRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Warning, contentDescription = null, tint = StatusDangerRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Account ${profile.status.uppercase()}",
                                style = MaterialTheme.typography.titleMedium,
                                color = StatusDangerRed
                            )
                        }
                        Text(
                            text = "Your account access is currently restricted by an administrator. Financial & standard request actions are paused.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        GoldPrimaryButton(
                            text = "Submit Reactivation Request",
                            onClick = {
                                presetRequestType = "reactivation"
                                showNewRequestDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Admin Shortcut Banner if user holds Admin role
        if (isAdmin) {
            item {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = GoldContainer,
                    border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.7f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Filled.AdminPanelSettings,
                                contentDescription = null,
                                tint = RoyalGold,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Admin Privileges Active", style = MaterialTheme.typography.titleSmall, color = SoftGold)
                                Text("Manage users, requests, ledger & reports", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                        GoldPrimaryButton(
                            text = "Admin",
                            onClick = onOpenAdminConsole,
                            modifier = Modifier.testTag("open_admin_console_banner_btn")
                        )
                    }
                }
            }
        }

        // KPI Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiMetricCard(
                    title = "Pending Requests",
                    value = pendingCount.toString(),
                    icon = Icons.Filled.PendingActions,
                    accentColor = StatusPendingAmber,
                    subtitle = "${requests.size} total",
                    modifier = Modifier.weight(1f)
                )
                KpiMetricCard(
                    title = "Transactions",
                    value = transactions.size.toString(),
                    icon = Icons.Filled.AccountBalanceWallet,
                    accentColor = ElectricBlue,
                    subtitle = "$unreadNotifications unread alerts",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GoldPrimaryButton(
                    text = "New Request",
                    onClick = {
                        presetRequestType = "deposit"
                        showNewRequestDialog = true
                    },
                    enabled = profile.isActive,
                    icon = Icons.Filled.AddCircle,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("new_request_button")
                )
                BlueOutlineButton(
                    text = if (profile.otpVerified) "Support" else "Verify OTP",
                    onClick = {
                        if (profile.otpVerified) onOpenSupport() else onRequestOtp()
                    },
                    icon = if (profile.otpVerified) Icons.Filled.SupportAgent else Icons.Filled.VerifiedUser,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_action_secondary_button")
                )
            }
        }

        // Account Status & User-Wise Access Control Card
        item {
            LuxuryCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Security, contentDescription = null, tint = RoyalGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Account Status & Access Control",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                        }
                        if (!profile.otpVerified) {
                            TextButton(onClick = onRequestOtp) {
                                Text("Verify OTP", color = ElectricBlue)
                            }
                        }
                    }

                    AccessPermissionRow("Account Activation", profile.isActive, if (profile.isActive) "Active" else profile.status.uppercase())
                    AccessPermissionRow("OTP Verification", profile.otpVerified, if (profile.otpVerified) "Verified" else "Unverified")
                    AccessPermissionRow("Financial Requests", profile.isActive && profile.accessLevel != "restricted", profile.accessLevel.uppercase())
                    AccessPermissionRow("Encrypted PIN Protection", profile.pinHash.isNotBlank(), if (profile.pinHash.isNotBlank()) "SHA-256 Enabled" else "Not Set")
                    AccessPermissionRow("Admin Console Access", isAdmin, if (isAdmin) "Authorized" else "Standard User")
                }
            }
        }

        // Recent Requests Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("My Requests", style = MaterialTheme.typography.titleLarge, color = RoyalGold)
                TextButton(onClick = onOpenNotifications) {
                    Icon(Icons.Filled.Notifications, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Alerts ($unreadNotifications)", color = ElectricBlue)
                }
            }
        }

        if (requests.isEmpty()) {
            item {
                LuxuryCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.PendingActions, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No requests submitted yet", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        Text("Tap 'New Request' above to submit a deposit, withdrawal, or upgrade request.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        } else {
            items(requests.take(8), key = { it.id }) { req ->
                RequestItemCard(req)
            }
        }

        // Recent Transactions Section
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Transaction History", style = MaterialTheme.typography.titleLarge, color = RoyalGold)
        }

        if (transactions.isEmpty()) {
            item {
                LuxuryCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No transactions recorded yet", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        Text("Approved deposits, withdrawals, and adjustments appear here in real time.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        } else {
            items(transactions.take(10), key = { it.id }) { tx ->
                TransactionItemCard(tx)
            }
        }
    }

    if (showNewRequestDialog) {
        NewRequestDialog(
            initialType = presetRequestType,
            onSubmit = { type, title, desc, amt ->
                onCreateRequest(type, title, desc, amt)
                showNewRequestDialog = false
            },
            onDismiss = { showNewRequestDialog = false }
        )
    }
}

@Composable
private fun AccessPermissionRow(label: String, granted: Boolean, valueText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(ElevatedSlate)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (granted) Icons.Filled.CheckCircle else Icons.Filled.Lock,
                contentDescription = null,
                tint = if (granted) StatusActiveGreen else StatusPendingAmber,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        }
        Text(
            text = valueText,
            style = MaterialTheme.typography.labelSmall,
            color = if (granted) StatusActiveGreen else StatusPendingAmber
        )
    }
}

@Composable
private fun RequestItemCard(req: PanelRequest) {
    val formatter = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val dateStr = req.createdAt?.toDate()?.let { formatter.format(it) } ?: "Pending sync"
    LuxuryCard {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = req.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatusPill(label = req.status, statusKey = req.status)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${req.type.replace('_', ' ').uppercase()} • $dateStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = ElectricBlue
                )
                if (req.amount > 0.0) {
                    Text(
                        text = "₹${"%,.2f".format(req.amount)}",
                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFamily),
                        color = RoyalGold
                    )
                }
            }
            if (req.description.isNotBlank()) {
                Text(text = req.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            if (req.adminNote.isNotBlank()) {
                Text(
                    text = "Admin Note: ${req.adminNote}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftGold
                )
            }
        }
    }
}

@Composable
fun TransactionItemCard(tx: TransactionRecord) {
    val formatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val dateStr = tx.createdAt?.toDate()?.let { formatter.format(it) } ?: "Just now"
    val isCredit = tx.type == "credit"
    LuxuryCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isCredit) Color(0xFF0A2A20) else Color(0xFF2E0D14)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCredit) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                    contentDescription = tx.type,
                    tint = if (isCredit) StatusActiveGreen else StatusDangerRed
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = tx.category, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Text(text = "${tx.referenceCode} • $dateStr", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                if (tx.note.isNotBlank()) {
                    Text(text = tx.note, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isCredit) "+" else "-"}₹${"%,.2f".format(tx.amount)}",
                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFamily),
                    color = if (isCredit) StatusActiveGreen else StatusDangerRed
                )
                StatusPill(label = tx.status, statusKey = tx.status)
            }
        }
    }
}

@Composable
private fun NewRequestDialog(
    initialType: String,
    onSubmit: (type: String, title: String, description: String, amountText: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableStateOf(initialType) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }

    val types = listOf(
        "deposit" to "Deposit",
        "withdrawal" to "Withdrawal",
        "access_upgrade" to "Upgrade",
        "reactivation" to "Reactivate"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MidnightSurface,
        titleContentColor = RoyalGold,
        title = { Text("Submit New Request", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    types.forEach { (key, label) ->
                        FilterChip(
                            selected = selectedType == key,
                            onClick = { selectedType = key },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldContainer,
                                selectedLabelColor = RoyalGold,
                                containerColor = ElevatedSlate,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Request Title") },
                    singleLine = true,
                    colors = mahadevFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (selectedType == "deposit" || selectedType == "withdrawal") {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount (₹)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = mahadevFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Reference / Details") },
                    minLines = 2,
                    maxLines = 4,
                    colors = mahadevFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            GoldPrimaryButton(
                text = "Submit",
                onClick = { onSubmit(selectedType, title, description, amountText) },
                enabled = title.trim().length >= 3
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
