package com.example.core.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // 1. User Progress (user_profile)
    @Query("SELECT * FROM user_profile WHERE id = 0")
    fun getUserProgress(): Flow<UserProgressEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 0")
    suspend fun getUserProgressOnce(): UserProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProgress(progress: UserProgressEntity)

    @Update
    suspend fun updateUserProgress(progress: UserProgressEntity)

    @Query("UPDATE user_profile SET xp = xp + :xpEarned WHERE id = 0")
    suspend fun addXp(xpEarned: Int)

    @Query("UPDATE user_profile SET level = :newLevel WHERE id = 0")
    suspend fun updateLevel(newLevel: Int)

    // 2. Spaced Repetition Cards (review_cards)
    @Query("SELECT * FROM review_cards")
    fun getAllCards(): Flow<List<CardEntity>>

    @Query("SELECT * FROM review_cards")
    suspend fun getAllCardsOnce(): List<CardEntity>

    @Query("SELECT * FROM review_cards WHERE boxIndex = :boxIndex")
    fun getCardsByBox(boxIndex: Int): Flow<List<CardEntity>>

    @Query("SELECT * FROM review_cards WHERE id = :id")
    suspend fun getCardById(id: Int): CardEntity?

    @Query("SELECT * FROM review_cards WHERE wordId = :wordId")
    suspend fun getCardByWordId(wordId: Int): CardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CardEntity)

    @Update
    suspend fun updateCard(card: CardEntity)

    @Delete
    suspend fun deleteCard(card: CardEntity)

    @Query("SELECT * FROM review_cards WHERE dueDate <= :currentTime")
    fun getDueCards(currentTime: Long): Flow<List<CardEntity>>

    @Query("SELECT * FROM review_cards WHERE dueDate <= :currentTime")
    suspend fun getDueCardsOnce(currentTime: Long): List<CardEntity>

    @Query("SELECT COUNT(*) FROM review_cards WHERE boxIndex = :boxIndex")
    fun getCardCountInBox(boxIndex: Int): Flow<Int>

    // 3. Review History (review_history)
    @Query("SELECT * FROM review_history ORDER BY timestamp DESC")
    fun getReviewHistory(): Flow<List<ReviewHistoryEntity>>

    @Query("SELECT * FROM review_history ORDER BY timestamp DESC")
    suspend fun getReviewHistoryOnce(): List<ReviewHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviewLog(log: ReviewHistoryEntity)

    // 4. Custom Boxes (custom_boxes)
    @Query("SELECT * FROM custom_boxes ORDER BY name ASC")
    fun getCustomBoxes(): Flow<List<CustomBoxEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomBox(box: CustomBoxEntity): Long

    @Delete
    suspend fun deleteCustomBox(box: CustomBoxEntity)

    @Update
    suspend fun updateCustomBox(box: CustomBoxEntity)

    @Query("SELECT * FROM custom_boxes WHERE id = :id")
    suspend fun getCustomBoxById(id: Int): CustomBoxEntity?

    @Query("UPDATE custom_boxes SET name = :newName WHERE id = :id")
    suspend fun updateCustomBoxName(id: Int, newName: String)

    // 5. Box Words (box_words)
    @Query("SELECT * FROM box_words WHERE boxId = :boxId ORDER BY addedAt DESC")
    fun getWordsInCustomBox(boxId: Int): Flow<List<BoxWordEntity>>

    @Query("SELECT * FROM box_words WHERE boxId = :boxId ORDER BY addedAt DESC")
    suspend fun getWordsInCustomBoxOnce(boxId: Int): List<BoxWordEntity>

    @Query("SELECT * FROM box_words WHERE id = :id")
    suspend fun getBoxWordById(id: Int): BoxWordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoxWord(boxWord: BoxWordEntity)

    @Update
    suspend fun updateBoxWord(boxWord: BoxWordEntity)

    @Query("DELETE FROM box_words WHERE boxId = :boxId AND wordId = :wordId")
    suspend fun deleteBoxWord(boxId: Int, wordId: Int)

    @Query("DELETE FROM box_words WHERE id = :id")
    suspend fun deleteBoxWordById(id: Int)

    @Query("DELETE FROM box_words WHERE boxId = :boxId")
    suspend fun deleteAllWordsInCustomBox(boxId: Int)

    // 5.5. Favorite Words (favorite_words)
    @Query("SELECT * FROM favorite_words ORDER BY addedAt DESC")
    fun getFavoriteWords(): Flow<List<FavoriteWordEntity>>

    @Query("SELECT * FROM favorite_words ORDER BY addedAt DESC")
    suspend fun getFavoriteWordsOnce(): List<FavoriteWordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteWord(favorite: FavoriteWordEntity)

    @Query("DELETE FROM favorite_words WHERE word = :word")
    suspend fun deleteFavoriteWord(word: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_words WHERE word = :word)")
    fun isFavoriteWordFlow(word: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_words WHERE word = :word)")
    suspend fun isFavoriteWord(word: String): Boolean

    // 5.6. Recent Searches (recent_searches)
    @Query("SELECT * FROM recent_searches ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSearches(limit: Int): Flow<List<RecentSearchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentSearch(search: RecentSearchEntity)

    @Query("DELETE FROM recent_searches WHERE `query` = :query")
    suspend fun deleteRecentSearch(query: String)

    @Query("DELETE FROM recent_searches")
    suspend fun clearRecentSearches()

    // 6. Achievements (achievements)
    @Query("SELECT * FROM achievements")
    fun getAchievements(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Query("UPDATE achievements SET unlocked = 1, unlockedAt = :timestamp WHERE id = :id")
    suspend fun unlockAchievement(id: String, timestamp: Long)

    // 7. Challenges (challenges)
    @Query("SELECT * FROM challenges")
    fun getChallenges(): Flow<List<ChallengeEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertChallenges(challenges: List<ChallengeEntity>)

    @Query("UPDATE challenges SET current = :current, completed = :completed WHERE id = :id")
    suspend fun updateChallengeProgress(id: String, current: Int, completed: Boolean)

    // 8. Statistics (daily_stats)
    @Query("SELECT * FROM daily_stats WHERE dateStr = :dateStr")
    suspend fun getStatisticsForDate(dateStr: String): StatisticsEntity?

    @Query("SELECT * FROM daily_stats ORDER BY dateStr DESC")
    fun getAllStatistics(): Flow<List<StatisticsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatistics(statistics: StatisticsEntity)

    // 9. Learning Sessions (learning_sessions)
    @Query("SELECT * FROM learning_sessions WHERE id = 0")
    fun getSessionState(): Flow<SessionStateEntity?>

    @Query("SELECT * FROM learning_sessions WHERE id = 0")
    suspend fun getSessionStateOnce(): SessionStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSessionState(sessionState: SessionStateEntity)

    // 10. Settings (settings)
    @Query("SELECT value FROM settings WHERE `key` = :key")
    suspend fun getSettingValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: SettingEntity)

    // 11. Reward History (reward_history)
    @Query("SELECT * FROM reward_history ORDER BY timestamp DESC")
    fun getRewardHistory(): Flow<List<RewardHistoryEntity>>

    @Query("SELECT * FROM reward_history ORDER BY timestamp DESC")
    suspend fun getRewardHistoryOnce(): List<RewardHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRewardLog(reward: RewardHistoryEntity)

    @Query("DELETE FROM reward_history WHERE id = :id")
    suspend fun deleteRewardLog(id: Int)

    @Query("DELETE FROM reward_history")
    suspend fun clearRewardHistory()

    // 12. Additional Challenge and Achievement once-queries
    @Query("SELECT * FROM challenges")
    suspend fun getChallengesOnce(): List<ChallengeEntity>

    @Query("SELECT * FROM challenges WHERE id = :id")
    suspend fun getChallengeById(id: String): ChallengeEntity?

    @Query("SELECT * FROM achievements")
    suspend fun getAchievementsOnce(): List<AchievementEntity>

    @Query("SELECT * FROM achievements WHERE id = :id")
    suspend fun getAchievementById(id: String): AchievementEntity?
}
