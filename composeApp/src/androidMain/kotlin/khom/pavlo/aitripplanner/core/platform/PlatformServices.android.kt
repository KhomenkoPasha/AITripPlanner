package khom.pavlo.aitripplanner.core.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
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

actual fun getDatabaseBuilder(): RoomDatabase.Builder<TravelPlannerDatabase> {
    val context = AndroidPlatformRuntime.appContext
    val dbFile = context.getDatabasePath(DATABASE_NAME)
    return Room.databaseBuilder<TravelPlannerDatabase>(
        context = context,
        name = dbFile.absolutePath,
    )
}

actual object PlatformTime {
    actual fun nowMillis(): Long = System.currentTimeMillis()
}

actual object PlatformMapLauncher {
    actual fun showOnMap(label: String, latitude: Double, longitude: Double): Boolean {
        val geoUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(label)})")
        val intent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching {
            AndroidPlatformRuntime.appContext.startActivity(intent)
        }.isSuccess
    }

    actual fun openInMaps(label: String, latitude: Double, longitude: Double): Boolean {
        val url = "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching {
            AndroidPlatformRuntime.appContext.startActivity(intent)
        }.isSuccess
    }
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
