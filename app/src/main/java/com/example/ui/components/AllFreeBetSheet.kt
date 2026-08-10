package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameBetItem

val SampleGameBets = listOf(
    GameBetItem(
        id = "GAME-101",
        title = "India vs Pakistan T20 World Cup",
        category = "Cricket",
        team1 = "India",
        team2 = "Pakistan",
        odds1 = "1.80",
        odds2 = "2.10",
        oddsDraw = "25.0",
        startTime = "LIVE 14.2 Overs",
        isLive = true,
        freeBetBonus = "₹200 FREE BET",
        gameUrl = "https://1goplus.com"
    ),
    GameBetItem(
        id = "GAME-102",
        title = "Aviator Multiplier Rocket Game",
        category = "Aviator",
        team1 = "1.00x - 100x Multiplier",
        team2 = "Instant Cashout",
        odds1 = "2.5x",
        odds2 = "10.0x",
        startTime = "ROUND STARTING IN 5s",
        isLive = true,
        freeBetBonus = "₹100 FREE",
        gameUrl = "https://1goplus.com"
    ),
    GameBetItem(
        id = "GAME-103",
        title = "Real Teen Patti & Rummy Lounge",
        category = "Teen Patti",
        team1 = "Player A",
        team2 = "Player B",
        odds1 = "1.95",
        odds2 = "1.95",
        startTime = "TABLE OPEN",
        isLive = true,
        freeBetBonus = "₹150 FREE",
        gameUrl = "https://1goplus.com"
    ),
    GameBetItem(
        id = "GAME-104",
        title = "Real Madrid vs Barcelona - El Clasico",
        category = "Football",
        team1 = "Real Madrid",
        team2 = "Barcelona",
        odds1 = "2.15",
        odds2 = "2.30",
        oddsDraw = "3.40",
        startTime = "Starting in 25 mins",
        isLive = false,
        freeBetBonus = "₹100 FREE",
        gameUrl = "https://1goplus.com"
    ),
    GameBetItem(
        id = "GAME-105",
        title = "Lightning Roulette & Live Dealer",
        category = "Casino",
        team1 = "Red",
        team2 = "Black",
        odds1 = "2.00",
        odds2 = "2.00",
        startTime = "SPINNING",
        isLive = true,
        freeBetBonus = "₹500 FREE",
        gameUrl = "https://1goplus.com"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllFreeBetSheet(
    onDismiss: () -> Unit,
    onOpenGameUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Cricket", "Aviator", "Teen Patti", "Football", "Casino")

    val filteredBets = remember(selectedCategory) {
        if (selectedCategory == "All") SampleGameBets
        else SampleGameBets.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header Banner
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFFF6D00),
                                    Color(0xFFFFAB00),
                                    MaterialTheme.colorScheme.primary
                                )
                            )
                        )
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color(0xFFFF6D00),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        "ALL FREE",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFFFF6D00)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "🔥 LIVE ODDS PREVIEW",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Game Bets & Free Odds Preview",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Pahle Game Odds Dekho aur Free Bet Bonus Claim Karo!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        leadingIcon = if (selectedCategory == cat) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Games & Odds List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
            ) {
                items(filteredBets) { game ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("game_bet_card_${game.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Top Tag line
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(game.category, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (game.isLive) {
                                        Badge(
                                            containerColor = Color(0xFFD32F2F),
                                            contentColor = Color.White
                                        ) {
                                            Text("● LIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(2.dp))
                                        }
                                    }
                                }

                                // Free Bet Tag
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color(0xFF2E7D32))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = game.freeBetBonus,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = game.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = game.startTime,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Odds preview cards
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (game.team1.isNotBlank()) {
                                    OddsBox(
                                        title = game.team1,
                                        odds = game.odds1,
                                        onClick = {
                                            Toast.makeText(context, "Selected ${game.team1} @ ${game.odds1}. Loading Game...", Toast.LENGTH_SHORT).show()
                                            onOpenGameUrl(game.gameUrl)
                                            onDismiss()
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                if (game.team2.isNotBlank()) {
                                    OddsBox(
                                        title = game.team2,
                                        odds = game.odds2,
                                        onClick = {
                                            Toast.makeText(context, "Selected ${game.team2} @ ${game.odds2}. Loading Game...", Toast.LENGTH_SHORT).show()
                                            onOpenGameUrl(game.gameUrl)
                                            onDismiss()
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                if (game.category == "Cricket" || game.category == "Football") {
                                    OddsBox(
                                        title = "Draw",
                                        odds = game.oddsDraw,
                                        onClick = {
                                            Toast.makeText(context, "Selected Draw @ ${game.oddsDraw}. Loading Game...", Toast.LENGTH_SHORT).show()
                                            onOpenGameUrl(game.gameUrl)
                                            onDismiss()
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    Toast.makeText(context, "Opening ${game.title}...", Toast.LENGTH_SHORT).show()
                                    onOpenGameUrl(game.gameUrl)
                                    onDismiss()
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.SportsEsports, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Play Now & Claim Free Bet")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OddsBox(
    title: String,
    odds: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
            Text(
                text = odds,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun AllFreeFabOverlay(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent,
        modifier = modifier
            .testTag("all_free_fab_button")
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFF6D00),
                            Color(0xFFFFAB00),
                            Color(0xFF2196F3)
                        )
                    )
                )
                .border(2.dp, Color.White, RoundedCornerShape(28.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Plus",
                        tint = Color(0xFFFF6D00),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = "ALL FREE",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White
                    )
                    Text(
                        text = "Game Bets & Odds",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}
