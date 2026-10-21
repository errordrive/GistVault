package com.gistvault.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gistvault.app.R
import com.gistvault.app.data.model.DeviceCodeResponse
import com.gistvault.app.data.repository.AuthRepository
import com.gistvault.app.ui.theme.GistTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun DeviceAuthScreen(
    authRepository: AuthRepository,
    onSuccess: (token: String) -> Unit,
    onCancel: () -> Unit,
    showToast: (String) -> Unit
) {
    val colors = GistTheme.colors
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var userCode by remember { mutableStateOf("••••–••••") }
    var verificationUri by remember { mutableStateOf("https://github.com/login/device") }
    var statusText by remember { mutableStateOf("Requesting code…") }
    var isError by remember { mutableStateOf(false) }
    var remainingSeconds by remember { mutableStateOf(900) }

    // Pulsing animation for status dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    // Device Flow Launch & Polling
    LaunchedEffect(Unit) {
        val result = authRepository.requestDeviceCode()
        result.onSuccess { deviceData ->
            userCode = deviceData.userCode
            verificationUri = deviceData.verificationUri
            remainingSeconds = deviceData.expiresIn
            statusText = "Waiting for authorization…"

            val expiryTime = System.currentTimeMillis() + deviceData.expiresIn * 1000L
            var pollInterval = deviceData.interval * 1000L

            while (isActive && System.currentTimeMillis() < expiryTime) {
                delay(pollInterval)

                val pollResult = authRepository.pollAccessToken(deviceData.deviceCode)
                pollResult.onSuccess { tokenResp ->
                    if (!tokenResp.accessToken.isNullOrBlank()) {
                        statusText = "Authorized! Signing you in…"
                        delay(600)
                        authRepository.saveToken(tokenResp.accessToken)
                        onSuccess(tokenResp.accessToken)
                        return@LaunchedEffect
                    }

                    when (tokenResp.error) {
                        "authorization_pending" -> {
                            statusText = "Waiting for authorization…"
                        }
                        "slow_down" -> {
                            pollInterval += 5000L
                        }
                        "expired_token" -> {
                            statusText = "Code expired. Go back and try again."
                            isError = true
                            return@LaunchedEffect
                        }
                        "access_denied" -> {
                            statusText = "Authorization denied on GitHub."
                            isError = true
                            return@LaunchedEffect
                        }
                        else -> {
                            if (tokenResp.error != null) {
                                statusText = "Login failed. Try again."
                                isError = true
                                return@LaunchedEffect
                            }
                        }
                    }
                }
            }

            if (System.currentTimeMillis() >= expiryTime) {
                statusText = "Code expired. Go back and try again."
                isError = true
            }
        }.onFailure {
            statusText = "Connection error. Please try again."
            isError = true
        }
    }

    // Countdown Timer
    LaunchedEffect(remainingSeconds) {
        while (remainingSeconds > 0 && !isError) {
            delay(1000L)
            remainingSeconds--
        }
    }

    val timerString = String.format("%02d:%02d", remainingSeconds / 60, remainingSeconds % 60)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.paper)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = colors.card),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.device_title),
                    color = colors.ink,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(id = R.string.device_sub),
                    color = colors.muted,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Big User Code Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.paper)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = userCode,
                            color = colors.ink,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )

                        IconButton(
                            onClick = {
                                if (userCode.isNotBlank() && !userCode.contains("•")) {
                                    clipboardManager.setText(AnnotatedString(userCode))
                                    showToast("Code copied")
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy code",
                                tint = colors.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Open GitHub Button
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(verificationUri))
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            showToast("Could not open browser")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = colors.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.device_open),
                        color = colors.onPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Status row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (isError) colors.danger else colors.primary.copy(alpha = dotAlpha)
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = statusText,
                        color = if (isError) colors.danger else colors.inkSoft,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Countdown Timer
                Text(
                    text = "${stringResource(id = R.string.device_expires)} $timerString",
                    color = colors.faint,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Permission requested: 'gist' only\nNo repository access needed",
                    color = colors.muted,
                    fontSize = 11.5.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(id = R.string.device_note),
                    color = colors.faint,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = R.string.device_cancel),
                        color = colors.muted
                    )
                }
            }
        }
    }
}
