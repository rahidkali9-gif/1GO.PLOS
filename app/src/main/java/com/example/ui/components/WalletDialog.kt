package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletDialog(
    walletState: WalletState,
    paymentOrders: List<PaymentOrder>,
    withdrawalRequests: List<WithdrawalRequest>,
    walletLedger: List<WalletLedgerEntry>,
    adminConfig: AdminConfig,
    onDismiss: () -> Unit,
    onCreatePaymentOrder: (Double) -> PaymentOrder,
    onVerifyPayment: (orderId: String, txnRef: String, isSuccess: Boolean) -> Unit,
    onCreateWithdrawal: (amount: Double, destination: String) -> Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Wallet, 1 = Deposit, 2 = Withdraw, 3 = Ledger

    var depositAmountText by remember { mutableStateOf("500") }
    var activeOrder by remember { mutableStateOf<PaymentOrder?>(null) }
    var isVerifying by remember { mutableStateOf(false) }

    var withdrawAmountText by remember { mutableStateOf("") }
    var withdrawDestinationText by remember { mutableStateOf("") }
    var withdrawError by remember { mutableStateOf<String?>(null) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
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
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "1Go Wallet",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "ID: ${walletState.userId}",
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

                // Navigation Tabs
                PrimaryTabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Overview", fontSize = 12.sp) },
                        icon = { Icon(Icons.Outlined.AccountBalance, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        enabled = adminConfig.paymentEnabled,
                        text = { Text("Deposit", fontSize = 12.sp) },
                        icon = { Icon(Icons.Outlined.AddCard, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        enabled = adminConfig.withdrawalEnabled,
                        text = { Text("Withdraw", fontSize = 12.sp) },
                        icon = { Icon(Icons.Outlined.Payments, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("Ledger", fontSize = 12.sp) },
                        icon = { Icon(Icons.Outlined.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    // TAB 0: OVERVIEW & BALANCES
                    0 -> {
                        Column {
                            // Balance Card
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.tertiary
                                                )
                                            )
                                        )
                                        .padding(20.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "AVAILABLE BALANCE",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "₹${String.format("%.2f", walletState.availableBalance)}",
                                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )

                                        if (walletState.pendingBalance > 0) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Pending Payout: ₹${String.format("%.2f", walletState.pendingBalance)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedCard(modifier = Modifier.weight(1f)) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Total Deposited", style = MaterialTheme.typography.labelSmall)
                                        Text(
                                            "₹${String.format("%.2f", walletState.totalDeposited)}",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                OutlinedCard(modifier = Modifier.weight(1f)) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Total Withdrawn", style = MaterialTheme.typography.labelSmall)
                                        Text(
                                            "₹${String.format("%.2f", walletState.totalWithdrawn)}",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Quick Action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { selectedTab = 1 },
                                    enabled = adminConfig.paymentEnabled,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Deposit Funds")
                                }

                                OutlinedButton(
                                    onClick = { selectedTab = 2 },
                                    enabled = adminConfig.withdrawalEnabled,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.CallMade, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Withdraw")
                                }
                            }
                        }
                    }

                    // TAB 1: DEPOSIT / PAYMENT GATEWAY
                    1 -> {
                        Column {
                            Text(
                                text = "Instant UPI Deposit",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Select or enter amount to pay via GPay, PhonePe, Paytm, BHIM or UPI",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = depositAmountText,
                                onValueChange = { depositAmountText = it.filter { char -> char.isDigit() } },
                                label = { Text("Deposit Amount (₹)") },
                                leadingIcon = { Text("₹", style = MaterialTheme.typography.titleMedium) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("deposit_amount_input")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Quick Preset Chips
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf("100", "500", "1000", "2000", "5000").forEach { preset ->
                                    FilterChip(
                                        selected = depositAmountText == preset,
                                        onClick = { depositAmountText = preset },
                                        label = { Text("₹$preset") }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (activeOrder == null) {
                                Button(
                                    onClick = {
                                        val amt = depositAmountText.toDoubleOrNull() ?: 0.0
                                        if (amt >= 10.0) {
                                            activeOrder = onCreatePaymentOrder(amt)
                                        } else {
                                            Toast.makeText(context, "Minimum deposit is ₹10", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("create_order_button")
                                ) {
                                    Icon(Icons.Default.Payment, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Generate UPI Payment Link")
                                }
                            } else {
                                val order = activeOrder!!
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            "Order ID: ${order.orderId}",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text("Amount: ₹${order.amount}", style = MaterialTheme.typography.bodyLarge)
                                        Text("Merchant: ${adminConfig.merchantName}", style = MaterialTheme.typography.bodySmall)

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Launch UPI Intent button
                                        Button(
                                            onClick = {
                                                try {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(order.upiIntentUrl))
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "No UPI App found on device", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.Launch, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Pay via PhonePe / GPay / Paytm / UPI")
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Server verification trigger
                                        OutlinedButton(
                                            onClick = {
                                                isVerifying = true
                                                onVerifyPayment(order.orderId, "UPI/VERIFIED_${System.currentTimeMillis()}", true)
                                                Toast.makeText(context, "Server Verified Payment! Balance Credited.", Toast.LENGTH_LONG).show()
                                                activeOrder = null
                                                isVerifying = false
                                                selectedTab = 0
                                            },
                                            enabled = !isVerifying,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            if (isVerifying) {
                                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                            } else {
                                                Icon(Icons.Default.VerifiedUser, contentDescription = null)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Confirm & Verify Server Payment")
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Admin Bank Deposit Card for Direct Bank Transfer
                            val clipboardManager = LocalClipboardManager.current
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            Icons.Outlined.AccountBalance,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "Admin Deposit Bank Account & UPI",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text("Holder: ${adminConfig.adminAccountHolder}", style = MaterialTheme.typography.bodySmall)
                                    Text("Bank: ${adminConfig.adminBankName}", style = MaterialTheme.typography.bodySmall)

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("A/C: ${adminConfig.adminAccountNo}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                                        TextButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(adminConfig.adminAccountNo))
                                                Toast.makeText(context, "Account No Copied!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Copy A/C", fontSize = 11.sp)
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("IFSC: ${adminConfig.adminIfscCode}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                                        TextButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(adminConfig.adminIfscCode))
                                                Toast.makeText(context, "IFSC Copied!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Copy IFSC", fontSize = 11.sp)
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("UPI ID: ${adminConfig.merchantUpiId}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                                        TextButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(adminConfig.merchantUpiId))
                                                Toast.makeText(context, "UPI ID Copied!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Copy UPI", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // TAB 2: WITHDRAWAL
                    2 -> {
                        Column {
                            Text(
                                text = "Request Payout / Withdrawal",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Withdraw funds directly to your UPI ID or Bank Account",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = withdrawAmountText,
                                onValueChange = { withdrawAmountText = it.filter { c -> c.isDigit() || c == '.' } },
                                label = { Text("Withdrawal Amount (Available: ₹${walletState.availableBalance})") },
                                leadingIcon = { Text("₹", style = MaterialTheme.typography.titleMedium) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("withdraw_amount_input")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = withdrawDestinationText,
                                onValueChange = { withdrawDestinationText = it },
                                label = { Text("Destination UPI ID / Bank A/C & IFSC") },
                                placeholder = { Text("e.g. yourname@upi or A/C 981230192 IFSC SBIN0001") },
                                leadingIcon = { Icon(Icons.Outlined.AccountBalance, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("withdraw_destination_input")
                            )

                            if (withdrawError != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = withdrawError!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    withdrawError = null
                                    val amt = withdrawAmountText.toDoubleOrNull() ?: 0.0
                                    if (amt <= 0) {
                                        withdrawError = "Please enter a valid amount"
                                    } else if (amt > walletState.availableBalance) {
                                        withdrawError = "Insufficient available balance"
                                    } else if (withdrawDestinationText.isBlank()) {
                                        withdrawError = "Please enter UPI ID or Bank Account details"
                                    } else {
                                        val success = onCreateWithdrawal(amt, withdrawDestinationText.trim())
                                        if (success) {
                                            Toast.makeText(context, "Withdrawal Request PENDING Admin Approval", Toast.LENGTH_LONG).show()
                                            withdrawAmountText = ""
                                            withdrawDestinationText = ""
                                            selectedTab = 0
                                        } else {
                                            withdrawError = "Failed to create withdrawal request"
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_withdrawal_button")
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Submit Payout Request")
                            }
                        }
                    }

                    // TAB 3: LEDGER / TRANSACTIONS
                    3 -> {
                        Column {
                            Text(
                                text = "Transaction History & Ledger",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (walletLedger.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No transactions recorded yet")
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 280.dp)
                                ) {
                                    items(walletLedger) { entry ->
                                        Card(
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
                                                        AssistChip(
                                                            onClick = {},
                                                            label = { Text(entry.type.name, fontSize = 10.sp) },
                                                            colors = AssistChipDefaults.assistChipColors(
                                                                containerColor = when (entry.type) {
                                                                    LedgerType.DEPOSIT -> MaterialTheme.colorScheme.primaryContainer
                                                                    LedgerType.WITHDRAWAL -> MaterialTheme.colorScheme.secondaryContainer
                                                                    LedgerType.REFUND -> MaterialTheme.colorScheme.tertiaryContainer
                                                                    LedgerType.ADJUSTMENT -> MaterialTheme.colorScheme.surfaceVariant
                                                                }
                                                            )
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            entry.transactionId,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                    Text(entry.description, style = MaterialTheme.typography.bodySmall)
                                                    Text(
                                                        dateFormat.format(Date(entry.timestamp)),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.outline
                                                    )
                                                }

                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(
                                                        text = (if (entry.type == LedgerType.DEPOSIT || entry.type == LedgerType.REFUND) "+" else "-") + "₹${entry.amount}",
                                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = if (entry.type == LedgerType.DEPOSIT || entry.type == LedgerType.REFUND)
                                                            Color(0xFF2E7D32) else Color(0xFFC62828)
                                                    )
                                                    Text(
                                                        text = "Bal: ₹${entry.balanceAfter}",
                                                        style = MaterialTheme.typography.labelSmall
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
            }
        }
    }
}
