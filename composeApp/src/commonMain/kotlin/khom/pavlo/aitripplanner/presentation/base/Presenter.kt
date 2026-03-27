package khom.pavlo.aitripplanner.presentation.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

abstract class Presenter {
    protected val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun clear() {
        scope.cancel()
    }
}
