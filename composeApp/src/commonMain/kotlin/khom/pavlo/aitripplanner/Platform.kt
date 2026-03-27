package khom.pavlo.aitripplanner

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform