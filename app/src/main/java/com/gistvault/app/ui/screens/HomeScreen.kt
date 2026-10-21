package com.gistvault.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gistvault.app.R
import com.gistvault.app.data.model.Gist
import com.gistvault.app.ui.components.*
import com.gistvault.app.ui.theme.GistTheme

@Composable
fun HomeScreen(
    gists: List<Gist>,
    isLoading: Boolean,
    isOffline: Boolean,
    onRefresh: () -> Unit,
    onOpenGist: (String) -> Unit,
    onNewGist: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleStar: (String) -> Unit,
    onEditGist: (String) -> Unit,
    onDeleteGist: (String) -> Unit,
    showToast: (String) -> Unit
) {
    val colors = GistTheme.colors
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") } // "all", "public", "secret", "starred"
    var selectedGistForSheet by remember { mutableStateOf<Gist?>(null) }

    // Counts
    val totalCount = gists.size
    val publicCount = remember(gists) { gists.count { it.public } }
    val secretCount = remember(gists) { gists.count { !it.public } }
    val starredCount = remember(gists) { gists.count { it.isStarred } }

    // Filtering logic
    val filteredGists = remember(gists, searchQuery, selectedFilter) {
        var list = when (selectedFilter) {
            "public" -> gists.filter { it.public }
            "secret" -> gists.filter { !it.public }
            "starred" -> gists.filter { it.isStarred }
            else -> gists
        }

        val query = searchQuery.trim().lowercase()
        if (query.isNotEmpty()) {
            if (query.startsWith("#")) {
                val tag = query.removePrefix("#")
                list = list.filter { gist -> gist.tags.any { it.contains(tag) } }
            } else {
                list = list.filter { gist ->
                    gist.displayTitle.lowercase().contains(query) ||
                    gist.primaryFile.filename.lowercase().contains(query) ||
                    (gist.description?.lowercase()?.contains(query) == true) ||
                    gist.tags.any { it.contains(query) }
                }
            }
        }
        list
    }

    Scaffold(
        topBar = {
            Column {
                TopBar(
                    title = "GistVault",
                    showLogo = true
                ) {
                    TopBarIconButton(
                        icon = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        onClick = onRefresh,
                        isSpinning = isLoading
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TopBarIconButton(
                        icon = Icons.Default.Settings,
                        contentDescription = "Settings",
                        onClick = onOpenSettings
                    )
                }

                // Offline Notice Bar
                AnimatedVisibility(visible = isOffline) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.dangerSoft)
                            .padding(vertical = 6.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Offline Mode — Cached Gists Displayed",
                            color = colors.danger,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Search Bar matching Screen 2
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        placeholder = {
                            Text(
                                text = "Search gists...",
                                color = colors.faint,
                                fontSize = 14.sp
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
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
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

                // Filter Chips Row (All 24, Public 18, Secret 6)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HomeFilterPill(
                        label = "All $totalCount",
                        isSelected = selectedFilter == "all",
                        onClick = { selectedFilter = "all" }
                    )
                    HomeFilterPill(
                        label = "Public $publicCount",
                        isSelected = selectedFilter == "public",
                        onClick = { selectedFilter = "public" }
                    )
                    HomeFilterPill(
                        label = "Secret $secretCount",
                        isSelected = selectedFilter == "secret",
                        onClick = { selectedFilter = "secret" }
                    )
                }

                // 4-Card Statistics Grid (Total, Public, Secret, Starred)
                StatsGrid(
                    total = totalCount,
                    publicCount = publicCount,
                    secretCount = secretCount,
                    starredCount = starredCount,
                    selectedFilter = selectedFilter,
                    onSelectFilter = { selectedFilter = it }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewGist,
                containerColor = colors.primary,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Gist",
                    modifier = Modifier.size(28.dp)
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
                isLoading && gists.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = colors.primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Syncing your gists...",
                            color = colors.muted,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                gists.isEmpty() -> {
                    EmptyVaultView(
                        title = "No gists yet",
                        subtitle = "Create your first gist to get started and keep your code, notes and snippets safe.",
                        actionText = "Create Your First Gist",
                        onAction = onNewGist
                    )
                }
                filteredGists.isEmpty() -> {
                    EmptyVaultView(
                        title = "No matching gists",
                        subtitle = "Try adjusting your search query or filter chips.",
                        actionText = "Clear Filters",
                        onAction = {
                            searchQuery = ""
                            selectedFilter = "all"
                        }
                    )
                }
                else -> {
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
                                onMoreOptions = { selectedGistForSheet = gist },
                                onTagClick = { tag -> searchQuery = "#$tag" }
                            )
                        }
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
fun HomeFilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = GistTheme.colors

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (isSelected) colors.primary else colors.card)
            .border(
                1.dp,
                if (isSelected) colors.primary else colors.line.copy(alpha = 0.5f),
                RoundedCornerShape(999.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
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
