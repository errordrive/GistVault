package com.gistvault.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gistvault.app.ui.theme.GistTheme

@Composable
fun StatsGrid(
    total: Int,
    publicCount: Int,
    secretCount: Int,
    starredCount: Int,
    selectedFilter: String,
    onSelectFilter: (String) -> Unit
) {
    val colors = GistTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatItem(
            modifier = Modifier.weight(1f),
            number = total,
            label = "Total",
            numColor = colors.primary,
            isSelected = selectedFilter == "all",
            onClick = { onSelectFilter("all") }
        )

        StatItem(
            modifier = Modifier.weight(1f),
            number = publicCount,
            label = "Public",
            numColor = colors.success,
            isSelected = selectedFilter == "public",
            onClick = { onSelectFilter("public") }
        )

        StatItem(
            modifier = Modifier.weight(1f),
            number = secretCount,
            label = "Secret",
            numColor = colors.danger,
            isSelected = selectedFilter == "secret",
            onClick = { onSelectFilter("secret") }
        )

        StatItem(
            modifier = Modifier.weight(1f),
            number = starredCount,
            label = "Starred",
            numColor = colors.amber,
            isSelected = selectedFilter == "starred",
            onClick = { onSelectFilter("starred") }
        )
    }
}

@Composable
private fun StatItem(
    modifier: Modifier = Modifier,
    number: Int,
    label: String,
    numColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = GistTheme.colors
    val borderColor = if (isSelected) numColor.copy(alpha = 0.8f) else colors.line.copy(alpha = 0.5f)
    val bgColor = if (isSelected) numColor.copy(alpha = 0.12f) else colors.card

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "$number",
            color = numColor,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = colors.muted,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
