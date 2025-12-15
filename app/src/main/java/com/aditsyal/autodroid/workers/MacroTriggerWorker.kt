package com.aditsyal.autodroid.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aditsyal.autodroid.domain.usecase.CheckTriggersUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class MacroTriggerWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val checkTriggersUseCase: CheckTriggersUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Timber.d("MacroTriggerWorker: Checking for time-based triggers")
        return try {
            checkTriggersUseCase("TIME")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "MacroTriggerWorker: Error checking triggers")
            Result.retry()
        }
    }
}
