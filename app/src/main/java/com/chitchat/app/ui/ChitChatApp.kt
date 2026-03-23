package com.chitchat.app.ui

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.withTimeoutOrNull
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.automirrored.filled.Forward
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.chitchat.app.navigation.LaunchRouter
import com.chitchat.app.R
import com.chitchat.app.data.model.ActiveCallUi
import com.chitchat.app.data.model.CallDirection
import com.chitchat.app.data.model.CallRecordUi
import com.chitchat.app.data.model.CallType
import com.chitchat.app.data.model.ContactSearchUi
import com.chitchat.app.data.model.FireUserProfile
import com.chitchat.app.data.model.ChatFilter
import com.chitchat.app.data.model.ChatMessageUi
import com.chitchat.app.data.model.ConversationType
import com.chitchat.app.data.model.ConversationUi
import com.chitchat.app.data.model.HomeTab
import com.chitchat.app.data.model.LinkedDeviceUi
import com.chitchat.app.data.model.LoginFlowStep
import com.chitchat.app.data.model.MessageContentType
import com.chitchat.app.data.model.MessageDeliveryStatus
import com.chitchat.app.data.model.StatusStoryUi
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.EncodeHintType
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeWriter
import com.chitchat.app.ui.theme.ChitChatTheme

private val WaGreen = Color(0xFF128C7E)
private val WaDarkGreen = Color(0xFF075E54)
private val WaHeaderGreen = Color(0xFF0B5D53)
private val WaLightBg = Color(0xFFECE5DD)
private val WaChatBg = Color(0xFFE7DDD4)
private val WaBubbleOut = Color(0xFFD9FDD3)
private val WaBubbleIn = Color(0xFFFFFFFF)
private val WaMutedText = Color(0xFF667781)
private val WaUnreadGreen = Color(0xFF25D366)
private val AccentPurple = Color(0xFF7367F0)
private val AccentBlue = Color(0xFF53BDEB)
private val AccentGlow = Color(0xFFA7F3D0)
private val GlassLight = Color(0x66FFFFFF)
private val DarkPanel = Color(0xFF111B21)
private val DarkPanelSoft = Color(0xFF202C33)
private val WaDivider = Color(0xFFD6D3CD)
private val WaCard = Color(0xFFFDFBF7)
private val StatusRingStart = Color(0xFF25D366)
private val StatusRingEnd = Color(0xFF00A884)
private val VerificationGold = Color(0xFFFFC83D)
private val VerificationGoldSoft = Color(0xFFFFF2C2)

private data class ChatThemePalette(
    val key: String,
    val title: String,
    val subtitle: String,
    val topBarColor: Color,
    val chatBaseColor: Color,
    val chatBackgroundBrush: Brush,
    val composerBarColor: Color,
    val composerFieldColor: Color,
)

private fun chatThemePalette(themeKey: String, darkMode: Boolean): ChatThemePalette {
    return when (themeKey.uppercase(Locale.ROOT)) {
        "OCEAN" -> ChatThemePalette(
            key = "OCEAN",
            title = "Ocean Blue",
            subtitle = "Cool blue glass look",
            topBarColor = Color(0xFF0B4F6C),
            chatBaseColor = Color(0xFFEAF7FF),
            chatBackgroundBrush = Brush.verticalGradient(listOf(Color(0xFFF4FBFF), Color(0xFFD7F0FF), Color(0xFFC8E6FF))),
            composerBarColor = Color(0xFFE4F4FF),
            composerFieldColor = Color.White,
        )
        "SUNSET" -> ChatThemePalette(
            key = "SUNSET",
            title = "Sunset Gold",
            subtitle = "Warm peach and gold",
            topBarColor = Color(0xFF9A4F1B),
            chatBaseColor = Color(0xFFFFF4E8),
            chatBackgroundBrush = Brush.verticalGradient(listOf(Color(0xFFFFFBF4), Color(0xFFFFE2BF), Color(0xFFFFC98F))),
            composerBarColor = Color(0xFFFFF0DF),
            composerFieldColor = Color.White,
        )
        "ROSE" -> ChatThemePalette(
            key = "ROSE",
            title = "Rose Pink",
            subtitle = "Soft pink premium feel",
            topBarColor = Color(0xFF9A4563),
            chatBaseColor = Color(0xFFFFF4F8),
            chatBackgroundBrush = Brush.verticalGradient(listOf(Color(0xFFFFFBFD), Color(0xFFFADAE7), Color(0xFFF4C2D7))),
            composerBarColor = Color(0xFFFFEEF4),
            composerFieldColor = Color.White,
        )
        "LAVENDER" -> ChatThemePalette(
            key = "LAVENDER",
            title = "Lavender Night",
            subtitle = "Purple neon chat style",
            topBarColor = Color(0xFF5A3E92),
            chatBaseColor = Color(0xFFF5F0FF),
            chatBackgroundBrush = Brush.verticalGradient(listOf(Color(0xFFFBF8FF), Color(0xFFE7DCFF), Color(0xFFD5C3FF))),
            composerBarColor = Color(0xFFF0E9FF),
            composerFieldColor = Color.White,
        )
        "FOREST" -> ChatThemePalette(
            key = "FOREST",
            title = "Forest Green",
            subtitle = "Fresh green matte feel",
            topBarColor = Color(0xFF2F6B3C),
            chatBaseColor = Color(0xFFF1F9F1),
            chatBackgroundBrush = Brush.verticalGradient(listOf(Color(0xFFF8FFF8), Color(0xFFDCF1DE), Color(0xFFC2E2C7))),
            composerBarColor = Color(0xFFEAF6EC),
            composerFieldColor = Color.White,
        )
        "CUSTOM" -> ChatThemePalette(
            key = "CUSTOM",
            title = "Customize Theme",
            subtitle = "Use your own background photo",
            topBarColor = if (darkMode) Color(0xFF22303A) else WaDarkGreen,
            chatBaseColor = if (darkMode) Color(0xFF0B141A) else WaChatBg,
            chatBackgroundBrush = if (darkMode) {
                Brush.verticalGradient(listOf(Color(0xCC0B141A), Color(0xB30B141A), Color(0xCC102027)))
            } else {
                Brush.verticalGradient(listOf(Color(0x99FFFDF8), Color(0xB3F2ECE3), Color(0x99E8DED3)))
            },
            composerBarColor = if (darkMode) DarkPanel else WaLightBg,
            composerFieldColor = if (darkMode) DarkPanelSoft else Color.White,
        )
        else -> ChatThemePalette(
            key = "EMERALD",
            title = "Emerald",
            subtitle = "Classic WhatsApp style",
            topBarColor = if (darkMode) DarkPanel else WaDarkGreen,
            chatBaseColor = if (darkMode) Color(0xFF0B141A) else WaChatBg,
            chatBackgroundBrush = if (darkMode) {
                Brush.verticalGradient(listOf(Color(0xFF0B141A), Color(0xFF0F1D25), Color(0xFF102129)))
            } else {
                Brush.verticalGradient(listOf(Color(0xFFF6F0E9), Color(0xFFEDE2D6), Color(0xFFE5D8CB)))
            },
            composerBarColor = if (darkMode) DarkPanel else WaLightBg,
            composerFieldColor = if (darkMode) DarkPanelSoft else Color.White,
        )
    }
}

private fun chatThemeLabel(themeKey: String): String = chatThemePalette(themeKey, darkMode = false).title

private fun availableChatThemes(darkMode: Boolean): List<ChatThemePalette> = listOf(
    chatThemePalette("EMERALD", darkMode),
    chatThemePalette("OCEAN", darkMode),
    chatThemePalette("SUNSET", darkMode),
    chatThemePalette("ROSE", darkMode),
    chatThemePalette("LAVENDER", darkMode),
    chatThemePalette("CUSTOM", darkMode),
)

private data class ProfileBackgroundPalette(
    val key: String,
    val title: String,
    val subtitle: String,
    val bannerBrush: Brush,
    val screenBrush: Brush,
    val heroBodyBrush: Brush,
    val panelBrush: Brush,
    val screenColor: Color,
    val topBarColor: Color,
    val cardColor: Color,
    val outlineColor: Color,
    val titleColor: Color,
    val subtitleColor: Color,
    val accentColor: Color,
)

private fun profileBackgroundPalette(backgroundKey: String, darkMode: Boolean): ProfileBackgroundPalette {
    return when (backgroundKey.uppercase(Locale.ROOT)) {
        "PLUM" -> ProfileBackgroundPalette(
            key = "PLUM",
            title = "Plum Night",
            subtitle = "Purple + black Discord vibe",
            bannerBrush = Brush.verticalGradient(listOf(Color(0xFFB579EA), Color(0xFF8E4EC6), Color(0xFF67369B), Color(0xFF251133))),
            screenBrush = Brush.verticalGradient(listOf(Color(0xFF3B1A4F), Color(0xFF231032), Color(0xFF170A20))),
            heroBodyBrush = Brush.verticalGradient(listOf(Color(0xFF6E42A6), Color(0xFF4B266C), Color(0xFF271137))),
            panelBrush = Brush.verticalGradient(listOf(Color(0xFF4F2D74), Color(0xFF2F1743), Color(0xFF1D0D2B))),
            screenColor = if (darkMode) Color(0xFF170A20) else Color(0xFFF7F0FF),
            topBarColor = if (darkMode) Color(0xFF210D2D) else Color(0xFFEAD8FF),
            cardColor = if (darkMode) Color(0xFF271137) else Color(0xFFFFFFFF),
            outlineColor = Color(0xFF7E52B2),
            titleColor = if (darkMode) Color.White else Color(0xFF210D2D),
            subtitleColor = if (darkMode) Color(0xFFD9C2F3) else Color(0xFF714F94),
            accentColor = Color(0xFFE39BFF),
        )
        "OCEAN" -> ProfileBackgroundPalette(
            key = "OCEAN",
            title = "Ocean Deep",
            subtitle = "Blue + cyan dual tone",
            bannerBrush = Brush.verticalGradient(listOf(Color(0xFF6EB2FF), Color(0xFF2A7FFF), Color(0xFF175FC8), Color(0xFF081C3A))),
            screenBrush = Brush.verticalGradient(listOf(Color(0xFF103C67), Color(0xFF0A2237), Color(0xFF061626))),
            heroBodyBrush = Brush.verticalGradient(listOf(Color(0xFF2F6CA7), Color(0xFF174666), Color(0xFF0D2438))),
            panelBrush = Brush.verticalGradient(listOf(Color(0xFF23527A), Color(0xFF123652), Color(0xFF0B2132))),
            screenColor = if (darkMode) Color(0xFF061626) else Color(0xFFF2F8FF),
            topBarColor = if (darkMode) Color(0xFF0A2237) else Color(0xFFDDEEFF),
            cardColor = if (darkMode) Color(0xFF0D2438) else Color.White,
            outlineColor = Color(0xFF3F7FBC),
            titleColor = if (darkMode) Color.White else Color(0xFF0A2237),
            subtitleColor = if (darkMode) Color(0xFFB4D3F2) else Color(0xFF4F6C86),
            accentColor = Color(0xFF7FD7FF),
        )
        "EMERALD" -> ProfileBackgroundPalette(
            key = "EMERALD",
            title = "Emerald Glow",
            subtitle = "Green + dark chat look",
            bannerBrush = Brush.verticalGradient(listOf(Color(0xFF5FE2A3), Color(0xFF15A46A), Color(0xFF0D7F52), Color(0xFF072117))),
            screenBrush = Brush.verticalGradient(listOf(Color(0xFF185C3F), Color(0xFF0C2D20), Color(0xFF071A12))),
            heroBodyBrush = Brush.verticalGradient(listOf(Color(0xFF23895C), Color(0xFF145235), Color(0xFF0E2A1F))),
            panelBrush = Brush.verticalGradient(listOf(Color(0xFF1A6A48), Color(0xFF103B29), Color(0xFF0A2419))),
            screenColor = if (darkMode) Color(0xFF071A12) else Color(0xFFF2FBF6),
            topBarColor = if (darkMode) Color(0xFF0B251B) else Color(0xFFD8F4E4),
            cardColor = if (darkMode) Color(0xFF0E2A1F) else Color.White,
            outlineColor = Color(0xFF247B58),
            titleColor = if (darkMode) Color.White else Color(0xFF0B251B),
            subtitleColor = if (darkMode) Color(0xFFAED8C4) else Color(0xFF4B7863),
            accentColor = Color(0xFF54E0A2),
        )
        "SUNSET" -> ProfileBackgroundPalette(
            key = "SUNSET",
            title = "Sunset Ember",
            subtitle = "Gold + deep brown contrast",
            bannerBrush = Brush.verticalGradient(listOf(Color(0xFFFFCB74), Color(0xFFFFA73D), Color(0xFFE07D2C), Color(0xFF321608))),
            screenBrush = Brush.verticalGradient(listOf(Color(0xFF6A3315), Color(0xFF2C160A), Color(0xFF1A0E07))),
            heroBodyBrush = Brush.verticalGradient(listOf(Color(0xFFAE6025), Color(0xFF5A2B12), Color(0xFF34190C))),
            panelBrush = Brush.verticalGradient(listOf(Color(0xFF8B4D21), Color(0xFF47210F), Color(0xFF291308))),
            screenColor = if (darkMode) Color(0xFF1A0E07) else Color(0xFFFFF7EF),
            topBarColor = if (darkMode) Color(0xFF261308) else Color(0xFFFFE8D2),
            cardColor = if (darkMode) Color(0xFF34190C) else Color.White,
            outlineColor = Color(0xFFAF6C37),
            titleColor = if (darkMode) Color.White else Color(0xFF261308),
            subtitleColor = if (darkMode) Color(0xFFF3CC9E) else Color(0xFF8F5C33),
            accentColor = Color(0xFFFFC168),
        )
        else -> ProfileBackgroundPalette(
            key = "CRIMSON",
            title = "Crimson Noir",
            subtitle = "Red + black Discord style",
            bannerBrush = Brush.verticalGradient(listOf(Color(0xFFFF8197), Color(0xFFC83B4B), Color(0xFF8E1C31), Color(0xFF220208))),
            screenBrush = Brush.verticalGradient(listOf(Color(0xFF5D1826), Color(0xFF23070D), Color(0xFF130306))),
            heroBodyBrush = Brush.verticalGradient(listOf(Color(0xFF8B3046), Color(0xFF47101D), Color(0xFF25070D))),
            panelBrush = Brush.verticalGradient(listOf(Color(0xFF6C2335), Color(0xFF34101B), Color(0xFF1A050A))),
            screenColor = if (darkMode) Color(0xFF130306) else Color(0xFFFFF3F5),
            topBarColor = if (darkMode) Color(0xFF1E050A) else Color(0xFFFFDFE5),
            cardColor = if (darkMode) Color(0xFF25070D) else Color.White,
            outlineColor = Color(0xFF7F2A3D),
            titleColor = if (darkMode) Color.White else Color(0xFF2A0A10),
            subtitleColor = if (darkMode) Color(0xFFF0B5C0) else Color(0xFF8B4B59),
            accentColor = Color(0xFFFF8AA3),
        )
    }
}

private fun profileBackgroundLabel(backgroundKey: String): String = profileBackgroundPalette(backgroundKey, darkMode = true).title

private fun availableProfileBackgrounds(darkMode: Boolean): List<ProfileBackgroundPalette> = listOf(
    profileBackgroundPalette("CRIMSON", darkMode),
    profileBackgroundPalette("PLUM", darkMode),
    profileBackgroundPalette("OCEAN", darkMode),
    profileBackgroundPalette("EMERALD", darkMode),
    profileBackgroundPalette("SUNSET", darkMode),
)


private fun parseColorHexOrDefault(value: String, fallback: Color): Color {
    val cleaned = value.trim().removePrefix("#")
    val argb = when (cleaned.length) {
        6 -> "FF$cleaned"
        8 -> cleaned
        else -> return fallback
    }
    return try {
        Color(argb.toLong(16))
    } catch (_: Exception) {
        fallback
    }
}

private fun colorToHex(color: Color): String {
    val red = (color.red.coerceIn(0f, 1f) * 255f + 0.5f).toInt()
    val green = (color.green.coerceIn(0f, 1f) * 255f + 0.5f).toInt()
    val blue = (color.blue.coerceIn(0f, 1f) * 255f + 0.5f).toInt()
    return String.format("#%02X%02X%02X", red, green, blue)
}

private fun blendColors(start: Color, end: Color, ratio: Float): Color {
    val t = ratio.coerceIn(0f, 1f)
    return Color(
        red = start.red + (end.red - start.red) * t,
        green = start.green + (end.green - start.green) * t,
        blue = start.blue + (end.blue - start.blue) * t,
        alpha = start.alpha + (end.alpha - start.alpha) * t,
    )
}

private fun lightenColor(color: Color, amount: Float): Color = blendColors(color, Color.White, amount)

private fun darkenColor(color: Color, amount: Float): Color = blendColors(color, Color.Black, amount)

private fun readableTextColor(background: Color): Color = if (background.luminance() > 0.48f) Color(0xFF101418) else Color.White

private fun readableSecondaryTextColor(background: Color): Color =
    if (background.luminance() > 0.48f) Color(0xFF101418) else Color.White

private fun profileBackgroundPalette(
    topColorHex: String,
    bottomColorHex: String,
    darkMode: Boolean,
): ProfileBackgroundPalette {
    val defaultTop = Color(0xFFC83B4B)
    val defaultBottom = Color(0xFF25070D)
    val topColor = parseColorHexOrDefault(topColorHex, defaultTop)
    val bottomColor = parseColorHexOrDefault(bottomColorHex, defaultBottom)
    val bottomIsLight = bottomColor.luminance() > 0.48f
    val topIsLight = topColor.luminance() > 0.48f
    val cardColor = if (bottomIsLight) {
        blendColors(bottomColor, Color.White, 0.10f)
    } else {
        blendColors(bottomColor, Color.Black, 0.10f)
    }
    val screenColor = if (bottomIsLight) {
        blendColors(bottomColor, Color.White, 0.18f)
    } else {
        blendColors(bottomColor, Color.Black, 0.18f)
    }
    val topBarColor = if (topIsLight) {
        blendColors(topColor, Color.White, 0.08f)
    } else {
        blendColors(topColor, Color.Black, 0.12f)
    }
    val titleColor = readableTextColor(cardColor)
    val subtitleColor = readableSecondaryTextColor(cardColor)
    val bridgeColor = blendColors(topColor, bottomColor, 0.42f)
    val bannerBrush = Brush.verticalGradient(
        listOf(
            lightenColor(topColor, if (topIsLight) 0.04f else 0.16f),
            topColor,
            blendColors(topColor, Color.White, if (topIsLight) 0.10f else 0.24f),
            bridgeColor,
        )
    )
    val heroBodyBrush = Brush.verticalGradient(
        listOf(
            blendColors(bridgeColor, Color.White, 0.16f),
            blendColors(bottomColor, Color.White, if (bottomIsLight) 0.06f else 0.20f),
            bottomColor,
            darkenColor(bottomColor, if (bottomIsLight) 0.12f else 0.18f),
        )
    )
    val screenBrush = Brush.verticalGradient(
        listOf(
            blendColors(topColor, Color.White, if (topIsLight) 0.08f else 0.14f),
            bridgeColor,
            screenColor,
            darkenColor(bottomColor, if (bottomIsLight) 0.06f else 0.22f),
        )
    )
    val panelBrush = Brush.verticalGradient(
        listOf(
            blendColors(bridgeColor, Color.White, 0.10f),
            cardColor,
            darkenColor(cardColor, if (bottomIsLight) 0.10f else 0.08f),
        )
    )
    return ProfileBackgroundPalette(
        key = "CUSTOM_HEX",
        title = "Custom colors",
        subtitle = "Top ${colorToHex(topColor)} • Bottom ${colorToHex(bottomColor)}",
        bannerBrush = bannerBrush,
        screenBrush = screenBrush,
        heroBodyBrush = heroBodyBrush,
        panelBrush = panelBrush,
        screenColor = screenColor,
        topBarColor = topBarColor,
        cardColor = cardColor,
        outlineColor = blendColors(topColor, bottomColor, 0.35f),
        titleColor = titleColor,
        subtitleColor = subtitleColor,
        accentColor = readableTextColor(topBarColor),
    )
}

private fun profileBackgroundLabel(topColorHex: String, bottomColorHex: String): String =
    "Top ${topColorHex.uppercase(Locale.ROOT)} • Bottom ${bottomColorHex.uppercase(Locale.ROOT)}"

private fun profileColorChoices(): List<Color> = listOf(
    Color(0xFFC83B4B),
    Color(0xFF8E4EC6),
    Color(0xFF2A7FFF),
    Color(0xFF15A46A),
    Color(0xFFFFA73D),
    Color(0xFF101418),
    Color(0xFFFFFFFF),
    Color(0xFFEF6C8D),
    Color(0xFF6D4C41),
    Color(0xFF00BCD4),
)

@Composable
private fun loginFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
    disabledTextColor = Color.Black.copy(alpha = 0.62f),
    cursorColor = WaDarkGreen,
    focusedBorderColor = WaGreen,
    unfocusedBorderColor = Color(0xFFB7C4CB),
    disabledBorderColor = Color(0xFFD7E1E6),
    focusedLabelColor = WaDarkGreen,
    unfocusedLabelColor = WaMutedText,
    focusedPlaceholderColor = WaMutedText,
    unfocusedPlaceholderColor = WaMutedText,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    disabledContainerColor = Color.White,
)

@Composable
private fun MiniPreviewPhone(title: String, subtitle: String, dark: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = if (dark) DarkPanel else Color.White,
        border = BorderStroke(2.dp, if (dark) Color.Black.copy(alpha = 0.32f) else Color(0xFFCFD8DC)),
        shadowElevation = 4.dp,
        modifier = Modifier
            .width(64.dp)
            .height(128.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(22.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (dark) Color(0xFF3A444B) else Color(0xFF101418))
            )
            if (dark) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF182229), Color(0xFF0F1418))
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f))
                        )
                        Text(title, color = Color.White.copy(alpha = 0.92f), style = MaterialTheme.typography.labelSmall)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF8FBFC))
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.horizontalGradient(listOf(WaDarkGreen, WaGreen)))
                        )
                        repeat(4) {
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFE1E8EC)))
                                Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(999.dp)).background(Color(0xFFE5EBEE)))
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(subtitle, color = WaMutedText, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChitChatApp(viewModel: ChatAppViewModel = viewModel()) {
    ChitChatTheme(darkTheme = viewModel.chatPreferences.darkMode) {
        var pickerMode by remember { mutableStateOf(MessageContentType.IMAGE) }
        var statusPickerMode by remember { mutableStateOf(MessageContentType.IMAGE) }
        var showChatThemeDialog by remember { mutableStateOf(false) }
        var showProfileBackgroundDialog by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.isLoggedIn, viewModel.visibleConversations.size) {
        val pendingChatId = LaunchRouter.pendingChatId
        if (pendingChatId != null && viewModel.tryOpenConversation(pendingChatId)) {
            LaunchRouter.pendingChatId = null
        }
    }

    val chatMediaPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.sendPickedMedia(it, pickerMode) }
    }
    val statusMediaPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.prepareStatusMediaForAudience(it, statusPickerMode) }
    }
    val context = LocalContext.current
    val profilePhotoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(viewModel::updateProfilePhoto)
    }
    val profileBannerPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(viewModel::updateProfileBanner)
    }
    val qrImagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) {
            viewModel.updateAuthStatusMessage("QR image selection cancelled.")
        } else {
            val payload = decodeQrPayloadFromImage(context, uri)
            if (payload.isNullOrBlank()) {
                viewModel.updateAuthStatusMessage("Selected image me QR code nahi mila.")
            } else {
                viewModel.openProfileFromQrPayload(payload)
            }
        }
    }
    val customThemePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val localThemeUri = importCustomChatThemeImage(context, uri)
            if (localThemeUri == null) {
                viewModel.updateAuthStatusMessage("Theme image save nahi ho payi. Dusri pic try karo.")
            } else {
                viewModel.updateChatPreferences {
                    it.copy(chatThemeKey = "CUSTOM", customThemeImageUri = localThemeUri.toString())
                }
            }
        }
    }
    val audioPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            viewModel.beginVoiceNoteRecording()
        } else {
            viewModel.updateAuthStatusMessage("Voice note ke liye microphone permission allow karo.")
        }
    }
    var pendingQrCameraUri by remember { mutableStateOf<Uri?>(null) }
    val qrCameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val capturedUri = pendingQrCameraUri
        pendingQrCameraUri = null
        if (!success || capturedUri == null) {
            viewModel.updateAuthStatusMessage("QR camera capture cancelled.")
        } else {
            val payload = decodeQrPayloadFromImage(context, capturedUri)
            if (payload.isNullOrBlank()) {
                viewModel.updateAuthStatusMessage("Camera photo me QR code clear nahi mila.")
            } else {
                viewModel.openProfileFromQrPayload(payload)
            }
        }
    }

    BackHandler(enabled = viewModel.selectedConversation != null) {
        viewModel.closeConversation()
    }
    BackHandler(enabled = viewModel.selectedUserProfile != null && viewModel.selectedConversation == null) {
        viewModel.closeUserProfile()
    }
    BackHandler(enabled = viewModel.showProfileEditor && viewModel.selectedConversation == null && viewModel.selectedUserProfile == null) {
        viewModel.closeProfileEditor()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!viewModel.isLoggedIn) {
            LoginScreen(viewModel = viewModel)
        } else if (viewModel.selectedConversation != null) {
            ChatScreen(
                viewModel = viewModel,
                onBack = { viewModel.closeConversation() },
                onOpenAttachmentPicker = viewModel::openMediaPickerDialog,
                onRequestVoiceNote = {
                    if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        viewModel.beginVoiceNoteRecording()
                    } else {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onStopVoiceNote = viewModel::finishVoiceNoteRecording,
                onCancelVoiceNote = viewModel::cancelVoiceNoteRecording,
                onOpenPicker = { type ->
                    pickerMode = type
                    chatMediaPicker.launch(
                        when (type) {
                            MessageContentType.IMAGE -> "image/*"
                            MessageContentType.VIDEO -> "video/*"
                            MessageContentType.DOCUMENT -> "*/*"
                            MessageContentType.VOICE -> "audio/*"
                            else -> "*/*"
                        }
                    )
                }
            )
        } else if (viewModel.selectedUserProfile != null) {
            UserProfileScreen(
                profile = viewModel.selectedUserProfile!!,
                viewModel = viewModel,
                onBack = viewModel::closeUserProfile,
            )
        } else if (viewModel.showProfileEditor) {
            ProfileScreen(
                viewModel = viewModel,
                onBack = viewModel::closeProfileEditor,
                onPickProfilePhoto = { profilePhotoPicker.launch("image/*") },
                onPickProfileBanner = { profileBannerPicker.launch("image/*") },
            )
        } else {
            HomeScreen(
                viewModel = viewModel,
                onOpenStatusPicker = { type ->
                    statusPickerMode = type
                    statusMediaPicker.launch(if (type == MessageContentType.VIDEO) "video/*" else "image/*")
                },
                onOpenThemeSettings = { showChatThemeDialog = true },
                onOpenProfileBackgroundSettings = { showProfileBackgroundDialog = true },
                onScanProfileQr = {
                    val captureUri = createHighQualityQrCaptureUri(context)
                    if (captureUri == null) {
                        viewModel.updateAuthStatusMessage("Camera start nahi ho paya. Storage path unavailable.")
                    } else {
                        pendingQrCameraUri = captureUri
                        qrCameraLauncher.launch(captureUri)
                    }
                },
                onPickQrFromGallery = { qrImagePicker.launch("image/*") }
            )
        }

        viewModel.activeCall?.let {
            ActiveCallOverlay(
                call = it,
                onMute = viewModel::toggleMuteOnCall,
                onSpeaker = viewModel::toggleSpeakerOnCall,
                onVideo = viewModel::toggleVideoOnCall,
                onEnd = viewModel::endActiveCall,
                onAcceptIncoming = viewModel::acceptIncomingCall,
                onRejectIncoming = viewModel::rejectIncomingCall,
            )
        }
        viewModel.selectedStatusStory?.let {
            StatusViewerOverlay(
                story = it,
                reply = viewModel.statusReplyInput,
                onReplyChange = { value -> viewModel.statusReplyInput = value },
                onClose = viewModel::closeStatusViewer,
                onReply = viewModel::replyToStatus,
                onReact = viewModel::reactToStatus,
                onMuteToggle = { viewModel.toggleStatusMuted(it.id) },
            )
        }
    }

    if (viewModel.showNewChatDialog) {
        CreatePrivateChatDialog(viewModel)
    }
    if (viewModel.showNewGroupDialog) {
        CreateGroupDialog(viewModel)
    }
    if (viewModel.showMediaPickerDialog) {
        MediaPickerDialog(
            title = "Share attachment",
            onDismiss = viewModel::dismissMediaPickerDialog,
            onPick = { type ->
                viewModel.dismissMediaPickerDialog()
                pickerMode = type
                chatMediaPicker.launch(
                    when (type) {
                        MessageContentType.IMAGE -> "image/*"
                        MessageContentType.VIDEO -> "video/*"
                        MessageContentType.DOCUMENT -> "*/*"
                        MessageContentType.VOICE -> "audio/*"
                        else -> "*/*"
                    }
                )
            }
        )
    }
    if (viewModel.showStatusMediaPickerDialog) {
        MediaPickerDialog(
            title = "Add status media",
            onDismiss = viewModel::dismissStatusMediaPickerDialog,
            onPick = { type ->
                viewModel.dismissStatusMediaPickerDialog()
                statusPickerMode = type
                statusMediaPicker.launch(if (type == MessageContentType.VIDEO) "video/*" else "image/*")
            },
            includeDocument = false,
        )
    }
    if (viewModel.showStatusAudienceDialog) {
        StatusAudienceDialog(
            viewModel = viewModel,
            onDismiss = viewModel::dismissStatusAudienceDialog,
            onConfirm = viewModel::confirmStatusAudienceSelection,
        )
    }
    if (showChatThemeDialog) {
        ChatThemeDialog(
            viewModel = viewModel,
            onDismiss = { showChatThemeDialog = false },
            onPickCustomBackground = { customThemePicker.launch("image/*") },
        )
    }
    if (showProfileBackgroundDialog) {
        ProfileBackgroundDialog(
            viewModel = viewModel,
            onDismiss = { showProfileBackgroundDialog = false },
        )
    }
    }
}

@Composable
private fun LoginScreen(viewModel: ChatAppViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0B5D53), Color(0xFF128C7E), Color(0xFFDCF8C6), Color(0xFFF5F1EA))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                shape = RoundedCornerShape(30.dp),
                color = Color.White.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = Color.White.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier.size(104.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color.White.copy(alpha = 0.24f), Color.Transparent)
                                    )
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, tint = Color.White, modifier = Modifier.size(46.dp))
                        }
                    }
                    Text("Black Chat", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Simple private messenger feel ke saath clean WhatsApp-style UI.",
                        color = Color.White.copy(alpha = 0.94f),
                        textAlign = TextAlign.Center,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        MiniPreviewPhone("Splash", "Secure")
                        MiniPreviewPhone("Chats", "Realtime")
                        MiniPreviewPhone("Chat", "Bubbles")
                        MiniPreviewPhone("Call", "Live", dark = true)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 12.dp),
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {                    when (viewModel.loginFlowStep) {
                        LoginFlowStep.CHOICE -> {
                            Text("Temporary login", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF111B21))
                            Text("Ab app me sirf anonymous temporary account rahega. Username + password se account banao ya wapas login karo.", color = WaMutedText, style = MaterialTheme.typography.bodyMedium)
                            Button(
                                onClick = { viewModel.openAnonymousUsernameStep() },
                                enabled = !viewModel.isAuthBusy,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = Color.White),
                            ) {
                                Text(if (viewModel.isAuthBusy) "Please wait…" else "Create temporary account")
                            }
                            OutlinedButton(
                                onClick = { viewModel.openTemporaryLoginStep() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                border = BorderStroke(1.dp, Color(0xFFD7E7DF)),
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 2.dp)) {
                                    Text("Log in temporary account", color = WaDarkGreen, fontWeight = FontWeight.SemiBold)
                                    Text("Username + password", color = WaMutedText, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        LoginFlowStep.EMAIL_LOGIN -> {
                            Text("Email login removed", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF111B21))
                            Text("Ab app me sirf anonymous temporary login available hai.", color = WaMutedText, style = MaterialTheme.typography.bodySmall)
                            Button(
                                onClick = { viewModel.openAnonymousUsernameStep() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = Color.White),
                            ) {
                                Text("Create temporary account")
                            }
                            OutlinedButton(
                                onClick = { viewModel.openTemporaryLoginStep() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                            ) {
                                Text("Log in temporary account")
                            }
                        }
                        LoginFlowStep.ANONYMOUS_USERNAME -> {
                            Text("Create temporary account", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF111B21))
                            Text("Username aur password daalte hi temporary account banega. Logout ke 30 days baad delete hoga.", color = WaMutedText, style = MaterialTheme.typography.bodySmall)
                            OutlinedTextField(
                                value = viewModel.usernameInput,
                                onValueChange = { viewModel.usernameInput = it },
                                label = { Text("Username") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(18.dp),
                                supportingText = { Text("Isi username se friends aapko dhoondh payenge") },
                                colors = loginFieldColors(),
                            )
                            OutlinedTextField(
                                value = viewModel.authPasswordInput,
                                onValueChange = { viewModel.authPasswordInput = it.take(64) },
                                label = { Text("Password") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(18.dp),
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                colors = loginFieldColors(),
                            )
                            Button(
                                onClick = { viewModel.completeAnonymousLogin() },
                                enabled = !viewModel.isAuthBusy,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = Color.White),
                            ) {
                                Text(if (viewModel.isAuthBusy) "Creating…" else "Continue")
                            }
                            TextButton(onClick = { viewModel.openTemporaryLoginStep() }, modifier = Modifier.fillMaxWidth()) { Text("You have an account?") }
                            TextButton(onClick = { viewModel.backToLoginChoice() }, modifier = Modifier.fillMaxWidth()) { Text("Back") }
                        }
                        LoginFlowStep.TEMPORARY_LOGIN -> {
                            Text("Log in temporary account", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF111B21))
                            Text("Apna username aur password daalo. Login hote hi purana account wapas open ho jayega.", color = WaMutedText, style = MaterialTheme.typography.bodySmall)
                            OutlinedTextField(
                                value = viewModel.usernameInput,
                                onValueChange = { viewModel.usernameInput = it },
                                label = { Text("Username") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(18.dp),
                                colors = loginFieldColors(),
                            )
                            OutlinedTextField(
                                value = viewModel.authPasswordInput,
                                onValueChange = { viewModel.authPasswordInput = it.take(64) },
                                label = { Text("Password") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(18.dp),
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                colors = loginFieldColors(),
                            )
                            Button(
                                onClick = { viewModel.loginTemporaryAccount() },
                                enabled = !viewModel.isAuthBusy,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = Color.White),
                            ) {
                                Text(if (viewModel.isAuthBusy) "Logging in…" else "Log in")
                            }
                            TextButton(onClick = { viewModel.openAnonymousUsernameStep() }, modifier = Modifier.fillMaxWidth()) { Text("Create new temporary account") }
                            TextButton(onClick = { viewModel.backToLoginChoice() }, modifier = Modifier.fillMaxWidth()) { Text("Back") }
                        }
                    }
                    if (viewModel.authStatusMessage.isNotBlank()) {
                        Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFFF5FAF7)) {
                            Text(viewModel.authStatusMessage, modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), color = WaDarkGreen, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(
    viewModel: ChatAppViewModel,
    onOpenStatusPicker: (MessageContentType) -> Unit,
    onOpenThemeSettings: () -> Unit,
    onOpenProfileBackgroundSettings: () -> Unit,
    onScanProfileQr: () -> Unit,
    onPickQrFromGallery: () -> Unit,
) {
    val homeLayout = rememberHomeLayoutMetrics()
    val colors = MaterialTheme.colorScheme
    BackHandler(enabled = viewModel.hasHomeConversationSelection) {
        viewModel.clearHomeConversationSelection()
    }
    val darkHome = viewModel.chatPreferences.darkMode
    val homeBackground = if (darkHome) Color(0xFF0B141A) else Color(0xFFF0F2F5)
    val headerSurfaceColor = if (darkHome) DarkPanel else WaDarkGreen
    val headerContentColor = Color.White.copy(alpha = 0.98f)
    val headerSecondaryColor = if (darkHome) Color(0xFFB6C6CF) else Color.White.copy(alpha = 0.82f)
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showLinkedDevicesDialog by remember { mutableStateOf(false) }
    var showProfileQrDialog by remember { mutableStateOf(false) }
    var showQrScanOptions by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(
                color = headerSurfaceColor,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (viewModel.currentTab == HomeTab.CHATS && viewModel.hasHomeConversationSelection) {
                            IconButton(onClick = viewModel::clearHomeConversationSelection) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel selection", tint = headerContentColor)
                            }
                            Text(
                                text = viewModel.selectedHomeConversation?.title ?: "1 selected",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = headerContentColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(onClick = viewModel::pinSelectedHomeConversation) {
                                Text("Pin", color = headerContentColor, fontWeight = FontWeight.SemiBold)
                            }
                            TextButton(onClick = viewModel::deleteSelectedHomeConversationForMe) {
                                Text("Delete", color = headerContentColor, fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            Text(
                                text = when (viewModel.currentTab) {
                                    HomeTab.CHATS -> "Chats"
                                    HomeTab.UPDATES -> "Updates"
                                    HomeTab.CALLS -> "Calls"
                                    HomeTab.SETTINGS -> "Global"
                                },
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = headerContentColor,
                                modifier = Modifier.weight(1f),
                            )
                            when (viewModel.currentTab) {
                                HomeTab.CHATS -> {
                                    IconButton(onClick = { showProfileQrDialog = true }) {
                                        Icon(Icons.Default.QrCode, contentDescription = "My profile QR", tint = headerContentColor)
                                    }
                                    IconButton(onClick = { showQrScanOptions = true }) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = "Scan profile QR", tint = headerContentColor)
                                    }
                                }
                                HomeTab.UPDATES -> {
                                    IconButton(onClick = { }) {
                                        Icon(Icons.Default.Search, contentDescription = null, tint = headerContentColor)
                                    }
                                }
                                HomeTab.CALLS, HomeTab.SETTINGS -> {
                                    IconButton(onClick = { }) {
                                        Icon(Icons.Default.Search, contentDescription = null, tint = headerContentColor)
                                    }
                                }
                            }
                            Box {
                                IconButton(onClick = { showOverflowMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = headerContentColor)
                                }
                                DropdownMenu(
                                    expanded = showOverflowMenu,
                                    onDismissRequest = { showOverflowMenu = false },
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Settings") },
                                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                        onClick = {
                                            showOverflowMenu = false
                                            showSettingsDialog = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Profile") },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                        onClick = {
                                            showOverflowMenu = false
                                            viewModel.openProfileEditor()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Linked devices") },
                                        leadingIcon = { Icon(Icons.Default.Devices, contentDescription = null) },
                                        onClick = {
                                            showOverflowMenu = false
                                            showLinkedDevicesDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                    if (viewModel.currentTab == HomeTab.CHATS && !viewModel.hasHomeConversationSelection) {
                        Surface(
                            shape = RoundedCornerShape(28.dp),
                            color = if (darkHome) DarkPanelSoft else Color.White,
                            tonalElevation = 0.dp,
                            shadowElevation = if (darkHome) 0.dp else 2.dp,
                            border = if (darkHome) null else BorderStroke(1.dp, Color(0xFFE4E8EB)),
                        ) {
                            OutlinedTextField(
                                value = viewModel.searchQuery,
                                onValueChange = viewModel::updateSearchQuery,
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(
                                        "Search Username Here",
                                        color = if (darkHome) headerSecondaryColor else WaMutedText,
                                    )
                                },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = if (darkHome) headerSecondaryColor else WaMutedText)
                                },
                                shape = RoundedCornerShape(28.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = if (darkHome) headerContentColor else Color(0xFF111B21),
                                    unfocusedTextColor = if (darkHome) headerContentColor else Color(0xFF111B21),
                                    disabledTextColor = if (darkHome) headerSecondaryColor else WaMutedText,
                                    cursorColor = if (darkHome) headerContentColor else WaDarkGreen,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    disabledBorderColor = Color.Transparent,
                                    errorBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                ),
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = if (darkHome) DarkPanel else Color.White,
                tonalElevation = 0.dp,
            ) {
                listOf(HomeTab.CHATS, HomeTab.UPDATES, HomeTab.SETTINGS, HomeTab.CALLS).forEach { tab ->
                    val icon = when (tab) {
                        HomeTab.CHATS -> Icons.AutoMirrored.Filled.Message
                        HomeTab.UPDATES -> Icons.Default.AutoAwesome
                        HomeTab.CALLS -> Icons.Default.Call
                        HomeTab.SETTINGS -> Icons.Default.Groups
                    }
                    val label = when (tab) {
                        HomeTab.CHATS -> "Chats"
                        HomeTab.UPDATES -> "Updates"
                        HomeTab.CALLS -> "Calls"
                        HomeTab.SETTINGS -> "Global"
                    }
                    val unreadCount = if (tab == HomeTab.CHATS) viewModel.visibleConversations.sumOf { it.unreadCount } else 0
                    NavigationBarItem(
                        selected = viewModel.currentTab == tab,
                        onClick = {
                            if (tab == HomeTab.SETTINGS) {
                                viewModel.openGlobalChat()
                            } else {
                                viewModel.updateCurrentTab(tab)
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = if (darkHome) Color(0xFFDFFFE9) else WaDarkGreen,
                            selectedTextColor = if (darkHome) Color(0xFFDFFFE9) else WaDarkGreen,
                            indicatorColor = if (darkHome) Color(0xFF103D33) else Color(0x1A128C7E),
                            unselectedIconColor = if (darkHome) Color(0xFFD6DEE3) else colors.onSurfaceVariant,
                            unselectedTextColor = if (darkHome) Color(0xFFD6DEE3) else colors.onSurfaceVariant,
                        ),
                        icon = {
                            if (unreadCount > 0) {
                                BadgedBox(badge = { Badge { Text(unreadCount.coerceAtMost(99).toString()) } }) {
                                    Icon(icon, contentDescription = null)
                                }
                            } else {
                                Icon(icon, contentDescription = null)
                            }
                        },
                        label = { Text(label) },
                    )
                }
            }
        },
        floatingActionButton = {
            when (viewModel.currentTab) {
                HomeTab.CHATS -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.End,
                    ) {
                        FloatingActionButton(
                            onClick = { viewModel.openGlobalChat() },
                            containerColor = if (darkHome) Color(0xFF1B2730) else Color.White,
                            contentColor = AccentBlue,
                            modifier = Modifier.size(56.dp),
                        ) {
                            Icon(Icons.Default.Groups, contentDescription = null)
                        }
                        FloatingActionButton(
                            onClick = { viewModel.createNewContactChat() },
                            containerColor = WaUnreadGreen,
                            contentColor = Color(0xFF04130E),
                            modifier = Modifier.size(64.dp),
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    }
                }
                HomeTab.UPDATES -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.End,
                    ) {
                        FloatingActionButton(
                            onClick = { viewModel.prepareTextStatusForAudience() },
                            containerColor = if (darkHome) Color(0xFF1B2730) else Color.White,
                            contentColor = colors.onSurface,
                            modifier = Modifier.size(56.dp),
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                        FloatingActionButton(
                            onClick = { viewModel.openStatusMediaPickerDialog() },
                            containerColor = WaUnreadGreen,
                            contentColor = Color(0xFF04130E),
                            modifier = Modifier.size(64.dp),
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                        }
                    }
                }
                else -> Unit
            }
        },
        containerColor = homeBackground,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .background(homeBackground)
        ) {
            when (viewModel.currentTab) {
                HomeTab.CHATS -> ChatsTab(viewModel, homeLayout)
                HomeTab.UPDATES -> UpdatesTab(viewModel, onOpenStatusPicker, homeLayout)
                HomeTab.CALLS -> CallsTab(viewModel, homeLayout)
                HomeTab.SETTINGS -> CommunitiesTab(viewModel, homeLayout)
            }
        }
    }

    if (showProfileQrDialog) {
        ProfileQrDialog(viewModel = viewModel, onDismiss = { showProfileQrDialog = false })
    }
    if (showQrScanOptions) {
        AlertDialog(
            onDismissRequest = { showQrScanOptions = false },
            title = { Text("Open QR") },
            text = { Text("Phone ka original camera open hoga ya gallery se QR image choose kar sakte ho.") },
            confirmButton = {
                Button(
                    onClick = {
                        showQrScanOptions = false
                        onScanProfileQr()
                    }
                ) { Text("Open original camera") }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        showQrScanOptions = false
                        onPickQrFromGallery()
                    }) { Text("Pick from gallery") }
                    TextButton(onClick = { showQrScanOptions = false }) { Text("Cancel") }
                }
            },
        )
    }
    if (showSettingsDialog) {
        AppSettingsDialog(
            viewModel = viewModel,
            onDismiss = { showSettingsDialog = false },
            onOpenThemeSettings = onOpenThemeSettings,
            onOpenProfileBackgroundSettings = onOpenProfileBackgroundSettings,
            onLogout = {
                showSettingsDialog = false
                viewModel.logout()
            },
        )
    }
    if (showLinkedDevicesDialog) {
        LinkedDevicesDialog(viewModel = viewModel, onDismiss = { showLinkedDevicesDialog = false })
    }
}


private fun importCustomChatThemeImage(context: Context, sourceUri: Uri): Uri? {
    return runCatching {
        val mimeType = context.contentResolver.getType(sourceUri).orEmpty()
        val extension = when {
            mimeType.contains("png", ignoreCase = true) -> "png"
            mimeType.contains("webp", ignoreCase = true) -> "webp"
            mimeType.contains("gif", ignoreCase = true) -> "gif"
            else -> MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"
        }
        val targetDir = File(context.filesDir, "chat_themes").apply { mkdirs() }
        val targetFile = File(targetDir, "custom_theme.$extension")
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            targetFile.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        targetFile.toUri()
    }.getOrNull()
}

private fun decodeQrPayloadFromImage(context: Context, uri: Uri): String? {
    val bitmap = loadBitmapForQr(context, uri) ?: return null
    val width = bitmap.width
    val height = bitmap.height
    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
    val source = RGBLuminanceSource(width, height, pixels)
    val hints = mapOf(
        DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
        DecodeHintType.TRY_HARDER to true,
    )
    val readers = listOf(
        BinaryBitmap(HybridBinarizer(source)),
        BinaryBitmap(GlobalHistogramBinarizer(source)),
    )
    for (binary in readers) {
        runCatching {
            MultiFormatReader().apply { setHints(hints) }.decode(binary).text
        }.getOrNull()?.let { return it }
    }
    return null
}

private fun loadBitmapForQr(context: Context, uri: Uri): Bitmap? =
    runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = false
                decoder.setTargetSampleSize(1)
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    }.getOrNull()

private fun createHighQualityQrCaptureUri(context: Context): Uri? =
    runCatching {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File.createTempFile("qr_capture_${timeStamp}_", ".jpg", context.cacheDir)
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }.getOrNull()

private data class HomeLayoutMetrics(
    val headerHorizontalPadding: Dp,
    val headerVerticalPadding: Dp,
    val listHorizontalPadding: Dp,
    val topContentPadding: Dp,
    val bottomContentPadding: Dp,
    val sectionSpacing: Dp,
    val firstBlockPadding: Dp,
)

@Composable
private fun rememberHomeLayoutMetrics(): HomeLayoutMetrics {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp

    val headerHorizontalPadding = when {
        screenWidth >= 840 -> 28.dp
        screenWidth >= 600 -> 22.dp
        else -> 16.dp
    }
    val listHorizontalPadding = when {
        screenWidth >= 840 -> 26.dp
        screenWidth >= 600 -> 20.dp
        else -> 12.dp
    }
    val topContentPadding = if (screenHeight < 700) 8.dp else 12.dp
    val bottomContentPadding = if (screenHeight < 700) 88.dp else 96.dp
    val sectionSpacing = if (screenWidth >= 600) 12.dp else 10.dp
    val firstBlockPadding = if (screenWidth >= 600) 8.dp else 4.dp

    return HomeLayoutMetrics(
        headerHorizontalPadding = headerHorizontalPadding,
        headerVerticalPadding = 12.dp,
        listHorizontalPadding = listHorizontalPadding,
        topContentPadding = topContentPadding,
        bottomContentPadding = bottomContentPadding,
        sectionSpacing = sectionSpacing,
        firstBlockPadding = firstBlockPadding,
    )
}

@Composable
private fun homeTabContentPadding(layout: HomeLayoutMetrics): PaddingValues = PaddingValues(
    start = layout.listHorizontalPadding,
    top = layout.topContentPadding,
    end = layout.listHorizontalPadding,
    bottom = layout.bottomContentPadding,
)

@Composable
private fun ChatsTab(viewModel: ChatAppViewModel, layout: HomeLayoutMetrics) {
    val colors = MaterialTheme.colorScheme
    val darkHome = viewModel.chatPreferences.darkMode

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = homeTabContentPadding(layout),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HomeFilterPill(
                        label = "All",
                        selected = viewModel.selectedFilter == ChatFilter.ALL,
                        onClick = { viewModel.selectFilter(ChatFilter.ALL) },
                    )
                    HomeFilterPill(
                        label = "Unread",
                        selected = viewModel.selectedFilter == ChatFilter.UNREAD,
                        onClick = { viewModel.selectFilter(ChatFilter.UNREAD) },
                    )
                    HomeFilterPill(
                        label = "Favorites",
                        selected = viewModel.selectedFilter == ChatFilter.STARRED,
                        onClick = { viewModel.selectFilter(ChatFilter.STARRED) },
                    )
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (darkHome) Color.Transparent else Color.White,
                        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.35f)),
                        shadowElevation = if (darkHome) 0.dp else 1.dp,
                        modifier = Modifier.clickable { viewModel.openGlobalChat() },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = if (darkHome) colors.onSurface else WaDarkGreen,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                    }
                }

                AnimatedVisibility(viewModel.searchQuery.isNotBlank()) {
                    Surface(
                        color = if (darkHome) Color.Transparent else Color.White,
                        shape = RoundedCornerShape(20.dp),
                        border = if (darkHome) null else BorderStroke(1.dp, Color(0xFFE4E8EB)),
                        shadowElevation = if (darkHome) 0.dp else 1.dp,
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            val quickSearchInfoColor = if (darkHome) Color(0xFFB6C6CF) else colors.onSurfaceVariant
                            when {
                                viewModel.isQuickUserSearchBusy -> {
                                    Text(
                                        "Searching…",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = quickSearchInfoColor,
                                        modifier = Modifier.padding(vertical = 6.dp),
                                    )
                                }
                                viewModel.quickUserSearchResults.isNotEmpty() -> {
                                    viewModel.quickUserSearchResults.forEachIndexed { index, result ->
                                        QuickUserSearchRow(result = result, onOpen = { viewModel.openUserProfileFromSearch(result) })
                                        if (index != viewModel.quickUserSearchResults.lastIndex) {
                                            HorizontalDivider(color = colors.outline.copy(alpha = 0.14f))
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                }
                                viewModel.backendConnected -> {
                                    Text(
                                        "No users found for '${viewModel.searchQuery}'.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = quickSearchInfoColor,
                                        modifier = Modifier.padding(vertical = 6.dp),
                                    )
                                }
                                else -> {
                                    Text(
                                        "Search works when realtime sync is connected.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = quickSearchInfoColor,
                                        modifier = Modifier.padding(vertical = 6.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        if (viewModel.visibleConversations.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = if (darkHome) DarkPanelSoft else Color.White,
                    border = if (darkHome) null else BorderStroke(1.dp, Color(0xFFE4E8EB)),
                    modifier = Modifier.padding(top = 12.dp),
                ) {
                    Text(
                        "No chats yet",
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                    )
                }
            }
        }
        items(viewModel.visibleConversations, key = { it.id }) { convo ->
            ConversationRow(
                conversation = convo,
                selected = viewModel.selectedHomeConversationId == convo.id,
                selectionMode = viewModel.hasHomeConversationSelection,
                onOpen = { viewModel.handleHomeConversationTap(convo.id) },
                onSelect = { viewModel.selectHomeConversation(convo.id) },
            )
        }
    }
}

@Composable
private fun UpdatesTab(viewModel: ChatAppViewModel, onOpenStatusPicker: (MessageContentType) -> Unit, layout: HomeLayoutMetrics) {
    val colors = MaterialTheme.colorScheme
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = homeTabContentPadding(layout),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Updates",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onBackground,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    text = "Share photos, videos and text updates with your chats.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }
        item {
            MyStatusRow(
                avatarSeed = viewModel.currentUser.avatarSeed,
                photoUrl = viewModel.currentUser.photoUrl,
                darkMode = viewModel.chatPreferences.darkMode,
                onClick = { viewModel.openStatusMediaPickerDialog() },
            )
        }
        if (viewModel.statusStories.isNotEmpty()) {
            item {
                Text(
                    text = "Recent updates",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                    modifier = Modifier.padding(top = 6.dp, bottom = 2.dp),
                )
            }
            items(viewModel.statusStories, key = { it.id }) { story ->
                StatusRow(story = story, onOpen = { viewModel.openStatusViewer(story.id) })
            }
        } else {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (viewModel.chatPreferences.darkMode) DarkPanelSoft else Color.White,
                    border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.12f)),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text("No recent updates", color = colors.onSurface, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Tap My status to share a photo, video or text update.",
                            color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MyStatusRow(
    avatarSeed: String,
    photoUrl: String,
    darkMode: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (darkMode) DarkPanelSoft else Color.White,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(if (darkMode) DarkPanel else Color(0xFFEAF5EE), if (darkMode) DarkPanelSoft else Color(0xFFD9FDD3)))),
                    contentAlignment = Alignment.Center,
                ) {
                    AvatarPhoto(seed = avatarSeed, photoUrl = photoUrl, fallbackColor = Color.Transparent, textColor = if (darkMode) Color.White else WaDarkGreen)
                }
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(WaUnreadGreen)
                        .border(2.dp, if (darkMode) DarkPanelSoft else Color.White, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF04130E), modifier = Modifier.size(14.dp))
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("My status", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "Tap to add status update",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = WaDarkGreen)
        }
    }
}

@Composable
private fun StatusStoryCard(
    story: StatusStoryUi,
    onOpen: () -> Unit,
) {
    val ringColor = if (story.viewed) Color(0xFF81919A) else WaUnreadGreen
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent,
        modifier = Modifier
            .width(182.dp)
            .height(248.dp)
            .clickable(onClick = onOpen),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF24303A),
                            Color(0xFF1A232B),
                            Color(0xFF0E1419),
                        )
                    )
                )
        ) {
            if (story.contentType == MessageContentType.IMAGE && story.mediaUrl.isNotBlank()) {
                AsyncImage(
                    model = story.mediaUrl,
                    contentDescription = story.authorName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = if (story.mediaUrl.isNotBlank()) 0.26f else 0.16f))
            )
            Box(
                modifier = Modifier
                    .padding(start = 12.dp, top = 12.dp)
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(ringColor),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F1A20)),
                    contentAlignment = Alignment.Center,
                ) {
                    AvatarPhoto(seed = story.avatarSeed, photoUrl = story.photoUrl, fallbackColor = Color.Transparent, textColor = Color.White)
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = story.authorName,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (story.contentType == MessageContentType.VIDEO) {
                        BadgeBoxText("Video")
                    }
                }
                if (story.content.isNotBlank()) {
                    Text(
                        text = story.content,
                        color = Color.White.copy(alpha = 0.88f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = story.privacyLabel,
                    color = Color.White.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun CallsTab(viewModel: ChatAppViewModel, layout: HomeLayoutMetrics) {
    val colors = MaterialTheme.colorScheme
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = homeTabContentPadding(layout),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Calls",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onBackground,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    text = "Start calls and see your recent call history.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (viewModel.chatPreferences.darkMode) DarkPanelSoft else Color.White,
                border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.12f)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Surface(shape = CircleShape, color = Color(0xFFD9FDD3)) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = WaDarkGreen,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("Create call link", color = colors.onSurface, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Share a link for your voice or video call.",
                            color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    TextButton(onClick = { viewModel.linkDesktopDevice() }) {
                        Text("Create", color = WaDarkGreen, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        item {
            Text(
                text = "Recent",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
            )
        }
        if (viewModel.callLogs.isNotEmpty()) {
            items(viewModel.callLogs, key = { it.id }) { log ->
                CallRow(log)
            }
        } else {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (viewModel.chatPreferences.darkMode) DarkPanelSoft else Color.White,
                    border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.12f)),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text("No recent calls", color = colors.onSurface, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Call history will appear here after your first voice or video call.",
                            color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommunitiesTab(viewModel: ChatAppViewModel, layout: HomeLayoutMetrics) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(homeTabContentPadding(layout)),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.10f)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    Icons.Default.Groups,
                    contentDescription = null,
                    tint = WaUnreadGreen,
                    modifier = Modifier.size(42.dp),
                )
                Text(
                    text = "Global chat",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                )
                Text(
                    text = "Open one public room and talk with everyone instantly.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = { viewModel.openGlobalChat() },
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WaUnreadGreen, contentColor = Color(0xFF04130E)),
                ) {
                    Text("Open global chat")
                }
            }
        }
    }
}

@Composable
private fun ChatScreen(
    viewModel: ChatAppViewModel,
    onBack: () -> Unit,
    onOpenAttachmentPicker: () -> Unit,
    onRequestVoiceNote: () -> Unit,
    onStopVoiceNote: () -> Unit,
    onCancelVoiceNote: () -> Unit,
    onOpenPicker: (MessageContentType) -> Unit,
) {
    val conversation = viewModel.selectedConversation ?: return
    val colors = MaterialTheme.colorScheme
    val darkMode = viewModel.chatPreferences.darkMode
    val themePalette = chatThemePalette(viewModel.chatPreferences.chatThemeKey, darkMode)
    val customThemeImageUri = viewModel.chatPreferences.customThemeImageUri
    val chatTopBarColor = themePalette.topBarColor
    val chatBackground = themePalette.chatBaseColor
    val composerBarColor = themePalette.composerBarColor
    val composerFieldColor = themePalette.composerFieldColor
    val topTextColor = Color.White
    val secondaryTopTextColor = if (darkMode) Color(0xFF98AAB5) else Color.White.copy(alpha = 0.82f)
    val messageListState = rememberLazyListState()
    var showChatMenu by remember { mutableStateOf(false) }

    LaunchedEffect(conversation.id, viewModel.selectedMessages.lastOrNull()?.id) {
        val lastMessageIndex = viewModel.selectedMessages.lastIndex
        if (lastMessageIndex >= 0) {
            messageListState.animateScrollToItem(lastMessageIndex + 2)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(
                color = chatTopBarColor,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(58.dp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = topTextColor)
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (darkMode) Color(0xFF233138) else Color.White.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        AvatarPhoto(seed = conversation.avatarSeed, photoUrl = conversation.photoUrl, fallbackColor = Color.Transparent, textColor = Color.White)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(1.dp),
                    ) {
                        VerifiedNameText(
                            name = conversation.title,
                            goldenVerified = conversation.goldenVerified,
                            color = topTextColor,
                            textStyle = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            badgeCompact = true,
                        )
                        Text(
                            viewModel.selectedConversationHeaderStatus(),
                            color = secondaryTopTextColor,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    IconButton(onClick = { viewModel.startCall(CallType.VIDEO) }) {
                        Icon(Icons.Default.Videocam, contentDescription = null, tint = topTextColor)
                    }
                    IconButton(onClick = { viewModel.startCall(CallType.AUDIO) }) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = topTextColor)
                    }
                    Box {
                        IconButton(onClick = { showChatMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = null, tint = topTextColor)
                        }
                        DropdownMenu(expanded = showChatMenu, onDismissRequest = { showChatMenu = false }) {
                            DropdownMenuItem(text = { Text("View contact") }, onClick = { showChatMenu = false })
                            DropdownMenuItem(text = { Text("Search") }, onClick = { showChatMenu = false })
                            DropdownMenuItem(text = { Text("Wallpaper") }, onClick = { showChatMenu = false })
                        }
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(composerBarColor)
                    .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
                    .imePadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                AnimatedVisibility(visible = viewModel.replyPreview != null) {
                    ReplyPreviewStrip(
                        text = viewModel.replyPreview.orEmpty(),
                        onClose = { viewModel.clearReplyPreview() },
                    )
                }
                AnimatedVisibility(visible = viewModel.isVoiceRecording) {
                    VoiceRecordingStrip(
                        elapsed = viewModel.formattedVoiceRecordingElapsed(),
                        onCancel = onCancelVoiceNote,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(28.dp),
                        color = composerFieldColor,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedTextField(
                                value = viewModel.composerText,
                                onValueChange = viewModel::updateComposerText,
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Type a message") },
                                maxLines = 5,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    disabledBorderColor = Color.Transparent,
                                    errorBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    cursorColor = if (darkMode) Color.White else WaDarkGreen,
                                    focusedTextColor = if (darkMode) Color.White else Color(0xFF111B21),
                                    unfocusedTextColor = if (darkMode) Color.White else Color(0xFF111B21),
                                    focusedPlaceholderColor = if (darkMode) Color(0xFF95A7B0) else WaMutedText,
                                    unfocusedPlaceholderColor = if (darkMode) Color(0xFF95A7B0) else WaMutedText,
                                ),
                            )
                            IconButton(onClick = onOpenAttachmentPicker) {
                                Icon(Icons.Default.AttachFile, contentDescription = null, tint = if (darkMode) Color(0xFFB7C7CE) else WaMutedText)
                            }
                            IconButton(onClick = { onOpenPicker(MessageContentType.IMAGE) }) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = if (darkMode) Color(0xFFB7C7CE) else WaMutedText)
                            }
                        }
                    }
                    FloatingActionButton(
                        onClick = {
                            when {
                                viewModel.composerText.isNotBlank() -> viewModel.sendTextMessage()
                                viewModel.isVoiceRecording -> onStopVoiceNote()
                                else -> onRequestVoiceNote()
                            }
                        },
                        containerColor = WaUnreadGreen,
                        contentColor = Color(0xFF08131A),
                    ) {
                        Icon(
                            imageVector = when {
                                viewModel.composerText.isNotBlank() -> Icons.AutoMirrored.Filled.Send
                                viewModel.isVoiceRecording -> Icons.Default.Done
                                else -> Icons.Default.Mic
                            },
                            contentDescription = null,
                        )
                    }
                }
            }
        },
        containerColor = chatBackground,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .background(chatBackground)
        ) {
            if (viewModel.chatPreferences.chatThemeKey == "CUSTOM" && customThemeImageUri.isNotBlank()) {
                AsyncImage(
                    model = customThemeImageUri,
                    contentDescription = "Custom chat theme background",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(alpha = if (darkMode) 0.18f else 0.28f),
                    contentScale = ContentScale.Crop,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(themePalette.chatBackgroundBrush)
            )
            LazyColumn(
                state = messageListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.Bottom),
                reverseLayout = false,
            ) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (darkMode) Color(0xFF182229) else Color(0xFFF7E7B6),
                        ) {
                            Text(
                                text = if (viewModel.isMediaBusy) viewModel.mediaActionStatus else conversation.lastActive,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (darkMode) Color(0xFF8FA1AA) else Color(0xFF5C4B2D),
                            )
                        }
                    }
                }
                items(viewModel.selectedMessages, key = { it.id }) { message ->
                    MessageBubble(
                        message = message,
                        isMine = message.senderId == viewModel.currentUser.id,
                        showSender = conversation.type == ConversationType.GROUP,
                        darkMode = darkMode,
                    )
                }
                item(key = "chat_bottom_spacer") {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ReplyPreviewStrip(
    text: String,
    onClose: () -> Unit,
) {
    val darkMode = MaterialTheme.colorScheme.background.luminance() < 0.2f
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (darkMode) DarkPanelSoft else Color.White,
        border = if (darkMode) null else BorderStroke(1.dp, Color(0xFFE4E8EB)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(34.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (darkMode) AccentPurple else WaDarkGreen)
            )
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (darkMode) Color.White else Color(0xFF111B21),
                style = MaterialTheme.typography.bodySmall,
            )
            TextButton(onClick = onClose) {
                Text("Close", color = if (darkMode) Color(0xFFB7C7CE) else WaMutedText)
            }
        }
    }
}

@Composable
private fun HomeHeroCard(
    viewModel: ChatAppViewModel,
    layout: HomeLayoutMetrics,
) {
    val unreadTotal = viewModel.visibleConversations.sumOf { it.unreadCount }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = layout.listHorizontalPadding)
            .animateContentSize(),
        shape = RoundedCornerShape(30.dp),
        color = Color.White.copy(alpha = 0.12f),
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color.White.copy(alpha = 0.26f), Color.White.copy(alpha = 0.10f)))),
                        contentAlignment = Alignment.Center,
                    ) {
                        AvatarSeed(seed = viewModel.currentUser.avatarSeed, color = Color.White.copy(alpha = 0.18f), textColor = Color.White)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(viewModel.currentUser.name, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = if (viewModel.backendConnected) "Connected to realtime sync" else "Using local cache fallback",
                            color = Color.White.copy(alpha = 0.72f),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                BadgeBoxText(if (viewModel.backendConnected) "Secure online" else "Cache mode")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                HomeStatPill(modifier = Modifier.weight(1f), value = viewModel.visibleConversations.size.toString(), label = "Chats")
                HomeStatPill(modifier = Modifier.weight(1f), value = unreadTotal.toString(), label = "Unread")
                HomeStatPill(modifier = Modifier.weight(1f), value = viewModel.statusStories.size.toString(), label = "Updates")
            }
        }
    }
}

@Composable
private fun HomeStatPill(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = 0.12f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(label, color = Color.White.copy(alpha = 0.72f), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun HomeInfoTile(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF6FBF8),
        border = BorderStroke(1.dp, Color(0xFFE3EFE9)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(shape = CircleShape, color = Color(0xFFDFF6EC)) {
                Icon(icon, contentDescription = null, modifier = Modifier.padding(8.dp), tint = WaDarkGreen)
            }
            Text(title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = WaMutedText)
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF13221F))
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = WaMutedText)
    }
}

@Composable
private fun GoldenVerificationBadge(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = VerificationGoldSoft,
        border = BorderStroke(1.dp, VerificationGold.copy(alpha = 0.55f)),
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Golden verified",
            tint = VerificationGold,
            modifier = Modifier.padding(if (compact) 3.dp else 4.dp).size(if (compact) 12.dp else 14.dp),
        )
    }
}

@Composable
private fun VerifiedNameText(
    name: String,
    goldenVerified: Boolean,
    modifier: Modifier = Modifier,
    color: Color,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge,
    fontWeight: FontWeight? = null,
    maxLines: Int = 1,
    textAlign: TextAlign? = null,
    badgeCompact: Boolean = false,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = horizontalArrangement,
    ) {
        Text(
            text = name,
            color = color,
            style = textStyle,
            fontWeight = fontWeight,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            textAlign = textAlign,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (goldenVerified) {
            Spacer(modifier = Modifier.width(6.dp))
            GoldenVerificationBadge(compact = badgeCompact)
        }
    }
}

@Composable
private fun QuickUserSearchRow(
    result: ContactSearchUi,
    onOpen: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        color = Color.Transparent,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF1D3A46), Color(0xFF122631)))),
                contentAlignment = Alignment.Center,
            ) {
                AvatarPhoto(
                    seed = result.displayName.take(1).ifBlank { "U" },
                    photoUrl = result.photoUrl,
                    fallbackColor = Color.Transparent,
                    textColor = Color.White,
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                VerifiedNameText(
                    name = result.displayName,
                    goldenVerified = result.goldenVerified,
                    color = colors.onSurface,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    badgeCompact = true,
                )
                Text(
                    text = listOfNotNull(result.username.takeIf { it.isNotBlank() }?.let { "@$it" }, result.phoneNumber.takeIf { it.isNotBlank() }).joinToString(" • ").ifBlank { "Anonymous user" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (result.online) {
                Surface(shape = CircleShape, color = WaUnreadGreen) {
                    Spacer(modifier = Modifier.size(10.dp))
                }
            }
        }
    }
}

@Composable
private fun ConversationRow(
    conversation: ConversationUi,
    selected: Boolean,
    selectionMode: Boolean,
    onOpen: () -> Unit,
    onSelect: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val isGlobalConversation = conversation.id == "global_chat_room"
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(conversation.id, selectionMode) {
                detectTapGestures(
                    onPress = {
                        val releasedBeforeTimeout = withTimeoutOrNull(3000) {
                            tryAwaitRelease()
                        } != null
                        if (releasedBeforeTimeout) {
                            if (selectionMode) onSelect() else onOpen()
                        } else {
                            onSelect()
                            tryAwaitRelease()
                        }
                    }
                )
            },
        color = if (selected) colors.primary.copy(alpha = 0.10f) else Color.Transparent,
        shape = RoundedCornerShape(16.dp),
        border = if (selected) BorderStroke(1.dp, colors.primary.copy(alpha = 0.24f)) else null,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    if (conversation.type == ConversationType.GROUP) Color(0xFF17323A) else Color(0xFF1E4850),
                                    if (conversation.type == ConversationType.GROUP) Color(0xFF0D2420) else Color(0xFF11343E)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    AvatarPhoto(
                        seed = conversation.avatarSeed,
                        photoUrl = conversation.photoUrl,
                        fallbackColor = Color.Transparent,
                        textColor = Color.White,
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        VerifiedNameText(
                            name = conversation.title,
                            goldenVerified = conversation.goldenVerified,
                            modifier = Modifier.weight(1f),
                            color = colors.onSurface,
                            textStyle = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            badgeCompact = true,
                        )
                        Text(
                            conversation.lastActive,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (conversation.unreadCount > 0) WaDarkGreen else colors.onSurfaceVariant,
                            fontWeight = if (conversation.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = conversation.subtitle.ifBlank { "Tap to start chatting" },
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isGlobalConversation) colors.onSurface else colors.onSurfaceVariant,
                            fontWeight = if (isGlobalConversation) FontWeight.Medium else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (selected) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        if (conversation.unreadCount > 0) {
                            Surface(shape = CircleShape, color = WaUnreadGreen) {
                                Text(
                                    text = conversation.unreadCount.coerceAtMost(99).toString(),
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    color = Color(0xFF04130E),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.width(74.dp))
                HorizontalDivider(color = colors.outline.copy(alpha = 0.14f), modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HomeFilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (selected) Color(0xFFD9FDD3) else Color.Transparent,
        border = BorderStroke(1.dp, if (selected) Color(0x8825D366) else colors.outline.copy(alpha = 0.26f)),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            color = if (selected) WaDarkGreen else colors.onSurface,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun StatusRow(story: StatusStoryUi, onOpen: () -> Unit) {
    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = WaCard),
        modifier = Modifier.clickable(onClick = onOpen)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Brush.sweepGradient(listOf(if (story.viewed) Color(0xFFDDE3E7) else StatusRingStart, if (story.viewed) Color(0xFFDDE3E7) else StatusRingEnd, if (story.viewed) Color(0xFFDDE3E7) else StatusRingStart)))
                )
                Box(modifier = Modifier.size(60.dp).clip(CircleShape).background(WaCard), contentAlignment = Alignment.Center) {
                    AvatarPhoto(
                        seed = story.avatarSeed,
                        photoUrl = story.photoUrl,
                        fallbackColor = if (story.viewed) Color(0xFF95A3AC) else WaGreen,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(story.authorName, fontWeight = FontWeight.SemiBold)
                Text(story.content, maxLines = 2, overflow = TextOverflow.Ellipsis, color = WaMutedText)
                Text("${story.timestamp} • ${story.privacyLabel}", style = MaterialTheme.typography.bodySmall, color = WaMutedText)
                if (story.reactions.isNotEmpty()) Text(story.reactions.joinToString(" "), style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (!story.viewed) BadgeBoxText("New")
                if (story.seenViewerNames.isNotEmpty()) Text("${story.seenViewerNames.size} views", style = MaterialTheme.typography.bodySmall, color = WaMutedText)
            }
        }
    }
}

@Composable
private fun CallRow(call: CallRecordUi) {
    ElevatedCard(shape = RoundedCornerShape(24.dp), colors = CardDefaults.elevatedCardColors(containerColor = WaCard)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(DarkPanelSoft, Color.Black))),
                contentAlignment = Alignment.Center,
            ) {
                AvatarPhoto(seed = call.avatarSeed, photoUrl = call.photoUrl, fallbackColor = Color.Transparent)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(call.contactName, fontWeight = FontWeight.SemiBold)
                Text(
                    text = when (call.direction) {
                        CallDirection.INCOMING -> "Incoming • ${call.timestamp}"
                        CallDirection.OUTGOING -> "Outgoing • ${call.timestamp}"
                        CallDirection.MISSED -> "Missed • ${call.timestamp}"
                    },
                    color = if (call.direction == CallDirection.MISSED) Color(0xFFD93025) else WaMutedText,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            BadgeBoxText(call.durationLabel)
            Surface(shape = CircleShape, color = Color(0xFFEBF5EF)) {
                Icon(
                    imageVector = if (call.type == CallType.VIDEO) Icons.Default.Videocam else Icons.Default.Call,
                    contentDescription = null,
                    tint = WaDarkGreen,
                    modifier = Modifier.padding(10.dp),
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessageUi,
    isMine: Boolean,
    showSender: Boolean = false,
    darkMode: Boolean = true,
) {
    val bubbleColor = when {
        darkMode && isMine -> Color(0xFF005C4B)
        darkMode && !isMine -> Color(0xFF202C33)
        isMine -> WaBubbleOut
        else -> WaBubbleIn
    }
    val metaColor = when {
        darkMode && isMine -> Color(0xFFD6F5E8)
        darkMode && !isMine -> Color(0xFFBDD0D8)
        else -> Color(0xFF667781)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 10.dp,
                topEnd = 10.dp,
                bottomStart = if (isMine) 10.dp else 3.dp,
                bottomEnd = if (isMine) 3.dp else 10.dp,
            ),
            color = bubbleColor,
            shadowElevation = if (darkMode) 0.dp else 1.dp,
            tonalElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 330.dp)
                    .padding(start = 10.dp, end = 10.dp, top = 7.dp, bottom = 5.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (showSender && !isMine) {
                    Text(
                        message.senderName,
                        color = if (darkMode) Color(0xFF87D4C0) else WaDarkGreen,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                if (message.replyToSnippet.isNotBlank()) {
                    ReplySnippetCard(reply = message.replyToSnippet, isMine = isMine, darkMode = darkMode)
                }
                MessageContent(message = message, isMine = isMine, darkMode = darkMode)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        message.timestamp,
                        color = metaColor,
                        style = MaterialTheme.typography.labelSmall,
                    )
                    if (isMine) {
                        Spacer(modifier = Modifier.width(4.dp))
                        DeliveryStatusIcon(status = message.deliveryStatus)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReplySnippetCard(
    reply: String,
    isMine: Boolean,
    darkMode: Boolean,
) {
    val name: String
    val body: String
    val parts = reply.split(":", limit = 2)
    if (parts.size == 2) {
        name = parts[0].removePrefix("Replying to ").trim().ifBlank { "Reply" }
        body = parts[1].trim()
    } else {
        name = "Reply"
        body = reply
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                when {
                    darkMode && isMine -> Color(0xFF0E7A61)
                    darkMode -> Color(0xFF182229)
                    isMine -> Color.White.copy(alpha = 0.45f)
                    else -> Color(0xFFF3F7F8)
                }
            )
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(34.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(if (darkMode) AccentPurple else WaDarkGreen)
        )
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(name, color = if (darkMode) Color(0xFFE7D7FF) else WaDarkGreen, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Text(body, color = if (darkMode) Color.White.copy(alpha = 0.88f) else Color(0xFF111B21), style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun MessageContent(
    message: ChatMessageUi,
    isMine: Boolean,
    darkMode: Boolean,
) {
    val primaryText = if (darkMode) Color.White else Color(0xFF111B21)
    val secondaryText = if (darkMode) Color(0xFFBDD0D8) else Color(0xFF667781)
    when (message.contentType) {
        MessageContentType.TEXT, MessageContentType.SYSTEM -> {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    message.body,
                    color = primaryText,
                    style = MaterialTheme.typography.bodyLarge,
                )
                if (message.metadata.isNotBlank()) {
                    Text(
                        message.metadata,
                        color = secondaryText,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
        MessageContentType.IMAGE -> {
            if (message.mediaUrl.isNotBlank()) {
                Column(
                    modifier = Modifier.widthIn(max = 260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AsyncImage(
                        model = message.mediaUrl,
                        contentDescription = message.body,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop,
                    )
                    if (message.metadata.isNotBlank()) {
                        Text(message.metadata, color = secondaryText, style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else {
                RichMessageCard(Icons.Default.Image, message.body, message.metadata, darkMode = darkMode, isMine = isMine)
            }
        }
        MessageContentType.VIDEO -> {
            if (message.mediaUrl.isNotBlank()) {
                OpenableMessageCard(
                    icon = Icons.Default.Videocam,
                    title = message.body,
                    subtitle = message.metadata.ifBlank { "Tap to open video" },
                    darkMode = darkMode,
                    isMine = isMine,
                    mediaUrl = message.mediaUrl,
                    mimeType = "video/*",
                )
            } else {
                RichMessageCard(Icons.Default.Videocam, message.body, message.metadata, darkMode = darkMode, isMine = isMine)
            }
        }
        MessageContentType.DOCUMENT -> {
            if (message.mediaUrl.isNotBlank()) {
                OpenableMessageCard(
                    icon = Icons.Default.Description,
                    title = message.body,
                    subtitle = message.metadata.ifBlank { "Tap to open document" },
                    darkMode = darkMode,
                    isMine = isMine,
                    mediaUrl = message.mediaUrl,
                    mimeType = "*/*",
                )
            } else {
                RichMessageCard(Icons.Default.Description, message.body, message.metadata, darkMode = darkMode, isMine = isMine)
            }
        }
        MessageContentType.VOICE -> {
            if (message.mediaUrl.isNotBlank()) {
                VoiceNoteMessageCard(message = message, darkMode = darkMode, isMine = isMine)
            } else {
                RichMessageCard(Icons.Default.Mic, message.body, message.metadata, darkMode = darkMode, isMine = isMine)
            }
        }
        MessageContentType.LOCATION -> RichMessageCard(Icons.Default.PhoneAndroid, message.body, message.metadata, darkMode = darkMode, isMine = isMine)
        MessageContentType.CONTACT -> RichMessageCard(Icons.Default.Person, message.body, message.metadata, darkMode = darkMode, isMine = isMine)
        MessageContentType.POLL -> RichMessageCard(Icons.Default.Poll, message.body, message.metadata, darkMode = darkMode, isMine = isMine)
    }
}


@Composable
private fun VoiceRecordingStrip(
    elapsed: String,
    onCancel: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0x1AFF3B30),
        border = BorderStroke(1.dp, Color(0x33FF3B30)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFF3B30)))
            Text("Recording voice note • $elapsed", modifier = Modifier.weight(1f), color = Color.White, fontWeight = FontWeight.SemiBold)
            TextButton(onClick = onCancel) { Text("Cancel") }
        }
    }
}

@Composable
private fun OpenableMessageCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    darkMode: Boolean,
    isMine: Boolean,
    mediaUrl: String,
    mimeType: String,
) {
    val context = LocalContext.current
    RichMessageCard(
        icon = icon,
        title = title,
        subtitle = subtitle,
        darkMode = darkMode,
        isMine = isMine,
        onClick = {
            runCatching {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW)
                        .setDataAndType(Uri.parse(mediaUrl), mimeType)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }.onFailure {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(mediaUrl)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        },
    )
}

@Composable
private fun VoiceNoteMessageCard(
    message: ChatMessageUi,
    darkMode: Boolean,
    isMine: Boolean,
) {
    val context = LocalContext.current
    var mediaPlayer by remember(message.id) { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember(message.id) { mutableStateOf(false) }

    DisposableEffect(message.id) {
        onDispose {
            runCatching { mediaPlayer?.release() }
            mediaPlayer = null
        }
    }

    RichMessageCard(
        icon = if (isPlaying) Icons.Default.Done else Icons.AutoMirrored.Filled.VolumeUp,
        title = if (isPlaying) "Playing voice note" else message.body,
        subtitle = message.metadata.ifBlank { if (isPlaying) "Tap to pause" else "Tap to play" },
        darkMode = darkMode,
        isMine = isMine,
        onClick = {
            if (isPlaying) {
                runCatching { mediaPlayer?.pause() }
                isPlaying = false
            } else {
                val existing = mediaPlayer
                if (existing != null) {
                    runCatching { existing.start() }
                    isPlaying = true
                } else {
                    val player = MediaPlayer().apply {
                        setOnCompletionListener {
                            isPlaying = false
                            runCatching { it.release() }
                            mediaPlayer = null
                        }
                        setOnPreparedListener {
                            it.start()
                            isPlaying = true
                        }
                    }
                    mediaPlayer = player
                    runCatching {
                        player.setDataSource(context, Uri.parse(message.mediaUrl))
                        player.prepareAsync()
                    }.onFailure {
                        isPlaying = false
                        runCatching { player.release() }
                        mediaPlayer = null
                    }
                }
            }
        },
    )
}

@Composable
private fun RichMessageCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    darkMode: Boolean,
    isMine: Boolean,
    onClick: (() -> Unit)? = null,
) {
    val cardColor = when {
        darkMode && isMine -> Color(0xFF115B4F)
        darkMode -> Color(0xFF22323A)
        isMine -> Color.White.copy(alpha = 0.42f)
        else -> Color(0xFFF6F7F8)
    }
    val titleColor = if (darkMode) Color.White else Color(0xFF111B21)
    val subtitleColor = if (darkMode) Color(0xFF9CB0B8) else Color(0xFF667781)
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = cardColor,
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (darkMode) Color(0xFF2D3F48) else Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = if (darkMode) Color.White else WaDarkGreen)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = titleColor)
                if (subtitle.isNotBlank()) {
                    Text(subtitle, color = subtitleColor, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}


@Composable
private fun DeliveryStatusIcon(status: MessageDeliveryStatus) {
    when (status) {
        MessageDeliveryStatus.QUEUED -> Text("🕓", style = MaterialTheme.typography.labelSmall)
        MessageDeliveryStatus.SENT -> Icon(Icons.Default.Done, contentDescription = null, tint = Color(0xFF9CB0B8), modifier = Modifier.size(15.dp))
        MessageDeliveryStatus.DELIVERED -> Icon(Icons.Default.DoneAll, contentDescription = null, tint = Color(0xFF9CB0B8), modifier = Modifier.size(16.dp))
        MessageDeliveryStatus.READ -> Icon(Icons.Default.DoneAll, contentDescription = null, tint = Color(0xFF53BDEB), modifier = Modifier.size(16.dp))
        MessageDeliveryStatus.FAILED -> Text("!", color = Color(0xFFD93025), style = MaterialTheme.typography.labelSmall)
    }
}


@Composable
private fun ActiveCallOverlay(
    call: ActiveCallUi,
    onMute: () -> Unit,
    onSpeaker: () -> Unit,
    onVideo: () -> Unit,
    onEnd: () -> Unit,
    onAcceptIncoming: () -> Unit,
    onRejectIncoming: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000)),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth()
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), RoundedCornerShape(34.dp)),
            colors = CardDefaults.elevatedCardColors(containerColor = DarkPanel),
            shape = RoundedCornerShape(34.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${if (call.direction == CallDirection.INCOMING) "Incoming" else "Active"} ${if (call.type == CallType.VIDEO) "video" else "voice"} call", color = Color.White, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(DarkPanelSoft, Color.Black))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White.copy(alpha = 0.82f), modifier = Modifier.size(56.dp))
                }
                Text(call.title, color = Color.White.copy(alpha = 0.96f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("${call.startedLabel} • ${call.networkLabel} • ${if (call.reconnecting) "reconnecting" else "stable"}", color = Color.White.copy(alpha = 0.72f), style = MaterialTheme.typography.bodySmall)
                if (call.direction == CallDirection.INCOMING && call.startedLabel.startsWith("Incoming")) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = onAcceptIncoming, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = WaUnreadGreen, contentColor = Color.White), shape = RoundedCornerShape(18.dp)) { Text("Accept") }
                        OutlinedButton(onClick = onRejectIncoming, modifier = Modifier.weight(1f), shape = RoundedCornerShape(18.dp)) { Text("Reject") }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        FloatingActionButton(onClick = onMute, modifier = Modifier.weight(1f), containerColor = Color.White.copy(alpha = 0.12f), contentColor = Color.White) { Icon(Icons.Default.Mic, contentDescription = null) }
                        FloatingActionButton(onClick = onSpeaker, modifier = Modifier.weight(1f), containerColor = Color.White.copy(alpha = 0.12f), contentColor = Color.White) { Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null) }
                        FloatingActionButton(onClick = onVideo, modifier = Modifier.weight(1f), containerColor = Color.White.copy(alpha = 0.12f), contentColor = Color.White) { Icon(Icons.Default.Videocam, contentDescription = null) }
                        FloatingActionButton(onClick = onEnd, modifier = Modifier.weight(1f), containerColor = Color(0xFFE53935), contentColor = Color.White) { Icon(Icons.Default.Call, contentDescription = null) }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    BadgeBoxText(call.networkLabel)
                    BadgeBoxText(if (call.canScreenShare) "Screen share" else "Low network")
                    BadgeBoxText("History")
                }
            }
        }
    }
}

@Composable
private fun SyncBanner(text: String) {
    Surface(color = Color.White.copy(alpha = 0.18f), shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
        Text(text = text, color = Color.White, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun StatusViewerOverlay(
    story: StatusStoryUi,
    reply: String,
    onReplyChange: (String) -> Unit,
    onClose: () -> Unit,
    onReply: () -> Unit,
    onReact: (String) -> Unit,
    onMuteToggle: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B141A).copy(alpha = 0.98f))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Brush.sweepGradient(listOf(StatusRingStart, StatusRingEnd, StatusRingStart))).padding(3.dp), contentAlignment = Alignment.Center) {
                    AvatarPhoto(seed = story.avatarSeed, photoUrl = story.photoUrl, fallbackColor = DarkPanelSoft)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(story.authorName, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("${story.timestamp} • ${story.privacyLabel}", color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
                }
                TextButton(onClick = onMuteToggle) { Text(if (story.muted) "Unmute" else "Mute") }
                TextButton(onClick = onClose) { Text("Close") }
            }
            ElevatedCard(shape = RoundedCornerShape(32.dp), colors = CardDefaults.elevatedCardColors(containerColor = DarkPanelSoft)) {
                Column(modifier = Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Brush.verticalGradient(listOf(Color(0xFF18342D), Color(0xFF10181F)))),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (story.contentType == MessageContentType.IMAGE && story.mediaUrl.isNotBlank()) {
                            AsyncImage(
                                model = story.mediaUrl,
                                contentDescription = story.authorName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                            if (story.content.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.22f))
                                        .padding(18.dp),
                                    contentAlignment = Alignment.BottomStart,
                                ) {
                                    Text(
                                        story.content,
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleLarge,
                                    )
                                }
                            }
                        } else if (story.contentType == MessageContentType.VIDEO) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(72.dp),
                                )
                                Text(
                                    text = "Video status uploaded",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge,
                                    textAlign = TextAlign.Center,
                                )
                                if (story.content.isNotBlank()) {
                                    Text(
                                        story.content,
                                        color = Color.White.copy(alpha = 0.82f),
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        } else {
                            Text(
                                story.content,
                                color = Color.White,
                                style = MaterialTheme.typography.headlineMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(18.dp),
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BadgeBoxText(story.contentType.name.lowercase().replaceFirstChar { it.uppercase() })
                        BadgeBoxText("${story.seenViewerNames.size} views")
                        if (story.reactions.isNotEmpty()) BadgeBoxText(story.reactions.joinToString(" "))
                    }
                    if (story.seenViewerNames.isNotEmpty()) {
                        Text("Seen by: ${story.seenViewerNames.joinToString()}", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("❤️", "🔥", "👏", "😂").forEach { reaction ->
                    OutlinedButton(onClick = { onReact(reaction) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) { Text(reaction) }
                }
            }
            OutlinedTextField(
                value = reply,
                onValueChange = onReplyChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Reply to status") },
                maxLines = 3,
                shape = RoundedCornerShape(18.dp),
            )
            Button(onClick = onReply, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = WaUnreadGreen, contentColor = Color.White), shape = RoundedCornerShape(18.dp)) { Text("Send reply") }
        }
    }
}

@Composable
private fun CreatePrivateChatDialog(viewModel: ChatAppViewModel) {
    AlertDialog(
        onDismissRequest = { viewModel.dismissNewChatDialog() },
        title = { Text("New chat") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = viewModel.newChatNameInput,
                    onValueChange = { viewModel.newChatNameInput = it },
                    label = { Text("Friend display name (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = viewModel.newChatPhoneInput,
                    onValueChange = { viewModel.searchDirectory(it) },
                    label = { Text("Friend username or phone") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text("Anonymous mode me username se, OTP mode me phone se chat resolve ho jayegi.", style = MaterialTheme.typography.bodySmall, color = WaMutedText)
                if (viewModel.isDirectorySearchBusy) {
                    Text("Searching registered users…", style = MaterialTheme.typography.bodySmall, color = WaMutedText)
                }
                viewModel.contactSearchResults.take(4).forEach { result ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { viewModel.applySearchContact(result) }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            VerifiedNameText(
                                name = result.displayName,
                                goldenVerified = result.goldenVerified,
                                color = MaterialTheme.colorScheme.onSurface,
                                textStyle = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                badgeCompact = true,
                            )
                            Text(
                                text = listOfNotNull(result.username.takeIf { it.isNotBlank() }?.let { "@$it" }, result.phoneNumber.takeIf { it.isNotBlank() }).joinToString(" • "),
                                style = MaterialTheme.typography.bodySmall,
                                color = WaMutedText,
                            )
                        }
                        Text(if (result.online) "online" else "offline", color = if (result.online) WaUnreadGreen else WaMutedText, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { viewModel.submitNewContactChat() }) { Text("Create") } },
        dismissButton = { TextButton(onClick = { viewModel.dismissNewChatDialog() }) { Text("Cancel") } },
    )
}

@Composable
private fun CreateGroupDialog(viewModel: ChatAppViewModel) {
    AlertDialog(
        onDismissRequest = { viewModel.dismissNewGroupDialog() },
        title = { Text("New group") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = viewModel.newGroupTitleInput,
                    onValueChange = { viewModel.newGroupTitleInput = it },
                    label = { Text("Group name") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = viewModel.newGroupPhonesInput,
                    onValueChange = { viewModel.newGroupPhonesInput = it },
                    label = { Text("Member usernames or phones") },
                    placeholder = { Text("rahul, ananya, +91...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )
            }
        },
        confirmButton = { TextButton(onClick = { viewModel.submitNewGroup() }) { Text("Create") } },
        dismissButton = { TextButton(onClick = { viewModel.dismissNewGroupDialog() }) { Text("Cancel") } },
    )
}

@Composable
private fun ProfileActionChip(
    label: String,
    onClick: () -> Unit,
    darkHome: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = if (darkHome) Color(0xCC0E1B21) else Color(0xFFD9FDD3),
        border = BorderStroke(1.dp, if (darkHome) Color(0xFF22323A) else Color(0x8825D366)),
    ) {
        Text(
            text = label,
            color = if (darkHome) Color.White else WaDarkGreen,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun ProfileHeroCard(
    displayName: String,
    usernameLabel: String,
    photoUrl: String,
    bannerUrl: String,
    avatarSeed: String,
    goldenVerified: Boolean,
    online: Boolean,
    darkHome: Boolean,
    titleColor: Color,
    subtitleColor: Color,
    bannerFallbackBrush: Brush,
    bodyColor: Color,
    bodyBrush: Brush,
    panelBrush: Brush,
    outlineColor: Color,
    onPickBanner: (() -> Unit)? = null,
    onPickPhoto: (() -> Unit)? = null,
    bannerActionLabel: String? = null,
    photoActionLabel: String? = null,
) {
    val heroShape = RoundedCornerShape(30.dp)
    val chipSurface = if (darkHome) Color(0xCC101218) else Color(0xEFFFFFFF)
    val chipText = if (darkHome) Color.White else Color(0xFF181B20)
    val avatarRing = if (darkHome) lightenColor(bodyColor, 0.08f) else Color.White
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = heroShape,
        color = Color.Transparent,
        border = BorderStroke(1.dp, outlineColor.copy(alpha = 0.62f)),
        tonalElevation = 0.dp,
        shadowElevation = 10.dp,
    ) {
        Column(
            modifier = Modifier.background(panelBrush)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(bodyBrush),
            ) {
                if (bannerUrl.isNotBlank()) {
                    AsyncImage(
                        model = bannerUrl,
                        contentDescription = "Profile banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(bannerFallbackBrush)
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    bodyColor.copy(alpha = 0.18f),
                                    bodyColor.copy(alpha = 0.94f),
                                )
                            )
                        )
                )
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = chipSurface,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 138.dp, end = 16.dp, bottom = 22.dp),
                    border = BorderStroke(1.dp, outlineColor.copy(alpha = 0.30f)),
                ) {
                    Text(
                        text = usernameLabel.ifBlank { "No username" },
                        color = chipText,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 18.dp, bottom = 18.dp)
                        .size(112.dp)
                        .clip(CircleShape)
                        .background(avatarRing)
                        .border(4.dp, avatarRing, CircleShape)
                        .let { base -> if (onPickPhoto != null) base.clickable(onClick = onPickPhoto) else base },
                    contentAlignment = Alignment.Center,
                ) {
                    if (photoUrl.isNotBlank()) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Profile photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(if (darkHome) DarkPanelSoft else Color(0xFFE8EEF1)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = avatarSeed.take(1).ifBlank { displayName.take(1).ifBlank { "U" } },
                                color = if (darkHome) Color.White else WaDarkGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 40.sp,
                            )
                        }
                    }
                    if (online) {
                        Surface(
                            shape = CircleShape,
                            color = WaUnreadGreen,
                            border = BorderStroke(3.dp, avatarRing),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 4.dp, bottom = 6.dp),
                        ) {
                            Spacer(modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bodyBrush)
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                VerifiedNameText(
                    name = displayName,
                    goldenVerified = goldenVerified,
                    color = titleColor,
                    textStyle = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    horizontalArrangement = Arrangement.Start,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = usernameLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = subtitleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = chipSurface,
                        border = BorderStroke(1.dp, outlineColor.copy(alpha = 0.30f)),
                    ) {
                        Text(
                            text = if (online) "Online" else "Offline",
                            color = chipText,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        )
                    }
                }
                if (bannerActionLabel != null || photoActionLabel != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        bannerActionLabel?.let { label ->
                            onPickBanner?.let { action ->
                                ProfileActionChip(
                                    label = label,
                                    onClick = action,
                                    darkHome = darkHome,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                        photoActionLabel?.let { label ->
                            onPickPhoto?.let { action ->
                                ProfileActionChip(
                                    label = label,
                                    onClick = action,
                                    darkHome = darkHome,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserProfileScreen(
    profile: FireUserProfile,
    viewModel: ChatAppViewModel,
    onBack: () -> Unit,
) {
    val darkHome = viewModel.chatPreferences.darkMode
    val profilePalette = profileBackgroundPalette(viewModel.chatPreferences.profileTopColorHex, viewModel.chatPreferences.profileBottomColorHex, darkHome)
    val headerColor = profilePalette.topBarColor
    val titleColor = readableTextColor(headerColor)
    val bodyTitleColor = profilePalette.titleColor
    val subtitleColor = profilePalette.subtitleColor
    val profileName = profile.displayName.ifBlank { profile.username.ifBlank { profile.phoneNumber.ifBlank { "ChitChat user" } } }
    val usernameLabel = profile.username.takeIf { it.isNotBlank() }?.let { "@$it" } ?: "No username"
    val payload = remember(profile.id, profile.username, profile.displayName, profile.phoneNumber) { viewModel.profileQrPayload(profile) }
    var showProfileQrDialog by remember(profile.id) { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Surface(color = headerColor, tonalElevation = 0.dp, shadowElevation = 0.dp) {
                Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = titleColor)
                        }
                        Text(
                            text = "Profile",
                            style = MaterialTheme.typography.headlineSmall,
                            color = titleColor,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    HorizontalDivider(color = if (darkHome) Color(0xFF15232B) else Color(0xFFE4E9EC))
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(profilePalette.screenBrush)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
            ProfileHeroCard(
                displayName = profileName,
                usernameLabel = usernameLabel,
                photoUrl = profile.photoUrl,
                bannerUrl = profile.bannerUrl,
                avatarSeed = profileName.take(1).ifBlank { "U" },
                goldenVerified = profile.goldenVerified,
                online = profile.online,
                darkHome = darkHome,
                titleColor = bodyTitleColor,
                subtitleColor = subtitleColor,
                bannerFallbackBrush = profilePalette.bannerBrush,
                bodyColor = profilePalette.cardColor,
                bodyBrush = profilePalette.heroBodyBrush,
                panelBrush = profilePalette.panelBrush,
                outlineColor = profilePalette.outlineColor,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { viewModel.openDirectMessageWithProfile(profile) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("DM")
                }
                OutlinedButton(
                    onClick = { showProfileQrDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share QR")
                }
            }
            Spacer(modifier = Modifier.height(28.dp))

            ProfileInfoDisplayCard(
                icon = Icons.Default.Info,
                label = "About",
                value = profile.about.ifBlank { "No bio added yet" },
                subtitleColor = subtitleColor,
                darkHome = darkHome,
                containerColor = profilePalette.cardColor,
                outlineColor = profilePalette.outlineColor,
                titleColor = bodyTitleColor,
                valueColor = bodyTitleColor,
            )
            ProfileInfoDisplayCard(
                icon = Icons.Default.CheckCircle,
                label = "Verification",
                value = if (profile.goldenVerified) "Golden verified" else "No golden badge",
                subtitleColor = if (profile.goldenVerified) VerificationGold else subtitleColor,
                darkHome = darkHome,
                containerColor = profilePalette.cardColor,
                outlineColor = profilePalette.outlineColor,
                titleColor = bodyTitleColor,
                valueColor = bodyTitleColor,
            )
            ProfileInfoDisplayCard(
                icon = Icons.Default.Image,
                label = "Banner",
                value = if (profile.bannerUrl.isNotBlank()) "Custom banner active" else "No banner added yet",
                subtitleColor = subtitleColor,
                darkHome = darkHome,
                containerColor = profilePalette.cardColor,
                outlineColor = profilePalette.outlineColor,
                titleColor = bodyTitleColor,
                valueColor = bodyTitleColor,
            )
            ProfileInfoDisplayCard(
                icon = Icons.Default.Person,
                label = "Status",
                value = if (profile.online) "Online" else "Offline",
                subtitleColor = subtitleColor,
                darkHome = darkHome,
                containerColor = profilePalette.cardColor,
                outlineColor = profilePalette.outlineColor,
                titleColor = bodyTitleColor,
                valueColor = bodyTitleColor,
            )
            if (profile.phoneNumber.isNotBlank()) {
                ProfileInfoDisplayCard(
                    icon = Icons.Default.Phone,
                    label = "Phone",
                    value = profile.phoneNumber,
                    subtitleColor = subtitleColor,
                    darkHome = darkHome,
                    containerColor = profilePalette.cardColor,
                    outlineColor = profilePalette.outlineColor,
                    titleColor = bodyTitleColor,
                    valueColor = bodyTitleColor,
                )
            }
            ProfileInfoDisplayCard(
                icon = Icons.Default.QrCode,
                label = "Scan support",
                value = "Home screen ke camera se is QR ko scan karoge to yahi profile khul jayegi.",
                subtitleColor = subtitleColor,
                darkHome = darkHome,
                containerColor = profilePalette.cardColor,
                outlineColor = profilePalette.outlineColor,
                titleColor = bodyTitleColor,
                valueColor = bodyTitleColor,
            )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showProfileQrDialog) {
        GenericProfileQrDialog(
            displayName = profileName,
            username = profile.username,
            payload = payload,
            helperText = "Home screen ke camera se scan karne par ${profileName} ka profile open hoga.",
            onDismiss = { showProfileQrDialog = false },
            onShare = {
                val shareIntent = Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "$profileName profile QR")
                        putExtra(Intent.EXTRA_TEXT, payload)
                    },
                    "Share profile QR"
                )
                context.startActivity(shareIntent)
            },
        )
    }
}

@Composable
private fun ProfileInfoDisplayCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    subtitleColor: Color,
    darkHome: Boolean,
    containerColor: Color = if (darkHome) Color(0xFF0E1B21) else Color.White,
    outlineColor: Color = if (darkHome) Color(0xFF15232B) else Color(0xFFE4E9EC),
    titleColor: Color = subtitleColor,
    valueColor: Color = Color.Unspecified,
) {
    val resolvedValueColor = if (valueColor == Color.Unspecified) {
        if (darkHome) Color.White else MaterialTheme.colorScheme.onSurface
    } else valueColor
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = containerColor,
        border = BorderStroke(1.dp, outlineColor.copy(alpha = 0.55f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(icon, contentDescription = null, tint = subtitleColor, modifier = Modifier.padding(top = 2.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(label, color = titleColor, style = MaterialTheme.typography.labelLarge)
                Text(value, color = resolvedValueColor, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun ProfileScreen(
    viewModel: ChatAppViewModel,
    onBack: () -> Unit,
    onPickProfilePhoto: () -> Unit,
    onPickProfileBanner: () -> Unit,
) {
    val darkHome = viewModel.chatPreferences.darkMode
    val profilePalette = profileBackgroundPalette(viewModel.chatPreferences.profileTopColorHex, viewModel.chatPreferences.profileBottomColorHex, darkHome)
    val headerColor = profilePalette.topBarColor
    val titleColor = readableTextColor(headerColor)
    val bodyTitleColor = profilePalette.titleColor
    val subtitleColor = profilePalette.subtitleColor
    var editMode by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Surface(color = headerColor, tonalElevation = 0.dp, shadowElevation = 0.dp) {
                Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = titleColor)
                        }
                        Text(
                            text = "Profile",
                            style = MaterialTheme.typography.headlineSmall,
                            color = titleColor,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = {
                            if (editMode) {
                                viewModel.saveProfile()
                                editMode = false
                            } else {
                                editMode = true
                            }
                        }) {
                            Text(if (editMode) "Save" else "Edit", color = WaUnreadGreen, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    HorizontalDivider(color = if (darkHome) Color(0xFF15232B) else Color(0xFFE4E9EC))
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(profilePalette.screenBrush)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
            ProfileHeroCard(
                displayName = viewModel.currentUser.name,
                usernameLabel = viewModel.currentUser.username.takeIf { it.isNotBlank() }?.let { "@$it" } ?: "No username",
                photoUrl = viewModel.currentUser.photoUrl,
                bannerUrl = viewModel.currentUser.bannerUrl,
                avatarSeed = viewModel.currentUser.avatarSeed,
                goldenVerified = viewModel.currentUser.goldenVerified,
                online = true,
                darkHome = darkHome,
                titleColor = bodyTitleColor,
                subtitleColor = subtitleColor,
                bannerFallbackBrush = profilePalette.bannerBrush,
                bodyColor = profilePalette.cardColor,
                bodyBrush = profilePalette.heroBodyBrush,
                panelBrush = profilePalette.panelBrush,
                outlineColor = profilePalette.outlineColor,
                onPickBanner = onPickProfileBanner,
                onPickPhoto = onPickProfilePhoto,
                bannerActionLabel = if (viewModel.isProfileBannerBusy) "Uploading banner…" else "Edit banner / GIF",
                photoActionLabel = if (viewModel.isProfilePhotoBusy) "Uploading photo…" else "Edit photo / GIF",
            )
            Spacer(modifier = Modifier.height(14.dp))

            ProfileInfoRow(
                icon = Icons.Default.Person,
                label = "Name",
                value = if (editMode) viewModel.profileNameInput else viewModel.currentUser.name,
                placeholder = "Your name",
                subtitleColor = subtitleColor,
                onValueChange = { viewModel.profileNameInput = it },
                editable = editMode,
                darkHome = darkHome,
                containerColor = profilePalette.cardColor,
                outlineColor = profilePalette.outlineColor,
                labelColor = bodyTitleColor,
                valueColor = bodyTitleColor,
            )
            ProfileInfoRow(
                icon = Icons.Default.Info,
                label = "About",
                value = if (editMode) viewModel.profileAboutInput else viewModel.currentUser.about,
                placeholder = "About",
                subtitleColor = subtitleColor,
                onValueChange = { viewModel.profileAboutInput = it },
                editable = editMode,
                darkHome = darkHome,
                containerColor = profilePalette.cardColor,
                outlineColor = profilePalette.outlineColor,
                labelColor = bodyTitleColor,
                valueColor = bodyTitleColor,
            )
            ProfileInfoRow(
                icon = Icons.Default.Phone,
                label = "Phone",
                value = if (editMode) viewModel.phoneInput else viewModel.currentUser.phoneNumber.ifBlank { "Add phone" },
                placeholder = "Phone",
                subtitleColor = subtitleColor,
                onValueChange = { viewModel.phoneInput = it },
                editable = editMode,
                darkHome = darkHome,
                containerColor = profilePalette.cardColor,
                outlineColor = profilePalette.outlineColor,
                labelColor = bodyTitleColor,
                valueColor = bodyTitleColor,
                prefix = if (editMode) viewModel.countryCodeInput else null,
                onPrefixChange = { viewModel.countryCodeInput = it },
            )
            ProfileInfoRow(
                icon = Icons.Default.CheckCircle,
                label = "Verification",
                value = if (viewModel.currentUser.goldenVerified) "Golden verified" else "No golden badge",
                placeholder = "Verification",
                subtitleColor = if (viewModel.currentUser.goldenVerified) VerificationGold else subtitleColor,
                onValueChange = {},
                editable = false,
                darkHome = darkHome,
                containerColor = profilePalette.cardColor,
                outlineColor = profilePalette.outlineColor,
                labelColor = bodyTitleColor,
                valueColor = bodyTitleColor,
                accentValue = viewModel.currentUser.goldenVerified,
            )
            ProfileInfoRow(
                icon = Icons.Default.Image,
                label = "Banner",
                value = if (viewModel.currentUser.bannerUrl.isNotBlank()) "Discord-style banner added" else "No banner yet",
                placeholder = "Banner",
                subtitleColor = subtitleColor,
                onValueChange = {},
                editable = false,
                darkHome = darkHome,
                containerColor = profilePalette.cardColor,
                outlineColor = profilePalette.outlineColor,
                labelColor = bodyTitleColor,
                valueColor = bodyTitleColor,
                accentValue = viewModel.currentUser.bannerUrl.isNotBlank(),
            )
            ProfileInfoRow(
                icon = Icons.Default.Link,
                label = "Links",
                value = if (viewModel.currentUser.username.isNotBlank()) "@${viewModel.currentUser.username}" else "Add links",
                placeholder = if (viewModel.canEditUsername()) "Username" else "Temporary username",
                subtitleColor = subtitleColor,
                onValueChange = { viewModel.usernameInput = it.removePrefix("@") },
                editable = editMode && viewModel.canEditUsername(),
                darkHome = darkHome,
                editValue = viewModel.usernameInput,
                accentValue = !editMode || !viewModel.canEditUsername(),
                containerColor = profilePalette.cardColor,
                outlineColor = profilePalette.outlineColor,
                labelColor = bodyTitleColor,
                valueColor = bodyTitleColor,
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
}

@Composable
private fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    placeholder: String,
    subtitleColor: Color,
    onValueChange: (String) -> Unit,
    editable: Boolean,
    darkHome: Boolean,
    prefix: String? = null,
    onPrefixChange: ((String) -> Unit)? = null,
    editValue: String = value,
    accentValue: Boolean = false,
    containerColor: Color = if (darkHome) Color(0xFF0E1B21) else Color.White,
    outlineColor: Color = if (darkHome) Color(0xFF15232B) else Color(0xFFE4E9EC),
    labelColor: Color = if (darkHome) Color.White else Color(0xFF0E171B),
    valueColor: Color = subtitleColor,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        shape = RoundedCornerShape(24.dp),
        color = containerColor,
        border = BorderStroke(1.dp, outlineColor.copy(alpha = 0.55f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
        Icon(icon, contentDescription = null, tint = subtitleColor, modifier = Modifier.padding(top = 8.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = label, color = labelColor, style = MaterialTheme.typography.titleLarge)
            if (editable) {
                if (prefix != null && onPrefixChange != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = prefix,
                            onValueChange = onPrefixChange,
                            modifier = Modifier.width(88.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp),
                        )
                        OutlinedTextField(
                            value = editValue,
                            onValueChange = onValueChange,
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            placeholder = { Text(placeholder) },
                            shape = RoundedCornerShape(18.dp),
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = editValue,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = label != "About",
                        minLines = if (label == "About") 2 else 1,
                        placeholder = { Text(placeholder) },
                        shape = RoundedCornerShape(18.dp),
                    )
                }
            } else {
                Text(
                    text = value.ifBlank { placeholder },
                    color = if (accentValue) WaUnreadGreen else valueColor,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}
}

@Composable
private fun StatusAudienceDialog(
    viewModel: ChatAppViewModel,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val options = viewModel.statusAudienceCandidates
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Who can see this status?") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Status sirf un users ko dikhega jo aapke home chats me hain.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AudienceChoiceCard(
                    title = "Share with everyone",
                    subtitle = if (options.isEmpty()) "Abhi home screen me koi user nahi mila" else "Home screen ke ${options.size} users status dekh sakenge",
                    selected = viewModel.statusShareWithEveryone,
                    onClick = { viewModel.updateStatusShareMode(true) },
                )
                AudienceChoiceCard(
                    title = "Choose who can see",
                    subtitle = "Users select karke sirf unko status dikhao",
                    selected = !viewModel.statusShareWithEveryone,
                    onClick = { viewModel.updateStatusShareMode(false) },
                )
                if (!viewModel.statusShareWithEveryone) {
                    if (options.isEmpty()) {
                        Text(
                            text = "Select karne ke liye pehle kisi user ka chat home screen me hona chahiye.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        options.forEach { user ->
                            StatusAudienceUserRow(
                                user = user,
                                selected = user.id in viewModel.selectedStatusAudienceIds,
                                onToggle = { viewModel.toggleStatusAudienceSelection(user.id) },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun AudienceChoiceCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (selected) WaUnreadGreen.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (selected) WaUnreadGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (selected) Icons.Default.CheckCircle else Icons.Default.Person,
                contentDescription = null,
                tint = if (selected) WaUnreadGreen else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun StatusAudienceUserRow(
    user: ContactSearchUi,
    selected: Boolean,
    onToggle: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (selected) WaUnreadGreen.copy(alpha = 0.12f) else Color.Transparent,
        border = BorderStroke(1.dp, if (selected) WaUnreadGreen.copy(alpha = 0.55f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (selected) WaUnreadGreen.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                AvatarPhoto(
                    seed = user.displayName.take(1).ifBlank { "U" },
                    photoUrl = user.photoUrl,
                    fallbackColor = Color.Transparent,
                    textColor = MaterialTheme.colorScheme.onSurface,
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                VerifiedNameText(
                    name = user.displayName,
                    goldenVerified = user.goldenVerified,
                    color = MaterialTheme.colorScheme.onSurface,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    badgeCompact = true,
                )
                Text(
                    text = user.username.takeIf { it.isNotBlank() }?.let { "@$it" } ?: "No username",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = if (selected) Icons.Default.CheckCircle else Icons.Default.Person,
                contentDescription = null,
                tint = if (selected) WaUnreadGreen else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MediaPickerDialog(
    title: String,
    onDismiss: () -> Unit,
    onPick: (MessageContentType) -> Unit,
    includeDocument: Boolean = true,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PickerActionRow("Photo or GIF", Icons.Default.Image) { onPick(MessageContentType.IMAGE) }
                PickerActionRow("Video", Icons.Default.Videocam) { onPick(MessageContentType.VIDEO) }
                if (includeDocument) {
                    PickerActionRow("Document", Icons.Default.Description) { onPick(MessageContentType.DOCUMENT) }
                }
                PickerActionRow("Audio / voice file", Icons.Default.Mic) { onPick(MessageContentType.VOICE) }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

@Composable
private fun PickerActionRow(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    OutlinedCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(18.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = WaDarkGreen)
            Text(label, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun SettingSection(title: String, content: @Composable () -> Unit) {
    ElevatedCard(shape = RoundedCornerShape(24.dp), colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                modifier = Modifier.padding(16.dp),
                color = WaDarkGreen,
                fontWeight = FontWeight.Bold,
            )
            content()
        }
    }
}

@Composable
private fun ProfileQrDialog(viewModel: ChatAppViewModel, onDismiss: () -> Unit) {
    GenericProfileQrDialog(
        displayName = viewModel.currentUser.name.ifBlank { "ChitChat user" },
        username = viewModel.currentUser.username,
        payload = remember(viewModel.currentUser.id, viewModel.currentUser.username, viewModel.currentUser.name) {
            viewModel.profileQrPayload()
        },
        helperText = "Friend is QR ko scan karega to aapka profile open hoga.",
        onDismiss = onDismiss,
    )
}

@Composable
private fun GenericProfileQrDialog(
    displayName: String,
    username: String,
    payload: String,
    helperText: String,
    onDismiss: () -> Unit,
    onShare: (() -> Unit)? = null,
) {
    val qrBitmap = remember(payload) { runCatching { generateQrBitmap(payload) }.getOrNull() }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            if (onShare != null) {
                Button(onClick = onShare) { Text("Share") }
            } else {
                TextButton(onClick = onDismiss) { Text("Done") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        title = { Text("Profile QR") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    tonalElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        qrBitmap?.let { bitmap ->
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Profile QR code",
                                modifier = Modifier.size(260.dp),
                            )
                        } ?: Text("QR generate nahi ho paaya")
                    }
                }
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = username.takeIf { it.isNotBlank() }?.let { "@$it" } ?: "Anonymous profile QR",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = helperText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        },
    )
}

@Composable
private fun AppSettingsDialog(
    viewModel: ChatAppViewModel,
    onDismiss: () -> Unit,
    onOpenThemeSettings: () -> Unit,
    onOpenProfileBackgroundSettings: () -> Unit,
    onLogout: () -> Unit,
) {
    val darkMode = viewModel.chatPreferences.darkMode
    val cardColor = if (darkMode) DarkPanelSoft else Color.White
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done", color = WaDarkGreen) }
        },
        title = { Text("Settings") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = cardColor,
                    border = BorderStroke(1.dp, outlineColor),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(WaDarkGreen.copy(alpha = 0.18f), WaUnreadGreen.copy(alpha = 0.30f)))),
                            contentAlignment = Alignment.Center,
                        ) {
                            AvatarPhoto(
                                seed = viewModel.currentUser.avatarSeed,
                                photoUrl = viewModel.currentUser.photoUrl,
                                fallbackColor = Color.Transparent,
                                textColor = if (darkMode) Color.White else WaDarkGreen,
                            )
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(viewModel.currentUser.name.ifBlank { "Your account" }, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                text = viewModel.currentUser.username.takeIf { it.isNotBlank() }?.let { "@$it" } ?: "Temporary account",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = cardColor,
                    border = BorderStroke(1.dp, outlineColor),
                ) {
                    Column {
                        ToggleSettingRow(
                            title = "Dark mode",
                            subtitle = "Use the WhatsApp-style dark theme",
                            checked = viewModel.chatPreferences.darkMode,
                            onToggle = { viewModel.updateChatPreferences { it.copy(darkMode = !it.darkMode) } }
                        )
                        ClickableSettingRow(
                            title = "Chat theme",
                            subtitle = "${chatThemeLabel(viewModel.chatPreferences.chatThemeKey)} • 5 ready themes + Customize Theme",
                            icon = Icons.Default.Brush,
                            onClick = onOpenThemeSettings,
                        )
                        ClickableSettingRow(
                            title = "Profile background color",
                            subtitle = "${profileBackgroundLabel(viewModel.chatPreferences.profileTopColorHex, viewModel.chatPreferences.profileBottomColorHex)} • upper + lower custom",
                            icon = Icons.Default.Brush,
                            onClick = onOpenProfileBackgroundSettings,
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = cardColor,
                    border = BorderStroke(1.dp, outlineColor),
                ) {
                    Column {
                        ToggleSettingRow(
                            title = "Push notifications",
                            subtitle = "Get alerts for new direct messages and replies",
                            checked = viewModel.notificationPreferences.pushNotifications,
                            onToggle = {
                                viewModel.updateNotificationPreferences { prefs ->
                                    prefs.copy(pushNotifications = !prefs.pushNotifications)
                                }
                            }
                        )
                        ToggleSettingRow(
                            title = "Read receipts",
                            subtitle = "Show blue ticks when messages are read",
                            checked = viewModel.securityPreferences.readReceipts,
                            onToggle = {
                                viewModel.updateSecurityPreferences { prefs ->
                                    prefs.copy(readReceipts = !prefs.readReceipts)
                                }
                            }
                        )
                        ToggleSettingRow(
                            title = "Online presence",
                            subtitle = "Show online and last seen in chats",
                            checked = viewModel.securityPreferences.showOnlinePresence,
                            onToggle = {
                                viewModel.updateSecurityPreferences { prefs ->
                                    prefs.copy(showOnlinePresence = !prefs.showOnlinePresence)
                                }
                            }
                        )
                    }
                }
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD64545),
                        contentColor = Color.White,
                    ),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text("Log out")
                }
            }
        },
    )
}

@Composable
private fun ChatThemeDialog(
    viewModel: ChatAppViewModel,
    onDismiss: () -> Unit,
    onPickCustomBackground: () -> Unit,
) {
    val darkMode = viewModel.chatPreferences.darkMode
    val selectedKey = viewModel.chatPreferences.chatThemeKey
    val customThemeImageUri = viewModel.chatPreferences.customThemeImageUri
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        },
        title = { Text("Chat Theme") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Pick 1 of 5 themes, ya Customize Theme me apni photo background lagao.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                availableChatThemes(darkMode).forEach { palette ->
                    val isSelected = palette.key == selectedKey
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.updateChatPreferences {
                                    it.copy(chatThemeKey = palette.key)
                                }
                            },
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) WaDarkGreen else DividerDefaults.color.copy(alpha = 0.45f),
                        ),
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 82.dp, height = 54.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                ) {
                                    if (palette.key == "CUSTOM" && customThemeImageUri.isNotBlank()) {
                                        AsyncImage(
                                            model = customThemeImageUri,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop,
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(palette.chatBackgroundBrush)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(palette.chatBackgroundBrush)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(palette.title, fontWeight = FontWeight.SemiBold)
                                    Text(palette.subtitle, color = WaMutedText, style = MaterialTheme.typography.bodySmall)
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = WaUnreadGreen)
                                }
                            }
                            if (palette.key == "CUSTOM") {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = onPickCustomBackground) { Text(if (customThemeImageUri.isBlank()) "Choose photo" else "Change photo") }
                                    if (customThemeImageUri.isNotBlank()) {
                                        OutlinedButton(onClick = {
                                            viewModel.updateChatPreferences {
                                                it.copy(customThemeImageUri = "")
                                            }
                                        }) { Text("Remove") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun ProfileBackgroundDialog(
    viewModel: ChatAppViewModel,
    onDismiss: () -> Unit,
) {
    val darkMode = viewModel.chatPreferences.darkMode
    var topColor by remember(viewModel.chatPreferences.profileTopColorHex) {
        mutableStateOf(parseColorHexOrDefault(viewModel.chatPreferences.profileTopColorHex, Color(0xFFC83B4B)))
    }
    var bottomColor by remember(viewModel.chatPreferences.profileBottomColorHex) {
        mutableStateOf(parseColorHexOrDefault(viewModel.chatPreferences.profileBottomColorHex, Color(0xFF25070D)))
    }
    val previewPalette = profileBackgroundPalette(colorToHex(topColor), colorToHex(bottomColor), darkMode)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                viewModel.updateChatPreferences {
                    it.copy(
                        profileTopColorHex = colorToHex(topColor),
                        profileBottomColorHex = colorToHex(bottomColor),
                    )
                }
                onDismiss()
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("Profile background color") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "Upar aur niche ka color user khud choose karega. Upar koi bhi color aur niche koi bhi color rakh sakta hai.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(containerColor = previewPalette.screenColor),
                    border = BorderStroke(1.dp, previewPalette.outlineColor.copy(alpha = 0.65f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(previewPalette.bannerBrush)
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(previewPalette.heroBodyBrush)
                        )
                        Text(
                            text = "Preview",
                            color = previewPalette.titleColor,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(14.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = profileBackgroundLabel(colorToHex(topColor), colorToHex(bottomColor)),
                            color = previewPalette.subtitleColor,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                ProfileColorEditorSection(
                    label = "Upper color",
                    color = topColor,
                    onColorChange = { topColor = it },
                )
                ProfileColorEditorSection(
                    label = "Lower color",
                    color = bottomColor,
                    onColorChange = { bottomColor = it },
                )
            }
        },
    )
}

@Composable
private fun ProfileColorEditorSection(
    label: String,
    color: Color,
    onColorChange: (Color) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), CircleShape)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontWeight = FontWeight.SemiBold)
                Text(colorToHex(color), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        ProfileColorSliderRow(
            channel = "Red",
            value = color.red,
            displayValue = (color.red * 255f).toInt(),
            onValueChange = { onColorChange(color.copy(red = it)) },
        )
        ProfileColorSliderRow(
            channel = "Green",
            value = color.green,
            displayValue = (color.green * 255f).toInt(),
            onValueChange = { onColorChange(color.copy(green = it)) },
        )
        ProfileColorSliderRow(
            channel = "Blue",
            value = color.blue,
            displayValue = (color.blue * 255f).toInt(),
            onValueChange = { onColorChange(color.copy(blue = it)) },
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(profileColorChoices()) { swatch ->
                val selected = colorToHex(swatch) == colorToHex(color)
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(swatch)
                        .border(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                            shape = CircleShape,
                        )
                        .clickable { onColorChange(swatch) }
                )
            }
        }
    }
}

@Composable
private fun ProfileColorSliderRow(
    channel: String,
    value: Float,
    displayValue: Int,
    onValueChange: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(channel, style = MaterialTheme.typography.bodySmall)
            Text(displayValue.toString(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Slider(value = value.coerceIn(0f, 1f), onValueChange = { onValueChange(it.coerceIn(0f, 1f)) })
    }
}


@Composable
private fun LinkedDevicesDialog(viewModel: ChatAppViewModel, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        },
        dismissButton = {
            OutlinedButton(onClick = viewModel::linkDesktopDevice) { Text("Link device") }
        },
        title = { Text("Linked devices") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                if (viewModel.linkedDevices.isEmpty()) {
                    Text(
                        text = "No linked browser or desktop session yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                    )
                } else {
                    viewModel.linkedDevices.forEach { device ->
                        LinkedDeviceRow(device = device, onUnlink = { viewModel.unlinkDevice(device.id) })
                    }
                }
                Text(
                    text = "Link device demo abhi local mode me hai. Later isko real web login ke saath connect kar sakte hain.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        },
    )
}

private fun generateQrBitmap(content: String, size: Int = 1200): Bitmap {
    val hints = mapOf(EncodeHintType.MARGIN to 2)
    val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    return bitmap
}

@Composable
private fun ToggleSettingRow(title: String, subtitle: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(subtitle, color = WaMutedText, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = checked, onCheckedChange = { onToggle() })
    }
    HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.35f))
}

@Composable
private fun ClickableSettingRow(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = WaDarkGreen)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(subtitle, color = WaMutedText, style = MaterialTheme.typography.bodySmall)
        }
        Icon(Icons.AutoMirrored.Filled.Forward, contentDescription = null, tint = WaMutedText)
    }
    HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.35f))
}

@Composable
private fun StaticSettingRow(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = WaDarkGreen)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(subtitle, color = WaMutedText, style = MaterialTheme.typography.bodySmall)
        }
    }
    HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.35f))
}

@Composable
private fun LinkedDeviceRow(device: LinkedDeviceUi, onUnlink: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Default.Devices, contentDescription = null, tint = WaDarkGreen)
        Column(modifier = Modifier.weight(1f)) {
            Text(device.name, fontWeight = FontWeight.Medium)
            Text("${device.location} • ${device.lastActive}", color = WaMutedText, style = MaterialTheme.typography.bodySmall)
        }
        TextButton(onClick = onUnlink) { Text("Log out") }
    }
    HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.35f))
}

@Composable
private fun BadgeBoxText(text: String) {
    Surface(shape = RoundedCornerShape(999.dp), color = Color(0xFFDFF6EC), border = BorderStroke(1.dp, Color(0xFFCBEADB))) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = WaDarkGreen,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun MiniActionChip(label: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE7ECEF)),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp), color = Color(0xFF46545E), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun AvatarPhoto(
    seed: String,
    photoUrl: String,
    fallbackColor: Color,
    textColor: Color = Color.White,
) {
    if (photoUrl.isNotBlank()) {
        AsyncImage(
            model = photoUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    } else {
        AvatarSeed(seed = seed, color = fallbackColor, textColor = textColor)
    }
}

@Composable
private fun AvatarSeed(seed: String, color: Color, textColor: Color = Color.White) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(seed.take(1).ifBlank { "C" }, color = textColor, fontWeight = FontWeight.Bold)
    }
}
