package com.aditsyal.autodroid.presentation.theme

import androidx.compose.ui.unit.dp

/**
 * Material Design 3 Spacing System
 * Based on 4dp base unit system for consistent spacing across all components
 */
object SpacingTokens {
    val Zero = 0.dp
    val XS = 4.dp
    val SM = 8.dp
    val MD = 12.dp
    val LG = 16.dp
    val XL = 20.dp
    val TwoXL = 24.dp
    val ThreeXL = 32.dp
    val FourXL = 48.dp
    val FiveXL = 64.dp

    object ScreenPadding {
        val Compact = 16.dp
        val Medium = 24.dp
        val Expanded = 32.dp
    }

    object Component {
        val Inner = 12.dp
        val Between = 8.dp
        val Section = 24.dp
    }
}
