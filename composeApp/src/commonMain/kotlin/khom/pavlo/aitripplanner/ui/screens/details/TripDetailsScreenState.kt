package khom.pavlo.aitripplanner.ui.screens.details

import androidx.compose.runtime.Immutable
import khom.pavlo.aitripplanner.ui.model.TripDetailsUiModel

@Immutable
data class TripDetailsScreenState(
    val trip: TripDetailsUiModel? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val syncStatusLabel: String? = null,
    val isDeleteDialogVisible: Boolean = false,
    val isDeleting: Boolean = false,
    val deleteErrorMessage: String? = null,
)
