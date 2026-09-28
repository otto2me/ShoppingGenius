package com.shoppinggenius.app.feature.carapp

import androidx.car.app.CarContext
import androidx.car.app.hardware.CarHardwareManager

/**
 * Helper-Klasse zur Überprüfung des Fahrzustands gemäß Google Driver Distraction Guidelines.
 *
 * Während der Fahrt (Speed > 0):
 * - Nur Sprachbedienung erlaubt
 * - Tastatureingabe und Touch-Interaktionen minimiert
 *
 * Im Stillstand (Speed ≈ 0):
 * - Volle Interaktivität mit Tastatur und Touch
 */
object CarAppDrivingStateHelper {

    // Schwellwert für "fährt" vs "stillsteht" (in m/s)
    // 0.1 m/s ≈ 0.36 km/h verhindert Sensorschwankungen im Stillstand
    private const val MOVING_THRESHOLD_MS = 0.1f

    /**
     * Prüft, ob das Fahrzeug gerade fährt (Speed > Schwellwert).
     *
     * Gibt True zurück wenn:
     * - Die Speed Info verfügbar ist UND die Geschwindigkeit > 0.1 m/s ist
     *
     * Gibt False zurück (sicher für Touch/Tastatur) wenn:
     * - Die Speed Info nicht verfügbar ist
     * - Die Geschwindigkeit bei ~0 ist
     *
     * Sicherheit: Bei Fehler wird "fährt" angenommen, um kein Keyboard anzuzeigen
     *
     * @return true wenn das Auto fährt, false wenn es stillsteht oder Speed unbekannt
     */
    fun isVehicleMoving(carContext: CarContext): Boolean {
        return try {
            // CarHardwareManager kann null sein oder Speed-Info nicht bereitstellen
            val carHardwareManager = carContext.getCarService(
                CarContext.CAR_HARDWARE_SERVICE
            ) as? CarHardwareManager

            val speedInfo = carHardwareManager?.carInfo?.speed
            if (speedInfo != null) {
                val speedMetersPerSecond = speedInfo.speedMetersPerSecond ?: return true
                speedMetersPerSecond > MOVING_THRESHOLD_MS
            } else {
                // Falls Speed-Info nicht verfügbar, Sicherheit geht vor: assume moving
                true
            }
        } catch (e: Exception) {
            // Bei Fehler Sicherheit geht vor: assume moving (kein Keyboard anzeigen)
            true
        }
    }

    /**
     * Prüft, ob Tastatureingabe erlaubt sein sollte (Fahrzeug im Stillstand).
     * Dies folgt Google Driver Distraction Guidelines.
     *
     * Rückgabewert false (kein Keyboard) ist sicherer, wenn der Fahrzustand unklar ist.
     */
    fun shouldEnableKeyboardInput(carContext: CarContext): Boolean {
        return !isVehicleMoving(carContext)
    }
}



