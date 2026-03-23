package com.chitchat.app.data.firebase

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

object FirebaseAuthManager {
    private const val TEMP_ACCOUNT_DOMAIN = "blackchat.app"

    private fun authOrNull(): FirebaseAuth? = runCatching { FirebaseAuth.getInstance() }.getOrNull()

    private fun requireAuth(): FirebaseAuth = authOrNull()
        ?: throw IllegalStateException("Firebase Auth is not ready. Check google-services.json and Firebase initialization.")

    suspend fun ensureSignedIn(): String {
        val auth = requireAuth()
        auth.currentUser?.uid?.let { return it }
        val result = auth.signInAnonymously().await()
        return result.user?.uid ?: error("Anonymous sign-in failed")
    }

    suspend fun ensureAnonymousSession(): String {
        val auth = requireAuth()
        val current = auth.currentUser
        if (current != null) return current.uid
        val result = auth.signInAnonymously().await()
        return result.user?.uid ?: error("Anonymous fallback session failed")
    }

    suspend fun registerTemporaryAccount(username: String, password: String): String {
        val auth = requireAuth()
        val email = temporaryEmailForUsername(username)
        val credential = EmailAuthProvider.getCredential(email, password)
        val current = auth.currentUser
        return when {
            current?.isAnonymous == true -> {
                val result = current.linkWithCredential(credential).await()
                result.user?.uid ?: error("Temporary account linking failed")
            }
            current == null -> {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                result.user?.uid ?: error("Temporary account creation failed")
            }
            else -> {
                auth.signOut()
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                result.user?.uid ?: error("Temporary account creation failed")
            }
        }
    }

    suspend fun signInTemporaryAccount(username: String, password: String, authEmailOverride: String? = null): String {
        val auth = requireAuth()
        auth.signOut()
        val email = authEmailOverride?.trim()?.lowercase().orEmpty().ifBlank { temporaryEmailForUsername(username) }
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user?.uid ?: error("Temporary account login failed")
    }

    suspend fun registerEmailAccount(email: String, password: String): String {
        val auth = requireAuth()
        val normalizedEmail = normalizeEmail(email)
        val credential = EmailAuthProvider.getCredential(normalizedEmail, password)
        val current = auth.currentUser
        return when {
            current?.isAnonymous == true -> {
                val result = current.linkWithCredential(credential).await()
                result.user?.uid ?: error("Email account linking failed")
            }
            current == null -> {
                val result = auth.createUserWithEmailAndPassword(normalizedEmail, password).await()
                result.user?.uid ?: error("Email account creation failed")
            }
            else -> {
                auth.signOut()
                val result = auth.createUserWithEmailAndPassword(normalizedEmail, password).await()
                result.user?.uid ?: error("Email account creation failed")
            }
        }
    }

    suspend fun signInEmailAccount(email: String, password: String): String {
        val auth = requireAuth()
        auth.signOut()
        val result = auth.signInWithEmailAndPassword(normalizeEmail(email), password).await()
        return result.user?.uid ?: error("Email login failed")
    }

    fun currentUid(): String? = authOrNull()?.currentUser?.uid

    fun currentPhoneNumber(): String = authOrNull()?.currentUser?.phoneNumber.orEmpty()

    fun currentEmail(): String = authOrNull()?.currentUser?.email.orEmpty()

    fun isAnonymous(): Boolean = authOrNull()?.currentUser?.isAnonymous == true

    fun hasPermanentAccount(): Boolean = authOrNull()?.currentUser?.let { !it.isAnonymous } == true

    suspend fun deleteAnonymousUserIfNeeded(): Boolean {
        val current = authOrNull()?.currentUser ?: return false
        if (!current.isAnonymous) return false
        runCatching { current.reload().await() }
        current.delete().await()
        return true
    }

    suspend fun deleteCurrentUserIfPossible(): Boolean {
        val current = authOrNull()?.currentUser ?: return false
        runCatching { current.reload().await() }
        current.delete().await()
        return true
    }

    fun signOut() {
        authOrNull()?.signOut()
    }

    fun currentLabel(): String {
        val uid = currentUid() ?: return "You"
        return "User-${uid.takeLast(6)}"
    }

    fun temporaryEmailForUsername(rawUsername: String): String {
        val normalized = rawUsername
            .trim()
            .removePrefix("@")
            .lowercase()
            .filter { it.isLetterOrDigit() || it == '_' || it == '.' }
            .ifBlank { error("Username required") }
        return "$normalized@$TEMP_ACCOUNT_DOMAIN"
    }

    private fun normalizeEmail(rawEmail: String): String = rawEmail.trim().lowercase()
}
