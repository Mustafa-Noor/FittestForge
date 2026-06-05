package com.fitforge.app.data.repository

import com.fitforge.app.data.models.Workout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class WorkoutRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private fun getWorkoutCollection() =
        db.collection("users").document(auth.currentUser?.uid ?: throw Exception("User not authenticated")).collection("workouts")

    private fun getUserDocument() =
        db.collection("users").document(auth.currentUser?.uid ?: throw Exception("User not authenticated"))

    suspend fun saveWorkoutAndUpdateStats(workout: Workout): Result<String> {
        return try {
            val workoutRef = getWorkoutCollection().document()
            val workoutWithId = workout.copy(id = workoutRef.id)
            
            val batch = db.batch()
            batch.set(workoutRef, workoutWithId)

            if (!workout.isRecoveryDay) {
                val userUpdates = mutableMapOf<String, Any>(
                    "totalWorkouts" to FieldValue.increment(1),
                    "totalMinutes" to FieldValue.increment(workout.durationMinutes.toLong()),
                    "lastWorkoutDate" to workout.dateString,
                    "lastActiveAt" to FieldValue.serverTimestamp()
                )
                if (workout.exercises.isNotEmpty()) {
                    userUpdates["lastMuscleGroup"] = workout.exercises.last().muscleGroup
                }
                batch.update(getUserDocument(), userUpdates)
            } else {
                batch.update(getUserDocument(), mapOf(
                    "totalMinutes" to FieldValue.increment(workout.durationMinutes.toLong()),
                    "lastActiveAt" to FieldValue.serverTimestamp()
                ))
            }

            batch.commit().await()
            Result.success(workoutRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWorkouts(): Result<List<Workout>> {
        return try {
            val snapshot = getWorkoutCollection()
                .whereEqualTo("deleted", false)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()
            val workouts = snapshot.toObjects(Workout::class.java)
            Result.success(workouts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWorkoutsByDateRange(startDate: String, endDate: String): Result<List<Workout>> {
        return try {
            val snapshot = getWorkoutCollection()
                .whereEqualTo("deleted", false)
                .whereGreaterThanOrEqualTo("dateString", startDate)
                .whereLessThanOrEqualTo("dateString", endDate)
                .orderBy("dateString", Query.Direction.DESCENDING)
                .get()
                .await()
            val workouts = snapshot.toObjects(Workout::class.java)
            Result.success(workouts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWorkoutsByMuscleGroup(): Result<Map<String, Int>> {
        return try {
            val snapshot = getWorkoutCollection()
                .whereEqualTo("deleted", false)
                .get()
                .await()
            val workouts = snapshot.toObjects(Workout::class.java)
            val counts = workouts.flatMap { it.exercises }
                .groupBy { it.muscleGroup }
                .mapValues { it.value.size }
            Result.success(counts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteWorkout(workoutId: String): Result<Unit> {
        return try {
            getWorkoutCollection().document(workoutId).update("deleted", true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWorkoutsForProgress(limitDays: Int): Result<List<Workout>> {
        return try {
            val snapshot = getWorkoutCollection()
                .whereEqualTo("deleted", false)
                .whereEqualTo("isRecoveryDay", false)
                .orderBy("dateString", Query.Direction.DESCENDING)
                .limit(limitDays.toLong())
                .get()
                .await()
            val workouts = snapshot.toObjects(Workout::class.java)
            Result.success(workouts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecentWorkouts(limit: Long = 10): Result<List<Workout>> {
        return try {
            val snapshot = getWorkoutCollection()
                .whereEqualTo("deleted", false)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            val workouts = snapshot.toObjects(Workout::class.java)
            Result.success(workouts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
