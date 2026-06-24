package com.example.features.smartlearn.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.SevenTicksApplication
import com.example.core.components.TikiPlaceholder
import com.example.core.navigation.Screen
import com.example.core.ui.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SmartLearnScreen(navController: NavController) {
    val repo = remember { SevenTicksApplication.instance.userRepository }
    val prefs = remember { SevenTicksApplication.instance.preferencesManager }
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val userProgress by repo.userProgress.collectAsState(initial = null)
    val allCards by repo.allCards.collectAsState(initial = emptyList())
    val sessionState by repo.sessionState.collectAsState(initial = null)
    val challengesList by repo.challenges.collectAsState(initial = emptyList())

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

    // Portal expansion state
    var showChallengePortal by remember { mutableStateOf(false) }

    // Breathing Hero Button Transition parameters
    val infiniteTransition = rememberInfiniteTransition(label = "hero_button_pulse")
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_breathe"
    )

    val floatOffsetY by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_float"
    )

    val glowGlow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_glow"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hi, $userName! 👋",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Today's Target: ${userProgress?.dailyGoal ?: prefs.dailyGoal}",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0x0CFFFFFF))
                        .border(1.dp, Color(0x1AFFFFFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Alerts",
                        tint = Color(0xFF00FFD2),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // SECTION 1: Progress Overview Cards
            SharedProgressHeader(
                streak = streak,
                level = level,
                xp = xp,
                badgeText = "Cognitive Guru"
            )

            // SECTION 2: Persistent Ticky Character & Typewriter Message Card
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TikiPlaceholder(
                    tikiState = "st-talking",
                    sizeDp = 90,
                    modifier = Modifier.padding(bottom = 2.dp)
                )

                SharedGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp
                ) {
                    TypewriterText(
                        text = if (isSessionActive) {
                            "You have an active spacing session in progress, $userName! Tap below to resume and solidify your memory trace!"
                        } else {
                            "Your neural pathways are highly receptive right now! Let's conquer some new terms and hit today's cognitive goal!"
                        },
                        color = Color(0xFF00FFD2),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // SECTION 5: Cognitive English Session Summary Card
            SharedGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0x159D00FF), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = Color(0xFF9D00FF),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "$targetLg Workspace",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$masteredWords of $totalWords mastered",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Text(
                            text = "$progressPct%",
                            color = Color(0xFF00FFD2),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0x12FFFFFF))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$dueCount",
                                color = Color(0xFFFF1744),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Reviews",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$learningCount",
                                color = Color(0xFF00C2FF),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
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
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
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

            // SECTION 4: Hero "START LEARNING" Button with Breathing, Pulse and Hover effect
            val btnInteractionSource = remember { MutableInteractionSource() }
            val btnPressed by btnInteractionSource.collectIsPressedAsState()
            val buttonScale by animateFloatAsState(
                targetValue = if (btnPressed) 0.94f else breatheScale,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "btn_press_breathe"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .offset(y = floatOffsetY.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(buttonScale)
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(22.dp),
                            clip = false,
                            ambientColor = Color(0xFF00C2FF).copy(alpha = glowGlow * 0.4f),
                            spotColor = Color(0xFF9D00FF).copy(alpha = glowGlow * 0.5f)
                        )
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF00C2FF), Color(0xFF9D00FF))
                            )
                        )
                        .clickable(
                            interactionSource = btnInteractionSource,
                            indication = LocalIndication.current
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            coroutineScope.launch {
                                if (!isSessionActive) {
                                    repo.generateSmartLearnSession()
                                }
                                navController.navigate(Screen.LearningSession.route)
                            }
                        }
                        .padding(horizontal = 24.dp, vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isSessionActive) Icons.Default.Refresh else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isSessionActive) "CONTINUE STUDY" else "START LEARNING SESSION",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // SECTION 3: Single Active Challenge Card
            if (challengesList.isNotEmpty()) {
                val challenge = challengesList.firstOrNull { !it.completed } ?: challengesList.first()
                val isDaily = challenge.id.startsWith("daily")
                val progress = if (challenge.target > 0) challenge.current.toFloat() / challenge.target.toFloat() else 0f

                Text(
                    text = "Active Challenge",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                SharedGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showChallengePortal = true
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (challenge.completed) Color(0x3300E676) else Color(0x1F9D00FF))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isDaily) "+150 XP" else "+250 XP",
                                    color = if (challenge.completed) Color(0xFF00E676) else Color(0xFF00FFD2),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = challenge.description,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
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

                        Text(
                            text = "Tap to open Quest & Challenge Portal",
                            color = Color(0xFF00C2FF).copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }

        // SECTION 9: CHALLENGE CENTER EXPANSION PORTAL OVERLAY
        AnimatedVisibility(
            visible = showChallengePortal,
            enter = fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.85f, animationSpec = spring(stiffness = Spring.StiffnessLow)),
            exit = fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.85f, animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xE6060713))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                SharedGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f),
                    cornerRadius = 28.dp,
                    backgroundColor = Color(0xFF0F1026)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Portal Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD600),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Quest Portal",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showChallengePortal = false
                                },
                                modifier = Modifier
                                    .background(Color(0x12FFFFFF), CircleShape)
                                    .border(1.dp, Color(0x1AFFFFFF), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Portal",
                                    tint = Color.White
                                )
                            }
                        }

                        // Scrollable challenge list
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            val portalScrollState = rememberScrollState()
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(portalScrollState),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "All Spaced Repetition Missions",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                challengesList.forEach { item ->
                                    val isDailyItem = item.id.startsWith("daily")
                                    val itemProgress = if (item.target > 0) item.current.toFloat() / item.target.toFloat() else 0f

                                    SharedGlassCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        cornerRadius = 16.dp,
                                        backgroundColor = if (item.completed) Color(0x1A00E676) else Color(0x0CFFFFFF)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = if (item.completed) Icons.Default.Check else Icons.Default.Star,
                                                        contentDescription = null,
                                                        tint = if (item.completed) Color(0xFF00E676) else Color(0xFF00C2FF),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = item.title,
                                                        color = Color.White,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                Text(
                                                    text = if (isDailyItem) "+150 XP" else "+250 XP",
                                                    color = if (item.completed) Color(0xFF00E676) else Color(0xFF00FFD2),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }

                                            Text(
                                                text = item.description,
                                                color = Color.White.copy(alpha = 0.6f),
                                                fontSize = 11.sp
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                LinearProgressIndicator(
                                                    progress = { itemProgress },
                                                    color = if (item.completed) Color(0xFF00E676) else Color(0xFF00C2FF),
                                                    trackColor = Color(0x0CFFFFFF),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(4.dp)
                                                        .clip(RoundedCornerShape(2.dp))
                                                )
                                                Text(
                                                    text = "${item.current}/${item.target}",
                                                    color = Color.White.copy(alpha = 0.5f),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Portal Footer Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF00FFD2),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Quests synchronize in real-time with the central FSRS engine",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
