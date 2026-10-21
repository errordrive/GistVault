package com.gistvault.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gistvault.app.R
import com.gistvault.app.data.model.Gist
import com.gistvault.app.ui.theme.GistTheme
import com.gistvault.app.ui.theme.getLanguageColor
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GistCard(
    gist: Gist,
    onClick: () -> Unit,
    onToggleStar: () -> Unit = {},
    onMoreOptions: () -> Unit = {},
    onTagClick: (String) -> Unit = {}
) {
    val colors = GistTheme.colors
    val primaryFile = gist.primaryFile
    val language = primaryFile.language ?: "Text"
    val langColor = getLanguageColor(language)

    val starColor by animateColorAsState(
        targetValue = if (gist.isStarred) colors.amber else colors.faint,
        label = "starColor"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.card)
            .border(1.dp, colors.line.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // File icon tile with language color
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(langColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = langColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Main Info Column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Headline: Display Name (Name, or filename fallback)
                Text(
                    text = gist.displayTitle,
                    color = colors.ink,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (gist.displaySubtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = gist.displaySubtitle!!,
                        color = colors.muted,
                        fontSize = 12.5.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Relative date
                Text(
                    text = "Updated ${formatRelativeTime(gist.updatedAt)}",
                    color = colors.muted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Badges row: Language pill + Public/Secret pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Language badge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(colors.paper)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(langColor)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = language,
                            color = colors.inkSoft,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Visibility badge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(colors.paper)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (gist.public) Icons.Default.Public else Icons.Default.Lock,
                            contentDescription = null,
                            tint = colors.muted,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (gist.public) "Public" else "Secret",
                            color = colors.muted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Right Actions: Star and Overflow More Options
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive Star Button
                IconButton(
                    onClick = onToggleStar,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (gist.isStarred) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = "Star",
                        tint = starColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // More Options Menu Button
                IconButton(
                    onClick = onMoreOptions,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "More",
                        tint = colors.faint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

fun formatRelativeTime(dateString: String?): String {
    if (dateString.isNullOrBlank()) return "recently"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(dateString) ?: return "recently"
        val diffSec = (System.currentTimeMillis() - date.time) / 1000
        when {
            diffSec < 60 -> "just now"
            diffSec < 3600 -> "${diffSec / 60}m ago"
            diffSec < 86400 -> "${diffSec / 3600}h ago"
            diffSec < 604800 -> "${diffSec / 86400}d ago"
            diffSec < 2592000 -> "${diffSec / 604800}w ago"
            else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date)
        }
    } catch (_: Exception) {
        "recently"
    }
}
