package ir.pingpong.game.game

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import ir.pingpong.game.R

/** پخش صداهای سبک بازی با SoundPool */
object SoundManager {
    private var pool: SoundPool? = null
    private val sounds = HashMap<String, Int>()

    @Volatile
    var enabled = true

    fun init(context: Context) {
        if (pool != null) return
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        pool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(attrs)
            .build()
        val map = mapOf(
            "hit" to R.raw.hit,
            "bounce" to R.raw.bounce,
            "point_win" to R.raw.point_win,
            "point_lose" to R.raw.point_lose,
            "win" to R.raw.win,
            "lose" to R.raw.lose,
            "click" to R.raw.click
        )
        for ((name, res) in map) {
            sounds[name] = pool!!.load(context, res, 1)
        }
    }

    fun play(name: String, volume: Float = 1f, rate: Float = 1f) {
        if (!enabled) return
        val id = sounds[name] ?: return
        pool?.play(id, volume, volume, 1, 0, rate.coerceIn(0.5f, 2f))
    }
}
