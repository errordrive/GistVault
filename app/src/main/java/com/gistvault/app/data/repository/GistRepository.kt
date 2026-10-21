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
        private const val KEY_STARRED_IDS = "gv_starred_ids"
        private const val MAX_CACHE_SIZE = 512 * 1024
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    fun getStarredIds(): Set<String> {
        return prefs.getStringSet(KEY_STARRED_IDS, emptySet()) ?: emptySet()
    }

    fun saveStarredIds(ids: Set<String>) {
        prefs.edit().putStringSet(KEY_STARRED_IDS, ids).apply()
    }

    fun getCachedGists(): List<Gist>? {
        val raw = prefs.getString(KEY_CACHE_GISTS, null) ?: return null
        return try {
            val list = json.decodeFromString<List<Gist>>(raw)
            val starredIds = getStarredIds()
            list.map { it.copy(isStarred = starredIds.contains(it.id)) }
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
            val gist = json.decodeFromString<Gist>(raw)
            gist.copy(isStarred = getStarredIds().contains(id))
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
            // First attempt to get starred gists silently to keep star statuses in sync
            val starredResult = runCatching { ApiClient.gitHubApi.getStarredGists() }
            val starredIds = if (starredResult.isSuccess && starredResult.getOrNull()?.isSuccessful == true) {
                val set = starredResult.getOrNull()?.body()?.map { it.id }?.toSet() ?: emptySet()
                saveStarredIds(set)
                set
            } else {
                getStarredIds()
            }

            val response = ApiClient.gitHubApi.getGists()
            if (response.isSuccessful && response.body() != null) {
                val rawGists = response.body()!!
                val gists = rawGists.map { gist ->
                    gist.copy(isStarred = starredIds.contains(gist.id))
                }
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

    suspend fun toggleStar(id: String, currentlyStarred: Boolean): Result<Boolean> = withContext(Dispatchers.IO) {
        val currentSet = getStarredIds().toMutableSet()
        val targetStarred = !currentlyStarred
        if (targetStarred) {
            currentSet.add(id)
        } else {
            currentSet.remove(id)
        }
        saveStarredIds(currentSet)

        try {
            val response = if (targetStarred) {
                ApiClient.gitHubApi.starGist(id)
            } else {
                ApiClient.gitHubApi.unstarGist(id)
            }
            if (response.isSuccessful || response.code() == 204) {
                // Update in cached list
                val cached = getCachedGists()
                if (cached != null) {
                    val updated = cached.map {
                        if (it.id == id) it.copy(isStarred = targetStarred) else it
                    }
                    saveCachedGists(updated)
                }
                Result.success(targetStarred)
            } else {
                // Revert on failure
                if (currentlyStarred) currentSet.add(id) else currentSet.remove(id)
                saveStarredIds(currentSet)
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            // Revert on failure
            if (currentlyStarred) currentSet.add(id) else currentSet.remove(id)
            saveStarredIds(currentSet)
            Result.failure(e)
        }
    }

    suspend fun fetchGistDetail(id: String): Result<Gist> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.gitHubApi.getGist(id)
            if (response.isSuccessful && response.body() != null) {
                val isStarred = getStarredIds().contains(id)
                val gist = response.body()!!.copy(isStarred = isStarred)
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

    suspend fun createGist(
        description: String,
        files: Map<String, String>,
        isPublic: Boolean
    ): Result<Gist> = withContext(Dispatchers.IO) {
        try {
            val fileMap = files.filter { it.key.isNotBlank() }.mapValues { GistFileContent(it.value) }
            val finalFiles = if (fileMap.isEmpty()) {
                mapOf("snippet.txt" to GistFileContent(""))
            } else {
                fileMap
            }
            val desc = description.trim().ifBlank {
                finalFiles.keys.firstOrNull() ?: "Gist"
            }
            val req = CreateGistRequest(
                description = desc,
                public = isPublic,
                files = finalFiles
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

    suspend fun createGist(
        description: String,
        filename: String,
        content: String,
        isPublic: Boolean
    ): Result<Gist> = createGist(
        description = description,
        files = mapOf(filename.ifBlank { "snippet.txt" } to content),
        isPublic = isPublic
    )

    suspend fun updateGist(
        id: String,
        description: String,
        files: Map<String, String>
    ): Result<Gist> = withContext(Dispatchers.IO) {
        try {
            val fileMap = files.filter { it.key.isNotBlank() }.mapValues { GistFileContent(it.value) }
            val finalFiles = if (fileMap.isEmpty()) {
                mapOf("snippet.txt" to GistFileContent(""))
            } else {
                fileMap
            }
            val desc = description.trim().ifBlank {
                finalFiles.keys.firstOrNull() ?: "Gist"
            }
            val req = UpdateGistRequest(
                description = desc,
                files = finalFiles
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

    suspend fun updateGist(
        id: String,
        description: String,
        filename: String,
        content: String
    ): Result<Gist> = updateGist(
        id = id,
        description = description,
        files = mapOf(filename.ifBlank { "snippet.txt" } to content)
    )

    suspend fun deleteGist(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.gitHubApi.deleteGist(id)
            if (response.isSuccessful) {
                // Remove from cached gists
                val cached = getCachedGists()
                if (cached != null) {
                    saveCachedGists(cached.filterNot { it.id == id })
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
