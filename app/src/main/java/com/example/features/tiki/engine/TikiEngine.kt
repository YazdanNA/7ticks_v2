package com.example.features.tiki.engine

import com.example.features.tiki.repository.DialogueRepository
import com.example.features.tiki.repository.EmotionAssetRepository
import com.example.features.tiki.context.ContextEngine
import com.example.features.tiki.context.ContextEvent
import com.example.features.tiki.memory.MemoryEngine
import com.example.features.tiki.behavior.BehaviorEngine
import com.example.features.tiki.behavior.BehaviorEvent
import com.example.features.tiki.dialogue.DialogueEngine
import com.example.features.tiki.dialogue.DialogueContext
import com.example.features.tiki.relationship.RelationshipEngine
import com.example.features.tiki.relationship.RelationshipEvent
import com.example.features.tiki.ambient.AmbientEngine
import com.example.features.tiki.director.DirectorEngine
import com.example.features.tiki.director.DirectorDecision
import com.example.features.tiki.director.DirectorPriority
import com.example.features.tiki.model.EmotionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class TikiEngine private constructor() {

    val dialogueRepository = DialogueRepository()
    val assetRepository = EmotionAssetRepository()
    val transitionManager = EmotionTransitionManager(CoroutineScope(Dispatchers.Main.immediate))
    val emotionEngine = EmotionEngine(dialogueRepository, assetRepository, transitionManager)
    val directorEngine = DirectorEngine.getInstance()
    val contentEngine = com.example.features.tiki.content.ContentEngine()

    val contextEngine = ContextEngine.getInstance()
    val memoryEngine = MemoryEngine.getInstance()
    val behaviorEngine = BehaviorEngine.getInstance()
    val dialogueEngine = DialogueEngine.getInstance()
    val relationshipEngine = RelationshipEngine.getInstance()
    val ambientEngine = AmbientEngine()

    init {
        loadDialogues()
    }

    private fun loadDialogues() {
        val languages = listOf("en", "fa", "fr", "de")
        val appInstance = com.example.SevenTicksApplication.instance
        val loader = com.example.features.tiki.content.DialogueLoader()
        for (lang in languages) {
            try {
                val stream = appInstance.assets.open("tiki_dialogues/dialogues_$lang.json")
                val library = loader.loadFromStream(stream)
                contentEngine.loadLibrary(library)
                android.util.Log.d("TikiEngine", "Successfully loaded dialogue library for language: $lang")
            } catch (e: Exception) {
                android.util.Log.e("TikiEngine", "Failed to load dialogue library for language: $lang", e)
            }
        }
    }

    /**
     * Executes the Tiki V2 pipeline for a given user interaction.
     * Expected Runtime Pipeline:
     * User Event -> Context Engine -> Memory Engine -> Behavior Engine -> Emotion Engine -> Dialogue Engine -> Relationship Engine -> Ambient Engine -> Director Engine -> Renderer
     */
    fun triggerPipeline(
        contextEvent: ContextEvent,
        behaviorEvent: BehaviorEvent? = null,
        relationshipEvent: RelationshipEvent? = null
    ) {
        val currentTime = System.currentTimeMillis()

        // 1. Context Engine
        val contextRecommendation = contextEngine.onEvent(contextEvent, currentTime)

        // 2. Memory Engine
        if (behaviorEvent != null) {
            memoryEngine.processEvent(behaviorEvent, currentTime)
        }
        val memorySnapshot = memoryEngine.getSnapshot(currentTime)

        // 3. Behavior Engine
        val behaviorResult = if (behaviorEvent != null) {
            behaviorEngine.processEvent(behaviorEvent, currentTime)
        } else {
            null
        }

        // 4. Emotion Engine (initial guess based on behavior/context)
        val initialEmotion = behaviorResult?.emotion 
            ?: when (contextRecommendation?.suggestedDialogueCategory) {
                "Celebration", "SessionComplete" -> EmotionState.LAUGH_BIG
                "Greeting" -> EmotionState.WELCOME
                "Encouragement", "Motivation" -> EmotionState.SMILE_BIG
                else -> EmotionState.HAPPY
            }

        // 5. Dialogue Engine / Library resolution (exclusively from Dialogue Library)
        val progress = contextEngine.getSnapshot().sessionProgress
        
        // Map behaviorEvent to standard dialogue categories based on trailing answer streak and action
        val trailingStreak = behaviorEngine.history.getTrailingAnswerStreak()
        val isEasyStreak = trailingStreak.first == BehaviorEvent.CardAnsweredEasy::class.java && trailingStreak.second >= 3
        val isAgainStreak = trailingStreak.first == BehaviorEvent.CardAnsweredAgain::class.java && trailingStreak.second >= 2

        val mappedCategory = when (behaviorEvent) {
            is BehaviorEvent.SessionStarted -> "Greeting"
            is BehaviorEvent.SessionFinished -> "SessionComplete"
            is BehaviorEvent.CardAnsweredEasy -> if (isEasyStreak) "EasyStreak" else "Easy"
            is BehaviorEvent.CardAnsweredGood -> "Good"
            is BehaviorEvent.CardAnsweredHard -> "Hard"
            is BehaviorEvent.CardAnsweredAgain -> if (isAgainStreak) "AgainStreak" else "Again"
            is BehaviorEvent.CardThinkingStarted -> "Thinking"
            is BehaviorEvent.CardThinkingFinished -> if (behaviorEvent.durationMillis >= 8000L) "LongThinking" else "Thinking"
            is BehaviorEvent.TranslationOpened -> "Idle"
            is BehaviorEvent.MoreDetailsOpened -> "Idle"
            is BehaviorEvent.DictionaryOpened -> "Idle"
            is BehaviorEvent.AnalysisOpened -> "Idle"
            is BehaviorEvent.BoxesOpened -> "Idle"
            is BehaviorEvent.ProfileOpened -> "Idle"
            is BehaviorEvent.WordSearched -> "Idle"
            is BehaviorEvent.WordStarred -> "Idle"
            is BehaviorEvent.OnboardingStepChanged -> "Idle"
            else -> null
        }
        
        val categoryToResolve = contextRecommendation?.suggestedDialogueCategory ?: mappedCategory ?: "Idle"
        val relationshipLevel = relationshipEngine.getSnapshot(currentTime).level

        val langCode = getAppLanguage()

        // Multi-pass dialogue resolution:
        // Pass 1: Strict match with both category and initial emotion
        var resolvedMetadata = contentEngine.resolveDialogue(
            category = categoryToResolve,
            language = langCode,
            emotion = initialEmotion.name,
            relationshipLevel = relationshipLevel,
            currentStreak = memorySnapshot.currentStreak,
            sessionProgress = progress,
            thinkingState = if (contextEvent is ContextEvent.ThinkingFinished) "THINKING_LONG" else null,
            currentTimeMillis = currentTime
        )

        // Pass 2: Fallback matching category with emotion = null
        if (resolvedMetadata == null) {
            resolvedMetadata = contentEngine.resolveDialogue(
                category = categoryToResolve,
                language = langCode,
                emotion = null,
                relationshipLevel = relationshipLevel,
                currentStreak = memorySnapshot.currentStreak,
                sessionProgress = progress,
                thinkingState = if (contextEvent is ContextEvent.ThinkingFinished) "THINKING_LONG" else null,
                currentTimeMillis = currentTime
            )
        }

        // Pass 2.5: Map standard categories to funny/rare categories if standard search yielded no results
        if (resolvedMetadata == null) {
            val funnyCats = getMappedFunnyCategories(categoryToResolve)
            for (funnyCat in funnyCats) {
                resolvedMetadata = contentEngine.resolveDialogue(
                    category = funnyCat,
                    language = langCode,
                    emotion = null,
                    relationshipLevel = relationshipLevel,
                    currentStreak = memorySnapshot.currentStreak,
                    sessionProgress = progress,
                    thinkingState = if (contextEvent is ContextEvent.ThinkingFinished) "THINKING_LONG" else null,
                    currentTimeMillis = currentTime
                )
                if (resolvedMetadata != null) break
            }
        }

        // Pass 3: Ultimate fallback matching category = "Idle" with emotion = null
        if (resolvedMetadata == null && categoryToResolve != "Idle") {
            resolvedMetadata = contentEngine.resolveDialogue(
                category = "Idle",
                language = langCode,
                emotion = null,
                relationshipLevel = relationshipLevel,
                currentStreak = memorySnapshot.currentStreak,
                sessionProgress = progress,
                thinkingState = null,
                currentTimeMillis = currentTime
            )
        }

        // Extract the final emotion from the selected dialogue's metadata if found
        val targetEmotion = if (behaviorResult != null) {
            behaviorResult.emotion
        } else if (resolvedMetadata != null) {
            try {
                val emotionStr = when (resolvedMetadata.emotion.uppercase()) {
                    "FUNNY_LIGHT_SURPRISE" -> "SWEAT_SMILE"
                    "SOFT_HUMOR" -> "SMIRK"
                    "UNEXPECTED_PATTERN" -> "EYEBROW_RAISE"
                    "ODD_CONNECTION" -> "DIZZY"
                    "PLAYFUL_OBSERVATION" -> "WINK"
                    "RARE_BEHAVIOR_DETECTED" -> "FLUSHED"
                    "QUIRKY_INSIGHT" -> "ROFL"
                    "RARE_MOMENT_AWARENESS" -> "SMILE_HEARTS"
                    "STRANGE_CLARITY" -> "ASTONISHED"
                    "GENTLE_LAUGHTER_STATE" -> "LAUGH_BIG"
                    else -> resolvedMetadata.emotion.uppercase()
                }
                EmotionState.valueOf(emotionStr)
            } catch (e: Exception) {
                initialEmotion
            }
        } else {
            initialEmotion
        }

        val resolvedText = resolvedMetadata?.text ?: "Ready!"

        // 6. Relationship Engine
        val relationshipSnapshot = if (relationshipEvent != null) {
            relationshipEngine.onEvent(relationshipEvent, currentTime)
        } else {
            relationshipEngine.getSnapshot(currentTime)
        }

        val modifiedText = relationshipEngine.modifyDialogue(
            baseText = resolvedText,
            category = com.example.features.tiki.dialogue.DialogueCategory.Idle,
            currentTimeMillis = currentTime
        )

        // 7. Ambient Engine
        ambientEngine.onUserInteraction(currentTime)
        ambientEngine.setSpeaking(modifiedText.isNotEmpty())

        // 8. Director Engine
        val decision = if (modifiedText.isNotEmpty()) {
            DirectorDecision.SpeakAndShowEmotion(
                text = modifiedText,
                emotion = targetEmotion
            )
        } else {
            DirectorDecision.ShowEmotion(
                emotion = targetEmotion
            )
        }
        
        val finalDecision = directorEngine.submitEvent(
            decision = decision,
            priority = if (behaviorResult != null) DirectorPriority.BEHAVIOR_REACTION else DirectorPriority.AMBIENT,
            currentTimeMillis = currentTime
        ) ?: decision

        // 9. Renderer
        // Propagate the final decided visual state of Director Engine to Emotion Engine to trigger the Renderer's updates
        val finalEmotion = when (finalDecision) {
            is DirectorDecision.ShowEmotion -> finalDecision.emotion
            is DirectorDecision.SpeakAndShowEmotion -> finalDecision.emotion
            else -> targetEmotion
        }
        val finalDialogue = when (finalDecision) {
            is DirectorDecision.ShowEmotion -> modifiedText
            is DirectorDecision.SpeakAndShowEmotion -> finalDecision.text
            is DirectorDecision.PlayDialogue -> finalDecision.text
            is DirectorDecision.RemainSilent -> ""
            else -> modifiedText
        }

        emotionEngine.changeEmotion(
            emotion = finalEmotion,
            customDialogue = finalDialogue,
            forceInstant = behaviorResult?.forceInstant ?: false
        )
    }

    private fun getMappedFunnyCategories(category: String): List<String> {
        return when (category) {
            "Greeting" -> listOf("funny_first_contact")
            "SessionComplete" -> listOf("funny_success_twist", "rare_insight_moment")
            "Easy", "EasyStreak" -> listOf("funny_unexpected_win", "rare_surprise_success", "funny_chain_reaction")
            "Good" -> listOf("funny_success_twist", "rare_insight_moment")
            "Hard" -> listOf("funny_repeat_attempt", "rare_context_shift")
            "Again", "AgainStreak" -> listOf("funny_failure_light", "rare_learning_glitch")
            "Thinking" -> listOf("funny_first_contact", "rare_pattern_break")
            "LongThinking" -> listOf("funny_confusion_flip", "rare_session_anomaly")
            "Idle" -> listOf("rare_micro_joke", "rare_context_shift", "rare_behavior_detected", "rare_insight_moment")
            else -> emptyList()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: TikiEngine? = null

        fun getInstance(): TikiEngine {
            return INSTANCE ?: synchronized(this) {
                val instance = TikiEngine()
                INSTANCE = instance
                instance
            }
        }

        fun getAppLanguage(): String {
            val systemLang = java.util.Locale.getDefault().language
            val prefs = com.example.core.database.PreferencesManager(com.example.SevenTicksApplication.instance)
            val prefLang = when (prefs.nativeLanguage.lowercase()) {
                "persian" -> "fa"
                "french" -> "fr"
                "german" -> "de"
                else -> "en"
            }
            
            // If the system language is English, the app's display language is English,
            // so Tiki must speak English as explicitly requested.
            if (systemLang.startsWith("en")) {
                return "en"
            }
            
            // If system is not English, but user preference is English, use English
            if (prefLang == "en") {
                return "en"
            }
            
            // Otherwise, match the system language or user preference
            if (systemLang.startsWith("fa") || prefLang == "fa") {
                return "fa"
            }
            if (systemLang.startsWith("fr") || prefLang == "fr") {
                return "fr"
            }
            if (systemLang.startsWith("de") || prefLang == "de") {
                return "de"
            }
            
            return "en"
        }
    }
}
