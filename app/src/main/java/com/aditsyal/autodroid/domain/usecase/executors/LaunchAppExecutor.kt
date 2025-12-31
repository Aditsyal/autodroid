package com.aditsyal.autodroid.domain.usecase.executors

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class LaunchAppExecutor @Inject constructor(
    @ApplicationContext private val context: Context
) : ActionExecutor {

    override suspend fun execute(config: Map<String, Any>): Result<Unit> {
        return runCatching {
            val packageName = config["packageName"]?.toString()

            if (packageName == null) {
                throw IllegalArgumentException("Package name is required")
            }

            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                Timber.i("App launched: $packageName")
            } else {
                throw IllegalStateException("App not found: $packageName")
            }
        }
    }
}