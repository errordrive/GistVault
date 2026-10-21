package com.gistvault.app.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gistvault.app.data.model.Draft
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

    var name by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var isLoadingRemote by remember { mutableStateOf(gistId != null) }
    var showUnsavedDialog by remember { mutableStateOf(false) }

    // Load initial data
    LaunchedEffect(gistId) {
        if (gistId != null) {
            val result = gistRepository.fetchGistDetail(gistId)
            result.onSuccess { remoteGist ->
                name = remoteGist.primaryFile.filename.ifBlank { remoteGist.description ?: "" }
                content = remoteGist.primaryFile.content ?: ""
                isPublic = remoteGist.public
                isLoadingRemote = false
            }.onFailure {
                val draft = gistRepository.getDraft(gistId)
                if (draft != null) {
                    name = draft.filename.ifBlank { draft.title }
                    content = draft.content
                    isPublic = draft.vis == "public"
                    showToast("Draft restored")
                }
                isLoadingRemote = false
            }
        } else {
            val draft = gistRepository.getDraft(null)
            if (draft != null) {
                name = draft.filename.ifBlank { draft.title }
                content = draft.content
                isPublic = draft.vis == "public"
                showToast("Draft restored")
            }
        }
    }

    // Save draft on change
    LaunchedEffect(name, content, isPublic) {
        gistRepository.saveDraft(
            gistId,
            Draft(
                title = name,
                filename = name,
                content = content,
                vis = if (isPublic) "public" else "secret"
            )
        )
    }

    val saveGistAction: () -> Unit = {
        val safeName = name.trim()
        if (safeName.isBlank()) {
            showToast("Please enter a name")
        } else if (!isSaving) {
            isSaving = true
            scope.launch {
                val result = if (gistId == null) {
                    gistRepository.createGist(
                        description = safeName,
                        filename = safeName,
                        content = content,
                        isPublic = isPublic
                    )
                } else {
                    gistRepository.updateGist(
                        id = gistId,
                        description = safeName,
                        filename = safeName,
                        content = content
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

    BackHandler {
        if (name.isNotBlank() || content.isNotBlank()) {
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
                    if (name.isNotBlank() || content.isNotBlank()) {
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
            // 1. Name (the preview name on the app)
            Text(
                text = "Name",
                color = colors.muted,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
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

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Code Editor with Line Numbers
            Text(
                text = "Content",
                color = colors.muted,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.line.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                val linesCount = content.lines().size.coerceAtLeast(1)

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
                        value = content,
                        onValueChange = { content = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 260.dp),
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
                    text = "You have unsaved changes. Are you sure you want to exit?",
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
