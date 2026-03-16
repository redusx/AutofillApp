package com.example.autofillapp.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String, val label: String) {
    data object Profile : Screen("profile", "Profile")
    data object Settings : Screen("settings", "Settings")
}

@Composable
fun MainNavigation(viewModel: ProfileViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
            bottomBar = {
                NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp
                ) {
                    val isProfileSelected =
                            currentDestination?.hierarchy?.any {
                                it.route == Screen.Profile.route
                            } == true
                    val isSettingsSelected =
                            currentDestination?.hierarchy?.any {
                                it.route == Screen.Settings.route
                            } == true

                    NavigationBarItem(
                            selected = isProfileSelected,
                            onClick = {
                                navController.navigate(Screen.Profile.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                        imageVector =
                                                if (isProfileSelected) Icons.Filled.Person
                                                else Icons.Outlined.Person,
                                        contentDescription = "Profile"
                                )
                            },
                            label = {
                                Text(
                                        text = "Profile",
                                        fontWeight =
                                                if (isProfileSelected) FontWeight.Bold
                                                else FontWeight.Normal
                                )
                            },
                            colors =
                                    NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor =
                                                    MaterialTheme.colorScheme.primaryContainer.copy(
                                                            alpha = 0.5f
                                                    )
                                    )
                    )

                    NavigationBarItem(
                            selected = isSettingsSelected,
                            onClick = {
                                navController.navigate(Screen.Settings.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                        imageVector =
                                                if (isSettingsSelected) Icons.Filled.Settings
                                                else Icons.Outlined.Settings,
                                        contentDescription = "Settings"
                                )
                            },
                            label = {
                                Text(
                                        text = "Settings",
                                        fontWeight =
                                                if (isSettingsSelected) FontWeight.Bold
                                                else FontWeight.Normal
                                )
                            },
                            colors =
                                    NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor =
                                                    MaterialTheme.colorScheme.primaryContainer.copy(
                                                            alpha = 0.5f
                                                    )
                                    )
                    )
                }
            }
    ) { innerPadding ->
        NavHost(
                navController = navController,
                startDestination = Screen.Profile.route,
                modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Profile.route) { ProfileScreen(viewModel = viewModel) }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}
