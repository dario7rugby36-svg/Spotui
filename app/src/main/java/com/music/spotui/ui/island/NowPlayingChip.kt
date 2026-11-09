package com.music.spotui.ui.island

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.music.spotui.MainActivity
import com.music.spotui.R

/**
 * Status-bar chip with the playing song ("Live Updates", Android 16+), like the
 * official Spotify app shows next to the clock: no overlay over other apps and no
 * extra permission prompt. Android only promotes the notification to a chip on
 * devices that support Live Updates; elsewhere [MusicIsland]'s floating pill is used.
 */
object NowPlayingChip {
    private const val TAG = "NowPlayingChip"
    private const val CHANNEL_ID = "now_playing_chip"
    private const val NOTIFICATION_ID = 4242

    private var shownKey: String? = null

    /** True when this device can show a promoted (status-bar chip) notification. */
    fun supported(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < 36) return false
        val nm = context.getSystemService(NotificationManager::class.java) ?: return false
        return runCatching { nm.canPostPromotedNotifications() }.getOrDefault(false)
    }

    /** Shows/updates the chip, or removes it when [show] is false. */
    fun update(context: Context, show: Boolean, title: String, artist: String) {
        val app = context.applicationContext
        val nm = app.getSystemService(NotificationManager::class.java) ?: return
        if (!show || title.isBlank() || Build.VERSION.SDK_INT < 36) {
            if (shownKey != null) runCatching { nm.cancel(NOTIFICATION_ID) }
            shownKey = null
            return
        }
        val key = "$title|$artist"
        if (key == shownKey) return
        ensureChannel(nm)
        val open = PendingIntent.getActivity(
            app, 42,
            Intent(app, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        // Live Updates rules: ongoing, a title, standard style, not colorized,
        // not a group summary, channel above "min" importance.
        val notification = Notification.Builder(app, CHANNEL_ID)
            .setSmallIcon(R.drawable.media3_notification_small_icon)
            .setContentTitle(title)
            .setContentText(artist)
            .setContentIntent(open)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setCategory(Notification.CATEGORY_TRANSPORT)
            .setShortCriticalText(title)
            .setRequestPromotedOngoing(true)
            .build()
        runCatching { nm.notify(NOTIFICATION_ID, notification) }
            .onSuccess { shownKey = key }
            .onFailure { Log.w(TAG, "notify failed: ${it.message}") }
    }

    private fun ensureChannel(nm: NotificationManager) {
        if (nm.getNotificationChannel(CHANNEL_ID) != null) return
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Brano in riproduzione (barra di stato)", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Mostra il brano in ascolto come chip accanto all'orologio"
                setSound(null, null)
                enableVibration(false)
                setShowBadge(false)
            }
        )
    }
}
