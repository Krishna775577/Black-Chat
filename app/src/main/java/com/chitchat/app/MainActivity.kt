package com.chitchat.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.chitchat.app.data.local.LocalChatStore
import com.chitchat.app.navigation.LaunchRouter
import com.chitchat.app.ui.ChitChatApp
import com.chitchat.app.ui.theme.ChitChatTheme

class MainActivity : ComponentActivity() {
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        applyPrivacyWindowFlags()
        captureLaunchRoute(intent?.getStringExtra("chatId"))
        requestNotificationPermissionIfNeeded()
        setContent {
            ChitChatTheme {
                ChitChatApp()
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        captureLaunchRoute(intent.getStringExtra("chatId"))
    }

    private fun captureLaunchRoute(chatId: String?) {
        chatId?.takeIf { it.isNotBlank() }?.let { LaunchRouter.pendingChatId = it }
    }

    private fun applyPrivacyWindowFlags() {
        val prefs = LocalChatStore.loadSecurityPreferences()
        if (prefs.screenshotBlock || prefs.blurInRecents) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) return
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 501)
    }
}
