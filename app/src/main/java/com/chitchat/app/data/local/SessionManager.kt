package com.chitchat.app.data.local

import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    private const val PREFS_NAME = "chitchat_session"
    private const val KEY_LOGGED_IN = "logged_in"
    private const val KEY_UID = "uid"
    private const val KEY_USERNAME = "username"
    private const val KEY_DISPLAY_NAME = "display_name"
    private const val KEY_PHONE = "phone"
    private const val KEY_ABOUT = "about"
    private const val KEY_IS_OTP_USER = "is_otp_user"
    private const val KEY_GOLDEN_VERIFIED = "golden_verified"
    private const val KEY_PHOTO_URL = "photo_url"
    private const val KEY_BANNER_URL = "banner_url"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun save(session: SavedSession) {
        ensureReady()
        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putString(KEY_UID, session.uid)
            .putString(KEY_USERNAME, session.username)
            .putString(KEY_DISPLAY_NAME, session.displayName)
            .putString(KEY_PHONE, session.phoneNumber)
            .putString(KEY_ABOUT, session.about)
            .putBoolean(KEY_IS_OTP_USER, session.isOtpUser)
            .putBoolean(KEY_GOLDEN_VERIFIED, session.goldenVerified)
            .putString(KEY_PHOTO_URL, session.photoUrl)
            .putString(KEY_BANNER_URL, session.bannerUrl)
            .apply()
    }

    fun load(): SavedSession? {
        ensureReady()
        if (!prefs.getBoolean(KEY_LOGGED_IN, false)) return null
        return SavedSession(
            uid = prefs.getString(KEY_UID, "").orEmpty(),
            username = prefs.getString(KEY_USERNAME, "").orEmpty(),
            displayName = prefs.getString(KEY_DISPLAY_NAME, "").orEmpty(),
            phoneNumber = prefs.getString(KEY_PHONE, "").orEmpty(),
            about = prefs.getString(KEY_ABOUT, "").orEmpty(),
            isOtpUser = prefs.getBoolean(KEY_IS_OTP_USER, false),
            goldenVerified = prefs.getBoolean(KEY_GOLDEN_VERIFIED, false),
            photoUrl = prefs.getString(KEY_PHOTO_URL, "").orEmpty(),
            bannerUrl = prefs.getString(KEY_BANNER_URL, "").orEmpty(),
        )
    }

    fun clear() {
        ensureReady()
        prefs.edit().clear().apply()
    }

    private fun ensureReady() {
        check(::prefs.isInitialized) { "SessionManager.init(context) must be called before use." }
    }
}

data class SavedSession(
    val uid: String,
    val username: String,
    val displayName: String,
    val phoneNumber: String,
    val about: String,
    val isOtpUser: Boolean,
    val goldenVerified: Boolean = false,
    val photoUrl: String = "",
    val bannerUrl: String = "",
)
