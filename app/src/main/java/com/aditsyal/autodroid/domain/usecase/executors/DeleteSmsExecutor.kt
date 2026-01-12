package com.aditsyal.autodroid.domain.usecase.executors

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class DeleteSmsExecutor @Inject constructor(
    @ApplicationContext private val context: Context
) : ActionExecutor {

    override suspend fun execute(config: Map<String, Any>): Result<Unit> {
        return runCatching {
            val phoneNumber = config["phoneNumber"]?.toString()
            val messageId = config["messageId"]?.toString()?.toLongOrNull()
            val threadId = config["threadId"]?.toString()?.toLongOrNull()
            val deleteAllFromNumber = config["deleteAllFromNumber"]?.toString()?.toBoolean() ?: false

            when {
                messageId != null -> deleteSmsById(messageId)
                phoneNumber != null && deleteAllFromNumber -> deleteAllSmsFromNumber(phoneNumber)
                phoneNumber != null && threadId != null -> deleteSmsByThread(phoneNumber, threadId)
                phoneNumber != null -> deleteSmsByNumber(phoneNumber)
                else -> throw IllegalArgumentException("Either messageId, phoneNumber, or threadId must be specified")
            }

            Timber.i("SMS deletion executed successfully")
        }.onFailure { e ->
            Timber.e(e, "SMS deletion failed")
        }
    }

    private fun deleteSmsById(messageId: Long) {
        try {
            val contentResolver = context.contentResolver
            val uri = Uri.parse("content://sms/$messageId")

            val deletedRows = contentResolver.delete(uri, null, null)

            if (deletedRows > 0) {
                Timber.d("Deleted SMS with ID: $messageId")
            } else {
                throw RuntimeException("No SMS found with ID: $messageId")
            }

        } catch (e: SecurityException) {
            throw SecurityException("READ_SMS and WRITE_SMS permissions required to delete SMS")
        } catch (e: Exception) {
            throw RuntimeException("Failed to delete SMS by ID: ${e.message}")
        }
    }

    private fun deleteSmsByNumber(phoneNumber: String) {
        try {
            val contentResolver = context.contentResolver
            val uri = Uri.parse("content://sms/")

            // Delete SMS from inbox and sent
            val where = "address = ?"
            val selectionArgs = arrayOf(phoneNumber)

            val deletedRows = contentResolver.delete(uri, where, selectionArgs)

            Timber.d("Deleted $deletedRows SMS messages from number: $phoneNumber")

            if (deletedRows == 0) {
                Timber.w("No SMS messages found from number: $phoneNumber")
            }

        } catch (e: SecurityException) {
            throw SecurityException("READ_SMS and WRITE_SMS permissions required to delete SMS")
        } catch (e: Exception) {
            throw RuntimeException("Failed to delete SMS by number: ${e.message}")
        }
    }

    private fun deleteAllSmsFromNumber(phoneNumber: String) {
        try {
            val contentResolver = context.contentResolver

            // Delete from all SMS folders: inbox, sent, draft, outbox, etc.
            val smsFolders = arrayOf("inbox", "sent", "draft", "outbox", "failed", "queued")

            var totalDeleted = 0

            smsFolders.forEach { folder ->
                val uri = Uri.parse("content://sms/$folder")
                val where = "address = ?"
                val selectionArgs = arrayOf(phoneNumber)

                val deletedRows = contentResolver.delete(uri, where, selectionArgs)
                totalDeleted += deletedRows
                Timber.d("Deleted $deletedRows SMS from $folder for number: $phoneNumber")
            }

            Timber.d("Total SMS deleted from all folders for $phoneNumber: $totalDeleted")

        } catch (e: SecurityException) {
            throw SecurityException("READ_SMS and WRITE_SMS permissions required to delete SMS")
        } catch (e: Exception) {
            throw RuntimeException("Failed to delete all SMS from number: ${e.message}")
        }
    }

    private fun deleteSmsByThread(phoneNumber: String, threadId: Long) {
        try {
            val contentResolver = context.contentResolver
            val uri = Uri.parse("content://sms/")

            val where = "thread_id = ? AND address = ?"
            val selectionArgs = arrayOf(threadId.toString(), phoneNumber)

            val deletedRows = contentResolver.delete(uri, where, selectionArgs)

            Timber.d("Deleted $deletedRows SMS messages from thread $threadId for number: $phoneNumber")

        } catch (e: SecurityException) {
            throw SecurityException("READ_SMS and WRITE_SMS permissions required to delete SMS")
        } catch (e: Exception) {
            throw RuntimeException("Failed to delete SMS by thread: ${e.message}")
        }
    }

    // Utility methods
    fun getSmsCount(phoneNumber: String? = null): Int {
        return try {
            val contentResolver = context.contentResolver
            val uri = Uri.parse("content://sms/")

            val cursor = if (phoneNumber != null) {
                contentResolver.query(uri, arrayOf("count(*) as count"),
                    "address = ?", arrayOf(phoneNumber), null)
            } else {
                contentResolver.query(uri, arrayOf("count(*) as count"), null, null, null)
            }

            cursor?.use {
                if (it.moveToFirst()) {
                    it.getInt(0)
                } else {
                    0
                }
            } ?: 0
        } catch (e: Exception) {
            Timber.e(e, "Failed to count SMS messages")
            0
        }
    }

    fun getSmsThreads(): List<SmsThread> {
        return try {
            val contentResolver = context.contentResolver
            val uri = Uri.parse("content://sms/conversations")

            val projection = arrayOf("thread_id", "address", "body", "date", "msg_count")
            val cursor = contentResolver.query(uri, projection, null, null, "date DESC")

            val threads = mutableListOf<SmsThread>()

            cursor?.use {
                while (it.moveToNext()) {
                    val threadId = it.getLong(0)
                    val address = it.getString(1) ?: ""
                    val body = it.getString(2) ?: ""
                    val date = it.getLong(3)
                    val msgCount = it.getInt(4)

                    threads.add(SmsThread(threadId, address, body, date, msgCount))
                }
            }

            threads
        } catch (e: Exception) {
            Timber.e(e, "Failed to get SMS threads")
            emptyList()
        }
    }

    data class SmsThread(
        val threadId: Long,
        val address: String,
        val lastMessage: String,
        val lastMessageDate: Long,
        val messageCount: Int
    )
}