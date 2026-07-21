package com.rendox.grocerygenius.feature.iconpicker

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class DuckDuckGoImageSearchServiceTest {

    @Test
    fun `extractDuckDuckGoVqd extracts token from html`() {
        val html = """
            <html>
              <head><script>var config = {vqd="4-1234567890"};</script></head>
              <body></body>
            </html>
        """.trimIndent()

        val token = extractDuckDuckGoVqd(html)

        assertThat(token).isEqualTo("4-1234567890")
    }
}


