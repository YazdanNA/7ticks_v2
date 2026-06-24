package com.example.features.profile.presentation

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.SevenTicksApplication
import com.example.core.components.GlassCard
import com.example.core.components.PremiumGlassButton
import com.example.core.components.TikiPlaceholder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val scrollState = rememberScrollState()
    var showSettings by remember { mutableStateOf(false) }
    var isDarkMode by remember { mutableStateOf(true) }

    val repo = remember { SevenTicksApplication.instance.userRepository }
    val prefs = remember { SevenTicksApplication.instance.preferencesManager }

    val userProgress by repo.userProgress.collectAsState(initial = null)
    val achievementsList by repo.achievements.collectAsState(initial = emptyList())

    val userName = userProgress?.userName ?: prefs.userName
    val level = userProgress?.level ?: 1
    val xp = userProgress?.xp ?: 0
    val streak = userProgress?.streak ?: 1
    val targetLg = userProgress?.targetLanguage ?: prefs.targetLanguage

    val xpNeeded = level * 500
    val progressPercentage = if (xpNeeded > 0) xp.toFloat() / xpNeeded.toFloat() else 0f

    // Map avatar string to Vector Icon
    val avatarIcon = when (userProgress?.avatar ?: prefs.avatar) {
        "avatar_1" -> Icons.Default.Face
        "avatar_2" -> Icons.Default.Person
        "avatar_3" -> Icons.Default.AccountCircle
        "avatar_4" -> Icons.Default.Star
        "avatar_5" -> Icons.Default.Favorite
        "avatar_6" -> Icons.Default.PlayArrow
        else -> Icons.Default.Person
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                modifier = Modifier.fillMaxWidth(),
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
                        Icon(
                            imageVector = avatarIcon,
                            contentDescription = "Avatar",
                            tint = Color(0xFF00FFD2),
                            modifier = Modifier.size(54.dp)
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

        // 3. Level & XP Progress Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                else -> Icons.Default.Star
            }
            val tintColor = Color(android.graphics.Color.parseColor(achievement.colorHex))
            
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
