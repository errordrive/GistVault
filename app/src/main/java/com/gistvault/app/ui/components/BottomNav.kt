package com.gistvault.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gistvault.app.navigation.Screen
import com.gistvault.app.ui.theme.GistTheme

@Composable
fun GistBottomBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    val colors = GistTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.card)
    ) {
        HorizontalDivider(
            color = colors.line.copy(alpha = 0.6f),
            thickness = 1.dp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(60.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                modifier = Modifier.weight(1f),
                label = "Home",
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                isSelected = currentScreen is Screen.Home,
                onClick = { onNavigate(Screen.Home) }
            )

            BottomNavItem(
                modifier = Modifier.weight(1f),
                label = "Search",
                selectedIcon = Icons.Filled.Search,
                unselectedIcon = Icons.Outlined.Search,
                isSelected = currentScreen is Screen.Search,
                onClick = { onNavigate(Screen.Search) }
            )

            BottomNavItem(
                modifier = Modifier.weight(1f),
                label = "Starred",
                selectedIcon = Icons.Filled.Star,
                unselectedIcon = Icons.Default.StarBorder,
                isSelected = currentScreen is Screen.Starred,
                onClick = { onNavigate(Screen.Starred) }
            )

            BottomNavItem(
                modifier = Modifier.weight(1f),
                label = "Settings",
                selectedIcon = Icons.Filled.Settings,
                unselectedIcon = Icons.Outlined.Settings,
                isSelected = currentScreen is Screen.Settings,
                onClick = { onNavigate(Screen.Settings) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    modifier: Modifier = Modifier,
    label: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = GistTheme.colors
    val contentColor = if (isSelected) colors.primary else colors.faint

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSelected) selectedIcon else unselectedIcon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            color = contentColor,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
