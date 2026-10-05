package com.shoppinggenius.app.feature.carapp

import android.util.Log
import androidx.car.app.CarContext

/**
 * Hilfsklasse zur Verwaltung von Driver Distraction Restrictions.
 * Prüft, ob der Fahrer aktuell fährt und welche Funktionen eingeschränkt sind.
 *
 * Funktioniert mit androidx.car.app 1.4.0+
 */
class CarUxRestrictionsHelper(private val carContext: CarContext) {

    enum class DrivingState {
        PARKED,
        RESTRICTED,
        UNAVAILABLE
    }

    fun getDrivingState(): DrivingState =
        readDrivingState() ?: DrivingState.UNAVAILABLE

    /**
     * Prüft, ob aktuell der Driver Distraction Mode aktiv ist (Fahrer fährt).
     * @return true wenn Fahrer fährt (Restrictions aktiv), false wenn Auto steht oder der Host
     * keine Runtime-Abfrage der Restrictions erlaubt.
     */
    fun isDriverDistracted(): Boolean = getDrivingState() == DrivingState.RESTRICTED

    /**
     * Prüft, ob Input-Gesten (Touch/Tap) möglich sind.
     * Die eigentliche Touch-Blockierung sollte zusätzlich immer über
     * ParkedOnlyOnClickListener vom Car App Framework erfolgen.
     */
    fun canPerformClickActions(): Boolean = getDrivingState() != DrivingState.RESTRICTED

    /**
     * Beschreibung für UI zum Anzeigen des aktuellen Modus.
     */
    fun getRestrictionStatusMessage(): String = when (getDrivingState()) {
        DrivingState.RESTRICTED -> "🚗 Fahrtmodus: Nur Spracheingaben möglich"
        DrivingState.PARKED -> "🏁 Standmodus: Alle Funktionen verfügbar"
        DrivingState.UNAVAILABLE -> "🚘 Host steuert Einschränkungen selbst"
    }

    private fun readDrivingState(): DrivingState? {
        return withCurrentRestrictions { restrictions ->
            val optimizationMethod = restrictions.javaClass.methods.firstOrNull {
                it.name == "isRequiresDistractionOptimization" && it.parameterCount == 0
            } ?: return@withCurrentRestrictions null

            val isRestricted = optimizationMethod.invoke(restrictions) as? Boolean ?: return@withCurrentRestrictions null
            Log.d(TAG, "AAOS driving restrictions active=$isRestricted")
            if (isRestricted) DrivingState.RESTRICTED else DrivingState.PARKED
        }
    }

    private fun withCurrentRestrictions(block: (Any) -> DrivingState?): DrivingState? {
        return try {
            val carClass = Class.forName("android.car.Car")
            val createCarMethod = carClass.methods.firstOrNull {
                it.name == "createCar" && it.parameterCount == 1
            } ?: return null.also {
                Log.d(TAG, "android.car.Car#createCar(Context) not available")
            }

            val car = createCarMethod.invoke(null, carContext) ?: return null

            try {
                carClass.methods.firstOrNull {
                    it.name == "connect" && it.parameterCount == 0
                }?.invoke(car)

                val serviceName = carClass.getField("CAR_UX_RESTRICTION_SERVICE").get(null) as? String
                    ?: return null

                val manager = carClass.getMethod("getCarManager", String::class.java)
                    .invoke(car, serviceName) ?: return null

                val currentRestrictionsMethod = manager.javaClass.methods.firstOrNull {
                    it.parameterCount == 0 &&
                        (it.name == "getCurrentCarUxRestrictions" || it.name == "getCurrentRestrictions")
                } ?: return null.also {
                    Log.d(TAG, "CarUxRestrictionsManager current restriction API not available")
                }

                val restrictions = currentRestrictionsMethod.invoke(manager) ?: return null
                block(restrictions)
            } finally {
                carClass.methods.firstOrNull {
                    it.name == "disconnect" && it.parameterCount == 0
                }?.invoke(car)
            }
        } catch (t: Throwable) {
            Log.d(TAG, "No AAOS CarUxRestrictionsManager available on this host: ${t.message}")
            null
        }
    }

    private companion object {
        private const val TAG = "CarUxRestrictions"
    }
}

