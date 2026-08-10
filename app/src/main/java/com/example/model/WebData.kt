package com.example.model

data class WebBookmark(
    val title: String,
    val url: String,
    val category: String = "General"
)

data class DownloadItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val fileName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSuccess: Boolean
)

data class ManagedUrl(
    val id: String = java.util.UUID.randomUUID().toString(),
    val label: String,
    val url: String,
    val isActive: Boolean = false,
    val addedTimestamp: Long = System.currentTimeMillis()
)

data class AdminConfig(
    val activeUrl: String = "https://1goplus.com",
    val paymentEnabled: Boolean = true,
    val walletEnabled: Boolean = true,
    val withdrawalEnabled: Boolean = true,
    val appVersion: Int = 1,
    val merchantUpiId: String = "1goplus@upi",
    val merchantName: String = "1GoPlus Pay"
)

data class WalletState(
    val userId: String = "USR-10082",
    val userName: String = "Active User",
    val availableBalance: Double = 2500.00,
    val pendingBalance: Double = 0.00,
    val totalDeposited: Double = 3000.00,
    val totalWithdrawn: Double = 500.00,
    val currency: String = "INR",
    val isLocked: Boolean = false
)

enum class PaymentStatus {
    PENDING, SUCCESS, FAILED, EXPIRED, REFUNDED
}

data class PaymentOrder(
    val orderId: String,
    val userId: String,
    val amount: Double,
    val gatewayName: String = "Razorpay / UPI",
    val upiIntentUrl: String = "",
    val status: PaymentStatus = PaymentStatus.PENDING,
    val transactionRef: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val verifiedAt: Long? = null,
    val isServerVerified: Boolean = false
)

enum class WithdrawalStatus {
    PENDING, APPROVED, PROCESSING, SUCCESS, REJECTED, REFUNDED
}

data class WithdrawalRequest(
    val requestId: String = "WD-" + System.currentTimeMillis().toString().takeLast(6),
    val userId: String,
    val amount: Double,
    val destinationUpiOrBank: String,
    val status: WithdrawalStatus = WithdrawalStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val processedAt: Long? = null,
    val adminNotes: String = ""
)

enum class LedgerType {
    DEPOSIT, WITHDRAWAL, REFUND, ADJUSTMENT
}

data class WalletLedgerEntry(
    val transactionId: String = "TXN-" + System.currentTimeMillis().toString().takeLast(8),
    val userId: String,
    val type: LedgerType,
    val amount: Double,
    val balanceAfter: Double,
    val description: String,
    val status: String = "SUCCESS",
    val timestamp: Long = System.currentTimeMillis(),
    val gatewayRef: String = ""
)

data class UserModel(
    val userId: String,
    val name: String,
    val mobileOrEmail: String,
    val walletBalance: Double,
    val totalDeposited: Double,
    val totalWithdrawn: Double,
    val isLocked: Boolean = false,
    val lastActivity: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis() - 864000000L
)

data class AdminAuditLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val adminId: String = "ADMIN-01",
    val action: String,
    val target: String,
    val previousValue: String = "",
    val newValue: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class AdminDashboardSummary(
    val totalUsers: Int = 142,
    val activeUsers: Int = 98,
    val pendingPayments: Int = 3,
    val successfulDeposits: Double = 128500.00,
    val pendingWithdrawals: Int = 2,
    val successfulWithdrawals: Double = 34200.00,
    val totalRefunds: Double = 1200.00,
    val totalTransactions: Int = 310
)

data class GameBetItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val category: String,
    val team1: String = "",
    val team2: String = "",
    val odds1: String = "1.95",
    val odds2: String = "1.85",
    val oddsDraw: String = "3.20",
    val startTime: String = "LIVE NOW",
    val isLive: Boolean = true,
    val isFreeBetEligible: Boolean = true,
    val freeBetBonus: String = "₹100 FREE",
    val gameUrl: String = "https://1goplus.com"
)


