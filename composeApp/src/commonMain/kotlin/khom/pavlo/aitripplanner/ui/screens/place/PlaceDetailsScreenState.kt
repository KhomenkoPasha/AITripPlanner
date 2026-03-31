package khom.pavlo.aitripplanner.ui.screens.place

import androidx.compose.runtime.Immutable
import khom.pavlo.aitripplanner.ui.model.PlacePhotoUiModel
import khom.pavlo.aitripplanner.ui.model.PlaceDetailsUiModel

@Immutable
data class PlaceDetailsScreenState(
    val place: PlaceDetailsUiModel? = null,
    val photos: List<PlacePhotoUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingPhotos: Boolean = false,
    val isAddingPhoto: Boolean = false,
    val deletingPhotoIds: Set<String> = emptySet(),
    val errorMessage: String? = null,
    val actionErrorMessage: String? = null,
)
