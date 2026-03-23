package com.chitchat.app.voice

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.SystemClock
import java.io.File

data class VoiceRecordingResult(
    val file: File,
    val durationMs: Long,
)

class VoiceNoteRecorder(
    private val context: Context,
) {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startedAt: Long = 0L

    fun suggestedOutputFile(): File = File(
        context.cacheDir,
        "voice-note-${System.currentTimeMillis()}.m4a",
    )

    private fun createMediaRecorder(): MediaRecorder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

    fun start(targetFile: File = suggestedOutputFile()) {
        stopQuietly()
        outputFile = targetFile
        startedAt = SystemClock.elapsedRealtime()
        recorder = createMediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(targetFile.absolutePath)
            prepare()
            start()
        }
    }

    fun stop(): VoiceRecordingResult {
        val file = outputFile ?: throw IllegalStateException("Recorder not started")
        val duration = (SystemClock.elapsedRealtime() - startedAt).coerceAtLeast(0L)
        val activeRecorder = recorder ?: throw IllegalStateException("Recorder not started")
        activeRecorder.stop()
        activeRecorder.release()
        recorder = null
        outputFile = null
        return VoiceRecordingResult(file = file, durationMs = duration)
    }

    fun cancel() {
        val file = outputFile
        stopQuietly()
        file?.delete()
    }

    private fun stopQuietly() {
        runCatching { recorder?.stop() }
        runCatching { recorder?.release() }
        recorder = null
        outputFile = null
    }
}
