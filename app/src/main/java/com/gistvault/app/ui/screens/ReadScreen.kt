package com.gistvault.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gistvault.app.data.model.Gist
import com.gistvault.app.data.model.GitHubUser
import com.gistvault.app.data.repository.GistRepository
import com.gistvault.app.ui.components.DeleteConfirmDialog
import com.gistvault.app.ui.components.GistActionsSheet
import com.gistvault.app.ui.components.TopBar
import com.gistvault.app.ui.components.highlightCode
import com.gistvault.app.ui.theme.GistTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var gist by remember { mutableStateOf<Gist?>(gistRepository.getCachedGist(gistId)) }
    var isLoading by remember { mutableStateOf(gist == null) }
    var isFailed by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMoreSheet by remember { mutableStateOf(false) }
    var isRawMode by remember { mutableStateOf(false) }
    var selectedFileIndex by remember { mutableStateOf(0) }

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

    val files = remember(gist) { gist?.files?.values?.toList() ?: emptyList() }
    val currentFile = files.getOrNull(selectedFileIndex) ?: gist?.primaryFile
    val content = currentFile?.content ?: ""
    val filename = currentFile?.filename ?: gist?.displayTitle ?: "file.txt"
    val language = currentFile?.language ?: "Text"

    val updatedDateStr = remember(gist?.updatedAt) {
        try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = parser.parse(gist?.updatedAt ?: "")
            if (date != null) SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date) else "Unknown"
        } catch (_: Exception) {
            "Unknown"
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = filename,
                showBack = true,
                onBack = onBack
            ) {
                // Interactive Star Button
                IconButton(
                    onClick = {
                        val currentGist = gist ?: return@IconButton
                        scope.launch {
                            val res = gistRepository.toggleStar(currentGist.id, currentGist.isStarred)
                            res.onSuccess { newStatus ->
                                gist = currentGist.copy(isStarred = newStatus)
                                showToast(if (newStatus) "Starred" else "Unstarred")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (gist?.isStarred == true) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = "Star",
                        tint = if (gist?.isStarred == true) colors.amber else colors.ink,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Three-dots overflow
                IconButton(onClick = { showMoreSheet = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "More",
                        tint = colors.ink,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        },
        bottomBar = {
            // Fixed Bottom Action Bar matching Screen 3 (Copy, Share, Edit)
            Surface(
                color = colors.card,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.line.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(vertical = 12.dp, horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DetailActionButton(
                        icon = Icons.Default.ContentCopy,
                        label = "Copy",
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Gist Content", content))
                            showToast("Copied to clipboard")
                        }
                    )

                    DetailActionButton(
                        icon = Icons.Default.Share,
                        label = "Share",
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, filename)
                                putExtra(Intent.EXTRA_TEXT, "https://gist.github.com/${gistId}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Gist"))
                        }
                    )

                    DetailActionButton(
                        icon = Icons.Default.Edit,
                        label = "Edit",
                        onClick = { onEdit(gistId) }
                    )
                }
            }
        },
        containerColor = colors.paper
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 1. Segmented Control: Preview | Raw
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.card)
                    .border(1.dp, colors.line.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(3.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isRawMode) colors.primary else Color.Transparent)
                            .clickable { isRawMode = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Preview",
                            color = if (!isRawMode) Color.White else colors.muted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isRawMode) colors.primary else Color.Transparent)
                            .clickable { isRawMode = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Raw",
                            color = if (isRawMode) Color.White else colors.muted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. 3-Column Metadata Card (Type, Visibility, Updated)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.card)
                    .border(1.dp, colors.line.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetaColumn(title = "Type", value = language)
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(colors.line.copy(alpha = 0.5f)))
                MetaColumn(title = "Visibility", value = if (gist?.public == true) "Public" else "Secret")
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(colors.line.copy(alpha = 0.5f)))
                MetaColumn(title = "Updated", value = updatedDateStr)
            }

            // 3. Multi-file tabs if more than 1 file
            if (files.size > 1) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    files.forEachIndexed { index, file ->
                        val isSelected = index == selectedFileIndex
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) colors.primarySoft else colors.card)
                                .border(1.dp, if (isSelected) colors.primary else colors.line, RoundedCornerShape(10.dp))
                                .clickable { selectedFileIndex = index }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = file.filename,
                                color = if (isSelected) colors.primary else colors.inkSoft,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Code Box with Line Numbers & Syntax Highlighting
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.line.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colors.primary, strokeWidth = 3.dp)
                    }
                } else if (content.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Empty file",
                            color = colors.faint,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    val lines = remember(content) { content.lines() }
                    val highlighted = remember(content, language, colors.isDark) {
                        highlightCode(content, language, colors.isDark)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        // Left Gutter: Line Numbers
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            lines.indices.forEach { idx ->
                                Text(
                                    text = "${idx + 1}",
                                    color = colors.faint,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        // Code Text with Horizontal Scroll
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(rememberScrollState())
                        ) {
                            if (isRawMode) {
                                Text(
                                    text = content,
                                    color = colors.ink,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 18.sp
                                )
                            } else {
                                Text(
                                    text = highlighted,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // More Options Bottom Sheet
    if (showMoreSheet && gist != null) {
        GistActionsSheet(
            gist = gist!!,
            onCopyContent = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Gist Content", content))
                showToast("Content copied to clipboard")
            },
            onShareGist = {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, filename)
                    putExtra(Intent.EXTRA_TEXT, "https://gist.github.com/${gistId}")
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Gist"))
            },
            onOpenInGitHub = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://gist.github.com/${gistId}"))
                context.startActivity(intent)
            },
            onEditGist = { onEdit(gistId) },
            onDeleteGist = { showDeleteDialog = true },
            onDismiss = { showMoreSheet = false }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        DeleteConfirmDialog(
            onConfirm = {
                showDeleteDialog = false
                scope.launch {
                    val result = gistRepository.deleteGist(gistId)
                    result.onSuccess {
                        showToast("Gist deleted")
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

@Composable
private fun MetaColumn(title: String, value: String) {
    val colors = GistTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            color = colors.muted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = colors.ink,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DetailActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val colors = GistTheme.colors

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(colors.primarySoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = colors.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = colors.inkSoft,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
