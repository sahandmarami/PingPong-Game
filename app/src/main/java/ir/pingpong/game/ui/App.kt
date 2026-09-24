package ir.pingpong.game.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/** پیمایش اصلی بازی — کاملاً راست‌چین (RTL) */
@Composable
fun PingPongApp() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val nav = rememberNavController()
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            NavHost(
                navController = nav,
                startDestination = "menu",
                modifier = Modifier.fillMaxSize(),
                enterTransition = {
                    fadeIn(tween(240)) + scaleIn(initialScale = 0.97f, animationSpec = tween(240))
                },
                exitTransition = { fadeOut(tween(180)) },
                popEnterTransition = { fadeIn(tween(240)) },
                popExitTransition = {
                    fadeOut(tween(180)) + scaleOut(targetScale = 0.97f, animationSpec = tween(180))
                }
            ) {
                composable("menu") {
                    MainMenuScreen(
                        onPlay = { nav.navigate("match") },
                        onCountry = { nav.navigate("country") },
                        onDifficulty = { nav.navigate("difficulty") },
                        onHowTo = { nav.navigate("howto") },
                        onSettings = { nav.navigate("settings") },
                        onAbout = { nav.navigate("about") }
                    )
                }
                composable("country") { CountryScreen(onBack = { nav.popBackStack() }) }
                composable("difficulty") { DifficultyScreen(onBack = { nav.popBackStack() }) }
                composable("howto") { HowToScreen(onBack = { nav.popBackStack() }) }
                composable("settings") { SettingsScreen(onBack = { nav.popBackStack() }) }
                composable("about") { AboutScreen(onBack = { nav.popBackStack() }) }
                composable("match") { MatchScreen(onExit = { nav.popBackStack() }) }
            }
        }
    }
}
