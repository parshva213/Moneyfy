package com.moneyfy.data

data class Contact(
    val id: String = "",
    val name: String,
    val type: ContactType = ContactType.PERSON,
    val phone: String? = null,
    val email: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
