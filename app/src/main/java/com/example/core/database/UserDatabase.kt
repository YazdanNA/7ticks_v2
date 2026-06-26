package com.example.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import com.example.core.learning.engine.SmartReviewLogEntity
import com.example.core.learning.engine.SmartDailyStatsEntity
import com.example.core.learning.engine.SmartSessionStatsEntity
import com.example.core.learning.engine.SmartSessionDao

@Database(
    entities = [
        UserProgressEntity::class,
        CardEntity::class,
        ReviewHistoryEntity::class,
        CustomBoxEntity::class,
        BoxWordEntity::class,
        AchievementEntity::class,
        ChallengeEntity::class,
        StatisticsEntity::class,
        SessionStateEntity::class,
        SettingEntity::class,
        RewardHistoryEntity::class,
        FavoriteWordEntity::class,
        RecentSearchEntity::class,
        SmartReviewLogEntity::class,
        SmartDailyStatsEntity::class,
        SmartSessionStatsEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class UserDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun smartSessionDao(): SmartSessionDao

    companion object {
        @Volatile
        private var INSTANCE: UserDatabase? = null

        fun getDatabase(context: Context): UserDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserDatabase::class.java,
                    "user_data.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
