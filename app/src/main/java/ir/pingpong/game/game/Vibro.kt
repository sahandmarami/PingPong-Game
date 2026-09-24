package ir.pingpong.game.game

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/** لرزش کوتاه گوشی با پشتیبانی از نسخه‌های مختلف اندروید */
object Vibro {

    @Volatile
    var enabled = true

    private fun vibrator(context: Context): Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

    fun buzz(context: Context, ms: Long) {
        if (!enabled) return
        val v = vibrator(context) ?: return
        if (v.hasVibrator()) {
            v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    fun pattern(context: Context) {
        if (!enabled) return
        val v = vibrator(context) ?: return
        if (v.hasVibrator()) {
            v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 70, 60, 110), -1))
        }
    }
}
