package com.example.features.tiki.engine

import com.example.features.tiki.repository.DialogueRepository
import com.example.features.tiki.repository.EmotionAssetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class TikiEngine private constructor() {

    val dialogueRepository = DialogueRepository()
    val assetRepository = EmotionAssetRepository()
    val transitionManager = EmotionTransitionManager(CoroutineScope(Dispatchers.Main.immediate))
    val emotionEngine = EmotionEngine(dialogueRepository, assetRepository, transitionManager)

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
