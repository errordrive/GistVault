package com.gistvault.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gistvault.app.R
import com.gistvault.app.data.model.Draft
import com.gistvault.app.data.model.Gist
import com.gistvault.app.data.repository.GistRepository
import com.gistvault.app.ui.components.ConflictDialog
import com.gistvault.app.ui.components.TopBar
import com.gistvault.app.ui.components.TopBarIconButton
import com.gistvault.app.ui.theme.GistTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EditorScreen(
    gistId: String?, // null if new
    gistRepository: GistRepository,
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    showToast: (String) -> Unit
) {
    val colors = GistTheme.colors
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var filename by remember { mutableStateOf(if (gistId == null) "config.json" else "") }
    var content by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var isLoadingRemote by remember { mutableStateOf(gistId != null) }

    var remoteUpdatedAt by remember { mutableStateOf<String?>(null) }
    var remoteConflictGist by remember { mutableStateOf<Gist?>(null) }
    var showConflictDialog by remember { mutableStateOf(false) }

    // Load initial data
    LaunchedEffect(gistId) {
        if (gistId != null) {
            val result = gistRepository.fetchGistDetail(gistId)
            result.onSuccess { remoteGist ->
                remoteUpdatedAt = remoteGist.updatedAt
                val draft = gistRepository.getDraft(gistId)
                if (draft != null && draft.ts > (remoteGist.updatedAt?.hashCode()?.toLong() ?: 0L)) {
                    title = draft.title
                    filename = draft.filename
                    content = draft.content
                    isPublic = draft.vis == "public"
                    showToast("Draft restored")
                } else {
                    title = remoteGist.description ?: ""
                    filename = remoteGist.primaryFile.filename
                    content = remoteGist.primaryFile.content ?: ""
                    isPublic = remoteGist.public
                }
                isLoadingRemote = false
            }.onFailure {
                val draft = gistRepository.getDraft(gistId)
                if (draft != null) {
                    title = draft.title
                    filename = draft.filename
                    content = draft.content
                    isPublic = draft.vis == "public"
                    showToast("Offline draft loaded")
                }
                isLoadingRemote = false
            }
        } else {
            val draft = gistRepository.getDraft(null)
            if (draft != null) {
                title = draft.title
                filename = draft.filename.ifBlank { "config.json" }
                content = draft.content
                isPublic = draft.vis == "public"
                showToast("Draft restored")
            }
            isLoadingRemote = false
        }
    }

    // Auto-save draft
    LaunchedEffect(title, filename, content, isPublic) {
        if (!isLoadingRemote && (title.isNotBlank() || content.isNotBlank())) {
            delay(1000L)
            gistRepository.saveDraft(
                gistId,
                Draft(
                    title = title,
                    filename = filename,
                    content = content,
                    vis = if (isPublic) "public" else "secret",
                    ts = System.currentTimeMillis()
                )
            )
        }
    }

    val handleSave: () -> Unit = {
        if (content.isBlank()) {
            showToast("Content cannot be empty")
        } else {
            isSaving = true
            scope.launch {
                // If editing, check for remote conflict first
                if (gistId != null) {
                    val checkResult = gistRepository.fetchGistDetail(gistId)
                    checkResult.onSuccess { latest ->
                        if (remoteUpdatedAt != null && latest.updatedAt != null && latest.updatedAt != remoteUpdatedAt) {
                            remoteConflictGist = latest
                            showConflictDialog = true
                            isSaving = false
                            return@launch
                        }
                    }
                }

                if (gistId == null) {
                    val createResult = gistRepository.createGist(
                        description = title,
                        filename = filename,
                        content = content,
                        isPublic = isPublic
                    )
                    createResult.onSuccess { newGist ->
                        showToast("Entry saved")
                        onSaved(newGist.id)
                    }.onFailure {
                        showToast("Failed to save")
                    }
                } else {
                    val updateResult = gistRepository.updateGist(
                        id = gistId,
                        description = title,
                        filename = filename,
                        content = content
                    )
                    updateResult.onSuccess { updatedGist ->
                        showToast("Entry updated")
                        onSaved(updatedGist.id)
                    }.onFailure {
                        showToast("Failed to update")
                    }
                }
                isSaving = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(id = if (gistId == null) R.string.ed_new else R.string.ed_edit),
                showBack = true,
                onBack = onBack
            ) {
                TopBarIconButton(
                    icon = Icons.Default.Save,
                    contentDescription = "Save",
                    onClick = handleSave,
                    isSpinning = isSaving
                )
            }
        },
        containerColor = colors.paper
    ) { innerPadding ->
        if (isLoadingRemote) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Title / Description
                Text(
                    text = stringResource(id = R.string.ed_label_title),
                    color = colors.inkSoft,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text(text = stringResource(id = R.string.ed_ph_title), color = colors.faint) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.card,
                        unfocusedContainerColor = colors.card,
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.line,
                        focusedTextColor = colors.ink,
                        unfocusedTextColor = colors.ink
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Filename
                Text(
                    text = stringResource(id = R.string.ed_label_file),
                    color = colors.inkSoft,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = filename,
                    onValueChange = { filename = it },
                    placeholder = { Text(text = stringResource(id = R.string.ed_ph_file), color = colors.faint) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.card,
                        unfocusedContainerColor = colors.card,
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.line,
                        focusedTextColor = colors.ink,
                        unfocusedTextColor = colors.ink
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Visibility Toggle (only selectable for new gists)
                if (gistId == null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = stringResource(id = R.string.ed_label_vis),
                        color = colors.inkSoft,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.card)
                            .padding(4.dp)
                    ) {
                        // Secret Tab
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (!isPublic) colors.primarySoft else Color.Transparent)
                                .clickable { isPublic = false }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (!isPublic) colors.primary else colors.muted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(id = R.string.ed_secret),
                                color = if (!isPublic) colors.primary else colors.muted,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Public Tab
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isPublic) colors.primarySoft else Color.Transparent)
                                .clickable { isPublic = true }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = if (isPublic) colors.primary else colors.muted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(id = R.string.ed_public),
                                color = if (isPublic) colors.primary else colors.muted,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Content editor
                Text(
                    text = stringResource(id = R.string.ed_label_content),
                    color = colors.inkSoft,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text(text = stringResource(id = R.string.ed_ph_content), color = colors.faint) },
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    ),
                    minLines = 14,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.card,
                        unfocusedContainerColor = colors.card,
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.line,
                        focusedTextColor = colors.ink,
                        unfocusedTextColor = colors.ink
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showConflictDialog && remoteConflictGist != null) {
        val remoteGist = remoteConflictGist!!
        ConflictDialog(
            onKeepMine = {
                showConflictDialog = false
                remoteUpdatedAt = remoteGist.updatedAt
                handleSave()
            },
            onUseTheirs = {
                showConflictDialog = false
                title = remoteGist.description ?: ""
                filename = remoteGist.primaryFile.filename
                content = remoteGist.primaryFile.content ?: ""
                remoteUpdatedAt = remoteGist.updatedAt
                showToast("Theirs loaded")
            },
            onCopyMine = {
                clipboardManager.setText(AnnotatedString(content))
                showToast("Content copied")
            }
        )
    }
}
