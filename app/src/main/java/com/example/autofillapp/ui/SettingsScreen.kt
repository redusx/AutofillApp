package com.example.autofillapp.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.autofill.AutofillManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val autofillManager = context.getSystemService(AutofillManager::class.java)
    var isAutofillEnabled by remember { mutableStateOf(false) }
    var isAccessibilityEnabled by remember { mutableStateOf(false) }
    var isOverlayGranted by remember { mutableStateOf(false) }

    // Re-check status when the screen resumes (user may have changed settings)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isAutofillEnabled = autofillManager?.hasEnabledAutofillServices() == true
                isAccessibilityEnabled = isAccessibilityServiceEnabled(context)
                isOverlayGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Settings.canDrawOverlays(context)
                } else true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val statusColor by
            animateColorAsState(
                    targetValue = if (isAutofillEnabled) Color(0xFF4CAF50) else Color(0xFFFF9800),
                    label = "statusColor"
            )

    val a11yColor by
            animateColorAsState(
                    targetValue = if (isAccessibilityEnabled) Color(0xFF4CAF50) else Color(0xFFFF9800),
                    label = "a11yColor"
            )

    val overlayColor by
            animateColorAsState(
                    targetValue = if (isOverlayGranted) Color(0xFF4CAF50) else Color(0xFFFF9800),
                    label = "overlayColor"
            )

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text(text = "Settings", fontWeight = FontWeight.Bold) },
                        colors =
                                TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                                )
                )
            }
    ) { innerPadding ->
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .padding(innerPadding)
                                .padding(horizontal = 20.dp)
                                .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── Autofill Service Status ─────────────────────────
            StatusCard(
                isEnabled = isAutofillEnabled,
                statusColor = statusColor,
                enabledTitle = "Autofill Active",
                disabledTitle = "Autofill Inactive",
                enabledDesc = "AutofillApp is your active autofill provider.",
                disabledDesc = "Enable AutofillApp to auto-fill forms in other apps."
            )

            if (!isAutofillEnabled) {
                Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                val intent =
                                        Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE).apply {
                                            data = Uri.parse("package:${context.packageName}")
                                        }
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = MaterialTheme.shapes.large,
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                )
                ) {
                    Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                            text = "Enable Autofill Service",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                    )
                }
            } else {
                OutlinedButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = MaterialTheme.shapes.large
                ) {
                    Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                            text = "Open Autofill Settings",
                            style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // ── Accessibility Service Status ────────────────────
            StatusCard(
                isEnabled = isAccessibilityEnabled,
                statusColor = a11yColor,
                enabledTitle = "Accessibility Active",
                disabledTitle = "Accessibility Inactive",
                enabledDesc = "Overlay autofill button is enabled.",
                disabledDesc = "Enable to show a floating autofill button in other apps."
            )

            if (!isAccessibilityEnabled) {
                Button(
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = MaterialTheme.shapes.large,
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                )
                ) {
                    Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                            text = "Enable Accessibility Service",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                    )
                }
            } else {
                OutlinedButton(
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = MaterialTheme.shapes.large
                ) {
                    Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                            text = "Open Accessibility Settings",
                            style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // ── Overlay Permission Status ───────────────────────
            StatusCard(
                isEnabled = isOverlayGranted,
                statusColor = overlayColor,
                enabledTitle = "Overlay Granted",
                disabledTitle = "Overlay Not Granted",
                enabledDesc = "AutofillApp can show floating buttons over other apps.",
                disabledDesc = "Grant permission to display the autofill overlay button."
            )

            if (!isOverlayGranted) {
                Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = MaterialTheme.shapes.large,
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                )
                ) {
                    Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                            text = "Grant Overlay Permission",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                    )
                }
            }

            // Instructions Card
            Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                            CardDefaults.cardColors(
                                    containerColor =
                                            MaterialTheme.colorScheme.surfaceVariant.copy(
                                                    alpha = 0.5f
                                            )
                            ),
                    shape = MaterialTheme.shapes.large
            ) {
                Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                                text = "How to Enable",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                        )
                    }

                    InstructionStep(
                            number = "1",
                            text = "Enable Autofill Service above"
                    )
                    InstructionStep(
                            number = "2",
                            text = "Enable Accessibility Service → find AutofillApp"
                    )
                    InstructionStep(
                            number = "3",
                            text = "Grant Overlay Permission"
                    )
                    InstructionStep(
                            number = "4",
                            text =
                                    "Open any app with a form — a floating button will appear near text fields"
                    )
                }
            }

            // Info footer
            Text(
                    text = "Your data stays on this device.\nNo internet connection is required.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatusCard(
    isEnabled: Boolean,
    statusColor: Color,
    enabledTitle: String,
    disabledTitle: String,
    enabledDesc: String,
    disabledDesc: String
) {
    Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                    CardDefaults.cardColors(
                            containerColor = statusColor.copy(alpha = 0.12f)
                    ),
            shape = MaterialTheme.shapes.large
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                    imageVector =
                            if (isEnabled) Icons.Filled.CheckCircle
                            else Icons.Filled.Warning,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                        text = if (isEnabled) enabledTitle else disabledTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                )
                Text(
                        text = if (isEnabled) enabledDesc else disabledDesc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun InstructionStep(number: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Card(
                colors =
                        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.size(28.dp)
        ) {
            Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
            ) {
                Text(
                        text = number,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/**
 * Check if our AccessibilityService is currently enabled in system settings.
 */
private fun isAccessibilityServiceEnabled(context: android.content.Context): Boolean {
    val serviceName = "${context.packageName}/com.example.autofillapp.accessibility.MyAccessibilityService"
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    return enabledServices.contains(serviceName, ignoreCase = true)
}

