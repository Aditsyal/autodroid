package com.aditsyal.autodroid.domain.usecase.executors

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class MakeCallExecutor @Inject constructor(
    @ApplicationContext private val context: Context
) : ActionExecutor {

    override suspend fun execute(config: Map<String, Any>): Result<Unit> {
        return runCatching {
            val phoneNumber = config["phoneNumber"]?.toString()
                ?: throw IllegalArgumentException("phoneNumber is required for making a call")

            val action = config["action"]?.toString()?.lowercase() ?: "call"
            val useDialer = config["useDialer"]?.toString()?.toBoolean() ?: false

            when (action) {
                "call", "dial" -> makePhoneCall(phoneNumber, useDialer)
                else -> throw IllegalArgumentException("Unknown call action: $action")
            }

            Timber.i("Phone call action executed for: $phoneNumber")
        }.onFailure { e ->
            Timber.e(e, "Phone call execution failed")
        }
    }

    private fun makePhoneCall(phoneNumber: String, useDialer: Boolean) {
        try {
            // Clean the phone number (remove spaces, dashes, etc.)
            val cleanNumber = phoneNumber.replace(Regex("[^\\d+]"), "")

            if (cleanNumber.isBlank()) {
                throw IllegalArgumentException("Invalid phone number: $phoneNumber")
            }

            val uri = Uri.parse("tel:$cleanNumber")
            val intent = if (useDialer) {
                // Use ACTION_DIAL to open dialer without making the call
                Intent(Intent.ACTION_DIAL, uri)
            } else {
                // Use ACTION_CALL to directly make the call
                Intent(Intent.ACTION_CALL, uri)
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(intent)

            Timber.d("Initiated ${if (useDialer) "dialer" else "call"} to: $cleanNumber")

        } catch (e: SecurityException) {
            throw SecurityException("CALL_PHONE permission required to make phone calls")
        } catch (e: Exception) {
            throw RuntimeException("Failed to make phone call: ${e.message}")
        }
    }

    // Utility method to check if we have the required permission
    fun hasCallPhonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CALL_PHONE
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    // Utility method to format phone number
    fun formatPhoneNumber(phoneNumber: String): String {
        // Remove all non-digit characters except +
        val clean = phoneNumber.replace(Regex("[^\\d+]"), "")

        // Basic formatting for US numbers (can be extended for other countries)
        return when {
            clean.startsWith("+1") && clean.length == 12 -> {
                // +12345678901 -> +1 (234) 567-8901
                "+1 (${clean.substring(2, 5)}) ${clean.substring(5, 8)}-${clean.substring(8)}"
            }
            clean.startsWith("+") -> {
                // Keep international format as-is
                clean
            }
            clean.length == 10 -> {
                // 1234567890 -> (234) 567-8901
                "(${clean.substring(0, 3)}) ${clean.substring(3, 6)}-${clean.substring(6)}"
            }
            clean.length == 11 && clean.startsWith("1") -> {
                // 12345678901 -> 1 (234) 567-8901
                "1 (${clean.substring(1, 4)}) ${clean.substring(4, 7)}-${clean.substring(7)}"
            }
            else -> {
                // Return as-is for other formats
                clean
            }
        }
    }

    // Method to check if a phone number is valid
    fun isValidPhoneNumber(phoneNumber: String): Boolean {
        val clean = phoneNumber.replace(Regex("[^\\d+]"), "")

        // Basic validation: should contain only digits and optionally start with +
        return clean.matches(Regex("^\\+?\\d+$")) && clean.length >= 7
    }
}