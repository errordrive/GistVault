package com.gistvault.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gistvault.app.R
import com.gistvault.app.data.model.Gist
import com.gistvault.app.ui.components.GistCard
import com.gistvault.app.ui.components.TopBar
import com.gistvault.app.ui.components.TopBarIconButton
import com.gistvault.app.ui.theme.GistTheme

@Composable
fun HomeScreen(
    gists: List<Gist>,
    isLoading: Boolean,
    isOffline: Boolean,
    onRefresh: () -> Unit,
    onOpenGist: (String) -> Unit,
    onNewGist: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val colors = GistTheme.colors
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") } // "all", "secret", "public"

    // Extract all unique tags across all gists
    val allTags = remember(gists) {
        gists.flatMap { it.tags }.distinct().take(12)
    }

    // Filtering by text, filter chip, or #tag
    val filteredGists = remember(gists, searchQuery, selectedFilter) {
        var list = gists
        if (selectedFilter == "secret") {
            list = list.filter { !it.public }
        } else if (selectedFilter == "public") {
            list = list.filter { it.public }
        }

        val query = searchQuery.trim().lowercase()
        if (query.isEmpty()) {
            list
        } else if (query.startsWith("#")) {
            val tag = query.removePrefix("#")
            list.filter { gist -> gist.tags.any { it.contains(tag) } }
        } else {
            list.filter { gist ->
                gist.displayTitle.lowercase().contains(query) ||
                gist.primaryFile.filename.lowercase().contains(query) ||
                gist.tags.any { it.contains(query) }
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopBar(
                    title = "GistVault",
                    subtitle = if (gists.isEmpty()) {
                        stringResource(id = R.string.home_empty)
                    } else {
                        "${gists.size} ${stringResource(id = if (gists.size == 1) R.string.home_entry else R.string.home_entries)}"
                    },
                    showLogo = true
                ) {
                    TopBarIconButton(
                        icon = Icons.Default.Refresh,
                        contentDescription = stringResource(id = R.string.home_refresh),
                        onClick = onRefresh,
                        isSpinning = isLoading
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TopBarIconButton(
                        icon = Icons.Default.Settings,
                        contentDescription = stringResource(id = R.string.home_settings),
                        onClick = onOpenSettings
                    )
                }

                // Offline Bar Banner
                AnimatedVisibility(visible = isOffline) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.dangerSoft)
                            .padding(vertical = 7.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.offline_bar),
                            color = colors.danger,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Search Bar with pill shape
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.paper)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = stringResource(id = R.string.home_search),
                                color = colors.faint,
                                fontSize = 13.5.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = colors.muted,
                                modifier = Modifier.size(19.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = colors.muted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(999.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.card,
                            unfocusedContainerColor = colors.card,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.line.copy(alpha = 0.7f),
                            focusedTextColor = colors.ink,
                            unfocusedTextColor = colors.ink
                        ),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    )
                }

                // Filter & Tag Chips Bar
                if (gists.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.paper)
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterPill(
                            label = "All",
                            isSelected = selectedFilter == "all",
                            onClick = { selectedFilter = "all" }
                        )
                        FilterPill(
                            label = "Secret",
                            isSelected = selectedFilter == "secret",
                            onClick = { selectedFilter = "secret" }
                        )
                        FilterPill(
                            label = "Public",
                            isSelected = selectedFilter == "public",
                            onClick = { selectedFilter = "public" }
                        )

                        allTags.forEach { tag ->
                            val isTagActive = searchQuery == "#$tag"
                            FilterPill(
                                label = "#$tag",
                                isSelected = isTagActive,
                                onClick = {
                                    searchQuery = if (isTagActive) "" else "#$tag"
                                }
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewGist,
                containerColor = colors.primary,
                contentColor = colors.onPrimary,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.home_new),
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
                            modifier = Modifier.size(38.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = stringResource(id = R.string.home_syncing),
                            color = colors.muted,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                gists.isEmpty() && isOffline -> {
                    EmptyStateView(
                        title = stringResource(id = R.string.home_no_conn),
                        subtitle = stringResource(id = R.string.home_no_conn_sub)
                    )
                }
                gists.isEmpty() -> {
                    EmptyStateView(
                        title = stringResource(id = R.string.home_empty_title),
                        subtitle = stringResource(id = R.string.home_empty_sub)
                    )
                }
                filteredGists.isEmpty() -> {
                    EmptyStateView(
                        title = stringResource(id = R.string.home_no_match),
                        subtitle = stringResource(id = R.string.home_no_match_sub)
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
                                onTagClick = { tag -> searchQuery = "#$tag" }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterPill(
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
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) colors.onPrimary else colors.inkSoft,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun EmptyStateView(
    title: String,
    subtitle: String
) {
    val colors = GistTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(colors.primarySoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = title,
            color = colors.ink,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = subtitle,
            color = colors.muted,
            fontSize = 13.5.sp,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp
        )
    }
}
