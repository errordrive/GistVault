package com.gistvault.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gistvault.app.R
import com.gistvault.app.ui.theme.GistTheme

data class ThemeItem(
    val id: String,
    val nameRes: Int,
    val color: Color? = null,
    val isGradient: Boolean = false
)

val ThemeOptions = listOf(
    ThemeItem("system", R.string.theme_system, isGradient = true),
    ThemeItem("light", R.string.theme_light, color = Color(0xFFEDF1F9)),
    ThemeItem("dark", R.string.theme_dark, color = Color(0xFF0A1120)),
    ThemeItem("pink", R.string.theme_pink, color = Color(0xFFF9A8D4))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeBottomSheet(
    currentTheme: String,
    onSelectTheme: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = GistTheme.colors

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.card,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = stringResource(id = R.string.set_theme),
                color = colors.ink,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ThemeOptions.forEach { option ->
                val isSelected = currentTheme == option.id

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onSelectTheme(option.id)
                            onDismiss()
                        }
                        .padding(vertical = 14.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Preview Dot
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .then(
                                if (option.isGradient) {
                                    Modifier.background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFFEDF1F9), Color(0xFF0A1120))
                                        )
                                    )
                                } else {
                                    Modifier.background(option.color ?: Color.Gray)
                                }
                            )
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = stringResource(id = option.nameRes),
                        color = colors.ink,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
