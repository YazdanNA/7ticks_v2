package com.example.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.components.SevenCircles
import com.example.core.database.CardEntity
import com.example.core.database.DictWord
import com.example.core.database.BoxWordEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Unified data representation for UniversalFlashcard.
 */
data class FlashcardData(
    val word: String,
    val phonetics: String,
    val phoneticsUs: String,
    val phoneticsUk: String,
    val partOfSpeech: String,
    val primaryDefinition: String,
    val primaryExample: String,
    val translation: String,
    val definitionsList: List<String>,
    val examplesList: List<String>,
    val translationsList: List<String>,
    val examplesFaList: List<String>,
    val synonyms: List<String> = emptyList(),
    val antonyms: List<String> = emptyList(),
    val wordFamily: List<String> = emptyList(),
    val collocations: List<String> = emptyList(),
    val phrases: List<String> = emptyList(),
    val notes: List<String> = emptyList(),
    val senseId: String = "1",
    val label: String = "",
    val topic: String = "",
    val level: String = ""
)

/**
 * Extension mapper for DictWord (Smart Learn model)
 */
fun Pair<CardEntity, DictWord>.toFlashcardData(): FlashcardData {
    val wordObj = this.second
    return FlashcardData(
        word = wordObj.word,
        phonetics = wordObj.phonetics,
        phoneticsUs = wordObj.phonetics_us,
        phoneticsUk = wordObj.phonetics_uk,
        partOfSpeech = wordObj.partOfSpeech,
        primaryDefinition = wordObj.definition_en.ifEmpty { wordObj.definition },
        primaryExample = wordObj.example.ifEmpty { wordObj.examples_en.firstOrNull() ?: "" },
        translation = wordObj.definition_fa.ifEmpty { wordObj.faDefinition },
        definitionsList = wordObj.definitions_en.ifEmpty { listOf(wordObj.definition_en.ifEmpty { wordObj.definition }) },
        examplesList = wordObj.examples_en.ifEmpty { if (wordObj.example.isNotEmpty()) listOf(wordObj.example) else emptyList() },
        translationsList = wordObj.translations,
        examplesFaList = wordObj.examples_fa,
        synonyms = wordObj.synonyms,
        antonyms = wordObj.antonyms,
        wordFamily = wordObj.word_family,
        collocations = wordObj.collocations,
        phrases = wordObj.phrases,
        notes = wordObj.notes,
        senseId = wordObj.sense_id,
        label = wordObj.label,
        topic = wordObj.topic,
        level = wordObj.level
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
        phoneticsUs = this.phoneticsUs ?: "",
        phoneticsUk = this.phoneticsUk ?: "",
        partOfSpeech = this.type,
        primaryDefinition = primaryDef,
        primaryExample = primaryEx,
        translation = farsi,
        definitionsList = rawDefs,
        examplesList = rawExs,
        translationsList = this.meanings.split("\n").filter { it.isNotBlank() },
        examplesFaList = emptyList(),
        synonyms = if (this.synonyms.isNotEmpty()) this.synonyms.split(",").map { it.trim() } else emptyList(),
        antonyms = if (this.antonyms.isNotEmpty()) this.antonyms.split(",").map { it.trim() } else emptyList(),
        wordFamily = if (this.wordFamily.isNotEmpty()) this.wordFamily.split(",").map { it.trim() } else emptyList(),
        collocations = emptyList(),
        phrases = emptyList(),
        notes = emptyList(),
        senseId = "1",
        label = "General",
        topic = this.topic,
        level = this.level
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
    easySubtext: String = "4d",
    onMoreDetailsClick: () -> Unit = {},
    onPronounceClick: (text: String, isMale: Boolean) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val rotationAngle by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "universal_flashcard_rotation"
    )

    // State to toggle translation display on the back side
    var showTranslation by remember { mutableStateOf(false) }

    val feedbackManager = remember { com.example.core.feedback.FeedbackManager.getInstance(context) }
    val isPronouncing by feedbackManager.isPronunciationActive.collectAsState()
    val spokenText by feedbackManager.spokenText.collectAsState()
    val spokenTextRange by feedbackManager.spokenTextRange.collectAsState()

    val highlightColor by animateColorAsState(
        targetValue = if (isPronouncing) Color(0xFF00FFD2) else Color(0x1A00FFD2),
        animationSpec = tween(250),
        label = "pronounce_button_bg"
    )
    val highlightBorderColor by animateColorAsState(
        targetValue = if (isPronouncing) Color.White else Color(0x3300FFD2),
        animationSpec = tween(250),
        label = "pronounce_button_border"
    )
    val highlightScale by animateFloatAsState(
        targetValue = if (isPronouncing) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium),
        label = "pronounce_button_scale"
    )
    val wordTextColor by animateColorAsState(
        targetValue = if (isPronouncing) Color(0xFF00FFD2) else Color.White,
        animationSpec = tween(250),
        label = "pronounce_word_color"
    )

    // Keep translation OFF by default when card changes or is flipped back
    LaunchedEffect(data.word) {
        showTranslation = false
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .graphicsLayer {
                rotationY = rotationAngle
                cameraDistance = 14f * density
            }
    ) {
        if (rotationAngle <= 90f) {
            // --- FRONT SIDE ---
            SharedGlassCard(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onFlip() },
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
                    // Header Tag Row (Part of Speech Badge)
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

                    // Main Word & US / UK Phonetics
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = data.word,
                            color = wordTextColor,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        
                        // Separate US and UK Phonetics display
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val usText = data.phoneticsUs.ifEmpty { data.phonetics }
                            if (usText.isNotEmpty()) {
                                Text(
                                    text = "US: $usText",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                            if (data.phoneticsUk.isNotEmpty()) {
                                Text(
                                    text = "UK: ${data.phoneticsUk}",
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Front Card Pronunciation Button (Pronounces Word - Male Voice)
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .graphicsLayer {
                                    scaleX = highlightScale
                                    scaleY = highlightScale
                                }
                                .clip(CircleShape)
                                .background(highlightColor)
                                .border(2.dp, highlightBorderColor, CircleShape)
                                .clickable {
                                    // Speak Word (Male voice)
                                    onPronounceClick(data.word, true)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Pronounce Word",
                                tint = if (isPronouncing) Color.Black else Color(0xFF00FFD2),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // Touch Indicator Hint
                    Text(
                        text = "Tap card to reveal",
                        color = Color(0xFF00C2FF).copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
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
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onFlip() },
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
                        // Header word display & back flip hint
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = data.word,
                                    color = Color(0xFF00C2FF),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (showTranslation && data.translationsList.isNotEmpty()) {
                                    Text(
                                        text = "(${data.translationsList.first()})",
                                        color = Color(0xFFFFD600),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x1A00C2FF))
                                    .clickable { onFlip() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Flip",
                                    color = Color(0xFF00C2FF),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Scrollable Content
                        Column(
                            modifier = Modifier
                                .weight(1.2f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. Definition (English) with a Play Pronounce Button (Male Voice)
                            if (data.primaryDefinition.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Definition",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        // Pronounce Definition Button (Male)
                                        IconButton(
                                            onClick = { onPronounceClick(data.primaryDefinition, true) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Pronounce Definition",
                                                tint = Color(0xFF00C2FF),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    HighlightableText(
                                        text = data.primaryDefinition,
                                        spokenText = spokenText,
                                        range = spokenTextRange,
                                        baseColor = Color.White,
                                        highlightColor = Color(0xFF00FFD2),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 18.sp
                                    )
                                    if (showTranslation && data.translation.isNotEmpty()) {
                                        Text(
                                            text = data.translation,
                                            color = Color(0xFFFFD600),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Right,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 4.dp)
                                        )
                                    }
                                }
                            }

                            // 2. Example(s) (English) with female voice pronunciation play button
                            if (data.examplesList.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Examples",
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    data.examplesList.forEachIndexed { idx, ex ->
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                HighlightableText(
                                                    text = ex,
                                                    spokenText = spokenText,
                                                    range = spokenTextRange,
                                                    baseColor = Color.White.copy(alpha = 0.8f),
                                                    highlightColor = Color(0xFF00FFD2),
                                                    fontStyle = FontStyle.Italic,
                                                    fontSize = 13.sp,
                                                    lineHeight = 16.sp,
                                                    modifier = Modifier.weight(1f).padding(end = 4.dp),
                                                    isExampleWithQuotes = true
                                                )
                                                // Pronounce Example Button (Female voice)
                                                IconButton(
                                                    onClick = { onPronounceClick(ex, false) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PlayArrow,
                                                        contentDescription = "Pronounce Example",
                                                        tint = Color(0xFF00FFD2),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            if (showTranslation) {
                                                val exFa = data.examplesFaList.getOrNull(idx)
                                                if (exFa != null && exFa.isNotEmpty()) {
                                                    Text(
                                                        text = exFa,
                                                        color = Color.White.copy(alpha = 0.6f),
                                                        fontSize = 12.sp,
                                                        textAlign = TextAlign.Right,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(top = 2.dp, bottom = 4.dp, end = 28.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Progress Ticks
                        if (circleStates != null) {
                            SevenCircles(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp, bottom = 2.dp),
                                activeStates = circleStates
                            )
                        }

                        // Built-in Rating Actions inside card if specified
                        if (onAgainClick != null || onHardClick != null || onGoodClick != null || onEasyClick != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
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

                        // Bottom Actions (Translate and More Details)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Translation Toggle Button
                            Button(
                                onClick = { showTranslation = !showTranslation },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (showTranslation) Color(0x3300FFD2) else Color(0x12FFFFFF),
                                    contentColor = if (showTranslation) Color(0xFF00FFD2) else Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text(
                                    text = if (showTranslation) "Hide Translation" else "Translation",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // More Details Screen Modal Sheet Opener
                            Button(
                                onClick = { onMoreDetailsClick() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0x12FFFFFF),
                                    contentColor = Color(0xFF00C2FF)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text(
                                    text = "More Details",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HighlightableText(
    text: String,
    spokenText: String,
    range: Pair<Int, Int>?,
    baseColor: Color,
    highlightColor: Color,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Medium,
    lineHeight: TextUnit = 18.sp,
    fontStyle: FontStyle? = null,
    isExampleWithQuotes: Boolean = false
) {
    val annotatedString = remember(text, spokenText, range, isExampleWithQuotes) {
        buildAnnotatedString {
            val isCurrentSpoken = spokenText.trim().equals(text.trim(), ignoreCase = true)
            
            if (isExampleWithQuotes) {
                append("\"")
            }
            
            if (isCurrentSpoken && range != null && range.first >= 0 && range.second <= text.length && range.first < range.second) {
                // Pre-range
                if (range.first > 0) {
                    append(text.substring(0, range.first))
                }
                // Highlight range
                withStyle(style = SpanStyle(color = highlightColor, fontWeight = FontWeight.Bold)) {
                    append(text.substring(range.first, range.second))
                }
                // Post-range
                if (range.second < text.length) {
                    append(text.substring(range.second))
                }
            } else {
                append(text)
            }
            
            if (isExampleWithQuotes) {
                append("\"")
            }
        }
    }

    Text(
        text = annotatedString,
        color = baseColor,
        fontSize = fontSize,
        fontWeight = fontWeight,
        lineHeight = lineHeight,
        fontStyle = fontStyle,
        modifier = modifier
    )
}
