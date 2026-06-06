package com.fitforge.app.ui.ai

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitforge.app.BuildConfig
import com.fitforge.app.data.models.User
import com.fitforge.app.data.repository.UserRepository
import com.fitforge.app.data.repository.WorkoutRepository
import com.fitforge.app.data.repository.FoodRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ChatMessage(val text: String, val isUser: Boolean)

class AICoachViewModel : ViewModel() {

    private val userRepository = UserRepository()
    private val workoutRepository = WorkoutRepository()
    private val foodRepository = FoodRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val generativeModel = GenerativeModel(
        modelName = "gemini-flash-latest",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    private var chat = generativeModel.startChat()
    private var isChatInitialized = false

    init {
        loadChatHistory()
    }

    /**
     * Call this when the user returns to the AI Coach screen
     * to ensure the context (weight, goal, workouts) is fully up-to-date.
     */
    fun refreshContext() {
        if (_messages.value.isNullOrEmpty()) {
            isChatInitialized = false
        } else {
            // We have existing messages, so we just set initialized to false
            // which will trigger initializeChat() on the next message sent.
            // But we can also proactively initialize it to avoid latency.
            viewModelScope.launch {
                try {
                    initializeChat()
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
    }

    /** Load previous session messages from Firestore so chat persists across app restarts */
    private fun loadChatHistory() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val sessions = db.collection("users").document(userId)
                    .collection("aiSessions")
                    .orderBy("sessionDate", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .await()

                val session = sessions.documents.firstOrNull()
                if (session != null) {
                    @Suppress("UNCHECKED_CAST")
                    val rawMessages = session.get("messages") as? List<Map<String, Any>> ?: emptyList()
                    val restored = rawMessages.map {
                        ChatMessage(
                            text = it["text"] as? String ?: "",
                            isUser = (it["role"] as? String) == "user"
                        )
                    }.filter { it.text.isNotEmpty() }
                    if (restored.isNotEmpty()) {
                        _messages.value = restored
                    }
                }
            } catch (e: Exception) {
                // Ignore — start fresh
            }
        }
    }

    fun sendMessage(userText: String) {
        val currentMessages = _messages.value.orEmpty().toMutableList()
        currentMessages.add(ChatMessage(userText, true))
        _messages.value = currentMessages

        _isLoading.value = true

        viewModelScope.launch {
            try {
                if (!isChatInitialized) {
                    initializeChat()
                }

                val response = chat.sendMessage(userText)
                val aiText = response.text ?: "I'm not sure how to respond to that."

                currentMessages.add(ChatMessage(aiText, false))
                _messages.value = currentMessages
            } catch (e: Exception) {
                currentMessages.add(ChatMessage("Sorry, I'm having trouble connecting. Error: ${e.message}", false))
                _messages.value = currentMessages
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun initializeChat() {
        val user = userRepository.getUserProfile().getOrNull()
        // Fetch last 30 workouts for full context
        val recentWorkouts = workoutRepository.getWorkoutsForProgress(30).getOrDefault(emptyList())
        val recentFoods = foodRepository.getFoodLogs().getOrDefault(emptyList()).take(7)

        val systemPromptText = buildSystemPrompt(user, recentWorkouts, recentFoods)

        // Restore prior messages as history so model has conversation continuity
        val historyMessages = _messages.value.orEmpty()
        val historyContent = mutableListOf(
            content(role = "user") { text(systemPromptText) },
            content(role = "model") { text("Understood. I have your complete fitness profile and I'm ready to coach you with full context.") }
        )

        for (msg in historyMessages) {
            historyContent.add(
                content(role = if (msg.isUser) "user" else "model") { text(msg.text) }
            )
        }

        chat = generativeModel.startChat(history = historyContent)
        isChatInitialized = true
    }

    private fun buildSystemPrompt(user: User?, recentWorkouts: List<com.fitforge.app.data.models.Workout>, recentFoods: List<com.fitforge.app.data.models.FoodLog>): String {
        val workoutSummary = if (recentWorkouts.isEmpty()) {
            "No workouts logged yet."
        } else {
            recentWorkouts.joinToString("\n") { w ->
                "- ${w.dateString}: ${w.exercises.joinToString { it.exerciseName }} (${w.durationMinutes} min, ${w.totalSets} sets)"
            }
        }

        val exerciseFrequency = recentWorkouts
            .flatMap { it.exercises }
            .groupingBy { it.exerciseName }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(5)
            .joinToString(", ") { "${it.key}(${it.value}x)" }

        val topMuscles = recentWorkouts
            .flatMap { it.exercises }
            .groupingBy { it.muscleGroup }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .joinToString(", ") { it.key }

        val weight = user?.weight ?: 0f
        val height = user?.height ?: 0f
        val age = user?.age ?: 0
        var bmiString = "Not provided"
        var maintenanceCalories = "Unknown"
        if (weight > 0 && height > 0 && age > 0) {
            val heightMeters = height / 100f
            val bmi = weight / (heightMeters * heightMeters)
            bmiString = String.format("%.1f", bmi)
            // Mifflin-St Jeor (sedentary baseline)
            val bmr = if (user?.gender == "Male") {
                10 * weight + 6.25 * height - 5 * age + 5
            } else {
                10 * weight + 6.25 * height - 5 * age - 161
            }
            maintenanceCalories = "${(bmr * 1.375).toInt()} kcal/day (lightly active)"
        }

        return """
You are the FitForge AI Coach — a deeply personalized fitness assistant. 
The user's coach personality mode is: ${user?.personalityMode ?: "hype"}.

Personality behavior:
- "hype": enthusiastic, motivational, occasional caps, emojis, high energy
- "drill": direct, military-style, no fluff, no excuses, very serious
- "chill": calm, supportive, mindful, no pressure
- "chaos": funny, meme-aware, Gen Z language, light roasting, unpredictable

CRITICAL FORMATTING RULE: Never use markdown formatting. No asterisks (**bold**), no headers (#), no bullet dashes (-). 
Write in plain conversational text only. Use emojis sparingly instead of markdown emphasis.

User Profile:
- Name: ${user?.displayName ?: "Athlete"}
- Age: $age years
- Weight: $weight kg | Height: $height cm
- BMI: $bmiString
- Gender: ${user?.gender ?: "Not specified"}
- Fitness Goal: ${user?.fitnessGoal ?: "stay active"}
- Maintenance Calories: $maintenanceCalories

Activity Stats:
- Total workouts logged: ${user?.totalWorkouts ?: 0}
- Current streak: ${user?.currentStreak ?: 0} days
- Best streak ever: ${user?.bestStreak ?: 0} days
- Total training time: ${user?.totalMinutes ?: 0} minutes
- Current momentum score: ${user?.momentum ?: 50}%
- Last muscle group trained: ${user?.lastMuscleGroup ?: "Unknown"}
- Top exercises (frequency): $exerciseFrequency
- Most trained muscles: $topMuscles

Last 30 Workouts:
$workoutSummary

Recent Calorie Intake (Last 7 days):
${if (recentFoods.isEmpty()) "No food logged recently." else recentFoods.joinToString("\n") { f -> "- ${f.dateString}: ${f.totalCalories} kcal" }}

Instructions:
- Use this data to give highly personalized advice. Reference their specific exercises, streak, and goal.
- If goal is to gain weight: recommend being in a caloric surplus and progressive overload.
- If goal is to lose weight: recommend deficit eating, cardio, and compound movements.
- Keep responses under 150 words. Always end with one concrete actionable tip.
- Never give medical advice. Only fitness and nutrition guidance.
        """.trimIndent()
    }

    override fun onCleared() {
        super.onCleared()
        saveSessionToFirestore()
    }

    private fun saveSessionToFirestore() {
        val userId = auth.currentUser?.uid ?: return
        val messagesList = _messages.value ?: return
        if (messagesList.isEmpty()) return

        val sessionData = mapOf(
            "sessionDate" to com.google.firebase.Timestamp.now(),
            "messages" to messagesList.map {
                mapOf(
                    "role" to if (it.isUser) "user" else "model",
                    "text" to it.text,
                    "timestamp" to com.google.firebase.Timestamp.now()
                )
            }
        )

        viewModelScope.launch {
            try {
                val userId2 = auth.currentUser?.uid ?: return@launch
                // Overwrite the single persistent session doc instead of appending
                db.collection("users").document(userId2)
                    .collection("aiSessions")
                    .document("current_session")
                    .set(sessionData)
                    .await()
            } catch (e: Exception) {
                // Fire and forget
            }
        }
    }
}
