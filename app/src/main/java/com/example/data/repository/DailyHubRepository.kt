package com.example.data.repository

import com.example.data.database.*
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest
import java.util.UUID

class DailyHubRepository(private val database: AppDatabase) {

    private val userDao = database.userDao()
    private val taskDao = database.taskDao()
    private val habitDao = database.habitDao()
    private val moodLogDao = database.moodLogDao()
    private val flashcardDao = database.flashcardDao()
    private val learningLogDao = database.learningLogDao()
    private val foxAiMessageDao = database.foxAiMessageDao()
    private val threeWinsDao = database.threeWinsDao()
    private val dailyReflectionDao = database.dailyReflectionDao()

    // --- Authentication & User Operations ---

    suspend fun signup(username: String, password: String): Boolean {
        if (username.isBlank() || password.isBlank()) return false
        val existing = userDao.getUserByUsername(username.trim())
        if (existing != null) return false // Username already exists

        val salt = UUID.randomUUID().toString()
        val hashedPassword = hashPassword(password, salt)

        val newUser = User(
            username = username.trim(),
            passwordHash = hashedPassword,
            salt = salt
        )
        val result = userDao.insertUser(newUser)
        return result > 0
    }

    suspend fun login(username: String, password: String): User? {
        val user = userDao.getUserByUsername(username.trim()) ?: return null
        val hashedInput = hashPassword(password, user.salt)
        return if (hashedInput == user.passwordHash) user else null
    }

    fun getUserById(id: Int): Flow<User?> = userDao.getUserById(id)

    suspend fun updateUser(user: User) = userDao.updateUser(user)

    suspend fun deleteUser(user: User) = userDao.deleteUser(user)

    private fun hashPassword(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val saltedInput = password + salt
        val hashBytes = digest.digest(saltedInput.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    // --- Tasks ---

    fun getTasks(userId: Int): Flow<List<Task>> = taskDao.getTasksForUser(userId)

    suspend fun insertTask(task: Task) = taskDao.insertTask(task)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    // --- Habits ---

    fun getHabits(userId: Int): Flow<List<Habit>> = habitDao.getHabitsForUser(userId)

    suspend fun insertHabit(habit: Habit) = habitDao.insertHabit(habit)

    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)

    suspend fun deleteHabit(habit: Habit) = habitDao.deleteHabit(habit)

    // --- Mood Logs ---

    fun getMoodLogs(userId: Int): Flow<List<MoodLog>> = moodLogDao.getMoodLogsForUser(userId)

    suspend fun insertMoodLog(moodLog: MoodLog) = moodLogDao.insertMoodLog(moodLog)

    suspend fun deleteMoodLog(moodLog: MoodLog) = moodLogDao.deleteMoodLog(moodLog)

    // --- Flashcards ---

    fun getDecks(userId: Int): Flow<List<FlashcardDeck>> = flashcardDao.getDecksForUser(userId)

    suspend fun getDeckById(deckId: Int): FlashcardDeck? = flashcardDao.getDeckById(deckId)

    suspend fun insertDeck(deck: FlashcardDeck): Long = flashcardDao.insertDeck(deck)

    suspend fun deleteDeck(deck: FlashcardDeck) = flashcardDao.deleteDeck(deck)

    fun getFlashcards(deckId: Int): Flow<List<Flashcard>> = flashcardDao.getFlashcardsForDeck(deckId)

    suspend fun insertFlashcard(flashcard: Flashcard) = flashcardDao.insertFlashcard(flashcard)

    suspend fun updateFlashcard(flashcard: Flashcard) = flashcardDao.updateFlashcard(flashcard)

    suspend fun deleteFlashcard(flashcard: Flashcard) = flashcardDao.deleteFlashcard(flashcard)

    // --- Learning Logs ---

    fun getLearningLogs(userId: Int): Flow<List<LearningLog>> = learningLogDao.getLearningLogsForUser(userId)

    suspend fun insertLearningLog(log: LearningLog) = learningLogDao.insertLearningLog(log)

    suspend fun deleteLearningLog(log: LearningLog) = learningLogDao.deleteLearningLog(log)

    // --- FoxAI Messages ---

    fun getFoxAiMessages(userId: Int): Flow<List<FoxAiMessage>> = foxAiMessageDao.getMessagesForUser(userId)

    suspend fun insertFoxAiMessage(message: FoxAiMessage) = foxAiMessageDao.insertMessage(message)

    suspend fun clearFoxAiMessages(userId: Int) = foxAiMessageDao.clearMessagesForUser(userId)

    // --- Three Wins ---

    fun getThreeWins(userId: Int, date: String): Flow<ThreeWins?> = threeWinsDao.getThreeWinsForUserAndDate(userId, date)

    suspend fun insertThreeWins(threeWins: ThreeWins) = threeWinsDao.insertThreeWins(threeWins)

    // --- Daily Reflections ---

    fun getDailyReflection(userId: Int, date: String): Flow<DailyReflection?> = dailyReflectionDao.getReflectionForUserAndDate(userId, date)

    suspend fun insertDailyReflection(reflection: DailyReflection) = dailyReflectionDao.insertReflection(reflection)
}
