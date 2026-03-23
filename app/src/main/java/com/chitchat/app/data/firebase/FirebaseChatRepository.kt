package com.chitchat.app.data.firebase

import com.chitchat.app.data.model.ChatMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseChatRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val messagesRef = db.collection("chats")
        .document("global_chat")
        .collection("messages")

    fun observeMessages(): Flow<List<ChatMessage>> = callbackFlow {
        val registration = messagesRef
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents
                    ?.mapNotNull { doc -> doc.toObject(ChatMessage::class.java)?.copy(id = doc.id) }
                    .orEmpty()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    suspend fun sendMessage(text: String) {
        val uid = FirebaseAuthManager.ensureSignedIn()
        val payload = ChatMessage(
            senderId = uid,
            senderLabel = "User-${uid.takeLast(6)}",
            text = text.trim(),
            createdAt = System.currentTimeMillis()
        )
        messagesRef.add(payload).await()
        db.collection("chats").document("global_chat").set(
            mapOf(
                "updatedAt" to System.currentTimeMillis(),
                "lastMessage" to text.trim(),
                "participants" to listOf(uid)
            )
        ).await()
    }
}
