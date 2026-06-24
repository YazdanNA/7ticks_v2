package com.example.features.splash.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.core.components.GlassCard
import com.example.core.components.PremiumGlassButton
import com.example.core.navigation.Screen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    var loadingProgress by remember { mutableStateOf(0f) }
    var showButton by remember { mutableStateOf(false) }

    val prefs = remember { com.example.SevenTicksApplication.instance.preferencesManager }

    LaunchedEffect(Unit) {
        // Animate progress smoothly
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(2500, easing = LinearEasing)
        ) { value, _ ->
            loadingProgress = value
        }
        showButton = true
        // Auto navigate after brief delay
        delay(800)
        val nextRoute = if (prefs.isFirstLaunch) Screen.Onboarding.route else Screen.Main.route
        navController.navigate(nextRoute) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    // Dynamic linear gradient moving background
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shift"
    )

    val bgBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF070814),
            Color(0xFF0F1026),
            Color(0xFF1F0C33),
            Color(0xFF070814)
        ),
        start = androidx.compose.ui.geometry.Offset(gradientShift, 0f),
        end = androidx.compose.ui.geometry.Offset(gradientShift + 1000f, 1500f)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Elegant Splash Card
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                cornerRadius = 32.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 32.dp, horizontal = 16.dp)
                ) {
                    // Glowing logo: "7Ticks"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "7",
                            color = Color(0xFF00C2FF),
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.displayLarge,
                            modifier = Modifier.animateContentSize()
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Ticks",
                            color = Color.White,
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.displayMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Learn • Remember • Grow",
                        color = Color(0xFFD0BCFF),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    // Progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x1AFFFFFF))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(loadingProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF00C2FF), Color(0xFF9D00FF))
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Preparing Space Repetition System...",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Direct fallback button if transition delay isn't completed
            AnimatedVisibility(
                visible = showButton,
                enter = fadeIn() + expandVertically()
            ) {
                PremiumGlassButton(
                    text = if (prefs.isFirstLaunch) "Get Started" else "Enter App",
                    onClick = {
                        val nextRoute = if (prefs.isFirstLaunch) Screen.Onboarding.route else Screen.Main.route
                        navController.navigate(nextRoute) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
        }
    }
}
