package com.moneyfy.firebase

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.moneyfy.data.Account
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AccountRepository {
    fun getAllAccounts(): Flow<List<Account>> = nodeFlow("accounts") { snapshot ->
        snapshot.children.mapNotNull { it.toAccount() }
            .sortedBy { it.name.lowercase() }
    }

    fun getActiveAccounts(): Flow<List<Account>> = nodeFlow("accounts") { snapshot ->
        snapshot.children.mapNotNull { it.toAccount() }
            .filter { it.isActive }
            .sortedBy { it.name.lowercase() }
    }

    suspend fun getAccountById(id: String): Account? {
        val snapshot = FirebaseProvider.userItemsNode("accounts").child(id).get().await() as DataSnapshot
        return snapshot.toAccount()
    }

    suspend fun insertAccount(account: Account): String {
        val node = FirebaseProvider.userItemsNode("accounts")
        val accountId = account.id.ifEmpty { node.push().key ?: throw IllegalStateException("Could not create account id") }
        val payload = account.copy(id = accountId)
        node.child(accountId).setValue(payload.toMap()).await()
        return accountId
    }

    suspend fun updateAccount(account: Account) {
        require(account.id.isNotEmpty()) { "Account id is required" }
        FirebaseProvider.userItemsNode("accounts")
            .child(account.id)
            .setValue(account.toMap())
            .await()
    }

    suspend fun deleteAccount(account: Account) {
        if (account.id.isEmpty()) return
        FirebaseProvider.userItemsNode("accounts")
            .child(account.id)
            .removeValue()
            .await()
    }

    suspend fun deactivateAccount(id: String) {
        val node = FirebaseProvider.userItemsNode("accounts").child(id)
        val snapshot = node.get().await() as DataSnapshot
        val account = snapshot.toAccount() ?: return
        node.setValue(account.copy(isActive = false).toMap()).await()
    }

    private fun nodeFlow(
        path: String,
        mapper: (DataSnapshot) -> List<Account>
    ): Flow<List<Account>> = callbackFlow {
        val node = try {
            FirebaseProvider.userItemsNode(path)
        } catch (e: Exception) {
            Log.e(TAG, "Cannot access $path", e)
            trySend(emptyList())
            close(e)
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(mapper(snapshot))
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Listener cancelled for $path: ${error.message}")
                close(error.toException())
            }
        }
        node.addValueEventListener(listener)
        awaitClose { node.removeEventListener(listener) }
    }

    companion object {
        private const val TAG = "AccountRepository"
    }
}
