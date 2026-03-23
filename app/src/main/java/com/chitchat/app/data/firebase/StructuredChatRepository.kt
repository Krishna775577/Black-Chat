package com.chitchat.app.data.firebase

import com.chitchat.app.data.model.FireChatThread
import com.chitchat.app.data.model.FireMessage
import com.chitchat.app.data.model.FireStatusStory
import com.chitchat.app.data.model.FireUserProfile
import com.chitchat.app.data.model.StorageUploadResult
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UsernameAlreadyExistsException(
    message: String = "This username already exists. Try another username.",
) : IllegalStateException(message)

class StructuredChatRepository(
    private val dbProvider: () -> FirebaseFirestore? = { runCatching { FirebaseFirestore.getInstance() }.getOrNull() },
) {
    private fun requireDb(): FirebaseFirestore = dbProvider()
        ?: throw IllegalStateException("Firebase Firestore is not ready. Check google-services.json and Firebase initialization.")

    private val users get() = requireDb().collection("users")
    private val usernames get() = requireDb().collection("usernames")
    private val chats get() = requireDb().collection("chat_threads")
    private val statuses get() = requireDb().collection("status_stories")
    private val globalRoom get() = requireDb().collection("public_rooms").document("global_chat")

    companion object {
        private const val GLOBAL_MESSAGE_TTL_MS = 60 * 60 * 1000L
    }

    fun observeThreads(userId: String): Flow<List<FireChatThread>> = callbackFlow {
        val registration = chats
            .whereArrayContains("participantIds", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val threads = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    doc.toObject(FireChatThread::class.java)?.copy(id = doc.id)
                }.filterNot { userId in it.hiddenFor }.sortedByDescending { it.updatedAt }
                trySend(threads)
            }
        awaitClose { registration.remove() }
    }

    fun observeMessages(chatId: String): Flow<List<FireMessage>> = callbackFlow {
        val registration = chats
            .document(chatId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    doc.toObject(FireMessage::class.java)?.copy(id = doc.id)
                }
                trySend(messages)
            }
        awaitClose { registration.remove() }
    }


    fun observeGlobalMessages(limit: Long = 300): Flow<List<FireMessage>> = callbackFlow {
        val registration = globalRoom
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val now = System.currentTimeMillis()
                val allMessages = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    doc.toObject(FireMessage::class.java)?.copy(id = doc.id)
                }
                val expiredMessages = allMessages.filter { isExpiredGlobalMessage(it, now) }
                if (expiredMessages.isNotEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        expiredMessages.forEach { expired ->
                            runCatching {
                                globalRoom.collection("messages").document(expired.id).delete().await()
                            }
                        }
                    }
                }
                val messages = allMessages.filterNot { isExpiredGlobalMessage(it, now) }
                trySend(messages)
            }
        awaitClose { registration.remove() }
    }

    private fun isExpiredGlobalMessage(message: FireMessage, now: Long = System.currentTimeMillis()): Boolean {
        val expiry = when {
            message.expiresAt > 0L -> message.expiresAt
            message.createdAt > 0L -> message.createdAt + GLOBAL_MESSAGE_TTL_MS
            else -> 0L
        }
        return expiry in 1..now
    }

    fun observeUserProfiles(userIds: List<String>): Flow<List<FireUserProfile>> = callbackFlow {
        val targetIds = userIds.filter { it.isNotBlank() }.distinct().take(10)
        if (targetIds.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val registration = users
            .whereIn(FieldPath.documentId(), targetIds)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val profiles = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    doc.toObject(FireUserProfile::class.java)?.copy(id = doc.id)
                }
                trySend(profiles)
            }
        awaitClose { registration.remove() }
    }

    suspend fun upsertUserProfile(profile: FireUserProfile) {
        require(profile.id.isNotBlank()) { "User id is required" }
        val normalizedUsername = normalizeUsername(profile.username)
        val normalizedPhone = normalizePhone(profile.phoneNumber)
        val cleanProfile = profile.copy(
            id = profile.id,
            username = normalizedUsername,
            phoneNumber = normalizedPhone,
            temporaryAuthEmail = profile.temporaryAuthEmail.trim().lowercase(),
        )
        requireDb().runTransaction { transaction ->
            val userRef = users.document(cleanProfile.id)
            val currentSnapshot = transaction.get(userRef)
            val currentUsername = normalizeUsername(currentSnapshot.getString("username").orEmpty())
            val preservedTemporaryAuthEmail = cleanProfile.temporaryAuthEmail.ifBlank {
                currentSnapshot.getString("temporaryAuthEmail").orEmpty()
            }

            if (normalizedUsername.isNotBlank()) {
                val usernameRef = usernames.document(normalizedUsername)
                val usernameSnapshot = transaction.get(usernameRef)
                val reservedBy = usernameSnapshot.getString("userId").orEmpty()
                if (usernameSnapshot.exists() && reservedBy.isNotBlank() && reservedBy != cleanProfile.id) {
                    throw UsernameAlreadyExistsException()
                }
                transaction.set(
                    usernameRef,
                    mapOf(
                        "userId" to cleanProfile.id,
                        "username" to normalizedUsername,
                        "displayName" to cleanProfile.displayName,
                        "updatedAt" to System.currentTimeMillis(),
                    ),
                    SetOptions.merge(),
                )
            }

            if (currentUsername.isNotBlank() && currentUsername != normalizedUsername) {
                val oldUsernameRef = usernames.document(currentUsername)
                val oldUsernameSnapshot = transaction.get(oldUsernameRef)
                if (oldUsernameSnapshot.getString("userId").orEmpty() == cleanProfile.id) {
                    transaction.delete(oldUsernameRef)
                }
            }

            transaction.set(userRef, cleanProfile.copy(temporaryAuthEmail = preservedTemporaryAuthEmail), SetOptions.merge())
            null
        }.await()
    }

    suspend fun updatePresence(userId: String, online: Boolean) {
        users.document(userId).set(
            mapOf(
                "online" to online,
                "appState" to if (online) "foreground" else "background",
                "lastSeenAt" to System.currentTimeMillis(),
            ),
            SetOptions.merge(),
        ).await()
    }

    suspend fun setGoldenVerifiedBadge(userId: String, enabled: Boolean) {
        users.document(userId).set(
            mapOf(
                "goldenVerified" to enabled,
                "updatedAt" to System.currentTimeMillis(),
            ),
            SetOptions.merge(),
        ).await()
    }

    suspend fun scheduleTemporaryAccountDeletion(userId: String, deleteAfterAt: Long) {
        users.document(userId).set(
            mapOf(
                "temporaryAccount" to true,
                "deleteAfterAt" to deleteAfterAt,
                "online" to false,
                "appState" to "signed_out",
                "lastSeenAt" to System.currentTimeMillis(),
            ),
            SetOptions.merge(),
        ).await()
    }

    suspend fun touchPresence(userId: String) {
        users.document(userId).set(
            mapOf(
                "online" to true,
                "appState" to "foreground",
                "lastSeenAt" to System.currentTimeMillis(),
            ),
            SetOptions.merge(),
        ).await()
    }

    suspend fun getUserProfile(userId: String): FireUserProfile? {
        val snapshot = users.document(userId).get().await()
        if (!snapshot.exists()) return null
        return snapshot.toObject(FireUserProfile::class.java)?.copy(id = snapshot.id)
    }

    suspend fun findUserByPhone(phoneNumber: String): FireUserProfile? {
        val normalizedPhone = normalizePhone(phoneNumber)
        if (normalizedPhone.isBlank()) return null
        val snapshot = users
            .whereEqualTo("phoneNumber", normalizedPhone)
            .limit(1)
            .get()
            .await()
        val doc = snapshot.documents.firstOrNull() ?: return null
        return doc.toObject(FireUserProfile::class.java)?.copy(id = doc.id)
    }

    suspend fun findUserByUsername(username: String): FireUserProfile? {
        val normalizedUsername = normalizeUsername(username)
        if (normalizedUsername.isBlank()) return null
        val snapshot = users
            .whereEqualTo("username", normalizedUsername)
            .limit(1)
            .get()
            .await()
        val doc = snapshot.documents.firstOrNull() ?: return null
        return doc.toObject(FireUserProfile::class.java)?.copy(id = doc.id)
    }

    suspend fun isUsernameAvailable(username: String, excludingUserId: String = ""): Boolean {
        val normalizedUsername = normalizeUsername(username)
        if (normalizedUsername.isBlank()) return false
        val snapshot = users
            .whereEqualTo("username", normalizedUsername)
            .limit(2)
            .get()
            .await()
        return snapshot.documents.none { it.id != excludingUserId }
    }

    suspend fun searchUsers(query: String, currentUserId: String, limit: Int = 10): List<FireUserProfile> {
        val normalized = normalizeUsername(query)
        val displayQuery = query.trim().lowercase()
        val phone = normalizePhone(query)
        val results = mutableListOf<FireUserProfile>()
        if (normalized.isNotBlank()) {
            val usernameSnapshot = users
                .orderBy("username")
                .startAt(normalized)
                .endAt(normalized + "\uf8ff")
                .limit(limit.toLong())
                .get()
                .await()
            results += usernameSnapshot.documents.mapNotNull { it.toObject(FireUserProfile::class.java)?.copy(id = it.id) }

            val displaySnapshot = users
                .orderBy("displayName")
                .startAt(query.trim())
                .endAt(query.trim() + "\uf8ff")
                .limit(limit.toLong())
                .get()
                .await()
            results += displaySnapshot.documents.mapNotNull { it.toObject(FireUserProfile::class.java)?.copy(id = it.id) }
        }
        if (phone.isNotBlank()) {
            findUserByPhone(phone)?.let { results += it }
        }
        return results
            .filter { it.id != currentUserId }
            .distinctBy { it.id }
            .sortedWith(
                compareByDescending<FireUserProfile> { it.online }
                    .thenBy { if (displayQuery.isBlank()) 1 else it.displayName.lowercase().startsWith(displayQuery).not() }
                    .thenBy { if (normalized.isBlank()) 1 else it.username.lowercase().startsWith(normalized).not() }
                    .thenBy { it.displayName.ifBlank { it.username } }
            )
            .take(limit)
    }

    suspend fun findUserByIdentifier(identifier: String): FireUserProfile? {
        val clean = identifier.trim()
        if (clean.isBlank()) return null
        return if (clean.any { it.isDigit() } || clean.startsWith("+")) {
            findUserByPhone(clean)
        } else {
            findUserByUsername(clean.removePrefix("@"))
        }
    }

    suspend fun resolveProfilesByIdentifiers(identifiers: List<String>): List<FireUserProfile> {
        return identifiers
            .mapNotNull { identifier -> findUserByIdentifier(identifier) }
            .distinctBy { it.id }
    }

    suspend fun findOrCreatePrivateThread(currentUserId: String, otherUserId: String, otherName: String): String {
        val chatId = privateThreadId(currentUserId, otherUserId)
        val now = System.currentTimeMillis()
        val thread = FireChatThread(
            id = chatId,
            type = "private",
            title = otherName,
            createdBy = currentUserId,
            participantIds = listOf(currentUserId, otherUserId).sorted(),
            admins = listOf(currentUserId),
            updatedAt = now,
            lastMessageAt = now,
            unreadCounts = mapOf(currentUserId to 0L, otherUserId to 0L),
        )
        chats.document(chatId).set(thread, SetOptions.merge()).await()
        chats.document(chatId).set(
            mapOf(
                "hiddenFor" to FieldValue.arrayRemove(currentUserId),
                "updatedAt" to now,
            ),
            SetOptions.merge(),
        ).await()
        return chatId
    }

    suspend fun findOrCreatePrivateThreadWithIdentifier(
        currentUserId: String,
        otherIdentifier: String,
        fallbackName: String = "",
    ): Pair<String, FireUserProfile> {
        val profile = findUserByIdentifier(otherIdentifier)
            ?: throw IllegalStateException("No registered ChitChat user found for $otherIdentifier")
        val title = fallbackName.ifBlank {
            profile.displayName.ifBlank {
                profile.username.ifBlank { profile.phoneNumber.ifBlank { "Friend" } }
            }
        }
        val chatId = findOrCreatePrivateThread(
            currentUserId = currentUserId,
            otherUserId = profile.id,
            otherName = title,
        )
        return chatId to profile
    }

    suspend fun createGroupThread(title: String, creatorId: String, memberIds: List<String>): String {
        val doc = chats.document()
        val now = System.currentTimeMillis()
        val participants = (memberIds + creatorId).distinct()
        val thread = FireChatThread(
            id = doc.id,
            type = "group",
            title = title,
            createdBy = creatorId,
            participantIds = participants,
            admins = listOf(creatorId),
            inviteLink = "https://chitchat.app/invite/${doc.id}",
            updatedAt = now,
            lastMessageAt = now,
            unreadCounts = participants.associateWith { 0L },
        )
        doc.set(thread).await()
        return doc.id
    }

    suspend fun createGroupThreadWithIdentifiers(
        title: String,
        creatorId: String,
        memberIdentifiers: List<String>,
    ): Pair<String, List<FireUserProfile>> {
        val profiles = resolveProfilesByIdentifiers(memberIdentifiers).filterNot { it.id == creatorId }
        val groupId = createGroupThread(
            title = title,
            creatorId = creatorId,
            memberIds = profiles.map { it.id },
        )
        return groupId to profiles
    }

    suspend fun addGroupMembers(chatId: String, requesterId: String, memberIdentifiers: List<String>): List<FireUserProfile> {
        val snapshot = chats.document(chatId).get().await()
        val thread = snapshot.toObject(FireChatThread::class.java)
            ?: throw IllegalStateException("Group not found")
        if (thread.type != "group") throw IllegalStateException("Only groups support member updates")
        if (requesterId !in thread.admins) throw IllegalStateException("Only admins can add members")
        val resolved = resolveProfilesByIdentifiers(memberIdentifiers)
            .filterNot { it.id in thread.participantIds }
        if (resolved.isEmpty()) return emptyList()
        val participants = (thread.participantIds + resolved.map { it.id }).distinct()
        val unread = thread.unreadCounts.toMutableMap().apply { resolved.forEach { put(it.id, 0L) } }
        chats.document(chatId).set(
            mapOf(
                "participantIds" to participants,
                "unreadCounts" to unread,
                "updatedAt" to System.currentTimeMillis(),
            ),
            SetOptions.merge(),
        ).await()
        return resolved
    }

    suspend fun removeGroupMember(chatId: String, requesterId: String, memberId: String) {
        val snapshot = chats.document(chatId).get().await()
        val thread = snapshot.toObject(FireChatThread::class.java)
            ?: throw IllegalStateException("Group not found")
        if (thread.type != "group") throw IllegalStateException("Only groups support member updates")
        if (requesterId !in thread.admins && requesterId != memberId) throw IllegalStateException("Only admins can remove this member")
        val participants = thread.participantIds.filterNot { it == memberId }
        val admins = thread.admins.filterNot { it == memberId }.ifEmpty { listOf(requesterId) }
        val unread = thread.unreadCounts.filterKeys { it != memberId }
        chats.document(chatId).set(
            mapOf(
                "participantIds" to participants,
                "admins" to admins,
                "unreadCounts" to unread,
                "updatedAt" to System.currentTimeMillis(),
            ),
            SetOptions.merge(),
        ).await()
    }

    suspend fun sendGlobalTextMessage(
        senderId: String,
        senderName: String,
        text: String,
        replyToMessageId: String = "",
        replyToSnippet: String = "",
    ) {
        val now = System.currentTimeMillis()
        globalRoom.set(
            mapOf(
                "title" to "Global Chat",
                "updatedAt" to now,
                "lastMessageAt" to now,
            ),
            SetOptions.merge(),
        ).await()
        val doc = globalRoom.collection("messages").document()
        val message = FireMessage(
            id = doc.id,
            chatId = "global_chat",
            senderId = senderId,
            senderName = senderName,
            type = "text",
            text = text.trim(),
            replyToMessageId = replyToMessageId,
            replyToSnippet = replyToSnippet,
            deliveredTo = listOf(senderId),
            readBy = listOf(senderId),
            createdAt = now,
            expiresAt = now + GLOBAL_MESSAGE_TTL_MS,
        )
        doc.set(message).await()
        globalRoom.set(
            mapOf(
                "lastMessageText" to message.text,
                "lastMessageType" to message.type,
                "lastMessageAt" to now,
                "updatedAt" to now,
            ),
            SetOptions.merge(),
        ).await()
    }

    suspend fun editGlobalMessage(messageId: String, editorId: String, newText: String) {
        val ref = globalRoom.collection("messages").document(messageId)
        val snapshot = ref.get().await()
        val message = snapshot.toObject(FireMessage::class.java)
            ?: throw IllegalStateException("Message not found")
        if (message.senderId != editorId) throw IllegalStateException("Only sender can edit the message")
        ref.update(
            mapOf(
                "text" to newText.trim(),
                "editedAt" to System.currentTimeMillis(),
            )
        ).await()
        globalRoom.set(
            mapOf(
                "lastMessageText" to newText.trim(),
                "lastMessageType" to message.type,
                "lastMessageAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis(),
            ),
            SetOptions.merge(),
        ).await()
    }

    suspend fun deleteGlobalMessageForEveryone(messageId: String, requesterId: String) {
        val ref = globalRoom.collection("messages").document(messageId)
        val snapshot = ref.get().await()
        val message = snapshot.toObject(FireMessage::class.java)
            ?: throw IllegalStateException("Message not found")
        if (message.senderId != requesterId) throw IllegalStateException("Only sender can delete the message for everyone")
        ref.update(
            mapOf(
                "text" to "This message was deleted",
                "deletedForEveryone" to true,
                "editedAt" to System.currentTimeMillis(),
                "mediaUrl" to "",
                "thumbnailUrl" to "",
            )
        ).await()
        globalRoom.set(
            mapOf(
                "lastMessageText" to "This message was deleted",
                "lastMessageType" to "system",
                "lastMessageAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis(),
            ),
            SetOptions.merge(),
        ).await()
    }

    suspend fun forwardGlobalMessage(senderId: String, senderName: String, source: FireMessage) {
        val now = System.currentTimeMillis()
        val doc = globalRoom.collection("messages").document()
        val forwarded = source.text.ifBlank { source.fileName.ifBlank { "Forwarded message" } }
        val message = source.copy(
            id = doc.id,
            chatId = "global_chat",
            senderId = senderId,
            senderName = senderName,
            deliveredTo = listOf(senderId),
            readBy = listOf(senderId),
            forwardedFromName = source.senderName,
            createdAt = now,
            editedAt = 0L,
            expiresAt = now + GLOBAL_MESSAGE_TTL_MS,
        )
        doc.set(message).await()
        globalRoom.set(
            mapOf(
                "lastMessageText" to forwarded,
                "lastMessageType" to source.type,
                "lastMessageAt" to now,
                "updatedAt" to now,
            ),
            SetOptions.merge(),
        ).await()
    }

    suspend fun setGlobalMessageStarred(messageId: String, userId: String, starred: Boolean) {
        globalRoom
            .collection("messages")
            .document(messageId)
            .update(
                "starredBy",
                if (starred) FieldValue.arrayUnion(userId) else FieldValue.arrayRemove(userId)
            )
            .await()
    }

    suspend fun markGlobalDelivered(messageId: String, userId: String) {
        globalRoom
            .collection("messages")
            .document(messageId)
            .update("deliveredTo", FieldValue.arrayUnion(userId))
            .await()
    }

    suspend fun markGlobalRead(messageId: String, userId: String) {
        globalRoom
            .collection("messages")
            .document(messageId)
            .update("readBy", FieldValue.arrayUnion(userId))
            .await()
    }

    suspend fun sendTextMessage(
        chatId: String,
        senderId: String,
        senderName: String,
        text: String,
        replyToMessageId: String = "",
        replyToSnippet: String = "",
    ) {
        ensureCanSend(chatId, senderId)
        val now = System.currentTimeMillis()
        val doc = chats.document(chatId).collection("messages").document()
        val message = FireMessage(
            id = doc.id,
            chatId = chatId,
            senderId = senderId,
            senderName = senderName,
            type = "text",
            text = text.trim(),
            replyToMessageId = replyToMessageId,
            replyToSnippet = replyToSnippet,
            deliveredTo = listOf(senderId),
            readBy = listOf(senderId),
            createdAt = now,
        )
        doc.set(message).await()
        updateThreadSummary(chatId, message.text, message.type, now, senderId)
        setTyping(chatId, senderId, senderName, false)
    }

    suspend fun sendMediaMessage(
        chatId: String,
        senderId: String,
        senderName: String,
        contentType: String,
        upload: StorageUploadResult,
        replyToMessageId: String = "",
        replyToSnippet: String = "",
    ) {
        ensureCanSend(chatId, senderId)
        val now = System.currentTimeMillis()
        val doc = chats.document(chatId).collection("messages").document()
        val message = FireMessage(
            id = doc.id,
            chatId = chatId,
            senderId = senderId,
            senderName = senderName,
            type = contentType,
            text = upload.fileName,
            mediaUrl = upload.downloadUrl,
            mimeType = upload.mimeType,
            fileName = upload.fileName,
            fileSizeBytes = upload.sizeBytes,
            durationMs = upload.durationMs,
            replyToMessageId = replyToMessageId,
            replyToSnippet = replyToSnippet,
            deliveredTo = listOf(senderId),
            readBy = listOf(senderId),
            createdAt = now,
        )
        doc.set(message).await()
        updateThreadSummary(chatId, upload.fileName, contentType, now, senderId)
        setTyping(chatId, senderId, senderName, false)
    }

    suspend fun setTyping(chatId: String, userId: String, displayName: String, isTyping: Boolean) {
        val threadRef = chats.document(chatId)
        if (isTyping) {
            threadRef.update(mapOf("typingUsers.$userId" to displayName, "updatedAt" to System.currentTimeMillis())).await()
        } else {
            threadRef.update(mapOf("typingUsers.$userId" to FieldValue.delete(), "updatedAt" to System.currentTimeMillis())).await()
        }
    }

    suspend fun setMuted(chatId: String, userId: String, muted: Boolean) {
        chats.document(chatId).set(
            mapOf("mutedFor" to if (muted) FieldValue.arrayUnion(userId) else FieldValue.arrayRemove(userId), "updatedAt" to System.currentTimeMillis()),
            SetOptions.merge(),
        ).await()
    }

    suspend fun setArchived(chatId: String, userId: String, archived: Boolean) {
        chats.document(chatId).set(
            mapOf("archivedFor" to if (archived) FieldValue.arrayUnion(userId) else FieldValue.arrayRemove(userId), "updatedAt" to System.currentTimeMillis()),
            SetOptions.merge(),
        ).await()
    }

    suspend fun setPinned(chatId: String, userId: String, pinned: Boolean) {
        chats.document(chatId).set(
            mapOf("pinnedFor" to if (pinned) FieldValue.arrayUnion(userId) else FieldValue.arrayRemove(userId), "updatedAt" to System.currentTimeMillis()),
            SetOptions.merge(),
        ).await()
    }

    suspend fun setHidden(chatId: String, userId: String, hidden: Boolean) {
        chats.document(chatId).set(
            mapOf("hiddenFor" to if (hidden) FieldValue.arrayUnion(userId) else FieldValue.arrayRemove(userId), "updatedAt" to System.currentTimeMillis()),
            SetOptions.merge(),
        ).await()
    }

    suspend fun markDelivered(chatId: String, messageId: String, userId: String) {
        chats.document(chatId)
            .collection("messages")
            .document(messageId)
            .update("deliveredTo", FieldValue.arrayUnion(userId))
            .await()
    }

    suspend fun markRead(chatId: String, messageId: String, userId: String) {
        chats.document(chatId)
            .collection("messages")
            .document(messageId)
            .update("readBy", FieldValue.arrayUnion(userId))
            .await()
        clearUnreadCount(chatId, userId)
    }

    suspend fun clearUnreadCount(chatId: String, userId: String) {
        val threadRef = chats.document(chatId)
        val snapshot = threadRef.get().await()
        val thread = snapshot.toObject(FireChatThread::class.java)
        val unreadCounts = thread?.unreadCounts?.toMutableMap() ?: mutableMapOf()
        unreadCounts[userId] = 0L
        threadRef.set(
            mapOf(
                "unreadCounts" to unreadCounts,
                "updatedAt" to System.currentTimeMillis(),
            ),
            SetOptions.merge(),
        ).await()
    }



    suspend fun editMessage(chatId: String, messageId: String, editorId: String, newText: String) {
        val ref = chats.document(chatId).collection("messages").document(messageId)
        val snapshot = ref.get().await()
        val message = snapshot.toObject(FireMessage::class.java)
            ?: throw IllegalStateException("Message not found")
        if (message.senderId != editorId) throw IllegalStateException("Only sender can edit the message")
        ref.update(
            mapOf(
                "text" to newText.trim(),
                "editedAt" to System.currentTimeMillis(),
            )
        ).await()
        updateThreadSummary(chatId, newText.trim(), message.type, System.currentTimeMillis(), editorId)
    }

    suspend fun deleteMessageForEveryone(chatId: String, messageId: String, requesterId: String) {
        val ref = chats.document(chatId).collection("messages").document(messageId)
        val snapshot = ref.get().await()
        val message = snapshot.toObject(FireMessage::class.java)
            ?: throw IllegalStateException("Message not found")
        if (message.senderId != requesterId) throw IllegalStateException("Only sender can delete the message for everyone")
        ref.update(
            mapOf(
                "text" to "This message was deleted",
                "deletedForEveryone" to true,
                "editedAt" to System.currentTimeMillis(),
                "mediaUrl" to "",
                "thumbnailUrl" to "",
            )
        ).await()
        updateThreadSummary(chatId, "This message was deleted", "system", System.currentTimeMillis(), requesterId)
    }

    suspend fun forwardMessage(chatId: String, senderId: String, senderName: String, source: FireMessage) {
        val now = System.currentTimeMillis()
        val doc = chats.document(chatId).collection("messages").document()
        val forwarded = source.text.ifBlank { source.fileName.ifBlank { "Forwarded message" } }
        val message = source.copy(
            id = doc.id,
            chatId = chatId,
            senderId = senderId,
            senderName = senderName,
            deliveredTo = listOf(senderId),
            readBy = listOf(senderId),
            forwardedFromName = source.senderName,
            createdAt = now,
            editedAt = 0L,
        )
        doc.set(message).await()
        updateThreadSummary(chatId, forwarded, source.type, now, senderId)
    }

    suspend fun setMessageStarred(chatId: String, messageId: String, userId: String, starred: Boolean) {
        chats.document(chatId)
            .collection("messages")
            .document(messageId)
            .update(
                "starredBy",
                if (starred) FieldValue.arrayUnion(userId) else FieldValue.arrayRemove(userId)
            )
            .await()
    }

    suspend fun updateGroupInfo(chatId: String, requesterId: String, title: String, description: String) {
        val snapshot = chats.document(chatId).get().await()
        val thread = snapshot.toObject(FireChatThread::class.java)
            ?: throw IllegalStateException("Group not found")
        if (thread.type != "group") throw IllegalStateException("Only groups can be updated")
        if (requesterId !in thread.admins) throw IllegalStateException("Only admins can update the group")
        chats.document(chatId).set(
            mapOf(
                "title" to title,
                "description" to description,
                "updatedAt" to System.currentTimeMillis(),
            ),
            SetOptions.merge(),
        ).await()
    }

    suspend fun promoteGroupAdmin(chatId: String, requesterId: String, memberId: String) {
        val snapshot = chats.document(chatId).get().await()
        val thread = snapshot.toObject(FireChatThread::class.java)
            ?: throw IllegalStateException("Group not found")
        if (requesterId !in thread.admins) throw IllegalStateException("Only admins can promote members")
        if (memberId !in thread.participantIds) throw IllegalStateException("Member is not in the group")
        chats.document(chatId).set(
            mapOf(
                "admins" to (thread.admins + memberId).distinct(),
                "updatedAt" to System.currentTimeMillis(),
            ),
            SetOptions.merge(),
        ).await()
    }

    suspend fun setAdminOnlySendMode(chatId: String, requesterId: String, enabled: Boolean) {
        val snapshot = chats.document(chatId).get().await()
        val thread = snapshot.toObject(FireChatThread::class.java)
            ?: throw IllegalStateException("Group not found")
        if (requesterId !in thread.admins) throw IllegalStateException("Only admins can change group permissions")
        chats.document(chatId).set(
            mapOf(
                "adminOnlySendMode" to enabled,
                "updatedAt" to System.currentTimeMillis(),
            ),
            SetOptions.merge(),
        ).await()
    }

    suspend fun requestToJoinGroup(chatId: String, requesterId: String) {
        chats.document(chatId).set(
            mapOf(
                "joinRequests" to FieldValue.arrayUnion(requesterId),
                "updatedAt" to System.currentTimeMillis(),
            ),
            SetOptions.merge(),
        ).await()
    }

    suspend fun publishStatus(story: FireStatusStory) {
        val docId = story.id.ifBlank { statuses.document().id }
        statuses.document(docId).set(story.copy(id = docId), SetOptions.merge()).await()
    }

    suspend fun deleteUserProfile(userId: String) {
        if (userId.isBlank()) return
        requireDb().runTransaction { transaction ->
            val userRef = users.document(userId)
            val userSnapshot = transaction.get(userRef)
            val username = normalizeUsername(userSnapshot.getString("username").orEmpty())
            if (username.isNotBlank()) {
                val usernameRef = usernames.document(username)
                val usernameSnapshot = transaction.get(usernameRef)
                if (usernameSnapshot.getString("userId").orEmpty() == userId) {
                    transaction.delete(usernameRef)
                }
            }
            transaction.delete(userRef)
            null
        }.await()
    }

    suspend fun deleteStatusesByAuthor(authorId: String) {
        if (authorId.isBlank()) return
        val snapshot = statuses.whereEqualTo("authorId", authorId).get().await()
        snapshot.documents.forEach { it.reference.delete().await() }
    }

    suspend fun markStatusViewed(statusId: String, viewerId: String) {
        statuses.document(statusId).set(
            mapOf("viewerIds" to FieldValue.arrayUnion(viewerId)),
            SetOptions.merge(),
        ).await()
    }

    fun observeStatuses(currentUserId: String, contactIds: List<String>): Flow<List<FireStatusStory>> = callbackFlow {
        if (contactIds.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val registration = statuses
            .whereIn("authorId", contactIds.take(10))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val now = System.currentTimeMillis()
                val stories = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    doc.toObject(FireStatusStory::class.java)?.copy(id = doc.id)
                }.filter { story ->
                    val allowed = story.allowedViewerIds
                    val canView = when (story.visibilityMode.lowercase()) {
                        "selected" -> currentUserId in allowed || story.authorId == currentUserId
                        "contacts" -> allowed.isEmpty() || currentUserId in allowed || story.authorId == currentUserId
                        else -> true
                    }
                    story.expiresAt > now && canView
                }.sortedByDescending { it.expiresAt }
                trySend(stories)
            }
        awaitClose { registration.remove() }
    }

    private fun normalizeUsername(raw: String): String {
        return raw
            .trim()
            .removePrefix("@")
            .lowercase()
            .filter { it.isLetterOrDigit() || it == '_' || it == '.' }
    }

    private suspend fun ensureCanSend(chatId: String, senderId: String) {
        val snapshot = chats.document(chatId).get().await()
        val thread = snapshot.toObject(FireChatThread::class.java) ?: return
        if (thread.type == "group" && thread.adminOnlySendMode && senderId !in thread.admins) {
            throw IllegalStateException("Only admins can send messages in this group right now")
        }
    }

    private suspend fun updateThreadSummary(chatId: String, preview: String, type: String, now: Long, senderId: String) {
        val threadRef = chats.document(chatId)
        val snapshot = threadRef.get().await()
        val thread = snapshot.toObject(FireChatThread::class.java)
        val unreadCounts = thread?.unreadCounts?.toMutableMap() ?: mutableMapOf()
        thread?.participantIds.orEmpty().forEach { participantId ->
            unreadCounts[participantId] = when {
                participantId == senderId -> 0L
                else -> (unreadCounts[participantId] ?: 0L) + 1L
            }
        }
        threadRef.set(
            mapOf(
                "lastMessageText" to preview,
                "lastMessageType" to type,
                "lastMessageAt" to now,
                "updatedAt" to now,
                "unreadCounts" to unreadCounts,
            ),
            SetOptions.merge(),
        ).await()
    }

    private fun privateThreadId(a: String, b: String): String = listOf(a, b).sorted().joinToString(separator = "__")

    private fun normalizePhone(raw: String): String = raw.filter { it.isDigit() || it == '+' }
}
