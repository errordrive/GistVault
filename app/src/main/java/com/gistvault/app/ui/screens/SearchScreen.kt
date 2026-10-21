package com.gistvault.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gistvault.app.data.model.Gist
import com.gistvault.app.ui.components.EmptyVaultView
import com.gistvault.app.ui.components.GistActionsSheet
import com.gistvault.app.ui.components.GistCard
import com.gistvault.app.ui.theme.GistTheme

@Composable
fun SearchScreen(
    gists: List<Gist>,
    onOpenGist: (String) -> Unit,
    onToggleStar: (String) -> Unit,
    onEditGist: (String) -> Unit,
    onDeleteGist: (String) -> Unit,
    onBack: () -> Unit,
    showToast: (String) -> Unit
) {
    val colors = GistTheme.colors
    val context = LocalContext.current

    var query by remember { mutableStateOf("") }
    var typeFilter by remember { mutableStateOf("All") } // "All", "Code", "Text", "Markdown"
    var selectedGistForSheet by remember { mutableStateOf<Gist?>(null) }

    val filteredGists = remember(gists, query, typeFilter) {
        var list = gists

        // Type filter
        when (typeFilter) {
            "Code" -> list = list.filter {
                val lang = it.primaryFile.language?.lowercase() ?: ""
                lang != "markdown" && lang != "text" && lang.isNotBlank()
            }
            "Text" -> list = list.filter {
                val lang = it.primaryFile.language?.lowercase() ?: ""
                lang == "text" || it.primaryFile.filename.endsWith(".txt")
            }
            "Markdown" -> list = list.filter {
                val lang = it.primaryFile.language?.lowercase() ?: ""
                lang == "markdown" || it.primaryFile.filename.endsWith(".md")
            }
        }

        val q = query.trim().lowercase()
        if (q.isNotEmpty()) {
            list = list.filter { gist ->
                gist.displayTitle.lowercase().contains(q) ||
                gist.primaryFile.filename.lowercase().contains(q) ||
                (gist.description?.lowercase()?.contains(q) == true) ||
                gist.tags.any { it.contains(q) }
            }
        }
        list
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.paper)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Top Search Bar with back arrow
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.ink,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        placeholder = {
                            Text(
                                text = "Search...",
                                color = colors.faint,
                                fontSize = 14.5.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = colors.muted,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = colors.faint,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
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
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filter chips: All, Code, Text, Markdown
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Code", "Text", "Markdown").forEach { type ->
                        SearchTypeChip(
                            label = type,
                            isSelected = typeFilter == type,
                            onClick = { typeFilter = type }
                        )
                    }
                }
            }
        },
        containerColor = colors.paper
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (filteredGists.isEmpty()) {
                EmptyVaultView(
                    title = "No results found",
                    subtitle = "No gists matched your search query '$query'.",
                    actionText = "Clear Search",
                    onAction = {
                        query = ""
                        typeFilter = "All"
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredGists, key = { it.id }) { gist ->
                        GistCard(
                            gist = gist,
                            onClick = { onOpenGist(gist.id) },
                            onToggleStar = { onToggleStar(gist.id) },
                            onMoreOptions = { selectedGistForSheet = gist }
                        )
                    }
                }
            }
        }
    }

    // More Options Bottom Sheet
    selectedGistForSheet?.let { gist ->
        GistActionsSheet(
            gist = gist,
            onCopyContent = {
                val content = gist.primaryFile.content ?: ""
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Gist Content", content))
                showToast("Content copied to clipboard")
            },
            onShareGist = {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, gist.displayTitle)
                    putExtra(Intent.EXTRA_TEXT, "https://gist.github.com/${gist.id}")
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Gist"))
            },
            onOpenInGitHub = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://gist.github.com/${gist.id}"))
                context.startActivity(intent)
            },
            onEditGist = { onEditGist(gist.id) },
            onDeleteGist = { onDeleteGist(gist.id) },
            onDismiss = { selectedGistForSheet = null }
        )
    }
}

@Composable
private fun SearchTypeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = GistTheme.colors

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (isSelected) colors.primary else colors.card)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else colors.inkSoft,
            fontSize = 12.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
