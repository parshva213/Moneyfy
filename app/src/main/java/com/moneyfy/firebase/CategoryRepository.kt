package com.moneyfy.firebase

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.moneyfy.data.Category
import com.moneyfy.data.CategoryType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CategoryRepository {
    fun getAllCategories(): Flow<List<Category>> = nodeFlow("categories") { snapshot ->
        snapshot.children.mapNotNull { it.toCategory() }
            .sortedWith(compareBy({ it.type.name }, { it.name.lowercase() }))
    }

    fun getCategoriesByType(type: CategoryType): Flow<List<Category>> = nodeFlow("categories") { snapshot ->
        snapshot.children.mapNotNull { it.toCategory() }
            .filter { it.type == type }
            .sortedBy { it.name.lowercase() }
    }

    suspend fun getCategoryById(id: String): Category? {
        val snapshot = FirebaseProvider.userItemsNode("categories").child(id).get().await() as DataSnapshot
        return snapshot.toCategory()
    }

    suspend fun insertCategory(category: Category): String {
        val node = FirebaseProvider.userItemsNode("categories")
        val categoryId = category.id.ifEmpty { node.push().key ?: throw IllegalStateException("Could not create category id") }
        val payload = category.copy(id = categoryId)
        node.child(categoryId).setValue(payload.toMap()).await()
        return categoryId
    }

    suspend fun updateCategory(category: Category) {
        require(category.id.isNotEmpty()) { "Category id is required" }
        FirebaseProvider.userItemsNode("categories")
            .child(category.id)
            .setValue(category.toMap())
            .await()
    }

    suspend fun deleteCategory(category: Category) {
        if (category.id.isEmpty()) return
        FirebaseProvider.userItemsNode("categories")
            .child(category.id)
            .removeValue()
            .await()
    }

    private fun nodeFlow(
        path: String,
        mapper: (DataSnapshot) -> List<Category>
    ): Flow<List<Category>> = callbackFlow {
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
        private const val TAG = "CategoryRepository"
    }
}
