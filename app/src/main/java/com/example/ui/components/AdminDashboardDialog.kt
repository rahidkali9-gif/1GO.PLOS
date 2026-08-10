package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardDialog(
    adminConfig: AdminConfig,
    adminApiUrl: String,
    adminSyncStatus: String?,
    isFetchingAdminUrl: Boolean,
    managedUrls: List<ManagedUrl>,
    usersList: List<UserModel>,
    withdrawalRequests: List<WithdrawalRequest>,
    paymentOrders: List<PaymentOrder>,
    auditLogs: List<AdminAuditLog>,
    onDismiss: () -> Unit,
    onSyncAdminPanel: (String) -> Unit,
    onAddManagedUrl: (label: String, url: String, setAsActive: Boolean) -> Unit,
    onSetActiveUrl: (id: String) -> Unit,
    onDeleteManagedUrl: (id: String) -> Unit,
    onProcessWithdrawal: (requestId: String, isApprove: Boolean, notes: String) -> Unit,
    onVerifyPayment: (orderId: String, gatewayTxnRef: String, isSuccess: Boolean) -> Unit,
    onToggleUserLock: (userId: String) -> Unit,
    onExportCsv: () -> String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedSection by remember { mutableIntStateOf(0) } // 0 = Dashboard, 1 = URL Manager, 2 = Withdrawals, 3 = Users, 4 = Audit Logs

    // URL Manager Inputs
    var newUrlLabel by remember { mutableStateOf("") }
    var newUrlInput by remember { mutableStateOf("") }
    var setAsActiveChecked by remember { mutableStateOf(true) }

    // Admin Sync Endpoint Input
    var apiEndpointInput by remember { mutableStateOf(adminApiUrl) }

    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }

    val pendingWithdrawals = remember(withdrawalRequests) {
        withdrawalRequests.filter { it.status == WithdrawalStatus.PENDING }
    }

    val pendingPayments = remember(paymentOrders) {
        paymentOrders.filter { it.status == PaymentStatus.PENDING }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Admin Control Panel",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "1Go WebView & Payment System",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section Tabs
                ScrollableTabRow(selectedTabIndex = selectedSection) {
                    Tab(
                        selected = selectedSection == 0,
                        onClick = { selectedSection = 0 },
                        text = { Text("Dashboard", fontSize = 12.sp) },
                        icon = { Icon(Icons.Outlined.Dashboard, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedSection == 1,
                        onClick = { selectedSection = 1 },
                        text = { Text("URL Manager", fontSize = 12.sp) },
                        icon = { Icon(Icons.Outlined.Link, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedSection == 2,
                        onClick = { selectedSection = 2 },
                        text = { Text("Withdrawals (${pendingWithdrawals.size})", fontSize = 12.sp) },
                        icon = { Icon(Icons.Outlined.Payments, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedSection == 3,
                        onClick = { selectedSection = 3 },
                        text = { Text("Users (${usersList.size})", fontSize = 12.sp) },
                        icon = { Icon(Icons.Outlined.People, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedSection == 4,
                        onClick = { selectedSection = 4 },
                        text = { Text("Audit Logs", fontSize = 12.sp) },
                        icon = { Icon(Icons.Outlined.History, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedSection) {
                    // SECTION 0: DASHBOARD SUMMARY CARDS & CONFIG SYNC
                    0 -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 380.dp)
                        ) {
                            item {
                                // Sync Endpoint Config
                                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            "Admin API Sync Endpoint",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = apiEndpointInput,
                                                onValueChange = { apiEndpointInput = it },
                                                placeholder = { Text("https://1go-real-money.onhercules.app") },
                                                singleLine = true,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("admin_api_endpoint_input")
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Button(
                                                onClick = { onSyncAdminPanel(apiEndpointInput) },
                                                enabled = !isFetchingAdminUrl
                                            ) {
                                                if (isFetchingAdminUrl) {
                                                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                                } else {
                                                    Icon(Icons.Default.Sync, contentDescription = null)
                                                }
                                            }
                                        }

                                        if (adminSyncStatus != null) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = adminSyncStatus,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Summary Grid Cards
                                Text(
                                    "System Summary Overview",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    SummaryMiniCard("Total Users", "${usersList.size}", Icons.Outlined.People, Modifier.weight(1f))
                                    SummaryMiniCard("Pending Payouts", "${pendingWithdrawals.size}", Icons.Outlined.HourglassTop, Modifier.weight(1f))
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    SummaryMiniCard("Active URL", adminConfig.activeUrl, Icons.Outlined.Language, Modifier.weight(1f))
                                    SummaryMiniCard("Pending Deposits", "${pendingPayments.size}", Icons.Outlined.MonetizationOn, Modifier.weight(1f))
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        val csv = onExportCsv()
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("TransactionLedger.csv", csv)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Copied Transaction Ledger CSV to Clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Export Transaction History (CSV)")
                                }
                            }
                        }
                    }

                    // SECTION 1: WEBVIEW URL MANAGER
                    1 -> {
                        Column {
                            Text(
                                text = "WebView URL Manager",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Paste active_url JSON or raw URL string. Only valid HTTPS URL will be saved.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = newUrlLabel,
                                onValueChange = { newUrlLabel = it },
                                label = { Text("URL Label / Title") },
                                placeholder = { Text("e.g. 1Go Official Portal") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = newUrlInput,
                                onValueChange = { newUrlInput = it },
                                label = { Text("Add New URL or JSON Payload") },
                                placeholder = { Text("""{"active_url":"https://1goplus.com"}""") },
                                singleLine = false,
                                maxLines = 3,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("add_url_input")
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = setAsActiveChecked,
                                    onCheckedChange = { setAsActiveChecked = it }
                                )
                                Text("Set as Active URL immediately", style = MaterialTheme.typography.bodySmall)
                            }

                            Button(
                                onClick = {
                                    if (newUrlInput.isNotBlank()) {
                                        onAddManagedUrl(newUrlLabel, newUrlInput, setAsActiveChecked)
                                        Toast.makeText(context, "Valid URL extracted and saved!", Toast.LENGTH_SHORT).show()
                                        newUrlLabel = ""
                                        newUrlInput = ""
                                    } else {
                                        Toast.makeText(context, "Please paste a URL or JSON object", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("save_url_button")
                            ) {
                                Icon(Icons.Default.AddLink, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Extract & Save URL")
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text("Managed URLs List", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(8.dp))

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                            ) {
                                items(managedUrls) { item ->
                                    OutlinedCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        colors = CardDefaults.outlinedCardColors(
                                            containerColor = if (item.isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                            else MaterialTheme.colorScheme.surface
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(item.label, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                                    if (item.isActive) {
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        AssistChip(
                                                            onClick = {},
                                                            label = { Text("ACTIVE", fontSize = 10.sp) },
                                                            colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primary)
                                                        )
                                                    }
                                                }
                                                Text(item.url, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }

                                            Row {
                                                if (!item.isActive) {
                                                    IconButton(onClick = { onSetActiveUrl(item.id) }) {
                                                        Icon(Icons.Default.CheckCircleOutline, contentDescription = "Set Active", tint = MaterialTheme.colorScheme.primary)
                                                    }
                                                }
                                                IconButton(onClick = { onDeleteManagedUrl(item.id) }) {
                                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // SECTION 2: WITHDRAWALS MANAGEMENT
                    2 -> {
                        Column {
                            Text(
                                text = "Pending Payout Requests",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (withdrawalRequests.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No withdrawal requests")
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 280.dp)
                                ) {
                                    items(withdrawalRequests) { req ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        req.requestId,
                                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                                    )
                                                    AssistChip(
                                                        onClick = {},
                                                        label = { Text(req.status.name, fontSize = 10.sp) },
                                                        colors = AssistChipDefaults.assistChipColors(
                                                            containerColor = when (req.status) {
                                                                WithdrawalStatus.PENDING -> MaterialTheme.colorScheme.tertiaryContainer
                                                                WithdrawalStatus.SUCCESS -> MaterialTheme.colorScheme.primaryContainer
                                                                WithdrawalStatus.REJECTED -> MaterialTheme.colorScheme.errorContainer
                                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                                            }
                                                        )
                                                    )
                                                }

                                                Text("User: ${req.userId}", style = MaterialTheme.typography.bodySmall)
                                                Text("Amount: ₹${req.amount}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                                Text("Destination: ${req.destinationUpiOrBank}", style = MaterialTheme.typography.bodySmall)

                                                if (req.status == WithdrawalStatus.PENDING) {
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Button(
                                                            onClick = {
                                                                onProcessWithdrawal(req.requestId, true, "Approved via Admin Panel")
                                                                Toast.makeText(context, "Approved payout for ${req.requestId}", Toast.LENGTH_SHORT).show()
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                                            modifier = Modifier.weight(1f)
                                                        ) {
                                                            Text("Approve Payout")
                                                        }

                                                        OutlinedButton(
                                                            onClick = {
                                                                onProcessWithdrawal(req.requestId, false, "Rejected by Admin")
                                                                Toast.makeText(context, "Rejected & Refunded to User Wallet", Toast.LENGTH_SHORT).show()
                                                            },
                                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                                            modifier = Modifier.weight(1f)
                                                        ) {
                                                            Text("Reject & Refund")
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // SECTION 3: USERS LIST & LOCK/UNLOCK
                    3 -> {
                        Column {
                            Text(
                                text = "Registered Users Directory",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 280.dp)
                            ) {
                                items(usersList) { u ->
                                    OutlinedCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(u.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("(${u.userId})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                                Text(u.mobileOrEmail, style = MaterialTheme.typography.bodySmall)
                                                Text("Wallet Balance: ₹${u.walletBalance}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                            }

                                            Button(
                                                onClick = { onToggleUserLock(u.userId) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (u.isLocked) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                                                )
                                            ) {
                                                Icon(
                                                    imageVector = if (u.isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(if (u.isLocked) "Unlock" else "Lock")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // SECTION 4: AUDIT LOGS
                    4 -> {
                        Column {
                            Text(
                                text = "Immutable System Audit Logs",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 280.dp)
                            ) {
                                items(auditLogs) { log ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    log.action,
                                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    dateFormat.format(Date(log.timestamp)),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                            }
                                            Text("Target: ${log.target}", style = MaterialTheme.typography.bodySmall)
                                            if (log.newValue.isNotBlank()) {
                                                Text("Val: ${log.newValue}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryMiniCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), maxLines = 1)
        }
    }
}
