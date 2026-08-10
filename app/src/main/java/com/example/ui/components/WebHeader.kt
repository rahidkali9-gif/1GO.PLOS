package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WebBookmark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebHeader(
    currentUrl: String,
    pageTitle: String,
    loadingProgress: Int,
    isLoading: Boolean,
    isDarkMode: Boolean,
    bookmarks: List<WebBookmark>,
    onNavigateToUrl: (String) -> Unit,
    onToggleDarkMode: () -> Unit,
    onAddBookmark: () -> Unit,
    onOpenDownloads: () -> Unit,
    adminApiUrl: String = "",
    adminSyncStatus: String? = null,
    isFetchingAdminUrl: Boolean = false,
    walletBalance: Double = 0.0,
    onSyncAdminUrl: (String) -> Unit = {},
    onOpenWallet: () -> Unit = {},
    onOpenAdminDashboard: () -> Unit = {},
    onOpenAllFreeBets: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var urlInput by remember(currentUrl) { mutableStateOf(currentUrl) }
    var isEditingUrl by remember { mutableStateOf(false) }
    var showBookmarksMenu by remember { mutableStateOf(false) }
    var showAdminDialog by remember { mutableStateOf(false) }
    var adminInputUrl by remember(adminApiUrl) { mutableStateOf(adminApiUrl) }
    val focusManager = LocalFocusManager.current

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // SSL Lock Icon / Brand Logo
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (currentUrl.startsWith("https://")) Icons.Filled.Lock else Icons.Filled.Public,
                        contentDescription = "Security Status",
                        tint = if (currentUrl.startsWith("https://")) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Address Input / Title Field
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = 1.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clickable { isEditingUrl = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isEditingUrl) {
                            TextField(
                                value = urlInput,
                                onValueChange = { urlInput = it },
                                placeholder = { Text("Search or type web address") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                                keyboardActions = KeyboardActions(onGo = {
                                    onNavigateToUrl(urlInput)
                                    isEditingUrl = false
                                    focusManager.clearFocus()
                                }),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("url_input_field")
                            )

                            IconButton(
                                onClick = {
                                    onNavigateToUrl(urlInput)
                                    isEditingUrl = false
                                    focusManager.clearFocus()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowForward,
                                    contentDescription = "Go",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = pageTitle.ifBlank { "Web Connect" },
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = currentUrl,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IconButton(
                                onClick = { isEditingUrl = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = "Edit URL",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Dark Mode Toggle Button
                IconButton(
                    onClick = onToggleDarkMode,
                    modifier = Modifier.testTag("dark_mode_button")
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Quick Menu Dropdown Button
                Box {
                    IconButton(
                        onClick = { showBookmarksMenu = true },
                        modifier = Modifier.testTag("bookmarks_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu Options"
                        )
                    }

                    DropdownMenu(
                        expanded = showBookmarksMenu,
                        onDismissRequest = { showBookmarksMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Add Bookmark") },
                            leadingIcon = { Icon(Icons.Outlined.BookmarkAdd, contentDescription = null) },
                            onClick = {
                                onAddBookmark()
                                showBookmarksMenu = false
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("ALL FREE Games & Odds") },
                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFFFF6D00)) },
                            onClick = {
                                onOpenAllFreeBets()
                                showBookmarksMenu = false
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("1Go Wallet & Payouts") },
                            leadingIcon = { Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                onOpenWallet()
                                showBookmarksMenu = false
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Admin Control Panel") },
                            leadingIcon = { Icon(Icons.Outlined.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                showAdminDialog = true
                                showBookmarksMenu = false
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Saved Gallery Downloads") },
                            leadingIcon = { Icon(Icons.Outlined.Download, contentDescription = null) },
                            onClick = {
                                onOpenDownloads()
                                showBookmarksMenu = false
                            }
                        )

                        HorizontalDivider()

                        Text(
                            text = "PORTALS & SHORTCUTS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )

                        bookmarks.forEach { bookmark ->
                            DropdownMenuItem(
                                text = { Text(bookmark.title) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (bookmark.url.contains("1goplus")) Icons.Default.Star else Icons.Outlined.Public,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                onClick = {
                                    onNavigateToUrl(bookmark.url)
                                    showBookmarksMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Row 2: Prominent Action Chips Bar (ALL FREE, Wallet, Admin Panel)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ALL FREE Badge Button
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFF6D00),
                    modifier = Modifier
                        .clickable { onOpenAllFreeBets() }
                        .testTag("all_free_header_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Plus",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ALL FREE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White
                        )
                    }
                }

                // Wallet Badge Button
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .clickable { onOpenWallet() }
                        .testTag("wallet_header_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Wallet",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "₹${String.format("%.0f", walletBalance)}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Admin Control Panel Button
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier
                        .clickable { onOpenAdminDashboard() }
                        .testTag("admin_dashboard_header_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Admin",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Admin",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Animated Loading Progress Line
            AnimatedVisibility(visible = isLoading) {
                LinearProgressIndicator(
                    progress = { loadingProgress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Transparent
                )
            }
        }
    }

    if (showAdminDialog) {
        AlertDialog(
            onDismissRequest = { showAdminDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Admin Panel / Hercules Sync")
                }
            },
            text = {
                Column {
                    Text(
                        "Enter your Admin Panel API or Hercules dynamic active URL endpoint. The APK will fetch and load the active website dynamically on launch.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = adminInputUrl,
                        onValueChange = { adminInputUrl = it },
                        label = { Text("Admin Panel API URL") },
                        placeholder = { Text("https://your-admin.app/api/active-url") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (!adminSyncStatus.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = adminSyncStatus,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (adminSyncStatus.startsWith("Synced")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSyncAdminUrl(adminInputUrl)
                    },
                    enabled = !isFetchingAdminUrl && adminInputUrl.isNotBlank()
                ) {
                    if (isFetchingAdminUrl) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Sync Active URL")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
