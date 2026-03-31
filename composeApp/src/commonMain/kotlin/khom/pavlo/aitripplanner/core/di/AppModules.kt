package khom.pavlo.aitripplanner.core.di

import khom.pavlo.aitripplanner.core.network.HttpClientFactory
import khom.pavlo.aitripplanner.data.local.DatabaseFactory
import khom.pavlo.aitripplanner.data.local.PlacePhotoLocalDataSource
import khom.pavlo.aitripplanner.data.local.TripLocalDataSource
import khom.pavlo.aitripplanner.data.remote.CityAutocompleteRemoteDataSource
import khom.pavlo.aitripplanner.data.remote.PhotonCityAutocompleteRemoteDataSource
import khom.pavlo.aitripplanner.data.remote.PhotonConfig
import khom.pavlo.aitripplanner.data.remote.KtorTripsRemoteDataSource
import khom.pavlo.aitripplanner.data.remote.TravelPlannerConfig
import khom.pavlo.aitripplanner.data.remote.TripsRemoteDataSource
import khom.pavlo.aitripplanner.data.repository.DefaultPlacePhotoRepository
import khom.pavlo.aitripplanner.data.repository.LocalSettingsRepository
import khom.pavlo.aitripplanner.data.repository.OfflineFirstTripRepository
import khom.pavlo.aitripplanner.domain.repository.PlacePhotoRepository
import khom.pavlo.aitripplanner.domain.repository.SettingsRepository
import khom.pavlo.aitripplanner.domain.repository.TripRepository
import khom.pavlo.aitripplanner.domain.usecase.AddPlacePhotoUseCase
import khom.pavlo.aitripplanner.domain.usecase.CreateTripUseCase
import khom.pavlo.aitripplanner.domain.usecase.DeleteTripUseCase
import khom.pavlo.aitripplanner.domain.usecase.GetCurrentAppLanguageUseCase
import khom.pavlo.aitripplanner.domain.usecase.GetCurrentAppThemeUseCase
import khom.pavlo.aitripplanner.domain.usecase.ObserveAppLanguageUseCase
import khom.pavlo.aitripplanner.domain.usecase.ObservePlacePhotosUseCase
import khom.pavlo.aitripplanner.domain.usecase.ObserveAppThemeUseCase
import khom.pavlo.aitripplanner.domain.usecase.ObserveSyncStateUseCase
import khom.pavlo.aitripplanner.domain.usecase.ObserveTripDetailsUseCase
import khom.pavlo.aitripplanner.domain.usecase.ObserveTripsUseCase
import khom.pavlo.aitripplanner.domain.usecase.DeletePlacePhotoUseCase
import khom.pavlo.aitripplanner.domain.usecase.RemoveMockDataUseCase
import khom.pavlo.aitripplanner.domain.usecase.RemovePlaceUseCase
import khom.pavlo.aitripplanner.domain.usecase.RequestSyncUseCase
import khom.pavlo.aitripplanner.domain.usecase.SearchCitiesUseCase
import khom.pavlo.aitripplanner.domain.usecase.SetAppLanguageUseCase
import khom.pavlo.aitripplanner.domain.usecase.SetAppThemeUseCase
import khom.pavlo.aitripplanner.domain.usecase.SetDayExpandedUseCase
import khom.pavlo.aitripplanner.domain.usecase.SetPlaceCompletedUseCase
import khom.pavlo.aitripplanner.domain.usecase.UpdateTripUseCase
import khom.pavlo.aitripplanner.presentation.app.AppViewModel
import khom.pavlo.aitripplanner.presentation.dayroute.DayRouteMapViewModel
import khom.pavlo.aitripplanner.presentation.details.TripDetailsViewModel
import khom.pavlo.aitripplanner.presentation.place.PlaceDetailsViewModel
import khom.pavlo.aitripplanner.presentation.planner.PlannerViewModel
import khom.pavlo.aitripplanner.presentation.saved.SavedTripsViewModel
import khom.pavlo.aitripplanner.sync.SyncEngine
import khom.pavlo.aitripplanner.sync.TripSyncEngine
import kotlinx.serialization.json.Json
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

val sharedModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            prettyPrint = false
            encodeDefaults = true
        }
    }
    single { TravelPlannerConfig() }
    single { PhotonConfig() }
    single { HttpClientFactory(get()).create() }
    single { DatabaseFactory().create() }
    single { TripLocalDataSource(get()) }
    single { PlacePhotoLocalDataSource(get()) }
    single<TripsRemoteDataSource> { KtorTripsRemoteDataSource(get(), get()) }
    single<CityAutocompleteRemoteDataSource> { PhotonCityAutocompleteRemoteDataSource(get(), get()) }
    single<SyncEngine> { TripSyncEngine(get(), get()) }
    single<PlacePhotoRepository> { DefaultPlacePhotoRepository(get(), get()) }
    single<TripRepository> { OfflineFirstTripRepository(get(), get(), get(), get()) }
    single<SettingsRepository> { LocalSettingsRepository(get(), get()) }

    factory { ObserveTripsUseCase(get()) }
    factory { ObserveTripDetailsUseCase(get()) }
    factory { ObserveSyncStateUseCase(get()) }
    factory { ObserveAppLanguageUseCase(get()) }
    factory { ObservePlacePhotosUseCase(get()) }
    factory { GetCurrentAppLanguageUseCase(get()) }
    factory { SetAppLanguageUseCase(get()) }
    factory { ObserveAppThemeUseCase(get()) }
    factory { GetCurrentAppThemeUseCase(get()) }
    factory { SetAppThemeUseCase(get()) }
    factory { AddPlacePhotoUseCase(get()) }
    factory { DeletePlacePhotoUseCase(get()) }
    factory { RemoveMockDataUseCase(get()) }
    factory { RemovePlaceUseCase(get()) }
    factory { SetPlaceCompletedUseCase(get()) }
    factory { CreateTripUseCase(get()) }
    factory { UpdateTripUseCase(get()) }
    factory { DeleteTripUseCase(get()) }
    factory { SetDayExpandedUseCase(get()) }
    factory { RequestSyncUseCase(get()) }
    factory { SearchCitiesUseCase(get()) }

    factory { AppViewModel(get(), get(), get(),
        get(), get(), get()) }
    factory { PlannerViewModel(get(), get(), get(),
        get(), get(), get(), get()) }
    factory { SavedTripsViewModel(get(), get(), get(),
        get()) }
    factory { (tripId: String) -> TripDetailsViewModel(tripId, get(),
        get(), get(), get(), get(),
        get(), get()) }
    factory { (tripId: String, dayId: String) ->
        DayRouteMapViewModel(
            tripId = tripId,
            dayId = dayId,
            observeAppLanguage = get(),
            observeTripDetails = get(),
        )
    }
    factory { (tripId: String, dayId: String, placeId: String) ->
        PlaceDetailsViewModel(
            tripId = tripId,
            dayId = dayId,
            placeId = placeId,
            observeAppLanguage = get(),
            observeTripDetails = get(),
            observePlacePhotos = get(),
            addPlacePhoto = get(),
            deletePlacePhoto = get(),
            setPlaceCompleted = get(),
        )
    }
}

fun initKoin(platformModule: Module): Koin {
    val existing = runCatching { KoinPlatform.getKoin() }.getOrNull()
    if (existing != null) return existing

    return startKoin {
        modules(sharedModule, platformModule)
    }.koin
}
