package com.gistvault.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.gistvault.app.data.api.ApiClient
import com.gistvault.app.data.model.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("gistvault_settings", Context.MODE_PRIVATE)

    companion object {
        const val CURRENT_APP_VERSION = "3.2"
        private const val KEY_THEME = "safestore_theme"
        private const val KEY_UPDATE_DISMISSED = "gv_update_dismissed"
        private const val KEY_NOTICE_DISMISSED = "gv_notice_dismissed"
    }

    private val _themeState = MutableStateFlow(getTheme())
    val themeState: StateFlow<String> = _themeState

    fun getTheme(): String {
        return prefs.getString(KEY_THEME, "system") ?: "system"
    }

    fun setTheme(theme: String) {
        prefs.edit().putString(KEY_THEME, theme).apply()
        _themeState.value = theme
    }

    fun isUpdateDismissed(version: String?): Boolean {
        if (version == null) return false
        val dismissed = prefs.getString(KEY_UPDATE_DISMISSED, null)
        return dismissed == version
    }

    fun dismissUpdate(version: String?) {
        if (version != null) {
            prefs.edit().putString(KEY_UPDATE_DISMISSED, version).apply()
        }
    }

    fun isNoticeDismissed(key: String): Boolean {
        val dismissed = prefs.getString(KEY_NOTICE_DISMISSED, null)
        return dismissed == key
    }

    fun dismissNotice(key: String) {
        prefs.edit().putString(KEY_NOTICE_DISMISSED, key).apply()
    }

    // Version compare: se(v1, v2)
    fun compareVersions(v1: String, v2: String): Int {
        val p1 = v1.split(".").mapNotNull { it.toIntOrNull() }
        val p2 = v2.split(".").mapNotNull { it.toIntOrNull() }
        val maxLen = maxOf(p1.size, p2.size)
        for (i in 0 until maxLen) {
            val num1 = p1.getOrElse(i) { 0 }
            val num2 = p2.getOrElse(i) { 0 }
            if (num1 > num2) return 1
            if (num1 < num2) return -1
        }
        return 0
    }

    suspend fun fetchRemoteConfig(): AppConfig? = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.configApi.getConfig()
            if (response.isSuccessful) response.body() else null
        } catch (_: Exception) {
            null
        }
    }
}
