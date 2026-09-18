package com.moneyfy.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object FirebaseProvider {
    private const val TAG = "FirebaseProvider"
    // URL without trailing slash to match google-services.json
    const val DB_URL = "https://moneyfy-phs-default-rtdb.firebaseio.com"

    val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    private var database: FirebaseDatabase? = null

    fun initDatabase() {
        if (database != null) return
        try {
            database = FirebaseDatabase.getInstance(DB_URL).apply {
                setPersistenceEnabled(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firebase Database with URL: $DB_URL", e)
            // Fallback to default instance if URL based fails
            database = FirebaseDatabase.getInstance().apply {
                try { setPersistenceEnabled(true) } catch (_: Exception) {}
            }
        }
    }

    private val realtimeDb: FirebaseDatabase
        get() {
            initDatabase()
            return database ?: FirebaseDatabase.getInstance()
        }

    fun getEffectiveUid(): String? = auth.currentUser?.uid

    fun requireUid(): String {
        return getEffectiveUid()
            ?: throw IllegalStateException("Not signed in to Firebase. Please log in again.")
    }

    fun userNode(uid: String = requireUid()): DatabaseReference {
        return realtimeDb.reference.child("users").child(uid)
    }

    fun userItemsNode(path: String): DatabaseReference {
        return userNode(requireUid()).child(path)
    }

    fun userItemsNode(path: String, uid: String): DatabaseReference {
        return userNode(uid).child(path)
    }

    fun usernameNode(username: String): DatabaseReference {
        return realtimeDb.reference.child("usernames").child(normalizeUsername(username))
    }

    fun normalizeUsername(username: String): String =
        username.trim().lowercase()

    fun toAuthEmail(username: String): String {
        val trimmed = username.trim()
        return if (trimmed.contains("@")) trimmed else "${normalizeUsername(trimmed)}@moneyfy.app"
    }
}
