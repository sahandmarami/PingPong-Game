package ir.pingpong.game.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.pingpong.game.data.Countries
import ir.pingpong.game.data.Prefs
import ir.pingpong.game.game.SoundManager

/** صفحه انتخاب کشور: ابتدا «کشور من» سپس «کشور ربات» */
@Composable
fun CountryScreen(onBack: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var selP by remember { mutableStateOf(Prefs.playerCountryId) }
    var selB by remember { mutableStateOf(Prefs.botCountryId) }
    val sel = if (step == 0) selP else selB

    AppScreen(
        title = if (step == 0) "کشور من" else "کشور ربات",
        emoji = if (step == 0) "🙋" else "🤖",
        onBack = { if (step == 1) step = 0 else onBack() }
    ) {
        Column(Modifier.fillMaxSize()) {
            Text(
                if (step == 0) "کشورت را انتخاب کن" else "حریفت را انتخاب کن",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(6.dp))

            Box(Modifier.weight(1f)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 8.dp, top = 4.dp)
                ) {
                    items(Countries.all, key = { it.id }) { c ->
                        val isSel = c.id == sel
                        Card(
                            onClick = {
                                if (!isSel) SoundManager.play("click", 0.6f)
                                if (step == 0) selP = c.id else selB = c.id
                            },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSel) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surface
                            ),
                            border = if (isSel) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                FlagIcon(c.flag, 46)
                                Spacer(Modifier.height(7.dp))
                                Text(c.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                if (isSel) {
                                    Text(
                                        "✓",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // نوار پایین: پیش‌نمایش مسابقه و دکمه ادامه
            Card(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (step == 1) {
                        val p = Countries.byIdOrNull(selP)
                        val b = Countries.byIdOrNull(selB)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FlagIcon(p.flag, 30)
                            Spacer(Modifier.width(6.dp))
                            Text(p.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("  🆚  ", fontSize = 15.sp)
                            Text(b.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(6.dp))
                            FlagIcon(b.flag, 30)
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                    PressableButton(
                        text = if (step == 0) "مرحله بعد" else "تأیید و بازگشت",
                        emoji = if (step == 0) "➡️" else "✅",
                        onClick = {
                            if (step == 0) {
                                step = 1
                                SoundManager.play("click")
                            } else {
                                Prefs.playerCountryId = selP
                                Prefs.botCountryId = selB
                                SoundManager.play("point_win", 0.7f)
                                onBack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = sel.isNotBlank()
                    )
                }
            }
        }
    }
}
