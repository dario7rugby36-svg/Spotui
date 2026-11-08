package com.music.spotui.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

object BatteryOptimizationHelper {

    fun isIgnoringBatteryOptimization(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun buildRequestOptimizationIntent(context: Context): Intent =
        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
        }

    fun buildAppSettingsIntent(context: Context): Intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }

    fun getManufacturerTips(): Pair<String, String>? = when (Build.MANUFACTURER.lowercase()) {
        "xiaomi", "redmi", "poco" -> "Xiaomi / MIUI" to
            "Impostazioni > App > Gestisci app > Spotui > Risparmio batteria > Nessuna restrizione"
        "huawei", "honor" -> "Huawei / EMUI" to
            "Impostazioni > App > App > Spotui > Batteria > Consenti attività in background"
        "samsung" -> "Samsung / One UI" to
            "Impostazioni > App > Spotui > Batteria > Consenti attività in background"
        "oneplus", "realme" -> "OnePlus / ColorOS" to
            "Impostazioni > App > Spotui > Utilizzo batteria > Consenti attività in background"
        "oppo" -> "OPPO / ColorOS" to
            "Impostazioni > App > Spotui > Utilizzo batteria > Consenti attività in background"
        "vivo" -> "Vivo / Funtouch" to
            "Impostazioni > App > Spotui > Batteria > Limitazione in background > Senza restrizioni"
        "sony" -> "Sony / Xperia" to
            "Impostazioni > App > Spotui > Batteria > Ottimizzazione batteria > Non ottimizzare"
        "nothing" -> "Nothing OS" to
            "Impostazioni > App > Spotui > Batteria > Senza restrizioni"
        else -> null
    }
}
