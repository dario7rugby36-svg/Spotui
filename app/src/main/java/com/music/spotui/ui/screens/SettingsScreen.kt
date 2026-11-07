package com.music.spotui.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.music.spotui.data.preferences.CROSSFADE_MAX_MS
import com.music.spotui.data.preferences.StreamQuality
import com.music.spotui.data.preferences.getCellularQuality
import com.music.spotui.data.preferences.getCrossfadeMs
import com.music.spotui.data.preferences.setCrossfadeMs
import com.music.spotui.data.preferences.getDownloadQuality
import com.music.spotui.data.preferences.getLyricTranslateLang
import com.music.spotui.data.preferences.isVideoFallbackEnabled
import com.music.spotui.data.preferences.getWifiQuality
import com.music.spotui.data.preferences.setCellularQuality
import com.music.spotui.data.preferences.setDownloadQuality
import com.music.spotui.data.preferences.setLyricTranslateLang
import com.music.spotui.data.preferences.setVideoFallbackEnabled
import com.music.spotui.data.preferences.setWifiQuality
import com.music.spotui.data.preferences.MusicSource
import com.music.spotui.data.preferences.getPrimaryMusicSource
import com.music.spotui.data.preferences.isYoutubeLoggedIn
import com.music.spotui.data.preferences.setPrimaryMusicSource
import com.music.spotui.ui.theme.AppBackground
import com.music.spotui.ui.theme.AppPalette
import java.util.Locale

private val translateLanguages by lazy {
    Locale.getISOLanguages()
        .map { it to Locale.forLanguageTag(it).getDisplayLanguage(Locale.getDefault()) }
        .filter { it.second.isNotBlank() }
        .sortedBy { it.second }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current

    var wifiQ by remember { mutableStateOf(getWifiQuality(context)) }
    var cellQ by remember { mutableStateOf(getCellularQuality(context)) }
    var dlQ by remember { mutableStateOf(getDownloadQuality(context)) }
    var crossfadeMs by remember { mutableStateOf(getCrossfadeMs(context).toFloat()) }
    var videoFallback by remember { mutableStateOf(isVideoFallbackEnabled(context)) }
    var selectedLanguage by remember { mutableStateOf(getLyricTranslateLang(context)) }
    var languageQuery by remember { mutableStateOf("") }
    // Read fresh each composition so returning from the Deezer login reflects it.
    val deezerConnected = com.music.spotui.data.preferences.getDeezerArl(context) != null
    val deezerTier = com.music.spotui.data.preferences.getDeezerTier(context)
    val youtubeConnected = isYoutubeLoggedIn(context)
    var primarySource by remember {
        mutableStateOf(getPrimaryMusicSource(context) ?: MusicSource.YOUTUBE_MUSIC)
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Impostazioni", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Indietro",
                        tint = Color.White,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(26.dp)
                            .clickable { navController.popBackStack() }
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = AppBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            SectionTitle("Qualità audio")
            QualityPicker(
                title = "Streaming su Wi-Fi",
                selected = wifiQ,
            ) { wifiQ = it; setWifiQuality(context, it) }

            QualityPicker(
                title = "Streaming su rete mobile",
                selected = cellQ,
            ) { cellQ = it; setCellularQuality(context, it) }

            QualityPicker(
                title = "Qualità download",
                selected = dlQ,
            ) { dlQ = it; setDownloadQuality(context, it) }

            // Lossless FLAC comes from the Tidal community backend (no login) and, if
            // connected, Deezer HiFi. Falls back to best-quality YouTube on a miss.
            val losslessNote = if (deezerConnected) {
                "Lossless: Tidal (gratuito) + Deezer${if (deezerTier.isNotBlank()) " $deezerTier" else ""} — FLAC reale"
            } else {
                "Lossless: FLAC Tidal della community (nessun accesso richiesto) — o aggiungi Deezer HiFi qui sotto"
            }
            Text(
                losslessNote,
                color = Color(0xFFB3B3B3),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 6.dp),
            )

            Spacer(Modifier.height(12.dp))
            SectionTitle("Abbinamento")
            SettingsSwitchRow(
                title = "Consenti ripiego sui video",
                subtitle = "Usa i normali video YouTube solo se i risultati di YouTube Music non funzionano",
                checked = videoFallback,
            ) {
                videoFallback = it
                setVideoFallbackEnabled(context, it)
            }

            Spacer(Modifier.height(12.dp))
            SectionTitle("Testi")
            val languageMatches = remember(languageQuery) {
                val query = languageQuery.trim()
                if (query.isBlank()) {
                    emptyList()
                } else {
                    translateLanguages.filter { (code, name) ->
                        code.contains(query, true) || name.contains(query, true)
                    }.sortedWith(
                        compareByDescending<Pair<String, String>> {
                            it.first.equals(query, true) || it.second.equals(query, true)
                        }.thenBy { it.second }
                    ).take(8)
                }
            }
            OutlinedTextField(
                value = languageQuery,
                onValueChange = {
                    languageQuery = it
                    if (it.isBlank()) {
                        selectedLanguage = ""
                        setLyricTranslateLang(context, "")
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                singleLine = true,
                label = { Text("Traduci i testi in", color = Color(0xFFB3B3B3)) },
                placeholder = { Text("Cerca lingue", color = Color(0xFF72728A)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = AppPalette,
                    focusedBorderColor = AppPalette,
                    unfocusedBorderColor = Color(0xFF3A3A46),
                    focusedLabelColor = AppPalette,
                    unfocusedLabelColor = Color(0xFFB3B3B3),
                    focusedContainerColor = AppBackground,
                    unfocusedContainerColor = AppBackground,
                ),
            )
            languageMatches.forEach { (code, name) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1A1A20))
                        .clickable {
                            selectedLanguage = code
                            setLyricTranslateLang(context, code)
                            languageQuery = ""
                        }
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(name, color = Color.White, fontSize = 15.sp, modifier = Modifier.weight(1f))
                    Text(code, color = AppPalette, fontSize = 13.sp)
                }
            }
            Text(
                text = if (selectedLanguage.isBlank()) {
                    "Lingua del telefono"
                } else {
                    "Selezionata: ${translateLanguages.firstOrNull { it.first == selectedLanguage }?.second ?: selectedLanguage} ($selectedLanguage)"
                },
                color = Color(0xFFB3B3B3),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp),
            )

            Spacer(Modifier.height(12.dp))
            SectionTitle("Dissolvenza incrociata")
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dissolvenza incrociata", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text(
                    if (crossfadeMs <= 0f) "Disattivata" else "${(crossfadeMs / 1000f).let { String.format("%.0f", it) }}s",
                    color = if (crossfadeMs <= 0f) Color(0xFFB3B3B3) else AppPalette,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                "Fondi la fine di un brano con l'inizio del successivo",
                color = Color(0xFFB3B3B3),
                fontSize = 13.sp,
            )
            Slider(
                value = crossfadeMs,
                onValueChange = { crossfadeMs = it },
                onValueChangeFinished = { setCrossfadeMs(context, crossfadeMs.toInt()) },
                valueRange = 0f..CROSSFADE_MAX_MS.toFloat(),
                steps = (CROSSFADE_MAX_MS / 1000) - 1, // 1-second stops
                colors = SliderDefaults.colors(
                    thumbColor = AppPalette,
                    activeTrackColor = AppPalette,
                    inactiveTrackColor = Color(0xFF333333),
                ),
            )
            Spacer(Modifier.height(12.dp))
            SectionTitle("Sorgente musicale")
            SourceSettingRow(
                title = "YouTube Music",
                subtitle = if (youtubeConnected) "Collegato — riproduzione di brani espliciti e con limiti di età attiva" else "Accesso richiesto per i brani espliciti e con limiti di età",
                selected = primarySource == MusicSource.YOUTUBE_MUSIC,
            ) {
                if (youtubeConnected) {
                    primarySource = MusicSource.YOUTUBE_MUSIC
                    setPrimaryMusicSource(context, primarySource)
                } else {
                    navController.navigate(com.music.spotui.ui.navigation.Routes.YouTubeLogin.route)
                }
            }
            SourceSettingRow(
                title = "Deezer",
                subtitle = if (deezerConnected) "Collegato${if (deezerTier.isNotBlank()) " — $deezerTier" else ""}" else "Non collegato",
                selected = primarySource == MusicSource.DEEZER,
            ) {
                if (deezerConnected) {
                    primarySource = MusicSource.DEEZER
                    setPrimaryMusicSource(context, primarySource)
                    com.music.spotui.data.preferences.setDeezerEnabled(context, true)
                } else {
                    navController.navigate(com.music.spotui.ui.navigation.Routes.DeezerLogin.route)
                }
            }
            Text(
                text = if (youtubeConnected) "Ricollega YouTube Music" else "Accedi a YouTube Music",
                color = AppPalette,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { navController.navigate(com.music.spotui.ui.navigation.Routes.YouTubeLogin.route) }
                    .padding(vertical = 12.dp),
            )
            Text(
                text = if (deezerConnected) "Ricollega / cambia account" else "Accedi a Deezer",
                color = AppPalette,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        navController.navigate(com.music.spotui.ui.navigation.Routes.DeezerLogin.route)
                    }
                    .padding(vertical = 14.dp),
            )
            if (deezerConnected) {
                Text(
                    text = "Scollega Deezer",
                    color = Color(0xFFE57373),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            com.music.spotui.data.preferences.clearDeezer(context)
                            navController.navigate(com.music.spotui.ui.navigation.Routes.Settings.route) {
                                popUpTo(com.music.spotui.ui.navigation.Routes.Settings.route) { inclusive = true }
                            }
                        }
                        .padding(vertical = 12.dp),
                )
            }

            Spacer(Modifier.height(12.dp))
            SectionTitle("SpotiFLAC (sperimentale)")
            val sfConnected = com.music.spotui.data.preferences.hasSpotiflacSession(context)
            Text(
                text = "Dà accesso ai server FLAC di SpotiFLAC. Risolvi tu stesso un rapido controllo — nessun aggiramento automatico. Sperimentale: potrebbe non funzionare se i loro server cambiano.",
                color = Color(0xFFB3B3B3),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 6.dp),
            )
            Text(
                text = if (sfConnected) "Verifica di nuovo SpotiFLAC" else "Configura la verifica SpotiFLAC",
                color = AppPalette,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        navController.navigate(com.music.spotui.ui.navigation.Routes.SpotiflacVerify.route)
                    }
                    .padding(vertical = 14.dp),
            )
            if (sfConnected) {
                Text(
                    text = "Sessione attiva ✓",
                    color = Color(0xFF00C7B7),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }

            Spacer(Modifier.height(12.dp))
            SectionTitle("Soulseek (sperimentale)")
            var soulseekOn by remember { mutableStateOf(com.music.spotui.data.preferences.isSoulseekEnabled(context)) }
            SettingsSwitchRow(
                title = "Ripiego lossless su Soulseek",
                subtitle = "Se i server FLAC non trovano il brano, scarica i FLAC da Soulseek (peer-to-peer). Consuma dati e mostra il tuo IP agli altri utenti.",
                checked = soulseekOn,
            ) {
                soulseekOn = it
                com.music.spotui.data.preferences.setSoulseekEnabled(context, it)
                // Off: leave the network now and close the listening port.
                if (!it) Thread { com.music.spotui.soulseek.SoulseekClient.disconnect() }.start()
            }

            Spacer(Modifier.height(12.dp))
            SectionTitle("Account")
            Text(
                text = "Esci",
                color = Color(0xFFE57373),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        com.music.spotui.data.api.SpotifySession.setSpDc(context, "")
                        com.music.spotui.data.api.Api.HomeCache.clear()
                        navController.navigate(com.music.spotui.ui.navigation.Routes.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                    .padding(vertical = 14.dp)
            )
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SourceSettingRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .background(if (selected) Color(0xFF1A1A20) else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Color(0xFFB3B3B3), fontSize = 12.sp)
        }
        if (selected) {
            Icon(Icons.Filled.Check, contentDescription = "Selezionato", tint = AppPalette, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = AppPalette,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Color(0xFFB3B3B3), fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AppPalette,
                uncheckedThumbColor = Color(0xFFB3B3B3),
                uncheckedTrackColor = Color(0xFF333333),
            ),
        )
    }
}

@Composable
private fun QualityPicker(
    title: String,
    selected: StreamQuality,
    onSelect: (StreamQuality) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        StreamQuality.entries.forEach { q ->
            val isSel = q == selected
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelect(q) }
                    .background(if (isSel) Color(0xFF1A1A20) else Color.Transparent)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(q.label, color = Color.White, fontSize = 15.sp)
                    Text(q.detail, color = Color(0xFFB3B3B3), fontSize = 12.sp)
                }
                if (isSel) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Selezionato",
                        tint = AppPalette,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
        }
    }
}
