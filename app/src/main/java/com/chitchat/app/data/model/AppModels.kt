package com.chitchat.app.data.model

enum class HomeTab { CHATS, UPDATES, CALLS, SETTINGS }

enum class LoginFlowStep { CHOICE, EMAIL_LOGIN, ANONYMOUS_USERNAME, TEMPORARY_LOGIN }

enum class ChatFilter { ALL, UNREAD, GROUPS, ARCHIVED, STARRED }

enum class ConversationType { PRIVATE, GROUP }

enum class MessageContentType { TEXT, IMAGE, VIDEO, DOCUMENT, VOICE, LOCATION, CONTACT, POLL, SYSTEM }

enum class MessageDeliveryStatus { QUEUED, SENT, DELIVERED, READ, FAILED }

enum class CallType { AUDIO, VIDEO }

enum class CallDirection { INCOMING, OUTGOING, MISSED }

data class AppUser(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val about: String,
    val avatarSeed: String,
    val online: Boolean,
    val lastSeen: String,
    val verified: Boolean = false,
    val goldenVerified: Boolean = false,
    val username: String = "",
    val photoUrl: String = "",
    val bannerUrl: String = "",
)

data class ConversationUi(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: ConversationType,
    val avatarSeed: String,
    val participantIds: List<String>,
    val unreadCount: Int,
    val pinned: Boolean,
    val archived: Boolean,
    val muted: Boolean,
    val verified: Boolean,
    val goldenVerified: Boolean = false,
    val lastActive: String,
    val photoUrl: String = "",
)

data class ChatMessageUi(
    val id: String,
    val senderId: String,
    val senderName: String,
    val contentType: MessageContentType,
    val body: String,
    val timestamp: String,
    val deliveryStatus: MessageDeliveryStatus,
    val metadata: String = "",
    val replyToSnippet: String = "",
    val reactions: List<String> = emptyList(),
    val starred: Boolean = false,
    val mediaUrl: String = "",
)

data class StatusStoryUi(
    val id: String,
    val authorName: String,
    val avatarSeed: String,
    val content: String,
    val timestamp: String,
    val viewed: Boolean,
    val muted: Boolean,
    val contentType: MessageContentType = MessageContentType.TEXT,
    val mediaUrl: String = "",
    val seenViewerNames: List<String> = emptyList(),
    val reactions: List<String> = emptyList(),
    val privacyLabel: String = "My contacts",
    val authorId: String = "",
    val photoUrl: String = "",
)

data class CallRecordUi(
    val id: String,
    val contactName: String,
    val avatarSeed: String,
    val type: CallType,
    val direction: CallDirection,
    val timestamp: String,
    val durationLabel: String,
    val photoUrl: String = "",
)

data class LinkedDeviceUi(
    val id: String,
    val name: String,
    val lastActive: String,
    val location: String,
)

data class NotificationPreferencesUi(
    val pushNotifications: Boolean = true,
    val groupNotifications: Boolean = true,
    val callAlerts: Boolean = true,
    val previews: Boolean = true,
    val vibration: Boolean = true,
    val quietHoursEnabled: Boolean = false,
    val groupedNotifications: Boolean = true,
    val inlineReply: Boolean = true,
    val markReadAction: Boolean = true,
    val customToneLabel: String = "Default",
)

data class SecurityPreferencesUi(
    val appLock: Boolean = false,
    val fingerprintUnlock: Boolean = false,
    val screenshotBlock: Boolean = false,
    val readReceipts: Boolean = true,
    val endToEndEncryption: Boolean = true,
    val disappearingMessages: Boolean = false,
    val showLastSeen: Boolean = true,
    val showOnlinePresence: Boolean = true,
    val profilePhotoPublic: Boolean = true,
    val blurInRecents: Boolean = true,
    val chatLock: Boolean = false,
    val encryptedLocalCache: Boolean = true,
)

data class ChatPreferencesUi(
    val darkMode: Boolean = true,
    val mediaAutoDownload: Boolean = true,
    val enterToSend: Boolean = false,
    val highQualityUploads: Boolean = true,
    val translateIncoming: Boolean = false,
    val compactMode: Boolean = false,
    val chatThemeKey: String = "EMERALD",
    val customThemeImageUri: String = "",
    val profileBackgroundKey: String = "CRIMSON",
    val profileTopColorHex: String = "#C83B4B",
    val profileBottomColorHex: String = "#25070D",
)

data class ReliabilityUi(
    val isOfflineMode: Boolean = false,
    val queuedMessages: Int = 0,
    val lastBackupLabel: String = "Today, 09:30 PM",
    val syncHealth: String = "Healthy",
    val cacheHealth: String = "Warm",
    val syncBanner: String = "Realtime sync active",
    val retryDashboardLabel: String = "Queue empty",
)

data class ActiveCallUi(
    val conversationId: String,
    val title: String,
    val type: CallType,
    val startedLabel: String,
    val muted: Boolean = false,
    val speakerOn: Boolean = true,
    val videoEnabled: Boolean = true,
    val direction: CallDirection = CallDirection.OUTGOING,
    val reconnecting: Boolean = false,
    val networkLabel: String = "HD",
    val canScreenShare: Boolean = true,
)

data class ContactSearchUi(
    val id: String,
    val displayName: String,
    val username: String,
    val phoneNumber: String,
    val online: Boolean,
    val goldenVerified: Boolean = false,
    val photoUrl: String = "",
)
