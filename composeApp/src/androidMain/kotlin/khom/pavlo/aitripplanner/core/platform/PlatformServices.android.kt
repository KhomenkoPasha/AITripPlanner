package khom.pavlo.aitripplanner.core.platform

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import khom.pavlo.aitripplanner.data.local.db.TravelPlannerDatabase
import khom.pavlo.aitripplanner.domain.model.SyncTrigger
import khom.pavlo.aitripplanner.sync.BackgroundSyncScheduler
import khom.pavlo.aitripplanner.sync.SyncEngine
import org.koin.core.context.GlobalContext

private const val SYNC_WORK_NAME = "travel_planner_sync"
private const val DATABASE_NAME = "travel_planner.db"

object AndroidPlatformRuntime {
    lateinit var appContext: Context
        private set

    fun install(context: Context) {
        appContext = context.applicationContext
    }
}

actual class SqlDriverFactory {
    actual fun createDriver(): SqlDriver = AndroidSqliteDriver(
        schema = TravelPlannerDatabase.Schema,
        context = AndroidPlatformRuntime.appContext,
        name = DATABASE_NAME,
    )
}

actual object PlatformTime {
    actual fun nowMillis(): Long = System.currentTimeMillis()
}

class AndroidWorkManagerSyncScheduler : BackgroundSyncScheduler {
    override suspend fun scheduleSync(trigger: SyncTrigger) {
        val request = OneTimeWorkRequestBuilder<TripSyncWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        WorkManager.getInstance(AndroidPlatformRuntime.appContext).enqueueUniqueWork(
            SYNC_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }
}

class TripSyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = runCatching {
        val syncEngine = GlobalContext.get().get<SyncEngine>()
        syncEngine.requestSync(SyncTrigger.BACKGROUND)
    }.fold(
        onSuccess = { Result.success() },
        onFailure = { Result.retry() },
    )
}
