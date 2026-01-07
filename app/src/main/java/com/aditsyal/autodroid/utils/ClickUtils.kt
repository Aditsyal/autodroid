package com.aditsyal.autodroid.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun rememberDebouncedClick(
    debounceTime: Long = 500L,
    onClick: () -> Unit
): () -> Unit {
    var lastClickTime = remember { 0L }
    return {
        val now = System.currentTimeMillis()
        if (now - lastClickTime > debounceTime) {
            lastClickTime = now
            onClick()
        }
    }
}
