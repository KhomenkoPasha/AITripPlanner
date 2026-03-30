package khom.pavlo.aitripplanner.ui.screens.place

import androidx.compose.runtime.Immutable
import khom.pavlo.aitripplanner.ui.model.PlaceDetailsUiModel

@Immutable
data class PlaceDetailsScreenState(
    val place: PlaceDetailsUiModel? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val actionErrorMessage: String? = null,
)
