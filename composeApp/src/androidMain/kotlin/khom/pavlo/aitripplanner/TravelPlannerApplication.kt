package khom.pavlo.aitripplanner

import android.app.Application

class TravelPlannerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoinAndroid(this)
    }
}
