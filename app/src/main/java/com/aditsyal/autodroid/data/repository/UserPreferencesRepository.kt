package com.aditsyal.autodroid.data.repository

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val _amoledMode = MutableStateFlow(prefs.getBoolean(KEY_AMOLED_MODE, false))
    val amoledMode: StateFlow<Boolean> = _amoledMode.asStateFlow()

    private val _hapticFeedbackEnabled = MutableStateFlow(prefs.getBoolean(KEY_HAPTIC_FEEDBACK, true))
    val hapticFeedbackEnabled: StateFlow<Boolean> = _hapticFeedbackEnabled.asStateFlow()

    private val _sidebarEnabled = MutableStateFlow(prefs.getBoolean(KEY_SIDEBAR_ENABLED, false))
    val sidebarEnabled: StateFlow<Boolean> = _sidebarEnabled.asStateFlow()

    fun setAmoledMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AMOLED_MODE, enabled).apply()
        _amoledMode.value = enabled
    }

    fun setHapticFeedbackEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC_FEEDBACK, enabled).apply()
        _hapticFeedbackEnabled.value = enabled
    }

    fun setSidebarEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SIDEBAR_ENABLED, enabled).apply()
        _sidebarEnabled.value = enabled
    }

    companion object {
        private const val KEY_AMOLED_MODE = "amoled_mode"
        private const val KEY_HAPTIC_FEEDBACK = "haptic_feedback"
        private const val KEY_SIDEBAR_ENABLED = "sidebar_enabled"
    }
}
