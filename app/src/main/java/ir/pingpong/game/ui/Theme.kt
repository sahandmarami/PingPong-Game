package ir.pingpong.game.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.pingpong.game.R

/** فونت فارسی وزیرمتن */
val AppFont = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

private fun Typography.withFont(f: FontFamily): Typography = copy(
    displayLarge = displayLarge.copy(fontFamily = f),
    displayMedium = displayMedium.copy(fontFamily = f),
    displaySmall = displaySmall.copy(fontFamily = f),
    headlineLarge = headlineLarge.copy(fontFamily = f),
    headlineMedium = headlineMedium.copy(fontFamily = f),
    headlineSmall = headlineSmall.copy(fontFamily = f),
    titleLarge = titleLarge.copy(fontFamily = f),
    titleMedium = titleMedium.copy(fontFamily = f),
    titleSmall = titleSmall.copy(fontFamily = f),
    bodyLarge = bodyLarge.copy(fontFamily = f),
    bodyMedium = bodyMedium.copy(fontFamily = f),
    bodySmall = bodySmall.copy(fontFamily = f),
    labelLarge = labelLarge.copy(fontFamily = f),
    labelMedium = labelMedium.copy(fontFamily = f),
    labelSmall = labelSmall.copy(fontFamily = f)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF0E9D6E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFBFF0DC),
    onPrimaryContainer = Color(0xFF00271A),
    secondary = Color(0xFF1E88E5),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD8EAFF),
    onSecondaryContainer = Color(0xFF0D2B45),
    tertiary = Color(0xFFFF8F00),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFE0B2),
    onTertiaryContainer = Color(0xFF3E2300),
    background = Color(0xFFF4FAF7),
    onBackground = Color(0xFF12261E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF12261E),
    surfaceVariant = Color(0xFFE4F1EA),
    onSurfaceVariant = Color(0xFF3F584D),
    outline = Color(0xFFB0C8BC),
    error = Color(0xFFD2606A),
    onError = Color(0xFFFFFFFF)
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp)
)

@Composable
fun PingPongTheme(content: @Composable () -> Unit) {
    // بازی همیشه با تم روشن نمایش داده می‌شود
    isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography().withFont(AppFont),
        shapes = AppShapes,
        content = content
    )
}
