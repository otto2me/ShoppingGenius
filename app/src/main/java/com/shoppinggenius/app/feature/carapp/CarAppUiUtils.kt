package com.shoppinggenius.app.feature.carapp

import androidx.annotation.DrawableRes
import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.model.CarIcon
import androidx.core.graphics.drawable.IconCompat

internal fun CarContext.defaultCarIcon(@DrawableRes drawableRes: Int): CarIcon =
    CarIcon.Builder(IconCompat.createWithResource(this, drawableRes)).build()

internal fun CarContext.showCarToast(
    message: CharSequence,
    duration: Int = CarToast.LENGTH_SHORT
) {
    CarToast.makeText(this, message, duration).show()
}

internal fun CarContext.showDrivingRestrictedToast(
    message: CharSequence = "This action is not available while driving."
) {
    showCarToast(message)
}


