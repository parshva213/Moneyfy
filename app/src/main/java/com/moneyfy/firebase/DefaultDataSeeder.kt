package com.moneyfy.firebase

import com.google.firebase.database.DataSnapshot
import com.moneyfy.data.Account
import com.moneyfy.data.AccountType
import com.moneyfy.data.Category
import com.moneyfy.data.CategoryType
import kotlinx.coroutines.tasks.await

object DefaultDataSeeder {
    suspend fun seedIfNeeded() {
        try {
            seedAccountsIfNeeded()
            seedCategoriesIfNeeded()
        } catch (_: Exception) {
            // Ignore seeding failures if offline / unauthenticated
        }
    }

    private suspend fun seedAccountsIfNeeded() {
        val node = FirebaseProvider.userItemsNode("accounts")
        val snapshot: DataSnapshot = node.get().await()
        if (snapshot.exists() && snapshot.childrenCount > 0) return

        val newRef = node.push()
        val accountId = newRef.key ?: "cash_default"
        newRef.setValue(
            Account(
                id = accountId,
                name = "Cash",
                type = AccountType.CASH,
                isActive = true
            ).toMap()
        ).await()
    }

    private suspend fun seedCategoriesIfNeeded() {
        val node = FirebaseProvider.userItemsNode("categories")
        val snapshot: DataSnapshot = node.get().await()
        if (snapshot.exists() && snapshot.childrenCount > 0) return

        val now = System.currentTimeMillis()
        val defaults = listOf(
            "Salary" to CategoryType.INCOME,
            "Business" to CategoryType.INCOME,
            "Gift" to CategoryType.INCOME,
            "Refund" to CategoryType.INCOME,
            "Other Income" to CategoryType.INCOME,
            "Food" to CategoryType.EXPENSE,
            "Transport" to CategoryType.EXPENSE,
            "Shopping" to CategoryType.EXPENSE,
            "Bills" to CategoryType.EXPENSE,
            "Rent" to CategoryType.EXPENSE,
            "Health" to CategoryType.EXPENSE,
            "Entertainment" to CategoryType.EXPENSE,
            "Education" to CategoryType.EXPENSE,
            "Other Expense" to CategoryType.EXPENSE
        )

        defaults.forEach { (name, type) ->
            val childRef = node.push()
            val catId = childRef.key ?: java.util.UUID.randomUUID().toString()
            childRef.setValue(
                Category(
                    id = catId,
                    name = name,
                    type = type,
                    isDefault = true,
                    createdAt = now
                ).toMap()
            ).await()
        }
    }
}
