package com.gistvault.app.data.api

import com.gistvault.app.data.model.*
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface GitHubApi {

    @GET("user")
    suspend fun getCurrentUser(): Response<GitHubUser>

    @GET("gists")
    suspend fun getGists(
        @Query("per_page") perPage: Int = 100,
        @Query("t") timestamp: Long = System.currentTimeMillis()
    ): Response<List<Gist>>

    @GET("gists/{id}")
    suspend fun getGist(
        @Path("id") id: String,
        @Query("t") timestamp: Long = System.currentTimeMillis()
    ): Response<Gist>

    @POST("gists")
    suspend fun createGist(
        @Body request: CreateGistRequest
    ): Response<Gist>

    @PATCH("gists/{id}")
    suspend fun updateGist(
        @Path("id") id: String,
        @Body request: UpdateGistRequest
    ): Response<Gist>

    @DELETE("gists/{id}")
    suspend fun deleteGist(
        @Path("id") id: String
    ): Response<Unit>
}

interface GitHubAuthApi {

    @POST("login/device/code")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun requestDeviceCode(
        @Body body: RequestBody
    ): Response<DeviceCodeResponse>

    @POST("login/oauth/access_token")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun pollAccessToken(
        @Body body: RequestBody
    ): Response<OAuthTokenResponse>
}

interface ConfigApi {
    @GET("api/config")
    suspend fun getConfig(): Response<AppConfig>

    @POST("api/ping")
    @Headers("Content-Type: application/json")
    suspend fun ping(
        @Body body: RequestBody
    ): Response<Unit>
}
