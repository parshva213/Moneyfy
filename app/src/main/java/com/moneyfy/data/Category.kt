package com.moneyfy.data

data class Category(
    val id: String = "",
    val name: String,
    val type: CategoryType,
    val iconName: String? = null,
    val colorHex: String? = null,
    val parentId: String? = null,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
