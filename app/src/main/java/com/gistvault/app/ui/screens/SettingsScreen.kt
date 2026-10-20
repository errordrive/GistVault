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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
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
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Section 1: GitHub Account & Profile Card
            Text(
                text = "GITHUB ACCOUNT",
                color = colors.muted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.line.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Avatar
                        if (!currentUser?.avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = currentUser!!.avatarUrl,
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(CircleShape)
                                    .border(2.5.dp, colors.primary, CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(CircleShape)
                                    .background(colors.primarySoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            // Display Name or Fallback
                            val displayName = currentUser?.name?.takeIf { it.isNotBlank() } ?: currentUser?.login ?: stringResource(id = R.string.set_not_signed)
                            Text(
                                text = displayName,
                                color = colors.ink,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (currentUser != null && !currentUser.login.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        openUrl("https://github.com/${currentUser.login}")
                                    }
                                ) {
                                    Text(
                                        text = "@${currentUser.login}",
                                        color = colors.primary,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.OpenInNew,
                                        contentDescription = null,
                                        tint = colors.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Verified encrypted session badge
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = colors.success,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(id = R.string.set_enc_conn),
                                    color = colors.success,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Optional Bio
                    if (!currentUser?.bio.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.paper)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = currentUser.bio,
                                color = colors.inkSoft,
                                fontSize = 12.5.sp,
                                fontStyle = FontStyle.Italic,
                                lineHeight = 17.sp
                            )
                        }
                    }

                    // Scope & Privacy Notice Pill
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.primarySoft.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Token scope: 'gist' only. No access to repositories or private code.",
                            color = colors.inkSoft,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 15.sp
                        )
                    }

                    // Action: View profile on GitHub
                    if (currentUser != null && !currentUser.login.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { openUrl("https://github.com/${currentUser.login}") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.inkSoft),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(colors.line)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                tint = colors.muted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "View GitHub Profile",
                                color = colors.inkSoft,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 2: Appearance & Preferences
            Text(
                text = stringResource(id = R.string.set_sys_prefs),
                color = colors.muted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.line.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            ) {
                SettingsRow(
                    icon = Icons.Default.Palette,
                    title = stringResource(id = R.string.set_theme),
                    value = themeLabel,
                    onClick = { showThemeSheet = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 3: Support & Community
            Text(
                text = stringResource(id = R.string.set_support),
                color = colors.muted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.line.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.Coffee,
                        title = stringResource(id = R.string.set_support_dev),
                        isExternal = true,
                        onClick = { openUrl("https://ko-fi.com/errordrive") }
                    )

                    HorizontalDivider(color = colors.line.copy(alpha = 0.35f), modifier = Modifier.padding(horizontal = 16.dp))

                    SettingsRow(
                        icon = Icons.Default.Send,
                        title = stringResource(id = R.string.set_channel),
                        isExternal = true,
                        onClick = { openUrl("https://t.me/+xdZcHWiCltQ0MjY1") }
                    )

                    HorizontalDivider(color = colors.line.copy(alpha = 0.35f), modifier = Modifier.padding(horizontal = 16.dp))

                    SettingsRow(
                        icon = Icons.Default.Chat,
                        title = stringResource(id = R.string.set_contact),
                        isExternal = true,
                        onClick = { openUrl("https://t.me/nayembrox") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 4: App Branding & About Card (High-res PNG App Logo)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.line.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.primarySoft)
                            .border(1.dp, colors.line.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_app),
                            contentDescription = "GistVault Logo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "GistVault",
                            color = colors.ink,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Version 3.2.0 • Native Android",
                            color = colors.muted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Encrypted local vault for GitHub Gists",
                            color = colors.faint,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Section 5: Security & Disconnect Card (Logout Section)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colors.dangerSoft.copy(alpha = 0.6f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.danger.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.danger.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = null,
                                tint = colors.danger,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Sign Out & Disconnect",
                            color = colors.danger,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Disconnecting will securely clear the saved GitHub token and cached entries from this device. Your online gists on GitHub remain completely safe and untouched.",
                        color = colors.inkSoft,
                        fontSize = 12.5.sp,
                        lineHeight = 17.5.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showDisconnectDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.danger),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.set_disconnect),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp
                        )
                    }
                }
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
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colors.primarySoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(18.dp)
            )
        }

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
            imageVector = if (isExternal) Icons.Default.OpenInNew else Icons.Default.ChevronRight,
            contentDescription = null,
            tint = colors.faint,
            modifier = Modifier.size(16.dp)
        )
    }
}
