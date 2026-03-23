package com.chitchat.app.notifications

import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.chitchat.app.data.firebase.FirebaseAuthManager
import com.chitchat.app.data.firebase.StructuredChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val chatId = intent.getStringExtra(EXTRA_CHAT_ID).orEmpty()
        val messageId = intent.getStringExtra(EXTRA_MESSAGE_ID).orEmpty()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val repository = StructuredChatRepository()
        val userId = FirebaseAuthManager.currentUid().orEmpty()
        if (chatId.isBlank() || userId.isBlank()) return
        when (intent.action) {
            ACTION_MARK_READ -> {
                scope.launch {
                    runCatching { repository.markRead(chatId, messageId, userId) }
                }
            }
            ACTION_REPLY -> {
                val replyText = RemoteInput.getResultsFromIntent(intent)?.getCharSequence(KEY_TEXT_REPLY)?.toString().orEmpty()
                if (replyText.isBlank()) return
                scope.launch {
                    runCatching {
                        repository.sendTextMessage(
                            chatId = chatId,
                            senderId = userId,
                            senderName = intent.getStringExtra(EXTRA_SENDER_NAME).orEmpty().ifBlank { "You" },
                            text = replyText,
                        )
                    }
                }
                Toast.makeText(context, "Reply queued", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val ACTION_MARK_READ = "com.chitchat.app.ACTION_MARK_READ"
        const val ACTION_REPLY = "com.chitchat.app.ACTION_REPLY"
        const val EXTRA_CHAT_ID = "chatId"
        const val EXTRA_MESSAGE_ID = "messageId"
        const val EXTRA_SENDER_NAME = "senderName"
        const val KEY_TEXT_REPLY = "key_text_reply"
    }
}
