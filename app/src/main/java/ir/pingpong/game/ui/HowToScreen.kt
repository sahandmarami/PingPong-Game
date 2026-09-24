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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** بخش آموزش کوتاه و تصویری */
@Composable
fun HowToScreen(onBack: () -> Unit) {
    AppScreen("آموزش", "📖", onBack) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
        ) {
            // تصویر آموزشی: اثر محل برخورد روی مسیر توپ
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(
                        Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    ) { drawHitZones() }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "برخورد چپ ← توپ به چپ",
                            fontSize = 12.sp,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(
                            "برخورد راست → توپ به راست",
                            fontSize = 12.sp,
                            color = Color(0xFFE65100),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            val tips = listOf(
                Triple("🏓", "حرکت راکت", "انگشتت را روی صفحه بکش؛ راکت قرمز همراه انگشتت حرکت می‌کند، روان و بدون تأخیر. با ماوس هم می‌توانی بازی کنی."),
                Triple("🎯", "محل برخورد", "محل برخورد توپ با راکت روی مسیر برگشت توپ اثر می‌گذارد: برخورد در سمت چپ راکت توپ را به چپ می‌فرستد و برعکس. ضربه به لبه راکت زاویه تندتری می‌سازد."),
                Triple("⚡", "قدرت ضربه", "حرکت سریع دست هنگام برخورد، توپ را سریع‌تر و عمیق‌تر به زمین حریف می‌فرستد. حرکت آرام، ضربه‌ای نرم و امن است."),
                Triple("🎈", "ارتفاع برخورد", "ضربه به توپ در ارتفاع کم، قوس بلندتری می‌سازد؛ ضربه در ارتفاع بلند، ضربه‌ای تخت و تند است."),
                Triple("🏆", "امتیازگیری", "هر کس نتواند توپ را قانونی برگرداند امتیاز را از دست می‌دهد. بازی تا ۱۱ امتیاز است و برنده باید حداقل ۲ امتیاز جلوتر باشد. در تساوی ۱۰-۱۰ بازی تا اختلاف ۲ امتیاز ادامه می‌یابد."),
                Triple("🔥", "رالی", "هرچه رالی طولانی‌تر شود، سرعت توپ کم‌کم بیشتر می‌شود! شمارنده رالی بالای صفحه دیده می‌شود.")
            )
            tips.forEach { (emoji, title, body) ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("$emoji $title", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            body,
                            fontSize = 13.sp,
                            lineHeight = 21.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

private fun DrawScope.drawHitZones() {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val r = size.minDimension * 0.30f

    // بدنه راکت (قرمز پایه)
    drawCircle(Color(0xFFE5484D), r, Offset(cx, cy))

    // نیمه چپ سبز / نیمه راست نارنجی
    val left = Path().apply {
        arcTo(
            Rect(cx - r, cy - r, cx + r, cy + r),
            startAngleDegrees = 90f,
            sweepAngleDegrees = 180f,
            forceMoveTo = true
        )
        close()
    }
    drawPath(left, Color(0x884CAF50))
    val right = Path().apply {
        arcTo(
            Rect(cx - r, cy - r, cx + r, cy + r),
            startAngleDegrees = -90f,
            sweepAngleDegrees = 180f,
            forceMoveTo = true
        )
        close()
    }
    drawPath(right, Color(0x88FF9800))

    // خط جداکننده
    drawLine(
        Color(0xFFFFFFFF),
        Offset(cx, cy - r),
        Offset(cx, cy + r),
        strokeWidth = size.width * 0.008f
    )

    // توپ و جهت حرکت (سمت چپ)
    val bx = cx - r * 1.55f
    val by = cy - r * 0.15f
    drawCircle(Color(0xFFFFB300), r * 0.24f, Offset(bx, by))
    drawCircle(Color(0x33FFB300), r * 0.36f, Offset(bx, by))
    // فلش به سمت چپ
    val ax = bx - r * 0.55f
    val arrow = Path().apply {
        moveTo(ax, by)
        lineTo(ax + r * 0.3f, by - r * 0.2f)
        lineTo(ax + r * 0.3f, by + r * 0.2f)
        close()
    }
    drawPath(arrow, Color(0xFF2E7D32))
}
