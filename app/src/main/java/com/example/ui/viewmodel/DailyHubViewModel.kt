package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.*
import com.example.data.network.GeminiApiClient
import com.example.data.repository.CuratedFacts
import com.example.data.repository.DailyHubRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class Screen {
    Login, Signup, Dashboard, Study, Work, Lifestyle, Facts, FoxAi, Settings
}

enum class TimerMode {
    Focus, ShortBreak, LongBreak
}

class DailyHubViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DailyHubRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = DailyHubRepository(database)
    }

    // --- Screen Navigation ---
    private val _currentScreen = MutableStateFlow(Screen.Login)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    // --- Authentication State ---
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _authSuccess = MutableStateFlow<Boolean>(false)
    val authSuccess: StateFlow<Boolean> = _authSuccess.asStateFlow()

    // --- User Data States ---
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    private val _moodLogs = MutableStateFlow<List<MoodLog>>(emptyList())
    val moodLogs: StateFlow<List<MoodLog>> = _moodLogs.asStateFlow()

    private val _decks = MutableStateFlow<List<FlashcardDeck>>(emptyList())
    val decks: StateFlow<List<FlashcardDeck>> = _decks.asStateFlow()

    private val _learningLogs = MutableStateFlow<List<LearningLog>>(emptyList())
    val learningLogs: StateFlow<List<LearningLog>> = _learningLogs.asStateFlow()

    private val _foxAiMessages = MutableStateFlow<List<FoxAiMessage>>(emptyList())
    val foxAiMessages: StateFlow<List<FoxAiMessage>> = _foxAiMessages.asStateFlow()

    private val _threeWins = MutableStateFlow<ThreeWins?>(null)
    val threeWins: StateFlow<ThreeWins?> = _threeWins.asStateFlow()

    private val _dailyReflection = MutableStateFlow<DailyReflection?>(null)
    val dailyReflection: StateFlow<DailyReflection?> = _dailyReflection.asStateFlow()

    private var userDataJob: Job? = null

    // --- Active Flashcard Study state ---
    private val _currentDeck = MutableStateFlow<FlashcardDeck?>(null)
    val currentDeck: StateFlow<FlashcardDeck?> = _currentDeck.asStateFlow()

    private val _deckCards = MutableStateFlow<List<Flashcard>>(emptyList())
    val deckCards: StateFlow<List<Flashcard>> = _deckCards.asStateFlow()

    private val _currentCardIndex = MutableStateFlow(0)
    val currentCardIndex: StateFlow<Int> = _currentCardIndex.asStateFlow()

    private val _isCardFlipped = MutableStateFlow(false)
    val isCardFlipped: StateFlow<Boolean> = _isCardFlipped.asStateFlow()

    // --- Pomodoro Timer State ---
    private val _durationSecondsRemaining = MutableStateFlow(25 * 60)
    val durationSecondsRemaining: StateFlow<Int> = _durationSecondsRemaining.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _timerMode = MutableStateFlow(TimerMode.Focus)
    val timerMode: StateFlow<TimerMode> = _timerMode.asStateFlow()

    private var timerJob: Job? = null

    // --- Facts State ---
    private val _currentFact = MutableStateFlow(CuratedFacts.facts.first())
    val currentFact: StateFlow<CuratedFacts.Fact> = _currentFact.asStateFlow()

    // --- FoxAI Chat State ---
    private val _isFoxAiThinking = MutableStateFlow(false)
    val isFoxAiThinking: StateFlow<Boolean> = _isFoxAiThinking.asStateFlow()

    private val _foxAiLimit = MutableStateFlow(20) // Local limit per session/day
    private val _foxAiCount = MutableStateFlow(0)
    val foxAiCount: StateFlow<Int> = _foxAiCount.asStateFlow()

    init {
        rotateFact()
    }

    // --- Get Current Date representation "YYYY-MM-DD" ---
    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    // --- Rotate Facts ---
    fun rotateFact() {
        _currentFact.value = CuratedFacts.facts.random()
    }

    // --- Auth Logic ---
    fun clearAuthStates() {
        _authError.value = null
        _authSuccess.value = false
    }

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _authError.value = "Username and password cannot be empty."
            return
        }
        viewModelScope.launch {
            val user = repository.login(username, password)
            if (user != null) {
                _currentUser.value = user
                _authSuccess.value = true
                clearAuthStates()
                startCollectingUserData(user.id)
                // Configure Pomodoro durations
                resetTimerDurations(user)
                navigateTo(Screen.Dashboard)
            } else {
                _authError.value = "Invalid username or password."
            }
        }
    }

    fun signup(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _authError.value = "Username and password cannot be empty."
            return
        }
        if (username.length < 3 || password.length < 4) {
            _authError.value = "Username must be >= 3 characters and password >= 4 characters."
            return
        }
        viewModelScope.launch {
            val success = repository.signup(username, password)
            if (success) {
                _authError.value = null
                // Automatically log in
                login(username, password)
            } else {
                _authError.value = "Username '$username' is already taken."
            }
        }
    }

    fun logout() {
        // Cancel timer
        pauseTimer()
        // Cancel collections
        userDataJob?.cancel()
        userDataJob = null
        // Clear variables
        _currentUser.value = null
        _tasks.value = emptyList()
        _habits.value = emptyList()
        _moodLogs.value = emptyList()
        _decks.value = emptyList()
        _learningLogs.value = emptyList()
        _foxAiMessages.value = emptyList()
        _threeWins.value = null
        _dailyReflection.value = null
        _currentDeck.value = null
        _deckCards.value = emptyList()
        _currentCardIndex.value = 0
        _isCardFlipped.value = false
        // Navigate
        navigateTo(Screen.Login)
    }

    fun deleteAccount() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteUser(user)
            logout()
        }
    }

    fun updatePassword(newPassword: String) {
        val user = _currentUser.value ?: return
        if (newPassword.isBlank() || newPassword.length < 4) {
            _authError.value = "Password must be at least 4 characters."
            return
        }
        viewModelScope.launch {
            // Need to re-salt and re-hash the password. Just create a new user profile with updated values
            // To make it easy, we delete and signup or we can do it via a custom updateUserPassword in repo.
            // Let's implement changing the settings or password. To be perfectly accurate we can just update the salt and passwordHash!
            val salt = java.util.UUID.randomUUID().toString()
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val saltedInput = newPassword + salt
            val hashBytes = digest.digest(saltedInput.toByteArray(Charsets.UTF_8))
            val hashString = hashBytes.joinToString("") { "%02x".format(it) }

            val updatedUser = user.copy(
                passwordHash = hashString,
                salt = salt
            )
            repository.updateUser(updatedUser)
            _currentUser.value = updatedUser
            _authError.value = "Password updated successfully!"
        }
    }

    // --- User Data Collection Sync ---
    private fun startCollectingUserData(userId: Int) {
        userDataJob?.cancel()
        userDataJob = viewModelScope.launch {
            // Collect Tasks
            launch {
                repository.getTasks(userId).collectLatest { _tasks.value = it }
            }
            // Collect Habits
            launch {
                repository.getHabits(userId).collectLatest { _habits.value = it }
            }
            // Collect Mood Logs
            launch {
                repository.getMoodLogs(userId).collectLatest { _moodLogs.value = it }
            }
            // Collect Flashcard Decks
            launch {
                repository.getDecks(userId).collectLatest { _decks.value = it }
            }
            // Collect Learning Logs
            launch {
                repository.getLearningLogs(userId).collectLatest { _learningLogs.value = it }
            }
            // Collect FoxAI messages
            launch {
                repository.getFoxAiMessages(userId).collectLatest { _foxAiMessages.value = it }
            }
            // Collect Three Wins
            launch {
                repository.getThreeWins(userId, getTodayDateString()).collectLatest { _threeWins.value = it }
            }
            // Collect Reflections
            launch {
                repository.getDailyReflection(userId, getTodayDateString()).collectLatest { _dailyReflection.value = it }
            }
        }
    }

    // --- Tasks Operations ---
    fun addTask(title: String, priority: String, dueDate: String) {
        val userId = _currentUser.value?.id ?: return
        if (title.isBlank()) return
        viewModelScope.launch {
            val task = Task(
                userId = userId,
                title = title.trim(),
                priority = priority,
                dueDate = dueDate.trim(),
                isCompleted = false
            )
            repository.insertTask(task)
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val updated = task.copy(
                isCompleted = !task.isCompleted,
                completedAt = if (!task.isCompleted) System.currentTimeMillis() else null
            )
            repository.updateTask(updated)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // --- Three Wins Operations ---
    fun saveThreeWins(win1: String, win2: String, win3: String) {
        val userId = _currentUser.value?.id ?: return
        viewModelScope.launch {
            val wins = ThreeWins(
                userId = userId,
                date = getTodayDateString(),
                win1 = win1.trim(),
                win2 = win2.trim(),
                win3 = win3.trim()
            )
            repository.insertThreeWins(wins)
        }
    }

    // --- Habits Operations ---
    fun addHabit(name: String) {
        val userId = _currentUser.value?.id ?: return
        if (name.isBlank()) return
        viewModelScope.launch {
            val habit = Habit(
                userId = userId,
                name = name.trim(),
                streak = 0,
                lastLoggedDate = null
            )
            repository.insertHabit(habit)
        }
    }

    fun logHabitToday(habit: Habit) {
        val today = getTodayDateString()
        if (habit.lastLoggedDate == today) return // Already logged today

        viewModelScope.launch {
            val currentStreak = habit.streak
            val lastDate = habit.lastLoggedDate

            val newStreak = if (lastDate == null) {
                1
            } else {
                // Calculate if last logged date was yesterday
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val lastDateObj = sdf.parse(lastDate)
                val todayObj = sdf.parse(today)
                if (lastDateObj != null && todayObj != null) {
                    val diff = todayObj.time - lastDateObj.time
                    val diffDays = diff / (24 * 60 * 60 * 1000)
                    if (diffDays <= 1) {
                        currentStreak + 1
                    } else {
                        1 // Streak broken, start fresh
                    }
                } else {
                    1
                }
            }

            val updated = habit.copy(
                streak = newStreak,
                lastLoggedDate = today
            )
            repository.updateHabit(updated)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    // --- Mood Logs Operations ---
    fun logMood(emoji: String, note: String) {
        val userId = _currentUser.value?.id ?: return
        viewModelScope.launch {
            val log = MoodLog(
                userId = userId,
                emoji = emoji,
                note = note.trim()
            )
            repository.insertMoodLog(log)
        }
    }

    fun deleteMoodLog(log: MoodLog) {
        viewModelScope.launch {
            repository.deleteMoodLog(log)
        }
    }

    // --- Daily Reflections Operations ---
    fun saveReflection(prompt: String, answer: String) {
        val userId = _currentUser.value?.id ?: return
        viewModelScope.launch {
            val reflection = DailyReflection(
                userId = userId,
                date = getTodayDateString(),
                prompt = prompt,
                answer = answer.trim()
            )
            repository.insertDailyReflection(reflection)
        }
    }

    // --- Learning Logs Operations ---
    fun addLearningLog(content: String) {
        val userId = _currentUser.value?.id ?: return
        if (content.isBlank()) return
        viewModelScope.launch {
            val log = LearningLog(
                userId = userId,
                content = content.trim()
            )
            repository.insertLearningLog(log)
        }
    }

    fun deleteLearningLog(log: LearningLog) {
        viewModelScope.launch {
            repository.deleteLearningLog(log)
        }
    }

    // --- Flashcards Decks Operations ---
    fun createDeck(name: String) {
        val userId = _currentUser.value?.id ?: return
        if (name.isBlank()) return
        viewModelScope.launch {
            val deck = FlashcardDeck(userId = userId, name = name.trim())
            repository.insertDeck(deck)
        }
    }

    fun deleteDeck(deck: FlashcardDeck) {
        viewModelScope.launch {
            repository.deleteDeck(deck)
            if (_currentDeck.value?.id == deck.id) {
                _currentDeck.value = null
                _deckCards.value = emptyList()
            }
        }
    }

    fun selectDeckForStudy(deck: FlashcardDeck) {
        _currentDeck.value = deck
        _currentCardIndex.value = 0
        _isCardFlipped.value = false
        viewModelScope.launch {
            repository.getFlashcards(deck.id).collectLatest { cards ->
                _deckCards.value = cards
            }
        }
    }

    fun addFlashcard(front: String, back: String) {
        val deckId = _currentDeck.value?.id ?: return
        if (front.isBlank() || back.isBlank()) return
        viewModelScope.launch {
            val card = Flashcard(
                deckId = deckId,
                front = front.trim(),
                back = back.trim()
            )
            repository.insertFlashcard(card)
        }
    }

    fun markCardKnown(card: Flashcard, isKnown: Boolean) {
        viewModelScope.launch {
            val updated = card.copy(isKnown = isKnown)
            repository.updateFlashcard(updated)
            nextCard()
        }
    }

    fun deleteFlashcard(card: Flashcard) {
        viewModelScope.launch {
            repository.deleteFlashcard(card)
        }
    }

    fun flipCard() {
        _isCardFlipped.value = !_isCardFlipped.value
    }

    fun nextCard() {
        if (_deckCards.value.isEmpty()) return
        _isCardFlipped.value = false
        val nextIndex = (_currentCardIndex.value + 1) % _deckCards.value.size
        _currentCardIndex.value = nextIndex
    }

    fun prevCard() {
        if (_deckCards.value.isEmpty()) return
        _isCardFlipped.value = false
        var prevIndex = _currentCardIndex.value - 1
        if (prevIndex < 0) {
            prevIndex = _deckCards.value.size - 1
        }
        _currentCardIndex.value = prevIndex
    }

    // --- Pomodoro Timer Operations ---
    private fun resetTimerDurations(user: User) {
        val durationMinutes = when (_timerMode.value) {
            TimerMode.Focus -> user.focusDurationMinutes
            TimerMode.ShortBreak -> user.shortBreakMinutes
            TimerMode.LongBreak -> user.longBreakMinutes
        }
        _durationSecondsRemaining.value = durationMinutes * 60
    }

    fun changeTimerMode(mode: TimerMode) {
        pauseTimer()
        _timerMode.value = mode
        val user = _currentUser.value ?: return
        resetTimerDurations(user)
    }

    fun startTimer() {
        if (_isTimerRunning.value) return
        _isTimerRunning.value = true
        timerJob = viewModelScope.launch {
            while (_durationSecondsRemaining.value > 0) {
                delay(1000)
                _durationSecondsRemaining.value -= 1
            }
            // Timer finished
            _isTimerRunning.value = false
            // Switch modes automatically
            val nextMode = when (_timerMode.value) {
                TimerMode.Focus -> TimerMode.ShortBreak
                TimerMode.ShortBreak -> TimerMode.Focus
                TimerMode.LongBreak -> TimerMode.Focus
            }
            _timerMode.value = nextMode
            val user = _currentUser.value
            if (user != null) {
                resetTimerDurations(user)
            }
        }
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        timerJob = null
    }

    fun resetTimer() {
        pauseTimer()
        val user = _currentUser.value ?: return
        resetTimerDurations(user)
    }

    fun saveTimerSettings(focusMin: Int, shortMin: Int, longMin: Int) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updated = user.copy(
                focusDurationMinutes = focusMin,
                shortBreakMinutes = shortMin,
                longBreakMinutes = longMin
            )
            repository.updateUser(updated)
            _currentUser.value = updated
            resetTimerDurations(updated)
        }
    }

    // --- FoxAI Helper: personalized prompt creation ---
    private fun createPersonalizedSystemPrompt(): String {
        val activeTasks = _tasks.value.filter { !it.isCompleted }
        val completedTasksToday = _tasks.value.filter { it.isCompleted } // In actual app, filter by date if needed.
        val habitsList = _habits.value
        val latestMood = _moodLogs.value.firstOrNull()
        val wins = _threeWins.value

        val statsBuilder = StringBuilder()
        statsBuilder.append("Active tasks list:\n")
        if (activeTasks.isEmpty()) {
            statsBuilder.append("- None! Wow, clean slate.\n")
        } else {
            activeTasks.take(5).forEach {
                statsBuilder.append("- ${it.title} (${it.priority} Priority, Due: ${it.dueDate})\n")
            }
        }

        statsBuilder.append("\nHabits status & streaks:\n")
        if (habitsList.isEmpty()) {
            statsBuilder.append("- No habits set up yet.\n")
        } else {
            habitsList.forEach {
                val loggedToday = it.lastLoggedDate == getTodayDateString()
                statsBuilder.append("- ${it.name}: Streak ${it.streak} days (Logged today: ${if (loggedToday) "Yes" else "No"})\n")
            }
        }

        if (latestMood != null) {
            statsBuilder.append("\nUser's latest mood: ${latestMood.emoji} with note: \"${latestMood.note}\"\n")
        }

        if (wins != null) {
            statsBuilder.append("\nUser's 3 Wins for today:\n")
            if (wins.win1.isNotBlank()) statsBuilder.append("1. ${wins.win1}\n")
            if (wins.win2.isNotBlank()) statsBuilder.append("2. ${wins.win2}\n")
            if (wins.win3.isNotBlank()) statsBuilder.append("3. ${wins.win3}\n")
        }

        val systemPrompt = """
            You are FoxAI, a warm, encouraging, witty orange fox mascot who lives inside DailyHub. 
            DailyHub blends study, work, habits, reflection, and interesting facts.
            
            You have access to the user's daily data:
            ${statsBuilder.toString()}
            
            Guidelines:
            1. Use this data natural and contextually in your answers! For example, congratulate them on streaks, gently encourage them to complete tasks, or comment on their mood.
            2. Speak like a friendly, caring fox mascot: warm, encouraging, a bit playful, witty, never nagging.
            3. Address the user directly and keep your responses concise, highly engaging, and helpful.
        """.trimIndent()

        return systemPrompt
    }

    fun sendFoxAiMessage(messageText: String) {
        val user = _currentUser.value ?: return
        if (messageText.isBlank()) return

        // Check local limit rate-limiting
        if (_foxAiCount.value >= _foxAiLimit.value) {
            viewModelScope.launch {
                val botMsg = FoxAiMessage(
                    userId = user.id,
                    text = "Aww, FoxAI has run out of juice for this session! Daily free-tier API quotas are shared. Let's rest a bit and try again later!",
                    isUser = false
                )
                repository.insertFoxAiMessage(botMsg)
            }
            return
        }

        viewModelScope.launch {
            // Save user message
            val userMsg = FoxAiMessage(
                userId = user.id,
                text = messageText.trim(),
                isUser = true
            )
            repository.insertFoxAiMessage(userMsg)
            _foxAiCount.value += 1

            // Call Gemini
            _isFoxAiThinking.value = true
            val systemPrompt = createPersonalizedSystemPrompt()
            
            val aiResponse = GeminiApiClient.generateContent(
                prompt = messageText,
                systemInstruction = systemPrompt
            )

            val botMsg = FoxAiMessage(
                userId = user.id,
                text = aiResponse,
                isUser = false
            )
            repository.insertFoxAiMessage(botMsg)
            _isFoxAiThinking.value = false
        }
    }

    fun clearFoxAiHistory() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.clearFoxAiMessages(user.id)
        }
    }
}
