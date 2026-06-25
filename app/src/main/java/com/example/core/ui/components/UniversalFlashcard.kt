package com.example.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.components.SevenCircles
import com.example.core.database.CardEntity
import com.example.core.database.DictWord
import com.example.core.database.BoxWordEntity
import com.example.features.smartlearn.presentation.getWordDetails
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Unified data representation for UniversalFlashcard.
 */
data class FlashcardData(
    val word: String,
    val phonetics: String,
    val partOfSpeech: String,
    val primaryDefinition: String,
    val primaryExample: String,
    val translation: String,
    val definitionsList: List<String>,
    val examplesList: List<String>,
    val synonyms: List<String> = emptyList(),
    val antonyms: List<String> = emptyList(),
    val wordFamily: List<String> = emptyList(),
    val notes: String = ""
)

/**
 * Extension mapper for DictWord (Smart Learn model)
 */
fun Pair<CardEntity, DictWord>.toFlashcardData(): FlashcardData {
    val wordObj = this.second
    val details = getWordDetails(wordObj.word)
    return FlashcardData(
        word = wordObj.word,
        phonetics = wordObj.phonetics,
        partOfSpeech = wordObj.partOfSpeech,
        primaryDefinition = wordObj.definition,
        primaryExample = wordObj.example,
        translation = wordObj.faDefinition,
        definitionsList = listOf(wordObj.definition),
        examplesList = if (wordObj.example.isNotEmpty()) listOf(wordObj.example) else emptyList(),
        synonyms = details.synonyms,
        antonyms = details.antonyms,
        wordFamily = details.wordFamily,
        notes = details.notes
    )
}

/**
 * Extension mapper for BoxWordEntity (Custom Boxes model)
 */
fun BoxWordEntity.toFlashcardData(): FlashcardData {
    val rawDefs = this.definitions.split("\n").filter { it.isNotBlank() }
    val rawExs = this.examples.split("\n").filter { it.isNotBlank() }
    val primaryDef = rawDefs.firstOrNull() ?: ""
    val primaryEx = rawExs.firstOrNull() ?: ""
    val farsi = this.meanings.split("\n").filter { it.isNotBlank() }.firstOrNull() ?: ""
    
    return FlashcardData(
        word = this.word,
        phonetics = when {
            this.phoneticsUs != null && this.phoneticsUk != null -> "US: ${this.phoneticsUs} / UK: ${this.phoneticsUk}"
            this.phoneticsUs != null -> "US: ${this.phoneticsUs}"
            this.phoneticsUk != null -> "UK: ${this.phoneticsUk}"
            else -> ""
        },
        partOfSpeech = this.type,
        primaryDefinition = primaryDef,
        primaryExample = primaryEx,
        translation = farsi,
        definitionsList = rawDefs,
        examplesList = rawExs,
        synonyms = emptyList(),
        antonyms = emptyList(),
        wordFamily = emptyList(),
        notes = ""
    )
}

/**
 * Bouncy ActionButton shared across review experiences.
 */
@Composable
fun FlashcardActionButton(
    text: String,
    subtext: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "flashcard_button_bounce"
    )
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .scale(scale)
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable {
                coroutineScope.launch {
                    isPressed = true
                    delay(80)
                    isPressed = false
                }
                onClick()
            }
            .padding(horizontal = 4.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = text,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            if (subtext.isNotEmpty()) {
                Text(
                    text = subtext,
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 9.sp
                )
            }
        }
    }
}

/**
 * Universal Flashcard Component.
 * Implements standard 3D flipping, glass styling, progress circles, and rating buttons.
 */
@Composable
fun UniversalFlashcard(
    data: FlashcardData,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier,
    circleStates: List<String>? = null,
    onAgainClick: (() -> Unit)? = null,
    onHardClick: (() -> Unit)? = null,
    onGoodClick: (() -> Unit)? = null,
    onEasyClick: (() -> Unit)? = null,
    againSubtext: String = "<1m",
    hardSubtext: String = "<10m",
    goodSubtext: String = "1d",
    easySubtext: String = "4d"
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "universal_flashcard_rotation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .graphicsLayer {
                rotationY = rotationAngle
                cameraDistance = 14f * density
            }
            .clickable {
                onFlip()
            }
    ) {
        if (rotationAngle <= 90f) {
            // --- FRONT SIDE ---
            SharedGlassCard(
                modifier = Modifier.fillMaxSize(),
                cornerRadius = 24.dp,
                depth = 1
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header tag row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x1A00C2FF))
                                .border(1.dp, Color(0x3300C2FF), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = data.partOfSpeech.uppercase(),
                                color = Color(0xFF00FFD2),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Active Flashcard",
                            tint = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Main Word & Phonetics
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = data.word,
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        if (data.phonetics.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = data.phonetics,
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Meaning and Examples
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (data.primaryDefinition.isNotEmpty()) {
                            Text(
                                text = data.primaryDefinition,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (data.primaryExample.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x0AFFFFFF))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "\"${data.primaryExample}\"",
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

                    // Hint indicator at bottom
                    Text(
                        text = "Tap Card to Reveal Translations & Details",
                        color = Color(0xFF00C2FF).copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            // --- BACK SIDE (Mirrored visually so it reads correctly) ---
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
                    depth = 1
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Word label and Pronounce button
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = data.word,
                                color = Color(0xFF00C2FF),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { onFlip() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Pronounce",
                                    tint = Color(0xFF00FFD2)
                                )
                            }
                        }

                        // Scrollable Translation, definition and more details content
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Primary translation (glowing Persian/Farsi word)
                            if (data.translation.isNotEmpty()) {
                                Text(
                                    text = data.translation,
                                    color = Color(0xFFFFD600),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)
                                )
                            }

                            // Secondary definitions list
                            if (data.definitionsList.isNotEmpty()) {
                                Text(
                                    text = "Definitions",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                data.definitionsList.forEach { def ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("•", color = Color(0xFF00C2FF))
                                        Text(def, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                                    }
                                }
                            }

                            // Examples list
                            if (data.examplesList.isNotEmpty()) {
                                Text(
                                    text = "Examples",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                data.examplesList.forEach { ex ->
                                    Text(
                                        text = "\"$ex\"",
                                        color = Color.White.copy(alpha = 0.65f),
                                        fontStyle = FontStyle.Italic,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Linguistic specifics (Synonyms / Antonyms / Word Family)
                            if (data.synonyms.isNotEmpty()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Synonyms:", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(data.synonyms.joinToString(", "), color = Color(0xFF00FFD2), fontSize = 11.sp)
                                }
                            }
                            if (data.antonyms.isNotEmpty()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Antonyms:", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(data.antonyms.joinToString(", "), color = Color(0xFFFF1744), fontSize = 11.sp)
                                }
                            }
                            if (data.wordFamily.isNotEmpty()) {
                                Text("Word Family", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                data.wordFamily.forEach { family ->
                                    Text("• $family", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                }
                            }
                            if (data.notes.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x08FFFFFF))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "Tiki's Note: ${data.notes}",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 11.sp,
                                        fontStyle = FontStyle.Italic
                                    )
                                }
                            }
                        }

                        // Progress Ticks system integration
                        if (circleStates != null) {
                            SevenCircles(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                activeStates = circleStates
                            )
                        }

                        // Built-in Rating Actions inside the card
                        if (onAgainClick != null || onHardClick != null || onGoodClick != null || onEasyClick != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (onAgainClick != null) {
                                    FlashcardActionButton(
                                        text = "Again",
                                        subtext = againSubtext,
                                        color = Color(0xFFFF1744),
                                        modifier = Modifier.weight(1f),
                                        onClick = onAgainClick
                                    )
                                }
                                if (onHardClick != null) {
                                    FlashcardActionButton(
                                        text = "Hard",
                                        subtext = hardSubtext,
                                        color = Color(0xFFFFD600),
                                        modifier = Modifier.weight(1f),
                                        onClick = onHardClick
                                    )
                                }
                                if (onGoodClick != null) {
                                    FlashcardActionButton(
                                        text = "Good",
                                        subtext = goodSubtext,
                                        color = Color(0xFF2979FF),
                                        modifier = Modifier.weight(1.1f),
                                        onClick = onGoodClick
                                    )
                                }
                                if (onEasyClick != null) {
                                    FlashcardActionButton(
                                        text = "Easy",
                                        subtext = easySubtext,
                                        color = Color(0xFF00E676),
                                        modifier = Modifier.weight(1f),
                                        onClick = onEasyClick
                                    )
                                }
                            }
                        }

                        // Hint indicator at bottom
                        Text(
                            text = "Tap Header/Pronounce to Flip",
                            color = Color.White.copy(alpha = 0.25f),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
