package com.moneyfy.data

data class TransactionWithDetails(
    val transaction: Transaction,
    val account: Account,
    val toAccount: Account? = null,
    val category: Category? = null,
    val contact: Contact? = null
)

fun buildTransactionDetails(
    transactions: List<Transaction>,
    accounts: List<Account>,
    categories: List<Category>,
    contacts: List<Contact>
): List<TransactionWithDetails> {
    val accountMap = accounts.associateBy { it.id }
    val categoryMap = categories.associateBy { it.id }
    val contactMap = contacts.associateBy { it.id }

    return transactions.mapNotNull { transaction ->
        val account = accountMap[transaction.accountId] ?: return@mapNotNull null
        TransactionWithDetails(
            transaction = transaction,
            account = account,
            toAccount = transaction.toAccountId?.let { accountMap[it] },
            category = transaction.categoryId?.let { categoryMap[it] },
            contact = transaction.contactId?.let { contactMap[it] }
        )
    }.sortedByDescending { it.transaction.date }
}
