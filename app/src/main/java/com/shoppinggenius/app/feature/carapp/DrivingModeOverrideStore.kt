package com.shoppinggenius.app.feature.carapp

import android.content.Context
import androidx.core.content.edit

object DrivingModeOverrideStore {

    private const val PREFS_NAME = "driving_mode_override"
    private const val KEY_MODE = "mode"

    enum class Mode {
        AUTO,
        PARKED,
        DRIVING
    }

    fun get(context: Context): Mode {
        val rawValue = context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_MODE, Mode.AUTO.name)
            ?.trim()
            ?.uppercase()

        return runCatching { Mode.valueOf(rawValue.orEmpty()) }
            .getOrDefault(Mode.AUTO)
    }

    fun set(context: Context, mode: Mode) {
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putString(KEY_MODE, mode.name) }
    }

    fun clear(context: Context) {
        set(context, Mode.AUTO)
    }
}

