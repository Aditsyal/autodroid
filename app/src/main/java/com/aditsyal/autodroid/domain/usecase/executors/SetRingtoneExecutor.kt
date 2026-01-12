package com.aditsyal.autodroid.domain.usecase.executors

import android.content.ContentValues
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class SetRingtoneExecutor @Inject constructor(
    @ApplicationContext private val context: Context
) : ActionExecutor {

    override suspend fun execute(config: Map<String, Any>): Result<Unit> {
        return runCatching {
            val ringtoneType = config["ringtoneType"]?.toString()?.lowercase() ?: "ringtone"
            val filePath = config["filePath"]?.toString()
            val uriString = config["uri"]?.toString()

            val ringtoneUri = when {
                filePath != null -> createRingtoneFromFile(filePath, ringtoneType)
                uriString != null -> Uri.parse(uriString)
                else -> throw IllegalArgumentException("Either filePath or uri must be specified")
            }

            setRingtone(ringtoneUri, ringtoneType)
            Timber.i("Ringtone set successfully for type: $ringtoneType")
        }.onFailure { e ->
            Timber.e(e, "Failed to set ringtone")
        }
    }

    private fun setRingtone(ringtoneUri: Uri, ringtoneType: String) {
        try {
            // Check if we have WRITE_SETTINGS permission (required for Android 6.0+)
            if (!canWriteSettings()) {
                throw SecurityException("WRITE_SETTINGS permission required to set ringtone")
            }

            val typeConstant = when (ringtoneType) {
                "ringtone", "phone" -> RingtoneManager.TYPE_RINGTONE
                "notification", "notif" -> RingtoneManager.TYPE_NOTIFICATION
                "alarm" -> RingtoneManager.TYPE_ALARM
                else -> RingtoneManager.TYPE_RINGTONE
            }

            // Set the actual default ringtone
            RingtoneManager.setActualDefaultRingtoneUri(context, typeConstant, ringtoneUri)

            // Also set in Settings.System for compatibility
            val settingKey = when (typeConstant) {
                RingtoneManager.TYPE_RINGTONE -> Settings.System.RINGTONE
                RingtoneManager.TYPE_NOTIFICATION -> Settings.System.NOTIFICATION_SOUND
                RingtoneManager.TYPE_ALARM -> Settings.System.ALARM_ALERT
                else -> Settings.System.RINGTONE
            }

            Settings.System.putString(context.contentResolver, settingKey, ringtoneUri.toString())

            Timber.d("Set ringtone to: $ringtoneUri for type: $ringtoneType")

        } catch (e: SecurityException) {
            throw e
        } catch (e: Exception) {
            throw RuntimeException("Failed to set ringtone: ${e.message}")
        }
    }

    private fun createRingtoneFromFile(filePath: String, ringtoneType: String): Uri {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                throw IllegalArgumentException("Ringtone file does not exist: $filePath")
            }

            // Insert the audio file into MediaStore
            val contentValues = ContentValues().apply {
                put(MediaStore.Audio.Media.DATA, file.absolutePath)
                put(MediaStore.Audio.Media.TITLE, file.nameWithoutExtension)
                put(MediaStore.Audio.Media.MIME_TYPE, getMimeType(file))
                put(MediaStore.Audio.Media.SIZE, file.length())
                put(MediaStore.Audio.Media.IS_RINGTONE, ringtoneType == "ringtone")
                put(MediaStore.Audio.Media.IS_NOTIFICATION, ringtoneType == "notification")
                put(MediaStore.Audio.Media.IS_ALARM, ringtoneType == "alarm")
                put(MediaStore.Audio.Media.IS_MUSIC, false)
            }

            val contentResolver = context.contentResolver

            // First, try to delete any existing entry for this file
            contentResolver.delete(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                "${MediaStore.Audio.Media.DATA} = ?",
                arrayOf(file.absolutePath)
            )

            // Insert the new audio file
            val uri = contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw RuntimeException("Failed to insert ringtone into MediaStore")

            Timber.d("Created ringtone URI from file: $uri")
            return uri

        } catch (e: Exception) {
            throw RuntimeException("Failed to create ringtone from file: ${e.message}")
        }
    }

    private fun getMimeType(file: File): String {
        return when (file.extension.lowercase()) {
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "ogg" -> "audio/ogg"
            "m4a" -> "audio/mp4"
            "aac" -> "audio/aac"
            else -> "audio/*"
        }
    }

    private fun canWriteSettings(): Boolean {
        return Settings.System.canWrite(context)
    }

    // Utility method to get current ringtone URI
    fun getCurrentRingtoneUri(ringtoneType: String = "ringtone"): Uri? {
        val typeConstant = when (ringtoneType.lowercase()) {
            "ringtone", "phone" -> RingtoneManager.TYPE_RINGTONE
            "notification", "notif" -> RingtoneManager.TYPE_NOTIFICATION
            "alarm" -> RingtoneManager.TYPE_ALARM
            else -> RingtoneManager.TYPE_RINGTONE
        }

        return RingtoneManager.getActualDefaultRingtoneUri(context, typeConstant)
    }

    // Utility method to get available ringtones
    fun getAvailableRingtones(ringtoneType: String = "ringtone"): List<RingtoneInfo> {
        return try {
            val typeConstant = when (ringtoneType.lowercase()) {
                "ringtone", "phone" -> RingtoneManager.TYPE_RINGTONE
                "notification", "notif" -> RingtoneManager.TYPE_NOTIFICATION
                "alarm" -> RingtoneManager.TYPE_ALARM
                else -> RingtoneManager.TYPE_RINGTONE
            }

            val ringtoneManager = RingtoneManager(context)
            ringtoneManager.setType(typeConstant)

            val cursor = ringtoneManager.cursor
            val ringtones = mutableListOf<RingtoneInfo>()

            while (cursor.moveToNext()) {
                val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                val uri = ringtoneManager.getRingtoneUri(cursor.position)
                ringtones.add(RingtoneInfo(title, uri))
            }

            cursor.close()
            ringtones
        } catch (e: Exception) {
            Timber.e(e, "Failed to get available ringtones")
            emptyList()
        }
    }

    data class RingtoneInfo(
        val title: String,
        val uri: Uri
    )
}