package com.aditsyal.autodroid.domain.usecase.executors

import android.net.Uri
import com.aditsyal.autodroid.utils.SoundPlayer
import timber.log.Timber
import javax.inject.Inject

class PlaySoundExecutor @Inject constructor(
    private val soundPlayer: SoundPlayer
) : ActionExecutor {

    override suspend fun execute(config: Map<String, Any>): Result<Unit> {
        return runCatching {
            val soundType = config["soundType"]?.toString() ?: "DEFAULT"
            val customUri = config["uri"]?.toString()?.let { Uri.parse(it) }

            soundPlayer.playSound(soundType, customUri)
        }
    }
}