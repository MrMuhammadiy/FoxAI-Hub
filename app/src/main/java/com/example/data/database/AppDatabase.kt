package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        User::class,
        Task::class,
        Habit::class,
        MoodLog::class,
        FlashcardDeck::class,
        Flashcard::class,
        LearningLog::class,
        FoxAiMessage::class,
        ThreeWins::class,
        DailyReflection::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun moodLogDao(): MoodLogDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun learningLogDao(): LearningLogDao
    abstract fun foxAiMessageDao(): FoxAiMessageDao
    abstract fun threeWinsDao(): ThreeWinsDao
    abstract fun dailyReflectionDao(): DailyReflectionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "daily_hub_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
