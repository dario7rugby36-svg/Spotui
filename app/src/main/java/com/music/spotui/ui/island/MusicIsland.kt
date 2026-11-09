package com.music.spotui.ui.island

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.compose.runtime.mutableStateOf
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.music.spotui.MainActivity
import com.music.spotui.R
import com.music.spotui.di.SongPlayer
import kotlin.math.sin

/** Whether the app UI is on screen (set by MainActivity's onStart/onStop). */
object AppVisibility {
    val inForeground = mutableStateOf(false)
}

/**
 * "Isola musicale": an iPhone-style floating pill shown at the top-right (away from
 * the camera cut-out) while music plays and the app is in the background. It shows
 * the cover and an animated wave; tap opens the app, long-press shows play/pause
 * and next. Drawn over other apps, so it needs the "display over other apps"
 * permission; enabled from Settings → Riproduzione.
 */
object MusicIsland {
    private const val TAG = "MusicIsland"

    private var root: LinearLayout? = null
    private var cover: ImageView? = null
    private var wave: WaveView? = null
    private var controls: LinearLayout? = null
    private var playPause: ImageView? = null
    private var currentCover: String? = null
    private var expanded = false

    /** Mirrors the Settings switch so the service reacts to it right away. */
    val enabled = mutableStateOf(
        com.music.spotui.data.preferences.isMusicIslandEnabled(com.music.spotui.MyApplication.instance)
    )

    fun setEnabled(context: Context, value: Boolean) {
        enabled.value = value
        com.music.spotui.data.preferences.setMusicIslandEnabled(context, value)
        if (!value) hide(context)
    }

    val isShowing: Boolean get() = root != null

    fun canDraw(context: Context): Boolean = Settings.canDrawOverlays(context)

    private var lastRequest: Triple<Boolean, String, Boolean>? = null
    private var watchingRotation = false

    /** Shows/updates the pill, or removes it when [show] is false. Main thread only. */
    fun render(context: Context, show: Boolean, coverUri: String, playing: Boolean) {
        val app = context.applicationContext
        lastRequest = Triple(show, coverUri, playing)
        watchRotation(app)
        // Landscape = full-screen video or games: stay out of the way.
        val landscape = app.resources.configuration.orientation ==
            android.content.res.Configuration.ORIENTATION_LANDSCAPE
        if (!show || landscape || !canDraw(app)) {
            detach(app)
            return
        }
        if (root == null) attach(app)
        if (coverUri != currentCover) {
            currentCover = coverUri
            cover?.let { iv ->
                Glide.with(app).load(coverUri)
                    .transform(CenterCrop(), RoundedCorners(dp(app, 8)))
                    .placeholder(R.drawable.placeholder)
                    .into(iv)
            }
        }
        wave?.setPlaying(playing)
        playPause?.setImageResource(if (playing) R.drawable.ic_playing else R.drawable.play_svgrepo_com)
    }

    /** Re-applies the last request when the screen rotates (hide in landscape, back in portrait). */
    private fun watchRotation(app: Context) {
        if (watchingRotation) return
        watchingRotation = true
        app.registerComponentCallbacks(object : android.content.ComponentCallbacks {
            override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
                val (show, cover, playing) = lastRequest ?: return
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    render(app, show, cover, playing)
                }
            }
            @Deprecated("Deprecated in Java")
            override fun onLowMemory() = Unit
        })
    }

    /** Removes the pill and forgets it, so a later rotation doesn't bring it back. */
    fun hide(context: Context) {
        lastRequest = null
        detach(context)
    }

    private fun detach(context: Context) {
        val view = root ?: return
        runCatching {
            (context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
                .removeView(view)
        }.onFailure { Log.w(TAG, "remove failed: ${it.message}") }
        wave?.setPlaying(false)
        root = null
        cover = null
        wave = null
        controls = null
        playPause = null
        currentCover = null
        expanded = false
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun attach(app: Context) {
        val wm = app.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val pill = LinearLayout(app).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = GradientDrawable().apply {
                setColor(0xF0000000.toInt())
                cornerRadius = dp(app, 22).toFloat()
                setStroke(dp(app, 1), 0x33FFFFFF)
            }
            setPadding(dp(app, 6), dp(app, 6), dp(app, 10), dp(app, 6))
            elevation = dp(app, 6).toFloat()
            contentDescription = "SplashBeat: apri l'app"
        }
        val coverView = ImageView(app).apply {
            layoutParams = LinearLayout.LayoutParams(dp(app, 30), dp(app, 30))
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        val waveView = WaveView(app).apply {
            layoutParams = LinearLayout.LayoutParams(dp(app, 26), dp(app, 18)).apply {
                marginStart = dp(app, 10)
            }
        }
        val controlsRow = LinearLayout(app).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            visibility = View.GONE
        }
        val playBtn = controlButton(app, R.drawable.ic_playing, "Riproduci o metti in pausa") {
            SongPlayer.togglePlay()
        }
        val nextBtn = controlButton(app, R.drawable.ic_player_skip, "Brano successivo") {
            SongPlayer.next(app)
        }
        controlsRow.addView(playBtn)
        controlsRow.addView(nextBtn)

        pill.addView(coverView)
        pill.addView(waveView)
        pill.addView(controlsRow)

        pill.setOnClickListener {
            if (expanded) {
                setExpanded(false)
            } else {
                app.startActivity(
                    Intent(app, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                )
            }
        }
        pill.setOnLongClickListener {
            setExpanded(!expanded)
            true
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        ).apply {
            // Top-right, just below the status bar: clear of a centred camera hole.
            gravity = Gravity.TOP or Gravity.END
            x = dp(app, 12)
            y = statusBarHeight(app) + dp(app, 6)
        }
        runCatching { wm.addView(pill, params) }
            .onSuccess {
                root = pill
                cover = coverView
                wave = waveView
                controls = controlsRow
                playPause = playBtn
            }
            .onFailure { Log.w(TAG, "add failed: ${it.message}") }
    }

    private fun setExpanded(value: Boolean) {
        expanded = value
        controls?.visibility = if (value) View.VISIBLE else View.GONE
    }

    private fun controlButton(app: Context, icon: Int, description: String, onClick: () -> Unit) =
        ImageView(app).apply {
            layoutParams = LinearLayout.LayoutParams(dp(app, 32), dp(app, 32)).apply {
                marginStart = dp(app, 8)
            }
            setPadding(dp(app, 6), dp(app, 6), dp(app, 6), dp(app, 6))
            setImageResource(icon)
            setColorFilter(0xFFFFFFFF.toInt())
            contentDescription = description
            setOnClickListener { onClick() }
        }

    private fun statusBarHeight(context: Context): Int {
        @SuppressLint("InternalInsetResource", "DiscouragedApi")
        val id = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (id > 0) context.resources.getDimensionPixelSize(id) else dp(context, 24)
    }

    private fun dp(context: Context, v: Int): Int =
        (v * context.resources.displayMetrics.density).toInt()
}

/** Four animated bars, like the wave in the iPhone pill. Calm and still when paused. */
private class WaveView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private var phase = 0f
    private var playing = false
    private val animator = ValueAnimator.ofFloat(0f, (2 * Math.PI).toFloat()).apply {
        duration = 1200
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            phase = it.animatedValue as Float
            invalidate()
        }
    }

    fun setPlaying(value: Boolean) {
        if (value == playing) return
        playing = value
        if (value) animator.start() else animator.cancel()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        paint.shader = LinearGradient(
            0f, 0f, 0f, h.toFloat(),
            0xFFF72585.toInt(), 0xFF00F5D4.toInt(), Shader.TileMode.CLAMP,
        )
    }

    override fun onDetachedFromWindow() {
        animator.cancel()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        val bars = 4
        val slot = width / bars.toFloat()
        val barW = slot * 0.5f
        for (i in 0 until bars) {
            val level = if (playing) {
                // Whole-number speed multipliers keep the loop seamless at 2π.
                0.3f + 0.7f * (sin(phase * (1 + i % 2) + i * 1.7f) * 0.5f + 0.5f)
            } else 0.25f
            val h = height * level
            val left = slot * i + (slot - barW) / 2
            rect.set(left, (height - h) / 2, left + barW, (height + h) / 2)
            canvas.drawRoundRect(rect, barW / 2, barW / 2, paint)
        }
    }
}
