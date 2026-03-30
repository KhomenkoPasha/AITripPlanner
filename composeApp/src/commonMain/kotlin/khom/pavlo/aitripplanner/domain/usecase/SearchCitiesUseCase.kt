package khom.pavlo.aitripplanner.domain.usecase

import khom.pavlo.aitripplanner.data.remote.CityAutocompleteRemoteDataSource
import khom.pavlo.aitripplanner.domain.model.AppLanguage
import khom.pavlo.aitripplanner.domain.model.CitySuggestion

class SearchCitiesUseCase(
    private val remoteDataSource: CityAutocompleteRemoteDataSource,
) {
    suspend operator fun invoke(query: String, language: AppLanguage): List<CitySuggestion> {
        return remoteDataSource.searchCities(query, language)
    }
}
