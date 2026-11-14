package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
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
fun AdminRequestsScreen(
    requestsState: UiState<List<PanelRequest>>,
    onResolveRequest: (request: PanelRequest, approve: Boolean, note: String) -> Unit
) {
    var statusFilter by remember { mutableStateOf("pending") }
    var targetRequest by remember { mutableStateOf<Pair<PanelRequest, Boolean>?>(null) }

    val allRequests = (requestsState as? UiState.Success)?.data ?: emptyList()
    val filtered = if (statusFilter == "all") allRequests else allRequests.filter { it.status == statusFilter }
    val formatter = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Requests Management", style = MaterialTheme.typography.headlineMedium, color = RoyalGold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("pending", "approved", "rejected", "all").forEach { key ->
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
                        Icon(Icons.Filled.PendingActions, null, tint = TextSecondary, modifier = Modifier.size(34.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No $statusFilter requests", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    }
                }
            }
        } else {
            items(filtered, key = { it.id }) { req ->
                val dateStr = req.createdAt?.toDate()?.let { formatter.format(it) } ?: "Pending"
                LuxuryCard(goldAccent = req.status == "pending") {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(req.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                Text("${req.userName} (${req.userEmail})", style = MaterialTheme.typography.bodySmall, color = ElectricBlue)
                            }
                            StatusPill(label = req.status, statusKey = req.status)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${req.type.replace('_', ' ').uppercase()} • $dateStr", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            if (req.amount > 0.0) {
                                Text(
                                    text = "₹${"%,.2f".format(req.amount)}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFamily),
                                    color = RoyalGold
                                )
                            }
                        }
                        if (req.description.isNotBlank()) {
                            Text(req.description, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                        if (req.adminNote.isNotBlank()) {
                            Text("Admin Note: ${req.adminNote}", style = MaterialTheme.typography.bodySmall, color = SoftGold)
                        }
                        if (req.status == "pending") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                GoldPrimaryButton(
                                    text = "Approve",
                                    onClick = { targetRequest = req to true },
                                    icon = Icons.Filled.Check,
                                    modifier = Modifier.weight(1f)
                                )
                                BlueOutlineButton(
                                    text = "Reject",
                                    onClick = { targetRequest = req to false },
                                    icon = Icons.Filled.Close,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    targetRequest?.let { (req, approve) ->
        ResolveRequestDialog(
            request = req,
            approve = approve,
            onConfirm = { note ->
                onResolveRequest(req, approve, note)
                targetRequest = null
            },
            onDismiss = { targetRequest = null }
        )
    }
}

@Composable
private fun ResolveRequestDialog(
    request: PanelRequest,
    approve: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var note by remember {
        mutableStateOf(if (approve) "Verified and approved by Admin" else "Unable to verify details")
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MidnightSurface,
        titleContentColor = RoyalGold,
        title = {
            Text(
                text = if (approve) "Approve Request" else "Reject Request",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "${request.userName}: ${request.title}" +
                        if (request.amount > 0.0) " (₹${"%,.2f".format(request.amount)})" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Admin Resolution Note") },
                    minLines = 2,
                    colors = mahadevFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            GoldPrimaryButton(
                text = if (approve) "Confirm Approval" else "Confirm Rejection",
                onClick = { onConfirm(note) }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
fun AdminLedgerAndReportsScreen(
    usersState: UiState<List<UserProfile>>,
    requestsState: UiState<List<PanelRequest>>,
    transactionsState: UiState<List<TransactionRecord>>,
    ticketsState: UiState<List<SupportTicket>>,
    onRecordTransaction: (targetUser: UserProfile, type: String, category: String, amountText: String, note: String) -> Unit,
    onReplyTicket: (ticket: SupportTicket, newStatus: String, reply: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showRecordTxDialog by remember { mutableStateOf(false) }
    var replyTicketTarget by remember { mutableStateOf<SupportTicket?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val users = (usersState as? UiState.Success)?.data ?: emptyList()
    val requests = (requestsState as? UiState.Success)?.data ?: emptyList()
    val transactions = (transactionsState as? UiState.Success)?.data ?: emptyList()
    val tickets = (ticketsState as? UiState.Success)?.data ?: emptyList()

    val totalCredits = transactions.filter { it.type == "credit" }.sumOf { it.amount }
    val totalDebits = transactions.filter { it.type == "debit" }.sumOf { it.amount }
    val netBalance = totalCredits - totalDebits
    val approvedCount = requests.count { it.status == "approved" }
    val approvalRate = if (requests.isEmpty()) 0 else (approvedCount * 100) / requests.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MidnightSurface,
            contentColor = RoyalGold,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = RoyalGold
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Transaction History") },
                icon = { Icon(Icons.Filled.ReceiptLong, null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Reports & Support") },
                icon = { Icon(Icons.Filled.Assessment, null, modifier = Modifier.size(18.dp)) }
            )
        }

        if (selectedTab == 0) {
            val filteredTx = transactions.filter { tx ->
                val q = searchQuery.trim().lowercase()
                q.isEmpty() ||
                    tx.userName.lowercase().contains(q) ||
                    tx.referenceCode.lowercase().contains(q) ||
                    tx.category.lowercase().contains(q)
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Master Ledger", style = MaterialTheme.typography.titleLarge, color = RoyalGold)
                        GoldPrimaryButton(
                            text = "Add Entry",
                            onClick = { showRecordTxDialog = true },
                            enabled = users.isNotEmpty(),
                            icon = Icons.Filled.AddCircle,
                            modifier = Modifier.testTag("admin_add_transaction_button")
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search user, reference code, or category") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Search, null, tint = ElectricBlue) },
                        colors = mahadevFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (filteredTx.isEmpty()) {
                    item {
                        LuxuryCard {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("No transactions in ledger", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            }
                        }
                    }
                } else {
                    items(filteredTx, key = { it.id }) { tx ->
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "User: ${tx.userName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricBlue,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                            TransactionItemCard(tx)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Operational & Financial Reports", style = MaterialTheme.typography.titleLarge, color = RoyalGold)
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        KpiMetricCard(
                            title = "Net Panel Flow",
                            value = "₹${"%,.0f".format(netBalance)}",
                            icon = Icons.Filled.Assessment,
                            accentColor = RoyalGold,
                            subtitle = "Credits ₹${"%,.0f".format(totalCredits)}",
                            modifier = Modifier.weight(1f)
                        )
                        KpiMetricCard(
                            title = "Approval Rate",
                            value = "$approvalRate%",
                            icon = Icons.Filled.Check,
                            accentColor = StatusActiveGreen,
                            subtitle = "$approvedCount / ${requests.size} approved",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    LuxuryCard {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("System Summary", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            ReportLine("Registered Users", "${users.size}")
                            ReportLine("Active Users", "${users.count { it.status == "active" }}")
                            ReportLine("OTP Verified Users", "${users.count { it.otpVerified }}")
                            ReportLine("Total Debits", "₹${"%,.2f".format(totalDebits)}")
                            ReportLine("Open Support Tickets", "${tickets.count { it.status != "resolved" }}")
                        }
                    }
                }
                item {
                    Text("Support Tickets Queue (${tickets.size})", style = MaterialTheme.typography.titleMedium, color = RoyalGold)
                }
                if (tickets.isEmpty()) {
                    item {
                        LuxuryCard {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("No support tickets", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            }
                        }
                    }
                } else {
                    items(tickets, key = { it.id }) { ticket ->
                        LuxuryCard {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(ticket.subject, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                        Text("From: ${ticket.userName}", style = MaterialTheme.typography.labelSmall, color = ElectricBlue)
                                    }
                                    StatusPill(label = ticket.status.replace('_', ' '), statusKey = ticket.status)
                                }
                                Text(ticket.message, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                if (ticket.adminReply.isNotBlank()) {
                                    Text("Reply: ${ticket.adminReply}", style = MaterialTheme.typography.bodySmall, color = SoftGold)
                                }
                                BlueOutlineButton(
                                    text = "Reply / Update Status",
                                    onClick = { replyTicketTarget = ticket },
                                    icon = Icons.Filled.Reply,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRecordTxDialog && users.isNotEmpty()) {
        RecordTransactionDialog(
            users = users,
            onSubmit = { user, type, cat, amt, note ->
                onRecordTransaction(user, type, cat, amt, note)
                showRecordTxDialog = false
            },
            onDismiss = { showRecordTxDialog = false }
        )
    }

    replyTicketTarget?.let { ticket ->
        ReplySupportTicketDialog(
            ticket = ticket,
            onSubmit = { newStatus, reply ->
                onReplyTicket(ticket, newStatus, reply)
                replyTicketTarget = null
            },
            onDismiss = { replyTicketTarget = null }
        )
    }
}

@Composable
private fun ReportLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(value, style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFamily), color = TextPrimary)
    }
}

@Composable
private fun RecordTransactionDialog(
    users: List<UserProfile>,
    onSubmit: (UserProfile, String, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedUser by remember { mutableStateOf(users.first()) }
    var userMenuExpanded by remember { mutableStateOf(false) }
    var type by remember { mutableStateOf("credit") }
    var category by remember { mutableStateOf("Deposit") }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MidnightSurface,
        titleContentColor = RoyalGold,
        title = { Text("Record Ledger Entry", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Column {
                    OutlinedTextField(
                        value = "${selectedUser.fullName} (${selectedUser.email})",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select User") },
                        colors = mahadevFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { userMenuExpanded = true }
                    )
                    TextButton(onClick = { userMenuExpanded = true }) {
                        Text("Choose User", color = ElectricBlue)
                    }
                    DropdownMenu(
                        expanded = userMenuExpanded,
                        onDismissRequest = { userMenuExpanded = false }
                    ) {
                        users.forEach { u ->
                            DropdownMenuItem(
                                text = { Text("${u.fullName} (${u.email})") },
                                onClick = {
                                    selectedUser = u
                                    userMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("credit", "debit").forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = {
                                type = t
                                category = if (t == "credit") "Deposit" else "Withdrawal"
                            },
                            label = { Text(t.uppercase(), style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    singleLine = true,
                    colors = mahadevFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (₹)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = mahadevFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Ledger Note") },
                    singleLine = true,
                    colors = mahadevFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            GoldPrimaryButton(
                text = "Save Entry",
                onClick = { onSubmit(selectedUser, type, category, amountText, note) },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0.0
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
private fun ReplySupportTicketDialog(
    ticket: SupportTicket,
    onSubmit: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var status by remember { mutableStateOf(if (ticket.status == "open") "resolved" else ticket.status) }
    var reply by remember { mutableStateOf(ticket.adminReply) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MidnightSurface,
        titleContentColor = RoyalGold,
        title = { Text("Update Ticket", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("open", "in_progress", "resolved").forEach { s ->
                        FilterChip(
                            selected = status == s,
                            onClick = { status = s },
                            label = { Text(s.replace('_', ' ').uppercase(), style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                OutlinedTextField(
                    value = reply,
                    onValueChange = { reply = it },
                    label = { Text("Admin Reply") },
                    minLines = 3,
                    colors = mahadevFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            GoldPrimaryButton(
                text = "Send Reply",
                onClick = { onSubmit(status, reply) },
                enabled = reply.trim().isNotEmpty()
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}
