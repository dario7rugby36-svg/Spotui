package com.music.spotui.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.music.spotui.R
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
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import com.music.spotui.data.preferences.BackupPref
import com.music.spotui.util.BackupHelper
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.style.TextOverflow
import com.music.spotui.data.preferences.AudioProviderOrderItem
import com.music.spotui.data.preferences.getAudioProviderOrder
import com.music.spotui.data.preferences.setAudioProviderOrder
import com.music.spotui.data.preferences.isAudioProviderEnabled
import com.music.spotui.data.preferences.setAudioProviderEnabled
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation.NavController
import com.music.spotui.data.BatteryOptimizationHelper
import com.music.spotui.data.preferences.CROSSFADE_MAX_MS
import com.music.spotui.data.preferences.StreamQuality
import com.music.spotui.data.preferences.getCellularQuality
import com.music.spotui.data.preferences.getCrossfadeMs
import com.music.spotui.data.preferences.setCrossfadeMs
import com.music.spotui.data.preferences.getDownloadQuality
import com.music.spotui.data.preferences.isVideoFallbackEnabled
import com.music.spotui.data.preferences.isAutoPlayEnabled
import com.music.spotui.data.preferences.isNeonPlayerEnabled
import com.music.spotui.data.preferences.setNeonPlayerEnabled
import com.music.spotui.data.preferences.getWifiQuality
import com.music.spotui.data.preferences.setCellularQuality
import com.music.spotui.data.preferences.setDownloadQuality
import com.music.spotui.data.preferences.setAutoPlayEnabled
import com.music.spotui.data.preferences.setVideoFallbackEnabled
import com.music.spotui.data.preferences.setWifiQuality
import com.music.spotui.data.preferences.getUpdateRepoUrl
import com.music.spotui.data.preferences.setUpdateRepoUrl
import com.music.spotui.data.preferences.resetUpdateRepoUrl
import com.music.spotui.data.preferences.DEFAULT_UPDATE_REPO_URL
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.music.spotui.ui.components.DefaultAppPrompt
import com.music.spotui.util.DefaultLinkHelper
import com.music.spotui.ui.theme.AppBackground
import com.music.spotui.ui.theme.AppPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current

    var wifiQ by remember { mutableStateOf(getWifiQuality(context)) }
    var cellQ by remember { mutableStateOf(getCellularQuality(context)) }
    var dlQ by remember { mutableStateOf(getDownloadQuality(context)) }
    var crossfadeMs by remember { mutableStateOf(getCrossfadeMs(context).toFloat()) }
    var videoFallback by remember { mutableStateOf(isVideoFallbackEnabled(context)) }
    var autoPlay by remember { mutableStateOf(isAutoPlayEnabled(context)) }
    var neonPlayer by remember { mutableStateOf(isNeonPlayerEnabled(context)) }
    val musicIsland by com.music.spotui.ui.island.MusicIsland.enabled
    var batteryOptExempt by remember { mutableStateOf(BatteryOptimizationHelper.isIgnoringBatteryOptimization(context)) }
    var updateRepoUrl by remember { mutableStateOf(getUpdateRepoUrl(context)) }
    var isDefaultLinkHandler by remember { mutableStateOf(DefaultLinkHelper.isAppDefaultLinkHandler(context)) }
    var showDefaultGuide by remember { mutableStateOf(false) }
    var showProviderStatusDialog by remember { mutableStateOf(false) }
    var providerStatuses by remember { mutableStateOf(emptyList<com.metrolist.spotify.SpotiFlac.ProviderStatus>()) }
    var isRefreshingStatuses by remember { mutableStateOf(false) }
    var providerOrder by remember { mutableStateOf(getAudioProviderOrder(context)) }
    var showProviderOrderDialog by remember { mutableStateOf(false) }

    var backupDirUri by remember { mutableStateOf(BackupPref.getDirectoryUri(context)) }
    var folderName by remember(backupDirUri) { mutableStateOf(BackupHelper.getFolderDisplayName(context, backupDirUri)) }
    var isAutoBackup by remember { mutableStateOf(BackupPref.isAutoBackupEnabled(context)) }
    var isRestoring by remember { mutableStateOf(false) }
    var isBackingUp by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val dirPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            runCatching { context.contentResolver.takePersistableUriPermission(uri, flags) }
            BackupPref.setDirectoryUri(context, uri.toString())
            backupDirUri = uri.toString()
            folderName = BackupHelper.getFolderDisplayName(context, uri.toString())
            scope.launch {
                val autoOk = BackupHelper.performAutoBackup(context)
                val msg = if (autoOk) "Cartella di backup impostata: $folderName (backup automatico creato)" else "Cartella di backup impostata: $folderName"
                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    val restoreFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            isRestoring = true
            scope.launch {
                val (success, message) = BackupHelper.restoreFromFileUri(context, uri)
                isRestoring = false
                android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
                if (success) {
                    wifiQ = getWifiQuality(context)
                    cellQ = getCellularQuality(context)
                    dlQ = getDownloadQuality(context)
                    crossfadeMs = getCrossfadeMs(context).toFloat()
                    videoFallback = isVideoFallbackEnabled(context)
                    autoPlay = isAutoPlayEnabled(context)
                    updateRepoUrl = getUpdateRepoUrl(context)
                    backupDirUri = BackupPref.getDirectoryUri(context)
                    isAutoBackup = BackupPref.isAutoBackupEnabled(context)
                }
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isDefaultLinkHandler = DefaultLinkHelper.isAppDefaultLinkHandler(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val batteryOptLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        batteryOptExempt = BatteryOptimizationHelper.isIgnoringBatteryOptimization(context)
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
        var showDevicesSheet by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                // Clear the bottom nav + mini player so the last section
                // (account / log out) isn't hidden under the bar.
                .padding(bottom = 200.dp)
        ) {
            SectionTitle("Devices & Bluetooth")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { showDevicesSheet = true }
                    .background(Color(0xFF1A1A20))
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Dispositivi di uscita audio", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        com.music.spotui.ui.utils.AudioDeviceHelper.getCurrentAudioRouteName(context),
                        color = Color(0xFF1ED760),
                        fontSize = 12.sp,
                    )
                }
                Icon(
                    painter = painterResource(id = R.drawable.ic_devices),
                    contentDescription = "Dispositivi",
                    tint = Color(0xFF1ED760),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            SectionTitle("Riproduzione in background")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        batteryOptLauncher.launch(BatteryOptimizationHelper.buildAppSettingsIntent(context))
                    }
                    .background(Color(0xFF1A1A20))
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Ottimizzazione batteria", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (batteryOptExempt) "Esclusa — l'app non verrà chiusa" else "Non esclusa — tocca per cambiare",
                        color = if (batteryOptExempt) Color(0xFF81C784) else Color(0xFFB3B3B3),
                        fontSize = 12.sp,
                    )
                }
                if (batteryOptExempt) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Attivo",
                        tint = AppPalette,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            BatteryOptimizationHelper.getManufacturerTips()?.let { (name, tip) ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Consiglio per $name",
                    color = Color(0xFFB3B3B3),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = tip,
                    color = Color(0xFF808080),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1A1A20))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }

            Spacer(Modifier.height(12.dp))
            SectionTitle("Gestione dei link")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        if (isDefaultLinkHandler) {
                            DefaultLinkHelper.openSpotuiDefaultSettings(context)
                        } else {
                            showDefaultGuide = true
                        }
                    }
                    .background(Color(0xFF1A1A20))
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Apri i link di Spotify per impostazione predefinita", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (isDefaultLinkHandler) "SplashBeat apre i link di Spotify per impostazione predefinita" else "Non predefinita — tocca per aprire la guida",
                        color = if (isDefaultLinkHandler) Color(0xFF81C784) else Color(0xFFB3B3B3),
                        fontSize = 12.sp,
                    )
                }
                if (isDefaultLinkHandler) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Attivo",
                        tint = AppPalette,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Apri impostazioni",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            SectionTitle("Qualità audio")
            QualityPicker(
                title = "Streaming su Wi-Fi",
                selected = wifiQ,
                showFlacWarning = wifiQ == StreamQuality.LOSSLESS,
                onDeezerLogin = { navController.navigate(com.music.spotui.ui.navigation.Routes.DeezerLogin.route) }
            ) { wifiQ = it; setWifiQuality(context, it) }

            QualityPicker(
                title = "Streaming su rete mobile",
                selected = cellQ,
                showFlacWarning = cellQ == StreamQuality.LOSSLESS,
                onDeezerLogin = { navController.navigate(com.music.spotui.ui.navigation.Routes.DeezerLogin.route) }
            ) { cellQ = it; setCellularQuality(context, it) }

            QualityPicker(
                title = "Qualità download",
                selected = dlQ,
                showFlacWarning = dlQ == StreamQuality.LOSSLESS,
                onDeezerLogin = { navController.navigate(com.music.spotui.ui.navigation.Routes.DeezerLogin.route) }
            ) { dlQ = it; setDownloadQuality(context, it) }

            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        com.music.spotui.di.SongPlayer.clearCaches(context)
                        android.widget.Toast.makeText(context, "Cache degli stream svuotata", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    .background(Color(0xFF1E1E24))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Svuota cache degli stream audio", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("Elimina tutti gli stream salvati e forza una nuova ricerca", color = Color.Gray, fontSize = 11.sp)
                }
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Svuota cache",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }

            var losslessStatusSummary by remember { mutableStateOf("Controllo dei server lossless…") }
            LaunchedEffect(Unit) {
                providerStatuses = com.metrolist.spotify.SpotiFlac.getProviderStatuses()
                val upCount = providerStatuses.count { it.isUp && !it.isCooldown }
                losslessStatusSummary = "$upCount/${providerStatuses.size} online • Tocca per vedere i fornitori"
            }

            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        scope.launch {
                            isRefreshingStatuses = true
                            providerStatuses = com.metrolist.spotify.SpotiFlac.getProviderStatuses()
                            val upCount = providerStatuses.count { it.isUp && !it.isCooldown }
                            losslessStatusSummary = "$upCount/${providerStatuses.size} online • Tocca per vedere i fornitori"
                            isRefreshingStatuses = false
                            showProviderStatusDialog = true
                        }
                    }
                    .background(Color(0xFF1E1E24))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Stato dei fornitori lossless", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(losslessStatusSummary, color = Color(0xFFB3B3B3), fontSize = 12.sp)
                }
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Vedi stato",
                    tint = AppPalette,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { showProviderOrderDialog = true }
                    .background(Color(0xFF1E1E24))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Priorità dei fornitori audio", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(providerOrder.joinToString(" → ") { it.displayName }, color = Color(0xFFB3B3B3), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Icon(
                    imageVector = Icons.Filled.SwapVert,
                    contentDescription = "Ordine di priorità",
                    tint = AppPalette,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.height(12.dp))
            SectionTitle("Deezer (sorgente preferita)")
            var deezerEnabled by remember { mutableStateOf(com.music.spotui.data.preferences.isDeezerEnabled(context)) }
            val deezerConnected = com.music.spotui.data.preferences.getDeezerArl(context) != null
            val deezerTier = com.music.spotui.data.preferences.getDeezerTier(context)

            SettingsSwitchRow(
                title = "Usa Deezer",
                subtitle = "Riproduci prima da Deezer, poi ripiega su YouTube",
                checked = deezerEnabled,
            ) {
                deezerEnabled = it
                com.music.spotui.data.preferences.setDeezerEnabled(context, it)
                com.music.spotui.di.SongPlayer.deezerEnabled = it
            }
            Text(
                text = if (deezerConnected) {
                    "Collegato" + if (deezerTier.isNotBlank()) " — $deezerTier" else ""
                } else "Non collegato",
                color = Color(0xFFB3B3B3),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp),
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
                text = if (sfConnected) "Verificato — sessione firmata attiva" else "Non verificato — tocca qui sotto per completare il controllo Turnstile",
                color = Color(0xFFB3B3B3),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp),
            )
            Text(
                text = if (sfConnected) "Verifica di nuovo SpotiFLAC" else "Verify SpotiFLAC",
                color = Color(0xFF00C7B7),
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
            SectionTitle("Riproduzione")
            SettingsSwitchRow(
                title = "Riproduzione automatica all'avvio",
                subtitle = "Riprendi l'ultimo brano quando apri l'app",
                checked = autoPlay,
            ) {
                autoPlay = it
                setAutoPlayEnabled(context, it)
            }
            SettingsSwitchRow(
                title = "Stile Neon del player",
                subtitle = "Schermata scura, copertina luminosa e onda sonora animata",
                checked = neonPlayer,
            ) {
                neonPlayer = it
                setNeonPlayerEnabled(context, it)
            }
            SettingsSwitchRow(
                title = "Isola musicale",
                subtitle = if (com.music.spotui.ui.island.NowPlayingChip.supported(context))
                    "Mostra il brano in ascolto accanto all'orologio, nella barra di stato, quando l'app è in background"
                else "Pillola in alto a destra con copertina e onda mentre ascolti con l'app in background. Tocca per aprire, tieni premuto per i comandi",
                checked = musicIsland,
            ) { on ->
                com.music.spotui.ui.island.MusicIsland.setEnabled(context, on)
                // The status-bar chip (Android 16+) needs no overlay permission.
                if (on && !com.music.spotui.ui.island.NowPlayingChip.supported(context) &&
                    !com.music.spotui.ui.island.MusicIsland.canDraw(context)
                ) {
                    // Android asks once for "Mostra sopra altre app".
                    runCatching {
                        context.startActivity(
                            android.content.Intent(
                                android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                android.net.Uri.parse("package:${context.packageName}"),
                            ).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                    android.widget.Toast.makeText(
                        context,
                        "Attiva \"Mostra sopra altre app\" per SplashBeat",
                        android.widget.Toast.LENGTH_LONG,
                    ).show()
                }
            }

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
            SectionTitle("Aggiornamenti")
            Text(
                "Repository per gli aggiornamenti",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "URL del repository GitHub usato per cercare nuove versioni",
                color = Color(0xFFB3B3B3),
                fontSize = 12.sp,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = updateRepoUrl,
                onValueChange = {
                    updateRepoUrl = it
                    setUpdateRepoUrl(context, it)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AppPalette,
                    unfocusedBorderColor = Color(0xFF333333),
                    cursorColor = AppPalette,
                    focusedPlaceholderColor = Color(0xFF666666),
                    unfocusedPlaceholderColor = Color(0xFF666666),
                ),
                placeholder = { Text("https://github.com/Owner/Repo") },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Ripristina predefinito",
                        tint = if (updateRepoUrl != DEFAULT_UPDATE_REPO_URL) AppPalette else Color(0xFF444444),
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clickable {
                                updateRepoUrl = DEFAULT_UPDATE_REPO_URL
                                resetUpdateRepoUrl(context)
                            }
                    )
                },
            )
            Spacer(Modifier.height(12.dp))
            SectionTitle("Backup e ripristino")

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(enabled = !isBackingUp) {
                            if (backupDirUri.isNullOrBlank()) {
                                dirPickerLauncher.launch(null)
                            } else {
                                isBackingUp = true
                                scope.launch {
                                    val (success, message) = BackupHelper.performManualBackup(context)
                                    isBackingUp = false
                                    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                        .background(Color(0xFF1A1A20))
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Esegui backup ora", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            when {
                                isBackingUp -> "Creazione del backup in background…"
                                backupDirUri.isNullOrBlank() -> "Tocca per scegliere la cartella ed eseguire il backup"
                                else -> "Cartella: $folderName"
                            },
                            color = if (backupDirUri.isNullOrBlank()) Color(0xFFFFB74D) else Color(0xFFB3B3B3),
                            fontSize = 12.sp,
                            maxLines = 1,
                        )
                    }
                    if (isBackingUp) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = AppPalette,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = "Esegui backup ora",
                            tint = AppPalette,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                if (!backupDirUri.isNullOrBlank()) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1A1A20))
                            .clickable(enabled = !isBackingUp) { dirPickerLauncher.launch(null) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Folder,
                            contentDescription = "Cambia cartella di backup",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(enabled = !isRestoring) {
                        restoreFileLauncher.launch(arrayOf("application/json", "*/*"))
                    }
                    .background(Color(0xFF1A1A20))
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Ripristina da file", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (isRestoring) "Ripristino del backup in background…" else "Importa playlist e impostazioni da un file di backup di SplashBeat",
                        color = Color(0xFFB3B3B3),
                        fontSize = 12.sp,
                    )
                }
                if (isRestoring) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = AppPalette,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.FolderOpen,
                        contentDescription = "Ripristina",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            SettingsSwitchRow(
                title = "Backup automatico",
                subtitle = if (backupDirUri.isNullOrBlank()) "Salva automaticamente impostazioni e playlist quando apri l'app" else "Backup automatico salvato in $folderName",
                checked = isAutoBackup,
            ) { enabled ->
                if (enabled && backupDirUri.isNullOrBlank()) {
                    dirPickerLauncher.launch(null)
                } else {
                    isAutoBackup = enabled
                    BackupPref.setAutoBackupEnabled(context, enabled)
                    if (enabled) {
                        scope.launch { BackupHelper.performAutoBackup(context) }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            SectionTitle("Risoluzione problemi")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            com.music.spotui.di.SongPlayer.clearCaches(context)
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                android.widget.Toast.makeText(
                                    context,
                                    "Sessione YouTube e cache degli stream reimpostate",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    .background(Color(0xFF1A1A20))
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Reimposta sessione YouTube e anti-bot", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Cancella token di sessione, ID visitatore, generatore PoToken e cache degli stream",
                        color = Color(0xFFB3B3B3),
                        fontSize = 12.sp,
                    )
                }
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Reimposta sessione",
                    tint = AppPalette,
                    modifier = Modifier.size(20.dp)
                )
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
            Spacer(Modifier.height(24.dp))
            val uriHandler = LocalUriHandler.current
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fatto con ♥ da ",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "Dario 😎",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        uriHandler.openUri("https://github.com/dario7rugby36-svg/Spotui")
                    }
                )
            }
            Spacer(Modifier.height(40.dp))
        }

        if (showDefaultGuide) {
            DefaultAppPrompt(
                forceShow = true,
                onDismiss = {
                    showDefaultGuide = false
                    isDefaultLinkHandler = DefaultLinkHelper.isAppDefaultLinkHandler(context)
                }
            )
        }

        if (showDevicesSheet) {
            com.music.spotui.ui.components.DevicesSheet(
                context = context,
                onDismiss = { showDevicesSheet = false }
            )
        }

        if (showProviderStatusDialog) {
            AlertDialog(
                onDismissRequest = { showProviderStatusDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Stato dei fornitori lossless", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        if (isRefreshingStatuses) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = AppPalette)
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Aggiorna",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(22.dp)
                                    .clickable {
                                        scope.launch {
                                            isRefreshingStatuses = true
                                            com.metrolist.spotify.SpotiFlac.clearStatusCache()
                                            providerStatuses = com.metrolist.spotify.SpotiFlac.getProviderStatuses()
                                            isRefreshingStatuses = false
                                        }
                                    }
                            )
                        }
                    }
                },
                text = {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(
                            "Real-time availability of lossless audio mirrors (Tidal, Qobuz, Amazon, Deezer, Monochrome). Playback automatically resolves from the fastest available online provider.",
                            color = Color(0xFFB3B3B3),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        providerStatuses.forEach { status ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1A1A20))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(status.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Text(status.detail, color = Color(0xFF999999), fontSize = 11.sp)
                                }
                                Spacer(Modifier.width(6.dp))
                                val (statusText, statusBg, statusFg) = when {
                                    status.isCooldown -> Triple("In pausa (${status.cooldownRemainingSec}s)", Color(0x33FFB74D), Color(0xFFFFB74D))
                                    status.isUp -> Triple("Online", Color(0x3381C784), Color(0xFF81C784))
                                    else -> Triple("Offline", Color(0x33E57373), Color(0xFFE57373))
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(statusBg)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(statusText, color = statusFg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showProviderStatusDialog = false }) {
                        Text("Chiudi", color = AppPalette)
                    }
                },
                containerColor = Color(0xFF141418),
                titleContentColor = Color.White,
                textContentColor = Color.White,
            )
        }

        if (showProviderOrderDialog) {
            var disabledSet by remember { mutableStateOf(com.music.spotui.data.preferences.getDisabledAudioProviders(context)) }
            AlertDialog(
                onDismissRequest = { showProviderOrderDialog = false },
                title = { Text("Priorità dei fornitori audio", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Seleziona per attivare/disattivare e riordinare i fornitori per streaming e download:", color = Color.Gray, fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                        providerOrder.forEachIndexed { index, item ->
                            val isYoutube = item == AudioProviderOrderItem.YOUTUBE_MUSIC
                            val isEnabled = if (isYoutube) true else item.id !in disabledSet
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isYoutube) {
                                    Spacer(Modifier.width(48.dp))
                                } else {
                                    Checkbox(
                                        checked = isEnabled,
                                        onCheckedChange = { checked ->
                                            setAudioProviderEnabled(context, item.id, checked)
                                            disabledSet = com.music.spotui.data.preferences.getDisabledAudioProviders(context)
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = AppPalette,
                                            uncheckedColor = Color.Gray,
                                            checkmarkColor = Color.Black,
                                        )
                                    )
                                }
                                Text(
                                    text = "${index + 1}. ${item.displayName}" + if (isYoutube) " (sempre come riserva)" else "",
                                    color = if (isEnabled) Color.White else Color.Gray,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                if (index > 0) {
                                    IconButton(onClick = {
                                        val mutable = providerOrder.toMutableList()
                                        val temp = mutable[index]
                                        mutable[index] = mutable[index - 1]
                                        mutable[index - 1] = temp
                                        providerOrder = mutable
                                        setAudioProviderOrder(context, mutable)
                                    }) {
                                        Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Sposta su", tint = AppPalette)
                                    }
                                }
                                if (index < providerOrder.size - 1) {
                                    IconButton(onClick = {
                                        val mutable = providerOrder.toMutableList()
                                        val temp = mutable[index]
                                        mutable[index] = mutable[index + 1]
                                        mutable[index + 1] = temp
                                        providerOrder = mutable
                                        setAudioProviderOrder(context, mutable)
                                    }) {
                                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Sposta giù", tint = AppPalette)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showProviderOrderDialog = false }) {
                        Text("Fatto", color = AppPalette)
                    }
                },
                containerColor = Color(0xFF141418),
                titleContentColor = Color.White,
                textContentColor = Color.White,
            )
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
    showFlacWarning: Boolean = false,
    onDeezerLogin: (() -> Unit)? = null,
    onSelect: (StreamQuality) -> Unit
) {
    val context = LocalContext.current
    val deezerConnected = remember(selected) { com.music.spotui.data.preferences.getDeezerArl(context) != null }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        StreamQuality.values().forEach { q ->
            val isSel = q == selected
            Column {
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
                if (isSel && q == StreamQuality.LOSSLESS && showFlacWarning) {
                    Spacer(Modifier.height(6.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x33FFB74D))
                            .border(1.dp, Color(0x66FFB74D), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "⚠️ Accesso a Deezer consigliato per il lossless",
                            color = Color(0xFFFFB74D),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Deezer è il fornitore più affidabile per riprodurre e scaricare in FLAC lossless. Accedi a Deezer per avere il lossless sul maggior numero di brani.",
                            color = Color(0xFFFFE0B2),
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                        )
                        Spacer(Modifier.height(8.dp))
                        if (deezerConnected) {
                            Text(
                                text = "✓ Account Deezer collegato (lossless pronto)",
                                color = Color(0xFF1ED760),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else if (onDeezerLogin != null) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AppPalette)
                                    .clickable { onDeezerLogin() }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Accedi a Deezer ora",
                                    color = Color.Black,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
        }
    }
}
