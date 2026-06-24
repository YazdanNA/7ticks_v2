package com.example.features.smartlearn.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.core.components.GlassCard
import com.example.core.components.PremiumGlassButton
import com.example.core.components.SevenCircles
import com.example.core.components.TikiPlaceholder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningSessionScreen(navController: NavController) {
    var isFlipped by remember { mutableStateOf(false) }
    var currentCardIndex by remember { mutableStateOf(0) }

    // Multi-card list for cyclic showcase
    val mockCards = listOf(
        FlashcardData(
            word = "Serendipity",
            phonetic = "/ˌserənˈdipədē/",
            partOfSpeech = "Noun",
            meaning = "The occurrence of events by chance in a happy or beneficial way.",
            meaningFa = "اتفاق خوشایند و غیر منتظره",
            example = "We found the charming little cafe by pure serendipity."
        ),
        FlashcardData(
            word = "Eloquent",
            phonetic = "/ˈeləkwənt/",
            partOfSpeech = "Adjective",
            meaning = "Fluent or persuasive in speaking or writing.",
            meaningFa = "سخنور و فصیح",
            example = "An eloquent speech that moved the entire audience to tears."
        ),
        FlashcardData(
            word = "Ephemeral",
            phonetic = "/əˈfemərəl/",
            partOfSpeech = "Adjective",
            meaning = "Lasting for a very short time.",
            meaningFa = "گذرا و زودگذر",
            example = "The beauty of autumn leaves is ephemeral but unforgettable."
        )
    )

    val currentCard = mockCards[currentCardIndex % mockCards.size]

    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF070814),
            Color(0xFF0A1033),
            Color(0xFF1B072E)
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Smart Learn",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Help context */ }) {
                        Icon(Icons.Default.Info, contentDescription = "Help", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Session Progress Indicators
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Session Progress",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${(currentCardIndex % mockCards.size) + 1} / ${mockCards.size}",
                        color = Color(0xFF00FFD2),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { ((currentCardIndex % mockCards.size) + 1).toFloat() / mockCards.size.toFloat() },
                    color = Color(0xFF00C2FF),
                    trackColor = Color(0x1AFFFFFF),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
            }

            // 2. Interactive Flashcard (Glass panel wrapper)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = isFlipped,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                    },
                    label = "card_flip"
                ) { flipped ->
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .clickable { isFlipped = !isFlipped },
                        cornerRadius = 28.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (!flipped) {
                                // --- FRONT SIDE ---
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0x2200C2FF))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = currentCard.partOfSpeech,
                                            color = Color(0xFF00FFD2),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = "Favorite",
                                        tint = Color.White.copy(alpha = 0.3f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                ) {
                                    Text(
                                        text = currentCard.word,
                                        color = Color.White,
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Black,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = currentCard.phonetic,
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Text(
                                    text = "Tap Card to Reveal Meaning",
                                    color = Color(0xFF00C2FF),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.animateContentSize()
                                )

                            } else {
                                // --- BACK SIDE ---
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = currentCard.word,
                                        color = Color(0xFF00C2FF),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(onClick = { /* Audio Pronounce placeholder */ }) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Speak", tint = Color(0xFF00FFD2))
                                    }
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 12.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    // Foreign translation
                                    Text(
                                        text = currentCard.meaningFa,
                                        color = Color(0xFFFFD600),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )
                                    
                                    // English definition
                                    Text(
                                        text = currentCard.meaning,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 22.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Example Sentence
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0x0AFFFFFF))
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = "Example:\n\"${currentCard.example}\"",
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }

                                Text(
                                    text = "Tap to show original card face",
                                    color = Color.White.copy(alpha = 0.3f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // 3. Seven Circles Component Integrated Preview
            SevenCircles(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                activeStates = listOf("Red", "Yellow", "Blue", "Green", "Blue", "Yellow", "Green")
            )

            // 4. Tiki Mascot Helper area
            TikiPlaceholder(
                message = if (!isFlipped) "Look at the word first. Try to recall before flipping!" else "Be honest! Select Again if you forgot, or Good/Easy to progress.",
                sizeDp = 50,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 5. Spaced Repetition (Again, Hard, Good, Easy) Action Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // AGAIN Button (Red)
                ActionButton(
                    text = "Again",
                    subtext = "<1m",
                    color = Color(0xFFFF1744),
                    modifier = Modifier.weight(1f)
                ) {
                    isFlipped = false
                    currentCardIndex++
                }

                // HARD Button (Yellow)
                ActionButton(
                    text = "Hard",
                    subtext = "<10m",
                    color = Color(0xFFFFD600),
                    modifier = Modifier.weight(1f)
                ) {
                    isFlipped = false
                    currentCardIndex++
                }

                // GOOD Button (Blue)
                ActionButton(
                    text = "Good",
                    subtext = "1d",
                    color = Color(0xFF2979FF),
                    modifier = Modifier.weight(1f)
                ) {
                    isFlipped = false
                    currentCardIndex++
                }

                // EASY Button (Green)
                ActionButton(
                    text = "Easy",
                    subtext = "4d",
                    color = Color(0xFF00E676),
                    modifier = Modifier.weight(1f)
                ) {
                    isFlipped = false
                    currentCardIndex++
                }
            }
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    subtext: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.12f))
            .border(width = 1.dp, color = color.copy(alpha = 0.4f), shape = RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = text,
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp
            )
        }
    }
}

data class FlashcardData(
    val word: String,
    val phonetic: String,
    val partOfSpeech: String,
    val meaning: String,
    val meaningFa: String,
    val example: String
)
