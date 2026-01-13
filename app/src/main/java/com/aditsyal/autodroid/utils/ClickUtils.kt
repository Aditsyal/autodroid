package com.aditsyal.autodroid.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@Composable
fun rememberDebouncedClick(
    debounceTime: Long = 500L,
    enableHaptic: Boolean = true,
    onClick: () -> Unit
): () -> Unit {
    var lastClickTime = remember { 0L }
    val haptic = LocalHapticFeedback.current
    val hapticSettings = LocalHapticSettings.current

    return {
        val now = System.currentTimeMillis()
        if (now - lastClickTime > debounceTime) {
            lastClickTime = now
            if (enableHaptic && hapticSettings.enabled) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            onClick()
        }
    }
}

fun Modifier.clickWithHaptic(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val haptic = LocalHapticFeedback.current
    val hapticSettings = LocalHapticSettings.current
    val interactionSource = remember { MutableInteractionSource() }

    clickable(
        interactionSource = interactionSource,
        indication = androidx.compose.foundation.LocalIndication.current,
        enabled = enabled,
        onClick = {
            if (hapticSettings.enabled) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            onClick()
        }
    )
}
