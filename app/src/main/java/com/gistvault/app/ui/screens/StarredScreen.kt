package com.gistvault.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gistvault.app.data.model.Gist
import com.gistvault.app.ui.components.EmptyVaultView
import com.gistvault.app.ui.components.GistActionsSheet
import com.gistvault.app.ui.components.GistCard
import com.gistvault.app.ui.components.TopBar
import com.gistvault.app.ui.theme.GistTheme

@Composable
fun StarredScreen(
    gists: List<Gist>,
    onOpenGist: (String) -> Unit,
    onToggleStar: (String) -> Unit,
    onEditGist: (String) -> Unit,
    onDeleteGist: (String) -> Unit,
    showToast: (String) -> Unit
) {
    val colors = GistTheme.colors
    val context = LocalContext.current
    val starredGists = remember(gists) { gists.filter { it.isStarred } }
    var selectedGistForSheet by remember { mutableStateOf<Gist?>(null) }

    Scaffold(
        topBar = {
            TopBar(
                title = "Starred Gists",
                subtitle = "${starredGists.size} starred"
            )
        },
        containerColor = colors.paper
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (starredGists.isEmpty()) {
                EmptyVaultView(
                    title = "No starred gists",
                    subtitle = "Tap the star icon on any gist to quickly access your favorite snippets here.",
                    actionText = null,
                    onAction = null
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(starredGists, key = { it.id }) { gist ->
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
