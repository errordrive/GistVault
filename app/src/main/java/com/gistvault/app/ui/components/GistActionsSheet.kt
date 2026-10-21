package com.gistvault.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gistvault.app.R
import com.gistvault.app.data.model.Gist
import com.gistvault.app.ui.theme.GistTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GistActionsSheet(
    gist: Gist,
    onCopyContent: () -> Unit,
    onShareGist: () -> Unit,
    onOpenInGitHub: () -> Unit,
    onEditGist: () -> Unit,
    onDeleteGist: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = GistTheme.colors

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.card,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 38.dp, height = 4.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(colors.line)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header showing filename
            Text(
                text = gist.displayTitle,
                color = colors.ink,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            HorizontalDivider(color = colors.line.copy(alpha = 0.5f), thickness = 1.dp)

            Spacer(modifier = Modifier.height(8.dp))

            ActionSheetRow(
                icon = Icons.Default.ContentCopy,
                title = "Copy Content",
                onClick = {
                    onDismiss()
                    onCopyContent()
                }
            )

            ActionSheetRow(
                icon = Icons.Default.Share,
                title = "Share Gist",
                onClick = {
                    onDismiss()
                    onShareGist()
                }
            )

            ActionSheetRow(
                icon = Icons.Default.OpenInBrowser,
                title = "Open in GitHub",
                onClick = {
                    onDismiss()
                    onOpenInGitHub()
                }
            )

            ActionSheetRow(
                icon = Icons.Default.Edit,
                title = "Edit Gist",
                onClick = {
                    onDismiss()
                    onEditGist()
                }
            )

            ActionSheetRow(
                icon = Icons.Default.Delete,
                title = "Delete Gist",
                tint = colors.danger,
                onClick = {
                    onDismiss()
                    onDeleteGist()
                }
            )
        }
    }
}

@Composable
private fun ActionSheetRow(
    icon: ImageVector,
    title: String,
    tint: Color? = null,
    onClick: () -> Unit
) {
    val colors = GistTheme.colors
    val itemColor = tint ?: colors.ink

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = itemColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            color = itemColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
