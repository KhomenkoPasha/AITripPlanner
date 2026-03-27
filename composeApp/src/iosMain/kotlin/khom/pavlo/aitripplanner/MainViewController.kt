package khom.pavlo.aitripplanner

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController {
    initKoinIos()
    App()
}
