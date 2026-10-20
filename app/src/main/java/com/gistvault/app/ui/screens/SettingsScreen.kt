package com.gistvault.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
    currentLang: String,
    onSelectTheme: (String) -> Unit,
    onToggleLang: () -> Unit,
    onDisconnect: () -> Unit,
    onBack: () -> Unit
) {
    val colors = GistTheme.colors
    val context = LocalContext.current
    var showThemeSheet by remember { mutableStateOf(false) }
    var showDisconnectDialog by remember { mutableStateOf(false) }

    val openUrl: (String) -> Unit = { url ->
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    val themeLabel = when (currentTheme) {
        "light" -> stringResource(id = R.string.theme_light)
        "dark" -> stringResource(id = R.string.theme_dark)
        "pink" -> stringResource(id = R.string.theme_pink)
        else -> stringResource(id = R.string.theme_system)
    }

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(id = R.string.set_title),
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
                .padding(16.dp)
        ) {
            // Section 1: GitHub Account Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!currentUser?.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = currentUser!!.avatarUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(colors.primarySoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = colors.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.login ?: stringResource(id = R.string.set_not_signed),
                            color = colors.ink,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = colors.success,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(id = R.string.set_enc_conn),
                                color = colors.success,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 2: System Preferences
            Text(
                text = stringResource(id = R.string.set_sys_prefs),
                color = colors.muted,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.Palette,
                        title = stringResource(id = R.string.set_theme),
                        value = themeLabel,
                        onClick = { showThemeSheet = true }
                    )

                    HorizontalDivider(color = colors.line.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))

                    SettingsRow(
                        icon = Icons.Default.Language,
                        title = stringResource(id = R.string.set_lang),
                        value = if (currentLang == "bn") "বাংলা" else "English",
                        onClick = onToggleLang
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 3: Support
            Text(
                text = stringResource(id = R.string.set_support),
                color = colors.muted,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.Coffee,
                        title = stringResource(id = R.string.set_support_dev),
                        isExternal = true,
                        onClick = { openUrl("https://ko-fi.com/errordrive") }
                    )

                    HorizontalDivider(color = colors.line.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))

                    SettingsRow(
                        icon = Icons.Default.Send,
                        title = stringResource(id = R.string.set_channel),
                        isExternal = true,
                        onClick = { openUrl("https://t.me/+xdZcHWiCltQ0MjY1") }
                    )

                    HorizontalDivider(color = colors.line.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))

                    SettingsRow(
                        icon = Icons.Default.Chat,
                        title = stringResource(id = R.string.set_contact),
                        isExternal = true,
                        onClick = { openUrl("https://t.me/nayembrox") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Section 4: Disconnect Button
            OutlinedButton(
                onClick = { showDisconnectDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.danger),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(colors.danger.copy(alpha = 0.4f)))
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    tint = colors.danger,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.set_disconnect),
                    color = colors.danger,
                    fontWeight = FontWeight.SemiBold
                )
            }
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
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    value: String? = null,
    isExternal: Boolean = false,
    onClick: () -> Unit
) {
    val colors = GistTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
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
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        if (value != null) {
            Text(
                text = value,
                color = colors.muted,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Icon(
            imageVector = if (isExternal) Icons.Default.OpenInNew else Icons.Default.ChevronRight,
            contentDescription = null,
            tint = colors.faint,
            modifier = Modifier.size(18.dp)
        )
    }
}
