package com.rendox.shoppinggenius.data.grocery

import java.util.concurrent.TimeUnit

internal object CompletedGroceriesAutoDeletePolicy {
    fun cutoffTimestampMs(
        nowMs: Long,
        autoDeleteAfterHours: Int
    ): Long {
        val safeHours = autoDeleteAfterHours.coerceIn(1, 120)
        return nowMs - TimeUnit.HOURS.toMillis(safeHours.toLong())
    }
}


