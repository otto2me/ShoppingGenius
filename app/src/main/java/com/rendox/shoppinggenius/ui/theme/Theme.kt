package com.rendox.shoppinggenius.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import com.rendox.shoppinggenius.model.DEFAULT_USER_PREFERENCES
import com.rendox.shoppinggenius.model.ShoppingGeniusColorScheme
import com.rendox.shoppinggenius.ui.theme.colorschemes.BeigeDarkColorScheme
import com.rendox.shoppinggenius.ui.theme.colorschemes.BeigeLightColorScheme
import com.rendox.shoppinggenius.ui.theme.colorschemes.CyanDarkColorScheme
import com.rendox.shoppinggenius.ui.theme.colorschemes.CyanLightColorScheme
import com.rendox.shoppinggenius.ui.theme.colorschemes.GreenDarkColorScheme
import com.rendox.shoppinggenius.ui.theme.colorschemes.GreenLightColorScheme
import com.rendox.shoppinggenius.ui.theme.colorschemes.PinkDarkColorScheme
import com.rendox.shoppinggenius.ui.theme.colorschemes.PinkLightColorScheme
import com.rendox.shoppinggenius.ui.theme.colorschemes.PurpleDarkColorScheme
import com.rendox.shoppinggenius.ui.theme.colorschemes.PurpleLightColorScheme
import com.rendox.shoppinggenius.ui.theme.colorschemes.YellowDarkColorScheme
import com.rendox.shoppinggenius.ui.theme.colorschemes.YellowLightColorScheme

@Composable
fun ShoppingGeniusTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    disableDynamicColor: Boolean = true,
    requestedColorScheme: ShoppingGeniusColorScheme? = null,
    content: @Composable () -> Unit
) {
    val dynamicColor = dynamicColorIsSupported && !disableDynamicColor
    val extendedColors =
        if (useDarkTheme) DarkExtendedColors else LightExtendedColors
    val dynamicColorScheme = when {
        dynamicColor && useDarkTheme -> dynamicDarkColorScheme(LocalContext.current)
        dynamicColor && !useDarkTheme ->
            dynamicLightColorScheme(LocalContext.current)

        else -> null
    }
    val staticColorScheme = requestedColorScheme ?: DEFAULT_USER_PREFERENCES.selectedTheme
    val resultingColorScheme =
        dynamicColorScheme ?: staticColorScheme.deriveColorScheme(useDarkTheme)

    CompositionLocalProvider(
        LocalExtendedColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = resultingColorScheme,
            content = content
        )
    }
}

@Composable
fun ShoppingGeniusColorScheme.deriveColorScheme(useDarkTheme: Boolean): ColorScheme = when (this) {
    ShoppingGeniusColorScheme.BeigeColorScheme -> {
        if (useDarkTheme) {
            BeigeDarkColorScheme
        } else {
            BeigeLightColorScheme
        }
    }

    ShoppingGeniusColorScheme.CyanColorScheme -> {
        if (useDarkTheme) {
            CyanDarkColorScheme
        } else {
            CyanLightColorScheme
        }
    }

    ShoppingGeniusColorScheme.GreenColorScheme -> {
        if (useDarkTheme) {
            GreenDarkColorScheme
        } else {
            GreenLightColorScheme
        }
    }

    ShoppingGeniusColorScheme.PinkColorScheme -> {
        if (useDarkTheme) {
            PinkDarkColorScheme
        } else {
            PinkLightColorScheme
        }
    }

    ShoppingGeniusColorScheme.PurpleColorScheme -> {
        if (useDarkTheme) {
            PurpleDarkColorScheme
        } else {
            PurpleLightColorScheme
        }
    }

    ShoppingGeniusColorScheme.YellowColorScheme -> {
        if (useDarkTheme) {
            YellowDarkColorScheme
        } else {
            YellowLightColorScheme
        }
    }
}

val dynamicColorIsSupported: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
