package com.music.spotui.audio

import android.net.Uri
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.cache.CacheKeyFactory

object LosslessCacheKeyFactory : CacheKeyFactory {
    private const val SPOTIFY_PREFIX = "spotify:track:"

    fun buildCacheKey(spotifyTrackId: String?, url: String): String {
        val uri = Uri.parse(url)
        val variant = variantOf(uri)
        trackPrefix(spotifyTrackId)?.let { return it + variant }

        val host = uri.host.orEmpty()
        val path = uri.path.orEmpty()

        if (host.contains("googlevideo.com") || host.contains("youtube.com")) {
            val videoId = uri.getQueryParameter("docid")
                ?: uri.getQueryParameter("id")
                ?: uri.getQueryParameter("v")
            if (!videoId.isNullOrBlank()) {
                return "spotui-yt:$videoId:$path$variant"
            }
        }

        val urlHash = java.util.zip.CRC32().apply { update(url.toByteArray()) }.value
        return "spotui-raw:$urlHash"
    }

    /**
     * Prefix shared by every cached copy of a track, whatever source or format it
     * came from (used to clear them all); null without a Spotify id.
     */
    fun trackPrefix(spotifyTrackId: String?): String? {
        val cleanId = spotifyTrackId?.removePrefix(SPOTIFY_PREFIX)?.substringBefore('|')?.trim()
        return if (cleanId.isNullOrBlank()) null else "spotui-flac:$cleanId"
    }

    /**
     * Which encoding a stream URL serves. The key must include it: the intro cached
     * by a prefetch from one YouTube client/format (e.g. OPUS) must never be joined
     * with the rest of the file fetched later in another format (e.g. MP4A). That
     * mix left playback stuck at 0:00 until the track's cache was cleared.
     */
    private fun variantOf(uri: Uri): String {
        val host = uri.host.orEmpty()
        if (host.contains("googlevideo.com") || host.contains("youtube.com")) {
            val itag = uri.getQueryParameter("itag").orEmpty()
            val clen = uri.getQueryParameter("clen").orEmpty()
            return ":yt:$itag:$clen"
        }
        return if (host.isNotBlank()) ":$host" else ""
    }

    override fun buildCacheKey(dataSpec: DataSpec): String {
        dataSpec.key?.takeIf { it.isNotBlank() }?.let { return it }
        return buildCacheKey(null, dataSpec.uri.toString())
    }
}
