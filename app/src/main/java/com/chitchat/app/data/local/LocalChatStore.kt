package com.chitchat.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.chitchat.app.data.model.ChatMessageUi
import com.chitchat.app.data.model.CallRecordUi
import com.chitchat.app.data.model.CallType
import com.chitchat.app.data.model.CallDirection
import com.chitchat.app.data.model.ChatPreferencesUi
import com.chitchat.app.data.model.ConversationType
import com.chitchat.app.data.model.ConversationUi
import com.chitchat.app.data.model.MessageContentType
import com.chitchat.app.data.model.MessageDeliveryStatus
import com.chitchat.app.data.model.NotificationPreferencesUi
import com.chitchat.app.data.model.SecurityPreferencesUi
import org.json.JSONArray
import org.json.JSONObject

object LocalChatStore {
    private const val PREFS_NAME = "chitchat_local_cache"
    private const val KEY_DRAFTS = "drafts"
    private const val KEY_PENDING = "pending"
    private const val KEY_CONVERSATIONS = "conversations"
    private const val KEY_MESSAGES_PREFIX = "messages_"
    private const val KEY_NOTIFICATION_PREFS = "notification_prefs"
    private const val KEY_SECURITY_PREFS = "security_prefs"
    private const val KEY_CHAT_PREFS = "chat_prefs"
    private const val KEY_HIDDEN_CHATS_PREFIX = "hidden_chats_"
    private const val KEY_CALL_LOGS = "call_logs"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveDraft(chatId: String, draft: String) {
        val drafts = getJsonObject(KEY_DRAFTS)
        if (draft.isBlank()) drafts.remove(chatId) else drafts.put(chatId, draft)
        putJsonObject(KEY_DRAFTS, drafts)
    }

    fun loadDraft(chatId: String): String = getJsonObject(KEY_DRAFTS).optString(chatId, "")

    fun saveNotificationPreferences(value: NotificationPreferencesUi) {
        putJsonObject(
            KEY_NOTIFICATION_PREFS,
            JSONObject()
                .put("pushNotifications", value.pushNotifications)
                .put("groupNotifications", value.groupNotifications)
                .put("callAlerts", value.callAlerts)
                .put("previews", value.previews)
                .put("vibration", value.vibration)
                .put("quietHoursEnabled", value.quietHoursEnabled)
                .put("groupedNotifications", value.groupedNotifications)
                .put("inlineReply", value.inlineReply)
                .put("markReadAction", value.markReadAction)
                .put("customToneLabel", value.customToneLabel)
        )
    }

    fun loadNotificationPreferences(): NotificationPreferencesUi {
        val item = getJsonObject(KEY_NOTIFICATION_PREFS)
        return NotificationPreferencesUi(
            pushNotifications = item.optBoolean("pushNotifications", true),
            groupNotifications = item.optBoolean("groupNotifications", true),
            callAlerts = item.optBoolean("callAlerts", true),
            previews = item.optBoolean("previews", true),
            vibration = item.optBoolean("vibration", true),
            quietHoursEnabled = item.optBoolean("quietHoursEnabled", false),
            groupedNotifications = item.optBoolean("groupedNotifications", true),
            inlineReply = item.optBoolean("inlineReply", true),
            markReadAction = item.optBoolean("markReadAction", true),
            customToneLabel = item.optString("customToneLabel", "Default"),
        )
    }

    fun saveSecurityPreferences(value: SecurityPreferencesUi) {
        putJsonObject(
            KEY_SECURITY_PREFS,
            JSONObject()
                .put("appLock", value.appLock)
                .put("fingerprintUnlock", value.fingerprintUnlock)
                .put("screenshotBlock", value.screenshotBlock)
                .put("readReceipts", value.readReceipts)
                .put("endToEndEncryption", value.endToEndEncryption)
                .put("disappearingMessages", value.disappearingMessages)
                .put("showLastSeen", value.showLastSeen)
                .put("showOnlinePresence", value.showOnlinePresence)
                .put("profilePhotoPublic", value.profilePhotoPublic)
                .put("blurInRecents", value.blurInRecents)
                .put("chatLock", value.chatLock)
                .put("encryptedLocalCache", value.encryptedLocalCache)
        )
    }

    fun loadSecurityPreferences(): SecurityPreferencesUi {
        val item = getJsonObject(KEY_SECURITY_PREFS)
        return SecurityPreferencesUi(
            appLock = item.optBoolean("appLock", false),
            fingerprintUnlock = item.optBoolean("fingerprintUnlock", false),
            screenshotBlock = item.optBoolean("screenshotBlock", false),
            readReceipts = item.optBoolean("readReceipts", true),
            endToEndEncryption = item.optBoolean("endToEndEncryption", true),
            disappearingMessages = item.optBoolean("disappearingMessages", false),
            showLastSeen = item.optBoolean("showLastSeen", true),
            showOnlinePresence = item.optBoolean("showOnlinePresence", true),
            profilePhotoPublic = item.optBoolean("profilePhotoPublic", true),
            blurInRecents = item.optBoolean("blurInRecents", true),
            chatLock = item.optBoolean("chatLock", false),
            encryptedLocalCache = item.optBoolean("encryptedLocalCache", true),
        )
    }

    fun saveChatPreferences(value: ChatPreferencesUi) {
        putJsonObject(
            KEY_CHAT_PREFS,
            JSONObject()
                .put("darkMode", value.darkMode)
                .put("mediaAutoDownload", value.mediaAutoDownload)
                .put("enterToSend", value.enterToSend)
                .put("highQualityUploads", value.highQualityUploads)
                .put("translateIncoming", value.translateIncoming)
                .put("compactMode", value.compactMode)
                .put("chatThemeKey", value.chatThemeKey)
                .put("customThemeImageUri", value.customThemeImageUri)
                .put("profileBackgroundKey", value.profileBackgroundKey)
                .put("profileTopColorHex", value.profileTopColorHex)
                .put("profileBottomColorHex", value.profileBottomColorHex)
        )
    }

    fun loadChatPreferences(): ChatPreferencesUi {
        val item = getJsonObject(KEY_CHAT_PREFS)
        return ChatPreferencesUi(
            darkMode = item.optBoolean("darkMode", true),
            mediaAutoDownload = item.optBoolean("mediaAutoDownload", true),
            enterToSend = item.optBoolean("enterToSend", false),
            highQualityUploads = item.optBoolean("highQualityUploads", true),
            translateIncoming = item.optBoolean("translateIncoming", false),
            compactMode = item.optBoolean("compactMode", false),
            chatThemeKey = item.optString("chatThemeKey", "EMERALD"),
            customThemeImageUri = item.optString("customThemeImageUri", ""),
            profileBackgroundKey = item.optString("profileBackgroundKey", "CRIMSON"),
            profileTopColorHex = item.optString("profileTopColorHex", "#C83B4B"),
            profileBottomColorHex = item.optString("profileBottomColorHex", "#25070D"),
        )
    }


    fun saveCallLogs(logs: List<CallRecordUi>) {
        val array = JSONArray()
        logs.forEach { log ->
            array.put(
                JSONObject()
                    .put("id", log.id)
                    .put("contactName", log.contactName)
                    .put("avatarSeed", log.avatarSeed)
                    .put("type", log.type.name)
                    .put("direction", log.direction.name)
                    .put("timestamp", log.timestamp)
                    .put("durationLabel", log.durationLabel)
                    .put("photoUrl", log.photoUrl)
            )
        }
        prefs.edit().putString(KEY_CALL_LOGS, array.toString()).apply()
    }

    fun loadCallLogs(): List<CallRecordUi> {
        val source = prefs.getString(KEY_CALL_LOGS, null) ?: return emptyList()
        val array = runCatching { JSONArray(source) }.getOrNull() ?: return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                add(
                    CallRecordUi(
                        id = item.optString("id"),
                        contactName = item.optString("contactName"),
                        avatarSeed = item.optString("avatarSeed"),
                        type = runCatching { CallType.valueOf(item.optString("type")) }.getOrDefault(CallType.AUDIO),
                        direction = runCatching { CallDirection.valueOf(item.optString("direction")) }.getOrDefault(CallDirection.OUTGOING),
                        timestamp = item.optString("timestamp"),
                        durationLabel = item.optString("durationLabel"),
                        photoUrl = item.optString("photoUrl"),
                    )
                )
            }
        }
    }


    fun saveHiddenConversationIds(userId: String, chatIds: Set<String>) {
        val cleanUserId = userId.trim()
        if (cleanUserId.isBlank()) return
        val array = JSONArray()
        chatIds.filter { it.isNotBlank() }.sorted().forEach(array::put)
        prefs.edit().putString("$KEY_HIDDEN_CHATS_PREFIX$cleanUserId", array.toString()).apply()
    }

    fun loadHiddenConversationIds(userId: String): Set<String> {
        val cleanUserId = userId.trim()
        if (cleanUserId.isBlank()) return emptySet()
        val source = prefs.getString("$KEY_HIDDEN_CHATS_PREFIX$cleanUserId", null) ?: return emptySet()
        val array = runCatching { JSONArray(source) }.getOrNull() ?: return emptySet()
        return buildSet {
            for (index in 0 until array.length()) {
                val value = array.optString(index).trim()
                if (value.isNotBlank()) add(value)
            }
        }
    }

    fun cacheConversations(conversations: List<ConversationUi>) {
        val array = JSONArray()
        conversations.forEach { conversation ->
            array.put(
                JSONObject()
                    .put("id", conversation.id)
                    .put("title", conversation.title)
                    .put("subtitle", conversation.subtitle)
                    .put("type", conversation.type.name)
                    .put("avatarSeed", conversation.avatarSeed)
                    .put("participantIds", JSONArray(conversation.participantIds))
                    .put("unreadCount", conversation.unreadCount)
                    .put("pinned", conversation.pinned)
                    .put("archived", conversation.archived)
                    .put("muted", conversation.muted)
                    .put("verified", conversation.verified)
                    .put("goldenVerified", conversation.goldenVerified)
                    .put("lastActive", conversation.lastActive)
                    .put("photoUrl", conversation.photoUrl)
            )
        }
        prefs.edit().putString(KEY_CONVERSATIONS, array.toString()).apply()
    }

    fun loadConversations(): List<ConversationUi> {
        val source = prefs.getString(KEY_CONVERSATIONS, null) ?: return emptyList()
        val array = runCatching { JSONArray(source) }.getOrNull() ?: return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                add(
                    ConversationUi(
                        id = item.optString("id"),
                        title = item.optString("title"),
                        subtitle = item.optString("subtitle"),
                        type = runCatching { ConversationType.valueOf(item.optString("type")) }.getOrDefault(ConversationType.PRIVATE),
                        avatarSeed = item.optString("avatarSeed"),
                        participantIds = item.optJSONArray("participantIds")?.let(::stringList) ?: emptyList(),
                        unreadCount = item.optInt("unreadCount"),
                        pinned = item.optBoolean("pinned"),
                        archived = item.optBoolean("archived"),
                        muted = item.optBoolean("muted"),
                        verified = item.optBoolean("verified"),
                        goldenVerified = item.optBoolean("goldenVerified"),
                        lastActive = item.optString("lastActive"),
                        photoUrl = item.optString("photoUrl"),
                    )
                )
            }
        }
    }

    fun cacheMessages(chatId: String, messages: List<ChatMessageUi>) {
        val array = JSONArray()
        messages.forEach { message ->
            array.put(
                JSONObject()
                    .put("id", message.id)
                    .put("senderId", message.senderId)
                    .put("senderName", message.senderName)
                    .put("contentType", message.contentType.name)
                    .put("body", message.body)
                    .put("timestamp", message.timestamp)
                    .put("deliveryStatus", message.deliveryStatus.name)
                    .put("metadata", message.metadata)
                    .put("replyToSnippet", message.replyToSnippet)
                    .put("reactions", JSONArray(message.reactions))
                    .put("starred", message.starred)
                    .put("mediaUrl", message.mediaUrl)
            )
        }
        prefs.edit().putString("$KEY_MESSAGES_PREFIX$chatId", array.toString()).apply()
    }

    fun loadMessages(chatId: String): List<ChatMessageUi> {
        val source = prefs.getString("$KEY_MESSAGES_PREFIX$chatId", null) ?: return emptyList()
        val array = runCatching { JSONArray(source) }.getOrNull() ?: return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                add(
                    ChatMessageUi(
                        id = item.optString("id"),
                        senderId = item.optString("senderId"),
                        senderName = item.optString("senderName"),
                        contentType = runCatching { MessageContentType.valueOf(item.optString("contentType")) }.getOrDefault(MessageContentType.TEXT),
                        body = item.optString("body"),
                        timestamp = item.optString("timestamp"),
                        deliveryStatus = runCatching { MessageDeliveryStatus.valueOf(item.optString("deliveryStatus")) }.getOrDefault(MessageDeliveryStatus.SENT),
                        metadata = item.optString("metadata"),
                        replyToSnippet = item.optString("replyToSnippet"),
                        reactions = item.optJSONArray("reactions")?.let(::stringList) ?: emptyList(),
                        starred = item.optBoolean("starred"),
                        mediaUrl = item.optString("mediaUrl"),
                    )
                )
            }
        }
    }

    fun loadAllCachedMessages(chatIds: List<String>): Map<String, List<ChatMessageUi>> {
        return chatIds.associateWith { loadMessages(it) }.filterValues { it.isNotEmpty() }
    }

    fun upsertPending(message: PendingMessageCache) {
        val all = loadPending().toMutableList()
        val index = all.indexOfFirst { it.localId == message.localId }
        if (index >= 0) all[index] = message else all.add(message)
        savePending(all)
    }

    fun removePending(localId: String) {
        savePending(loadPending().filterNot { it.localId == localId })
    }

    fun loadPending(): List<PendingMessageCache> {
        val source = prefs.getString(KEY_PENDING, null) ?: return emptyList()
        val array = runCatching { JSONArray(source) }.getOrNull() ?: return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                add(
                    PendingMessageCache(
                        localId = item.optString("localId"),
                        chatId = item.optString("chatId"),
                        contentType = runCatching { MessageContentType.valueOf(item.optString("contentType")) }.getOrDefault(MessageContentType.TEXT),
                        body = item.optString("body"),
                        metadata = item.optString("metadata"),
                        replyToSnippet = item.optString("replyToSnippet"),
                        durationMs = item.optLong("durationMs"),
                        createdAt = item.optLong("createdAt"),
                    )
                )
            }
        }
    }

    fun cleanupCache(maxPendingAgeMs: Long = 48L * 60L * 60L * 1000L) {
        val now = System.currentTimeMillis()
        val cleanedPending = loadPending().filter { now - it.createdAt <= maxPendingAgeMs }
        savePending(cleanedPending)
    }

    fun clearConversationCache(chatId: String) {
        saveDraft(chatId, "")
        prefs.edit().remove("$KEY_MESSAGES_PREFIX$chatId").apply()
    }

    private fun savePending(messages: List<PendingMessageCache>) {
        val array = JSONArray()
        messages.forEach { message ->
            array.put(
                JSONObject()
                    .put("localId", message.localId)
                    .put("chatId", message.chatId)
                    .put("contentType", message.contentType.name)
                    .put("body", message.body)
                    .put("metadata", message.metadata)
                    .put("replyToSnippet", message.replyToSnippet)
                    .put("durationMs", message.durationMs)
                    .put("createdAt", message.createdAt)
            )
        }
        prefs.edit().putString(KEY_PENDING, array.toString()).apply()
    }

    private fun getJsonObject(key: String): JSONObject {
        val source = prefs.getString(key, null)
        return if (source.isNullOrBlank()) JSONObject() else runCatching { JSONObject(source) }.getOrDefault(JSONObject())
    }

    private fun putJsonObject(key: String, value: JSONObject) {
        prefs.edit().putString(key, value.toString()).apply()
    }

    private fun stringList(array: JSONArray): List<String> = buildList {
        for (index in 0 until array.length()) {
            add(array.optString(index))
        }
    }
}

data class PendingMessageCache(
    val localId: String,
    val chatId: String,
    val contentType: MessageContentType,
    val body: String,
    val metadata: String = "",
    val replyToSnippet: String = "",
    val durationMs: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
)
