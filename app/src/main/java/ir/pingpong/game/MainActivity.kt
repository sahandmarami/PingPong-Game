package ir.pingpong.game

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import ir.pingpong.game.data.Prefs
import ir.pingpong.game.game.SoundManager
import ir.pingpong.game.game.Vibro
import ir.pingpong.game.ui.PingPongApp
import ir.pingpong.game.ui.PingPongTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Prefs.init(applicationContext)
        SoundManager.init(applicationContext)
        SoundManager.enabled = Prefs.soundOn
        Vibro.enabled = Prefs.vibrationOn
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            PingPongTheme {
                val view = LocalView.current
                SideEffect {
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
                }
                PingPongApp()
            }
        }
    }
}
