package khom.pavlo.aitripplanner.core.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.sqlite.db.SupportSQLiteDatabase
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
        callback = object : AndroidSqliteDriver.Callback(TravelPlannerDatabase.Schema) {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                migrateLegacyPlaceSchema(db)
            }
        },
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

private fun migrateLegacyPlaceSchema(db: SupportSQLiteDatabase) {
    val existingColumns = mutableSetOf<String>()
    db.query("PRAGMA table_info(PlaceEntity)").use { cursor ->
        val nameColumnIndex = cursor.getColumnIndex("name")
        while (cursor.moveToNext()) {
            if (nameColumnIndex >= 0) {
                existingColumns += cursor.getString(nameColumnIndex)
            }
        }
    }

    if ("note" !in existingColumns) {
        db.execSQL("ALTER TABLE PlaceEntity ADD COLUMN note TEXT NOT NULL DEFAULT ''")
    }
    if ("category" !in existingColumns) {
        db.execSQL("ALTER TABLE PlaceEntity ADD COLUMN category TEXT")
    }
    if ("short_description" !in existingColumns) {
        db.execSQL("ALTER TABLE PlaceEntity ADD COLUMN short_description TEXT NOT NULL DEFAULT ''")
    }
    if ("full_description" !in existingColumns) {
        db.execSQL("ALTER TABLE PlaceEntity ADD COLUMN full_description TEXT NOT NULL DEFAULT ''")
    }
    if ("why_included" !in existingColumns) {
        db.execSQL("ALTER TABLE PlaceEntity ADD COLUMN why_included TEXT NOT NULL DEFAULT ''")
    }
    if ("tips_text" !in existingColumns) {
        db.execSQL("ALTER TABLE PlaceEntity ADD COLUMN tips_text TEXT NOT NULL DEFAULT ''")
    }
    if ("opening_hours_text" !in existingColumns) {
        db.execSQL("ALTER TABLE PlaceEntity ADD COLUMN opening_hours_text TEXT NOT NULL DEFAULT ''")
    }
    if ("best_time_to_visit" !in existingColumns) {
        db.execSQL("ALTER TABLE PlaceEntity ADD COLUMN best_time_to_visit TEXT NOT NULL DEFAULT ''")
    }
    if ("is_open_now" !in existingColumns) {
        db.execSQL("ALTER TABLE PlaceEntity ADD COLUMN is_open_now INTEGER")
    }
    if ("website_url" !in existingColumns) {
        db.execSQL("ALTER TABLE PlaceEntity ADD COLUMN website_url TEXT")
    }
    if ("latitude" !in existingColumns) {
        db.execSQL("ALTER TABLE PlaceEntity ADD COLUMN latitude REAL")
    }
    if ("longitude" !in existingColumns) {
        db.execSQL("ALTER TABLE PlaceEntity ADD COLUMN longitude REAL")
    }
    if ("photo_url" !in existingColumns) {
        db.execSQL("ALTER TABLE PlaceEntity ADD COLUMN photo_url TEXT")
    }
    if ("photo_urls_text" !in existingColumns) {
        db.execSQL("ALTER TABLE PlaceEntity ADD COLUMN photo_urls_text TEXT NOT NULL DEFAULT ''")
    }
    if ("photo_attribution" !in existingColumns) {
        db.execSQL("ALTER TABLE PlaceEntity ADD COLUMN photo_attribution TEXT")
    }
    if ("price_level" !in existingColumns) {
        db.execSQL("ALTER TABLE PlaceEntity ADD COLUMN price_level TEXT")
    }
    if ("visit_notes" !in existingColumns) {
        db.execSQL("ALTER TABLE PlaceEntity ADD COLUMN visit_notes TEXT NOT NULL DEFAULT ''")
    }
    if ("neighborhood" !in existingColumns) {
        db.execSQL("ALTER TABLE PlaceEntity ADD COLUMN neighborhood TEXT NOT NULL DEFAULT ''")
    }
    if ("stop_index" !in existingColumns) {
        db.execSQL("ALTER TABLE PlaceEntity ADD COLUMN stop_index INTEGER")
    }
    if ("previous_place_name" !in existingColumns) {
        db.execSQL("ALTER TABLE PlaceEntity ADD COLUMN previous_place_name TEXT")
    }
    if ("next_place_name" !in existingColumns) {
        db.execSQL("ALTER TABLE PlaceEntity ADD COLUMN next_place_name TEXT")
    }
    if ("is_completed" !in existingColumns) {
        db.execSQL("ALTER TABLE PlaceEntity ADD COLUMN is_completed INTEGER NOT NULL DEFAULT 0")
    }
}
