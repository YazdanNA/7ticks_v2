package com.example.core.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.sin

@Composable
fun TikiPlaceholder(
    modifier: Modifier = Modifier,
    sizeDp: Int = 140,
    message: String = "",
    tikiState: String = "st-happy"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ticky_mascot")

    // 1. General hovering (float3D)
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ticky_float"
    )

    // 2. Wing flapping animation
    val wingFlapAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ticky_wing"
    )

    // 3. Eye blinking trigger (scale Y)
    val blinkAnim by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000
                1.0f at 0
                1.0f at 3800
                0.05f at 3900
                1.0f at 4000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "ticky_blink"
    )

    // 4. Talking beak animation
    val talkBeakAnim by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(150, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ticky_talk"
    )

    // 5. Crying tear drop animation
    val tearDropAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "ticky_tear"
    )

    // 6. Alarm bell ringing (rotation)
    val bellRingAnim by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(150, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ticky_bell"
    )

    // 7. Loading spin rotation
    val loadSpinAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ticky_loading"
    )

    // 8. Furious shake animation
    val shakeAnim by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ticky_shake"
    )

    // 9. ROFL roll animation
    val roflRollAnim by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ticky_rofl_roll"
    )

    // 10. Cinema peek animations
    val peekOffsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 14000
                0f at 0
                115f at 2100
                0f at 4200
                -115f at 6300
                0f at 8400
                0f at 10500
                0f at 11900
                0f at 14000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "ticky_peek_x"
    )

    val peekOffsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 14000
                0f at 0
                105f at 2100
                0f at 4200
                105f at 6300
                0f at 8400
                -150f at 10500
                130f at 11900
                0f at 14000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "ticky_peek_y"
    )

    val peekRotate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 14000
                0f at 0
                -22f at 2100
                0f at 4200
                22f at 6300
                0f at 8400
                180f at 10500
                0f at 11900
                0f at 14000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "ticky_peek_rotate"
    )

    val peekScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 14000
                1f at 0
                1.1f at 2100
                1f at 4200
                1.1f at 6300
                1f at 8400
                1.1f at 10500
                1.1f at 11900
                1f at 14000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "ticky_peek_scale"
    )

    // 11. Drifting animations for hearts, kiss, Zs
    val driftProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ticky_drift"
    )

    // 12. Angry-red pulsing opacity
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ticky_pulse"
    )

    // Parse sub-states
    val (face, body, gadget) = when (tikiState) {
        "st-happy" -> Triple("happy", "normal", "none")
        "st-laugh-tears" -> Triple("laugh-tears", "normal", "none")
        "st-rofl" -> Triple("rofl", "rofl-roll", "none")
        "st-laugh-big" -> Triple("laugh-big", "normal", "none")
        "st-smile-big" -> Triple("smile-big", "normal", "none")
        "st-smile-simple" -> Triple("smile-simple", "normal", "none")
        "st-smile-shy" -> Triple("smile-shy", "normal", "none")
        "st-heart-eyes" -> Triple("heart-eyes", "normal", "none")
        "st-smile-hearts" -> Triple("smile-hearts", "normal", "none")
        "st-wink" -> Triple("wink", "normal", "none")
        "st-kiss" -> Triple("kiss", "normal", "none")
        "st-tears-of-joy" -> Triple("tears-of-joy", "normal", "none")

        "st-pleading" -> Triple("pleading", "sad", "none")
        "st-sad" -> Triple("sad", "sad", "none")
        "st-cry" -> Triple("cry", "sad", "none")
        "st-disappointed" -> Triple("disappointed", "sad", "none")
        "st-sad-simple" -> Triple("sad-simple", "sad", "none")

        "st-angry" -> Triple("angry", "normal", "none")
        "st-angry-red" -> Triple("angry-red", "furious", "none")
        "st-frown" -> Triple("frown", "normal", "none")
        "st-cursing" -> Triple("cursing", "furious", "none")

        "st-scream" -> Triple("scream", "furious", "none")
        "st-astonished" -> Triple("astonished", "normal", "none")
        "st-mouth-open" -> Triple("mouth-open", "normal", "none")
        "st-flushed" -> Triple("flushed", "normal", "none")

        "st-thinking" -> Triple("thinking", "normal", "none")
        "st-roll-eyes" -> Triple("roll-eyes", "normal", "none")
        "st-smirk" -> Triple("smirk", "normal", "none")
        "st-poker" -> Triple("poker", "normal", "none")
        "st-eyebrow-raise" -> Triple("eyebrow-raise", "normal", "none")

        "st-sweat-smile" -> Triple("sweat-smile", "normal", "none")
        "st-sweat-cold" -> Triple("sweat-cold", "normal", "none")
        "st-yawn" -> Triple("yawn", "normal", "none")
        "st-sleep" -> Triple("sleep", "normal", "none")
        "st-zipped" -> Triple("zipped", "normal", "none")
        "st-dizzy" -> Triple("dizzy", "normal", "none")
        "st-talking" -> Triple("talk", "normal", "none")

        "st-welcome" -> Triple("happy", "wave", "stars")
        "st-name" -> Triple("happy", "normal", "pencil")
        "st-native-lang" -> Triple("happy", "normal", "globe-native")
        "st-target-lang" -> Triple("happy", "normal", "globe-target")
        "st-study-time" -> Triple("happy", "normal", "timer")
        "st-remind-time" -> Triple("happy", "normal", "bell")
        "st-placement" -> Triple("happy", "normal", "placement")
        "st-loading-data" -> Triple("hidden", "normal", "loading")
        "st-streak-fire" -> Triple("happy", "large", "fire")
        "st-locked-level" -> Triple("locked", "locked", "lock")
        "st-header-peek" -> Triple("happy", "peek", "none")
        "st-collection-search" -> Triple("search", "normal", "search-data")
        "st-rap" -> Triple("rap", "normal", "none")
        else -> Triple("happy", "normal", "none") // default st-happy
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.requiredSize(sizeDp.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Establish virtual coordinate mapping (0 to 400)
                val scaleX = w / 400f
                val scaleY = h / 400f

                withTransform({
                    scale(scaleX, scaleY, pivot = Offset.Zero)
                }) {
                    // 1. Shadow Floor (if not peeking)
                    if (body != "peek") {
                        val shadowOpacity = if (body == "sad") 0.4f else 0.6f
                        val shadowScale = if (body == "large") 1.1f else if (body == "locked") 0.9f else 1.0f
                        drawOval(
                            color = Color(0x00000000).copy(alpha = shadowOpacity),
                            topLeft = Offset(200f - 70f * shadowScale, 330f - 12f),
                            size = Size(140f * shadowScale, 24f),
                            alpha = shadowOpacity
                        )
                    }

                    // 2. Neon Glow Fire Aura behind body
                    if (gadget == "fire") {
                        // Drawing flame background
                        val firePath1 = Path().apply {
                            moveTo(140f, 250f)
                            cubicTo(110f, 160f, 160f, 110f, 170f, 80f)
                            cubicTo(180f, 140f, 200f, 100f, 210f, 50f)
                            cubicTo(220f, 110f, 250f, 130f, 260f, 250f)
                            close()
                        }
                        drawPath(firePath1, color = Color(0xFFEF4444).copy(alpha = 0.8f))

                        val firePath2 = Path().apply {
                            moveTo(165f, 250f)
                            cubicTo(150f, 180f, 180f, 150f, 190f, 110f)
                            cubicTo(195f, 140f, 210f, 130f, 220f, 90f)
                            cubicTo(225f, 140f, 235f, 170f, 235f, 250f)
                            close()
                        }
                        drawPath(firePath2, color = Color(0xFFFBBF24).copy(alpha = 0.7f))
                    }

                    // 3. Ambient Loading Ring around body
                    if (gadget == "loading") {
                        withTransform({
                            rotate(loadSpinAnim, pivot = Offset(200f, 195f))
                        }) {
                            drawCircle(
                                color = Color(0xFF38BDF8),
                                radius = 95f,
                                center = Offset(200f, 195f),
                                style = Stroke(width = 3f, miter = 4f)
                            )
                            drawCircle(
                                color = Color(0xFF34D399),
                                radius = 6f,
                                center = Offset(200f, 100f)
                            )
                        }
                    }

                    // 4. Floating body core group
                    val bodyOffsetY = if (body == "peek") 0f else floatAnim
                    val bodyScale = if (body == "large") 1.06f else if (body == "locked") 0.9f else 1.0f
                    val bodyRotate = if (body == "sad") -4f else 0f

                    withTransform({
                        translate(0f, bodyOffsetY)
                        scale(bodyScale, bodyScale, pivot = Offset(200f, 195f))
                        rotate(bodyRotate, pivot = Offset(200f, 195f))
                    }) {
                        // Wing Left
                        val wingLRotation = if (body == "wave") -35f else if (body == "sad") 16f else if (body == "locked") 20f else -wingFlapAnim
                        withTransform({
                            rotate(wingLRotation, pivot = Offset(165f, 190f))
                        }) {
                            val wingL = Path().apply {
                                moveTo(160f, 185f)
                                cubicTo(95f, 150f, 95f, 230f, 165f, 230f)
                                cubicTo(140f, 215f, 140f, 195f, 160f, 185f)
                                close()
                            }
                            drawPath(wingL, color = Color(0xFF2563EB))
                        }

                        // Wing Right
                        val wingRRotation = if (body == "sad") -16f else if (body == "locked") -20f else wingFlapAnim
                        withTransform({
                            rotate(wingRRotation, pivot = Offset(235f, 190f))
                        }) {
                            val wingR = Path().apply {
                                moveTo(240f, 185f)
                                cubicTo(305f, 150f, 305f, 230f, 235f, 230f)
                                cubicTo(260f, 215f, 260f, 195f, 240f, 185f)
                                close()
                            }
                            drawPath(wingR, color = Color(0xFF2563EB))
                        }

                        // Head tuft
                        val headTuft = Path().apply {
                            moveTo(195f, 137f)
                            cubicTo(190f, 120f, 185f, 110f, 195f, 125f)
                            cubicTo(200f, 115f, 205f, 115f, 205f, 125f)
                            cubicTo(215f, 110f, 210f, 120f, 205f, 137f)
                            close()
                        }
                        drawPath(headTuft, color = Color(0xFF38BDF8))

                        // Body core radial circle
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF7DD3FC), Color(0xFF0EA5E9), Color(0xFF4338CA), Color(0xFF312E81)),
                                center = Offset(140f, 120f),
                                radius = 260f
                            ),
                            radius = 62f,
                            center = Offset(200f, 195f)
                        )

                        if (face == "angry-red") {
                            drawCircle(
                                color = Color(0xFFEF4444).copy(alpha = pulseAnim),
                                radius = 62f,
                                center = Offset(200f, 195f)
                            )
                        }

                        // Belly glow
                        drawCircle(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xCCE0F2FE), Color(0x1A38BDF8)),
                                start = Offset(200f, 174f),
                                end = Offset(200f, 258f)
                            ),
                            radius = 42f,
                            center = Offset(200f, 216f)
                        )

                        // Highlight cheek gloss
                        withTransform({
                            rotate(-15f, pivot = Offset(190f, 155f))
                        }) {
                            drawOval(
                                color = Color(0x26FFFFFF),
                                topLeft = Offset(190f - 25f, 155f - 15f),
                                size = Size(50f, 30f)
                            )
                        }

                        // FACE DRAWING LAYERS
                        // Local helpers for face drawings
                        fun drawHeartLocal(x: Float, y: Float, scale: Float, alpha: Float, color: Color) {
                            val heartPath = Path().apply {
                                moveTo(175f, 190f)
                                cubicTo(150f, 170f, 155f, 155f, 168f, 155f)
                                cubicTo(173f, 155f, 175f, 160f, 175f, 160f)
                                cubicTo(175f, 160f, 177f, 155f, 182f, 155f)
                                cubicTo(195f, 155f, 200f, 170f, 175f, 190f)
                                close()
                            }
                            withTransform({
                                translate(x - 175f, y - 160f)
                                scale(scale, scale, pivot = Offset(175f, 160f))
                            }) {
                                drawPath(heartPath, color = color, alpha = alpha)
                            }
                        }

                        fun createZPathLocal(x: Float, y: Float, size: Float): Path {
                            return Path().apply {
                                moveTo(x, y)
                                lineTo(x + size, y)
                                lineTo(x, y + size)
                                lineTo(x + size, y + size)
                            }
                        }

                        fun getPhaseProgressLocal(progress: Float, delay: Float): Float {
                            val raw = progress + delay
                            return if (raw > 1f) raw - 1f else raw
                        }

                        if (face == "happy") {
                            // Left Eye
                            withTransform({
                                scale(1f, blinkAnim, pivot = Offset(175f, 175f))
                            }) {
                                drawCircle(color = Color(0xFF0F172A), radius = 14f, center = Offset(175f, 175f))
                                drawCircle(color = Color(0xFFFFFFFF), radius = 4.5f, center = Offset(172f, 170f))
                                drawCircle(color = Color(0xFFFFFFFF), radius = 1.5f, center = Offset(180f, 178f))
                            }
                            // Right Eye
                            withTransform({
                                scale(1f, blinkAnim, pivot = Offset(225f, 175f))
                            }) {
                                drawCircle(color = Color(0xFF0F172A), radius = 14f, center = Offset(225f, 175f))
                                drawCircle(color = Color(0xFFFFFFFF), radius = 4.5f, center = Offset(222f, 170f))
                                drawCircle(color = Color(0xFFFFFFFF), radius = 1.5f, center = Offset(230f, 178f))
                            }
                            // Open Beak Mouth
                            val beakInsidePath = Path().apply {
                                moveTo(184f, 192f)
                                cubicTo(184f, 220f, 216f, 220f, 216f, 192f)
                                close()
                            }
                            drawPath(
                                beakInsidePath,
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFEF4444), Color(0xFF9F1239)),
                                    start = Offset(200f, 192f),
                                    end = Offset(200f, 220f)
                                )
                            )
                            // Tongue
                            val tonguePath = Path().apply {
                                moveTo(192f, 209f)
                                quadraticTo(200f, 201f, 208f, 209f)
                                close()
                            }
                            drawPath(tonguePath, color = Color(0xFFFDA4AF))
                            // Beak top outer
                            val beakTopPath = Path().apply {
                                moveTo(184f, 192f)
                                quadraticTo(200f, 181f, 216f, 192f)
                                quadraticTo(200f, 199f, 184f, 192f)
                                close()
                            }
                            drawPath(
                                beakTopPath,
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFFDE047), Color(0xFFF59E0B), Color(0xFFB45309)),
                                    start = Offset(200f, 181f),
                                    end = Offset(200f, 199f)
                                )
                            )
                        } else if (face == "talk") {
                            // Left Eye
                            drawCircle(color = Color(0xFF0F172A), radius = 14f, center = Offset(175f, 175f))
                            drawCircle(color = Color(0xFFFFFFFF), radius = 4.5f, center = Offset(172f, 170f))
                            // Right Eye
                            drawCircle(color = Color(0xFF0F172A), radius = 14f, center = Offset(225f, 175f))
                            drawCircle(color = Color(0xFFFFFFFF), radius = 4.5f, center = Offset(222f, 170f))

                            // Lower Beak moving scale
                            withTransform({
                                translate(0f, (1f - talkBeakAnim) * 8f)
                                scale(1f, talkBeakAnim, pivot = Offset(200f, 192f))
                            }) {
                                val talkBeakLower = Path().apply {
                                    moveTo(184f, 192f)
                                    lineTo(216f, 192f)
                                    lineTo(200f, 208f)
                                    close()
                                }
                                drawPath(
                                    talkBeakLower,
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFFFDE047), Color(0xFFF59E0B), Color(0xFFB45309)),
                                        start = Offset(200f, 192f),
                                        end = Offset(200f, 208f)
                                    )
                                )
                            }
                            // Beak top
                            val beakTop = Path().apply {
                                moveTo(184f, 192f)
                                quadraticTo(200f, 184f, 216f, 192f)
                                close()
                            }
                            drawPath(
                                beakTop,
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFFDE047), Color(0xFFF59E0B), Color(0xFFB45309)),
                                    start = Offset(200f, 184f),
                                    end = Offset(200f, 192f)
                                )
                            )
                        } else if (face == "sad") {
                            // Closed/Crying Eyes
                            val leftSadEye = Path().apply {
                                moveTo(161f, 178f)
                                quadraticTo(175f, 165f, 189f, 178f)
                                quadraticTo(175f, 190f, 161f, 178f)
                                close()
                            }
                            drawPath(leftSadEye, color = Color(0xFF0F172A))
                            val rightSadEye = Path().apply {
                                moveTo(211f, 178f)
                                quadraticTo(225f, 165f, 239f, 178f)
                                quadraticTo(225f, 190f, 211f, 178f)
                                close()
                            }
                            drawPath(rightSadEye, color = Color(0xFF0F172A))

                            drawCircle(color = Color.White, radius = 4f, center = Offset(175f, 180f))
                            drawCircle(color = Color.White, radius = 4f, center = Offset(225f, 180f))

                            // Downturned sad beak
                            val sadBeak = Path().apply {
                                moveTo(184f, 195f)
                                quadraticTo(200f, 176f, 216f, 195f)
                                quadraticTo(200f, 207f, 184f, 195f)
                                close()
                            }
                            drawPath(
                                sadBeak,
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFFDE047), Color(0xFFF59E0B), Color(0xFFB45309)),
                                    start = Offset(200f, 176f),
                                    end = Offset(200f, 207f)
                                )
                            )

                            // Falling tear drop
                            drawCircle(
                                color = Color(0xFF7DD3FC),
                                radius = 4f,
                                center = Offset(175f, 186f + tearDropAnim)
                            )
                        } else if (face == "poker") {
                            // Poker straight eyebrows
                            drawLine(color = Color(0xFF0F172A), start = Offset(160f, 175f), end = Offset(190f, 175f), strokeWidth = 4f)
                            drawLine(color = Color(0xFF0F172A), start = Offset(210f, 175f), end = Offset(240f, 175f), strokeWidth = 4f)
                            // Beak
                            val pokerBeak = Path().apply {
                                moveTo(184f, 192f)
                                quadraticTo(200f, 187f, 216f, 192f)
                                lineTo(200f, 195f)
                                close()
                            }
                            drawPath(pokerBeak, color = Color(0xFFF59E0B))
                        } else if (face == "locked") {
                            // Locked eyes
                            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(175f, 178f))
                            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(225f, 178f))
                            drawCircle(color = Color(0xFF38BDF8), radius = 4f, center = Offset(177f, 180f))
                            drawCircle(color = Color(0xFF38BDF8), radius = 4f, center = Offset(223f, 180f))
                            // Beak
                            val lockedBeak = Path().apply {
                                moveTo(188f, 193f)
                                quadraticTo(200f, 188f, 212f, 193f)
                                quadraticTo(200f, 198f, 188f, 193f)
                                close()
                            }
                            drawPath(lockedBeak, color = Color(0xFFF59E0B))
                        } else if (face == "search") {
                            // Search eyes
                            drawCircle(color = Color(0xFF0F172A), radius = 14f, center = Offset(175f, 175f))
                            drawCircle(color = Color(0xFF0F172A), radius = 14f, center = Offset(225f, 175f))
                            // Beak
                            val searchBeak = Path().apply {
                                moveTo(184f, 192f)
                                quadraticTo(200f, 187f, 216f, 192f)
                                lineTo(200f, 195f)
                                close()
                            }
                            drawPath(searchBeak, color = Color(0xFFF59E0B))

                            // Monocle magnifier on right eye
                            drawCircle(
                                color = Color(0xFF34D399),
                                radius = 18f,
                                center = Offset(175f, 175f),
                                style = Stroke(width = 3f)
                            )
                            drawLine(
                                color = Color(0xFF34D399),
                                start = Offset(160f, 185f),
                                end = Offset(145f, 200f),
                                strokeWidth = 3f
                            )
                        } else if (face == "hidden") {
                            // Cyberpunk visor glasses
                            val glassesPath = Path().apply {
                                moveTo(150f, 172f)
                                quadraticTo(200f, 160f, 250f, 172f)
                                quadraticTo(255f, 188f, 245f, 190f)
                                quadraticTo(200f, 180f, 155f, 190f)
                                close()
                            }
                            drawPath(glassesPath, color = Color(0xFF0F172A))
                            drawPath(glassesPath, color = Color(0xFF38BDF8), style = Stroke(width = 2.5f))
                            drawLine(color = Color(0xFF38BDF8), start = Offset(162f, 180f), end = Offset(185f, 180f), strokeWidth = 2f)
                            drawLine(color = Color(0xFF38BDF8), start = Offset(215f, 180f), end = Offset(238f, 180f), strokeWidth = 2f)
                        } else if (face == "rap") {
                            // High-fidelity hip-hop gold shades
                            val leftShade = Path().apply {
                                moveTo(152f, 172f)
                                lineTo(190f, 164f)
                                lineTo(194f, 182f)
                                lineTo(174f, 190f)
                                lineTo(152f, 182f)
                                close()
                            }
                            val rightShade = Path().apply {
                                moveTo(210f, 164f)
                                lineTo(248f, 172f)
                                lineTo(248f, 182f)
                                lineTo(226f, 190f)
                                lineTo(206f, 182f)
                                close()
                            }
                            drawPath(leftShade, brush = Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B))))
                            drawPath(rightShade, brush = Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B))))

                            // Shiny gold borders
                            drawPath(leftShade, color = Color(0xFFFFD600), style = Stroke(width = 2.5f))
                            drawPath(rightShade, color = Color(0xFFFFD600), style = Stroke(width = 2.5f))

                            // Bridge
                            drawLine(color = Color(0xFFFFD600), start = Offset(190f, 173f), end = Offset(210f, 173f), strokeWidth = 3.5f)

                            // Highlights
                            drawLine(color = Color.White.copy(alpha = 0.6f), start = Offset(158f, 174f), end = Offset(170f, 184f), strokeWidth = 2f)
                            drawLine(color = Color.White.copy(alpha = 0.6f), start = Offset(214f, 174f), end = Offset(226f, 184f), strokeWidth = 2f)

                            // Open swagger beak mouth
                            val beakInsidePath = Path().apply {
                                moveTo(184f, 192f)
                                cubicTo(184f, 220f, 216f, 220f, 216f, 192f)
                                close()
                            }
                            drawPath(
                                beakInsidePath,
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFEF4444), Color(0xFF9F1239)),
                                    start = Offset(200f, 192f),
                                    end = Offset(200f, 220f)
                                )
                            )
                            // Beak top outer
                            val beakTopPath = Path().apply {
                                moveTo(184f, 192f)
                                quadraticTo(200f, 181f, 216f, 192f)
                                quadraticTo(200f, 199f, 184f, 192f)
                                close()
                            }
                            drawPath(
                                beakTopPath,
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFFDE047), Color(0xFFF59E0B), Color(0xFFB45309)),
                                    start = Offset(200f, 181f),
                                    end = Offset(200f, 199f)
                                )
                            )
                        } else if (face == "laugh-tears") {
                            // Left Eye Squint
                            drawArc(Color(0xFF0F172A), startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(160f, 165f), size = Size(30f, 20f), style = Stroke(width = 5f, cap = StrokeCap.Round))
                            // Right Eye Squint
                            drawArc(Color(0xFF0F172A), startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(210f, 165f), size = Size(30f, 20f), style = Stroke(width = 5f, cap = StrokeCap.Round))
                            // Beak Inside
                            val bInside = Path().apply {
                                moveTo(182f, 190f)
                                cubicTo(182f, 225f, 218f, 225f, 218f, 190f)
                                close()
                            }
                            drawPath(bInside, brush = Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFF9F1239)), start = Offset(200f, 190f), end = Offset(200f, 225f)))
                            // Beak Top
                            val bTop = Path().apply {
                                moveTo(182f, 190f)
                                quadraticTo(200f, 175f, 218f, 190f)
                                quadraticTo(200f, 205f, 182f, 190f)
                                close()
                            }
                            drawPath(bTop, brush = Brush.linearGradient(listOf(Color(0xFFFDE047), Color(0xFFF59E0B), Color(0xFFB45309)), start = Offset(200f, 175f), end = Offset(200f, 205f)))
                            // Tear drops left & right
                            drawCircle(Color(0xFF7DD3FC), radius = 5f * pulseAnim, center = Offset(150f, 180f))
                            drawCircle(Color(0xFF7DD3FC), radius = 5f * pulseAnim, center = Offset(250f, 180f))
                        } else if (face == "rofl") {
                            // Left Eye slanted cross
                            drawLine(Color(0xFF0F172A), start = Offset(165f, 168f), end = Offset(180f, 175f), strokeWidth = 5f, cap = StrokeCap.Round)
                            drawLine(Color(0xFF0F172A), start = Offset(180f, 175f), end = Offset(165f, 182f), strokeWidth = 5f, cap = StrokeCap.Round)
                            // Right Eye slanted cross
                            drawLine(Color(0xFF0F172A), start = Offset(235f, 168f), end = Offset(220f, 175f), strokeWidth = 5f, cap = StrokeCap.Round)
                            drawLine(Color(0xFF0F172A), start = Offset(220f, 175f), end = Offset(235f, 182f), strokeWidth = 5f, cap = StrokeCap.Round)
                            // Beak Inside
                            val bInside = Path().apply {
                                moveTo(180f, 188f)
                                cubicTo(180f, 230f, 220f, 230f, 220f, 188f)
                                close()
                            }
                            drawPath(bInside, brush = Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFF9F1239)), start = Offset(200f, 188f), end = Offset(200f, 230f)))
                            // Beak Top
                            val bTop = Path().apply {
                                moveTo(180f, 188f)
                                quadraticTo(200f, 175f, 220f, 188f)
                                quadraticTo(200f, 200f, 180f, 188f)
                                close()
                            }
                            drawPath(bTop, brush = Brush.linearGradient(listOf(Color(0xFFFDE047), Color(0xFFF59E0B), Color(0xFFB45309)), start = Offset(200f, 175f), end = Offset(200f, 200f)))
                        } else if (face == "laugh-big") {
                            // Eyes
                            drawArc(Color(0xFF0F172A), startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(160f, 165f), size = Size(30f, 20f), style = Stroke(width = 5f, cap = StrokeCap.Round))
                            drawArc(Color(0xFF0F172A), startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(210f, 165f), size = Size(30f, 20f), style = Stroke(width = 5f, cap = StrokeCap.Round))
                            // Beak
                            val bInside = Path().apply {
                                moveTo(182f, 190f)
                                cubicTo(182f, 225f, 218f, 225f, 218f, 190f)
                                close()
                            }
                            drawPath(bInside, brush = Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFF9F1239)), start = Offset(200f, 190f), end = Offset(200f, 225f)))
                            val bTop = Path().apply {
                                moveTo(182f, 190f)
                                quadraticTo(200f, 175f, 218f, 190f)
                                quadraticTo(200f, 205f, 182f, 190f)
                                close()
                            }
                            drawPath(bTop, brush = Brush.linearGradient(listOf(Color(0xFFFDE047), Color(0xFFF59E0B), Color(0xFFB45309)), start = Offset(200f, 175f), end = Offset(200f, 205f)))
                        } else if (face == "smile-big") {
                            drawCircle(color = Color(0xFF0F172A), radius = 14f, center = Offset(175f, 175f))
                            drawCircle(color = Color.White, radius = 4.5f, center = Offset(172f, 170f))
                            drawCircle(color = Color(0xFF0F172A), radius = 14f, center = Offset(225f, 175f))
                            drawCircle(color = Color.White, radius = 4.5f, center = Offset(222f, 170f))
                            val bInside = Path().apply {
                                moveTo(182f, 190f)
                                cubicTo(182f, 220f, 218f, 220f, 218f, 190f)
                                close()
                            }
                            drawPath(bInside, brush = Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFF9F1239)), start = Offset(200f, 190f), end = Offset(200f, 220f)))
                            val bTop = Path().apply {
                                moveTo(182f, 190f)
                                quadraticTo(200f, 180f, 218f, 190f)
                                quadraticTo(200f, 200f, 182f, 190f)
                                close()
                            }
                            drawPath(bTop, brush = Brush.linearGradient(listOf(Color(0xFFFDE047), Color(0xFFF59E0B), Color(0xFFB45309)), start = Offset(200f, 180f), end = Offset(200f, 200f)))
                        } else if (face == "smile-simple") {
                            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(175f, 175f))
                            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(225f, 175f))
                            val bTop = Path().apply {
                                moveTo(184f, 192f)
                                quadraticTo(200f, 205f, 216f, 192f)
                                close()
                            }
                            drawPath(bTop, brush = Brush.linearGradient(listOf(Color(0xFFFDE047), Color(0xFFB45309)), start = Offset(200f, 192f), end = Offset(200f, 205f)))
                        } else if (face == "smile-shy") {
                            drawArc(Color(0xFF0F172A), startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(160f, 165f), size = Size(30f, 20f), style = Stroke(width = 5f, cap = StrokeCap.Round))
                            drawArc(Color(0xFF0F172A), startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(210f, 165f), size = Size(30f, 20f), style = Stroke(width = 5f, cap = StrokeCap.Round))
                            drawCircle(Color(0xFFF43F5E).copy(alpha = 0.4f), radius = 12f, center = Offset(160f, 185f))
                            drawCircle(Color(0xFFF43F5E).copy(alpha = 0.4f), radius = 12f, center = Offset(240f, 185f))
                            val bTop = Path().apply {
                                moveTo(184f, 192f)
                                quadraticTo(200f, 200f, 216f, 192f)
                                quadraticTo(200f, 195f, 184f, 192f)
                                close()
                            }
                            drawPath(bTop, brush = Brush.linearGradient(listOf(Color(0xFFFDE047), Color(0xFFB45309)), start = Offset(200f, 192f), end = Offset(200f, 200f)))
                        } else if (face == "heart-eyes") {
                            drawHeartLocal(x = 175f, y = 170f, scale = 1.1f, alpha = 1f, color = Color(0xFFF43F5E))
                            drawHeartLocal(x = 225f, y = 170f, scale = 1.1f, alpha = 1f, color = Color(0xFFF43F5E))
                            val bInside = Path().apply {
                                moveTo(184f, 192f)
                                cubicTo(184f, 215f, 216f, 215f, 216f, 192f)
                                close()
                            }
                            drawPath(bInside, brush = Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFF9F1239)), start = Offset(200f, 192f), end = Offset(200f, 215f)))
                            val bTop = Path().apply {
                                moveTo(184f, 192f)
                                quadraticTo(200f, 180f, 216f, 192f)
                                quadraticTo(200f, 200f, 184f, 192f)
                                close()
                            }
                            drawPath(bTop, brush = Brush.linearGradient(listOf(Color(0xFFFDE047), Color(0xFFB45309)), start = Offset(200f, 180f), end = Offset(200f, 200f)))
                        } else if (face == "smile-hearts") {
                            drawArc(Color(0xFF0F172A), startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(160f, 165f), size = Size(30f, 20f), style = Stroke(width = 5f, cap = StrokeCap.Round))
                            drawArc(Color(0xFF0F172A), startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(210f, 165f), size = Size(30f, 20f), style = Stroke(width = 5f, cap = StrokeCap.Round))
                            val bInside = Path().apply {
                                moveTo(184f, 192f)
                                cubicTo(184f, 215f, 216f, 215f, 216f, 192f)
                                close()
                            }
                            drawPath(bInside, brush = Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFF9F1239)), start = Offset(200f, 192f), end = Offset(200f, 215f)))
                            val bTop = Path().apply {
                                moveTo(184f, 192f)
                                quadraticTo(200f, 180f, 216f, 192f)
                                quadraticTo(200f, 200f, 184f, 192f)
                                close()
                            }
                            drawPath(bTop, brush = Brush.linearGradient(listOf(Color(0xFFFDE047), Color(0xFFB45309)), start = Offset(200f, 180f), end = Offset(200f, 200f)))

                            // Drifting hearts
                            val p1 = getPhaseProgressLocal(driftProgress, 0f)
                            val alpha1 = if (p1 < 0.2f) p1 / 0.2f else 1f - (p1 - 0.2f) / 0.8f
                            drawHeartLocal(x = 160f, y = 165f - 40f * p1, scale = 0.6f + 0.4f * p1, alpha = alpha1, color = Color(0xFFF43F5E))

                            val p2 = getPhaseProgressLocal(driftProgress, 0.33f)
                            val alpha2 = if (p2 < 0.2f) p2 / 0.2f else 1f - (p2 - 0.2f) / 0.8f
                            drawHeartLocal(x = 240f, y = 160f - 40f * p2, scale = 0.6f + 0.4f * p2, alpha = alpha2, color = Color(0xFFF43F5E))

                            val p3 = getPhaseProgressLocal(driftProgress, 0.66f)
                            val alpha3 = if (p3 < 0.2f) p3 / 0.2f else 1f - (p3 - 0.2f) / 0.8f
                            drawHeartLocal(x = 200f, y = 135f - 40f * p3, scale = 0.5f + 0.4f * p3, alpha = alpha3, color = Color(0xFFF43F5E))
                        } else if (face == "wink") {
                            drawCircle(color = Color(0xFF0F172A), radius = 14f, center = Offset(175f, 175f))
                            drawCircle(color = Color.White, radius = 4.5f, center = Offset(172f, 170f))
                            drawArc(Color(0xFF0F172A), startAngle = 0f, sweepAngle = 180f, useCenter = false, topLeft = Offset(212f, 170f), size = Size(26f, 12f), style = Stroke(width = 4f, cap = StrokeCap.Round))
                            val bTop = Path().apply {
                                moveTo(184f, 192f)
                                quadraticTo(200f, 205f, 216f, 192f)
                                close()
                            }
                            drawPath(bTop, brush = Brush.linearGradient(listOf(Color(0xFFFDE047), Color(0xFFB45309)), start = Offset(200f, 192f), end = Offset(200f, 205f)))
                        } else if (face == "kiss") {
                            drawArc(Color(0xFF0F172A), startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(160f, 168f), size = Size(30f, 14f), style = Stroke(width = 5f, cap = StrokeCap.Round))
                            drawArc(Color(0xFF0F172A), startAngle = 0f, sweepAngle = 180f, useCenter = false, topLeft = Offset(212f, 170f), size = Size(26f, 12f), style = Stroke(width = 4f, cap = StrokeCap.Round))
                            drawCircle(brush = Brush.linearGradient(listOf(Color(0xFFFDE047), Color(0xFFB45309)), start = Offset(192f, 195f), end = Offset(208f, 195f)), radius = 8f, center = Offset(200f, 195f))
                            drawCircle(Color(0xFF9F1239), radius = 3f, center = Offset(200f, 195f))

                            // Drifting kiss heart
                            val pk = getPhaseProgressLocal(driftProgress, 0f)
                            val alphak = if (pk < 0.2f) pk / 0.2f else 1f - (pk - 0.2f) / 0.8f
                            drawHeartLocal(x = 210f, y = 198f - 35f * pk, scale = 0.5f + 0.3f * pk, alpha = alphak, color = Color(0xFFF43F5E))
                        } else if (face == "tears-of-joy") {
                            drawCircle(color = Color(0xFF0F172A), radius = 16f, center = Offset(175f, 175f))
                            drawCircle(color = Color.White, radius = 6f, center = Offset(175f, 180f))
                            drawCircle(color = Color.White, radius = 3f, center = Offset(182f, 170f))
                            drawCircle(color = Color(0xFF0F172A), radius = 16f, center = Offset(225f, 175f))
                            drawCircle(color = Color.White, radius = 6f, center = Offset(225f, 180f))
                            drawCircle(color = Color.White, radius = 3f, center = Offset(218f, 170f))
                            val bInside = Path().apply {
                                moveTo(184f, 192f)
                                cubicTo(184f, 215f, 216f, 215f, 216f, 192f)
                                close()
                            }
                            drawPath(bInside, brush = Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFF9F1239)), start = Offset(200f, 192f), end = Offset(200f, 215f)))
                            val bTop = Path().apply {
                                moveTo(184f, 192f)
                                quadraticTo(200f, 180f, 216f, 192f)
                                quadraticTo(200f, 200f, 184f, 192f)
                                close()
                            }
                            drawPath(bTop, brush = Brush.linearGradient(listOf(Color(0xFFFDE047), Color(0xFFB45309)), start = Offset(200f, 180f), end = Offset(200f, 200f)))

                            drawCircle(Color(0xFF7DD3FC), radius = 4f, center = Offset(165f, 188f + 10f * sin(System.currentTimeMillis() / 150f)))
                            drawCircle(Color(0xFF7DD3FC), radius = 4f, center = Offset(235f, 188f + 10f * sin(System.currentTimeMillis() / 150f)))
                        } else if (face == "pleading") {
                            drawCircle(color = Color(0xFF0F172A), radius = 18f, center = Offset(175f, 175f))
                            drawCircle(color = Color.White, radius = 7f, center = Offset(175f, 178f))
                            drawCircle(color = Color.White, radius = 3f, center = Offset(184f, 168f))
                            drawCircle(color = Color(0xFF0F172A), radius = 18f, center = Offset(225f, 175f))
                            drawCircle(color = Color.White, radius = 7f, center = Offset(225f, 178f))
                            drawCircle(color = Color.White, radius = 3f, center = Offset(216f, 168f))
                            val bTop = Path().apply {
                                moveTo(188f, 198f)
                                quadraticTo(200f, 190f, 212f, 198f)
                                quadraticTo(200f, 205f, 188f, 198f)
                                close()
                            }
                            drawPath(bTop, brush = Brush.linearGradient(listOf(Color(0xFFFDE047), Color(0xFFB45309)), start = Offset(200f, 190f), end = Offset(200f, 205f)))
                        } else if (face == "cry") {
                            // Crying eyebrows
                            drawArc(Color(0xFF0F172A), startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(160f, 155f), size = Size(30f, 15f), style = Stroke(width = 4f, cap = StrokeCap.Round))
                            drawArc(Color(0xFF0F172A), startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(210f, 155f), size = Size(30f, 15f), style = Stroke(width = 4f, cap = StrokeCap.Round))
                            drawLine(Color(0xFF0F172A), start = Offset(160f, 180f), end = Offset(175f, 170f), strokeWidth = 5f, cap = StrokeCap.Round)
                            drawLine(Color(0xFF0F172A), start = Offset(175f, 170f), end = Offset(190f, 180f), strokeWidth = 5f, cap = StrokeCap.Round)
                            drawLine(Color(0xFF0F172A), start = Offset(210f, 180f), end = Offset(225f, 170f), strokeWidth = 5f, cap = StrokeCap.Round)
                            drawLine(Color(0xFF0F172A), start = Offset(225f, 170f), end = Offset(240f, 180f), strokeWidth = 5f, cap = StrokeCap.Round)
                            val bInside = Path().apply {
                                moveTo(184f, 200f)
                                cubicTo(184f, 185f, 216f, 185f, 216f, 200f)
                                close()
                            }
                            drawPath(bInside, brush = Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFF9F1239)), start = Offset(200f, 185f), end = Offset(200f, 200f)))
                            val bTop = Path().apply {
                                moveTo(184f, 200f)
                                quadraticTo(200f, 210f, 216f, 200f)
                                close()
                            }
                            drawPath(bTop, brush = Brush.linearGradient(listOf(Color(0xFFFDE047), Color(0xFFB45309)), start = Offset(200f, 200f), end = Offset(200f, 210f)))

                            // Waterfall tears
                            drawLine(Color(0xCCE0F2FE), start = Offset(175f, 180f), end = Offset(175f, 230f), strokeWidth = 12f, cap = StrokeCap.Round)
                            drawLine(Color(0xCCE0F2FE), start = Offset(225f, 180f), end = Offset(225f, 230f), strokeWidth = 12f, cap = StrokeCap.Round)
                        } else if (face == "disappointed") {
                            drawArc(Color(0xFF0F172A), startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(160f, 170f), size = Size(30f, 15f), style = Stroke(width = 4f, cap = StrokeCap.Round))
                            drawArc(Color(0xFF0F172A), startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(210f, 170f), size = Size(30f, 15f), style = Stroke(width = 4f, cap = StrokeCap.Round))
                            val bTop = Path().apply {
                                moveTo(188f, 198f)
                                quadraticTo(200f, 193f, 212f, 198f)
                                lineTo(200f, 200f)
                                close()
                            }
                            drawPath(bTop, brush = Brush.linearGradient(listOf(Color(0xFFFDE047), Color(0xFFB45309)), start = Offset(200f, 193f), end = Offset(200f, 200f)))
                        } else if (face == "sad-simple") {
                            drawCircle(color = Color(0xFF0F172A), radius = 10f, center = Offset(175f, 178f))
                            drawCircle(color = Color(0xFF0F172A), radius = 10f, center = Offset(225f, 178f))
                            drawArc(Color(0xFF0F172A), startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(165f, 158f), size = Size(20f, 10f), style = Stroke(width = 3f, cap = StrokeCap.Round))
                            drawArc(Color(0xFF0F172A), startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(215f, 158f), size = Size(20f, 10f), style = Stroke(width = 3f, cap = StrokeCap.Round))
                            val bTop = Path().apply {
                                moveTo(186f, 198f)
                                quadraticTo(200f, 190f, 214f, 198f)
                                quadraticTo(200f, 202f, 186f, 198f)
                                close()
                            }
                            drawPath(bTop, brush = Brush.linearGradient(listOf(Color(0xFFFDE047), Color(0xFFB45309)), start = Offset(200f, 190f), end = Offset(200f, 202f)))
                        } else if (face == "angry") {
                            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(175f, 175f))
                            drawCircle(color = Color.White, radius = 3f, center = Offset(175f, 175f))
                            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(225f, 175f))
                            drawCircle(color = Color.White, radius = 3f, center = Offset(225f, 175f))
                            drawLine(Color(0xFF0F172A), start = Offset(155f, 160f), end = Offset(185f, 172f), strokeWidth = 6f, cap = StrokeCap.Round)
                            drawLine(Color(0xFF0F172A), start = Offset(245f, 160f), end = Offset(215f, 172f), strokeWidth = 6f, cap = StrokeCap.Round)
                            val bTop = Path().apply {
                                moveTo(184f, 195f)
                                quadraticTo(200f, 190f, 216f, 195f)
                                lineTo(200f, 198f)
                                close()
                            }
                            drawPath(bTop, brush = Brush.linearGradient(listOf(Color(0xFFFDE047), Color(0xFFB45309)), start = Offset(200f, 190f), end = Offset(200f, 198f)))
                        } else if (face == "angry-red") {
                            drawCircle(color = Color(0xFF0F172A), radius = 14f, center = Offset(175f, 175f))
                            drawCircle(color = Color.White, radius = 3f, center = Offset(175f, 175f))
                            drawCircle(color = Color(0xFF0F172A), radius = 14f, center = Offset(225f, 175f))
                            drawCircle(color = Color.White, radius = 3f, center = Offset(225f, 175f))
                            drawLine(Color(0xFF0F172A), start = Offset(150f, 155f), end = Offset(188f, 175f), strokeWidth = 8f, cap = StrokeCap.Round)
                            drawLine(Color(0xFF0F172A), start = Offset(250f, 155f), end = Offset(212f, 175f), strokeWidth = 8f, cap = StrokeCap.Round)
                            val teethPath = Path().apply {
                                moveTo(184f, 192f)
                                lineTo(192f, 196f)
                                lineTo(200f, 190f)
                                lineTo(208f, 196f)
                                lineTo(216f, 192f)
                                lineTo(208f, 200f)
                                lineTo(200f, 196f)
                                lineTo(192f, 200f)
                                close()
                            }
                            drawPath(teethPath, brush = Brush.linearGradient(listOf(Color(0xFFFDE047), Color(0xFFB45309)), start = Offset(184f, 190f), end = Offset(216f, 200f)))
                        } else if (face == "frown") {
                            drawLine(Color(0xFF0F172A), start = Offset(155f, 165f), end = Offset(185f, 172f), strokeWidth = 4f, cap = StrokeCap.Round)
                            drawLine(Color(0xFF0F172A), start = Offset(245f, 165f), end = Offset(215f, 172f), strokeWidth = 4f, cap = StrokeCap.Round)
                            drawArc(Color(0xFF0F172A), startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(160f, 170f), size = Size(30f, 15f), style = Stroke(width = 4f, cap = StrokeCap.Round))
                            drawArc(Color(0xFF0F172A), startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(210f, 170f), size = Size(30f, 15f), style = Stroke(width = 4f, cap = StrokeCap.Round))
                            val bTop = Path().apply {
                                moveTo(184f, 198f)
                                quadraticTo(200f, 190f, 216f, 198f)
                                quadraticTo(200f, 202f, 184f, 198f)
                                close()
                            }
                            drawPath(bTop, brush = Brush.linearGradient(listOf(Color(0xFFFDE047), Color(0xFFB45309)), start = Offset(200f, 190f), end = Offset(200f, 202f)))
                        } else if (face == "cursing") {
                            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(175f, 175f))
                            drawCircle(color = Color.White, radius = 3f, center = Offset(175f, 175f))
                            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(225f, 175f))
                            drawCircle(color = Color.White, radius = 3f, center = Offset(225f, 175f))
                            drawLine(Color(0xFF0F172A), start = Offset(155f, 160f), end = Offset(185f, 172f), strokeWidth = 6f, cap = StrokeCap.Round)
                            drawLine(Color(0xFF0F172A), start = Offset(245f, 160f), end = Offset(215f, 172f), strokeWidth = 6f, cap = StrokeCap.Round)

                            drawIntoCanvas { canvas ->
                                val paint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.WHITE
                                    textSize = 14f
                                    isFakeBoldText = true
                                    textAlign = android.graphics.Paint.Align.CENTER
                                }
                                canvas.drawRect(
                                    rect = Rect(170f, 188f, 230f, 208f),
                                    paint = androidx.compose.ui.graphics.Paint().apply { color = Color(0xFF0F172A) }
                                )
                                canvas.nativeCanvas.drawText("#@!&", 200f, 202f, paint)
                            }
                        } else if (face == "scream") {
                            drawCircle(color = Color.White, radius = 16f, center = Offset(175f, 170f))
                            drawCircle(color = Color(0xFF0F172A), radius = 16f, center = Offset(175f, 170f), style = Stroke(width = 2f))
                            drawCircle(color = Color(0xFF0F172A), radius = 3f, center = Offset(175f, 170f))
                            drawCircle(color = Color.White, radius = 16f, center = Offset(225f, 170f))
                            drawCircle(color = Color(0xFF0F172A), radius = 16f, center = Offset(225f, 170f), style = Stroke(width = 2f))
                            drawCircle(color = Color(0xFF0F172A), radius = 3f, center = Offset(225f, 170f))

                            drawOval(brush = Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFF9F1239)), start = Offset(200f, 187f), end = Offset(200f, 223f)), topLeft = Offset(190f, 187f), size = Size(20f, 36f))
                            drawOval(brush = Brush.linearGradient(listOf(Color(0xFFFDE047), Color(0xFFB45309)), start = Offset(190f, 187f), end = Offset(210f, 223f)), topLeft = Offset(190f, 187f), size = Size(20f, 36f), style = Stroke(width = 3f))
                        } else if (face == "astonished") {
                            drawCircle(color = Color(0xFF0F172A), radius = 6f, center = Offset(175f, 172f))
                            drawCircle(color = Color(0xFF0F172A), radius = 6f, center = Offset(225f, 172f))
                            drawArc(Color(0xFF0F172A), startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(165f, 148f), size = Size(20f, 10f), style = Stroke(width = 3f, cap = StrokeCap.Round))
                            drawArc(Color(0xFF0F172A), startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(215f, 148f), size = Size(20f, 10f), style = Stroke(width = 3f, cap = StrokeCap.Round))
                            drawCircle(brush = Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFF9F1239)), start = Offset(200f, 188f), end = Offset(200f, 212f)), radius = 12f, center = Offset(200f, 200f))
                            drawCircle(brush = Brush.linearGradient(listOf(Color(0xFFFDE047), Color(0xFFB45309)), start = Offset(188f, 188f), end = Offset(212f, 212f)), radius = 12f, center = Offset(200f, 200f), style = Stroke(width = 4f))
                        } else if (face == "mouth-open") {
                            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(175f, 175f))
                            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(225f, 175f))
                            drawOval(brush = Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFF9F1239)), start = Offset(200f, 186f), end = Offset(200f, 210f)), topLeft = Offset(192f, 186f), size = Size(16f, 24f))
                            drawOval(brush = Brush.linearGradient(listOf(Color(0xFFFDE047), Color(0xFFB45309)), start = Offset(192f, 186f), end = Offset(208f, 210f)), topLeft = Offset(192f, 186f), size = Size(16f, 24f), style = Stroke(width = 3f))
                        } else if (face == "flushed") {
                            drawCircle(color = Color(0xFF0F172A), radius = 14f, center = Offset(175f, 175f))
                            drawCircle(color = Color.White, radius = 4f, center = Offset(175f, 175f))
                            drawCircle(color = Color(0xFF0F172A), radius = 14f, center = Offset(225f, 175f))
                            drawCircle(color = Color.White, radius = 4f, center = Offset(225f, 175f))
                            drawCircle(Color(0xFFF43F5E).copy(alpha = 0.5f), radius = 15f, center = Offset(160f, 190f))
                            drawCircle(Color(0xFFF43F5E).copy(alpha = 0.5f), radius = 15f, center = Offset(240f, 190f))
                            drawLine(brush = Brush.linearGradient(listOf(Color(0xFFFDE047), Color(0xFFB45309)), start = Offset(190f, 195f), end = Offset(210f, 195f)), start = Offset(190f, 195f), end = Offset(210f, 195f), strokeWidth = 5f, cap = StrokeCap.Round)
                        } else if (face == "thinking") {
                            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(180f, 172f))
                            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(220f, 172f))
                            drawArc(Color(0xFF0F172A), startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(168f, 153f), size = Size(24f, 10f), style = Stroke(width = 3f, cap = StrokeCap.Round))
                            drawLine(Color(0xFF0F172A), start = Offset(208f, 155f), end = Offset(232f, 155f), strokeWidth = 3f, cap = StrokeCap.Round)
                            drawLine(brush = Brush.linearGradient(listOf(Color(0xFFFDE047), Color(0xFFB45309)), start = Offset(188f, 198f), end = Offset(212f, 192f)), start = Offset(188f, 198f), end = Offset(212f, 192f), strokeWidth = 6f, cap = StrokeCap.Round)
                        } else if (face == "roll-eyes") {
                            drawCircle(color = Color.White, radius = 14f, center = Offset(175f, 175f))
                            drawCircle(color = Color(0xFF0F172A), radius = 14f, center = Offset(175f, 175f), style = Stroke(width = 2f))
                            drawCircle(color = Color(0xFF0F172A), radius = 5f, center = Offset(170f, 170f))
                            drawCircle(color = Color.White, radius = 14f, center = Offset(225f, 175f))
                            drawCircle(color = Color(0xFF0F172A), radius = 14f, center = Offset(225f, 175f), style = Stroke(width = 2f))
                            drawCircle(color = Color(0xFF0F172A), radius = 5f, center = Offset(220f, 170f))
                            drawLine(brush = Brush.linearGradient(listOf(Color(0xFFFDE047), Color(0xFFB45309)), start = Offset(186f, 195f), end = Offset(214f, 195f)), start = Offset(186f, 195f), end = Offset(214f, 195f), strokeWidth = 5f, cap = StrokeCap.Round)
                        } else if (face == "smirk") {
                            drawLine(Color(0xFF0F172A), start = Offset(161f, 175f), end = Offset(189f, 175f), strokeWidth = 4f, cap = StrokeCap.Round)
                            drawArc(Color(0xFF0F172A), startAngle = 0f, sweepAngle = 180f, useCenter = false, topLeft = Offset(168f, 175f), size = Size(14f, 10f), style = Stroke(width = 4f, cap = StrokeCap.Round))
                            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(225f, 175f))
                            drawArc(brush = Brush.linearGradient(listOf(Color(0xFFFDE047), Color(0xFFB45309)), start = Offset(186f, 190f), end = Offset(214f, 190f)), startAngle = 0f, sweepAngle = 180f, useCenter = false, topLeft = Offset(186f, 190f), size = Size(28f, 16f), style = Stroke(width = 6f, cap = StrokeCap.Round))
                        } else if (face == "eyebrow-raise") {
                            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(175f, 175f))
                            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(225f, 175f))
                            drawLine(Color(0xFF0F172A), start = Offset(160f, 162f), end = Offset(190f, 162f), strokeWidth = 4f, cap = StrokeCap.Round)
                            drawArc(Color(0xFF0F172A), startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(210f, 145f), size = Size(30f, 15f), style = Stroke(width = 4f, cap = StrokeCap.Round))
                            val bTop = Path().apply {
                                moveTo(184f, 195f)
                                lineTo(216f, 195f)
                                lineTo(200f, 198f)
                                close()
                            }
                            drawPath(bTop, brush = Brush.linearGradient(listOf(Color(0xFFFDE047), Color(0xFFB45309)), start = Offset(200f, 195f), end = Offset(200f, 198f)))
                        } else if (face == "sweat-smile") {
                            drawArc(Color(0xFF0F172A), startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(160f, 165f), size = Size(30f, 20f), style = Stroke(width = 5f, cap = StrokeCap.Round))
                            drawArc(Color(0xFF0F172A), startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(210f, 165f), size = Size(30f, 20f), style = Stroke(width = 5f, cap = StrokeCap.Round))
                            val bTop = Path().apply {
                                moveTo(184f, 192f)
                                quadraticTo(200f, 205f, 216f, 192f)
                                close()
                            }
                            drawPath(bTop, brush = Brush.linearGradient(listOf(Color(0xFFFDE047), Color(0xFFB45309)), start = Offset(200f, 192f), end = Offset(200f, 205f)))
                            drawCircle(Color(0xFF7DD3FC), radius = 4f, center = Offset(150f, 150f))
                            val sweatTail = Path().apply {
                                moveTo(150f, 150f)
                                lineTo(145f, 140f)
                                lineTo(155f, 145f)
                                close()
                            }
                            drawPath(sweatTail, color = Color(0xFF7DD3FC))
                        } else if (face == "sweat-cold") {
                            drawArc(Color(0xFF0F172A), startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(160f, 170f), size = Size(30f, 15f), style = Stroke(width = 4f, cap = StrokeCap.Round))
                            drawArc(Color(0xFF0F172A), startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(210f, 170f), size = Size(30f, 15f), style = Stroke(width = 4f, cap = StrokeCap.Round))
                            val bTop = Path().apply {
                                moveTo(184f, 198f)
                                quadraticTo(200f, 190f, 216f, 198f)
                                lineTo(200f, 200f)
                                close()
                            }
                            drawPath(bTop, brush = Brush.linearGradient(listOf(Color(0xFFFDE047), Color(0xFFB45309)), start = Offset(200f, 190f), end = Offset(200f, 200f)))
                            drawCircle(Color(0xFF7DD3FC), radius = 6f, center = Offset(145f, 155f))
                            val coldSweatTail = Path().apply {
                                moveTo(145f, 155f)
                                lineTo(140f, 140f)
                                lineTo(150f, 145f)
                                close()
                            }
                            drawPath(coldSweatTail, color = Color(0xFF7DD3FC))
                        } else if (face == "yawn") {
                            drawArc(Color(0xFF0F172A), startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(160f, 165f), size = Size(30f, 15f), style = Stroke(width = 4f, cap = StrokeCap.Round))
                            drawArc(Color(0xFF0F172A), startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(210f, 165f), size = Size(30f, 15f), style = Stroke(width = 4f, cap = StrokeCap.Round))
                            drawOval(brush = Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFF9F1239)), start = Offset(200f, 185f), end = Offset(200f, 225f)), topLeft = Offset(184f, 185f), size = Size(32f, 40f))
                            drawOval(brush = Brush.linearGradient(listOf(Color(0xFFFDE047), Color(0xFFB45309)), start = Offset(184f, 185f), end = Offset(216f, 225f)), topLeft = Offset(184f, 185f), size = Size(32f, 40f), style = Stroke(width = 4f))
                        } else if (face == "sleep") {
                            drawLine(Color(0xFF0F172A), start = Offset(160f, 175f), end = Offset(190f, 175f), strokeWidth = 5f, cap = StrokeCap.Round)
                            drawLine(Color(0xFF0F172A), start = Offset(210f, 175f), end = Offset(240f, 175f), strokeWidth = 5f, cap = StrokeCap.Round)
                            val bTop = Path().apply {
                                moveTo(188f, 195f)
                                quadraticTo(200f, 198f, 212f, 195f)
                                lineTo(200f, 198f)
                                close()
                            }
                            drawPath(bTop, brush = Brush.linearGradient(listOf(Color(0xFFFDE047), Color(0xFFB45309)), start = Offset(200f, 195f), end = Offset(200f, 198f)))

                            // Drifting Zs
                            val p1z = getPhaseProgressLocal(driftProgress, 0f)
                            val alpha1z = if (p1z < 0.2f) p1z / 0.2f else 1f - (p1z - 0.2f) / 0.8f
                            val zPath1 = createZPathLocal(210f + 30f * p1z, 160f - 40f * p1z, size = 10f + 10f * p1z)
                            drawPath(zPath1, color = Color(0xFF38BDF8), alpha = alpha1z, style = Stroke(width = 2f + 2f * p1z, cap = StrokeCap.Round))

                            val p2z = getPhaseProgressLocal(driftProgress, 0.5f)
                            val alpha2z = if (p2z < 0.2f) p2z / 0.2f else 1f - (p2z - 0.2f) / 0.8f
                            val zPath2 = createZPathLocal(210f + 30f * p2z, 160f - 40f * p2z, size = 10f + 10f * p2z)
                            drawPath(zPath2, color = Color(0xFF38BDF8), alpha = alpha2z, style = Stroke(width = 2f + 2f * p2z, cap = StrokeCap.Round))
                        } else if (face == "zipped") {
                            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(175f, 175f))
                            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(225f, 175f))
                            drawLine(Color(0xFF94A3B8), start = Offset(180f, 195f), end = Offset(220f, 195f), strokeWidth = 4f)
                            for (i in 185..215 step 10) {
                                drawLine(Color(0xFF94A3B8), start = Offset(i.toFloat(), 190f), end = Offset(i.toFloat(), 200f), strokeWidth = 2f)
                            }
                            drawRoundRect(Color(0xFFCBD5E1), topLeft = Offset(220f, 192f), size = Size(6f, 8f), cornerRadius = CornerRadius(2f, 2f))
                        } else if (face == "dizzy") {
                            for (r in listOf(6f, 12f, 18f)) {
                                drawCircle(Color(0xFF0F172A), radius = r, center = Offset(175f, 175f), style = Stroke(width = 2.5f))
                            }
                            for (r in listOf(6f, 12f, 18f)) {
                                drawCircle(Color(0xFF0F172A), radius = r, center = Offset(225f, 175f), style = Stroke(width = 2.5f))
                            }
                            val dizzyBeak = Path().apply {
                                moveTo(184f, 195f)
                                lineTo(192f, 190f)
                                lineTo(200f, 198f)
                                lineTo(208f, 190f)
                                lineTo(216f, 195f)
                            }
                            drawPath(dizzyBeak, color = Color(0xFFB45309), style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                        }


                        // Headphones
                        drawArc(
                            color = Color(0xFF020617),
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(130f, 125f),
                            size = Size(140f, 140f),
                            style = Stroke(width = 8f)
                        )
                        // Left ear cup
                        drawRoundRect(
                            color = Color(0xFF1E293B),
                            topLeft = Offset(123f, 175f),
                            size = Size(14f, 32f),
                            cornerRadius = CornerRadius(7f, 7f)
                        )
                        drawCircle(color = Color(0xFF38BDF8), radius = 3f, center = Offset(130f, 191f))
                        // Right ear cup
                        drawRoundRect(
                            color = Color(0xFF1E293B),
                            topLeft = Offset(263f, 175f),
                            size = Size(14f, 32f),
                            cornerRadius = CornerRadius(7f, 7f)
                        )
                        drawCircle(color = Color(0xFF38BDF8), radius = 3f, center = Offset(270f, 191f))

                        // GADGETS LAYERS
                        if (gadget == "stars") {
                            drawCircle(color = Color(0xFFFBBF24), radius = 5f, center = Offset(85f, 115f))
                            drawCircle(color = Color(0xFFFBBF24), radius = 5f, center = Offset(315f, 115f))
                            drawCircle(color = Color(0xFFFBBF24), radius = 7f, center = Offset(200f, 55f))
                        } else if (gadget == "pencil") {
                            // Pencil body drawing at (135, 210)
                            withTransform({
                                translate(120f, 150f)
                                rotate(-15f, pivot = Offset(9f, 24f))
                            }) {
                                drawRoundRect(
                                    color = Color(0xFFFBBF24),
                                    topLeft = Offset(0f, 7f),
                                    size = Size(18f, 41f),
                                    cornerRadius = CornerRadius(2f, 2f)
                                )
                                // Tip
                                val tipPath = Path().apply {
                                    moveTo(0f, 48f)
                                    lineTo(18f, 48f)
                                    lineTo(9f, 62f)
                                    close()
                                }
                                drawPath(tipPath, color = Color(0xFFD97706)) // wood color
                                val leadPath = Path().apply {
                                    moveTo(6f, 57f)
                                    lineTo(12f, 57f)
                                    lineTo(9f, 62f)
                                    close()
                                }
                                drawPath(leadPath, color = Color(0xFF0F172A)) // graphite
                                // Metal band
                                drawRect(color = Color(0xFF94A3B8), topLeft = Offset(0f, 0f), size = Size(18f, 7f))
                                // Eraser
                                val eraserPath = Path().apply {
                                    moveTo(0f, 0f)
                                    quadraticTo(9f, -10f, 18f, 0f)
                                    close()
                                }
                                drawPath(eraserPath, color = Color(0xFFF43F5E))
                            }
                        } else if (gadget == "globe-native" || gadget == "globe-target") {
                            withTransform({
                                translate(200f, 242f)
                                scale(0.5f, 0.5f, pivot = Offset.Zero)
                            }) {
                                drawCircle(color = Color(0xFF34D399), radius = 23f, center = Offset.Zero)
                                // Grid
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.4f),
                                    radius = 23f,
                                    center = Offset.Zero,
                                    style = Stroke(width = 1.5f)
                                )
                                // Pin
                                val pinColor = if (gadget == "globe-native") Color(0xFF4C1D95) else Color(0xFFF43F5E)
                                val pinPath = Path().apply {
                                    moveTo(0f, -4f)
                                    cubicTo(-6f, -16f, 6f, -16f, 0f, -4f)
                                    close()
                                }
                                drawPath(pinPath, color = pinColor)
                                drawCircle(color = Color.White, radius = 1.5f, center = Offset(0f, -10f))
                            }
                        } else if (gadget == "timer") {
                            // Hourglass timer
                            withTransform({
                                translate(180f, 220f)
                            }) {
                                // Glass Frame
                                val frame = Path().apply {
                                    moveTo(5f, 2f)
                                    lineTo(35f, 2f)
                                    lineTo(23f, 17f)
                                    lineTo(35f, 32f)
                                    lineTo(5f, 32f)
                                    lineTo(17f, 17f)
                                    close()
                                }
                                drawPath(frame, color = Color(0xFF38BDF8).copy(alpha = 0.5f))
                                // Upper sand
                                val sandTop = Path().apply {
                                    moveTo(8f, 5f)
                                    lineTo(32f, 5f)
                                    lineTo(20f, 17f)
                                    close()
                                }
                                drawPath(sandTop, color = Color(0xFFFBBF24))
                                // Lower sand
                                val sandBottom = Path().apply {
                                    moveTo(17f, 17f)
                                    lineTo(30f, 30f)
                                    lineTo(10f, 30f)
                                    close()
                                }
                                drawPath(sandBottom, color = Color(0xFFFBBF24))
                                // Frame borders
                                drawRect(color = Color(0xFF0F172A), topLeft = Offset(0f, 0f), size = Size(40f, 4f))
                                drawRect(color = Color(0xFF0F172A), topLeft = Offset(0f, 30f), size = Size(40f, 4f))
                            }
                        } else if (gadget == "bell") {
                            // Rotating Bell Alarm
                            withTransform({
                                rotate(bellRingAnim, pivot = Offset(200f, 105f))
                            }) {
                                drawCircle(
                                    color = Color(0xFFF43F5E),
                                    radius = 12f,
                                    center = Offset(200f, 105f),
                                    style = Stroke(width = 3f)
                                )
                                drawLine(color = Color(0xFFF43F5E), start = Offset(200f, 105f), end = Offset(200f, 98f), strokeWidth = 2f)
                                drawLine(color = Color(0xFFF43F5E), start = Offset(200f, 105f), end = Offset(205f, 105f), strokeWidth = 2f)
                                // Sound waves left and right
                                drawArc(
                                    color = Color(0xFFF43F5E),
                                    startAngle = 180f,
                                    sweepAngle = 130f,
                                    useCenter = false,
                                    topLeft = Offset(180f, 85f),
                                    size = Size(10f, 10f),
                                    style = Stroke(width = 3f)
                                )
                                drawArc(
                                    color = Color(0xFFF43F5E),
                                    startAngle = 230f,
                                    sweepAngle = 130f,
                                    useCenter = false,
                                    topLeft = Offset(210f, 85f),
                                    size = Size(10f, 10f),
                                    style = Stroke(width = 3f)
                                )
                            }
                        } else if (gadget == "placement") {
                            // Mortarboard cap on head (200, 85)
                            val capBoard = Path().apply {
                                moveTo(200f, 85f)
                                lineTo(225f, 95f)
                                lineTo(200f, 105f)
                                lineTo(175f, 95f)
                                close()
                            }
                            drawPath(capBoard, color = Color(0xFFFBBF24))
                            // Cap base
                            val capBase = Path().apply {
                                moveTo(185f, 99f)
                                lineTo(185f, 109f)
                                cubicTo(185f, 115f, 215f, 115f, 215f, 109f)
                                lineTo(215f, 99f)
                                close()
                            }
                            drawPath(capBase, color = Color(0xFFFBBF24).copy(alpha = 0.8f))
                            // Tassel
                            drawLine(color = Color(0xFFFBBF24), start = Offset(225f, 95f), end = Offset(225f, 110f), strokeWidth = 2f)
                            drawCircle(color = Color(0xFFFBBF24), radius = 2f, center = Offset(225f, 112f))

                            // Miniature test paper rotated at bottom left (120, 210)
                            withTransform({
                                translate(110f, 210f)
                                rotate(-15f, pivot = Offset.Zero)
                            }) {
                                drawRoundRect(
                                    color = Color.White,
                                    topLeft = Offset.Zero,
                                    size = Size(25f, 35f),
                                    cornerRadius = CornerRadius(3f, 3f)
                                )
                                drawLine(color = Color(0xFFCBD5E1), start = Offset(4f, 8f), end = Offset(20f, 8f), strokeWidth = 2f)
                                drawLine(color = Color(0xFFCBD5E1), start = Offset(4f, 14f), end = Offset(16f, 14f), strokeWidth = 2f)
                                // Checkmark
                                val checkPath = Path().apply {
                                    moveTo(5f, 25f)
                                    lineTo(9f, 29f)
                                    lineTo(18f, 20f)
                                }
                                drawPath(checkPath, color = Color(0xFF34D399), style = Stroke(width = 2f))
                            }
                        } else if (gadget == "lock") {
                            // Lock security gadget at bottom of body (184, 215)
                            withTransform({
                                translate(184f, 210f)
                            }) {
                                drawRoundRect(
                                    color = Color(0xFF0F172A),
                                    topLeft = Offset(0f, 10f),
                                    size = Size(32f, 22f),
                                    cornerRadius = CornerRadius(6f, 6f)
                                )
                                drawRoundRect(
                                    color = Color(0xFFF43F5E),
                                    topLeft = Offset(0f, 10f),
                                    size = Size(32f, 22f),
                                    cornerRadius = CornerRadius(6f, 6f),
                                    style = Stroke(width = 3f)
                                )
                                // Shackle
                                val shackle = Path().apply {
                                    moveTo(6f, 10f)
                                    lineTo(6f, 4f)
                                    cubicTo(6f, -3f, 26f, -3f, 26f, 4f)
                                    lineTo(26f, 10f)
                                }
                                drawPath(shackle, color = Color(0xFFF43F5E), style = Stroke(width = 3f))
                                drawCircle(color = Color(0xFFF43F5E), radius = 3f, center = Offset(16f, 21f))
                            }
                        } else if (gadget == "search-data") {
                            // Small floating binary codes or tech lines
                            drawLine(color = Color(0xFF34D399), start = Offset(60f, 130f), end = Offset(80f, 130f), strokeWidth = 2f)
                            drawLine(color = Color(0xFF34D399), start = Offset(65f, 140f), end = Offset(85f, 140f), strokeWidth = 2f)
                            drawLine(color = Color(0xFF34D399), start = Offset(320f, 130f), end = Offset(340f, 130f), strokeWidth = 2f)
                            drawLine(color = Color(0xFF34D399), start = Offset(315f, 140f), end = Offset(335f, 140f), strokeWidth = 2f)
                        }
                    }
                }
            }
        }

        if (message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            GlassCard(
                cornerRadius = 16.dp,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = message,
                    color = Color(0xFF00FFD2),
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}
