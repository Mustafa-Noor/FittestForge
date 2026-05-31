package com.fitforge.app.data.remote

import com.fitforge.app.data.models.Exercise
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ExerciseApiService {
    @GET("exercises")
    suspend fun getExercises(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): List<Exercise>

    @GET("exercises/bodyPart/{bodyPart}")
    suspend fun getExercisesByBodyPart(
        @Path("bodyPart") bodyPart: String
    ): List<Exercise>

    @GET("exercises/equipment/{equipment}")
    suspend fun getExercisesByEquipment(
        @Path("equipment") equipment: String
    ): List<Exercise>

    @GET("exercises/{id}")
    suspend fun getExerciseById(
        @Path("id") id: String
    ): Exercise

    @GET("exercises/bodyPartList")
    suspend fun getBodyPartList(): List<String>
}
