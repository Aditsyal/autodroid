package com.aditsyal.autodroid.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.aditsyal.autodroid.data.local.dao.ExecutionLogDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.concurrent.TimeUnit

@HiltWorker
class CleanupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val executionLogDao: ExecutionLogDao
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "cleanup_worker"
        const val TAG = "CleanupWorker"
        const val KEY_RETENTION_DAYS = "retention_days"

        fun schedulePeriodicCleanup(context: Context, retentionDays: Int = 30) {
            val constraints = Constraints.Builder()
                .setRequiresDeviceIdle(true) // Only run when device is idle
                .setRequiresBatteryNotLow(true) // Require decent battery
                .build()

            val workRequest = PeriodicWorkRequestBuilder<CleanupWorker>(
                7, TimeUnit.DAYS, // Run weekly with 7-day flex
                1, TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .setInputData(workDataOf(KEY_RETENTION_DAYS to retentionDays))
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )

            Timber.i("Scheduled periodic cleanup with $retentionDays days retention")
        }

        fun runOneTimeCleanup(context: Context, retentionDays: Int = 30) {
            val workRequest = OneTimeWorkRequestBuilder<CleanupWorker>()
                .setInputData(workDataOf(KEY_RETENTION_DAYS to retentionDays))
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
            Timber.i("Scheduled one-time cleanup with $retentionDays days retention")
        }

        fun cancelCleanup(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag(TAG)
            Timber.i("Cancelled all cleanup workers")
        }
    }

    override suspend fun doWork(): Result {
        val retentionDays = inputData.getInt(KEY_RETENTION_DAYS, 30)
        Timber.d("CleanupWorker: Starting cleanup of logs older than $retentionDays days")

        return try {
            val cutoffTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(retentionDays.toLong())
            val deletedCount = executionLogDao.deleteOldLogs(cutoffTime)

            Timber.i("CleanupWorker: Successfully deleted $deletedCount old execution logs")

            Result.success(workDataOf(
                "deleted_count" to deletedCount,
                "cutoff_time" to cutoffTime
            ))
        } catch (e: Exception) {
            Timber.e(e, "CleanupWorker: Failed to cleanup logs")
            Result.failure(workDataOf(
                "error" to (e.message ?: "Unknown error")
            ))
        }
    }
}