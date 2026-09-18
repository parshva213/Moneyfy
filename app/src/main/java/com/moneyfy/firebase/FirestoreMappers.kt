package com.moneyfy.firebase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.firestore.DocumentSnapshot
import com.moneyfy.data.Account
import com.moneyfy.data.AccountType
import com.moneyfy.data.Category
import com.moneyfy.data.CategoryType
import com.moneyfy.data.Contact
import com.moneyfy.data.ContactType
import com.moneyfy.data.Transaction
import com.moneyfy.data.TransactionType
import com.moneyfy.data.UserProfile

/** Firebase Realtime Database rejects null values in setValue/updateChildren maps. */
private fun firebaseMapOf(vararg pairs: Pair<String, Any?>): Map<String, Any> =
    pairs.mapNotNull { (key, value) ->
        if (value == null) null else key to value
    }.toMap()

fun Account.toMap(): Map<String, Any> = firebaseMapOf(
    "id" to id,
    "name" to name,
    "type" to type.name,
    "currency" to currency,
    "initialBalance" to initialBalance,
    "colorHex" to colorHex,
    "isActive" to isActive,
    "createdAt" to createdAt
)

fun DocumentSnapshot.toAccount(): Account? {
    if (!exists()) return null
    return Account(
        id = id,
        name = getString("name") ?: return null,
        type = getString("type")?.let { runCatching { AccountType.valueOf(it) }.getOrNull() }
            ?: AccountType.CASH,
        currency = getString("currency") ?: "INR",
        initialBalance = getDouble("initialBalance") ?: 0.0,
        colorHex = getString("colorHex"),
        isActive = getBoolean("isActive") ?: true,
        createdAt = getLong("createdAt") ?: System.currentTimeMillis()
    )
}

fun DataSnapshot.toAccount(): Account? {
    if (!exists()) return null
    val keyId = key ?: ""
    val map = value as? Map<*, *> ?: return null
    val name = map["name"] as? String ?: return null
    val typeStr = map["type"] as? String
    val type = typeStr?.let { runCatching { AccountType.valueOf(it) }.getOrNull() } ?: AccountType.CASH
    val currency = map["currency"] as? String ?: "INR"
    val initialBalance = (map["initialBalance"] as? Number)?.toDouble() ?: 0.0
    val colorHex = map["colorHex"] as? String
    val isActive = map["isActive"] as? Boolean ?: true
    val createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()

    return Account(
        id = keyId,
        name = name,
        type = type,
        currency = currency,
        initialBalance = initialBalance,
        colorHex = colorHex,
        isActive = isActive,
        createdAt = createdAt
    )
}

fun Category.toMap(): Map<String, Any> = firebaseMapOf(
    "id" to id,
    "name" to name,
    "type" to type.name,
    "iconName" to iconName,
    "colorHex" to colorHex,
    "parentId" to parentId,
    "isDefault" to isDefault,
    "createdAt" to createdAt
)

fun DocumentSnapshot.toCategory(): Category? {
    if (!exists()) return null
    return Category(
        id = id,
        name = getString("name") ?: return null,
        type = getString("type")?.let { runCatching { CategoryType.valueOf(it) }.getOrNull() }
            ?: return null,
        iconName = getString("iconName"),
        colorHex = getString("colorHex"),
        parentId = getString("parentId"),
        isDefault = getBoolean("isDefault") ?: false,
        createdAt = getLong("createdAt") ?: System.currentTimeMillis()
    )
}

fun DataSnapshot.toCategory(): Category? {
    if (!exists()) return null
    val keyId = key ?: ""
    val map = value as? Map<*, *> ?: return null
    val name = map["name"] as? String ?: return null
    val typeStr = map["type"] as? String ?: return null
    val type = runCatching { CategoryType.valueOf(typeStr) }.getOrNull() ?: return null
    val iconName = map["iconName"] as? String
    val colorHex = map["colorHex"] as? String
    val parentId = map["parentId"] as? String
    val isDefault = map["isDefault"] as? Boolean ?: false
    val createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()

    return Category(
        id = keyId,
        name = name,
        type = type,
        iconName = iconName,
        colorHex = colorHex,
        parentId = parentId,
        isDefault = isDefault,
        createdAt = createdAt
    )
}

fun Contact.toMap(): Map<String, Any> = firebaseMapOf(
    "id" to id,
    "name" to name,
    "type" to type.name,
    "phone" to phone,
    "email" to email,
    "notes" to notes,
    "createdAt" to createdAt
)

fun DocumentSnapshot.toContact(): Contact? {
    if (!exists()) return null
    return Contact(
        id = id,
        name = getString("name") ?: return null,
        type = getString("type")?.let { runCatching { ContactType.valueOf(it) }.getOrNull() }
            ?: ContactType.PERSON,
        phone = getString("phone"),
        email = getString("email"),
        notes = getString("notes"),
        createdAt = getLong("createdAt") ?: System.currentTimeMillis()
    )
}

fun DataSnapshot.toContact(): Contact? {
    if (!exists()) return null
    val keyId = key ?: ""
    val map = value as? Map<*, *> ?: return null
    val name = map["name"] as? String ?: return null
    val typeStr = map["type"] as? String
    val type = typeStr?.let { runCatching { ContactType.valueOf(it) }.getOrNull() } ?: ContactType.PERSON
    val phone = map["phone"] as? String
    val email = map["email"] as? String
    val notes = map["notes"] as? String
    val createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()

    return Contact(
        id = keyId,
        name = name,
        type = type,
        phone = phone,
        email = email,
        notes = notes,
        createdAt = createdAt
    )
}

fun Transaction.toMap(): Map<String, Any> = firebaseMapOf(
    "id" to id,
    "type" to type.name,
    "amount" to amount,
    "title" to title,
    "notes" to notes,
    "date" to date,
    "accountId" to accountId,
    "toAccountId" to toAccountId,
    "categoryId" to categoryId,
    "contactId" to contactId,
    "createdAt" to createdAt,
    "updatedAt" to updatedAt
)

fun DocumentSnapshot.toTransaction(): Transaction? {
    if (!exists()) return null
    return Transaction(
        id = id,
        type = getString("type")?.let { runCatching { TransactionType.valueOf(it) }.getOrNull() }
            ?: return null,
        amount = getDouble("amount") ?: return null,
        title = getString("title") ?: return null,
        notes = getString("notes"),
        date = getLong("date") ?: return null,
        accountId = getString("accountId") ?: return null,
        toAccountId = getString("toAccountId"),
        categoryId = getString("categoryId"),
        contactId = getString("contactId"),
        createdAt = getLong("createdAt") ?: System.currentTimeMillis(),
        updatedAt = getLong("updatedAt") ?: System.currentTimeMillis()
    )
}

fun DataSnapshot.toTransaction(): Transaction? {
    if (!exists()) return null
    val keyId = key ?: ""
    val map = value as? Map<*, *> ?: return null
    val typeStr = map["type"] as? String ?: return null
    val type = runCatching { TransactionType.valueOf(typeStr) }.getOrNull() ?: return null
    val amount = (map["amount"] as? Number)?.toDouble() ?: return null
    val title = map["title"] as? String ?: return null
    val notes = map["notes"] as? String
    val date = (map["date"] as? Number)?.toLong() ?: return null
    val accountId = map["accountId"] as? String ?: return null
    val toAccountId = map["toAccountId"] as? String
    val categoryId = map["categoryId"] as? String
    val contactId = map["contactId"] as? String
    val createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
    val updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()

    return Transaction(
        id = keyId,
        type = type,
        amount = amount,
        title = title,
        notes = notes,
        date = date,
        accountId = accountId,
        toAccountId = toAccountId,
        categoryId = categoryId,
        contactId = contactId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun UserProfile.toMap(): Map<String, Any> = firebaseMapOf(
    "username" to username,
    "email" to email,
    "securityQuestion" to securityQuestion,
    "securityAnswer" to securityAnswer
)

fun DocumentSnapshot.toUserProfile(): UserProfile? {
    if (!exists()) return null
    return UserProfile(
        username = getString("username") ?: "",
        email = getString("email") ?: "",
        securityQuestion = getString("securityQuestion") ?: "",
        securityAnswer = getString("securityAnswer") ?: ""
    )
}

fun DataSnapshot.toUserProfile(): UserProfile? {
    if (!exists()) return null
    val map = value as? Map<*, *> ?: return null
    val username = map["username"] as? String ?: ""
    val email = map["email"] as? String ?: ""
    val securityQuestion = map["securityQuestion"] as? String ?: ""
    val securityAnswer = map["securityAnswer"] as? String ?: ""
    return UserProfile(
        username = username,
        email = email,
        securityQuestion = securityQuestion,
        securityAnswer = securityAnswer
    )
}
