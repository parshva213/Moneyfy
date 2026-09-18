package com.moneyfy.firebase

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.moneyfy.data.Contact
import com.moneyfy.data.ContactType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ContactRepository {
    fun getAllContacts(): Flow<List<Contact>> = nodeFlow("contacts") { snapshot ->
        snapshot.children.mapNotNull { it.toContact() }
            .sortedBy { it.name.lowercase() }
    }

    fun getContactsByType(type: ContactType): Flow<List<Contact>> = nodeFlow("contacts") { snapshot ->
        snapshot.children.mapNotNull { it.toContact() }
            .filter { it.type == type }
            .sortedBy { it.name.lowercase() }
    }

    suspend fun getContactById(id: String): Contact? {
        val snapshot = FirebaseProvider.userItemsNode("contacts").child(id).get().await() as DataSnapshot
        return snapshot.toContact()
    }

    suspend fun insertContact(contact: Contact): String {
        val node = FirebaseProvider.userItemsNode("contacts")
        val contactId = contact.id.ifEmpty { node.push().key ?: throw IllegalStateException("Could not create contact id") }
        val payload = contact.copy(id = contactId)
        node.child(contactId).setValue(payload.toMap()).await()
        return contactId
    }

    suspend fun updateContact(contact: Contact) {
        require(contact.id.isNotEmpty()) { "Contact id is required" }
        FirebaseProvider.userItemsNode("contacts")
            .child(contact.id)
            .setValue(contact.toMap())
            .await()
    }

    suspend fun deleteContact(contact: Contact) {
        if (contact.id.isEmpty()) return
        FirebaseProvider.userItemsNode("contacts")
            .child(contact.id)
            .removeValue()
            .await()
    }

    private fun nodeFlow(
        path: String,
        mapper: (DataSnapshot) -> List<Contact>
    ): Flow<List<Contact>> = callbackFlow {
        val node = try {
            FirebaseProvider.userItemsNode(path)
        } catch (e: Exception) {
            Log.e(TAG, "Cannot access $path", e)
            trySend(emptyList())
            close(e)
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(mapper(snapshot))
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Listener cancelled for $path: ${error.message}")
                close(error.toException())
            }
        }
        node.addValueEventListener(listener)
        awaitClose { node.removeEventListener(listener) }
    }

    companion object {
        private const val TAG = "ContactRepository"
    }
}
