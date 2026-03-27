package khom.pavlo.aitripplanner.core.di

import khom.pavlo.aitripplanner.core.network.HttpClientFactory
import khom.pavlo.aitripplanner.data.local.DatabaseFactory
import khom.pavlo.aitripplanner.data.local.TripLocalDataSource
import khom.pavlo.aitripplanner.data.remote.KtorTripsRemoteDataSource
import khom.pavlo.aitripplanner.data.remote.TravelPlannerConfig
import khom.pavlo.aitripplanner.data.remote.TripsRemoteDataSource
import khom.pavlo.aitripplanner.data.repository.LocalSettingsRepository
import khom.pavlo.aitripplanner.data.repository.OfflineFirstTripRepository
import khom.pavlo.aitripplanner.domain.repository.SettingsRepository
import khom.pavlo.aitripplanner.domain.repository.TripRepository
import khom.pavlo.aitripplanner.domain.usecase.CreateTripUseCase
import khom.pavlo.aitripplanner.domain.usecase.DeleteTripUseCase
import khom.pavlo.aitripplanner.domain.usecase.EnsureSeedDataUseCase
import khom.pavlo.aitripplanner.domain.usecase.GenerateTripPlanUseCase
import khom.pavlo.aitripplanner.domain.usecase.GetCurrentAppLanguageUseCase
import khom.pavlo.aitripplanner.domain.usecase.ObserveAppLanguageUseCase
import khom.pavlo.aitripplanner.domain.usecase.ObserveSyncStateUseCase
import khom.pavlo.aitripplanner.domain.usecase.ObserveTripDetailsUseCase
import khom.pavlo.aitripplanner.domain.usecase.ObserveTripsUseCase
import khom.pavlo.aitripplanner.domain.usecase.RequestSyncUseCase
import khom.pavlo.aitripplanner.domain.usecase.SetAppLanguageUseCase
import khom.pavlo.aitripplanner.domain.usecase.SetDayExpandedUseCase
import khom.pavlo.aitripplanner.domain.usecase.UpdateTripUseCase
import khom.pavlo.aitripplanner.presentation.app.AppViewModel
import khom.pavlo.aitripplanner.presentation.details.TripDetailsViewModel
import khom.pavlo.aitripplanner.presentation.planner.PlannerViewModel
import khom.pavlo.aitripplanner.presentation.saved.SavedTripsViewModel
import khom.pavlo.aitripplanner.sync.SyncEngine
import khom.pavlo.aitripplanner.sync.TripSyncEngine
import kotlinx.serialization.json.Json
import org.koin.core.Koin
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

val sharedModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            prettyPrint = false
            encodeDefaults = true
        }
    }
    single { TravelPlannerConfig() }
    single { HttpClientFactory(get()).create() }
    single { DatabaseFactory(get()).create() }
    single { TripLocalDataSource(get()) }
    single<TripsRemoteDataSource> { KtorTripsRemoteDataSource(get(), get()) }
    single<SyncEngine> { TripSyncEngine(get(), get()) }
    single<TripRepository> { OfflineFirstTripRepository(get(), get(), get(), get()) }
    single<SettingsRepository> { LocalSettingsRepository(get()) }

    factory { ObserveTripsUseCase(get()) }
    factory { ObserveTripDetailsUseCase(get()) }
    factory { ObserveSyncStateUseCase(get()) }
    factory { ObserveAppLanguageUseCase(get()) }
    factory { GetCurrentAppLanguageUseCase(get()) }
    factory { SetAppLanguageUseCase(get()) }
    factory { GenerateTripPlanUseCase(get()) }
    factory { CreateTripUseCase(get()) }
    factory { UpdateTripUseCase(get()) }
    factory { DeleteTripUseCase(get()) }
    factory { EnsureSeedDataUseCase(get()) }
    factory { SetDayExpandedUseCase(get()) }
    factory { RequestSyncUseCase(get()) }

    factory { AppViewModel(get(), get()) }
    factory { PlannerViewModel(get(), get(), get(), get(), get(), get()) }
    factory { SavedTripsViewModel(get(), get(), get(), get()) }
    factory { (tripId: String) -> TripDetailsViewModel(tripId, get(), get(), get(), get(), get()) }
}

fun initKoin(platformModule: Module): Koin {
    val existing = GlobalContext.getOrNull()
    if (existing != null) return existing

    return startKoin {
        modules(sharedModule, platformModule)
    }.koin
}
