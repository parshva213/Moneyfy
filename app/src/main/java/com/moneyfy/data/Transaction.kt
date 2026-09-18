package com.moneyfy.data

data class Transaction(
    val id: String = "",
    val type: TransactionType,
    val amount: Double,
    val title: String,
    val notes: String? = null,
    val date: Long,
    val accountId: String,
    val toAccountId: String? = null,
    val categoryId: String? = null,
    val contactId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
