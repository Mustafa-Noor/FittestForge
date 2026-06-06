package com.fitforge.app.ui.ai

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitforge.app.BuildConfig
import com.fitforge.app.data.models.User
import com.fitforge.app.data.repository.UserRepository
import com.fitforge.app.data.repository.WorkoutRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

data class ChatMessage(val text: String, val isUser: Boolean)

class AICoachViewModel : ViewModel() {

    private val userRepository = UserRepository()
    private val workoutRepository = WorkoutRepository()
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
    private var systemPromptText: String = ""

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
        val recentWorkouts = workoutRepository.getRecentWorkouts(5).getOrDefault(emptyList())
        
        systemPromptText = buildSystemPrompt(user, recentWorkouts)

        chat = generativeModel.startChat(
            history = listOf(
                content(role = "user") { text(systemPromptText) },
                content(role = "model") { text("Understood. I'm ready to coach.") }
            )
        )
        isChatInitialized = true
    }

    private fun buildSystemPrompt(user: User?, recentWorkouts: List<com.fitforge.app.data.models.Workout>): String {
        val workoutSummary = recentWorkouts.joinToString("\n") { w ->
            "- ${w.dateString}: ${w.exercises.joinToString { it.exerciseName }} (${w.durationMinutes} min)"
        }

        val weight = user?.weight ?: 0f
        val height = user?.height ?: 0f
        val age = user?.age ?: 0
        var bmiString = "Not provided"
        if (weight > 0 && height > 0) {
            // Assuming height is in cm. BMI = kg / (m * m)
            val heightMeters = height / 100f
            val bmi = weight / (heightMeters * heightMeters)
            bmiString = String.format("%.1f", bmi)
        }

        return """
            You are the FitForge AI Coach. The user's personality mode is ${user?.personalityMode ?: "hype"}.
            Personality behavior:
            - "hype": enthusiastic, lots of energy, use motivational language, occasional caps for emphasis, emojis.
            - "drill": direct, no fluff, military-style brevity, no excuses accepted, very serious.
            - "chill": calm, supportive, no pressure, understanding tone, mindful.
            - "chaos": funny, meme-aware, Gen Z language, roast them lightly when appropriate, unpredictable but helpful.

            User context:
            - Name: ${user?.displayName ?: "Athlete"}
            - Age: $age
            - Weight: $weight kg
            - Height: $height cm
            - BMI: $bmiString
            - Current momentum: ${user?.momentum ?: 50}%
            - Total workouts: ${user?.totalWorkouts ?: 0}
            - Fitness goal: ${user?.fitnessGoal ?: "stay active"}
            - Current streak: ${user?.currentStreak ?: 0} days
            
            Recent Workouts:
            $workoutSummary

            Provide specialized workout plans or recommendations based on their BMI and body data if they ask.
            Keep responses concise (under 120 words). Be practical. No medical advice.
            Always end with a small actionable suggestion.
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
                db.collection("users").document(userId)
                    .collection("aiSessions").add(sessionData)
            } catch (e: Exception) {
                // Fire and forget, but log error if needed
            }
        }
    }
}
