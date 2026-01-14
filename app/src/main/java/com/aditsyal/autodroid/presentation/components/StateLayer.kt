package com.aditsyal.autodroid.presentation.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

object StateLayers {
    const val HoverOpacity = 0.08f
    const val FocusOpacity = 0.12f
    const val PressOpacity = 0.12f
    const val DragOpacity = 0.16f

    @Composable
    fun getOverlayColor(
        interactionSource: MutableInteractionSource,
        baseColor: Color = LocalContentColor.current
    ): Color {
        val isPressed by interactionSource.collectIsPressedAsState()
        val isHovered by interactionSource.collectIsHoveredAsState()
        val isFocused by interactionSource.collectIsFocusedAsState()

        return when {
            isPressed -> baseColor.copy(alpha = PressOpacity)
            isFocused -> baseColor.copy(alpha = FocusOpacity)
            isHovered -> baseColor.copy(alpha = HoverOpacity)
            else -> Color.Transparent
        }
    }
}
