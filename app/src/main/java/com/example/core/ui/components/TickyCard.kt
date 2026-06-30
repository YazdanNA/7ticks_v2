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
import com.example.features.tiki.renderer.EmotionRenderer
import com.example.ui.theme.LocalAppLanguage
import com.example.core.localization.Translations
import kotlinx.coroutines.delay

private fun splitMessageIntoChunks(message: String, maxChars: Int = 60): List<String> {
    if (message.length <= maxChars) return listOf(message)
    val words = message.split(" ")
    val chunks = mutableListOf<String>()
    var currentChunk = StringBuilder()
    for (word in words) {
        if (currentChunk.isEmpty()) {
            currentChunk.append(word)
        } else if (currentChunk.length + 1 + word.length <= maxChars) {
            currentChunk.append(" ").append(word)
        } else {
            chunks.add(currentChunk.toString())
            currentChunk = StringBuilder(word)
        }
    }
    if (currentChunk.isNotEmpty()) {
        chunks.add(currentChunk.toString())
    }
    return chunks
}

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
    tikiState: String? = null,
    sizeDp: Int = 80,
    message: String? = null,
    messages: List<String>? = null,
    speedMs: Long = 25,
    pauseMs: Long = 3000
) {
    val globalState by com.example.features.tiki.api.TikiController.getInstance().state.collectAsState()
    val currentLang = LocalAppLanguage.current
    val isRtl = currentLang == "fa"

    val resolvedTikiState = tikiState ?: globalState.tikiStateKey
    val baseMessage = message ?: if (messages == null || messages.isEmpty()) globalState.dialogue else null

    // Resolve which messages to display, and split long ones to keep speech bubble max 2 lines
    val resolvedMessages = remember(baseMessage, messages, currentLang) {
        val rawList = when {
            messages != null && messages.isNotEmpty() -> messages.map { Translations.translate(it, currentLang) }
            baseMessage != null && baseMessage.isNotEmpty() -> listOf(Translations.translate(baseMessage, currentLang))
            else -> listOf(Translations.translate("Welcome! Let's level up your vocabulary today!", currentLang))
        }
        rawList.flatMap { splitMessageIntoChunks(it, 60) }
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
    val isSpeaking = isTypingOrErasing

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

    // Enforce 135dp for 4x larger visual appearance
    val resolvedSizeDp = if (sizeDp < 135) 135 else sizeDp

    // Layout mirroring configs
    val rowPaddingStart = if (isRtl) 12.dp else 114.dp
    val rowPaddingEnd = if (isRtl) 114.dp else 12.dp
    val tikiAlignment = if (isRtl) Alignment.CenterEnd else Alignment.CenterStart
    val tikiOffsetX = if (isRtl) 4.dp else (-4).dp
    val speechBubbleAlignment = if (isRtl) Alignment.CenterEnd else Alignment.CenterStart
    val textAlignment = if (isRtl) androidx.compose.ui.text.style.TextAlign.Right else androidx.compose.ui.text.style.TextAlign.Left

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp), // STRICT FIXED HEIGHT FOR COMPANION CARD
        contentAlignment = Alignment.CenterStart
    ) {
        // 1. Glass Card background & layout (containing the speech bubble, padded dynamically to leave space for Tiki)
        SharedGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            cornerRadius = 24.dp,
            depth = 1
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(start = rowPaddingStart, end = rowPaddingEnd, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center
                ) {
                    // Speech bubble background - strict fixed height, max 2 lines
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x0AFFFFFF))
                            .border(1.dp, Color(0x10FFFFFF), RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = speechBubbleAlignment
                    ) {
                        Text(
                            text = displayedText,
                            color = Color(0xFF00FFD2),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 17.sp,
                            maxLines = 2,
                            textAlign = textAlignment,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // 2. Large Tiki drawn on top of the card as a stack, sticking out of boundaries dynamically positioned
        Box(
            modifier = Modifier
                .requiredSize(resolvedSizeDp.dp)
                .align(tikiAlignment)
                .offset(x = tikiOffsetX, y = (-20).dp) // letting Tiki stick out of the card organically
                .scale(breatheScale),
            contentAlignment = Alignment.Center
        ) {
            EmotionRenderer(
                tikiStateOverride = resolvedTikiState,
                isSpeaking = isSpeaking,
                sizeDp = resolvedSizeDp,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
