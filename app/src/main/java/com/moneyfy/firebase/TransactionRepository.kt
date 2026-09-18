package com.moneyfy.firebase

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.moneyfy.data.Transaction
import com.moneyfy.data.TransactionType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>> = nodeFlow("transactions") { snapshot ->
        snapshot.children.mapNotNull { it.toTransaction() }
            .sortedByDescending { it.date }
    }

    suspend fun getTransactionById(id: String): Transaction? {
        val snapshot = FirebaseProvider.userItemsNode("transactions").child(id).get().await() as DataSnapshot
        return snapshot.toTransaction()
    }

    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> =
        getAllTransactions().map { transactions ->
            transactions.filter { it.date in startDate..endDate }
        }

    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> =
        getAllTransactions().map { transactions ->
            transactions.filter { it.type == type }
        }

    fun getTransactionsByAccount(accountId: String): Flow<List<Transaction>> =
        getAllTransactions().map { transactions ->
            transactions.filter { it.accountId == accountId || it.toAccountId == accountId }
        }

    fun getTransactionsByCategory(categoryId: String): Flow<List<Transaction>> =
        getAllTransactions().map { transactions ->
            transactions.filter { it.categoryId == categoryId }
        }

    fun getTransactionsByContact(contactId: String): Flow<List<Transaction>> =
        getAllTransactions().map { transactions ->
            transactions.filter { it.contactId == contactId }
        }

    fun getTotalIncome(startDate: Long, endDate: Long): Flow<Double> =
        getAllTransactions().map { transactions ->
            transactions.filter {
                it.type == TransactionType.INCOME && it.date in startDate..endDate
            }.sumOf { it.amount }
        }

    fun getTotalExpense(startDate: Long, endDate: Long): Flow<Double> =
        getAllTransactions().map { transactions ->
            transactions.filter {
                it.type == TransactionType.EXPENSE && it.date in startDate..endDate
            }.sumOf { it.amount }
        }

    suspend fun insertTransaction(transaction: Transaction): String {
        val node = FirebaseProvider.userItemsNode("transactions")
        val txnId = transaction.id.ifEmpty {
            node.push().key ?: throw IllegalStateException("Could not create transaction id")
        }
        val payload = transaction.copy(
            id = txnId,
            updatedAt = System.currentTimeMillis()
        )
        node.child(txnId).setValue(payload.toMap()).await()
        return txnId
    }

    suspend fun updateTransaction(transaction: Transaction) {
        require(transaction.id.isNotEmpty()) { "Transaction id is required" }
        val payload = transaction.copy(updatedAt = System.currentTimeMillis())
        FirebaseProvider.userItemsNode("transactions")
            .child(transaction.id)
            .setValue(payload.toMap())
            .await()
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        if (transaction.id.isEmpty()) return
        FirebaseProvider.userItemsNode("transactions")
            .child(transaction.id)
            .removeValue()
            .await()
    }

    private fun nodeFlow(
        path: String,
        mapper: (DataSnapshot) -> List<Transaction>
    ): Flow<List<Transaction>> = callbackFlow {
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
        private const val TAG = "TransactionRepository"
    }
}
