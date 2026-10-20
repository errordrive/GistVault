package com.gistvault.app.ui.screens

import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gistvault.app.R
import com.gistvault.app.data.model.Gist
import com.gistvault.app.data.model.GitHubUser
import com.gistvault.app.data.repository.GistRepository
import com.gistvault.app.ui.components.DeleteConfirmDialog
import com.gistvault.app.ui.components.TopBar
import com.gistvault.app.ui.components.TopBarIconButton
import com.gistvault.app.ui.theme.GistTheme
import com.gistvault.app.ui.theme.getLanguageColor
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ReadScreen(
    gistId: String,
    currentUser: GitHubUser?,
    gistRepository: GistRepository,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onDeleted: () -> Unit,
    showToast: (String) -> Unit
) {
    val colors = GistTheme.colors
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var gist by remember { mutableStateOf<Gist?>(gistRepository.getCachedGist(gistId)) }
    var isLoading by remember { mutableStateOf(gist == null) }
    var isFailed by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isPreviewTab by remember { mutableStateOf(false) }

    LaunchedEffect(gistId) {
        isLoading = gist == null
        val result = gistRepository.fetchGistDetail(gistId)
        result.onSuccess {
            gist = it
            isLoading = false
        }.onFailure {
            if (gist == null) isFailed = true
            isLoading = false
        }
    }

    val primaryFile = gist?.primaryFile
    val isMarkdown = primaryFile?.filename?.endsWith(".md", ignoreCase = true) == true ||
            primaryFile?.language.equals("Markdown", ignoreCase = true)
    val langColor = getLanguageColor(primaryFile?.language)

    Scaffold(
        topBar = {
            TopBar(
                title = gist?.displayTitle ?: if (isFailed) stringResource(id = R.string.read_failed) else stringResource(id = R.string.read_loading),
                showBack = true,
                onBack = onBack
            ) {
                // Export
                TopBarIconButton(
                    icon = Icons.Default.Download,
                    contentDescription = stringResource(id = R.string.read_export),
                    onClick = {
                        val file = primaryFile ?: return@TopBarIconButton
                        val content = file.content ?: ""
                        try {
                            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "GistVault")
                            if (!dir.exists()) dir.mkdirs()
                            val dest = File(dir, file.filename)
                            dest.writeText(content)
                            showToast("Exported ${file.filename}")
                        } catch (_: Exception) {
                            showToast("Export failed")
                        }
                    }
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Copy Raw Link
                TopBarIconButton(
                    icon = Icons.Default.Link,
                    contentDescription = stringResource(id = R.string.read_copy_raw),
                    onClick = {
                        val ownerLogin = currentUser?.login ?: gist?.owner?.login ?: "gist"
                        val filename = primaryFile?.filename ?: "file.txt"
                        val rawUrl = "https://gist.githubusercontent.com/$ownerLogin/$gistId/raw/$filename"
                        clipboardManager.setText(AnnotatedString(rawUrl))
                        showToast("Raw link copied")
                    }
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Edit
                TopBarIconButton(
                    icon = Icons.Default.Edit,
                    contentDescription = stringResource(id = R.string.read_edit),
                    onClick = { onEdit(gistId) }
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Delete
                TopBarIconButton(
                    icon = Icons.Default.Delete,
                    contentDescription = stringResource(id = R.string.read_delete),
                    isDanger = true,
                    onClick = { showDeleteDialog = true }
                )
            }
        },
        containerColor = colors.paper
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = colors.primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                isFailed -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.read_failed),
                            color = colors.danger,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                gist != null -> {
                    val currentGist = gist!!
                    val file = currentGist.primaryFile

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // Header Info Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = file.filename,
                                    color = colors.ink,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(colors.primarySoft)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = stringResource(id = if (currentGist.public) R.string.home_public else R.string.home_secret),
                                        color = colors.primary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(langColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = file.language ?: "Text",
                                    color = colors.muted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Code Container Card
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.card),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, colors.line.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                        ) {
                            Column {
                                // Code Card Top Toolbar
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(colors.card)
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(langColor, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = file.language ?: "Text",
                                            color = colors.inkSoft,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    // Markdown Preview / Code Tab Toggle
                                    if (isMarkdown) {
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(colors.paper)
                                                .padding(2.dp)
                                        ) {
                                            Text(
                                                text = stringResource(id = R.string.read_code),
                                                color = if (!isPreviewTab) colors.primary else colors.muted,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (!isPreviewTab) colors.card else Color.Transparent)
                                                    .clickable { isPreviewTab = false }
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                            Text(
                                                text = stringResource(id = R.string.read_preview),
                                                color = if (isPreviewTab) colors.primary else colors.muted,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isPreviewTab) colors.card else Color.Transparent)
                                                    .clickable { isPreviewTab = true }
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    // One-tap Copy Code Button
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                val content = file.content ?: ""
                                                clipboardManager.setText(AnnotatedString(content))
                                                showToast("Content copied")
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = null,
                                            tint = colors.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(id = R.string.read_copy),
                                            color = colors.primary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                HorizontalDivider(color = colors.line.copy(alpha = 0.6f))

                                // Code Editor Block with horizontal scrolling
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (colors.isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC))
                                        .horizontalScroll(rememberScrollState())
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = file.content ?: "",
                                        color = colors.ink,
                                        fontFamily = if (isPreviewTab && isMarkdown) FontFamily.Default else FontFamily.Monospace,
                                        fontSize = 13.sp,
                                        lineHeight = 21.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmDialog(
            onConfirm = {
                showDeleteDialog = false
                scope.launch {
                    val result = gistRepository.deleteGist(gistId)
                    result.onSuccess {
                        showToast("Entry deleted")
                        onDeleted()
                    }.onFailure {
                        showToast("Delete failed")
                    }
                }
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}
