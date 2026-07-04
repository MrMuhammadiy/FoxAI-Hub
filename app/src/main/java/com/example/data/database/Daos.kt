package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: Int): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY isCompleted ASC, priority DESC, id DESC")
    fun getTasksForUser(userId: Int): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE userId = :userId ORDER BY id DESC")
    fun getHabitsForUser(userId: Int): Flow<List<Habit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit)

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)
}

@Dao
interface MoodLogDao {
    @Query("SELECT * FROM mood_logs WHERE userId = :userId ORDER BY timestamp DESC")
    fun getMoodLogsForUser(userId: Int): Flow<List<MoodLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoodLog(moodLog: MoodLog)

    @Delete
    suspend fun deleteMoodLog(moodLog: MoodLog)
}

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcard_decks WHERE userId = :userId ORDER BY id DESC")
    fun getDecksForUser(userId: Int): Flow<List<FlashcardDeck>>

    @Query("SELECT * FROM flashcard_decks WHERE id = :deckId LIMIT 1")
    suspend fun getDeckById(deckId: Int): FlashcardDeck?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: FlashcardDeck): Long

    @Delete
    suspend fun deleteDeck(deck: FlashcardDeck)

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId ORDER BY id DESC")
    fun getFlashcardsForDeck(deckId: Int): Flow<List<Flashcard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: Flashcard)

    @Update
    suspend fun updateFlashcard(flashcard: Flashcard)

    @Delete
    suspend fun deleteFlashcard(flashcard: Flashcard)
}

@Dao
interface LearningLogDao {
    @Query("SELECT * FROM learning_logs WHERE userId = :userId ORDER BY timestamp DESC")
    fun getLearningLogsForUser(userId: Int): Flow<List<LearningLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLearningLog(log: LearningLog)

    @Delete
    suspend fun deleteLearningLog(log: LearningLog)
}

@Dao
interface FoxAiMessageDao {
    @Query("SELECT * FROM fox_ai_messages WHERE userId = :userId ORDER BY timestamp ASC")
    fun getMessagesForUser(userId: Int): Flow<List<FoxAiMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: FoxAiMessage)

    @Query("DELETE FROM fox_ai_messages WHERE userId = :userId")
    suspend fun clearMessagesForUser(userId: Int)
}

@Dao
interface ThreeWinsDao {
    @Query("SELECT * FROM three_wins WHERE userId = :userId AND date = :date LIMIT 1")
    fun getThreeWinsForUserAndDate(userId: Int, date: String): Flow<ThreeWins?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThreeWins(threeWins: ThreeWins)
}

@Dao
interface DailyReflectionDao {
    @Query("SELECT * FROM daily_reflections WHERE userId = :userId AND date = :date LIMIT 1")
    fun getReflectionForUserAndDate(userId: Int, date: String): Flow<DailyReflection?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReflection(reflection: DailyReflection)
}
