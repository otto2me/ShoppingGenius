package com.shoppinggenius.app.feature.carapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat

internal class CarDirectVoiceRecognizer(
    private val context: Context
) {

    enum class StartResult {
        STARTED,
        PERMISSION_REQUIRED,
        UNAVAILABLE,
        ALREADY_RUNNING,
        FAILED
    }

    private var speechRecognizer: SpeechRecognizer? = null

    fun start(
        prompt: String,
        onReady: () -> Unit = {},
        onListening: () -> Unit = {},
        onResult: (String) -> Unit,
        onCancelled: () -> Unit
    ): StartResult {
        if (speechRecognizer != null) return StartResult.ALREADY_RUNNING
        if (!SpeechRecognizer.isRecognitionAvailable(context)) return StartResult.UNAVAILABLE
        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return StartResult.PERMISSION_REQUIRED
        }

        return runCatching {
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer = recognizer
            recognizer.setRecognitionListener(
                object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        onReady()
                    }

                    override fun onBeginningOfSpeech() {
                        onListening()
                    }

                    override fun onRmsChanged(rmsdB: Float) = Unit

                    override fun onBufferReceived(buffer: ByteArray?) = Unit

                    override fun onEndOfSpeech() = Unit

                    override fun onError(error: Int) {
                        cleanup()
                        onCancelled()
                    }

                    override fun onResults(results: Bundle?) {
                        val spokenText = results
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()
                            ?.trim()
                            .orEmpty()

                        cleanup()
                        if (spokenText.isNotEmpty()) {
                            onResult(spokenText)
                        } else {
                            onCancelled()
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) = Unit

                    override fun onEvent(eventType: Int, params: Bundle?) = Unit
                }
            )

            recognizer.startListening(
                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
                    if (prompt.isNotBlank()) {
                        putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
                    }
                }
            )

            StartResult.STARTED
        }.getOrElse {
            cleanup()
            StartResult.FAILED
        }
    }

    fun cancel() {
        speechRecognizer?.cancel()
        cleanup()
    }

    private fun cleanup() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}

