package ir.pingpong.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.pingpong.game.data.Countries
import ir.pingpong.game.data.Prefs
import ir.pingpong.game.game.PingPongEngine
import ir.pingpong.game.game.GameRenderer
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/** منوی اصلی با پیش‌نمایش زنده بازی دو ربات در پس‌زمینه */
@Composable
fun MainMenuScreen(
    onPlay: () -> Unit,
    onCountry: () -> Unit,
    onDifficulty: () -> Unit,
    onHowTo: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        // پیش‌نمایش زنده: دو ربات در حال بازی
        DemoPreview(Modifier.matchParentSize())

        // لایه شفاف ملایم برای خوانایی دکمه‌ها
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xE8FFFFFF), Color(0xCCF1FAF6), Color(0xE9FFFFFF))
                    )
                )
        )

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(30.dp))
            Text(
                "🏓 پینگ پنگ",
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "بازیکن در برابر ربات",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            SelectionChips(onCountry, onDifficulty)
            Spacer(Modifier.height(16.dp))

            PressableButton(
                "شروع بازی", "🏓", onPlay,
                Modifier.fillMaxWidth().height(62.dp)
            )
            Spacer(Modifier.height(10.dp))
            PressableButton(
                "انتخاب کشور", "🌍", onCountry,
                Modifier.fillMaxWidth().height(56.dp),
                container = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.height(10.dp))
            PressableButton(
                "سطح سختی", "⭐", onDifficulty,
                Modifier.fillMaxWidth().height(56.dp),
                container = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.height(10.dp))
            PressableButton(
                "آموزش", "📖", onHowTo,
                Modifier.fillMaxWidth().height(56.dp),
                container = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(10.dp))
            PressableButton(
                "تنظیمات", "⚙️", onSettings,
                Modifier.fillMaxWidth().height(56.dp),
                container = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(10.dp))
            PressableButton(
                "درباره بازی", "ℹ️", onAbout,
                Modifier.fillMaxWidth().height(56.dp),
                container = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(18.dp))
            Text(
                "نسخه ۱٫۰ • کاملاً آفلاین",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SelectionChips(onCountry: () -> Unit, onDifficulty: () -> Unit) {
    val p = Countries.byIdOrNull(Prefs.playerCountryId)
    val b = Countries.byIdOrNull(Prefs.botCountryId)
    Row(verticalAlignment = Alignment.CenterVertically) {
        AssistChip(
            onClick = onCountry,
            label = { Text("${p.name} 🆚 ${b.name}", fontSize = 13.sp) },
            leadingIcon = { FlagIcon(p.flag, 22) },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        Spacer(Modifier.width(8.dp))
        AssistChip(
            onClick = onDifficulty,
            label = { Text("⭐ ${Prefs.difficulty.title}", fontSize = 13.sp) },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}

/**
 * پیش‌نمایش زنده منوی اصلی:
 * همان موتور فیزیک بازی اصلی با دو ربات؛ آرام‌تر و بدون امتیاز نمایشی.
 * روی دستگاه‌های ضعیف به صورت خودکار به ۳۰ فریم کاهش می‌یابد.
 */
@Composable
fun DemoPreview(modifier: Modifier) {
    val engine = remember { PingPongEngine(demoMode = true) }
    var frame by remember { mutableIntStateOf(0) }
    LaunchedEffect(engine) {
        var last = 0L
        var slow = false
        var flip = false
        var accN = 0
        var accT = 0L
        while (isActive) {
            withFrameNanos { now ->
                val raw = if (last == 0L) 16_600_000L else now - last
                last = now
                val dt = (raw / 1e9f).coerceIn(0.001f, 0.05f)
                flip = !flip
                if (!slow || flip) engine.update(dt)
                accT += raw
                accN++
                if (accN >= 120) {
                    if (accT / accN > 24_000_000L) slow = true
                    accN = 0
                    accT = 0L
                }
                frame++
            }
        }
    }
    Canvas(modifier) {
        val f = frame // رندر مجدد با هر فریم
        GameRenderer.draw(this, engine, demo = true)
    }
}
