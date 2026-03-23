package com.chitchat.app.data.firebase

import android.content.ContentResolver
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.chitchat.app.BuildConfig
import com.chitchat.app.ChitChatApplication
import com.chitchat.app.data.model.MessageContentType
import com.chitchat.app.data.model.StorageUploadProgress
import com.chitchat.app.data.model.StorageUploadResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSink
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Locale
import java.util.UUID

/**
 * Kept with the original class name so the rest of the app code stays unchanged,
 * but uploads are now backed by Cloudinary unsigned uploads instead of Firebase Storage.
 */
class FirebaseStorageManager {
    private val appContext get() = ChitChatApplication.instance.applicationContext
    private val contentResolver: ContentResolver get() = appContext.contentResolver
    private val httpClient = OkHttpClient()

    suspend fun uploadChatAttachment(
        localUri: Uri,
        chatId: String,
        messageId: String,
        type: MessageContentType,
        durationMs: Long = 0L,
    ): StorageUploadResult {
        return uploadChatAttachmentFlow(localUri, chatId, messageId, type, durationMs)
            .first { it.result != null }
            .result ?: error("Upload failed before completion")
    }

    fun uploadChatAttachmentFlow(
        localUri: Uri,
        chatId: String,
        messageId: String,
        type: MessageContentType,
        durationMs: Long = 0L,
    ): Flow<StorageUploadProgress> = callbackFlow {
        trySend(StorageUploadProgress(progressPercent = 0, stage = "preparing"))
        val job = launch(Dispatchers.IO) {
            runCatching {
                uploadToCloudinary(
                    localUri = localUri,
                    publicIdPrefix = "chat_media/$chatId/${type.name.lowercase(Locale.US)}/$messageId",
                ) { percent ->
                    trySend(StorageUploadProgress(progressPercent = percent.coerceIn(0, 99), stage = "uploading"))
                }.copy(durationMs = durationMs)
            }.onSuccess { result ->
                trySend(StorageUploadProgress(progressPercent = 100, stage = "completed", result = result))
                close()
            }.onFailure { error ->
                if (error is CancellationException) {
                    close()
                } else {
                    close(error)
                }
            }
        }
        awaitClose { job.cancel() }
    }

    suspend fun uploadStatusMedia(localUri: Uri, authorId: String): StorageUploadResult {
        return uploadStatusMediaFlow(localUri, authorId)
            .first { it.result != null }
            .result ?: error("Status upload failed before completion")
    }

    fun uploadStatusMediaFlow(localUri: Uri, authorId: String): Flow<StorageUploadProgress> = callbackFlow {
        trySend(StorageUploadProgress(progressPercent = 0, stage = "preparing"))
        val job = launch(Dispatchers.IO) {
            runCatching {
                uploadToCloudinary(
                    localUri = localUri,
                    publicIdPrefix = "status_media/$authorId/${System.currentTimeMillis()}",
                ) { percent ->
                    trySend(StorageUploadProgress(progressPercent = percent.coerceIn(0, 99), stage = "uploading"))
                }
            }.onSuccess { result ->
                trySend(StorageUploadProgress(progressPercent = 100, stage = "completed", result = result))
                close()
            }.onFailure { error ->
                if (error is CancellationException) {
                    close()
                } else {
                    close(error)
                }
            }
        }
        awaitClose { job.cancel() }
    }

    suspend fun uploadProfilePhoto(localUri: Uri, userId: String): StorageUploadResult = withContext(Dispatchers.IO) {
        val optimized = prepareProfileImageUpload(
            localUri = localUri,
            maxDimension = 960,
            skipOptimizeBelowBytes = 220_000L,
            targetMaxBytes = 280_000,
            qualityRange = 50..84,
        )
        if (optimized != null) {
            uploadPreparedToCloudinary(
                prepared = optimized,
                publicIdPrefix = "profile_photos/$userId/profile",
            )
        } else {
            uploadToCloudinary(
                localUri = localUri,
                publicIdPrefix = "profile_photos/$userId/profile",
            )
        }
    }

    suspend fun uploadProfileBanner(localUri: Uri, userId: String): StorageUploadResult = withContext(Dispatchers.IO) {
        val optimized = prepareProfileImageUpload(
            localUri = localUri,
            maxDimension = 1600,
            skipOptimizeBelowBytes = 320_000L,
            targetMaxBytes = 420_000,
            qualityRange = 52..86,
        )
        if (optimized != null) {
            uploadPreparedToCloudinary(
                prepared = optimized,
                publicIdPrefix = "profile_banners/$userId/banner",
            )
        } else {
            uploadToCloudinary(
                localUri = localUri,
                publicIdPrefix = "profile_banners/$userId/banner",
            )
        }
    }

    suspend fun deleteChatMediaByPrefix(chatId: String) {
        // No-op for Cloudinary unsigned uploads from the client.
        // Deleting remote media safely should be handled from a trusted backend if needed.
    }

    private fun uploadToCloudinary(
        localUri: Uri,
        publicIdPrefix: String,
        onProgress: (Int) -> Unit = {},
    ): StorageUploadResult {
        ensureConfigured()
        val localFileName = queryDisplayName(localUri)
        val mimeType = resolveMimeType(localUri)
        val resourceType = when {
            mimeType.startsWith("video/") -> "video"
            mimeType.startsWith("image/") -> "image"
            else -> "raw"
        }
        val contentLength = querySize(localUri)
        val publicId = buildPublicId(
            prefix = publicIdPrefix,
            originalFileName = localFileName,
            resourceType = resourceType,
        )
        val endpoint = "https://api.cloudinary.com/v1_1/${BuildConfig.CLOUDINARY_CLOUD_NAME}/$resourceType/upload"
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("upload_preset", BuildConfig.CLOUDINARY_UPLOAD_PRESET)
            .addFormDataPart(
                "file",
                localFileName,
                ContentUriRequestBody(
                    contentResolver = contentResolver,
                    uri = localUri,
                    mimeType = mimeType,
                    contentLength = contentLength,
                    onProgress = onProgress,
                )
            )
            .addFormDataPart("public_id", publicId)
            .build()
        val request = Request.Builder()
            .url(endpoint)
            .post(body)
            .build()
        httpClient.newCall(request).execute().use { response ->
            val payload = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val apiMessage = runCatching {
                    JSONObject(payload).optJSONObject("error")?.optString("message").orEmpty()
                }.getOrDefault("")
                throw IOException(apiMessage.ifBlank { "Cloudinary upload failed (${response.code})" })
            }
            val json = JSONObject(payload)
            val secureUrl = json.optString("secure_url")
            if (secureUrl.isBlank()) throw IOException("Cloudinary upload response missing secure_url")
            return StorageUploadResult(
                downloadUrl = secureUrl,
                fileName = localFileName,
                mimeType = mimeType,
                sizeBytes = json.optLong("bytes", contentLength.coerceAtLeast(0L)),
            )
        }
    }


    private fun uploadPreparedToCloudinary(
        prepared: PreparedUpload,
        publicIdPrefix: String,
    ): StorageUploadResult {
        ensureConfigured()
        val publicId = buildPublicId(
            prefix = publicIdPrefix,
            originalFileName = prepared.fileName,
            resourceType = prepared.resourceType,
        )
        val endpoint = "https://api.cloudinary.com/v1_1/${BuildConfig.CLOUDINARY_CLOUD_NAME}/${prepared.resourceType}/upload"
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("upload_preset", BuildConfig.CLOUDINARY_UPLOAD_PRESET)
            .addFormDataPart("file", prepared.fileName, prepared.requestBody)
            .addFormDataPart("public_id", publicId)
            .build()
        val request = Request.Builder()
            .url(endpoint)
            .post(body)
            .build()
        httpClient.newCall(request).execute().use { response ->
            val payload = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val apiMessage = runCatching {
                    JSONObject(payload).optJSONObject("error")?.optString("message").orEmpty()
                }.getOrDefault("")
                throw IOException(apiMessage.ifBlank { "Cloudinary upload failed (${response.code})" })
            }
            val json = JSONObject(payload)
            val secureUrl = json.optString("secure_url")
            if (secureUrl.isBlank()) throw IOException("Cloudinary upload response missing secure_url")
            return StorageUploadResult(
                downloadUrl = secureUrl,
                fileName = prepared.fileName,
                mimeType = prepared.mimeType,
                sizeBytes = json.optLong("bytes", prepared.contentLength.coerceAtLeast(0L)),
            )
        }
    }

    private fun ensureConfigured() {
        require(BuildConfig.CLOUDINARY_CLOUD_NAME.isNotBlank() && BuildConfig.CLOUDINARY_CLOUD_NAME != "demo-cloud-name") {
            "Set CLOUDINARY_CLOUD_NAME in gradle.properties before uploading media."
        }
        require(BuildConfig.CLOUDINARY_UPLOAD_PRESET.isNotBlank() && BuildConfig.CLOUDINARY_UPLOAD_PRESET != "demo_unsigned_preset") {
            "Set CLOUDINARY_UPLOAD_PRESET in gradle.properties before uploading media."
        }
    }

    private fun resolveMimeType(localUri: Uri): String {
        return contentResolver.getType(localUri)
            ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                localUri.lastPathSegment?.substringAfterLast('.', "")?.lowercase(Locale.US)
            )
            ?: "application/octet-stream"
    }

    private fun queryDisplayName(localUri: Uri): String {
        queryOpenableCursor(localUri)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) {
                val value = cursor.getString(index)
                if (!value.isNullOrBlank()) return value
            }
        }
        return localUri.lastPathSegment?.substringAfterLast('/') ?: "upload-${System.currentTimeMillis()}"
    }

    private fun querySize(localUri: Uri): Long {
        queryOpenableCursor(localUri)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (index >= 0 && cursor.moveToFirst() && !cursor.isNull(index)) {
                return cursor.getLong(index)
            }
        }
        return runCatching {
            contentResolver.openAssetFileDescriptor(localUri, "r")?.use { descriptor -> descriptor.length ?: -1L } ?: -1L
        }.getOrDefault(-1L)
    }

    private fun queryOpenableCursor(localUri: Uri): Cursor? = runCatching {
        contentResolver.query(localUri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)
    }.getOrNull()

    private fun prepareProfileImageUpload(
        localUri: Uri,
        maxDimension: Int,
        skipOptimizeBelowBytes: Long,
        targetMaxBytes: Int,
        qualityRange: IntRange,
    ): PreparedUpload? {
        val mimeType = resolveMimeType(localUri)
        if (!mimeType.startsWith("image/")) return null
        if (mimeType.equals("image/gif", ignoreCase = true)) return null

        val originalSize = querySize(localUri)
        if (originalSize in 1..skipOptimizeBelowBytes) return null

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contentResolver.openInputStream(localUri)?.use { input ->
            BitmapFactory.decodeStream(input, null, bounds)
        } ?: return null

        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val decodeOptions = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.RGB_565
            inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDimension)
        }

        val decoded = contentResolver.openInputStream(localUri)?.use { input ->
            BitmapFactory.decodeStream(input, null, decodeOptions)
        } ?: return null

        val oriented = applyImageRotation(decoded, readImageRotation(localUri))
        if (oriented !== decoded) decoded.recycle()

        return try {
            val jpegQuality = when {
                originalSize > 10_000_000L -> qualityRange.first + 2
                originalSize > 6_000_000L -> qualityRange.first + 8
                originalSize > 3_000_000L -> qualityRange.first + 14
                else -> qualityRange.last - 8
            }
            val jpegBytes = compressBitmapForProfile(oriented, jpegQuality, targetMaxBytes, qualityRange)
            val originalBase = queryDisplayName(localUri).substringBeforeLast('.').ifBlank { "profile" }
            PreparedUpload(
                fileName = "$originalBase.jpg",
                mimeType = "image/jpeg",
                resourceType = "image",
                contentLength = jpegBytes.size.toLong(),
                requestBody = jpegBytes.toRequestBody("image/jpeg".toMediaTypeOrNull()),
            )
        } finally {
            oriented.recycle()
        }
    }

    private fun compressBitmapForProfile(
        bitmap: Bitmap,
        startQuality: Int,
        targetMaxBytes: Int,
        qualityRange: IntRange,
    ): ByteArray {
        var quality = startQuality.coerceIn(qualityRange.first, qualityRange.last)
        var bestBytes = ByteArray(0)
        repeat(4) {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            val bytes = stream.toByteArray()
            bestBytes = bytes
            if (bytes.size <= targetMaxBytes || quality <= qualityRange.first + 4) {
                return bytes
            }
            quality -= 7
        }
        return bestBytes
    }

    private fun calculateInSampleSize(width: Int, height: Int, maxDimension: Int): Int {
        var sample = 1
        var currentWidth = width
        var currentHeight = height
        while (currentWidth > maxDimension || currentHeight > maxDimension) {
            currentWidth /= 2
            currentHeight /= 2
            sample *= 2
        }
        return sample.coerceAtLeast(1)
    }

    private fun readImageRotation(localUri: Uri): Float {
        return runCatching {
            contentResolver.openInputStream(localUri)?.use { input ->
                when (ExifInterface(input).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f
        }.getOrDefault(0f)
    }

    private fun applyImageRotation(bitmap: Bitmap, rotation: Float): Bitmap {
        if (rotation == 0f) return bitmap
        val matrix = Matrix().apply { postRotate(rotation) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun buildPublicId(prefix: String, originalFileName: String, resourceType: String): String {
        val cleanedPrefix = prefix.trim('/').replace(Regex("[^A-Za-z0-9/_-]"), "_")
        val baseName = originalFileName.substringBeforeLast('.')
            .ifBlank { "file" }
            .replace(Regex("[^A-Za-z0-9/_-]"), "_")
            .trim('_')
            .ifBlank { "file" }
        val extension = originalFileName.substringAfterLast('.', "").lowercase(Locale.US)
        val uniqueSuffix = UUID.randomUUID().toString().substring(0, 8)
        val basePublicId = "$cleanedPrefix-$uniqueSuffix-$baseName"
        return if (resourceType == "raw" && extension.isNotBlank()) {
            "$basePublicId.$extension"
        } else {
            basePublicId
        }
    }

    private class ContentUriRequestBody(
        private val contentResolver: ContentResolver,
        private val uri: Uri,
        private val mimeType: String,
        private val contentLength: Long,
        private val onProgress: (Int) -> Unit,
    ) : RequestBody() {
        override fun contentType() = mimeType.toMediaTypeOrNull()

        override fun contentLength(): Long = contentLength

        override fun writeTo(sink: BufferedSink) {
            val totalBytes = contentLength.coerceAtLeast(-1L)
            contentResolver.openInputStream(uri)?.use { input ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var uploaded = 0L
                var lastPercent = -1
                while (true) {
                    val read = input.read(buffer)
                    if (read == -1) break
                    sink.write(buffer, 0, read)
                    uploaded += read
                    if (totalBytes > 0L) {
                        val percent = ((uploaded * 100L) / totalBytes).toInt().coerceIn(0, 99)
                        if (percent != lastPercent) {
                            lastPercent = percent
                            onProgress(percent)
                        }
                    }
                }
            } ?: throw IOException("Unable to open selected file for upload.")
        }
    }

    private data class PreparedUpload(
        val fileName: String,
        val mimeType: String,
        val resourceType: String,
        val contentLength: Long,
        val requestBody: RequestBody,
    )
}
