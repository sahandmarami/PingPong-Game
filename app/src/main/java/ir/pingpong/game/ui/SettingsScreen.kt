package ir.pingpong.game.ui

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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.pingpong.game.data.Prefs
import ir.pingpong.game.game.SoundManager
import ir.pingpong.game.game.Vibro

/** تنظیمات ساده: صدا و لرزش */
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var sound by remember { mutableStateOf(Prefs.soundOn) }
    var vibro by remember { mutableStateOf(Prefs.vibrationOn) }
    val context = LocalContext.current

    AppScreen("تنظیمات", "⚙️", onBack) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
        ) {
            Spacer(Modifier.height(6.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔊", fontSize = 24.sp)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("صدا", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "صداهای برخورد و امتیاز",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = sound,
                        onCheckedChange = {
                            sound = it
                            Prefs.soundOn = it
                            SoundManager.enabled = it
                            if (it) SoundManager.play("click")
                        }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📳", fontSize = 24.sp)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("لرزش", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "لرزش کوتاه هنگام امتیاز",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = vibro,
                        onCheckedChange = {
                            vibro = it
                            Prefs.vibrationOn = it
                            Vibro.enabled = it
                            if (it) Vibro.buzz(context, 40)
                        }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Text(
                "تنظیمات بلافاصله ذخیره می‌شود.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
        }
    }
}
