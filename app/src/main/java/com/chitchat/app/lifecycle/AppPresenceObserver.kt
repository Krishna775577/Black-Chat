package com.chitchat.app.lifecycle

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.chitchat.app.data.firebase.FcmTokenRepository
import com.chitchat.app.data.firebase.StructuredChatRepository
import com.chitchat.app.data.local.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppPresenceObserver(
    private val repository: StructuredChatRepository = StructuredChatRepository(),
    private val tokenRepository: FcmTokenRepository = FcmTokenRepository(),
) : DefaultLifecycleObserver {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onStart(owner: LifecycleOwner) {
        val uid = SessionManager.load()?.uid.orEmpty()
        if (uid.isBlank()) return
        scope.launch {
            runCatching { repository.updatePresence(uid, true) }
            runCatching { tokenRepository.syncCurrentToken(uid) }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        val uid = SessionManager.load()?.uid.orEmpty()
        if (uid.isBlank()) return
        scope.launch { runCatching { repository.updatePresence(uid, false) } }
    }
}
