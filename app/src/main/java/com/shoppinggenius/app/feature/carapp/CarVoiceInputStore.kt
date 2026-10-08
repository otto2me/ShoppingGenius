package com.shoppinggenius.app.feature.carapp

import android.content.Context
import androidx.core.content.edit
import java.util.UUID

internal object CarVoiceInputStore {
    private const val PREFS_NAME = "car_voice_input_store"
    private const val KEY_REQUEST_ID = "request_id"
    private const val KEY_STATUS = "status"
    private const val KEY_TEXT = "text"

    private enum class Status {
        IDLE,
        PENDING,
        SUCCESS,
        CANCELLED
    }

    sealed interface Result {
        data class Success(val text: String) : Result
        data object Cancelled : Result
    }

    fun start(context: Context): String {
        val requestId = UUID.randomUUID().toString()
        prefs(context).edit {
            putString(KEY_REQUEST_ID, requestId)
            putString(KEY_STATUS, Status.PENDING.name)
            remove(KEY_TEXT)
        }
        return requestId
    }

    fun completeSuccess(context: Context, requestId: String, text: String) {
        prefs(context).edit {
            putString(KEY_REQUEST_ID, requestId)
            putString(KEY_STATUS, Status.SUCCESS.name)
            putString(KEY_TEXT, text)
        }
    }

    fun completeCancelled(context: Context, requestId: String) {
        prefs(context).edit {
            putString(KEY_REQUEST_ID, requestId)
            putString(KEY_STATUS, Status.CANCELLED.name)
            remove(KEY_TEXT)
        }
    }

    fun consumeCompleted(context: Context, requestId: String): Result? {
        val prefs = prefs(context)
        val storedRequestId = prefs.getString(KEY_REQUEST_ID, null) ?: return null
        if (storedRequestId != requestId) return null

        return when (prefs.getString(KEY_STATUS, Status.IDLE.name)) {
            Status.SUCCESS.name -> {
                val text = prefs.getString(KEY_TEXT, null)?.trim().orEmpty()
                clear(context)
                if (text.isEmpty()) Result.Cancelled else Result.Success(text)
            }

            Status.CANCELLED.name -> {
                clear(context)
                Result.Cancelled
            }

            else -> null
        }
    }

    fun clear(context: Context) {
        prefs(context).edit { clear() }
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}


