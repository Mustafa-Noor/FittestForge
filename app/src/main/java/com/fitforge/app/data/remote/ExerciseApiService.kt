package com.fitforge.app.data.remote

import com.fitforge.app.data.models.Exercise
import com.google.gson.JsonElement
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ExerciseApiService {
    @GET("api/v1/exercises")
    suspend fun getExercises(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): JsonElement

    @GET("api/v1/exercises")
    suspend fun getExercisesByBodyPart(
        @Query("bodyParts") bodyPart: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): JsonElement

    @GET("api/v1/exercises")
    suspend fun getExercisesByEquipment(
        @Query("equipments") equipment: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): JsonElement

    @GET("api/v1/exercises/{id}")
    suspend fun getExerciseById(
        @Path("id") id: String
    ): JsonElement

    @GET("api/v1/exercises/bodyparts")
    suspend fun getBodyPartList(): JsonElement
}

data class ExerciseResponse(
    val exerciseId: String = "",
    val name: String = "",
    val bodyParts: List<String> = emptyList(),
    val targetMuscles: List<String> = emptyList(),
    val equipments: List<String> = emptyList(),
    val gifUrl: String = "",
    val instructions: List<String> = emptyList(),
    val secondaryMuscles: List<String> = emptyList()
) {
    fun toExercise(): Exercise {
        return Exercise(
            id = exerciseId,
            name = name,
            bodyPart = bodyParts.joinToString(", "),
            target = targetMuscles.joinToString(", "),
            equipment = equipments.joinToString(", "),
            gifUrl = gifUrl,
            instructions = instructions,
            secondaryMuscles = secondaryMuscles
        )
    }
}
