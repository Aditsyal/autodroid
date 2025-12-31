package com.aditsyal.autodroid.domain.usecase.executors

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class ToastExecutor @Inject constructor(
    @ApplicationContext private val context: Context
) : ActionExecutor {

    override suspend fun execute(config: Map<String, Any>): Result<Unit> {
        return runCatching {
            val message = config["message"]?.toString() ?: "Automation executed"
            val duration = when (config["duration"]?.toString()?.lowercase()) {
                "long" -> android.widget.Toast.LENGTH_LONG
                else -> android.widget.Toast.LENGTH_SHORT
            }

            withContext(Dispatchers.Main) {
                android.widget.Toast.makeText(context, message, duration).show()
                Timber.i("Toast shown: $message")
            }
        }
    }
}