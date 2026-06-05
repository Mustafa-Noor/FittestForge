package com.fitforge.app.ui.ai

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitforge.app.BuildConfig
import com.fitforge.app.data.models.User
import com.fitforge.app.data.models.Workout
import com.fitforge.app.data.repository.UserRepository
import com.fitforge.app.data.repository.WorkoutRepository
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

data class ChatMessage(val text: String, val isUser: Boolean, val timestamp: Long = System.currentTimeMillis())

class AICoachViewModel : ViewModel() {

    private val userRepository = UserRepository()
    private val workoutRepository = WorkoutRepository()

    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    private var chat: Chat? = null

    private suspend fun getOrBuildChat(): Chat {
        val existingChat = chat
        if (existingChat != null) return existingChat

        val user = userRepository.getUserProfile().getOrNull()
        val recentWorkouts = workoutRepository.getRecentWorkouts(5).getOrDefault(emptyList())
        
        val systemPrompt = buildSystemPrompt(user, recentWorkouts)

        val history = listOf(
            content(role = "user") { text(systemPrompt) },
            content(role = "model") { text("Understood. I'm ready to coach. How can I help you today?") }
        )

        val newChat = generativeModel.startChat(history = history)
        chat = newChat
        return newChat
    }

    private fun buildSystemPrompt(user: User?, recentWorkouts: List<Workout>): String {
        val workoutSummary = recentWorkouts
            .take(5)
            .joinToString("\n") { workout ->
                val names = workout.exercises
                    .take(4)
                    .joinToString { it.exerciseName.ifBlank { "Unnamed exercise" } }
                val muscleGroups = workout.exercises
                    .map { it.muscleGroup }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .take(3)
                    .joinToString()
                "- ${workout.dateString}: ${names.ifBlank { "Recovery day" }}; muscles: ${muscleGroups.ifBlank { "n/a" }}; ${workout.totalSets} sets; ${workout.durationMinutes} min"
            }
            .ifBlank { "No workouts logged yet." }

        return """
            You are the FitForge AI Coach. The user's personality mode is ${user?.personalityMode ?: "hype"}.
            Personality behavior:
            - "hype": enthusiastic, lots of energy, use motivational language, occasional caps for emphasis, emojis.
            - "drill": direct, no fluff, military-style brevity, no excuses accepted, very serious.
            - "chill": calm, supportive, no pressure, understanding tone, mindful.
            - "chaos": funny, meme-aware, Gen Z language, roast them lightly when appropriate, unpredictable but helpful.

            User context:
            - Name: ${user?.displayName ?: "Athlete"}
            - Current momentum: ${user?.momentum ?: 50}%
            - Total workouts: ${user?.totalWorkouts ?: 0}
            - Total minutes trained: ${user?.totalMinutes ?: 0}
            - Fitness goal: ${user?.fitnessGoal ?: "stay active"}
            - Current streak: ${user?.currentStreak ?: 0} days
            - Best streak: ${user?.bestStreak ?: 0} days
            - Last workout date: ${user?.lastWorkoutDate?.ifBlank { "none" } ?: "none"}
            - Last muscle group: ${user?.lastMuscleGroup?.ifBlank { "none" } ?: "none"}
            
            Recent Workouts:
            $workoutSummary

            Keep responses concise (under 120 words). Be practical. No medical advice.
            Use the user's actual data when it is relevant, but do not dump all stats unless asked.
            Always end with a small actionable suggestion.
        """.trimIndent()
    }

    fun sendMessage(userText: String) {
        val currentMessages = _messages.value.orEmpty().toMutableList()
        currentMessages.add(ChatMessage(userText, true))
        _messages.value = currentMessages

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val activeChat = getOrBuildChat()
                val response = activeChat.sendMessage(userText)
                
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

    override fun onCleared() {
        super.onCleared()
        saveSessionToFirestore()
    }

    private fun saveSessionToFirestore() {
        val msgs = _messages.value ?: return
        if (msgs.isEmpty()) return

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val sessionData = mapOf(
            "sessionDate" to com.google.firebase.Timestamp.now(),
            "messages" to msgs.takeLast(50).map {
                mapOf(
                    "role" to if (it.isUser) "user" else "model",
                    "text" to it.text,
                    "timestamp" to com.google.firebase.Timestamp(it.timestamp / 1000, ((it.timestamp % 1000) * 1000000).toInt())
                )
            }
        )

        db.collection("users").document(uid).collection("aiSessions")
            .add(sessionData)
    }
}
