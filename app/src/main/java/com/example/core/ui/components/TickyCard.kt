package com.example.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.components.TikiPlaceholder
import kotlinx.coroutines.delay

/**
 * Reusable Global Ticky Card Component.
 * Features:
 * - Tiki mascot avatar (uses TikiPlaceholder with talking/breathing animations)
 * - Single message or rotating typewriter message sequence
 * - Deletion/erase visual typewriter animation
 * - Glassmorphic styled card container
 * - State-driven animation hooks
 */
@Composable
fun TickyCard(
    modifier: Modifier = Modifier,
    tikiState: String = "st-happy",
    sizeDp: Int = 80,
    message: String? = null,
    messages: List<String>? = null,
    speedMs: Long = 25,
    pauseMs: Long = 3000
) {
    // Resolve which messages to display
    val resolvedMessages = remember(message, messages) {
        when {
            messages != null && messages.isNotEmpty() -> messages
            message != null && message.isNotEmpty() -> listOf(message)
            else -> listOf("Welcome! Let's level up your vocabulary today!")
        }
    }

    var currentMsgIdx by remember { mutableStateOf(0) }
    var displayedText by remember { mutableStateOf("") }
    var isTypingOrErasing by remember { mutableStateOf(false) }

    // Typewriter state tracking
    LaunchedEffect(resolvedMessages) {
        currentMsgIdx = 0
        // If there's only 1 message, type it once and keep it.
        if (resolvedMessages.size == 1) {
            val singleMsg = resolvedMessages[0]
            isTypingOrErasing = true
            for (i in 1..singleMsg.length) {
                displayedText = singleMsg.substring(0, i)
                delay(speedMs)
            }
            isTypingOrErasing = false
        } else {
            // Loop rotating messages
            while (true) {
                val currentMsg = resolvedMessages[currentMsgIdx % resolvedMessages.size]
                isTypingOrErasing = true
                for (i in 1..currentMsg.length) {
                    displayedText = currentMsg.substring(0, i)
                    delay(speedMs)
                }
                isTypingOrErasing = false
                
                delay(pauseMs)
                
                isTypingOrErasing = true
                for (i in currentMsg.length downTo 0) {
                    displayedText = currentMsg.substring(0, i)
                    delay(speedMs / 2) // erase faster
                }
                isTypingOrErasing = false
                delay(400) // small gap before next message
                
                currentMsgIdx++
            }
        }
    }

    // Tiki speaks (mouth moves) when typewriter is typing
    val activeTikiState = remember(tikiState, isTypingOrErasing) {
        if (isTypingOrErasing) "st-talking" else tikiState
    }

    // Breathing motion scale
    val infiniteTransition = rememberInfiniteTransition(label = "ticky_breathe")
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ticky_scale"
    )

    SharedGlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        depth = 1
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TikiPlaceholder(
                tikiState = activeTikiState,
                sizeDp = sizeDp,
                modifier = Modifier
                    .size(sizeDp.dp)
                    .scale(breatheScale)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Speech bubble background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x0AFFFFFF))
                        .border(1.dp, Color(0x10FFFFFF), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = displayedText,
                        color = Color(0xFF00FFD2),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 17.sp,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
