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
        try {
            val appInstance = com.example.SevenTicksApplication.instance
            val stream = appInstance.assets.open("tiki_dialogues/dialogues_en.json")
            val library = com.example.features.tiki.content.DialogueLoader().loadFromStream(stream)
            contentEngine.loadLibrary(library)
        } catch (e: Exception) {
            android.util.Log.e("TikiEngine", "Failed to load dialogues asset", e)
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

        // 4. Emotion Engine
        val targetEmotion = behaviorResult?.emotion 
            ?: when (contextRecommendation?.suggestedDialogueCategory) {
                "Celebration", "SessionComplete" -> EmotionState.LAUGH_BIG
                "Greeting" -> EmotionState.WELCOME
                "Encouragement", "Motivation" -> EmotionState.SMILE_BIG
                else -> EmotionState.HAPPY
            }

        // 5. Dialogue Engine / Library resolution (exclusively from Dialogue Library)
        val progress = contextEngine.getSnapshot().sessionProgress
        val resolvedMetadata = contentEngine.resolveDialogue(
            category = contextRecommendation?.suggestedDialogueCategory,
            language = "en",
            emotion = targetEmotion.name,
            relationshipLevel = relationshipEngine.getSnapshot(currentTime).level,
            currentStreak = memorySnapshot.currentStreak,
            sessionProgress = progress,
            thinkingState = if (contextEvent is ContextEvent.ThinkingFinished) "THINKING_LONG" else null
        )

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
    }
}
