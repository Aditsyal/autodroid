package com.aditsyal.autodroid.presentation.theme

import androidx.compose.ui.unit.dp

/**
 * Material Design 3 Elevation System
 * Uses tonal overlays and shadows for surface depth differentiation
 */
object ElevationTokens {
    object Tonal {
        val Level0 = 0.dp
        val Level1 = 1.dp
        val Level2 = 3.dp
        val Level3 = 6.dp
        val Level4 = 8.dp
        val Level5 = 12.dp
    }

    object Shadow {
        val None = 0.dp
        val Small = 2.dp
        val Medium = 4.dp
        val Large = 6.dp
        val ExtraLarge = 8.dp
    }

    object Component {
        val Card = 4.dp
        val FAB = 6.dp
        val TopAppBar = 3.dp
        val Dialog = 6.dp
        val BottomSheet = 6.dp
        val NavigationRail = 1.dp
        val NavigationDrawer = 1.dp
    }
}
