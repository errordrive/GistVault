package com.gistvault.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.gistvault.app.data.api.ApiClient
import com.gistvault.app.data.model.DeviceCodeResponse
import com.gistvault.app.data.model.GitHubUser
import com.gistvault.app.data.model.OAuthTokenResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AuthRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("gistvault_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "gistvault_gh_token"
        private const val KEY_CLIENT_ID = "safestore_client_id"
        private const val KEY_DEVICE_ID = "gistvault_device_id"
    }

    private val _tokenState = MutableStateFlow(getToken())
    val tokenState: StateFlow<String?> = _tokenState

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)?.takeIf { it.isNotBlank() }
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
        ApiClient.setToken(token)
        _tokenState.value = token
    }

    fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply()
        ApiClient.setToken(null)
        _tokenState.value = null
    }

    fun getClientId(): String {
        val custom = prefs.getString(KEY_CLIENT_ID, null)?.takeIf { it.isNotBlank() }
        return custom ?: ApiClient.BUILTIN_CLIENT_ID
    }

    fun saveCustomClientId(clientId: String) {
        prefs.edit().putString(KEY_CLIENT_ID, clientId).apply()
    }

    fun removeCustomClientId() {
        prefs.edit().remove(KEY_CLIENT_ID).apply()
    }

    fun getDeviceId(): String {
        var id = prefs.getString(KEY_DEVICE_ID, null)
        if (id == null) {
            id = "dev-" + System.currentTimeMillis().toString(36) + "-" + (100000..999999).random().toString(36)
            prefs.edit().putString(KEY_DEVICE_ID, id).apply()
        }
        return id
    }

    suspend fun requestDeviceCode(): Result<DeviceCodeResponse> = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("client_id", getClientId())
                put("scope", "gist")
            }.toString().toRequestBody("application/json".toMediaType())

            val response = ApiClient.gitHubAuthApi.requestDeviceCode(jsonBody)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to request code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun pollAccessToken(deviceCode: String): Result<OAuthTokenResponse> = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("client_id", getClientId())
                put("device_code", deviceCode)
                put("grant_type", "urn:ietf:params:oauth:grant-type:device_code")
            }.toString().toRequestBody("application/json".toMediaType())

            val response = ApiClient.gitHubAuthApi.pollAccessToken(jsonBody)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Polling error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyCurrentUser(): Result<Pair<GitHubUser, Boolean>> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.gitHubApi.getCurrentUser()
            if (response.isSuccessful && response.body() != null) {
                val hasScope = ApiClient.hasGistScope(ApiClient.lastOAuthScopes)
                Result.success(Pair(response.body()!!, hasScope))
            } else {
                Result.failure(Exception("Failed to verify user: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
