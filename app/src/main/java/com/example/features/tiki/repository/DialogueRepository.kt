package com.example.features.tiki.repository

import com.example.features.tiki.model.Dialogue
import com.example.features.tiki.model.EmotionState

class DialogueRepository {

    private val dialoguesByEmotion = mutableMapOf<EmotionState, List<Dialogue>>()

    init {
        // Load default dialogue candidates (max 5 words, natural, short)
        addDialogues(EmotionState.HAPPY, listOf("Nice work!", "Awesome!", "Great job!", "Keep going!", "You got this!"))
        addDialogues(EmotionState.LAUGH_TEARS, listOf("Hahaha, too good!", "That was fun!", "So close yet so funny!"))
        addDialogues(EmotionState.ROFL, listOf("Oh my god, hilarious!", "Unbelievable recall!", "That was spectacular!"))
        addDialogues(EmotionState.LAUGH_BIG, listOf("Haha, yes!", "Excellent recall!", "Spot on!"))
        addDialogues(EmotionState.SMILE_BIG, listOf("Perfect!", "Well done!", "Incredible memory!", "You are a genius!"))
        addDialogues(EmotionState.SMILE_SIMPLE, listOf("Keep it up!", "Nice recall.", "Looking good."))
        addDialogues(EmotionState.SMILE_SHY, listOf("Aw, thank you!", "You make me proud.", "Just a little mentor!"))
        addDialogues(EmotionState.HEART_EYES, listOf("I love this!", "Absolutely perfect!", "Simply stunning!", "Pure brilliance!"))
        addDialogues(EmotionState.SMILE_HEARTS, listOf("Heartwarming effort!", "You are amazing!", "So proud of you!"))
        addDialogues(EmotionState.WINK, listOf("Gotcha!", "Piece of cake, right?", "A secret master!"))
        addDialogues(EmotionState.KISS, listOf("Smooch!", "Mwah, splendid work!"))
        addDialogues(EmotionState.TEARS_OF_JOY, listOf("Truly beautiful!", "I am so proud!", "Magnificent study flow!"))

        addDialogues(EmotionState.PLEADING, listOf("Please focus!", "One more time?", "Don't give up yet!"))
        addDialogues(EmotionState.SAD, listOf("Oops, not quite.", "A tiny slip.", "We will learn this.", "Almost had it."))
        addDialogues(EmotionState.CRY, listOf("Oh no, a mistake!", "That hurts!", "Let's review again.", "Stay strong!"))
        addDialogues(EmotionState.DISAPPOINTED, listOf("Bummer.", "Let's focus more.", "Keep trying."))
        addDialogues(EmotionState.SAD_SIMPLE, listOf("Don't worry.", "Try again.", "We'll get there."))

        addDialogues(EmotionState.ANGRY, listOf("Focus up!", "That was careless!", "Read carefully!"))
        addDialogues(EmotionState.ANGRY_RED, listOf("No, no, no!", "Concentrate!", "Stop rushing!"))
        addDialogues(EmotionState.FROWN, listOf("Are you distracted?", "Double check that."))
        addDialogues(EmotionState.CURSING, listOf("Argh, shoot!", "What was that?", "Unbelievable!"))

        addDialogues(EmotionState.SCREAM, listOf("Whoa, watch out!", "Yikes, alert!"))
        addDialogues(EmotionState.ASTONISHED, listOf("Wait, what?!", "Wow, incredible!"))
        addDialogues(EmotionState.MOUTH_OPEN, listOf("Oh, interesting!", "Whoa!"))
        addDialogues(EmotionState.FLUSHED, listOf("Oh, my!", "Gosh, exciting!"))

        addDialogues(EmotionState.THINKING, listOf("Hmm, let's see...", "Thinking deeply...", "Let's figure it out."))
        addDialogues(EmotionState.ROLL_EYES, listOf("Really?", "Again?", "Concentration, please."))
        addDialogues(EmotionState.SMIRK, listOf("Sneaky!", "I knew it."))
        addDialogues(EmotionState.POKER, listOf("Let's stay neutral.", "Keep moving forward."))
        addDialogues(EmotionState.EYEBROW_RAISE, listOf("Is that so?", "Sure? Check again."))

        addDialogues(EmotionState.SWEAT_SMILE, listOf("Phew, close call!", "Glad we passed!"))
        addDialogues(EmotionState.SWEAT_COLD, listOf("That was scary...", "Need a breather?"))
        addDialogues(EmotionState.YAWN, listOf("Time to wake up!", "Getting tired?"))
        addDialogues(EmotionState.SLEEP, listOf("Zzz...", "Rest is cognitive growth!"))
        addDialogues(EmotionState.ZIPPED, listOf("Mouth zipped.", "Silence is golden."))
        addDialogues(EmotionState.DIZZY, listOf("My head is spinning!", "Too much vocabulary!"))
        addDialogues(EmotionState.TALKING, listOf("Listen carefully!", "Pronouncing now..."))

        addDialogues(EmotionState.WELCOME, listOf("Welcome to SevenTicks!", "Let's master vocabulary!", "I am your mentor, Tiki!"))
        addDialogues(EmotionState.NAME, listOf("What is your name?", "Let's write it down!"))
        addDialogues(EmotionState.NATIVE_LANG, listOf("Choose your native language."))
        addDialogues(EmotionState.TARGET_LANG, listOf("Choose target language!"))
        addDialogues(EmotionState.STUDY_TIME, listOf("Set your study goal!"))
        addDialogues(EmotionState.REMIND_TIME, listOf("I'll remind you daily!"))
        addDialogues(EmotionState.PLACEMENT, listOf("Let's test your level!"))
        addDialogues(EmotionState.LOADING_DATA, listOf("Setting up the database...", "Downloading cognitive nodes..."))
        addDialogues(EmotionState.STREAK_FIRE, listOf("You are on fire!", "Look at that streak!"))
        addDialogues(EmotionState.LOCKED_LEVEL, listOf("This level is locked!"))
        addDialogues(EmotionState.HEADER_PEEK, listOf("Peekaboo!", "I am watching!"))
        addDialogues(EmotionState.COLLECTION_SEARCH, listOf("Searching the database...", "Found anything?"))
    }

    fun addDialogues(emotion: EmotionState, textList: List<String>) {
        val current = dialoguesByEmotion[emotion].orEmpty().toMutableList()
        textList.forEachIndexed { index, text ->
            val words = text.split("\\s+".toRegex()).filter { it.isNotEmpty() }
            require(words.size <= 5) { "Dialogue text exceeds 5 words: '$text'" }
            current.add(Dialogue("${emotion.name}_$index", text, emotion))
        }
        dialoguesByEmotion[emotion] = current
    }

    fun getDialogueForEmotion(emotion: EmotionState, lastDialogueText: String?): String {
        val candidates = dialoguesByEmotion[emotion].orEmpty()
        if (candidates.isEmpty()) return "Tiki is here!"
        if (candidates.size == 1) return candidates.first().text

        val filtered = if (lastDialogueText != null) {
            candidates.filter { it.text != lastDialogueText }
        } else {
            candidates
        }

        val finalCandidates = if (filtered.isEmpty()) candidates else filtered
        return finalCandidates.random().text
    }
}
