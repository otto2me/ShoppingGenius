package com.rendox.shoppinggenius.data.grocery

import com.google.common.truth.Truth.assertThat
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Test

class CompletedGroceriesAutoDeletePolicyTest {

    @Test
    fun `cutoff subtracts configured hours from current time`() {
        val nowMs = 10_000_000L

        val cutoff = CompletedGroceriesAutoDeletePolicy.cutoffTimestampMs(
            nowMs = nowMs,
            autoDeleteAfterHours = 3
        )

        assertThat(cutoff).isEqualTo(nowMs - TimeUnit.HOURS.toMillis(3))
    }

    @Test
    fun `cutoff clamps hours below allowed range`() {
        val nowMs = 10_000_000L

        val cutoff = CompletedGroceriesAutoDeletePolicy.cutoffTimestampMs(
            nowMs = nowMs,
            autoDeleteAfterHours = 0
        )

        assertThat(cutoff).isEqualTo(nowMs - TimeUnit.HOURS.toMillis(1))
    }

    @Test
    fun `cutoff clamps hours above allowed range`() {
        val nowMs = 10_000_000L

        val cutoff = CompletedGroceriesAutoDeletePolicy.cutoffTimestampMs(
            nowMs = nowMs,
            autoDeleteAfterHours = 999
        )

        assertThat(cutoff).isEqualTo(nowMs - TimeUnit.HOURS.toMillis(120))
    }
}


