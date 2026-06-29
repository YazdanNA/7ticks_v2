const fs = require('fs');
const path = require('path');

// 1. Load Dialogue Library
const enPath = './app/src/main/assets/tiki_dialogues/dialogues_en.json';
const dePath = './app/src/main/assets/tiki_dialogues/dialogues_de.json';
const faPath = './app/src/main/assets/tiki_dialogues/dialogues_fa.json';
const frPath = './app/src/main/assets/tiki_dialogues/dialogues_fr.json';

const enLibrary = JSON.parse(fs.readFileSync(enPath, 'utf8'));
const deLibrary = JSON.parse(fs.readFileSync(dePath, 'utf8'));
const faLibrary = JSON.parse(fs.readFileSync(faPath, 'utf8'));
const frLibrary = JSON.parse(fs.readFileSync(frPath, 'utf8'));

// Valid EmotionState enum values from EmotionState.kt
const VALID_EMOTIONS = new Set([
  "HAPPY", "LAUGH_TEARS", "ROFL", "LAUGH_BIG", "SMILE_BIG", "SMILE_SIMPLE", "SMILE_SHY", 
  "HEART_EYES", "SMILE_HEARTS", "WINK", "KISS", "TEARS_OF_JOY", "PLEADING", "SAD", "CRY", 
  "DISAPPOINTED", "SAD_SIMPLE", "ANGRY", "ANGRY_RED", "FROWN", "CURSING", "SCREAM", 
  "ASTONISHED", "MOUTH_OPEN", "FLUSHED", "THINKING", "ROLL_EYES", "SMIRK", "POKER", 
  "EYEBROW_RAISE", "SWEAT_SMILE", "SWEAT_COLD", "YAWN", "SLEEP", "ZIPPED", "DIZZY", 
  "TALKING", "WELCOME", "NAME", "NATIVE_LANG", "TARGET_LANG", "STUDY_TIME", "REMIND_TIME", 
  "PLACEMENT", "LOADING_DATA", "STREAK_FIRE", "LOCKED_LEVEL", "HEADER_PEEK", "COLLECTION_SEARCH"
]);

// Runtime defined Categories that can be resolved
const RUNTIME_CATEGORIES = new Set([
  "Greeting", "SessionComplete", "Easy", "EasyStreak", "Good", "Hard", "Again", "AgainStreak", 
  "Thinking", "LongThinking", "Idle", "Celebration", "Motivation", "Reminder"
]);

// 2. Perform static analysis of the library
console.log("=== TASK 1: DIALOGUE LIBRARY USAGE ===");
console.log(`Total dialogues: ${enLibrary.dialogues.length}`);

const categoriesMap = {};
enLibrary.dialogues.forEach(d => {
  categoriesMap[d.category] = (categoriesMap[d.category] || 0) + 1;
});

const categoryStats = [];
Object.keys(categoriesMap).forEach(cat => {
  const count = categoriesMap[cat];
  const isReferenced = RUNTIME_CATEGORIES.has(cat);
  categoryStats.push({
    category: cat,
    count: count,
    referenced: isReferenced ? "Yes" : "No",
    reachable: isReferenced ? "Yes" : "No"
  });
});

console.log("\nCategories stats:");
console.table(categoryStats);

// 3. Perform static analysis of emotions
console.log("\n=== TASK 2: EMOTIONS ===");
const emotionsInLibrary = {};
enLibrary.dialogues.forEach(d => {
  emotionsInLibrary[d.emotion.toUpperCase()] = (emotionsInLibrary[d.emotion.toUpperCase()] || 0) + 1;
});

const emotionsStats = [];
VALID_EMOTIONS.forEach(em => {
  const countInLib = emotionsInLibrary[em] || 0;
  emotionsStats.push({
    emotion: em,
    existsInEnum: "Yes",
    existsInLib: countInLib > 0 ? "Yes" : "No",
    occurrencesInLib: countInLib,
    mappedInAssetRepo: "Yes" // Checked in EmotionAssetRepository
  });
});

// Also find emotions in the library that are NOT in the enum
const invalidEmotions = [];
Object.keys(emotionsInLibrary).forEach(em => {
  if (!VALID_EMOTIONS.has(em)) {
    invalidEmotions.push({
      emotion: em,
      existsInEnum: "No",
      existsInLib: "Yes",
      occurrencesInLib: emotionsInLibrary[em],
      mappedInAssetRepo: "No"
    });
  }
});

console.log("\nEmotions from Enum in Library:");
console.table(emotionsStats);
console.log("\nInvalid Emotions (In Library but NOT in Enum):");
console.table(invalidEmotions);

// 4. Dialogue selection simulation
class DialogueResolver {
  resolve(library, category, emotion, relationshipLevel, currentStreak, sessionProgress, thinkingState, lastSpokenMap, currentTimeMillis) {
    const candidates = library.dialogues.filter(d => {
      if (!d.enabled) return false;
      if (category !== null && d.category !== category) return false;
      if (emotion !== null && d.emotion.toUpperCase() !== emotion.toUpperCase()) return false;
      if (d.relationshipLevel !== undefined && d.relationshipLevel !== null && relationshipLevel < d.relationshipLevel) return false;
      if (d.minimumStreak !== undefined && d.minimumStreak !== null && currentStreak < d.minimumStreak) return false;
      if (d.maximumStreak !== undefined && d.maximumStreak !== null && currentStreak > d.maximumStreak) return false;
      if (d.minimumSessionProgress !== undefined && d.minimumSessionProgress !== null && sessionProgress < d.minimumSessionProgress) return false;
      if (d.maximumSessionProgress !== undefined && d.maximumSessionProgress !== null && sessionProgress > d.maximumSessionProgress) return false;
      if (d.thinkingState !== undefined && d.thinkingState !== null && thinkingState !== d.thinkingState) return false;
      return true;
    });

    if (candidates.length === 0) return null;

    const uncooledCandidates = candidates.filter(d => {
      const lastSpoken = lastSpokenMap[d.id];
      if (lastSpoken !== undefined && d.cooldown !== undefined && d.cooldown !== null) {
        const elapsed = currentTimeMillis - lastSpoken;
        return elapsed >= d.cooldown * 1000;
      }
      return true;
    });

    const finalCandidates = uncooledCandidates.length > 0 ? uncooledCandidates : candidates;

    // Weight selection
    const totalWeight = finalCandidates.reduce((sum, d) => sum + (d.weight || 100), 0);
    if (totalWeight <= 0) {
      return finalCandidates[Math.floor(Math.random() * finalCandidates.length)];
    }

    const targetWeight = Math.floor(Math.random() * totalWeight);
    let currentWeightSum = 0;
    for (const candidate of finalCandidates) {
      currentWeightSum += (candidate.weight || 100);
      if (targetWeight < currentWeightSum) {
        return candidate;
      }
    }
    return finalCandidates[finalCandidates.length - 1];
  }
}

// Simulated Pipeline
class TikiEngineSimulator {
  constructor(library) {
    this.library = library;
    this.resolver = new DialogueResolver();
    this.lastSpokenMap = {};
    this.usedDialogueCounts = {};
    this.displayedEmotionCounts = {};
  }

  triggerPipeline(contextEvent, behaviorEvent, relationshipLevel, currentStreak, sessionProgress, currentTimeMillis) {
    // Determine suggested category from context rules
    let suggestedCategory = null;
    if (contextEvent.type === "FirstMasterWord") {
      suggestedCategory = "Celebration";
    } else if (contextEvent.type === "ReviewQueueEmpty") {
      suggestedCategory = "Celebration";
    } else if (contextEvent.type === "SessionFinished") {
      suggestedCategory = "SessionComplete";
    } else if (contextEvent.type === "LastCard") {
      suggestedCategory = "Motivation";
    } else if (contextEvent.type === "FirstCard" || contextEvent.type === "SessionStarted") {
      suggestedCategory = "Greeting";
    } else if (contextEvent.type === "HalfSessionReached") {
      suggestedCategory = "Encouragement";
    } else if (contextEvent.type === "SessionAbandoned") {
      suggestedCategory = "Reminder";
    } else if (contextEvent.type === "ThinkingFinished" && contextEvent.durationMillis >= 8000) {
      suggestedCategory = "LongThinking";
    }

    // Determine mapped category from behavior event
    let mappedCategory = null;
    if (behaviorEvent) {
      if (behaviorEvent.type === "SessionStarted") mappedCategory = "Greeting";
      else if (behaviorEvent.type === "SessionFinished") mappedCategory = "SessionComplete";
      else if (behaviorEvent.type === "CardAnsweredEasy") {
        mappedCategory = behaviorEvent.isStreak ? "EasyStreak" : "Easy";
      } else if (behaviorEvent.type === "CardAnsweredGood") mappedCategory = "Good";
      else if (behaviorEvent.type === "CardAnsweredHard") mappedCategory = "Hard";
      else if (behaviorEvent.type === "CardAnsweredAgain") {
        mappedCategory = behaviorEvent.isStreak ? "AgainStreak" : "Again";
      } else if (behaviorEvent.type === "CardThinkingStarted") mappedCategory = "Thinking";
      else if (behaviorEvent.type === "CardThinkingFinished") {
        mappedCategory = behaviorEvent.durationMillis >= 8000 ? "LongThinking" : "Thinking";
      } else if (behaviorEvent.type === "TranslationOpened") mappedCategory = "Idle";
      else if (behaviorEvent.type === "MoreDetailsOpened") mappedCategory = "Idle";
    }

    const categoryToResolve = suggestedCategory || mappedCategory || "Idle";

    // Determine initial emotion
    let initialEmotion = "HAPPY";
    if (suggestedCategory === "Celebration" || suggestedCategory === "SessionComplete") {
      initialEmotion = "LAUGH_BIG";
    } else if (suggestedCategory === "Greeting") {
      initialEmotion = "WELCOME";
    } else if (suggestedCategory === "Encouragement" || suggestedCategory === "Motivation") {
      initialEmotion = "SMILE_BIG";
    } else if (behaviorEvent) {
      if (behaviorEvent.type === "CardAnsweredEasy") {
        initialEmotion = behaviorEvent.isStreak ? "ROFL" : "HAPPY";
      } else if (behaviorEvent.type === "CardAnsweredAgain") {
        initialEmotion = behaviorEvent.isStreak ? "DISAPPOINTED" : "SAD";
      } else if (behaviorEvent.type === "CardAnsweredHard") {
        initialEmotion = "THINKING";
      } else if (behaviorEvent.type === "CardThinkingFinished") {
        initialEmotion = behaviorEvent.durationMillis >= 15000 ? "POKER" : "THINKING";
      } else if (behaviorEvent.type === "TranslationOpened") {
        initialEmotion = "SMIRK";
      } else if (behaviorEvent.type === "MoreDetailsOpened") {
        initialEmotion = "EYEBROW_RAISE";
      }
    }

    // Resolve dialogue (exclusively from Dialogue Library)
    // Pass 1: Strict match with category and initial emotion
    let resolved = this.resolver.resolve(
      this.library,
      categoryToResolve,
      initialEmotion,
      relationshipLevel,
      currentStreak,
      sessionProgress,
      contextEvent.type === "ThinkingFinished" ? "THINKING_LONG" : null,
      this.lastSpokenMap,
      currentTimeMillis
    );

    // Pass 2: Fallback with emotion = null
    if (!resolved) {
      resolved = this.resolver.resolve(
        this.library,
        categoryToResolve,
        null,
        relationshipLevel,
        currentStreak,
        sessionProgress,
        contextEvent.type === "ThinkingFinished" ? "THINKING_LONG" : null,
        this.lastSpokenMap,
        currentTimeMillis
      );
    }

    // Pass 3: Ultimate fallback matching category = "Idle" with emotion = null
    if (!resolved && categoryToResolve !== "Idle") {
      resolved = this.resolver.resolve(
        this.library,
        "Idle",
        null,
        relationshipLevel,
        currentStreak,
        sessionProgress,
        null,
        this.lastSpokenMap,
        currentTimeMillis
      );
    }

    // Process resolved dialogue
    let targetEmotion = initialEmotion;
    if (resolved) {
      this.lastSpokenMap[resolved.id] = currentTimeMillis;
      this.usedDialogueCounts[resolved.id] = (this.usedDialogueCounts[resolved.id] || 0) + 1;
      
      const dialogueEmotion = resolved.emotion.toUpperCase();
      if (VALID_EMOTIONS.has(dialogueEmotion)) {
        targetEmotion = dialogueEmotion;
      } else {
        // Fallback because of invalid emotion state name
        targetEmotion = initialEmotion;
      }
    }

    this.displayedEmotionCounts[targetEmotion] = (this.displayedEmotionCounts[targetEmotion] || 0) + 1;

    return {
      resolved,
      initialEmotion,
      targetEmotion
    };
  }
}

// 5. Run simulation: 500 random realistic events
console.log("\n=== SIMULATION: 500 STUDY EVENTS ===");
const sim = new TikiEngineSimulator(enLibrary);

let currentTime = Date.now();
let eventCount = 0;
let streak = 0;
let relationshipLevel = 1;

const EVENT_TYPES = [
  "SessionStarted",
  "CardThinkingStarted",
  "CardThinkingFinished",
  "CardAnsweredEasy",
  "CardAnsweredGood",
  "CardAnsweredHard",
  "CardAnsweredAgain",
  "CardFlipped",
  "TranslationOpened",
  "MoreDetailsOpened",
  "SessionFinished"
];

const sessions = 25;
const cardsPerSession = 15;

for (let s = 1; s <= sessions; s++) {
  // Session started
  sim.triggerPipeline({ type: "SessionStarted" }, { type: "SessionStarted" }, relationshipLevel, streak, 0.0, currentTime);
  currentTime += 5000;
  eventCount++;

  let easyStreakCount = 0;
  let againStreakCount = 0;

  for (let c = 1; c <= cardsPerSession; c++) {
    const sessionProgress = c / cardsPerSession;
    
    // Thinking started
    sim.triggerPipeline({ type: "ThinkingStarted" }, { type: "CardThinkingStarted" }, relationshipLevel, streak, sessionProgress, currentTime);
    currentTime += 3000;
    eventCount++;

    // Random duration for thinking finished
    const thinkDuration = Math.random() * 20000; // 0 to 20s
    sim.triggerPipeline(
      { type: "ThinkingFinished", durationMillis: thinkDuration }, 
      { type: "CardThinkingFinished", durationMillis: thinkDuration }, 
      relationshipLevel, streak, sessionProgress, currentTime
    );
    currentTime += 1000;
    eventCount++;

    // Card flipped
    sim.triggerPipeline({ type: "CardFlipped" }, { type: "CardFlipped" }, relationshipLevel, streak, sessionProgress, currentTime);
    currentTime += 1000;
    eventCount++;

    // Randomly view translation or more details
    if (Math.random() < 0.15) {
      sim.triggerPipeline({ type: "Idle" }, { type: "TranslationOpened" }, relationshipLevel, streak, sessionProgress, currentTime);
      currentTime += 2000;
      eventCount++;
    }
    if (Math.random() < 0.1) {
      sim.triggerPipeline({ type: "Idle" }, { type: "MoreDetailsOpened" }, relationshipLevel, streak, sessionProgress, currentTime);
      currentTime += 2000;
      eventCount++;
    }

    // Answer card
    const rand = Math.random();
    let rating = "Good";
    let isEasyStreak = false;
    let isAgainStreak = false;

    if (rand < 0.6) {
      rating = "Easy";
      easyStreakCount++;
      againStreakCount = 0;
      if (easyStreakCount >= 3) isEasyStreak = true;
      streak++;
    } else if (rand < 0.85) {
      rating = "Good";
      easyStreakCount = 0;
      againStreakCount = 0;
      streak++;
    } else if (rand < 0.93) {
      rating = "Hard";
      easyStreakCount = 0;
      againStreakCount = 0;
      streak++;
    } else {
      rating = "Again";
      againStreakCount++;
      easyStreakCount = 0;
      if (againStreakCount >= 2) isAgainStreak = true;
      streak = 0;
    }

    sim.triggerPipeline(
      { type: "CardAnswered", isMastered: rating === "Easy" },
      { type: `CardAnswered${rating}`, isStreak: rating === "Easy" ? isEasyStreak : (rating === "Again" ? isAgainStreak : false) },
      relationshipLevel, streak, sessionProgress, currentTime
    );
    currentTime += 4000;
    eventCount++;

    if (c === Math.floor(cardsPerSession / 2)) {
      sim.triggerPipeline({ type: "HalfSessionReached" }, null, relationshipLevel, streak, sessionProgress, currentTime);
      currentTime += 1000;
      eventCount++;
    }

    if (c === cardsPerSession - 1) {
      sim.triggerPipeline({ type: "LastCard" }, null, relationshipLevel, streak, sessionProgress, currentTime);
      currentTime += 1000;
      eventCount++;
    }
  }

  // Session finished
  sim.triggerPipeline({ type: "SessionFinished" }, { type: "SessionFinished" }, relationshipLevel, streak, 1.0, currentTime);
  currentTime += 10000;
  eventCount++;

  relationshipLevel = Math.min(10, relationshipLevel + 1);
}

console.log(`\nSimulated ${eventCount} events.`);

console.log("\nUnique Dialogues Displayed Count:", Object.keys(sim.usedDialogueCounts).length);
console.log("\nTop Displayed Dialogues (out of total unique):");
const dialogueFreq = Object.entries(sim.usedDialogueCounts)
  .map(([id, count]) => {
    const d = enLibrary.dialogues.find(x => x.id === id);
    return { id, text: d ? d.text : "N/A", category: d ? d.category : "N/A", emotionInLib: d ? d.emotion : "N/A", count };
  })
  .sort((a, b) => b.count - a.count);

console.table(dialogueFreq.slice(0, 15));

console.log("\nDisplayed Emotions Frequencies:");
console.table(Object.entries(sim.displayedEmotionCounts)
  .map(([emotion, count]) => ({ emotion, count }))
  .sort((a, b) => b.count - a.count));

// 6. Dead content detection
console.log("\n=== DEAD CONTENT DETECTION ===");
const unusedDialogues = enLibrary.dialogues.filter(d => !sim.usedDialogueCounts[d.id]);
console.log(`Unused Dialogues count: ${unusedDialogues.length} (out of ${enLibrary.dialogues.length})`);

const unusedCategories = [];
categoryStats.forEach(cat => {
  if (cat.reachable === "No") {
    unusedCategories.push(cat.category);
  }
});
console.log("Unused/Unreachable Categories in Library:", unusedCategories);

const unusedEmotions = [];
Object.keys(emotionsInLibrary).forEach(em => {
  if (!sim.displayedEmotionCounts[em]) {
    unusedEmotions.push(em);
  }
});
console.log("Unused/Unreached Emotions:", unusedEmotions);
