package com.chitchat.app.data.firebase

import com.chitchat.app.data.model.FireDeviceToken
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class FcmTokenRepository(
    private val dbProvider: () -> FirebaseFirestore? = { runCatching { FirebaseFirestore.getInstance() }.getOrNull() },
    private val messagingProvider: () -> FirebaseMessaging? = { runCatching { FirebaseMessaging.getInstance() }.getOrNull() },
) {
    private fun requireDb(): FirebaseFirestore = dbProvider()
        ?: throw IllegalStateException("Firebase Firestore is not ready. Check google-services.json and Firebase initialization.")

    private fun requireMessaging(): FirebaseMessaging = messagingProvider()
        ?: throw IllegalStateException("Firebase Messaging is not ready. Check google-services.json and Firebase initialization.")
    suspend fun syncCurrentToken(userId: String) {
        val token = requireMessaging().token.await()
        saveToken(userId, token)
    }

    suspend fun saveToken(userId: String, token: String) {
        val payload = FireDeviceToken(
            userId = userId,
            token = token,
            platform = "android",
            updatedAt = System.currentTimeMillis(),
        )
        val db = requireDb()
        db.collection("users").document(userId).collection("device_tokens").document(token).set(payload).await()
        db.collection("users").document(userId).set(mapOf(
            "notificationToken" to token,
            "lastTokenRefreshAt" to System.currentTimeMillis(),
        ), com.google.firebase.firestore.SetOptions.merge()).await()
    }
}
