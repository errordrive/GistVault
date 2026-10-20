package com.gistvault.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.gistvault.app.R
import com.gistvault.app.ui.theme.GistTheme

@Composable
fun LoginScreen(
    onLoginWithGitHub: () -> Unit,
    onConnectToken: (String) -> Unit,
    isLoading: Boolean,
    onSetupCustomClientId: (String) -> Unit,
    currentClientId: String
) {
    val colors = GistTheme.colors
    var tokenText by remember { mutableStateOf("") }
    var isTokenVisible by remember { mutableStateOf(false) }
    var isTokenDetailsOpen by remember { mutableStateOf(false) }
    var showSetupDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.paper)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main Card Container with refined border
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.line.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo with glowing container
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(colors.primarySoft)
                            .border(1.dp, colors.line.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_app),
                            contentDescription = "GistVault Logo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(18.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "GistVault",
                        color = colors.ink,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = stringResource(id = R.string.login_sub),
                        color = colors.muted,
                        fontSize = 13.5.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(26.dp))

                    // Login with GitHub Button (One-click device flow)
                    Button(
                        onClick = onLoginWithGitHub,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (colors.isDark) Color(0xFF24292F) else Color(0xFF1F2328)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(id = R.string.login_github),
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = colors.line.copy(alpha = 0.7f)
                        )
                        Text(
                            text = stringResource(id = R.string.login_or),
                            color = colors.faint,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = colors.line.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Personal Access Token expandable toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isTokenDetailsOpen) colors.primarySoft.copy(alpha = 0.5f) else Color.Transparent)
                            .clickable { isTokenDetailsOpen = !isTokenDetailsOpen }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.login_token_toggle),
                                color = colors.primary,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    AnimatedVisibility(
                        visible = isTokenDetailsOpen,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.login_token_label),
                                color = colors.inkSoft,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = tokenText,
                                onValueChange = { tokenText = it },
                                placeholder = {
                                    Text(
                                        text = "ghp_••••••••••••••••",
                                        color = colors.faint,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 13.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = null,
                                        tint = colors.muted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { isTokenVisible = !isTokenVisible }) {
                                        Icon(
                                            imageVector = if (isTokenVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = colors.muted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                visualTransformation = if (isTokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    if (tokenText.isNotBlank()) onConnectToken(tokenText)
                                }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.primary,
                                    unfocusedBorderColor = colors.line,
                                    focusedTextColor = colors.ink,
                                    unfocusedTextColor = colors.ink,
                                    focusedContainerColor = colors.paper.copy(alpha = 0.5f),
                                    unfocusedContainerColor = colors.paper.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Privacy badge reassurance: Only Gist scope!
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.successSoft.copy(alpha = 0.7f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = colors.success,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Requires ONLY 'gist' permission. No repo or workflow access needed.",
                                    color = colors.success,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 15.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Steps guide Card
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = colors.paper),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = stringResource(id = R.string.login_steps_title),
                                        color = colors.ink,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    StepRow(number = "1", text = stringResource(id = R.string.login_step1))
                                    StepRow(number = "2", text = stringResource(id = R.string.login_step2))
                                    StepRow(number = "3", text = "Tick ONLY the 'gist' checkbox (no repo access needed)")
                                    StepRow(number = "4", text = "Copy token, paste above, and connect")
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { onConnectToken(tokenText) },
                                enabled = !isLoading && tokenText.isNotBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = colors.onPrimary,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = stringResource(id = R.string.login_working))
                                } else {
                                    Text(
                                        text = stringResource(id = R.string.login_connect),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = stringResource(id = R.string.login_cred_note),
                        color = colors.faint,
                        fontSize = 11.5.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    if (showSetupDialog) {
        SetupClientIdDialog(
            initialId = currentClientId,
            onSave = {
                onSetupCustomClientId(it)
                showSetupDialog = false
            },
            onDismiss = { showSetupDialog = false }
        )
    }
}

@Composable
fun StepRow(number: String, text: String) {
    val colors = GistTheme.colors

    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(colors.primarySoft, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = colors.primary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = colors.muted,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
    }
}

@Composable
fun SetupClientIdDialog(
    initialId: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = GistTheme.colors
    var clientId by remember { mutableStateOf(initialId) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = colors.card),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(id = R.string.setup_title),
                    color = colors.ink,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(id = R.string.setup_sub),
                    color = colors.muted,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                StepRow("1", stringResource(id = R.string.setup_s1))
                StepRow("2", stringResource(id = R.string.setup_s2))
                StepRow("3", stringResource(id = R.string.setup_s3))
                StepRow("4", stringResource(id = R.string.setup_s4))

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = stringResource(id = R.string.setup_label),
                    color = colors.inkSoft,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = clientId,
                    onValueChange = { clientId = it },
                    placeholder = { Text(text = stringResource(id = R.string.setup_ph), color = colors.faint) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.line,
                        focusedTextColor = colors.ink,
                        unfocusedTextColor = colors.ink
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = stringResource(id = R.string.setup_cancel), color = colors.muted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(clientId.trim()) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                    ) {
                        Text(text = stringResource(id = R.string.setup_save), color = colors.onPrimary)
                    }
                }
            }
        }
    }
}
