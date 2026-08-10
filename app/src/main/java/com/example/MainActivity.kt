package com.example

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.*
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.DownloadItem
import com.example.ui.components.*
import com.example.ui.theme.MyApplicationTheme
import com.example.util.DownloadHelper
import com.example.util.IntentHelper
import com.example.viewmodel.WebViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: WebViewModel = viewModel()
            val isDarkMode by viewModel.isDarkModeEnabled.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = isDarkMode) {
                MainAppScreen(
                    viewModel = viewModel,
                    onShareUrl = { url, title -> shareUrl(url, title) }
                )
            }
        }
    }

    private fun shareUrl(url: String, title: String) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "$title\n$url")
            }
            startActivity(Intent.createChooser(shareIntent, "Share Web Link"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Unable to share link", Toast.LENGTH_SHORT).show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: WebViewModel,
    onShareUrl: (String, String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentUrl by viewModel.currentUrl.collectAsStateWithLifecycle()
    val adminApiUrl by viewModel.adminApiUrl.collectAsStateWithLifecycle()
    val isFetchingAdminUrl by viewModel.isFetchingAdminUrl.collectAsStateWithLifecycle()
    val adminSyncStatus by viewModel.adminSyncStatus.collectAsStateWithLifecycle()
    val pageTitle by viewModel.pageTitle.collectAsStateWithLifecycle()
    val loadingProgress by viewModel.loadingProgress.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val canGoBack by viewModel.canGoBack.collectAsStateWithLifecycle()
    val canGoForward by viewModel.canGoForward.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkModeEnabled.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val downloadHistory by viewModel.downloadHistory.collectAsStateWithLifecycle()

    val walletState by viewModel.walletState.collectAsStateWithLifecycle()
    val adminConfig by viewModel.adminConfig.collectAsStateWithLifecycle()
    val paymentOrders by viewModel.paymentOrders.collectAsStateWithLifecycle()
    val withdrawalRequests by viewModel.withdrawalRequests.collectAsStateWithLifecycle()
    val walletLedger by viewModel.walletLedger.collectAsStateWithLifecycle()
    val usersList by viewModel.usersList.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val managedUrls by viewModel.managedUrls.collectAsStateWithLifecycle()

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var showDownloadSheet by remember { mutableStateOf(false) }
    var showWalletDialog by remember { mutableStateOf(false) }
    var showAdminDashboardDialog by remember { mutableStateOf(false) }
    var showAllFreeBetsSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.initAdminConfig(context)
    }

    // Intercept hardware Back Button to navigate back in WebView history
    BackHandler(enabled = canGoBack) {
        webViewRef?.goBack()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            WebHeader(
                currentUrl = currentUrl,
                pageTitle = pageTitle,
                loadingProgress = loadingProgress,
                isLoading = isLoading,
                isDarkMode = isDarkMode,
                bookmarks = bookmarks,
                onNavigateToUrl = { newUrl -> viewModel.updateUrl(newUrl) },
                onToggleDarkMode = { viewModel.toggleDarkMode() },
                onAddBookmark = {
                    viewModel.addBookmark(pageTitle, currentUrl)
                    Toast.makeText(context, "Added to Bookmarks", Toast.LENGTH_SHORT).show()
                },
                onOpenDownloads = { showDownloadSheet = true },
                adminApiUrl = adminApiUrl,
                adminSyncStatus = adminSyncStatus,
                isFetchingAdminUrl = isFetchingAdminUrl,
                walletBalance = walletState.availableBalance,
                onSyncAdminUrl = { url -> viewModel.syncWithAdminPanel(context, url) },
                onOpenWallet = { showWalletDialog = true },
                onOpenAdminDashboard = { showAdminDashboardDialog = true },
                onOpenAllFreeBets = { showAllFreeBetsSheet = true }
            )
        },
        bottomBar = {
            WebBottomBar(
                canGoBack = canGoBack,
                canGoForward = canGoForward,
                isLoading = isLoading,
                onBackClick = { webViewRef?.goBack() },
                onForwardClick = { webViewRef?.goForward() },
                onRefreshClick = {
                    if (isLoading) webViewRef?.stopLoading()
                    else webViewRef?.reload()
                },
                onHomeClick = { viewModel.updateUrl(WebViewModel.DEFAULT_URL) },
                onOpenDownloads = { showDownloadSheet = true },
                onShareClick = { onShareUrl(currentUrl, pageTitle) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isOffline) {
                OfflineCard(
                    onRetry = {
                        viewModel.setOfflineState(false)
                        webViewRef?.reload()
                    }
                )
            } else {
                WebViewContainer(
                    url = currentUrl,
                    isDarkMode = isDarkMode,
                    onWebViewCreated = { webViewRef = it },
                    onProgressChanged = { viewModel.updateProgress(it) },
                    onTitleReceived = { viewModel.updatePageTitle(it) },
                    onUrlChanged = { viewModel.onInternalPageNavigated(it) },
                    onNavigationStateChanged = { canBack, canFwd ->
                        viewModel.updateNavigationState(canBack, canFwd)
                    },
                    onError = { viewModel.setOfflineState(true) },
                    onDownloadTriggered = { downloadUrl, contentDisposition, mimeType ->
                        Toast.makeText(context, "Downloading Images...", Toast.LENGTH_SHORT).show()
                        coroutineScope.launch {
                            val success = if (downloadUrl.startsWith("data:")) {
                                DownloadHelper.saveBase64ImageToGallery(
                                    context = context,
                                    base64Data = downloadUrl,
                                    fileName = "image_${System.currentTimeMillis()}.jpg",
                                    mimeType = mimeType
                                )
                            } else {
                                val suggestedName = URLUtil.guessFileName(downloadUrl, contentDisposition, mimeType)
                                DownloadHelper.downloadUrlToGallery(
                                    context = context,
                                    urlStr = downloadUrl,
                                    suggestedFileName = suggestedName,
                                    mimeType = mimeType
                                )
                            }

                            if (success) {
                                Toast.makeText(context, "Saved To Gallery", Toast.LENGTH_SHORT).show()
                                viewModel.addDownloadItem("Image_${System.currentTimeMillis()}.jpg", true)
                            } else {
                                Toast.makeText(context, "Save Failed", Toast.LENGTH_SHORT).show()
                                viewModel.addDownloadItem("Image_${System.currentTimeMillis()}.jpg", false)
                            }
                        }
                    }
                )
            }

            // Floating "ALL FREE (+)" FAB Overlay
            AllFreeFabOverlay(
                onClick = { showAllFreeBetsSheet = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }

        if (showAllFreeBetsSheet) {
            AllFreeBetSheet(
                onDismiss = { showAllFreeBetsSheet = false },
                onOpenGameUrl = { gameUrl -> viewModel.updateUrl(gameUrl) }
            )
        }

        if (showDownloadSheet) {

            DownloadHistorySheet(
                history = downloadHistory,
                onDismiss = { showDownloadSheet = false },
                onClearHistory = { viewModel.clearDownloadHistory() }
            )
        }

        if (showWalletDialog) {
            WalletDialog(
                walletState = walletState,
                paymentOrders = paymentOrders,
                withdrawalRequests = withdrawalRequests,
                walletLedger = walletLedger,
                adminConfig = adminConfig,
                onDismiss = { showWalletDialog = false },
                onCreatePaymentOrder = { amount -> viewModel.createPaymentOrder(amount) },
                onVerifyPayment = { orderId, ref, isSuccess ->
                    viewModel.verifyAndCompletePaymentServerSide(orderId, ref, isSuccess)
                },
                onCreateWithdrawal = { amount, dest ->
                    viewModel.createWithdrawalRequest(amount, dest)
                }
            )
        }

        if (showAdminDashboardDialog) {
            AdminDashboardDialog(
                adminConfig = adminConfig,
                adminApiUrl = adminApiUrl,
                adminSyncStatus = adminSyncStatus,
                isFetchingAdminUrl = isFetchingAdminUrl,
                managedUrls = managedUrls,
                usersList = usersList,
                withdrawalRequests = withdrawalRequests,
                paymentOrders = paymentOrders,
                auditLogs = auditLogs,
                onDismiss = { showAdminDashboardDialog = false },
                onSyncAdminPanel = { url -> viewModel.syncWithAdminPanel(context, url) },
                onAddManagedUrl = { label, url, setAsActive -> viewModel.addManagedUrl(label, url, setAsActive) },
                onSetActiveUrl = { id -> viewModel.setActiveManagedUrl(id) },
                onDeleteManagedUrl = { id -> viewModel.deleteManagedUrl(id) },
                onProcessWithdrawal = { reqId, approve, notes -> viewModel.processWithdrawalByAdmin(reqId, approve, notes) },
                onVerifyPayment = { orderId, ref, success -> viewModel.verifyAndCompletePaymentServerSide(orderId, ref, success) },
                onToggleUserLock = { userId -> viewModel.toggleUserLockStatus(userId) },
                onExportCsv = { viewModel.exportTransactionsCsv() },
                onUpdateBankDetails = { bankName, accountNo, ifscCode, holder, upiId, phoneNo, autoTransfer ->
                    viewModel.updateAdminBankDetails(bankName, accountNo, ifscCode, holder, upiId, phoneNo, autoTransfer)
                }
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewContainer(
    url: String,
    isDarkMode: Boolean,
    onWebViewCreated: (WebView) -> Unit,
    onProgressChanged: (Int) -> Unit,
    onTitleReceived: (String) -> Unit,
    onUrlChanged: (String) -> Unit,
    onNavigationStateChanged: (Boolean, Boolean) -> Unit,
    onError: () -> Unit,
    onDownloadTriggered: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var lastLoadedUrl by remember { mutableStateOf("") }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )

                setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    allowFileAccess = true
                    allowContentAccess = true
                    javaScriptCanOpenWindowsAutomatically = true
                    mediaPlaybackRequiresUserGesture = false
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    userAgentString = userAgentString.replace("wv", "") // Clean Mobile User Agent
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        settings.forceDark = if (isDarkMode) WebSettings.FORCE_DARK_ON else WebSettings.FORCE_DARK_OFF
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val reqUrl = request?.url?.toString() ?: return false
                        return IntentHelper.handleExternalUri(context, reqUrl)
                    }

                    @Deprecated("Deprecated in Java")
                    override fun shouldOverrideUrlLoading(view: WebView?, urlStr: String?): Boolean {
                        if (urlStr == null) return false
                        return IntentHelper.handleExternalUri(context, urlStr)
                    }

                    override fun onPageStarted(view: WebView?, pageUrl: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, pageUrl, favicon)
                        view?.let {
                            onNavigationStateChanged(it.canGoBack(), it.canGoForward())
                            if (!pageUrl.isNullOrBlank()) {
                                lastLoadedUrl = pageUrl
                                onUrlChanged(pageUrl)
                            }
                        }
                    }

                    override fun onPageFinished(view: WebView?, pageUrl: String?) {
                        super.onPageFinished(view, pageUrl)
                        view?.let {
                            onNavigationStateChanged(it.canGoBack(), it.canGoForward())
                            it.title?.let { title -> onTitleReceived(title) }
                            if (!pageUrl.isNullOrBlank()) {
                                lastLoadedUrl = pageUrl
                                onUrlChanged(pageUrl)
                            }
                        }
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        if (request?.isForMainFrame == true) {
                            onError()
                        }
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        onProgressChanged(newProgress)
                        view?.let {
                            onNavigationStateChanged(it.canGoBack(), it.canGoForward())
                        }
                    }

                    override fun onReceivedTitle(view: WebView?, title: String?) {
                        super.onReceivedTitle(view, title)
                        title?.let { onTitleReceived(it) }
                    }

                    override fun onJsAlert(
                        view: WebView?,
                        url: String?,
                        message: String?,
                        result: android.webkit.JsResult?
                    ): Boolean {
                        result?.confirm()
                        return true
                    }

                    override fun onJsConfirm(
                        view: WebView?,
                        url: String?,
                        message: String?,
                        result: android.webkit.JsResult?
                    ): Boolean {
                        result?.confirm()
                        return true
                    }
                }

                // Download Listener matching exact decompiled method behavior
                setDownloadListener { downloadUrl, userAgent, contentDisposition, mimeType, contentLength ->
                    onDownloadTriggered(downloadUrl, contentDisposition, mimeType ?: "image/jpeg")
                }

                onWebViewCreated(this)
                if (url.isNotBlank()) {
                    lastLoadedUrl = url
                    loadUrl(url)
                }
            }
        },
        update = { webView ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    webView.settings.forceDark = if (isDarkMode) WebSettings.FORCE_DARK_ON else WebSettings.FORCE_DARK_OFF
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Only trigger loadUrl if the top-level URL requested has explicitly changed
            if (url.isNotBlank() && url != lastLoadedUrl) {
                lastLoadedUrl = url
                webView.loadUrl(url)
            }
        }
    )
}
