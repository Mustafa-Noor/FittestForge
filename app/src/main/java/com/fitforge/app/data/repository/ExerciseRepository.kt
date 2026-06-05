package com.fitforge.app.data.repository

import com.fitforge.app.data.models.Exercise
import com.fitforge.app.data.remote.ExerciseResponse
import com.fitforge.app.data.remote.RetrofitClient
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken

class ExerciseRepository {
    private val api = RetrofitClient.apiService
    private val gson = Gson()

    suspend fun getExercises(limit: Int = 20, offset: Int = 0): Result<List<Exercise>> {
        return try {
            val response = api.getExercises(limit, offset)
            Result.success(parseExerciseList(response))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getExercisesByBodyPart(bodyPart: String): Result<List<Exercise>> {
        return try {
            val response = api.getExercisesByBodyPart(bodyPart)
            Result.success(parseExerciseList(response))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getExerciseById(id: String): Result<Exercise> {
        return try {
            val response = api.getExerciseById(id)
            Result.success(parseExercise(response))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getBodyPartList(): Result<List<String>> {
         return try {
             val response = api.getBodyPartList()
             Result.success(parseStringList(response))
         } catch (e: Exception) {
             Result.failure(e)
         }
    }

    private fun parseExerciseList(json: JsonElement): List<Exercise> {
        val listJson = when {
            json.isJsonArray -> json
            json.isJsonObject -> {
                val obj = json.asJsonObject
                listOf("data", "exercises", "results")
                    .firstNotNullOfOrNull { key -> obj.get(key)?.takeIf { it.isJsonArray } }
                    ?: json
            }
            else -> json
        }

        val type = object : TypeToken<List<ExerciseResponse>>() {}.type
        return gson.fromJson<List<ExerciseResponse>>(listJson, type).map { it.toExercise() }
    }

    private fun parseExercise(json: JsonElement): Exercise {
        val exerciseJson = when {
            json.isJsonObject && json.asJsonObject.get("data")?.isJsonObject == true ->
                json.asJsonObject.get("data")
            else -> json
        }

        return gson.fromJson(exerciseJson, ExerciseResponse::class.java).toExercise()
    }

    private fun parseStringList(json: JsonElement): List<String> {
        val listJson = when {
            json.isJsonArray -> json
            json.isJsonObject -> {
                val obj = json.asJsonObject
                listOf("data", "bodyParts", "bodyparts", "results")
                    .firstNotNullOfOrNull { key -> obj.get(key)?.takeIf { it.isJsonArray } }
                    ?: json
            }
            else -> json
        }

        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(listJson, type)
    }
}
