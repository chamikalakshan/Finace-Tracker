package com.spendly.financetracker.util

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.spendly.financetracker.worker.SpendlySyncWorker
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val workManager: WorkManager
) {
    fun schedulePeriodicSync() {
        workManager.enqueueUniquePeriodicWork(
            SpendlySyncWorker.PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            SpendlySyncWorker.periodicRequest()
        )
    }

    fun startImmediateSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<SpendlySyncWorker>()
            .setConstraints(constraints)
            .build()
        workManager.enqueueUniqueWork(
            SpendlySyncWorker.IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
