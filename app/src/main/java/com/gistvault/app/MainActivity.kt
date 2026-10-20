package com.gistvault.app

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.gistvault.app.data.api.ApiClient
import com.gistvault.app.data.model.AppConfig
import com.gistvault.app.data.model.Gist
import com.gistvault.app.data.model.GitHubUser
import com.gistvault.app.data.repository.AuthRepository
import com.gistvault.app.data.repository.GistRepository
import com.gistvault.app.data.repository.SettingsRepository
import com.gistvault.app.navigation.Screen
import com.gistvault.app.ui.components.NoticeDialog
import com.gistvault.app.ui.components.ToastHost
import com.gistvault.app.ui.components.UpdateDialog
import com.gistvault.app.ui.screens.*
import com.gistvault.app.ui.theme.GistVaultAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var authRepository: AuthRepository
    private lateinit var gistRepository: GistRepository
    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authRepository = AuthRepository(this)
        gistRepository = GistRepository(this)
        settingsRepository = SettingsRepository(this)

        // Initialize ApiClient token
        val initialToken = authRepository.getToken()
        if (!initialToken.isNullOrBlank()) {
            ApiClient.setToken(initialToken)
        }

        setContent {
            val currentTheme by settingsRepository.themeState.collectAsState()
            val currentLang by settingsRepository.langState.collectAsState()

            // Update app locale if needed
            LaunchedEffect(currentLang) {
                val locale = Locale(currentLang)
                Locale.setDefault(locale)
                val config = resources.configuration
                config.setLocale(locale)
                resources.updateConfiguration(config, resources.displayMetrics)
            }

            GistVaultAppTheme(themeSetting = currentTheme) {
                GistVaultApp(
                    authRepository = authRepository,
                    gistRepository = gistRepository,
                    settingsRepository = settingsRepository,
                    currentTheme = currentTheme,
                    currentLang = currentLang,
                    onExitApp = { finish() }
                )
            }
        }
    }
}

@Composable
fun GistVaultApp(
    authRepository: AuthRepository,
    gistRepository: GistRepository,
    settingsRepository: SettingsRepository,
    currentTheme: String,
    currentLang: String,
    onExitApp: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }
    var currentUser by remember { mutableStateOf<GitHubUser?>(null) }
    var gists by remember { mutableStateOf<List<Gist>>(gistRepository.getCachedGists() ?: emptyList()) }
    var isLoadingGists by remember { mutableStateOf(false) }
    var isOffline by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    // Remote config modals
    var remoteConfig by remember { mutableStateOf<AppConfig?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showNoticeDialog by remember { mutableStateOf(false) }

    val showToast: (String, Long) -> Unit = { msg, duration ->
        toastMessage = msg
        scope.launch {
            delay(duration)
            if (toastMessage == msg) {
                toastMessage = null
            }
        }
    }

    // Refresh Gists logic
    val refreshGists: () -> Unit = {
        isLoadingGists = true
        scope.launch {
            val result = gistRepository.fetchGists()
            result.onSuccess {
                gists = it
                isOffline = false
            }.onFailure {
                isOffline = true
                showToast("Offline — showing cached entries", 2600)
            }
            isLoadingGists = false
        }
    }

    // Startup check
    LaunchedEffect(Unit) {
        // Fetch remote config for updates & notices
        scope.launch {
            val config = settingsRepository.fetchRemoteConfig()
            if (config != null) {
                remoteConfig = config
                config.update?.let { update ->
                    if (update.enabled && update.latestVersion != null) {
                        if (settingsRepository.compareVersions(update.latestVersion, SettingsRepository.CURRENT_APP_VERSION) > 0) {
                            if (update.force || !settingsRepository.isUpdateDismissed(update.latestVersion)) {
                                showUpdateDialog = true
                            }
                        }
                    }
                }
                config.notice?.let { notice ->
                    if (notice.enabled && !notice.message.isNullOrBlank()) {
                        val key = "${notice.title}|${notice.message}"
                        if (!settingsRepository.isNoticeDismissed(key)) {
                            showNoticeDialog = true
                        }
                    }
                }
            }
        }

        // Verify session
        val token = authRepository.getToken()
        if (!token.isNullOrBlank()) {
            delay(1200) // Animated splash delay
            val verify = authRepository.verifyCurrentUser()
            verify.onSuccess { (user, hasScope) ->
                currentUser = user
                currentScreen = Screen.Home
                refreshGists()
                if (!hasScope) {
                    showToast("Connected, but token lacks 'gist' scope", 4200)
                }
            }.onFailure {
                // If offline, still allow home with cached gists
                val cached = gistRepository.getCachedGists()
                if (cached != null) {
                    currentScreen = Screen.Home
                    isOffline = true
                } else {
                    currentScreen = Screen.Login
                }
            }
        } else {
            delay(1200)
            currentScreen = Screen.Login
        }
    }

    // Back handler matching wt()
    var lastBackPressTime by remember { mutableStateOf(0L) }
    BackHandler {
        when (val screen = currentScreen) {
            is Screen.Login -> onExitApp()
            is Screen.Home -> {
                val now = System.currentTimeMillis()
                if (now - lastBackPressTime < 2000L) {
                    onExitApp()
                } else {
                    lastBackPressTime = now
                    showToast("Press back again to exit", 2000L)
                }
            }
            is Screen.DeviceAuth -> currentScreen = Screen.Login
            is Screen.Settings -> currentScreen = Screen.Home
            is Screen.Read -> currentScreen = Screen.Home
            is Screen.Editor -> {
                if (screen.gistId != null) {
                    currentScreen = Screen.Read(screen.gistId)
                } else {
                    currentScreen = Screen.Home
                }
            }
            Screen.Splash -> onExitApp()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val screen = currentScreen) {
            Screen.Splash -> {
                SplashScreen()
            }
            Screen.Login -> {
                var isConnecting by remember { mutableStateOf(false) }
                LoginScreen(
                    onLoginWithGitHub = {
                        currentScreen = Screen.DeviceAuth
                    },
                    onConnectToken = { token ->
                        isConnecting = true
                        authRepository.saveToken(token)
                        scope.launch {
                            val result = authRepository.verifyCurrentUser()
                            result.onSuccess { (user, hasScope) ->
                                currentUser = user
                                showToast(
                                    if (hasScope) "Connected as ${user.login}" else "Connected, but token lacks 'gist' scope",
                                    3200
                                )
                                currentScreen = Screen.Home
                                refreshGists()
                            }.onFailure {
                                authRepository.clearToken()
                                showToast("Invalid token. Check and try again.", 3200)
                            }
                            isConnecting = false
                        }
                    },
                    isLoading = isConnecting,
                    onSetupCustomClientId = { authRepository.saveCustomClientId(it) },
                    currentClientId = authRepository.getClientId()
                )
            }
            Screen.DeviceAuth -> {
                DeviceAuthScreen(
                    authRepository = authRepository,
                    onSuccess = { token ->
                        scope.launch {
                            val result = authRepository.verifyCurrentUser()
                            result.onSuccess { (user, hasScope) ->
                                currentUser = user
                                showToast(
                                    if (hasScope) "Connected as ${user.login}" else "Connected, but lacks 'gist' scope",
                                    3200
                                )
                                currentScreen = Screen.Home
                                refreshGists()
                            }
                        }
                    },
                    onCancel = { currentScreen = Screen.Login },
                    showToast = { showToast(it, 2600) }
                )
            }
            Screen.Home -> {
                HomeScreen(
                    gists = gists,
                    isLoading = isLoadingGists,
                    isOffline = isOffline,
                    onRefresh = refreshGists,
                    onOpenGist = { gistId -> currentScreen = Screen.Read(gistId) },
                    onNewGist = { currentScreen = Screen.Editor(null) },
                    onOpenSettings = { currentScreen = Screen.Settings }
                )
            }
            is Screen.Read -> {
                ReadScreen(
                    gistId = screen.gistId,
                    currentUser = currentUser,
                    gistRepository = gistRepository,
                    onBack = { currentScreen = Screen.Home },
                    onEdit = { id -> currentScreen = Screen.Editor(id) },
                    onDeleted = {
                        refreshGists()
                        currentScreen = Screen.Home
                    },
                    showToast = { showToast(it, 2600) }
                )
            }
            is Screen.Editor -> {
                EditorScreen(
                    gistId = screen.gistId,
                    gistRepository = gistRepository,
                    onBack = {
                        if (screen.gistId != null) {
                            currentScreen = Screen.Read(screen.gistId)
                        } else {
                            currentScreen = Screen.Home
                        }
                    },
                    onSaved = { savedId ->
                        refreshGists()
                        currentScreen = Screen.Read(savedId)
                    },
                    showToast = { showToast(it, 2600) }
                )
            }
            Screen.Settings -> {
                SettingsScreen(
                    currentUser = currentUser,
                    currentTheme = currentTheme,
                    currentLang = currentLang,
                    onSelectTheme = { settingsRepository.setTheme(it) },
                    onToggleLang = {
                        val nextLang = if (currentLang == "bn") "en" else "bn"
                        settingsRepository.setLanguage(nextLang)
                        showToast(if (nextLang == "bn") "ভাষা: বাংলা" else "Language: English", 2600)
                    },
                    onDisconnect = {
                        authRepository.clearToken()
                        currentUser = null
                        gists = emptyList()
                        currentScreen = Screen.Login
                        showToast("Disconnected", 2600)
                    },
                    onBack = { currentScreen = Screen.Home }
                )
            }
        }

        // Floating Toast
        ToastHost(message = toastMessage)

        // Update Modal
        if (showUpdateDialog && remoteConfig?.update != null) {
            val update = remoteConfig!!.update!!
            UpdateDialog(
                update = update,
                onUpdateNow = {
                    update.url?.let {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                },
                onDismiss = {
                    settingsRepository.dismissUpdate(update.latestVersion)
                    showUpdateDialog = false
                }
            )
        }

        // Notice Modal
        if (showNoticeDialog && remoteConfig?.notice != null) {
            val notice = remoteConfig!!.notice!!
            NoticeDialog(
                notice = notice,
                onDismiss = {
                    val key = "${notice.title}|${notice.message}"
                    settingsRepository.dismissNotice(key)
                    showNoticeDialog = false
                }
            )
        }
    }
}
