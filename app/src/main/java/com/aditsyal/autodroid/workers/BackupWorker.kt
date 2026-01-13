package com.aditsyal.autodroid.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import androidx.work.ExistingPeriodicWorkPolicy
import com.aditsyal.autodroid.domain.usecase.ImportExportMacrosUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.concurrent.TimeUnit

@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val importExportMacrosUseCase: ImportExportMacrosUseCase
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME_DAILY = "backup_worker_daily"
        const val WORK_NAME_WEEKLY = "backup_worker_weekly"
        const val TAG = "BackupWorker"
        const val KEY_BACKUP_TYPE = "backup_type"

        fun scheduleDailyBackup(context: Context) {
            scheduleBackup(context, WORK_NAME_DAILY, 1, TimeUnit.DAYS)
        }

        fun scheduleWeeklyBackup(context: Context) {
            scheduleBackup(context, WORK_NAME_WEEKLY, 7, TimeUnit.DAYS)
        }

        private fun scheduleBackup(context: Context, workName: String, interval: Long, unit: TimeUnit) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(true) // Only backup when battery is not low
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .setRequiresStorageNotLow(true) // Require sufficient storage
                .build()

            val workRequest = PeriodicWorkRequestBuilder<BackupWorker>(
                interval, unit,
                interval / 4, unit // Flex interval
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .setInitialDelay(1, TimeUnit.HOURS) // Initial delay of 1 hour
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                workName,
                ExistingPeriodicWorkPolicy.REPLACE, // Replace existing work
                workRequest
            )

            Timber.i("Scheduled $workName backup with interval: $interval ${unit.name.lowercase()}")
        }

        fun cancelAllBackups(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag(TAG)
            Timber.i("Cancelled all backup workers")
        }
    }

    override suspend fun doWork(): Result {
        Timber.d("BackupWorker: Starting automated backup")

        return try {
            val result = importExportMacrosUseCase.exportAllMacros()

            if (result.success && result.uri != null) {
                Timber.i("BackupWorker: Successfully created backup with ${result.macroCount} macros, ${result.variableCount} variables, ${result.templateCount} templates")

                // Here you could optionally upload to cloud storage, send via email, etc.
                // For now, we just log success

                Result.success(workDataOf(
                    "backup_success" to true,
                    "macro_count" to result.macroCount,
                    "timestamp" to System.currentTimeMillis()
                ))
            } else {
                Timber.e("BackupWorker: Backup failed: ${result.error}")
                Result.failure(workDataOf(
                    "backup_success" to false,
                    "error" to (result.error ?: "Unknown error")
                ))
            }
        } catch (e: Exception) {
            Timber.e(e, "BackupWorker: Exception during backup")
            Result.failure(workDataOf(
                "backup_success" to false,
                "error" to (e.message ?: "Exception occurred")
            ))
        }
    }
}