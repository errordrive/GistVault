package com.gistvault.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Gist(
    val id: String = "",
    val description: String? = null,
    val public: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val files: Map<String, GistFile> = emptyMap(),
    val owner: GistOwner? = null,
    val isStarred: Boolean = false
) {
    val primaryFile: GistFile
        get() = files.values.firstOrNull() ?: GistFile(filename = "file.txt", language = "Text")

    val displayTitle: String
        get() = primaryFile.filename.takeIf { it.isNotBlank() }
            ?: description?.takeIf { it.isNotBlank() }
            ?: "Untitled Gist"

    val displaySubtitle: String?
        get() = if (description != null && description.isNotBlank() && description != primaryFile.filename) description else null

    val tags: List<String>
        get() {
            val text = description ?: return emptyList()
            val regex = Regex("""#([\p{L}\p{N}][\p{L}\p{M}\p{N}_-]{1,31})""")
            val result = mutableListOf<String>()
            for (match in regex.findAll(text)) {
                val tag = match.groupValues[1].lowercase()
                if (!result.contains(tag)) {
                    result.add(tag)
                    if (result.size >= 8) break
                }
            }
            return result
        }
}

@Serializable
data class GistFile(
    val filename: String = "",
    val type: String? = null,
    val language: String? = "Text",
    @SerialName("raw_url") val rawUrl: String? = null,
    val size: Long = 0,
    val content: String? = null,
    val truncated: Boolean? = false
)

@Serializable
data class GistOwner(
    val login: String = "",
    val id: Long = 0,
    @SerialName("avatar_url") val avatarUrl: String? = null
)

@Serializable
data class CreateGistRequest(
    val description: String,
    val public: Boolean,
    val files: Map<String, GistFileContent>
)

@Serializable
data class UpdateGistRequest(
    val description: String,
    val files: Map<String, GistFileContent>
)

@Serializable
data class GistFileContent(
    val content: String
)

@Serializable
data class GitHubUser(
    val login: String = "",
    val id: Long = 0,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val name: String? = null,
    val email: String? = null,
    val bio: String? = null
)

@Serializable
data class DeviceCodeResponse(
    @SerialName("device_code") val deviceCode: String = "",
    @SerialName("user_code") val userCode: String = "",
    @SerialName("verification_uri") val verificationUri: String = "https://github.com/login/device",
    @SerialName("expires_in") val expiresIn: Int = 900,
    val interval: Int = 5,
    val error: String? = null,
    @SerialName("error_description") val errorDescription: String? = null
)

@Serializable
data class OAuthTokenResponse(
    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("token_type") val tokenType: String? = null,
    val scope: String? = null,
    val error: String? = null,
    @SerialName("error_description") val errorDescription: String? = null
)

@Serializable
data class AppConfig(
    val update: UpdateConfig? = null,
    val notice: NoticeConfig? = null
)

@Serializable
data class UpdateConfig(
    val enabled: Boolean = false,
    @SerialName("latest_version") val latestVersion: String? = null,
    val title: String? = null,
    val message: String? = null,
    val url: String? = null,
    val force: Boolean = false
)

@Serializable
data class NoticeConfig(
    val enabled: Boolean = false,
    val title: String? = null,
    val message: String? = null
)

data class CachedGists(
    val ts: Long,
    val gists: List<Gist>
)

data class Draft(
    val title: String = "",
    val filename: String = "",
    val content: String = "",
    val vis: String = "secret",
    val ts: Long = System.currentTimeMillis()
)

data class EditorFile(
    val id: String = java.util.UUID.randomUUID().toString(),
    val filename: String = "",
    val content: String = ""
)
