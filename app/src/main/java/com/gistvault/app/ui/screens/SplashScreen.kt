package com.gistvault.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gistvault.app.R

@Composable
fun SplashScreen() {
    val transition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF132247),
                        Color(0xFF0A1329),
                        Color(0xFF090E17)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glowing 3D Squircle Logo matching Screen 1
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(112.dp)
                    .scale(pulseScale)
            ) {
                // Outer glow border disc
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color(0xFF2563EB).copy(alpha = 0.25f))
                        .border(1.5.dp, Color(0xFF388BFD).copy(alpha = 0.6f), RoundedCornerShape(32.dp))
                )

                // High-resolution App Logo
                Image(
                    painter = painterResource(id = R.drawable.logo_app),
                    contentDescription = "GistVault Logo",
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(28.dp))
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Two-tone Brand Wordmark matching Screen 1: "Gist" (White) + "Vault" (Electric Blue)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Gist",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Vault",
                    color = Color(0xFF2563EB),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle Tagline matching Screen 1: "Your GitHub Gists Safely Stored"
            Text(
                text = "Your GitHub Gists Safely Stored",
                color = Color(0xFF8B949E),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(56.dp))

            // Minimal circular spinner matching Screen 1
            CircularProgressIndicator(
                color = Color(0xFF2563EB),
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
