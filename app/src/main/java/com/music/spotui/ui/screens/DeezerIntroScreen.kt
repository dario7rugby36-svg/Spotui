package com.music.spotui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.music.spotui.ui.navigation.Routes

private const val DEEZER_PURPLE = 0xFFA238FF

/**
 * Post-Spotify-login nudge to connect a Deezer account. Deezer is the preferred
 * audio source (see [com.music.spotui.deezer.DeezerSource]); this screen sells
 * why and lets the user log in or skip. Only shown when no Deezer account is
 * connected yet.
 */
@Composable
fun DeezerIntroScreen(navController: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    // Skipping Deezer still advances to the SpotiFLAC step so the onboarding order
    // (Spotify → Deezer → SpotiFLAC) holds; if SpotiFLAC is already set up, go Home.
    val goHome: () -> Unit = {
        if (com.music.spotui.data.preferences.hasSpotiflacSession(context)) {
            navController.navigate(Routes.Home.route) {
                popUpTo(Routes.DeezerIntro.route) { inclusive = true }
            }
        } else {
            navController.navigate("${Routes.SpotiflacVerify.route}?next=home")
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(Color(DEEZER_PURPLE).copy(alpha = 0.15f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text("🎵", fontSize = 44.sp)
            }

            Spacer(Modifier.height(28.dp))
            Text(
                "Accedi a Deezer",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Spotui riproduce la tua musica da Deezer. Se salti questo passaggio, la riproduzione userà YouTube — più lento da caricare, e alcuni brani potrebbero essere censurati o in una versione diversa.",
                color = Color(0xFFB3B3B3),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
            )

            Spacer(Modifier.height(28.dp))
            Benefit("Abbinamenti precisi", "Il brano esatto — versione giusta, durata giusta — non un'ipotesi di YouTube.")
            Benefit("Senza censure", "Nessuna versione censurata o modificata al posto dell'originale.")
            Benefit("Caricamento più rapido", "La riproduzione parte prima — molta meno attesa rispetto a YouTube.")

            Spacer(Modifier.height(36.dp))
            Button(
                onClick = {
                    navController.navigate("${Routes.DeezerLogin.route}?next=home")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(DEEZER_PURPLE),
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text("Accedi a Deezer", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(10.dp))
            TextButton(onClick = goHome, modifier = Modifier.fillMaxWidth()) {
                Text("Salta per ora", color = Color(0xFFB3B3B3), fontSize = 14.sp)
            }
            Text(
                "Puoi collegare Deezer in qualsiasi momento dalle Impostazioni.",
                color = Color(0xFF6A6A6A),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun Benefit(title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = Color(DEEZER_PURPLE),
            modifier = Modifier.size(20.dp).padding(top = 2.dp),
        )
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
            Text(subtitle, color = Color(0xFF9A9A9A), fontSize = 12.sp, lineHeight = 16.sp)
        }
    }
}
