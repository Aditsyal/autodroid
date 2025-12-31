package com.aditsyal.autodroid.domain.usecase.executors

import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject

class DelayExecutor @Inject constructor() : ActionExecutor {

    override suspend fun execute(config: Map<String, Any>): Result<Unit> {
        return runCatching {
            val delayMs = config["delayMs"]?.toString()?.toLongOrNull()
                ?: config["delaySeconds"]?.toString()?.toLongOrNull()?.times(1000)
                ?: 1000L

            Timber.d("Delaying for ${delayMs}ms")
            delay(delayMs)
        }
    }
}