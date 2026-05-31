package com.fitforge.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser get() = auth.currentUser

    suspend fun signUp(email: String, password: String) = try {
        auth.createUserWithEmailAndPassword(email, password).await()
        Result.success(auth.currentUser)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun signIn(email: String, password: String) = try {
        auth.signInWithEmailAndPassword(email, password).await()
        Result.success(auth.currentUser)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun resetPassword(email: String) = try {
        auth.sendPasswordResetEmail(email).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun signOut() {
        auth.signOut()
    }
}
