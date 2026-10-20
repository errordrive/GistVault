package com.gistvault.app.data.api

import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // Built-in Client ID decoded from the original XOR array:
    // e = [90, 63, 28, 119, 43], t = [21, 73, 46, 68, 71, 51, 104, 88, 51, 108, 10, 118, 36, 18, 99, 47, 79, 101, 49, 72]
    const val BUILTIN_CLIENT_ID = "Ov23liWDDGPI8eHupyFc"
    const val GITHUB_API_BASE_URL = "https://api.github.com/"
    const val GITHUB_AUTH_BASE_URL = "https://github.com/"
    const val CONFIG_API_BASE_URL = "https://gistvault-api.nctti.tech/"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        coerceInputValues = true
    }

    private var currentToken: String? = null
    var lastRateLimitReset: String? = null
    var lastOAuthScopes: String? = null

    fun setToken(token: String?) {
        currentToken = token
    }

    fun hasGistScope(scopesHeader: String?): Boolean {
        if (scopesHeader == null) return false
        return scopesHeader.split(",").map { it.trim() }.contains("gist")
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            .header("Accept", "application/vnd.github+json")

        currentToken?.takeIf { it.isNotBlank() }?.let {
            requestBuilder.header("Authorization", "Bearer $it")
        }

        val response = chain.proceed(requestBuilder.build())

        // Extract rate limiting and scopes
        response.header("x-ratelimit-reset")?.let {
            lastRateLimitReset = it
        }
        val scopes = response.header("X-OAuth-Scopes") ?: response.header("X-Accepted-OAuth-Scopes")
        if (scopes != null) {
            lastOAuthScopes = scopes
        }

        response
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val contentType = "application/json".toMediaType()

    val gitHubApi: GitHubApi by lazy {
        Retrofit.Builder()
            .baseUrl(GITHUB_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(GitHubApi::class.java)
    }

    val gitHubAuthApi: GitHubAuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(GITHUB_AUTH_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(GitHubAuthApi::class.java)
    }

    val configApi: ConfigApi by lazy {
        Retrofit.Builder()
            .baseUrl(CONFIG_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(ConfigApi::class.java)
    }
}
