package com.gistvault.app.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gistvault.app.data.model.Draft
import com.gistvault.app.data.model.EditorFile
import com.gistvault.app.data.repository.GistRepository
import com.gistvault.app.ui.components.TopBar
import com.gistvault.app.ui.theme.GistTheme
import kotlinx.coroutines.launch

@Composable
fun EditorScreen(
    gistId: String?, // null if creating new
    gistRepository: GistRepository,
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    showToast: (String) -> Unit
) {
    val colors = GistTheme.colors
    val scope = rememberCoroutineScope()

    var description by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var isLoadingRemote by remember { mutableStateOf(gistId != null) }
    var showUnsavedDialog by remember { mutableStateOf(false) }

    // Multi-file state
    var files by remember {
        mutableStateOf(
            listOf(EditorFile(filename = if (gistId == null) "main.json" else "", content = ""))
        )
    }
    var activeFileIndex by remember { mutableStateOf(0) }

    // Load initial data
    LaunchedEffect(gistId) {
        if (gistId != null) {
            val result = gistRepository.fetchGistDetail(gistId)
            result.onSuccess { remoteGist ->
                description = remoteGist.description ?: ""
                isPublic = remoteGist.public
                val loadedFiles = remoteGist.files.values.map {
                    EditorFile(filename = it.filename, content = it.content ?: "")
                }
                if (loadedFiles.isNotEmpty()) {
                    files = loadedFiles
                }
                isLoadingRemote = false
            }.onFailure {
                val draft = gistRepository.getDraft(gistId)
                if (draft != null) {
                    description = draft.title
                    files = listOf(EditorFile(filename = draft.filename, content = draft.content))
                    isPublic = draft.vis == "public"
                    showToast("Draft restored")
                }
                isLoadingRemote = false
            }
        } else {
            val draft = gistRepository.getDraft(null)
            if (draft != null) {
                description = draft.title
                files = listOf(EditorFile(filename = draft.filename.ifBlank { "main.json" }, content = draft.content))
                isPublic = draft.vis == "public"
                showToast("Draft restored")
            }
        }
    }

    // Save draft on every change
    LaunchedEffect(description, files, isPublic) {
        val first = files.firstOrNull() ?: return@LaunchedEffect
        gistRepository.saveDraft(
            gistId,
            Draft(
                title = description,
                filename = first.filename,
                content = first.content,
                vis = if (isPublic) "public" else "secret"
            )
        )
    }

    val currentFile = files.getOrNull(activeFileIndex) ?: files.firstOrNull() ?: EditorFile()

    val saveGistAction: () -> Unit = {
        if (!isSaving) {
            val emptyFilename = files.any { it.filename.isBlank() }
            if (emptyFilename) {
                showToast("Every file must have a filename")
            } else {
                isSaving = true
                scope.launch {
                    val fileMap = files.associate { it.filename.trim() to it.content }
                    val result = if (gistId == null) {
                        gistRepository.createGist(
                            description = description,
                            files = fileMap,
                            isPublic = isPublic
                        )
                    } else {
                        gistRepository.updateGist(
                            id = gistId,
                            description = description,
                            files = fileMap
                        )
                    }

                    result.onSuccess { savedGist ->
                        gistRepository.clearDraft(gistId)
                        showToast(if (gistId == null) "Gist created" else "Gist updated")
                        onSaved(savedGist.id)
                    }.onFailure { err ->
                        showToast(err.message ?: "Failed to save gist")
                        isSaving = false
                    }
                }
            }
        }
    }

    BackHandler {
        if (description.isNotBlank() || files.any { it.content.isNotBlank() }) {
            showUnsavedDialog = true
        } else {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = if (gistId == null) "Create Gist" else "Edit Gist",
                showBack = true,
                onBack = {
                    if (description.isNotBlank() || files.any { it.content.isNotBlank() }) {
                        showUnsavedDialog = true
                    } else {
                        onBack()
                    }
                }
            ) {
                // Checkmark Save button in circular electric-blue button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.primary)
                        .clickable(enabled = !isSaving, onClick = saveGistAction),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
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
            // 1. Description (optional)
            Text(
                text = "Description (optional)",
                color = colors.muted,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Add a description...",
                        color = colors.faint,
                        fontSize = 14.sp
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.card,
                    unfocusedContainerColor = colors.card,
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.line.copy(alpha = 0.6f),
                    focusedTextColor = colors.ink,
                    unfocusedTextColor = colors.ink
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Visibility Controls (Public / Secret)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Public Pill
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isPublic) colors.primary else colors.card)
                        .border(
                            1.dp,
                            if (isPublic) colors.primary else colors.line.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { isPublic = true },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = if (isPublic) Color.White else colors.muted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Public",
                            color = if (isPublic) Color.White else colors.inkSoft,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Secret Pill
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (!isPublic) colors.primary else colors.card)
                        .border(
                            1.dp,
                            if (!isPublic) colors.primary else colors.line.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { isPublic = false },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (!isPublic) Color.White else colors.muted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Secret",
                            color = if (!isPublic) Color.White else colors.inkSoft,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Multi-file tabs if > 1 file
            if (files.size > 1) {
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    files.forEachIndexed { index, file ->
                        val isSelected = index == activeFileIndex
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) colors.primarySoft else colors.card)
                                .border(
                                    1.dp,
                                    if (isSelected) colors.primary else colors.line,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { activeFileIndex = index }
                                .padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = file.filename.ifBlank { "file_${index + 1}" },
                                color = if (isSelected) colors.primary else colors.inkSoft,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                            if (files.size > 1) {
                                IconButton(
                                    onClick = {
                                        val mutable = files.toMutableList()
                                        mutable.removeAt(index)
                                        files = mutable
                                        if (activeFileIndex >= files.size) {
                                            activeFileIndex = files.size - 1
                                        }
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = colors.faint,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Filename field
            Text(
                text = "Filename",
                color = colors.muted,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            OutlinedTextField(
                value = currentFile.filename,
                onValueChange = { newName ->
                    val mutable = files.toMutableList()
                    mutable[activeFileIndex] = currentFile.copy(filename = newName)
                    files = mutable
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "e.g. flexconfig.json",
                        color = colors.faint,
                        fontSize = 14.sp
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.card,
                    unfocusedContainerColor = colors.card,
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.line.copy(alpha = 0.6f),
                    focusedTextColor = colors.ink,
                    unfocusedTextColor = colors.ink
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Code Editor with Line Numbers
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.line.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                val linesCount = currentFile.content.lines().size.coerceAtLeast(1)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    // Line numbers gutter
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier
                            .padding(end = 12.dp, top = 16.dp)
                    ) {
                        for (i in 1..linesCount) {
                            Text(
                                text = "$i",
                                color = colors.faint,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    // Content editor
                    OutlinedTextField(
                        value = currentFile.content,
                        onValueChange = { newContent ->
                            val mutable = files.toMutableList()
                            mutable[activeFileIndex] = currentFile.copy(content = newContent)
                            files = mutable
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 240.dp),
                        placeholder = {
                            Text(
                                text = "// Write code or notes here...",
                                color = colors.faint,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        textStyle = LocalTextStyle.current.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = colors.ink
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 5. Add Another File Button matching Screen 4
            OutlinedButton(
                onClick = {
                    val nextNum = files.size + 1
                    files = files + EditorFile(filename = "file_$nextNum.txt", content = "")
                    activeFileIndex = files.size - 1
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(colors.primary.copy(alpha = 0.5f))
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Another File",
                    color = colors.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    // Unsaved Changes Confirmation Dialog
    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = {
                Text(text = "Discard Changes?", color = colors.ink, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "You have unsaved changes. Your draft is saved locally, but are you sure you want to exit?",
                    color = colors.muted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUnsavedDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.danger)
                ) {
                    Text("Discard", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnsavedDialog = false }) {
                    Text("Keep Editing", color = colors.inkSoft)
                }
            },
            containerColor = colors.card
        )
    }
}
