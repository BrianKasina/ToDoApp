package com.example.programmingassignment.util

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthUtils(private val auth: FirebaseAuth) {

    companion object {
        private const val TAG = "AuthUtils"
    }

    private var authStateListener: FirebaseAuth.AuthStateListener? = null

    // Sign up a new user with email and password
    fun signUp(email: String, password: String, onResult: (FirebaseUser?, Exception?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    onResult(user, null)
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    onResult(null, task.exception)
                }
            }
    }

    // Sign in an existing user with email and password
    fun signIn(email: String, password: String, onResult: (FirebaseUser?, Exception?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    onResult(user, null)
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    onResult(null, task.exception)
                }
            }
    }

    // Sign out the current user
    fun signOut() {
        auth.signOut()
        Log.d(TAG, "User signed out")
    }

    // Get the current user (if any)
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    // Check if a user is authenticated
    fun isAuthenticated(): Boolean {
        return getCurrentUser() != null
    }

    // Listen for authentication state changes
    fun addAuthStateListener(onAuthStateChanged: (FirebaseUser?) -> Unit) {
        authStateListener = FirebaseAuth.AuthStateListener { auth ->
            onAuthStateChanged(auth.currentUser)
        }
        auth.addAuthStateListener(authStateListener!!)
    }

    // Remove the authentication state listener
    fun removeAuthStateListener() {
        authStateListener?.let { auth.removeAuthStateListener(it) }
        authStateListener = null
    }
}
