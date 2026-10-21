package com.gistvault.app.navigation

sealed class Screen {
    data object Splash : Screen()
    data object Login : Screen()
    data object DeviceAuth : Screen()
    data object Home : Screen()
    data object Search : Screen()
    data object Starred : Screen()
    data object Settings : Screen()
    data class Read(val gistId: String) : Screen()
    data class Editor(val gistId: String?) : Screen()
}
