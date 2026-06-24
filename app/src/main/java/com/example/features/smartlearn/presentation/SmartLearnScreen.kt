package com.example.features.smartlearn.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.SevenTicksApplication
import com.example.core.components.GlassCard
import com.example.core.components.PremiumGlassButton
import com.example.core.components.TikiPlaceholder
import com.example.core.navigation.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartLearnScreen(navController: NavController) {
    val repo = remember { SevenTicksApplication.instance.userRepository }
    val prefs = remember { SevenTicksApplication.instance.preferencesManager }
    val coroutineScope = rememberCoroutineScope()

    val userProgress by repo.userProgress.collectAsState(initial = null)
    val allCards by repo.allCards.collectAsState(initial = emptyList())
    val sessionState by repo.sessionState.collectAsState(initial = null)
    val challengesList by repo.challenges.collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val userName = userProgress?.userName ?: prefs.userName
    val streak = userProgress?.streak ?: 1
    val level = userProgress?.level ?: 1
    val xp = userProgress?.xp ?: 0
    val targetLg = userProgress?.targetLanguage ?: prefs.targetLanguage

    val totalWords = allCards.size
    val masteredWords = allCards.count { it.boxIndex >= 7 }
    val progressPct = if (totalWords > 0) (masteredWords * 100) / totalWords else 0

    val dueCount = allCards.count { it.dueDate <= System.currentTimeMillis() && it.boxIndex in 1..6 }
    val learningCount = allCards.count { it.boxIndex in 2..6 }
    val newCount = allCards.count { it.boxIndex == 1 }

    val isSessionActive = sessionState?.active == true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Search Area
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search words or boxes...", color = Color.White.copy(alpha = 0.5f)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.5f)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White)
                    }
                }
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0x0AFFFFFF),
                unfocusedContainerColor = Color(0x0AFFFFFF),
                focusedIndicatorColor = Color(0xFF00C2FF),
                unfocusedIndicatorColor = Color(0x22FFFFFF),
                cursorColor = Color(0xFF00C2FF),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            textStyle = LocalTextStyle.current.copy(color = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x0AFFFFFF))
        )

        // 2. Large Greeting Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hi, $userName! 👋",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Keep going, you're doing great! Today's goal: ${userProgress?.dailyGoal ?: prefs.dailyGoal}.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0x1100C2FF))
                        .border(1.dp, Color(0x3300C2FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Alerts",
                        tint = Color(0xFF00FFD2),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // 3. Tiki Reserved Workspace Area
        TikiPlaceholder(
            message = if (isSessionActive) "You have an active session in progress! Resume it below." else "Your brain is highly receptive right now! Tap 'Start Learning' to begin.",
            modifier = Modifier.fillMaxWidth()
        )

        // 4. Score metrics: Day Streak, Level, XP Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Day Streak Card
            GlassCard(
                modifier = Modifier.weight(1f),
                cornerRadius = 18.dp
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Warning, // Flame-like representation
                        contentDescription = null,
                        tint = Color(0xFFFFAB00),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$streak",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Day Streak",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }
            }

            // Level Card
            GlassCard(
                modifier = Modifier.weight(1f),
                cornerRadius = 18.dp
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD600),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Level $level",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Explorer",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }
            }

            // XP Card
            GlassCard(
                modifier = Modifier.weight(1f),
                cornerRadius = 18.dp
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color(0xFFFF1744),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$xp XP",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Total Points",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // 5. Today's Session Preview Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0x1A9D00FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF9D00FF))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "$targetLg Session",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$masteredWords / $totalWords words mastered",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        }
                    }
                    Text(
                        text = "$progressPct%",
                        color = Color(0xFF00FFD2),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = Color.White.copy(alpha = 0.1f))

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$dueCount",
                            color = Color(0xFFFF1744),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Due Reviews",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$learningCount",
                            color = Color(0xFF00C2FF),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Learning",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$newCount",
                            color = Color(0xFF00E676),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "New Words",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // 6. Real dynamic challenges
        if (challengesList.isNotEmpty()) {
            Text(
                text = "⚡ Active Quests & Challenges",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            challengesList.forEach { challenge ->
                val progress = if (challenge.target > 0) challenge.current.toFloat() / challenge.target.toFloat() else 0f
                val isDaily = challenge.id.startsWith("daily")
                
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (challenge.completed) Icons.Default.Check else Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (challenge.completed) Color(0xFF00E676) else Color(0xFF00C2FF),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = challenge.title,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (challenge.completed) Color(0x3300E676) else Color(0x339D00FF))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isDaily) "+150 XP" else "+250 XP",
                                    color = if (challenge.completed) Color(0xFF00E676) else Color(0xFF00FFD2),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Text(
                            text = challenge.description,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { progress },
                                color = if (challenge.completed) Color(0xFF00E676) else Color(0xFF00C2FF),
                                trackColor = Color(0x1AFFFFFF),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                            Text(
                                text = "${challenge.current}/${challenge.target}",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 7. Large Primary Action Learning Button
        PremiumGlassButton(
            text = if (isSessionActive) "Continue Session" else "Start Learning Session",
            onClick = {
                coroutineScope.launch {
                    if (!isSessionActive) {
                        repo.generateSmartLearnSession()
                    }
                    navController.navigate(Screen.LearningSession.route)
                }
            },
            icon = {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
    }
}
