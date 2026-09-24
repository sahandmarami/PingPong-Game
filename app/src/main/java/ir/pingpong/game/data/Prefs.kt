package ir.pingpong.game.data

import android.content.Context
import android.content.SharedPreferences
import ir.pingpong.game.game.Difficulty

/** ذخیره‌سازی محلی تنظیمات و رکوردها روی گوشی (بدون سرور) */
object Prefs {
    private lateinit var sp: SharedPreferences

    fun init(context: Context) {
        if (!::sp.isInitialized) {
            sp = context.getSharedPreferences("ping_pong_prefs", Context.MODE_PRIVATE)
        }
    }

    var soundOn: Boolean
        get() = sp.getBoolean("sound_on", true)
        set(v) = sp.edit().putBoolean("sound_on", v).apply()

    var vibrationOn: Boolean
        get() = sp.getBoolean("vib_on", true)
        set(v) = sp.edit().putBoolean("vib_on", v).apply()

    var playerCountryId: String
        get() = sp.getString("pc", "ir") ?: "ir"
        set(v) = sp.edit().putString("pc", v).apply()

    var botCountryId: String
        get() = sp.getString("bc", "cn") ?: "cn"
        set(v) = sp.edit().putString("bc", v).apply()

    var difficulty: Difficulty
        get() = try {
            Difficulty.valueOf(sp.getString("diff", "MEDIUM") ?: "MEDIUM")
        } catch (e: Exception) {
            Difficulty.MEDIUM
        }
        set(v) = sp.edit().putString("diff", v.name).apply()

    // رکوردهای محلی
    val wins: Int get() = sp.getInt("wins", 0)
    val losses: Int get() = sp.getInt("losses", 0)
    val bestOpp: Int get() = sp.getInt("best_opp", -1)
    val longestRally: Int get() = sp.getInt("longest_rally", 0)

    fun recordMatch(won: Boolean, oppScore: Int, rally: Int) {
        val e = sp.edit()
        if (won) e.putInt("wins", wins + 1) else e.putInt("losses", losses + 1)
        if (won && (bestOpp < 0 || oppScore < bestOpp)) e.putInt("best_opp", oppScore)
        if (rally > longestRally) e.putInt("longest_rally", rally)
        e.apply()
    }

    fun resetRecords() {
        sp.edit()
            .putInt("wins", 0)
            .putInt("losses", 0)
            .putInt("best_opp", -1)
            .putInt("longest_rally", 0)
            .apply()
    }
}
