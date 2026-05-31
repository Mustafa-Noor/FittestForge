package com.fitforge.app.data.repository

import com.fitforge.app.data.models.Exercise
import com.fitforge.app.data.remote.RetrofitClient

class ExerciseRepository {
    private val api = RetrofitClient.apiService

    suspend fun getExercises(limit: Int = 20, offset: Int = 0): Result<List<Exercise>> {
        return try {
            val response = api.getExercises(limit, offset)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getExercisesByBodyPart(bodyPart: String): Result<List<Exercise>> {
        return try {
            val response = api.getExercisesByBodyPart(bodyPart)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getExerciseById(id: String): Result<Exercise> {
        return try {
            val response = api.getExerciseById(id)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getBodyPartList(): Result<List<String>> {
         return try {
             val response = api.getBodyPartList()
             Result.success(response)
         } catch (e: Exception) {
             Result.failure(e)
         }
    }
}
