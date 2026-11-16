package br.com.sensorauto.ui.theme

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.os.LocaleListCompat

/** Persistent language selection. Applies immediately via AppCompat per-app locales. */
class LocalePreferences(context: Context) {
    private val preferences = context.applicationContext
        .getSharedPreferences("locale", Context.MODE_PRIVATE)

    var languageTag by mutableStateOf(preferences.getString("lang", "pt-BR") ?: "pt-BR")
        private set

    init {
        apply(languageTag)
    }

    fun update(tag: String) {
        preferences.edit().putString("lang", tag).apply()
        languageTag = tag
        apply(tag)
    }

    private fun apply(tag: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
    }
}

val LocalLocalePreferences = staticCompositionLocalOf<LocalePreferences> {
    error("LocalePreferences must be provided by MainActivity")
}
