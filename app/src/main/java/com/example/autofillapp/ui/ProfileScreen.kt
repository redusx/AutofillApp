package com.example.autofillapp.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.MarkunreadMailbox
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
        val uiState by viewModel.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(uiState.isSaved) {
                if (uiState.isSaved) {
                        snackbarHostState.showSnackbar("Profile saved successfully!")
                }
        }

        Scaffold(
                topBar = {
                        TopAppBar(
                                title = {
                                        Text(
                                                text =
                                                        if (uiState.hasExistingProfile)
                                                                "Edit Profile"
                                                        else "Setup Profile",
                                                fontWeight = FontWeight.Bold
                                        )
                                },
                                colors =
                                        TopAppBarDefaults.topAppBarColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                titleContentColor =
                                                        MaterialTheme.colorScheme.onPrimary
                                        )
                        )
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
                if (uiState.isLoading) {
                        Column(
                                modifier = Modifier.fillMaxSize().padding(innerPadding),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                        ) {
                                CircularProgressIndicator(
                                        modifier = Modifier.size(48.dp),
                                        color = MaterialTheme.colorScheme.primary
                                )
                        }
                } else {
                        AnimatedVisibility(
                                visible = !uiState.isLoading,
                                enter = fadeIn() + slideInVertically { it / 4 }
                        ) {
                                Column(
                                        modifier =
                                                Modifier.fillMaxSize()
                                                        .padding(innerPadding)
                                                        .padding(horizontal = 20.dp)
                                                        .verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Personal Info Section
                                        SectionHeader(text = "Personal Information")

                                        AutofillProfileField(
                                                value = uiState.profile.fullName,
                                                onValueChange = viewModel::updateFullName,
                                                label = "Full Name",
                                                icon = Icons.Outlined.Person,
                                                keyboardType = KeyboardType.Text,
                                                imeAction = ImeAction.Next,
                                                autofillTypes = listOf(AutofillType.PersonFullName)
                                        )

                                        AutofillProfileField(
                                                value = uiState.profile.firstName,
                                                onValueChange = viewModel::updateFirstName,
                                                label = "First Name",
                                                icon = Icons.Outlined.PersonOutline,
                                                keyboardType = KeyboardType.Text,
                                                imeAction = ImeAction.Next,
                                                autofillTypes = listOf(AutofillType.PersonFirstName)
                                        )

                                        AutofillProfileField(
                                                value = uiState.profile.lastName,
                                                onValueChange = viewModel::updateLastName,
                                                label = "Last Name",
                                                icon = Icons.Outlined.Badge,
                                                keyboardType = KeyboardType.Text,
                                                imeAction = ImeAction.Next,
                                                autofillTypes = listOf(AutofillType.PersonLastName)
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Contact Info Section
                                        SectionHeader(text = "Contact")

                                        AutofillProfileField(
                                                value = uiState.profile.email,
                                                onValueChange = viewModel::updateEmail,
                                                label = "Email",
                                                icon = Icons.Outlined.Email,
                                                keyboardType = KeyboardType.Email,
                                                imeAction = ImeAction.Next,
                                                autofillTypes = listOf(AutofillType.EmailAddress)
                                        )

                                        AutofillProfileField(
                                                value = uiState.profile.phone,
                                                onValueChange = viewModel::updatePhone,
                                                label = "Phone",
                                                icon = Icons.Outlined.Phone,
                                                keyboardType = KeyboardType.Phone,
                                                imeAction = ImeAction.Next,
                                                autofillTypes = listOf(AutofillType.PhoneNumber)
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Address Section
                                        SectionHeader(text = "Address")

                                        AutofillProfileField(
                                                value = uiState.profile.address,
                                                onValueChange = viewModel::updateAddress,
                                                label = "Address",
                                                icon = Icons.Outlined.Home,
                                                keyboardType = KeyboardType.Text,
                                                imeAction = ImeAction.Next,
                                                autofillTypes = listOf(AutofillType.AddressStreet)
                                        )

                                        AutofillProfileField(
                                                value = uiState.profile.city,
                                                onValueChange = viewModel::updateCity,
                                                label = "City",
                                                icon = Icons.Outlined.LocationCity,
                                                keyboardType = KeyboardType.Text,
                                                imeAction = ImeAction.Next,
                                                autofillTypes = listOf(AutofillType.AddressLocality)
                                        )

                                        AutofillProfileField(
                                                value = uiState.profile.postalCode,
                                                onValueChange = viewModel::updatePostalCode,
                                                label = "Postal Code",
                                                icon = Icons.Outlined.MarkunreadMailbox,
                                                keyboardType = KeyboardType.Number,
                                                imeAction = ImeAction.Next,
                                                autofillTypes = listOf(AutofillType.PostalCode)
                                        )

                                        AutofillProfileField(
                                                value = uiState.profile.country,
                                                onValueChange = viewModel::updateCountry,
                                                label = "Country",
                                                icon = Icons.Outlined.Public,
                                                keyboardType = KeyboardType.Text,
                                                imeAction = ImeAction.Done,
                                                autofillTypes = listOf(AutofillType.AddressCountry)
                                        )

                                        Spacer(modifier = Modifier.height(20.dp))

                                        // Save Button
                                        Button(
                                                onClick = viewModel::saveProfile,
                                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                                shape = MaterialTheme.shapes.large,
                                                colors =
                                                        ButtonDefaults.buttonColors(
                                                                containerColor =
                                                                        MaterialTheme.colorScheme
                                                                                .primary
                                                        )
                                        ) {
                                                Text(
                                                        text = "Save Profile",
                                                        style =
                                                                MaterialTheme.typography
                                                                        .titleMedium,
                                                        fontWeight = FontWeight.Bold
                                                )
                                        }

                                        Spacer(modifier = Modifier.height(24.dp))
                                }
                        }
                }
        }
}

@Composable
private fun SectionHeader(text: String) {
        Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                        CardDefaults.cardColors(
                                containerColor =
                                        MaterialTheme.colorScheme.primaryContainer.copy(
                                                alpha = 0.3f
                                        )
                        ),
                shape = MaterialTheme.shapes.medium
        ) {
                Text(
                        text = text,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
        }
}

/**
 * Profile text field with Compose Autofill integration.
 *
 * Uses AutofillNode + onGloballyPositioned + onFocusChanged to register the field with the Android
 * Autofill framework, so the system can trigger autofill on this field.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun AutofillProfileField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        icon: ImageVector,
        keyboardType: KeyboardType,
        imeAction: ImeAction,
        autofillTypes: List<AutofillType>
) {
        val autofill = LocalAutofill.current
        val autofillTree = LocalAutofillTree.current

        // Create an AutofillNode that the system can target
        val autofillNode = remember {
                AutofillNode(
                        autofillTypes = autofillTypes,
                        onFill = { filledValue -> onValueChange(filledValue) }
                )
        }

        // Register the node in the autofill tree
        autofillTree += autofillNode

        OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label) },
                leadingIcon = {
                        Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = MaterialTheme.colorScheme.primary
                        )
                },
                modifier =
                        Modifier.fillMaxWidth()
                                .semantics { contentDescription = label }
                                .onGloballyPositioned { coordinates ->
                                        // Update the AutofillNode bounds so the system knows where
                                        // to show the popup
                                        autofillNode.boundingBox = coordinates.boundsInWindow()
                                }
                                .onFocusChanged { focusState ->
                                        // Request/cancel autofill based on focus
                                        autofill?.run {
                                                if (focusState.isFocused) {
                                                        requestAutofillForNode(autofillNode)
                                                } else {
                                                        cancelAutofillForNode(autofillNode)
                                                }
                                        }
                                },
                singleLine = true,
                keyboardOptions =
                        KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
                shape = MaterialTheme.shapes.medium
        )
}
