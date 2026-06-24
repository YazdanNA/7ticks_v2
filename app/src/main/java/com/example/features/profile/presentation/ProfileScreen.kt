package com.example.features.profile.presentation

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.SevenTicksApplication
import com.example.core.components.GlassCard
import com.example.core.components.PremiumGlassButton
import com.example.core.components.TikiPlaceholder
import com.example.core.components.AvatarManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showSettings by remember { mutableStateOf(false) }
    var isDarkMode by remember { mutableStateOf(true) }

    val repo = remember { SevenTicksApplication.instance.userRepository }
    val prefs = remember { SevenTicksApplication.instance.preferencesManager }

    val userProgress by repo.userProgress.collectAsState(initial = null)
    val achievementsList by repo.achievements.collectAsState(initial = emptyList())
    val allCards by repo.allCards.collectAsState(initial = emptyList())
    
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val userName = userProgress?.userName ?: prefs.userName
    val level = userProgress?.level ?: 1
    val xp = userProgress?.xp ?: 0
    val streak = userProgress?.streak ?: 1
    val longestStreak = userProgress?.longestStreak ?: streak
    val targetLg = userProgress?.targetLanguage ?: prefs.targetLanguage
    val spells = userProgress?.streakRestoreSpells ?: 1

    val xpNeeded = level * 500
    val progressPercentage = if (xpNeeded > 0) xp.toFloat() / xpNeeded.toFloat() else 0f

    val currentAvatarId = userProgress?.avatar ?: prefs.avatar

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Profile Title & Settings Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Profile",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { showSettings = true },
                    modifier = Modifier
                        .background(Color(0x0AFFFFFF), CircleShape)
                        .border(1.dp, Color(0x1AFFFFFF), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }
            }

            // 2. Avatar & Name Section
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF00C2FF), Color(0xFF9D00FF))
                                )
                            )
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(0xFF0F1026)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = AvatarManager.getAvatarResId(context, currentAvatarId)),
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize().padding(8.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = userName,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Level $level • $targetLg Master • $streak-Day Streak",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 13.sp
                    )
                }
            }

            // 2.5 Streak Maintenance & Restoration Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Streak Status", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF00FFD2), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("$spells Freeze Spells", color = Color(0xFF00FFD2), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$streak Days", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                            Text("Current Streak", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$longestStreak Days", color = Color(0xFF00FFD2), fontSize = 18.sp, fontWeight = FontWeight.Black)
                            Text("Longest Streak", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                    }

                    if (streak < longestStreak) {
                        PremiumGlassButton(
                            text = "Cast Streak Restore Spell (-1 Spell)",
                            onClick = {
                                coroutineScope.launch {
                                    val success = repo.restoreStreak()
                                    if (success) {
                                        snackbarHostState.showSnackbar("✨ Streak successfully restored to your longest record! ✨")
                                    } else {
                                        snackbarHostState.showSnackbar("❌ No Streak Restore Spells available!")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = "🔥 Streak is fully secure and healthy! Keep learning to extend your record.",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }

            // 3. Level & XP Progress Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Experience Progress",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$xp / $xpNeeded XP",
                            color = Color(0xFF00FFD2),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    LinearProgressIndicator(
                        progress = { progressPercentage },
                        color = Color(0xFF9D00FF),
                        trackColor = Color(0x1AFFFFFF),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    
                    Text(
                        text = "${xpNeeded - xp} XP required to level up to Level ${level + 1}. Keep it up!",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }
            }

            // 3.5 Learning Statistics Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Cognitive Statistics", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${allCards.size}", color = Color(0xFF00C2FF), fontSize = 20.sp, fontWeight = FontWeight.Black)
                            Text("Total Cards", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${allCards.count { it.boxIndex >= 7 }}", color = Color(0xFF00E676), fontSize = 20.sp, fontWeight = FontWeight.Black)
                            Text("Mastered", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${allCards.count { it.boxIndex in 1..6 }}", color = Color(0xFFFFD600), fontSize = 20.sp, fontWeight = FontWeight.Black)
                            Text("In Learning", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                    }

                    // Simple visual representation of Box distribution
                    Row(
                        modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(8.dp)),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        for (box in 1..7) {
                            val count = allCards.count { it.boxIndex == box }
                            val weight = if (allCards.isEmpty()) 1f else (count.toFloat() / allCards.size.toFloat()).coerceAtLeast(0.05f)
                            val color = when (box) {
                                1 -> Color(0xFFFF1744)
                                2 -> Color(0xFFFF5252)
                                3 -> Color(0xFFFF9100)
                                4 -> Color(0xFFFFD600)
                                5 -> Color(0xFF00E5FF)
                                6 -> Color(0xFF2979FF)
                                else -> Color(0xFF00E676)
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(weight)
                                    .background(color)
                            )
                        }
                    }
                    Text(
                        text = "Distribution across Leitner Box 1 (Left/Red) to Box 7 (Right/Green).",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

            // 4. Achievements List
            val unlockedCount = achievementsList.count { it.unlocked }
            Text(
                text = "Achievements ($unlockedCount/${achievementsList.size.coerceAtLeast(3)})",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            achievementsList.forEach { achievement ->
                val iconVec = when (achievement.iconName) {
                    "Check" -> Icons.Default.Check
                    "Star" -> Icons.Default.Star
                    "Info" -> Icons.Default.Info
                    "PlayArrow" -> Icons.Default.PlayArrow
                    "LockOpen" -> Icons.Default.Lock
                    else -> Icons.Default.Star
                }
                val tintColor = Color(android.graphics.Color.parseColor(achievement.colorHex))
                
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (achievement.unlocked) tintColor.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconVec,
                                contentDescription = null,
                                tint = if (achievement.unlocked) tintColor else Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = achievement.name,
                                color = if (achievement.unlocked) Color.White else Color.White.copy(alpha = 0.4f),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = achievement.description,
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // 5. Tiki Mascot Box
            TikiPlaceholder(
                message = if (unlockedCount > 0) {
                    "You are making stellar progress, $userName! You have already unlocked $unlockedCount achievements."
                } else {
                    "Let's master some words today to unlock your first spaced repetition milestone!"
                },
                sizeDp = 60,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // 6. Settings Interactive Dialog Modal (Mirroring Reference Menus)
    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            containerColor = Color(0xFF0F1026),
            title = {
                Text(
                    text = "Settings",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    // Option 1: Native Language
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF00C2FF))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Native Lang", color = Color.White, fontSize = 14.sp)
                        }
                        Text("${userProgress?.nativeLanguage ?: prefs.nativeLanguage} >", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                    }

                    // Option 2: Target Language
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ThumbUp, contentDescription = null, tint = Color(0xFF9D00FF))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Target Lang", color = Color.White, fontSize = 14.sp)
                        }
                        Text("${userProgress?.targetLanguage ?: prefs.targetLanguage} >", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                    }

                    // Option 3: Dark Mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD600))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Dark Mode", color = Color.White, fontSize = 14.sp)
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { isDarkMode = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF00C2FF),
                                checkedTrackColor = Color(0x3300C2FF)
                            )
                        )
                    }

                    // Option 4: Daily Goal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFFFF1744))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Daily Goal", color = Color.White, fontSize = 14.sp)
                        }
                        Text("${userProgress?.dailyGoal ?: prefs.dailyGoal} >", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                    }

                    // Option 5: About
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color.White.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("About 7Ticks", color = Color.White, fontSize = 14.sp)
                        }
                        Text("v1.0.0", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettings = false }) {
                    Text("Close", color = Color(0xFF00FFD2), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

data class AchievementItem(
    val name: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)
