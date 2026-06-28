const fs = require('fs');
const path = require('path');

const funnyRareCategories = {
  funny_first_contact: {
    dialogue_category: "funny_first_contact",
    emotion: "FUNNY_LIGHT_SURPRISE",
    minFunnyScore: 0.0, maxFunnyScore: 0.4,
    minSessionChaos: 0.0, maxSessionChaos: 0.3,
    minUnexpectedEvents: 0, maxUnexpectedEvents: 2,
    minTimeSpent: 0, maxTimeSpent: 120,
    phrases: [
      "A new word. Please do not feed the vocabulary.",
      "First contact with an extremely introverted word.",
      "A fresh term appears. Approach with mild curiosity.",
      "Introducing a word that dislikes bright lights.",
      "First meeting. It is polite to look interested.",
      "An unfamiliar word. It seems friendly enough.",
      "A new linguistic specimen has wandered in.",
      "Meet a word that refuses to be ignored.",
      "First contact. The word is sizing you up.",
      "An unexpected guest in our review deck today.",
      "A quiet introduction to a very private noun.",
      "This word has arrived without an invitation.",
      "A new concept. Handle with light curiosity.",
      "First contact. This term is surprisingly quiet.",
      "An unusual word. It is still adjusting.",
      "Greeting a word that recently arrived here.",
      "First encounter. No sudden movements, please.",
      "A fresh term has quietly joined us.",
      "Meet a word with a very specific attitude.",
      "First contact. This node seems slightly confused.",
      "A new verb. It is highly experimental.",
      "First meeting. The word seems slightly defensive.",
      "A rare word has entered your hemisphere.",
      "An unfamiliar term. Let's keep our distance.",
      "A new vocabulary node. Mildly intriguing indeed.",
      "First contact. The letters are still settling.",
      "An unexpected word joins today's quiet circle.",
      "Introducing a term that prefers quiet corners.",
      "A fresh node. It smells like paper.",
      "First meeting with a rather curious adjective.",
      "A new word. Try not to startle it.",
      "First contact. The spelling is highly intentional.",
      "An interesting term. Let us observe quietly.",
      "First meeting. The word nods back silently.",
      "A new arrival. It seems quite stable."
    ]
  },
  funny_repeat_attempt: {
    dialogue_category: "funny_repeat_attempt",
    emotion: "SOFT_HUMOR",
    minFunnyScore: 0.1, maxFunnyScore: 0.6,
    minSessionChaos: 0.1, maxSessionChaos: 0.5,
    minUnexpectedEvents: 0, maxUnexpectedEvents: 3,
    minTimeSpent: 60, maxTimeSpent: 900,
    phrases: [
      "Trying this word again. Third time is charm.",
      "Another attempt. The word feels very familiar.",
      "We meet again. Did you miss me?",
      "Back so soon? This word has gravity.",
      "A repeat attempt. Let's try a smile.",
      "Revisiting this term. It has missed you.",
      "Attempt number two. Let us keep it simple.",
      "Back here again. The word looks different.",
      "Another try. The letters haven't changed.",
      "Meeting this word again. A quiet reunion.",
      "Re-attempting. Let's pretend it's the first time.",
      "This word again. It has a persistent nature.",
      "Another encounter. The letters are staying put.",
      "Back for seconds. The vocabulary is rich.",
      "A repeat try. Let's negotiate with memory.",
      "Once more. The meaning remains the same.",
      "Re-examining. The definition is still holding on.",
      "Back to this card. A persistent little node.",
      "A second glance. The word stands its ground.",
      "Another round. Memory is playing gentle games.",
      "Revisiting. The word is waiting patiently here.",
      "Back here. Let's try a softer touch.",
      "A repeat attempt. No pressure, just letters.",
      "Once again. The term greets you quietly.",
      "Re-attempting this. Let's keep our focus.",
      "Another try. The definition is still stable.",
      "Back to this concept. A familiar path.",
      "Meeting again. The spelling is still intact.",
      "Another go. Let us tread softly here.",
      "Back to the start. The letters wait.",
      "Revisiting this noun. It is quite stubborn.",
      "Another attempt. A quiet conversation with memory.",
      "Back for another look. A highly persistent term.",
      "Once more. Let's approach with peace.",
      "Re-attempting. The definition is watching us."
    ]
  },
  funny_chain_reaction: {
    dialogue_category: "funny_chain_reaction",
    emotion: "UNEXPECTED_PATTERN",
    minFunnyScore: 0.2, maxFunnyScore: 0.8,
    minSessionChaos: 0.2, maxSessionChaos: 0.7,
    minUnexpectedEvents: 1, maxUnexpectedEvents: 5,
    minTimeSpent: 120, maxTimeSpent: 1800,
    phrases: [
      "One right answer triggers a quiet cascade.",
      "A chain reaction of clear, quiet thoughts.",
      "Success breeds success. A gentle domino effect.",
      "A quiet row of correct answers falling.",
      "One spark triggers a silent vocabulary flame.",
      "A cascade of correct thoughts has started.",
      "A quiet avalanche of precise word choices.",
      "One good memory triggers another beautiful node.",
      "A chain reaction of perfect semantic clicks.",
      "The dominoes of memory are falling gracefully.",
      "One correct word triggers a quiet waterfall.",
      "A chain of success. The mind hums.",
      "One spark. A beautifully silent mental chain.",
      "A sequence of correct answers begins today.",
      "A calm cascade of vocabulary success now.",
      "One right answer. The queue nods back.",
      "A chain reaction. The vocabulary is waking.",
      "One node ignites another in absolute silence.",
      "A steady row of correct cards cleared.",
      "One perfect recall triggers a quiet slide.",
      "A cascade of knowledge. Tread lightly here.",
      "One success. The deck begins to move.",
      "A quiet chain of perfect semantic links.",
      "One correct choice. The system smiles back.",
      "A gentle cascade of successful vocabulary cards.",
      "One spark of memory. A quiet chain.",
      "A beautifully silent chain of correct recalls.",
      "One right choice. The dominoes are aligned.",
      "A cascade of definitions falling in place.",
      "One success triggers a quiet, clear pattern.",
      "A chain of memory nodes lighting up.",
      "One click. The entire deck aligns itself.",
      "A beautiful sequence of silent correct choices.",
      "One answer sparks a quiet mental path.",
      "A peaceful cascade of perfect vocabulary cards."
    ]
  },
  funny_confusion_flip: {
    dialogue_category: "funny_confusion_flip",
    emotion: "ODD_CONNECTION",
    minFunnyScore: 0.3, maxFunnyScore: 0.9,
    minSessionChaos: 0.3, maxSessionChaos: 0.8,
    minUnexpectedEvents: 2, maxUnexpectedEvents: 6,
    minTimeSpent: 180, maxTimeSpent: 1200,
    phrases: [
      "A sudden flip from confusion to clarity.",
      "Confused? Now it makes perfect, odd sense.",
      "A beautiful flip. The meaning is inverted.",
      "From chaos to quiet order in seconds.",
      "A sudden flip. The definition has landed.",
      "The confusion dissolves. An elegant flip occurred.",
      "A flip of meaning. Now it shines.",
      "From complete mystery to a quiet click.",
      "A sudden flip. The brain has adjusted.",
      "Confusion turns into a beautiful, quiet path.",
      "A sudden flip. The letters aligned themselves.",
      "The mind flips. The word makes sense.",
      "From uncertainty to absolute, quiet semantic peace.",
      "A beautiful, strange flip of linguistic perspective.",
      "Confusion vanishes. A quiet flip of understanding.",
      "The puzzle flips. The picture is clear.",
      "A sudden flip. The definition is yours.",
      "Confusion turns to a quiet, serene smile.",
      "A flip of memory. The word clicked.",
      "From dark mystery to a soft light.",
      "A sudden flip. The definition settled down.",
      "Confusion flips. A tranquil path is found.",
      "The card flips. The mind understands completely.",
      "A sudden flip. The term is clear.",
      "Confusion clears. A beautiful, silent transition happened.",
      "A flip of clarity. The word surrenders.",
      "From doubt to a quiet, confident smile.",
      "A sudden flip. The concept is stable.",
      "Confusion flips into a peaceful mental note.",
      "The puzzle flips. The meaning is caught.",
      "A sudden flip. The letters make sense.",
      "From chaos to a quiet, elegant map.",
      "Confusion flips. A beautiful, stable word remains.",
      "A sudden flip. The meaning is locked.",
      "From mystery to a calm, clear definition."
    ]
  },
  funny_success_twist: {
    dialogue_category: "funny_success_twist",
    emotion: "PLAYFUL_OBSERVATION",
    minFunnyScore: 0.2, maxFunnyScore: 0.8,
    minSessionChaos: 0.1, maxSessionChaos: 0.6,
    minUnexpectedEvents: 1, maxUnexpectedEvents: 4,
    minTimeSpent: 90, maxTimeSpent: 1500,
    phrases: [
      "Success, but with a highly unusual spelling.",
      "Correct. Even the dictionary is slightly surprised.",
      "A successful recall. A highly elegant twist.",
      "Correct. A very creative way to remember.",
      "Success. The letters aligned with dramatic flair.",
      "A correct answer. A quiet, playful twist.",
      "Success. The brain took a scenic route.",
      "Correct. A highly unorthodox path to victory.",
      "Success. The word was almost too easy.",
      "Correct. A beautiful twist of mental fate.",
      "Success. The letters co-operated beautifully today.",
      "Correct. The memory took a lovely detour.",
      "Success. A highly precise, unexpected recall today.",
      "Correct. The word is smiling back now.",
      "Success. A very sophisticated twist of recall.",
      "Correct. Your brain played a beautiful trick.",
      "Success. The letters found their true home.",
      "Correct. A highly elegant, unusual mental path.",
      "Success. The vocabulary node has surrendered quietly.",
      "Correct. An intriguing twist of memory today.",
      "Success. The definition was waiting in ambush.",
      "Correct. A lovely, calm twist of logic.",
      "Success. The word was caught red-handed.",
      "Correct. Your mind is highly unpredictable today.",
      "Success. A delightful twist in your path.",
      "Correct. The letters had a quiet meeting.",
      "Success. The meaning was beautifully preserved here.",
      "Correct. A very graceful twist of understanding.",
      "Success. The card has been quietly conquered.",
      "Correct. A highly unusual but correct path.",
      "Success. The spelling was a quiet adventure.",
      "Correct. The word is quite pleased now.",
      "Success. A very delicate, beautiful mental twist.",
      "Correct. The brain has done something remarkable.",
      "Success. A quiet twist of perfect vocabulary."
    ]
  },
  funny_failure_light: {
    dialogue_category: "funny_failure_light",
    emotion: "SOFT_HUMOR",
    minFunnyScore: 0.1, maxFunnyScore: 0.5,
    minSessionChaos: 0.2, maxSessionChaos: 0.7,
    minUnexpectedEvents: 1, maxUnexpectedEvents: 4,
    minTimeSpent: 45, maxTimeSpent: 900,
    phrases: [
      "A light miss. The letters went sideways.",
      "Not quite. The word is playing hide-and-seek.",
      "A gentle miss. The definition was close.",
      "Incorrect, but with a highly elegant effort.",
      "A soft miss. The letters are laughing.",
      "Not today. The word has escaped quietly.",
      "A light failure. Let's pretend it didn't happen.",
      "Incorrect. The brain was looking elsewhere, friend.",
      "A soft miss. The dictionary remains unmoved.",
      "Not quite. The letters had other plans.",
      "A gentle failure. Let's take a breath.",
      "Incorrect. A highly creative guess, though.",
      "A light miss. The meaning slipped away.",
      "Not quite. The word is being stubborn.",
      "A gentle miss. The brain took break.",
      "Incorrect. The spelling is slightly offended today.",
      "A soft failure. Let's try once more.",
      "Not today. The definition is highly elusive.",
      "A light miss. The letters are rearranging.",
      "Incorrect. A beautiful, quiet attempt, however.",
      "A soft miss. The word remains mysterious.",
      "Not quite. The memory had a hiccup.",
      "A gentle failure. The letters are sleeping.",
      "Incorrect. Let's brush it off quietly.",
      "A light miss. The definition took holiday.",
      "Not today. The word is playing difficult.",
      "A soft miss. Let's keep it peaceful.",
      "Incorrect. The dictionary is gently shaking head.",
      "A light failure. A minor cognitive detour.",
      "Not quite. The spelling went on vacation.",
      "A soft miss. The concept remains active.",
      "Incorrect. A very artistic attempt, indeed.",
      "A gentle miss. Let's try again tomorrow.",
      "Not today. The letters are slightly scrambled.",
      "A soft failure. A quiet, unimportant stumble."
    ]
  },
  funny_unexpected_win: {
    dialogue_category: "funny_unexpected_win",
    emotion: "FUNNY_LIGHT_SURPRISE",
    minFunnyScore: 0.4, maxFunnyScore: 1.0,
    minSessionChaos: 0.3, maxSessionChaos: 0.9,
    minUnexpectedEvents: 3, maxUnexpectedEvents: 8,
    minTimeSpent: 180, maxTimeSpent: 2400,
    phrases: [
      "An unexpected win. The brain did that?",
      "Correct. Even the system is pleasantly surprised.",
      "An unexpected victory. Let us celebrate quietly.",
      "Correct. A beautiful, surprising flash of memory.",
      "A win. Your guess was highly elegant.",
      "Correct. An unexpected but highly precise answer.",
      "A surprising win. The mind is deep.",
      "Correct. That was a highly impressive recall.",
      "An unexpected victory. The letters are pleased.",
      "Correct. A beautiful, silent triumph of memory.",
      "A surprising win. Let's keep this secret.",
      "Correct. The brain pulled that from nowhere.",
      "An unexpected win. A very precise stroke.",
      "Correct. A highly creative, successful recall today.",
      "A surprising victory. The deck is stunned.",
      "Correct. You made that look extremely easy.",
      "An unexpected win. The definition is caught.",
      "Correct. A quiet, beautiful flash of brilliance.",
      "A surprising win. The database is impressed.",
      "Correct. A highly elegant, unexpected vocabulary triumph.",
      "An unexpected victory. The spelling was perfect.",
      "Correct. A highly sophisticated guess proved right.",
      "A surprising win. Memory is a marvel.",
      "Correct. The word is officially in bag.",
      "An unexpected win. A tranquil cognitive miracle.",
      "Correct. You have surprised the quiet queue.",
      "A surprising victory. The definition fell into place.",
      "Correct. An unexpected but beautiful mental click.",
      "An unexpected win. The card yields quietly.",
      "Correct. A very impressive, calm vocabulary victory.",
      "A surprising win. Your focus is powerful.",
      "Correct. The letters bowed to your mind.",
      "An unexpected victory. A very quiet success.",
      "Correct. The memory bank is highly rich.",
      "A surprising win. The definition is conquered."
    ]
  },
  rare_behavior_detected: {
    dialogue_category: "rare_behavior_detected",
    emotion: "RARE_BEHAVIOR_DETECTED",
    minFunnyScore: 0.0, maxFunnyScore: 0.5,
    minSessionChaos: 0.4, maxSessionChaos: 1.0,
    minUnexpectedEvents: 2, maxUnexpectedEvents: 10,
    minTimeSpent: 300, maxTimeSpent: 3600,
    phrases: [
      "Rare behavior detected. The mind is shifting.",
      "An unusual pattern has been quietly observed.",
      "A rare cognitive sequence has occurred today.",
      "Unusual learning behavior detected. Keep going calmly.",
      "A highly unique review pattern is unfolding.",
      "Rare behavior. The memory is highly active.",
      "An exceptional study pattern has been logged.",
      "Unusual cognitive choices. Highly interesting to observe.",
      "A rare, beautiful sequence of correct answers.",
      "Uncommon learning behavior. Let us proceed quietly.",
      "A highly distinct study rhythm is happening.",
      "Rare behavior detected. The brain is humming.",
      "An unusual, quiet connection has been made.",
      "Uncommon cognitive flow. Highly sophisticated study session.",
      "A rare learning pattern is taking shape.",
      "Unusual consistency today. A very quiet power.",
      "A highly unique vocabulary path is detected.",
      "Rare behavior. Your mind is highly focused.",
      "An exceptional study flow is quietly active.",
      "Uncommon learning decisions. A peaceful mind works.",
      "A highly unique sequence of memory recalls.",
      "Rare behavior detected. The spelling is flawless.",
      "An unusual, beautiful focus is on display.",
      "Uncommon cognitive speed today. A calm river.",
      "A rare, elegant learning trajectory is active.",
      "Unusual pattern detected. The dictionary is impressed.",
      "A highly distinct memory sequence has occurred.",
      "Rare behavior. Your reviews are exceptionally calm.",
      "An unusual, peaceful focus has settled in.",
      "Uncommon learning stability. A beautiful data node.",
      "A rare, highly precise review style today.",
      "Unusual mental stamina. Keep the pace gentle.",
      "A highly unique study signature is recorded.",
      "Rare behavior detected. The cards are flying.",
      "An exceptional, quiet learning curve is visible."
    ]
  },
  rare_insight_moment: {
    dialogue_category: "rare_insight_moment",
    emotion: "QUIRKY_INSIGHT",
    minFunnyScore: 0.3, maxFunnyScore: 0.9,
    minSessionChaos: 0.2, maxSessionChaos: 0.8,
    minUnexpectedEvents: 1, maxUnexpectedEvents: 5,
    minTimeSpent: 120, maxTimeSpent: 1800,
    phrases: [
      "A rare flash of quiet linguistic insight.",
      "An unusual, beautiful insight has occurred today.",
      "A quirky insight. The word is unlocked.",
      "A rare moment of absolute semantic clarity.",
      "An exceptional insight. The letters speak clearly.",
      "A quirky, beautiful flash of vocabulary understanding.",
      "A rare insight. The definition is yours.",
      "An unusual, highly precise grasp of meaning.",
      "A quirky insight. Memory has succeeded quietly.",
      "A rare moment of deep vocabulary connection.",
      "An exceptional insight. The mind is clear.",
      "A quirky flash of perfect linguistic alignment.",
      "A rare insight. The spelling is logical.",
      "An unusual, beautiful understanding has settled in.",
      "A quirky insight. The card is cleared.",
      "A rare moment of serene cognitive breakthrough.",
      "An exceptional insight. The dictionary makes sense.",
      "A quirky, quiet flash of word mastery.",
      "A rare insight. Your focus is deep.",
      "An unusual, highly elegant linguistic insight today.",
      "A quirky flash of semantic precision occurred.",
      "A rare moment of complete vocabulary peace.",
      "An exceptional insight. The letters are aligned.",
      "A quirky, beautiful connection of word definitions.",
      "A rare insight. The vocabulary is conquered.",
      "An unusual flash of serene mental clarity.",
      "A quirky insight. The spelling is yours.",
      "A rare moment of deep learning harmony.",
      "An exceptional, quiet flash of cognitive insight.",
      "A quirky insight. The word is settled.",
      "A rare moment of absolute, calm clarity.",
      "An unusual, beautiful breakthrough of semantic knowledge.",
      "A quirky insight. The deck is moving.",
      "A rare flash of quiet, confident memory.",
      "An exceptional insight. Your mind is quiet."
    ]
  },
  rare_learning_glitch: {
    dialogue_category: "rare_learning_glitch",
    emotion: "UNEXPECTED_PATTERN",
    minFunnyScore: 0.1, maxFunnyScore: 0.6,
    minSessionChaos: 0.5, maxSessionChaos: 1.0,
    minUnexpectedEvents: 2, maxUnexpectedEvents: 8,
    minTimeSpent: 60, maxTimeSpent: 1200,
    phrases: [
      "A minor cognitive glitch. Let's restart.",
      "A quiet learning glitch. No worries, friend.",
      "The letters had a very small glitch.",
      "A gentle cognitive hiccup. Keep moving forward.",
      "A rare learning glitch. The brain rebooted.",
      "A quiet, harmless glitch in our reviews.",
      "The spelling had a temporary minor glitch.",
      "A soft learning glitch. Let's adjust.",
      "A gentle glitch. The definition is safe.",
      "A temporary cognitive detour has occurred quietly.",
      "A rare learning glitch. A quiet pause.",
      "The dictionary had a very small glitch.",
      "A gentle, harmless slip of the letters.",
      "A quiet glitch. The database remains stable.",
      "A soft learning hiccup. Settle back down.",
      "A rare cognitive glitch. Let's breathe.",
      "The memory node had a minor glitch.",
      "A quiet, gentle shift in the spelling.",
      "A harmless learning glitch. Let's review.",
      "A soft glitch. The letters are fine.",
      "A rare cognitive hiccup. Keep the calm.",
      "The vocabulary system had a quiet glitch.",
      "A gentle, harmless learning detour occurred today.",
      "A quiet glitch. The spelling is resting.",
      "A soft cognitive slip. Let's continue.",
      "A rare learning glitch. Settle your mind.",
      "The dictionary had a quiet, gentle blink.",
      "A harmless glitch. The meaning is intact.",
      "A soft learning detour. Keep things peaceful.",
      "A rare cognitive blink. The word waits.",
      "The memory buffer had a quiet glitch.",
      "A gentle, harmless slip of vocabulary nodes.",
      "A quiet glitch. Let's move along.",
      "A soft learning pause. Nothing is lost.",
      "A rare cognitive detour. A clean slate."
    ]
  },
  rare_surprise_success: {
    dialogue_category: "rare_surprise_success",
    emotion: "FUNNY_LIGHT_SURPRISE",
    minFunnyScore: 0.5, maxFunnyScore: 1.0,
    minSessionChaos: 0.4, maxSessionChaos: 1.0,
    minUnexpectedEvents: 3, maxUnexpectedEvents: 10,
    minTimeSpent: 240, maxTimeSpent: 3000,
    phrases: [
      "A surprise success. The letters aligned perfectly.",
      "A highly unexpected, beautiful flash of success.",
      "Surprise success. Even the deck is smiling.",
      "A quiet, surprising victory for your memory.",
      "A surprise success. A very elegant recall.",
      "An unexpected, beautiful triumph has been recorded.",
      "Surprise success. The definition clicked beautifully today.",
      "A quiet, surprising flash of perfect recall.",
      "A surprise success. The mind is deep.",
      "An unexpected, highly precise vocabulary win today.",
      "Surprise success. Let's celebrate with silence.",
      "A quiet, surprising victory of spelling today.",
      "A surprise success. The letters are stable.",
      "An unexpected, beautiful breakthrough of linguistic memory.",
      "Surprise success. You made that look easy.",
      "A quiet, surprising flash of vocabulary mastery.",
      "A surprise success. Settle back into focus.",
      "An unexpected, highly elegant recall occurred today.",
      "Surprise success. Your mind is quiet, powerful.",
      "A quiet, surprising triumph of semantic knowledge.",
      "A surprise success. The card has surrendered.",
      "An unexpected, beautiful click in your memory.",
      "Surprise success. The dictionary is pleased today.",
      "A quiet, surprising victory. A peaceful mind.",
      "A surprise success. The spelling is correct.",
      "An unexpected, highly precise recall has happened.",
      "Surprise success. Your study path is stable.",
      "A quiet, surprising flash of cognitive clarity.",
      "A surprise success. The letters aligned nicely.",
      "An unexpected, beautiful vocabulary success today, friend.",
      "Surprise success. Memory has performed beautifully.",
      "A quiet, surprising breakthrough in your reviews.",
      "A surprise success. The concept is locked.",
      "An unexpected, highly elegant triumph of spelling.",
      "Surprise success. Settle into this quiet win."
    ]
  },
  rare_pattern_break: {
    dialogue_category: "rare_pattern_break",
    emotion: "UNEXPECTED_PATTERN",
    minFunnyScore: 0.2, maxFunnyScore: 0.7,
    minSessionChaos: 0.3, maxSessionChaos: 0.9,
    minUnexpectedEvents: 2, maxUnexpectedEvents: 7,
    minTimeSpent: 180, maxTimeSpent: 2400,
    phrases: [
      "A rare pattern break. Something has shifted.",
      "An unusual break in our study pattern.",
      "A quiet pattern break has occurred today.",
      "A unique break in your study rhythm.",
      "A rare pattern break. Highly interesting indeed.",
      "An uncommon shift in your learning pattern.",
      "A quiet, gentle break in the routine.",
      "A unique pattern break. The mind adapts.",
      "A rare pattern break. Settle back calmly.",
      "An unusual shift in our daily trajectory.",
      "A quiet pattern break. Let us observe.",
      "A unique break in the spelling flow.",
      "A rare pattern break. The deck is fresh.",
      "An uncommon shift in your cognitive path.",
      "A quiet, beautiful break in our routine.",
      "A unique pattern break. Memory is flexible.",
      "A rare pattern break. Keep going peacefully.",
      "An unusual shift in today's review flow.",
      "A quiet pattern break. The mind is alert.",
      "A unique break in the learning sequence.",
      "A rare pattern break. Let's keep moving.",
      "An uncommon shift in your study style.",
      "A quiet, gentle break in the sequence.",
      "A unique pattern break. Settle your mind.",
      "A rare pattern break. A fresh perspective.",
      "An unusual shift in our vocabulary deck.",
      "A quiet pattern break. The letters are shifting.",
      "A unique break in the cognitive routine.",
      "A rare pattern break. A calm adaptation.",
      "An uncommon shift in today's review pattern.",
      "A quiet pattern break. Settle back down.",
      "A unique break in your daily habits.",
      "A rare pattern break. Pure, quiet interest.",
      "An unusual shift in our learning flow.",
      "A quiet pattern break. Let's keep balance."
    ]
  },
  rare_micro_joke: {
    dialogue_category: "rare_micro_joke",
    emotion: "SOFT_HUMOR",
    minFunnyScore: 0.3, maxFunnyScore: 0.8,
    minSessionChaos: 0.1, maxSessionChaos: 0.6,
    minUnexpectedEvents: 1, maxUnexpectedEvents: 4,
    minTimeSpent: 90, maxTimeSpent: 1200,
    phrases: [
      "The letters are having a quiet conversation.",
      "This word has a very dry humor.",
      "A tiny, quiet joke from the dictionary.",
      "The definition is smiling in the background.",
      "A micro joke. The letters are playful today.",
      "The vocabulary system shares a quiet laugh.",
      "A tiny, silent joke from our deck.",
      "This spelling is highly amused by memory.",
      "A micro joke. Try not to laugh.",
      "The letters are playing a quiet trick.",
      "A tiny, peaceful joke from today's list.",
      "The dictionary is sharing a subtle smile.",
      "A micro joke. The word is pleased.",
      "The spelling is having a quiet chuckle.",
      "A tiny, silent joke from the database.",
      "The letters are in a very good mood.",
      "A micro joke. A gentle, dry observation.",
      "The dictionary has a very quiet humor.",
      "A tiny, peaceful joke to lighten recall.",
      "The spelling has a subtle, playful nature.",
      "A micro joke. Settle into the amusement.",
      "The letters are whispers of quiet fun.",
      "A tiny, silent joke to keep balance.",
      "The dictionary is smiling at our progress.",
      "A micro joke. The spelling is playful.",
      "The letters are having a silent laugh.",
      "A tiny, peaceful joke from your deck.",
      "The spelling shares a quiet, dry humor.",
      "A micro joke. Keep the atmosphere relaxed.",
      "The letters are playing a gentle game.",
      "A tiny, silent joke. No pressure here.",
      "The dictionary is highly amused by today.",
      "A micro joke. The letters are resting.",
      "The spelling has a quiet, dry smile.",
      "A tiny, peaceful joke from the dictionary."
    ]
  },
  rare_context_shift: {
    dialogue_category: "rare_context_shift",
    emotion: "ODD_CONNECTION",
    minFunnyScore: 0.2, maxFunnyScore: 0.7,
    minSessionChaos: 0.4, maxSessionChaos: 0.9,
    minUnexpectedEvents: 2, maxUnexpectedEvents: 8,
    minTimeSpent: 150, maxTimeSpent: 1800,
    phrases: [
      "A quiet, sudden shift in semantic context.",
      "The word has moved to fresh territory.",
      "A rare context shift. The meaning adjusts.",
      "A unique shift in vocabulary context today.",
      "The definition is looking at different sky.",
      "A quiet, gentle shift in word context.",
      "The word is wearing a new coat.",
      "A rare context shift. The mind adapts.",
      "A unique shift in the sentence structure.",
      "The meaning has taken a quiet turn.",
      "A rare context shift. Highly elegant, friend.",
      "The word is exploring a new neighborhood.",
      "A quiet, sudden shift in semantic flow.",
      "The definition has traveled to new place.",
      "A rare context shift. Keep focus clear.",
      "A unique shift in today's word landscape.",
      "The letters are resting in fresh context.",
      "A quiet, gentle transition of semantic context.",
      "The word is looking from another window.",
      "A rare context shift. A beautiful view.",
      "A unique shift in word placement today.",
      "The meaning is flowing down new path.",
      "A quiet, sudden shift in today's grammar.",
      "The definition has adjusted its quiet posture.",
      "A rare context shift. Settle back down.",
      "A unique shift in our study perspective.",
      "The word is resting in cozy sentence.",
      "A quiet, gentle shift of word meaning.",
      "The definition has shifted its focus slightly.",
      "A rare context shift. A quiet adventure.",
      "A unique shift in today's review landscape.",
      "The meaning is exploring fresh semantic ground.",
      "A quiet, sudden shift in vocabulary placement.",
      "The word has settled into new home.",
      "A rare context shift. A peaceful transition."
    ]
  },
  rare_session_anomaly: {
    dialogue_category: "rare_session_anomaly",
    emotion: "RARE_MOMENT_AWARENESS",
    minFunnyScore: 0.4, maxFunnyScore: 1.0,
    minSessionChaos: 0.5, maxSessionChaos: 1.0,
    minUnexpectedEvents: 4, maxUnexpectedEvents: 12,
    minTimeSpent: 360, maxTimeSpent: 4800,
    phrases: [
      "A quiet, beautiful anomaly in today's session.",
      "An unusual, serene session event has occurred.",
      "A rare session anomaly. Keep focus steady.",
      "The study session has touched unique node.",
      "An exceptional, quiet anomaly is logged today.",
      "A quiet anomaly. The deck is calm.",
      "An unusual session rhythm has been observed.",
      "A rare session anomaly. Pure, quiet progress.",
      "The learning path has taken beautiful detour.",
      "An exceptional, quiet moment in our session.",
      "A rare session anomaly. No pressure, friend.",
      "An unusual, peaceful review event is recorded.",
      "A quiet anomaly. The letters are aligned.",
      "The study path has touched quiet center.",
      "A rare session anomaly. A beautiful space.",
      "An exceptional, silent event in our deck.",
      "A quiet anomaly. Settle your mind calmly.",
      "An unusual session occurrence. Highly interesting indeed.",
      "A rare session anomaly. The mind is deep.",
      "The study flow has reached unique peak.",
      "An exceptional, gentle anomaly in our reviews.",
      "A quiet anomaly. The dictionary is peaceful.",
      "An unusual session event has settled quietly.",
      "A rare session anomaly. A clean slate.",
      "The study path has touched peaceful horizon.",
      "An exceptional, quiet shift in our deck.",
      "A quiet anomaly. Settle back with ease.",
      "An unusual session rhythm. Keep the calm.",
      "A rare session anomaly. A beautiful moment.",
      "The study flow is exceptionally, quietly rich.",
      "An exceptional, quiet event has been logged.",
      "A quiet anomaly. The spelling is perfect.",
      "An unusual session transition. A peaceful river.",
      "A rare session anomaly. Rest your mind.",
      "An exceptional, gentle progress has been made."
    ]
  }
};

const getCooldown = (rarity) => {
  if (rarity === "epic") return 2000;
  if (rarity === "very_rare") return 1200;
  if (rarity === "rare") return 800;
  return 400;
};

const generatedDialogues = [];

Object.keys(funnyRareCategories).forEach(catKey => {
  const cat = funnyRareCategories[catKey];
  cat.phrases.forEach((phrase, index) => {
    // Determine rarity & weight
    let rarity = "common";
    let weight = 100;
    
    if (index >= 34) {
      rarity = "epic";
      weight = 1;
    } else if (index >= 32) {
      rarity = "very_rare";
      weight = 10;
    } else if (index >= 29) {
      rarity = "rare";
      weight = 50;
    }

    const paddedIndex = String(index + 1).padStart(3, '0');
    const id = `${catKey}_${paddedIndex}`;

    generatedDialogues.push({
      id: id,
      text: phrase,
      emotion: cat.emotion,
      category: cat.dialogue_category,
      priority: 30, // Custom priority for funny and rare dialogue triggers
      weight: weight,
      rarity: rarity,

      // Metadata required per item (exactly as requested in spec)
      minFunnyScore: cat.minFunnyScore,
      maxFunnyScore: cat.maxFunnyScore,
      minSessionChaos: cat.minSessionChaos,
      maxSessionChaos: cat.maxSessionChaos,
      minUnexpectedEvents: cat.minUnexpectedEvents,
      maxUnexpectedEvents: cat.maxUnexpectedEvents,
      minTimeSpent: cat.minTimeSpent,
      maxTimeSpent: cat.maxTimeSpent,

      enabled: true,
      cooldown: getCooldown(rarity),
      language: "en",
      tags: ["funny_rare", catKey, rarity]
    });
  });
});

// Load the existing dialogues file to merge (using accurate path inside project root)
const enDialoguesPath = './app/src/main/assets/tiki_dialogues/dialogues_en.json';
let existingLibrary = { version: "1.0", language: "en", dialogues: [] };

if (fs.existsSync(enDialoguesPath)) {
  try {
    existingLibrary = JSON.parse(fs.readFileSync(enDialoguesPath, 'utf8'));
    console.log(`Loaded ${existingLibrary.dialogues.length} existing dialogues.`);
  } catch (e) {
    console.error("Error parsing dialogues_en.json:", e.message);
  }
} else {
  console.log("No existing dialogues_en.json found. Creating a fresh one.");
}

// Filter out any previous phase funny rare dialogues to avoid duplicates
const funnyRareKeys = Object.keys(funnyRareCategories);
const otherDialogues = existingLibrary.dialogues.filter(d => {
  return !funnyRareKeys.some(key => d.id.startsWith(key));
});
console.log(`Retained ${otherDialogues.length} dialogues.`);

// Merge them
const fullDialoguesList = [...otherDialogues, ...generatedDialogues];

const finalLibrary = {
  version: "1.0",
  language: "en",
  dialogues: fullDialoguesList
};

// Strict Validations
console.log("\n=== RUNNING STRICT FUNNY & RARE LIBRARY VALIDATIONS ===");

const uniqueIds = new Set();
const uniqueTexts = new Set();
const duplicateIds = [];
const duplicateTexts = [];
const wordCountErrors = [];
const categoryErrors = [];
const emotionErrors = [];
const metadataKeyErrors = [];

const validCategories = new Set([
  "Greeting", "Encouragement", "Celebration", "Failure", "Recovery", 
  "Thinking", "LongThinking", "Again", "AgainStreak", "Easy", 
  "EasyStreak", "Good", "Hard", "HalfSession", "SessionComplete", 
  "Motivation", "Reminder", "Idle", "Silence",
  "session_start_flow", "session_mid_progress", "session_soft_checkpoint",
  "session_continuation_hint", "session_progress_update", "session_near_completion",
  "session_completion_ready", "session_flow_stable", "session_light_pause_return",
  "session_session_smoothing",
  "finish_first_close", "finish_soft_end", "finish_complete_state",
  "finish_neutral_exit", "finish_flow_release", "finish_session_reset",
  "finish_completion_ack", "finish_stable_end", "finish_light_wrap",
  "finish_silent_close",
  "streak_first_start", "streak_growth_step", "streak_continuation",
  "streak_milestone_reached", "streak_long_chain", "achievement_unlock_basic",
  "achievement_unlock_advanced", "achievement_progress_track", "achievement_consistency_high",
  "achievement_mastery_path", "reward_soft_ack", "reward_neutral_gain",
  "reward_stable_progress", "reward_long_term", "reward_session_chain",
  "master_word_first_contact", "master_word_recognition", "master_word_recall",
  "master_word_precision", "master_word_confidence", "master_word_retention",
  "master_word_context_fit", "master_word_deep_link", "master_word_stability",
  "master_word_completion",
  "empty_first_state", "empty_after_review", "empty_session_end",
  "empty_all_clear", "empty_waiting_mode", "empty_soft_pause",
  "empty_stable_idle", "empty_ready_next", "empty_flow_reset",
  "empty_silent_hold",
  "goal_first_setup", "goal_adjustment", "goal_progress_check",
  "goal_soft_reminder", "goal_direction_shift", "coaching_first_hint",
  "coaching_mid_support", "coaching_focus_reset", "coaching_path_correction",
  "coaching_stability",
  "funny_first_contact", "funny_repeat_attempt", "funny_chain_reaction",
  "funny_confusion_flip", "funny_success_twist", "funny_failure_light",
  "funny_unexpected_win", "rare_behavior_detected", "rare_insight_moment",
  "rare_learning_glitch", "rare_surprise_success", "rare_pattern_break",
  "rare_micro_joke", "rare_context_shift", "rare_session_anomaly"
]);

const validEmotions = new Set([
  "HAPPY", "SAD", "CURIOUS", "POKER", "THINKING", "SLEEPY",
  "SESSION_FLOW_AWARENESS", "SESSION_MICRO_PROGRESS", "SESSION_SOFT_MILESTONE",
  "SESSION_CONTINUATION_STATE", "SESSION_NEUTRAL_UPDATE", "SESSION_LIGHT_RECOGNITION",
  "SESSION_PROGRESS_STABILITY", "SESSION_FOCUS_CONTINUITY", "SESSION_LOW_PRESSURE_MOTIVATION",
  "SESSION_SESSION_SMOOTHING",
  "SESSION_END_CALM", "SESSION_COMPLETION_SOFT", "SESSION_WRAP_NEUTRAL",
  "SESSION_EXIT_FLOW", "SESSION_FINISH_STABLE", "SESSION_LIGHT_CLOSURE",
  "SESSION_RESET_READY", "SESSION_END_AWARENESS", "SESSION_GENTLE_FINISH",
  "SESSION_SILENT_COMPLETION",
  "STREAK_STABLE_GROWTH", "ACHIEVEMENT_SOFT_UNLOCK", "CONSISTENCY_RECOGNITION",
  "HABIT_REINFORCEMENT", "PROGRESS_MILESTONE_CALM", "EFFORT_CONTINUITY",
  "LONG_TERM_SUPPORT", "QUIET_PRIDE", "STEADY_MASTERY", "LOW_PRESSURE_REWARD",
  "MASTERY_RECOGNITION_SOFT", "VOCABULARY_PRECISION", "SEMANTIC_DEPTH_AWARENESS",
  "LINGUISTIC_CLARITY", "ADVANCED_UNDERSTANDING", "RARE_WORD_ACKNOWLEDGMENT",
  "KNOWLEDGE_STABILITY", "MEANING_LOCK_STATE", "FINAL_UNDERSTANDING", "QUIET_MASTERY",
  "QUEUE_EMPTY_CALM", "WAITING_NEXT_INPUT", "EMPTY_STATE_STABLE", "REVIEW_COMPLETE_SILENT",
  "NO_TASKS_AWARENESS", "PAUSE_BETWEEN_CYCLES", "NEUTRAL_COMPLETION_SPACE", "SOFT_IDLE_READINESS",
  "LEARNING_BUFFER_CLEAR", "GENTLE_STOP_STATE",
  "GOAL_AWARENESS", "COACHING_SOFT_DIRECTION", "PATH_GUIDANCE", "FOCUS_ALIGNMENT",
  "PROGRESS_ORIENTATION", "HABIT_SUPPORT", "GENTLE_PLANNING", "DIRECTION_CLARITY",
  "STEADY_MOTIVATION", "QUIET_STRUCTURE",
  "FUNNY_LIGHT_SURPRISE", "RARE_BEHAVIOR_DETECTED", "QUIRKY_INSIGHT", "UNEXPECTED_PATTERN",
  "SOFT_HUMOR", "ODD_CONNECTION", "PLAYFUL_OBSERVATION", "STRANGE_CLARITY",
  "RARE_MOMENT_AWARENESS", "GENTLE_LAUGHTER_STATE"
]);

const requiredKeys = [
  "id", "text", "emotion", "category", "weight", "rarity",
  "minFunnyScore", "maxFunnyScore", "minSessionChaos", "maxSessionChaos",
  "minUnexpectedEvents", "maxUnexpectedEvents", "minTimeSpent", "maxTimeSpent",
  "enabled", "cooldown", "language"
];

let totalWordCount = 0;

generatedDialogues.forEach(d => {
  // Check unique ID
  if (uniqueIds.has(d.id)) {
    duplicateIds.push(d.id);
  }
  uniqueIds.add(d.id);

  // Check unique text
  const cleanText = d.text.trim().toLowerCase();
  if (uniqueTexts.has(cleanText)) {
    duplicateTexts.push(d.text);
  }
  uniqueTexts.add(cleanText);

  // Check word count (3-12 words average, max 16 words)
  const words = d.text.split(/\s+/).filter(w => w.length > 0);
  totalWordCount += words.length;
  if (words.length < 3 || words.length > 16) {
    wordCountErrors.push({ id: d.id, text: d.text, count: words.length });
  }

  // Check category
  if (!validCategories.has(d.category)) {
    categoryErrors.push({ id: d.id, category: d.category });
  }

  // Check emotion
  if (!validEmotions.has(d.emotion.toUpperCase())) {
    emotionErrors.push({ id: d.id, emotion: d.emotion });
  }

  // Check required keys
  requiredKeys.forEach(k => {
    if (d[k] === undefined) {
      metadataKeyErrors.push({ id: d.id, key: k });
    }
  });
});

const avgWordCount = totalWordCount / generatedDialogues.length;

console.log(`Total FUNNY & RARE dialogues generated: ${generatedDialogues.length}`);
console.log(`Unique Categories of FUNNY & RARE: ${funnyRareKeys.length}`);
console.log(`Duplicate IDs: ${duplicateIds.length}`);
console.log(`Duplicate Texts: ${duplicateTexts.length}`);
console.log(`Word Count Errors (outside 3-16 words): ${wordCountErrors.length}`);
console.log(`Category Violations: ${categoryErrors.length}`);
console.log(`Emotion Violations: ${emotionErrors.length}`);
console.log(`Missing FUNNY & RARE Metadata Keys: ${metadataKeyErrors.length}`);
console.log(`Average Word Count: ${avgWordCount.toFixed(2)} words (Target: 3-12 average)`);

if (duplicateIds.length === 0 && duplicateTexts.length === 0 && wordCountErrors.length === 0 && categoryErrors.length === 0 && emotionErrors.length === 0 && metadataKeyErrors.length === 0 && generatedDialogues.length >= 500) {
  // Write to asset directory
  fs.writeFileSync(enDialoguesPath, JSON.stringify(finalLibrary, null, 2), 'utf8');
  console.log(`\nSUCCESS: Merged and wrote ${finalLibrary.dialogues.length} total dialogues to ${enDialoguesPath}`);
  process.exit(0);
} else {
  console.error("\nVALIDATION FAILED! Check error counts above.");
  if (duplicateTexts.length > 0) console.log("Duplicate Texts found:", duplicateTexts);
  if (wordCountErrors.length > 0) console.log("Word count errors:", wordCountErrors);
  process.exit(1);
}
