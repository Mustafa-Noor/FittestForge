package com.fitforge.app.data.repository

import com.fitforge.app.data.models.FoodLog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FoodRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private fun getUserId() = auth.currentUser?.uid ?: throw Exception("User not authenticated")

    private fun getFoodCollection() =
        db.collection("users").document(getUserId()).collection("foodLogs")

    suspend fun saveFoodLog(log: FoodLog): Result<Unit> {
        return try {
            val docId = log.dateString  // Use date as document ID so each day has one entry
            val finalLog = log.copy(
                id = docId,
                totalCalories = log.breakfast + log.lunch + log.dinner + log.snacks,
                timestamp = com.google.firebase.Timestamp.now()
            )
            getFoodCollection().document(docId).set(finalLog).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFoodLogs(): Result<List<FoodLog>> {
        return try {
            val snapshot = getFoodCollection()
                .orderBy("dateString", Query.Direction.DESCENDING)
                .limit(60)
                .get()
                .await()
            Result.success(snapshot.toObjects(FoodLog::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFoodLogForDate(dateString: String): Result<FoodLog?> {
        return try {
            val doc = getFoodCollection().document(dateString).get().await()
            if (doc.exists()) {
                Result.success(doc.toObject(FoodLog::class.java))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
