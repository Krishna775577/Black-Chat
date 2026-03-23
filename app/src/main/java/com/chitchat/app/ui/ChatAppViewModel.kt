package com.chitchat.app.ui

import android.os.SystemClock
import android.net.Uri
import com.chitchat.app.ChitChatApplication
import com.chitchat.app.calls.WebRtcCallManager
import com.chitchat.app.voice.VoiceNoteRecorder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chitchat.app.BuildConfig
import com.chitchat.app.data.firebase.FirebaseAuthManager
import com.chitchat.app.data.firebase.FirebaseStorageManager
import com.chitchat.app.data.local.LocalChatStore
import com.chitchat.app.data.local.PendingMessageCache
import com.chitchat.app.data.local.SavedSession
import com.chitchat.app.data.local.SessionManager
import com.chitchat.app.data.firebase.FcmTokenRepository
import com.chitchat.app.data.firebase.PhoneOtpAuthManager
import com.chitchat.app.data.firebase.StructuredChatRepository
import com.chitchat.app.data.firebase.UsernameAlreadyExistsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.chitchat.app.data.model.ActiveCallUi
import com.chitchat.app.data.model.AppUser
import com.chitchat.app.data.model.CallDirection
import com.chitchat.app.data.model.CallRecordUi
import com.chitchat.app.data.model.CallType
import com.chitchat.app.data.model.ContactSearchUi
import com.chitchat.app.data.model.ChatFilter
import com.chitchat.app.data.model.ChatMessageUi
import com.chitchat.app.data.model.ChatPreferencesUi
import com.chitchat.app.data.model.ConversationType
import com.chitchat.app.data.model.ConversationUi
import com.chitchat.app.data.model.HomeTab
import com.chitchat.app.data.model.FireChatThread
import com.chitchat.app.data.model.FireMessage
import com.chitchat.app.data.model.FireStatusStory
import com.chitchat.app.data.model.FireUserProfile
import com.chitchat.app.data.model.LinkedDeviceUi
import com.chitchat.app.data.model.LoginFlowStep
import com.chitchat.app.data.model.MessageContentType
import com.chitchat.app.data.model.MessageDeliveryStatus
import com.chitchat.app.data.model.NotificationPreferencesUi
import com.chitchat.app.data.model.ReliabilityUi
import com.chitchat.app.data.model.SecurityPreferencesUi
import com.chitchat.app.data.model.StatusStoryUi
import kotlin.random.Random
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAppViewModel : ViewModel() {
    companion object {
        private const val GLOBAL_CHAT_ID = "global_chat_room"
        private const val TEMP_ACCOUNT_DELETE_DELAY_MS = 30L * 24L * 60L * 60L * 1000L
    }

    var isLoggedIn by mutableStateOf(false)
        private set
    var loginFlowStep by mutableStateOf(LoginFlowStep.ANONYMOUS_USERNAME)
        private set

    var usernameInput by mutableStateOf("")
    var emailInput by mutableStateOf("")
    var countryCodeInput by mutableStateOf("+91")
    var phoneInput by mutableStateOf("")
    var otpInput by mutableStateOf("")
    var authPasswordInput by mutableStateOf("")
    var profileNameInput by mutableStateOf("")
    var profileAboutInput by mutableStateOf("Hey there! I am on ChitChat.")
    var newChatPhoneInput by mutableStateOf("")
    var newChatNameInput by mutableStateOf("")
    var newGroupTitleInput by mutableStateOf("")
    var newGroupPhonesInput by mutableStateOf("")
    var statusCaptionInput by mutableStateOf("Available on ChitChat ✨")

    var showStatusAudienceDialog by mutableStateOf(false)
        private set
    var statusShareWithEveryone by mutableStateOf(true)
        private set
    val selectedStatusAudienceIds = mutableStateListOf<String>()

    var currentTab by mutableStateOf(HomeTab.CHATS)
        private set
    var selectedChatId by mutableStateOf<String?>(null)
        private set
    var selectedHomeConversationId by mutableStateOf<String?>(null)
        private set
    var searchQuery by mutableStateOf("")
        private set
    var messageSearchQuery by mutableStateOf("")
        private set
    var composerText by mutableStateOf("")
        private set
    var replyPreview by mutableStateOf<String?>(null)
        private set
    var selectedFilter by mutableStateOf(ChatFilter.ALL)
        private set
    var activeCall by mutableStateOf<ActiveCallUi?>(null)
        private set
    var showProfileEditor by mutableStateOf(false)
        private set
    var selectedUserProfile by mutableStateOf<FireUserProfile?>(null)
        private set
    var isProfilePhotoBusy by mutableStateOf(false)
        private set
    var isProfileBannerBusy by mutableStateOf(false)
        private set

    var isVoiceRecording by mutableStateOf(false)
        private set
    var voiceRecordingElapsedMs by mutableStateOf(0L)
        private set

    var currentUser by mutableStateOf(
        AppUser(
            id = "me",
            name = "You",
            phoneNumber = "",
            about = profileAboutInput,
            avatarSeed = "M",
            online = true,
            lastSeen = "online",
            verified = false,
            goldenVerified = false,
            username = usernameInput,
        )
    )
        private set

    var notificationPreferences by mutableStateOf(NotificationPreferencesUi())
        private set
    var securityPreferences by mutableStateOf(SecurityPreferencesUi())
        private set
    var chatPreferences by mutableStateOf(ChatPreferencesUi())
        private set
    var reliability by mutableStateOf(ReliabilityUi())
        private set

    private val conversationsState = mutableStateListOf<ConversationUi>()
    private val messagesByChat = mutableStateMapOf<String, MutableList<ChatMessageUi>>()
    val statusStories = mutableStateListOf<StatusStoryUi>()
    val callLogs = mutableStateListOf<CallRecordUi>()
    val linkedDevices = mutableStateListOf<LinkedDeviceUi>()
    val blockedUserIds = mutableStateListOf<String>()
    private val hiddenConversationIds = mutableStateListOf<String>()

    var selectedStatusStory by mutableStateOf<StatusStoryUi?>(null)
        private set
    var statusReplyInput by mutableStateOf("")
    var loadedMessageWindow by mutableStateOf(40)
        private set
    private val contactProfiles = mutableStateMapOf<String, FireUserProfile>()
    private var pendingStatusMediaUri: Uri? = null
    private var pendingStatusMediaType: MessageContentType? = null
    private var pendingStatusCaption: String = ""


    val contactSearchResults = mutableStateListOf<ContactSearchUi>()
    val quickUserSearchResults = mutableStateListOf<ContactSearchUi>()
    var isDirectorySearchBusy by mutableStateOf(false)
        private set
    var isQuickUserSearchBusy by mutableStateOf(false)
        private set

    var authStatusMessage by mutableStateOf("Username aur password se temporary account banao, ya apne purane temporary account me log in karo.")
        private set

    fun updateAuthStatusMessage(message: String) {
        authStatusMessage = message
    }
    var isAuthBusy by mutableStateOf(false)
        private set
    var isOtpRequested by mutableStateOf(false)
        private set
    var otpResendSeconds by mutableStateOf(0)
        private set
    var otpAttemptCount by mutableStateOf(0)
        private set
    var backendConnected by mutableStateOf(false)
        private set
    var showNewChatDialog by mutableStateOf(false)
        private set
    var showNewGroupDialog by mutableStateOf(false)
        private set
    var showMediaPickerDialog by mutableStateOf(false)
        private set
    var showStatusMediaPickerDialog by mutableStateOf(false)
        private set
    var mediaActionStatus by mutableStateOf("No media selected yet.")
        private set
    var isMediaBusy by mutableStateOf(false)
        private set
    var mediaUploadProgress by mutableStateOf(0)
        private set

    private val otpAuthManager = PhoneOtpAuthManager()
    private val structuredChatRepository = StructuredChatRepository()
    private val storageManager = FirebaseStorageManager()
    private val fcmTokenRepository = FcmTokenRepository()
    private val callManager = WebRtcCallManager()
    private val voiceRecorder = VoiceNoteRecorder(ChitChatApplication.instance.applicationContext)
    private var threadsJob: Job? = null
    private var messagesJob: Job? = null
    private var globalMessagesJob: Job? = null
    private var profilesJob: Job? = null
    private var statusesJob: Job? = null
    private var incomingCallsJob: Job? = null
    private var quickUserSearchJob: Job? = null
    private var activeCallRoomId: String? = null
    private var otpCountdownJob: Job? = null
    private var voiceRecordingTickerJob: Job? = null
    private var voiceRecordingStartedAtMs: Long = 0L
    private var lastObservedGlobalMessageId: String? = null

    init {
        loadStoredPreferences()
        LocalChatStore.cleanupCache()
        restoreExistingSessionOrSeedDemo()
    }

    private fun realtimeEnabledForCurrentSession(): Boolean = BuildConfig.REALTIME_BACKEND_ENABLED && currentUser.id.isNotBlank()

    val hasHomeConversationSelection: Boolean
        get() = selectedHomeConversationId != null

    val selectedHomeConversation: ConversationUi?
        get() = conversationsState.firstOrNull { it.id == selectedHomeConversationId }

    private fun isGlobalChat(chatId: String): Boolean = chatId == GLOBAL_CHAT_ID

    private fun currentUserPrivacyProfile(
        userId: String = currentUser.id,
        displayName: String = currentUser.name,
        phoneNumber: String = normalizePhone(currentUser.phoneNumber),
        username: String = normalizeUsername(currentUser.username),
        about: String = currentUser.about,
        photoUrl: String = currentUser.photoUrl,
        bannerUrl: String = currentUser.bannerUrl,
        verified: Boolean = currentUser.verified,
        goldenVerified: Boolean = currentUser.goldenVerified,
        temporaryAuthEmail: String = "",
    ): FireUserProfile = FireUserProfile(
        id = userId,
        phoneNumber = phoneNumber,
        displayName = displayName,
        username = username,
        about = about,
        photoUrl = photoUrl,
        bannerUrl = bannerUrl,
        verified = verified,
        goldenVerified = goldenVerified,
        temporaryAuthEmail = temporaryAuthEmail,
        online = true,
        lastSeenAt = System.currentTimeMillis(),
        linkedDeviceCount = linkedDevices.size,
        appState = "foreground",
        temporaryAccount = !verified && phoneNumber.isBlank(),
        deleteAfterAt = 0L,
        readReceiptsEnabled = securityPreferences.readReceipts,
        showLastSeenEnabled = securityPreferences.showLastSeen,
        showOnlinePresenceEnabled = securityPreferences.showOnlinePresence,
    )

    private fun syncCurrentUserPrivacyPreferences() {
        if (!backendConnected || currentUser.id.isBlank()) return
        viewModelScope.launch {
            runCatching {
                structuredChatRepository.upsertUserProfile(currentUserPrivacyProfile())
            }
        }
    }

    private fun buildGlobalConversation(unreadCountOverride: Int? = null): ConversationUi {
        val existing = conversationsState.firstOrNull { it.id == GLOBAL_CHAT_ID }
        val latest = messagesByChat[GLOBAL_CHAT_ID]?.lastOrNull()
        val latestPreview = latest?.let { message ->
            when (message.contentType) {
                MessageContentType.TEXT -> message.body
                MessageContentType.IMAGE -> "📷 ${message.body}"
                MessageContentType.VIDEO -> "🎥 ${message.body}"
                MessageContentType.DOCUMENT -> "📄 ${message.body}"
                MessageContentType.VOICE -> "🎤 ${message.body}"
                MessageContentType.LOCATION -> "📍 ${message.body}"
                MessageContentType.CONTACT -> "👤 ${message.body}"
                MessageContentType.POLL -> "📊 ${message.body}"
                MessageContentType.SYSTEM -> message.body
            }
        } ?: "Discuss with everyone in one room"
        return ConversationUi(
            id = GLOBAL_CHAT_ID,
            title = "Global Chat",
            subtitle = latestPreview,
            type = ConversationType.GROUP,
            avatarSeed = "G",
            participantIds = listOf(currentUser.id),
            unreadCount = unreadCountOverride ?: existing?.unreadCount ?: 0,
            pinned = existing?.pinned ?: true,
            archived = existing?.archived ?: false,
            muted = existing?.muted ?: false,
            verified = false,
            lastActive = latest?.timestamp ?: existing?.lastActive ?: "Now",
            photoUrl = "",
        )
    }

    private fun ensureGlobalConversation(unreadCountOverride: Int? = null) {
        val globalConversation = buildGlobalConversation(unreadCountOverride)
        val currentIndex = conversationsState.indexOfFirst { it.id == GLOBAL_CHAT_ID }
        if (currentIndex >= 0) {
            conversationsState[currentIndex] = globalConversation
        } else {
            conversationsState.add(0, globalConversation)
        }
    }

    fun connectionStatusLabel(): String {
        return when {
            backendConnected -> "realtime sync on"
            realtimeEnabledForCurrentSession() -> "global chat live"
            else -> "local cache only"
        }
    }

    fun selectedConversationSyncBanner(): String {
        val chatId = selectedConversation?.id.orEmpty()
        return when {
            isGlobalChat(chatId) && realtimeEnabledForCurrentSession() && !reliability.isOfflineMode -> "Global chat live"
            backendConnected -> reliability.syncBanner
            else -> "Cached locally"
        }
    }

    val visibleConversations: List<ConversationUi>
        get() {
            return conversationsState
                .filterNot { it.id in hiddenConversationIds }
                .filter { convo ->
                    val queryMatches = searchQuery.isBlank() || convo.title.contains(searchQuery, true) || convo.subtitle.contains(searchQuery, true)
                    val filterMatches = when (selectedFilter) {
                        ChatFilter.ALL -> !convo.archived
                        ChatFilter.UNREAD -> convo.unreadCount > 0 && !convo.archived
                        ChatFilter.GROUPS -> convo.type == ConversationType.GROUP && !convo.archived
                        ChatFilter.ARCHIVED -> convo.archived
                        ChatFilter.STARRED -> conversationMessages(convo.id).any { it.starred }
                    }
                    queryMatches && filterMatches
                }
                .sortedWith(
                    compareByDescending<ConversationUi> { it.pinned }
                        .thenByDescending { it.unreadCount }
                        .thenByDescending { it.lastActive }
                )
        }

    val selectedConversation: ConversationUi?
        get() = conversationsState.firstOrNull { it.id == selectedChatId }

    val selectedMessages: List<ChatMessageUi>
        get() {
            val current = selectedChatId ?: return emptyList()
            return conversationMessages(current)
                .filter {
                    messageSearchQuery.isBlank() ||
                        it.body.contains(messageSearchQuery, true) ||
                        it.metadata.contains(messageSearchQuery, true) ||
                        it.senderName.contains(messageSearchQuery, true)
                }
                .takeLast(loadedMessageWindow)
        }


    fun openEmailLoginStep() {
        openAnonymousUsernameStep()
        authStatusMessage = "Sirf username + password temporary account available hai."
    }

    fun openAnonymousUsernameStep() {
        loginFlowStep = LoginFlowStep.ANONYMOUS_USERNAME
        usernameInput = usernameInput.ifBlank { "" }
        authPasswordInput = ""
        authStatusMessage = "Choose a username and password for your temporary account. Logout ke 30 din baad hi delete hoga."
    }

    fun openTemporaryLoginStep() {
        loginFlowStep = LoginFlowStep.TEMPORARY_LOGIN
        authPasswordInput = ""
        authStatusMessage = "Enter your username and password to log in to your temporary account."
    }

    fun backToLoginChoice() {
        loginFlowStep = LoginFlowStep.CHOICE
        isOtpRequested = false
        isAuthBusy = false
        otpResendSeconds = 0
        otpCountdownJob?.cancel()
        otpAuthManager.reset()
        authPasswordInput = ""
        authStatusMessage = "Sirf username + password temporary account available hai."
    }

    fun completeAnonymousLogin() {
        if (isAuthBusy) return
        val username = normalizeUsername(usernameInput)
        val password = authPasswordInput.trim()
        if (username.isBlank()) {
            authStatusMessage = "Enter a username first."
            return
        }
        if (password.length < 6) {
            authStatusMessage = "Password kam se kam 6 characters ka rakho."
            return
        }
        val displayName = username
        profileNameInput = displayName
        currentUser = currentUser.copy(
            name = displayName,
            phoneNumber = "",
            about = profileAboutInput.ifBlank { "Temporary user on Black Chat" },
            avatarSeed = displayName.firstOrNull()?.uppercase() ?: "U",
            verified = false,
            username = username,
        )
        isAuthBusy = true
        authStatusMessage = "Creating temporary account…"
        viewModelScope.launch {
            runCatching { FirebaseAuthManager.registerTemporaryAccount(username, password) }
                .onSuccess { uid ->
                    currentUser = currentUser.copy(id = uid)
                    val bound = persistProfileAndBind(uid, rollbackAnonymousAuthOnUsernameConflict = true)
                    isAuthBusy = false
                    isLoggedIn = bound
                    if (bound) {
                        authPasswordInput = ""
                    } else {
                        currentUser = currentUser.copy(id = "me")
                        loginFlowStep = LoginFlowStep.ANONYMOUS_USERNAME
                    }
                }
                .onFailure { error ->
                    isAuthBusy = false
                    backendConnected = false
                    isLoggedIn = false
                    authStatusMessage = when (error) {
                        is FirebaseAuthUserCollisionException -> "This username already exists. Tap ‘You have an account?’ to log in."
                        is FirebaseAuthInvalidCredentialsException -> "Password kam se kam 6 characters ka rakho."
                        else -> "Temporary account create nahi ho paya: ${error.message ?: "unknown error"}"
                    }
                }
        }
    }

    fun loginTemporaryAccount() {
        if (isAuthBusy) return
        val username = normalizeUsername(usernameInput)
        val password = authPasswordInput.trim()
        if (username.isBlank()) {
            authStatusMessage = "Username enter karo."
            return
        }
        if (password.isBlank()) {
            authStatusMessage = "Password enter karo."
            return
        }
        isAuthBusy = true
        authStatusMessage = "Logging in…"
        viewModelScope.launch {
            runCatching { FirebaseAuthManager.signInTemporaryAccount(username, password) }
                .onSuccess { uid ->
                    val profile = runCatching { structuredChatRepository.getUserProfile(uid) }.getOrNull()
                    val now = System.currentTimeMillis()
                    if (profile?.temporaryAccount == true && profile.deleteAfterAt in 1..now) {
                        runCatching { structuredChatRepository.deleteStatusesByAuthor(uid) }
                        runCatching { structuredChatRepository.deleteUserProfile(uid) }
                        runCatching { FirebaseAuthManager.deleteCurrentUserIfPossible() }
                        runCatching { FirebaseAuthManager.signOut() }
                        isAuthBusy = false
                        isLoggedIn = false
                        authStatusMessage = "Ye temporary account 30 days logout rehne ki wajah se delete ho chuka hai. Naya account banao."
                        loginFlowStep = LoginFlowStep.ANONYMOUS_USERNAME
                        return@launch
                    }

                    val displayName = profile?.displayName?.ifBlank { username } ?: username
                    profileNameInput = displayName
                    profileAboutInput = profile?.about?.ifBlank { "Temporary user on Black Chat" } ?: "Temporary user on Black Chat"
                    currentUser = currentUser.copy(
                        id = uid,
                        name = displayName,
                        phoneNumber = profile?.phoneNumber.orEmpty(),
                        about = profileAboutInput,
                        avatarSeed = displayName.firstOrNull()?.uppercase() ?: "U",
                        verified = false,
                        goldenVerified = profile?.goldenVerified == true,
                        username = profile?.username?.ifBlank { username } ?: username,
                        photoUrl = profile?.photoUrl.orEmpty(),
                        bannerUrl = profile?.bannerUrl.orEmpty(),
                    )
                    val bound = persistProfileAndBind(uid)
                    isAuthBusy = false
                    isLoggedIn = bound
                    authPasswordInput = ""
                    if (!bound) {
                        runCatching { FirebaseAuthManager.signOut() }
                    }
                }
                .onFailure { error ->
                    isAuthBusy = false
                    isLoggedIn = false
                    authStatusMessage = when (error) {
                        is FirebaseAuthInvalidUserException,
                        is FirebaseAuthInvalidCredentialsException -> "Username ya password galat hai."
                        else -> "Login failed: ${error.message ?: "unknown error"}"
                    }
                }
        }
    }

    fun createEmailAccount() {
        authStatusMessage = "Email login hata diya gaya hai. Sirf temporary username account use karo."
        openAnonymousUsernameStep()
    }

    fun loginEmailAccount() {
        authStatusMessage = "Email login hata diya gaya hai. Sirf temporary username account use karo."
        openTemporaryLoginStep()
    }

    private suspend fun completePermanentAccountLogin(
        uid: String,
        email: String,
        displayNameHint: String = "",
        usernameHint: String = "",
        photoUrlHint: String = "",
    ): Boolean {
        val profile = runCatching { structuredChatRepository.getUserProfile(uid) }.getOrNull()
        val fallbackDisplayName = profile?.displayName?.ifBlank {
            displayNameHint.ifBlank { deriveDisplayNameFromEmail(email) }
        } ?: displayNameHint.ifBlank { deriveDisplayNameFromEmail(email) }
        val requestedUsername = normalizeUsername(
            profile?.username?.ifBlank {
                usernameHint.ifBlank { deriveUsernameFromEmail(email) }
            } ?: usernameHint.ifBlank { deriveUsernameFromEmail(email) }
        )
        val resolvedUsername = ensureAvailableUsername(
            requestedUsername.ifBlank { "user${uid.takeLast(6)}" },
            uid,
        )
        profileNameInput = fallbackDisplayName
        profileAboutInput = profile?.about?.ifBlank { profileAboutInput.ifBlank { "Hey there! I am on Black Chat." } }
            ?: profileAboutInput.ifBlank { "Hey there! I am on Black Chat." }
        emailInput = normalizeEmailAddress(email.ifBlank { FirebaseAuthManager.currentEmail() })
        usernameInput = resolvedUsername
        currentUser = currentUser.copy(
            id = uid,
            name = fallbackDisplayName,
            phoneNumber = profile?.phoneNumber.orEmpty(),
            about = profileAboutInput,
            avatarSeed = fallbackDisplayName.firstOrNull()?.uppercase() ?: currentUser.avatarSeed,
            verified = true,
            goldenVerified = profile?.goldenVerified == true,
            username = resolvedUsername,
            photoUrl = profile?.photoUrl?.ifBlank { photoUrlHint } ?: photoUrlHint,
            bannerUrl = profile?.bannerUrl.orEmpty(),
        )
        return persistProfileAndBind(uid)
    }

    private fun deriveDisplayNameFromEmail(email: String): String {
        val localPart = email.substringBefore("@").ifBlank { "black chat user" }
        val cleaned = localPart.replace('.', ' ').replace('_', ' ').replace('-', ' ')
        val words = cleaned.split(' ').filter { it.isNotBlank() }
        return if (words.isEmpty()) {
            "Black Chat User"
        } else {
            words.joinToString(" ") { part ->
                part.lowercase(Locale.ROOT).replaceFirstChar { ch -> ch.uppercase(Locale.ROOT) }
            }
        }
    }

    private fun deriveUsernameFromEmail(email: String): String = normalizeUsername(email.substringBefore("@"))

    private fun normalizeEmailAddress(raw: String): String = raw.trim().lowercase(Locale.ROOT)

    private fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        val parts = email.split("@")
        return parts.size == 2 && parts[0].isNotBlank() && parts[1].contains(".")
    }

    fun openNewChatDialog() {

        newChatPhoneInput = ""
        newChatNameInput = ""
        contactSearchResults.clear()
        showNewChatDialog = true
    }

    fun dismissNewChatDialog() {
        contactSearchResults.clear()
        showNewChatDialog = false
    }

    fun openNewGroupDialog() {
        newGroupTitleInput = ""
        newGroupPhonesInput = ""
        showNewGroupDialog = true
    }

    val statusAudienceCandidates: List<ContactSearchUi>
        get() = conversationsState
            .filter { it.type == ConversationType.PRIVATE && it.id != GLOBAL_CHAT_ID }
            .mapNotNull { conversation ->
                val otherId = conversation.participantIds.firstOrNull { it != currentUser.id } ?: return@mapNotNull null
                val profile = contactProfiles[otherId]
                ContactSearchUi(
                    id = otherId,
                    displayName = profile?.displayName?.ifBlank { profile.username.ifBlank { conversation.title } } ?: conversation.title,
                    username = profile?.username.orEmpty(),
                    phoneNumber = profile?.phoneNumber.orEmpty(),
                    online = profile?.online == true,
                    goldenVerified = profile?.goldenVerified == true,
                    photoUrl = profile?.photoUrl.orEmpty(),
                )
            }
            .distinctBy { it.id }

    fun prepareTextStatusForAudience() {
        pendingStatusMediaUri = null
        pendingStatusMediaType = MessageContentType.TEXT
        pendingStatusCaption = statusCaptionInput
        statusShareWithEveryone = true
        selectedStatusAudienceIds.clear()
        showStatusAudienceDialog = true
    }

    fun prepareStatusMediaForAudience(localUri: Uri, type: MessageContentType) {
        pendingStatusMediaUri = localUri
        pendingStatusMediaType = type
        pendingStatusCaption = statusCaptionInput
        statusShareWithEveryone = true
        selectedStatusAudienceIds.clear()
        showStatusAudienceDialog = true
    }

    fun updateStatusShareMode(shareWithEveryone: Boolean) {
        statusShareWithEveryone = shareWithEveryone
        if (shareWithEveryone) selectedStatusAudienceIds.clear()
    }

    fun toggleStatusAudienceSelection(userId: String) {
        if (userId.isBlank()) return
        if (userId in selectedStatusAudienceIds) {
            selectedStatusAudienceIds.remove(userId)
        } else {
            selectedStatusAudienceIds.add(userId)
        }
    }

    fun dismissStatusAudienceDialog() {
        showStatusAudienceDialog = false
        pendingStatusMediaUri = null
        pendingStatusMediaType = null
        pendingStatusCaption = ""
        statusShareWithEveryone = true
        selectedStatusAudienceIds.clear()
    }

    fun confirmStatusAudienceSelection() {
        val audienceIds = if (statusShareWithEveryone) {
            statusAudienceCandidates.map { it.id }.distinct()
        } else {
            selectedStatusAudienceIds.toList()
        }
        if (audienceIds.isEmpty()) {
            authStatusMessage = if (statusShareWithEveryone) {
                "Home screen me users add karo, tabhi status share hoga."
            } else {
                "Kam se kam 1 user select karo jo status dekh sake."
            }
            return
        }
        val privacyMode = if (statusShareWithEveryone) "contacts" else "selected"
        val privacyLabel = if (statusShareWithEveryone) "Share with everyone" else "Selected (${audienceIds.size})"
        val pendingUri = pendingStatusMediaUri
        val pendingType = pendingStatusMediaType
        pendingStatusMediaUri = null
        pendingStatusMediaType = null
        pendingStatusCaption = ""
        showStatusAudienceDialog = false
        if (pendingType == null) return
        if (pendingType == MessageContentType.TEXT) {
            publishTextStatusInternal(privacyMode, audienceIds, privacyLabel)
        } else if (pendingUri != null) {
            publishPickedStatusMedia(pendingUri, pendingType, privacyMode, audienceIds, privacyLabel)
        }
        statusShareWithEveryone = true
        selectedStatusAudienceIds.clear()
    }

    fun dismissNewGroupDialog() {
        showNewGroupDialog = false
    }

    fun openMediaPickerDialog() {
        showMediaPickerDialog = true
    }

    fun dismissMediaPickerDialog() {
        showMediaPickerDialog = false
    }

    fun openStatusMediaPickerDialog() {
        showStatusMediaPickerDialog = true
    }

    fun dismissStatusMediaPickerDialog() {
        showStatusMediaPickerDialog = false
    }

    fun searchDirectory(query: String) {
        newChatPhoneInput = query
        if (!backendConnected || query.isBlank()) {
            contactSearchResults.clear()
            isDirectorySearchBusy = false
            return
        }
        isDirectorySearchBusy = true
        viewModelScope.launch {
            runCatching { structuredChatRepository.searchUsers(query, currentUser.id) }
                .onSuccess { profiles ->
                    contactSearchResults.clear()
                    contactSearchResults.addAll(profiles.map {
                        ContactSearchUi(
                            id = it.id,
                            displayName = it.displayName.ifBlank { it.username.ifBlank { it.phoneNumber } },
                            username = it.username,
                            phoneNumber = it.phoneNumber,
                            online = it.online,
                            goldenVerified = it.goldenVerified,
                            photoUrl = it.photoUrl,
                        )
                    })
                    isDirectorySearchBusy = false
                }
                .onFailure {
                    isDirectorySearchBusy = false
                    contactSearchResults.clear()
                }
        }
    }

    fun applySearchContact(result: ContactSearchUi) {
        newChatNameInput = result.displayName
        newChatPhoneInput = if (result.username.isNotBlank()) result.username else result.phoneNumber
        contactSearchResults.clear()
    }

    private fun refreshQuickUserSearch(query: String) {
        quickUserSearchJob?.cancel()
        val trimmed = query.trim()
        if (!backendConnected || trimmed.isBlank()) {
            quickUserSearchResults.clear()
            isQuickUserSearchBusy = false
            return
        }
        isQuickUserSearchBusy = true
        quickUserSearchJob = viewModelScope.launch {
            delay(180)
            runCatching { structuredChatRepository.searchUsers(trimmed, currentUser.id, limit = 25) }
                .onSuccess { profiles ->
                    if (searchQuery.trim() != trimmed) return@onSuccess
                    quickUserSearchResults.clear()
                    quickUserSearchResults.addAll(
                        profiles.map {
                            ContactSearchUi(
                                id = it.id,
                                displayName = it.displayName.ifBlank { it.username.ifBlank { it.phoneNumber.ifBlank { "Unknown user" } } },
                                username = it.username,
                                phoneNumber = it.phoneNumber,
                                online = it.online,
                                goldenVerified = it.goldenVerified,
                                photoUrl = it.photoUrl,
                            )
                        }
                    )
                    isQuickUserSearchBusy = false
                }
                .onFailure {
                    if (searchQuery.trim() == trimmed) {
                        quickUserSearchResults.clear()
                        isQuickUserSearchBusy = false
                    }
                }
        }
    }

    private fun resolvedProfileName(profile: FireUserProfile): String {
        return profile.displayName.ifBlank {
            profile.username.ifBlank { profile.phoneNumber.ifBlank { "ChitChat user" } }
        }
    }

    private fun openViewedProfile(profile: FireUserProfile, statusMessage: String) {
        selectedChatId = null
        showProfileEditor = false
        selectedUserProfile = profile
        searchQuery = ""
        quickUserSearchResults.clear()
        isQuickUserSearchBusy = false
        authStatusMessage = statusMessage
    }

    fun openUserProfileFromSearch(result: ContactSearchUi) {
        if (!backendConnected) {
            authStatusMessage = "Realtime profile search abhi available nahi hai."
            return
        }
        viewModelScope.launch {
            runCatching {
                structuredChatRepository.getUserProfile(result.id)
                    ?: FireUserProfile(
                        id = result.id,
                        displayName = result.displayName,
                        username = result.username,
                        phoneNumber = result.phoneNumber,
                        online = result.online,
                        goldenVerified = result.goldenVerified,
                        photoUrl = result.photoUrl,
                    )
            }.onSuccess { profile ->
                if (profile.id == currentUser.id) {
                    openProfileEditor()
                    authStatusMessage = "Yeh aapka apna profile hai."
                } else {
                    openViewedProfile(profile, "Profile opened for ${resolvedProfileName(profile)}")
                }
            }.onFailure { error ->
                authStatusMessage = error.message ?: "Could not open profile."
            }
        }
    }

    fun closeUserProfile() {
        selectedUserProfile = null
    }

    fun openDirectMessageWithProfile(profile: FireUserProfile) {
        if (!backendConnected) {
            authStatusMessage = "Realtime private chat abhi available nahi hai. Global chat use karo."
            return
        }
        val title = resolvedProfileName(profile)
        viewModelScope.launch {
            runCatching { structuredChatRepository.findOrCreatePrivateThread(currentUser.id, profile.id, title) }
                .onSuccess { chatId ->
                    upsertConversationLocally(
                        ConversationUi(
                            id = chatId,
                            title = title,
                            subtitle = if (profile.username.isNotBlank()) "@${profile.username}" else profile.phoneNumber.ifBlank { "Tap to start chatting" },
                            type = ConversationType.PRIVATE,
                            avatarSeed = title.take(1).ifBlank { "U" },
                            participantIds = listOf(currentUser.id, profile.id),
                            unreadCount = 0,
                            pinned = false,
                            archived = false,
                            muted = false,
                            verified = profile.verified,
                            goldenVerified = profile.goldenVerified,
                            lastActive = "Now",
                            photoUrl = profile.photoUrl,
                        )
                    )
                    selectedUserProfile = null
                    authStatusMessage = "DM opened with $title"
                    openConversation(chatId)
                }
                .onFailure { error ->
                    authStatusMessage = error.message ?: "Could not open direct chat."
                }
        }
    }

    fun openDirectMessageFromSearch(result: ContactSearchUi) {
        openUserProfileFromSearch(result)
    }

    private fun buildProfileQrPayload(userId: String, username: String, displayName: String): String {
        return Uri.Builder()
            .scheme("chitchat")
            .authority("profile")
            .appendQueryParameter("id", userId)
            .appendQueryParameter("username", username.ifBlank { "anonymous" })
            .appendQueryParameter("name", displayName.ifBlank { "ChitChat user" })
            .build()
            .toString()
    }

    fun profileQrPayload(): String {
        return buildProfileQrPayload(
            userId = currentUser.id,
            username = currentUser.username,
            displayName = currentUser.name,
        )
    }

    fun profileQrPayload(profile: FireUserProfile): String {
        return buildProfileQrPayload(
            userId = profile.id,
            username = profile.username,
            displayName = resolvedProfileName(profile),
        )
    }

    fun openProfileFromQrPayload(payload: String) {
        val content = payload.trim()
        if (content.isBlank()) {
            authStatusMessage = "QR scan cancelled."
            return
        }
        val parsed = runCatching {
            val uri = Uri.parse(content)
            if (uri.scheme == "chitchat" && uri.authority == "profile") {
                uri.getQueryParameter("id").orEmpty() to uri.getQueryParameter("username").orEmpty()
            } else {
                "" to ""
            }
        }.getOrDefault("" to "")
        val legacyUserId = content.removePrefix("CHITCHAT_PROFILE|").takeIf { it != content }?.substringBefore('|').orEmpty()
        val userId = parsed.first.ifBlank { legacyUserId }
        val username = parsed.second.removePrefix("@").trim()

        if (userId.isBlank() && username.isBlank()) {
            authStatusMessage = "This QR code is not a valid ChitChat profile."
            return
        }
        if ((userId.isNotBlank() && userId == currentUser.id) || (username.isNotBlank() && username.equals(currentUser.username, ignoreCase = true))) {
            openProfileEditor()
            authStatusMessage = "Yeh aapka apna profile QR hai."
            return
        }
        if (!backendConnected) {
            authStatusMessage = "Realtime connection required to open profile from QR."
            return
        }
        viewModelScope.launch {
            runCatching {
                when {
                    userId.isNotBlank() -> structuredChatRepository.getUserProfile(userId)
                    username.isNotBlank() -> structuredChatRepository.findUserByUsername(username)
                    else -> null
                } ?: throw IllegalStateException("This ChitChat user was not found in Firebase.")
            }.onSuccess { profile ->
                openViewedProfile(profile, "Profile opened for ${resolvedProfileName(profile)}")
            }.onFailure { error ->
                authStatusMessage = error.message ?: "Could not open profile from scanned QR."
            }
        }
    }

    fun openDirectMessageFromQrPayload(payload: String) {
        openProfileFromQrPayload(payload)
    }

    fun updateCurrentTab(tab: HomeTab) {
        currentTab = tab
        selectedChatId = null
        selectedUserProfile = null
    }

    fun updateSearchQuery(value: String) {
        searchQuery = value
        refreshQuickUserSearch(value)
    }

    fun updateMessageSearchQuery(value: String) {
        messageSearchQuery = value
    }

    fun updateComposerText(value: String) {
        composerText = value
        selectedChatId?.let { chatId ->
            LocalChatStore.saveDraft(chatId, value)
            if (backendConnected && !isGlobalChat(chatId)) {
                viewModelScope.launch {
                    runCatching { structuredChatRepository.setTyping(chatId, currentUser.id, currentUser.name, value.isNotBlank()) }
                }
            }
        }
    }

    fun selectFilter(filter: ChatFilter) {
        selectedFilter = filter
    }

    fun selectHomeConversation(id: String) {
        selectedHomeConversationId = id
        authStatusMessage = "Chat selected. Pin ya delete use karo."
    }

    fun clearHomeConversationSelection() {
        selectedHomeConversationId = null
    }

    fun handleHomeConversationTap(id: String) {
        if (selectedHomeConversationId != null) {
            selectedHomeConversationId = id
            return
        }
        openConversation(id)
    }

    fun pinSelectedHomeConversation() {
        val chatId = selectedHomeConversationId ?: return
        togglePinned(chatId)
        selectedHomeConversationId = null
        authStatusMessage = "Chat pinned to top."
    }

    fun deleteSelectedHomeConversationForMe() {
        val chatId = selectedHomeConversationId ?: return
        deleteConversationForCurrentUser(chatId)
        selectedHomeConversationId = null
    }

    fun openConversation(id: String) {
        selectedUserProfile = null
        showProfileEditor = false
        selectedHomeConversationId = null
        revealConversationIfHidden(id)
        selectedChatId = id
        composerText = LocalChatStore.loadDraft(id)
        markConversationRead(id)
        if (isGlobalChat(id)) {
            if (realtimeEnabledForCurrentSession() && !reliability.isOfflineMode) {
                bindGlobalMessages()
            } else {
                val cached = LocalChatStore.loadMessages(id)
                if (cached.isNotEmpty()) {
                    messagesByChat[id] = cached.toMutableStateList()
                }
            }
            return
        }
        if (backendConnected) {
            bindMessages(id)
        } else {
            val cached = LocalChatStore.loadMessages(id)
            if (cached.isNotEmpty()) {
                messagesByChat[id] = cached.toMutableStateList()
            }
        }
    }

    fun closeConversation() {
        selectedChatId?.let { chatId ->
            LocalChatStore.saveDraft(chatId, composerText)
            if (backendConnected && !isGlobalChat(chatId)) {
                viewModelScope.launch {
                    runCatching { structuredChatRepository.setTyping(chatId, currentUser.id, currentUser.name, false) }
                }
            }
        }
        selectedChatId = null
        replyPreview = null
        messageSearchQuery = ""
        composerText = ""
    }

    fun prepareReplyToLatestIncoming() {
        val target = selectedMessages.lastOrNull { it.senderId != currentUser.id } ?: return
        replyPreview = "Replying to ${target.senderName}: ${target.body.take(40)}"
    }


    fun clearReplyPreview() {
        replyPreview = null
    }

    fun editLatestOutgoingMessage() {
        val chatId = selectedChatId ?: return
        val latest = conversationMessages(chatId).lastOrNull { it.senderId == currentUser.id && it.contentType == MessageContentType.TEXT } ?: run {
            authStatusMessage = "No outgoing text message available to edit."
            return
        }
        composerText = latest.body
        replyPreview = "Editing your last message"
        if (isGlobalChat(chatId) && realtimeEnabledForCurrentSession()) {
            viewModelScope.launch {
                runCatching {
                    structuredChatRepository.editGlobalMessage(latest.id, currentUser.id, latest.body + " (edited)")
                }.onFailure { authStatusMessage = it.message ?: "Could not edit global message." }
            }
        } else if (backendConnected) {
            viewModelScope.launch {
                runCatching {
                    structuredChatRepository.editMessage(chatId, latest.id, currentUser.id, latest.body + " (edited)")
                }.onFailure { authStatusMessage = it.message ?: "Could not edit message." }
            }
        } else {
            val items = messagesByChat[chatId] ?: return
            val index = items.indexOfFirst { it.id == latest.id }
            if (index >= 0) {
                items[index] = items[index].copy(body = latest.body + " (edited)")
            }
        }
    }

    fun deleteLatestOutgoingForEveryone() {
        val chatId = selectedChatId ?: return
        val latest = conversationMessages(chatId).lastOrNull { it.senderId == currentUser.id } ?: run {
            authStatusMessage = "No outgoing message found to delete."
            return
        }
        if (isGlobalChat(chatId) && realtimeEnabledForCurrentSession()) {
            viewModelScope.launch {
                runCatching { structuredChatRepository.deleteGlobalMessageForEveryone(latest.id, currentUser.id) }
                    .onFailure { authStatusMessage = it.message ?: "Could not delete global message for everyone." }
            }
        } else if (backendConnected) {
            viewModelScope.launch {
                runCatching { structuredChatRepository.deleteMessageForEveryone(chatId, latest.id, currentUser.id) }
                    .onFailure { authStatusMessage = it.message ?: "Could not delete message for everyone." }
            }
        } else {
            val items = messagesByChat[chatId] ?: return
            val index = items.indexOfFirst { it.id == latest.id }
            if (index >= 0) {
                items[index] = items[index].copy(body = "This message was deleted", metadata = "deleted", deliveryStatus = MessageDeliveryStatus.SENT)
            }
        }
    }

    fun deleteLatestOutgoingForMe() {
        val chatId = selectedChatId ?: return
        val items = messagesByChat[chatId] ?: return
        val index = items.indexOfLast { it.senderId == currentUser.id }
        if (index == -1) {
            authStatusMessage = "No outgoing message found to delete."
            return
        }
        items.removeAt(index)
        LocalChatStore.cacheMessages(chatId, items.toList())
    }

    fun toggleStarOnLatestMessage() {
        val chatId = selectedChatId ?: return
        val items = messagesByChat[chatId] ?: return
        val index = items.lastIndex
        if (index < 0) return
        val target = items[index]
        val next = !target.starred
        items[index] = target.copy(starred = next)
        LocalChatStore.cacheMessages(chatId, items.toList())
        if (isGlobalChat(chatId) && realtimeEnabledForCurrentSession()) {
            viewModelScope.launch { runCatching { structuredChatRepository.setGlobalMessageStarred(target.id, currentUser.id, next) } }
            return
        }
        if (backendConnected) {
            viewModelScope.launch { runCatching { structuredChatRepository.setMessageStarred(chatId, target.id, currentUser.id, next) } }
        }
    }

    fun forwardLatestMessage() {
        val chatId = selectedChatId ?: return
        val source = conversationMessages(chatId).lastOrNull() ?: return
        if (isGlobalChat(chatId) && realtimeEnabledForCurrentSession()) {
            viewModelScope.launch {
                runCatching {
                    val backendSource = FireMessage(
                        id = source.id,
                        chatId = chatId,
                        senderId = source.senderId,
                        senderName = source.senderName,
                        type = source.contentType.name.lowercase(),
                        text = source.body,
                        fileName = source.body,
                        replyToSnippet = source.replyToSnippet,
                    )
                    structuredChatRepository.forwardGlobalMessage(currentUser.id, currentUser.name, backendSource)
                }.onFailure { authStatusMessage = it.message ?: "Could not forward global message." }
            }
        } else if (backendConnected) {
            viewModelScope.launch {
                runCatching {
                    val backendSource = FireMessage(
                        id = source.id,
                        chatId = chatId,
                        senderId = source.senderId,
                        senderName = source.senderName,
                        type = source.contentType.name.lowercase(),
                        text = source.body,
                        fileName = source.body,
                        replyToSnippet = source.replyToSnippet,
                    )
                    structuredChatRepository.forwardMessage(chatId, currentUser.id, currentUser.name, backendSource)
                }.onFailure { authStatusMessage = it.message ?: "Could not forward message." }
            }
        } else {
            appendMessage(
                chatId,
                source.copy(
                    id = randomId("fwd"),
                    senderId = currentUser.id,
                    senderName = currentUser.name,
                    timestamp = "Now",
                    deliveryStatus = MessageDeliveryStatus.SENT,
                    metadata = listOf(source.metadata, "Forwarded").filter { it.isNotBlank() }.joinToString(" • "),
                )
            )
        }
    }

    fun scheduleCurrentMessage() {
        val chatId = selectedChatId ?: return
        val text = composerText.trim()
        if (text.isBlank()) {
            authStatusMessage = "Type a message before scheduling it."
            return
        }
        queuePendingText(chatId = chatId, text = text, replySnippet = replyPreview.orEmpty())
        composerText = ""
        replyPreview = null
        authStatusMessage = "Message scheduled in local retry queue. It will be sent on the next sync."
    }

    fun latestMessageTextForCopyShare(): String = selectedMessages.lastOrNull()?.body.orEmpty()

    fun addReactionToLatestIncoming(reaction: String = "👍") {
        val chatId = selectedChatId ?: return
        val items = messagesByChat[chatId] ?: return
        val index = items.indexOfLast { it.senderId != currentUser.id }
        if (index == -1) return
        val target = items[index]
        items[index] = target.copy(reactions = (target.reactions + reaction).distinct())
    }

    fun sendTextMessage() {
        val chatId = selectedChatId ?: return
        val text = composerText.trim()
        if (text.isEmpty()) return
        val replySnippet = replyPreview.orEmpty()

        composerText = ""
        replyPreview = null
        LocalChatStore.saveDraft(chatId, "")
        if (backendConnected && !isGlobalChat(chatId)) {
            viewModelScope.launch {
                runCatching {
                    structuredChatRepository.setTyping(chatId, currentUser.id, currentUser.name, false)
                }
            }
        }

        val canUseLiveGlobal = isGlobalChat(chatId) && realtimeEnabledForCurrentSession() && !reliability.isOfflineMode
        if (canUseLiveGlobal) {
            val optimisticId = randomId("local")
            appendMessage(
                chatId,
                ChatMessageUi(
                    id = optimisticId,
                    senderId = currentUser.id,
                    senderName = currentUser.name,
                    contentType = MessageContentType.TEXT,
                    body = text,
                    timestamp = "Now",
                    deliveryStatus = MessageDeliveryStatus.SENT,
                    replyToSnippet = replySnippet,
                )
            )
            viewModelScope.launch {
                runCatching {
                    structuredChatRepository.sendGlobalTextMessage(
                        senderId = currentUser.id,
                        senderName = currentUser.name,
                        text = text,
                        replyToSnippet = replySnippet,
                    )
                }.onFailure { error ->
                    updateLocalMessageDeliveryStatus(chatId, optimisticId, MessageDeliveryStatus.QUEUED)
                    authStatusMessage = "Global send failed: ${error.message ?: "unknown error"}"
                    queuePendingText(chatId = chatId, text = text, replySnippet = replySnippet, appendToUi = false)
                }
            }
            return
        }
        if (backendConnected && !reliability.isOfflineMode && !isGlobalChat(chatId)) {
            val optimisticId = randomId("local")
            appendMessage(
                chatId,
                ChatMessageUi(
                    id = optimisticId,
                    senderId = currentUser.id,
                    senderName = currentUser.name,
                    contentType = MessageContentType.TEXT,
                    body = text,
                    timestamp = "Now",
                    deliveryStatus = MessageDeliveryStatus.SENT,
                    replyToSnippet = replySnippet,
                )
            )
            viewModelScope.launch {
                runCatching {
                    structuredChatRepository.sendTextMessage(
                        chatId = chatId,
                        senderId = currentUser.id,
                        senderName = currentUser.name,
                        text = text,
                        replyToSnippet = replySnippet,
                    )
                }.onFailure { error ->
                    updateLocalMessageDeliveryStatus(chatId, optimisticId, MessageDeliveryStatus.QUEUED)
                    authStatusMessage = "Send failed: ${error.message ?: "unknown error"}"
                    queuePendingText(chatId = chatId, text = text, replySnippet = replySnippet, appendToUi = false)
                }
            }
            return
        }
        queuePendingText(chatId = chatId, text = text, replySnippet = replySnippet)
    }

    fun sendRichMessage(type: MessageContentType) {
        val chatId = selectedChatId ?: return
        if (isGlobalChat(chatId)) {
            authStatusMessage = "Global chat currently supports text messages for maximum reliability."
            return
        }
        val message = when (type) {
            MessageContentType.IMAGE -> buildOutgoingMessage(type, "Vacation photo", metadata = "1280x720 • 240 KB")
            MessageContentType.VIDEO -> buildOutgoingMessage(type, "Weekend highlight reel", metadata = "00:18 • 4.2 MB")
            MessageContentType.DOCUMENT -> buildOutgoingMessage(type, "Quarterly plan.pdf", metadata = "PDF • 1.1 MB")
            MessageContentType.VOICE -> buildOutgoingMessage(type, "Voice note", metadata = "00:14 • 1.5x")
            MessageContentType.LOCATION -> buildOutgoingMessage(type, "Live location shared", metadata = "Connaught Place, Delhi • 15 min")
            MessageContentType.CONTACT -> buildOutgoingMessage(type, "Priya Mehta", metadata = "+91 90000 12345")
            MessageContentType.POLL -> buildOutgoingMessage(type, "Poll: Saturday meetup?", metadata = "Yes • Maybe • No")
            else -> buildOutgoingMessage(MessageContentType.TEXT, "Quick message")
        }
        appendMessage(chatId, message)
    }

    fun sendPickedMedia(localUri: Uri, type: MessageContentType, durationMs: Long = 0L) {
        val chatId = selectedChatId ?: return
        if (isGlobalChat(chatId)) {
            dismissMediaPickerDialog()
            authStatusMessage = "Global chat currently supports text messages only."
            return
        }
        val fileName = localUri.lastPathSegment?.substringAfterLast('/')?.takeIf { it.isNotBlank() } ?: "attachment"
        mediaActionStatus = "Selected ${type.name.lowercase()}: $fileName"
        dismissMediaPickerDialog()
        if (!backendConnected || reliability.isOfflineMode) {
            queuePendingMedia(chatId = chatId, type = type, localUri = localUri, fileName = fileName, durationMs = durationMs)
            return
        }
        isMediaBusy = true
        mediaUploadProgress = 0
        mediaActionStatus = "Uploading $fileName to Firebase Storage…"
        viewModelScope.launch {
            runCatching {
                var uploadedFileName = fileName
                storageManager.uploadChatAttachmentFlow(
                    localUri = localUri,
                    chatId = chatId,
                    messageId = randomId("media"),
                    type = type,
                    durationMs = durationMs,
                ).collectLatest { progress ->
                    mediaUploadProgress = progress.progressPercent
                    mediaActionStatus = when (progress.stage) {
                        "completed" -> "Upload complete • 100%"
                        else -> "Uploading $fileName • ${progress.progressPercent}%"
                    }
                    progress.result?.let { upload ->
                        uploadedFileName = upload.fileName
                        structuredChatRepository.sendMediaMessage(
                            chatId = chatId,
                            senderId = currentUser.id,
                            senderName = currentUser.name,
                            contentType = type.name.lowercase(),
                            upload = upload,
                            replyToSnippet = replyPreview.orEmpty(),
                        )
                    }
                }
                uploadedFileName
            }.onSuccess { uploadedFileName ->
                isMediaBusy = false
                mediaUploadProgress = 100
                mediaActionStatus = "Uploaded $uploadedFileName"
                replyPreview = null
            }.onFailure { error ->
                isMediaBusy = false
                mediaUploadProgress = 0
                authStatusMessage = "Upload failed: ${error.message ?: "unknown error"}"
                mediaActionStatus = "Queued $fileName for retry"
                queuePendingMedia(chatId = chatId, type = type, localUri = localUri, fileName = fileName, durationMs = durationMs)
            }
        }
    }

    private fun publishPickedStatusMedia(localUri: Uri, type: MessageContentType, privacyMode: String, allowedViewerIds: List<String>, privacyLabel: String) {
        val fileName = localUri.lastPathSegment?.substringAfterLast('/')?.takeIf { it.isNotBlank() } ?: "status"
        dismissStatusMediaPickerDialog()
        mediaActionStatus = "Preparing status: $fileName"
        if (!backendConnected) {
            statusStories.add(
                0,
                StatusStoryUi(
                    id = randomId("status"),
                    authorName = currentUser.name,
                    avatarSeed = currentUser.avatarSeed,
                    content = statusCaptionInput.ifBlank { fileName },
                    timestamp = "Just now",
                    viewed = false,
                    muted = false,
                    contentType = type,
                    privacyLabel = privacyLabel,
                    authorId = currentUser.id,
                    photoUrl = currentUser.photoUrl,
                )
            )
            return
        }
        isMediaBusy = true
        mediaUploadProgress = 0
        viewModelScope.launch {
            runCatching {
                var uploadedFileName = fileName
                storageManager.uploadStatusMediaFlow(localUri, currentUser.id).collectLatest { progress ->
                    mediaUploadProgress = progress.progressPercent
                    mediaActionStatus = if (progress.stage == "completed") {
                        "Status upload complete • 100%"
                    } else {
                        "Uploading status • ${progress.progressPercent}%"
                    }
                    progress.result?.let { upload ->
                        uploadedFileName = upload.fileName
                        structuredChatRepository.publishStatus(
                            FireStatusStory(
                                authorId = currentUser.id,
                                authorName = currentUser.name,
                                contentType = type.name.lowercase(),
                                caption = statusCaptionInput.ifBlank { fileName },
                                mediaUrl = upload.downloadUrl,
                                createdAt = System.currentTimeMillis(),
                                expiresAt = System.currentTimeMillis() + 24L * 60L * 60L * 1000L,
                                visibilityMode = privacyMode,
                                allowedViewerIds = allowedViewerIds,
                            )
                        )
                    }
                }
                uploadedFileName
            }.onSuccess { uploadedFileName ->
                isMediaBusy = false
                mediaUploadProgress = 100
                mediaActionStatus = "Status uploaded: $uploadedFileName"
                addLocalStatusPreview(
                    content = statusCaptionInput.ifBlank { uploadedFileName },
                    contentType = type,
                    mediaUrl = if (type == MessageContentType.TEXT) "" else localUri.toString(),
                    privacyLabel = privacyLabel,
                )
            }.onFailure { error ->
                isMediaBusy = false
                mediaUploadProgress = 0
                authStatusMessage = error.message ?: "Status upload failed."
                mediaActionStatus = "Status upload failed"
            }
        }
    }

    private fun publishTextStatusInternal(privacyMode: String, allowedViewerIds: List<String>, privacyLabel: String) {
        val content = statusCaptionInput.ifBlank { currentUser.about }
        addLocalStatusPreview(content = content, privacyLabel = privacyLabel)
        if (!backendConnected) return
        viewModelScope.launch {
            runCatching {
                structuredChatRepository.publishStatus(
                    FireStatusStory(
                        authorId = currentUser.id,
                        authorName = currentUser.name,
                        contentType = "text",
                        caption = content,
                        createdAt = System.currentTimeMillis(),
                        expiresAt = System.currentTimeMillis() + 24L * 60L * 60L * 1000L,
                        visibilityMode = privacyMode,
                        allowedViewerIds = allowedViewerIds,
                    )
                )
            }.onSuccess {
                mediaActionStatus = "Text status published"
                reliability = reliability.copy(syncBanner = "Status synced live")
            }.onFailure { error ->
                authStatusMessage = error.message ?: "Status publish failed."
            }
        }
    }

    fun publishTextStatus() = prepareTextStatusForAudience()

    fun openStatusViewer(storyId: String) {
        removeExpiredStatuses()
        selectedStatusStory = statusStories.firstOrNull { it.id == storyId }?.let { story ->
            val updated = story.copy(
                viewed = true,
                seenViewerNames = (story.seenViewerNames + currentUser.name).distinct(),
            )
            replaceStatusStory(updated)
            updated
        }
        statusReplyInput = ""
    }

    fun closeStatusViewer() {
        selectedStatusStory = null
        statusReplyInput = ""
    }

    fun reactToStatus(reaction: String) {
        val story = selectedStatusStory ?: return
        val updated = story.copy(reactions = (story.reactions + reaction).distinct())
        selectedStatusStory = updated
        replaceStatusStory(updated)
        mediaActionStatus = "Status reaction sent: $reaction"
    }

    fun replyToStatus() {
        val story = selectedStatusStory ?: return
        val text = statusReplyInput.trim()
        if (text.isBlank()) {
            authStatusMessage = "Type a reply for the status first."
            return
        }
        authStatusMessage = "Reply sent to ${story.authorName}"
        statusReplyInput = ""
    }

    fun toggleStatusMuted(storyId: String) {
        val current = statusStories.firstOrNull { it.id == storyId } ?: return
        val updated = current.copy(muted = !current.muted)
        replaceStatusStory(updated)
        if (selectedStatusStory?.id == storyId) selectedStatusStory = updated
    }

    fun removeExpiredStatuses() {
        if (statusStories.isEmpty()) return
        val filtered = statusStories.filterNot { it.timestamp.contains("Expired", ignoreCase = true) }
        if (filtered.size != statusStories.size) {
            statusStories.clear()
            statusStories.addAll(filtered)
        }
    }

    fun loadOlderMessages() {
        loadedMessageWindow += 25
    }

    fun reportCurrentConversation() {
        val conversation = selectedConversation ?: return
        authStatusMessage = "${conversation.title} reported for review."
    }

    fun blockCurrentConversation() {
        val conversation = selectedConversation ?: return
        blockedUserIds.addAll(conversation.participantIds.filterNot { it == currentUser.id })
        authStatusMessage = "${conversation.title} blocked."
    }

    fun togglePinned(id: String) {
        val target = conversationsState.firstOrNull { it.id == id } ?: return
        val next = !target.pinned
        updateConversation(id) { it.copy(pinned = next) }
        if (isGlobalChat(id)) return
        if (backendConnected) {
            viewModelScope.launch { runCatching { structuredChatRepository.setPinned(id, currentUser.id, next) } }
        }
    }

    fun toggleMuted(id: String) {
        val target = conversationsState.firstOrNull { it.id == id } ?: return
        val next = !target.muted
        updateConversation(id) { it.copy(muted = next) }
        if (isGlobalChat(id)) return
        if (backendConnected) {
            viewModelScope.launch { runCatching { structuredChatRepository.setMuted(id, currentUser.id, next) } }
        }
    }

    fun toggleArchived(id: String) {
        val target = conversationsState.firstOrNull { it.id == id } ?: return
        val next = !target.archived
        updateConversation(id) { it.copy(archived = next, unreadCount = 0) }
        if (isGlobalChat(id)) return
        if (backendConnected) {
            viewModelScope.launch { runCatching { structuredChatRepository.setArchived(id, currentUser.id, next) } }
        }
    }

    fun deleteConversationForCurrentUser(id: String) {
        if (id.isBlank()) return
        if (id !in hiddenConversationIds) {
            hiddenConversationIds.add(id)
            persistHiddenConversationIds()
        }
        conversationsState.removeAll { it.id == id }
        messagesByChat.remove(id)
        LocalChatStore.clearConversationCache(id)
        selectedHomeConversationId = null
        if (selectedChatId == id) {
            selectedChatId = null
            composerText = ""
            replyPreview = null
            messageSearchQuery = ""
        }
        if (backendConnected && !isGlobalChat(id)) {
            viewModelScope.launch { runCatching { structuredChatRepository.setHidden(id, currentUser.id, true) } }
        }
        authStatusMessage = "Chat sirf aapki side se delete hui. Dusri side safe rahegi."
    }

    private fun revealConversationIfHidden(id: String) {
        if (id !in hiddenConversationIds) return
        hiddenConversationIds.remove(id)
        persistHiddenConversationIds()
        if (backendConnected && !isGlobalChat(id)) {
            viewModelScope.launch { runCatching { structuredChatRepository.setHidden(id, currentUser.id, false) } }
        }
    }

    private fun persistHiddenConversationIds() {
        LocalChatStore.saveHiddenConversationIds(currentUser.id, hiddenConversationIds.toSet())
    }

    private fun loadHiddenConversationIdsForCurrentUser() {
        hiddenConversationIds.clear()
        hiddenConversationIds.addAll(LocalChatStore.loadHiddenConversationIds(currentUser.id))
    }

    fun startCall(type: CallType) {
        val conversation = selectedConversation ?: return
        if (conversation.id == GLOBAL_CHAT_ID) {
            authStatusMessage = "Calls are not available in global chat yet."
            return
        }
        activeCall = ActiveCallUi(
            conversationId = conversation.id,
            title = conversation.title,
            type = type,
            startedLabel = if (backendConnected) "Calling…" else "Local call preview",
            videoEnabled = type == CallType.VIDEO,
            direction = CallDirection.OUTGOING,
            reconnecting = false,
            networkLabel = if (chatPreferences.highQualityUploads) "HD" else "Low data",
            canScreenShare = type == CallType.VIDEO,
        )
        if (!backendConnected) return
        val calleeIds = conversation.participantIds.filterNot { it == currentUser.id }
        if (calleeIds.isEmpty()) {
            activeCall = activeCall?.copy(startedLabel = "Call unavailable")
            return
        }
        viewModelScope.launch {
            runCatching {
                callManager.createOutgoingRoom(
                    callerId = currentUser.id,
                    calleeIds = calleeIds,
                    isVideo = type == CallType.VIDEO,
                )
            }.onSuccess { roomId ->
                activeCallRoomId = roomId
            }.onFailure {
                activeCall = activeCall?.copy(startedLabel = "Call room unavailable")
            }
        }
    }

    fun acceptIncomingCall() {
        val roomId = activeCallRoomId ?: return
        val current = activeCall ?: return
        viewModelScope.launch {
            runCatching { callManager.acceptCall(roomId) }
                .onSuccess { activeCall = current.copy(startedLabel = "Connected", direction = CallDirection.INCOMING, reconnecting = false, networkLabel = if (chatPreferences.highQualityUploads) "HD" else "Low data") }
                .onFailure { authStatusMessage = it.message ?: "Could not accept call." }
        }
    }

    fun rejectIncomingCall() {
        val roomId = activeCallRoomId ?: return
        val current = activeCall
        viewModelScope.launch {
            runCatching { callManager.rejectCall(roomId) }
            current?.let {
                pushCallLog(
                    CallRecordUi(
                        id = randomId("call"),
                        contactName = it.title,
                        avatarSeed = it.title.take(1),
                        type = it.type,
                        direction = CallDirection.MISSED,
                        timestamp = "Now",
                        durationLabel = "Missed",
                        photoUrl = selectedConversation?.photoUrl.orEmpty(),
                    )
                )
            }
            activeCall = null
            activeCallRoomId = null
        }
    }

    fun toggleMuteOnCall() {
        val current = activeCall ?: return
        activeCall = current.copy(muted = !current.muted)
    }

    fun toggleSpeakerOnCall() {
        val current = activeCall ?: return
        activeCall = current.copy(speakerOn = !current.speakerOn)
    }

    fun toggleVideoOnCall() {
        val current = activeCall ?: return
        activeCall = current.copy(videoEnabled = !current.videoEnabled)
    }

    fun endActiveCall() {
        val call = activeCall ?: return
        val roomId = activeCallRoomId
        if (backendConnected && roomId != null) {
            viewModelScope.launch { runCatching { callManager.endCall(roomId) } }
        }
        pushCallLog(
            CallRecordUi(
                id = randomId("call"),
                contactName = call.title,
                avatarSeed = call.title.take(1),
                type = call.type,
                direction = call.direction,
                timestamp = "Now",
                durationLabel = if (call.startedLabel.contains("Incoming", ignoreCase = true)) "Answered" else "02:13",
                photoUrl = selectedConversation?.photoUrl.orEmpty(),
            )
        )
        activeCallRoomId = null
        activeCall = null
    }

    private fun pushCallLog(record: CallRecordUi) {
        callLogs.add(0, record)
        LocalChatStore.saveCallLogs(callLogs.toList())
    }

    fun beginVoiceNoteRecording() {
        if (isVoiceRecording) return
        runCatching { voiceRecorder.start() }
            .onSuccess {
                isVoiceRecording = true
                voiceRecordingStartedAtMs = SystemClock.elapsedRealtime()
                voiceRecordingElapsedMs = 0L
                mediaActionStatus = "Recording voice note…"
                voiceRecordingTickerJob?.cancel()
                voiceRecordingTickerJob = viewModelScope.launch {
                    while (isVoiceRecording) {
                        voiceRecordingElapsedMs = (SystemClock.elapsedRealtime() - voiceRecordingStartedAtMs).coerceAtLeast(0L)
                        delay(250)
                    }
                }
            }
            .onFailure { error ->
                authStatusMessage = error.message ?: "Microphone start nahi ho paya."
            }
    }

    fun cancelVoiceNoteRecording() {
        if (!isVoiceRecording) return
        voiceRecordingTickerJob?.cancel()
        voiceRecordingTickerJob = null
        runCatching { voiceRecorder.cancel() }
        isVoiceRecording = false
        voiceRecordingElapsedMs = 0L
        mediaActionStatus = "Voice note cancelled"
    }

    fun finishVoiceNoteRecording() {
        if (!isVoiceRecording) return
        voiceRecordingTickerJob?.cancel()
        voiceRecordingTickerJob = null
        runCatching { voiceRecorder.stop() }
            .onSuccess { result ->
                isVoiceRecording = false
                voiceRecordingElapsedMs = 0L
                mediaActionStatus = "Voice note ready • ${formatDurationCompact(result.durationMs)}"
                sendPickedMedia(
                    localUri = Uri.fromFile(result.file),
                    type = MessageContentType.VOICE,
                    durationMs = result.durationMs,
                )
            }
            .onFailure { error ->
                isVoiceRecording = false
                voiceRecordingElapsedMs = 0L
                authStatusMessage = error.message ?: "Voice note save nahi ho paya."
            }
    }

    fun formattedVoiceRecordingElapsed(): String = formatDurationCompact(voiceRecordingElapsedMs)

    fun addMyStatus() {
        publishTextStatus()
    }

    fun createNewContactChat() {
        openNewChatDialog()
    }

    fun openGlobalChat() {
        currentTab = HomeTab.CHATS
        ensureGlobalConversation()
        openConversation(GLOBAL_CHAT_ID)
    }

    fun submitNewContactChat() {
        val typedIdentifier = normalizeIdentifier(newChatPhoneInput)
        val typedName = newChatNameInput.ifBlank { typedIdentifier.ifBlank { "New contact" } }
        if (typedIdentifier.isBlank() && backendConnected) {
            authStatusMessage = "Enter your friend's username or phone number to create a live private chat."
            return
        }
        if (backendConnected) {
            viewModelScope.launch {
                runCatching {
                    structuredChatRepository.findOrCreatePrivateThreadWithIdentifier(
                        currentUserId = currentUser.id,
                        otherIdentifier = typedIdentifier,
                        fallbackName = typedName,
                    )
                }.onSuccess { (chatId, profile) ->
                    showNewChatDialog = false
                    val connectedName = profile.displayName.ifBlank { profile.username.ifBlank { profile.phoneNumber } }
                    authStatusMessage = "Live private chat connected with $connectedName"
                    openConversation(chatId)
                }.onFailure { error ->
                    authStatusMessage = error.message ?: "Could not create live chat."
                }
            }
            return
        }
        val title = typedName.takeIf { it.isNotBlank() } ?: listOf("Ananya", "Karan", "Nisha", "Dev", "Simran").random()
        val conversation = ConversationUi(
            id = randomId("chat"),
            title = title,
            subtitle = if (typedIdentifier.isNotBlank()) "Tap to start a private conversation with $typedIdentifier" else "Tap to start a private conversation",
            type = ConversationType.PRIVATE,
            avatarSeed = title.take(1),
            participantIds = listOf(currentUser.id, typedIdentifier.ifBlank { title.lowercase(Locale.ROOT) }),
            unreadCount = 0,
            pinned = false,
            archived = false,
            muted = false,
            verified = Random.nextBoolean(),
            goldenVerified = false,
            lastActive = "Now",
        )
        conversationsState.add(0, conversation)
        messagesByChat[conversation.id] = mutableStateListOf(
            ChatMessageUi(
                id = randomId("msg"),
                senderId = typedIdentifier.ifBlank { title.lowercase(Locale.ROOT) },
                senderName = title,
                contentType = MessageContentType.SYSTEM,
                body = "Private chat created. Start messaging now.",
                timestamp = "Now",
                deliveryStatus = MessageDeliveryStatus.DELIVERED,
            )
        )
        showNewChatDialog = false
        openConversation(conversation.id)
    }

    fun createNewGroup() {
        openNewGroupDialog()
    }

    fun submitNewGroup() {
        val title = newGroupTitleInput.ifBlank { "New group" }
        val rawPhones = newGroupPhonesInput
            .split(',', '\n')
            .map { normalizeIdentifier(it.trim()) }
            .filter { it.isNotBlank() }
        if (backendConnected) {
            viewModelScope.launch {
                runCatching {
                    structuredChatRepository.createGroupThreadWithIdentifiers(
                        title = title,
                        creatorId = currentUser.id,
                        memberIdentifiers = rawPhones,
                    )
                }.onSuccess { (chatId, profiles) ->
                    showNewGroupDialog = false
                    authStatusMessage = if (profiles.isEmpty()) {
                        "Group created. Add more registered users later from Firestore-backed contacts."
                    } else {
                        "Live group created with ${profiles.size} members."
                    }
                    openConversation(chatId)
                }.onFailure { error ->
                    authStatusMessage = error.message ?: "Could not create live group."
                }
            }
            return
        }
        val members = rawPhones.ifEmpty { listOf("ananya", "karan", "priya") }
        val conversation = ConversationUi(
            id = randomId("group"),
            title = title,
            subtitle = "Group created successfully",
            type = ConversationType.GROUP,
            avatarSeed = title.take(1),
            participantIds = listOf(currentUser.id) + members,
            unreadCount = 2,
            pinned = false,
            archived = false,
            muted = false,
            verified = false,
            goldenVerified = false,
            lastActive = "Now",
        )
        conversationsState.add(0, conversation)
        messagesByChat[conversation.id] = mutableStateListOf(
            ChatMessageUi(
                id = randomId("msg"),
                senderId = currentUser.id,
                senderName = currentUser.name,
                contentType = MessageContentType.SYSTEM,
                body = "Group created successfully. Start messaging your members now.",
                timestamp = "Now",
                deliveryStatus = MessageDeliveryStatus.READ,
            )
        )
        showNewGroupDialog = false
        openConversation(conversation.id)
    }

    fun linkDesktopDevice() {
        linkedDevices.add(
            0,
            LinkedDeviceUi(
                id = randomId("device"),
                name = "Chrome on Windows",
                lastActive = "Just now",
                location = "New Delhi, India",
            )
        )
    }

    fun unlinkDevice(id: String) {
        linkedDevices.removeAll { it.id == id }
    }

    fun openProfileEditor() {
        selectedUserProfile = null
        profileNameInput = currentUser.name
        profileAboutInput = currentUser.about
        usernameInput = currentUser.username
        if (currentUser.phoneNumber.startsWith("+")) {
            val digitsOnly = currentUser.phoneNumber.drop(1)
            val codeLength = when {
                digitsOnly.length > 12 -> 3
                digitsOnly.length > 10 -> 2
                else -> 2
            }
            countryCodeInput = "+" + digitsOnly.take(codeLength)
            phoneInput = digitsOnly.drop(codeLength)
        } else {
            phoneInput = currentUser.phoneNumber
        }
        showProfileEditor = true
    }

    fun closeProfileEditor() {
        showProfileEditor = false
    }

    fun toggleProfileEditor() {
        if (showProfileEditor) closeProfileEditor() else openProfileEditor()
    }

    fun updateProfilePhoto(localUri: Uri) {
        val targetUid = currentUser.id.ifBlank { FirebaseAuthManager.currentUid().orEmpty() }
        if (targetUid.isBlank()) {
            authStatusMessage = "Profile photo update karne ke liye pehle account me log in karo."
            return
        }

        viewModelScope.launch {
            isProfilePhotoBusy = true
            authStatusMessage = "Uploading profile photo…"
            runCatching { storageManager.uploadProfilePhoto(localUri, targetUid) }
                .onSuccess { upload ->
                    currentUser = currentUser.copy(id = targetUid, photoUrl = upload.downloadUrl)
                    runCatching {
                        structuredChatRepository.upsertUserProfile(
                            currentUserPrivacyProfile(userId = targetUid, photoUrl = upload.downloadUrl)
                        )
                    }
                    saveSession(targetUid)
                    authStatusMessage = "Profile photo updated."
                }
                .onFailure { error ->
                    authStatusMessage = "Profile photo update failed: ${error.message ?: "unknown error"}"
                }
            isProfilePhotoBusy = false
        }
    }

    fun updateProfileBanner(localUri: Uri) {
        val targetUid = currentUser.id.ifBlank { FirebaseAuthManager.currentUid().orEmpty() }
        if (targetUid.isBlank()) {
            authStatusMessage = "Profile banner update karne ke liye pehle account me log in karo."
            return
        }

        viewModelScope.launch {
            isProfileBannerBusy = true
            authStatusMessage = "Uploading profile banner…"
            runCatching { storageManager.uploadProfileBanner(localUri, targetUid) }
                .onSuccess { upload ->
                    currentUser = currentUser.copy(id = targetUid, bannerUrl = upload.downloadUrl)
                    runCatching {
                        structuredChatRepository.upsertUserProfile(
                            currentUserPrivacyProfile(userId = targetUid, bannerUrl = upload.downloadUrl)
                        )
                    }
                    saveSession(targetUid)
                    authStatusMessage = "Profile banner updated."
                }
                .onFailure { error ->
                    authStatusMessage = "Profile banner update failed: ${error.message ?: "unknown error"}"
                }
            isProfileBannerBusy = false
        }
    }

    fun saveProfile() {
        val updatedUser = currentUser.copy(
            name = profileNameInput.ifBlank { currentUser.name },
            about = profileAboutInput.ifBlank { currentUser.about },
            phoneNumber = normalizedPhoneWithCountryCode().ifBlank { currentUser.phoneNumber },
            avatarSeed = profileNameInput.firstOrNull()?.uppercase() ?: currentUser.avatarSeed,
            username = if (currentUser.verified) normalizeUsername(usernameInput.ifBlank { currentUser.username }) else currentUser.username,
        )
        if (backendConnected && updatedUser.id.isNotBlank()) {
            viewModelScope.launch {
                runCatching {
                    structuredChatRepository.upsertUserProfile(
                        currentUserPrivacyProfile(
                            userId = updatedUser.id,
                            displayName = updatedUser.name,
                            phoneNumber = normalizePhone(updatedUser.phoneNumber),
                            username = updatedUser.username,
                            about = updatedUser.about,
                            photoUrl = updatedUser.photoUrl,
                            bannerUrl = updatedUser.bannerUrl,
                            verified = updatedUser.verified,
                            goldenVerified = updatedUser.goldenVerified,
                        )
                    )
                }.onSuccess {
                    currentUser = updatedUser
                    saveSession(currentUser.id)
                    authStatusMessage = "Profile updated successfully."
                }.onFailure { error ->
                    authStatusMessage = if (error is UsernameAlreadyExistsException) {
                        "This username already exists. Choose another username."
                    } else {
                        "Profile update failed: ${error.message ?: "unknown error"}"
                    }
                }
            }
        } else {
            currentUser = updatedUser
            saveSession(currentUser.id)
        }
    }

    fun canEditUsername(): Boolean = currentUser.verified

    fun updateNotificationPreferences(transform: (NotificationPreferencesUi) -> NotificationPreferencesUi) {
        notificationPreferences = transform(notificationPreferences)
        LocalChatStore.saveNotificationPreferences(notificationPreferences)
        reliability = reliability.copy(syncBanner = "Notifications: ${notificationPreferences.customToneLabel} tone")
    }

    fun updateSecurityPreferences(transform: (SecurityPreferencesUi) -> SecurityPreferencesUi) {
        securityPreferences = transform(securityPreferences)
        LocalChatStore.saveSecurityPreferences(securityPreferences)
        reliability = reliability.copy(syncBanner = if (securityPreferences.encryptedLocalCache) "Encrypted local cache active" else "Standard local cache active")
        syncCurrentUserPrivacyPreferences()
    }

    fun updateChatPreferences(transform: (ChatPreferencesUi) -> ChatPreferencesUi) {
        chatPreferences = transform(chatPreferences)
        LocalChatStore.saveChatPreferences(chatPreferences)
    }

    fun updateOfflineMode(enabled: Boolean) {
        reliability = reliability.copy(
            isOfflineMode = enabled,
            syncHealth = if (enabled) "Offline queue active" else "Healthy",
            cacheHealth = if (enabled) "Local-first" else "Warm",
            syncBanner = if (enabled) "Offline mode: messages will retry later" else "Realtime sync active",
        )
    }

    fun syncPendingMessages() {
        if ((!backendConnected && !realtimeEnabledForCurrentSession()) || reliability.isOfflineMode) {
            reliability = reliability.copy(syncHealth = "Waiting for realtime connection")
            return
        }
        val pending = LocalChatStore.loadPending()
        if (pending.isEmpty()) {
            reliability = reliability.copy(queuedMessages = 0, syncHealth = "All caught up")
            return
        }
        viewModelScope.launch {
            var sentCount = 0
            var failedCount = 0
            pending.forEach { item ->
                runCatching {
                    when (item.contentType) {
                        MessageContentType.TEXT -> if (isGlobalChat(item.chatId)) {
                            structuredChatRepository.sendGlobalTextMessage(
                                senderId = currentUser.id,
                                senderName = currentUser.name,
                                text = item.body,
                                replyToSnippet = item.replyToSnippet,
                            )
                        } else {
                            structuredChatRepository.sendTextMessage(
                                chatId = item.chatId,
                                senderId = currentUser.id,
                                senderName = currentUser.name,
                                text = item.body,
                                replyToSnippet = item.replyToSnippet,
                            )
                        }
                        MessageContentType.IMAGE,
                        MessageContentType.VIDEO,
                        MessageContentType.DOCUMENT,
                        MessageContentType.VOICE -> {
                            val localUri = item.metadata.takeIf { it.startsWith("content://") || it.startsWith("file://") }?.let(Uri::parse)
                                ?: error("Missing local media reference for ${item.body}")
                            val upload = storageManager.uploadChatAttachment(
                                localUri = localUri,
                                chatId = item.chatId,
                                messageId = item.localId,
                                type = item.contentType,
                                durationMs = item.durationMs,
                            )
                            structuredChatRepository.sendMediaMessage(
                                chatId = item.chatId,
                                senderId = currentUser.id,
                                senderName = currentUser.name,
                                contentType = item.contentType.name.lowercase(),
                                upload = upload,
                                replyToSnippet = item.replyToSnippet,
                            )
                        }
                        else -> if (isGlobalChat(item.chatId)) {
                            structuredChatRepository.sendGlobalTextMessage(
                                senderId = currentUser.id,
                                senderName = currentUser.name,
                                text = item.body,
                                replyToSnippet = item.replyToSnippet,
                            )
                        } else {
                            structuredChatRepository.sendTextMessage(
                                chatId = item.chatId,
                                senderId = currentUser.id,
                                senderName = currentUser.name,
                                text = item.body,
                                replyToSnippet = item.replyToSnippet,
                            )
                        }
                    }
                }.onSuccess {
                    LocalChatStore.removePending(item.localId)
                    sentCount += 1
                }.onFailure {
                    failedCount += 1
                }
            }
            reliability = reliability.copy(
                queuedMessages = LocalChatStore.loadPending().size,
                syncHealth = when {
                    sentCount > 0 && failedCount == 0 -> "Synced $sentCount queued items"
                    sentCount > 0 -> "Synced $sentCount items, $failedCount still pending"
                    else -> "Queued items still pending"
                },
                syncBanner = if (failedCount == 0) "All queued work synced" else "$failedCount items still waiting",
                retryDashboardLabel = "Sent $sentCount • Pending ${LocalChatStore.loadPending().size}",
            )
        }
    }

    fun backupNow() {
        reliability = reliability.copy(lastBackupLabel = "Just now", syncHealth = "Backup saved successfully", syncBanner = "Backup completed", retryDashboardLabel = "Queue empty")
    }

    fun logout() {
        threadsJob?.cancel()
        messagesJob?.cancel()
        globalMessagesJob?.cancel()
        profilesJob?.cancel()
        statusesJob?.cancel()
        incomingCallsJob?.cancel()
        activeCallRoomId = null
        otpCountdownJob?.cancel()
        otpAuthManager.reset()
        val currentId = currentUser.id
        val wasOtpUser = currentUser.verified || SessionManager.load()?.isOtpUser == true
        val deleteAfterAt = System.currentTimeMillis() + TEMP_ACCOUNT_DELETE_DELAY_MS
        viewModelScope.launch {
            if (currentId.isNotBlank() && backendConnected) {
                runCatching { structuredChatRepository.updatePresence(currentId, false) }
            }
            if (!wasOtpUser && currentId.isNotBlank() && backendConnected) {
                runCatching { structuredChatRepository.scheduleTemporaryAccountDeletion(currentId, deleteAfterAt) }
            }
            runCatching { FirebaseAuthManager.signOut() }
        }
        SessionManager.clear()
        isLoggedIn = false
        loginFlowStep = LoginFlowStep.CHOICE
        isOtpRequested = false
        otpResendSeconds = 0
        otpAttemptCount = 0
        backendConnected = false
        selectedChatId = null
        selectedUserProfile = null
        runCatching { voiceRecorder.cancel() }
        voiceRecordingTickerJob?.cancel()
        isVoiceRecording = false
        voiceRecordingElapsedMs = 0L
        searchQuery = ""
        messageSearchQuery = ""
        composerText = ""
        replyPreview = null
        otpInput = ""
        authPasswordInput = ""
        currentTab = HomeTab.CHATS
        authStatusMessage = if (wasOtpUser) {
            "Logged out. Your permanent account is safe and can be used again later."
        } else {
            "Temporary account logged out. 30 days tak dubara login nahi kiya to account delete ho jayega."
        }
        conversationsState.clear()
        messagesByChat.clear()
        statusStories.clear()
        callLogs.clear()
        linkedDevices.clear()
        contactProfiles.clear()
        reliability = ReliabilityUi()
        lastObservedGlobalMessageId = null
        currentUser = currentUser.copy(
            id = "me",
            name = "You",
            phoneNumber = "",
            about = "Hey there! I am on ChitChat.",
            avatarSeed = "Y",
            online = true,
            lastSeen = "online",
            verified = false,
            goldenVerified = false,
            username = "",
            photoUrl = "",
        )
        usernameInput = ""
        emailInput = ""
        phoneInput = ""
        countryCodeInput = "+91"
        profileNameInput = ""
    }

    private fun loadStoredPreferences() {
        notificationPreferences = LocalChatStore.loadNotificationPreferences()
        securityPreferences = LocalChatStore.loadSecurityPreferences()
        chatPreferences = LocalChatStore.loadChatPreferences()
        callLogs.clear()
        callLogs.addAll(LocalChatStore.loadCallLogs())
    }

    private fun restoreExistingSessionOrSeedDemo() {
        val saved = SessionManager.load()
        if (saved == null) {
            authStatusMessage = "Username aur password se temporary account banao, ya apne purane temporary account me log in karo."
            return
        }
        applySavedSession(saved)
        loadHiddenConversationIdsForCurrentUser()
        val cachedConversations = LocalChatStore.loadConversations()
        conversationsState.clear()
        conversationsState.addAll(cachedConversations)
        messagesByChat.clear()
        val cacheIds = (cachedConversations.map { it.id } + GLOBAL_CHAT_ID).distinct()
        messagesByChat.putAll(LocalChatStore.loadAllCachedMessages(cacheIds).mapValues { it.value.toMutableStateList() })
        ensureGlobalConversation()
        reliability = reliability.copy(queuedMessages = LocalChatStore.loadPending().size, retryDashboardLabel = "Pending ${LocalChatStore.loadPending().size}")
        viewModelScope.launch {
            val uid = FirebaseAuthManager.currentUid().orEmpty()
            if (uid.isNotBlank()) {
                currentUser = currentUser.copy(id = uid)
                persistProfileAndBind(uid)
            } else {
                backendConnected = false
                authStatusMessage = "Welcome back ${currentUser.name}. Local session restored. Agar realtime sync reconnect na ho to dubara sign in kar lo."
            }
        }
    }

    private fun applySavedSession(saved: SavedSession) {
        usernameInput = saved.username.ifBlank { saved.displayName }
        emailInput = FirebaseAuthManager.currentEmail().trim().lowercase(Locale.ROOT)
        profileNameInput = saved.displayName.ifBlank { saved.username }
        profileAboutInput = saved.about.ifBlank { "Hey there! I am on ChitChat." }
        if (saved.phoneNumber.startsWith("+")) {
            val digitsOnly = saved.phoneNumber.drop(1)
            val codeLength = when {
                digitsOnly.length > 12 -> 3
                digitsOnly.length > 10 -> 2
                else -> 2
            }
            countryCodeInput = "+" + digitsOnly.take(codeLength)
            phoneInput = digitsOnly.drop(codeLength)
        } else {
            phoneInput = saved.phoneNumber
        }
        currentUser = currentUser.copy(
            id = saved.uid.ifBlank { currentUser.id },
            name = profileNameInput.ifBlank { usernameInput },
            phoneNumber = saved.phoneNumber,
            about = profileAboutInput,
            avatarSeed = profileNameInput.firstOrNull()?.uppercase() ?: currentUser.avatarSeed,
            verified = saved.isOtpUser,
            goldenVerified = saved.goldenVerified,
            username = normalizeUsername(usernameInput),
            photoUrl = sanitizeSavedPhotoUrl(saved.photoUrl),
            bannerUrl = sanitizeSavedPhotoUrl(saved.bannerUrl),
        )
        isLoggedIn = true
        loginFlowStep = LoginFlowStep.CHOICE
        loadHiddenConversationIdsForCurrentUser()
        authStatusMessage = "Welcome back ${currentUser.name}. Aap is device par pehle se logged in ho."
    }

    private fun saveSession(uid: String) {
        persistHiddenConversationIds()
        SessionManager.save(
            SavedSession(
                uid = uid,
                username = normalizeUsername(usernameInput.ifBlank { currentUser.username.ifBlank { currentUser.name } }),
                displayName = profileNameInput.ifBlank { currentUser.name },
                phoneNumber = resolvedProfilePhoneNumber(),
                about = profileAboutInput.ifBlank { currentUser.about },
                isOtpUser = currentUser.verified,
                goldenVerified = currentUser.goldenVerified,
                photoUrl = currentUser.photoUrl,
                bannerUrl = currentUser.bannerUrl,
            )
        )
    }

    private fun sanitizeSavedPhotoUrl(raw: String): String {
        val trimmed = raw.trim()
        return if (trimmed.startsWith("content://") || trimmed.startsWith("file://")) "" else trimmed
    }

    private fun onOtpLoginSuccess(phoneNumber: String) {
        otpCountdownJob?.cancel()
        otpResendSeconds = 0
        isAuthBusy = false
        isOtpRequested = true
        isLoggedIn = true
        loginFlowStep = LoginFlowStep.CHOICE
        val defaultName = profileNameInput.ifBlank { "User ${phoneNumber.takeLast(4)}" }
        val requestedUsername = normalizeUsername(usernameInput.ifBlank { "user${phoneNumber.takeLast(6)}" })
        authStatusMessage = "Phone verified successfully. Connecting Firebase chat…"
        viewModelScope.launch {
            val uid = FirebaseAuthManager.currentUid()
            if (uid == null) {
                backendConnected = false
                authStatusMessage = "Phone verified, but Firebase user session is missing."
                return@launch
            }
            val defaultUsername = ensureAvailableUsername(requestedUsername, uid)
            profileNameInput = defaultName
            usernameInput = defaultUsername
            currentUser = currentUser.copy(
                id = uid,
                name = defaultName,
                phoneNumber = phoneNumber,
                about = profileAboutInput.ifBlank { currentUser.about },
                avatarSeed = defaultName.firstOrNull()?.uppercase() ?: currentUser.avatarSeed,
                verified = true,
                goldenVerified = currentUser.goldenVerified,
                username = defaultUsername,
            )
            persistProfileAndBind(uid)
        }
    }

    private suspend fun persistProfileAndBind(
        uid: String,
        rollbackAnonymousAuthOnUsernameConflict: Boolean = false,
    ): Boolean {
        val existingProfile = runCatching { structuredChatRepository.getUserProfile(uid) }.getOrNull()
        val preservedGoldenVerified = existingProfile?.goldenVerified ?: currentUser.goldenVerified
        val preservedBannerUrl = currentUser.bannerUrl.ifBlank { existingProfile?.bannerUrl.orEmpty() }
        if (currentUser.goldenVerified != preservedGoldenVerified || currentUser.bannerUrl != preservedBannerUrl) {
            currentUser = currentUser.copy(goldenVerified = preservedGoldenVerified, bannerUrl = preservedBannerUrl)
        }
        val result = runCatching {
            structuredChatRepository.upsertUserProfile(
                currentUserPrivacyProfile(
                    userId = uid,
                    displayName = profileNameInput.ifBlank { currentUser.name },
                    phoneNumber = resolvedProfilePhoneNumber(),
                    username = normalizeUsername(usernameInput.ifBlank { currentUser.username.ifBlank { currentUser.name } }),
                    about = profileAboutInput.ifBlank { currentUser.about },
                    photoUrl = currentUser.photoUrl,
                    bannerUrl = currentUser.bannerUrl,
                    verified = currentUser.verified,
                    goldenVerified = preservedGoldenVerified,
                    temporaryAuthEmail = resolveTemporaryAuthEmail(existingProfile),
                )
            )
            structuredChatRepository.updatePresence(uid, true)
        }
        result.onSuccess {
            saveSession(uid)
            loadHiddenConversationIdsForCurrentUser()
            backendConnected = BuildConfig.REALTIME_BACKEND_ENABLED
            if (backendConnected) {
                viewModelScope.launch { runCatching { fcmTokenRepository.syncCurrentToken(uid) } }
            }
            if (realtimeEnabledForCurrentSession()) {
                bindGlobalMessages()
            }
            ensureGlobalConversation()
            reliability = reliability.copy(queuedMessages = LocalChatStore.loadPending().size, retryDashboardLabel = "Pending ${LocalChatStore.loadPending().size}")
            authStatusMessage = if (backendConnected) "Realtime Firestore sync connected." else "Logged in, but realtime backend is disabled for this build."
            if (backendConnected) {
                bindThreads(uid)
                bindIncomingCalls(uid)
                syncPendingMessages()
            }
        }.onFailure { error ->
            backendConnected = false
            authStatusMessage = if (error is UsernameAlreadyExistsException) {
                "This username already exists. Choose another username."
            } else {
                "Logged in, but Firestore sync failed: ${error.message ?: "unknown error"}"
            }
            if (rollbackAnonymousAuthOnUsernameConflict && error is UsernameAlreadyExistsException) {
                runCatching { structuredChatRepository.deleteUserProfile(uid) }
                runCatching { FirebaseAuthManager.deleteCurrentUserIfPossible() }
                runCatching { FirebaseAuthManager.signOut() }
            }
        }
        return result.isSuccess
    }

    private fun bindThreads(uid: String) {
        threadsJob?.cancel()
        threadsJob = viewModelScope.launch {
            runCatching {
                structuredChatRepository.observeThreads(uid).collectLatest { threads ->
                    refreshThreads(threads)
                }
            }.onFailure { error ->
                backendConnected = false
                authStatusMessage = "Thread sync stopped: ${error.message ?: "unknown error"}"
            }
        }
    }

    private fun bindGlobalMessages() {
        globalMessagesJob?.cancel()
        globalMessagesJob = viewModelScope.launch {
            runCatching {
                structuredChatRepository.observeGlobalMessages().collectLatest { messages ->
                    val mapped = messages.sortedBy { it.createdAt }.map(::mapMessageToUi)
                    messagesByChat[GLOBAL_CHAT_ID] = mapped.toMutableStateList()
                    LocalChatStore.cacheMessages(GLOBAL_CHAT_ID, mapped)
                    val currentGlobal = conversationsState.firstOrNull { it.id == GLOBAL_CHAT_ID }
                    val latestIncoming = messages.lastOrNull { it.senderId != currentUser.id }
                    val latestMessageId = messages.lastOrNull()?.id
                    if (lastObservedGlobalMessageId == null) {
                        lastObservedGlobalMessageId = latestMessageId
                    }
                    val unreadCount = when {
                        selectedChatId == GLOBAL_CHAT_ID -> 0
                        latestIncoming == null -> currentGlobal?.unreadCount ?: 0
                        latestMessageId.isNullOrBlank() -> currentGlobal?.unreadCount ?: 0
                        latestMessageId == lastObservedGlobalMessageId -> currentGlobal?.unreadCount ?: 0
                        else -> (currentGlobal?.unreadCount ?: 0) + 1
                    }
                    lastObservedGlobalMessageId = latestMessageId ?: lastObservedGlobalMessageId
                    ensureGlobalConversation(unreadCountOverride = unreadCount)
                    if (selectedChatId == GLOBAL_CHAT_ID) {
                        messages.filter { it.senderId != currentUser.id }.forEach { incoming ->
                            runCatching {
                                structuredChatRepository.markGlobalDelivered(incoming.id, currentUser.id)
                                if (securityPreferences.readReceipts) {
                                    structuredChatRepository.markGlobalRead(incoming.id, currentUser.id)
                                }
                            }
                        }
                    }
                }
            }.onFailure { error ->
                authStatusMessage = "Global chat sync stopped: ${error.message ?: "unknown error"}"
            }
        }
    }

    private fun bindMessages(chatId: String) {
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            runCatching {
                structuredChatRepository.observeMessages(chatId).collectLatest { messages ->
                    val mapped = messages.sortedBy { it.createdAt }.map(::mapMessageToUi)
                    messagesByChat[chatId] = mapped.toMutableStateList()
                    LocalChatStore.cacheMessages(chatId, mapped)
                    messages.filter { it.senderId != currentUser.id }.forEach { incoming ->
                        runCatching {
                            structuredChatRepository.markDelivered(chatId, incoming.id, currentUser.id)
                            if (securityPreferences.readReceipts) {
                                structuredChatRepository.markRead(chatId, incoming.id, currentUser.id)
                            } else {
                                structuredChatRepository.clearUnreadCount(chatId, currentUser.id)
                            }
                        }
                    }
                }
            }.onFailure { error ->
                authStatusMessage = "Message sync stopped: ${error.message ?: "unknown error"}"
            }
        }
    }

    private fun refreshThreads(threads: List<FireChatThread>) {
        val unreadThreadIds = threads.filter { (it.unreadCounts[currentUser.id] ?: 0L) > 0L }.map { it.id }
        val restoredHiddenIds = hiddenConversationIds.intersect(unreadThreadIds.toSet())
        if (restoredHiddenIds.isNotEmpty()) {
            hiddenConversationIds.removeAll(restoredHiddenIds)
            persistHiddenConversationIds()
        }
        val existingGlobal = conversationsState.firstOrNull { it.id == GLOBAL_CHAT_ID }
        val globalConversation = buildGlobalConversation(unreadCountOverride = existingGlobal?.unreadCount)
        conversationsState.clear()
        conversationsState.add(globalConversation)
        conversationsState.addAll(threads.map(::mapThreadToUi).filterNot { it.id == GLOBAL_CHAT_ID })
        LocalChatStore.cacheConversations(conversationsState.toList())
        selectedChatId?.let { chatId ->
            if (conversationsState.any { it.id == chatId }) {
                composerText = LocalChatStore.loadDraft(chatId)
                bindMessages(chatId)
            } else {
                selectedChatId = null
                composerText = ""
            }
        }
        val participantIds = threads.flatMap { it.participantIds }.filterNot { it == currentUser.id }.distinct()
        bindProfiles(participantIds)
        loadStatusesFromThreads(threads)
    }

    private fun loadStatusesFromThreads(threads: List<FireChatThread>) {
        val contactIds = threads.flatMap { it.participantIds }.filterNot { it == currentUser.id }.distinct()
        statusesJob?.cancel()
        if (contactIds.isEmpty()) {
            statusStories.clear()
            return
        }
        statusesJob = viewModelScope.launch {
            runCatching {
                structuredChatRepository.observeStatuses(currentUser.id, contactIds).collectLatest { stories ->
                    statusStories.clear()
                    statusStories.addAll(stories.map(::mapStatusToUi))
                }
            }
        }
    }

    private fun mapThreadToUi(thread: FireChatThread): ConversationUi {
        val typingNames = thread.typingUsers.filterKeys { it != currentUser.id }.values.distinct()
        val otherProfile = if (thread.type == "group") null else thread.participantIds.firstOrNull { it != currentUser.id }?.let { contactProfiles[it] }
        val title = if (thread.type == "group") {
            thread.title.ifBlank { "New group" }
        } else {
            otherProfile?.displayName?.ifBlank { otherProfile.username.ifBlank { thread.title.ifBlank { "Private chat" } } }
                ?: thread.title.ifBlank { "Private chat" }
        }
        val subtitle = when {
            typingNames.isNotEmpty() -> if (typingNames.size == 1) "${typingNames.first()} typing…" else "${typingNames.first()} +${typingNames.size - 1} typing…"
            thread.lastMessageText.isNotBlank() -> thread.lastMessageText
            else -> "Start chatting"
        }
        return ConversationUi(
            id = thread.id,
            title = title,
            subtitle = subtitle,
            type = if (thread.type == "group") ConversationType.GROUP else ConversationType.PRIVATE,
            avatarSeed = (otherProfile?.displayName ?: thread.title).take(1).ifBlank { "C" },
            participantIds = thread.participantIds,
            unreadCount = (thread.unreadCounts[currentUser.id] ?: 0L).toInt(),
            pinned = thread.pinnedFor.contains(currentUser.id),
            archived = thread.archivedFor.contains(currentUser.id),
            muted = thread.mutedFor.contains(currentUser.id),
            verified = if (thread.type == "group") false else (otherProfile?.verified ?: true),
            goldenVerified = if (thread.type == "group") false else (otherProfile?.goldenVerified == true),
            lastActive = formatTime(thread.lastMessageAt.takeIf { it > 0 } ?: thread.updatedAt),
            photoUrl = if (thread.type == "group") thread.photoUrl else otherProfile?.photoUrl.orEmpty(),
        )
    }

    private fun resolveOutgoingDeliveryStatus(message: FireMessage): MessageDeliveryStatus {
        val recipientIds = resolveMessageRecipientIds(message)
        val deliveredRecipientIds = recipientIds.filter { it in message.deliveredTo }
        val readEligibleRecipientIds = recipientIds.filter { recipientId ->
            contactProfiles[recipientId]?.readReceiptsEnabled != false
        }
        return when {
            readEligibleRecipientIds.isNotEmpty() && readEligibleRecipientIds.all { it in message.readBy } -> MessageDeliveryStatus.READ
            deliveredRecipientIds.isNotEmpty() -> MessageDeliveryStatus.DELIVERED
            else -> MessageDeliveryStatus.SENT
        }
    }

    private fun resolveMessageRecipientIds(message: FireMessage): List<String> {
        if (message.chatId == "global_chat") {
            return message.deliveredTo.filterNot { it == currentUser.id }.distinct()
        }
        val knownParticipants = conversationsState
            .firstOrNull { it.id == message.chatId }
            ?.participantIds
            .orEmpty()
            .filterNot { it == currentUser.id }
            .distinct()
        if (knownParticipants.isNotEmpty()) return knownParticipants
        return (message.deliveredTo + message.readBy)
            .filterNot { it == currentUser.id }
            .distinct()
    }

    private fun mapMessageToUi(message: FireMessage): ChatMessageUi {
        val type = when (message.type.lowercase(Locale.ROOT)) {
            "image" -> MessageContentType.IMAGE
            "video" -> MessageContentType.VIDEO
            "document" -> MessageContentType.DOCUMENT
            "voice" -> MessageContentType.VOICE
            "location" -> MessageContentType.LOCATION
            "contact" -> MessageContentType.CONTACT
            "poll" -> MessageContentType.POLL
            "system" -> MessageContentType.SYSTEM
            else -> MessageContentType.TEXT
        }
        val metadata = buildString {
            if (message.forwardedFromName.isNotBlank()) append("Forwarded from ${message.forwardedFromName}")
            if (message.mimeType.isNotBlank()) {
                if (isNotBlank()) append(" • ")
                append(message.mimeType)
            }
            if (message.fileSizeBytes > 0) {
                if (isNotBlank()) append(" • ")
                append("${message.fileSizeBytes / 1024} KB")
            }
            if (message.durationMs > 0) {
                if (isNotBlank()) append(" • ")
                append("${message.durationMs / 1000}s")
            }
            if (message.editedAt > 0) {
                if (isNotBlank()) append(" • ")
                append("edited")
            }
        }
        val deliveryStatus = if (message.senderId == currentUser.id) {
            resolveOutgoingDeliveryStatus(message)
        } else {
            when {
                securityPreferences.readReceipts && message.readBy.contains(currentUser.id) -> MessageDeliveryStatus.READ
                message.deliveredTo.contains(currentUser.id) -> MessageDeliveryStatus.DELIVERED
                else -> MessageDeliveryStatus.SENT
            }
        }
        return ChatMessageUi(
            id = message.id,
            senderId = message.senderId,
            senderName = message.senderName.ifBlank { "User" },
            contentType = type,
            body = if (message.deletedForEveryone) "This message was deleted" else message.text.ifBlank { message.fileName.ifBlank { type.name.lowercase().replaceFirstChar { ch -> ch.uppercase() } } },
            timestamp = formatTime(message.createdAt),
            deliveryStatus = deliveryStatus,
            metadata = metadata,
            replyToSnippet = message.replyToSnippet,
            reactions = message.reactions.keys.toList(),
            starred = currentUser.id in message.starredBy,
            mediaUrl = message.mediaUrl,
        )
    }

    private fun mapStatusToUi(story: FireStatusStory): StatusStoryUi {
        val mappedType = when (story.contentType.lowercase(Locale.ROOT)) {
            "image" -> MessageContentType.IMAGE
            "video" -> MessageContentType.VIDEO
            "voice" -> MessageContentType.VOICE
            else -> MessageContentType.TEXT
        }
        val authorLabel = story.authorName.ifBlank { "Story" }
        val privacyLabel = when (story.visibilityMode.lowercase()) {
            "selected" -> "Selected (${story.allowedViewerIds.size})"
            else -> if (story.allowedViewerIds.isEmpty()) "Share with everyone" else "Everyone in home (${story.allowedViewerIds.size})"
        }
        return StatusStoryUi(
            id = story.id,
            authorName = authorLabel,
            avatarSeed = authorLabel.take(1).ifBlank { "S" },
            content = story.caption.ifBlank { if (story.contentType == "text") "New status" else "${story.contentType.replaceFirstChar { ch -> ch.uppercase() }} status" },
            timestamp = formatTime(story.createdAt),
            viewed = currentUser.id in story.viewerIds,
            muted = false,
            contentType = mappedType,
            mediaUrl = story.mediaUrl,
            seenViewerNames = story.viewerIds.take(6).map { viewerId ->
                when (viewerId) {
                    currentUser.id -> "You"
                    else -> contactProfiles[viewerId]?.displayName?.ifBlank { contactProfiles[viewerId]?.username.orEmpty() }.orEmpty().ifBlank { viewerId }
                }
            },
            reactions = emptyList(),
            privacyLabel = privacyLabel,
            authorId = story.authorId,
            photoUrl = contactProfiles[story.authorId]?.photoUrl.orEmpty(),
        )
    }

    private fun addLocalStatusPreview(
        content: String,
        contentType: MessageContentType = MessageContentType.TEXT,
        mediaUrl: String = "",
        privacyLabel: String = "Share with everyone",
    ) {
        statusStories.removeAll { it.authorId == currentUser.id }
        statusStories.add(
            0,
            StatusStoryUi(
                id = randomId("status"),
                authorName = currentUser.name,
                avatarSeed = currentUser.avatarSeed,
                content = content,
                timestamp = "Just now",
                viewed = true,
                muted = false,
                contentType = contentType,
                mediaUrl = mediaUrl,
                seenViewerNames = listOf("You"),
                privacyLabel = privacyLabel,
                authorId = currentUser.id,
                photoUrl = currentUser.photoUrl,
            )
        )
    }


    private fun replaceStatusStory(updated: StatusStoryUi) {
        val index = statusStories.indexOfFirst { it.id == updated.id }
        if (index >= 0) {
            statusStories[index] = updated
        }
    }

    fun selectedConversationHeaderStatus(): String {
        val conversation = selectedConversation ?: return ""
        if (conversation.id == GLOBAL_CHAT_ID) {
            return "Public room • everyone can discuss here"
        }
        val updatedConversation = conversationsState.firstOrNull { it.id == conversation.id } ?: conversation
        if (updatedConversation.subtitle.contains("typing", ignoreCase = true)) {
            return updatedConversation.subtitle
        }
        if (conversation.type == ConversationType.GROUP) {
            val onlineCount = conversation.participantIds.count { id -> id != currentUser.id && (contactProfiles[id]?.online == true) }
            return if (onlineCount > 0) "${conversation.participantIds.size} participants • $onlineCount online" else "${conversation.participantIds.size} participants"
        }
        val otherId = conversation.participantIds.firstOrNull { it != currentUser.id }
        val profile = otherId?.let { contactProfiles[it] }
        return when {
            profile?.online == true && securityPreferences.showOnlinePresence -> "online"
            profile != null && profile.lastSeenAt > 0L && securityPreferences.showLastSeen -> "last seen ${formatTime(profile.lastSeenAt)}"
            else -> "private chat"
        }
    }

    private fun bindProfiles(userIds: List<String>) {
        profilesJob?.cancel()
        statusesJob?.cancel()
        val targetIds = userIds.filter { it.isNotBlank() }.distinct().take(10)
        if (targetIds.isEmpty()) return
        profilesJob = viewModelScope.launch {
            runCatching {
                structuredChatRepository.observeUserProfiles(targetIds).collectLatest { profiles ->
                    profiles.forEach { profile -> contactProfiles[profile.id] = profile }
                    refreshUiFromProfiles()
                }
            }
        }
    }

    private fun refreshUiFromProfiles() {
        for (index in conversationsState.indices) {
            val conversation = conversationsState[index]
            if (conversation.type != ConversationType.PRIVATE) continue
            val otherId = conversation.participantIds.firstOrNull { it != currentUser.id } ?: continue
            val profile = contactProfiles[otherId] ?: continue
            val resolvedTitle = profile.displayName.ifBlank {
                profile.username.ifBlank { profile.phoneNumber.ifBlank { conversation.title } }
            }
            conversationsState[index] = conversation.copy(
                title = resolvedTitle,
                avatarSeed = resolvedTitle.take(1).ifBlank { conversation.avatarSeed },
                verified = profile.verified,
                goldenVerified = profile.goldenVerified,
                photoUrl = profile.photoUrl,
            )
        }
        for (index in statusStories.indices) {
            val story = statusStories[index]
            if (story.authorId.isBlank()) continue
            val profile = contactProfiles[story.authorId] ?: continue
            val resolvedName = profile.displayName.ifBlank {
                profile.username.ifBlank { story.authorName }
            }
            statusStories[index] = story.copy(
                authorName = resolvedName,
                avatarSeed = resolvedName.take(1).ifBlank { story.avatarSeed },
                photoUrl = profile.photoUrl,
            )
        }
        selectedUserProfile?.let { openProfile ->
            val updatedProfile = contactProfiles[openProfile.id]
            if (updatedProfile != null) {
                selectedUserProfile = updatedProfile
            }
        }
    }

    private fun bindIncomingCalls(uid: String) {
        incomingCallsJob?.cancel()
        incomingCallsJob = viewModelScope.launch {
            runCatching {
                callManager.observeIncomingRooms(uid).collectLatest { rooms ->
                    val ringing = rooms.firstOrNull { it.state == "ringing" }
                    if (ringing != null && activeCall == null) {
                        activeCallRoomId = ringing.roomId
                        activeCall = ActiveCallUi(
                            conversationId = ringing.roomId,
                            title = if (ringing.isVideo) "Incoming video call" else "Incoming voice call",
                            type = if (ringing.isVideo) CallType.VIDEO else CallType.AUDIO,
                            startedLabel = "Incoming…",
                            videoEnabled = ringing.isVideo,
                            direction = CallDirection.INCOMING,
                        )
                    }
                }
            }
        }
    }

    fun tryOpenConversation(chatId: String): Boolean {
        if (!isLoggedIn) return false
        val exists = conversationsState.any { it.id == chatId }
        if (!exists) return false
        currentTab = HomeTab.CHATS
        openConversation(chatId)
        return true
    }

    private fun queuePendingMedia(chatId: String, type: MessageContentType, localUri: Uri, fileName: String, durationMs: Long = 0L) {
        val localId = randomId("pending")
        LocalChatStore.upsertPending(
            PendingMessageCache(
                localId = localId,
                chatId = chatId,
                contentType = type,
                body = fileName,
                metadata = localUri.toString(),
                replyToSnippet = replyPreview.orEmpty(),
                durationMs = durationMs,
            )
        )
        appendMessage(
            chatId,
            ChatMessageUi(
                id = localId,
                senderId = currentUser.id,
                senderName = currentUser.name,
                contentType = type,
                body = fileName,
                timestamp = "Queued",
                deliveryStatus = MessageDeliveryStatus.QUEUED,
                metadata = when {
                    durationMs > 0L -> "${formatDurationCompact(durationMs)} • Waiting to upload"
                    else -> "Waiting to upload"
                },
                replyToSnippet = replyPreview.orEmpty(),
            )
        )
        replyPreview = null
        reliability = reliability.copy(
            queuedMessages = LocalChatStore.loadPending().size,
            syncHealth = if (backendConnected) "Upload queued for retry" else "Queued locally until Firebase reconnects",
        )
    }

    private fun queuePendingText(chatId: String, text: String, replySnippet: String, appendToUi: Boolean = true): String {
        val localId = randomId("pending")
        val pending = PendingMessageCache(
            localId = localId,
            chatId = chatId,
            contentType = MessageContentType.TEXT,
            body = text,
            replyToSnippet = replySnippet,
        )
        LocalChatStore.upsertPending(pending)
        if (appendToUi) {
            appendMessage(
                chatId,
                ChatMessageUi(
                    id = localId,
                    senderId = currentUser.id,
                    senderName = currentUser.name,
                    contentType = MessageContentType.TEXT,
                    body = text,
                    timestamp = "Queued",
                    deliveryStatus = MessageDeliveryStatus.QUEUED,
                    replyToSnippet = replySnippet,
                )
            )
        }
        reliability = reliability.copy(
            queuedMessages = LocalChatStore.loadPending().size,
            syncHealth = if (backendConnected) "Queued locally, waiting to resend" else "Working offline",
        )
        LocalChatStore.saveDraft(chatId, "")
        return localId
    }

    private fun startOtpCountdown(seconds: Int) {
        otpCountdownJob?.cancel()
        otpResendSeconds = seconds
        otpCountdownJob = viewModelScope.launch {
            while (otpResendSeconds > 0) {
                delay(1000)
                otpResendSeconds -= 1
            }
        }
    }

    private fun normalizedPhoneWithCountryCode(): String {
        val digits = phoneInput.filter(Char::isDigit)
        if (digits.isBlank()) return ""
        val country = countryCodeInput.ifBlank { "+91" }.let { if (it.startsWith("+")) it else "+$it" }
        return normalizePhone(country + digits)
    }

    private fun isPhoneNumberValid(): Boolean {
        val digits = phoneInput.filter(Char::isDigit)
        val countryDigits = countryCodeInput.filter(Char::isDigit)
        return digits.length in 8..15 && countryDigits.length in 1..4
    }

    private fun normalizePhone(raw: String): String = raw.filter { it.isDigit() || it == '+' }
    private fun resolveTemporaryAuthEmail(existingProfile: FireUserProfile? = null): String {
        val currentEmail = FirebaseAuthManager.currentEmail().trim().lowercase(Locale.ROOT)
        if (currentEmail.isNotBlank()) return currentEmail
        val savedProfileEmail = existingProfile?.temporaryAuthEmail?.trim()?.lowercase(Locale.ROOT).orEmpty()
        if (savedProfileEmail.isNotBlank()) return savedProfileEmail
        return if (!currentUser.verified && resolvedProfilePhoneNumber().isBlank()) {
            FirebaseAuthManager.temporaryEmailForUsername(usernameInput.ifBlank { currentUser.username.ifBlank { currentUser.name } })
        } else {
            ""
        }
    }

    private suspend fun resolveTemporaryProfileForLogin(username: String): FireUserProfile? {
        val hadExistingSession = FirebaseAuthManager.currentUid().isNullOrBlank().not()
        val createdLookupSession = !hadExistingSession
        if (createdLookupSession) {
            runCatching { FirebaseAuthManager.ensureAnonymousSession() }
        }
        val profile = runCatching { structuredChatRepository.findUserByUsername(username) }.getOrNull()
        if (createdLookupSession) {
            runCatching { FirebaseAuthManager.deleteAnonymousUserIfNeeded() }
            runCatching { FirebaseAuthManager.signOut() }
        }
        return profile
    }


    private fun resolvedProfilePhoneNumber(): String {
        return if (currentUser.verified) {
            normalizedPhoneWithCountryCode().ifBlank { normalizePhone(currentUser.phoneNumber) }
        } else {
            normalizePhone(currentUser.phoneNumber)
        }
    }


    private suspend fun ensureAvailableUsername(baseUsername: String, excludingUserId: String = ""): String {
        val normalizedBase = normalizeUsername(baseUsername).ifBlank { "user${System.currentTimeMillis().toString().takeLast(6)}" }
        val baseAvailable = runCatching {
            structuredChatRepository.isUsernameAvailable(normalizedBase, excludingUserId)
        }.getOrDefault(true)
        if (baseAvailable) return normalizedBase
        for (suffix in 1..9999) {
            val candidate = normalizeUsername("$normalizedBase$suffix")
            val available = runCatching {
                structuredChatRepository.isUsernameAvailable(candidate, excludingUserId)
            }.getOrDefault(false)
            if (available) return candidate
        }
        return normalizeUsername(normalizedBase + Random.nextInt(10000, 99999))
    }

    private fun normalizeUsername(raw: String): String {
        return raw
            .trim()
            .removePrefix("@")
            .lowercase(Locale.ROOT)
            .filter { it.isLetterOrDigit() || it == '_' || it == '.' }
    }

    private fun normalizeIdentifier(raw: String): String {
        val trimmed = raw.trim()
        return if (trimmed.any { it.isDigit() } || trimmed.startsWith("+")) normalizePhone(trimmed) else normalizeUsername(trimmed)
    }

    private fun formatTime(epochMillis: Long): String {
        if (epochMillis <= 0L) return "Now"
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(epochMillis))
    }

    private fun formatDurationCompact(durationMs: Long): String {
        val totalSeconds = (durationMs / 1000L).coerceAtLeast(0L)
        val minutes = totalSeconds / 60L
        val seconds = totalSeconds % 60L
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    private fun appendMessage(chatId: String, message: ChatMessageUi) {
        val list = messagesByChat.getOrPut(chatId) { mutableStateListOf() }
        list.add(message)
        updateConversation(chatId) {
            it.copy(
                subtitle = when (message.contentType) {
                    MessageContentType.TEXT -> message.body
                    MessageContentType.IMAGE -> "📷 ${message.body}"
                    MessageContentType.VIDEO -> "🎥 ${message.body}"
                    MessageContentType.DOCUMENT -> "📄 ${message.body}"
                    MessageContentType.VOICE -> "🎤 ${message.body}"
                    MessageContentType.LOCATION -> "📍 ${message.body}"
                    MessageContentType.CONTACT -> "👤 ${message.body}"
                    MessageContentType.POLL -> "📊 ${message.body}"
                    MessageContentType.SYSTEM -> message.body
                },
                unreadCount = 0,
                lastActive = message.timestamp,
            )
        }
        LocalChatStore.cacheMessages(chatId, list.toList())
        if (message.deliveryStatus == MessageDeliveryStatus.QUEUED) {
            reliability = reliability.copy(queuedMessages = LocalChatStore.loadPending().size, syncHealth = "Waiting for network")
        }
    }

    private fun updateLocalMessageDeliveryStatus(chatId: String, messageId: String, status: MessageDeliveryStatus) {
        val list = messagesByChat[chatId] ?: return
        val index = list.indexOfFirst { it.id == messageId }
        if (index == -1) return
        list[index] = list[index].copy(
            deliveryStatus = status,
            timestamp = if (status == MessageDeliveryStatus.QUEUED) "Queued" else list[index].timestamp,
        )
        LocalChatStore.cacheMessages(chatId, list.toList())
    }

    private fun conversationMessages(chatId: String): List<ChatMessageUi> = messagesByChat[chatId]?.toList().orEmpty()

    private fun markConversationRead(id: String) {
        updateConversation(id) { it.copy(unreadCount = 0) }
        if (isGlobalChat(id)) {
            ensureGlobalConversation(unreadCountOverride = 0)
            return
        }
        if (backendConnected) {
            viewModelScope.launch { runCatching { structuredChatRepository.clearUnreadCount(id, currentUser.id) } }
        }
    }

    private fun buildOutgoingMessage(
        contentType: MessageContentType,
        body: String,
        metadata: String = "",
        replyToSnippet: String = "",
    ): ChatMessageUi {
        val status = if (reliability.isOfflineMode) MessageDeliveryStatus.QUEUED else MessageDeliveryStatus.DELIVERED
        return ChatMessageUi(
            id = randomId("msg"),
            senderId = currentUser.id,
            senderName = currentUser.name,
            contentType = contentType,
            body = body,
            timestamp = if (reliability.isOfflineMode) "Queued" else "Now",
            deliveryStatus = status,
            metadata = metadata,
            replyToSnippet = replyToSnippet,
            starred = contentType == MessageContentType.DOCUMENT,
        )
    }

    private fun upsertConversationLocally(conversation: ConversationUi) {
        val index = conversationsState.indexOfFirst { it.id == conversation.id }
        if (index >= 0) {
            conversationsState[index] = conversation
        } else {
            conversationsState.add(0, conversation)
        }
    }

    private fun updateConversation(id: String, transform: (ConversationUi) -> ConversationUi) {
        val index = conversationsState.indexOfFirst { it.id == id }
        if (index == -1) return
        conversationsState[index] = transform(conversationsState[index])
    }

    private fun seedDemoData() = Unit

    private fun randomId(prefix: String): String = "$prefix-${Random.nextInt(1000, 9999)}"
}
