package ir.pingpong.game.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.pingpong.game.data.Country
import ir.pingpong.game.data.Countries
import ir.pingpong.game.data.Prefs
import ir.pingpong.game.game.GameEvent
import ir.pingpong.game.game.GameRenderer
import ir.pingpong.game.game.MatchPhase
import ir.pingpong.game.game.PingPongEngine
import ir.pingpong.game.game.Side
import ir.pingpong.game.game.SoundManager
import ir.pingpong.game.game.Vibro
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay

/** صفحه مسابقه: معرفی، شمارش معکوس، بازی، توقف و نتیجه */
@Composable
fun MatchScreen(onExit: () -> Unit) {
    val context = LocalContext.current
    val playerCountry = Countries.byIdOrNull(Prefs.playerCountryId)
    val botCountry = Countries.byIdOrNull(Prefs.botCountryId)

    val engine = remember { PingPongEngine() }

    // معرفی و شمارش معکوس: ۰ معرفی، ۱ تا ۳ شمارش، ۴ شروع، ۵ پایان معرفی
    var introStep by remember { mutableIntStateOf(-1) }
    var introKey by remember { mutableIntStateOf(0) }
    var paused by remember { mutableStateOf(false) }
    var recorded by remember { mutableStateOf(false) }

    // حالت‌های آینه‌ای برای رابط کاربری
    var sP by remember { mutableIntStateOf(0) }
    var sB by remember { mutableIntStateOf(0) }
    var rally by remember { mutableIntStateOf(0) }
    var phase by remember { mutableStateOf(MatchPhase.SERVE) }
    var lastPointWinner by remember { mutableStateOf<Side?>(null) }

    // معرفی و شمارش معکوس
    LaunchedEffect(introKey) {
        recorded = false
        engine.difficulty = Prefs.difficulty
        introStep = 0
        delay(1600)
        introStep = 1
        SoundManager.play("click", 0.8f)
        delay(640)
        introStep = 2
        SoundManager.play("click", 0.8f)
        delay(640)
        introStep = 3
        SoundManager.play("click", 0.8f)
        delay(640)
        introStep = 4
        SoundManager.play("point_win", 0.8f)
        delay(700)
        introStep = 5
        engine.startMatch()
    }

    // حلقه بازی: با هر فریم نمایشگر
    var frame by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        var last = 0L
        while (isActive) {
            withFrameNanos { now ->
                val dt = if (last == 0L) 0.0166f else ((now - last) / 1e9f).coerceIn(0.001f, 0.05f)
                last = now
                if (introStep >= 5 && !paused) engine.update(dt)
                frame++
            }
            // همگام‌سازی حالت‌ها با موتور
            if (sP != engine.scorePlayer) sP = engine.scorePlayer
            if (sB != engine.scoreBot) sB = engine.scoreBot
            if (rally != engine.rallyHits) rally = engine.rallyHits
            if (phase != engine.phase) {
                phase = engine.phase
                if (phase == MatchPhase.POINT) {
                    lastPointWinner = engine.pointWinner
                    if (engine.pointWinner == Side.PLAYER) {
                        SoundManager.play("point_win")
                    } else {
                        SoundManager.play("point_lose")
                    }
                    Vibro.buzz(context, 45)
                }
                if (phase == MatchPhase.GAME_OVER) {
                    val winner = if (engine.scorePlayer > engine.scoreBot) Side.PLAYER else Side.BOT
                    SoundManager.play(if (winner == Side.PLAYER) "win" else "lose")
                    Vibro.pattern(context)
                }
            }
            // صدا و افکت رویدادهای جزئی
            for (ev in engine.events) {
                when (ev) {
                    is GameEvent.Hit ->
                        SoundManager.play("hit", 0.9f, if (ev.side == Side.PLAYER) 1f else 0.9f)
                    is GameEvent.Bounce -> SoundManager.play("bounce", 0.65f)
                    else -> Unit
                }
            }
            engine.events.clear()
            // ثبت رکورد محلی پس از پایان مسابقه
            if (phase == MatchPhase.GAME_OVER && !recorded) {
                recorded = true
                val won = engine.scorePlayer > engine.scoreBot
                Prefs.recordMatch(won, engine.scoreBot, engine.longestRally)
            }
        }
    }

    // کنترل لمسی و ماوس: حرکت نسبی انگشت = حرکت راکت
    val inputModifier = Modifier.pointerInput(Unit) {
        awaitPointerEventScope {
            var lastPos = Offset.Zero
            var tracking = false
            while (true) {
                val ev = awaitPointerEvent()
                val c = ev.changes.firstOrNull() ?: continue
                val w = size.width.toFloat()
                val h = size.height.toFloat()
                val xSens = 1f / (w * 0.40f).coerceAtLeast(1f)
                val ySens = 0.53f / (0.185f * h).coerceAtLeast(1f)
                if (c.pressed) {
                    val pos = c.position
                    if (!tracking) {
                        lastPos = pos
                        tracking = true
                    } else {
                        val d = pos - lastPos
                        engine.inputX += d.x * xSens
                        engine.inputY += d.y * ySens
                        lastPos = pos
                    }
                } else {
                    tracking = false
                    // پشتیبانی از ماوس در شبیه‌ساز (حرکت بدون کلیک)
                    if (c.type == PointerType.Mouse) {
                        val d = c.positionChange()
                        if (d != Offset.Zero) {
                            engine.inputX += d.x * xSens
                            engine.inputY += d.y * ySens
                        }
                    }
                }
                c.consume()
            }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .then(inputModifier)
    ) {
        // صحنه بازی
        Canvas(Modifier.fillMaxSize()) {
            val f = frame // رندر مجدد با هر فریم
            GameRenderer.draw(this, engine, demo = false)
        }

        // نوار امتیاز بالا
        Column(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScoreChip(playerCountry, sP, highlight = lastPointWinner == Side.PLAYER)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { paused = true }) {
                    Text("⏸", fontSize = 19.sp)
                }
                Spacer(Modifier.weight(1f))
                ScoreChip(botCountry, sB, highlight = lastPointWinner == Side.BOT)
            }
            AnimatedVisibility(
                visible = rally >= 3 && phase == MatchPhase.RALLY && introStep >= 5,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        "رالی: ${rally.fa()}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(Color(0xCCFFFFFF), RoundedCornerShape(50))
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // پیام امتیاز
        AnimatedVisibility(
            visible = phase == MatchPhase.POINT && introStep >= 5,
            enter = scaleIn(
                initialScale = 0.6f,
                animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium)
            ) + fadeIn(),
            exit = fadeOut(tween(150)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            val win = lastPointWinner == Side.PLAYER
            Text(
                if (win) "🎉 امتیاز شما!" else "امتیاز ربات",
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                color = if (win) Color(0xFF0E9D6E) else Color(0xFFD2606A),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(Color(0xE8FFFFFF), RoundedCornerShape(24.dp))
                    .padding(horizontal = 26.dp, vertical = 14.dp)
            )
        }

        // معرفی و شمارش معکوس
        if (introStep in 0..4) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFF4FBF8), Color(0xFFFFFFFF))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = introStep,
                    transitionSpec = {
                        (scaleIn(
                            initialScale = 0.5f,
                            animationSpec = spring(dampingRatio = 0.55f)
                        ) + fadeIn(tween(150))) togetherWith fadeOut(tween(150))
                    },
                    label = "intro"
                ) { step ->
                    when {
                        step == 0 -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    FlagIcon(playerCountry.flag, 84)
                                    Spacer(Modifier.height(8.dp))
                                    Text(playerCountry.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    "در برابر",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 18.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    FlagIcon(botCountry.flag, 84)
                                    Spacer(Modifier.height(8.dp))
                                    Text(botCountry.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(Modifier.height(22.dp))
                            Text("آماده‌ای؟", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        step in 1..3 -> Text(
                            step.fa(),
                            fontSize = 110.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        step == 4 -> Text(
                            "🏓 شروع!",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // صفحه نتیجه
        if (phase == MatchPhase.GAME_OVER && introStep >= 5) {
            val win = sP > sB
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0xEBF3FAF7)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (win) "🏆" else "😔", fontSize = 60.sp)
                    Text(
                        if (win) "پیروزی!" else "شکست",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black,
                        color = if (win) Color(0xFF0E9D6E) else Color(0xFFD2606A)
                    )
                    Spacer(Modifier.height(16.dp))
                    ResultRow(playerCountry, sP, win)
                    ResultRow(botCountry, sB, !win)
                    if (engine.longestRally >= 6) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "طولانی‌ترین رالی: ${engine.longestRally.fa()}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    Row {
                        PressableButton(
                            "بازی دوباره", "🔄",
                            onClick = {
                                SoundManager.play("click")
                                introKey++
                                phase = MatchPhase.SERVE
                            },
                            modifier = Modifier.width(160.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        PressableButton(
                            "بازگشت به منو", "🏠",
                            onClick = {
                                SoundManager.play("click")
                                onExit()
                            },
                            modifier = Modifier.width(170.dp),
                            container = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }

    // گفتگوی توقف
    if (paused && phase != MatchPhase.GAME_OVER && introStep >= 5) {
        AlertDialog(
            onDismissRequest = { paused = false },
            title = { Text("توقف بازی") },
            text = { Text("می‌خواهی چه کار کنی؟") },
            confirmButton = {
                Button(onClick = { paused = false }) { Text("ادامه بازی") }
            },
            dismissButton = {
                OutlinedButton(onClick = onExit) { Text("بازگشت به منو") }
            }
        )
    }

    BackHandler {
        if (phase == MatchPhase.GAME_OVER || introStep in 0..4) {
            onExit()
        } else {
            paused = true
        }
    }
}

@Composable
private fun ScoreChip(country: Country, score: Int, highlight: Boolean) {
    val scale = remember { Animatable(1f) }
    LaunchedEffect(score) {
        if (score > 0) {
            scale.snapTo(1.4f)
            scale.animateTo(1f, spring(stiffness = Spring.StiffnessMedium))
        }
    }
    Row(
        Modifier
            .background(if (highlight) Color(0xFFFFFFFF) else Color(0xCCFFFFFF), RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FlagIcon(country.flag, 30)
        Spacer(Modifier.width(8.dp))
        Text(country.name, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(8.dp))
        Text(
            score.fa(),
            fontSize = 23.sp,
            fontWeight = FontWeight.Black,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
        )
    }
}

@Composable
private fun ResultRow(country: Country, score: Int, winner: Boolean) {
    Row(
        Modifier
            .width(240.dp)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FlagIcon(country.flag, 36)
        Spacer(Modifier.width(10.dp))
        Text(
            country.name,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = if (winner) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.weight(1f))
        Text(
            score.fa(),
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
            color = if (winner) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
