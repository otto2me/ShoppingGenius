package com.shoppinggenius.app.feature.carapp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

class CarVoiceInputCaptureActivity : ComponentActivity() {

    private val voiceInputLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        val requestId = intent.getStringExtra(EXTRA_REQUEST_ID)
        if (requestId != null) {
            if (activityResult.resultCode == RESULT_OK) {
                val spokenText = activityResult.data
                    ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    ?.firstOrNull()
                    ?.trim()

                if (!spokenText.isNullOrEmpty()) {
                    CarVoiceInputStore.completeSuccess(this, requestId, spokenText)
                } else {
                    CarVoiceInputStore.completeCancelled(this, requestId)
                }
            } else {
                CarVoiceInputStore.completeCancelled(this, requestId)
            }
        }
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val requestId = intent.getStringExtra(EXTRA_REQUEST_ID)
        if (requestId.isNullOrBlank()) {
            finish()
            return
        }

        val prompt = intent.getStringExtra(EXTRA_PROMPT).orEmpty()
        val recognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            if (prompt.isNotBlank()) {
                putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
            }
        }

        try {
            voiceInputLauncher.launch(recognitionIntent)
        } catch (_: ActivityNotFoundException) {
            CarVoiceInputStore.completeCancelled(this, requestId)
            finish()
        }
    }

    companion object {
        private const val EXTRA_REQUEST_ID = "extra_request_id"
        private const val EXTRA_PROMPT = "extra_prompt"

        fun createIntent(
            context: android.content.Context,
            requestId: String,
            prompt: String
        ): Intent = Intent(context, CarVoiceInputCaptureActivity::class.java).apply {
            putExtra(EXTRA_REQUEST_ID, requestId)
            putExtra(EXTRA_PROMPT, prompt)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}


