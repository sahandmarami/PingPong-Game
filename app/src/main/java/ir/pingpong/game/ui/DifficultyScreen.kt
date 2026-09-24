package ir.pingpong.game.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.pingpong.game.data.Prefs
import ir.pingpong.game.game.Difficulty
import ir.pingpong.game.game.SoundManager

/** صفحه انتخاب سطح سختی ربات */
@Composable
fun DifficultyScreen(onBack: () -> Unit) {
    var sel by remember { mutableStateOf(Prefs.difficulty) }

    AppScreen("سطح سختی", "⭐", onBack) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            val items = listOf(
                Triple(Difficulty.EASY, "🟢", listOf("ربات کندتر و با خطای بیشتر", "مناسب برای شروع و یادگیری")),
                Triple(Difficulty.MEDIUM, "🟡", listOf("واکنش طبیعی و خطای متعادل", "بازی رقابتی و لذت‌بخش")),
                Triple(Difficulty.HARD, "🔴", listOf("واکنش سریع و دقت بالا", "رالی‌های طولانی و چالش جدی"))
            )
            items.forEach { (d, emoji, lines) ->
                val isSel = d == sel
                Card(
                    onClick = {
                        if (!isSel) SoundManager.play("click", 0.6f)
                        sel = d
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSel) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isSel) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 7.dp)
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(emoji, fontSize = 30.sp)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(d.title, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            lines.forEach {
                                Text(
                                    it,
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                        if (isSel) {
                            Text(
                                "✓",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            PressableButton(
                "ذخیره و بازگشت", "✅",
                onClick = {
                    Prefs.difficulty = sel
                    SoundManager.play("point_win", 0.7f)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}
