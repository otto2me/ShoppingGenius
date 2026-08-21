package com.rendox.shoppinggenius.model

import androidx.core.os.LocaleListCompat
import java.util.Locale

object AppLanguage {
    val supportedLanguageTags = listOf(
        "en",
        "af-ZA",
        "ar-SA",
        "ca-ES",
        "cs-CZ",
        "da-DK",
        "de-DE",
        "el-GR",
        "es-ES",
        "fi-FI",
        "fr-FR",
        "he-IL",
        "hu-HU",
        "it-IT",
        "ja-JP",
        "ko-KR",
        "nl-NL",
        "no-NO",
        "pl-PL",
        "pt-BR",
        "pt-PT",
        "ro-RO",
        "ru-RU",
        "sr-SP",
        "sv-SE",
        "tr-TR",
        "uk-UA",
        "vi-VN",
        "zh-CN",
        "zh-TW"
    )

    fun toLocaleListCompat(languageTag: String?): LocaleListCompat = if (languageTag.isNullOrBlank()) {
        LocaleListCompat.getEmptyLocaleList()
    } else {
        LocaleListCompat.forLanguageTags(languageTag)
    }

    fun resolveAssetLanguageTag(languageTag: String?): String {
        val candidateLocale = normalizeLocale(languageTag) ?: Locale.getDefault()
        return supportedLanguageTags.firstOrNull { supportedLanguageTag ->
            val supportedLocale = Locale.forLanguageTag(supportedLanguageTag)
            candidateLocale.language.equals(supportedLocale.language, ignoreCase = true) &&
                candidateLocale.country.equals(supportedLocale.country, ignoreCase = true)
        } ?: supportedLanguageTags.firstOrNull { supportedLanguageTag ->
            val supportedLocale = Locale.forLanguageTag(supportedLanguageTag)
            candidateLocale.language.equals(supportedLocale.language, ignoreCase = true)
        } ?: "en"
    }

    fun displayName(
        languageTag: String,
        displayLocale: Locale = Locale.getDefault()
    ): String {
        val locale = Locale.forLanguageTag(languageTag)
        return locale.getDisplayName(displayLocale).replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase(displayLocale) else char.toString()
        }
    }

    private fun normalizeLocale(languageTag: String?): Locale? {
        val normalizedTag = languageTag?.takeIf { it.isNotBlank() } ?: return null
        return Locale.forLanguageTag(normalizedTag)
    }
}
