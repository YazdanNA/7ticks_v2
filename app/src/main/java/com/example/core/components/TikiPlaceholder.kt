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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.sin

@Composable
fun TikiPlaceholder(
    modifier: Modifier = Modifier,
    sizeDp: Int = 140,
    message: String = "",
    tikiState: String = "st-happy",
    isSpeaking: Boolean = false
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

    // 8. Furious shake offset
    val shakeOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ticky_furious_shake"
    )

    // 9. ROFL roll rotation
    val roflRollAnim by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ticky_rofl_roll"
    )

    // Parse sub-states
    val (baseFace, body, gadget) = when (tikiState) {
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

    val face = if (isSpeaking && baseFace != "hidden") "talk" else baseFace

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
                    val bodyRotate = if (body == "sad") -4f else if (body == "rofl-roll") roflRollAnim else 0f
                    val shakeX = if (body == "furious") shakeOffset else 0f

                    withTransform({
                        translate(shakeX, bodyOffsetY)
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
                        drawFaceLayer(
                            face = face,
                            blinkAnim = blinkAnim,
                            talkBeakAnim = talkBeakAnim,
                            tearDropAnim = tearDropAnim,
                            dizzySpinAnim = loadSpinAnim
                        )

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
                        drawGadgetLayer(gadget = gadget, bellRingAnim = bellRingAnim)
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

private fun DrawScope.drawFaceLayer(
    face: String,
    blinkAnim: Float,
    talkBeakAnim: Float,
    tearDropAnim: Float,
    dizzySpinAnim: Float
) {
    // Shared beak brushes
    val beakBrush = Brush.linearGradient(
        colors = listOf(Color(0xFFFDE047), Color(0xFFF59E0B), Color(0xFFB45309)),
        start = Offset(200f, 181f),
        end = Offset(200f, 207f)
    )
    val beakInsideBrush = Brush.linearGradient(
        colors = listOf(Color(0xFFEF4444), Color(0xFF9F1239)),
        start = Offset(200f, 192f),
        end = Offset(200f, 220f)
    )

    when (face) {
        "happy" -> {
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

            val beakInsidePath = Path().apply {
                moveTo(184f, 192f)
                cubicTo(184f, 220f, 216f, 220f, 216f, 192f)
                close()
            }
            drawPath(beakInsidePath, brush = beakInsideBrush)

            val tonguePath = Path().apply {
                moveTo(192f, 209f)
                quadraticTo(200f, 201f, 208f, 209f)
                close()
            }
            drawPath(tonguePath, color = Color(0xFFFDA4AF))

            val beakTopPath = Path().apply {
                moveTo(184f, 192f)
                quadraticTo(200f, 181f, 216f, 192f)
                quadraticTo(200f, 199f, 184f, 192f)
                close()
            }
            drawPath(beakTopPath, brush = beakBrush)
        }

        "laugh-tears" -> {
            drawLine(color = Color(0xFF0F172A), start = Offset(160f, 170f), end = Offset(185f, 180f), strokeWidth = 5f, cap = StrokeCap.Round)
            drawLine(color = Color(0xFF0F172A), start = Offset(240f, 170f), end = Offset(215f, 180f), strokeWidth = 5f, cap = StrokeCap.Round)

            val laughBeak = Path().apply {
                moveTo(184f, 192f)
                cubicTo(184f, 224f, 216f, 224f, 216f, 192f)
                close()
            }
            drawPath(laughBeak, brush = beakInsideBrush)

            val laughBeakTop = Path().apply {
                moveTo(184f, 192f)
                quadraticTo(200f, 180f, 216f, 192f)
                close()
            }
            drawPath(laughBeakTop, brush = beakBrush)

            // Tear drops
            drawCircle(color = Color(0xFF38BDF8), radius = 5f, center = Offset(150f, 185f))
            drawCircle(color = Color(0xFF38BDF8), radius = 5f, center = Offset(250f, 185f))
        }

        "rofl" -> {
            drawLine(color = Color(0xFF0F172A), start = Offset(160f, 180f), end = Offset(185f, 170f), strokeWidth = 5f, cap = StrokeCap.Round)
            drawLine(color = Color(0xFF0F172A), start = Offset(240f, 180f), end = Offset(215f, 170f), strokeWidth = 5f, cap = StrokeCap.Round)

            val roflBeak = Path().apply {
                moveTo(184f, 192f)
                cubicTo(184f, 226f, 216f, 226f, 216f, 192f)
                close()
            }
            drawPath(roflBeak, brush = beakInsideBrush)

            val roflBeakTop = Path().apply {
                moveTo(184f, 192f)
                quadraticTo(200f, 180f, 216f, 192f)
                close()
            }
            drawPath(roflBeakTop, brush = beakBrush)
        }

        "laugh-big" -> {
            drawCircle(color = Color(0xFF0F172A), radius = 13f, center = Offset(175f, 175f))
            drawCircle(color = Color(0xFF0F172A), radius = 13f, center = Offset(225f, 175f))

            val laughBeak = Path().apply {
                moveTo(184f, 192f)
                cubicTo(184f, 224f, 216f, 224f, 216f, 192f)
                close()
            }
            drawPath(laughBeak, brush = beakInsideBrush)

            val laughBeakTop = Path().apply {
                moveTo(184f, 192f)
                quadraticTo(200f, 180f, 216f, 192f)
                close()
            }
            drawPath(laughBeakTop, brush = beakBrush)
        }

        "smile-big" -> {
            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(175f, 175f))
            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(225f, 175f))

            val smileBeak = Path().apply {
                moveTo(184f, 192f)
                quadraticTo(200f, 212f, 216f, 192f)
                close()
            }
            drawPath(smileBeak, brush = beakBrush)
        }

        "smile-simple" -> {
            drawCircle(color = Color(0xFF0F172A), radius = 10f, center = Offset(175f, 175f))
            drawCircle(color = Color(0xFF0F172A), radius = 10f, center = Offset(225f, 175f))

            val smileBeak = Path().apply {
                moveTo(186f, 192f)
                quadraticTo(200f, 204f, 214f, 192f)
                close()
            }
            drawPath(smileBeak, brush = beakBrush)
        }

        "smile-shy" -> {
            drawCircle(color = Color(0xFF0F172A), radius = 10f, center = Offset(175f, 175f))
            drawCircle(color = Color(0xFF0F172A), radius = 10f, center = Offset(225f, 175f))

            val smileBeak = Path().apply {
                moveTo(188f, 194f)
                quadraticTo(200f, 202f, 212f, 194f)
                close()
            }
            drawPath(smileBeak, brush = beakBrush)

            // Rosy cheeks
            drawCircle(color = Color(0xFFF43F5E).copy(alpha = 0.4f), radius = 8f, center = Offset(160f, 185f))
            drawCircle(color = Color(0xFFF43F5E).copy(alpha = 0.4f), radius = 8f, center = Offset(240f, 185f))
        }

        "heart-eyes" -> {
            // Heart left
            val heartLeft = Path().apply {
                moveTo(175f, 178f)
                cubicTo(170f, 162f, 155f, 165f, 165f, 180f)
                lineTo(175f, 190f)
                lineTo(185f, 180f)
                cubicTo(195f, 165f, 180f, 162f, 175f, 178f)
                close()
            }
            drawPath(heartLeft, color = Color(0xFFEF4444))

            // Heart right
            val heartRight = Path().apply {
                moveTo(225f, 178f)
                cubicTo(220f, 162f, 205f, 165f, 215f, 180f)
                lineTo(225f, 190f)
                lineTo(235f, 180f)
                cubicTo(245f, 165f, 230f, 162f, 225f, 178f)
                close()
            }
            drawPath(heartRight, color = Color(0xFFEF4444))

            val smileBeak = Path().apply {
                moveTo(184f, 192f)
                quadraticTo(200f, 210f, 216f, 192f)
                close()
            }
            drawPath(smileBeak, brush = beakBrush)
        }

        "smile-hearts" -> {
            drawCircle(color = Color(0xFF0F172A), radius = 10f, center = Offset(175f, 175f))
            drawCircle(color = Color(0xFF0F172A), radius = 10f, center = Offset(225f, 175f))

            val smileBeak = Path().apply {
                moveTo(186f, 192f)
                quadraticTo(200f, 204f, 214f, 192f)
                close()
            }
            drawPath(smileBeak, brush = beakBrush)

            // Tiny floating hearts around head
            val tinyHeart = Path().apply {
                moveTo(145f, 145f)
                cubicTo(142f, 137f, 134f, 138f, 139f, 146f)
                lineTo(145f, 152f)
                lineTo(151f, 146f)
                cubicTo(156f, 138f, 148f, 137f, 145f, 145f)
                close()
            }
            drawPath(tinyHeart, color = Color(0xFFF43F5E))
        }

        "wink" -> {
            drawCircle(color = Color(0xFF0F172A), radius = 11f, center = Offset(175f, 175f))
            drawCircle(color = Color.White, radius = 3f, center = Offset(175f, 175f))

            // Winking right eye arc
            val winkRight = Path().apply {
                moveTo(215f, 175f)
                quadraticTo(225f, 163f, 235f, 175f)
            }
            drawPath(winkRight, color = Color(0xFF0F172A), style = Stroke(width = 4.5f, cap = StrokeCap.Round))

            val smileBeak = Path().apply {
                moveTo(186f, 192f)
                quadraticTo(200f, 206f, 214f, 192f)
                close()
            }
            drawPath(smileBeak, brush = beakBrush)
        }

        "kiss" -> {
            drawLine(color = Color(0xFF0F172A), start = Offset(162f, 170f), end = Offset(182f, 180f), strokeWidth = 4f, cap = StrokeCap.Round)
            drawLine(color = Color(0xFF0F172A), start = Offset(182f, 170f), end = Offset(162f, 180f), strokeWidth = 4f, cap = StrokeCap.Round)

            drawLine(color = Color(0xFF0F172A), start = Offset(218f, 170f), end = Offset(238f, 180f), strokeWidth = 4f, cap = StrokeCap.Round)
            drawLine(color = Color(0xFF0F172A), start = Offset(238f, 170f), end = Offset(218f, 180f), strokeWidth = 4f, cap = StrokeCap.Round)

            // Kissy heart
            val kissyHeart = Path().apply {
                moveTo(200f, 200f)
                cubicTo(197f, 193f, 190f, 194f, 194f, 201f)
                lineTo(200f, 206f)
                lineTo(206f, 201f)
                cubicTo(210f, 194f, 203f, 193f, 200f, 200f)
                close()
            }
            drawPath(kissyHeart, color = Color(0xFFEF4444))
        }

        "tears-of-joy" -> {
            drawLine(color = Color(0xFF0F172A), start = Offset(160f, 170f), end = Offset(185f, 180f), strokeWidth = 5f, cap = StrokeCap.Round)
            drawLine(color = Color(0xFF0F172A), start = Offset(240f, 170f), end = Offset(215f, 180f), strokeWidth = 5f, cap = StrokeCap.Round)

            val laughBeak = Path().apply {
                moveTo(184f, 192f)
                cubicTo(184f, 222f, 216f, 222f, 216f, 192f)
                close()
            }
            drawPath(laughBeak, brush = beakInsideBrush)

            val laughBeakTop = Path().apply {
                moveTo(184f, 192f)
                quadraticTo(200f, 180f, 216f, 192f)
                close()
            }
            drawPath(laughBeakTop, brush = beakBrush)

            // Streaming tears
            drawRoundRect(color = Color(0xFF60A5FA), topLeft = Offset(148f, 180f), size = Size(6f, 25f), cornerRadius = CornerRadius(3f, 3f))
            drawRoundRect(color = Color(0xFF60A5FA), topLeft = Offset(246f, 180f), size = Size(6f, 25f), cornerRadius = CornerRadius(3f, 3f))
        }

        "pleading" -> {
            drawCircle(color = Color(0xFF0F172A), radius = 15f, center = Offset(175f, 175f))
            drawCircle(color = Color.White, radius = 6.5f, center = Offset(171f, 171f))
            drawCircle(color = Color.White, radius = 2f, center = Offset(179f, 179f))

            drawCircle(color = Color(0xFF0F172A), radius = 15f, center = Offset(225f, 175f))
            drawCircle(color = Color.White, radius = 6.5f, center = Offset(221f, 171f))
            drawCircle(color = Color.White, radius = 2f, center = Offset(229f, 179f))

            val pleadingBeak = Path().apply {
                moveTo(186f, 198f)
                quadraticTo(200f, 191f, 214f, 198f)
                close()
            }
            drawPath(pleadingBeak, brush = beakBrush)
        }

        "sad" -> {
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

            val sadBeak = Path().apply {
                moveTo(184f, 195f)
                quadraticTo(200f, 176f, 216f, 195f)
                quadraticTo(200f, 207f, 184f, 195f)
                close()
            }
            drawPath(sadBeak, brush = beakBrush)

            drawCircle(
                color = Color(0xFF7DD3FC),
                radius = 4f,
                center = Offset(175f, 186f + tearDropAnim)
            )
        }

        "talk" -> {
            drawCircle(color = Color(0xFF0F172A), radius = 14f, center = Offset(175f, 175f))
            drawCircle(color = Color(0xFFFFFFFF), radius = 4.5f, center = Offset(172f, 170f))
            drawCircle(color = Color(0xFF0F172A), radius = 14f, center = Offset(225f, 175f))
            drawCircle(color = Color(0xFFFFFFFF), radius = 4.5f, center = Offset(222f, 170f))

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
                drawPath(talkBeakLower, brush = beakBrush)
            }

            val beakTop = Path().apply {
                moveTo(184f, 192f)
                quadraticTo(200f, 184f, 216f, 192f)
                close()
            }
            drawPath(beakTop, brush = beakBrush)
        }

        "poker" -> {
            drawLine(color = Color(0xFF0F172A), start = Offset(160f, 175f), end = Offset(190f, 175f), strokeWidth = 4f, cap = StrokeCap.Round)
            drawLine(color = Color(0xFF0F172A), start = Offset(210f, 175f), end = Offset(240f, 175f), strokeWidth = 4f, cap = StrokeCap.Round)

            val pokerBeak = Path().apply {
                moveTo(184f, 192f)
                quadraticTo(200f, 187f, 216f, 192f)
                lineTo(200f, 195f)
                close()
            }
            drawPath(pokerBeak, color = Color(0xFFF59E0B))
        }

        "cry" -> {
            drawLine(color = Color(0xFF0F172A), start = Offset(160f, 170f), end = Offset(185f, 170f), strokeWidth = 4.5f, cap = StrokeCap.Round)
            drawLine(color = Color(0xFF0F172A), start = Offset(240f, 170f), end = Offset(215f, 170f), strokeWidth = 4.5f, cap = StrokeCap.Round)

            // Streaming waterfalls
            drawRoundRect(color = Color(0xFF38BDF8), topLeft = Offset(165f, 175f), size = Size(10f, 40f), cornerRadius = CornerRadius(5f, 5f))
            drawRoundRect(color = Color(0xFF38BDF8), topLeft = Offset(225f, 175f), size = Size(10f, 40f), cornerRadius = CornerRadius(5f, 5f))

            val cuteBeak = Path().apply {
                moveTo(188f, 198f)
                quadraticTo(200f, 190f, 212f, 198f)
                close()
            }
            drawPath(cuteBeak, brush = beakBrush)
        }

        "disappointed" -> {
            drawLine(color = Color(0xFF0F172A), start = Offset(160f, 170f), end = Offset(190f, 170f), strokeWidth = 4f, cap = StrokeCap.Round)
            drawLine(color = Color(0xFF0F172A), start = Offset(210f, 170f), end = Offset(240f, 170f), strokeWidth = 4f, cap = StrokeCap.Round)

            val sadBeak = Path().apply {
                moveTo(188f, 198f)
                quadraticTo(200f, 193f, 212f, 198f)
                close()
            }
            drawPath(sadBeak, brush = beakBrush)
        }

        "sad-simple" -> {
            drawCircle(color = Color(0xFF0F172A), radius = 10f, center = Offset(175f, 178f))
            drawCircle(color = Color(0xFF0F172A), radius = 10f, center = Offset(225f, 178f))

            // Sad eyebrows
            drawLine(color = Color(0xFF0F172A), start = Offset(165f, 165f), end = Offset(185f, 165f), strokeWidth = 3f, cap = StrokeCap.Round)
            drawLine(color = Color(0xFF0F172A), start = Offset(215f, 165f), end = Offset(235f, 165f), strokeWidth = 3f, cap = StrokeCap.Round)

            val sadBeak = Path().apply {
                moveTo(186f, 198f)
                quadraticTo(200f, 190f, 214f, 198f)
                close()
            }
            drawPath(sadBeak, brush = beakBrush)
        }

        "angry", "angry-red" -> {
            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(175f, 175f))
            drawCircle(color = Color.White, radius = 3f, center = Offset(175f, 175f))
            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(225f, 175f))
            drawCircle(color = Color.White, radius = 3f, center = Offset(225f, 175f))

            // Downward angry eyebrows
            drawLine(color = Color(0xFF0F172A), start = Offset(155f, 160f), end = Offset(185f, 172f), strokeWidth = 6f, cap = StrokeCap.Round)
            drawLine(color = Color(0xFF0F172A), start = Offset(245f, 160f), end = Offset(215f, 172f), strokeWidth = 6f, cap = StrokeCap.Round)

            val angryBeak = Path().apply {
                moveTo(184f, 195f)
                quadraticTo(200f, 190f, 216f, 195f)
                close()
            }
            drawPath(angryBeak, brush = beakBrush)
        }

        "frown" -> {
            drawLine(color = Color(0xFF0F172A), start = Offset(160f, 170f), end = Offset(175f, 165f), strokeWidth = 4f, cap = StrokeCap.Round)
            drawLine(color = Color(0xFF0F172A), start = Offset(210f, 175f), end = Offset(225f, 165f), strokeWidth = 4f, cap = StrokeCap.Round)

            val sadBeak = Path().apply {
                moveTo(184f, 198f)
                quadraticTo(200f, 190f, 216f, 198f)
                close()
            }
            drawPath(sadBeak, brush = beakBrush)
        }

        "cursing" -> {
            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(175f, 175f))
            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(225f, 175f))

            drawLine(color = Color(0xFF0F172A), start = Offset(155f, 160f), end = Offset(185f, 172f), strokeWidth = 6f, cap = StrokeCap.Round)
            drawLine(color = Color(0xFF0F172A), start = Offset(245f, 160f), end = Offset(215f, 172f), strokeWidth = 6f, cap = StrokeCap.Round)

            // Censor bar
            drawRoundRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(175f, 188f),
                size = Size(50f, 20f),
                cornerRadius = CornerRadius(3f, 3f)
            )
        }

        "scream" -> {
            drawCircle(color = Color.White, radius = 16f, center = Offset(175f, 170f))
            drawCircle(color = Color(0xFF0F172A), radius = 16f, center = Offset(175f, 170f), style = Stroke(width = 2f))
            drawCircle(color = Color(0xFF0F172A), radius = 3f, center = Offset(175f, 170f))

            drawCircle(color = Color.White, radius = 16f, center = Offset(225f, 170f))
            drawCircle(color = Color(0xFF0F172A), radius = 16f, center = Offset(225f, 170f), style = Stroke(width = 2f))
            drawCircle(color = Color(0xFF0F172A), radius = 3f, center = Offset(225f, 170f))

            // Scream mouth oval
            drawOval(
                color = Color(0xFFEF4444),
                topLeft = Offset(190f, 187f),
                size = Size(20f, 36f)
            )
        }

        "astonished" -> {
            drawCircle(color = Color(0xFF0F172A), radius = 6f, center = Offset(175f, 172f))
            drawCircle(color = Color(0xFF0F172A), radius = 6f, center = Offset(225f, 172f))

            drawCircle(color = Color(0xFFEF4444), radius = 12f, center = Offset(200f, 200f))
        }

        "mouth-open" -> {
            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(175f, 175f))
            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(225f, 175f))

            drawOval(
                color = Color(0xFFEF4444),
                topLeft = Offset(192f, 186f),
                size = Size(16f, 24f)
            )
        }

        "flushed" -> {
            drawCircle(color = Color(0xFF0F172A), radius = 14f, center = Offset(175f, 175f))
            drawCircle(color = Color.White, radius = 4f, center = Offset(175f, 175f))
            drawCircle(color = Color(0xFF0F172A), radius = 14f, center = Offset(225f, 175f))
            drawCircle(color = Color.White, radius = 4f, center = Offset(225f, 175f))

            // Blushing red circles
            drawCircle(color = Color(0xFFF43F5E).copy(alpha = 0.5f), radius = 15f, center = Offset(160f, 190f))
            drawCircle(color = Color(0xFFF43F5E).copy(alpha = 0.5f), radius = 15f, center = Offset(240f, 190f))

            drawLine(color = Color(0xFFB45309), start = Offset(190f, 195f), end = Offset(210f, 195f), strokeWidth = 5f, cap = StrokeCap.Round)
        }

        "thinking" -> {
            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(180f, 172f))
            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(220f, 172f))

            // Pensiveness eyebrows
            drawLine(color = Color(0xFF0F172A), start = Offset(168f, 158f), end = Offset(192f, 158f), strokeWidth = 3f, cap = StrokeCap.Round)
            drawLine(color = Color(0xFF0F172A), start = Offset(208f, 152f), end = Offset(232f, 158f), strokeWidth = 3f, cap = StrokeCap.Round)

            drawLine(color = Color(0xFFF59E0B), start = Offset(188f, 198f), end = Offset(212f, 192f), strokeWidth = 6f, cap = StrokeCap.Round)
        }

        "roll-eyes" -> {
            drawCircle(color = Color.White, radius = 14f, center = Offset(175f, 175f))
            drawCircle(color = Color(0xFF0F172A), radius = 14f, center = Offset(175f, 175f), style = Stroke(width = 2f))
            drawCircle(color = Color(0xFF0F172A), radius = 5f, center = Offset(170f, 170f))

            drawCircle(color = Color.White, radius = 14f, center = Offset(225f, 175f))
            drawCircle(color = Color(0xFF0F172A), radius = 14f, center = Offset(225f, 175f), style = Stroke(width = 2f))
            drawCircle(color = Color(0xFF0F172A), radius = 5f, center = Offset(220f, 170f))

            drawLine(color = Color(0xFFF59E0B), start = Offset(186f, 195f), end = Offset(214f, 195f), strokeWidth = 5f, cap = StrokeCap.Round)
        }

        "smirk" -> {
            drawLine(color = Color(0xFF0F172A), start = Offset(161f, 175f), end = Offset(189f, 175f), strokeWidth = 4f, cap = StrokeCap.Round)
            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(225f, 175f))

            val smirkBeak = Path().apply {
                moveTo(186f, 198f)
                quadraticTo(200f, 198f, 214f, 192f)
            }
            drawPath(smirkBeak, color = Color(0xFFF59E0B), style = Stroke(width = 6f, cap = StrokeCap.Round))
        }

        "eyebrow-raise" -> {
            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(175f, 175f))
            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(225f, 175f))

            drawLine(color = Color(0xFF0F172A), start = Offset(160f, 162f), end = Offset(190f, 162f), strokeWidth = 4f, cap = StrokeCap.Round)
            drawLine(color = Color(0xFF0F172A), start = Offset(210f, 152f), end = Offset(240f, 155f), strokeWidth = 4f, cap = StrokeCap.Round)

            val raiseBeak = Path().apply {
                moveTo(184f, 195f)
                lineTo(216f, 195f)
                lineTo(200f, 198f)
                close()
            }
            drawPath(raiseBeak, brush = beakBrush)
        }

        "sweat-smile" -> {
            drawLine(color = Color(0xFF0F172A), start = Offset(160f, 175f), end = Offset(175f, 160f), strokeWidth = 5f, cap = StrokeCap.Round)
            drawLine(color = Color(0xFF0F172A), start = Offset(175f, 160f), end = Offset(190f, 175f), strokeWidth = 5f, cap = StrokeCap.Round)
            drawLine(color = Color(0xFF0F172A), start = Offset(210f, 175f), end = Offset(225f, 160f), strokeWidth = 5f, cap = StrokeCap.Round)
            drawLine(color = Color(0xFF0F172A), start = Offset(225f, 160f), end = Offset(240f, 175f), strokeWidth = 5f, cap = StrokeCap.Round)

            val happyBeak = Path().apply {
                moveTo(184f, 192f)
                quadraticTo(200f, 205f, 216f, 192f)
                close()
            }
            drawPath(happyBeak, brush = beakBrush)

            drawCircle(color = Color(0xFF7DD3FC), radius = 5f, center = Offset(150f, 150f))
        }

        "sweat-cold" -> {
            drawLine(color = Color(0xFF0F172A), start = Offset(160f, 170f), end = Offset(190f, 170f), strokeWidth = 4f, cap = StrokeCap.Round)
            drawLine(color = Color(0xFF0F172A), start = Offset(210f, 170f), end = Offset(240f, 170f), strokeWidth = 4f, cap = StrokeCap.Round)

            val sadBeak = Path().apply {
                moveTo(184f, 198f)
                quadraticTo(200f, 190f, 216f, 198f)
                close()
            }
            drawPath(sadBeak, brush = beakBrush)

            drawCircle(color = Color(0xFF7DD3FC), radius = 6f, center = Offset(145f, 155f))
        }

        "yawn" -> {
            drawLine(color = Color(0xFF0F172A), start = Offset(160f, 175f), end = Offset(175f, 165f), strokeWidth = 4f, cap = StrokeCap.Round)
            drawLine(color = Color(0xFF0F172A), start = Offset(210f, 175f), end = Offset(225f, 165f), strokeWidth = 4f, cap = StrokeCap.Round)

            drawOval(
                color = Color(0xFFEF4444),
                topLeft = Offset(184f, 195f),
                size = Size(32f, 44f)
            )
        }

        "sleep" -> {
            drawLine(color = Color(0xFF0F172A), start = Offset(160f, 175f), end = Offset(190f, 175f), strokeWidth = 5f, cap = StrokeCap.Round)
            drawLine(color = Color(0xFF0F172A), start = Offset(210f, 175f), end = Offset(240f, 175f), strokeWidth = 5f, cap = StrokeCap.Round)

            val closedBeak = Path().apply {
                moveTo(188f, 195f)
                quadraticTo(200f, 198f, 212f, 195f)
                close()
            }
            drawPath(closedBeak, brush = beakBrush)
        }

        "zipped" -> {
            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(175f, 175f))
            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(225f, 175f))

            drawLine(color = Color(0xFF94A3B8), start = Offset(180f, 195f), end = Offset(220f, 195f), strokeWidth = 4f)
            drawLine(color = Color(0xFF94A3B8), start = Offset(185f, 190f), end = Offset(185f, 200f), strokeWidth = 2f)
            drawLine(color = Color(0xFF94A3B8), start = Offset(195f, 190f), end = Offset(195f, 200f), strokeWidth = 2f)
            drawLine(color = Color(0xFF94A3B8), start = Offset(205f, 190f), end = Offset(205f, 200f), strokeWidth = 2f)
            drawLine(color = Color(0xFF94A3B8), start = Offset(215f, 190f), end = Offset(215f, 200f), strokeWidth = 2f)
        }

        "dizzy" -> {
            withTransform({
                rotate(dizzySpinAnim, pivot = Offset(175f, 175f))
            }) {
                drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(175f, 175f), style = Stroke(width = 3f))
            }
            withTransform({
                rotate(-dizzySpinAnim, pivot = Offset(225f, 175f))
            }) {
                drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(225f, 175f), style = Stroke(width = 3f))
            }

            val squigglyBeak = Path().apply {
                moveTo(184f, 195f)
                lineTo(192f, 190f)
                lineTo(200f, 198f)
                lineTo(208f, 190f)
                lineTo(216f, 195f)
            }
            drawPath(squigglyBeak, brush = beakBrush, style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }

        "locked" -> {
            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(175f, 178f))
            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(225f, 178f))
            drawCircle(color = Color(0xFF38BDF8), radius = 4f, center = Offset(177f, 180f))
            drawCircle(color = Color(0xFF38BDF8), radius = 4f, center = Offset(223f, 180f))

            val lockedBeak = Path().apply {
                moveTo(188f, 193f)
                quadraticTo(200f, 188f, 212f, 193f)
                close()
            }
            drawPath(lockedBeak, color = Color(0xFFF59E0B))
        }

        "search" -> {
            drawCircle(color = Color(0xFF0F172A), radius = 14f, center = Offset(175f, 175f))
            drawCircle(color = Color(0xFF0F172A), radius = 14f, center = Offset(225f, 175f))

            val searchBeak = Path().apply {
                moveTo(184f, 192f)
                quadraticTo(200f, 187f, 216f, 192f)
                close()
            }
            drawPath(searchBeak, color = Color(0xFFF59E0B))

            drawCircle(color = Color(0xFF34D399), radius = 18f, center = Offset(175f, 175f), style = Stroke(width = 3f))
            drawLine(color = Color(0xFF34D399), start = Offset(160f, 185f), end = Offset(145f, 200f), strokeWidth = 3f, cap = StrokeCap.Round)
        }

        "hidden" -> {
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
        }

        else -> {
            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(175f, 175f))
            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(225f, 175f))
            val fallbackBeak = Path().apply {
                moveTo(184f, 192f)
                quadraticTo(200f, 205f, 216f, 192f)
                close()
            }
            drawPath(fallbackBeak, brush = beakBrush)
        }
    }
}

private fun DrawScope.drawGadgetLayer(gadget: String, bellRingAnim: Float) {
    when (gadget) {
        "stars" -> {
            drawCircle(color = Color(0xFFFBBF24), radius = 5f, center = Offset(85f, 115f))
            drawCircle(color = Color(0xFFFBBF24), radius = 5f, center = Offset(315f, 115f))
            drawCircle(color = Color(0xFFFBBF24), radius = 7f, center = Offset(200f, 55f))
        }

        "pencil" -> {
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
                val tipPath = Path().apply {
                    moveTo(0f, 48f)
                    lineTo(18f, 48f)
                    lineTo(9f, 62f)
                    close()
                }
                drawPath(tipPath, color = Color(0xFFD97706))
                val leadPath = Path().apply {
                    moveTo(6f, 57f)
                    lineTo(12f, 57f)
                    lineTo(9f, 62f)
                    close()
                }
                drawPath(leadPath, color = Color(0xFF0F172A))
                drawRect(color = Color(0xFF94A3B8), topLeft = Offset(0f, 0f), size = Size(18f, 7f))
                val eraserPath = Path().apply {
                    moveTo(0f, 0f)
                    quadraticTo(9f, -10f, 18f, 0f)
                    close()
                }
                drawPath(eraserPath, color = Color(0xFFF43F5E))
            }
        }

        "globe-native", "globe-target" -> {
            withTransform({
                translate(200f, 242f)
                scale(0.5f, 0.5f, pivot = Offset.Zero)
            }) {
                drawCircle(color = Color(0xFF34D399), radius = 23f, center = Offset.Zero)
                drawCircle(
                    color = Color.White.copy(alpha = 0.4f),
                    radius = 23f,
                    center = Offset.Zero,
                    style = Stroke(width = 1.5f)
                )
                val pinColor = if (gadget == "globe-native") Color(0xFF4C1D95) else Color(0xFFF43F5E)
                val pinPath = Path().apply {
                    moveTo(0f, -4f)
                    cubicTo(-6f, -16f, 6f, -16f, 0f, -4f)
                    close()
                }
                drawPath(pinPath, color = pinColor)
                drawCircle(color = Color.White, radius = 1.5f, center = Offset(0f, -10f))
            }
        }

        "timer" -> {
            withTransform({
                translate(180f, 220f)
            }) {
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
                val sandTop = Path().apply {
                    moveTo(8f, 5f)
                    lineTo(32f, 5f)
                    lineTo(20f, 17f)
                    close()
                }
                drawPath(sandTop, color = Color(0xFFFBBF24))
                val sandBottom = Path().apply {
                    moveTo(17f, 17f)
                    lineTo(30f, 30f)
                    lineTo(10f, 30f)
                    close()
                }
                drawPath(sandBottom, color = Color(0xFFFBBF24))
                drawRect(color = Color(0xFF0F172A), topLeft = Offset(0f, 0f), size = Size(40f, 4f))
                drawRect(color = Color(0xFF0F172A), topLeft = Offset(0f, 30f), size = Size(40f, 4f))
            }
        }

        "bell" -> {
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
            }
        }

        "placement" -> {
            val capBoard = Path().apply {
                moveTo(200f, 85f)
                lineTo(225f, 95f)
                lineTo(200f, 105f)
                lineTo(175f, 95f)
                close()
            }
            drawPath(capBoard, color = Color(0xFFFBBF24))
            val capBase = Path().apply {
                moveTo(185f, 99f)
                lineTo(185f, 109f)
                cubicTo(185f, 115f, 215f, 115f, 215f, 109f)
                lineTo(215f, 99f)
                close()
            }
            drawPath(capBase, color = Color(0xFFFBBF24).copy(alpha = 0.8f))
            drawLine(color = Color(0xFFFBBF24), start = Offset(225f, 95f), end = Offset(225f, 110f), strokeWidth = 2f)
            drawCircle(color = Color(0xFFFBBF24), radius = 2f, center = Offset(225f, 112f))
        }

        "lock" -> {
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
                val shackle = Path().apply {
                    moveTo(6f, 10f)
                    lineTo(6f, 4f)
                    cubicTo(6f, -3f, 26f, -3f, 26f, 4f)
                    lineTo(26f, 10f)
                }
                drawPath(shackle, color = Color(0xFFF43F5E), style = Stroke(width = 3f))
                drawCircle(color = Color(0xFFF43F5E), radius = 3f, center = Offset(16f, 21f))
            }
        }

        "search-data" -> {
            drawLine(color = Color(0xFF34D399), start = Offset(60f, 130f), end = Offset(80f, 130f), strokeWidth = 2f, cap = StrokeCap.Round)
            drawLine(color = Color(0xFF34D399), start = Offset(65f, 140f), end = Offset(85f, 140f), strokeWidth = 2f, cap = StrokeCap.Round)
            drawLine(color = Color(0xFF34D399), start = Offset(320f, 130f), end = Offset(340f, 130f), strokeWidth = 2f, cap = StrokeCap.Round)
            drawLine(color = Color(0xFF34D399), start = Offset(315f, 140f), end = Offset(335f, 140f), strokeWidth = 2f, cap = StrokeCap.Round)
        }
    }
}
