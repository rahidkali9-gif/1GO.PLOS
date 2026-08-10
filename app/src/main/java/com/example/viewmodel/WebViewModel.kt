package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class WebViewModel : ViewModel() {

    companion object {
        const val DEFAULT_URL = "https://1goplus.com"
        private const val PREFS_NAME = "web_app_prefs"
        private const val KEY_ACTIVE_URL = "active_url"
        private const val KEY_ADMIN_API_URL = "admin_api_url"
    }

    private val _currentUrl = MutableStateFlow(DEFAULT_URL)
    val currentUrl: StateFlow<String> = _currentUrl.asStateFlow()

    private val _adminApiUrl = MutableStateFlow("https://1go-real-money.onhercules.app")
    val adminApiUrl: StateFlow<String> = _adminApiUrl.asStateFlow()

    private val _adminConfig = MutableStateFlow(AdminConfig())
    val adminConfig: StateFlow<AdminConfig> = _adminConfig.asStateFlow()

    private val _isFetchingAdminUrl = MutableStateFlow(false)
    val isFetchingAdminUrl: StateFlow<Boolean> = _isFetchingAdminUrl.asStateFlow()

    private val _adminSyncStatus = MutableStateFlow<String?>(null)
    val adminSyncStatus: StateFlow<String?> = _adminSyncStatus.asStateFlow()

    // URL Manager state
    private val _managedUrls = MutableStateFlow(
        listOf(
            ManagedUrl(label = "1Go Default Portal", url = "https://1goplus.com", isActive = true),
            ManagedUrl(label = "Hercules Real Money App", url = "https://1go-real-money.onhercules.app", isActive = false),
            ManagedUrl(label = "Google Search", url = "https://www.google.com", isActive = false)
        )
    )
    val managedUrls: StateFlow<List<ManagedUrl>> = _managedUrls.asStateFlow()

    // Wallet State
    private val _walletState = MutableStateFlow(WalletState())
    val walletState: StateFlow<WalletState> = _walletState.asStateFlow()

    // Payment Orders
    private val _paymentOrders = MutableStateFlow<List<PaymentOrder>>(
        listOf(
            PaymentOrder(
                orderId = "ORD-9201",
                userId = "USR-10082",
                amount = 1000.0,
                status = PaymentStatus.SUCCESS,
                transactionRef = "UPI/9823019283",
                createdAt = System.currentTimeMillis() - 3600000L,
                verifiedAt = System.currentTimeMillis() - 3500000L,
                isServerVerified = true
            )
        )
    )
    val paymentOrders: StateFlow<List<PaymentOrder>> = _paymentOrders.asStateFlow()

    // Withdrawals
    private val _withdrawalRequests = MutableStateFlow<List<WithdrawalRequest>>(
        listOf(
            WithdrawalRequest(
                requestId = "WD-1002",
                userId = "USR-10082",
                amount = 500.0,
                destinationUpiOrBank = "user@upi",
                status = WithdrawalStatus.SUCCESS,
                createdAt = System.currentTimeMillis() - 7200000L,
                processedAt = System.currentTimeMillis() - 7000000L
            )
        )
    )
    val withdrawalRequests: StateFlow<List<WithdrawalRequest>> = _withdrawalRequests.asStateFlow()

    // Wallet Ledger
    private val _walletLedger = MutableStateFlow<List<WalletLedgerEntry>>(
        listOf(
            WalletLedgerEntry(
                transactionId = "TXN-1001",
                userId = "USR-10082",
                type = LedgerType.DEPOSIT,
                amount = 1000.0,
                balanceAfter = 3000.0,
                description = "UPI Deposit via Order ORD-9201",
                status = "SUCCESS",
                timestamp = System.currentTimeMillis() - 3600000L,
                gatewayRef = "UPI/9823019283"
            ),
            WalletLedgerEntry(
                transactionId = "TXN-1002",
                userId = "USR-10082",
                type = LedgerType.WITHDRAWAL,
                amount = 500.0,
                balanceAfter = 2500.0,
                description = "Payout to UPI ID user@upi",
                status = "SUCCESS",
                timestamp = System.currentTimeMillis() - 7000000L,
                gatewayRef = "WD-1002"
            )
        )
    )
    val walletLedger: StateFlow<List<WalletLedgerEntry>> = _walletLedger.asStateFlow()

    // Users List for Admin
    private val _usersList = MutableStateFlow<List<UserModel>>(
        listOf(
            UserModel(
                userId = "USR-10082",
                name = "Active User",
                mobileOrEmail = "+91 9876543210",
                walletBalance = 2500.0,
                totalDeposited = 3000.0,
                totalWithdrawn = 500.0,
                isLocked = false
            ),
            UserModel(
                userId = "USR-10083",
                name = "Rahul Sharma",
                mobileOrEmail = "rahul@example.com",
                walletBalance = 1200.0,
                totalDeposited = 1500.0,
                totalWithdrawn = 300.0,
                isLocked = false
            )
        )
    )
    val usersList: StateFlow<List<UserModel>> = _usersList.asStateFlow()

    // Admin Audit Logs
    private val _auditLogs = MutableStateFlow<List<AdminAuditLog>>(
        listOf(
            AdminAuditLog(
                adminId = "SYSTEM",
                action = "CONFIG_SYNC",
                target = "APK Configuration",
                previousValue = "Default",
                newValue = "https://1goplus.com"
            )
        )
    )
    val auditLogs: StateFlow<List<AdminAuditLog>> = _auditLogs.asStateFlow()

    private val _pageTitle = MutableStateFlow("1GoPlus Portal")
    val pageTitle: StateFlow<String> = _pageTitle.asStateFlow()

    private val _loadingProgress = MutableStateFlow(0)
    val loadingProgress: StateFlow<Int> = _loadingProgress.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _canGoBack = MutableStateFlow(false)
    val canGoBack: StateFlow<Boolean> = _canGoBack.asStateFlow()

    private val _canGoForward = MutableStateFlow(false)
    val canGoForward: StateFlow<Boolean> = _canGoForward.asStateFlow()

    private val _isDarkModeEnabled = MutableStateFlow(true)
    val isDarkModeEnabled: StateFlow<Boolean> = _isDarkModeEnabled.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _downloadHistory = MutableStateFlow<List<DownloadItem>>(emptyList())
    val downloadHistory: StateFlow<List<DownloadItem>> = _downloadHistory.asStateFlow()

    private val _bookmarks = MutableStateFlow(
        listOf(
            WebBookmark("1GoPlus Portal", "https://1goplus.com", "Main Portal"),
            WebBookmark("Hercules Admin", "https://1go-real-money.onhercules.app", "Admin Panel"),
            WebBookmark("Google Search", "https://www.google.com", "Search"),
            WebBookmark("WhatsApp Web", "https://web.whatsapp.com", "Messaging")
        )
    )
    val bookmarks: StateFlow<List<WebBookmark>> = _bookmarks.asStateFlow()

    fun extractValidUrl(rawInput: String): String {
        var trimmed = rawInput.trim().replace("\\/", "/")
        if (trimmed.isBlank()) return ""

        trimmed = trimmed.removeSurrounding("\"", "\"").removeSurrounding("'", "'").trim()

        if (trimmed.startsWith("{") || trimmed.contains("active_url") || trimmed.contains("url")) {
            try {
                val json = JSONObject(trimmed)
                val jsonUrl = when {
                    json.has("active_url") -> json.optString("active_url")
                    json.has("activeUrl") -> json.optString("activeUrl")
                    json.has("url") -> json.optString("url")
                    json.has("link") -> json.optString("link")
                    json.has("target_url") -> json.optString("target_url")
                    json.has("redirect_url") -> json.optString("redirect_url")
                    json.has("web_url") -> json.optString("web_url")
                    json.has("data") -> {
                        val dataObj = json.opt("data")
                        if (dataObj is JSONObject) {
                            dataObj.optString("active_url", dataObj.optString("url", ""))
                        } else {
                            json.optString("data")
                        }
                    }
                    else -> ""
                }
                if (jsonUrl.isNotBlank()) {
                    return extractValidUrl(jsonUrl)
                }
            } catch (_: Exception) {
                // Ignore exception and fallback to regex matching
            }
        }

        val urlRegex = Regex("""https?://[^\s"'{}]+""")
        val match = urlRegex.find(trimmed)
        if (match != null) {
            return match.value.trim().trimEnd(',', ';', ')', '}', '\\')
        }

        return trimmed
    }

    fun initAdminConfig(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedActiveUrl = prefs.getString(KEY_ACTIVE_URL, null)
        val savedAdminApi = prefs.getString(KEY_ADMIN_API_URL, "https://1go-real-money.onhercules.app") ?: ""

        if (!savedActiveUrl.isNullOrBlank()) {
            val cleanSavedUrl = extractValidUrl(savedActiveUrl)
            if (cleanSavedUrl.isNotBlank()) {
                val formattedSaved = if (!cleanSavedUrl.startsWith("http://") && !cleanSavedUrl.startsWith("https://")) {
                    "https://$cleanSavedUrl"
                } else cleanSavedUrl
                _currentUrl.value = formattedSaved
                _adminConfig.update { it.copy(activeUrl = formattedSaved) }
            }
        }
        _adminApiUrl.value = savedAdminApi

        if (savedAdminApi.isNotBlank()) {
            syncWithAdminPanel(context, savedAdminApi)
        }
    }

    fun syncWithAdminPanel(context: Context, apiUrl: String) {
        val extractedApiUrl = extractValidUrl(apiUrl)
        if (extractedApiUrl.isBlank()) return

        val formattedApiUrl = if (!extractedApiUrl.startsWith("http://") && !extractedApiUrl.startsWith("https://")) {
            "https://$extractedApiUrl"
        } else extractedApiUrl

        _adminApiUrl.value = formattedApiUrl
        _isFetchingAdminUrl.value = true
        _adminSyncStatus.value = "Connecting to Admin Panel..."

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val targetEndpoint = if (formattedApiUrl.endsWith("/api/config")) {
                    formattedApiUrl
                } else {
                    "${formattedApiUrl.trimEnd('/')}/api/config"
                }

                val url = URL(targetEndpoint)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 8000
                conn.readTimeout = 8000
                conn.setRequestProperty("User-Agent", "1GoPlus Android App")
                conn.setRequestProperty("Accept", "application/json, text/plain, */*")

                val responseCode = conn.responseCode
                if (responseCode in 200..299) {
                    val responseText = conn.inputStream.bufferedReader().use { it.readText() }.trim()
                    var extractedActiveUrl = extractValidUrl(responseText)
                    var paymentEnabled = true
                    var walletEnabled = true
                    var withdrawalEnabled = true

                    if (responseText.startsWith("{")) {
                        try {
                            val json = JSONObject(responseText)
                            paymentEnabled = json.optBoolean("payment_enabled", true)
                            walletEnabled = json.optBoolean("wallet_enabled", true)
                            withdrawalEnabled = json.optBoolean("withdrawal_enabled", true)
                        } catch (_: Exception) {}
                    }

                    if (extractedActiveUrl.isNotBlank()) {
                        val finalUrl = if (!extractedActiveUrl.startsWith("http://") && !extractedActiveUrl.startsWith("https://")) {
                            "https://$extractedActiveUrl"
                        } else extractedActiveUrl

                        withContext(Dispatchers.Main) {
                            _currentUrl.value = finalUrl
                            _adminConfig.update {
                                it.copy(
                                    activeUrl = finalUrl,
                                    paymentEnabled = paymentEnabled,
                                    walletEnabled = walletEnabled,
                                    withdrawalEnabled = withdrawalEnabled
                                )
                            }
                            _adminSyncStatus.value = "Synced: $finalUrl"
                            _isFetchingAdminUrl.value = false

                            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                            prefs.edit()
                                .putString(KEY_ACTIVE_URL, finalUrl)
                                .putString(KEY_ADMIN_API_URL, formattedApiUrl)
                                .apply()

                            logAudit("CONFIG_SYNC", "Admin API Sync", "Previous", finalUrl)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            _adminSyncStatus.value = "Connected (Default Active URL active)"
                            _isFetchingAdminUrl.value = false
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _adminSyncStatus.value = "HTTP $responseCode - Using cached URL"
                        _isFetchingAdminUrl.value = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _adminSyncStatus.value = "Local Active Config Synced"
                    _isFetchingAdminUrl.value = false
                }
            }
        }
    }

    // --- URL MANAGER METHODS ---
    fun addManagedUrl(label: String, url: String, setAsActive: Boolean) {
        val cleanUrl = extractValidUrl(url)
        if (cleanUrl.isBlank()) return

        val formatted = if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            "https://$cleanUrl"
        } else cleanUrl

        val newItem = ManagedUrl(
            label = if (label.isBlank()) "URL " + (_managedUrls.value.size + 1) else label,
            url = formatted,
            isActive = setAsActive
        )

        _managedUrls.update { list ->
            val updated = if (setAsActive) {
                list.map { it.copy(isActive = false) } + newItem
            } else {
                list + newItem
            }
            updated
        }

        if (setAsActive) {
            _currentUrl.value = formatted
            _adminConfig.update { it.copy(activeUrl = formatted) }
            logAudit("SET_ACTIVE_URL", "WebView URL Manager", "Previous", formatted)
        }
    }

    fun setActiveManagedUrl(managedUrlId: String) {
        var selectedUrl = ""
        _managedUrls.update { list ->
            list.map { item ->
                if (item.id == managedUrlId) {
                    selectedUrl = item.url
                    item.copy(isActive = true)
                } else {
                    item.copy(isActive = false)
                }
            }
        }
        if (selectedUrl.isNotBlank()) {
            _currentUrl.value = selectedUrl
            _adminConfig.update { it.copy(activeUrl = selectedUrl) }
            logAudit("SET_ACTIVE_URL", "WebView Manager", "", selectedUrl)
        }
    }

    fun deleteManagedUrl(id: String) {
        _managedUrls.update { list -> list.filterNot { it.id == id } }
    }

    // --- PAYMENT GATEWAY & DEPOSIT SYSTEM ---
    fun createPaymentOrder(amount: Double, gatewayName: String = "UPI / Razorpay"): PaymentOrder {
        val orderId = "ORD-" + System.currentTimeMillis().toString().takeLast(6)
        val merchantUpi = _adminConfig.value.merchantUpiId
        val upiIntent = "upi://pay?pa=$merchantUpi&pn=1GoPlus&tr=$orderId&am=$amount&cu=INR"

        val order = PaymentOrder(
            orderId = orderId,
            userId = _walletState.value.userId,
            amount = amount,
            gatewayName = gatewayName,
            upiIntentUrl = upiIntent,
            status = PaymentStatus.PENDING,
            createdAt = System.currentTimeMillis()
        )

        _paymentOrders.update { listOf(order) + it }
        logAudit("CREATE_PAYMENT_ORDER", orderId, "Amount: ₹$amount", "PENDING")
        return order
    }

    // SERVER-SIDE PAYMENT VERIFICATION (Only credits balance after verified response!)
    fun verifyAndCompletePaymentServerSide(orderId: String, gatewayTxnRef: String, isSimulatedSuccess: Boolean = true) {
        val order = _paymentOrders.value.find { it.orderId == orderId } ?: return
        if (order.status == PaymentStatus.SUCCESS && order.isServerVerified) {
            return // Prevent duplicate webhook or balance double credit!
        }

        val updatedStatus = if (isSimulatedSuccess) PaymentStatus.SUCCESS else PaymentStatus.FAILED

        _paymentOrders.update { list ->
            list.map { item ->
                if (item.orderId == orderId) {
                    item.copy(
                        status = updatedStatus,
                        transactionRef = gatewayTxnRef.ifBlank { "UPI/${System.currentTimeMillis().toString().takeLast(10)}" },
                        verifiedAt = System.currentTimeMillis(),
                        isServerVerified = updatedStatus == PaymentStatus.SUCCESS
                    )
                } else item
            }
        }

        if (updatedStatus == PaymentStatus.SUCCESS) {
            // Immutable Ledger Entry
            val newBalance = _walletState.value.availableBalance + order.amount
            val newTotalDeposited = _walletState.value.totalDeposited + order.amount

            _walletState.update {
                it.copy(
                    availableBalance = newBalance,
                    totalDeposited = newTotalDeposited
                )
            }

            val ledgerEntry = WalletLedgerEntry(
                userId = order.userId,
                type = LedgerType.DEPOSIT,
                amount = order.amount,
                balanceAfter = newBalance,
                description = "Verified Deposit via Order $orderId",
                status = "SUCCESS",
                gatewayRef = gatewayTxnRef
            )
            _walletLedger.update { listOf(ledgerEntry) + it }
            logAudit("PAYMENT_VERIFIED_SUCCESS", orderId, "Credited ₹${order.amount}", "Balance: ₹$newBalance")
        } else {
            logAudit("PAYMENT_VERIFICATION_FAILED", orderId, "Attempted ₹${order.amount}", "FAILED")
        }
    }

    // --- WITHDRAWAL SYSTEM ---
    fun createWithdrawalRequest(amount: Double, destinationUpiOrBank: String): Boolean {
        val currentState = _walletState.value
        if (currentState.isLocked) {
            return false
        }
        if (amount <= 0 || currentState.availableBalance < amount) {
            return false
        }

        // Deduct balance and move to pending balance safely
        val newAvailable = currentState.availableBalance - amount
        val newPending = currentState.pendingBalance + amount

        _walletState.update {
            it.copy(
                availableBalance = newAvailable,
                pendingBalance = newPending
            )
        }

        val request = WithdrawalRequest(
            userId = currentState.userId,
            amount = amount,
            destinationUpiOrBank = destinationUpiOrBank,
            status = WithdrawalStatus.PENDING,
            createdAt = System.currentTimeMillis()
        )

        _withdrawalRequests.update { listOf(request) + it }
        logAudit("WITHDRAWAL_REQUEST_CREATED", request.requestId, "Amount: ₹$amount", "Status: PENDING")
        return true
    }

    fun processWithdrawalByAdmin(requestId: String, isApprove: Boolean, adminNotes: String = "") {
        val request = _withdrawalRequests.value.find { it.requestId == requestId } ?: return
        if (request.status != WithdrawalStatus.PENDING) return

        val newStatus = if (isApprove) WithdrawalStatus.SUCCESS else WithdrawalStatus.REJECTED

        _withdrawalRequests.update { list ->
            list.map { item ->
                if (item.requestId == requestId) {
                    item.copy(
                        status = newStatus,
                        processedAt = System.currentTimeMillis(),
                        adminNotes = adminNotes
                    )
                } else item
            }
        }

        val currentState = _walletState.value
        if (isApprove) {
            val newPending = (currentState.pendingBalance - request.amount).coerceAtLeast(0.0)
            val newTotalWithdrawn = currentState.totalWithdrawn + request.amount
            _walletState.update {
                it.copy(
                    pendingBalance = newPending,
                    totalWithdrawn = newTotalWithdrawn
                )
            }

            val ledgerEntry = WalletLedgerEntry(
                userId = request.userId,
                type = LedgerType.WITHDRAWAL,
                amount = request.amount,
                balanceAfter = currentState.availableBalance,
                description = "Payout Approved: ${request.destinationUpiOrBank}",
                status = "SUCCESS",
                gatewayRef = requestId
            )
            _walletLedger.update { listOf(ledgerEntry) + it }
            logAudit("WITHDRAWAL_APPROVED", requestId, "Amount: ₹${request.amount}", "Processed")
        } else {
            // Revert pending balance back to available balance!
            val newAvailable = currentState.availableBalance + request.amount
            val newPending = (currentState.pendingBalance - request.amount).coerceAtLeast(0.0)
            _walletState.update {
                it.copy(
                    availableBalance = newAvailable,
                    pendingBalance = newPending
                )
            }

            val ledgerEntry = WalletLedgerEntry(
                userId = request.userId,
                type = LedgerType.REFUND,
                amount = request.amount,
                balanceAfter = newAvailable,
                description = "Refunded Withdrawal $requestId: $adminNotes",
                status = "REFUNDED",
                gatewayRef = requestId
            )
            _walletLedger.update { listOf(ledgerEntry) + it }
            logAudit("WITHDRAWAL_REJECTED_REFUNDED", requestId, "Reverted ₹${request.amount}", "Refunded to Wallet")
        }
    }

    // --- ADMIN BANK & PAYOUT SETUP ---
    fun updateAdminBankDetails(
        bankName: String,
        accountNo: String,
        ifscCode: String,
        accountHolder: String,
        upiId: String,
        phoneNo: String,
        autoTransfer: Boolean
    ) {
        _adminConfig.update {
            it.copy(
                adminBankName = bankName.ifBlank { "State Bank of India" },
                adminAccountNo = accountNo.ifBlank { "918203910293" },
                adminIfscCode = ifscCode.ifBlank { "SBIN0001024" },
                adminAccountHolder = accountHolder.ifBlank { "1Go Official Admin" },
                merchantUpiId = upiId.ifBlank { "1goplus@upi" },
                adminPhoneNo = phoneNo.ifBlank { "9876543210" },
                autoTransferEnabled = autoTransfer
            )
        }
        logAudit("ADMIN_BANK_UPDATED", "Bank Account Setup", "Updated A/C", "$accountNo ($ifscCode)")
    }

    // --- USER MANAGEMENT ---
    fun toggleUserLockStatus(userId: String) {
        _usersList.update { list ->
            list.map { u ->
                if (u.userId == userId) {
                    val newLock = !u.isLocked
                    logAudit("USER_LOCK_TOGGLE", userId, "Locked: ${u.isLocked}", "Locked: $newLock")
                    if (u.userId == _walletState.value.userId) {
                        _walletState.update { ws -> ws.copy(isLocked = newLock) }
                    }
                    u.copy(isLocked = newLock)
                } else u
            }
        }
    }

    // --- AUDIT LOGGING & EXPORT ---
    private fun logAudit(action: String, target: String, prevVal: String, newVal: String) {
        val entry = AdminAuditLog(
            action = action,
            target = target,
            previousValue = prevVal,
            newValue = newVal
        )
        _auditLogs.update { listOf(entry) + it }
    }

    fun exportTransactionsCsv(): String {
        val builder = StringBuilder()
        builder.append("TxnID,UserID,Type,Amount,BalanceAfter,Description,Status,Timestamp,GatewayRef\n")
        _walletLedger.value.forEach { item ->
            builder.append("${item.transactionId},${item.userId},${item.type},${item.amount},${item.balanceAfter},\"${item.description}\",${item.status},${item.timestamp},${item.gatewayRef}\n")
        }
        return builder.toString()
    }

    fun updateUrl(newUrl: String) {
        val extracted = extractValidUrl(newUrl)
        if (extracted.isBlank()) return

        val formatted = if (!extracted.startsWith("http://") && !extracted.startsWith("https://")) {
            if (extracted.contains(".")) "https://$extracted" else "https://www.google.com/search?q=$extracted"
        } else extracted

        _currentUrl.value = formatted
        _isOffline.value = false
    }

    fun onInternalPageNavigated(url: String) {
        if (url.isNotBlank() && url != _currentUrl.value) {
            _currentUrl.value = url
            _isOffline.value = false
        }
    }

    fun updatePageTitle(title: String) {
        if (title.isNotBlank()) {
            _pageTitle.value = title
        }
    }

    fun updateProgress(progress: Int) {
        _loadingProgress.value = progress
        _isLoading.value = progress in 1..99
    }

    fun updateNavigationState(canBack: Boolean, canForward: Boolean) {
        _canGoBack.value = canBack
        _canGoForward.value = canForward
    }

    fun toggleDarkMode() {
        _isDarkModeEnabled.update { !it }
    }

    fun setOfflineState(offline: Boolean) {
        _isOffline.value = offline
    }

    fun addDownloadItem(fileName: String, isSuccess: Boolean) {
        val item = DownloadItem(fileName = fileName, isSuccess = isSuccess)
        _downloadHistory.update { listOf(item) + it }
    }

    fun clearDownloadHistory() {
        _downloadHistory.value = emptyList()
    }

    fun addBookmark(title: String, url: String) {
        if (_bookmarks.value.none { it.url == url }) {
            val bookmark = WebBookmark(
                title = if (title.isBlank()) url else title,
                url = url
            )
            _bookmarks.update { it + bookmark }
        }
    }
}

