package com.moneyfy.firebase

import android.content.Context
import android.util.Log
import android.os.Handler
import android.os.Looper
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseException
import com.google.firebase.auth.GoogleAuthProvider
import com.moneyfy.data.UserProfile
import kotlinx.coroutines.tasks.await

class AuthRepository(private val context: Context) {
    private val auth = FirebaseProvider.auth

    private val userPrefs = context.getSharedPreferences("moneyfy_local_users", Context.MODE_PRIVATE)
    private val sessionPrefs = context.getSharedPreferences("moneyfy_auth_session", Context.MODE_PRIVATE)

    val currentUser get() = auth.currentUser
    val isLoggedIn get() = auth.currentUser != null

    suspend fun login(username: String, password: String): Result<Unit> = try {
        FirebaseProvider.initDatabase()
        val normalizedInput = FirebaseProvider.normalizeUsername(username)
        var loginEmail = normalizedInput
        var currentUsername = normalizedInput

        // If it's a username, resolve it to the correct internal email
        if (!normalizedInput.contains("@")) {
            try {
                val usernameNode = FirebaseProvider.usernameNode(normalizedInput).get().await() as DataSnapshot
                if (usernameNode.exists()) {
                    val map = usernameNode.value as? Map<*, *>
                    loginEmail = map?.get("email") as? String ?: FirebaseProvider.toAuthEmail(normalizedInput)
                } else {
                    // Fallback to default format if node is missing
                    loginEmail = FirebaseProvider.toAuthEmail(normalizedInput)
                }
            } catch (e: Exception) {
                loginEmail = FirebaseProvider.toAuthEmail(normalizedInput)
            }
        }

        auth.signInWithEmailAndPassword(loginEmail, password).await()
        
        auth.currentUser?.uid?.let { uid ->
            try {
                val profileSnap = FirebaseProvider.userItemsNode("profile", uid).child("data").get().await() as DataSnapshot
                (profileSnap.value as? Map<*, *>)?.get("username")?.toString()?.let { currentUsername = it }
            } catch (e: Exception) {}
        }

        saveLocalUserBackup(currentUsername, password)
        sessionPrefs.edit().putString("current_user", currentUsername).apply()
        
        try {
            DefaultDataSeeder.seedIfNeeded()
            testDatabaseConnection()
        } catch (e: Exception) {
            Log.w(TAG, "Database unreachable, working offline")
        }
        
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e.toFriendlyException())
    }

    suspend fun register(
        username: String,
        email: String,
        password: String,
        securityQuestion: String,
        securityAnswer: String
    ): Result<Unit> = try {
        FirebaseProvider.initDatabase()
        val normalizedUsername = FirebaseProvider.normalizeUsername(username)
        
        // 1. Check if username is free in the database
        try {
            val usernameNode = FirebaseProvider.usernameNode(normalizedUsername).get().await() as DataSnapshot
            if (usernameNode.exists()) throw IllegalStateException("Username is already taken")
        } catch (e: Exception) {
            if (e is IllegalStateException) throw e
        }

        // 2. Generate a unique internal email to allow "taking" deleted usernames
        // If the user provided a real email, use it. Otherwise generate a versioned one.
        val authEmail = if (email.isNotBlank()) {
            email.trim().lowercase()
        } else {
            // Adding a timestamp ensures this account is unique even if the username was used before
            "${normalizedUsername}.${System.currentTimeMillis()}@moneyfy.app"
        }

        val authResult = auth.createUserWithEmailAndPassword(authEmail, password).await()
        val uid = authResult.user?.uid ?: throw IllegalStateException("Registration failed")

        val profile = UserProfile(
            username = normalizedUsername,
            email = authEmail,
            securityQuestion = securityQuestion.trim(),
            securityAnswer = securityAnswer.trim().lowercase()
        )

        // 3. Save the mapping
        try {
            FirebaseProvider.userItemsNode("profile", uid).child("data").setValue(profile.toMap()).await()
            FirebaseProvider.usernameNode(normalizedUsername)
                .setValue(mapOf("uid" to uid, "username" to normalizedUsername, "email" to authEmail))
                .await()
            DefaultDataSeeder.seedIfNeeded()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write database record")
        }

        saveLocalUserBackup(normalizedUsername, password, securityQuestion, securityAnswer)
        sessionPrefs.edit().putString("current_user", normalizedUsername).apply()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e.toFriendlyException())
    }

    suspend fun loginWithGoogleToken(idToken: String): Result<Unit> = try {
        FirebaseProvider.initDatabase()
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = auth.signInWithCredential(credential).await()
        val user = authResult.user ?: throw IllegalStateException("Google sign-in failed")
        
        val email = user.email ?: ""
        val displayName = user.displayName?.ifBlank { null } ?: email.substringBefore("@")
        val normalizedUsername = FirebaseProvider.normalizeUsername(displayName)
        val uid = user.uid

        syncGoogleProfile(uid, normalizedUsername, email)
        sessionPrefs.edit().putString("current_user", normalizedUsername).apply()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e.toFriendlyException())
    }

    suspend fun loginWithGoogleFallback(email: String, displayName: String, googleId: String): Result<Unit> = try {
        FirebaseProvider.initDatabase()
        val normalizedUsername = FirebaseProvider.normalizeUsername(displayName.ifBlank { email.substringBefore("@") })
        val authEmail = email.ifBlank { "${normalizedUsername}@google.moneyfy.app" }
        val fallbackPass = "GoogleAuth_${googleId}_Security"

        val authResult = try {
            auth.signInWithEmailAndPassword(authEmail, fallbackPass).await()
        } catch (e: Exception) {
            auth.createUserWithEmailAndPassword(authEmail, fallbackPass).await()
        }

        val uid = authResult.user?.uid ?: throw IllegalStateException("Google authentication failed")
        syncGoogleProfile(uid, normalizedUsername, authEmail)
        saveLocalUserBackup(normalizedUsername, fallbackPass)
        sessionPrefs.edit().putString("current_user", normalizedUsername).apply()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e.toFriendlyException())
    }

    private suspend fun syncGoogleProfile(uid: String, username: String, email: String) {
        try {
            val profileSnap = FirebaseProvider.userItemsNode("profile", uid).child("data").get().await() as DataSnapshot
            if (!profileSnap.exists()) {
                val profile = UserProfile(
                    username = username,
                    email = email,
                    securityQuestion = "Google Account",
                    securityAnswer = "google"
                )
                FirebaseProvider.userItemsNode("profile", uid).child("data").setValue(profile.toMap()).await()
                FirebaseProvider.usernameNode(username)
                    .setValue(mapOf("uid" to uid, "username" to username, "email" to email))
                    .await()
            }
            DefaultDataSeeder.seedIfNeeded()
        } catch (e: Exception) {
            Log.w(TAG, "Syncing google user profile: ${e.localizedMessage}")
        }
    }

    suspend fun resetPassword(username: String, newPassword: String): Result<Unit> = try {
        val normalizedUsername = FirebaseProvider.normalizeUsername(username)
        saveLocalUserBackup(normalizedUsername, newPassword)
        
        val email = try {
            val node = FirebaseProvider.usernameNode(normalizedUsername).get().await() as DataSnapshot
            (node.value as? Map<*, *>)?.get("email")?.toString() ?: FirebaseProvider.toAuthEmail(normalizedUsername)
        } catch (e: Exception) {
            FirebaseProvider.toAuthEmail(normalizedUsername)
        }
        
        auth.sendPasswordResetEmail(email).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e.toFriendlyException())
    }

    suspend fun deleteAccount(): Result<Unit> = try {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Not logged in")
        val userSnap = FirebaseProvider.userItemsNode("profile", uid).child("data").get().await() as DataSnapshot
        val username = (userSnap.value as? Map<*, *>)?.get("username")?.toString()
        
        if (username != null) {
            FirebaseProvider.usernameNode(username).removeValue().await()
        }
        
        FirebaseProvider.userNode(uid).removeValue().await()
        auth.currentUser?.delete()?.await()
        logout()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e.toFriendlyException())
    }

    suspend fun getSecurityQuestion(username: String): Result<String> = try {
        val normalizedUsername = FirebaseProvider.normalizeUsername(username)
        val usernameNode = FirebaseProvider.usernameNode(normalizedUsername).get().await() as DataSnapshot
        val uid = (usernameNode.value as? Map<*, *>)?.get("uid") as? String
            ?: throw IllegalStateException("User not found")

        val profileSnapshot = FirebaseProvider.userItemsNode("profile", uid).child("data").get().await() as DataSnapshot
        val question = (profileSnapshot.value as? Map<*, *>)?.get("securityQuestion")?.toString()
            ?: throw IllegalStateException("Security question not found")
        Result.success(question)
    } catch (e: Exception) {
        getLocalSecurityQuestion(FirebaseProvider.normalizeUsername(username))?.let { Result.success(it) }
            ?: Result.failure(e.toFriendlyException())
    }

    suspend fun verifySecurityAnswer(username: String, answer: String): Result<Unit> = try {
        val normalizedUsername = FirebaseProvider.normalizeUsername(username)
        val usernameNode = FirebaseProvider.usernameNode(normalizedUsername).get().await() as DataSnapshot
        val uid = (usernameNode.value as? Map<*, *>)?.get("uid") as? String
            ?: throw IllegalStateException("User not found")

        val profileSnapshot = FirebaseProvider.userItemsNode("profile", uid).child("data").get().await() as DataSnapshot
        val correctAnswer = (profileSnapshot.value as? Map<*, *>)?.get("securityAnswer")?.toString()
        
        if (correctAnswer != answer.trim().lowercase()) throw IllegalStateException("Incorrect answer")
        Result.success(Unit)
    } catch (e: Exception) {
        if (verifyLocalSecurityAnswer(FirebaseProvider.normalizeUsername(username), answer)) Result.success(Unit)
        else Result.failure(e.toFriendlyException())
    }

    private fun showNotification(message: String) {
        Handler(Looper.getMainLooper()).post { com.moneyfy.util.AppNotificationManager.showNotification(context, "Moneyfy", message) }
    }

    fun logout() {
        try { auth.signOut() } catch (_: Exception) {}
        sessionPrefs.edit().clear().apply()
    }

    private suspend fun testDatabaseConnection() {
        val uid = FirebaseProvider.requireUid()
        FirebaseProvider.userNode(uid).child("_connection_check").setValue(System.currentTimeMillis()).await()
        FirebaseProvider.userNode(uid).child("_connection_check").removeValue().await()
    }

    private fun saveLocalUserBackup(username: String, pass: String, q: String = "", a: String = "") {
        userPrefs.edit().putString("${username}_pass", pass).apply()
        if (q.isNotBlank()) userPrefs.edit().putString("${username}_q", q).putString("${username}_a", a.trim().lowercase()).apply()
    }

    private fun getLocalSecurityQuestion(username: String) = userPrefs.getString("${username}_q", null)
    private fun verifyLocalSecurityAnswer(username: String, answer: String) = userPrefs.getString("${username}_a", null) == answer.trim().lowercase()

    private fun Throwable.toFriendlyException(): Throwable {
        val msg = message ?: ""
        return when (this) {
            is FirebaseAuthInvalidCredentialsException -> IllegalStateException("Invalid password")
            is FirebaseAuthInvalidUserException -> IllegalStateException("User not found")
            is FirebaseAuthUserCollisionException -> IllegalStateException("Username or Email already exists")
            is FirebaseNetworkException -> IllegalStateException("Check your internet connection")
            is FirebaseAuthException -> {
                if (msg.contains("configuration not found", ignoreCase = true) || msg.contains("internal error", ignoreCase = true)) {
                    IllegalStateException("Firebase Auth not configured. Enable Email/Password in Firebase Console.")
                } else IllegalStateException(localizedMessage ?: msg)
            }
            else -> IllegalStateException(localizedMessage ?: msg.ifBlank { "Error occurred" })
        }
    }

    companion object { private const val TAG = "AuthRepository" }
}
