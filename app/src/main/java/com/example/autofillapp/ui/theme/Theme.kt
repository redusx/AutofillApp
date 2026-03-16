package com.example.autofillapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
        darkColorScheme(
                primary = Teal60,
                onPrimary = Color.Black,
                primaryContainer = Teal20,
                onPrimaryContainer = Teal80,
                secondary = Indigo80,
                onSecondary = Color.Black,
                tertiary = Amber80,
                onTertiary = Color.Black,
                background = DarkSurface,
                onBackground = DarkOnSurface,
                surface = DarkSurfaceVariant,
                onSurface = DarkOnSurface,
                surfaceVariant = DarkSurfaceVariant,
                onSurfaceVariant = DarkOnSurface
        )

private val LightColorScheme =
        lightColorScheme(
                primary = Teal40,
                onPrimary = Color.White,
                primaryContainer = Teal80,
                onPrimaryContainer = Teal20,
                secondary = Indigo40,
                onSecondary = Color.White,
                tertiary = Amber40,
                onTertiary = Color.Black,
                background = LightSurface,
                onBackground = LightOnSurface,
                surface = Color.White,
                onSurface = LightOnSurface,
                surfaceVariant = LightSurfaceVariant,
                onSurfaceVariant = LightOnSurface
        )

@Composable
fun AutofillAppTheme(
        darkTheme: Boolean = isSystemInDarkTheme(),
        dynamicColor: Boolean = true,
        content: @Composable () -> Unit
) {
    val colorScheme =
            when {
                dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    val context = LocalContext.current
                    if (darkTheme) dynamicDarkColorScheme(context)
                    else dynamicLightColorScheme(context)
                }
                darkTheme -> DarkColorScheme
                else -> LightColorScheme
            }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
