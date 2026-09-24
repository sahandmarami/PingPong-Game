package ir.pingpong.game.game

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/** سطوح سختی بازی */
enum class Difficulty(val title: String, val line1: String, val line2: String) {
    EASY("آسان", "واکنش کندتر ربات و خطای بیشتر", "مناسب برای شروع و یادگیری"),
    MEDIUM("متوسط", "واکنش طبیعی و خطای متعادل", "بازی رقابتی و لذت‌بخش"),
    HARD("سخت", "واکنش سریع و دقت بالا", "رالی‌های طولانی و چالش جدی")
}

enum class Side { PLAYER, BOT }

enum class MatchPhase { SERVE, RALLY, POINT, GAME_OVER }

/** پارامترهای رفتار ربات در هر سطح */
data class BotParams(
    val reaction: Float,   // ثانیه تاخیر واکنش
    val speed: Float,      // حداکثر سرعت حرکت راکت (واحد جهانی بر ثانیه)
    val errX: Float,       // خطای مکانی در حدس محل توپ
    val miss: Float,       // احتمال از دست دادن توپ
    val predict: Float,    // کیفیت پیش‌بینی مسیر (۰ تا ۱)
    val powerMin: Float,   // کمترین قدرت ضربه
    val powerMax: Float,   // بیشترین قدرت ضربه
    val reach: Float,      // شعاع دسترسی راکت ربات
    val aim: Float         // مهارت هدف‌گیری به سمت خالی زمین حریف
)

object BotProfiles {
    val EASY = BotParams(0.46f, 1.55f, 0.42f, 0.24f, 0.25f, 0.12f, 0.42f, 0.28f, 0.15f)
    val MEDIUM = BotParams(0.27f, 2.30f, 0.20f, 0.10f, 0.55f, 0.32f, 0.68f, 0.30f, 0.55f)
    val HARD = BotParams(0.16f, 3.10f, 0.09f, 0.045f, 0.85f, 0.52f, 0.95f, 0.33f, 0.85f)
    val DEMO = BotParams(0.31f, 2.05f, 0.30f, 0.15f, 0.45f, 0.22f, 0.50f, 0.30f, 0.35f)
}

sealed class GameEvent {
    data class Hit(val side: Side) : GameEvent()
    object Bounce : GameEvent()
    data class Point(val winner: Side) : GameEvent()
    data class MatchOver(val winner: Side) : GameEvent()
}

enum class EffectType { HIT, BOUNCE, POINT }

class Effect(val x: Float, val y: Float, val z: Float, val type: EffectType, val life: Float = 0.4f) {
    var t = 0f
}

class TrailPoint(val x: Float, val y: Float, val z: Float)

class Paddle(val side: Side) {
    var x = 0f
    var y = if (side == Side.PLAYER) 1.82f else 0.18f
    var vx = 0f
    // وضعیت هوش مصنوعی
    var planned = false
    var reactT = 0f
    var targetX = 0f
}

class Ball {
    var x = 0f
    var y = 1f
    var z = 0.4f
    var vx = 0f
    var vy = 0f
    var vz = 0f
    var spin = 0f
}

/**
 * موتور اصلی بازی پینگ پنگ.
 * مختصات جهانی: x عرض میز (۱- تا ۱)، y طول میز (۰ سمت ربات تا ۲ سمت بازیکن)، z ارتفاع از میز.
 * این موتور هم برای مسابقه واقعی و هم برای پیش‌نمایش زنده منو (ربات در برابر ربات) استفاده می‌شود.
 */
class PingPongEngine(val demoMode: Boolean = false) {

    companion object {
        const val TABLE_X = 1.0f
        const val NET_Y = 1.0f
        const val TABLE_LEN = 2f
        const val GRAV = 8.6f
        const val NET_H = 0.13f
        const val BALL_R = 0.055f
        const val PADDLE_R = 0.26f
        const val BAND_TOP = 1.95f
        const val BAND_BOTTOM = 1.42f
    }

    val ball = Ball()
    val playerPaddle = Paddle(Side.PLAYER)
    val botPaddle = Paddle(Side.BOT)

    var difficulty = Difficulty.MEDIUM
    var phase = MatchPhase.SERVE
        private set
    var scorePlayer = 0
        private set
    var scoreBot = 0
        private set
    var server = Side.PLAYER
        private set
    var pointWinner = Side.PLAYER
        private set
    var rallyHits = 0
        private set
    var longestRally = 0
        private set
    var speedScale = 1f
        private set

    private val timeScale = if (demoMode) 1.18f else 1f
    private var firstServer = Side.PLAYER
    private var hitCooldown = 0f
    private var serveTimer = 0f
    private var serveStage = 0
    private var pointTimer = 0f
    private var lastHitter: Side? = null
    private var bouncedReceiver = false
    private var lastBounceSide: Side? = null
    private var frame = 0L

    val events = ArrayList<GameEvent>()
    val effects = ArrayList<Effect>()
    val trail = ArrayDeque<TrailPoint>()

    /** ورودی لمسی بازیکن (فقط مسابقه واقعی) */
    var inputX = 0f
    var inputY = 1.80f

    private val rng = Random(System.nanoTime())

    init {
        resetServe()
    }

    private fun botParams(): BotParams = if (demoMode) BotProfiles.DEMO else when (difficulty) {
        Difficulty.EASY -> BotProfiles.EASY
        Difficulty.MEDIUM -> BotProfiles.MEDIUM
        Difficulty.HARD -> BotProfiles.HARD
    }

    fun startMatch() {
        scorePlayer = 0
        scoreBot = 0
        rallyHits = 0
        longestRally = 0
        speedScale = 1f
        firstServer = if (rng.nextBoolean()) Side.PLAYER else Side.BOT
        server = firstServer
        playerPaddle.x = 0f
        playerPaddle.y = 1.82f
        botPaddle.x = 0f
        inputX = 0f
        inputY = 1.80f
        phase = MatchPhase.SERVE
        resetServe()
    }

    private fun resetServe() {
        serveTimer = 0f
        serveStage = 0
        hitCooldown = 0f
        rallyHits = 0
        speedScale = 1f
        lastHitter = null
        bouncedReceiver = false
        lastBounceSide = null
        trail.clear()
        val p = if (server == Side.PLAYER) playerPaddle else botPaddle
        val sign = if (server == Side.PLAYER) -1f else 1f
        ball.x = p.x
        ball.y = p.y + sign * 0.16f
        ball.z = 0.45f
        ball.vx = 0f
        ball.vy = 0f
        ball.vz = 0f
    }

    fun update(dtRaw: Float) {
        frame++
        val dt = (dtRaw * timeScale).coerceIn(0.0005f, 0.05f)
        updateEffects(dt)
        when (phase) {
            MatchPhase.SERVE -> updateServe(dt)
            MatchPhase.RALLY -> updateRally(dt)
            MatchPhase.POINT -> {
                pointTimer -= dt
                integrateBall(dt, scoring = false)
                if (pointTimer <= 0f) nextAfterPoint()
            }
            MatchPhase.GAME_OVER -> Unit
        }
    }

    // ---------- سرویس ----------

    private fun updateServe(dt: Float) {
        serveTimer += dt
        val p = if (server == Side.PLAYER) playerPaddle else botPaddle
        val sign = if (server == Side.PLAYER) -1f else 1f
        ball.x += (p.x * 0.9f - ball.x) * min(1f, dt * 8f)
        ball.y += (p.y + sign * 0.16f - ball.y) * min(1f, dt * 8f)
        ball.z = 0.45f + abs(sin(serveTimer * 5.0f)) * 0.16f
        if (demoMode) aiUpdate(dt) else updatePlayerPaddle(dt)
        if (serveTimer > 0.85f) launchServePhase1()
    }

    private fun launchServePhase1() {
        serveStage = 1
        val dir = if (server == Side.PLAYER) -1f else 1f
        val ownBounceY = if (server == Side.PLAYER) 1.32f else 0.68f
        val tx = (ball.x * 0.6f + rng.nextFloat() * 0.4f - 0.2f).coerceIn(-0.8f, 0.8f)
        val t = 0.42f
        ball.vx = (tx - ball.x) / t
        ball.vy = dir * abs(ownBounceY - ball.y) / t
        ball.vz = (0.5f * GRAV * t * t - ball.z) / t
        lastHitter = server
        bouncedReceiver = false
        lastBounceSide = null
        events.add(GameEvent.Hit(server))
        addEffect(ball.x, ball.y, ball.z, EffectType.HIT)
    }

    /** پس از برخورد سرویس به زمین خودی، قوس دوم به سمت زمین حریف تنظیم می‌شود */
    private fun serveSecondArc() {
        serveStage = 2
        phase = MatchPhase.RALLY
        val t = (0.62f + rng.nextFloat() * 0.18f) / speedScale
        val tx = rng.nextFloat() * 1.2f - 0.6f
        val ty = if (server == Side.PLAYER) 0.35f + rng.nextFloat() * 0.4f else 1.05f + rng.nextFloat() * 0.35f
        setArc(tx, ty, t)
        fixNetClearance(tx, ty, t)
        bouncedReceiver = false
        lastBounceSide = null
    }

    // ---------- رالی ----------

    private fun updateRally(dt: Float) {
        if (demoMode) aiUpdate(dt) else updatePlayerPaddle(dt)
        integrateBall(dt, scoring = true)
        tryHit()
    }

    private fun updatePlayerPaddle(dt: Float) {
        val p = playerPaddle
        val nx = inputX.coerceIn(-1.28f, 1.28f)
        val ny = inputY.coerceIn(BAND_BOTTOM, BAND_TOP)
        val oldX = p.x
        p.x += (nx - p.x) * min(1f, dt * 30f)
        p.y += (ny - p.y) * min(1f, dt * 22f)
        val inst = (p.x - oldX) / max(dt, 1e-4f)
        p.vx = inst * 0.55f + p.vx * 0.45f
    }

    // ---------- هوش مصنوعی ربات ----------

    private fun aiUpdate(dt: Float) {
        val params = botParams()
        aiMove(botPaddle, params, dt)
        if (demoMode) aiMove(playerPaddle, params, dt)
    }

    private fun aiMove(p: Paddle, q: BotParams, dt: Float) {
        val coming = phase == MatchPhase.RALLY && lastHitter != null && lastHitter != p.side
        if (coming) {
            p.reactT -= dt
            if (p.reactT <= 0f && !p.planned) {
                p.planned = true
                val predicted = predictBallX(p.y)
                val blended = ball.x + (predicted - ball.x) * q.predict
                var tx = blended + gauss() * q.errX
                if (rng.nextFloat() < q.miss) {
                    // از دست دادن عمدی توپ با خطای بزرگ
                    tx += (if (rng.nextBoolean()) 1f else -1f) * PADDLE_R * (1.1f + rng.nextFloat() * 0.6f)
                }
                p.targetX = tx.coerceIn(-1.18f, 1.18f)
            }
        } else {
            p.planned = false
            p.reactT = q.reaction * (0.8f + rng.nextFloat() * 0.4f)
            p.targetX += (ball.x * 0.25f - p.targetX) * min(1f, dt * 1.5f)
        }
        val dx = p.targetX - p.x
        val step = q.speed * dt
        val oldX = p.x
        p.x += dx.coerceIn(-step, step)
        val inst = (p.x - oldX) / max(dt, 1e-4f)
        p.vx = inst * 0.5f + p.vx * 0.5f
    }

    /** پیش‌بینی محل رسیدن توپ به عمق مشخص با شبیه‌سازی سبک */
    private fun predictBallX(planeY: Float): Float {
        var x = ball.x
        var y = ball.y
        var z = ball.z
        var vx = ball.vx
        var vy = ball.vy
        var vz = ball.vz
        var t = 0f
        val h = 0.006f
        while (t < 3f) {
            x += vx * h
            y += vy * h
            z += vz * h
            vz -= GRAV * h
            if (z - BALL_R <= 0f && vz < 0f && abs(x) <= TABLE_X && y in 0f..TABLE_LEN) {
                z = BALL_R
                vz = -vz * 0.85f
            }
            if ((vy < 0f && y <= planeY) || (vy > 0f && y >= planeY)) return x
            if (z < -0.5f) return x
            t += h
        }
        return x
    }

    private fun gauss(): Float =
        (rng.nextFloat() + rng.nextFloat() + rng.nextFloat() - 1.5f) * 1.2f

    // ---------- ضربه ----------

    private fun tryHit() {
        val params = botParams()
        val b = ball
        val p = playerPaddle
        if (b.vy > 0f && hitCooldown <= 0f &&
            b.y >= p.y - 0.05f && b.y <= p.y + 0.14f &&
            abs(b.x - p.x) <= PADDLE_R + BALL_R &&
            b.z in -0.05f..1.45f
        ) {
            val power = if (demoMode) {
                params.powerMin + rng.nextFloat() * (params.powerMax - params.powerMin)
            } else {
                (abs(p.vx) / 3.2f).coerceIn(0.18f, 1f)
            }
            performHit(Side.PLAYER, p, power, params)
            return
        }
        val q = botPaddle
        if (b.vy < 0f && hitCooldown <= 0f &&
            b.y <= q.y + 0.05f && b.y >= q.y - 0.14f &&
            abs(b.x - q.x) <= params.reach + BALL_R &&
            b.z in -0.05f..1.45f
        ) {
            val power = params.powerMin + rng.nextFloat() * (params.powerMax - params.powerMin)
            performHit(Side.BOT, q, power, params)
        }
    }

    /**
     * ضربه به توپ:
     * - محل برخورد توپ روی راکت جهت برگشت را تعیین می‌کند (برخورد چپ → برگشت به چپ)
     * - سرعت حرکت راکت روی قدرت و سرعت توپ اثر می‌گذارد
     * - ارتفاع برخورد روی قوس و عمق توپ اثر می‌گذارد
     */
    private fun performHit(side: Side, p: Paddle, power: Float, params: BotParams) {
        val t = (lerp(1.02f, 0.55f, power) / speedScale).coerceIn(0.4f, 1.1f)
        val off = ((ball.x - p.x) / PADDLE_R).coerceIn(-1f, 1f)
        val targetX: Float
        val targetY: Float
        if (side == Side.PLAYER && !demoMode) {
            targetX = (off * 0.85f + p.vx * 0.05f).coerceIn(-1.04f, 1.04f)
            targetY = (lerp(0.86f, 0.38f, power) + rng.nextFloat() * 0.1f).coerceIn(0.3f, 0.95f)
        } else {
            val aim = params.aim
            targetX = (-playerPaddle.x * aim + gauss() * 0.3f * (1f - aim)).coerceIn(-1.02f, 1.02f)
            targetY = if (side == Side.PLAYER) {
                (lerp(0.9f, 0.4f, power) + rng.nextFloat() * 0.12f).coerceIn(0.3f, 0.95f)
            } else {
                (lerp(1.08f, 1.42f, power) + rng.nextFloat() * 0.06f).coerceIn(1.02f, 1.48f)
            }
        }
        setArc(targetX, targetY, t)
        fixNetClearance(targetX, targetY, t)

        rallyHits++
        speedScale = 1f + min(0.38f, rallyHits * 0.022f)
        if (rallyHits > longestRally) longestRally = rallyHits
        lastHitter = side
        bouncedReceiver = false
        lastBounceSide = null
        hitCooldown = 0.22f
        events.add(GameEvent.Hit(side))
        addEffect(ball.x, ball.y, ball.z, EffectType.HIT)
    }

    private fun setArc(tx: Float, ty: Float, t: Float) {
        ball.vx = (tx - ball.x) / t
        ball.vy = (ty - ball.y) / t
        ball.vz = (0.5f * GRAV * t * t - ball.z) / t
    }

    /** اطمینان از رد شدن توپ از بالای تور */
    private fun fixNetClearance(tx: Float, ty: Float, t0: Float) {
        var t = t0
        var tries = 0
        while (zAtNet(t) < NET_H + 0.05f && tries < 4) {
            t *= 1.16f
            setArc(tx, ty, t)
            tries++
        }
    }

    private fun zAtNet(t: Float): Float {
        val vy = ball.vy
        if (abs(vy) < 1e-4f) return 10f
        val tNet = (NET_Y - ball.y) / vy
        if (tNet <= 0f || tNet >= t) return 10f
        return ball.z + ball.vz * tNet - 0.5f * GRAV * tNet * tNet
    }

    // ---------- فیزیک توپ ----------

    private fun integrateBall(dt: Float, scoring: Boolean) {
        val b = ball
        b.x += b.vx * dt
        b.y += b.vy * dt
        b.z += b.vz * dt
        b.vz -= GRAV * dt
        b.spin += b.vx * dt * 6f
        if (b.z - BALL_R <= 0f && b.vz < 0f &&
            abs(b.x) <= TABLE_X + BALL_R * 0.5f &&
            b.y >= -BALL_R * 0.5f && b.y <= TABLE_LEN + BALL_R * 0.5f
        ) {
            b.z = BALL_R
            b.vz = -b.vz * 0.85f
            b.vx *= 0.985f
            b.vy *= 0.99f
            onTableBounce(scoring)
        } else if (scoring && (b.z < -0.55f || b.y < -0.55f || b.y > TABLE_LEN + 0.55f || abs(b.x) > 2.4f)) {
            val hitter = lastHitter
            if (hitter == null) {
                phase = MatchPhase.SERVE
                resetServe()
            } else if (bouncedReceiver) {
                awardPoint(hitter)
            } else {
                awardPoint(other(hitter))
            }
        }
    }

    private fun onTableBounce(scoring: Boolean) {
        addEffect(ball.x, ball.y, 0f, EffectType.BOUNCE)
        events.add(GameEvent.Bounce)
        if (!scoring) return
        val side = if (ball.y > NET_Y) Side.PLAYER else Side.BOT
        if (serveStage == 1 && lastHitter == server && side == server) {
            serveSecondArc()
            return
        }
        val hitter = lastHitter ?: return
        if (side == hitter) {
            // برگشت توپ به زمین خودی در رالی = خطا
            awardPoint(other(hitter))
        } else if (lastBounceSide == side) {
            // دو بار فرود آمدن توپ در زمین دریافت‌کننده = امتیاز زننده
            awardPoint(hitter)
        } else {
            bouncedReceiver = true
            lastBounceSide = side
        }
    }

    // ---------- امتیاز و قوانین ----------

    private fun awardPoint(winner: Side) {
        if (phase == MatchPhase.GAME_OVER) return
        if (winner == Side.PLAYER) scorePlayer++ else scoreBot++
        pointWinner = winner
        events.add(GameEvent.Point(winner))
        addEffect(ball.x, ball.y, max(ball.z, 0f), EffectType.POINT)
        if (demoMode) {
            server = other(server)
            phase = MatchPhase.SERVE
            resetServe()
            return
        }
        pointTimer = 1.5f
        phase = MatchPhase.POINT
        // چرخش استاندارد سرویس: هر ۲ امتیاز، و در تساوی بالای ۱۰ هر ۱ امتیاز
        val total = scorePlayer + scoreBot
        server = if (scorePlayer >= 10 && scoreBot >= 10) {
            if (total % 2 == 0) firstServer else other(firstServer)
        } else {
            if ((total / 2) % 2 == 0) firstServer else other(firstServer)
        }
    }

    private fun nextAfterPoint() {
        val done = (scorePlayer >= 11 || scoreBot >= 11) && abs(scorePlayer - scoreBot) >= 2
        if (done) {
            phase = MatchPhase.GAME_OVER
            events.add(GameEvent.MatchOver(if (scorePlayer > scoreBot) Side.PLAYER else Side.BOT))
        } else {
            phase = MatchPhase.SERVE
            resetServe()
        }
    }

    // ---------- کمکی ----------

    private fun addEffect(x: Float, y: Float, z: Float, type: EffectType) {
        if (effects.size > 24) effects.clear()
        effects.add(Effect(x, y, z, type))
    }

    private fun updateEffects(dt: Float) {
        val it = effects.iterator()
        while (it.hasNext()) {
            val e = it.next()
            e.t += dt
            if (e.t > e.life) it.remove()
        }
        if (phase == MatchPhase.RALLY && frame % 2 == 0L) {
            trail.addLast(TrailPoint(ball.x, ball.y, ball.z))
            while (trail.size > 12) trail.removeFirst()
        }
    }

    private fun other(s: Side) = if (s == Side.PLAYER) Side.BOT else Side.PLAYER

    private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
}
