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

    private fun getUserId() = auth.currentUser?.uid ?: throw Exception("User not authenticated")

    private fun getWorkoutCollection() =
        db.collection("users").document(getUserId()).collection("workouts")

    private fun getUserDocument() = db.collection("users").document(getUserId())

    suspend fun saveWorkoutAndUpdateStats(workout: Workout): Result<String> {
        return try {
            val workoutRef = getWorkoutCollection().document()
            val workoutId = workoutRef.id
            val finalWorkout = workout.copy(id = workoutId)

            val batch = db.batch()

            // 1. Write workout document
            batch.set(workoutRef, finalWorkout)

            // 2. Update user stats
            val userUpdates = mutableMapOf<String, Any>(
                "totalMinutes" to FieldValue.increment(workout.durationMinutes.toLong()),
                "lastActiveAt" to FieldValue.serverTimestamp()
            )

            if (!workout.isRecoveryDay) {
                userUpdates["totalWorkouts"] = FieldValue.increment(1)
                userUpdates["lastWorkoutDate"] = workout.dateString
                
                if (workout.exercises.isNotEmpty()) {
                    userUpdates["lastMuscleGroup"] = workout.exercises.last().muscleGroup
                }
            }

            batch.update(getUserDocument(), userUpdates)

            batch.commit().await()
            Result.success(workoutId)
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
                .get()
                .await()
            Result.success(snapshot.toObjects(Workout::class.java))
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
            val muscleGroupCounts = workouts
                .flatMap { it.exercises }
                .groupingBy { it.muscleGroup }
                .eachCount()
            
            Result.success(muscleGroupCounts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteWorkout(workoutId: String): Result<Unit> {
        return try {
            getWorkoutCollection().document(workoutId)
                .update("deleted", true)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWorkoutsForProgress(limitDays: Int): Result<List<Workout>> {
        return try {
            // Fetch recent workouts, excluding recovery and deleted
            val snapshot = getWorkoutCollection()
                .whereEqualTo("deleted", false)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(limitDays.toLong())
                .get()
                .await()
            val workouts = snapshot.toObjects(Workout::class.java).filter { !it.isRecoveryDay }
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
}
