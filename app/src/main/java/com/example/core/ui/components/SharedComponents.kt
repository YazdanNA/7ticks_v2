package com.example.core.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * SECTION 2. TRUE GLASSMORPHISM
 * SharedGlassCard implements rich frosted glass effects, dynamic highlights,
 * inner gradients, soft border glowing strokes, and custom depth layers.
 */
@Composable
fun SharedGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    borderWidth: Dp = 1.dp,
    glowColor: Color = Color(0x3DFFFFFF),
    backgroundColor: Color = Color(0x1F7A88FF),
    depth: Int = 1, // 1 to 3 layers for stacked card depth
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Bouncy press animation
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "glass_card_scale"
    )

    // Base transparency gradient
    val cardBrush = Brush.verticalGradient(
        colors = listOf(
            backgroundColor.copy(alpha = (0.16f * depth).coerceAtMost(0.45f)),
            backgroundColor.copy(alpha = (0.04f * depth).coerceAtMost(0.18f))
        )
    )

    // High quality border glow brush
    val borderBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.35f),
            glowColor.copy(alpha = 0.12f),
            Color.White.copy(alpha = 0.04f)
        ),
        start = Offset(0f, 0f),
        end = Offset(200f, 400f)
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = (4 * depth).dp,
                shape = RoundedCornerShape(cornerRadius),
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.3f),
                spotColor = Color.Black.copy(alpha = 0.5f)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(cardBrush)
            .border(
                width = borderWidth,
                brush = borderBrush,
                shape = RoundedCornerShape(cornerRadius)
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = LocalIndication.current
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClick()
                    }
                } else {
                    Modifier
                }
            )
    ) {
        Box(
            modifier = Modifier
                .drawBehind {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.05f), Color.Transparent),
                            center = Offset(size.width / 3f, 0f),
                            radius = size.width / 1.5f
                        )
                    )
                }
                .padding(20.dp)
        ) {
            content()
        }
    }
}

/**
 * SECTION 4. GLOBAL TOUCH FEEDBACK SYSTEM
 * SharedPrimaryButton is a premium glowing button component with spring bouncy
 * touch scale effect, gradient support, custom icons, and built-in haptic integration.
 */
@Composable
fun SharedPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    accentColor: Color = Color(0xFF00C2FF),
    purpleColor: Color = Color(0xFF9D00FF),
    isPulseEnabled: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Pulse animation for CTA buttons
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_trans")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isPulseEnabled && !isPressed) 1.03f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "button_pulse"
    )

    // Interaction scale animation
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else pulseScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "btn_scale"
    )

    // Glowing border color
    val glowGlow by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "btn_glow"
    )

    val gradientBrush = if (enabled) {
        Brush.horizontalGradient(
            colors = listOf(accentColor, purpleColor)
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(Color(0x33FFFFFF), Color(0x11FFFFFF))
        )
    }

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (enabled) 12.dp else 0.dp,
                shape = RoundedCornerShape(20.dp),
                clip = false,
                ambientColor = accentColor.copy(alpha = glowGlow * 0.4f),
                spotColor = purpleColor.copy(alpha = glowGlow * 0.5f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(gradientBrush)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = LocalIndication.current
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = text,
                color = if (enabled) Color.White else Color.White.copy(alpha = 0.4f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * SECTION 12. TEXT FIELD REDESIGN
 * Custom styled text fields with rounded corners, frosted glass background,
 * glow border on focus, and smooth focus indicator animations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    label: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isError: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // Animate indicator colors on focus
    val borderGlowColor by animateColorAsState(
        targetValue = if (isError) {
            Color(0xFFFF1744)
        } else if (isFocused) {
            Color(0xFF00C2FF)
        } else {
            Color(0x22FFFFFF)
        },
        animationSpec = tween(300),
        label = "text_field_border_color"
    )

    val labelColor by animateColorAsState(
        targetValue = if (isError) {
            Color(0xFFFF1744)
        } else if (isFocused) {
            Color(0xFF00FFD2)
        } else {
            Color.White.copy(alpha = 0.5f)
        },
        animationSpec = tween(300),
        label = "text_field_label_color"
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (label != null) {
            Text(
                text = label,
                color = labelColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color.White.copy(alpha = 0.35f),
                    fontSize = 14.sp
                )
            },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            isError = isError,
            interactionSource = interactionSource,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0x1A00C2FF),
                unfocusedContainerColor = Color(0x0CFFFFFF),
                errorContainerColor = Color(0x1AFF1744),
                focusedBorderColor = borderGlowColor,
                unfocusedBorderColor = borderGlowColor,
                errorBorderColor = borderGlowColor,
                cursorColor = Color(0xFF00FFD2),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                errorTextColor = Color.White
            ),
            textStyle = LocalTextStyle.current.copy(
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (isFocused) 8.dp else 0.dp,
                    shape = RoundedCornerShape(20.dp),
                    clip = false,
                    ambientColor = Color(0xFF00C2FF).copy(alpha = 0.15f),
                    spotColor = Color(0xFF9D00FF).copy(alpha = 0.25f)
                )
        )
    }
}

/**
 * SECTION 10. SHARED PROGRESS HEADER
 * SharedProgressHeader displays Streak, Level, and XP in three clean,
 * modern glassmorphic compact cards.
 */
@Composable
fun SharedProgressHeader(
    streak: Int,
    level: Int,
    xp: Int,
    modifier: Modifier = Modifier,
    badgeText: String = "Explorer"
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Card 1: Streak
        SharedGlassCard(
            modifier = Modifier.weight(1f),
            cornerRadius = 18.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Build, // Flame icon placeholder or similar
                    contentDescription = "Streak",
                    tint = Color(0xFFFF7043),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$streak Days",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Day Streak",
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 10.sp
                )
            }
        }

        // Card 2: Level
        SharedGlassCard(
            modifier = Modifier.weight(1.1f),
            cornerRadius = 18.dp,
            backgroundColor = Color(0x3D7A88FF) // Highlight middle card
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Level",
                    tint = Color(0xFFFFD600),
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Level $level",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = badgeText,
                    color = Color(0xFF00FFD2),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Card 3: Experience
        SharedGlassCard(
            modifier = Modifier.weight(1f),
            cornerRadius = 18.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow, // XP/Point representer
                    contentDescription = "XP",
                    tint = Color(0xFF00E676),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$xp XP",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Total Points",
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

/**
 * SECTION 11. SHARED FLASHCARD COMPONENT
 * Implements a card flip animation with 3D rotation, containing proper Persian translations,
 * phonetics, parts of speech, and comprehensive examples.
 */
@Composable
fun SharedFlashcard(
    word: String,
    phonetics: String,
    partOfSpeech: String,
    enDefinition: String,
    faDefinition: String,
    example: String,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "flashcard_rotation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onFlip()
            }
            .graphicsLayer {
                rotationY = rotationAngle
                cameraDistance = 14f * density
            },
        contentAlignment = Alignment.Center
    ) {
        if (rotationAngle <= 90f) {
            // Front Side
            SharedGlassCard(
                modifier = Modifier.fillMaxSize(),
                cornerRadius = 24.dp,
                backgroundColor = Color(0x1F7A88FF)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x1C00C2FF))
                                .border(1.dp, Color(0x3300C2FF), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = partOfSpeech.uppercase(),
                                color = Color(0xFF00FFD2),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = word,
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = phonetics,
                            color = Color.White.copy(alpha = 0.45f),
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = enDefinition,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (example.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x0DFFFFFF))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "\"$example\"",
                                    color = Color(0xFF00FFD2).copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 16.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Text(
                        text = "Tap Card to Reveal Meaning",
                        color = Color(0xFF00C2FF).copy(alpha = 0.75f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // Back Side (mirrored horizontally)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationY = 180f
                    }
            ) {
                SharedGlassCard(
                    modifier = Modifier.fillMaxSize(),
                    cornerRadius = 24.dp,
                    backgroundColor = Color(0x2E7A88FF)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = word,
                                color = Color(0xFF00C2FF),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                            IconButton(onClick = {}) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Speak Pronunciation",
                                    tint = Color(0xFF00FFD2)
                                )
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Persian Translation
                            Text(
                                text = faDefinition,
                                color = Color(0xFFFFD600),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Definition
                            Text(
                                text = enDefinition,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                        }

                        Text(
                            text = "Tap Card to Show Front",
                            color = Color(0xFF9D00FF).copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * SECTION 3. ANIMATED BACKGROUND SYSTEM
 * Battery-friendly, luxurious multi-layered moving stars and dynamic glow lines background.
 * Optimized with exact coordinates and simple time-shifting mathematics.
 */
@Composable
fun AnimatedBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora_trans")

    // Slow moving gradient shift
    val bgShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_shift"
    )

    // Slow pulse for particles
    val particleGlow by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "part_glow"
    )

    // Solid dark-space space background + soft canvas drawing
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF060713))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Layer 1: Slow rotating aurora lights
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x1F00C2FF), Color.Transparent),
                    radius = width * 0.8f
                ),
                center = Offset(
                    x = width * 0.2f + (bgShift * 1.5f),
                    y = height * 0.3f - (bgShift * 0.8f)
                )
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x149D00FF), Color.Transparent),
                    radius = width * 0.9f
                ),
                center = Offset(
                    x = width * 0.8f - (bgShift * 1.2f),
                    y = height * 0.7f + (bgShift * 1.1f)
                )
            )

            // Layer 2: Battery-friendly dynamic floating particle stars
            val stars = listOf(
                Offset(width * 0.15f, height * 0.15f),
                Offset(width * 0.85f, height * 0.22f),
                Offset(width * 0.35f, height * 0.55f),
                Offset(width * 0.72f, height * 0.48f),
                Offset(width * 0.22f, height * 0.82f),
                Offset(width * 0.88f, height * 0.78f),
                Offset(width * 0.50f, height * 0.18f),
                Offset(width * 0.60f, height * 0.88f)
            )

            stars.forEachIndexed { i, pos ->
                val sizeVal = (3f + (i % 3) * 2f).dp.toPx()
                val opacity = ((particleGlow + (i * 0.1f)) % 0.8f).coerceAtLeast(0.1f)
                
                // Draw elegant glowing star points
                drawCircle(
                    color = Color(0xFF00FFD2).copy(alpha = opacity),
                    radius = sizeVal,
                    center = Offset(
                        x = pos.x + (bgShift * 0.2f * if (i % 2 == 0) 1 else -1),
                        y = pos.y + (bgShift * 0.15f * if (i % 3 == 0) 1 else -1)
                    )
                )
            }
        }

        // Overlay slot
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

/**
 * SECTION 7. TYPOGRAPHY ANIMATIONS
 * Dynamic typewriter text compositor with typing speed, and stationary mascot support.
 */
@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    speedMs: Long = 40,
    color: Color = Color.White,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    textAlign: TextAlign = TextAlign.Start,
    lineHeight: androidx.compose.ui.unit.TextUnit = 18.sp
) {
    var visibleText by remember { mutableStateOf("") }

    LaunchedEffect(text) {
        // Reverse-typewriter to clear previous message if any, then type new one
        if (visibleText.isNotEmpty()) {
            for (i in visibleText.length downTo 0) {
                visibleText = visibleText.substring(0, i)
                delay(12)
            }
        }
        
        // Type in
        for (i in 1..text.length) {
            visibleText = text.substring(0, i)
            delay(speedMs)
        }
    }

    Text(
        text = visibleText,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        textAlign = textAlign,
        lineHeight = lineHeight,
        modifier = modifier
    )
}
