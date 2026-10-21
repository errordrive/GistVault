package com.gistvault.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gistvault.app.R
import com.gistvault.app.data.model.GitHubUser
import com.gistvault.app.ui.components.DisconnectConfirmDialog
import com.gistvault.app.ui.components.ThemeBottomSheet
import com.gistvault.app.ui.components.TopBar
import com.gistvault.app.ui.theme.GistTheme

@Composable
fun SettingsScreen(
    currentUser: GitHubUser?,
    currentTheme: String,
    onSelectTheme: (String) -> Unit,
    onDisconnect: () -> Unit,
    onBack: () -> Unit
) {
    val colors = GistTheme.colors
    val context = LocalContext.current
    var showThemeSheet by remember { mutableStateOf(false) }
    var showDisconnectDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val openUrl: (String) -> Unit = { url ->
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    val themeLabel = when (currentTheme) {
        "light" -> "Light"
        "dark" -> "Dark"
        "pink" -> "Pink"
        else -> "System"
    }

    Scaffold(
        topBar = {
            TopBar(
                title = "Settings",
                showBack = true,
                onBack = onBack
            )
        },
        containerColor = colors.paper
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Section 1: GitHub Account Card matching Screen 5
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.line.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .clickable {
                        currentUser?.login?.let {
                            openUrl("https://github.com/$it")
                        }
                    }
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // User Avatar / GitHub icon
                    if (!currentUser?.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = currentUser!!.avatarUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .border(2.dp, colors.primary, CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF24292F)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_github),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.login ?: "GitHub User",
                            color = colors.ink,
                            fontSize = 17.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(colors.success)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Connected to GitHub",
                                color = colors.success,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = colors.faint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 2: Appearance Group matching Screen 5
            Text(
                text = "Appearance",
                color = colors.muted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.line.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Outlined.Palette,
                        title = "App Theme",
                        value = themeLabel,
                        onClick = { showThemeSheet = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 3: Support Group matching Screen 5
            Text(
                text = "Support",
                color = colors.muted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.line.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Outlined.Favorite,
                        title = "Support Developer",
                        onClick = { openUrl("https://ko-fi.com/errordrive") }
                    )

                    HorizontalDivider(
                        color = colors.line.copy(alpha = 0.35f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    SettingsRow(
                        icon = Icons.Outlined.Send,
                        title = "Official Channel",
                        onClick = { openUrl("https://t.me/+xdZcHWiCltQ0MjY1") }
                    )

                    HorizontalDivider(
                        color = colors.line.copy(alpha = 0.35f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    SettingsRow(
                        icon = Icons.Outlined.Sms,
                        title = "Contact Developer",
                        onClick = { openUrl("https://t.me/nayembrox") }
                    )

                    HorizontalDivider(
                        color = colors.line.copy(alpha = 0.35f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    SettingsRow(
                        icon = Icons.Outlined.Info,
                        title = "About App",
                        value = "v3.2.0",
                        onClick = { showAboutDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Section 4: Red Outlined Disconnect Button matching Screen 5
            OutlinedButton(
                onClick = { showDisconnectDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.danger),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(colors.danger.copy(alpha = 0.6f))
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    tint = colors.danger,
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Disconnect",
                    color = colors.danger,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showThemeSheet) {
        ThemeBottomSheet(
            currentTheme = currentTheme,
            onSelectTheme = onSelectTheme,
            onDismiss = { showThemeSheet = false }
        )
    }

    if (showDisconnectDialog) {
        DisconnectConfirmDialog(
            onConfirm = {
                showDisconnectDialog = false
                onDisconnect()
            },
            onDismiss = { showDisconnectDialog = false }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_app),
                        contentDescription = null,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "GistVault",
                        color = colors.ink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Version 3.2.0 • Native Jetpack Compose",
                        color = colors.muted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Premium, secure, offline-first vault for your GitHub Gists and code snippets.",
                        color = colors.inkSoft,
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tokens and gists are encrypted locally on this device.",
                        color = colors.faint,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK", color = colors.primary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = colors.card
        )
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    value: String? = null,
    onClick: () -> Unit
) {
    val colors = GistTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            color = colors.ink,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        if (value != null) {
            Text(
                text = value,
                color = colors.muted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = colors.faint,
            modifier = Modifier.size(18.dp)
        )
    }
}
