package com.moneyfy.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moneyfy.MoneyfyApplication
import com.moneyfy.data.Contact
import com.moneyfy.firebase.ContactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ContactViewModel(application: Application) : AndroidViewModel(application) {
    private val contactRepository: ContactRepository =
        (application as MoneyfyApplication).contactRepository

    val allContacts: Flow<List<Contact>> = contactRepository.getAllContacts()

    val contactsList = contactRepository.getAllContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addContact(contact: Contact) {
        viewModelScope.launch {
            try {
                contactRepository.insertContact(contact)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateContact(contact: Contact) {
        viewModelScope.launch {
            try {
                contactRepository.updateContact(contact)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            try {
                contactRepository.deleteContact(contact)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
