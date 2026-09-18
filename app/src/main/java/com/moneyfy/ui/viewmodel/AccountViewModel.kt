package com.moneyfy.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moneyfy.MoneyfyApplication
import com.moneyfy.data.Account
import com.moneyfy.firebase.AccountRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AccountViewModel(application: Application) : AndroidViewModel(application) {
    private val accountRepository: AccountRepository =
        (application as MoneyfyApplication).accountRepository

    val allAccounts = accountRepository.getAllAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeAccounts = accountRepository.getActiveAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    fun addAccount(account: Account, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                accountRepository.insertAccount(account)
                _messages.emit("Account added")
                onComplete(true)
            } catch (e: Exception) {
                e.printStackTrace()
                _messages.emit(e.message ?: "Failed to add account")
                onComplete(false)
            }
        }
    }

    fun updateAccount(account: Account) {
        viewModelScope.launch {
            try {
                accountRepository.updateAccount(account)
                _messages.emit("Account updated")
            } catch (e: Exception) {
                e.printStackTrace()
                _messages.emit(e.message ?: "Failed to update account")
            }
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            try {
                accountRepository.deleteAccount(account)
                _messages.emit("Account deleted")
            } catch (e: Exception) {
                e.printStackTrace()
                _messages.emit(e.message ?: "Failed to delete account")
            }
        }
    }
}
