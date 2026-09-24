package ir.pingpong.game.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.pingpong.game.data.Prefs

/** درباره بازی + رکوردهای محلی */
@Composable
fun AboutScreen(onBack: () -> Unit) {
    var wins by remember { mutableIntStateOf(Prefs.wins) }
    var losses by remember { mutableIntStateOf(Prefs.losses) }
    var bestOpp by remember { mutableIntStateOf(Prefs.bestOpp) }
    var longest by remember { mutableIntStateOf(Prefs.longestRally) }
    var confirmReset by remember { mutableStateOf(false) }

    AppScreen("درباره بازی", "ℹ️", onBack) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🏓", fontSize = 52.sp)
            Text("پینگ پنگ", fontSize = 24.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            Text(
                "یک بازی پینگ پنگ کامل و آفلاین؛ تو را در برابر ربات قرار می‌دهد. کشور و سطح سختی را انتخاب کن، با فیزیک واقعی توپ بازی کن و رکوردت را بهتر کن. همه چیز روی خود گوشی ذخیره می‌شود.",
                fontSize = 13.5.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(18.dp))

            // رکورد من
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("📊 رکورد من", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    RecordRow("تعداد بردها", wins.fa())
                    RecordRow("تعداد باخت‌ها", losses.fa())
                    RecordRow(
                        "بهترین نتیجه",
                        if (bestOpp >= 0) "۱۱ - ${bestOpp.fa()}" else "—"
                    )
                    RecordRow(
                        "طولانی‌ترین رالی",
                        if (longest > 0) longest.fa() else "—"
                    )
                }
            }

            TextButton(onClick = { confirmReset = true }) {
                Text(
                    "🗑 پاک کردن رکوردها",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "نسخه ۱٫۰",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "ساخته‌شده با Kotlin و Jetpack Compose",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("پاک کردن رکوردها؟") },
            text = { Text("همه بردها، باخت‌ها و رکوردهای تو پاک می‌شوند.") },
            confirmButton = {
                Button(onClick = {
                    Prefs.resetRecords()
                    wins = 0
                    losses = 0
                    bestOpp = -1
                    longest = 0
                    confirmReset = false
                }) { Text("پاک کن") }
            },
            dismissButton = {
                OutlinedButton(onClick = { confirmReset = false }) { Text("بی‌خیال") }
            }
        )
    }
}

@Composable
private fun RecordRow(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}
