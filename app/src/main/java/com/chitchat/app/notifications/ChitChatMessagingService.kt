package com.chitchat.app.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import com.chitchat.app.ChitChatApplication
import com.chitchat.app.MainActivity
import com.chitchat.app.R
import com.chitchat.app.data.firebase.FcmTokenRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ChitChatMessagingService : FirebaseMessagingService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        serviceScope.launch {
            runCatching { FcmTokenRepository().saveToken(userId, token) }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val chatId = message.data["chatId"].orEmpty()
        val type = message.data["type"].orEmpty()
        val messageId = message.data["messageId"].orEmpty()
        val senderName = message.data["senderName"].orEmpty()
        val fallbackTitle = when (type) {
            "call" -> "Incoming call"
            "status" -> "New status update"
            else -> if (senderName.isBlank()) "New ChitChat message" else senderName
        }
        val title = message.notification?.title ?: message.data["title"] ?: fallbackTitle
        val body = message.notification?.body ?: message.data["body"] ?: "Open the app to view the update."
        val channel = when (type) {
            "call" -> ChitChatApplication.CALL_NOTIFICATION_CHANNEL
            "status" -> ChitChatApplication.STATUS_NOTIFICATION_CHANNEL
            else -> ChitChatApplication.DEFAULT_NOTIFICATION_CHANNEL
        }
        if (!canPostNotifications()) return

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("chatId", chatId)
            putExtra("notificationType", type)
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            (chatId.hashCode() + 9001),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val markReadIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_MARK_READ
            putExtra(NotificationActionReceiver.EXTRA_CHAT_ID, chatId)
            putExtra(NotificationActionReceiver.EXTRA_MESSAGE_ID, messageId)
        }
        val markReadPendingIntent = PendingIntent.getBroadcast(
            this,
            chatId.hashCode() + 300,
            markReadIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val replyRemoteInput = RemoteInput.Builder(NotificationActionReceiver.KEY_TEXT_REPLY)
            .setLabel("Reply")
            .build()
        val replyIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_REPLY
            putExtra(NotificationActionReceiver.EXTRA_CHAT_ID, chatId)
            putExtra(NotificationActionReceiver.EXTRA_MESSAGE_ID, messageId)
            putExtra(NotificationActionReceiver.EXTRA_SENDER_NAME, senderName)
        }
        val replyPendingIntent = PendingIntent.getBroadcast(
            this,
            chatId.hashCode() + 600,
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )

        val notification = NotificationCompat.Builder(this, channel)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .setCategory(
                when (type) {
                    "call" -> NotificationCompat.CATEGORY_CALL
                    "status" -> NotificationCompat.CATEGORY_STATUS
                    else -> NotificationCompat.CATEGORY_MESSAGE
                }
            )
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setOnlyAlertOnce(false)
            .setContentIntent(openPendingIntent)
            .setGroup(if (chatId.isBlank()) "general_updates" else "chat_$chatId")
            .addAction(0, "Mark read", markReadPendingIntent)
            .addAction(
                NotificationCompat.Action.Builder(0, "Reply", replyPendingIntent)
                    .addRemoteInput(replyRemoteInput)
                    .build()
            )
            .apply {
                if (type == "call") {
                    setFullScreenIntent(openPendingIntent, true)
                }
            }
            .build()

        runCatching {
            with(NotificationManagerCompat.from(this)) {
                notify((if (messageId.isNotBlank()) messageId else "$chatId-${System.currentTimeMillis()}").hashCode() and 0x7fffffff, notification)
                if (chatId.isNotBlank()) {
                    val summary = NotificationCompat.Builder(this@ChitChatMessagingService, channel)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("ChitChat")
                        .setContentText("New updates in your chats")
                        .setStyle(NotificationCompat.InboxStyle().addLine(body))
                        .setGroup("chat_$chatId")
                        .setGroupSummary(true)
                        .build()
                    notify((chatId.hashCode() and 0x7fffffff), summary)
                }
            }
        }
    }

    private fun canPostNotifications(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }
}
