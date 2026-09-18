package com.moneyfy

import android.app.Application
import android.util.Log
import com.moneyfy.firebase.AccountRepository
import com.moneyfy.firebase.AuthRepository
import com.moneyfy.firebase.CategoryRepository
import com.moneyfy.firebase.ContactRepository
import com.moneyfy.firebase.FirebaseProvider
import com.moneyfy.firebase.TransactionRepository

class MoneyfyApplication : Application() {
    val authRepository: AuthRepository by lazy { AuthRepository(this) }
    val accountRepository: AccountRepository by lazy { AccountRepository() }
    val categoryRepository: CategoryRepository by lazy { CategoryRepository() }
    val contactRepository: ContactRepository by lazy { ContactRepository() }
    val transactionRepository: TransactionRepository by lazy { TransactionRepository() }

    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseProvider.initDatabase()
            // Clear session on app startup so app always requires login when reopened
            authRepository.logout()
        } catch (e: Exception) {
            Log.e("MoneyfyApplication", "Failed to initialize Firebase Database", e)
        }
    }
}
