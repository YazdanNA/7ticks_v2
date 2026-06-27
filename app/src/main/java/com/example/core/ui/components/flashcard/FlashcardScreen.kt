package com.example.core.ui.components.flashcard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.core.database.WordDetails
import com.example.core.fsrs.ReviewRatingModel
import com.example.core.ui.components.TickyCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(
    wordDetails: WordDetails,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onRatingClick: (ReviewRatingModel) -> Unit,
    modifier: Modifier = Modifier,
    circleStates: List<String>? = null,
    againSubtext: String = "<1m",
    hardSubtext: String = "<10m",
    goodSubtext: String = "1d",
    easySubtext: String = "4d",
    tikiMessage: String = "Let's learn this word!",
    tikiState: String = "st-happy",
    progressContent: @Composable (ColumnScope.() -> Unit)? = null
) {
    val context = LocalContext.current
    val audioController = remember { FlashcardAudioController(context) }
    var showMoreDetails by remember { mutableStateOf(false) }

    // Sequential Auto-Pronunciation when card flips to back
    LaunchedEffect(wordDetails, isFlipped) {
        if (!isFlipped) {
            audioController.stop()
            audioController.speakWord(wordDetails.word)
        } else {
            audioController.stop()
            
            // 1. Definition (English) -> Male voice
            val definition = wordDetails.definitions.firstOrNull() ?: ""
            if (definition.isNotEmpty()) {
                audioController.speakSuspend(definition, isMale = true)
            }
            
            // 2. Examples -> Female voice
            wordDetails.examples.forEach { example ->
                if (example.isNotEmpty()) {
                    audioController.speakSuspend(example, isMale = false)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            audioController.shutdown()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Progress or Custom Header
        progressContent?.invoke(this)

        Spacer(modifier = Modifier.height(12.dp))

        // FlashCardWidget from shared components
        val flashCardState = remember(wordDetails, isFlipped, circleStates) {
            FlashCardState(
                data = com.example.core.ui.components.FlashcardData(
                    word = wordDetails.word,
                    phonetics = wordDetails.phonetics,
                    phoneticsUs = wordDetails.phoneticsUs,
                    phoneticsUk = wordDetails.phoneticsUk,
                    partOfSpeech = wordDetails.type,
                    primaryDefinition = wordDetails.definitions.firstOrNull() ?: "",
                    primaryExample = wordDetails.examples.firstOrNull() ?: "",
                    translation = wordDetails.translations.firstOrNull() ?: "",
                    definitionsList = wordDetails.definitions,
                    examplesList = wordDetails.examples,
                    translationsList = wordDetails.translations,
                    examplesFaList = wordDetails.exampleTranslations,
                    synonyms = wordDetails.synonyms,
                    antonyms = wordDetails.antonyms,
                    wordFamily = wordDetails.wordFamily,
                    collocations = wordDetails.collocations,
                    phrases = wordDetails.phrases,
                    notes = wordDetails.notes,
                    senseId = "1",
                    label = wordDetails.type,
                    topic = wordDetails.topic,
                    level = wordDetails.level
                ),
                isFlipped = isFlipped,
                circleStates = circleStates
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            FlashCardWidget(
                state = flashCardState,
                onFlip = {
                    audioController.stop()
                    onFlip()
                },
                onAgainClick = {
                    audioController.stop()
                    onRatingClick(ReviewRatingModel.AGAIN)
                },
                onHardClick = {
                    audioController.stop()
                    onRatingClick(ReviewRatingModel.HARD)
                },
                onGoodClick = {
                    audioController.stop()
                    onRatingClick(ReviewRatingModel.GOOD)
                },
                onEasyClick = {
                    audioController.stop()
                    onRatingClick(ReviewRatingModel.EASY)
                },
                againSubtext = againSubtext,
                hardSubtext = hardSubtext,
                goodSubtext = goodSubtext,
                easySubtext = easySubtext,
                onMoreDetailsClick = { showMoreDetails = true },
                onPronounceClick = { text, isMale ->
                    if (isMale) {
                        audioController.speakWord(text)
                    } else {
                        audioController.speakExample(text)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Mascot
        TickyCard(
            message = tikiMessage,
            tikiState = tikiState,
            sizeDp = 50,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }

    if (showMoreDetails) {
        SharedMoreDetailsDialog(
            wordDetails = wordDetails,
            onDismiss = { showMoreDetails = false }
        )
    }
}
