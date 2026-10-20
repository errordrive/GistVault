package com.gistvault.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.gistvault.app.data.api.ApiClient
import com.gistvault.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class GistRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("gistvault_cache", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CACHE_GISTS = "gv_cache_gists"
        private const val PREFIX_CACHE_GIST = "gv_cache_gist_"
        private const val PREFIX_DRAFT = "gv_draft_"
        private const val MAX_CACHE_SIZE = 512 * 1024
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    fun getCachedGists(): List<Gist>? {
        val raw = prefs.getString(KEY_CACHE_GISTS, null) ?: return null
        return try {
            json.decodeFromString<List<Gist>>(raw)
        } catch (e: Exception) {
            null
        }
    }

    fun saveCachedGists(gists: List<Gist>) {
        try {
            val raw = json.encodeToString(gists)
            prefs.edit().putString(KEY_CACHE_GISTS, raw).apply()
        } catch (_: Exception) {}
    }

    fun getCachedGist(id: String): Gist? {
        val raw = prefs.getString(PREFIX_CACHE_GIST + id, null) ?: return null
        return try {
            json.decodeFromString<Gist>(raw)
        } catch (e: Exception) {
            null
        }
    }

    fun saveCachedGist(id: String, gist: Gist) {
        try {
            val raw = json.encodeToString(gist)
            if (raw.length <= MAX_CACHE_SIZE) {
                prefs.edit().putString(PREFIX_CACHE_GIST + id, raw).apply()
            }
        } catch (_: Exception) {}
    }

    fun getDraft(id: String?): Draft? {
        val key = PREFIX_DRAFT + (id ?: "new")
        val raw = prefs.getString(key, null) ?: return null
        return try {
            json.decodeFromString<Draft>(raw)
        } catch (e: Exception) {
            null
        }
    }

    fun saveDraft(id: String?, draft: Draft) {
        val key = PREFIX_DRAFT + (id ?: "new")
        try {
            val raw = json.encodeToString(draft)
            prefs.edit().putString(key, raw).apply()
        } catch (_: Exception) {}
    }

    fun clearDraft(id: String?) {
        val key = PREFIX_DRAFT + (id ?: "new")
        prefs.edit().remove(key).apply()
    }

    suspend fun fetchGists(): Result<List<Gist>> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.gitHubApi.getGists()
            if (response.isSuccessful && response.body() != null) {
                val gists = response.body()!!
                saveCachedGists(gists)
                Result.success(gists)
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            val cached = getCachedGists()
            if (cached != null) {
                Result.success(cached)
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun fetchGistDetail(id: String): Result<Gist> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.gitHubApi.getGist(id)
            if (response.isSuccessful && response.body() != null) {
                val gist = response.body()!!
                saveCachedGist(id, gist)
                Result.success(gist)
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            val cached = getCachedGist(id)
            if (cached != null) {
                Result.success(cached)
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun createGist(description: String, filename: String, content: String, isPublic: Boolean): Result<Gist> = withContext(Dispatchers.IO) {
        try {
            val req = CreateGistRequest(
                description = description.ifBlank { "Untitled entry" },
                public = isPublic,
                files = mapOf(filename.ifBlank { "config.json" } to GistFileContent(content))
            )
            val response = ApiClient.gitHubApi.createGist(req)
            if (response.isSuccessful && response.body() != null) {
                clearDraft(null)
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateGist(id: String, description: String, filename: String, content: String): Result<Gist> = withContext(Dispatchers.IO) {
        try {
            val req = UpdateGistRequest(
                description = description.ifBlank { "Untitled entry" },
                files = mapOf(filename.ifBlank { "config.json" } to GistFileContent(content))
            )
            val response = ApiClient.gitHubApi.updateGist(id, req)
            if (response.isSuccessful && response.body() != null) {
                clearDraft(id)
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteGist(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.gitHubApi.deleteGist(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
