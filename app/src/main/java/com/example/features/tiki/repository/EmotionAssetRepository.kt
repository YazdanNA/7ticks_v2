package com.example.features.tiki.repository

import com.example.features.tiki.model.EmotionState

data class EmotionVisualAsset(
    val face: String,
    val body: String,
    val gadget: String,
    val tikiStateKey: String
)

class EmotionAssetRepository {

    private val assetMap = mapOf(
        EmotionState.HAPPY to EmotionVisualAsset("happy", "normal", "none", "st-happy"),
        EmotionState.LAUGH_TEARS to EmotionVisualAsset("laugh-tears", "normal", "none", "st-laugh-tears"),
        EmotionState.ROFL to EmotionVisualAsset("rofl", "rofl-roll", "none", "st-rofl"),
        EmotionState.LAUGH_BIG to EmotionVisualAsset("laugh-big", "normal", "none", "st-laugh-big"),
        EmotionState.SMILE_BIG to EmotionVisualAsset("smile-big", "normal", "none", "st-smile-big"),
        EmotionState.SMILE_SIMPLE to EmotionVisualAsset("smile-simple", "normal", "none", "st-smile-simple"),
        EmotionState.SMILE_SHY to EmotionVisualAsset("smile-shy", "normal", "none", "st-smile-shy"),
        EmotionState.HEART_EYES to EmotionVisualAsset("heart-eyes", "normal", "none", "st-heart-eyes"),
        EmotionState.SMILE_HEARTS to EmotionVisualAsset("smile-hearts", "normal", "none", "st-smile-hearts"),
        EmotionState.WINK to EmotionVisualAsset("wink", "normal", "none", "st-wink"),
        EmotionState.KISS to EmotionVisualAsset("kiss", "normal", "none", "st-kiss"),
        EmotionState.TEARS_OF_JOY to EmotionVisualAsset("tears-of-joy", "normal", "none", "st-tears-of-joy"),
        EmotionState.PLEADING to EmotionVisualAsset("pleading", "sad", "none", "st-pleading"),
        EmotionState.SAD to EmotionVisualAsset("sad", "sad", "none", "st-sad"),
        EmotionState.CRY to EmotionVisualAsset("cry", "sad", "none", "st-cry"),
        EmotionState.DISAPPOINTED to EmotionVisualAsset("disappointed", "sad", "none", "st-disappointed"),
        EmotionState.SAD_SIMPLE to EmotionVisualAsset("sad-simple", "sad", "none", "st-sad-simple"),
        EmotionState.ANGRY to EmotionVisualAsset("angry", "normal", "none", "st-angry"),
        EmotionState.ANGRY_RED to EmotionVisualAsset("angry-red", "furious", "none", "st-angry-red"),
        EmotionState.FROWN to EmotionVisualAsset("frown", "normal", "none", "st-frown"),
        EmotionState.CURSING to EmotionVisualAsset("cursing", "furious", "none", "st-cursing"),
        EmotionState.SCREAM to EmotionVisualAsset("scream", "furious", "none", "st-scream"),
        EmotionState.ASTONISHED to EmotionVisualAsset("astonished", "normal", "none", "st-astonished"),
        EmotionState.MOUTH_OPEN to EmotionVisualAsset("mouth-open", "normal", "none", "st-mouth-open"),
        EmotionState.FLUSHED to EmotionVisualAsset("flushed", "normal", "none", "st-flushed"),
        EmotionState.THINKING to EmotionVisualAsset("thinking", "normal", "none", "st-thinking"),
        EmotionState.ROLL_EYES to EmotionVisualAsset("roll-eyes", "normal", "none", "st-roll-eyes"),
        EmotionState.SMIRK to EmotionVisualAsset("smirk", "normal", "none", "st-smirk"),
        EmotionState.POKER to EmotionVisualAsset("poker", "normal", "none", "st-poker"),
        EmotionState.EYEBROW_RAISE to EmotionVisualAsset("eyebrow-raise", "normal", "none", "st-eyebrow-raise"),
        EmotionState.SWEAT_SMILE to EmotionVisualAsset("sweat-smile", "normal", "none", "st-sweat-smile"),
        EmotionState.SWEAT_COLD to EmotionVisualAsset("sweat-cold", "normal", "none", "st-sweat-cold"),
        EmotionState.YAWN to EmotionVisualAsset("yawn", "normal", "none", "st-yawn"),
        EmotionState.SLEEP to EmotionVisualAsset("sleep", "normal", "none", "st-sleep"),
        EmotionState.ZIPPED to EmotionVisualAsset("zipped", "normal", "none", "st-zipped"),
        EmotionState.DIZZY to EmotionVisualAsset("dizzy", "normal", "none", "st-dizzy"),
        EmotionState.TALKING to EmotionVisualAsset("talk", "normal", "none", "st-talking"),
        EmotionState.WELCOME to EmotionVisualAsset("happy", "wave", "stars", "st-welcome"),
        EmotionState.NAME to EmotionVisualAsset("happy", "normal", "pencil", "st-name"),
        EmotionState.NATIVE_LANG to EmotionVisualAsset("happy", "normal", "globe-native", "st-native-lang"),
        EmotionState.TARGET_LANG to EmotionVisualAsset("happy", "normal", "globe-target", "st-target-lang"),
        EmotionState.STUDY_TIME to EmotionVisualAsset("happy", "normal", "timer", "st-study-time"),
        EmotionState.REMIND_TIME to EmotionVisualAsset("happy", "normal", "bell", "st-remind-time"),
        EmotionState.PLACEMENT to EmotionVisualAsset("happy", "normal", "placement", "st-placement"),
        EmotionState.LOADING_DATA to EmotionVisualAsset("hidden", "normal", "loading", "st-loading-data"),
        EmotionState.STREAK_FIRE to EmotionVisualAsset("happy", "large", "fire", "st-streak-fire"),
        EmotionState.LOCKED_LEVEL to EmotionVisualAsset("locked", "locked", "lock", "st-locked-level"),
        EmotionState.HEADER_PEEK to EmotionVisualAsset("happy", "peek", "none", "st-header-peek"),
        EmotionState.COLLECTION_SEARCH to EmotionVisualAsset("search", "normal", "search-data", "st-collection-search")
    )

    fun resolve(emotion: EmotionState?): EmotionVisualAsset {
        if (emotion == null) {
            return assetMap[EmotionState.HAPPY]!!
        }
        return assetMap[emotion] ?: assetMap[EmotionState.HAPPY]!!
    }
}
