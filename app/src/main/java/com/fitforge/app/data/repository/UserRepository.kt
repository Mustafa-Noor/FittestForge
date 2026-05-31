package com.fitforge.app.data.repository

import com.fitforge.app.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    suspend fun getUserProfile(): Result<User?> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val document = usersCollection.document(uid).get().await()
            if (document.exists()) {
                Result.success(document.toObject(User::class.java))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            usersCollection.document(uid).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePersonalityMode(mode: String): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            usersCollection.document(uid).update("personalityMode", mode).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateMomentum(value: Float): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            usersCollection.document(uid).update("momentum", value).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
