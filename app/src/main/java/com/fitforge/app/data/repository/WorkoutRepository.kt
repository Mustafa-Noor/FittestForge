package com.fitforge.app.data.repository

import com.fitforge.app.data.models.Workout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class WorkoutRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private fun getWorkoutCollection() =
        db.collection("users").document(auth.currentUser?.uid ?: throw Exception("User not authenticated")).collection("workouts")

    suspend fun saveWorkout(workout: Workout): Result<Unit> {
        return try {
            val document = getWorkoutCollection().document()
            val workoutWithId = workout.copy(id = document.id)
            document.set(workoutWithId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecentWorkouts(limit: Long = 10): Result<List<Workout>> {
        return try {
            val snapshot = getWorkoutCollection()
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