package com.example.features.tiki.ambient

enum class AmbientAction(val actionName: String, val emoji: String) {
    SMILE("Smile", "😊"),
    WINK("Wink", "😉"),
    LOOK_UP("Look up", "🤔"),
    YAWN("Yawn", "🥱"),
    SLEEPY_EYES("Sleepy eyes", "😴"),
    LOOK_SIDEWAYS("Look sideways", "🙄"),
    SMALL_NERVOUS_SMILE("Small nervous smile", "😅"),
    NEUTRAL_FACE("Neutral face", "😑"),
    HAPPY_EYES("Happy eyes", "🥹"),
    CURIOUS_FACE("Curious face", "🤨"),
    BLINK("Blink", "👁️"),
    LOOK_AROUND("Look Around", "👀"),
    STRETCH("Stretch", "🙆"),
    SMALL_LAUGH("Small Laugh", "😆"),
    TINY_BOUNCE("Tiny Bounce", "↕️"),

    // Thinking states
    THINKING_0_5("Thinking 0-5s", "🙂"),
    THINKING_5_10("Thinking 5-10s", "🤔"),
    THINKING_10_20("Thinking 10-20s", "😐"),
    THINKING_20_40("Thinking 20-40s", "🥱"),
    THINKING_40_60("Thinking 40-60s", "😵💫")
}
