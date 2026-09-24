package ir.pingpong.game.game

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate

/** رندر بازی روی Canvas با نمای شبه‌سه‌بعدی و ظاهر روشن و تمیز */
object GameRenderer {

    fun perspAt(y: Float): Float = 0.55f + 0.45f * (y / 2f)

    fun project(x: Float, y: Float, z: Float, w: Float, h: Float): Offset {
        val t = y / 2f
        val persp = 0.55f + 0.45f * t
        val topY = h * 0.14f
        val botY = h * 0.845f
        val sy = topY + (botY - topY) * t - z * (h * 0.27f) * (0.62f + 0.38f * t)
        val sx = w / 2f + x * (w * 0.40f) * persp
        return Offset(sx, sy)
    }

    fun draw(s: DrawScope, e: PingPongEngine, demo: Boolean) {
        val w = s.size.width
        val h = s.size.height
        val u = w / 420f

        // پس‌زمینه روشن
        s.drawRect(
            Brush.verticalGradient(listOf(Color(0xFFDCEFE6), Color(0xFFFDFEFD))),
            size = s.size
        )

        val farL = project(-1f, 0f, 0f, w, h)
        val farR = project(1f, 0f, 0f, w, h)
        val nearL = project(-1f, 2f, 0f, w, h)
        val nearR = project(1f, 2f, 0f, w, h)
        val cx = w / 2f

        // سایه زیر میز
        val shadowW = (nearR.x - nearL.x) * 1.08f
        s.drawOval(
            Color(0x26002A1E),
            topLeft = Offset(cx - shadowW / 2f, nearL.y + 6f * u),
            size = Size(shadowW, 40f * u)
        )

        // پایه‌های میز
        for (lx in listOf(-0.82f, 0.82f)) {
            val top = project(lx, 1.88f, 0f, w, h)
            s.drawRoundRect(
                Color(0xFF24513F),
                topLeft = Offset(top.x - 5f * u, top.y - 4f * u),
                size = Size(10f * u, 66f * u),
                cornerRadius = CornerRadius(4f * u, 4f * u)
            )
        }

        // صفحه میز
        val tablePath = Path().apply {
            moveTo(farL.x, farL.y)
            lineTo(farR.x, farR.y)
            lineTo(nearR.x, nearR.y)
            lineTo(nearL.x, nearL.y)
            close()
        }
        s.drawPath(
            tablePath,
            Brush.verticalGradient(
                listOf(Color(0xFF38AC84), Color(0xFF2B8F68)),
                startY = farL.y,
                endY = nearL.y
            )
        )

        // لبه جلویی میز (عمق بصری)
        val frontPath = Path().apply {
            moveTo(nearL.x, nearL.y)
            lineTo(nearR.x, nearR.y)
            lineTo(nearR.x, nearR.y + 15f * u)
            lineTo(nearL.x, nearL.y + 15f * u)
            close()
        }
        s.drawPath(frontPath, Color(0xFF20724F))

        // خطوط استاندارد سفید
        s.drawPath(tablePath, Color(0xE6FFFFFF), style = Stroke(2.4f * u))
        val midA = project(0f, 0f, 0f, w, h)
        val midB = project(0f, 2f, 0f, w, h)
        s.drawLine(Color(0xB8FFFFFF), midA, midB, strokeWidth = 2f * u)

        // تور وسط
        val netL = project(-1f, 1f, 0f, w, h)
        val netR = project(1f, 1f, 0f, w, h)
        val netH = PingPongEngine.NET_H * (h * 0.27f) * (0.62f + 0.38f * 0.5f)
        val netTop = netL.y - netH
        s.drawRect(
            Color(0x59FFFFFF),
            topLeft = Offset(netL.x, netTop),
            size = Size(netR.x - netL.x, netH)
        )
        for (i in 1..7) {
            val x = netL.x + (netR.x - netL.x) * i / 8f
            s.drawLine(Color(0x2EFFFFFF), Offset(x, netTop), Offset(x, netL.y), strokeWidth = 1.1f * u)
        }
        s.drawLine(Color(0xFFFFFFFF), Offset(netL.x, netTop), Offset(netR.x, netTop), strokeWidth = 3f * u)
        s.drawCircle(Color(0xFFE8E8E8), 4.2f * u, Offset(netL.x, netTop))
        s.drawCircle(Color(0xFFE8E8E8), 4.2f * u, Offset(netR.x, netTop))

        // سایه توپ روی میز
        val b = e.ball
        if (b.z > 0.03f) {
            val sh = project(b.x, b.y, 0f, w, h)
            val rr = PingPongEngine.BALL_R * (w * 0.40f) * perspAt(b.y)
            val alpha = ((0.30f - b.z * 0.16f).coerceIn(0.05f, 0.30f) * 255f).toInt()
            s.drawOval(
                Color(0, 0, 0, alpha),
                topLeft = Offset(sh.x - rr * 1.1f, sh.y - rr * 0.42f),
                size = Size(rr * 2.2f, rr * 0.84f)
            )
        }

        // رد توپ (فقط در مسابقه)
        if (!demo && e.trail.isNotEmpty()) {
            val n = e.trail.size
            var i = 0
            for (tp in e.trail) {
                val p = project(tp.x, tp.y, tp.z, w, h)
                val a = ((i + 1).toFloat() / n) * 0.20f
                val r = PingPongEngine.BALL_R * (w * 0.40f) * perspAt(tp.y) *
                    (0.55f + 0.45f * (i + 1).toFloat() / n)
                s.drawCircle(Color(0xFFFFA726).copy(alpha = a), r, p)
                i++
            }
        }

        // راکت‌ها (ربات آبی، بازیکن قرمز)
        drawPaddle(s, e.botPaddle, w, h, Color(0xFF5CA3EA), Color(0xFF2E86E0), Color(0xFF1D66B8), bottom = false, u = u)
        drawPaddle(s, e.playerPaddle, w, h, Color(0xFFEF6B6F), Color(0xFFE5484D), Color(0xFFB93840), bottom = true, u = u)

        // توپ نارنجی با هاله و درخشش
        val bp = project(b.x, b.y, b.z, w, h)
        val br = PingPongEngine.BALL_R * (w * 0.40f) * perspAt(b.y) * 1.55f
        s.drawCircle(Color(0x33FFB300), br * 1.55f, bp)
        s.drawCircle(
            Brush.radialGradient(
                listOf(Color(0xFFFFE082), Color(0xFFFFB300), Color(0xFFEF6C00)),
                center = bp - Offset(br * 0.3f, br * 0.35f),
                radius = br * 1.3f
            ),
            br, bp
        )

        // افکت‌های برخورد
        for (ef in e.effects) {
            val p = project(ef.x, ef.y, ef.z, w, h)
            val pr = ef.t / ef.life
            val base = when (ef.type) {
                EffectType.HIT -> 22f * u
                EffectType.BOUNCE -> 13f * u
                EffectType.POINT -> 42f * u
            }
            val col = when (ef.type) {
                EffectType.HIT -> Color(0xFFFFFFFF)
                EffectType.BOUNCE -> Color(0xCCFFFFFF)
                EffectType.POINT -> Color(0xFFFFD54F)
            }
            s.drawCircle(
                col.copy(alpha = (1f - pr) * 0.75f),
                base * (0.4f + pr),
                p,
                style = Stroke(3f * u * (1f - pr) + 1f)
            )
        }
    }

    private fun drawPaddle(
        s: DrawScope,
        p: Paddle,
        w: Float,
        h: Float,
        light: Color,
        main: Color,
        dark: Color,
        bottom: Boolean,
        u: Float
    ) {
        val pos = project(p.x, p.y, 0.10f, w, h)
        val pr = PingPongEngine.PADDLE_R * (w * 0.40f) * perspAt(p.y)

        // سایه راکت
        val sh = project(p.x, p.y, 0f, w, h)
        s.drawOval(
            Color(0x28000000),
            topLeft = Offset(sh.x - pr * 1.02f, sh.y - pr * 0.5f),
            size = Size(pr * 2.04f, pr * 1.0f)
        )

        // دسته راکت
        s.rotate(if (bottom) 10f else -10f, pivot = pos) {
            val handleTop = if (bottom) pos.y + pr * 0.72f else pos.y - pr * 1.55f
            s.drawRoundRect(
                Color(0xFF7A4A21),
                topLeft = Offset(pos.x - pr * 0.19f, handleTop),
                size = Size(pr * 0.38f, pr * 0.85f),
                cornerRadius = CornerRadius(pr * 0.16f, pr * 0.16f)
            )
        }

        // بدنه راکت
        s.drawOval(
            Brush.radialGradient(
                listOf(light, main, dark),
                center = pos - Offset(pr * 0.25f, pr * 0.28f),
                radius = pr * 1.35f
            ),
            topLeft = Offset(pos.x - pr, pos.y - pr * 0.82f),
            size = Size(pr * 2f, pr * 1.64f)
        )
    }
}
