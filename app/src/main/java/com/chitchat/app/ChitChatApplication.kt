package com.chitchat.app

import android.app.Application
import android.content.Context
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.lifecycle.ProcessLifecycleOwner
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder

import com.chitchat.app.data.firebase.FcmTokenRepository
import com.chitchat.app.data.local.LocalChatStore
import com.chitchat.app.data.local.SessionManager
import com.chitchat.app.lifecycle.AppPresenceObserver
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ChitChatApplication : Application(), ImageLoaderFactory {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)


    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .components {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .crossfade(true)
            .build()

    override fun onCreate() {
        super.onCreate()
        instance = this
        SessionManager.init(this)
        LocalChatStore.init(this)
        runCatching { FirebaseApp.initializeApp(this) }
        runCatching { FirebaseMessaging.getInstance().isAutoInitEnabled = true }
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppPresenceObserver())
        createNotificationChannels()
        syncPushTokenIfPossible()
    }

    private fun syncPushTokenIfPossible() {
        val fallbackUid = SessionManager.load()?.uid.orEmpty()
        val uid = runCatching { FirebaseAuth.getInstance().currentUser?.uid.orEmpty() }.getOrDefault("")
            .ifBlank { fallbackUid }
        if (uid.isBlank()) return
        appScope.launch {
            runCatching { FcmTokenRepository().syncCurrentToken(uid) }
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java) ?: return
        listOf(
            NotificationChannel(DEFAULT_NOTIFICATION_CHANNEL, "ChitChat messages", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Private and group chat alerts"
                enableVibration(true)
                enableLights(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PRIVATE
            },
            NotificationChannel(CALL_NOTIFICATION_CHANNEL, "ChitChat calls", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Incoming and missed call alerts"
                enableVibration(true)
                enableLights(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            },
            NotificationChannel(STATUS_NOTIFICATION_CHANNEL, "ChitChat updates", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Status and story alerts"
                enableVibration(true)
                enableLights(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PRIVATE
            },
        ).forEach(manager::createNotificationChannel)
    }

    companion object {
        lateinit var instance: Context
            private set

        const val DEFAULT_NOTIFICATION_CHANNEL = "chitchat_messages"
        const val CALL_NOTIFICATION_CHANNEL = "chitchat_calls"
        const val STATUS_NOTIFICATION_CHANNEL = "chitchat_status"
    }
}
