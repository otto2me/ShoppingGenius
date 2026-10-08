package com.shoppinggenius.app.feature.carapp

import androidx.car.app.CarContext
import androidx.car.app.annotations.RequiresCarApi
import androidx.car.app.hardware.CarHardwareManager
import androidx.car.app.hardware.common.OnCarDataAvailableListener
import androidx.car.app.hardware.info.Speed
import java.util.concurrent.Executor

/**
 * Helper-Klasse zur Überprüfung des Fahrzustands gemäß Google Driver Distraction Guidelines.
 *
 * Während der Fahrt (Speed > 0):
 * - Nur Sprachbedienung erlaubt
 * - Tastatureingabe sollte minimiert werden
 *
 * Im Stillstand (Speed ≈ 0):
 * - Volle Interaktivität mit Tastatur und Touch
 *
 * Speed-Daten werden asynchron über OnCarDataAvailableListener bereitgestellt.
 * Diese Klasse speichert den letzten bekannten Speed und nutzt ihn für Entscheidungen.
 */
object CarAppDrivingStateHelper {

    // Schwellwert für "fährt" vs "stillsteht" (in m/s)
    // 0.1 m/s ≈ 0.36 km/h verhindert Sensorschwankungen im Stillstand
    private const val MOVING_THRESHOLD_MS = 0.1f

    // Letzter bekannter Speed (asynchron aktualisiert)
    // Fallback: null = unbekannt = konservativ annehmen "fährt"
    @Volatile
    private var lastKnownSpeedMetersPerSecond: Float? = null

    /**
     * Initialisiert den Speed-Listener (optional, für volle DDG-Compliance).
     *
     * Sollte beim Screen-Lifecycle aufgerufen werden, um Speed-Updates zu bekommen.
     * Dies ist optional - ohne Listener wird "konservativ" angenommen (kein Keyboard).
     *
     * @param carContext: Kontext um CarHardwareManager zu bekommen
     * @param executor: Executor für asynchrone Listener-Callbacks
     */
    @RequiresCarApi(3)
    fun initializeSpeedListener(carContext: CarContext, executor: Executor) {
        try {
            val carHardwareManager = carContext.getCarService(
                CarHardwareManager::class.java
            )

            // Speed-Listener registrieren mit OnCarDataAvailableListener Implementierung
            val speedListener = OnCarDataAvailableListener<Speed> { speed ->
                val displaySpeed = speed.displaySpeedMetersPerSecond.value
                lastKnownSpeedMetersPerSecond = displaySpeed?.toFloat()
            }

            carHardwareManager.carInfo.addSpeedListener(executor, speedListener)
        } catch (e: Exception) {
            // Speed nicht verfügbar oder Error - bleibt null
            lastKnownSpeedMetersPerSecond = null
        }
    }

    /**
     * Prüft, ob Tastatureingabe erlaubt sein sollte (Fahrzeug im Stillstand).
     *
     * Dies folgt Google Driver Distraction Guidelines:
     * - Während der Fahrt (Speed > 0.1 m/s): false (keine Tastatureingabe)
     * - Im Stillstand (Speed ≤ 0.1 m/s): true (Tastatureingabe möglich)
     * - Speed unbekannt (kein Listener): false (sicher: kein Keyboard)
     *
     * Die Android-Auto-Host-App kann Tastatureinput trotzdem blockieren
     * bei hoher Geschwindigkeit (CAN-Bus Check).
     */
    fun shouldEnableKeyboardInput(): Boolean {
        // Wenn kein Speed bekannt: Fallback zu false (fahren annahmen)
        // Dies ist konservativ, aber sicherer für DDG
        val speed = lastKnownSpeedMetersPerSecond ?: return false

        // Während Fahrt: false (kein Keyboard)
        // Im Stillstand: true (Keyboard erlaubt)
        return speed <= MOVING_THRESHOLD_MS
    }

    /**
     * Setzt den Speed manuell (für Tests oder wenn Listener nicht genutzt wird).
     */
    fun setManualSpeed(speedMetersPerSecond: Float) {
        lastKnownSpeedMetersPerSecond = speedMetersPerSecond
    }

    /**
     * Setzt den Speed zurück auf null (unbekannt).
     */
    fun resetSpeed() {
        lastKnownSpeedMetersPerSecond = null
    }
}



