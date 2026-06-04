package com.fitforge.app.ui.ai

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitforge.app.BuildConfig
import com.fitforge.app.data.models.PersonalityMode
import com.fitforge.app.data.repository.UserRepository
import com.fitforge.app.data.repository.WorkoutRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch

data class ChatMessage(val text: String, val isUser: Boolean)

class AICoachViewModel : ViewModel() {

    private val userRepository = UserRepository()
    private val workoutRepository = WorkoutRepository()

    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    private val chat = generativeModel.startChat()

    fun sendMessage(userText: String) {
        val currentMessages = _messages.value.orEmpty().toMutableList()
        currentMessages.add(ChatMessage(userText, true))
        _messages.value = currentMessages

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val userResult = userRepository.getUserProfile()
                val user = userResult.getOrNull()
                
                val recentWorkoutsResult = workoutRepository.getRecentWorkouts(5)
                val recentWorkouts = recentWorkoutsResult.getOrDefault(emptyList())
                val workoutSummary = recentWorkouts.joinToString("\n") { w ->
                    "- ${w.dateString}: ${w.exercises.joinToString { it.exerciseName }} (${w.durationMinutes} min)"
                }

                val systemPrompt = """
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
                    - Fitness goal: ${user?.fitnessGoal ?: "stay active"}
                    - Current streak: ${user?.currentStreak ?: 0} days
                    
                    Recent Workouts:
                    $workoutSummary

                    Keep responses concise (under 120 words). Be practical. No medical advice.
                    Always end with a small actionable suggestion.
                """.trimIndent()

                val response = chat.sendMessage(content {
                    text(systemPrompt)
                    text(userText)
                })
                
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
}