package com.example.thisdayinhistory.auth

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserProfile(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val photoUrl: String?
)

class AuthManager(context: Context) {
    private var firebaseAuth: FirebaseAuth? = null

    private val _userState = MutableStateFlow<UserProfile?>(null)
    val userState: StateFlow<UserProfile?> = _userState.asStateFlow()

    init {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            firebaseAuth = FirebaseAuth.getInstance()
            updateUser(firebaseAuth?.currentUser)
            firebaseAuth?.addAuthStateListener { auth ->
                updateUser(auth.currentUser)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateUser(user: FirebaseUser?) {
        if (user != null) {
            _userState.value = UserProfile(
                uid = user.uid,
                displayName = user.displayName ?: user.email?.substringBefore("@"),
                email = user.email,
                photoUrl = user.photoUrl?.toString()
            )
        } else {
            _userState.value = null
        }
    }

    fun signOut() {
        try {
            firebaseAuth?.signOut()
            _userState.value = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun signInSimulatedUser(name: String = "Google User", email: String = "user@gmail.com") {
        _userState.value = UserProfile(
            uid = "simulated_google_id",
            displayName = name,
            email = email,
            photoUrl = null
        )
    }
}
