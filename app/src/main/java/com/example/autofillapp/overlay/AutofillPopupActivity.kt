package com.example.autofillapp.overlay

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.autofillapp.accessibility.MyAccessibilityService
import com.example.autofillapp.data.AppDatabase
import com.example.autofillapp.data.ProfileRepository
import com.example.autofillapp.data.UserProfile
import com.example.autofillapp.security.CryptoManager
import com.example.autofillapp.ui.theme.AutofillAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Transparent dialog-style activity that shows autofill options.
 * Launched by OverlayManager when the floating button is tapped.
 */
class AutofillPopupActivity : ComponentActivity() {

    companion object {
        private const val TAG = "A11Y_AUTOFILL"

        /**
         * Static callback reference to the running AccessibilityService.
         * Set by MyAccessibilityService when connected.
         */
        var onItemSelected: ((String) -> Unit)? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cryptoManager = CryptoManager()
        val dao = AppDatabase.getInstance(applicationContext).userProfileDao()
        val repository = ProfileRepository(dao, cryptoManager)

        setContent {
            AutofillAppTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    AutofillPopupContent(
                        repository = repository,
                        onItemClick = { value ->
                            fillField(value)
                        },
                        onDismiss = { finish() }
                    )
                }
            }
        }
    }

    private fun fillField(value: String) {
        // Try to get the accessibility service instance and fill
        val node = MyAccessibilityService.currentFocusedNode
        if (node != null) {
            val args = android.os.Bundle().apply {
                putCharSequence(
                    android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    value
                )
            }
            val success = node.performAction(
                android.view.accessibility.AccessibilityNodeInfo.ACTION_SET_TEXT,
                args
            )
            Log.d(TAG, "Text inserted via popup: \"$value\" success=$success")

            if (!success) {
                Toast.makeText(this, "Could not insert text", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.w(TAG, "No focused node available")
            Toast.makeText(this, "No text field focused", Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}

@Composable
private fun AutofillPopupContent(
    repository: ProfileRepository,
    onItemClick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        profile = withContext(Dispatchers.IO) {
            repository.getProfileSync()
        }
        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Text(
            text = "Autofill",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (loading) {
            Text("Loading...", style = MaterialTheme.typography.bodyMedium)
        } else if (profile == null) {
            Text(
                "No profile data saved yet.\nOpen AutofillApp and add your info.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            val items = buildList {
                profile?.let { p ->
                    if (p.fullName.isNotBlank()) add(
                        AutofillItem("Full Name", p.fullName, Icons.Outlined.Person)
                    )
                    if (p.email.isNotBlank()) add(
                        AutofillItem("Email", p.email, Icons.Outlined.Email)
                    )
                    if (p.phone.isNotBlank()) add(
                        AutofillItem("Phone", p.phone, Icons.Outlined.Phone)
                    )
                    val address = buildString {
                        if (p.address.isNotBlank()) append(p.address)
                        if (p.city.isNotBlank()) {
                            if (isNotBlank()) append(", ")
                            append(p.city)
                        }
                        if (p.postalCode.isNotBlank()) {
                            if (isNotBlank()) append(" ")
                            append(p.postalCode)
                        }
                    }
                    if (address.isNotBlank()) add(
                        AutofillItem("Address", address, Icons.Outlined.Home)
                    )
                }
            }

            if (items.isEmpty()) {
                Text(
                    "Profile fields are empty.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    items.forEachIndexed { index, item ->
                        AutofillRow(item = item, onClick = { onItemClick(item.value) })
                        if (index < items.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

private data class AutofillItem(val label: String, val value: String, val icon: ImageVector)

@Composable
private fun AutofillRow(item: AutofillItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = item.value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
