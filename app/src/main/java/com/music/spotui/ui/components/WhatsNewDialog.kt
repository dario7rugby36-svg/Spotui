package com.music.spotui.ui.components

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.spotui.BuildConfig

/**
 * Release notes shown once after an update ("Novità"). Add an entry at the TOP of
 * this list for every new version (same string as versionName in build.gradle.kts).
 */
private data class ReleaseNotes(val version: String, val items: List<String>)

private val RELEASE_NOTES = listOf(
    ReleaseNotes(
        "1.5.8",
        listOf(
            "🛠️ Risolto il crash all'apertura di \"Brani che ti piacciono\"",
            "🛠️ Le playlist con lo stesso brano ripetuto non mandano più in crash l'app",
        ),
    ),
    ReleaseNotes(
        "1.5.7",
        listOf(
            "❤️ \"Mi piace\" direttamente dalla notifica e da Android Auto",
            "🎤 Testi karaoke più precisi: le righe cambiano al momento giusto",
            "⏱️ Nuovo comando \"Sync\" nei testi per spostarli avanti o indietro (utile con il Bluetooth)",
            "🆕 Questa pagina delle novità, che compare una volta dopo ogni aggiornamento",
        ),
    ),
    ReleaseNotes(
        "1.5.6",
        listOf("🎤 Testi in stile karaoke: la riga cantata si colora da sinistra a destra"),
    ),
    ReleaseNotes(
        "1.5.5",
        listOf(
            "✨ Nuovo nome: SplashBeat",
            "🎨 Nuova icona, anche nella schermata di avvio",
        ),
    ),
    ReleaseNotes(
        "1.5.4",
        listOf("🎧 Riproduzione più stabile: niente più pause durante l'ascolto"),
    ),
    ReleaseNotes(
        "1.5.3",
        listOf(
            "🌈 Player in stile Neon (si cambia in Impostazioni → Riproduzione)",
            "📋 Coda sistemata: i brani aggiunti partono subito dopo quello in ascolto",
        ),
    ),
    ReleaseNotes(
        "1.5.2",
        listOf(
            "🚗 Android Auto: ricerca dei brani, comandi vocali e \"Ascoltati di recente\"",
            "😎 Fatto con ♥ da Dario",
        ),
    ),
)

private const val PREFS = "whats_new"
private const val KEY_LAST_SEEN = "last_seen_version"

private fun versionParts(v: String): List<Int> =
    v.split('-', '+').first().split('.').map { it.toIntOrNull() ?: 0 }

/** Positive if [a] is newer than [b]. */
private fun compareVersions(a: String, b: String): Int {
    val x = versionParts(a)
    val y = versionParts(b)
    for (i in 0 until maxOf(x.size, y.size)) {
        val d = x.getOrElse(i) { 0 } - y.getOrElse(i) { 0 }
        if (d != 0) return d
    }
    return 0
}

/**
 * Notes to show now: every version newer than the last one seen, up to the
 * installed one. With nothing recorded yet, only the installed version's notes.
 */
private fun pendingNotes(context: Context): List<ReleaseNotes> {
    val current = BuildConfig.VERSION_NAME
    val lastSeen = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getString(KEY_LAST_SEEN, null)
    return RELEASE_NOTES.filter { notes ->
        compareVersions(notes.version, current) <= 0 &&
            (if (lastSeen == null) compareVersions(notes.version, current) == 0
            else compareVersions(notes.version, lastSeen) > 0)
    }
}

private fun markSeen(context: Context) {
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        .putString(KEY_LAST_SEEN, BuildConfig.VERSION_NAME)
        .apply()
}

/** "Novità" dialog: shown once after each update, then never again for that version. */
@Composable
fun WhatsNewDialog() {
    val context = LocalContext.current
    var notes by remember {
        mutableStateOf(pendingNotes(context).also { if (it.isEmpty()) markSeen(context) })
    }
    if (notes.isEmpty()) return

    val close = {
        markSeen(context)
        notes = emptyList()
    }
    AlertDialog(
        onDismissRequest = close,
        containerColor = Color(0xFF15151C),
        titleContentColor = Color.White,
        title = {
            Column {
                Text("Novità di SplashBeat", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Text(
                    "Versione ${BuildConfig.VERSION_NAME}",
                    color = Color(0xFF00F5D4),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                notes.forEachIndexed { i, release ->
                    if (notes.size > 1) {
                        Text(
                            "Versione ${release.version}",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = if (i == 0) 0.dp else 14.dp, bottom = 6.dp),
                        )
                    }
                    release.items.forEach { item ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(item, color = Color(0xFFD0D0D8), fontSize = 15.sp, lineHeight = 21.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = close) {
                Text("Fantastico!", color = Color(0xFF00F5D4), fontWeight = FontWeight.Bold)
            }
        },
    )
}
