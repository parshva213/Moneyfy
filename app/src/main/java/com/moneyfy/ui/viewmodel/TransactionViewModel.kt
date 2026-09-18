package com.moneyfy.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moneyfy.MoneyfyApplication
import com.moneyfy.data.Transaction
import com.moneyfy.data.TransactionType
import com.moneyfy.data.TransactionWithDetails
import com.moneyfy.data.buildTransactionDetails
import com.moneyfy.firebase.AccountRepository
import com.moneyfy.firebase.CategoryRepository
import com.moneyfy.firebase.ContactRepository
import com.moneyfy.firebase.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as MoneyfyApplication
    private val transactionRepository: TransactionRepository = app.transactionRepository
    private val accountRepository: AccountRepository = app.accountRepository
    private val categoryRepository: CategoryRepository = app.categoryRepository
    private val contactRepository: ContactRepository = app.contactRepository

    val allTransactionsWithDetails: Flow<List<TransactionWithDetails>> = combine(
        transactionRepository.getAllTransactions(),
        accountRepository.getAllAccounts(),
        categoryRepository.getAllCategories(),
        contactRepository.getAllContacts()
    ) { transactions, accounts, categories, contacts ->
        buildTransactionDetails(transactions, accounts, categories, contacts)
    }

    private val _selectedTransaction = MutableStateFlow<TransactionWithDetails?>(null)
    val selectedTransaction: StateFlow<TransactionWithDetails?> = _selectedTransaction.asStateFlow()

    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> {
        return transactionRepository.getTransactionsByDateRange(startDate, endDate)
    }

    fun getTransactionsWithDetailsByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionWithDetails>> {
        return combine(
            transactionRepository.getTransactionsByDateRange(startDate, endDate),
            accountRepository.getAllAccounts(),
            categoryRepository.getAllCategories(),
            contactRepository.getAllContacts()
        ) { transactions, accounts, categories, contacts ->
            buildTransactionDetails(transactions, accounts, categories, contacts)
        }
    }

    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> {
        return transactionRepository.getTransactionsByType(type)
    }

    fun getTransactionsByAccount(accountId: String): Flow<List<Transaction>> {
        return transactionRepository.getTransactionsByAccount(accountId)
    }

    fun getTransactionsByCategory(categoryId: String): Flow<List<Transaction>> {
        return transactionRepository.getTransactionsByCategory(categoryId)
    }

    fun getTransactionsByContact(contactId: String): Flow<List<Transaction>> {
        return transactionRepository.getTransactionsByContact(contactId)
    }

    fun loadTransactionById(id: String) {
        viewModelScope.launch {
            try {
                val transaction = transactionRepository.getTransactionById(id) ?: run {
                    _selectedTransaction.value = null
                    return@launch
                }

                val account = accountRepository.getAccountById(transaction.accountId) ?: run {
                    _selectedTransaction.value = null
                    return@launch
                }

                _selectedTransaction.value = TransactionWithDetails(
                    transaction = transaction,
                    account = account,
                    toAccount = transaction.toAccountId?.let { accountRepository.getAccountById(it) },
                    category = transaction.categoryId?.let { categoryRepository.getCategoryById(it) },
                    contact = transaction.contactId?.let { contactRepository.getContactById(it) }
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                transactionRepository.insertTransaction(transaction)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                transactionRepository.updateTransaction(transaction)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                transactionRepository.deleteTransaction(transaction)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getTotalIncome(startDate: Long, endDate: Long): Flow<Double> {
        return transactionRepository.getTotalIncome(startDate, endDate)
    }

    fun getTotalExpense(startDate: Long, endDate: Long): Flow<Double> {
        return transactionRepository.getTotalExpense(startDate, endDate)
    }

    fun getMonthRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis

        return Pair(start, end)
    }
}
