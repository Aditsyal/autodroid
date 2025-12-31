package com.aditsyal.autodroid.domain.usecase.executors

import com.aditsyal.autodroid.utils.SoundPlayer
import timber.log.Timber
import javax.inject.Inject

class StopSoundExecutor @Inject constructor(
    private val soundPlayer: SoundPlayer
) : ActionExecutor {

    override suspend fun execute(config: Map<String, Any>): Result<Unit> {
        return runCatching {
            soundPlayer.stop()
        }
    }
}