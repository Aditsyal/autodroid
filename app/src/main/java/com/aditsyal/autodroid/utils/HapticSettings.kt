package com.aditsyal.autodroid.utils

import androidx.compose.runtime.staticCompositionLocalOf

data class HapticSettings(val enabled: Boolean)

val LocalHapticSettings = staticCompositionLocalOf { HapticSettings(true) }
