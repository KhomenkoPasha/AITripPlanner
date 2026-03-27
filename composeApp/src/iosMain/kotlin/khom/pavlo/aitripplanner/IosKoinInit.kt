package khom.pavlo.aitripplanner

import khom.pavlo.aitripplanner.core.di.initKoin
import khom.pavlo.aitripplanner.core.platform.SqlDriverFactory
import khom.pavlo.aitripplanner.domain.model.SyncTrigger
import khom.pavlo.aitripplanner.sync.BackgroundSyncScheduler
import org.koin.dsl.module
import platform.BackgroundTasks.BGAppRefreshTask
import platform.BackgroundTasks.BGAppRefreshTaskRequest
import platform.BackgroundTasks.BGTaskScheduler
import platform.Foundation.NSDate

private const val IOS_SYNC_TASK_ID = "khom.pavlo.aitripplanner.sync.refresh"

private class IOSBackgroundSyncScheduler : BackgroundSyncScheduler {
    override suspend fun scheduleSync(trigger: SyncTrigger) {
        val request = BGAppRefreshTaskRequest(identifier = IOS_SYNC_TASK_ID)
        request.earliestBeginDate = NSDate.dateWithTimeIntervalSinceNow(15.0 * 60.0)
        BGTaskScheduler.sharedScheduler.submitTaskRequest(request, null)
    }
}

private fun registerIosBackgroundTasks() {
    BGTaskScheduler.sharedScheduler.registerForTaskWithIdentifier(
        identifier = IOS_SYNC_TASK_ID,
        usingQueue = null,
    ) { task ->
        val refreshTask = task as BGAppRefreshTask
        refreshTask.setTaskCompletedWithSuccess(true)
    }
}

fun initKoinIos() {
    initKoin(
        module {
            single<BackgroundSyncScheduler> { IOSBackgroundSyncScheduler() }
            single { SqlDriverFactory() }
        },
    )
    registerIosBackgroundTasks()
}
