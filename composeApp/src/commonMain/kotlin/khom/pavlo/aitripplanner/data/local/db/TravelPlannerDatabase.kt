package khom.pavlo.aitripplanner.data.local.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [
        TripEntity::class,
        DayEntity::class,
        PlaceEntity::class,
        SyncQueueEntity::class,
        AppSettingsEntity::class,
        PlacePhotoEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@ConstructedBy(TravelPlannerDatabaseConstructor::class)
abstract class TravelPlannerDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun placePhotoDao(): PlacePhotoDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object TravelPlannerDatabaseConstructor : RoomDatabaseConstructor<TravelPlannerDatabase> {
    override fun initialize(): TravelPlannerDatabase
}

fun buildTravelPlannerDatabase(
    builder: RoomDatabase.Builder<TravelPlannerDatabase>,
): TravelPlannerDatabase = builder
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.Default)
    .fallbackToDestructiveMigration(dropAllTables = true)
    .build()
