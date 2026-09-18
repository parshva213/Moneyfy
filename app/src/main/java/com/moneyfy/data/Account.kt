package com.moneyfy.data

data class Account(
    val id: String = "",
    val name: String,
    val type: AccountType = AccountType.CASH,
    val currency: String = "INR",
    val initialBalance: Double = 0.0,
    val colorHex: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
