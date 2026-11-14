package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.model.AppNotification
import com.example.data.model.SupportTicket
import com.example.data.model.UiState
import com.example.data.model.UserProfile
import com.example.ui.components.BlueOutlineButton
import com.example.ui.components.GoldPrimaryButton
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
fun UserNotificationsScreen(
    notificationsState: UiState<List<AppNotification>>,
    onMarkRead: (String) -> Unit
) {
    var unreadOnly by remember { mutableStateOf(false) }
    val allNotifications = (notificationsState as? UiState.Success)?.data ?: emptyList()
    val filtered = if (unreadOnly) allNotifications.filter { !it.isRead } else allNotifications
    val unreadCount = allNotifications.count { !it.isRead }
    val formatter = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Notifications", style = MaterialTheme.typography.headlineMedium, color = RoyalGold)
                    Text("$unreadCount unread security & panel alerts", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !unreadOnly,
                        onClick = { unreadOnly = false },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldContainer,
                            selectedLabelColor = RoyalGold
                        )
                    )
                    FilterChip(
                        selected = unreadOnly,
                        onClick = { unreadOnly = true },
                        label = { Text("Unread") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BlueContainer,
                            selectedLabelColor = ElectricBlue
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
                        Icon(Icons.Filled.Notifications, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No notifications to show", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text("Status updates, request approvals, and transaction alerts appear here.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        } else {
            items(filtered, key = { it.id }) { notif ->
                val dateStr = notif.createdAt?.toDate()?.let { formatter.format(it) } ?: "Just now"
                LuxuryCard(goldAccent = !notif.isRead) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !notif.isRead) { onMarkRead(notif.id) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (notif.isRead) ElevatedSlate else GoldContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (notif.isRead) Icons.Filled.MarkEmailRead else Icons.Filled.NotificationsActive,
                                contentDescription = notif.category,
                                tint = if (notif.isRead) TextSecondary else RoyalGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = notif.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (notif.isRead) TextPrimary else SoftGold,
                                    modifier = Modifier.weight(1f)
                                )
                                StatusPill(label = notif.category, statusKey = notif.category)
                            }
                            Text(text = notif.message, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text(
                                text = if (notif.isRead) dateStr else "$dateStr • Tap to mark read",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (notif.isRead) TextSecondary else ElectricBlue
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserSupportScreen(
    ticketsState: UiState<List<SupportTicket>>,
    onCreateTicket: (subject: String, message: String, priority: String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    val tickets = (ticketsState as? UiState.Success)?.data ?: emptyList()
    val formatter = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.SupportAgent, contentDescription = null, tint = RoyalGold, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Help & Support Desk", style = MaterialTheme.typography.titleLarge, color = RoyalGold)
                            Text("24/7 Priority Assistance & Ticket Tracking", style = MaterialTheme.typography.bodySmall, color = ElectricBlue)
                        }
                    }
                    Text(
                        text = "Open a ticket for account status reviews, deposit/withdrawal queries, or access level upgrades.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    GoldPrimaryButton(
                        text = "Open Support Ticket",
                        onClick = { showCreateDialog = true },
                        icon = Icons.Filled.AddComment,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_support_ticket_button")
                    )
                }
            }
        }

        item {
            Text("My Support Tickets (${tickets.size})", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        }

        if (tickets.isEmpty()) {
            item {
                LuxuryCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.SupportAgent, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(34.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No active support tickets", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        Text("Your submitted tickets and admin responses appear here.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        } else {
            items(tickets, key = { it.id }) { ticket ->
                val dateStr = ticket.createdAt?.toDate()?.let { formatter.format(it) } ?: "Pending"
                LuxuryCard {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ticket.subject,
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            StatusPill(label = ticket.status.replace('_', ' '), statusKey = ticket.status)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            StatusPill(label = ticket.priority, statusKey = ticket.priority)
                            Text(dateStr, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                        Text(ticket.message, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        if (ticket.adminReply.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = BlueContainer,
                                border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("ADMIN RESPONSE", style = MaterialTheme.typography.labelSmall, color = ElectricBlue)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(ticket.adminReply, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateSupportTicketDialog(
            onSubmit = { subj, msg, prio ->
                onCreateTicket(subj, msg, prio)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }
}

@Composable
private fun CreateSupportTicketDialog(
    onSubmit: (subject: String, message: String, priority: String) -> Unit,
    onDismiss: () -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("normal") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MidnightSurface,
        titleContentColor = RoyalGold,
        title = { Text("New Support Ticket", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("normal", "high", "urgent").forEach { prio ->
                        FilterChip(
                            selected = priority == prio,
                            onClick = { priority = prio },
                            label = { Text(prio.uppercase(), style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldContainer,
                                selectedLabelColor = RoyalGold
                            )
                        )
                    }
                }
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    singleLine = true,
                    colors = mahadevFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Describe your issue") },
                    minLines = 3,
                    maxLines = 5,
                    colors = mahadevFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            GoldPrimaryButton(
                text = "Submit Ticket",
                onClick = { onSubmit(subject, message, priority) },
                enabled = subject.trim().length >= 3 && message.trim().length >= 5
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
fun UserProfileScreen(
    profile: UserProfile,
    isAdmin: Boolean,
    isBusy: Boolean,
    onUpdateProfile: (fullName: String, phone: String, newPin: String?) -> Unit,
    onRequestOtp: () -> Unit,
    onOpenAdminConsole: () -> Unit,
    onSignOut: () -> Unit
) {
    var fullName by remember(profile.fullName) { mutableStateOf(profile.fullName) }
    var phone by remember(profile.phone) { mutableStateOf(profile.phone) }
    var newPin by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            LuxuryCard(goldAccent = true) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(GoldContainer)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile.fullName.trim().take(2).uppercase().ifBlank { "MP" },
                            style = MaterialTheme.typography.titleLarge,
                            color = RoyalGold
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(profile.fullName, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text(profile.email, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            StatusPill(label = profile.status, statusKey = profile.status)
                            StatusPill(label = if (isAdmin) "Admin" else profile.accessLevel, statusKey = if (isAdmin) "admin" else profile.accessLevel)
                        }
                    }
                }
            }
        }

        if (isAdmin) {
            item {
                LuxuryCard(goldAccent = true) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = RoyalGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Secure Admin Console", style = MaterialTheme.typography.titleMedium, color = RoyalGold)
                        }
                        Text(
                            text = "Access user activation, request approvals, financial transaction ledger, and system reports behind PIN protection.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        GoldPrimaryButton(
                            text = "Open Admin Panel",
                            onClick = onOpenAdminConsole,
                            icon = Icons.Filled.AdminPanelSettings,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_open_admin_button")
                        )
                    }
                }
            }
        }

        item {
            LuxuryCard {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Profile & Encryption Settings", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Person, null, tint = RoyalGold) },
                        colors = mahadevFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { if (it.length <= 15) phone = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        leadingIcon = { Icon(Icons.Filled.Phone, null, tint = ElectricBlue) },
                        colors = mahadevFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) newPin = it },
                        label = { Text("Update Security PIN (Optional, 4–6 digits)") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        leadingIcon = { Icon(Icons.Filled.Lock, null, tint = RoyalGold) },
                        supportingText = {
                            Text(
                                if (profile.pinHash.isNotBlank()) "SHA-256 salted PIN digest is active" else "Set a new PIN",
                                color = TextSecondary
                            )
                        },
                        colors = mahadevFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    GoldPrimaryButton(
                        text = if (isBusy) "Saving..." else "Save Profile Changes",
                        onClick = {
                            onUpdateProfile(fullName, phone, newPin.takeIf { it.isNotBlank() })
                            newPin = ""
                        },
                        enabled = !isBusy && fullName.trim().length >= 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_profile_button")
                    )
                }
            }
        }

        item {
            LuxuryCard {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (profile.otpVerified) Icons.Filled.CheckCircle else Icons.Filled.Security,
                                contentDescription = null,
                                tint = if (profile.otpVerified) StatusActiveGreen else StatusPendingAmber
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("OTP Verification", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                Text(
                                    text = if (profile.otpVerified) "Account verified via 6-digit OTP" else "Pending verification",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                        StatusPill(
                            label = if (profile.otpVerified) "Verified" else "Pending",
                            statusKey = if (profile.otpVerified) "verified" else "pending"
                        )
                    }
                    BlueOutlineButton(
                        text = if (profile.otpVerified) "Re-Verify via OTP" else "Verify Now via OTP",
                        onClick = onRequestOtp,
                        icon = Icons.Filled.VerifiedUser,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            BlueOutlineButton(
                text = "Sign Out",
                onClick = onSignOut,
                icon = Icons.AutoMirrored.Filled.Logout,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sign_out_button")
            )
        }
    }
}
