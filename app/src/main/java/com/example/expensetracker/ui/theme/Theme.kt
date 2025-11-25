package com.example.expensetracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class CustomColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color
)

val LocalCustomColors = staticCompositionLocalOf {
    CustomColors(
        success = Color.Unspecified,
        onSuccess = Color.Unspecified,
        successContainer = Color.Unspecified
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = Emerald80,
    onPrimary = Color(0xFF003828),
    primaryContainer = Color(0xFF00513C),
    onPrimaryContainer = Emerald90,

    secondary = Slate80,
    onSecondary = Color(0xFF1C3438),

    tertiary = Amber80,

    background = DarkBackground,
    surface = DarkSurface,

    error = RedExpenseDark,
    onError = Color(0xFF690005)
)

private val LightColorScheme = lightColorScheme(
    primary = Emerald40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF82F8CD),
    onPrimaryContainer = Color(0xFF002116),

    secondary = Slate40,
    onSecondary = Color.White,

    tertiary = Amber40,

    background = LightBackground,
    surface = LightSurface,

    error = RedExpenseLight,
    onError = Color.White
)

@Composable
fun ExpenseTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val customColors = if (darkTheme) {
        CustomColors(
            success = GreenIncomeDark,
            onSuccess = Color(0xFF00390A),
            successContainer = Color(0xFF005313)
        )
    } else {
        CustomColors(
            success = GreenIncomeLight,
            onSuccess = Color.White,
            successContainer = Color(0xFFC8E6C9)
        )
    }

    CompositionLocalProvider(LocalCustomColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MaterialTheme.typography,
            content = content
        )
    }
}

val MaterialTheme.customColors: CustomColors
    @Composable
    @ReadOnlyComposable
    get() = LocalCustomColors.current