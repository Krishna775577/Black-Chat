package com.chitchat.app.data.model

data class FireUserProfile(
    val id: String = "",
    val phoneNumber: String = "",
    val displayName: String = "",
    val username: String = "",
    val about: String = "",
    val photoUrl: String = "",
    val bannerUrl: String = "",
    val verified: Boolean = false,
    val goldenVerified: Boolean = false,
    val temporaryAuthEmail: String = "",
    val online: Boolean = false,
    val lastSeenAt: Long = 0L,
    val linkedDeviceCount: Int = 0,
    val notificationToken: String = "",
    val appState: String = "offline",
    val temporaryAccount: Boolean = false,
    val deleteAfterAt: Long = 0L,
    val readReceiptsEnabled: Boolean = true,
    val showLastSeenEnabled: Boolean = true,
    val showOnlinePresenceEnabled: Boolean = true,
)

data class FireChatThread(
    val id: String = "",
    val type: String = "private",
    val title: String = "",
    val description: String = "",
    val photoUrl: String = "",
    val createdBy: String = "",
    val participantIds: List<String> = emptyList(),
    val admins: List<String> = emptyList(),
    val inviteLink: String = "",
    val joinRequests: List<String> = emptyList(),
    val adminOnlySendMode: Boolean = false,
    val lastMessageText: String = "",
    val lastMessageType: String = "text",
    val lastMessageAt: Long = 0L,
    val updatedAt: Long = 0L,
    val archivedFor: List<String> = emptyList(),
    val mutedFor: List<String> = emptyList(),
    val pinnedFor: List<String> = emptyList(),
    val hiddenFor: List<String> = emptyList(),
    val unreadCounts: Map<String, Long> = emptyMap(),
    val typingUsers: Map<String, String> = emptyMap(),
)

data class FireChatParticipant(
    val userId: String = "",
    val role: String = "member",
    val unreadCount: Int = 0,
    val pinned: Boolean = false,
    val joinedAt: Long = 0L,
    val lastReadAt: Long = 0L,
)

data class FireMessage(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val type: String = "text",
    val text: String = "",
    val mediaUrl: String = "",
    val thumbnailUrl: String = "",
    val mimeType: String = "",
    val fileName: String = "",
    val fileSizeBytes: Long = 0L,
    val durationMs: Long = 0L,
    val replyToMessageId: String = "",
    val replyToSnippet: String = "",
    val deliveredTo: List<String> = emptyList(),
    val readBy: List<String> = emptyList(),
    val starredBy: List<String> = emptyList(),
    val deletedForEveryone: Boolean = false,
    val forwardedFromName: String = "",
    val reactions: Map<String, List<String>> = emptyMap(),
    val createdAt: Long = 0L,
    val editedAt: Long = 0L,
    val expiresAt: Long = 0L,
)

data class FireStatusStory(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val contentType: String = "text",
    val caption: String = "",
    val mediaUrl: String = "",
    val createdAt: Long = 0L,
    val expiresAt: Long = 0L,
    val viewerIds: List<String> = emptyList(),
    val visibilityMode: String = "contacts",
    val allowedViewerIds: List<String> = emptyList(),
)

data class FireCallRoom(
    val id: String = "",
    val callerId: String = "",
    val calleeIds: List<String> = emptyList(),
    val isVideo: Boolean = false,
    val state: String = "ringing",
    val offerSdp: String = "",
    val answerSdp: String = "",
    val startedAt: Long = 0L,
    val updatedAt: Long = 0L,
)

data class FireDeviceToken(
    val userId: String = "",
    val token: String = "",
    val platform: String = "android",
    val updatedAt: Long = 0L,
)

data class StorageUploadResult(
    val downloadUrl: String,
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val durationMs: Long = 0L,
)


data class StorageUploadProgress(
    val progressPercent: Int = 0,
    val stage: String = "preparing",
    val result: StorageUploadResult? = null,
)
