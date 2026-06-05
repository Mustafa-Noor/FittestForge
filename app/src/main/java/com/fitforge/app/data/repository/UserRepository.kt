package com.fitforge.app.data.repository

import com.fitforge.app.data.models.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    private fun getUserDocument() =
        usersCollection.document(auth.currentUser?.uid ?: throw Exception("User not authenticated"))

    suspend fun getUserProfile(): Result<User?> {
        return try {
            val document = getUserDocument().get().await()
            if (document.exists()) {
                Result.success(document.toObject(User::class.java))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createUserProfile(user: User): Result<Unit> {
        return try {
            getUserDocument().set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfileFields(fields: Map<String, Any>): Result<Unit> {
        return try {
            getUserDocument().update(fields).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateMomentumAndStreak(
        newMomentum: Float,
        newStreak: Int,
        bestStreak: Int,
        lastWorkoutDate: String
    ): Result<Unit> {
        return try {
            val updates = mapOf(
                "momentum" to newMomentum,
                "currentStreak" to newStreak,
                "bestStreak" to bestStreak,
                "lastWorkoutDate" to lastWorkoutDate,
                "momentumUpdatedAt" to java.time.LocalDate.now().toString()
            )
            getUserDocument().update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unlockBadge(badgeId: String): Result<Unit> {
        return try {
            val updates = mapOf(
                "badges.$badgeId" to true,
                "badgeUnlockedAt.$badgeId" to FieldValue.serverTimestamp()
            )
            getUserDocument().update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserStats(): Result<User> {
        return try {
            val document = getUserDocument().get().await()
            val user = document.toObject(User::class.java) ?: throw Exception("User not found")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Keeping these for backward compatibility if needed, but they should ideally use updateProfileFields
    suspend fun updatePersonalityMode(mode: String): Result<Unit> {
        return updateProfileFields(mapOf("personalityMode" to mode))
    }

    suspend fun updateMomentum(value: Float): Result<Unit> {
        return updateProfileFields(mapOf("momentum" to value, "momentumUpdatedAt" to java.time.LocalDate.now().toString()))
    }
}
