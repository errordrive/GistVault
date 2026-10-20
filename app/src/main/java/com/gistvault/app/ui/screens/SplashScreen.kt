package com.gistvault.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gistvault.app.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen() {
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    // Spring entrance scale for logo
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.6f,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "logoScale"
    )

    // Fade-in alpha for logo
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "logoAlpha"
    )

    // Content slide-in & alpha
    val contentAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 200, easing = FastOutSlowInEasing),
        label = "contentAlpha"
    )

    // Breathing glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "breathingGlow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070D19),
                        Color(0xFF0A1120),
                        Color(0xFF0D172A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Subtle ambient radial light
        Box(
            modifier = Modifier
                .size(280.dp)
                .scale(glowScale)
                .alpha(glowAlpha)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF2563EB).copy(alpha = 0.35f),
                            Color(0xFF3B82F6).copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // Logo container with subtle shadow & glow
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(110.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha)
            ) {
                // Glow disc
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color(0xFF2563EB).copy(alpha = 0.25f))
                )

                // High-resolution PNG Logo
                Image(
                    painter = painterResource(id = R.drawable.logo_app),
                    contentDescription = "GistVault Logo",
                    modifier = Modifier
                        .size(82.dp)
                        .clip(RoundedCornerShape(22.dp))
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Name
            Text(
                text = "GistVault",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp,
                modifier = Modifier.alpha(contentAlpha)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle Tagline
            Text(
                text = "SECURE VAULT FOR GISTS & CODE",
                color = Color(0xFF8B9BB8),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.4.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(contentAlpha)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Three modern pulsing loader dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.alpha(contentAlpha)
            ) {
                val dot1 = rememberDotAnimation(delay = 0)
                val dot2 = rememberDotAnimation(delay = 200)
                val dot3 = rememberDotAnimation(delay = 400)

                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .scale(dot1)
                        .clip(CircleShape)
                        .background(Color(0xFF4D8DFF))
                )
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .scale(dot2)
                        .clip(CircleShape)
                        .background(Color(0xFF4D8DFF))
                )
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .scale(dot3)
                        .clip(CircleShape)
                        .background(Color(0xFF4D8DFF))
                )
            }
        }
    }
}

@Composable
private fun rememberDotAnimation(delay: Int): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "dot$delay")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, delayMillis = delay, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale$delay"
    )
    return scale
}
