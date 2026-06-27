package com.example.features.tiki.engine

import com.example.features.tiki.api.TikiState
import com.example.features.tiki.model.EmotionState
import com.example.features.tiki.repository.EmotionVisualAsset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EmotionTransitionManager(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main.immediate)
) {
    private val _state = MutableStateFlow(TikiState())
    val state: StateFlow<TikiState> = _state.asStateFlow()

    fun transitionTo(
        emotion: EmotionState,
        asset: EmotionVisualAsset,
        dialogue: String,
        forceInstant: Boolean = false
    ) {
        if (forceInstant) {
            _state.value = TikiState(
                emotion = emotion,
                face = asset.face,
                body = asset.body,
                gadget = asset.gadget,
                tikiStateKey = asset.tikiStateKey,
                dialogue = dialogue,
                isSquished = false
            )
            return
        }

        scope.launch {
            // Step 1: Squish the companion
            _state.value = _state.value.copy(isSquished = true)
            
            // Step 2: Hold squish for 150ms
            delay(150)
            
            // Step 3: Swap state attributes while squished
            _state.value = TikiState(
                emotion = emotion,
                face = asset.face,
                body = asset.body,
                gadget = asset.gadget,
                tikiStateKey = asset.tikiStateKey,
                dialogue = dialogue,
                isSquished = true
            )
            
            // Step 4: Instantly trigger spring release
            _state.value = _state.value.copy(isSquished = false)
        }
    }

    fun updateDialogueOnly(text: String, emotion: EmotionState? = null, asset: EmotionVisualAsset? = null) {
        val current = _state.value
        _state.value = current.copy(
            dialogue = text,
            emotion = emotion ?: current.emotion,
            face = asset?.face ?: current.face,
            body = asset?.body ?: current.body,
            gadget = asset?.gadget ?: current.gadget,
            tikiStateKey = asset?.tikiStateKey ?: current.tikiStateKey
        )
    }

    fun reset() {
        _state.value = TikiState()
    }
}
