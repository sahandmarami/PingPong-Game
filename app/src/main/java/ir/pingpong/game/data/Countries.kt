package ir.pingpong.game.data

import androidx.annotation.DrawableRes
import ir.pingpong.game.R

data class Country(val id: String, val name: String, @DrawableRes val flag: Int)

/** فهرست کشورهای بازی (ایران حتماً حضور دارد) */
object Countries {
    val all = listOf(
        Country("ir", "ایران", R.drawable.flag_ir),
        Country("cn", "چین", R.drawable.flag_cn),
        Country("jp", "ژاپن", R.drawable.flag_jp),
        Country("kr", "کره جنوبی", R.drawable.flag_kr),
        Country("de", "آلمان", R.drawable.flag_de),
        Country("fr", "فرانسه", R.drawable.flag_fr),
        Country("it", "ایتالیا", R.drawable.flag_it),
        Country("es", "اسپانیا", R.drawable.flag_es),
        Country("gb", "انگلیس", R.drawable.flag_gb),
        Country("us", "آمریکا", R.drawable.flag_us),
        Country("br", "برزیل", R.drawable.flag_br),
        Country("tr", "ترکیه", R.drawable.flag_tr)
    )

    private val byId = all.associateBy { it.id }

    fun byIdOrNull(id: String): Country = byId[id] ?: all.first()
}
